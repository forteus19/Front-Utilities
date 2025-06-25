package red.vuis.frontutil.mixin;

import com.boehmod.bflib.fds.tag.FDSTagCompound;
import com.boehmod.blockfront.map.effect.ParticleEmitterMapEffect;
import com.boehmod.blockfront.map.effect.PositionedMapEffect;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import red.vuis.frontutil.inject.ParticleEmitterMapEffectInject;

@Mixin(ParticleEmitterMapEffect.class)
public abstract class ParticleEmitterMapEffectMixin extends PositionedMapEffect implements ParticleEmitterMapEffectInject {
	@Unique
	private Vec3 frontutil$velocity = new Vec3(0, 0, 0);
	
	@ModifyArgs(
		method = "updateGameClient",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/util/ClientUtils;spawnParticle(Lnet/minecraft/client/Minecraft;Lcom/boehmod/blockfront/client/BFClientManager;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
		)
	)
	private void setVelocityArgs(Args args) {
		args.set(7, frontutil$velocity.x);
		args.set(8, frontutil$velocity.y);
		args.set(9, frontutil$velocity.z);
	}
	
	@Inject(
		method = "writeToFDS",
		at = @At("TAIL")
	)
	private void writeVelocityFDS(FDSTagCompound root, CallbackInfo ci) {
		root.setDouble("velX", frontutil$velocity.x);
		root.setDouble("velY", frontutil$velocity.y);
		root.setDouble("velZ", frontutil$velocity.z);
	}
	
	@Inject(
		method = "readFromFDS",
		at = @At("TAIL")
	)
	private void readVelocityFDS(FDSTagCompound root, CallbackInfo ci) {
		frontutil$velocity = new Vec3(
			root.getDouble("velX"),
			root.getDouble("velY"),
			root.getDouble("velZ")
		);
	}
	
	@Override
	public Vec3 frontutil$getVelocity() {
		return frontutil$velocity;
	}
	
	@Override
	public void frontutil$setVelocity(Vec3 velocity) {
		this.frontutil$velocity = velocity;
	}
}
