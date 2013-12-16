package couchbase.client;

import couchbase.client.xjc.generated.RulesType;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


public class RulesFileParser {

    public RulesType parseRulesFile() throws SAXException, ParserConfigurationException, IOException, JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance("com.unibet.mockhttpserver.xjc.generated");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RulesType rulesType = (RulesType) unmarshaller.unmarshal(getClass().getClassLoader().getResourceAsStream("rules.xml"));

        return rulesType;
    }
}
