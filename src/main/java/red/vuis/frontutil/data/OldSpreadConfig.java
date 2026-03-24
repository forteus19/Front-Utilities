package red.vuis.frontutil.data;

import lombok.Builder;

@Builder
public record OldSpreadConfig(
	float recoil,
	float idleSpread,
	float spreadWhenFired,
	float spreadWhenJump,
	float spreadWhenWalking,
	float spreadWhenCrawling,
	float spreadWhenSprinting,
	float spreadDecreaseWhileIdle,
	float spreadDecreaseWhileSneaking,
	float spreadMaxAngle,
	float crosshairSpread
) {
	public static float currentSpread = 0f;
	public static float prevSpread = 0f;
}
