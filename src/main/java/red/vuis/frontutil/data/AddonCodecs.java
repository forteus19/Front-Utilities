package red.vuis.frontutil.data;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public final class AddonCodecs {
	public static final Codec<Loadout> LOADOUT = RecordCodecBuilder.create(instance ->
		instance.group(
			ItemStack.OPTIONAL_CODEC.fieldOf("primary").forGetter(Loadout::getPrimary),
			ItemStack.OPTIONAL_CODEC.fieldOf("secondary").forGetter(Loadout::getPrimary),
			ItemStack.OPTIONAL_CODEC.fieldOf("melee").forGetter(Loadout::getPrimary),
			ItemStack.OPTIONAL_CODEC.fieldOf("off_hand").forGetter(Loadout::getPrimary),
			ItemStack.OPTIONAL_CODEC.fieldOf("head").forGetter(Loadout::getPrimary),
			ItemStack.OPTIONAL_CODEC.fieldOf("chest").forGetter(Loadout::getPrimary),
			ItemStack.OPTIONAL_CODEC.fieldOf("legs").forGetter(Loadout::getPrimary),
			ItemStack.OPTIONAL_CODEC.fieldOf("feet").forGetter(Loadout::getPrimary),
			ItemStack.CODEC.listOf().fieldOf("extra").forGetter(Loadout::getExtra),
			Codec.INT.fieldOf("minimum_xp").forGetter(Loadout::getMinimumXp)
		).apply(instance, AddonCodecs::newLoadout)
	);
	public static final Codec<MatchClass> MATCH_CLASS = stringKey(MatchClass.values(), MatchClass::getKey, key -> "Invalid match class: " + key);
	
	private AddonCodecs() {
	}
	
	public static <T> Codec<T> stringKey(T[] values, Function<T, String> keyFunc, Function<String, String> errorMsg) {
		return Codec.STRING.comapFlatMap(
			str -> Arrays.stream(values)
				.filter(value -> keyFunc.apply(value).equals(str))
				.findFirst()
				.map(DataResult::success)
				.orElseGet(() -> DataResult.error(() -> errorMsg.apply(str))),
			keyFunc
		);
	}
	
	public static Loadout newLoadout(
		ItemStack primary,
		ItemStack secondary,
		ItemStack melee,
		ItemStack offHand,
		ItemStack head,
		ItemStack chest,
		ItemStack legs,
		ItemStack feet,
		List<ItemStack> extra,
		int minimumXp
	) {
		return new Loadout(primary, secondary, melee, offHand, head, chest, legs, feet)
			.addExtra(extra)
			.setMinimumXp(minimumXp);
	}
}
