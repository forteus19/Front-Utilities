package red.vuis.frontutil.command;

import com.boehmod.blockfront.client.mapeffect.AbstractMapEffect;
import com.boehmod.blockfront.client.mapeffect.FallingArtilleryMapEffect;
import com.boehmod.blockfront.client.mapeffect.LoopingSoundPointMapEffect;
import com.boehmod.blockfront.client.mapeffect.ParticleEmitterMapEffect;
import com.boehmod.blockfront.game.AbstractCapturePoint;
import com.boehmod.blockfront.util.RegistryUtils;
import com.boehmod.blockfront.util.math.FDSPose;

import red.vuis.frontutil.mixin.FallingArtilleryMapEffectAccessor;
import red.vuis.frontutil.mixin.ParticleEmitterMapEffectAccessor;

public final class InfoFunctions {
	private InfoFunctions() {
	}
	
	public static String pose(FDSPose pose) {
		return String.format(
			"(x: %.2f, y: %.2f, z: %.2f)",
			pose.position.x,
			pose.position.y,
			pose.position.z
		);
	}
	
	public static String capturePoint(AbstractCapturePoint<?> capturePoint) {
		return String.format("%s %s", capturePoint.name, pose(capturePoint));
	}
	
	public static String mapEffect(AbstractMapEffect absEffect) {
		return switch (absEffect) {
			case FallingArtilleryMapEffect effect -> {
				var accessor = (FallingArtilleryMapEffectAccessor) effect;
				yield String.format(
					"fallingArtillery (minX: %.2f, minZ: %.2f, maxX: %.2f, maxZ: %.2f)",
					accessor.getMin().x,
					accessor.getMin().y,
					accessor.getMax().x,
					accessor.getMax().y
				);
			}
			case LoopingSoundPointMapEffect effect -> String.format(
				"loopingSoundPoint (sound: %s, maxTime: %d, x: %.2f, y: %.2f, z: %.2f, volume: %.2f, pitch: %.2f, activationDistance: %.2f)",
				effect.sound != null ? RegistryUtils.getSoundEventId(effect.sound.get()) : "null",
				effect.maxTime,
				effect.position.x,
				effect.position.y,
				effect.position.z,
				effect.volume,
				effect.pitch,
				effect.activationDistance
			);
			case ParticleEmitterMapEffect effect -> {
				var accessor = (ParticleEmitterMapEffectAccessor) effect;
				yield String.format(
					"particleEmitter (particle: %s, maxTick: %d, x: %.2f, y: %.2f, z: %.2f, sound: %s, soundVolume: %.2f)",
					accessor.getParticle() != null ? RegistryUtils.getParticleTypeId(accessor.getParticle()) : "null",
					accessor.getMaxTick(),
					effect.getPosition().x,
					effect.getPosition().y,
					effect.getPosition().z,
					accessor.getSound() != null ? RegistryUtils.getSoundEventId(accessor.getSound().get()) : "null",
					accessor.getSoundVolume()
				);
			}
			default -> "unknown";
		};
	}
}
