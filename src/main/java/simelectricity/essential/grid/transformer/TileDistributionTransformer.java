package simelectricity.essential.grid.transformer;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelDataMap;
import rikka.librikka.Utils;
import rikka.librikka.multiblock.IMultiBlockTile;
import simelectricity.api.SEAPI;
import simelectricity.api.node.ISEGridNode;
import simelectricity.essential.client.grid.PowerPoleRenderHelper;
import simelectricity.essential.grid.TileCableJoint;

public abstract class TileDistributionTransformer extends SEMultiBlockGridTile{	
	protected BlockPos accessory;
	
	protected abstract boolean acceptAccessory(TileEntity accessory);
		
	@Override
    protected void collectModelData(ModelDataMap.Builder builder) {
		builder.withInitial(IMultiBlockTile.prop, this);
    }
	
	@Override
	@OnlyIn(Dist.CLIENT)
    public BlockPos getAccessoryPos() {
		return accessory;
	}
	
    @Override
    public boolean canConnect(@Nullable BlockPos to) {
    	if (to == null)
    		return this.neighbor == null || this.accessory == null;
    	
    	TileEntity te = world.getTileEntity(to);
    	if (acceptAccessory(te)) {
    		return this.accessory == null;
    	} else {
    		return this.neighbor == null;
    	}
    }
    
    @Override
    public void onGridNeighborUpdated() {
    	this.neighbor = null;
        this.accessory = null;
        
        if (this.mbInfo == null)
        	return;
        
    	BlockPos complementPos = getComplementPos();
    	for (ISEGridNode node: this.gridNode.getNeighborList()) {
    		BlockPos pos = node.getPos();
    		if (pos.equals(complementPos))
    			continue;
    		
    		TileEntity tile = world.getTileEntity(node.getPos());
        	if (acceptAccessory(tile)) 
        		this.accessory = node.getPos();
        	else
        		this.neighbor = node.getPos();
    	}

        markTileEntityForS2CSync();
    }
	
    /////////////////////////////////////////////////////////
    ///Sync
    /////////////////////////////////////////////////////////
    @Override
    public void prepareS2CPacketData(CompoundNBT nbt) {
        super.prepareS2CPacketData(nbt);
        Utils.saveToNbt(nbt, "accessory", this.accessory);
    }

    @Override
	@OnlyIn(Dist.CLIENT)
    public void onSyncDataFromServerArrived(CompoundNBT nbt) {
		this.accessory = Utils.posFromNbt(nbt, "accessory");
        super.onSyncDataFromServerArrived(nbt);
    }
    
	protected abstract BlockPos getComplementPos();
	
	public static class Pole415V extends TileDistributionTransformer {
		public final static Vector3i rightPos = new Vector3i(5, 4, 0);
		public final static Vector3i leftPos = new Vector3i(0, 4, 0);
		
		@Override
		protected boolean acceptAccessory(TileEntity accessory) {
			return accessory instanceof TileCableJoint.Type415V;
		}
		
		@Override
		protected void onStructureCreating() {
	        gridNode = SEAPI.energyNetAgent.newGridNode(this.pos, 4);
	        SEAPI.energyNetAgent.attachGridNode(this.world, this.gridNode);
		}
		
		@Override
		public void onStructureCreated() {
			if (this.mbInfo.isPart(Pole415V.rightPos)) {
				BlockPos compPos = this.mbInfo.getPartPos(Pole415V.leftPos);
				Pole415V comp = (Pole415V) this.world.getTileEntity(compPos);
				SEAPI.energyNetAgent.connectGridNode(this.world, gridNode, comp.gridNode, 0.1F);
			}
		}

		@Override
		protected BlockPos getComplementPos() {
			if (mbInfo.isPart(rightPos))
				return mbInfo.getPartPos(leftPos);
			else
				return mbInfo.getPartPos(rightPos);
		}
		
        @Override
        @OnlyIn(Dist.CLIENT)
        protected PowerPoleRenderHelper createRenderHelper() {
            PowerPoleRenderHelper helper = new PowerPoleRenderHelper(this.pos, PowerPoleRenderHelper.facing2rotation(mbInfo.facing) - 2, mbInfo.mirrored, 1, 4);
            helper.addInsulatorGroup(0, 0.55F, 0,
                    helper.createInsulator(0, 1.2F, -0.9F, 0.3F, 0),
                    helper.createInsulator(0, 1.2F, -0.45F, 0.3F, 0),
                    helper.createInsulator(0, 1.2F, 0.45F, 0.3F, 0),
                    helper.createInsulator(0, 1.2F, 0.9F, 0.3F, 0)
            );
            return helper;
        }
	}
	
	public static class Pole10kV extends TileDistributionTransformer {
		public final static Vector3i rightPos = new Vector3i(5, 6, 0);
		public final static Vector3i leftPos = new Vector3i(0, 6, 0);
		
		@Override
		protected boolean acceptAccessory(TileEntity accessory) {
			return accessory instanceof TileCableJoint.Type10kV;
		}
		
		@Override
		protected void onStructureCreating() {
	        gridNode = SEAPI.energyNetAgent.newGridNode(this.pos, 3);
	        SEAPI.energyNetAgent.attachGridNode(this.world, this.gridNode);
		}
		
		@Override
		public void onStructureCreated() {
			if (this.mbInfo.isPart(Pole10kV.rightPos)) {
				BlockPos secPos = this.mbInfo.getPartPos(Pole415V.rightPos);
				Pole415V secondaryTile = (Pole415V) this.world.getTileEntity(secPos);
				SEAPI.energyNetAgent.makeTransformer(this.world, gridNode, secondaryTile.gridNode, 1, 415F / 10000F);
				
				BlockPos compPos = this.mbInfo.getPartPos(Pole10kV.leftPos);
				Pole10kV comp = (Pole10kV) this.world.getTileEntity(compPos);
				SEAPI.energyNetAgent.connectGridNode(this.world, gridNode, comp.gridNode, 0.1F);
			}
		}
		
		@Override
		protected BlockPos getComplementPos() {
			if (mbInfo.isPart(rightPos))
				return mbInfo.getPartPos(leftPos);
			else
				return mbInfo.getPartPos(rightPos);
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		protected PowerPoleRenderHelper createRenderHelper() {
            PowerPoleRenderHelper helper = new PowerPoleRenderHelper(pos, PowerPoleRenderHelper.facing2rotation(mbInfo.facing) - 2, mbInfo.mirrored, 1, 3);
            helper.addInsulatorGroup(0, 0.5F, 0,
                    helper.createInsulator(0, 1.2F, -0.74F, 0.55F, 0),
                    helper.createInsulator(0, 1.2F, 0, 1.5F, 0),
                    helper.createInsulator(0, 1.2F, 0.74F, 0.55F, 0)
            );
            return helper;
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
	    public BlockPos getAccessoryPos() {
			return accessory;
		}
	}

    @Override
    public void onStructureRemoved() {
        SEAPI.energyNetAgent.detachGridNode(this.world, this.gridNode);
    }
}
