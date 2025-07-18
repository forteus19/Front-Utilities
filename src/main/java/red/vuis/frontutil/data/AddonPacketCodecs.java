package red.vuis.frontutil.data;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import com.boehmod.blockfront.common.gun.GunFireMode;
import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import com.boehmod.blockfront.unnamed.BF_959;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.setup.LoadoutIndex;

public final class AddonPacketCodecs {
	public static final PacketCodec<ByteBuf, BFCountry> BF_COUNTRY = enumOrdinal(BFCountry.values());
	public static final PacketCodec<ByteBuf, GunFireMode> GUN_FIRE_MODE = enumOrdinal(GunFireMode.values());
	public static final PacketCodec<ByteBuf, BF_959> GUN_FIRE_TYPE = enumOrdinal(BF_959.values());
	public static final PacketCodec<ByteBuf, MatchClass> MATCH_CLASS = enumOrdinal(MatchClass.values());
	public static final PacketCodec<RegistryByteBuf, Loadout> LOADOUT = new PacketCodec<>() {
		@Override
		public @NotNull Loadout decode(@NotNull RegistryByteBuf buf) {
			Loadout loadout = new Loadout(
				ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),
				ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),
				ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),
				ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),
				ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),
				ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),
				ItemStack.OPTIONAL_PACKET_CODEC.decode(buf),
				ItemStack.OPTIONAL_PACKET_CODEC.decode(buf)
			);
			
			int numExtra = PacketCodecs.VAR_INT.decode(buf);
			for (int i = 0; i < numExtra; i++) {
				loadout.addExtra(ItemStack.PACKET_CODEC.decode(buf));
			}
			
			loadout.setMinimumXp(PacketCodecs.VAR_INT.decode(buf));
			
			return loadout;
		}
		
		@Override
		public void encode(@NotNull RegistryByteBuf buf, @NotNull Loadout loadout) {
			for (Function<Loadout, ItemStack> slotFunc : LoadoutIndex.SLOT_FUNCS) {
				ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, slotFunc.apply(loadout));
			}
			
			List<ItemStack> extras = loadout.getExtra();
			PacketCodecs.VAR_INT.encode(buf, extras.size());
			for (ItemStack extra : extras) {
				ItemStack.PACKET_CODEC.encode(buf, extra);
			}
			
			PacketCodecs.VAR_INT.encode(buf, loadout.getMinimumXp());
		}
	};
	public static final PacketCodec<ByteBuf, Vec3d> VEC3D = PacketCodec.tuple(
		PacketCodecs.DOUBLE, Vec3d::getX,
		PacketCodecs.DOUBLE, Vec3d::getY,
		PacketCodecs.DOUBLE, Vec3d::getZ,
		Vec3d::new
	);
	
	private AddonPacketCodecs() {
	}
	
	public static <T extends Enum<T>> PacketCodec<ByteBuf, T> enumOrdinal(T[] values, ValueLists.OutOfBoundsHandling oobStrategy) {
		return PacketCodecs.indexed(createIdToValueFunction(Enum::ordinal, values, oobStrategy), Enum::ordinal);
	}
	
	public static <T extends Enum<T>> PacketCodec<ByteBuf, T> enumOrdinal(T[] values) {
		return enumOrdinal(values, ValueLists.OutOfBoundsHandling.ZERO);
	}
	
	// ambiguous method call workaround :P
	private static <T> IntFunction<T> createIdToValueFunction(ToIntFunction<T> valueToIdFunction, T[] values, ValueLists.OutOfBoundsHandling outOfBoundsHandling) {
		return ValueLists.createIdToValueFunction(valueToIdFunction, values, outOfBoundsHandling);
	}
}
