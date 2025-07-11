package red.vuis.frontutil.net.packet;

import com.boehmod.blockfront.common.item.GunItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.data.WeaponExtraSettings;

public record GiveGunPacket(Holder<Item> item, WeaponExtraSettings settings) implements CustomPacketPayload {
	public static final Type<GiveGunPacket> TYPE = new Type<>(AddonConstants.res("give_gun"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GiveGunPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.holderRegistry(Registries.ITEM),
		GiveGunPacket::item,
		WeaponExtraSettings.STREAM_CODEC,
		GiveGunPacket::settings,
		GiveGunPacket::new
	);
	
	public GiveGunPacket {
		if (!(item.value() instanceof GunItem)) {
			throw new IllegalArgumentException("Item must be a GunItem!");
		}
	}
	
	public GiveGunPacket(Item item, WeaponExtraSettings settings) {
		this(BuiltInRegistries.ITEM.wrapAsHolder(item), settings);
	}
	
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	
	public static void handle(GiveGunPacket packet, IPayloadContext context) {
		context.player().addItem(packet.settings.getItemStack((GunItem) packet.item.value()));
	}
}
