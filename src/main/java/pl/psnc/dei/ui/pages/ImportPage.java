package pl.psnc.dei.ui.pages;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import pl.psnc.dei.model.DAO.RecordsRepository;
import pl.psnc.dei.model.Project;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.service.TranscriptionPlatformService;
import pl.psnc.dei.ui.MainView;
import pl.psnc.dei.ui.components.ProjectSelectionComponent;

import java.util.List;

/**
 * Page for import generation
 * <p>
 * Created by pwozniak on 4/8/19
 */
@Route(value = "import", layout = MainView.class)
public class ImportPage extends HorizontalLayout {

    public ImportPage(RecordsRepository repo,
                      TranscriptionPlatformService transcriptionPlatformService) {
        add(
                new ProjectSelectionComponent(transcriptionPlatformService, new ProjectChangeListener(), null));

        List<Record> allRecords = repo.findAll();
        for (Record r : allRecords) {
            //display records
        }
    }

    class ProjectChangeListener implements HasValue.ValueChangeListener<HasValue.ValueChangeEvent<Project>> {

        @Override
        public void valueChanged(HasValue.ValueChangeEvent<Project> event) {
            Project project = (Project) event.getValue();
        }
    }
}


