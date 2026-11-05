#version 150

#moj_import <fog.glsl>

// The vertex stage of the Monolith's emblems (the three eyes, the back symbol): colour-only geometry laid on the
// stone. The stone is drawn by item/monolith_fragment, whose vertex stage breathes each vertex in and out; a slab face
// has four vertices, so the face itself moves as the interpolation of its corners. An emblem is many small quads, and
// morphed at its own vertices it would differ from the face under it by up to twice the morph and the stone would show
// through. So the emblem takes the face's corner displacements, interpolated by where it lies on the face. Keep the
// morph formula word for word with that shader.

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float StoneSeed;
uniform float MorphTime;
uniform float MorphStrength;
uniform vec3 FaceOrigin;
uniform vec3 FaceRight;
uniform vec3 FaceUp;
// Which diagonal the face's quad splits on: 0 for first-to-third corner a-c, 1 for b-d. A quad is drawn as two
// triangles, so the face's surface is linear in each, and the emblem must interpolate within the same triangle.
uniform float FaceDiagonal;

out float vertexDistance;
out vec4 vertexColor;

const float VERTEX_MORPH_RATE = 1.45;

// The surface shader's displacement of a vertex at p (camera-relative world space).
vec3 morphAt(vec3 p) {
    vec3 d = vec3(0.0);
    vec2 fromCenter = p.xz;
    float centerDistance = length(fromCenter);
    if (centerDistance > 0.0001) {
        float morph = sin(
            p.y * 10.0
            + p.x * 15.0
            - p.z * 13.0
            + MorphTime * VERTEX_MORPH_RATE
            + StoneSeed * 6.28318
        );
        float edgeWeight = smoothstep(0.0, 0.16, centerDistance);
        p.xz += normalize(fromCenter) * morph * MorphStrength * edgeWeight;
        d.xz = p.xz - fromCenter;
        d.y = sin((p.x - p.z) * 12.0 + MorphTime * 1.1 + StoneSeed * 4.2) * MorphStrength * 0.35 * edgeWeight;
    }
    return d;
}

void main() {
    vec3 p = Position;
    vec3 a = FaceOrigin;
    vec3 b = FaceOrigin + FaceRight;
    vec3 c = FaceOrigin + FaceRight + FaceUp;
    vec3 d = FaceOrigin + FaceUp;
    float u = clamp(dot(p - a, FaceRight) / max(dot(FaceRight, FaceRight), 1e-6), 0.0, 1.0);
    float v = clamp(dot(p - a, FaceUp) / max(dot(FaceUp, FaceUp), 1e-6), 0.0, 1.0);
    vec3 da = morphAt(a);
    vec3 db = morphAt(b);
    vec3 dc = morphAt(c);
    vec3 dd = morphAt(d);
    vec3 shift;
    if (FaceDiagonal < 0.5) {
        // Split a-c: triangle (a, b, c) below the diagonal, (a, c, d) above it.
        shift = u >= v ? da * (1.0 - u) + db * (u - v) + dc * v
                       : da * (1.0 - v) + dc * u + dd * (v - u);
    } else {
        // Split b-d: triangle (a, b, d) below it, (b, c, d) above it.
        shift = u + v <= 1.0 ? da * (1.0 - u - v) + db * u + dd * v
                             : db * (1.0 - v) + dc * (u + v - 1.0) + dd * (1.0 - u);
    }
    p += shift;

    gl_Position = ProjMat * ModelViewMat * vec4(p, 1.0);
    vertexDistance = fog_distance(p, FogShape);
    vertexColor = Color;
}
