package red.vuis.frontutil.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.boehmod.blockfront.common.entity.base.IProducedProjectileEntity;
import com.boehmod.blockfront.common.gun.GunDamageConfig;
import com.boehmod.blockfront.common.gun.GunFireConfig;
import com.boehmod.blockfront.common.gun.GunFireMode;
import com.boehmod.blockfront.common.gun.GunMagType;
import com.boehmod.blockfront.common.item.GunItem;
import com.boehmod.blockfront.unnamed.BF_959;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.mixin.GunItemAccessor;
import red.vuis.frontutil.util.AddonEntityUtils;

import static red.vuis.frontutil.util.AddonAccessors.applyGunItem;

public record GunModifier(
	Optional<Ammo> ammo,
	Optional<List<Damage>> damage,
	Optional<List<FireMode>> fireModes,
	Optional<Float> weight
) {
	public static final Map<RegistryEntry<Item>, GunModifier> ACTIVE = new Object2ObjectOpenHashMap<>();
	public static final Codec<GunModifier> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Ammo.CODEC.optionalFieldOf("ammo").forGetter(GunModifier::ammo),
			Damage.CODEC.listOf(1, Integer.MAX_VALUE).optionalFieldOf("damage").forGetter(GunModifier::damage),
			FireMode.CODEC.listOf(1, Integer.MAX_VALUE).optionalFieldOf("fire_modes").forGetter(GunModifier::fireModes),
			Codec.FLOAT.optionalFieldOf("weight").forGetter(GunModifier::weight)
		).apply(instance, GunModifier::new)
	);
	public static final PacketCodec<RegistryByteBuf, GunModifier> PACKET_CODEC = PacketCodec.tuple(
		Ammo.PACKET_CODEC.collect(PacketCodecs::optional), GunModifier::ammo,
		Damage.PACKET_CODEC.collect(PacketCodecs.toList()).collect(PacketCodecs::optional), GunModifier::damage,
		FireMode.PACKET_CODEC.collect(PacketCodecs.toList()).collect(PacketCodecs::optional), GunModifier::fireModes,
		PacketCodecs.FLOAT.collect(PacketCodecs::optional), GunModifier::weight,
		GunModifier::new
	);
	
	public GunModifier(Ammo ammo, List<Damage> damage, List<FireMode> fireModes, float weight) {
		this(
			Optional.of(ammo),
			Optional.of(damage),
			Optional.of(fireModes),
			Optional.of(weight)
		);
	}
	
	public void apply(@NotNull GunItem item) {
		ammo.ifPresent(ammo -> Ammo.apply(ammo, item));
		damage.ifPresent(damage -> Damage.apply(damage, item));
		fireModes.ifPresent(fireModes -> FireMode.apply(fireModes, item));
		weight.ifPresent(item::weight);
	}
	
	public record Ammo(
		int magazine,
		int reserve
	) {
		public static final Codec<Ammo> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				Codec.intRange(0, Integer.MAX_VALUE).fieldOf("magazine").forGetter(Ammo::magazine),
				Codec.intRange(0, Integer.MAX_VALUE).fieldOf("reserve").forGetter(Ammo::reserve)
			).apply(instance, Ammo::new)
		);
		public static final PacketCodec<ByteBuf, Ammo> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, Ammo::magazine,
			PacketCodecs.VAR_INT, Ammo::reserve,
			Ammo::new
		);
		
		public static Ammo of(GunMagType magType) {
			return new Ammo(magType.capacity(), magType.maxAmmo());
		}
		
		private static void apply(Ammo ammo, @NotNull GunItem item) {
			Map<String, GunMagType> magIdMap = applyGunItem(item, GunItemAccessor::getMagIdMap);
			GunMagType prevMagType = magIdMap.get("default");
			magIdMap.replace("default", new GunMagType(prevMagType.isDefault(), prevMagType.displayName(), ammo.magazine, ammo.reserve));
		}
	}
	
	public record Damage(
		float body,
		float head,
		float minDist
	) {
		public static final Codec<Damage> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				Codec.floatRange(0f, Float.MAX_VALUE).fieldOf("body").forGetter(Damage::body),
				Codec.floatRange(0f, Float.MAX_VALUE).fieldOf("head").forGetter(Damage::head),
				Codec.floatRange(0f, Float.MAX_VALUE).optionalFieldOf("min_dist", 0f).forGetter(Damage::minDist)
			).apply(instance, Damage::new)
		);
		public static final PacketCodec<ByteBuf, Damage> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.FLOAT, Damage::body,
			PacketCodecs.FLOAT, Damage::head,
			PacketCodecs.FLOAT, Damage::minDist,
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
	
	public record FireMode(
		BF_959 type,
		GunFireMode mode,
		int ticks,
		int numInstances,
		Optional<RegistryEntry<EntityType<?>>> entity
	) {
		public static final Codec<FireMode> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				AddonCodecs.GUN_FIRE_TYPE.fieldOf("type").forGetter(FireMode::type),
				AddonCodecs.GUN_FIRE_MODE.fieldOf("mode").forGetter(FireMode::mode),
				Codec.intRange(1, Integer.MAX_VALUE).fieldOf("ticks").forGetter(FireMode::ticks),
				Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("num_instances", 1).forGetter(FireMode::numInstances),
				Registries.ENTITY_TYPE.getEntryCodec().optionalFieldOf("entity").forGetter(FireMode::entity)
			).apply(instance, FireMode::new)
		);
		public static final PacketCodec<RegistryByteBuf, FireMode> PACKET_CODEC = PacketCodec.tuple(
			AddonPacketCodecs.GUN_FIRE_TYPE, FireMode::type,
			AddonPacketCodecs.GUN_FIRE_MODE, FireMode::mode,
			PacketCodecs.VAR_INT, FireMode::ticks,
			PacketCodecs.VAR_INT, FireMode::numInstances,
			PacketCodecs.registryEntry(RegistryKeys.ENTITY_TYPE).collect(PacketCodecs::optional), FireMode::entity,
			FireMode::new
		);
		
		public static List<FireMode> of(GunFireConfig[] fireConfigs) {
			return Arrays.stream(fireConfigs)
				.map(config -> new FireMode(
					config.method_4023(),
					config.getMode(),
					config.getFireRate(),
					config.method_4026(),
					Optional.ofNullable(config.method_4024())
						.map(Supplier::get)
						.map(Registries.ENTITY_TYPE::getEntry)
				))
				.toList();
		}
		
		@SuppressWarnings("unchecked")
		private static void apply(List<FireMode> fireModes, @NotNull GunItem item) {
			List<GunFireConfig> fireConfigs = new ArrayList<>();
			
			for (FireMode fireMode : fireModes) {
				if (fireMode.type == BF_959.ENTITY && fireMode.entity.isPresent()) {
					EntityType<?> entityType = fireMode.entity.orElseThrow().value();
					
					if (!AddonEntityUtils.PRODUCED_PROJECTILES.contains(entityType)) {
						AddonConstants.LOGGER.error(
							"Entity type {} is not a valid projectile! Discarding fire mode.",
							Registries.ENTITY_TYPE.getId(entityType)
						);
						continue;
					}
				}
				
				switch (fireMode.type) {
					case BULLET -> fireConfigs.add(new GunFireConfig(
						fireMode.mode, fireMode.ticks, fireMode.numInstances
					));
					case ENTITY -> {
						if (fireMode.entity.isEmpty()) {
							AddonConstants.LOGGER.error("Entity is not specified for fire mode of type entity! Discarding fire mode.");
							break;
						}
						
						fireConfigs.add(new GunFireConfig(
							fireMode.mode, fireMode.ticks, () -> (EntityType<? extends IProducedProjectileEntity>) fireMode.entity.orElseThrow().value()
						));
					}
				}
			}
			
			if (fireConfigs.isEmpty()) {
				AddonConstants.LOGGER.error(
					"No valid fire modes for item {}! Not applying.",
					Registries.ITEM.getId(item)
				);
			}
			
			item.fireModes(fireConfigs.toArray(GunFireConfig[]::new));
		}
	}
}
