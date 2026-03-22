package red.vuis.frontutil.client.compat.embeddium;

import java.util.concurrent.atomic.AtomicBoolean;

import com.boehmod.blockfront.common.block.entity.BakedEntityBlock;
import org.embeddedt.embeddium.api.BlockRendererRegistry;

public final class EmbeddiumCompatibility {
	private static final AtomicBoolean REGISTERED = new AtomicBoolean();
	
	private EmbeddiumCompatibility() {
	}
	
	public static void init() {
		if (!REGISTERED.compareAndSet(false, true)) {
			return;
		}
		BlockRendererRegistry.instance().registerRenderPopulator((renderers, context) -> {
			if (context.state().getBlock() instanceof BakedEntityBlock) {
				renderers.add(new EmbeddiumBakedBlockEntityRenderer());
			}
		});
	}
}
