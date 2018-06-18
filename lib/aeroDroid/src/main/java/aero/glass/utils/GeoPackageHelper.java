package aero.glass.utils;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import org.glob3.mobile.generated.Angle;
import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.EllipsoidalPlanet;
import org.glob3.mobile.generated.GLPrimitive;
import org.glob3.mobile.generated.Geodetic2D;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.IFactory;
import org.glob3.mobile.generated.IFloatBuffer;
import org.glob3.mobile.generated.IShortBuffer;
import org.glob3.mobile.generated.IndexedMesh;
import org.glob3.mobile.generated.Mesh;
import org.glob3.mobile.generated.Planet;
import org.glob3.mobile.generated.Vector3D;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import aero.glass.primary.SensorComponent;
import aero.glass.unit.Location;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.factory.GeoPackageFactory;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.MultiLineString;
import mil.nga.wkb.geom.MultiPoint;
import mil.nga.wkb.geom.MultiPolygon;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;
import poly2Tri.Triangle;
import poly2Tri.Triangulation;

import static aero.glass.primary.AeroActivity.DEMO_MODE;
import static aero.glass.unit.AltitudeReference.WGS84;
import static aero.glass.unit.LengthUnit.Meter;

/**
 * Created by zolta on 2018. 01. 08..
 */

public class GeoPackageHelper {
    private static final double ALTITUDE = 100.0;
    private static final String DB_NAME = "hacknhunt-with-RTE";
    private static final String DB_FILE_NAME = DB_NAME + ".gpkg";
    private static final boolean INJECT_CUSTOM_JSON_TO_GPKG = true;
    private static List<Location> demoPos = new ArrayList<Location>();

    private Vector3D center = null;
    private ArrayList<Float> verticeArray;
    private ArrayList<Short> indexArray;
    private ArrayList<Float> colorArray;
    private Map<String, List<Geodetic3D>> trails;
    private List<Mesh> meshes;
    private Map<String, Pair<Geodetic3D,List<Bitmap>>> coordImageMap;
    private List<Bitmap> images;

    private String name;
    private final File dbFile;
    private Activity activity;

    public GeoPackageHelper(Activity a) {
        activity = a;
        GeoPackageManager manager = GeoPackageFactory.getManager(activity);
        manager.deleteAll();
        File dir = new File(Environment.getExternalStorageDirectory(), "aeroglass/geopackage");
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }

        dbFile = new File(dir, DB_FILE_NAME);

        if (!dbFile.exists()) {
            FileIOHelper.copyFromAssetToSdcard(activity.getAssets(),
                    "aeroglass", "geopackage/" + DB_FILE_NAME);

            if (INJECT_CUSTOM_JSON_TO_GPKG) {
                manager.importGeoPackage(dbFile, true);
                injectCustomRoute();
                manager.exportGeoPackage(DB_NAME, dir);
                injectCustomImages(dbFile);
            }
        }

