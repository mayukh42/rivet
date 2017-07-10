package org.mayukh.rivet.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mayukh42 on 11/6/17.
 *
 * Simple POJO to hold information about classes to construct
 */
public class Bean {

    private final String name;
    private String type;

    /* <field name, class type> */
    private Map<String, SetterParam> setterParams;
    private List<ConsParam> consParams;

    Bean(String name, String type, Map<String, SetterParam> setterParams, List<ConsParam> consParams) {
        this.name = name;
        this.type = type;
        this.setterParams = setterParams;
        this.consParams = consParams;

        addInjectablesToSetterParmas();
    }

    /**
     * pick injectable fields and add to setter params
     */
    private void addInjectablesToSetterParmas() {
        try {
            Class beanType = Class.forName(type);
            Field[] fields = beanType.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Riveted.class)) {
                    if (this.setterParams == null) this.setterParams = new HashMap<>();
                    this.setterParams.put(field.getName(), new SetterParam(
                            // should be of reference type only
                            field.getName(), field.getType().getName(), null, field.getName()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * create a simple POJO bean object using default cons, i.e. all fields set to default value.
     */
    private Object createPojo() {
        Object o = null;
        try {
            Class oClass = Class.forName(type);
            o = oClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    /**
     * encapsulates the various object creation methods
     */
    public Object create() {
        Object o = null;
        if (consParams != null) o = createWithCons();
        else o = createPojo();
        callSetters(o);
        DiContainer.addBean(this.name, o);
        return o;
    }

    /**
     * call setter methods on the bean object after creation.
     * If a bean has cons params and setter params both for same field in xml, the setter will override the value.
     */
    private void callSetters(Object o) {
        if (setterParams != null) {
            try {
                for (String field : setterParams.keySet()) {
                    SetterParam param = setterParams.get(field);
                    Class fieldType = Class.forName(param.getType());

                    /* Heavy use of convention */
                    String methodName = "set" + param.getName().substring(0, 1).toUpperCase() +
                            param.getName().substring(1);
                    Method method = o.getClass().getMethod(methodName, fieldType);

                    /* check if the field is already created in the beanstore */
                    Object beanStoreRef = DiContainer.getBean(param.getName());
                    if (beanStoreRef != null) method.invoke(o, beanStoreRef);
                    else method.invoke(o, param.create());
                }
            } catch (Exception e) { // lazy for now
                e.printStackTrace();
            }
        }
    }

    /**
     * create bean object with cons params
     */
    private Object createWithCons() {
        Object o = null;
        try {
            Class oClass = Class.forName(type);
            Class[] consParamTypes = new Class[consParams.size()];
            for (int i = 0; i < consParams.size(); i++)
                consParamTypes[i] = Class.forName(consParams.get(i).getType());

            Constructor cons = oClass.getDeclaredConstructor(consParamTypes);
            Object[] consObjects = consParams.stream().map(ConsParam::create).toArray();
            o = cons.newInstance(consObjects);
        } catch (Exception e) { // lazy for now
            e.printStackTrace();
        }

        return o;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "\n\tname=" + name +
                ", \n\ttype=" + type +
                ", \n\tsetterParams=" + setterParams +
                ", \n\tconsParams=" + consParams +
                "\n}";
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public SetterParam getSetterParam(String param) {
        return setterParams.get(param);
    }
}
