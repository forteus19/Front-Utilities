package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.common.net.PacketAppliers;
import com.boehmod.blockfront.common.net.packet.BFGamePacket;
import com.boehmod.blockfront.game.AbstractGame;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.client.data.config.AddonClientConfig;
import red.vuis.frontutil.ex.AbstractGameEx;

@Mixin(PacketAppliers.class)
public abstract class PacketAppliersMixin {
	@Inject(
		method = "game",
		at = @At("TAIL")
	)
	private static void gameCustom(BFGamePacket packet, IPayloadContext context, CallbackInfo ci, @Local(ordinal = 0) AbstractGame<?, ?, ?> game) {
		if (game == null) {
			AddonClientConfig.INSTANCE.forced = null;
			return;
		}
		
		AbstractGameEx gameEx = (AbstractGameEx) game;
		if (gameEx.frontutil$isForceClientConfig()) {
			AddonClientConfig.INSTANCE.forced = gameEx.frontutil$getClientConfig();
		}
	}
}
