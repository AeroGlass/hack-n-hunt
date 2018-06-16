package aero.glass.renderer;

import org.glob3.mobile.generated.CustomShaderGLFeature;
import org.glob3.mobile.generated.GL;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.GLVariable;
import org.glob3.mobile.generated.GPUAttribute;
import org.glob3.mobile.generated.GPUAttributeKey;
import org.glob3.mobile.generated.GPUProgram;
import org.glob3.mobile.generated.GPUUniform;
import org.glob3.mobile.generated.GPUUniformKey;
import org.glob3.mobile.generated.ILogger;
import org.glob3.mobile.generated.INativeGL;

/**
 * This feature is a holder for common helper functions for shader handling.
 * Created by DrakkLord on 2015. 12. 02..
 */
public abstract class AbstractCustomShaderGLFeature extends CustomShaderGLFeature {

    /** Excludes G3M built in attribute names from the enumeration. */
    private final boolean excludeBuiltInsFromAttributeEnumeration;

    /** Excludes G3M built in uniform names from the enumeration. */
    private final boolean excludeBuiltInsFromUniformEnumeration;

    public AbstractCustomShaderGLFeature(String shaderName) {
        this(shaderName, true, true);
    }

    public AbstractCustomShaderGLFeature(String shaderName,
                                         boolean excludeAttribs, boolean excludeUniforms) {
        super(shaderName);
        excludeBuiltInsFromAttributeEnumeration = excludeAttribs;
        excludeBuiltInsFromUniformEnumeration = excludeUniforms;
    }

    /** Function called when shader attribute is found.
     * @see #AbstractCustomShaderGLFeature(String, boolean, boolean) constructor exclusion policy
     * @param gl G3M opengl wrapper object
     * @param linkedProgram compiled and linked shader program
     * @param attrib attribute found in linkedProgram
     * @return true if successfully handled the attribute.
     */
    protected abstract boolean onInitializeShaderAttribute(GL gl, GPUProgram linkedProgram,
                                                           GPUAttribute attrib);

    /** function called when a shader uniforms is found.
     * @see #AbstractCustomShaderGLFeature(String, boolean, boolean) constructor exclusion policy
     * @param gl G3M opengl wrapper object
     * @param linkedProgram compiled and linked shader program
     * @param uniform found in linkedProgram
     * @return true if successfully handled the uniform.
     */
    protected  abstract boolean onInitializeShaderUniform(GL gl, GPUProgram linkedProgram,
                                                          GPUUniform uniform);

    @Override
    public boolean onInitializeShader(GL gl, GLState state, GPUProgram linkedProgram) {
        final INativeGL igl = gl.getNative();

        // atrributes
        final int nAttribs = igl.getProgramiv(linkedProgram,
                                              GLVariable.activeAttributes());
        for (int i = 0; i < nAttribs; i++) {
            final GPUAttribute attrib = igl.getActiveAttribute(linkedProgram, i);
            if (attrib != null) {
                if (excludeBuiltInsFromAttributeEnumeration
                        && attrib._key != GPUAttributeKey.UNRECOGNIZED_ATTRIBUTE) {
                    continue;
                }
                if (!onInitializeShaderAttribute(gl, linkedProgram, attrib)) {
                    ILogger.instance().logError(
                            "custom shader feature failed to initialize shader ["
                                    + linkedProgram.getName()
                                    + "] attribute [" + attrib._name + "]");
                    return false;
                }
            }
        }

        // uniforms
        final int nUniforms = igl.getProgramiv(linkedProgram,
                                               GLVariable.activeUniforms());
        for (int i = 0; i < nUniforms; i++) {
            final GPUUniform uniform = igl.getActiveUniform(linkedProgram, i);
            if (uniform != null) {
                if (excludeBuiltInsFromUniformEnumeration
                        && uniform._key != GPUUniformKey.UNRECOGNIZED_UNIFORM) {
                    continue;
                }
                if (!onInitializeShaderUniform(gl, linkedProgram, uniform)) {
                    ILogger.instance().logError(
                            "custom shader feature failed to initialize shader ["
                                    + linkedProgram.getName()
                                    + "] uniform [" + uniform._name + "]");
                    return false;
                }
            }
        }
        return true;
    }
}
