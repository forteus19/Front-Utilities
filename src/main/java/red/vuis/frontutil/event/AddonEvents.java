package red.vuis.frontutil.event;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import red.vuis.frontutil.FrontUtil;
import red.vuis.frontutil.command.FrontUtilCommand;

@EventBusSubscriber(
	modid = FrontUtil.MOD_ID,
	bus = EventBusSubscriber.Bus.GAME
)
public final class AddonEvents {
	private static final Logger LOGGER = LogUtils.getLogger();
	
	private AddonEvents() {
	}
	
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		LOGGER.info("Registering commands...");
		
		var dispatcher = event.getDispatcher();
		FrontUtilCommand.register(dispatcher);
	}
}
