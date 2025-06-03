package red.vuis.frontutil.setup;

import com.boehmod.bflib.cloud.common.CloudRegistry;
import com.boehmod.bflib.cloud.common.item.CloudItem;
import com.boehmod.bflib.cloud.common.item.types.CloudItemGun;
import com.boehmod.blockfront.util.BFRes;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

public final class GunSkinIndex {
	public static final Object2ObjectMap<ResourceLocation, Object2FloatMap<String>> SKINS = new Object2ObjectOpenHashMap<>();
	
	private GunSkinIndex() {
	}
	
	public static void init(CloudRegistry registry) {
		for (CloudItem<?> item : registry.getItems()) {
			if (!(item instanceof CloudItemGun) || item.isDefault() || item.isDeprecated()) {
				continue;
			}
			
			SKINS.computeIfAbsent(BFRes.fromCloud(item.getMinecraftItem()), key -> new Object2FloatArrayMap<>())
				.put(item.getSuffix().toLowerCase().replace(' ', '_'), item.getSkin());
		}
	}
}
