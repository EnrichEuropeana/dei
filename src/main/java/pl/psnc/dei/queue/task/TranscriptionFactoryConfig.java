package pl.psnc.dei.queue.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.psnc.dei.model.TranscriptionType;
import pl.psnc.dei.model.factory.HTRTranscriptionFactory;
import pl.psnc.dei.model.factory.ManualTranscriptionFactory;
import pl.psnc.dei.model.factory.TranscriptionFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class TranscriptionFactoryConfig {
    @Bean
    Map<TranscriptionType, TranscriptionFactory> transcriptionFactories(
            @Autowired List<TranscriptionFactory> factories) {
        Map<TranscriptionType, TranscriptionFactory> factoryMap = new HashMap<>();
        factoryMap.put(TranscriptionType.MANUAL, factories.stream()
                .filter(transcriptionFactory -> transcriptionFactory instanceof ManualTranscriptionFactory).findFirst()
                .orElseThrow());
        factoryMap.put(TranscriptionType.HTR, factories.stream()
                .filter(transcriptionFactory -> transcriptionFactory instanceof HTRTranscriptionFactory).findFirst()
                .orElseThrow());
        return factoryMap;
    }
}
