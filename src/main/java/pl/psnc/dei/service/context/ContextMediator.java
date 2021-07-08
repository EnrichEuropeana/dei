package pl.psnc.dei.service.context;

import org.hibernate.sql.Update;
import org.springframework.stereotype.Service;
import pl.psnc.dei.exception.NotFoundException;
import pl.psnc.dei.model.DAO.*;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.conversion.*;
import pl.psnc.dei.queue.task.ConversionTask;
import pl.psnc.dei.queue.task.Task;
import pl.psnc.dei.queue.task.TranscribeTask;

import java.util.List;
import java.util.Optional;

/**
 * Service used to manage context of processing. Each context could be possibly saved in DB by usage of this class
 */
@Service
public class ContextMediator {

    private final List<ContextService> contextServiceList;

    public ContextMediator(List<ContextService> contextServiceList){
        this.contextServiceList = contextServiceList;
    };

    public get(Record record) {

    }

}
