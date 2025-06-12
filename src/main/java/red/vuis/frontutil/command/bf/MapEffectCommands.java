package red.vuis.frontutil.command.bf;

import com.boehmod.blockfront.map.effect.BulletTracerSpawnerMapEffect;
import com.boehmod.blockfront.map.effect.LoopingSoundPointMapEffect;
import com.boehmod.blockfront.map.effect.ParticleEmitterMapEffect;
import com.boehmod.blockfront.util.RegistryUtils;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.mixin.BulletTracerSpawnerMapEffectAccessor;
import red.vuis.frontutil.util.AddonUtils;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public final class MapEffectCommands {
	private MapEffectCommands() {
	}
	
	@Nullable
	public static BulletTracerSpawnerMapEffect parseBulletTracerSpawner(String[] args) {
		if (!(args.length == 6 || args.length == 11)) {
			return null;
		}
		
		var x = AddonUtils.parse(Double::parseDouble, args[0]);
		var y = AddonUtils.parse(Double::parseDouble, args[1]);
		var z = AddonUtils.parse(Double::parseDouble, args[2]);
		var endPosX = AddonUtils.parse(Double::parseDouble, args[3]);
		var endPosY = AddonUtils.parse(Double::parseDouble, args[4]);
		var endPosZ = AddonUtils.parse(Double::parseDouble, args[5]);
		
		if (AddonUtils.anyEmpty(x, y, z, endPosX, endPosY, endPosZ)) {
			return null;
		}
		
		var mapEffect = new BulletTracerSpawnerMapEffect(new Vec3(x.get(), y.get(), z.get()), new Vec3(endPosX.get(), endPosY.get(), endPosZ.get()));
		
		if (args.length == 11) {
			var chance = AddonUtils.parse(Float::parseFloat, args[6]);
			var playSound = AddonUtils.parse(Boolean::parseBoolean, args[7]);
			var spreadX = AddonUtils.parse(Double::parseDouble, args[8]);
			var spreadY = AddonUtils.parse(Double::parseDouble, args[9]);
			var spreadZ = AddonUtils.parse(Double::parseDouble, args[10]);
			
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
	public static LoopingSoundPointMapEffect parseLoopingSoundPoint(String[] args) {
		if (args.length != 5) {
			return null;
		}
		
		var sound = AddonUtils.parse(RegistryUtils::retrieveSoundEvent, args[0]);
		var maxTime = AddonUtils.parse(Integer::parseInt, args[1]);
		var x = AddonUtils.parse(Double::parseDouble, args[2]);
		var y = AddonUtils.parse(Double::parseDouble, args[3]);
		var z = AddonUtils.parse(Double::parseDouble, args[4]);
		
		if (AddonUtils.anyEmpty(sound, maxTime, x, y, z)) {
			return null;
		}
		
		return new LoopingSoundPointMapEffect(sound.get(), new Vec3(x.get(), y.get(), z.get()), maxTime.get());
	}
	
	@Nullable
	public static ParticleEmitterMapEffect parseParticleEmitter(String[] args) {
		if (!(args.length == 5 || args.length == 7)) {
			return null;
		}
		
		var particle = AddonUtils.parse(RegistryUtils::retrieveParticleType, args[0]);
		var maxTick = AddonUtils.parse(Integer::parseInt, args[1]);
		var x = AddonUtils.parse(Double::parseDouble, args[2]);
		var y = AddonUtils.parse(Double::parseDouble, args[3]);
		var z = AddonUtils.parse(Double::parseDouble, args[4]);
		
		if (AddonUtils.anyEmpty(particle, maxTick, x, y, z)) {
			return null;
		}
		
		var mapEffect = new ParticleEmitterMapEffect((SimpleParticleType) particle.get().get(), new Vec3(x.get(), y.get(), z.get()), maxTick.get());
		
		if (args.length == 7) {
			var sound = AddonUtils.parse(RegistryUtils::retrieveSoundEvent, args[5]);
			var soundVolume = AddonUtils.parse(Float::parseFloat, args[6]);
			
			if (AddonUtils.anyEmpty(sound, soundVolume)) {
				return null;
			}
			
			mapEffect.method_3111(sound.get(), soundVolume.get());
		}
		
		return mapEffect;
	}
}
