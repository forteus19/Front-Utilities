package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.client.event.BFRenderLevelSubscriber;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import red.vuis.frontutil.client.data.config.AddonClientConfig;

@Debug(export = true)
@Mixin(BFRenderLevelSubscriber.class)
public abstract class BFRenderLevelSubscriberMixin {
	@Redirect(
		method = "renderAfterLevel",
		at = @At(
			value = "INVOKE",
			target = "Ljava/lang/Math;max(FF)F",
			ordinal = 0
		)
	)
	private static float disableDeathBlur(float a, float b) {
		return AddonClientConfig.getEnableDeathFade() ? Math.max(a, b) : 0;
	}
}
