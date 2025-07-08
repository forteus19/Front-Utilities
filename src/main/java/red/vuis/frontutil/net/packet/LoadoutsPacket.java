package red.vuis.frontutil.net.packet;

import java.util.List;
import java.util.Map;

import com.boehmod.blockfront.common.match.Loadout;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.client.data.AddonClientData;
import red.vuis.frontutil.data.AddonStreamCodecs;
import red.vuis.frontutil.setup.LoadoutIndex;
import red.vuis.frontutil.util.AddonUtils;

public record LoadoutsPacket(Map<LoadoutIndex.Identifier, List<Loadout>> loadouts) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<LoadoutsPacket> TYPE = new CustomPacketPayload.Type<>(AddonConstants.res("loadouts"));
	public static final StreamCodec<RegistryFriendlyByteBuf, LoadoutsPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.map(
			Object2ObjectOpenHashMap::new,
			AddonStreamCodecs.BF_COUNTRY,
			ByteBufCodecs.map(
				Object2ObjectOpenHashMap::new,
				ByteBufCodecs.STRING_UTF8,
				ByteBufCodecs.map(
					Object2ObjectOpenHashMap::new,
					AddonStreamCodecs.MATCH_CLASS,
					ByteBufCodecs.collection(
						ObjectArrayList::new,
						AddonStreamCodecs.LOADOUT
					)
				)
			)
		),
		packet -> LoadoutIndex.flatToNested(packet.loadouts),
		nested -> new LoadoutsPacket(LoadoutIndex.nestedToFlat(nested))
	);
	
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	
	public static void handleClient(LoadoutsPacket packet, IPayloadContext context) {
		AddonConstants.LOGGER.info("Applying loadouts from server.");
		
		LoadoutIndex.apply(packet.loadouts);
		AddonClientData.getInstance().reloadLoadouts();
	}
	
	public static void handleServer(LoadoutsPacket packet, IPayloadContext context) {
		// Prevent the handler from running on the integrated server
		if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
			return;
		}
		
		if (AddonUtils.anyGamesActive()) {
			context.player().sendSystemMessage(Component.translatable(
				"frontutil.message.packet.loadouts.game",
				Component.literal("/frontutil loadout sync").withStyle(ChatFormatting.DARK_GREEN)
			).withStyle(ChatFormatting.GOLD));
			return;
		}
		
		LoadoutIndex.apply(packet.loadouts);
		PacketDistributor.sendToAllPlayers(packet);
		
		context.player().sendSystemMessage(Component.translatable(
			"frontutil.message.packet.loadouts.success"
		));
	}
}
