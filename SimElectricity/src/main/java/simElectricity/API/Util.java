package simElectricity.API;

import simElectricity.EnergyNet;
import simElectricity.mod_SimElectricity;
import simElectricity.Network.PacketTileEntityFieldUpdate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

public class Util {
	public static ForgeDirection getPlayerSight(EntityLivingBase player){
        int heading = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int pitch = Math.round(player.rotationPitch);
        
        if (pitch >= 65)
        	return ForgeDirection.DOWN;
        
        if (pitch<=-65)
        	return ForgeDirection.UP;
        
        switch (heading){
        case 2:
        	return ForgeDirection.NORTH;
        case 0:
        	return ForgeDirection.SOUTH;
        case 1:
        	return ForgeDirection.WEST;
        case 3:
        	return ForgeDirection.EAST;
        default:
        	return null;
        }
	}
	
	public static ForgeDirection byte2Direction(byte byteData){
        switch (byteData){
        case 2:
        	return ForgeDirection.NORTH;
        case 0:
        	return ForgeDirection.SOUTH;
        case 1:
        	return ForgeDirection.WEST;
        case 3:
        	return ForgeDirection.EAST;
        case 4:
        	return ForgeDirection.UP;
        case 5:
        	return ForgeDirection.DOWN;
        default:
        	return null;
        }		
	}
	
	public static byte direction2Byte(ForgeDirection direction){
        switch (direction){
        case NORTH:
        	return 2;
        case SOUTH:
        	return 0;
        case WEST:
        	return 1;
        case EAST:
        	return 3;
        case UP:
        	return 4;
        case DOWN:
        	return 5;
        default:
        	return 0;
        }
	}
	
	public static float getPower(IEnergyTile Tile){
		if(Tile.getOutputVoltage()>0){//Energy Source
			return ((Tile.getOutputVoltage()-getVoltage(Tile))*(Tile.getOutputVoltage()-getVoltage(Tile)))/Tile.getResistance(); 
		}else{//Energy Sink
			return getVoltage(Tile)*getVoltage(Tile)/Tile.getResistance();    				
		}
	}

	public static float getCurrent(IEnergyTile Tile){
		if(Tile.getOutputVoltage()>0){//Energy Source
			return (Tile.getOutputVoltage()-getVoltage(Tile))/Tile.getResistance(); 
		}else{//Energy Sink
			return getVoltage(Tile)/Tile.getResistance();    				
		}
	}
	
	public static float getVoltage(IBaseComponent Tile){
		if (EnergyNet.getForWorld(((TileEntity)Tile).getWorldObj()).voltageCache.containsKey(Tile))
			return EnergyNet.getForWorld(((TileEntity)Tile).getWorldObj()).voltageCache.get(Tile);
		else
			return 0;
	}
	
	public static void updateTileEntityField(TileEntity te,String field){
		mod_SimElectricity.instance.packetPipeline.sendToDimension(new PacketTileEntityFieldUpdate(te,field),te.getWorldObj().getWorldInfo().getVanillaDimension());
	}
}