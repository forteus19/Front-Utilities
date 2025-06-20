package red.vuis.frontutil.client.widget;

import java.util.Optional;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

public class IntegerEditBox extends EditBox {
	public IntegerEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
		setFilter(str -> (str != null && str.isEmpty()) || StringUtils.isNumeric(str));
	}
	
	public IntegerEditBox(Font font, WidgetDim dim, Component message) {
		this(font, dim.x(), dim.y(), dim.width(), dim.height(), message);
	}
	
	public Optional<Integer> getIntValue() {
		try {
			return Optional.of(Integer.parseInt(getValue()));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}
	
	public void setIntValue(int value) {
		setValue(Integer.toString(value));
	}
}
