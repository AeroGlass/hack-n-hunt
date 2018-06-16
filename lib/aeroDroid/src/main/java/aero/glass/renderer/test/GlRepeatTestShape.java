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
import org.glob3.mobile.generated.GLTextureParameterValue;
import org.glob3.mobile.generated.Geodetic3D;
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
 * Created by Pillio on 2015. 12. 14..
 */
public class GlRepeatTestShape extends TestShape {

    private TextureIDReference texId;
    private boolean textIdGetInProgress;

    public GlRepeatTestShape(Geodetic3D position) {
        super(position);
    }

    // this called AFTER a mesh has been returned!
    @Override
    protected void modifyGLState(GLState stateIn) {
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

        CompositeMesh mesh = new CompositeMesh();

        // add the gray fill
        DirectMesh planarMesh = new DirectMesh(GLPrimitive.triangleStrip(),
                true, center, builder.create(), 0, 0, null);

        FloatBufferBuilderFromCartesian2D texCoords = new FloatBufferBuilderFromCartesian2D();

        texCoords.add(3, 0);
        texCoords.add(0, 0);
        texCoords.add(3, 3);
        texCoords.add(0, 3);

        TextureMapping texMap = new SimpleTextureMapping(texId,
                texCoords.create(), true, true);

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
                        .getTextureIDReference(image, GLFormat.rgba(), "repeated_aero_logo_test",
                                false, GLTextureParameterValue.repeat());
                textIdGetInProgress = false;
            }

            @Override
            public void onError(String error) {
                textIdGetInProgress = false;
            }
        }, true);
    }
}
