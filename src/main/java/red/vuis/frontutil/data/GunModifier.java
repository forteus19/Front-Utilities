package red.vuis.frontutil.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.boehmod.blockfront.common.gun.GunDamageConfig;
import com.boehmod.blockfront.common.gun.GunMagType;
import com.boehmod.blockfront.common.item.GunItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.mixin.GunItemAccessor;

public record GunModifier(Optional<Ammo> ammo, Optional<List<Damage>> damage) {
	public static final Map<Holder<Item>, GunModifier> ACTIVE = new Object2ObjectOpenHashMap<>();
	public static final Codec<GunModifier> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Ammo.CODEC.optionalFieldOf("ammo").forGetter(GunModifier::ammo),
			Damage.CODEC.listOf(1, Integer.MAX_VALUE).optionalFieldOf("damage").forGetter(GunModifier::damage)
		).apply(instance, GunModifier::new)
	);
	public static final StreamCodec<ByteBuf, GunModifier> STREAM_CODEC = StreamCodec.composite(
		Ammo.STREAM_CODEC.apply(ByteBufCodecs::optional), GunModifier::ammo,
		Damage.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional), GunModifier::damage,
		GunModifier::new
	);
	
	public GunModifier(Ammo ammo, List<Damage> damage) {
		this(Optional.of(ammo), Optional.of(damage));
	}
	
	public void apply(@NotNull GunItem item) {
		ammo.ifPresent(ammo -> Ammo.apply(ammo, item));
		damage.ifPresent(damage -> Damage.apply(damage, item));
	}
	
	public record Ammo(int magazine, int reserve) {
		public static final Codec<Ammo> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				Codec.intRange(0, Integer.MAX_VALUE).fieldOf("magazine").forGetter(Ammo::magazine),
				Codec.intRange(0, Integer.MAX_VALUE).fieldOf("reserve").forGetter(Ammo::reserve)
			).apply(instance, Ammo::new)
		);
		public static final StreamCodec<ByteBuf, Ammo> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, Ammo::magazine,
			ByteBufCodecs.VAR_INT, Ammo::reserve,
			Ammo::new
		);
		
		public static Ammo of(GunMagType magType) {
			return new Ammo(magType.clipCapacity(), magType.reserveCapacity());
		}
		
		private static void apply(Ammo ammo, @NotNull GunItem item) {
			GunItemAccessor accessor = (GunItemAccessor) (Object) item;
			
			GunMagType prevMagType = accessor.getIdToMagTypeMap().get("default");
			accessor.getIdToMagTypeMap().replace("default", new GunMagType(prevMagType.method_4235(), prevMagType.name(), ammo.magazine, ammo.reserve));
		}
	}
	
	public record Damage(float body, float head, float minDist) {
		public static final Codec<Damage> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				Codec.floatRange(0f, Float.MAX_VALUE).fieldOf("body").forGetter(Damage::body),
				Codec.floatRange(0f, Float.MAX_VALUE).fieldOf("head").forGetter(Damage::head),
				Codec.floatRange(0f, Float.MAX_VALUE).optionalFieldOf("min_dist", 0f).forGetter(Damage::minDist)
			).apply(instance, Damage::new)
		);
		public static final StreamCodec<ByteBuf, Damage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, Damage::body,
			ByteBufCodecs.FLOAT, Damage::head,
			ByteBufCodecs.FLOAT, Damage::minDist,
			Damage::new
		);
		
		public static List<Damage> of(GunDamageConfig config) {
			return config.getEntries().entrySet().stream().map(
				entry -> new Damage(entry.getValue().firstFloat(), entry.getValue().secondFloat(), entry.getKey())
			).toList();
		}
		
		private static void apply(List<Damage> damage, @NotNull GunItem item) {
			GunDamageConfig config = new GunDamageConfig(damage.getFirst().body, damage.getFirst().head);
			for (int i = 1; i < damage.size(); i++) {
				Damage additionalDamage = damage.get(i);
				config.add(additionalDamage.minDist, additionalDamage.body, additionalDamage.head);
			}
			item.damage(config);
		}
	}
}
