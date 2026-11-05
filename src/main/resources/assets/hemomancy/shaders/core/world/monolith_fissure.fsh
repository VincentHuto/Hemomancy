#version 150

#moj_import <fog.glsl>

// The Sanguine Monolith cracking and split open, in the manner of the ring door's cracks: thin jagged lines through the
// stone, dark at the lips with a light inside, opening behind a ragged front that climbs from the base with Progress.
// UV is slab space in blocks. Vertex red is nearness to the break line (1 on it, 0 at the stone's far edge), so the main
// fault and its faint red hue follow the jagged break the geometry really has. Vertex green shades a facet, and vertex
// blue picks the layer: 0 for the slab's faces, 1 for the raw surface of the break, 0.5 for surfaces that take only an
// Apotheos's infestation and never a crack (the slab's outer sides and the rubble).
//
// The cracks are cut into the stone, like the rite floor's trench: each crack is a V-shaped groove, and a pixel inside
// one marches the view ray down it until it meets a wall. Near the lip the wall is scorched dark, deeper it runs crimson
// then arterial, and at the bottom it is blood-hot. Moving the camera shows a different wall.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float Progress;
uniform float StoneSeed;
uniform float Overgrown;
uniform float Fill;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 rayDir;

out vec4 fragColor;

#moj_import <hemomancy:noise/vein_hash3.glsl>
#moj_import <hemomancy:noise/vein_hash1.glsl>
#moj_import <hemomancy:noise/vein_noise.glsl>
#moj_import <hemomancy:noise/vein_fbm.glsl>
#moj_import <hemomancy:noise/vein_cellular.glsl>

void over(inout vec3 colour, inout float alpha, vec3 top, float topAlpha) {
    float combined = 1.0 - (1.0 - alpha) * (1.0 - topAlpha);
    colour = (colour * alpha * (1.0 - topAlpha) + top * topAlpha) / max(combined, 0.0001);
    alpha = combined;
}

// Distance in slab pixels to the nearest crack of the web.
float webGap(vec2 px, float scale) {
    vec2 jag = vec2(fbm(vec3(px * 0.21, StoneSeed + 61.0)), fbm(vec3(px * 0.21, StoneSeed + 67.0))) - 0.5;
    vec2 cells = cellular(vec3((px + jag * 5.0) * scale, StoneSeed * 5.0));
    return cells.y * 0.5 / scale;
}

// The wall of a groove at depth t (0 the lip, 1 the bottom), in bands like the strata of the stone. Where the stone is
// infected the crack keeps its red mouth and runs to orange and amber further down; elsewhere it stays blood red.
vec3 grooveWall(vec2 px, float t, float infected) {
    vec3 scorch = vec3(0.035, 0.002, 0.003);
    vec3 crimson = vec3(0.50, 0.008, 0.012);
    vec3 arterial = mix(vec3(0.95, 0.025, 0.012), vec3(0.95, 0.24, 0.035), infected);
    vec3 hot = mix(vec3(1.0, 0.13, 0.05), vec3(1.0, 0.58, 0.09), infected);
    vec3 wall = mix(scorch, crimson, smoothstep(0.08, 0.42, t));
    wall = mix(wall, arterial, smoothstep(0.38, 0.72, t));
    wall = mix(wall, hot, smoothstep(0.72, 1.0, t));
    float strata = 0.62 + 0.76 * smoothNoise(vec3(px * 1.1, t * 7.0 + StoneSeed * 9.0));
    return wall * strata;
}

