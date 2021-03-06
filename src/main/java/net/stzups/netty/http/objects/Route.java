package net.stzups.netty.http.objects;

import net.stzups.netty.http.exception.exceptions.BadRequestException;
import net.stzups.util.DebugString;

public class Route extends Query {


    private final String[] route;

    public Route(String uri) throws BadRequestException {
        super(uri);
        if (!path().startsWith("/"))
            throw new BadRequestException("Route must start with a /");

        String[] route = path().substring(1).split("/");
        if (route.length == 0) {
            this.route = new String[] {""};
        } else {
            this.route = route;
        }
    }

    /** get route at index */
    public String get(int index) throws BadRequestException {
        if (!length(index)) {
            throw new BadRequestException("Route not long enough");
        }

        return route[index];
    }

    /** get length */
    public int length() {
        return route.length;
    }

    /** true if length is long enough */
    public boolean length(int index) {
        return route.length >= index;
    }

    @Override
    public String toString() {
        return DebugString.get(Route.class, super.toString())
                .add("route", route)
                .toString();
    }
}
