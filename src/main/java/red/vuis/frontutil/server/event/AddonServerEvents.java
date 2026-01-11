package red.vuis.frontutil.server.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.data.AddonCommonData;
import red.vuis.frontutil.net.packet.LoadoutsPacket;
import red.vuis.frontutil.net.packet.SetProfileOverridesPacket;
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
		if (!(event.getEntity() instanceof ServerPlayerEntity player)) {
			return;
		}
		
		AddonConstants.LOGGER.info("Syncing custom server data with player '{}'.", player.getName().getString());
		
		PacketDistributor.sendToPlayer(
			player,
			new LoadoutsPacket(LoadoutIndex.currentFlat()),
			new SetProfileOverridesPacket(AddonCommonData.getInstance().getProfileOverrideData())
		);
	}
}
