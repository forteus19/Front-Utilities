package red.vuis.frontutil.mixin.client;

import java.util.List;
import java.util.function.Predicate;

import com.boehmod.blockfront.client.corpse.ClientCorpseManager;
import com.boehmod.blockfront.client.corpse.render.RagdollRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientCorpseManager.class)
public abstract class ClientCorpseManagerMixin {
	@WrapOperation(
		method = "update",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;removeIf(Ljava/util/function/Predicate;)Z"
		)
	)
	private boolean checkFrustrumIsNull(List<RagdollRenderer> instance, Predicate<RagdollRenderer> predicate, Operation<Boolean> operation, @Local Frustum frustum) {
		if (frustum != null) {
			return operation.call(instance, predicate);
		}
		return false;
	}
}
