package simElectricity.Samples;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import simElectricity.API.*;
import simElectricity.API.EnergyTile.IBaseComponent;
import simElectricity.API.Events.TileAttachEvent;
import simElectricity.API.Events.TileDetachEvent;

public abstract class TileSampleBaseComponent extends TileEntity implements	IBaseComponent {
	protected boolean isAddedToEnergyNet = false;
	
	@Override
	public void updateEntity() {
		if (!worldObj.isRemote && !isAddedToEnergyNet) {
			MinecraftForge.EVENT_BUS.post(new TileAttachEvent(this));
			this.isAddedToEnergyNet=true;
			Util.scheduleBlockUpdate(this);
		}
	}

	@Override
	public void invalidate() {
		if (!worldObj.isRemote & isAddedToEnergyNet) {
			MinecraftForge.EVENT_BUS.post(new TileDetachEvent(this));
		}
	}
}
