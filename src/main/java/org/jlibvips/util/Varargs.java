package org.jlibvips.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Varargs {

    /**
     * Returns an empty varags list as Object array.
     *
     * @return Object array
     */
    public static Object[] empty() {
        return new Varargs().toArray();
    }

    private final Map<String, Object> arguments;

    public Varargs() {
        this.arguments = new HashMap<>();
    }

    public Varargs add(String name, Object value) {
        if(name != null && value != null)
            arguments.put(name, value);
        return this;
    }

    public Object[] toArray() {
        return arguments.keySet().stream()
                .flatMap(key -> Stream.of(key, arguments.get(key)))
                .toArray();
    }
}
