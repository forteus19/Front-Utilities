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
import com.mojang.datafixers.util.Function7;
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
	
	public static <B, C, T1, T2, T3, T4, T5, T6, T7> PacketCodec<B, C> tuple(
		PacketCodec<? super B, T1> codec1,
		Function<C, T1> from1,
		PacketCodec<? super B, T2> codec2,
		Function<C, T2> from2,
		PacketCodec<? super B, T3> codec3,
		Function<C, T3> from3,
		PacketCodec<? super B, T4> codec4,
		Function<C, T4> from4,
		PacketCodec<? super B, T5> codec5,
		Function<C, T5> from5,
		PacketCodec<? super B, T6> codec6,
		Function<C, T6> from6,
		PacketCodec<? super B, T7> codec7,
		Function<C, T7> from7,
		Function7<T1, T2, T3, T4, T5, T6, T7, C> to
	) {
		return new PacketCodec<>() {
			@Override
			public C decode(B buf) {
				T1 t1 = codec1.decode(buf);
				T2 t2 = codec2.decode(buf);
				T3 t3 = codec3.decode(buf);
				T4 t4 = codec4.decode(buf);
				T5 t5 = codec5.decode(buf);
				T6 t6 = codec6.decode(buf);
				T7 t7 = codec7.decode(buf);
				return to.apply(t1, t2, t3, t4, t5, t6, t7);
			}
			
			@Override
			public void encode(B buf, C value) {
				codec1.encode(buf, from1.apply(value));
				codec2.encode(buf, from2.apply(value));
				codec3.encode(buf, from3.apply(value));
				codec4.encode(buf, from4.apply(value));
				codec5.encode(buf, from5.apply(value));
				codec6.encode(buf, from6.apply(value));
				codec7.encode(buf, from7.apply(value));
			}
		};
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
