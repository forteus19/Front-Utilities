package red.vuis.frontutil.data;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.boehmod.blockfront.common.gun.GunFireMode;
import com.boehmod.blockfront.common.match.BFCountry;
import com.boehmod.blockfront.common.match.Loadout;
import com.boehmod.blockfront.common.match.MatchClass;
import com.boehmod.blockfront.unnamed.BF_959;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;

import red.vuis.frontutil.setup.LoadoutIndex;

public final class AddonCodecs {
	public static final Codec<GunFireMode> GUN_FIRE_MODE = stringKey(GunFireMode.values(), mode -> mode.getName().toLowerCase(), key -> "Invalid fire mode: " + key);
	public static final Codec<BF_959> GUN_FIRE_TYPE = stringKey(BF_959.values(), mode -> mode.name().toLowerCase(), key -> "Invalid fire type: " + key);
	public static final Codec<Loadout> LOADOUT = RecordCodecBuilder.create(instance ->
		instance.group(
			ItemStack.CODEC.optionalFieldOf("primary").forGetter(wrapLoadoutGetter(Loadout::getPrimary)),
			ItemStack.CODEC.optionalFieldOf("secondary").forGetter(wrapLoadoutGetter(Loadout::getSecondary)),
			ItemStack.CODEC.optionalFieldOf("melee").forGetter(wrapLoadoutGetter(Loadout::getMelee)),
			ItemStack.CODEC.optionalFieldOf("off_hand").forGetter(wrapLoadoutGetter(Loadout::getOffHand)),
			ItemStack.CODEC.optionalFieldOf("head").forGetter(wrapLoadoutGetter(Loadout::getHead)),
			ItemStack.CODEC.optionalFieldOf("chest").forGetter(wrapLoadoutGetter(Loadout::getChest)),
			ItemStack.CODEC.optionalFieldOf("legs").forGetter(wrapLoadoutGetter(Loadout::getLegs)),
			ItemStack.CODEC.optionalFieldOf("feet").forGetter(wrapLoadoutGetter(Loadout::getFeet)),
			ItemStack.CODEC.listOf().fieldOf("extra").forGetter(Loadout::getExtra),
			Codec.INT.fieldOf("minimum_xp").forGetter(Loadout::getMinimumXp)
		).apply(instance, AddonCodecs::newLoadout)
	);
	// Absolute behemoth of a codec
	public static final Codec<Map<LoadoutIndex.Identifier, List<Loadout>>> LOADOUT_INDEX =
		Codec.unboundedMap(
			Codec.STRING, Codec.unboundedMap(
				Codec.STRING, Codec.unboundedMap(
					Codec.STRING, LOADOUT.listOf()
				)
			)
		).xmap(
			nested -> {
				Map<LoadoutIndex.Identifier, List<Loadout>> flat = new Object2ObjectOpenHashMap<>();
				
				for (Map.Entry<String, Map<String, Map<String, List<Loadout>>>> countryEntry : nested.entrySet()) {
					BFCountry country = LoadoutIndex.COUNTRIES.get(countryEntry.getKey());
					if (country == null) {
						continue;
					}
					
					for (Map.Entry<String, Map<String, List<Loadout>>> skinEntry : countryEntry.getValue().entrySet()) {
						String skin = skinEntry.getKey();
						if (!LoadoutIndex.SKINS.getOrDefault(country, List.of()).contains(skin)) {
							continue;
						}
						
						for (Map.Entry<String, List<Loadout>> matchClassEntry : skinEntry.getValue().entrySet()) {
							MatchClass matchClass = LoadoutIndex.MATCH_CLASSES.get(matchClassEntry.getKey());
							if (matchClass == null) {
								continue;
							}
							
							flat.put(new LoadoutIndex.Identifier(country, skin, matchClass), matchClassEntry.getValue());
						}
					}
				}
				
				return flat;
			},
			flat -> {
				Map<String, Map<String, Map<String, List<Loadout>>>> nested = new Object2ObjectOpenHashMap<>();
				
				for (Map.Entry<LoadoutIndex.Identifier, List<Loadout>> entry : flat.entrySet()) {
					LoadoutIndex.Identifier id = entry.getKey();
					nested.computeIfAbsent(id.country().getTag(), k -> new Object2ObjectOpenHashMap<>())
						.computeIfAbsent(id.skin(), k -> new Object2ObjectOpenHashMap<>())
						.put(id.matchClass().getKey(), entry.getValue());
				}
				
				return nested;
			}
		);
	
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
	
	private static Function<Loadout, Optional<ItemStack>> wrapLoadoutGetter(Function<Loadout, ItemStack> original) {
		return loadout -> {
			ItemStack itemStack = original.apply(loadout);
			if (itemStack == ItemStack.EMPTY) {
				return Optional.empty();
			} else {
				return Optional.ofNullable(itemStack);
			}
		};
	}
	
	private static Loadout newLoadout(
		Optional<ItemStack> primary,
		Optional<ItemStack> secondary,
		Optional<ItemStack> melee,
		Optional<ItemStack> offHand,
		Optional<ItemStack> head,
		Optional<ItemStack> chest,
		Optional<ItemStack> legs,
		Optional<ItemStack> feet,
		List<ItemStack> extra,
		int minimumXp
	) {
		return new Loadout(
			primary.orElse(null),
			secondary.orElse(null),
			melee.orElse(null),
			offHand.orElse(null),
			head.orElse(null),
			chest.orElse(null),
			legs.orElse(null),
			feet.orElse(null)
		)
			.addExtra(extra)
			.setMinimumXp(minimumXp);
	}
}
