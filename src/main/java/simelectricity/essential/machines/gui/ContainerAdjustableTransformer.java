package simelectricity.essential.machines.gui;

import java.util.Iterator;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import simelectricity.api.SEAPI;
import simelectricity.essential.common.ContainerNoInventoryTwoPort;
import simelectricity.essential.machines.tile.TileAdjustableTransformer;
import simelectricity.essential.utils.network.ISEButtonEventHandler;
import simelectricity.essential.utils.network.ISEContainerUpdate;
import simelectricity.essential.utils.network.MessageContainerSync;

public class ContainerAdjustableTransformer extends ContainerNoInventoryTwoPort<TileAdjustableTransformer> implements ISEContainerUpdate, ISEButtonEventHandler{
	public double ratio, outputResistance;
	public EnumFacing inputSide, outputSide;
	public double vPri, vSec;
	
	public ContainerAdjustableTransformer(TileEntity tileEntity) {
		super(tileEntity);
	}

	@Override
	public void detectAndSendChanges() {
		double ratio = tileEntity.ratio, outputResistance = tileEntity.outputResistance;
		EnumFacing inputSide = tileEntity.inputSide, outputSide = tileEntity.outputSide;
		double vPri = tileEntity.vPri, vSec = tileEntity.vSec;
		
		//Look for any changes
		if (this.ratio == ratio &&
			this.outputResistance == outputResistance &&
			this.inputSide == inputSide &&
			this.outputSide == outputSide &&
			this.vPri == vPri &&
			this.vSec == vSec)
			return;

		this.ratio = ratio;
		this.outputResistance = outputResistance;
		this.inputSide = inputSide;
		this.outputSide = outputSide;
		this.vPri = vPri;
		this.vSec = vSec;
		
		//Send change to all crafter
    	Iterator<IContainerListener> iterator = this.listeners.iterator();
    	while (iterator.hasNext()) {
    		IContainerListener crafter = iterator.next();
    		
    		if (crafter instanceof EntityPlayerMP){
    			MessageContainerSync.sendToClient((EntityPlayerMP)crafter, ratio, outputResistance, inputSide, outputSide, vPri, vSec);
    		}
    	}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onDataArrivedFromServer(Object[] data) {
		this.ratio = (Double) data[0];
		this.outputResistance = (Double) data[1];
		this.inputSide = (EnumFacing) data[2];
		this.outputSide = (EnumFacing) data[3];
		this.vPri = (Double) data[4];
		this.vSec = (Double) data[5];
	}
	
	@Override
	public void onButtonPressed(int buttonID, boolean isCtrlPressed) {
		double ratio = tileEntity.ratio, outputResistance = tileEntity.outputResistance;
		
		switch(buttonID){
		case 6:
			if (isCtrlPressed)
				outputResistance -= 10;
			else
				outputResistance -= 1;
			break;
		case 7:
			outputResistance -= 0.1;
			break;
		case 8:
			if (isCtrlPressed)
				outputResistance -= 0.001;
			else
				outputResistance -= 0.01;
			break;
		case 9:
			if (isCtrlPressed)
				outputResistance += 0.001;
			else
				outputResistance += 0.01;
			break;
		case 10:
			outputResistance += 0.1;
			break;
		case 11:
			if (isCtrlPressed)
				outputResistance += 10;
			else
				outputResistance += 1;
			break;
			
			
			
		case 0:
			if (isCtrlPressed)
				ratio -= 10;
			else
				ratio -= 1;
			break;
		case 1:
			ratio -= 0.1;
			break;
		case 2:
			if (isCtrlPressed)
				ratio -= 0.001;
			else
				ratio -= 0.01;
			break;
		case 3:
			if (isCtrlPressed)
				ratio += 0.001;
			else
				ratio += 0.01;
			break;
		case 4:
			ratio += 0.1;
			break;
		case 5:
			if (isCtrlPressed)
				ratio += 10;
			else
				ratio += 1;
			break;
		}
		
        if (outputResistance < 0.001)
        	outputResistance = 0.001;
        if (outputResistance > 100)
        	outputResistance = 100;


        if (ratio < 0.001)
            ratio = 0.001;
        if (ratio > 100)
            ratio = 100;
		
		tileEntity.ratio = ratio;
		tileEntity.outputResistance = outputResistance;
		
		SEAPI.energyNetAgent.updateTileParameter(tileEntity);
	}
}
