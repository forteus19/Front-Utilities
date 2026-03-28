package red.vuis.frontutil.mixin.client;

import java.util.Random;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.event.tick.PlayerTickable;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.common.player.PlayerCloudData;
import com.boehmod.blockfront.game.AbstractGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTickable.class)
public abstract class PlayerTickableMixin {
	@Inject(
		method = "run",
		at = @At("TAIL")
	)
	private void updateExtraFields(
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
		// TODO
	}
}
