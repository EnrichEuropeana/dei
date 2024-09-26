package pl.psnc.dei.model.enrichments.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RDF", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
public class EDMEnrichments {
    @XmlElement(namespace = "http://www.europeana.eu/schemas/edm/", name = "ProvidedCHO")
    private EDMProvidedCHO providedCHO;
}
