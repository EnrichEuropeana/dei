package pl.psnc.dei.exception;

import pl.psnc.dei.model.Aggregator;

public class EuropeanaAggregatorException extends AggregatorException {

    private static final Aggregator aggregator = Aggregator.EUROPEANA;

    public EuropeanaAggregatorException() {
        super(aggregator);
    }

    public EuropeanaAggregatorException(String message) {
        super(aggregator, "Error during retrieving data from Europeana. Reason: " + message);
    }

    public EuropeanaAggregatorException(String message, Throwable cause) {
        super(aggregator, "Error during retrieving data from Europeana. Reason: " + message, cause);
    }
}