void main() {
    float p = clamp(Progress, 0.0, 1.0);
    if (p <= 0.0) discard;
    float near = clamp(vertexColor.r, 0.0, 1.0);
    bool rawBreak = vertexColor.b > 0.75;
    bool crustOnly = vertexColor.b > 0.25 && !rawBreak;
    vec2 px = texCoord0 * 16.0;
    vec3 body = vec3(px, StoneSeed * 37.0);

    // The front climbs from the middle of the base, ragged, and has passed the top corners by full progress.
    float radius = pow(p, 0.85) * 46.0;
    float front = length(px * vec2(1.4, 1.0)) + (fbm(body * 0.14) - 0.5) * 12.0;
    float growth = rawBreak ? 1.0 : 1.0 - smoothstep(radius - 2.5, radius, front);
    if (growth < 0.002) discard;
    float behind = rawBreak ? 40.0 : max(radius - front, 0.0);
    float opening = smoothstep(0.5, 12.0, behind) * (0.35 + 0.65 * p);
    float t = HemoTime;
    float beat = 0.86 + 0.14 * sin(t * 0.13 + StoneSeed * 6.0);
    // The light in the cracks has climbed Fill of the way up the halves (2 blocks of slab, with a soft margin). Below
    // its ragged front the cracks run blood-hot; above it, and all over a shut husk, they are dark scorched grooves with
    // the faintest ember left in them.
    float fillHeight = Fill * 2.4 + (fbm(vec3(px.x * 0.3, StoneSeed * 11.0, 2.0)) - 0.5) * 0.5;
    float lit = (1.0 - smoothstep(fillHeight - 0.35, fillHeight + 0.05, texCoord0.y)) * smoothstep(0.0, 0.04, Fill);
    float ember = mix(0.05, 1.0, lit);

    // The web: only some of its cracks are open, more of them near the break, and each is its own width.
    float scale = rawBreak ? 0.3 : 0.17;
    float open = smoothstep(0.36, 0.52, smoothNoise(vec3(px * 0.13, StoneSeed + 51.0)));
    open *= rawBreak ? 1.0 : mix(0.3, 1.0, smoothstep(0.1, 0.75, near));
    float girth = 0.45 + 1.1 * smoothNoise(vec3(px * 0.09, StoneSeed + 83.0));
    float webWidth = crustOnly ? 0.0005 : max((0.07 + 0.3 * opening) * girth * open * growth * (rawBreak ? 0.45 : 1.0), 0.0005);
    float web = webGap(px, scale);

    // The main fault on the break line, its width wandering; it stands open as the stone gives.
    float lineGap = (1.0 - near) * 8.0;
    float gap = rawBreak ? 1000.0 : max(lineGap + (smoothNoise(body * 0.9) - 0.5) * 0.35 * smoothstep(0.0, 0.5, 1.0 - near), 0.0);
    float faultWidth = rawBreak || crustOnly ? 0.0005
            : max((0.18 + 0.42 * p) * (0.7 + 0.6 * smoothNoise(vec3(px.y * 0.5, StoneSeed, 3.0))) * growth, 0.0005);

    float depthAt = min(web / webWidth, gap / faultWidth);

    // An Apotheos's blood has started something in the stone: an infection in patches, seeded along the cracks and up
    // from the base, with most of the stone left bare. The cracks inside a patch take its colour.
    vec3 s3 = vec3(StoneSeed * 731.0, StoneSeed * 419.0, StoneSeed * 257.0);
    float field = 0.0;
    float infected = 0.0;
    if (Overgrown > 0.5) {
        float crackNear = max(exp(-web * 0.55) * open * opening, rawBreak ? 0.0 : exp(-gap * 0.35));
        float rising = 1.0 - smoothstep(0.1, 1.3, texCoord0.y);
        field = fbm(body * 0.17 + s3 + 21.0) + crackNear * 0.09 + rising * 0.05;
        infected = smoothstep(0.46, 0.66, field);
    }

    // The stone around a crack: bloom from the light inside, then a scorched lip right at the edge.
    float reach = rawBreak || crustOnly ? 0.0 : exp(-gap * 0.5) * (0.55 + 0.45 * fbm(body * 0.35)) * growth * smoothstep(0.1, 0.8, p);
    float bloom = crustOnly ? 0.0 : exp(-web * 1.15) * opening * open * growth;
    vec3 rgb = vec3(0.0);
    float alpha = 0.0;
    vec3 rawHeat = mix(vec3(0.30, 0.008, 0.008), vec3(0.30, 0.065, 0.012), infected);
    vec3 reachLight = mix(vec3(0.62, 0.012, 0.01), vec3(0.62, 0.10, 0.018), infected);
    vec3 bloomLight = mix(vec3(0.70, 0.022, 0.012), vec3(0.72, 0.19, 0.026), infected);
    if (rawBreak) {
        // The inside of the stone: raw, hot from within, every facet catching it differently.
        float heat = 0.45 + 0.55 * fbm(body * 0.4 + vec3(0.0, t * 0.006, 0.0));
        over(rgb, alpha, rawHeat * heat * vertexColor.g * ember, 0.94);
    }
    if (Overgrown > 0.5) {
        // Over the patch: clustered colonies of crust, soft-edged and filled, with no outlines. Deep red where one thins
        // out, orange through its body and ochre at the heart of the thickest, flecked with tiny fruiting specks and a few
        // fine hairs creeping ahead of it. All partly clear, so the stone shows through between and under them.
        float crust = fbm(body * 0.8 + s3);
        vec2 colonyWarp = vec2(fbm(body * 0.19 + s3 + 13.0), fbm(body * 0.19 + s3 + 29.0)) - 0.5;
        vec2 colonies = cellular(vec3((px + colonyWarp * 6.0) * 0.21, s3.y * 0.1));
        float colony = 1.0 - smoothstep(0.16, 0.66, colonies.x + (crust - 0.5) * 0.55);
        float cover = infected * colony;
        vec3 tint = mix(vec3(0.30, 0.030, 0.012), vec3(0.70, 0.25, 0.028), smoothstep(0.08, 0.5, cover));
        tint = mix(tint, vec3(0.84, 0.60, 0.12), smoothstep(0.55, 0.95, cover) * (0.4 + 0.6 * crust));
        over(rgb, alpha, vec3(0.20, 0.028, 0.010), infected * 0.24);
        over(rgb, alpha, tint, cover * (0.5 + 0.38 * crust));
        vec2 specks = cellular(vec3(px * 0.34, s3.x * 0.1));
        float size = mix(0.05, 0.2, smoothNoise(vec3(px * 0.21, s3.y)));
        float speck = (1.0 - smoothstep(size * 0.55, size, specks.x)) * smoothstep(0.42, 0.82, infected);
        over(rgb, alpha, mix(vec3(0.90, 0.38, 0.05), vec3(0.93, 0.68, 0.16), smoothNoise(vec3(px * 0.5, s3.z))) * (0.8 + 0.2 * beat), speck * 0.85);
        float edge = smoothstep(0.42, 0.54, field) * (1.0 - smoothstep(0.58, 0.7, field));
        vec2 warpHair = vec2(fbm(body * 0.31 + s3 + 3.1), fbm(body * 0.31 + s3 + 7.7)) - 0.5;
        vec2 hairs = cellular(vec3((px + warpHair * 7.0) * 0.26, s3.x * 0.2 + 9.0));
        float hairGrain = fwidth(hairs.y);
        float hair = (1.0 - smoothstep(0.004, 0.012 + hairGrain, hairs.y)) * edge * smoothstep(0.46, 0.6, smoothNoise(vec3(px * 0.12, s3.z + 4.0)));
        over(rgb, alpha, vec3(0.62, 0.12, 0.022), hair * 0.34);
    }
    over(rgb, alpha, reachLight * beat, clamp(reach * 0.72 * ember, 0.0, 1.0));
    over(rgb, alpha, bloomLight * beat, clamp(bloom * 0.62 * ember, 0.0, 1.0));
    float lip = crustOnly ? 0.0 : smoothstep(2.3, 1.0, depthAt) * max(open * opening, rawBreak ? 0.0 : growth);
    over(rgb, alpha, vec3(0.012, 0.001, 0.002), lip * 0.85);

    if (depthAt < 1.0 && !crustOnly) {
        // The basis of the face in slab pixels, from how the position and UV change across the screen.
        vec2 dx = dFdx(px);
        vec2 dy = dFdy(px);
        float det = dx.x * dy.y - dx.y * dy.x;
        vec2 slide = vec2(0.0);
        vec2 lineSlope = vec2(0.0);
        if (abs(det) > 1e-7) {
            vec3 tx = (dFdx(rayDir) * dy.y - dFdy(rayDir) * dx.y) / det;
            vec3 ty = (dFdy(rayDir) * dx.x - dFdx(rayDir) * dy.x) / det;
            vec3 view = normalize(rayDir);
            vec3 normal = normalize(cross(tx, ty));
            float into = max(abs(dot(view, normal)), 0.28);
            // Walls are 3 pixels deep at the widest crack, shallower at a thin one.
            float groove = 1.4 + 1.6 * clamp(max(webWidth, faultWidth), 0.0, 1.0);
            slide = vec2(dot(view, normalize(tx)), dot(view, normalize(ty))) / into * groove;
            float gx = dFdx(lineGap);
            float gy = dFdy(lineGap);
            lineSlope = vec2(gx * dy.y - gy * dx.y, gy * dx.x - gx * dy.x) / det;
        }

        // Down the groove until the ray meets a wall, which is nearer the deeper it goes.
        const int STEPS = 8;
        float hit = 1.0;
        bool walled = false;
        for (int i = 1; i <= STEPS; i++) {
            float at = float(i) / float(STEPS);
            vec2 offset = slide * at;
            float here = min(webGap(px + offset, scale) / webWidth, max(gap + dot(lineSlope, offset), 0.0) / faultWidth);
            if (here > 1.0 - at) {
                hit = at;
                walled = true;
                break;
            }
        }
        vec2 floorAt = px + slide * hit;
        vec3 inside = grooveWall(floorAt, hit, infected);
        if (!walled) {
            // The bottom: blood-hot and crawling, its brightest threads paling toward white.
            float flow = fbm(vec3(floorAt * vec2(0.5, 0.22) - vec2(0.0, t * 0.05), StoneSeed + t * 0.012));
            float vein = smoothstep(0.42, 0.7, flow);
            inside = mix(vec3(1.0, 0.07, 0.03), vec3(1.0, 0.46, 0.34), vein * vein) * (0.85 + 0.25 * vein);
            inside = mix(inside, mix(vec3(1.0, 0.30, 0.04), vec3(1.0, 0.72, 0.20), vein * vein) * (0.85 + 0.25 * vein), infected);
        }
        inside *= beat * (0.72 + 0.4 * p) * ember;
        // Antialias the lip: the groove fades in over the last of its width.
        over(rgb, alpha, inside, smoothstep(1.0, 0.82, depthAt));
    }

    // Over the mound vertex green is its facet's light, so the hyphae keep the shape of the rocks beneath them.
    if (Overgrown > 0.5 && !rawBreak) rgb *= vertexColor.g;
    alpha *= vertexColor.a * ColorModulator.a;
    if (alpha < 0.01) discard;
    fragColor = linear_fog(vec4(rgb * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
