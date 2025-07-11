package red.vuis.frontutil.event;

import com.boehmod.blockfront.BlockFront;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.command.FrontUtilCommand;
import red.vuis.frontutil.data.GunModifierTarget;
import red.vuis.frontutil.net.packet.GiveGunPacket;
import red.vuis.frontutil.net.packet.GunModifiersPacket;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.setup.GunItemIndex;
import red.vuis.frontutil.setup.GunModifierIndex;
import red.vuis.frontutil.setup.GunSkinIndex;
import red.vuis.frontutil.setup.LoadoutIndex;

@EventBusSubscriber(
	modid = AddonConstants.MOD_ID
)
public final class AddonCommonEvents {
	private AddonCommonEvents() {
	}
	
	@SubscribeEvent
	public static void onLoadComplete(FMLLoadCompleteEvent event) {
		AddonConstants.LOGGER.info("Indexing default gun properties...");
		GunModifierIndex.init();
		
		AddonConstants.LOGGER.info("Indexing default loadouts...");
		LoadoutIndex.init();
		
		AddonConstants.LOGGER.info("Indexing gun items...");
		GunItemIndex.init();
		
		var manager = BlockFront.getInstance().getManager();
		if (manager == null) {
			AddonConstants.LOGGER.error("Failed to get BlockFront manager! Some things will be broken.");
		} else {
			AddonConstants.LOGGER.info("Indexing skins...");
			GunSkinIndex.init(manager.getCloudRegistry());
		}
	}
	
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		AddonConstants.LOGGER.info("Registering commands...");
		
		var dispatcher = event.getDispatcher();
		FrontUtilCommand.register(dispatcher);
	}
	
	@SubscribeEvent
	public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToServer(GiveGunPacket.TYPE, GiveGunPacket.STREAM_CODEC, GiveGunPacket::handle);
		registrar.playToClient(GunModifiersPacket.TYPE, GunModifiersPacket.STREAM_CODEC, GunModifiersPacket::handle);
		registrar.playBidirectional(LoadoutsPacket.TYPE, LoadoutsPacket.STREAM_CODEC, new DirectionalPayloadHandler<>(
			LoadoutsPacket::handleClient, LoadoutsPacket::handleServer
		));
	}
	
	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		AddonConstants.LOGGER.info("Preparing gun modifiers...");
		
		GunModifierIndex.applyDefaults();
		
		AddonConstants.LOGGER.info("Parsing and applying gun modifier targets...");
		
		GunModifierTarget.parseAndApply(event.getServer().getResourceManager());
	}
	
	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		GunModifierTarget.ACTIVE.clear();
	}
}
