package aero.glass.renderer.textrender;

import org.glob3.mobile.generated.GLBlendFactor;
import org.glob3.mobile.generated.GLFeature;
import org.glob3.mobile.generated.GLFeatureGroupName;
import org.glob3.mobile.generated.GLFeatureID;
import org.glob3.mobile.generated.GLGlobalState;
import org.glob3.mobile.generated.GPUAttributeKey;
import org.glob3.mobile.generated.GPUAttributeValueVec4Float;
import org.glob3.mobile.generated.GPUUniformKey;
import org.glob3.mobile.generated.GPUUniformValueInt;
import org.glob3.mobile.generated.IFloatBuffer;
import org.glob3.mobile.generated.IGLTextureId;

/**
 * Created by premeczmatyas on 19/01/16.
 */
public class TextGeometryGLFeature extends GLFeature {

    private IGLTextureId mTexID;

    private static final GPUUniformValueInt TEX_UNIT = new GPUUniformValueInt(0);

    public TextGeometryGLFeature(IFloatBuffer buffer,
                                 IGLTextureId id) {

        super(GLFeatureGroupName.NO_GROUP, GLFeatureID.GLF_GEOMETRY);

        GPUAttributeValueVec4Float mPosition = new GPUAttributeValueVec4Float(buffer,
                4, 0, 0, true);

        _values.addAttributeValue(GPUAttributeKey.POSITION_2D, mPosition, false);
        _values.addUniformValue(GPUUniformKey.SAMPLER, TEX_UNIT, false);
        mTexID = id;
    }


    public void setTextureId(IGLTextureId id) {
        mTexID = id;
    }


    @Override
    public void applyOnGlobalGLState(GLGlobalState state) {
        state.enableBlend();
        state.disableCullFace();
        state.disableDepthTest();
        state.setBlendFactors(GLBlendFactor.srcAlpha(),
                GLBlendFactor.oneMinusSrcAlpha());

        state.bindTexture(0, mTexID);
    }
}
