package red.vuis.frontutil.util;

import java.util.Objects;

public class Diff<T> {
	private T value;
	private T prevValue;
	private boolean updated = false;
	
	public Diff(T value) {
		this.value = value;
		this.prevValue = value;
	}
	
	public void update(T newValue) {
		updated = !Objects.equals(value, newValue);
		prevValue = value;
		value = newValue;
	}
	
	public T value() {
		return value;
	}
	
	public T prevValue() {
		return prevValue;
	}
	
	public boolean updated() {
		return updated;
	}
}
