package havis.middleware.reader.llrp.service.event;

import org.junit.Assert;
import org.junit.Test;

public class LLRPEventHandlerTest {
	
	private int test = 0;

	@Test
	public void checkAddRemove() {
		LLRPEventHandler<Object> eventHandler = new LLRPEventHandler<Object>();
		
		LLRPEventHandler.LLRPEvent<Object> event1 = new LLRPEventHandler.LLRPEvent<Object>() {
			@Override
			public void fire(Object sender, Object eventArgs) {
				
			}
		};
		
		LLRPEventHandler.LLRPEvent<Object> event2 = new LLRPEventHandler.LLRPEvent<Object>() {
			@Override
			public void fire(Object sender, Object eventArgs) {
				
			}
		};
		
		LLRPEventHandler.LLRPEvent<Object> event3 = new LLRPEventHandler.LLRPEvent<Object>() {
			@Override
			public void fire(Object sender, Object eventArgs) {
				
			}
		};

		eventHandler.add(event1);
		eventHandler.add(event2);
		eventHandler.add(event3);
		
		Assert.assertEquals(3, eventHandler.getDelegates().size());
		
		eventHandler.remove(event1);
		eventHandler.remove(event2);
		eventHandler.remove(event3);
		
		Assert.assertEquals(0, eventHandler.getDelegates().size());
	}

	@Test
	public void checkHandle() {
		
		final Integer v1 = new Integer(1);
		test = 0;
		
		LLRPEventHandler<Object> eventHandler = new LLRPEventHandler<Object>();
		
		LLRPEventHandler.LLRPEvent<Object> event1 = new LLRPEventHandler.LLRPEvent<Object>() {
			@Override
			public void fire(Object sender, Object eventArgs) {
				Assert.assertEquals(v1, eventArgs);
				test++;
			}
		};
		
		LLRPEventHandler.LLRPEvent<Object> event2 = new LLRPEventHandler.LLRPEvent<Object>() {
			@Override
			public void fire(Object sender, Object eventArgs) {
				Assert.assertEquals(v1, eventArgs);
				test++;
			}
		};
		
		LLRPEventHandler.LLRPEvent<Object> event3 = new LLRPEventHandler.LLRPEvent<Object>() {
			@Override
			public void fire(Object sender, Object eventArgs) {
				Assert.assertEquals(v1, eventArgs);
				test++;
			}
		};

		eventHandler.add(event1);
		eventHandler.add(event2);
		eventHandler.add(event3);
		
		eventHandler.handleEvent(this, v1);
		
		eventHandler.remove(event1);
		eventHandler.remove(event2);
		eventHandler.remove(event3);
		
		Assert.assertEquals(3, test);
	}
}