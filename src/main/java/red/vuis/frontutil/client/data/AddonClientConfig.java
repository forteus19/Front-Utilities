package red.vuis.frontutil.client.data;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class AddonClientConfig {
	public static final AddonClientConfig INSTANCE;
	public static final ModConfigSpec SPEC;
	
	public final ModConfigSpec.BooleanValue nostalgiaMode;
	
	private AddonClientConfig(ModConfigSpec.Builder builder) {
		nostalgiaMode = builder.define("nostalgia_mode", false);
	}
	
	public static boolean isNostalgiaMode() {
		return INSTANCE.nostalgiaMode.getAsBoolean();
	}
	
	static {
		Pair<AddonClientConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(AddonClientConfig::new);
		INSTANCE = pair.getLeft();
		SPEC = pair.getRight();
	}
}
