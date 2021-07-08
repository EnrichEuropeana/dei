package pl.psnc.dei.service.context;

import pl.psnc.dei.model.Record;

public abstract class ContextService<T> {
    public abstract T get (Record record);
    public abstract T save (T context);
}
