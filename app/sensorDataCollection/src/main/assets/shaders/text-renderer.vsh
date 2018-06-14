attribute vec4 aPosition2D;
varying vec2 TextureCoordOut;
uniform vec2 uViewPortExtent;

void main() {
    vec2 pixel = aPosition2D.xy;
    pixel.x -= uViewPortExtent.x / 2.0;
    pixel.y += uViewPortExtent.y / 2.0;

    gl_Position = vec4((pixel.x) / (uViewPortExtent.x / 2.0),
        (pixel.y) / (uViewPortExtent.y / 2.0), 0, 1);

    TextureCoordOut = aPosition2D.zw;
}



