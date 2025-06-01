package red.vuis.frontutil.command;

import com.boehmod.blockfront.client.mapeffect.AbstractMapEffect;
import com.boehmod.blockfront.client.mapeffect.FallingArtilleryMapEffect;
import com.boehmod.blockfront.client.mapeffect.LoopingSoundPointMapEffect;
import com.boehmod.blockfront.client.mapeffect.ParticleEmitterMapEffect;
import com.boehmod.blockfront.util.RegistryUtils;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import red.vuis.frontutil.mixin.FallingArtilleryMapEffectAccessor;
import red.vuis.frontutil.mixin.ParticleEmitterMapEffectAccessor;
import red.vuis.frontutil.util.AddonUtils;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public final class MapEffectCommands {
    private MapEffectCommands() {}

    public static String getInfo(AbstractMapEffect absEffect) {
        return switch (absEffect) {
            case FallingArtilleryMapEffect effect -> {
                FallingArtilleryMapEffectAccessor accessor = (FallingArtilleryMapEffectAccessor) effect;
                yield String.format(
                        "fallingArtillery (minX: %.2f, minZ: %.2f, maxX: %.2f, maxZ: %.2f)",
                        accessor.getMin().x,
                        accessor.getMin().y,
                        accessor.getMax().x,
                        accessor.getMax().y
                );
            }
            case LoopingSoundPointMapEffect effect ->
                String.format(
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
                ParticleEmitterMapEffectAccessor accessor = (ParticleEmitterMapEffectAccessor) effect;
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
