uniform sampler2D Sampler;
uniform lowp vec4 uFlatColor;
varying lowp vec2 widthOut;

void main() {
    gl_FragColor = vec4(uFlatColor.rgb,
        uFlatColor.a * min(min(widthOut.x, 1.0), min(widthOut.y - widthOut.x, 1.0)));
}