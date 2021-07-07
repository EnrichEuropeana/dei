package pl.psnc.dei.model.DAO;


import org.springframework.data.repository.CrudRepository;
import pl.psnc.dei.model.conversion.ConversionContext;

public interface ConversionContextRepository extends CrudRepository<ConversionContext, Long> {
}
