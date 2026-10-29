#version 150

// The consecrated ward over a Saint's mausoleum (MausoleumWardRenderer), and the same ward burning into an Apotheos
// who stands on hallowed ground (HallowedBurnLayer). One program in two vertex formats; WardBody picks the surface.
//
// On the stone, vertex red says what a quad is: 0 a ward face, 0.5 the threshold sheet, 1 a struck face outside the
// ward that only answers strikes. Green and blue seed a ward face's glyph (blue is the face's direction ordinal * 36);
// on the sheet, green is how hard it presses back on the viewer. UV0 is the world face coordinate * 0.37
// (BlockShellFaces), so a block is 0.37 of UV. The ward is quiet: thin threads and a sparse scatter of runes that
// breathe slowly, rise where the crosshair rests and fade out towards the edge of the drawn radius. A strike sends a
// ring out from where it landed and flashes the struck face, harder for blows that come quickly.
//
// On a body, vertex rgb is the ward colour and alpha how far the burn has taken; UV0 is the skin's. The hyphae under
// the skin char black and the ward's runes crawl up over them.

uniform sampler2D Sampler0; // The Abocipher font: 26 glyphs in a row, lowercase above uppercase.
uniform sampler2D Sampler1; // The burning body's skin, for its outline (body only).

uniform float HemoTime;
uniform vec3 WardColor;
uniform float WardRadius;
uniform vec3 PlayerOffset;
uniform vec3 FocusOffset;
// Up to four strikes: xyz where each landed (camera-relative, just off the struck face), w its life from 0 to 1.
uniform vec4 Strike0;
uniform vec4 Strike1;
uniform vec4 Strike2;
uniform vec4 Strike3;
// Each strike's flash strength (0 for an empty slot), and 1 where it was a visitor turned back at the threshold.
uniform vec4 StrikePower;
uniform vec4 StrikeKind;
uniform float WardBody;

in vec4 vertexColor;
in vec2 faceUv;
in vec3 surfacePos;

out vec4 fragColor;

#moj_import <hemomancy:noise/value_hash.glsl>
#moj_import <hemomancy:noise/material_noise.glsl>

const float BLOCK_UV = 0.37;

float thinLine(float d, float width) {
    return 1.0 - smoothstep(width, width * 2.5, d);
}

// A rune of the Abocipher font in the unit cell (y up), chosen by pick; only its red strokes, not the grey shadow.
float rune(vec2 cell, float pick) {
    if (cell.x < 0.0 || cell.x > 1.0 || cell.y < 0.0 || cell.y > 1.0) return 0.0;
    float letter = min(floor(pick * 26.0), 25.0);
    float row = step(0.5, fract(pick * 7.13)) * 0.5;
    vec4 glyph = texture(Sampler0, vec2((letter + cell.x) / 26.0, row + (1.0 - cell.y) * 0.5));
    return glyph.a * smoothstep(0.08, 0.3, glyph.r - glyph.g);
}

// One strike on this fragment: the ring running out across the stone in x, the flash of the struck face in y. A
// threshold strike blooms across the sheet where the visitor touched it instead of lighting one face.
vec2 strikeAt(vec4 strike, float power, float threshold) {
    if (power <= 0.0) return vec2(0.0);
    float life = clamp(strike.w, 0.0, 1.0);
    float left = 1.0 - life;
    vec3 d = surfacePos - strike.xyz;
    float dist = length(d);
    float reachOut = threshold > 0.5 ? WardRadius * 0.45 : WardRadius * 0.8;
    float radius = mix(0.4, reachOut, 1.0 - pow(left, 2.2));
    float ring = exp(-pow((dist - radius) / 0.45, 2.0)) * pow(left, 1.5) * power;
    float flash;
    if (threshold > 0.5) {
        flash = exp(-dist * 1.1) * 1.6 * pow(left, 2.0) * power;
    } else {
        // The struck face: everything within half a block of its centre, which on the stone is that face alone. It
        // takes the blow at its heart and burns along its edges, over a low fill.
        float box = max(abs(d.x), max(abs(d.y), abs(d.z)));
        float inside = 1.0 - smoothstep(0.47, 0.53, box);
        float core = exp(-dot(d, d) * 9.0);
        float rim = smoothstep(0.34, 0.5, box);
        flash = inside * (0.2 + 0.95 * core + 0.65 * rim) * pow(left, 3.0) * power;
    }
    return vec2(ring, flash);
}

vec2 strikes() {
    return strikeAt(Strike0, StrikePower.x, StrikeKind.x) + strikeAt(Strike1, StrikePower.y, StrikeKind.y)
            + strikeAt(Strike2, StrikePower.z, StrikeKind.z) + strikeAt(Strike3, StrikePower.w, StrikeKind.w);
}

