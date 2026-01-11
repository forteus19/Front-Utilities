package red.vuis.frontutil.net.packet;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.boehmod.blockfront.common.player.PlayerCloudData;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.data.AddonCommonData;
import red.vuis.frontutil.data.AddonPacketCodecs;
import red.vuis.frontutil.util.AddonUtils;

public record SetProfileOverridesPropertyPacket(Set<Pair<UUID, String>> idPairs, IntObjectPair<Object> property) implements CustomPayload {
	public static final Id<SetProfileOverridesPropertyPacket> ID = new Id<>(AddonConstants.id("set_profile_overrides_property"));
	public static final PacketCodec<ByteBuf, SetProfileOverridesPropertyPacket> PACKET_CODEC = PacketCodec.tuple(
		AddonPacketCodecs.pair(Uuids.PACKET_CODEC, PacketCodecs.STRING).collect(PacketCodecs.toCollection(ObjectOpenHashSet::new)), SetProfileOverridesPropertyPacket::idPairs,
		PropertyCodec.INSTANCE, SetProfileOverridesPropertyPacket::property,
		SetProfileOverridesPropertyPacket::new
	);
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
	
	public static void handleClient(SetProfileOverridesPropertyPacket packet, IPayloadContext context) {
		Map<UUID, PlayerCloudData> profileOverrides = AddonCommonData.getInstance().profileOverrides;
		
		for (Pair<UUID, String> idPair : packet.idPairs()) {
			PlayerCloudData cloudData = profileOverrides.computeIfAbsent(idPair.left(), uuid -> AddonUtils.createPlayerCloudData(idPair));
			IntObjectPair<Object> property = packet.property();
			switch (property.leftInt()) {
				case 0 -> cloudData.setUsername((String) property.right());
				case 1 -> cloudData.setExp((Integer) property.right());
				case 2 -> cloudData.setPrestigeLevel((Integer) property.right());
				default -> throw new AssertionError();
			}
		}
	}
	
	private static class PropertyCodec implements PacketCodec<ByteBuf, IntObjectPair<Object>> {
		public static final PropertyCodec INSTANCE = new PropertyCodec();
		
		private PropertyCodec() {
		}
		
		@Override
		public IntObjectPair<Object> decode(ByteBuf buf) {
			int propertyId = VarInts.read(buf);
			Object obj = switch (propertyId) {
				case 0 -> StringEncoding.decode(buf, 16);
				case 1, 2 -> VarInts.read(buf);
				default -> throw new DecoderException("Received unknown property id");
			};
			return IntObjectPair.of(propertyId, obj);
		}
		
		@Override
		public void encode(ByteBuf buf, IntObjectPair<Object> value) {
			int propertyId = value.leftInt();
			VarInts.write(buf, propertyId);
			switch (propertyId) {
				case 0 -> StringEncoding.encode(buf, (String) value.right(), 16);
				case 1, 2 -> VarInts.write(buf, (Integer) value.right());
			}
		}
	}
}
