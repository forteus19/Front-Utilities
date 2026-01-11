package red.vuis.frontutil.data;

import com.boehmod.bflib.cloud.common.player.AbstractPlayerCloudData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record ProfileOverrideData(
	String username,
	int exp,
	int prestige
) {
	public static final PacketCodec<ByteBuf, ProfileOverrideData> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.STRING, ProfileOverrideData::username,
		PacketCodecs.VAR_INT, ProfileOverrideData::exp,
		PacketCodecs.VAR_INT, ProfileOverrideData::prestige,
		ProfileOverrideData::new
	);
	
	public static ProfileOverrideData of(AbstractPlayerCloudData<?> cloudData) {
		return new ProfileOverrideData(cloudData.getUsername(), cloudData.getExp(), cloudData.getPrestigeLevel());
	}
	
	public void apply(AbstractPlayerCloudData<?> cloudData) {
		cloudData.setUsername(username());
		cloudData.setExp(exp());
		cloudData.setPrestigeLevel(prestige());
	}
}
