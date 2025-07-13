package red.vuis.frontutil.util;

import net.minecraft.util.FastColor;

public final class ColorUtils {
	private ColorUtils() {
	}
	
	public static float redFloat(int color) {
		return FastColor.ARGB32.red(color) / 255f;
	}
	
	public static float greenFloat(int color) {
		return FastColor.ARGB32.green(color) / 255f;
	}
	
	public static float blueFloat(int color) {
		return FastColor.ARGB32.blue(color) / 255f;
	}
	
	public static float alphaFloat(int color) {
		return FastColor.ARGB32.alpha(color) / 255f;
	}
}
