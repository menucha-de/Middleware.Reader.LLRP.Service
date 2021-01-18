package havis.middleware.reader.llrp.util;

public class IDGenerator {

	private static long uniqueMessageID = 0;

	private IDGenerator() {
	}

	public static synchronized long getUniqueMessageID() {
		return ++uniqueMessageID;
	}
}