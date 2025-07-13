package red.vuis.frontutil.client.event;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import red.vuis.frontutil.AddonConstants;
import red.vuis.frontutil.client.command.FrontUtilClientCommand;
import red.vuis.frontutil.client.render.AssetEditRenderer;

@EventBusSubscriber(
	modid = AddonConstants.MOD_ID,
	value = Dist.CLIENT
)
public final class AddonClientEvents {
	private static final Supplier<AssetEditRenderer> ASSET_EDIT_RENDERER = Suppliers.memoize(() -> new AssetEditRenderer(Minecraft.getInstance()));
	
	private AddonClientEvents() {
	}
	
	@SubscribeEvent
	public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		AddonConstants.LOGGER.info("Registering client-only commands...");
		
		var dispatcher = event.getDispatcher();
		FrontUtilClientCommand.register(dispatcher);
	}
	
	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
			ASSET_EDIT_RENDERER.get().render();
		}
	}
}
