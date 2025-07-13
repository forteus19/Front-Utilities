package red.vuis.frontutil.client.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.boehmod.blockfront.map.effect.AbstractMapEffect;
import com.boehmod.blockfront.map.effect.LoopingSoundPointMapEffect;
import com.boehmod.blockfront.map.effect.PositionedMapEffect;
import com.boehmod.blockfront.util.BFRes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import red.vuis.frontutil.client.data.AddonClientData;

public final class AssetEditRenderer extends RenderObject {
	private static final ResourceLocation LOOPING_SOUND_POINT = BFRes.loc("textures/misc/debug/sound_looping.png");
	private static final float ICON_SIZE = 0.75f;
	
	public AssetEditRenderer(Minecraft minecraft) {
		super(minecraft);
	}
	
	@Override
	public void render() {
		super.render();
		cameraAsOrigin();
		
		AddonClientData clientData = AddonClientData.getInstance();
		if (clientData.editing == null) {
			return;
		}
		
		List<Pair<Double, AABB>> highlights = new ArrayList<>();
		
		for (AbstractMapEffect absMapEffect : clientData.editing.getMapEffects()) {
			renderInfo(absMapEffect);
			if (absMapEffect instanceof PositionedMapEffect posMapEffect) {
				getHighlightDist(posMapEffect).ifPresent(highlights::add);
			}
		}
		
		if (!highlights.isEmpty()) {
			highlights.sort(Comparator.comparing(Pair::getLeft));
			boxOutline(highlights.getFirst().getRight(), 0xFFFFFFFF);
		}
	}
	
	private void renderInfo(AbstractMapEffect absMapEffect) {
		switch (absMapEffect) {
			case LoopingSoundPointMapEffect mapEffect -> {
				billboardTexture(LOOPING_SOUND_POINT, mapEffect.position, ICON_SIZE, ICON_SIZE);
				Optional.ofNullable(mapEffect.sound)
					.map(Supplier::get)
					.map(BuiltInRegistries.SOUND_EVENT::getKeyOrNull)
					.map(ResourceLocation::toString)
					.ifPresent(id -> billboardString(id, mapEffect.position.add(0, 0.6, 0), 0.5f));
			}
			default -> {
			}
		}
	}
	
	private Optional<Pair<Double, AABB>> getHighlightDist(PositionedMapEffect mapEffect) {
		Vec3 cameraPos = camera.getPosition();
		Vec3 lookingPos = cameraPos.add(new Vec3(camera.getLookVector()).scale(4.5f));
		AABB aabb = getHighlightBox(mapEffect.position);
		
		return aabb.clip(cameraPos, lookingPos).isPresent() ? Optional.of(Pair.of(cameraPos.distanceTo(mapEffect.position), aabb)) : Optional.empty();
	}
	
	private static AABB getHighlightBox(Vec3 position) {
		double s = ICON_SIZE / 2.0;
		return new AABB(position.subtract(s, s, s), position.add(s, s, s));
	}
}
