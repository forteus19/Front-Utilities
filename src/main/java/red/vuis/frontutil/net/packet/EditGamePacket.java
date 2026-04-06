package red.vuis.frontutil.net.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.client.screen.GameEditorScreen;
import red.vuis.frontutil.data.edit.GameEditData;

public record EditGamePacket(String name, GameEditData data) implements CustomPayload {
	public static final Id<EditGamePacket> ID = new Id<>(AddonConstants.id("edit_game"));
	public static final PacketCodec<ByteBuf, EditGamePacket> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.STRING, EditGamePacket::name,
		GameEditData.PACKET_CODEC, EditGamePacket::data,
		EditGamePacket::new
	);
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
	
	public static void handleClient(EditGamePacket packet, IPayloadContext context) {
		MinecraftClient.getInstance().setScreen(new GameEditorScreen(packet.name(), packet.data()));
	}
}
