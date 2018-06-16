package aero.glass.renderer.test;

import org.glob3.mobile.generated.GL;
import org.glob3.mobile.generated.GLGlobalState;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.GPUAttribute;
import org.glob3.mobile.generated.GPUProgram;
import org.glob3.mobile.generated.GPUUniform;
import org.glob3.mobile.generated.IFloatBuffer;
import org.glob3.mobile.generated.IGLUniformID;

import aero.glass.renderer.AbstractCustomShaderGLFeature;

/**
 * Used to test the custom GL shader feature of G3M.
 * Created by DrakkLord on 2015. 12. 03..
 */
public class TestShaderCustomGLFeature extends AbstractCustomShaderGLFeature {

    private final IFloatBuffer colorData;
    private int colorAttributeID = -1;
    private IGLUniformID colorOffsetUniformID = null;
    private float colorAnim = 0.0f;

    public TestShaderCustomGLFeature(IFloatBuffer colorAttrData) {
        super("test-shader");
        colorData = colorAttrData;
    }

    @Override
    protected boolean onInitializeShaderAttribute(GL gl, GPUProgram linkedProgram,
                                                  GPUAttribute attrib) {
        if ("aColorData".equalsIgnoreCase(attrib._name)) {
            colorAttributeID = attrib._id;
        }
        return true;
    }

    @Override
    protected boolean onInitializeShaderUniform(GL gl, GPUProgram linkedProgram,
                                                GPUUniform uniform) {
        if ("uColorOffset".equalsIgnoreCase(uniform._name)) {
            colorOffsetUniformID = uniform._id;
        }
        return true;
    }

    @Override
    protected void onAfterApplyShaderOnGPU(GL gl, GLState state, GPUProgram linkedProgram) {
        if (colorAttributeID != -1 && colorData != null) {
            gl.enableVertexAttribArray(colorAttributeID);
            gl.vertexAttribPointer(colorAttributeID, 2, false, 0, colorData);
        }
        if (colorOffsetUniformID != null) {
            // lazy hack to change color
            colorAnim += 0.2f;
            gl.uniform3f(colorOffsetUniformID, 0.2f, (float) Math.sin(colorAnim), 0.2f);
        }
    }

    @Override
    public void applyOnGlobalGLState(GLGlobalState state) {
    }
}
