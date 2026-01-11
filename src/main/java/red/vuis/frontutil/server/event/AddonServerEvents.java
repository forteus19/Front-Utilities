package red.vuis.frontutil.server.event;

import java.util.Map;
import java.util.UUID;

import com.boehmod.blockfront.common.player.PlayerCloudData;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.data.AddonCommonData;
import red.vuis.frontutil.data.ProfileOverrideData;
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
		
		AddonConstants.LOGGER.info("Syncing custom server data with player '{}'.", player.getNameForScoreboard());
		
		CustomPayload[] extraPayloads;
		
		AddonCommonData commonData = AddonCommonData.getInstance();
		Map<UUID, PlayerCloudData> profileOverrides = commonData.profileOverrides;
		
		GameProfile profile = player.getGameProfile();
		PlayerCloudData cloudData = profileOverrides.get(profile.getId());
		if (cloudData != null && !cloudData.getUsername().equals(profile.getName())) {
			AddonConstants.LOGGER.info("Refreshing profile override username for player '{}' (new: '{}').", cloudData.getUsername(), profile.getName());
			cloudData.setUsername(profile.getName());
			
			PacketDistributor.sendToAllPlayers(new SetProfileOverridesPacket(Map.of(profile.getId(), ProfileOverrideData.of(cloudData))));
			extraPayloads = new CustomPayload[0];
		} else {
			extraPayloads = new CustomPayload[]{
				new SetProfileOverridesPacket(commonData.getProfileOverrideData())
			};
		}
		
		PacketDistributor.sendToPlayer(
			player,
			new LoadoutsPacket(LoadoutIndex.currentFlat()),
			extraPayloads
		);
	}
}
