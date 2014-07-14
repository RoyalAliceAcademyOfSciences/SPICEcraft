package simElectricity.EnergyNet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import simElectricity.API.EnergyTile.*;
import simElectricity.API.EnergyTile.ITransformer.ITransformerWinding;
import simElectricity.API.Util;
import simElectricity.ConfigManager;

import java.util.*;

public final class EnergyNet {
    // private WeightedMultigraph<IBaseComponent, Resistor> tileEntityGraph =
    // new WeightedMultigraph<IBaseComponent, Resistor>(Resistor.class);
    private SimpleGraph<IBaseComponent, DefaultEdge> tileEntityGraph = new SimpleGraph<IBaseComponent, DefaultEdge>(DefaultEdge.class);
    public Map<IBaseComponent, Float> voltageCache = new HashMap<IBaseComponent, Float>();
    /**
     * A flag for recalculate the energynet
     */
    private boolean calc = false;

    //Optimization--------------------------------------------------------------------
    private boolean nodeIsLine(IBaseComponent conductor, SimpleGraph<IBaseComponent, DefaultEdge> optimizedTileEntityGraph) {
        if (conductor.getClass() == VirtualConductor.class)
            return false;
        if (!(conductor instanceof IConductor))
            return false;
        if (VirtualConductor.conductorInVirtual((IConductor) conductor))
            return false;

        List<IBaseComponent> list = Graphs.neighborListOf(optimizedTileEntityGraph, conductor);
        for (IBaseComponent iBaseComponent : list) {
            if (!(iBaseComponent instanceof IConductor))
                return false;
        }

        return list.size() == 2;
    }

    private VirtualConductor floodFill(IBaseComponent conductor, VirtualConductor virtualConductor, SimpleGraph<IBaseComponent, DefaultEdge> optimizedTileEntityGraph) {
        if (nodeIsLine(conductor, optimizedTileEntityGraph)) {
            if (virtualConductor == null)
                virtualConductor = new VirtualConductor();
            virtualConductor.append((IConductor) conductor);
            List<IBaseComponent> neighborList = Graphs.neighborListOf(optimizedTileEntityGraph, conductor);
            for (IBaseComponent iBaseComponent : neighborList) {
                floodFill(iBaseComponent, virtualConductor, optimizedTileEntityGraph);
            }
        } else if (virtualConductor != null) {
            virtualConductor.appendConnection(conductor);
        }

        return virtualConductor;
    }

    private boolean mergeIConductorNode(SimpleGraph<IBaseComponent, DefaultEdge> optimizedTileEntityGraph) {
        boolean result = false;
        VirtualConductor virtualConductor = null;

        Set<IBaseComponent> iBaseComponentSet = optimizedTileEntityGraph.vertexSet();
        for (IBaseComponent iBaseComponent : iBaseComponentSet) {
            virtualConductor = floodFill(iBaseComponent, virtualConductor, optimizedTileEntityGraph);

            if (virtualConductor != null) {
                break;
            }
        }

        if (virtualConductor != null) {
            optimizedTileEntityGraph.addVertex(virtualConductor);
            optimizedTileEntityGraph.addEdge(virtualConductor, virtualConductor.getConnection(0));
            optimizedTileEntityGraph.addEdge(virtualConductor, virtualConductor.getConnection(1));

            for (IConductor conductor : VirtualConductor.allConductorInVirtual())
                optimizedTileEntityGraph.removeVertex(conductor);

            result = true;
        }

        return result;
    }