void wardStone() {
    float kind = vertexColor.r;
    vec2 blockUv = faceUv / BLOCK_UV;
    float dist = length(surfacePos);
    vec2 hit = strikes();
    float ring = hit.x;
    float flash = hit.y;
    float alpha;
    float heat;

    if (kind < 0.25) {
        // A ward face: a rune on some blocks, a web of faint threads over all of them.
        vec2 seed = floor(vertexColor.gb * 255.0 + 0.5);
        float facing = seed.y / 36.0;
        vec2 cell = floor(blockUv);
        vec2 local = blockUv - cell;
        // North and east faces are seen with their U running leftwards; turn them so the runes read the right way.
        if (abs(facing - 2.0) < 0.5 || abs(facing - 5.0) < 0.5) local.x = 1.0 - local.x;
        float pick = hash(cell + seed * 0.731);
        float edgeFade = 1.0 - smoothstep(WardRadius * 0.35, WardRadius, dist);
        // The breath rolls out from the viewer, a few seconds a beat.
        float breath = 0.5 + 0.5 * sin(HemoTime * 0.045 - dist * 0.35 - pick * 1.5);
        vec3 toFocus = surfacePos - FocusOffset;
        float focus = exp(-dot(toFocus, toFocus) / 7.0);
        float web = noise(blockUv * 0.9 + seed * 0.37 + vec2(0.0, HemoTime * 0.0035));
        float threads = thinLine(abs(fract(web * 3.0) - 0.5), 0.03);
        float glyph = pick < 0.42 ? rune((local - 0.24) / 0.52, fract(pick * 13.7)) : 0.0;
        float lit = 0.12 + 0.35 * breath + 1.2 * focus + 2.0 * ring;
        alpha = (threads * 0.14 + glyph * 0.32) * lit * edgeFade + ring * 0.3 * edgeFade + flash * 0.75;
        heat = flash * 0.45 + ring * 0.25 + focus * 0.1;
    } else if (kind < 0.75) {
        // The threshold sheet: a faint veil hung with slowly falling rune columns, pressing back where the viewer
        // stands against it.
        float press = vertexColor.g;
        float column = floor(blockUv.x * 2.0);
        float columnPick = hash(vec2(column, 17.0));
        float fall = blockUv.y * 1.4 + HemoTime * (0.01 + columnPick * 0.012);
        vec2 cell = vec2(fract(blockUv.x * 2.0), fract(fall));
        float glyph = columnPick < 0.6 ? rune((cell - 0.15) / 0.7, hash(vec2(column, floor(fall)))) : 0.0;
        float veil = 0.5 + 0.5 * noise(blockUv * vec2(1.2, 0.35) + vec2(0.0, HemoTime * 0.01));
        vec3 toPlayer = surfacePos - PlayerOffset;
        float nearness = exp(-dot(toPlayer, toPlayer) / 2.2) * press;
        float sheetFade = 1.0 - smoothstep(16.0, 32.0, dist);
        alpha = (0.035 * veil + glyph * 0.1 + nearness * (0.22 + glyph * 0.5)) * sheetFade + flash * 0.6 + ring * 0.35;
        heat = flash * 0.5 + nearness * 0.2 + ring * 0.2;
    } else {
        // A struck face outside the ward: nothing until it is struck.
        alpha = flash * 0.75 + ring * 0.3;
        heat = flash * 0.45 + ring * 0.25;
    }
    vec3 colour = mix(WardColor, vec3(1.0), clamp(heat, 0.0, 0.75));
    fragColor = vec4(colour, clamp(alpha, 0.0, 0.85));
}

void wardBody() {
    vec4 skin = texture(Sampler1, faceUv);
    if (skin.a < 0.1) discard;
    float burn = vertexColor.a;
    vec2 px = faceUv * 64.0;
    // The hyphae under the skin, charred: a creeping web of black threads over a sooted body.
    float web = noise(px * 0.32 + vec2(0.0, HemoTime * 0.02));
    float fibre = thinLine(abs(fract(web * 4.0) - 0.5), 0.05);
    float soot = 0.3 + 0.35 * noise(px * 0.12 + 3.1);
    // The ward's runes crawl up over the body a few at a time, each flickering as it burns in.
    vec2 crawl = px / 4.0 + vec2(0.0, HemoTime * 0.05);
    vec2 cell = floor(crawl);
    float pick = hash(cell + 41.0);
    float flicker = 0.55 + 0.45 * sin(HemoTime * 0.3 + pick * 31.0);
    float glyph = pick < 0.3 ? rune(vec2(fract(crawl.x), 1.0 - fract(crawl.y)) * 1.2 - 0.1, fract(pick * 17.3)) * flicker
            : 0.0;
    vec3 charColour = vec3(0.04, 0.025, 0.02);
    vec3 ember = vec3(1.0, 0.36, 0.1);
    vec3 colour = mix(charColour, ember, fibre * 0.25 * (0.5 + 0.5 * sin(HemoTime * 0.2 + web * 12.0)));
    colour = mix(colour, min(vertexColor.rgb * 1.3, vec3(1.0)), glyph);
    float alpha = burn * max(soot + fibre * 0.5, glyph * 0.9);
    fragColor = vec4(colour, clamp(alpha, 0.0, 0.92));
}

void main() {
    if (WardBody > 0.5) {
        wardBody();
    } else {
        wardStone();
    }
}
