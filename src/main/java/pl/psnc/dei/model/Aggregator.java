package pl.psnc.dei.model;


public enum Aggregator {
	UNKNOWN(-1, "Unknown"),
	EUROPEANA(0, "Europeana"),
	DDB(1, "Deutsche Digitale Bibliothek");

	private int id;
	private String fullName;

	public int getId() {
		return id;
	}

	Aggregator(int id, String fullName) {
		this.id = id;
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public static Aggregator getById(int id) {
		for (Aggregator aggregator : values()) {
			if (aggregator.getId() == id) {
				return aggregator;
			}
		}
		return UNKNOWN;
	}

	public static boolean isValid(String id) {
		try {
			int aggregatorId = Integer.parseInt(id);
			return isValid(aggregatorId);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isValid(int id) {
		Aggregator aggregator = getById(id);
		return aggregator != UNKNOWN;
	}

	@Override
	public String toString() {
		return this.fullName;
	}
}
