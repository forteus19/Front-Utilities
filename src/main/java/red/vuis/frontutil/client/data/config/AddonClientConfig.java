package red.vuis.frontutil.client.data.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class AddonClientConfig {
	public static final AddonClientConfig INSTANCE;
	public static final ModConfigSpec SPEC;
	
	public final ModConfigSpec.EnumValue<MatchHudStyle> matchHudStyle;
	
	private AddonClientConfig(ModConfigSpec.Builder builder) {
		matchHudStyle = builder.defineEnum("match_hud_style", MatchHudStyle.MODERN);
	}
	
	public static MatchHudStyle getMatchHudStyle() {
		return INSTANCE.matchHudStyle.get();
	}
	
	static {
		Pair<AddonClientConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(AddonClientConfig::new);
		INSTANCE = pair.getLeft();
		SPEC = pair.getRight();
	}
}
