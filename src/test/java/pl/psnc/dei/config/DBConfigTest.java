package pl.psnc.dei.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Record;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DBConfigTest {
    @Autowired
    private RecordsRepository recordsRepository;

    private Record recordToPersist;

    @Before
    public void initRecord() {
        this.recordToPersist = new Record();
        this.recordToPersist.setTitle("Just Testing");
    }

    @Test
    public void shouldPersistRecord() {
        Record persistedRecord = this.recordsRepository.save(this.recordToPersist);
        // check if DB saved data properly
        assertEquals(this.recordToPersist.getTitle(), persistedRecord.getTitle());
        Optional<Record> fetchedRecord = this.recordsRepository.findById(persistedRecord.getId());
        assertTrue(fetchedRecord.isPresent());
        assertEquals(fetchedRecord.get().getTitle(), this.recordToPersist.getTitle());
        assertEquals(fetchedRecord.get().getTitle(), persistedRecord.getTitle());
    }
}
