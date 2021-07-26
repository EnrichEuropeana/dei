package pl.psnc.dei.controllers;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.DAO.RecordsRepository;

@RunWith(SpringRunner.class)
@ActiveProfiles("integration")
@SpringBootTest
public class TranscriptionControllerTest {

    private final RecordsRepository recordsRepository;

    public TranscriptionControllerTest(RecordsRepository recordsRepository) {
        this.recordsRepository = recordsRepository;
    }
}
