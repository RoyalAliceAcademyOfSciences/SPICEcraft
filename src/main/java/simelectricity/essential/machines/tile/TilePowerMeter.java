package simelectricity.essential.machines.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rikka.librikka.tileentity.INamedContainerProvider2;
import simelectricity.api.ISEEnergyNetUpdateHandler;
import simelectricity.api.components.ISESwitch;
import simelectricity.essential.common.semachine.SETwoPortMachine;
import simelectricity.essential.machines.gui.ContainerPowerMeter;

public class TilePowerMeter extends SETwoPortMachine<ISESwitch> implements 
		ISESwitch, ISEEnergyNetUpdateHandler, ITickableTileEntity, INamedContainerProvider2 {
    public boolean isOn;
    public double current, voltage, bufferedEnergy;

    ///////////////////////////////////
    /// TileEntity
    ///////////////////////////////////
    @Override
    public void tick() {
        if (this.world.isRemote)
            return;

        if (this.isOn)
            this.bufferedEnergy += voltage * current / 20;
    }

    @Override
    public void read(BlockState blockState, CompoundNBT tagCompound) {
        super.read(blockState, tagCompound);

        this.bufferedEnergy = tagCompound.getDouble("bufferedEnergy");
        this.isOn = tagCompound.getBoolean("isOn");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        tagCompound.putDouble("bufferedEnergy", this.bufferedEnergy);
        tagCompound.putBoolean("isOn", this.isOn);

        return super.write(tagCompound);
    }

    /////////////////////////////////////////////////////////
    ///ISEEnergyNetUpdateHandler
    /////////////////////////////////////////////////////////
    @Override
    public void onEnergyNetUpdate() {
        this.voltage = this.input.getVoltage();
        if (this.isOn) {
            this.current = (voltage - this.input.getComplement().getVoltage()) / this.cachedParam.getResistance();
        } else {
            this.current = 0;
        }
    }

    /////////////////////////////////////////////////////////
    ///ISESwitchData
    /////////////////////////////////////////////////////////
    @Override
    public boolean isOn() {
        return this.isOn;
    }

    @Override
    public double getResistance() {
        return 0.01;
    }

    ///////////////////////////////////
    /// ISESocketProvider
    ///////////////////////////////////
    @Override
    @OnlyIn(Dist.CLIENT)
    public int getSocketIconIndex(Direction side) {
        if (side == this.inputSide)
            return 2;
        else if (side == this.outputSide)
            return 4;
        else
            return -1;
    }

    ///////////////////////////////////
    /// INamedContainerProvider
    ///////////////////////////////////
	@Override
	public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
        return new ContainerPowerMeter(this, windowId);
    }
}
