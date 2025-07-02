package red.vuis.frontutil.client.data.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class AddonClientConfig {
	public static final AddonClientConfig INSTANCE;
	public static final ModConfigSpec SPEC;
	
	public final ModConfigSpec.EnumValue<MatchHudStyle> matchHudStyle;
	public final ModConfigSpec.BooleanValue renderCorpses;
	
	private AddonClientConfig(ModConfigSpec.Builder builder) {
		matchHudStyle = builder.defineEnum("match_hud_style", MatchHudStyle.MODERN);
		renderCorpses = builder.define("render_corpses", true);
	}
	
	public static MatchHudStyle getMatchHudStyle() {
		return INSTANCE.matchHudStyle.get();
	}
	
	public static boolean getRenderCorpses() {
		return INSTANCE.renderCorpses.getAsBoolean();
	}
	
	static {
		Pair<AddonClientConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(AddonClientConfig::new);
		INSTANCE = pair.getLeft();
		SPEC = pair.getRight();
	}
}
