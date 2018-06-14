package aero.glass.renderer.test;

import org.glob3.mobile.generated.CompositeMesh;
import org.glob3.mobile.generated.DirectMesh;
import org.glob3.mobile.generated.DownloaderImageBuilder;
import org.glob3.mobile.generated.FloatBufferBuilderFromCartesian2D;
import org.glob3.mobile.generated.FloatBufferBuilderFromCartesian3D;
import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.GLFormat;
import org.glob3.mobile.generated.GLPrimitive;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.IFloatBuffer;
import org.glob3.mobile.generated.IImage;
import org.glob3.mobile.generated.IImageBuilderListener;
import org.glob3.mobile.generated.Mesh;
import org.glob3.mobile.generated.SimpleTextureMapping;
import org.glob3.mobile.generated.TextureIDReference;
import org.glob3.mobile.generated.TextureMapping;
import org.glob3.mobile.generated.TexturedMesh;
import org.glob3.mobile.generated.URL;
import org.glob3.mobile.generated.Vector3D;

/**
 * Shape that tests the custom shader features.
 * Created by DrakkLord on 2015. 12. 03..
 */
public class CustomShaderTestShape extends TestShape {

    private TextureIDReference texId;
    private boolean textIdGetInProgress;

    /** This is just a test to use custom attributes as "color" can be sent as part of
     * a GL feature already! */
    private IFloatBuffer colorBuffer;

    public CustomShaderTestShape(Geodetic3D position) {
        super(position);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (colorBuffer != null) {
            colorBuffer.dispose();
        }
    }

    // this called AFTER a mesh has been returned!
    @Override
    protected void modifyGLState(GLState stateIn) {
        stateIn.addGLFeature(new TestShaderCustomGLFeature(colorBuffer), true);
    }

    @Override
    protected Mesh createMesh(G3MRenderContext rc) {
        if (texId == null) {
            if (!textIdGetInProgress) {
                getTextureId(rc);
            }
            return null;
        }

        Vector3D center = new Vector3D(0, 0, 0);

        FloatBufferBuilderFromCartesian3D builder = FloatBufferBuilderFromCartesian3D
                .builderWithGivenCenter(center);

        Vector3D leLeft = new Vector3D(-4000, 0, 0);
        Vector3D leRight = new Vector3D(0, 0, 0);
        Vector3D heLeft = new Vector3D(-4000, 4000, 0);
        Vector3D heRight = new Vector3D(0, 4000, 0);

        builder.add(heRight);
        builder.add(heLeft);
        builder.add(leRight);
        builder.add(leLeft);

        FloatBufferBuilderFromCartesian3D cbuilder =
                FloatBufferBuilderFromCartesian3D.builderWithoutCenter();
        cbuilder.add(0.0, 1.0, 0.5);
        cbuilder.add(1.0, 0.5, 0.0);
        cbuilder.add(0.5, 0.0, 1.0);
        cbuilder.add(1.0, 1.0, 0.5);
        colorBuffer = cbuilder.create();
        cbuilder.dispose();

        CompositeMesh mesh = new CompositeMesh();

        // add the gray fill
        DirectMesh planarMesh = new DirectMesh(GLPrimitive.triangleStrip(),
                                               true, center, builder.create(), 0, 0, null);

        FloatBufferBuilderFromCartesian2D texCoords = new FloatBufferBuilderFromCartesian2D();

        texCoords.add(1, 0);
        texCoords.add(0, 0);
        texCoords.add(1, 1);
        texCoords.add(0, 1);

        colorBuffer = texCoords.create();
        TextureMapping texMap = new SimpleTextureMapping(texId,
                                                         colorBuffer, true, true);

        TexturedMesh texturedMesh = new TexturedMesh(planarMesh, true,
                                                         texMap, true, false);
        mesh.addMesh(texturedMesh);

        builder.dispose();
        return mesh;
    }

    private void getTextureId(final G3MRenderContext rc) {
        DownloaderImageBuilder dl = new DownloaderImageBuilder(new URL("file:///aero_logo.png"));

        dl.build(rc, new IImageBuilderListener() {
            @Override
            public void dispose() {
            }

            @Override
            public void imageCreated(IImage image, String imageName) {
                texId = rc.getTexturesHandler()
                        .getTextureIDReference(image, GLFormat.rgba(), "aero_logo_test", false);
                textIdGetInProgress = false;
            }

            @Override
            public void onError(String error) {
                textIdGetInProgress = false;
            }
        }, true);
    }
}
