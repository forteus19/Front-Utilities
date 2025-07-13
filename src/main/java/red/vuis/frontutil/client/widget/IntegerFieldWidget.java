package red.vuis.frontutil.client.widget;

import java.util.Optional;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

public class IntegerFieldWidget extends TextFieldWidget {
	public IntegerFieldWidget(TextRenderer font, int x, int y, int width, int height, Text message) {
		super(font, x, y, width, height, message);
		setTextPredicate(str -> (str != null && str.isEmpty()) || StringUtils.isNumeric(str));
	}
	
	public IntegerFieldWidget(TextRenderer font, WidgetDim dim, Text message) {
		this(font, dim.x(), dim.y(), dim.width(), dim.height(), message);
	}
	
	public Optional<Integer> getInt() {
		try {
			return Optional.of(Integer.parseInt(getText()));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}
	
	public void setInt(int value) {
		setText(Integer.toString(value));
	}
}
