package red.vuis.frontutil.setup;

import com.boehmod.blockfront.registry.BFItems;
import com.boehmod.blockfront.util.BFRes;

public final class AddonRegistryMigration {
	private AddonRegistryMigration() {
	}
	
	public static void init() {
		BFItems.DR.addAlias(BFRes.loc("ammo_crate"), BFRes.loc("ammo_box"));
	}
}
