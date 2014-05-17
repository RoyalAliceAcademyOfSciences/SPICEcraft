package simElectricity.Blocks;

import java.util.Iterator;

import simElectricity.API.Common.ContainerBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerElectricFurnace extends ContainerBase{
	protected TileElectricFurnace tileEntity;
	protected int progress;
	
	public ContainerElectricFurnace (InventoryPlayer inventoryPlayer, TileElectricFurnace te){
        tileEntity = te;
        
        addSlotToContainer(new Slot(tileEntity, 0, 43, 33));
        addSlotToContainer(new Slot(tileEntity, 1, 103, 34){public boolean isItemValid(ItemStack par1ItemStack){return false;}});
        
        bindPlayerInventory(inventoryPlayer);
	}
    
    @Override
    public void addCraftingToCrafters(ICrafting par1iCrafting){
        super.addCraftingToCrafters(par1iCrafting);
      	par1iCrafting.sendProgressBarUpdate(this, 0, tileEntity.progress);     
    }
    
    
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int par1, int par2){
    	if (par1 == 0)	tileEntity.progress = par2;
   	}
    
    @Override
    public void detectAndSendChanges(){
    	super.detectAndSendChanges();
    	Iterator var1 = this.crafters.iterator();
    	while (var1.hasNext())
    	{
    		ICrafting var2 = (ICrafting)var1.next();
            	var2.sendProgressBarUpdate(this, 0, progress);          	
    	}
    	
    	progress=tileEntity.progress;
    }
	
    public int getPlayerInventoryStartIndex(){
    	return 2;
    }
    public int getPlayerInventoryEndIndex(){
    	return 38;
    }
    public int getTileInventoryStartIndex(){
    	return 0;
    }
    public int getTileInventoryEndIndex(){
    	return 1;
    }
}