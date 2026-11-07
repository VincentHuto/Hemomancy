#version 150
#moj_import <fog.glsl>

// Saint Velorum's Frozen Cruor standing up out of his floor (revision 2026-11-06): blood arrested where it stood, glassy
// oxblood with darker clots held inside it, black-red where it is thick, pale frost crusted on its points, its edges and
// its upper faces. Blades, splinters, slabs, javelins, spines, chips and the panes of a preserving shell, lit by their
// own facets (his chamber is dark and the cruor carries its own dull glow). Shape 0 is a shard face: U runs across it in
// blocks, V up it from 0 at the foot to 1 at the point. Shape 1 is a chip. Shape 2 is a shell pane and shape 3 a clear
// shard, both translucent and drawn in the clear pass. The vertex colour is state, never a tint: red the hoarfrost
// crusted on it (raised on up-facing faces by the geometry), green how thick the cruor is (0 thin and lit, 1 black-red),
// blue its glint (the frost light at its birth and at a commit). Alpha is the formation: V above the formation front is
// gone, so the cruor sublimes and crumbles from the point down. The body colours are red; blue lives only in the rim,
// the glint and the frost light. CruorGlow is the cruor's own glow: faint always, strong only in the Eclipse.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float CruorGlow;
in float vertexDistance;
in vec4 vertexColor;
in vec2 uv;
in vec3 viewPosition;
flat in float shape;
flat in float seed;
out vec4 fragColor;

#moj_import <hemomancy:noise/value_hash.glsl>
#moj_import <hemomancy:noise/material_noise.glsl>
#moj_import <hemomancy:noise/material_fbm.glsl>

// Frozen Cruor, R.2: thick cruor, the body, thin cruor with light behind it, the clots inside, the frost crust.
const vec3 CRUOR_DEEP = vec3(0.102, 0.020, 0.031);
const vec3 CRUOR = vec3(0.227, 0.055, 0.078);
const vec3 CRUOR_LIT = vec3(0.416, 0.110, 0.149);
const vec3 CLOT = vec3(0.071, 0.012, 0.039);
const vec3 HOAR = vec3(0.863, 0.910, 0.957);
// The only blue: frost light on the rims, the glints and the specular.
const vec3 FROST_LIGHT = vec3(0.604, 0.800, 0.941);
const vec3 FROST_CORE = vec3(0.910, 0.957, 1.000);
// The cruor's own dull glow.
const vec3 GLOW_LOW = vec3(0.353, 0.102, 0.133);
const vec3 GLOW_HIGH = vec3(0.541, 0.141, 0.188);

// The fracture planes inside the glass: x small along a fracture, y a value fixed per piece between them.
vec2 fractures(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    float a = 8.0;
    float b = 8.0;
    float id = 0.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 o = vec2(float(x), float(y));
            vec2 c = cell + o;
            vec2 centre = o + vec2(hash(c + seed * 0.23), hash(c + vec2(17.9, 29.3) + seed * 0.41));
            float d = length(centre - f);
            if (d < a) {
                b = a;
                a = d;
                id = hash(c + vec2(3.3, 8.7));
            } else {
                b = min(b, d);
            }
        }
    }
    return vec2(b - a, id);
}

// One to three clots held inside a face: dark nodules under the glass, never on its skin.
float clots(vec2 p) {
    float count = 1.0 + floor(hash(vec2(seed, 5.1)) * 3.0);
    float most = 0.0;
    for (int i = 0; i < 3; i++) {
        float k = float(i);
        if (k >= count) break;
        vec2 c = vec2(0.05 + 0.5 * hash(vec2(seed + k * 7.1, 1.3)), 0.12 + 0.6 * hash(vec2(seed + k * 3.3, 9.7)));
        float r = 0.06 + 0.07 * hash(vec2(seed + k, 4.4));
        float d = length((p - c) * vec2(1.0, 1.5)) + (noise(p * 18.0 + k) - 0.5) * 0.03;
        most = max(most, 1.0 - smoothstep(r * 0.55, r, d));
    }
    return most;
}

