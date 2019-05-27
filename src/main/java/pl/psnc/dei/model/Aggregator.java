package pl.psnc.dei.model;

import java.util.HashMap;
import java.util.Map;

public enum Aggregator {
	EUROPEANA(0, "Europeana"),
	DDB(1, "Deutsche Digitale Bibliothek");

	private int id;
	private String fullName;

	private static final Map<Integer, Aggregator> map = new HashMap<>();
	static {
		for(Aggregator aggregator : Aggregator.values())
			map.put(aggregator.getId(), aggregator);
	}

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

	public static Aggregator getAggregator(int id) {
		return map.get(id);
	}

	@Override
	public String toString() {
		return this.fullName;
	}
}
