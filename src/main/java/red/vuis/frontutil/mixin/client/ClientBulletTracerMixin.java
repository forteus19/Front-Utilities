package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.client.gun.bullet.ClientBulletTracer;
import com.boehmod.blockfront.common.item.GunItem;
import com.boehmod.blockfront.util.math.MathUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import red.vuis.frontutil.client.data.AddonClientData;
import red.vuis.frontutil.data.OldSpreadConfig;
import red.vuis.frontutil.data.OldSpreadConfigs;

@Mixin(ClientBulletTracer.class)
public abstract class ClientBulletTracerMixin {
	@Inject(
		method = "method_4269(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/math/random/Random;)Lnet/minecraft/util/math/Vec3d;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void oldSpreadCalculation(ClientPlayerEntity player, Random random, CallbackInfoReturnable<Vec3d> cir) {
		if (!(AddonClientData.getInstance().useOldSpread && player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof GunItem item)) {
			return;
		}
		
		OldSpreadConfig config = OldSpreadConfigs.get(item).config();
		
		float scale = OldSpreadConfig.currentSpread * config.spreadMaxAngle() * 0.5f;
		if (GunItem.field_4019) { // ADSing
			scale *= 0.75f;
		}
		
		float rotX = (float) random.nextGaussian() * scale;
		float rotY = (float) random.nextGaussian() * scale;
		
		cir.setReturnValue(MathUtils.lookingVec(player, rotX, rotY));
	}
}