    //Simulator------------------------------------------------------------------------
    private void runSimulator() {
        SimpleGraph<IBaseComponent, DefaultEdge> optimizedTileEntityGraph = (SimpleGraph<IBaseComponent, DefaultEdge>) tileEntityGraph.clone();

        //try to optimization
        if (ConfigManager.optimizeNodes) {
            System.out.printf("raw:%d nodes\n", optimizedTileEntityGraph.vertexSet().size());
            VirtualConductor.mapClear();
            while (mergeIConductorNode(optimizedTileEntityGraph)) ;
            System.out.printf("optimized:%d nodes\n", optimizedTileEntityGraph.vertexSet().size());
        }


        List<IBaseComponent> unknownVoltageNodes = new ArrayList<IBaseComponent>();
        unknownVoltageNodes.addAll(optimizedTileEntityGraph.vertexSet());

        int matrixSize = unknownVoltageNodes.size();

        float[][] A = new float[matrixSize][matrixSize];
        float[] b = new float[matrixSize];

        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            IBaseComponent currentRowComponent = unknownVoltageNodes.get(rowIndex);

            //Add fixed voltage sources
            if (currentRowComponent instanceof ICircuitComponent) {
                //Possible voltage source, getOutputVoltage()=0 for sinks, getOutputVoltage>0 for sources
                b[rowIndex] = ((ICircuitComponent) currentRowComponent).getOutputVoltage()
                        / currentRowComponent.getResistance();
            } else {
                //Normal conductor nodes
                b[rowIndex] = 0;
            }

            List<IBaseComponent> neighborList = Graphs.neighborListOf(optimizedTileEntityGraph, currentRowComponent);
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                float cellData = 0;

                if (rowIndex == columnIndex) { //Key cell
                    //Add neighbor resistance
                    for (IBaseComponent neighbor : neighborList) {
                        cellData += 1.0 / (getResistance(currentRowComponent) + getResistance(neighbor));    //Normally either of them will be 0
                    }

                    //Add internal resistance for fixed voltage sources
                    if (currentRowComponent instanceof ICircuitComponent)
                        cellData += 1.0 / currentRowComponent.getResistance();

                    //Add induction to transformers
                    if (currentRowComponent instanceof ITransformerWinding) {
                        ITransformerWinding winding = (ITransformerWinding) currentRowComponent;
                        if (winding.isPrimary()) {
                            cellData += winding.getRatio() * winding.getRatio() / winding.getResistance();
                        } else {
                            cellData += 1.0 / winding.getResistance();
                        }
                    }


                } else {  //Normal cells
                    IBaseComponent currentColumnComponent = unknownVoltageNodes.get(columnIndex);
                    if (neighborList.contains(currentColumnComponent)) {   //Add neighbor's resistance (-Gi)
                        cellData = (float) (-1.0 / (getResistance(currentRowComponent) + getResistance(currentColumnComponent)));

                    } else if (currentRowComponent instanceof ITransformerWinding) { //Add transformer association
                        ITransformerWinding winding = (ITransformerWinding) currentRowComponent;
                        ITransformer core = winding.getCore();

                        if ((winding.isPrimary() && core.getSecondary() == currentColumnComponent) ||
                                ((!winding.isPrimary()) && core.getPrimary() == currentColumnComponent))
                            cellData = -winding.getRatio() / winding.getResistance();
                    }
                }

                A[rowIndex][columnIndex] = cellData;
            }
        }

        float[] x = MatrixOperation.lsolve(A, b);

        voltageCache.clear();
        for (int i = 0; i < x.length; i++) {
            voltageCache.put(unknownVoltageNodes.get(i), x[i]);
        }
    }

    private static float getResistance(IBaseComponent te) {
        if (te instanceof IConductor)
            return te.getResistance() / 2;
        return 0;
    }
    /*End of Simulator*/


    /**
     * Called in each tick to attempt to do calculation
     */
    public static void onTick(World world) {
        EnergyNet energyNet = getForWorld(world);
        //energyNet.calc = true;
        if (energyNet.calc) {
            energyNet.calc = false;
            energyNet.runSimulator();

            //Check power distribution
            try {
                for (IBaseComponent tile : energyNet.tileEntityGraph.vertexSet()) {
                    if (tile instanceof ICircuitComponent) {
                        ICircuitComponent te = (ICircuitComponent) tile;
                        if (te.getMaxSafeVoltage() != 0 && te.getMaxSafeVoltage() < energyNet.voltageCache.get(tile))
                            te.onOverVoltage(); //Over voltage check
                    } else if (tile instanceof IConductor) {
                        IConductor te = (IConductor) tile;
                        if (te.getInsulationBreakdownVoltage() != 0 && te.getInsulationBreakdownVoltage() < energyNet.voltageCache.get(tile))
                            te.onInsulationBreakdown();
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    //Editing of the jGraph--------------------------------------------------------------------------------

    /**
     * Internal use only, return a list containing neighbor TileEntities (Just for IBaseComponent)
     */
    private static List<IBaseComponent> neighborListOf(TileEntity te) {
        List<IBaseComponent> result = new ArrayList<IBaseComponent>();
        TileEntity temp;

        if (te instanceof IConductor) {
            ForgeDirection[] directions = new ForgeDirection[6];
            directions[0] = ForgeDirection.EAST;
            directions[1] = ForgeDirection.WEST;
            directions[2] = ForgeDirection.UP;
            directions[3] = ForgeDirection.DOWN;
            directions[4] = ForgeDirection.SOUTH;
            directions[5] = ForgeDirection.NORTH;


            for (int i = 0; i < 6; i++) {
                temp = Util.getTEonDirection(te, directions[i]);
                if (temp instanceof IConductor) {  //Conductor
                    result.add((IConductor) temp);
                } else if (temp instanceof IEnergyTile) {   //IEnergyTile
                    if (((IEnergyTile) temp).getFunctionalSide() == directions[i].getOpposite())
                        result.add((IEnergyTile) temp);
                } else if (temp instanceof IComplexTile) {  //IComplexTile
                    if (((IComplexTile) temp).getCircuitComponent(directions[i].getOpposite()) != null)
                        result.add(((IComplexTile) temp).getCircuitComponent(directions[i].getOpposite()));
                } else if (temp instanceof ITransformer) {
                    if (((ITransformer) temp).getPrimarySide() == directions[i].getOpposite())
                        result.add(((ITransformer) temp).getPrimary());

                    if (((ITransformer) temp).getSecondarySide() == directions[i].getOpposite())
                        result.add(((ITransformer) temp).getSecondary());
                }
            }
        }


        if (te instanceof IEnergyTile) {
            temp = Util.getTEonDirection(te, ((IEnergyTile) te).getFunctionalSide());

            if (temp instanceof IConductor) {
                result.add((IBaseComponent) temp);
            }
        }

        return result;
    }

    /**
     * Add a TileEntity to the energynet
     */
    public void addTileEntity(TileEntity te) {
        if (te instanceof IComplexTile) {      //IComplexTile
            IComplexTile ct = ((IComplexTile) te);

            ICircuitComponent SubComponent;
            TileEntity neighbor;

            ForgeDirection[] directions = new ForgeDirection[6];
            directions[0] = ForgeDirection.EAST;
            directions[1] = ForgeDirection.WEST;
            directions[2] = ForgeDirection.UP;
            directions[3] = ForgeDirection.DOWN;
            directions[4] = ForgeDirection.SOUTH;
            directions[5] = ForgeDirection.NORTH;

            for (int i = 0; i < 6; i++) {
                SubComponent = ct.getCircuitComponent(directions[i]);

                if (SubComponent instanceof IBaseComponent) {
                    if (!tileEntityGraph.containsVertex(SubComponent))    //If the subComponent haven't been added, add it!
                        tileEntityGraph.addVertex(SubComponent);

                    neighbor = Util.getTEonDirection(te, directions[i]);

                    if (neighbor instanceof IConductor) {                //Connected properly
                        if (!tileEntityGraph.containsVertex((IConductor) neighbor))
                            tileEntityGraph.addVertex((IConductor) neighbor);
                        tileEntityGraph.addEdge(SubComponent, (IConductor) neighbor); //Add association
                    }
                }
            }

        } else if (te instanceof ITransformer) {         //Transformer
            ITransformer transformer = ((ITransformer) te);
            ITransformerWinding primary = ((ITransformer) te).getPrimary();
            ITransformerWinding secondary = ((ITransformer) te).getSecondary();

            TileEntity neighbor;

            //Add primary
            if (!tileEntityGraph.containsVertex(primary))
                tileEntityGraph.addVertex(primary);

            neighbor = Util.getTEonDirection(te, transformer.getPrimarySide());
            if (neighbor instanceof IConductor) {
                if (!tileEntityGraph.containsVertex((IBaseComponent) neighbor))
                    tileEntityGraph.addVertex((IBaseComponent) neighbor);
                tileEntityGraph.addEdge(primary, (IBaseComponent) neighbor);
            }

            //Add secondary
            if (!tileEntityGraph.containsVertex(secondary))
                tileEntityGraph.addVertex(secondary);

            neighbor = Util.getTEonDirection(te, transformer.getSecondarySide());
            if (neighbor instanceof IConductor) {
                if (!tileEntityGraph.containsVertex((IBaseComponent) neighbor))
                    tileEntityGraph.addVertex((IBaseComponent) neighbor);
                tileEntityGraph.addEdge(secondary, (IBaseComponent) neighbor);
            }

        } else {  //IBaseComponent and IConductor
            List<IBaseComponent> neighborList = neighborListOf(te);

            if (!tileEntityGraph.containsVertex((IBaseComponent) te))
                tileEntityGraph.addVertex((IBaseComponent) te);

            for (IBaseComponent tileEntity : neighborList) {
                if (!tileEntityGraph.containsVertex(tileEntity))
                    tileEntityGraph.addVertex(tileEntity);
                tileEntityGraph.addEdge((IBaseComponent) te, tileEntity);
            }
        }
        calc = true;
    }

    /**
     * Remove a TileEntiy from the energy net
     */
    public void removeTileEntity(TileEntity te) {
        if (te instanceof IComplexTile) { //For a comlexTile every subComponents has to be removed!
            ICircuitComponent[] SubComponents = new ICircuitComponent[6];
            SubComponents[0] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.NORTH);
            SubComponents[1] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.SOUTH);
            SubComponents[2] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.EAST);
            SubComponents[3] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.WEST);
            SubComponents[4] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.UP);
            SubComponents[5] = ((IComplexTile) te).getCircuitComponent(ForgeDirection.DOWN);

            for (int i = 0; i < 6; i++) {
                if (SubComponents[i] instanceof IBaseComponent)
                    tileEntityGraph.removeVertex(SubComponents[i]);
            }
        } else if (te instanceof ITransformer) {
            tileEntityGraph.removeVertex(((ITransformer) te).getPrimary());
            tileEntityGraph.removeVertex(((ITransformer) te).getSecondary());
        } else {  //IBaseComponent and IConductor
            tileEntityGraph.removeVertex((IBaseComponent) te);
        }
        calc = true;
    }

    /**
     * Refresh a node information for a tile which ALREADY attached to the energy network
     */
    public void rejoinTileEntity(TileEntity te) {
        removeTileEntity(te);
        addTileEntity(te);
    }

    /**
     * Mark the energy net for updating in next tick
     */
    public void markForUpdate(TileEntity te) {
        calc = true;
    }

    /**
     * Return a instance of energynet for a specific world
     */
    public static EnergyNet getForWorld(World world) {
        WorldData worldData = WorldData.get(world);
        return worldData.energyNet;
    }

    /**
     * Creation of the energy network
     */
    public EnergyNet() {
        System.out.println("EnergyNet create");
    }
}
