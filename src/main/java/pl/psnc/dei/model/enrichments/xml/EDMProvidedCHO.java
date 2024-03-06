package pl.psnc.dei.model.enrichments.xml;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class EDMProvidedCHO {
    @XmlAttribute(namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#", name = "about")
    private String about;

    @XmlElement(name = "date", namespace = "http://purl.org/dc/elements/1.1/")
    
    private DCDate date;
}
