package red.vuis.frontutil.data;

import net.minecraft.entity.player.PlayerEntity;

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
	
	public float calculateDelta(PlayerEntity player) {
		float spreadDelta = -(player.isInSneakingPose() ? spreadDecreaseWhileSneaking : spreadDecreaseWhileIdle);
		float horizontalDelta = player.horizontalSpeed - player.prevHorizontalSpeed;
		boolean isMoving = horizontalDelta > 0.04f;
		boolean isJump = Math.abs(player.getVelocity().y) >= 0.31f;
		
		if (player.getVehicle() == null) {
			if (isJump) {
				spreadDelta = spreadWhenJump;
			} else if (isMoving) {
				spreadDelta = player.isSprinting() ? spreadWhenSprinting : spreadWhenWalking;
			}
		}
		
		return spreadDelta;
	}
}
