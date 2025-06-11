package red.vuis.frontutil.event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

import com.boehmod.blockfront.common.item.GunItem;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.FrontUtil;
import red.vuis.frontutil.command.FrontUtilCommand;
import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.data.GunModifierTarget;
import red.vuis.frontutil.net.packet.GunModifiersPacket;

@EventBusSubscriber(
	modid = FrontUtil.MOD_ID,
	bus = EventBusSubscriber.Bus.GAME
)
public final class AddonGameEvents {
	private static final String GUN_MODIFIER_TARGETS_FILENAME = "gun_modifier_targets.json";
	
	private AddonGameEvents() {
	}
	
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		FrontUtil.info("Registering commands...");
		
		var dispatcher = event.getDispatcher();
		FrontUtilCommand.register(dispatcher);
	}

	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		FrontUtil.info("Parsing gun modifier targets...");
		
		ResourceManager resourceManager = event.getServer().getResourceManager();
		List<Resource> targetResources = resourceManager.getResourceStack(FrontUtil.res(GUN_MODIFIER_TARGETS_FILENAME));
		
		for (Resource targetResource : targetResources) {
			try (BufferedReader targetReader = targetResource.openAsReader()) {
				List<GunModifierTarget> targets = parseGunModifierTargets(targetResource, targetReader);
				if (targets == null || !targets.stream().allMatch(AddonGameEvents::checkGunModifierTarget)) {
					continue;
				}
				GunModifierTarget.ACTIVE.addAll(targets);
				
				for (GunModifierTarget target : targets) {
					Optional<Resource> modifierResource = resourceManager.getResource(target.modifier());
					if (modifierResource.isEmpty()) {
						FrontUtil.error("Modifier '{}' does not exist!", target.modifier());
						continue;
					}
					try (BufferedReader modifierReader = modifierResource.get().openAsReader()) {
						GunModifier modifier = parseGunModifier(target.modifier(), modifierReader);
						if (modifier == null) {
							continue;
						}
						
						for (ResourceLocation itemRes : target.targets()) {
							GunItem item = (GunItem) BuiltInRegistries.ITEM.get(itemRes);
							GunModifier.ACTIVE.put(BuiltInRegistries.ITEM.wrapAsHolder(item), modifier);
							modifier.apply(item);
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		GunModifierTarget.ACTIVE.clear();
	}
	
	private static @Nullable List<GunModifierTarget> parseGunModifierTargets(Resource targetResource, Reader targetReader) {
		DataResult<List<GunModifierTarget>> targetResult = GunModifierTarget.CODEC.listOf().parse(JsonOps.INSTANCE, JsonParser.parseReader(targetReader));
		if (targetResult.isError()) {
			FrontUtil.error("Failed to parse gun modifier targets for pack id '{}'!", targetResource.sourcePackId());
			return null;
		}
		return targetResult.getOrThrow();
	}
	
	private static boolean checkGunModifierTarget(GunModifierTarget target) {
		for (ResourceLocation itemRes : target.targets()) {
			if (!BuiltInRegistries.ITEM.containsKey(itemRes) || !(BuiltInRegistries.ITEM.get(itemRes) instanceof GunItem)) {
				FrontUtil.error("Modifier target '{}' is not a modifiable item!", itemRes);
				return false;
			}
		}
		return true;
	}
	
	private static @Nullable GunModifier parseGunModifier(ResourceLocation name, Reader reader) {
		DataResult<GunModifier> result = GunModifier.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader));
		if (result.isError()) {
			FrontUtil.error("Failed to parse gun modifier '{}'!", name);
		}
		return result.getOrThrow();
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
				PacketDistributor.sendToPlayer(serverPlayer, new GunModifiersPacket(GunModifier.ACTIVE));
			}
		}
	}
}
