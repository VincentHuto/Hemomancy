#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float HemoTime;
uniform float ParchmentSeed;
uniform float WindRippleStrength;
uniform float WindRippleScale;
uniform vec3 WindDirection;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;
out float rippleShade;

float wave(vec2 value, vec2 axis, float scale, float speed, float phase) {
    return sin(dot(value, axis) * scale + HemoTime * speed + ParchmentSeed * phase);
}

void main() {
    vec2 pageUv = vec2(fract(UV0.x * 4.0), UV0.y);
    vec2 local = pageUv - vec2(0.5);
    vec2 wind = normalize(WindDirection.xy + vec2(0.001, 0.0));
    vec2 crossWind = vec2(-wind.y, wind.x);
    float edgeDistance = min(min(pageUv.x, 1.0 - pageUv.x), min(pageUv.y, 1.0 - pageUv.y));
    float edgeDamping = smoothstep(0.04, 0.20, edgeDistance);
    float broad = wave(local, wind, WindRippleScale * 7.0, 2.10, 0.031);
    float cross = wave(local + broad * 0.025, crossWind, WindRippleScale * 10.0, -1.35, 0.047);
    float fine = wave(local - cross * 0.018, normalize(wind + crossWind * 0.42), WindRippleScale * 17.5, 3.65, 0.071);
    float rippleOffset = (broad * 0.46 + cross * 0.20 + fine * 0.06) * WindRippleStrength * edgeDamping;
    float windSlide = fine * WindRippleStrength * 0.035 * edgeDamping;

    vec3 surfacePosition = Position;
    surfacePosition.y += rippleOffset;
    surfacePosition.x += wind.x * windSlide;
    surfacePosition.z += wind.y * windSlide;

    gl_Position = ProjMat * ModelViewMat * vec4(surfacePosition, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
    rippleShade = (broad * 0.5 + fine * 0.5) * edgeDamping;
}
