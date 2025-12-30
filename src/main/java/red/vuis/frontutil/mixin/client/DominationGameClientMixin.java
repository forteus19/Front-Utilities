package red.vuis.frontutil.mixin.client;

import java.util.Set;
import java.util.UUID;

import com.boehmod.blockfront.client.BFClientManager;
import com.boehmod.blockfront.client.player.BFClientPlayerData;
import com.boehmod.blockfront.client.player.ClientPlayerDataHandler;
import com.boehmod.blockfront.game.CapturePointGameClient;
import com.boehmod.blockfront.game.GameStageTimer;
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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import red.vuis.frontutil.client.data.config.AddonClientConfig;
import red.vuis.frontutil.client.render.game.DominationAddonRendering;

@Mixin(DominationGameClient.class)
public abstract class DominationGameClientMixin extends CapturePointGameClient<DominationGame, DominationPlayerManager> {
	public DominationGameClientMixin(@NotNull BFClientManager manager, @NotNull DominationGame game, @NotNull ClientPlayerDataHandler dataHandler) {
		super(manager, game, dataHandler);
	}
	
	@Override
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
		
		GameStageTimer timer = method_2678();
		switch (AddonClientConfig.getMatchHudStyle()) {
			case OLD -> DominationAddonRendering.oldMatchHud(client, manager, dataHandler, game, timer, context, textRenderer, matrices, midX, renderTime);
			case DAY_OF_INFAMY -> DominationAddonRendering.dayOfInfamyMatchHud(game, timer, context, textRenderer, matrices, height, midX);
		}
	}
}
