package aero.glass.renderer.linerender;

import org.glob3.mobile.generated.GLBlendFactor;
import org.glob3.mobile.generated.GLFeature;
import org.glob3.mobile.generated.GLFeatureGroupName;
import org.glob3.mobile.generated.GLFeatureID;
import org.glob3.mobile.generated.GLGlobalState;
import org.glob3.mobile.generated.GPUAttributeKey;
import org.glob3.mobile.generated.GPUAttributeValueVec4Float;
import org.glob3.mobile.generated.IFloatBuffer;

/**
 * Created by premeczmatyas on 14/06/16.
 */
public class SmoothPatternGeometryGLFeature extends GLFeature {

    public SmoothPatternGeometryGLFeature(IFloatBuffer buffer) {

        super(GLFeatureGroupName.NO_GROUP, GLFeatureID.GLF_GEOMETRY);


        GPUAttributeValueVec4Float mPosition = new GPUAttributeValueVec4Float(buffer,
                4, 0, 0, true);
        _values.addAttributeValue(GPUAttributeKey.POSITION_2D, mPosition, false);
    }


    @Override
    public void applyOnGlobalGLState(GLGlobalState state) {
        state.enableBlend();
        state.disableCullFace();
        state.disableDepthTest();
        state.setBlendFactors(GLBlendFactor.srcAlpha(),
                GLBlendFactor.oneMinusSrcAlpha());
    }
}
