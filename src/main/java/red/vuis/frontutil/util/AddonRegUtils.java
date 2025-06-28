package red.vuis.frontutil.util;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class AddonRegUtils {
	private AddonRegUtils() {
	}
	
	public static @Nullable SimpleParticleType getSimpleParticle(String id) {
		ParticleType<?> particle = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.tryParse(id));
		return particle instanceof SimpleParticleType simpleParticle ? simpleParticle : null;
	}
}
