#version 150

// A Founding Fane seen from inside (FaneGroundRenderer): the ground within its bounds inscribed with Engram glyphs
// instead of a dome overhead. A sparse scatter of glyphs over a faint web of threads fills the interior, breathing
// slowly; a heartbeat rolls out from the heart every few seconds and lights what it passes; where the bounds meet the
// ground a close ring of glyphs runs along a bright cut line, so the edge reads at a glance.
//
// Vertex red is the corner's depth inside the bounds, (depth + 2) / 10 in blocks, so the cut line falls between
// blocks rather than on their edges; below 0 the fragment is outside and is discarded. The depth is the server's shape
// with each stake grown into the bounds through a rounded neck (FaneGroundGeometry). Vertex green is the quarter turn
// toward the centre of the lobe the face lies deepest in (0 +x, 0.25 +z, 0.5 -x, 0.75 -z), so the ring along a stake's
// edge reads outward from that stake. UV0 is the world x/z in blocks
// (wrapped every 4096), the glyph grid is two blocks a cell so the 32 px glyphs sit at 16 px a block.

uniform sampler2D Sampler0; // The Engram glyphs: 36 characters of 32 px in a row, white on clear.

uniform float HemoTime;
uniform vec3 FaneColor;
// xyz the heart's centre (camera-relative), w the heart radius.
uniform vec4 HeartOffset;
uniform vec3 PlayerOffset;
// x the viewer's relation (0 member, 1 rival elder, 2 outsider, 3 mundane outsider), y the fane's seed, z the drawn
// reach in blocks, w the whole layer's fade.
uniform vec4 FaneLook;

in vec4 vertexColor;
in vec2 faceUv;
in vec3 surfacePos;

out vec4 fragColor;

#moj_import <hemomancy:noise/value_hash.glsl>
#moj_import <hemomancy:noise/material_noise.glsl>

const float CELL = 2.0;
const float EDGE_BAND = 3.0;
const float HEARTBEAT_TICKS = 140.0;

// A hairline along the half-way contour of v, a pixel or so wide however far off or slanted the ground is, and gone
// where the contours crowd closer than a few pixels.
float hairline(float v) {
    float d = abs(fract(v) - 0.5);
    float w = fwidth(v);
    return (1.0 - smoothstep(w * 0.5, w * 1.5, d)) * (1.0 - smoothstep(0.12, 0.35, w));
}

// One Engram character in the unit cell (y up), chosen by pick.
float glyph(vec2 cell, float pick) {
    if (cell.x < 0.0 || cell.x > 1.0 || cell.y < 0.0 || cell.y > 1.0) return 0.0;
    float character = min(floor(pick * 36.0), 35.0);
    return texture(Sampler0, vec2((character + cell.x) / 36.0, 1.0 - cell.y)).a;
}

// Turns the cell a quarter at a time so the glyph's foot points at its lobe's centre: the ring reads outward, never
// askew. facing is 0 +x, 1 +z, 2 -x, 3 -z.
vec2 faceLobe(vec2 local, float facing) {
    vec2 p = local - 0.5;
    if (facing < 0.5) {
        p = vec2(p.y, -p.x);
    } else if (facing < 1.5) {
        p = -p;
    } else if (facing < 2.5) {
        p = vec2(-p.y, p.x);
    }
    return p + 0.5;
}

void main() {
    float depth = vertexColor.r * 10.0 - 2.0;
    if (depth < 0.0) discard;
    float relation = FaneLook.x;
    float seed = FaneLook.y;
    float dist = length(surfacePos);
    float reachFade = 1.0 - smoothstep(FaneLook.z * 0.7, FaneLook.z, dist);

    vec2 world = faceUv;
    vec2 cellId = floor(world / CELL);
    vec2 local = fract(world / CELL);
    float pick = hash(cellId + seed * 0.37);
    float facing = floor(vertexColor.g * 4.0 + 0.5);
    // Across the ground, as the fane's lobes are.
    float fromHeart = length(surfacePos.xz - HeartOffset.xz);

    // How hostile the ground feels: members see it calm, outsiders see it flicker.
    bool member = relation < 0.5;
    bool rival = relation > 0.5 && relation < 1.5;
    bool outsider = relation > 1.5 && relation < 2.5;
    bool mundane = relation > 2.5;

    // The heartbeat: a ring rolling out from the heart, then a long rest.
    float beat = fract(HemoTime / HEARTBEAT_TICKS + seed * 0.013);
    float front = beat * (HeartOffset.w + 6.0);
    float pulse = exp(-pow((fromHeart - front) / 2.2, 2.0)) * (1.0 - beat);
    if (rival) pulse *= 0.6 + 0.4 * sin(HemoTime * 0.045);

    // The edge: a ring of glyphs on every cell along the bounds and a bright cut where they meet the ground.
    float edge = 1.0 - smoothstep(EDGE_BAND - 0.6, EDGE_BAND, depth);
    float cut = exp(-depth * depth * 9.0);
    float cutGlow = exp(-depth * 1.4);

    // The breath rolls inward from the edge, a few seconds a beat.
    float breath = 0.5 + 0.5 * sin(HemoTime * 0.04 - depth * 0.3 - pick * 2.0);
    vec3 toPlayer = surfacePos - PlayerOffset;
    float feet = exp(-dot(toPlayer, toPlayer) / 9.0);

    float web = noise(world * 0.35 + seed + vec2(0.0, HemoTime * 0.003));
    float threads = hairline(web * 3.0);

    float density = member ? 0.22 : outsider ? 0.36 : rival ? 0.28 : 0.0;
    float onCell = edge > 0.01 ? 1.0 : step(pick, density);
    float character = fract(pick * 13.7 + floor(HemoTime / 600.0 + pick * 4.0) * 0.31);
    float mark = onCell > 0.5 ? glyph((faceLobe(local, facing) - 0.12) / 0.76, character) : 0.0;
    if (outsider) mark *= 0.65 + 0.35 * step(0.15, fract(HemoTime * 0.05 + pick * 9.0));

    float lit = 0.55 + 0.3 * breath + 1.2 * pulse + 0.7 * feet;
    float glyphAlpha = mark * mix(0.42, 0.62, edge) * lit;
    float threadAlpha = threads * (mundane ? 0.3 : 0.2) * (0.5 + lit);
    float edgeAlpha = cut * 0.75 + cutGlow * 0.18;
    float alpha = (glyphAlpha + threadAlpha + edgeAlpha + pulse * 0.05) * reachFade * FaneLook.w;

    float heat = cut * 0.6 + pulse * 0.35 + feet * 0.2 + mark * edge * 0.15;
    vec3 colour = mix(FaneColor, vec3(1.0, 0.86, 0.78), clamp(heat, 0.0, 0.7));
    fragColor = vec4(colour, clamp(alpha, 0.0, 0.85));
}
