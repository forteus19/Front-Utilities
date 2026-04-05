package red.vuis.frontutil.mixin.client;

import java.util.Random;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.event.tick.GunSpreadTickable;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.common.item.BFWeaponItem;
import com.boehmod.blockfront.common.player.PlayerCloudData;
import com.boehmod.blockfront.game.AbstractGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.data.AddonCommonData;
import red.vuis.frontutil.data.OldSpreadConfig;
import red.vuis.frontutil.data.OldSpreadConfigs;

@Mixin(GunSpreadTickable.class)
public abstract class GunSpreadTickableMixin {
	@Inject(
		method = "run",
		at = @At("HEAD"),
		cancellable = true
	)
	private void runOldSpread(
		ClientTickEvent.@NotNull Post event,
		@NotNull Random random,
		@NotNull MinecraftClient client,
		@NotNull ClientPlayerDataHandler dataHandler,
		@NotNull BFClientManager manager,
		@Nullable ClientPlayerEntity player,
		@Nullable ClientWorld world,
		@NotNull BFClientPlayerData playerData,
		@NotNull PlayerCloudData cloudData,
		@NotNull Vec3d pos,
		@NotNull BlockPos blockPos,
		@Nullable AbstractGame<?, ?, ?> game,
		boolean par13,
		float renderTime,
		CallbackInfo ci
	) {
		if (!AddonCommonData.getInstance().useOldSpread || player == null) {
			return;
		}
		
		Item heldItem = player.getMainHandStack().getItem();
		if (!(heldItem instanceof BFWeaponItem<?> weaponItem)) {
			return;
		}
		OldSpreadConfig config = OldSpreadConfigs.get(weaponItem).config();
		
		OldSpreadConfig.prevSpread = OldSpreadConfig.currentSpread;
		OldSpreadConfig.currentSpread = MathHelper.clamp(OldSpreadConfig.currentSpread + config.calculateDelta(player), config.idleSpread(), 1.0f);
		
		ci.cancel();
	}
}
