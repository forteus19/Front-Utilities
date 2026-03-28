package red.vuis.frontutil.mixin.client;

import java.util.Random;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.event.tick.PlayerTickable;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.common.gun.GunCameraConfig;
import com.boehmod.blockfront.common.item.GunItem;
import com.boehmod.blockfront.unnamed.BF_1163;
import com.boehmod.blockfront.util.GunUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.client.data.AddonClientData;
import red.vuis.frontutil.data.OldSpreadConfig;
import red.vuis.frontutil.data.OldSpreadConfigs;

@Mixin(GunUtils.class)
public abstract class GunUtilsMixin {
	@Inject(
		method = "method_1424",
		at = @At("HEAD")
	)
	private static void earlyFireSpreadIncrease(
		MinecraftClient client,
		Random random,
		BFClientManager manager,
		ClientPlayerDataHandler dataHandler,
		GunItem item,
		ClientPlayerEntity player,
		ClientWorld world,
		ItemStack heldStack,
		CallbackInfo ci
	) {
		OldSpreadConfigs.Entry spreadEntry = OldSpreadConfigs.get(item);
		if (spreadEntry.earlyFireSpread()) {
			OldSpreadConfig.currentSpread += spreadEntry.config().spreadWhenFired();
		}
	}
	
	@Redirect(
		method = "method_1424",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/unnamed/BF_1163;method_5621(Lcom/boehmod/blockfront/common/gun/GunCameraConfig;)V",
			ordinal = 0
		)
	)
	private static void oldRecoil(
		GunCameraConfig cameraConfig,
		@Local(argsOnly = true) MinecraftClient client,
		@Local(argsOnly = true) Random random,
		@Local(argsOnly = true) ClientPlayerDataHandler dataHandler,
		@Local(argsOnly = true) GunItem item,
		@Local(argsOnly = true) ClientPlayerEntity player
	) {
		if (!AddonClientData.getInstance().useOldSpread) {
			BF_1163.method_5621(cameraConfig);
			return;
		}
		
		BFClientPlayerData playerData = dataHandler.getPlayerData(client);
		
		OldSpreadConfigs.Entry spreadEntry = OldSpreadConfigs.get(item);
		if (!spreadEntry.earlyFireSpread()) {
			OldSpreadConfig.currentSpread += spreadEntry.config().spreadWhenFired();
		}
		
		float recoilPitch = OldSpreadConfigs.get(item).config().recoil();
		float recoilYaw = recoilPitch * 0.5f;
		
		if (player.isSneaking()) {
			recoilPitch *= 0.7f;
			recoilYaw += 0.7f;
		}
		if (playerData.method_842()) {
			recoilPitch *= 0.6f;
			recoilYaw *= 0.6f;
		}
		float idk = PlayerTickable.field_153 * 0.75f;
		recoilPitch *= 1.0f - idk;
		recoilYaw *= 1.0f - idk;
		
		player.setPitch(MathHelper.clamp(player.getPitch() - recoilPitch, -90f, 90f));
		if (random.nextFloat() < 0.3f) {
			player.setYaw(player.getYaw() + (random.nextFloat() < 0.5f ? recoilYaw : -recoilYaw));
		}
	}
}
