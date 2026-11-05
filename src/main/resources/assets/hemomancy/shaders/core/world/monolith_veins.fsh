#version 150

#moj_import <fog.glsl>

// The tendrils climbing a standing Sanguine Monolith from the ground: three layers of noise contours, stretched upward
// so they read as strands that meander and fork, thick at the root and thinning to nothing at a ragged top. A dark wall
// sits each strand into the stone, and pulses of light travel up it. UV is in blocks: u runs around the stone's
// perimeter so strands carry over its corners, v up from the base. Zone is how high they reach, Tint their colour.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float StoneSeed;
uniform float Zone;
uniform vec3 Tint;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

#moj_import <hemomancy:noise/vein_hash3.glsl>
#moj_import <hemomancy:noise/vein_hash1.glsl>
#moj_import <hemomancy:noise/vein_noise.glsl>
#moj_import <hemomancy:noise/vein_fbm.glsl>

void over(inout vec3 colour, inout float alpha, vec3 top, float topAlpha) {
    float combined = 1.0 - (1.0 - alpha) * (1.0 - topAlpha);
    colour = (colour * alpha * (1.0 - topAlpha) + top * topAlpha) / max(combined, 0.0001);
    alpha = combined;
}

void main() {
    vec2 px = texCoord0 * 16.0;
    float t = HemoTime;
    float seed = StoneSeed * 91.0;
    float height = texCoord0.y / max(Zone, 0.05);

    // The top of the growth is ragged and creeps a little.
    float ragged = height + (fbm(vec3(px.x * 0.16, seed, t * 0.006)) - 0.5) * 0.55;
    float reach = 1.0 - smoothstep(0.62, 1.0, ragged);
    if (reach < 0.003) discard;

    // The strands writhe: the whole web drifts as something alive under the skin does, and each strand wriggles
    // along its length, out of step with its neighbours.
    vec2 warp = vec2(fbm(vec3(px * 0.10, seed + 3.0 + t * 0.022)), fbm(vec3(px * 0.10, seed + 9.0 - t * 0.019))) - 0.5;
    float wriggle = sin(px.y * 0.55 + t * 0.07 + seed) * 1.3 + sin(px.y * 0.21 - t * 0.045 + px.x * 0.3) * 1.1;
    vec2 q = vec2(px.x + warp.x * 11.0 + wriggle, px.y * 0.36 + warp.y * 5.0);

    vec3 rgb = vec3(0.0);
    float alpha = 0.0;
    float root = 1.45 - min(ragged, 1.0);
    over(rgb, alpha, Tint * 0.22, (1.0 - smoothstep(0.0, 0.45, height)) * 0.32 * reach);

    for (int i = 0; i < 3; i++) {
        float scale = 0.2 + float(i) * 0.14;
        float n = smoothNoise(vec3(q * scale + float(i) * 13.7, seed + float(i) * 5.3));
        float gap = abs(n - 0.5);
        float grain = fwidth(n) * 0.6;
        float width = (0.042 - float(i) * 0.011) * root;
        float strand = (1.0 - smoothstep(width * 0.55, width + grain, gap)) * reach;
        float wall = (1.0 - smoothstep(width, width * 2.8 + grain, gap)) * reach;
        float core = (1.0 - smoothstep(0.0, width * 0.5 + grain, gap)) * reach;
        float pulse = 0.62 + 0.38 * sin(px.y * 0.42 - t * (0.09 + float(i) * 0.02) + n * 7.0 + float(i) * 2.1);
        over(rgb, alpha, vec3(0.012, 0.001, 0.002), wall * 0.62);
        over(rgb, alpha, Tint * (0.52 + 0.3 * pulse), strand);
        over(rgb, alpha, mix(Tint, vec3(1.0, 0.82, 0.78), 0.32) * (0.72 + 0.5 * pulse), core * 0.85);
    }

    alpha *= vertexColor.a * ColorModulator.a;
    if (alpha < 0.01) discard;
    fragColor = linear_fog(vec4(rgb * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