        manager.importGeoPackage(dbFile, true);
    }

    private List<Point> readPointsFromJSON(AssetManager assetManager, String file) {
        List<Point> points = new ArrayList<Point>();
        try {
            final JSONArray json = new JSONArray(FileIOHelper.loadString(assetManager, file));
            for (int i = 0; i < json.length(); i++) {
                JSONObject coord = json.getJSONObject(i);
                points.add(new Point(coord.getDouble("lng"), coord.getDouble("lat")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return points;
    }

    private List<String> readColumnNamesFromJSON(AssetManager assetManager, String file) {
        List<String> names = new ArrayList<String>();
        try {
            final JSONArray json = new JSONArray(FileIOHelper.loadString(assetManager, file));
            if (json.length() > 0) {
                Iterator keys = json.getJSONObject(0).keys();

                while(keys.hasNext()) {
                    String key = (String) keys.next();
                    if (!"lat".equals(key) && !"lng".equals(key)) {
                        names.add(key);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return names;
    }

    private Set<Map.Entry<String, Object>> readColumnValuesFromJSON(AssetManager assetManager, String file, int id) {
        Map<String, Object> values = new HashMap<String, Object>();
        try {
            final JSONArray json = new JSONArray(FileIOHelper.loadString(assetManager, file));
            if (json.length() > 0) {
                Iterator keys = json.getJSONObject(id).keys();

                while(keys.hasNext()) {
                    String key = (String) keys.next();
                    if (!"lat".equals(key) && !"lng".equals(key)) {
                        values.put(key, json.getJSONObject(id).get(key));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return values.entrySet();
    }

    private Map<Integer,List<Bitmap>> readImagesFromJSON(AssetManager assetManager, String file) {
        Map<Integer,List<Bitmap>> images = new HashMap<Integer,List<Bitmap>>();
        try {
            final JSONArray json = new JSONArray(FileIOHelper.loadString(assetManager, file));
            for (int i = 0; i < json.length(); i++) {
                JSONObject raw = json.getJSONObject(i);
                JSONArray fileNames = raw.getJSONArray("images");
                List<Bitmap> bitmaps = new ArrayList<Bitmap>(fileNames.length());
                for (int j = 0; j < fileNames.length(); j++) {
                    try {
                        bitmaps.add(BitmapFactory.decodeStream(FileIOHelper.openFile(assetManager, "custom_data/" + fileNames.getString(j))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                images.put(raw.getInt("id"), bitmaps);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }
        return images;
    }

    public List<String> getRoutes() {
        List<String> suffixes = new ArrayList<String>();
        GeoPackageManager manager = GeoPackageFactory.getManager(activity);
        GeoPackage geoPackage = manager.open(DB_NAME);
        for (String featureTable : geoPackage.getFeatureTables()) {
            if (featureTable.startsWith("route_")) {
                suffixes.add(featureTable.substring(6));
            }
        }
        return suffixes;
    }

    public List<Map<String,String>> getSOSConnectionInfos(String suffix) {
        List<Map<String,String>> connectionInfos = new ArrayList<Map<String,String>>();
        GeoPackageManager manager = GeoPackageFactory.getManager(activity);
        GeoPackage geoPackage = manager.open(DB_NAME);
        for (String featureTable : geoPackage.getFeatureTables()) {
            if (featureTable.equals("cnp_" + suffix)) {
                FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
                FeatureCursor featureCursor = featureDao.queryForAll();
                try {
                    while (featureCursor.moveToNext()) {
                        Map<String,String> map = new HashMap<String, String>();
                        map.put("ssid", (String) featureCursor.getValue(3, GeoPackageDataType.TEXT));
                        map.put("wifi_pwd", (String) featureCursor.getValue(4, GeoPackageDataType.TEXT));
                        map.put("sos_uri", (String) featureCursor.getValue(5, GeoPackageDataType.TEXT));
                        map.put("sos_user", (String) featureCursor.getValue(6, GeoPackageDataType.TEXT));
                        map.put("sos_pwd", (String) featureCursor.getValue(7, GeoPackageDataType.TEXT));
                        connectionInfos.add(map);
                    }
                } finally {
                    featureCursor.close();
                }
            }
        }
        return connectionInfos;
    }

    public void injectCustomRoute() {
        final GeoPackageManager manager = GeoPackageFactory.getManager(activity);
        final GeoPackage geoPackage = manager.open(DB_NAME);
        final AssetManager assetManager = activity.getAssets();

        List<String> columnNames = readColumnNamesFromJSON(assetManager, "custom_data/custom_route.json");
        createFeatureTable("route_custom", GeometryType.LINESTRING, geoPackage, columnNames);
        FeatureDao route_dao = geoPackage.getFeatureDao("route_custom");
        FeatureRow route_row = route_dao.newRow();
        LineString lineString = new LineString(false, false);
        for (Point point : readPointsFromJSON(assetManager, "custom_data/custom_route.json")) {
            lineString.addPoint(point);
        }
        route_row.setGeometry(new GeoPackageGeometryData(4326));
        route_row.getGeometry().setGeometry(lineString);
        for (Map.Entry<String, Object> value : readColumnValuesFromJSON(assetManager, "custom_data/custom_route.json", 0)) {
            route_row.setValue(value.getKey(), value.getValue());
        }
        route_dao.insert(route_row);

        columnNames = readColumnNamesFromJSON(assetManager, "custom_data/custom_cnp.json");
        createFeatureTable("cnp_custom", GeometryType.POINT, geoPackage, columnNames);
        FeatureDao cnp_dao = geoPackage.getFeatureDao("cnp_custom");
        int id = 0;
        for (Point point : readPointsFromJSON(assetManager, "custom_data/custom_cnp.json")) {
            FeatureRow cnp_row = cnp_dao.newRow();
            cnp_row.setGeometry(new GeoPackageGeometryData(4326));
            cnp_row.getGeometry().setGeometry(point);
            for (Map.Entry<String, Object> value : readColumnValuesFromJSON(assetManager, "custom_data/custom_cnp.json", id++)) {
                cnp_row.setValue(value.getKey(), value.getValue());
            }
            cnp_dao.insert(cnp_row);
        }

        columnNames = readColumnNamesFromJSON(assetManager, "custom_data/custom_poi.json");
        createFeatureTable("poi_custom", GeometryType.POINT, geoPackage, columnNames);
        FeatureDao poi_dao = geoPackage.getFeatureDao("poi_custom");
        id = 0;
        for (Point point : readPointsFromJSON(assetManager, "custom_data/custom_poi.json")) {
            FeatureRow poi_row = poi_dao.newRow();
            poi_row.setGeometry(new GeoPackageGeometryData(4326));
            poi_row.getGeometry().setGeometry(point);
            for (Map.Entry<String, Object> value : readColumnValuesFromJSON(assetManager, "custom_data/custom_poi.json", id++)) {
                poi_row.setValue(value.getKey(), value.getValue());
            }
            poi_dao.insert(poi_row);
        }

        columnNames = readColumnNamesFromJSON(assetManager, "custom_data/custom_aoi.json");
        createFeatureTable("aoi_custom", GeometryType.POLYGON, geoPackage, columnNames);
        FeatureDao aoi_dao = geoPackage.getFeatureDao("aoi_custom");
        FeatureRow aoi_row = aoi_dao.newRow();
        lineString = new LineString(false, false);
        for (Point point : readPointsFromJSON(assetManager, "custom_data/custom_aoi.json")) {
            lineString.addPoint(point);
        }
        if (lineString.numPoints() > 3) {
            aoi_row.setGeometry(new GeoPackageGeometryData(4326));
            Polygon polygon = new Polygon(false, false);
            polygon.addRing(lineString);
            aoi_row.getGeometry().setGeometry(polygon);
            for (Map.Entry<String, Object> value : readColumnValuesFromJSON(assetManager, "custom_data/custom_aoi.json", 0)) {
                aoi_row.setValue(value.getKey(), value.getValue());
            }
            aoi_dao.insert(aoi_row);
        }

        geoPackage.close();
    }

    public void injectCustomImages(File file) {
        RTEHelper rteHelper = new RTEHelper(file);
        rteHelper.createRTEMappingTables();

        for(Map.Entry<Integer, List<Bitmap>> imageMap :
                readImagesFromJSON(activity.getAssets(), "custom_data/custom_cnp_image_map.json").entrySet()) {
            for (Bitmap image : imageMap.getValue()) {
                rteHelper.addImage("cnp_custom", imageMap.getKey(), image);
            }
        }

        for(Map.Entry<Integer, List<Bitmap>> imageMap :
                readImagesFromJSON(activity.getAssets(), "custom_data/custom_aoi_image_map.json").entrySet()) {
            for (Bitmap image : imageMap.getValue()) {
                rteHelper.addImage("aoi_custom", imageMap.getKey(), image);
            }
        }
    }

    private void createFeatureTable(String name, GeometryType type, GeoPackage geoPackage, List <String> columnNames) {
        if (geoPackage.isFeatureTable(name)) {
            geoPackage.deleteTable(name);
        }
        List<FeatureColumn> columns = new ArrayList<FeatureColumn>();
        columns.add(FeatureColumn.createPrimaryKeyColumn(0, "fid"));
        columns.add(FeatureColumn.createGeometryColumn(1, "geom", type, false, null));
        columns.add(FeatureColumn.createColumn(2, "id", GeoPackageDataType.INT, false, null));
        int id = 3;
        for (String columnName : columnNames) {
            columns.add(FeatureColumn.createColumn(id++, columnName, GeoPackageDataType.TEXT, true, ""));
        }
        geoPackage.createFeatureTable(new FeatureTable(name, columns));
        geoPackage.execSQL("INSERT INTO gpkg_contents (table_name, data_type, identifier, srs_id)" +
                " VALUES ('" + name + "', 'features', '" + name + "', '4326');");
        geoPackage.execSQL("INSERT INTO gpkg_geometry_columns (table_name, column_name, geometry_type_name, srs_id, z, m)" +
                " VALUES ('" + name + "', 'geom', '" + type.getName() + "', '4326', 0, 0);");
    }

    private void read(String featureName, String suffix, boolean readRelatedImages) {
        if (dbFile == null) {
            return;
        }

        verticeArray = new ArrayList<Float>();
        indexArray = new ArrayList<Short>();
        colorArray = new ArrayList<Float>();
        coordImageMap = new HashMap<String, Pair<Geodetic3D,List<Bitmap>>>();
        trails = new HashMap<String, List<Geodetic3D>>();
        meshes = new ArrayList<Mesh>();

        RTEHelper rteHelper = new RTEHelper(dbFile);

        final Planet planet = EllipsoidalPlanet.createEarth();
        GeoPackageManager manager = GeoPackageFactory.getManager(activity);
        GeoPackage geoPackage = manager.open(DB_NAME);

        final String featureTable = featureName + "_" + suffix;
        if (!geoPackage.isFeatureTable(featureTable)) {
            return;
        }

        FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
        FeatureCursor featureCursor = featureDao.queryForAll();
        try {
            int count = 0;
            while (featureCursor.moveToNext()) {
                Long id = (Long) featureCursor.getRow().getValue("fid");
                if (readRelatedImages) {
                    images = rteHelper.getImage(featureTable, id);
                }

                GeoPackageGeometryData geometryData = featureCursor.getGeometry();
                if (geometryData != null) {
                    Geometry geometry = geometryData.getGeometry();

                    try {
                        name = (String) featureCursor.getRow().getValue("name");
                    } catch (GeoPackageException e) {
                        name = featureName + " #" + count++;
                    }

                    Color color = Color.cyan();

                    switch (geometry.getGeometryType()) {
                        case MULTIPOLYGON:
                            parseMultiPolygon((MultiPolygon) geometry, planet, color);
                            break;
                        case POLYGON:
                            parsePolygon((Polygon) geometry, planet, color);
                            break;
                        case MULTILINESTRING:
                            name = name.replace("MULTILINESTRING","routes");
                            parseMultiLineString((MultiLineString) geometry);
                            break;
                        case LINESTRING:
                            name = name.replace("LINESTRING","route");
                            parseLineString((LineString) geometry);
                            break;
                        case MULTIPOINT:
                            parseMultiPoint((MultiPoint) geometry);
                            break;
                        case POINT:
                            parsePoint((Point) geometry);
                            break;
                        default:
                            Log.d("geom", "unhandled geometry: "
                                    + geometry.getGeometryType().getName());
                            break;
                    }
                }
            }
        } finally {
            featureCursor.close();
        }
        finaliseMesh();
    }

    public List<Mesh> getMeshes(String featureName, String route) {
        read(featureName, route, false);
        return meshes;
    }

    public Map<String, Pair<Geodetic3D,List<Bitmap>>> getCoordImageMap(String featureName, String route) {
        read(featureName, route, true);
        return coordImageMap;
    }

    public List<Geodetic3D> getRoutes(String featureName, String route) {
        if (DEMO_MODE) {
            demoPos = new ArrayList<Location>();
        }
        read(featureName, route, false);
        for (List<Geodetic3D> trail : trails.values()) {
            for (Geodetic3D location: trail) {
                if (DEMO_MODE) {
                    demoPos.add(new Location(location._latitude._degrees, location._longitude._degrees, 0.0, WGS84, Meter));
                }
            }
            SensorComponent.setDemoPos(demoPos);
            return trail;
        }
        return new ArrayList<Geodetic3D>();
    }

    private void parseMultiPoint(MultiPoint multiPoint) {
        for (Point point : multiPoint.getPoints()) {
            parsePoint(point);
        }
    }

    private void parseMultiLineString(MultiLineString multiLineString) {
        for (LineString lineString : multiLineString.getLineStrings()) {
            parseLineString(lineString);
        }
    }

    private void parseLineString(LineString lineString) {
        List<Geodetic3D> trail = new ArrayList<Geodetic3D>();
        for (Point point : lineString.getPoints()) {
            trail.add(new Geodetic3D(Angle.fromDegrees(point.getY()), Angle.fromDegrees(point.getX()), ALTITUDE));
        }
        trails.put(name, trail);
    }

    private void parseMultiPolygon(MultiPolygon multiPolygon, Planet planet, Color color) {
        for (Polygon polygon : multiPolygon.getPolygons()) {
            parsePolygon(polygon, planet, color);
        }
    }

    private void parsePoint(Point point) {
        coordImageMap.put(name, new Pair<Geodetic3D, List<Bitmap>>(
                new Geodetic3D(Angle.fromDegrees(point.getY()),
                Angle.fromDegrees(point.getX()), ALTITUDE + 10), images));
    }

    private void parsePolygon(Polygon polygon, Planet planet, Color color) {
        int numContures = polygon.numRings();
        int totalVertices = 0;
        int[] numVerticesInContures = new int[numContures];
        for (LineString ring : polygon.getRings()) {
            totalVertices += ring.numPoints();
        }
        double[][] vertices = new double[totalVertices][2];

        int contureIndex = 0;
        int verticeIndex = 0;
        for (LineString ring : polygon.getRings()) {
            numVerticesInContures[contureIndex] = ring.numPoints();
            boolean first = true;
            for (Point point : ring.getPoints()) {
                if (first) {
                    Log.d("gpkg", point.getM() + " " + point.getY() + " " + point.getX()
                            + " " + point.getZ());
                    vertices[verticeIndex][0] = point.getY();
                    vertices[verticeIndex][1] = point.getX();
                    verticeIndex++;
                    first = false;
                } else {
                    if (vertices[verticeIndex-1][0] != point.getY()
                            || vertices[verticeIndex-1][1] != point.getX()) {
                        vertices[verticeIndex][0] = point.getY();
                        vertices[verticeIndex][1] = point.getX();
                        verticeIndex++;
                    } else {
                        totalVertices--;
                        numVerticesInContures[contureIndex]--;
                    }
                }
            }
            if (numVerticesInContures[contureIndex] > 1
                    && vertices[verticeIndex-1][0] == vertices[0][0]
                    && vertices[verticeIndex-1][1] == vertices[0][1]) {
                totalVertices--;
                numVerticesInContures[contureIndex]--;
            }
        }

        try {
            List<Triangle> triangles = Triangulation.triangulate(
                    numContures, numVerticesInContures, vertices);
            createMesh(vertices, totalVertices, triangles, planet, color);
        } catch (final Exception e0) {
            // Triangulation is order sensitive so we try revert vertices order
            try {
                double[][] revertVertices = new double[totalVertices][2];
                int offset = 0;
                for (int i = 0; i < numContures; i++) {
                    for (int j = 0; j < numVerticesInContures[i]; j++) {
                        final int newIndex = offset + numVerticesInContures[i] - (j + 1);
                        revertVertices[newIndex][0] = vertices[offset + j][0];
                        revertVertices[newIndex][1] = vertices[offset + j][1];
                    }
                    offset += numVerticesInContures[i];
                }
                List<Triangle> triangles = Triangulation.triangulate(numContures,
                        numVerticesInContures, revertVertices);
                createMesh(revertVertices, totalVertices, triangles, planet, color);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createMesh(double[][] vertices, int size, List<Triangle> triangles,
                            Planet planet, Color color) {
        int offset = verticeArray.size() / 3;
        for (int i = 0; i < size; ++i) {
            final Geodetic2D point = new Geodetic2D(Angle.fromDegrees(vertices[i][0]),
                    Angle.fromDegrees(vertices[i][1]));
            if (center == null) {
                center = planet.toCartesian(point);
            }

            Vector3D coord = planet.toCartesian(new Geodetic3D(point, ALTITUDE)).sub(center);
            verticeArray.add((float) coord._x);
            verticeArray.add((float) coord._y);
            verticeArray.add((float) coord._z);
            colorArray.add(color._red);
            colorArray.add(color._green);
            colorArray.add(color._blue);
            colorArray.add(color._alpha);
        }

        for (Triangle triangle : triangles) {
            indexArray.add((short) (triangle._vertex0 + offset));
            indexArray.add((short) (triangle._vertex1 + offset));
            indexArray.add((short) (triangle._vertex2 + offset));
        }

        if (verticeArray.size() > 10000) {
            finaliseMesh();
        }
    }

    private void finaliseMesh() {
        if (verticeArray.isEmpty()) {
            return;
        }

        float[] verticeArray2 = new float[verticeArray.size()];
        for (int i = 0; i < verticeArray.size(); i++) {
            verticeArray2[i] = verticeArray.get(i);
        }
        IFloatBuffer verticeBuffer = IFactory.instance().createFloatBuffer(verticeArray.size());
        verticeBuffer.put(verticeArray2);
        verticeBuffer.rewind();
        verticeArray.clear();

        short[] indexArray2 = new short[indexArray.size()];
        for (int i = 0; i < indexArray.size(); i++) {
            indexArray2[i] = indexArray.get(i);
        }
        IShortBuffer indexBuffer = IFactory.instance().createShortBuffer(indexArray.size());
        indexBuffer.put(indexArray2);
        indexBuffer.rewind();
        indexArray.clear();

        float[] colorArray2 = new float[colorArray.size()];
        for (int i = 0; i < colorArray.size(); i++) {
            colorArray2[i] = colorArray.get(i);
        }
        IFloatBuffer colorBuffer = IFactory.instance().createFloatBuffer(colorArray.size());
        colorBuffer.put(colorArray2);
        colorBuffer.rewind();
        colorArray.clear();

        Random rnd = new Random();
        Color color = Color.fromRGBA(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat(), 1.0f);
        meshes.add(new IndexedMesh(GLPrimitive.triangles(), center, verticeBuffer,
                true, indexBuffer, true, 1.0f, 1.0f, color, colorBuffer, 1, false, null));
    }
}
