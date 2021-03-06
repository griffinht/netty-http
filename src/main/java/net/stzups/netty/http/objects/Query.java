package net.stzups.netty.http.objects;

import net.stzups.netty.http.exception.exceptions.BadRequestException;
import net.stzups.util.DebugString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Query extends Uri {

    // /index?key=value&otherKey=value
    private static final String QUERY_DELIMITER = "?";
    private static final String QUERY_SEPARATOR = "&";
    private static final String QUERY_PAIR_SEPARATOR = "=";

    public static final String QUERY_REGEX = QUERY_DELIMITER + QUERY_SEPARATOR + QUERY_PAIR_SEPARATOR;

    private final String path;
    private final String rawQuery;
    private final Map<String, String> queries;

    public Query(String uri) throws BadRequestException {
        super(uri);
        String[] splitQuery = splitQuery(uri);
        path = splitQuery[0];
        rawQuery = splitQuery[1];
        queries = parseQuery(rawQuery);
    }

    public String get(String key) throws BadRequestException {
        String query = queries.get(key);
        if (query == null)
            throw new BadRequestException("Missing query for key " + key);

        return query;
    }

    public String path() {
        return path;
    }

    public String rawQuery() {
        return rawQuery;
    }

    /**
     * Returns String array with length of 2, with the first element as the path and the second element as the raw query
     * Example:
     * /index.html?key=value&otherKey=otherValue -> [ /index.html, key=value&otherKey=otherValue ]
     */
    private String[] splitQuery(String uri) throws BadRequestException {
        int index = uri.lastIndexOf(QUERY_DELIMITER);
        if (index <= 0) { // check for a query
            if (uri.contains(QUERY_SEPARATOR) || uri.contains(QUERY_PAIR_SEPARATOR)) {
                throw new BadRequestException("Empty query contains illegal characters");
            } else {
                return new String[] {uri, ""};
            }
        } else if (uri.indexOf(QUERY_DELIMITER) != index) {
            throw new BadRequestException("Encountered multiple " + QUERY_DELIMITER + " in uri, there should only be one");
        } else {
            return new String[] {uri.substring(0, index), uri.substring(index + 1)};
        }
    }

    /**
     * Parses key=value&otherKey=otherValue&keyWithEmptyValue to a Map of key-value pairs
     */
    public static Map<String, String> parseQuery(String query) throws BadRequestException {
        if (query.isEmpty())
            return Collections.emptyMap(); // no query to parse

        Map<String, String> queries = new HashMap<>();
        String[] keyValuePairs = query.split(QUERY_SEPARATOR);
        for (String keyValuePair : keyValuePairs) {
            String[] split = keyValuePair.split(QUERY_PAIR_SEPARATOR, 3); // a limit of 2 (expected) would not detect malformed queries such as ?key==, so we need to go one more
            if (split.length == 1) { // key with no value, such as ?key
                queries.put(split[0], "");
            } else if (split.length != 2) {
                throw new BadRequestException("Each key should have one value of query " + query);
            } else {
                queries.put(split[0], split[1]);
            }
        }

        return queries;
    }

    @Override
    public String toString() {
        return DebugString.get(Query.class)
                .add("path", path)
                .add("rawQuery", rawQuery)
                .add("queries", queries)
                .toString();
    }
}
