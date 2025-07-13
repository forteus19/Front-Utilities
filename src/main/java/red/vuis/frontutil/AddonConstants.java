package red.vuis.frontutil;

import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AddonConstants {
	public static final String MOD_ID = "frontutil";
	public static final Logger LOGGER = LogManager.getLogger("Front-Utilities");
	
	private AddonConstants() {
	}
	
	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
