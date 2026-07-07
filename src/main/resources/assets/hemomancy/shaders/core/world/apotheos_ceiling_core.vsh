#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float HemoTime;
uniform float RotationSpeed;
uniform float CoreUndulationIntensity;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;
out float ceilingAngleT;
out float ceilingRadialT;
out float ceilingBowlDepth;
out float organicShift;
out float innerSurfaceIrregularity;

void main() {
    float angle = UV0.x * 6.2831853;
    vec2 unitCircle = vec2(cos(angle), sin(angle));
    float radial = UV0.y;
    float time = HemoTime * RotationSpeed;
    float foldA = sin(angle * 3.0 + radial * 8.0 + time * 1.6);
    float foldB = sin(angle * 7.0 - radial * 5.0 - time * 1.1);
    float inwardWeight = pow(1.0 - radial, 1.35);
    float centerWritheMask = smoothstep(0.22, 0.68, radial);
    float innerSurfaceRoughnessMask = smoothstep(0.05, 0.20, radial) * (1.0 - smoothstep(0.58, 0.92, radial));
    float innerSurfaceCrawlTime = time * 2.85;
    float innerSurfacePulse = 0.74 + sin(time * 1.9 + radial * 6.4) * 0.18
            + sin(time * 3.3 - angle * 2.0) * 0.08;
    float crawlA = angle + sin(radial * 8.0 + innerSurfaceCrawlTime * 0.43) * 0.24;
    float crawlB = radial + sin(angle * 4.0 - innerSurfaceCrawlTime * 0.37) * 0.032;
    float cellularSurfaceBump = sin(crawlA * 10.0 + crawlB * 23.0 + innerSurfaceCrawlTime)
            + sin(crawlA * 17.0 - crawlB * 31.0 - innerSurfaceCrawlTime * 0.72) * 0.48
            + sin(crawlA * 27.0 + crawlB * 11.0 + innerSurfaceCrawlTime * 1.27) * 0.24;
    float foldedSurfaceRidge = sin(crawlA * 6.0 - crawlB * 18.0 + innerSurfaceCrawlTime * 0.58)
            * sin(crawlA * 14.0 + crawlB * 29.0 - innerSurfaceCrawlTime * 0.91);
    float broadPlanetoidBreath = sin(time * 1.3 + inwardWeight * 2.4) * 0.020 * (1.0 - centerWritheMask);
    organicShift = ((foldA * 0.043 + foldB * 0.024) * centerWritheMask + broadPlanetoidBreath)
            * (0.30 + inwardWeight * 0.76) * CoreUndulationIntensity - inwardWeight * 0.082;
    innerSurfaceIrregularity = (cellularSurfaceBump * 0.018 + foldedSurfaceRidge * 0.014)
            * innerSurfacePulse
            * innerSurfaceRoughnessMask * CoreUndulationIntensity;
    float innerSurfaceRadialPush = innerSurfaceIrregularity * (0.52 + inwardWeight * 0.24);

    vec3 surfacePosition = Position;
    surfacePosition.xz += unitCircle * innerSurfaceRadialPush;
    surfacePosition.y += organicShift + innerSurfaceIrregularity;

    gl_Position = ProjMat * ModelViewMat * vec4(surfacePosition, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
    ceilingAngleT = UV0.x;
    ceilingRadialT = radial;
    ceilingBowlDepth = inwardWeight;
}