void main() {
    float rime = vertexColor.r;
    float thick = vertexColor.g;
    float glint = vertexColor.b;
    float formation = vertexColor.a;
    vec3 n = normalize(cross(dFdx(viewPosition), dFdy(viewPosition)));
    vec3 view = normalize(-viewPosition);
    if (dot(n, view) < 0.0) n = -n;
    float facing = clamp(dot(n, view), 0.0, 1.0);
    float fres = pow(1.0 - facing, 3.0);
    vec3 key = normalize(vec3(0.35, 0.85, 0.40));
    float lambert = max(dot(n, key), 0.0) * 0.75 + 0.25 * max(dot(n, normalize(vec3(-0.6, 0.2, 0.7))), 0.0);
    float spec = pow(max(dot(reflect(-view, n), key), 0.0), 36.0);
    // sixteen grains a block across a face and sixteen up a block-tall shard
    vec2 g = floor(vec2(uv.x * 16.0, uv.y * 16.0));
    float grain = hash(g + seed);

    float threshold = uv.y * 0.86 + noise(vec2(uv.x * 7.0, uv.y * 3.0) + seed) * 0.14;
    if (abs(shape - 1.0) < 0.5) threshold = 0.0;
    if (formation < 0.995 && threshold > formation) discard;

    // the glass: thin cruor lit from behind, thickening to black-red, darker streaks of thick blood deep inside
    vec3 body = mix(CRUOR_LIT, CRUOR_DEEP, thick);
    vec3 colour = mix(body * 0.42, mix(body, CRUOR_LIT * 1.2, 0.3 + 0.35 * (1.0 - thick)), lambert);
    colour = mix(colour, CRUOR_DEEP, smoothstep(0.45, 0.8, fbm(vec2(uv.x * 2.0, uv.y * 3.0) + seed)) * 0.55);
    vec2 f = fractures(vec2(uv.x * 3.0, uv.y * 4.0) + seed * 0.37);
    float crack = 1.0 - smoothstep(0.0, 0.05, f.x);
    colour = mix(colour, CRUOR_LIT * (0.9 + 0.4 * f.y), crack * 0.45);
    colour = mix(colour, CLOT, clots(uv) * 0.88);
    colour *= 0.9 + 0.14 * grain;
    // its skin: a frost-light specular and rim, never a blue body
    colour += FROST_CORE * spec * mix(0.45, 0.8, thick);
    colour += FROST_LIGHT * fres * 0.3;
    // hoarfrost: crusting the foot, the edges and the points, and the up-facing faces (raised in red by the geometry)
    float wobble = noise(g * 0.5 + seed);
    // polish round 1: a crust, never a coat. At eye height the old foot term frosted the lower half of every splinter
    // and the cruor read as clear white ice; now the foot is a ragged band, the point its last tenth, the edge a hairline
    float foot = smoothstep(0.0, 0.06, (0.1 + 0.12 * rime) - uv.y + (wobble - 0.5) * 0.08) * step(0.01, rime);
    float point = smoothstep(0.84, 0.94, uv.y + (wobble - 0.5) * 0.1) * (0.3 + 0.6 * rime);
    float edge = (1.0 - smoothstep(0.0, 0.025, uv.x + (wobble - 0.5) * 0.02)) * (0.15 + 0.45 * rime);
    float face = smoothstep(0.6, 0.98, rime) * smoothstep(0.4, 0.75, noise(g * 0.35 + seed * 1.7)) * 0.85;
    float crust = clamp(max(max(foot, point), max(edge, face)), 0.0, 1.0);
    colour = mix(colour, HOAR * (0.68 + 0.35 * lambert), crust);
    // the frost light inside it at its birth and at a commit
    colour += FROST_LIGHT * glint * (0.2 + 0.5 * (1.0 - uv.y));
    // the cruor's own glow: dull always, strong in the Eclipse, held under the crust
    float glow = clamp(CruorGlow, 0.0, 1.0);
    colour += mix(GLOW_LOW, GLOW_HIGH, glow) * glow * (1.0 - crust * 0.7) * (0.55 + 0.45 * crack + 0.3 * (1.0 - thick));
    // where it sublimes, the front frosts white
    float front = (1.0 - smoothstep(0.0, 0.06, formation - threshold)) * (1.0 - step(0.995, formation));
    colour = mix(colour, HOAR, front * 0.8);

    float alpha = 1.0;
    if (abs(shape - 2.0) < 0.5) {
        // a shell pane: the player's own blood frozen round them at about half alpha, so they show through dark red
        alpha = 0.55 + 0.25 * fres + crack * 0.12 + crust * 0.35 + glint * 0.15;
    } else if (shape > 2.5) {
        alpha = 0.62 + 0.3 * fres + crust * 0.3;
    }
    vec4 lit = vec4(colour * ColorModulator.rgb, clamp(alpha, 0.0, 1.0));
    // the cruor carries its own glow: it takes the fog at 0.35 strength, so the dark never hides an attack
    fragColor = mix(lit, linear_fog(lit, vertexDistance, FogStart, FogEnd, FogColor), 0.35);
}
