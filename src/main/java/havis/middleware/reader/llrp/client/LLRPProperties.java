package havis.middleware.reader.llrp.client;

/**
 * Class that represents a set of properties for LLRP reader connector.
 */
public class LLRPProperties {
	private int keepalive = 30000;
	private int inventoryAttempts = 3;

	/**
	 * Gets the keepalive interval after which no new messages from the reader
	 * cause the sending of an keepalive message. The defaultz value is 30000
	 * milli seconds.
	 * 
	 * @return keepalive
	 */
	public int getKeepalive() {
		return this.keepalive;
	}

	public void setKeepalive(int keepalive) {
		this.keepalive = keepalive;
	}

	/**
	 * Sets the number of inventory attempts to find the tag to execute
	 * operation on. The dafault value is 3.
	 * 
	 * @param inventoryAttempts
	 *            To set
	 */
	public void setInventoryAttempts(int inventoryAttempts) {
		this.inventoryAttempts = inventoryAttempts;
	}

	/**
	 * Returns the number of inventory attempts to find the tag to execute
	 * operation on. The dafault value is 3.
	 * 
	 * @return inventoryAttempts
	 */
	public int getInventoryAttempts() {
		return this.inventoryAttempts;
	}

	/**
	 * Static class that hold all property names for LLRP reader connector.
	 */
	public static class PropertyName {
		/**
		 * Describe the keepalive timeout value.
		 */
		public static final String Keepalive = havis.middleware.ale.reader.Prefix.Connector
				+ "Keepalive";
		/**
		 * Describe the inventory attempty value.
		 */
		public static final String InventoryAttempts = havis.middleware.ale.reader.Prefix.Connector
				+ "InventoryAttempts";
	}
}
