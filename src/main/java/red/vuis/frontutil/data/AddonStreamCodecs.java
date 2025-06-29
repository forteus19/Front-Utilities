package red.vuis.frontutil.data;

import java.util.List;
import java.util.function.Function;

import com.boehmod.blockfront.common.gun.GunFireMode;
import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import com.boehmod.blockfront.unnamed.BF_959;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.setup.LoadoutIndex;

public final class AddonStreamCodecs {
	public static final StreamCodec<ByteBuf, BFCountry> BF_COUNTRY = enumOrdinal(BFCountry.values());
	public static final StreamCodec<ByteBuf, GunFireMode> GUN_FIRE_MODE = enumOrdinal(GunFireMode.values());
	public static final StreamCodec<ByteBuf, BF_959> GUN_FIRE_TYPE = enumOrdinal(BF_959.values());
	public static final StreamCodec<ByteBuf, MatchClass> MATCH_CLASS = enumOrdinal(MatchClass.values());
	public static final StreamCodec<RegistryFriendlyByteBuf, Loadout> LOADOUT = new StreamCodec<>() {
		@Override
		public @NotNull Loadout decode(@NotNull RegistryFriendlyByteBuf buf) {
			Loadout loadout = new Loadout(
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(buf)
			);
			
			int numExtra = ByteBufCodecs.VAR_INT.decode(buf);
			for (int i = 0; i < numExtra; i++) {
				loadout.addExtra(ItemStack.STREAM_CODEC.decode(buf));
			}
			
			loadout.setMinimumXp(ByteBufCodecs.VAR_INT.decode(buf));
			
			return loadout;
		}
		
		@Override
		public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull Loadout loadout) {
			for (Function<Loadout, ItemStack> slotFunc : LoadoutIndex.SLOT_FUNCS) {
				ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, slotFunc.apply(loadout));
			}
			
			List<ItemStack> extras = loadout.getExtra();
			ByteBufCodecs.VAR_INT.encode(buf, extras.size());
			for (ItemStack extra : extras) {
				ItemStack.STREAM_CODEC.encode(buf, extra);
			}
			
			ByteBufCodecs.VAR_INT.encode(buf, loadout.getMinimumXp());
		}
	};
	
	private AddonStreamCodecs() {
	}
	
	public static <T extends Enum<T>> StreamCodec<ByteBuf, T> enumOrdinal(T[] values, ByIdMap.OutOfBoundsStrategy oobStrategy) {
		return ByteBufCodecs.idMapper(ByIdMap.continuous(Enum::ordinal, values, oobStrategy), Enum::ordinal);
	}
	
	public static <T extends Enum<T>> StreamCodec<ByteBuf, T> enumOrdinal(T[] values) {
		return enumOrdinal(values, ByIdMap.OutOfBoundsStrategy.ZERO);
	}
}
