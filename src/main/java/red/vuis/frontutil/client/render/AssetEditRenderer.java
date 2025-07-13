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
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

import red.vuis.frontutil.client.data.AddonClientData;

public final class AssetEditRenderer extends RenderObject {
	private static final Identifier LOOPING_SOUND_POINT = BFRes.loc("textures/misc/debug/sound_looping.png");
	private static final float ICON_SIZE = 0.75f;
	
	public AssetEditRenderer(MinecraftClient minecraft) {
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
		
		List<Pair<Double, Box>> highlights = new ArrayList<>();
		
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
					.map(Registries.SOUND_EVENT::getKeyOrNull)
					.map(Identifier::toString)
					.ifPresent(id -> billboardString(id, mapEffect.position.add(0, 0.6, 0), 0.5f));
			}
			default -> {
			}
		}
	}
	
	private Optional<Pair<Double, Box>> getHighlightDist(PositionedMapEffect mapEffect) {
		Vec3d cameraPos = camera.getPos();
		Vec3d lookingPos = cameraPos.add(new Vec3d(camera.getHorizontalPlane()).multiply(4.5f));
		Box aabb = getHighlightBox(mapEffect.position);
		
		return aabb.raycast(cameraPos, lookingPos).isPresent() ? Optional.of(Pair.of(cameraPos.distanceTo(mapEffect.position), aabb)) : Optional.empty();
	}
	
	private static Box getHighlightBox(Vec3d position) {
		double s = ICON_SIZE / 2.0;
		return new Box(position.subtract(s, s, s), position.add(s, s, s));
	}
}
