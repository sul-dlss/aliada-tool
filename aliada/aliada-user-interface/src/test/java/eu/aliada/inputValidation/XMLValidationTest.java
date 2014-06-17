// ALIADA - Automatic publication under Linked Data paradigm
//          of library and museum data
//
// Component: aliada-user-interface
// Responsible: ALIADA Consortium

package eu.aliada.inputValidation;

import junit.framework.TestCase;
import eu.aliada.shared.log.Log;

/**
 * @author elena
 * @since 1.0
 */
public class XMLValidationTest extends TestCase {
    private final Log log = new Log(XMLValidationTest.class);

    /**
     * @see
     * @since 1.0
     */
    public void testValidate() {
        XMLValidation xmlVal = new XMLValidation();
        boolean result = 
                //LIDO
                //xmlVal.isValidatedXMLFile("src/test/resources/lido/lido_mFBA.xml",
                //"src/main/webapp/WEB-INF/xmlValidators/lido-v1.0.xsd");
                //MARC
                xmlVal.isValidatedXMLFile("src/test/resources/marc/marc_comic.xml",
                "src/main/webapp/WEB-INF/xmlValidators/MARC21slim.xsd");
        if (result) {
            log.info("BIEN");
        } else {
            log.info("MAL");
        }
    }

}