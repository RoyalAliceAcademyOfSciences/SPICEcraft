/*
 * Copyright (C) 2014 SimElectricity
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package simElectricity;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import simElectricity.API.SEAPI;

import simElectricity.Client.ClientRender;
import simElectricity.Common.SEUtils;
import simElectricity.Common.CableRenderHelper;
import simElectricity.Common.CommandSimE;
import simElectricity.Common.ConfigManager;
import simElectricity.Common.FluidUtil;
import simElectricity.Common.EnergyNet.EnergyNetAgent;
import simElectricity.Common.EnergyNet.EnergyNetEventHandler;
import simElectricity.Common.Network.MessageTileEntityUpdate;
import simElectricity.Common.Network.NetworkManager;
import simElectricity.Items.*;

@Mod(modid = SEUtils.MODID, name = SEUtils.NAME, version = SimElectricity.version, guiFactory = "simElectricity.Client.SimEGuiFactory", dependencies = "required-after:Forge@[10.12.2.1147,)")
public class SimElectricity {
	public static final String version = "1.0.0";

    @Instance(SEUtils.MODID)
    public static SimElectricity instance;

    public SimpleNetworkWrapper networkChannel;

    public static ItemUltimateMultimeter ultimateMultimeter;
    public static ItemGlove itemGlove;
    public static ItemWrench itemWrench;
    
    /**
     * PreInitialize
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    	
    	//Initialize utility functions
    	SEAPI.isSELoaded = true;
    	SEAPI.fluid = new FluidUtil();
    	SEAPI.cableRenderHelper = new CableRenderHelper();
    	SEAPI.utils = new SEUtils();
    	SEAPI.energyNetAgent = new EnergyNetAgent();
    	
    	if (event.getSide().isClient()){
    		SEAPI.clientRender = new ClientRender();
    	}

        //Load configurations
        FMLCommonHandler.instance().bus().register(new ConfigManager());
        ConfigManager.init(event);

        //Register event buses
        SEAPI.networkManager = new NetworkManager();
        new EnergyNetEventHandler();

        //Register creative tabs
        SEAPI.SETab = new CreativeTabs(SEUtils.MODID) {
            @Override
            @SideOnly(Side.CLIENT)
            public Item getTabIconItem() {
                return ultimateMultimeter;
            }
        };
        
        
        //Register items
    	ultimateMultimeter = new ItemUltimateMultimeter();
    	itemGlove = new ItemGlove();
    	itemWrench = new ItemWrench();

        //Register network channel
        networkChannel = NetworkRegistry.INSTANCE.newSimpleChannel(SEUtils.MODID);
        networkChannel.registerMessage(MessageTileEntityUpdate.Handler.class, MessageTileEntityUpdate.class, 0, Side.CLIENT);
        networkChannel.registerMessage(MessageTileEntityUpdate.Handler.class, MessageTileEntityUpdate.class, 1, Side.SERVER);
    }

    /**
     * Initialize
     */
    @EventHandler
    public void init(FMLInitializationEvent event) {}

    /**
     * PostInitialize
     */
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {}
    
    @EventHandler
    public void serverStart(FMLServerStartingEvent event){
    	event.registerServerCommand(new CommandSimE());
    }
}
