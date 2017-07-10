package org.mayukh.rivet.core;

import org.mayukh.xparse.dom.RegularXml;
import org.mayukh.xparse.dom.XmlElement;
import org.mayukh.xparse.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by mayukh42 on 11/6/17.
 * The Rivet DI container
 * Reads bean definitions from rivet/rivet-config.xml in resources
 *
 * Bean lifecycle:
 *  First, a map of bean references and corresponding Bean objects are created.
 *  Then, actual bean objects are created on demand.
 */
public class DiContainer {

    /* created beans */
    static final Map<String, Object> beanstore = new HashMap<>();

    /* requested beans, for a lazy instantiation approach */
    static final Map<String, Bean> beanDefs = new HashMap<>();

    /**
     * getBean(ref): returns bean already created in beanstore
     * TODO: implement on-demand creation
     */
    public static Object getBean(String ref) {
        if (beanstore.containsKey(ref)) return beanstore.get(ref);
        else if (beanDefs.containsKey(ref)) {
            // bean defined but not yet created
            Bean bean = beanDefs.get(ref);
            return bean.create();
        }
        else return null;
    }

    /**
     * add newly created bean to beanstore
     */
    static void addBean(String ref, Object object) {
        if (ref != null && ref.length() > 0 && object != null)
            beanstore.put(ref, object);
        else throw new RivetException("Bean invalid");
    }

    /**
     * read bean defs from XML file.
     *  1. create XmlElement (generic DOM)
     *  2. create semantic DOM from XmlElement
     *      the semantic DOM is the beanDefs map
     *
     * schema:
     * <beans>
     *  bean with cons param:
     *  <bean name="battery" type="language.reflect.rivet.model.Battery">
     *      <cons-param name="name" type="java.lang.String" value="Amaron" />
     *      <cons-param name="chargeLeft" type="java.lang.Double" value="42.0" />
     *  </bean>
     *
     *  bean with setter param:
     *  <bean name="tesla" type="language.reflect.rivet.model.ElectricCar">
     *      <setter-param name="name" type="java.lang.String" value="Tesla" />
     *  </bean>
     *
     *  bean with inline properties; default cons, fields set to default
     *  <bean name="compactEV" type="language.reflect.rivet.model.ElectricCar" />
     * </beans>
     */
    static void createBeanDefs() {
        XmlElement xml = parseXmlFromFile();
        if (Objects.equals("beans", xml.getStartTag().getName())) {
            RegularXml beansXml = (RegularXml) xml;

            // iterate through each bean
            for (XmlElement child : beansXml.getChildren()) {
                String beanName = child.getStartTag().getAttributes().get("name");
                String beanType =  child.getStartTag().getAttributes().get("type");

                List<ConsParam> consParams = null;
                Map<String, SetterParam> setterParams = null;

                if (child instanceof RegularXml)  {
                    // bean tag is a regular xml
                    RegularXml beanXml = (RegularXml) child;

                    // iterate through each cons-param or setter-param (both can exist)
                    for (XmlElement paramXml : beanXml.getChildren()) {
                        Map<String, String> attributes = paramXml.getStartTag().getAttributes();

                        if (Objects.equals("cons-param", paramXml.getStartTag().getName())) {
                            // cons-param
                            if (consParams == null) consParams = new ArrayList<>();
                            ConsParam consParam = new ConsParam(
                                    attributes.get("name"),
                                    attributes.get("type"),
                                    attributes.get("value"),
                                    attributes.get("ref")
                            );
                            consParams.add(consParam);
                        }
                        else if (Objects.equals("setter-param", paramXml.getStartTag().getName())){
                            // setter-param
                            if (setterParams == null) setterParams = new HashMap<>();
                            SetterParam setterParam = new SetterParam(
                                    attributes.get("name"),
                                    attributes.get("type"),
                                    attributes.get("value"),
                                    attributes.get("ref")
                            );
                            setterParams.put(attributes.get("name"), setterParam);
                        }
                    }
                }
                beanDefs.put(beanName,
                        new Bean(beanName, beanType, setterParams, consParams));    // last two null for inline defs
            }
        }
    }

    /**
     * utility function to parse XML DOM from a well-formed xml config file.
     * Standard location for the xml file: src/main/resources/rivet/rivet-config.xml
     */
    private static XmlElement parseXmlFromFile() {
        String location = "src/main/resources/rivet/";
        Path filePath = Paths.get(location, "rivet-config.xml");

        byte[] fileContentsInBytes = null;
        try {
            fileContentsInBytes = Files.readAllBytes(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String data = new String(fileContentsInBytes);
        XmlElement xml = Parser.parse(data);
        return xml;
    }

    // for testing only
    static Map<String, Bean> getBeanDefs() {
        return beanDefs;
    }

    static Map<String, Object> getBeanstore() {
        return beanstore;
    }

    static void reset() {
        Set<String> refs = new HashSet<>(beanDefs.keySet());    // avoid concurrent modification exception
        for (String ref : refs) {
            beanstore.remove(ref);
            beanDefs.remove(ref);
        }
    }
}
