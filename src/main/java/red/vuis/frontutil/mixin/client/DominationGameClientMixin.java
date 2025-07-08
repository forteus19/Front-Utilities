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
import com.boehmod.blockfront.game.dom.DominationGame;
import com.boehmod.blockfront.game.dom.DominationGameClient;
import com.boehmod.blockfront.game.dom.DominationPlayerManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
		@NotNull Minecraft minecraft,
		@NotNull BFClientManager manager,
		@NotNull LocalPlayer player,
		@NotNull ClientLevel level,
		@NotNull BFClientPlayerData playerData,
		@NotNull GuiGraphics graphics,
		@NotNull Font font,
		@NotNull PoseStack poseStack,
		@NotNull MultiBufferSource buffer,
		@NotNull Set<UUID> players,
		int width,
		int height,
		int midX,
		int midY,
		float renderTime,
		float delta
	) {
		super.method_2722(minecraft, manager, player, level, playerData, graphics, font, poseStack, buffer, players, width, height, midX, midY, renderTime, delta);
		
		if (AddonClientConfig.getMatchHudStyle() == MatchHudStyle.MODERN) {
			return;
		}
		
		DominationPlayerManager playerManager = game.getPlayerManager();
		
		AddonRendering.oldTimer(poseStack, font, graphics, midX, method_2678());
		
		GameTeam axisTeam = playerManager.getTeamByName("Axis");
		GameTeam alliesTeam = playerManager.getTeamByName("Allies");
		if (axisTeam == null || alliesTeam == null) {
			return;
		}
		
		int axisColor = 0x7E3831;
		int alliesColor = 0x747948;
		
		int axisScore = axisTeam.getObjectInt(BFStats.SCORE);
		int alliesScore = alliesTeam.getObjectInt(BFStats.SCORE);
		Component axisScoreComponent = Component.literal(Integer.toString(axisScore)).withColor(axisColor);
		Component alliesScoreComponent = Component.literal(Integer.toString(alliesScore)).withColor(alliesColor);
		
		float axisRectStartX = midX - 99f;
		float axisScoreStartX = midX - 19f;
		
		float alliesRectStartX = midX + 21f;
		float alliesScoreStartX = midX + 0.5f;
		
		int axisScoreRectWidth = (int) (78f * (axisScore / 500f));
		int alliesScoreRectWidth = (int) (78f * (alliesScore / 500f));
		
		// Axis score
		BFRendering.rectangle(poseStack, graphics, axisScoreStartX, 15f, 18.5f, 10f, BFRendering.translucentBlack());
		BFRendering.centeredComponent2d(poseStack, font, graphics, axisScoreComponent, axisScoreStartX + 9.75f, 16.5f);
		
		// Axis rect
		BFRendering.rectangle(poseStack, graphics, axisRectStartX - 1f, 15f, 80f, 10f, BFRendering.translucentBlack());
		BFRendering.rectangle(poseStack, graphics, axisRectStartX, 16f, 78f, 8f, 0, 0.35f);
		BFRendering.rectangle(poseStack, graphics, axisRectStartX, 16f, 78f, 8f, axisColor, 0.35f);
		BFRendering.rectangle(poseStack, graphics, axisRectStartX, 16f, axisScoreRectWidth, 8f, axisColor, 1f);
		
		// Allies score
		BFRendering.rectangle(poseStack, graphics, alliesScoreStartX, 15f, 18.5f, 10f, BFRendering.translucentBlack());
		BFRendering.centeredComponent2d(poseStack, font, graphics, alliesScoreComponent, alliesScoreStartX + 9.75f, 16.5f);
		
		// Allies rect
		BFRendering.rectangle(poseStack, graphics, alliesRectStartX - 1f, 15f, 80f, 10f, BFRendering.translucentBlack());
		BFRendering.rectangle(poseStack, graphics, alliesRectStartX, 16f, 78f, 8f, 0, 0.35f);
		BFRendering.rectangle(poseStack, graphics, alliesRectStartX, 16f, 78f, 8f, alliesColor, 0.35f);
		BFRendering.rectangle(poseStack, graphics, alliesRectStartX + (78f - alliesScoreRectWidth), 16f, alliesScoreRectWidth, 8f, alliesColor, 1f);
		
		int axisArrows = 0;
		int alliesArrows = 0;
		
		for (AbstractCapturePoint<?> capturePoint : game.getCapturePoints()) {
			if (capturePoint.cbTeam == axisTeam) {
				axisArrows++;
			} else if (capturePoint.cbTeam == alliesTeam) {
				alliesArrows++;
			}
		}
		
		float arrowAlpha = Math.min(0.5f + Mth.sin(renderTime / 15f) / 2f, 0.5f);
		
		for (int i = 0; i < axisArrows; i++) {
			BFRendering.texture(poseStack, graphics, AddonRendering.CPOINT_ARROW_RIGHT_BLACK, midX - 98f + 7f * i, 17f, 6f, 6f, arrowAlpha);
		}
		for (int i = 0; i < alliesArrows; i++) {
			BFRendering.texture(poseStack, graphics, AddonRendering.CPOINT_ARROW_LEFT_BLACK, midX + 92f - 7 * i, 17f, 6f, 6f, arrowAlpha);
		}
	}
}
