package aero.glass.renderer.mark;

import org.glob3.mobile.generated.Camera;
import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.DefaultRenderer;
import org.glob3.mobile.generated.G3MEventContext;
import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.Planet;
import org.glob3.mobile.generated.Vector3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import aero.glass.renderer.linerender.PatternRenderer;
import aero.glass.renderer.linerender.RenderablePattern;
import aero.glass.renderer.linerender.RenderableTriStrip;
import aero.glass.renderer.textrender.TextRenderer;

/**
 * Created by premeczmatyas on 12/01/16.
 */
public class AeroMarksRenderer extends DefaultRenderer {

    protected final TextRenderer textRenderer;
    protected final IAeroOverlapDetector detector;
    private final PatternRenderer patternRenderer;
    protected boolean bUpdateNeeded;
    private int screenWidth;
    private int screenHeight;

    private final ArrayList<AeroMark> marks = new ArrayList<AeroMark>();
    private Set<AeroMark> lastVisible = new HashSet<AeroMark>();
    private RenderablePattern dirPattern = new RenderablePattern(
            Color.fromRGBA255(0x70, 0xD0, 0xFF, 0xB0));
    private RenderableTriStrip altitudePattern = new RenderableTriStrip(
            Color.fromRGBA255(0xF0, 0xF0, 0xFF, 0xFF));
    private RenderablePattern threatPattern = new RenderablePattern(
            Color.fromRGBA255(0xFF, 0x0F, 0x0F, 0xBF));

    private boolean bSortNeeded = false;
    private boolean stereo;

    public AeroMarksRenderer(TextRenderer r, PatternRenderer p, IAeroOverlapDetector d,
                             boolean stereo) {
        textRenderer = r;
        patternRenderer = p;
        detector = d;
        patternRenderer.addPattern(dirPattern);
        patternRenderer.addPattern(threatPattern);
        patternRenderer.addPattern(altitudePattern);
        this.stereo = stereo;
    }

    public void addMark(AeroMark m) {
        marks.add(m);
        m.setEnabled(false);
        bUpdateNeeded = true;
        bSortNeeded = true;
    }

    public void removeMark(AeroMark m) {
        m.dispose();
        marks.remove(m);
        bUpdateNeeded = true;
        bSortNeeded = true;
    }


    public boolean needsUpdate() {
        return bUpdateNeeded;
    }


    public void setUpdate(boolean flag) {
        bUpdateNeeded = flag;
    }


    public void update(Camera c, Planet p) {
        bUpdateNeeded = false;
        final Set<AeroMark> visible = new HashSet<AeroMark>();
        detector.init(visible);

        Vector3D camPos = c.getCartesianPosition();
        for (AeroMark m : marks) {
            m.updateDistance(camPos, p);
            if (!m.isEnabled()) {
                // enabled marks get their positions updated in the render() method every frame
                m.updateScreenPos(stereo, screenWidth, c, p, System.currentTimeMillis());
            }
            m.updateRealEstate(c, p);
        }

        // sort by priority
        if (bSortNeeded) {
            Collections.sort(marks);
            bSortNeeded = false;
        }


        final Iterator<AeroMark> i = marks.iterator();
        while (i.hasNext()) {
            final AeroMark m = i.next();
            if (m.getRealEstate() == null) {
                continue;
            }
            // //// adopted from G3M Mark (including comments)
            boolean occludedByHorizon = false;

            final Vector3D cameraPosition = c.getCartesianPosition();
            final Vector3D markCameraVector = m.getCartesianPosition(p)
                    .sub(cameraPosition);

            if (m.getPosition()._height > c.getGeodeticPosition()._height) {
                // Computing horizon culling
                final ArrayList<Double> dists = p
                        .intersectionsDistances(cameraPosition,
                                markCameraVector);
                if (!dists.isEmpty()) {
                    final double dist = dists.get(0);
                    if (dist > 0.0 && dist < 1.0) {
                        occludedByHorizon = true;
                    }
                }
            } else {
                // if camera position is upper than mark we can compute horizon
                // culling in a much simpler way
                occludedByHorizon = m.getNormalAtMarkPosition(p).angleBetween(
                        markCameraVector)._radians <= 0.5 * Math.PI;
            }

            if (!occludedByHorizon) {
                detector.check(m);
            }

        }

        for (AeroMark m : lastVisible) {
            if (!visible.contains(m)) {
                m.setEnabled(false);
            }
        }
        for (AeroMark m : visible) {
            if (!lastVisible.contains(m)) {
                m.setEnabled(true);
            }
        }
        lastVisible = visible;
    }


