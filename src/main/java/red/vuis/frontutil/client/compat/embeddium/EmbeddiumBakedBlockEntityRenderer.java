package red.vuis.frontutil.client.compat.embeddium;

import com.boehmod.blockfront.client.render.block.BFBlockRenderer;
import com.boehmod.blockfront.common.block.entity.BakedEntityBlock;
import com.boehmod.blockfront.unnamed.BF_232;
import com.boehmod.blockfront.unnamed.BF_364;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.embeddedt.embeddium.api.BlockRendererRegistry;
import org.embeddedt.embeddium.api.render.chunk.BlockRenderContext;
import org.joml.Vector2d;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;

import red.vuis.frontutil.AddonConstants;

// all credit goes to TomerItzko
// https://github.com/TomerItzko/FrontCore/blob/main/src/main/java/dev/tomerdev/mercfrontcore/client/compat/EmbeddiumBakedEntityBlockCompiler.java
public final class EmbeddiumBakedBlockEntityRenderer implements BlockRendererRegistry.Renderer {
	@SuppressWarnings({"rawtypes", "unchecked", "UnstableApiUsage"})
	public BlockRendererRegistry.RenderResult renderBlock(BlockRenderContext context, Random random, VertexConsumer vertices) {
		BlockState state = context.state();
		BlockPos pos = context.pos();
		BlockRenderView world = context.localSlice();
		
		if (!(state.getBlock() instanceof BakedEntityBlock)) {
			return BlockRendererRegistry.RenderResult.PASS;
		}
		
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity == null || blockEntity.isRemoved()) {
			return BlockRendererRegistry.RenderResult.PASS;
		}
		
		MinecraftClient client = MinecraftClient.getInstance();
		BlockEntityRenderer<BlockEntity> renderer = client.getBlockEntityRenderDispatcher().get(blockEntity);
		if (!(renderer instanceof BFBlockRenderer rawRenderer)) {
			return BlockRendererRegistry.RenderResult.PASS;
		}
		
		if (!rawRenderer.method_1284(blockEntity)) {
			return rawRenderer.method_5953() ? BlockRendererRegistry.RenderResult.PASS : BlockRendererRegistry.RenderResult.OVERRIDE;
		}
		
		if (client.world == null) {
			return BlockRendererRegistry.RenderResult.PASS;
		}
		
		BF_232 model = (BF_232) rawRenderer.getGeoModel();
		Identifier texture = model.method_1031(blockEntity);
		Sprite sprite = client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(texture);
		
		MatrixStack matrices = new MatrixStack();
		
		Vector2d minUv = new Vector2d(sprite.getMinU(), sprite.getMinV());
		Vector2d maxUv = new Vector2d(sprite.getMaxU(), sprite.getMaxV());
		BF_364 buffer = new BF_364(client.world, vertices, minUv, maxUv);
		int packedLight = WorldRenderer.getLightmapCoordinates(world, pos.up());
		
		try {
			GeoAnimatable animatable = (GeoAnimatable) blockEntity;
			long instanceId = rawRenderer.getInstanceId(blockEntity);
			
			AnimationState animationState = new AnimationState(animatable, 0, 0, 1, false);
			animationState.setData(DataTickets.TICK, animatable.getTick(blockEntity));
			animationState.setData(DataTickets.BLOCK_ENTITY, blockEntity);
			
			model.addAdditionalStateData(animatable, instanceId, (ticket, value) -> animationState.setData((DataTicket) ticket, value));
			model.handleAnimations(animatable, instanceId, animationState, 1);
			
			rawRenderer.defaultRender(matrices, animatable, client.getBufferBuilders().getEntityVertexConsumers(), null, buffer, 0, 1, packedLight);
		} catch (Exception e) {
			AddonConstants.LOGGER.error("Failed to compile Embeddium baked entity block", e);
			return BlockRendererRegistry.RenderResult.PASS;
		}
		
		return client.player != null && client.player.isCreative() ? BlockRendererRegistry.RenderResult.PASS : BlockRendererRegistry.RenderResult.OVERRIDE;
	}
}
