package simelectricity.essential;

import simelectricity.essential.api.SEEAPI;
import simelectricity.essential.cable.CoverPanelFactory;
import simelectricity.essential.extensions.ExtensionBuildCraft;
import simelectricity.essential.extensions.ExtensionRailCraft;
import simelectricity.essential.fluids.FluidManager;
import simelectricity.essential.utils.ITileRenderingInfoSyncHandler;
import simelectricity.essential.utils.network.MessageContainerSync;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = Essential.modID, name = "SimElectricity Essential", dependencies = "required-after:simelectricity")
public class Essential {
	public final static String modID = "sime_essential";
	
    @SidedProxy(clientSide="simelectricity.essential.ClientProxy", serverSide="simelectricity.essential.CommonProxy") 
    public static CommonProxy proxy;
	
    @Instance(modID)
    public static Essential instance;
    
    public SimpleNetworkWrapper networkChannel;
	
    /**
     * PreInitialize
     */
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {    	
    	ItemRegistry.registerItems();
    	BlockRegistry.registerBlocks();
    	FluidManager.registerFluids();
    	
    	SEEAPI.coverPanelFactory = new CoverPanelFactory();
    	
        //Register Forge Event Handlers
        new ITileRenderingInfoSyncHandler.ForgeEventHandler();
        
        networkChannel = NetworkRegistry.INSTANCE.newSimpleChannel(modID);
        networkChannel.registerMessage(MessageContainerSync.Handler.class, MessageContainerSync.class, 0, Side.CLIENT);
        networkChannel.registerMessage(MessageContainerSync.Handler.class, MessageContainerSync.class, 1, Side.SERVER);
    }
    
    /**
     * Initialize
     */
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	BlockRegistry.registerTileEntities();
    	
    	proxy.registerRenders();
    	
        //Register GUI handler
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
    }

    /**
     * PostInitialize
     */
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	ExtensionBuildCraft.postInit();
    	ExtensionRailCraft.postInit();
    }
}
