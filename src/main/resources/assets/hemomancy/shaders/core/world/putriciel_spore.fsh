#version 150
#moj_import <fog.glsl>

// Saint Putriciel's rot in the air (visual pass 2): spore clouds with body, the ooze the rot throws, its spore glints
// and the standing curtain of a spore wave. Four shapes, each on a card or a ribbon whose U runs 0 to 1 left to right
// (or along a ribbon) and V 0 to 1 bottom to top (or across it):
//   0 PUFF     a billow of spores, round, lit from above with a pale crest and a darker underside
//   1 OOZE     thick rot ooze along a ribbon: a rounded, glossy, near-black body with a shine line and cream scum
//   2 MOTE     a spore glint: a small pale point with a soft halo
//   3 CURTAIN  a standing sheet of spores, dense at the floor, its top edge billowing
//   4 ASH      a flake of ash shed off the burning body or a burnt crust (revision 2026-11-06): greys only, no hue;
//              green is how dark (ash grey to charcoal), red how far it has curled as it falls
// The vertex colour is state, never a tint: red how far the spores have thinned (a fresh puff is dense, an old one
// frays to nothing), green how dark (pale spore cloud at 0, dark rot smoke at 1), blue the glow of the spores in it.
// Alpha is the strength. Charcoal olive, olive, ochre, muted yellow and pale cream only: no red, no fire.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
in float vertexDistance;
in vec4 vertexColor;
in vec2 uv;
flat in float shape;
flat in float seed;
out vec4 fragColor;

#moj_import <hemomancy:noise/value_hash.glsl>
#moj_import <hemomancy:noise/material_noise.glsl>
#moj_import <hemomancy:noise/material_fbm.glsl>

const vec3 ROT_DARK = vec3(0.060, 0.064, 0.030);
const vec3 OLIVE = vec3(0.320, 0.330, 0.140);
const vec3 OCHRE = vec3(0.600, 0.480, 0.210);
const vec3 YELLOW = vec3(0.720, 0.650, 0.340);
const vec3 CREAM = vec3(0.920, 0.860, 0.660);
const vec3 OOZE = vec3(0.060, 0.066, 0.028);
const vec3 SHEEN = vec3(0.700, 0.680, 0.480);
// the ash flakes: neutral greys, never the rot's hue or the fire's
const vec3 ASH_PALE = vec3(0.470, 0.462, 0.455);
const vec3 ASH_DARK = vec3(0.105, 0.100, 0.098);

float sq(float x) {
    return x * x;
}

// The lit colour of spore cloud at a point whose surface faces {n}: pale and creamy where the light falls, olive in its
// body, charcoal olive underneath; dark rot smoke when {dark} is high.
vec3 cloud(vec3 n, float dark, float grain) {
    float light = clamp(dot(n, normalize(vec3(-0.3, 0.85, 0.42))), 0.0, 1.0);
    vec3 pale = mix(OLIVE * 1.1, mix(YELLOW, CREAM, 0.6), light * 0.85);
    pale = mix(pale, OCHRE * 0.9, smoothstep(0.55, 0.8, grain) * 0.35);
    vec3 smoke = mix(ROT_DARK, OLIVE * 0.75, light * 0.7);
    vec3 colour = mix(pale, smoke, dark);
    colour *= 0.55 + 0.45 * smoothstep(-0.6, 0.4, n.y);
    return colour;
}

