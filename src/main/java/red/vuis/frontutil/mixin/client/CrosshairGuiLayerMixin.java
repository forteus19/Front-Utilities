package red.vuis.frontutil.mixin.client;

import com.boehmod.blockfront.client.gui.layer.CrosshairGuiLayer;
import com.boehmod.blockfront.common.item.BFWeaponItem;
import com.boehmod.blockfront.util.math.MathUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import red.vuis.frontutil.data.AddonCommonData;
import red.vuis.frontutil.data.OldSpreadConfig;
import red.vuis.frontutil.data.OldSpreadConfigs;

@Mixin(CrosshairGuiLayer.class)
public abstract class CrosshairGuiLayerMixin {
	@Redirect(
		method = "method_512",
		at = @At(
			value = "INVOKE",
			target = "Lcom/boehmod/blockfront/util/math/MathUtils;lerpf1(FFF)F",
			ordinal = 0
		)
	)
	private float overrideSpreadFields(float max, float min, float t) {
		if (AddonCommonData.getInstance().useOldSpread) {
			return MathUtils.lerpf1(OldSpreadConfig.currentSpread, OldSpreadConfig.prevSpread, t);
		} else {
			return MathUtils.lerpf1(max, min, t);
		}
	}
	
	@ModifyConstant(
		method = "method_512",
		constant = @Constant(
			floatValue = 3.0F
		)
	)
	private float overrideSpreadScale(float constant, @Local(argsOnly = true) ItemStack heldStack) {
		if (AddonCommonData.getInstance().useOldSpread && heldStack.getItem() instanceof BFWeaponItem<?> weaponItem) {
			return OldSpreadConfigs.get(weaponItem).config().crosshairSpread();
		} else {
			return constant;
		}
	}
}
