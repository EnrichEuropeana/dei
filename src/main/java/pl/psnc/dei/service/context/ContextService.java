package pl.psnc.dei.service.context;

import pl.psnc.dei.model.Record;

/**
 * Service used for manipulation on concrete service
 *
 * @param <T> context class being served by this service
 */
public interface ContextService<T> {
    T get(Record record);

    T save(T context);

    void delete(T context);

    Boolean canHandle(Record record);

    Boolean canHandle(Class<?> aClass);
}
