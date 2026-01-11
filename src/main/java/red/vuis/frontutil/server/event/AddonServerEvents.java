package red.vuis.frontutil.server.event;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.boehmod.blockfront.common.player.PlayerCloudData;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
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
import red.vuis.frontutil.net.packet.SetProfileOverridesPropertyPacket;
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
		if (!(event.getEntity() instanceof ServerPlayerEntity player)) {
			return;
		}
		
		AddonConstants.LOGGER.info("Syncing custom server data with player '{}'.", player.getNameForScoreboard());
		
		AddonCommonData commonData = AddonCommonData.getInstance();
		
		PacketDistributor.sendToPlayer(
			player,
			new LoadoutsPacket(LoadoutIndex.currentFlat()),
			new SetProfileOverridesPacket(commonData.getProfileOverrideData())
		);
		
		Map<UUID, PlayerCloudData> profileOverrides = commonData.profileOverrides;
		
		GameProfile profile = player.getGameProfile();
		PlayerCloudData cloudData = profileOverrides.get(profile.getId());
		if (cloudData != null && !cloudData.getUsername().equals(profile.getName())) {
			AddonConstants.LOGGER.info("Refreshing profile override username for player '{}' (new: '{}').", cloudData.getUsername(), profile.getName());
			cloudData.setUsername(profile.getName());
			
			PacketDistributor.sendToAllPlayers(new SetProfileOverridesPropertyPacket(
				Set.of(AddonUtils.createIdPair(profile)),
				IntObjectPair.of(0, profile.getName())
			));
		}
	}
}
