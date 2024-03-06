package pl.psnc.dei.model.DAO;

import org.springframework.stereotype.Repository;
import pl.psnc.dei.model.Record;
import pl.psnc.dei.model.enrichments.PersonEnrichment;

import java.util.Optional;

@Repository
public interface PersonEnrichmentRepository extends MetadataEnrichmentRepository<PersonEnrichment> {

    Optional<PersonEnrichment> findByRecordAndFirstNameAndLastNameAndBirthPlaceAndBirthDateAndDeathPlaceAndDeathDateAndItemLink(Record record, String firstName, String lastName, String birthPlace, String birthDate, String deathPlace, String deathDate, String itemLink);

    boolean existsByRecordAndFirstNameAndLastNameAndBirthPlaceAndBirthDateAndDeathPlaceAndDeathDateAndItemLink(Record record, String firstName, String lastName, String birthPlace, String birthDate, String deathPlace, String deathDate, String itemLink);
}
