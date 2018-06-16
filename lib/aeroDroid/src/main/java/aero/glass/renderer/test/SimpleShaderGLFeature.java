package aero.glass.renderer.test;

import org.glob3.mobile.generated.GL;
import org.glob3.mobile.generated.GLGlobalState;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.GPUAttribute;
import org.glob3.mobile.generated.GPUProgram;
import org.glob3.mobile.generated.GPUUniform;

import aero.glass.renderer.AbstractCustomShaderGLFeature;

/**
 * Created by premeczmatyas on 20/10/16.
 */
public class SimpleShaderGLFeature extends AbstractCustomShaderGLFeature {

    public SimpleShaderGLFeature(String shaderName) {
        super(shaderName);
    }

    @Override
    protected boolean onInitializeShaderAttribute(GL gl, GPUProgram linkedProgram,
                                                  GPUAttribute attrib) {
        return true;
    }

    @Override
    protected boolean onInitializeShaderUniform(GL gl, GPUProgram linkedProgram,
                                                GPUUniform uniform) {
        return true;
    }

    @Override
    protected void onAfterApplyShaderOnGPU(GL gl, GLState state, GPUProgram linkedProgram) {

    }

    @Override
    public void applyOnGlobalGLState(GLGlobalState state) {

    }
}
