package red.vuis.frontutil.client.event;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.client.MinecraftClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.client.command.FrontUtilClientCommand;
import red.vuis.frontutil.client.input.InputAcceptor;
import red.vuis.frontutil.client.input.InputTracker;
import red.vuis.frontutil.client.input.MouseButton;
import red.vuis.frontutil.client.render.AssetEditRenderer;
import red.vuis.frontutil.client.render.RenderObject;
import red.vuis.frontutil.util.AddonUtils;

@EventBusSubscriber(
	modid = AddonConstants.MOD_ID,
	value = Dist.CLIENT
)
public final class AddonClientEvents {
	private static final Map<RenderLevelStageEvent.Stage, List<Supplier<? extends RenderObject>>> RENDER_OBJECTS = Map.of(
		RenderLevelStageEvent.Stage.AFTER_WEATHER, List.of(
			RenderObject.of(AssetEditRenderer::new)
		)
	);
	
	private AddonClientEvents() {
	}
	
	@SubscribeEvent
	public static void onInputMouseButtonPost(InputEvent.MouseButton.Post event) {
		AddonUtils.forEachRecursive(RENDER_OBJECTS.values(), supplier -> {
			RenderObject obj = supplier.get();
			if (obj instanceof InputAcceptor acceptor) {
				MouseButton.apply(event.getButton(), event.getAction() > 0 ? acceptor::mousePressed : acceptor::mouseReleased);
			}
		});
	}
	
	@SubscribeEvent
	public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		AddonConstants.LOGGER.info("Registering client-only commands...");
		
		var dispatcher = event.getDispatcher();
		FrontUtilClientCommand.register(dispatcher);
	}
	
	@SubscribeEvent
	public static void onRenderFramePre(RenderFrameEvent.Pre event) {
		InputTracker.getInstance().update(MinecraftClient.getInstance().mouse);
	}
	
	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		List<Supplier<? extends RenderObject>> objs = RENDER_OBJECTS.get(event.getStage());
		if (objs != null) {
			objs.forEach(obf -> obf.get().render());
		}
	}
}
