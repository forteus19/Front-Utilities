package red.vuis.frontutil.net.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.data.AddonCommonData;

public record SetOldSpreadPacket(boolean value) implements CustomPayload {
	public static final Id<SetOldSpreadPacket> ID = new Id<>(AddonConstants.id("set_old_spread"));
	public static final PacketCodec<ByteBuf, SetOldSpreadPacket> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.BOOL, SetOldSpreadPacket::value,
		SetOldSpreadPacket::new
	);
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
	
	public static void handleClient(SetOldSpreadPacket packet, IPayloadContext context) {
		AddonCommonData.getInstance().useOldSpread = packet.value();
	}
}
