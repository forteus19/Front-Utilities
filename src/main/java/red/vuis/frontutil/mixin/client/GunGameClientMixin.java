package red.vuis.frontutil.mixin.client;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.game.AbstractGameClient;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.impl.gg.GunGame;
import com.boehmod.blockfront.game.impl.gg.GunGameClient;
import com.boehmod.blockfront.game.impl.gg.GunGamePlayerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import red.vuis.frontutil.client.data.config.AddonClientConfig;
import red.vuis.frontutil.client.data.config.MatchHudStyle;
import red.vuis.frontutil.client.render.AddonRendering;

@Mixin(GunGameClient.class)
public abstract class GunGameClientMixin extends AbstractGameClient<GunGame, GunGamePlayerManager> {
	public GunGameClientMixin(@NotNull BFClientManager manager, @NotNull GunGame game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);
	}
	
	@Inject(
		method = "method_2722",
		at = @At("TAIL")
	)
	private void renderOldHud(
		@NotNull MinecraftClient client,
		@NotNull BFClientManager manager,
		@NotNull ClientPlayerEntity player,
		@NotNull ClientWorld world,
		@NotNull BFClientPlayerData playerData,
		@NotNull DrawContext context,
		@NotNull TextRenderer textRenderer,
		@NotNull MatrixStack matrices,
		@NotNull VertexConsumerProvider vertexConsumers,
		@NotNull Set<UUID> players,
		int width,
		int height,
		int midX,
		int midY,
		float renderTime,
		float delta,
		CallbackInfo ci
	) {
		if (AddonClientConfig.getMatchHudStyle() == MatchHudStyle.MODERN) {
			return;
		}
		
		GunGamePlayerManager playerManager = game.getPlayerManager();
		
		AddonRendering.oldTimer(matrices, textRenderer, context, midX, method_2678());
		
		GameTeam axisTeam = playerManager.getTeamByName("Axis");
		GameTeam alliesTeam = playerManager.getTeamByName("Allies");
		if (axisTeam == null || alliesTeam == null) {
			return;
		}
		
		List<UUID> axisPlayers = axisTeam.getPlayers().stream().sorted().toList();
		List<UUID> alliesPlayers = alliesTeam.getPlayers().stream().sorted().toList();
		AddonRendering.oldPlayerHeadList(client, manager, context, dataHandler, midX, axisPlayers, alliesPlayers);
	}
}