void main() {
    float thin = vertexColor.r;
    float dark = vertexColor.g;
    float glow = vertexColor.b;
    float strength = vertexColor.a * ColorModulator.a;
    float t = HemoTime;
    vec2 p = uv * 2.0 - 1.0;
    vec3 colour = OLIVE;
    float alpha = 0.0;

    if (shape < 0.5) {
        // PUFF: the billow's edge rolls slowly; as it thins, holes open in it from the inside out
        float billow = fbm(p * 1.9 + vec2(seed * 0.37, seed * 0.11 - t * 0.012));
        float r = length(p) + (billow - 0.5) * 0.55;
        float body = 1.0 - smoothstep(0.5, 0.96, r);
        float fray = smoothstep(thin * 0.85 - 0.05, thin * 0.85 + 0.3, billow + 0.3 * (1.0 - r));
        vec3 n = vec3(p.x, p.y, sqrt(max(0.0, 1.0 - dot(p, p))));
        colour = cloud(normalize(n + vec3(0.0, 0.0, 0.25)), dark, billow);
        float speck = step(1.0 - 0.1 * glow, hash(floor(uv * 16.0) + seed)) * body;
        colour = mix(colour, CREAM * 1.05, speck);
        colour += CREAM * 0.12 * glow * body;
        alpha = body * fray * (0.86 - 0.3 * dark * thin);
    } else if (shape < 1.5) {
        // OOZE: a rounded body seen from the side, its edge worried by the flow
        float across = uv.y * 2.0 - 1.0;
        float along = uv.x;
        float edge = (noise(vec2(along * 9.0 - t * 0.05, seed)) - 0.5) * 0.35;
        float profile = 1.0 - sq(across);
        float body = smoothstep(0.0, 0.22, profile + edge * (1.0 - profile));
        vec3 colour0 = mix(OOZE, OLIVE * 0.55, smoothstep(0.2, 1.0, profile) * 0.6);
        colour0 = mix(colour0, ROT_DARK * 0.6, dark);
        float shine = exp(-sq((across + 0.38) * 6.5)) * (0.55 + 0.45 * noise(vec2(along * 6.0 - t * 0.08, seed + 3.0)));
        colour0 += SHEEN * shine * 0.45;
        float scum = smoothstep(0.62, 0.76, fbm(vec2(along * 7.0 - t * 0.02, across * 2.0) + seed));
        colour0 = mix(colour0, mix(CREAM * 0.6, CREAM, glow), scum * 0.75);
        colour0 = mix(colour0, OCHRE * 0.85, smoothstep(0.75, 0.95, abs(across)) * body * 0.5);
        colour = colour0;
        alpha = body * (0.95 - 0.6 * thin);
    } else if (shape < 2.5) {
        // MOTE: a spore glint
        float d = length(p);
        float core = exp(-d * d * 14.0);
        colour = mix(YELLOW, CREAM * 1.1, core);
        alpha = (core + exp(-d * d * 4.0) * 0.35 * (0.4 + glow)) * (1.0 - thin);
    } else if (shape > 3.5) {
        // ASH: an uneven flake turned by its seed, charcoal or ash grey, flecked, its far side darker as it curls
        float turn = seed * 1.7;
        vec2 r = mat2(cos(turn), -sin(turn), sin(turn), cos(turn)) * p;
        float edge = max(abs(r.x) * 1.1, abs(r.y) * 1.6) + (noise(floor(uv * 8.0) + seed) - 0.5) * 0.45;
        float body = 1.0 - smoothstep(0.62, 0.78, edge);
        float tone = noise(floor(uv * 6.0) + seed + 3.0);
        vec3 grey = mix(ASH_PALE, ASH_DARK, dark) * (0.8 + 0.35 * tone);
        grey *= 1.0 - 0.35 * thin * smoothstep(0.0, 0.8, r.x);
        colour = grey;
        alpha = body * 0.95;
    } else {
        // CURTAIN: dense at the floor, thinning upward, its top edge a rolling billow
        float v = uv.y;
        float billow = fbm(vec2(uv.x * 3.2 + seed, v * 1.6 - t * 0.018));
        float top = 0.78 + (billow - 0.5) * 0.45;
        float body = 1.0 - smoothstep(top - 0.22, top, v);
        body *= smoothstep(0.0, 0.06, v) * (0.55 + 0.45 * (1.0 - v));
        float fray = smoothstep(thin * 0.8 - 0.05, thin * 0.8 + 0.3, billow + 0.25 * (1.0 - v));
        vec3 n = normalize(vec3((billow - 0.5) * 0.8, 0.35 + v * 0.6, 0.7));
        colour = cloud(n, dark, billow);
        float crest = exp(-sq((v - top + 0.1) / 0.07));
        colour = mix(colour, CREAM * 0.95, crest * 0.5 * (1.0 - dark));
        float speck = step(1.0 - 0.08 * glow, hash(floor(uv * vec2(32.0, 16.0)) + seed)) * body;
        colour = mix(colour, CREAM, speck);
        alpha = body * fray * 0.8;
    }

    alpha *= strength;
    if (alpha < 0.003) discard;
    fragColor = linear_fog(vec4(colour * ColorModulator.rgb, clamp(alpha, 0.0, 1.0)), vertexDistance, FogStart, FogEnd, FogColor);
}
