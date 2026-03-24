package red.vuis.frontutil.data;

import java.util.Map;
import java.util.function.Supplier;

import com.boehmod.blockfront.common.item.BFWeaponItem;
import com.boehmod.blockfront.registry.BFItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public final class OldSpreadConfigs {
	private static final OldSpreadConfig PISTOL = new OldSpreadConfig(3.0F, 0.0F, 0.3F, 0.8F, 0.08F, 0.025F, 0.1F, 0.1F, 0.15F, 10.0F, 20.0F);
	private static final OldSpreadConfig PISTOL_AUTO = new OldSpreadConfig(3.0F, 0.0F, 0.15F, 0.8F, 0.08F, 0.025F, 0.1F, 0.1F, 0.15F, 10.0F, 20.0F);
	private static final OldSpreadConfig REVOLVER = new OldSpreadConfig(8.0F, 0.0F, 0.8F, 0.8F, 0.08F, 0.025F, 0.1F, 0.06F, 0.1F, 10.0F, 20.0F);
	private static final OldSpreadConfig RIFLE = new OldSpreadConfig(15.0F, 0.0F, 1.0F, 1.0F, 0.08F, 0.05F, 0.16F, 0.04F, 0.07F, 10.0F, 25.0F);
	private static final OldSpreadConfig LT_RIFLE = new OldSpreadConfig(5.0F, 0.0F, 0.5F, 0.8F, 0.08F, 0.025F, 0.1F, 0.05F, 0.2F, 10.0F, 20.0F);
	private static final OldSpreadConfig SMG = new OldSpreadConfig(4.0F, 0.0F, 0.15F, 1.0F, 0.06F, 0.1F, 0.12F, 0.05F, 0.1F, 10.0F, 25.0F);
	private static final OldSpreadConfig SMG_FAST = new OldSpreadConfig(2.0F, 0.0F, 0.15F, 1.0F, 0.06F, 0.1F, 0.12F, 0.05F, 0.1F, 10.0F, 25.0F);
	private static final OldSpreadConfig SMG_SLOW = new OldSpreadConfig(4.0F, 0.0F, 0.21F, 1.0F, 0.06F, 0.1F, 0.12F, 0.05F, 0.1F, 10.0F, 25.0F);
	private static final OldSpreadConfig LMG = new OldSpreadConfig(3.0F, 0.0F, 0.2F, 1.0F, 0.06F, 0.05F, 0.12F, 0.08F, 0.19F, 10.0F, 20.0F);
	private static final OldSpreadConfig LMG_SLOW = new OldSpreadConfig(3.0F, 0.0F, 0.28F, 1.0F, 0.06F, 0.05F, 0.12F, 0.08F, 0.19F, 10.0F, 20.0F);
	private static final OldSpreadConfig MG = new OldSpreadConfig(4.0F, 0.0F, 0.15F, 1.0F, 0.09F, 0.2F, 0.18F, 0.06F, 0.07F, 10.0F, 40.0F);
	private static final OldSpreadConfig ROCKET = new OldSpreadConfig(3.0F, 0.0F, 0.01F, 0.01F, 0.01F, 0.01F, 0.01F, 1.0F, 1.0F, 1.0F, 1.0F);
	private static final OldSpreadConfig ATR = new OldSpreadConfig(30.0F, 0.0F, 1.0F, 1.0F, 0.08F, 0.05F, 0.16F, 0.02F, 0.04F, 10.0F, 25.0F);
	private static final OldSpreadConfig SHOTGUN = new OldSpreadConfig(13.0F, 0.3F, 0.4F, 1.0F, 0.08F, 0.05F, 0.16F, 0.02F, 0.025F, 7.5F, 20.0F);
	private static final OldSpreadConfig FLAMETHROWER = new OldSpreadConfig(0.1F, 0.0F, 0.01F, 0.01F, 0.01F, 0.01F, 0.01F, 1.0F, 1.0F, 1.0F, 1.0F);
	
	private static final Map<BFWeaponItem<?>, OldSpreadConfig> ITEM_MAP = new Object2ObjectOpenHashMap<>();
	
	private OldSpreadConfigs() {
	}
	
	public static void init() {
		register(BFItems.GUN_TOKAREV_AVT40, SMG);
		register(BFItems.GUN_TOKAREV_SVT40, SMG);
		register(BFItems.GUN_TYPE92, MG);
		register(BFItems.GUN_WELROD, PISTOL);
		register(BFItems.GUN_BROWNBESS, RIFLE);
		register(BFItems.GUN_BROWNING30, MG);
		register(BFItems.GUN_MG42, MG);
		register(BFItems.GUN_VICKERS_K, MG);
		register(BFItems.GUN_FIAT_REVELLI, MG);
		register(BFItems.GUN_MAC_MLE_1931, MG);
		register(BFItems.GUN_LEWISGUN, MG);
		register(BFItems.GUN_BAR, LMG_SLOW);
		register(BFItems.GUN_DP28, LMG);
		register(BFItems.GUN_MG34, LMG);
		register(BFItems.GUN_ZB26, LMG_SLOW);
		register(BFItems.GUN_TYPE11, LMG);
		register(BFItems.GUN_BREN_MK2, LMG);
		register(BFItems.GUN_MODEL_1930, LMG);
		register(BFItems.GUN_BREDA_SAFAT, LMG);
		register(BFItems.GUN_TYPE96, LMG);
		register(BFItems.GUN_TYPE98, LMG);
		register(BFItems.GUN_M1928A1_THOMPSON, SMG_FAST);
		register(BFItems.GUN_M1A1_THOMPSON, SMG);
		register(BFItems.GUN_GREASEGUN, SMG);
		register(BFItems.GUN_MP40, SMG);
		register(BFItems.GUN_STG44, SMG_SLOW);
		register(BFItems.GUN_BLYSKAWICA, SMG);
		register(BFItems.GUN_KOP_PAL, SMG);
		register(BFItems.GUN_PPS43, SMG);
		register(BFItems.GUN_PPSH, SMG_FAST);
		register(BFItems.GUN_TYPE100, SMG);
		register(BFItems.GUN_STEN_MK2, SMG);
		register(BFItems.GUN_MODEL_38, SMG);
		register(BFItems.GUN_MAS_38, SMG);
		register(BFItems.GUN_AK74, SMG);
		register(BFItems.GUN_M4A4, SMG);
		register(BFItems.GUN_FG42, SMG);
		register(BFItems.GUN_TRENCHGUN, SHOTGUN);
		register(BFItems.GUN_M30, SHOTGUN);
		register(BFItems.GUN_BECKER, SHOTGUN);
		register(BFItems.GUN_MAUSER_M712, PISTOL_AUTO);
		register(BFItems.GUN_SPRINGFIELD, RIFLE);
		register(BFItems.GUN_KAR98K, RIFLE);
		register(BFItems.GUN_KBK_WZ_29, RIFLE);
		register(BFItems.GUN_MOSIN_NAGANT, RIFLE);
		register(BFItems.GUN_TYPE38, RIFLE);
		register(BFItems.GUN_TYPE99, RIFLE);
		register(BFItems.GUN_LEE_ENFIELD_MK1, RIFLE);
		register(BFItems.GUN_CARCANO_M91TS_CARBINE, RIFLE);
		register(BFItems.GUN_CARCANO_M38, RIFLE);
		register(BFItems.GUN_LEBEL_1886, RIFLE);
		register(BFItems.GUN_M1_GARAND, LT_RIFLE);
		register(BFItems.GUN_M1_CARBINE, LT_RIFLE);
		register(BFItems.GUN_M2_CARBINE, LT_RIFLE);
		register(BFItems.GUN_GEWEHR_43, LT_RIFLE);
		register(BFItems.GUN_TYPE4, LT_RIFLE);
		register(BFItems.GUN_LEE_ENFIELD_TURNER, LT_RIFLE);
		register(BFItems.GUN_MODEL_1935, MG); // for some reason
		register(BFItems.GUN_FUSIL_1917, LT_RIFLE);
		register(BFItems.GUN_HOWELL, LT_RIFLE);
		register(BFItems.GUN_PTRS, ATR);
		register(BFItems.GUN_BATR, ATR);
		register(BFItems.GUN_PANZERBUCHSE39, ATR);
		register(BFItems.GUN_TYPE26, REVOLVER);
		register(BFItems.GUN_WEBLEY_MK6, REVOLVER);
		register(BFItems.GUN_MODELE_1892_REVOLVER, REVOLVER);
		register(BFItems.GUN_M2_FLAMETHROWER, FLAMETHROWER);
		register(BFItems.GUN_FLAMMENWERFER_34, FLAMETHROWER);
		register(BFItems.GUN_COLT, PISTOL);
		register(BFItems.GUN_BERETTA_M1934, PISTOL);
		register(BFItems.GUN_FN_MODEL_1910, PISTOL);
		register(BFItems.GUN_WALTHER_P38, PISTOL);
		register(BFItems.GUN_LUGER, PISTOL);
		register(BFItems.GUN_MAUSER_C96, PISTOL);
		register(BFItems.GUN_TOKAREV_TT33, PISTOL);
		register(BFItems.GUN_FB_VIS, PISTOL);
		register(BFItems.GUN_TYPE14, PISTOL);
		register(BFItems.GUN_TYPE94, PISTOL);
		register(BFItems.GUN_GLISENTI_MODEL_1910, PISTOL);
		register(BFItems.GUN_PISTOLET_AUTOMATIQUE_MODELE_1935A, PISTOL);
		register(BFItems.GUN_BROWNING_HIPOWER, PISTOL);
		register(BFItems.GUN_BAZOOKA, ROCKET);
		register(BFItems.GUN_PANZERSCHRECK, ROCKET);
		register(BFItems.GUN_PANZERFAUST, ROCKET);
		register(BFItems.GUN_PIAT, ROCKET);
		register(BFItems.GUN_MELON_CANNON, ROCKET);
		register(BFItems.GUN_KIS, SMG);
		register(BFItems.GUN_MP_3008, SMG);
		register(BFItems.GUN_WINCHESTER_1895, RIFLE);
		register(BFItems.GUN_MP41, SMG);
		register(BFItems.GUN_TYPE18_SHOTGUN, SHOTGUN);
		register(BFItems.GUN_DE_LISLE_CARBINE, RIFLE);
		register(BFItems.GUN_BROWNING_A5, SHOTGUN);
		register(BFItems.GUN_TYPE4_70MM, ROCKET);
		register(BFItems.GUN_WZ_35, ATR);
	}
	
	private static void register(Supplier<? extends BFWeaponItem<?>> itemHolder, OldSpreadConfig config) {
		ITEM_MAP.put(itemHolder.get(), config);
	}
	
	public static OldSpreadConfig get(BFWeaponItem<?> item) {
		return ITEM_MAP.getOrDefault(item, PISTOL);
	}
}
