package red.vuis.frontutil.client.widget;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BoolButtonWidget extends ButtonWidget {
	private boolean value;
	
	public BoolButtonWidget(boolean initial, WidgetDim dim, PressAction onPress) {
		super(dim.x(), dim.y(), dim.width(), dim.height(), boolText(initial), onPress, DEFAULT_NARRATION_SUPPLIER);
		this.value = initial;
	}
	
	public BoolButtonWidget(boolean initial, WidgetDim dim) {
		this(initial, dim, button -> {});
	}
	
	@Override
	public void onPress() {
		value = !value;
		updateMessage();
		super.onPress();
	}
	
	private void updateMessage() {
		setMessage(boolText(value));
	}
	
	public boolean getBool() {
		return value;
	}
	
	public void setBool(boolean value) {
		this.value = value;
		updateMessage();
	}
	
	private static Text boolText(boolean value) {
		return Text.literal(value ? "TRUE" : "FALSE").formatted(value ? Formatting.GREEN : Formatting.RED);
	}
}
