package havis.middleware.reader.llrp.service.event;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LLRPEventHandler<T> {

	private List<LLRPEventHandler.LLRPEvent<T>> delegates = new CopyOnWriteArrayList<LLRPEventHandler.LLRPEvent<T>>();

	public void add(LLRPEventHandler.LLRPEvent<T> e) {
		delegates.add(e);
	}

	public void remove(LLRPEventHandler.LLRPEvent<T> e) {
		delegates.remove(e);
	}

	public void handleEvent(Object sender, T eventArgs) {
		for (LLRPEventHandler.LLRPEvent<T> e : delegates) {
			e.fire(sender, eventArgs);
		}
	}

	public Collection<LLRPEventHandler.LLRPEvent<T>> getDelegates() {
		return Collections.unmodifiableCollection(delegates);
	}

	public static interface LLRPEvent<T> {
		void fire(Object sender, T eventArgs);
	}
}