package org.mayukh.rivet.core;

import java.time.LocalDate;

/**
 * Created by mayukh42 on 6/19/2017.
 */
public class ConsParam {

    private String name;
    private String type;
    private String value;
    private String ref;
    private boolean isRef;

    public ConsParam(String name, String type, String value, String ref) {
        if (name == null || type == null) throw new IllegalArgumentException("name and type cannot be null");
        if (value != null && ref != null) throw new IllegalArgumentException("only one of value or ref can exist");

        if (!Primitives.contains(type) && ref == null)
            throw new IllegalArgumentException("only primitive types can be directly created with value. " +
                    "use reference for other types");

        this.name = name;
        this.type = type;
        this.value = value;
        this.ref = ref;
        this.isRef = ref != null;
    }

    @Override
    public String toString() {
        String prefix = "<cons-param name=\"" + name + "\" class=\"" + type;
        String suffix = isRef ? "\" ref=\"" + ref + "\" />" : "\" value=\"" + value + "\" />";
        return prefix + suffix;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getRef() {
        return ref;
    }

    public String getName() {
        return name;
    }

    public Object create() {
        Object param = null;
        try {
            switch (type) {
                case "java.lang.Integer":
                    if (value != null) param = Integer.valueOf(value);
                    else param = DiContainer.getBean(ref);
                    break;
                case "java.lang.Long":
                    if (value != null) param = Long.valueOf(value);
                    else param = DiContainer.getBean(ref);
                    break;
                case "java.lang.Double":
                    if (value != null) param = Double.valueOf(value);
                    else param = DiContainer.getBean(ref);
                    break;
                case "java.time.LocalDate":
                    if (value != null) param = LocalDate.parse(value);
                    else param = DiContainer.getBean(ref);
                    break;
                case "java.lang.String":
                    if (value != null) param = String.valueOf(value);
                    else param = DiContainer.getBean(ref);
                    break;
                default:
                    param = DiContainer.getBean(ref);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return param;
    }
}
