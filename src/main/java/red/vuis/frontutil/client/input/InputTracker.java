package red.vuis.frontutil.client.input;

import net.minecraft.client.Mouse;
import net.neoforged.fml.loading.FMLEnvironment;

import red.vuis.frontutil.util.Diff;

public final class InputTracker {
	private static InputTracker instance = null;
	
	private final Diff<Boolean> leftClicked = new Diff<>(false);
	
	private InputTracker() {
	}
	
	public static InputTracker getInstance() {
		if (!FMLEnvironment.dist.isClient()) {
			throw new RuntimeException("Tried to get input tracker when not on the client!");
		}
		if (instance == null) {
			instance = new InputTracker();
		}
		return instance;
	}
	
	public void update(Mouse mouse) {
		leftClicked.update(mouse.wasLeftButtonClicked());
	}
	
	public boolean leftClicked() {
		return leftClicked.updated() && leftClicked.value() == true;
	}
	
	public boolean leftReleased() {
		return leftClicked.updated() && leftClicked.value() == false;
	}
}
