#version 150
#moj_import <fog.glsl>

// Saint Putriciel's growths (visual pass 2): bracket fungus, spore sacs and roots the rot pushes up out of the chamber
// floor and the bodies it strikes, and the clods they throw. Solid, lit by their own facets (the chamber is dark and
// the material carries its own light). U runs across a face in blocks, V along it from 0 at the root to 1 at the rim
// or the top. Five shapes:
//   0 CAP   a bracket's upper face: concentric growth zones in ochre, muted yellow and olive, a pale cream rim
//   1 GILL  its underside and edge: fine gills in mould green and cream
//   2 SAC   a spore sac's skin: olive and cream, veined, glistening; it tears open from the top as it bursts
//   3 ROOT  a root or knot: charcoal-olive bark with ochre fissures
//   4 CLOD  a clod of floor and rot
//   5 SLUDGE  rot sludge standing in a pool (revision 2026-11-06): U and V are its place about the pool's centre in
//             blocks (U / 4 + 0.95, V / 5.6 + 0.5), alpha its place out from the centre (0) to the rim (1), never the
//             formation. Red the char behind a burning front, green the gloss, blue the scum
// The vertex colour is state, never a tint: red the char (0 living, 1 burnt black and cold; embers glow in its
// cracks between), green the wet, blue the bloom (sporulating, the cream catching light). Alpha is the formation: a
// growth unfurls from its root (V above the front is not grown yet) and crumbles back from its rim. The rot is
// charcoal olive, olive, mould green, ochre, muted yellow and pale cream; only a burning growth shows the fire's
// crimson and pink-white, never green or yellow in its embers.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
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

const vec3 ROT_DARK = vec3(0.060, 0.062, 0.030);
const vec3 OLIVE = vec3(0.300, 0.310, 0.130);
const vec3 MOULD = vec3(0.320, 0.460, 0.210);
const vec3 OCHRE = vec3(0.580, 0.455, 0.185);
const vec3 YELLOW = vec3(0.700, 0.630, 0.330);
const vec3 CREAM = vec3(0.900, 0.845, 0.650);
const vec3 BARK = vec3(0.110, 0.105, 0.060);
const vec3 LOAM = vec3(0.105, 0.085, 0.055);
const vec3 SHEEN = vec3(0.780, 0.760, 0.560);
const vec3 SOOT = vec3(0.020, 0.016, 0.016);
const vec3 CHARCOAL = vec3(0.070, 0.060, 0.056);
const vec3 ASH = vec3(0.430, 0.412, 0.398);
const vec3 CRIMSON = vec3(0.840, 0.118, 0.180);
const vec3 HEAT = vec3(1.000, 0.660, 0.745);
// the sludge: near-black olive deep, lifting at the shallows, whitish olive gloss, cream scum, an ochre meniscus, a cold
// sheen at a glancing look and the matte crust it dulls to as it settles
const vec3 SLUDGE_DEEP = vec3(0.102, 0.110, 0.055);
const vec3 SLUDGE_SHALLOW = vec3(0.188, 0.196, 0.102);
const vec3 GLOSS = vec3(0.769, 0.784, 0.659);
const vec3 SCUM = vec3(0.863, 0.816, 0.659);
const vec3 MENISCUS = vec3(0.549, 0.455, 0.220);
const vec3 COLD_SHEEN = vec3(0.560, 0.620, 0.640);
const vec3 MATTE_CRUST = vec3(0.200, 0.200, 0.110);

// The net of veins over a sac and the alligator cracks of a burnt growth: the gap between the nearest two cells.
float cellGap(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    float a = 8.0;
    float b = 8.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 key = cell + offset + seed * 0.41;
            vec2 centre = offset + vec2(hash(key), hash(key + vec2(13.7, 3.1)));
            float d = length(centre - f);
            if (d < a) {
                b = a;
                a = d;
            } else {
                b = min(b, d);
            }
        }
    }
    return b - a;
}

