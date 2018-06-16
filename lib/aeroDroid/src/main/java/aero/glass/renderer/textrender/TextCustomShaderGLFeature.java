package aero.glass.renderer.textrender;

import org.glob3.mobile.generated.GL;
import org.glob3.mobile.generated.GLGlobalState;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.GPUAttribute;
import org.glob3.mobile.generated.GPUProgram;
import org.glob3.mobile.generated.GPUUniform;

import aero.glass.renderer.AbstractCustomShaderGLFeature;

/**
 * Created by premeczmatyas on 19/01/16.
 */
public class TextCustomShaderGLFeature extends AbstractCustomShaderGLFeature {

    public TextCustomShaderGLFeature() {
        super("text-renderer");
    }

    @Override
    public void applyOnGlobalGLState(GLGlobalState state) {
    }

    @Override
    protected void onAfterApplyShaderOnGPU(GL gl, GLState state,
                                           GPUProgram linkedProgram) {
       /* if (dummyUniformID != null) {
            // lazy hack to change color
            gl.uniform2f(dummyUniformID, 0.2f, 0.2f);
        }
*/
    }

    @Override
    protected boolean onInitializeShaderAttribute(GL gl, GPUProgram linkedProgram,
                                                  GPUAttribute attrib) {
        return true;
    }

    @Override
    protected boolean onInitializeShaderUniform(GL gl, GPUProgram linkedProgram,
                                                GPUUniform uniform) {
  /*      if ("DUMMY".equalsIgnoreCase(uniform._name)) {
            dummyUniformID = uniform._id;
        }
*/
        return true;
    }
}
