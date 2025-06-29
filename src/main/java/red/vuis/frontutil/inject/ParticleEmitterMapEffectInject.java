package red.vuis.frontutil.inject;

import net.minecraft.world.phys.Vec3;

public interface ParticleEmitterMapEffectInject {
	Vec3 frontutil$getVelocity();
	
	void frontutil$setVelocity(Vec3 velocity);
}
