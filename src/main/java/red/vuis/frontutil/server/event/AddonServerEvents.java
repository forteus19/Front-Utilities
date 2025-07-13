package red.vuis.frontutil.server.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.net.packet.GunModifiersPacket;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.setup.LoadoutIndex;

@EventBusSubscriber(
	modid = AddonConstants.MOD_ID,
	value = Dist.DEDICATED_SERVER
)
public final class AddonServerEvents {
	private AddonServerEvents() {
	}
	
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity serverPlayer) {
			AddonConstants.LOGGER.info("Syncing custom data with player '{}'.", event.getEntity().getName().getString());
			PacketDistributor.sendToPlayer(serverPlayer, new GunModifiersPacket(GunModifier.ACTIVE));
			PacketDistributor.sendToPlayer(serverPlayer, new LoadoutsPacket(LoadoutIndex.currentFlat()));
		}
	}
}
