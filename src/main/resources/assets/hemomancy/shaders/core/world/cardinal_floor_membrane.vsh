#version 150

#moj_import <fog.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec3 viewOffset;
// The brazier trench packs its sideways direction, taper and the colour of the ground it cuts through into UV0;
// they are unpacked here, per vertex, so they interpolate cleanly across the trench.
out vec2 gashSide;
out float gashLean;
out vec3 soilTint;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;
    // Camera-relative position, so the fragment shader can see down into the brazier gash.
    viewOffset = Position;
    gashSide = vec2(1.0, 0.0);
    gashLean = 1.0;
    soilTint = vec3(0.0);
    if (Color.g < 0.06) {
        // UV0.x = lean step * 8 + side angle; UV0.y = the ground's colour as a 24-bit integer.
        float leanStep = floor(UV0.x / 8.0);
        float angle = UV0.x - leanStep * 8.0;
        gashSide = vec2(cos(angle), sin(angle));
        gashLean = leanStep / 1000.0;
        float red = floor(UV0.y / 65536.0);
        float green = floor((UV0.y - red * 65536.0) / 256.0);
        float blue = UV0.y - red * 65536.0 - green * 256.0;
        soilTint = vec3(red, green, blue) / 255.0;
    }
}
