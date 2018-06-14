attribute vec4 aPosition2D;
uniform vec2 uViewPortExtent;
varying lowp vec2 widthOut;

void main() {
    vec2 pixel = aPosition2D.xy;
    pixel.x -= uViewPortExtent.x / 2.0;
    pixel.y += uViewPortExtent.y / 2.0;

    gl_Position = vec4((pixel.x) / (uViewPortExtent.x / 2.0),
        (pixel.y) / (uViewPortExtent.y / 2.0), 0, 1);

    widthOut = aPosition2D.zw;
}

