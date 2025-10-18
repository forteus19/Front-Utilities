package red.vuis.frontutil.client.widget;

import java.util.Optional;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import red.vuis.frontutil.util.IntBounds;

import static red.vuis.frontutil.util.IntBounds.intAll;

public class IntegerFieldWidget extends TextFieldWidget {
	public final IntBounds bounds;
	
	public IntegerFieldWidget(TextRenderer font, int x, int y, int width, int height, Text message, IntBounds bounds) {
		super(font, x, y, width, height, message);
		this.bounds = bounds;
		setTextPredicate(str -> {
			if (str == null) {
				return false;
			}
			if (str.isEmpty() || StringUtils.isNumeric(str)) {
				return true;
			}
			if (str.charAt(0) == '-' && StringUtils.isNumeric(str.substring(1))) {
				return true;
			}
			return false;
		});
		setChangedListener(str -> {
			int value = Integer.parseInt(str);
			if (!bounds.within(value)) {
				setText(Integer.toString(bounds.clamp(value)));
			}
		});
	}
	
	public IntegerFieldWidget(TextRenderer font, int x, int y, int width, int height, Text message) {
		this(font, x, y, width, height, message, intAll());
	}
	
	public IntegerFieldWidget(TextRenderer font, WidgetDim dim, Text message, IntBounds bounds) {
		this(font, dim.x(), dim.y(), dim.width(), dim.height(), message, bounds);
	}
	
	public IntegerFieldWidget(TextRenderer font, WidgetDim dim, Text message) {
		this(font, dim, message, intAll());
	}
	
	public Optional<Integer> getInt() {
		try {
			return Optional.of(Integer.parseInt(getText()));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}
	
	public void setInt(int value) {
		setText(Integer.toString(bounds.clamp(value)));
	}
}
