package havis.middleware.reader.llrp.util;

public class LLRPReturnContainerUtil<T> {

	private T value;
	private boolean is;

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public boolean isTrue() {
		return is;
	}

	public void setTrue(boolean is) {
		this.is = is;
	}
}