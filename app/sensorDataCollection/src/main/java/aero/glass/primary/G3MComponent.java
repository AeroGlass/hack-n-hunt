package aero.glass.primary;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.widget.Toast;

import org.glob3.mobile.generated.AltitudeMode;
import org.glob3.mobile.generated.Angle;
import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.CompositeRenderer;
import org.glob3.mobile.generated.Geodetic2D;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.LayerSet;
import org.glob3.mobile.generated.Mesh;
import org.glob3.mobile.generated.MeshRenderer;
import org.glob3.mobile.generated.OSMLayer;
import org.glob3.mobile.generated.Planet;
import org.glob3.mobile.generated.PlanetRenderer;
import org.glob3.mobile.generated.Quality;

import org.glob3.mobile.generated.SceneJSShapesParser;
import org.glob3.mobile.generated.Shape;
import org.glob3.mobile.generated.ShapesRenderer;

import org.glob3.mobile.generated.TimeInterval;
import org.glob3.mobile.generated.Trail;
import org.glob3.mobile.generated.TrailsRenderer;
import org.glob3.mobile.specific.G3MBuilder_Android;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aero.glass.renderer.linerender.PatternRenderer;
import aero.glass.renderer.mark.AeroMark;
import aero.glass.renderer.mark.AeroMarkInfo;
import aero.glass.renderer.mark.AeroMarkType;
import aero.glass.renderer.mark.AeroMarksRenderer;
import aero.glass.renderer.mark.AeroOverlapDetector;
import aero.glass.renderer.textrender.TextRenderer;
import aero.glass.unit.LengthUnit;
import aero.glass.unit.Location;
import aero.glass.utils.JSONHelper;
import aero.glass.utils.JSONModel;

import static aero.glass.primary.SDCActivity.PLANET;
import static aero.glass.unit.AltitudeReference.WGS84;
import static aero.glass.unit.LengthUnit.Meter;
import static org.glob3.mobile.generated.AltitudeMode.ABSOLUTE;

/**
 * Component that handles basic G3M tasks.
 * Created by DrakkLord on 2015. 10. 26..
 */
public class G3MComponent extends G3MBaseComponent {
    /** Set this value to true to make G3M put FPS info into the log periodically. */
    protected static final boolean LOG_FPS = true;
    protected static final String AOI_FEATURE_NAME = "aoi";
    protected static final String POI_FEATURE_NAME = "poi";
    protected static final String CNP_FEATURE_NAME = "cnp";
    protected static final String ROUTE_FEATURE_NAME = "route";
    private static final double LOC_LABEL_DIST = 1.0;
    private static final float LOC_FONT_SIZE = 0.45f*1.5f;
    private static final float ROUTE_SIZE_IN_METER = 10.0f;
    private static final double LOOK_AHEAD_TEST_DISTANCE = 20.0;

    // Renderers
    private ShapesRenderer cnpRenderer;
    private ShapesRenderer referenceRenderer;
    private ShapesRenderer poiRenderer;
    private TrailsRenderer routeRenderer;
    private MeshRenderer aoiRenderer;
    private AeroMarksRenderer labelRendererAero;
    private TextRenderer textRenderer;
    private PatternRenderer patternRenderer;
    private CompositeRenderer hudCompositeRenderer;

    private List<AeroMark> distanceLabels = new ArrayList<AeroMark>();
    private AeroMark referenceMark;
    private volatile int selectedCNPIndex = 0;
    private Map<String, Shape> shapeMarkMap = new HashMap<String, Shape>();
    private List<String> sortedSCNPs = new ArrayList<String>();
    private long lastLabelUpdate = System.currentTimeMillis();
    private volatile boolean bCreateVisualsDone = false;

    protected G3MComponent(SDCActivity a) {
        super(a);
    }

