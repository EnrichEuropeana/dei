package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.iiif.ConversionException;
import pl.psnc.dei.iiif.ConversionImpossibleException;
import pl.psnc.dei.model.DAO.PersistableExceptionRepository;
import pl.psnc.dei.model.PersistableException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.Context;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;
import pl.psnc.dei.service.context.ContextMediator;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PersistableExceptionService {

    @Autowired
    private PersistableExceptionRepository persistableExceptionRepository;

    @Autowired
    private ContextMediator contextMediator;

    public <T extends  Exception> T findByContextAndExceptionClass(Context context, Class<T> exceptionClass) {
        PersistableException.ExceptionType exceptionType = this.convertExceptionClassToExceptionType(exceptionClass);
        Optional<PersistableException> optionalExceptions = this.persistableExceptionRepository.findByContextAndType(context, exceptionType);
        return optionalExceptions.map(persistableException -> this.inflateException(persistableException, exceptionClass)).orElse(null);
    }

    public PersistableException save(Exception exception, Context context) {
        PersistableException.ExceptionType exceptionType = this.convertExceptionClassToExceptionType(exception.getClass());
        Optional<PersistableException> optionalException = this.persistableExceptionRepository.findByContextAndType(context, exceptionType);
        if (optionalException.isPresent()) {
            PersistableException fetchedException = optionalException.get();
            fetchedException.setMessage(exception.getMessage());
            PersistableException persistableException = this.persistableExceptionRepository.save(fetchedException);
            context.getExceptions().add(persistableException);
            this.contextMediator.save(context);
            return persistableException;
        }
        else {
            PersistableException newPersistableException = new PersistableException();
            newPersistableException.setContext(context);
            newPersistableException.setMessage(exception.getMessage());
            newPersistableException.setType(this.convertExceptionClassToExceptionType(exception.getClass()));
            newPersistableException = this.persistableExceptionRepository.save(newPersistableException);
            context.getExceptions().add(newPersistableException);
            this.contextMediator.save(context);
            return newPersistableException;
        }
    }

    // ConversionException | InterruptedException | IOException

    private <T extends Exception> T inflateException(PersistableException persistableException, Class<T> exceptionClass) {
        switch (persistableException.getType()) {
            case TRANSCRIPTION_PLATFORM_EXCEPTION: {
                // unchecked cast without it
                if (exceptionClass.isAssignableFrom(TranscriptionPlatformException.class)) {
                    return exceptionClass.cast(new TranscriptionPlatformException(persistableException.getMessage()));
                }
            }
            case NOT_FOUND_EXCEPTION: {
                if (exceptionClass.isAssignableFrom(NotFoundException.class)) {
                    return exceptionClass.cast(new NotFoundException(persistableException.getMessage()));
                }
            }

            case IO_EXCEPTION: {
                if (exceptionClass.isAssignableFrom(IOException.class)) {
                    return exceptionClass.cast(new IOException(persistableException.getMessage()));
                }
            }
            case CONVERSION_EXCEPTION: {
                if (exceptionClass.isAssignableFrom(ConversionImpossibleException.class)) {
                    return exceptionClass.cast(new ConversionImpossibleException(persistableException.getMessage()));
                }
            }
            case INTERRUPTED_EXCEPTION: {
                if (exceptionClass.isAssignableFrom(InterruptedException.class)) {
                    return exceptionClass.cast(new InterruptedException(persistableException.getMessage()));
                }
            }
            case CONVERSION_IMPOSSIBLE_EXCEPTION: {
                if (exceptionClass.isAssignableFrom(ConversionImpossibleException.class)) {
                    return exceptionClass.cast(new ConversionImpossibleException(persistableException.getMessage()));
                }
            }
            default: {
                throw new IllegalArgumentException("Cannot inflate exception of type: " + exceptionClass.getName());
            }
        }
    }

    private <T> PersistableException.ExceptionType convertExceptionClassToExceptionType(Class<T> aClass) {
        if (aClass.isAssignableFrom(TranscriptionPlatformException.class)) {
            return PersistableException.ExceptionType.TRANSCRIPTION_PLATFORM_EXCEPTION;
        }
        else if (aClass.isAssignableFrom(NotFoundException.class)) {
            return PersistableException.ExceptionType.NOT_FOUND_EXCEPTION;
        }
        else if (aClass.isAssignableFrom(IOException.class)) {
            return PersistableException.ExceptionType.IO_EXCEPTION;
        }
        else if (aClass.isAssignableFrom(ConversionException.class)) {
            return PersistableException.ExceptionType.CONVERSION_EXCEPTION;
        }
        else if (aClass.isAssignableFrom(ConversionImpossibleException.class)) {
            return PersistableException.ExceptionType.CONVERSION_IMPOSSIBLE_EXCEPTION;
        }
        else if (aClass.isAssignableFrom(InterruptedException.class)) {
            return PersistableException.ExceptionType.INTERRUPTED_EXCEPTION;
        }
        throw new IllegalArgumentException("Class " + aClass.getName() + " cannot be translated");
    }

    public void findFirstOfAndThrow(List<Class<? extends Exception>> exceptionClasses, Context context) throws Exception {
        for (Class<? extends Exception> aClass : exceptionClasses) {
            Exception exception = this.findByContextAndExceptionClass(context, aClass);
            if (exception != null) {
                throw exception;
            }
        }
    }
}