    @Override
    public void render(G3MRenderContext rc, GLState glState) {
        final int marksSize = marks.size();
        Camera camera = rc.getCurrentCamera();

        if (marksSize > 0 && TextRenderer.isTextRenderCoreAvailable()) {
            Planet planet = rc.getPlanet();

            // erase traffic patterns
            dirPattern.reset();
            threatPattern.reset();
            altitudePattern.reset();

            double camRoll = camera.getRoll()._radians;
            for (AeroMark mark : marks) {
                if (mark.isEnabled()) {
                    mark.updateScreenPos(stereo, screenWidth, camera, planet,
                            System.currentTimeMillis());
                    if (mark instanceof AeroTrafficMark && !mark.isBehindCamera()) {
                        ((AeroTrafficMark) mark).updateIcon(planet,
                               dirPattern, threatPattern, altitudePattern,
                               screenWidth, screenHeight,
                               camRoll);
                    }

                    if (!mark.isBehindCamera()) {
                        textRenderer.addText(mark.getRenderableText());
                    }
                }
            }
            // no actual rendering is done here, we are just setting up the TextRenderer and the
            // patterns already given to PatternRenderer in the constructor
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (dirPattern != null) {
            patternRenderer.removePattern(dirPattern);
            patternRenderer.removePattern(altitudePattern);
            patternRenderer.removePattern(threatPattern);
            dirPattern.dispose();
            altitudePattern.dispose();
            threatPattern.dispose();
            dirPattern = null;
            altitudePattern = null;
            threatPattern = null;
        }
    }

/*
    @Override
    public boolean onTouchEvent(G3MEventContext ec, TouchEvent touchEvent) {

        boolean handled = false;
        if (touchEvent.getType() == TouchEventType.DownUp && mLastCamera != null) {
            final Vector2F touchedPixel = touchEvent.getTouch(0).getPos();
            final Planet planet = ec.getPlanet();

            double minSqDistance = IMathUtils.instance().maxDouble();
            AeroMark nearestMark = null;

            final int marksSize = marks.size();
            for (int i = 0; i < marksSize; i++) {
                AeroMark mark = marks.get(i);


                final Vector3D cartesianMarkPosition = mark.getCartesianPosition(planet);
                final Vector2F markPixel = mLastCamera.point2Pixel(cartesianMarkPosition);

                final RectangleF markPixelBounds = mark.getRealEstate();

                if (markPixelBounds.contains(touchedPixel._x, touchedPixel._y)) {
                    final double sqDistance = markPixel.squaredDistanceTo(touchedPixel);
                    if (sqDistance < minSqDistance) {
                        nearestMark = mark;
                        minSqDistance = sqDistance;
                    }
                }
            }

            if (nearestMark != null) {
                handled = nearestMark.touched();
                if (!handled && mMarkTouchListener != null) {
                    handled = mMarkTouchListener.touchedMark(nearestMark);
                }
            }
        }
        return handled;
    }
*/
    @Override
    public void onResizeViewportEvent(G3MEventContext ec, int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }


    public void removeType(AeroMarkType type) {
        for (Iterator<AeroMark> i = marks.iterator(); i.hasNext();) {
            AeroMark m = i.next();
            if (m.getType().equals(type)) {
                m.dispose();
                i.remove();
                bUpdateNeeded = true;
            }
        }
    }
}
