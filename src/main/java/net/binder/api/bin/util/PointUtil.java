package net.binder.api.bin.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class PointUtil {

    private static final int SRID = 4326; // WGS84 좌표계
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

    public static Point getPoint(Double longitude, Double latitude){

        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}
