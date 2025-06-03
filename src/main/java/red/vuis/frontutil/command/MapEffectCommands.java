package red.vuis.frontutil.command;

import com.boehmod.blockfront.client.mapeffect.LoopingSoundPointMapEffect;
import com.boehmod.blockfront.client.mapeffect.ParticleEmitterMapEffect;
import com.boehmod.blockfront.util.RegistryUtils;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.util.AddonUtils;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public final class MapEffectCommands {
	private MapEffectCommands() {
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
