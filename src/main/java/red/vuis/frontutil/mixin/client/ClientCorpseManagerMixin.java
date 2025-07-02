package red.vuis.frontutil.mixin.client;

import java.util.List;
import java.util.function.Predicate;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.corpse.ClientCorpseManager;
import com.boehmod.blockfront.client.corpse.render.RagdollRenderer;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.client.data.config.AddonClientConfig;

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
	
	@Inject(
		method = "render",
		at = @At("HEAD"),
		cancellable = true
	)
	private void checkCorpsesEnabled(Minecraft minecraft, BFClientManager manager, BFClientPlayerData playerData, ClientLevel level, LocalPlayer player, PoseStack poseStack, float delta, CallbackInfo ci) {
		if (!AddonClientConfig.getRenderCorpses()) {
			ci.cancel();
		}
	}
}
