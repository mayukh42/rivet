package org.mayukh.rivet.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mayukh42 on 6/19/2017.
 */
public class Primitives {

    private static final Set<String> primitives = new HashSet<>(Arrays.asList(
            "java.lang.String",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Double",
            "java.time.LocalDate"
    ));

    public static boolean contains(String type) {
        return primitives.contains(type);
    }
}
