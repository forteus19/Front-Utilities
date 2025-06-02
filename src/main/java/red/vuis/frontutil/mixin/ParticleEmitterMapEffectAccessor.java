package red.vuis.frontutil.mixin;

import java.util.function.Supplier;

import com.boehmod.blockfront.client.mapeffect.ParticleEmitterMapEffect;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ParticleEmitterMapEffect.class)
public interface ParticleEmitterMapEffectAccessor {
	@Accessor("field_3165")
	SimpleParticleType getParticle();

	@Accessor("field_3168")
	int getMaxTick();

	@Accessor("field_3167")
	Supplier<SoundEvent> getSound();

	@Accessor("field_3166")
	float getSoundVolume();
}
