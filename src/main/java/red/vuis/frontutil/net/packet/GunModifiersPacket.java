package red.vuis.frontutil.net.packet;

import java.util.Map;

import com.boehmod.blockfront.common.item.GunItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.setup.GunModifierIndex;

public record GunModifiersPacket(Map<Holder<Item>, GunModifier> modifiers) implements CustomPacketPayload {
	public static final Type<GunModifiersPacket> TYPE = new Type<>(AddonConstants.res("gun_modifiers"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GunModifiersPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.map(
			Object2ObjectOpenHashMap::new,
			ByteBufCodecs.holderRegistry(Registries.ITEM), GunModifier.STREAM_CODEC
		), GunModifiersPacket::modifiers,
		GunModifiersPacket::new
	);
	
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	
	public static void handle(GunModifiersPacket packet, IPayloadContext context) {
		GunModifierIndex.applyDefaults();
		
		for (Map.Entry<Holder<Item>, GunModifier> entry : packet.modifiers().entrySet()) {
			Item item = entry.getKey().value();
			if (item instanceof GunItem gunItem) {
				entry.getValue().apply(gunItem);
			}
		}
	}
}