    public void onAsyncinit() {
        PlanetRenderer planetRenderer = g3mWidget.getG3MWidget().getPlanetRenderer();
        planetRenderer.setRenderTileMeshes(true);
        planetRenderer.setElevationDataProvider(null, false);
        planetRenderer.setEnable(PLANET);

        updateStartupProgressText("loading last route");
        String lastRoute = activity.activityStateComponent.getLastRoute();
        if (lastRoute != null) {
            createVisuals(activity.activityStateComponent.getLastRoute(),
                    activity.geoPackageHelper.getCoordImageMap(CNP_FEATURE_NAME, lastRoute));
            selectedCNPIndex = activity.activityStateComponent.getLastCNP();
            sortedSCNPs = sortCNPs(activity.geoPackageHelper.getCoordImageMap(G3MComponent.CNP_FEATURE_NAME, lastRoute), lastRoute);
        } else {
            bCreateVisualsDone = true;
        }

        bCreateVisualsDone = true;
        try {
            updateStartupProgressText("waiting for location");
            while (getAHLR() == null) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            updateStartupProgressText("async init done");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onPreRenderTask() {
        long now = System.currentTimeMillis();
        if (now >= lastLabelUpdate + 100L) {
            lastLabelUpdate = now;
            if (activity.activityStateComponent.isLookAhead()) {
                for (AeroMark aeroMark : distanceLabels) {
                    if (aeroMark.getName().equals(sortedSCNPs.get(selectedCNPIndex))) {
                        if (calcDistance(aeroMark) < LOOK_AHEAD_TEST_DISTANCE) {
                            if (selectedCNPIndex < sortedSCNPs.size() - 1) {
                                selectedCNPIndex++;
                                activity.activityStateComponent.setLastCNP(selectedCNPIndex);
                            } else {
                                activity.activityStateComponent.setLastCNP(0);
                            }
                        }
                    }
                }
            }

            for (AeroMark aeroMark : distanceLabels) {
                if (!activity.activityStateComponent.isLookAhead()
                        || (aeroMark.getName().equals(sortedSCNPs.get(selectedCNPIndex)))
                        || (((selectedCNPIndex + 1) < sortedSCNPs.size()) && aeroMark.getName().equals(sortedSCNPs.get(selectedCNPIndex + 1)))) {
                    aeroMark.setEnabled(true);
                    Shape shape = shapeMarkMap.get(aeroMark.getName());
                    if (shape != null) {
                        shape.setEnable(true);
                    }
                    aeroMark.setLabel(aeroMark.getName() + " : " + unitForm(calcDistance(aeroMark)));
                } else {
                    aeroMark.setEnabled(false);
                    Shape shape = shapeMarkMap.get(aeroMark.getName());
                    if (shape != null) {
                        shape.setEnable(false);
                    }
                }
                aeroMark.invalidateRenderableText();
            }

            if (referenceMark != null) {
                referenceMark.setLabel(referenceMark.getName() + " : " + unitForm(calcDistance(referenceMark)));
            }

            labelRendererAero.update(g3mWidget.getG3MWidget().getNextCamera(),
                    g3mWidget.getG3MContext().getPlanet());
        }
    }

    /**
     * <p> Initialization code that is supposed to happen in sync with the Activity's onCreate
     * method. </p> <p> This is a blocking part of the startup sequence. The 3GM widget is created
     * afterwards from the builder. </p> <p> In case the superclass method returns false, the sub
     * class function is expected to return false immediately as well! </p>
     *
     * @param builder G3M widget builder that will be used later to create the widget
     * @return Return false in case a fatal startup error occurs, so the app will exit!
     */
    protected boolean onSyncInit(G3MBuilder_Android builder) {
        builder.setBackgroundColor(Color.black());
        builder.setLogFPS(LOG_FPS);

        /**
         * This renderer inject a hook into the rendering loop by being the
         * first to be called BEFORE the renderers (method preRenderTask), and AFTER the renderers
         * (method postRenderTask)
         */
        builder.setPrePostRenderTasks(new PrePostRenderTasks());

        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int displayHeight = metrics.heightPixels;
        int displayWidth = metrics.widthPixels;
        progLabel = StartupScreen.createBusyRenderer(builder, displayWidth, displayHeight);
        /** This must be set so the context can be used by the factory later on */
        // Map Box terrain
        final LayerSet layerSet = new LayerSet();
        final OSMLayer osmLayer = new OSMLayer(TimeInterval.fromDays(30));
        osmLayer.setTitle("OSM Terrain");
        osmLayer.setEnable(true);
        layerSet.addLayer(osmLayer);
        builder.getPlanetRendererBuilder().setLayerSet(layerSet);
        builder.getPlanetRendererBuilder().setQuality(Quality.QUALITY_MEDIUM);
        // HAX : maybe this will remove the startup hanging
        builder.getPlanetRendererBuilder().setForceFirstLevelTilesRenderOnStart(false);
        // create essential renderers

        hudCompositeRenderer = new CompositeRenderer();

        textRenderer = new TextRenderer();
        patternRenderer = new PatternRenderer(activity.shouldUseStereo());
        labelRendererAero = new AeroMarksRenderer(
                textRenderer, patternRenderer, new AeroOverlapDetector(), activity.shouldUseStereo()
        );

        hudCompositeRenderer.addRenderer(labelRendererAero);
        hudCompositeRenderer.addRenderer(textRenderer);
        hudCompositeRenderer.addRenderer(patternRenderer);

        cnpRenderer = new ShapesRenderer();
        referenceRenderer = new ShapesRenderer();
        poiRenderer = new ShapesRenderer();
        routeRenderer = new TrailsRenderer();
        aoiRenderer = new MeshRenderer();

        builder.addRenderer(cnpRenderer);
        builder.addRenderer(referenceRenderer);
        builder.addRenderer(poiRenderer);
        builder.addRenderer(routeRenderer);
        builder.addRenderer(aoiRenderer);

        builder.setHUDRenderer(hudCompositeRenderer);

        return true;
    }

    private void createVisuals(String route, Map<String, Pair<Geodetic3D, List<Bitmap>>> coordImageMap) {
        if (route == null) {
            bCreateVisualsDone = true;
            return;
        }
        showToast("Loading...", Toast.LENGTH_SHORT);
        // TODO FIX IT!
        try {aoiRenderer.clearMeshes();} catch (NullPointerException e) {e.printStackTrace();}
        try {cnpRenderer.removeAllShapes();} catch (NullPointerException e) {e.printStackTrace();}
        try {poiRenderer.removeAllShapes();} catch (NullPointerException e) {e.printStackTrace();}
        try {routeRenderer.removeAllTrails();} catch (NullPointerException e) {e.printStackTrace();}
        labelRendererAero.removeType(AeroMarkType.GENERAL_MARK);
        shapeMarkMap.clear();

        for (Mesh mesh : activity.geoPackageHelper.getMeshes(AOI_FEATURE_NAME, route)) {
            aoiRenderer.addMesh(mesh);
        }

        JSONModel poiModel = JSONHelper.getModel(activity.getAssets(),
                "custom_data" + File.separator + "custom_objects.json", "poi_model");
        if (poiModel != null) {
            for (Map.Entry<String, Pair<Geodetic3D, List<Bitmap>>> entry :
                    activity.geoPackageHelper.getCoordImageMap(POI_FEATURE_NAME, route).entrySet()) {
                Shape shape = createShape(entry.getValue().first, poiModel);
                shape.setScale(0.5);
                poiRenderer.addShape(shape);
                final AeroMarkInfo mi = new AeroMarkInfo();
                mi.setLabel(entry.getKey());
                mi.minDistanceToCamera = LOC_LABEL_DIST;
                mi.labelFontSize = LOC_FONT_SIZE;
                mi.labelFontColor = Color.green();
                mi.type = AeroMarkType.GENERAL_MARK;
                Geodetic3D shapePos = entry.getValue().first;
                AeroMark mark = new AeroMark(
                        new Geodetic3D(shapePos._latitude, shapePos._longitude, 100.0), ABSOLUTE, mi);
                mark.setName(entry.getKey());
                labelRendererAero.addMark(mark);
            }
        }

        JSONModel cnpModel = JSONHelper.getModel(activity.getAssets(),
                "custom_data" + File.separator + "custom_objects.json", "cnp_model");
        if (cnpModel != null) {
            for (Map.Entry<String, Pair<Geodetic3D, List<Bitmap>>> entry : coordImageMap.entrySet()) {
                Shape shape = createShape(entry.getValue().first, cnpModel);
                cnpRenderer.addShape(shape);
                final AeroMarkInfo mi = new AeroMarkInfo();
                mi.setLabel(entry.getKey() + unitForm(Double.NaN));
                mi.minDistanceToCamera = LOC_LABEL_DIST;
                mi.labelFontSize = LOC_FONT_SIZE;
                mi.labelFontColor = Color.green();
                mi.type = AeroMarkType.GENERAL_MARK;
                Geodetic3D shapePos = entry.getValue().first;
                AeroMark mark = new AeroMark(
                        new Geodetic3D(shapePos._latitude, shapePos._longitude, 100.0), ABSOLUTE, mi);
                mark.setName(entry.getKey());
                shapeMarkMap.put(mark.getName(), shape);
                labelRendererAero.addMark(mark);
                distanceLabels.add(mark);
            }
        }

        List<Geodetic3D> coords = activity.geoPackageHelper.getRoutes(ROUTE_FEATURE_NAME, route);
        if (coords != null) {
            routeRenderer.addTrail(createTrail(coords, ROUTE_SIZE_IN_METER, Color.green()));
        }

        bCreateVisualsDone = true;
        showToast("Loading done!", Toast.LENGTH_SHORT);
    }

    public void routeSelected(final String route, final List<String> sCNPS,
                              final Map<String, Pair<Geodetic3D, List<Bitmap>>> coordImageMap) {
        bCreateVisualsDone = false;

        runOnRendererThread(new Runnable() {
            @Override
            public void run() {
                sortedSCNPs = sCNPS;
                createVisuals(route, coordImageMap);
            }
        });
    }

    public void cnpSelected(int index) {
        selectedCNPIndex = index;
    }

    public void setReference() {
        runOnRendererThread(new Runnable() {
            @Override
            public void run() {
                if (getAHLR() != null) {
                    JSONModel referenceModel = JSONHelper.getModel(activity.getAssets(),
                            "custom_data" + File.separator + "custom_objects.json", "reference_model");
                    Location location = getAHLR().getLocation();
                    Shape shape = createShape(new Geodetic3D(Angle.fromDegrees(location.getLatitude()),
                            Angle.fromDegrees(location.getLongitude()),
                            location.getAltitude(WGS84, Meter) + 5.0),referenceModel);

                    // TODO FIX IT!
                    try {referenceRenderer.removeAllShapes();} catch (NullPointerException e) {e.printStackTrace();}

                    referenceRenderer.addShape(shape);

                    if (referenceMark == null) {
                        final AeroMarkInfo mi = new AeroMarkInfo();
                        mi.setLabel("REFERENCE");
                        mi.minDistanceToCamera = LOC_LABEL_DIST;
                        mi.labelFontSize = LOC_FONT_SIZE;
                        mi.labelFontColor = Color.green();
                        mi.type = AeroMarkType.GENERAL_MARK;
                        referenceMark = new AeroMark(shape.getPosition(), ABSOLUTE, mi);
                        referenceMark.setName("REFERENCE");
                        labelRendererAero.addMark(referenceMark);
                    } else {
                        referenceMark.setPosition(shape.getPosition());
                    }
                }
            }
        });
    }

    private String unitForm(Double distance) {
        LengthUnit lengthUnit = activity.activityStateComponent.getLengthUnit();
        if (distance == Double.NaN) {
            return "N/A " + lengthUnit.symbol();
        } else {
            return Math.round(lengthUnit.calcFrom(distance, Meter)) + " " + lengthUnit.symbol();
        }
    }

    public Shape createShape(Geodetic3D location, JSONModel model) {
        Shape shape = SceneJSShapesParser.parseFromJSON(model.getJson(), model.getPrefix(),
                false, location, AltitudeMode.ABSOLUTE);
        shape.setScale(1.0);
        shape.setPitch(Angle.fromDegrees(model.getRotationPitch()));
        shape.setHeading(Angle.fromDegrees(model.getRotationHeading()));
        return shape;
    }

    public static Trail createTrail(List<Geodetic3D> route, float size, Color color) {
        Trail trail = new Trail(color, size, 0);
        for (Geodetic3D location : route) {
            trail.addPosition(location);
        }
        return trail;
    }

    private double calcDistance(AeroMark aeroMark) {
        return g3mWidget.getG3MContext().getPlanet().computePreciseLatLonDistance(
                new Geodetic2D(aeroMark.getPosition()._latitude,
                        aeroMark.getPosition()._longitude),
                new Geodetic2D(Angle.fromDegrees(getAHLR().getLocation().getLatitude()),
                        Angle.fromDegrees(getAHLR().getLocation().getLongitude())));

    }

    public List<String> sortCNPs(Map<String, Pair<Geodetic3D, List<Bitmap>>> coordImageMap, String route) {
        List<Geodetic3D> routeList = activity.geoPackageHelper.getRoutes(G3MComponent.ROUTE_FEATURE_NAME, route);
        List<String> cnpArray = new ArrayList<String>();
        Planet planet = activity.g3mComponent.g3mWidget.getG3MContext().getPlanet();
        for(Geodetic3D geodetic3D : routeList) {
            for(Map.Entry<String, Pair<Geodetic3D, List<Bitmap>>> cnp : coordImageMap.entrySet()) {
                Double distance = planet.computePreciseLatLonDistance(
                        new Geodetic2D(cnp.getValue().first._latitude, cnp.getValue().first._longitude),
                        new Geodetic2D(geodetic3D._latitude, geodetic3D._longitude));
                if (distance < 10.0) {
                    cnpArray.add(cnp.getKey());
                    break;
                }
            }
        }
        return cnpArray;
    }

    public boolean isCreateVisualsDone() {
        return bCreateVisualsDone;
    }
}
