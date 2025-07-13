package red.vuis.frontutil.net.packet;

import java.util.Map;

import com.boehmod.blockfront.common.item.GunItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.data.GunModifier;
import red.vuis.frontutil.setup.GunModifierIndex;

public record GunModifiersPacket(Map<RegistryEntry<Item>, GunModifier> modifiers) implements CustomPayload {
	public static final Id<GunModifiersPacket> ID = new Id<>(AddonConstants.id("gun_modifiers"));
	public static final PacketCodec<RegistryByteBuf, GunModifiersPacket> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.map(
			Object2ObjectOpenHashMap::new,
			PacketCodecs.registryEntry(RegistryKeys.ITEM), GunModifier.PACKET_CODEC
		), GunModifiersPacket::modifiers,
		GunModifiersPacket::new
	);
	
	@Override
	public @NotNull Id<? extends CustomPayload> getId() {
		return ID;
	}
	
	public static void handle(GunModifiersPacket packet, IPayloadContext context) {
		GunModifierIndex.applyDefaults();
		
		for (Map.Entry<RegistryEntry<Item>, GunModifier> entry : packet.modifiers().entrySet()) {
			Item item = entry.getKey().value();
			if (item instanceof GunItem gunItem) {
				entry.getValue().apply(gunItem);
			}
		}
	}
}
