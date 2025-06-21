package red.vuis.frontutil.event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.FrontUtil;
import red.vuis.frontutil.client.command.FrontUtilClientCommand;
import red.vuis.frontutil.command.FrontUtilCommand;
import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.data.GunModifierTarget;
import red.vuis.frontutil.net.packet.GunModifiersPacket;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.setup.GunModifierIndex;
import red.vuis.frontutil.setup.LoadoutIndex;

@EventBusSubscriber(
	modid = FrontUtil.MOD_ID,
	bus = EventBusSubscriber.Bus.GAME
)
public final class AddonGameEvents {
	private AddonGameEvents() {
	}
	
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		FrontUtil.LOGGER.info("Registering commands...");
		
		var dispatcher = event.getDispatcher();
		FrontUtilCommand.register(dispatcher);
	}
	
	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		FrontUtil.LOGGER.info("Preparing gun modifiers...");
		
		GunModifierIndex.applyDefaults();
		
		FrontUtil.LOGGER.info("Parsing and applying gun modifier targets...");
		
		GunModifierTarget.parseAndApply(event.getServer().getResourceManager());
	}
	
	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		GunModifierTarget.ACTIVE.clear();
	}
	
	@EventBusSubscriber(
		modid = FrontUtil.MOD_ID,
		bus = EventBusSubscriber.Bus.GAME,
		value = Dist.CLIENT
	)
	public static final class Client {
		private Client() {
		}
		
		@SubscribeEvent
		public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
			FrontUtil.LOGGER.info("Registering client-only commands...");
			
			var dispatcher = event.getDispatcher();
			FrontUtilClientCommand.register(dispatcher);
		}
	}
	
	@EventBusSubscriber(
		modid = FrontUtil.MOD_ID,
		bus = EventBusSubscriber.Bus.GAME,
		value = Dist.DEDICATED_SERVER
	)
	public static final class Server {
		private Server() {
		}
		
		@SubscribeEvent
		public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
			if (event.getEntity() instanceof ServerPlayer serverPlayer) {
				FrontUtil.LOGGER.info("Syncing custom data with player '{}'.", event.getEntity().getName().getString());
				PacketDistributor.sendToPlayer(serverPlayer, new GunModifiersPacket(GunModifier.ACTIVE));
				PacketDistributor.sendToPlayer(serverPlayer, new LoadoutsPacket(LoadoutIndex.currentFlat()));
			}
		}
		
		@SubscribeEvent
		public static void onServerAboutToStart(ServerAboutToStartEvent event) {
			Path basePath = getBasePath(event.getServer());
			
			LoadoutIndex.parseAndApply(basePath.resolve("loadouts.dat"));
		}
		
		@SubscribeEvent
		public static void onServerStopping(ServerStoppingEvent event) {
			Path basePath = getBasePath(event.getServer());
			
			LoadoutIndex.saveCurrent(basePath.resolve("loadouts.dat"));
		}
		
		private static Path getBasePath(MinecraftServer server) {
			Path basePath = server.getFile("frontutil");
			if (!Files.isDirectory(basePath)) {
				try {
					Files.createDirectory(basePath);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			return basePath;
		}
	}
}
