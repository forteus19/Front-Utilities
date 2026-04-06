package red.vuis.frontutil.client.widget;

import java.util.Optional;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import red.vuis.frontutil.util.math.DoubleBounds;

public class DoubleFieldWidget extends TextFieldWidget {
	public final DoubleBounds bounds;
	
	public DoubleFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message, DoubleBounds bounds) {
		super(textRenderer, x, y, width, height, message);
		this.bounds = bounds;
		setTextPredicate(str -> {
			if (str == null) {
				return false;
			}
			if (str.isEmpty()) {
				return true;
			}
			try {
				Double.parseDouble(str);
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		});
		setChangedListener(str -> {
			if (str.isEmpty()) {
				return;
			}
			
			double value = Double.parseDouble(str);
			if (!bounds.within(value)) {
				setText(Double.toString(bounds.clamp(value)));
			}
		});
	}
	
	public DoubleFieldWidget(TextRenderer font, int x, int y, int width, int height, Text message) {
		this(font, x, y, width, height, message, DoubleBounds.all());
	}
	
	public DoubleFieldWidget(TextRenderer font, WidgetDim dim, Text message, DoubleBounds bounds) {
		this(font, dim.x(), dim.y(), dim.width(), dim.height(), message, bounds);
	}
	
	public DoubleFieldWidget(TextRenderer font, WidgetDim dim, Text message) {
		this(font, dim, message, DoubleBounds.all());
	}
	
	public Optional<Double> getOptionalDouble() {
		try {
			return Optional.of(getDouble());
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}
	
	public double getDouble() {
		return Double.parseDouble(getText());
	}
	
	public void setDouble(double value) {
		setText(Double.toString(bounds.clamp(value)));
		setCursorToStart(false);
	}
}
