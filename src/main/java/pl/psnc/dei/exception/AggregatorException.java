package pl.psnc.dei.exception;

import lombok.Getter;
import pl.psnc.dei.model.Aggregator;

public class AggregatorException extends RuntimeException {

    @Getter
    private Aggregator aggregator;

    public AggregatorException() {
        this.aggregator = Aggregator.UNKNOWN;
    }

    public AggregatorException(Aggregator agg) {
        this.aggregator = agg;
    }

    public AggregatorException(Aggregator agg, String message) {
        super(message);
        this.aggregator = agg;
    }

    public AggregatorException(Aggregator agg, String message, Throwable cause) {
        super(message, cause);
        this.aggregator = agg;
    }
}
