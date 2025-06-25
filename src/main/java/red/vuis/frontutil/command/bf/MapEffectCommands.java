package red.vuis.frontutil.command.bf;

import java.util.List;

import com.boehmod.blockfront.map.effect.BulletTracerSpawnerMapEffect;
import com.boehmod.blockfront.map.effect.LoopingSoundPointMapEffect;
import com.boehmod.blockfront.map.effect.ParticleEmitterMapEffect;
import com.boehmod.blockfront.util.RegistryUtils;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import red.vuis.frontutil.mixin.BulletTracerSpawnerMapEffectAccessor;
import red.vuis.frontutil.util.AddonUtils;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public final class MapEffectCommands {
	private MapEffectCommands() {
	}
	
	@Nullable
	public static BulletTracerSpawnerMapEffect parseBulletTracerSpawner(@UnmodifiableView List<String> args) {
		if (!(args.size() == 6 || args.size() == 11)) {
			return null;
		}
		
		var x = AddonUtils.parse(Double::parseDouble, args.get(0));
		var y = AddonUtils.parse(Double::parseDouble, args.get(1));
		var z = AddonUtils.parse(Double::parseDouble, args.get(2));
		var endPosX = AddonUtils.parse(Double::parseDouble, args.get(3));
		var endPosY = AddonUtils.parse(Double::parseDouble, args.get(4));
		var endPosZ = AddonUtils.parse(Double::parseDouble, args.get(5));
		
		if (AddonUtils.anyEmpty(x, y, z, endPosX, endPosY, endPosZ)) {
			return null;
		}
		
		var mapEffect = new BulletTracerSpawnerMapEffect(new Vec3(x.get(), y.get(), z.get()), new Vec3(endPosX.get(), endPosY.get(), endPosZ.get()));
		
		if (args.size() == 11) {
			var chance = AddonUtils.parse(Float::parseFloat, args.get(6));
			var playSound = AddonUtils.parse(Boolean::parseBoolean, args.get(7));
			var spreadX = AddonUtils.parse(Double::parseDouble, args.get(8));
			var spreadY = AddonUtils.parse(Double::parseDouble, args.get(9));
			var spreadZ = AddonUtils.parse(Double::parseDouble, args.get(10));
			
			if (AddonUtils.anyEmpty(x, y, z, endPosX, endPosY, endPosZ)) {
				return null;
			}
			
			var accessor = (BulletTracerSpawnerMapEffectAccessor) mapEffect;
			accessor.setChance(chance.get());
			accessor.setPlaySound(playSound.get());
			mapEffect.method_3105(new Vec3(spreadX.get(), spreadY.get(), spreadZ.get()));
		}
		
		return mapEffect;
	}
	
	@Nullable
	public static LoopingSoundPointMapEffect parseLoopingSoundPoint(@UnmodifiableView List<String> args) {
		if (args.size() != 5) {
			return null;
		}
		
		var sound = AddonUtils.parse(RegistryUtils::retrieveSoundEvent, args.get(0));
		var maxTime = AddonUtils.parse(Integer::parseInt, args.get(1));
		var x = AddonUtils.parse(Double::parseDouble, args.get(2));
		var y = AddonUtils.parse(Double::parseDouble, args.get(3));
		var z = AddonUtils.parse(Double::parseDouble, args.get(4));
		
		if (AddonUtils.anyEmpty(sound, maxTime, x, y, z)) {
			return null;
		}
		
		return new LoopingSoundPointMapEffect(sound.get(), new Vec3(x.get(), y.get(), z.get()), maxTime.get());
	}
	
	@Nullable
	public static ParticleEmitterMapEffect parseParticleEmitter(@UnmodifiableView List<String> args) {
		if (!(args.size() == 5 || args.size() == 7)) {
			return null;
		}
		
		var particle = AddonUtils.parse(RegistryUtils::retrieveParticleType, args.get(0));
		var maxTick = AddonUtils.parse(Integer::parseInt, args.get(1));
		var x = AddonUtils.parse(Double::parseDouble, args.get(2));
		var y = AddonUtils.parse(Double::parseDouble, args.get(3));
		var z = AddonUtils.parse(Double::parseDouble, args.get(4));
		
		if (AddonUtils.anyEmpty(particle, maxTick, x, y, z)) {
			return null;
		}
		
		var mapEffect = new ParticleEmitterMapEffect((SimpleParticleType) particle.get().get(), new Vec3(x.get(), y.get(), z.get()), maxTick.get());
		
		if (args.size() == 7) {
			var sound = AddonUtils.parse(RegistryUtils::retrieveSoundEvent, args.get(5));
			var soundVolume = AddonUtils.parse(Float::parseFloat, args.get(6));
			
			if (AddonUtils.anyEmpty(sound, soundVolume)) {
				return null;
			}
			
			mapEffect.method_3111(sound.get(), soundVolume.get());
		}
		
		return mapEffect;
	}
}
