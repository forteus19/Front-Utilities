package red.vuis.frontutil.util;

import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class AddonRegUtils {
	private AddonRegUtils() {
	}
	
	public static @Nullable SimpleParticleType getSimpleParticle(String id) {
		ParticleType<?> particle = Registries.PARTICLE_TYPE.get(Identifier.tryParse(id));
		return particle instanceof SimpleParticleType simpleParticle ? simpleParticle : null;
	}
}
