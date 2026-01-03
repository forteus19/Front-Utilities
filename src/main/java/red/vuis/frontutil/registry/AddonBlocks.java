package red.vuis.frontutil.registry;

import java.util.function.Supplier;

import com.boehmod.blockfront.common.block.TinyFloorBlock;
import com.boehmod.blockfront.registry.BFBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import red.vuis.frontutil.AddonConstants;

@SuppressWarnings("unused")
public final class AddonBlocks {
	public static final DeferredRegister.Blocks DR = DeferredRegister.createBlocks(AddonConstants.MOD_ID);
	
	public static final DeferredBlock<TinyFloorBlock> BLOOD_NO_COLLISION = registerWithBlockItem(
		"blood_no_collision",
		() -> new TinyFloorBlock(
			AbstractBlock.Settings.copy(BFBlocks.BLOOD.get()).noCollision()
		)
	);
	public static final DeferredBlock<TinyFloorBlock> DEAD_FISH_NO_COLLISION = registerWithBlockItem(
		"dead_fish_no_collision",
		() -> new TinyFloorBlock(
			AbstractBlock.Settings.copy(BFBlocks.DEAD_FISH.get()).noCollision()
		)
	);
	
	private AddonBlocks() {
	}
	
	private static <T extends Block> DeferredBlock<T> registerWithBlockItem(String id, Supplier<T> blockSupplier) {
		DeferredBlock<T> block = DR.register(id, blockSupplier);
		AddonItems.DR.registerSimpleBlockItem(id, block);
		return block;
	}
	
	public static void init(IEventBus eventBus) {
		DR.register(eventBus);
	}
}
