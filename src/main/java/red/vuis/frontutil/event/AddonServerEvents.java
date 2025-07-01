package red.vuis.frontutil.event;

import java.nio.file.Path;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.net.packet.GunModifiersPacket;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.setup.LoadoutIndex;
import red.vuis.frontutil.util.AddonUtils;

@EventBusSubscriber(
	modid = AddonConstants.MOD_ID,
	value = Dist.DEDICATED_SERVER
)
public final class AddonServerEvents {
	private AddonServerEvents() {
	}
	
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			AddonConstants.LOGGER.info("Syncing custom data with player '{}'.", event.getEntity().getName().getString());
			PacketDistributor.sendToPlayer(serverPlayer, new GunModifiersPacket(GunModifier.ACTIVE));
			PacketDistributor.sendToPlayer(serverPlayer, new LoadoutsPacket(LoadoutIndex.currentFlat()));
		}
	}
	
	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		Path basePath = AddonUtils.getServerDataPath(event.getServer());
		
		LoadoutIndex.parseAndApply(basePath.resolve(LoadoutIndex.DEFAULT_LOADOUTS_PATH_NAME));
	}
}
