package pl.psnc.dei.model.enrichments.xml;

import javax.xml.bind.annotation.XmlAttribute;

public class DCDate {
    @XmlAttribute(namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#", name = "about")
    private String about;
}
