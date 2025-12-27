package red.vuis.frontutil.mixin.client;

import java.util.Set;
import java.util.UUID;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.client.render.BFRendering;
import com.boehmod.blockfront.common.stat.BFStats;
import com.boehmod.blockfront.game.AbstractCapturePoint;
import com.boehmod.blockfront.game.CapturePointGameClient;
import com.boehmod.blockfront.game.GameTeam;
import com.boehmod.blockfront.game.impl.dom.DominationGame;
import com.boehmod.blockfront.game.impl.dom.DominationGameClient;
import com.boehmod.blockfront.game.impl.dom.DominationPlayerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import red.vuis.frontutil.client.data.config.AddonClientConfig;
import red.vuis.frontutil.client.data.config.MatchHudStyle;
import red.vuis.frontutil.client.render.AddonRendering;

@Mixin(DominationGameClient.class)
public abstract class DominationGameClientMixin extends CapturePointGameClient<DominationGame, DominationPlayerManager> {
	public DominationGameClientMixin(@NotNull BFClientManager manager, @NotNull DominationGame game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void method_2722(
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
		float delta
	) {
		super.method_2722(client, manager, player, world, playerData, context, textRenderer, matrices, vertexConsumers, players, width, height, midX, midY, renderTime, delta);
		
		if (AddonClientConfig.getMatchHudStyle() == MatchHudStyle.MODERN) {
			return;
		}
		
		DominationPlayerManager playerManager = game.getPlayerManager();
		
		AddonRendering.oldTimer(matrices, textRenderer, context, midX, method_2678());
		
		GameTeam axisTeam = playerManager.getTeamByName("Axis");
		GameTeam alliesTeam = playerManager.getTeamByName("Allies");
		if (axisTeam == null || alliesTeam == null) {
			return;
		}
		
		int axisColor = 0x7E3831;
		int alliesColor = 0x747948;
		
		int axisScore = axisTeam.getObjectInt(BFStats.SCORE);
		int alliesScore = alliesTeam.getObjectInt(BFStats.SCORE);
		Text axisScoreComponent = Text.literal(Integer.toString(axisScore)).withColor(axisColor);
		Text alliesScoreComponent = Text.literal(Integer.toString(alliesScore)).withColor(alliesColor);
		
		float axisRectStartX = midX - 99f;
		float axisScoreStartX = midX - 19f;
		
		float alliesRectStartX = midX + 21f;
		float alliesScoreStartX = midX + 0.5f;
		
		int axisScoreRectWidth = (int) (78f * (axisScore / 500f));
		int alliesScoreRectWidth = (int) (78f * (alliesScore / 500f));
		
		// Axis score
		BFRendering.rectangle(matrices, context, axisScoreStartX, 15f, 18.5f, 10f, BFRendering.translucentBlack());
		BFRendering.centeredComponent2d(matrices, textRenderer, context, axisScoreComponent, axisScoreStartX + 9.75f, 16.5f);
		
		// Axis rect
		BFRendering.rectangle(matrices, context, axisRectStartX - 1f, 15f, 80f, 10f, BFRendering.translucentBlack());
		BFRendering.rectangle(matrices, context, axisRectStartX, 16f, 78f, 8f, 0, 0.35f);
		BFRendering.rectangle(matrices, context, axisRectStartX, 16f, 78f, 8f, axisColor, 0.35f);
		BFRendering.rectangle(matrices, context, axisRectStartX, 16f, axisScoreRectWidth, 8f, axisColor, 1f);
		
		// Allies score
		BFRendering.rectangle(matrices, context, alliesScoreStartX, 15f, 18.5f, 10f, BFRendering.translucentBlack());
		BFRendering.centeredComponent2d(matrices, textRenderer, context, alliesScoreComponent, alliesScoreStartX + 9.75f, 16.5f);
		
		// Allies rect
		BFRendering.rectangle(matrices, context, alliesRectStartX - 1f, 15f, 80f, 10f, BFRendering.translucentBlack());
		BFRendering.rectangle(matrices, context, alliesRectStartX, 16f, 78f, 8f, 0, 0.35f);
		BFRendering.rectangle(matrices, context, alliesRectStartX, 16f, 78f, 8f, alliesColor, 0.35f);
		BFRendering.rectangle(matrices, context, alliesRectStartX + (78f - alliesScoreRectWidth), 16f, alliesScoreRectWidth, 8f, alliesColor, 1f);
		
		int axisArrows = 0;
		int alliesArrows = 0;
		
		for (AbstractCapturePoint<?> capturePoint : game.getCapturePoints()) {
			if (capturePoint.cbTeam == axisTeam) {
				axisArrows++;
			} else if (capturePoint.cbTeam == alliesTeam) {
				alliesArrows++;
			}
		}
		
		float arrowAlpha = Math.min(0.5f + MathHelper.sin(renderTime / 15f) / 2f, 0.5f);
		
		for (int i = 0; i < axisArrows; i++) {
			BFRendering.texture(matrices, context, AddonRendering.CPOINT_ARROW_RIGHT_BLACK, midX - 98f + 7f * i, 17f, 6f, 6f, arrowAlpha);
		}
		for (int i = 0; i < alliesArrows; i++) {
			BFRendering.texture(matrices, context, AddonRendering.CPOINT_ARROW_LEFT_BLACK, midX + 92f - 7f * i, 17f, 6f, 6f, arrowAlpha);
		}
	}
}
