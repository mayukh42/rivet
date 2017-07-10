package org.mayukh.rivet.core;

import org.junit.*;
import org.mayukh.rivet.model.Battery;
import org.mayukh.rivet.model.ElectricCar;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mayukh42 on 6/12/2017.
 *
 * Tests for the Rivet framework
 * TODO: make the reset method run before each test; i.e. make each test independent
 */
public class BeanTest {

    private static OutputStream out;
    private static PrintStream ps;

    private void resetDiContainer() {
        DiContainer.reset();
    }

    @BeforeClass
    public static void setup() {
        Path outputPath = Paths.get("target/", "rivet-output.txt");
        try {
            out = Files.newOutputStream(outputPath);
            ps = new PrintStream(out);
            System.setOut(ps);
            System.setErr(ps);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void cleanup() {
        try {
            if (out != null) out.close();
            if (ps != null) ps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * testSetterParams()
     * <bean name="myBattery" type="org.mayukh.rivet.model.Battery">
     *     <setter-param name="name" type="java.lang.String" value="Amaron" />
     *     <setter-param name="chargeLeft" type="java.lang.Double" value="40.0" />
     * </bean>
     */
    @Test
    public void testSetterParamsString() {
        SetterParam name = new SetterParam("name", "java.lang.String", "Amaron", null);
        SetterParam chargeLeft = new SetterParam("chargeLeft", "java.lang.Double", null,
                "randomDouble");
        System.out.println(name);
        System.out.println(chargeLeft);
    }

    @Test
    public void testConsParamsString() {
        ConsParam name = new ConsParam("name", "java.lang.String", "Amaron", null);
        ConsParam chargeLeft = new ConsParam("chargeLeft", "java.lang.Double", null,
                "randomDouble");
        System.out.println(name);
        System.out.println(chargeLeft);
    }

    @Test
    public void testCreatePrimitiveParam() {
        SetterParam integerField = new SetterParam("number", "java.lang.Integer", "42", null);
        Integer number = (Integer) integerField.create();
        System.out.println(number + ", " + number.getClass().getName());
    }

    private Battery createBatteryUsingSetters() {
        /* Read setterParams and create map from xml/ json file */
        SetterParam batteryName = new SetterParam("name", "java.lang.String", "Amaron", null);
        SetterParam batteryChargeLeft = new SetterParam("chargeLeft", "java.lang.Double", "40.0",
                null);
        Map<String, SetterParam> batterySetters = new HashMap<>();
        batterySetters.put("name", batteryName);
        batterySetters.put("chargeLeft", batteryChargeLeft);

        Bean batteryBean = new Bean("battery", "org.mayukh.rivet.model.Battery", batterySetters,
                null);
        Battery battery = (Battery) batteryBean.create();
        return battery;
    }

    private Battery createBatteryUsingCons() {
        /* Read consParams and create map from xml/ json file */
        ConsParam batteryName = new ConsParam("name", "java.lang.String", "Amaron", null);
        ConsParam batteryChargeLeft = new ConsParam("chargeLeft", "java.lang.Double", "40.0",
                null);
        List<ConsParam> batteryCons = new ArrayList<>();
        batteryCons.add(batteryName);
        batteryCons.add(batteryChargeLeft);

        Bean batteryBean = new Bean("battery", "org.mayukh.rivet.model.Battery", null,
                batteryCons);
        Battery battery = (Battery) batteryBean.create();
        return battery;
    }

    private ElectricCar createElectricCarUsingSetters() {
        // no need to capture the output; it is added in beanstore from which it will be later picked up
        createBatteryUsingSetters();

        // only non-injectable fields need to be specified in xml
        SetterParam ecName = new SetterParam("name", "java.lang.String", "Tesla", null);
        Map<String, SetterParam> ecParams = new HashMap<>();
        ecParams.put("name", ecName);

        Bean ecBean = new Bean("tesla", "org.mayukh.rivet.model.ElectricCar", ecParams,
                null);
        return (ElectricCar) ecBean.create();
    }

    @Test
    public void testCreateBeanWithNewSetter() {
        Battery battery = createBatteryUsingSetters();
        System.out.println(battery);
        assertEquals("Battery name should be Amaron", "Amaron", battery.getName());
        assertEquals("Battery charge left should be 40.0%", 40.0, battery.getChargeLeft(), 0.01);
    }

    @Test
    public void testCreateBeanWithConsParams() {
        Battery battery = createBatteryUsingCons();
        System.out.println(battery);
        assertEquals("Battery name should be Amaron", "Amaron", battery.getName());
        assertEquals("Battery charge left should be 40.0%", 40.0, battery.getChargeLeft(), 0.01);
    }

    @Test
    public void testCreateBeanWithSetterRef() {
        ElectricCar tesla = createElectricCarUsingSetters();
        System.out.println(tesla);

        Battery battery = tesla.getBattery();
        assertEquals("Battery name should be Amaron", "Amaron", battery.getName());
        assertEquals("Battery charge left should be 40.0%", 40.0, battery.getChargeLeft(), 0.01);

        assertEquals("Car name should be Tesla", "Tesla", tesla.getName());
    }

    @Test
    public void testBeanDefsCreationFromXmlFile() {
        DiContainer.createBeanDefs();
        Map<String, Bean> beanDefs = DiContainer.getBeanDefs();
        System.out.println(beanDefs);
    }

    /**
     * Eager creation of beans when DI container loads. This scenario is for testing only.
     * In reality, beans will be created on demand, i.e. when asked first time
     * The same instance in bean store is used unless specified as a separate bean entity.
     */
    @Test
    public void testEagerCreation() {
        // create bean defs from xml
        DiContainer.createBeanDefs();
        Map<String, Bean> beanDefs = DiContainer.getBeanDefs();

        // create all beans in defs
        for (String ref : beanDefs.keySet()) DiContainer.getBean(ref);
        Map<String, Object> beanStore = DiContainer.getBeanstore();
        System.out.println(beanStore);
    }

    /**
     * Typical usage scenario
     */
    @Test
    public void testLazyCreation() {
        resetDiContainer();
        DiContainer.createBeanDefs();
        assertTrue("Bean store should not yet have any object", DiContainer.getBeanstore().isEmpty());

        // tesla is defined in rivet-config.xml, and its type has a injectable field, which will also be created
        ElectricCar tesla = (ElectricCar) DiContainer.getBean("tesla");
        Battery battery = tesla.getBattery();

        assertEquals("Battery name should be Amaron", "Amaron", battery.getName());
        assertEquals("Battery charge left should be 42.0%", 42.0, battery.getChargeLeft(), 0.01);
        assertEquals("Car name should be Tesla", "Tesla", tesla.getName());
        assertTrue("Bean store should now have exactly 2 objects",
                DiContainer.getBeanstore().size() == 2);
    }
}
