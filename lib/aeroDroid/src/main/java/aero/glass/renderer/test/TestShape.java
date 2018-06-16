package aero.glass.renderer.test;

import org.glob3.mobile.generated.Angle;
import org.glob3.mobile.generated.G3MContext;
import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.GLFeatureGroupName;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.Mesh;
import org.glob3.mobile.generated.ModelTransformGLFeature;
import org.glob3.mobile.generated.MutableMatrix44D;
import org.glob3.mobile.generated.Planet;
import org.glob3.mobile.generated.Vector3D;

/**
 * Test shape that is used for testing basic features visually.
 * Created by DrakkLord on 2015. 12. 03..
 */
public abstract class TestShape {

    private Geodetic3D position;

    private Angle heading;
    private Angle pitch;
    private Angle roll;

    private double scaleX;
    private double scaleY;
    private double scaleZ;

    private double translationX;
    private double translationY;
    private double translationZ;

    private MutableMatrix44D transformMatrix;

    private final GLState glState;
    private Mesh mesh;

    private boolean modifiedGLState;

    protected abstract Mesh createMesh(G3MRenderContext rc);

    public TestShape(Geodetic3D positionIn) {
        position = positionIn;
        heading = new Angle(Angle.zero());
        pitch = new Angle(Angle.zero());
        roll = new Angle(Angle.zero());
        scaleX = 1;
        scaleY = 1;
        scaleZ = 1;
        translationX = 0;
        translationY = 0;
        translationZ = 0;
        transformMatrix = null;
        glState = new GLState();
        mesh = null;
        modifiedGLState = false;
    }

    public void dispose() {
        if (position != null) {
            position.dispose();
        }

        if (heading != null) {
            heading.dispose();
        }
        if (pitch != null) {
            pitch.dispose();
        }
        if (roll != null) {
            roll.dispose();
        }

        if (transformMatrix != null) {
            transformMatrix.dispose();
        }

        if (mesh != null) {
            mesh.dispose();
        }

        glState._release();
    }

    protected void modifyGLState(GLState stateIn) {
    }

    private MutableMatrix44D getTransformMatrix(Planet planet) {
        if (transformMatrix == null) {
            transformMatrix = createTransformMatrix(planet);
            glState.clearGLFeatureGroup(GLFeatureGroupName.CAMERA_GROUP);
            glState.addGLFeature(new ModelTransformGLFeature(transformMatrix.asMatrix44D()), false);
        }
        return transformMatrix;
    }

    protected void cleanTransformMatrix() {
        if (transformMatrix != null) {
            transformMatrix.dispose();
        }
        transformMatrix = null;
    }

    public final MutableMatrix44D createTransformMatrix(Planet planet) {
        double altitude = position._height;

        Geodetic3D positionWithSurfaceElevation =
                new Geodetic3D(position._latitude, position._longitude, altitude);

        final MutableMatrix44D geodeticTransform =
                (position == null)
                ? MutableMatrix44D.identity()
                : planet.createGeodeticTransformMatrix(positionWithSurfaceElevation);

        final MutableMatrix44D headingRotation = MutableMatrix44D
                .createRotationMatrix(heading, Vector3D.downZ());
        final MutableMatrix44D pitchRotation = MutableMatrix44D
                .createRotationMatrix(pitch, Vector3D.upX());
        final MutableMatrix44D rollRotation = MutableMatrix44D
                .createRotationMatrix(roll, Vector3D.upY());
        final MutableMatrix44D scale = MutableMatrix44D
                .createScaleMatrix(scaleX, scaleY, scaleZ);
        final MutableMatrix44D translation = MutableMatrix44D
                .createTranslationMatrix(translationX, translationY, translationZ);
        final MutableMatrix44D localTransform = headingRotation
                .multiply(pitchRotation)
                .multiply(rollRotation)
                .multiply(translation)
                .multiply(scale);

        return new MutableMatrix44D(geodeticTransform.multiply(localTransform));
    }

    public final Geodetic3D getPosition() {
        return position;
    }

    public final Angle getHeading() {
        return heading;
    }

    public final Angle getPitch() {
        return pitch;
    }

    public final Angle getRoll() {
        return roll;
    }

    public final void setPosition(Geodetic3D positionIn) {
        if (position != null) {
            position.dispose();
        }
        position = positionIn;
        cleanTransformMatrix();
    }

    public final void setHeading(Angle headingIn) {
        heading = headingIn;
        cleanTransformMatrix();
    }

    public final void setPitch(Angle pitchIn) {
        pitch = pitchIn;
        cleanTransformMatrix();
    }

    public final void setRoll(Angle rollIn) {
        roll = rollIn;
        cleanTransformMatrix();
    }

    public final void setScale(double scale) {
        setScale(scale, scale, scale);
    }

    public final void setTranslation(Vector3D translation) {
        setTranslation(translation._x, translation._y, translation._z);
    }

    public final void setTranslation(double translationXi,
                                     double translationYi, double translationZi) {
        translationX = translationXi;
        translationY = translationYi;
        translationZ = translationZi;
        cleanTransformMatrix();
    }

    public final void setScale(double scaleXi, double scaleYi, double scaleZi) {
        scaleX = scaleXi;
        scaleY = scaleYi;
        scaleZ = scaleZi;
        cleanTransformMatrix();
    }

    public final void setScale(Vector3D scale) {
        setScale(scale._x, scale._y, scale._z);
    }

    public final Vector3D getScale() {
        return new Vector3D(scaleX, scaleY, scaleZ);
    }

    public final void render(G3MRenderContext rc, GLState parentGLState) {
        // Applying transform to glState
        getTransformMatrix(rc.getPlanet());
        glState.setParent(parentGLState);
        rawRender(rc, glState);
    }

    public void initialize(G3MContext context) {
        // empty on purpose
    }

    public final void rawRender(G3MRenderContext rc, GLState parentState) {
        Mesh meshr = getMesh(rc);
        if (meshr != null) {
            if (!modifiedGLState) {
                modifyGLState(glState);
                modifiedGLState = true;
            }
            meshr.render(rc, parentState);
        }
    }

    protected final Mesh getMesh(G3MRenderContext rc) {
        if (mesh == null) {
            mesh = createMesh(rc);
        }
        return mesh;
    }

    protected final void cleanMesh() {
        if (mesh != null) {
            mesh.dispose();
        }
        mesh = null;
    }
}