void main() {
    float charred = vertexColor.r;
    float wet = vertexColor.g;
    float bloom = vertexColor.b;
    float formation = vertexColor.a;
    vec3 n = normalize(cross(dFdx(viewPosition), dFdy(viewPosition)));
    vec3 view = normalize(-viewPosition);
    float key = max(0.0, dot(n, normalize(vec3(0.4, 0.82, 0.38))));
    float back = max(0.0, dot(-n, normalize(vec3(0.4, 0.82, 0.38))));
    float lambert = max(key, back * 0.8);
    float facing = abs(dot(n, view));
    float rim = pow(1.0 - facing, 3.0);
    // sixteen texels a block across a face
    vec2 g = vec2(uv.x * 16.0, uv.y * 12.0);
    float grain = noise(floor(g) + seed);

    // the growth front: unfurling from the root, crumbling back from the rim (the sludge's alpha is its place, not this)
    bool sludge = shape > 4.5;
    float threshold = uv.y * 0.86 + noise(vec2(uv.x * 7.0, uv.y * 3.0) + seed) * 0.14;
    if (!sludge && formation < 0.995 && threshold > formation) discard;

    vec3 colour;
    if (shape < 0.5) {
        // CAP: zoned like a bracket fungus, banded outward from the root, a pale rim
        float zone = floor(fract(uv.y * 4.2 + noise(vec2(uv.x * 1.6, uv.y * 2.0) + seed) * 0.7) * 4.0);
        vec3 band = zone < 0.5 ? OCHRE : (zone < 1.5 ? YELLOW * 0.9 : (zone < 2.5 ? OLIVE * 1.2 : CREAM * 0.8));
        band *= 0.85 + 0.25 * grain;
        band = mix(band, CREAM, smoothstep(0.84, 0.95, uv.y) * 0.85);
        colour = band * (0.38 + 0.72 * lambert);
    } else if (shape < 1.5) {
        // GILL: fine gills radiating from the root, dark slits between them
        float gill = abs(fract(uv.x * 14.0 + noise(vec2(uv.y * 4.0, seed)) * 0.4) - 0.5);
        vec3 tone = mix(MOULD * 0.65, CREAM * 0.7, smoothstep(0.1, 0.32, gill));
        tone = mix(tone, OCHRE * 0.6, smoothstep(0.8, 1.0, uv.y) * 0.5);
        colour = tone * (0.32 + 0.6 * lambert);
    } else if (shape < 2.5) {
        // SAC: veined skin, paler where it is stretched tight toward the top
        float vein = 1.0 - smoothstep(0.03, 0.1, cellGap(vec2(uv.x * 3.0, uv.y * 4.0)));
        vec3 skin = mix(OLIVE * 1.15, CREAM * 0.82, 0.25 + 0.45 * uv.y + 0.2 * grain);
        skin = mix(skin, ROT_DARK * 1.6, vein * 0.75);
        skin = mix(skin, CREAM, step(0.93, hash(floor(g) + seed + 2.0)) * 0.8);
        colour = skin * (0.4 + 0.7 * lambert);
        colour += CREAM * 0.35 * bloom * vein;
        // a torn edge where it split, wet and dark
        float tear = (1.0 - smoothstep(0.0, 0.06, formation - threshold)) * (1.0 - step(0.995, formation));
        colour = mix(colour, ROT_DARK, tear * 0.9);
    } else if (shape < 3.5) {
        // ROOT: furrowed bark
        float furrow = abs(fract(uv.x * 7.0 + noise(vec2(uv.y * 5.0, seed)) * 0.6) - 0.5);
        vec3 bark = mix(BARK, OLIVE * 0.8, smoothstep(0.08, 0.3, furrow) * (0.6 + 0.4 * grain));
        bark = mix(bark, OCHRE * 0.7, step(0.88, noise(vec2(uv.x * 3.0, uv.y * 9.0) + seed)) * 0.7);
        colour = bark * (0.4 + 0.7 * lambert);
    } else if (!sludge) {
        // CLOD: loam with mould in it
        vec3 earth = mix(LOAM, OLIVE * 0.8, grain * 0.5);
        earth = mix(earth, CREAM * 0.7, step(0.9, hash(floor(g) + seed)) * 0.6);
        colour = earth * (0.35 + 0.75 * lambert);
    } else {
        // SLUDGE: thick wet rot with body, on the floor's sixteen texels a block
        float place = formation;
        vec2 p = vec2((uv.x - 0.95) * 4.0, (uv.y - 0.5) * 5.6);
        vec2 q = floor(p * 16.0) / 16.0;
        float deep = 1.0 - smoothstep(0.15, 0.85, place);
        vec3 body = mix(SLUDGE_SHALLOW, SLUDGE_DEEP, deep);
        body *= 0.78 + 0.4 * noise(q * 1.7 + seed);
        // clots of near-black rot and threads of mould green in it, so it reads as rot with body, not murky water
        body = mix(body, ROT_DARK, smoothstep(0.55, 0.72, fbm(q * 1.1 + seed * 0.31)) * 0.75);
        body = mix(body, MOULD * 0.45, step(0.86, noise(q * 2.6 + seed + 3.0)) * step(0.5, hash(floor(p * 16.0) + seed + 4.0)) * 0.6);
        // slow drifting islands of cream scum where the bloom is high
        // (polish: islands smaller and fewer, so the pool stays dark rot with scum on it, not a pale dish)
        float scum = smoothstep(0.68, 0.76, fbm(q * 1.3 + vec2(HemoTime * 0.0035, seed * 0.13 - HemoTime * 0.002))) * bloom;
        body = mix(body, SCUM * 0.8, scum * 0.7);
        // the ochre meniscus, a texel or two wide at the lip, broken
        float band = smoothstep(0.82, 0.85, place) * (1.0 - smoothstep(0.9, 0.93, place));
        band *= step(0.35, hash(floor(p * 16.0) + seed));
        body = mix(body, MENISCUS * 0.75, band * 0.6 * (1.0 - scum));
        // dulled to a matte olive crust as the gloss goes (settling, or the fire drying it)
        vec3 crust = MATTE_CRUST * (0.8 + 0.4 * noise(q * 2.0 + seed));
        colour = mix(crust, body, wet) * (0.45 + 0.55 * lambert);
        // a sharp gloss off the key light that moves with the view, broken by the surface's texels into wet glints and
        // streaks (a smooth sheet read as a mirror), and a faint cold sheen at a glancing look
        vec3 bump = vec3(noise(q * 3.0 + seed) - 0.5, 0.0, noise(q * 3.0 + seed + 7.0) - 0.5) * 0.3
            + vec3(hash(floor(p * 16.0) + seed + 21.0) - 0.5, 0.0, hash(floor(p * 16.0) + seed + 33.0) - 0.5) * 0.35;
        vec3 sn = normalize(n + bump);
        float highlight = pow(max(0.0, dot(sn, normalize(normalize(vec3(0.4, 0.82, 0.38)) + view))), 140.0);
        float glint = 0.15 + 0.75 * step(0.52, noise(q * 2.3 + seed + 11.0));
        // a glancing look smears the highlight down the whole pool, so it is held back there
        colour += GLOSS * highlight * glint * (0.35 + 0.65 * facing) * wet;
        colour += COLD_SHEEN * pow(1.0 - facing, 4.0) * 0.1 * wet;
    }

    if (!sludge) {
        // wet: a glossy coat that catches a moving shine
        float shine = pow(max(0.0, dot(reflect(-view, n), normalize(vec3(0.2, 0.9, 0.3)))), 20.0);
        colour += SHEEN * wet * (shine * 0.7 + rim * 0.12);
        // bloom: the spores catching light along the edges
        colour += CREAM * bloom * (0.12 + 0.35 * rim) * (0.5 + 0.5 * grain);
        colour += mix(OCHRE, CREAM, 0.5) * rim * 0.18;
    }

    // char: the growth blackens, cracks like burnt bark, glows in its cracks while it burns, and goes to cold ash
    if (charred > 0.001) {
        float burnt = smoothstep(0.0, 0.55, charred);
        vec3 black = mix(SOOT, CHARCOAL * 1.4, 0.3 + 0.7 * lambert) * (0.8 + 0.3 * grain);
        black = mix(black, ASH * (0.5 + 0.5 * lambert), smoothstep(0.85, 1.0, charred) * step(0.55, grain));
        colour = mix(colour, black, burnt);
        // a burning pool's crust cracks into bigger curled plates than a growth's bark
        float crack = 1.0 - smoothstep(0.02, 0.07, cellGap(g * (sludge ? 0.32 : 0.45)));
        // embers only once the growth is black round them, so no ember texel mixes with the living colours
        float burning = smoothstep(0.45, 0.62, charred) * (1.0 - smoothstep(0.8, 0.96, charred));
        float flicker = 0.8 + 0.2 * sin(HemoTime * 0.5 + seed * 1.3 + uv.x * 7.0);
        colour = mix(colour, mix(CRIMSON, HEAT, burning * 0.5) * flicker, crack * burning);
    }

    fragColor = linear_fog(vec4(colour * ColorModulator.rgb, 1.0), vertexDistance, FogStart, FogEnd, FogColor);
}
