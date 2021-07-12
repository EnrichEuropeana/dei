package pl.psnc.dei.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.PersistableExceptionRepository;
import pl.psnc.dei.model.PersistableException;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.Context;
import pl.psnc.dei.model.exception.TranscriptionPlatformException;

import java.util.List;
import java.util.Optional;

@Service
public class PersistableExceptionService {

    @Autowired
    private PersistableExceptionRepository persistableExceptionRepository;

    public <T> T findByContextAndExceptionClass(Context context, Class<T> exceptionClass) throws NotFoundException {
        PersistableException.ExceptionType exceptionType = this.convertExceptionClassToExceptionType(exceptionClass);
        Optional<PersistableException> optionalExceptions = this.persistableExceptionRepository.findByContextAndType(context, exceptionType);
        if (optionalExceptions.isPresent()) {
            return this.inflateException(optionalExceptions.get(), exceptionClass);
        }
        else {
            throw new NotFoundException("No exceptions saved for record identifier: " + context.getRecord().getIdentifier());
        }
    }

    public PersistableException save(Exception exception, Context context) {
        PersistableException.ExceptionType exceptionType = this.convertExceptionClassToExceptionType(exception.getClass());
        Optional<PersistableException> optionalException = this.persistableExceptionRepository.findByContextAndType(context, exceptionType);
        if (optionalException.isPresent()) {
            PersistableException fetchedException = optionalException.get();
            fetchedException.setMessage(exception.getMessage());
            this.persistableExceptionRepository.save(fetchedException);
        }
        else {
            PersistableException newPersistableException = new PersistableException();
            newPersistableException.setContext(context);
            newPersistableException.setMessage(exception.getMessage());
            newPersistableException.setType(this.convertExceptionClassToExceptionType(exception.getClass()));
            this.persistableExceptionRepository.save(newPersistableException);
        }
        throw new IllegalArgumentException("Cannot save exception of type: " + exception.getClass());
    }

    private <T> T inflateException(PersistableException persistableException, Class<T> exceptionClass) {
        switch (persistableException.getType()) {
            case TRANSCRIPTION_PLATFORM_EXCEPTION: {
                // unchecked cast without it
                if (exceptionClass.isAssignableFrom(TranscriptionPlatformException.class)) {
                    return exceptionClass.cast(new TranscriptionPlatformException(persistableException.getMessage()));
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
        throw new IllegalArgumentException("Class " + aClass.getName() + " cannot be translated");
    }
}
