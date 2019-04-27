package systems.opalia.service.neo4j.embedded.impl;

import java.lang.reflect.Array;
import java.util.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import systems.opalia.service.neo4j.embedded.api.EmbeddedDatabaseQueryRow;


public final class EmbeddedDatabaseQueryRowImpl
        implements EmbeddedDatabaseQueryRow {

    private final Map<String, Object> row;

    EmbeddedDatabaseQueryRowImpl(List<String> columns, Map<String, Object> row) {

        this.row = normalize(columns, row);
    }

    public Map<String, Object> getData() {

        return copy(row);
    }

    private Map<String, Object> normalize(List<String> columns, Map<String, Object> row) {

        Map<String, Object> result = new LinkedHashMap<>();

        for (String key : columns)
            result.put(key, normalize(row.get(key)));

        return result;
    }

    private Object normalize(Object value) {

        if (value == null)
            return null;

        if (value instanceof Boolean)
            return value;

        if (value instanceof Byte)
            return value;

        if (value instanceof Short)
            return value;

        if (value instanceof Integer)
            return value;

        if (value instanceof Long)
            return value;

        if (value instanceof Float)
            return value;

        if (value instanceof Double)
            return value;

        if (value instanceof Character)
            return value;

        if (value instanceof String)
            return value;

        if (value instanceof Node) {

            Map<String, Object> result = new LinkedHashMap<>();
            Node node = ((Node) value);

            for (String key : node.getPropertyKeys())
                result.put(key, normalize(node.getProperty(key)));

            return result;
        }

        if (value instanceof Relationship) {

            Map<String, Object> result = new LinkedHashMap<>();
            Relationship relationship = ((Relationship) value);

            for (String key : relationship.getPropertyKeys())
                result.put(key, normalize(relationship.getProperty(key)));

            return result;
        }

        if (value instanceof Path) {

            List<Object> result = new ArrayList<>();
            Path path = ((Path) value);

            for (PropertyContainer container : path)
                result.add(normalize(container));

            return result;
        }

        if (value instanceof Collection<?>) {

            List<Object> result = new ArrayList<>();
            Collection<?> collection = ((Collection<?>) value);

            for (Object item : collection)
                result.add(normalize(item));

            return result;
        }

        if (value.getClass().isArray()) {

            List<Object> result = new ArrayList<>();
            int length = Array.getLength(value);

            for (int i = 0; i < length; i++)
                result.add(normalize(Array.get(value, i)));

            return result;
        }

        throw new IllegalArgumentException("Cannot normalize value "
                + value + " (" + value.getClass().getName() + ").");
    }

    private Map<String, Object> copy(Map<String, Object> data) {

        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : data.entrySet())
            result.put(entry.getKey(), copy(entry.getValue()));

        return result;
    }

    private Object copy(Object value) {

        if (value == null)
            return null;

        if (value instanceof Map<?, ?>) {

            Map<Object, Object> result = new LinkedHashMap<>();
            Map<?, ?> map = ((Map<?, ?>) value);

            for (Map.Entry entry : map.entrySet())
                result.put(entry.getKey(), copy(entry.getValue()));

            return result;
        }

        if (value instanceof Collection<?>) {

            List<Object> list = new ArrayList<>();
            Collection<?> collection = ((Collection<?>) value);

            for (Object item : collection)
                list.add(copy(item));

            return list;
        }

        return value;
    }
}
