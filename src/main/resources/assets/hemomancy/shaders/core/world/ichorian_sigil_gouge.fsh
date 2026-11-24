#version 150

#moj_import <fog.glsl>

// An Ichorian Sigil link cut into the ground (CardinalRiteBoundaryRenderer.drawSigilGouges). Across the strip: the cut
// down its middle, a torn and bruised lip, then a skirt of ground that stays clear until the sigil wakes and veins
// creep out of the lip. The cut is not geometry: each pixel follows the view ray down into a V-shaped trench, so its
// walls, its floor and the blood that fills it shift with the camera like a real hole.
//
// Vertex rgb is the sigil's ichor colour and alpha the rite's fade. UV0 x runs -1 (left edge) to +1 (right edge),
// y is the distance down the link in blocks. The stage values (cut-in, fill, glow, vein reach) come from the vertex
// shader; the glow is worked out per cross-section on the CPU, so a front runs the pattern node to node.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;

in float vertexDistance;
in vec4 vertexColor;
in vec2 stripUv;
in vec4 stage;
in vec3 viewOffset;
in float linkSeed;
in float linkAlong;
in vec2 acrossDir;

out vec4 fragColor;

#moj_import <hemomancy:noise/value_hash.glsl>
#moj_import <hemomancy:noise/material_noise.glsl>
#moj_import <hemomancy:noise/material_fbm.glsl>

// Matching IchorianSigilGougeGeometry: half the strip, half the cut; the lip runs out to LIP_OUTER.
const float STRIP_HALF = 0.90;
const float CUT_HALF = 0.16;
const float LIP_OUTER = 0.26;
// How deep the cut is at full width, in blocks.
const float CUT_DEPTH = 0.30;
// The floor of the V is this share of the lip's width.
const float FLOOR_SHARE = 0.45;
// How far up the cut the blood rises when full: its share of the cut's depth.
const float POOL_RISE = 0.55;
// Ground veins: one root at most every VEIN_SPACING blocks down the lip, on each side.
const float VEIN_SPACING = 0.55;
const float VEIN_MAX_ALPHA = 0.5;

float sq(float x) {
    return x * x;
}

// Where two links meet at a node their strips end on one shared slanted edge (the renderer mitres them), and each
// link's own tearing and grain settle to their mean over this many blocks before it, so the cut, its walls and its
// blood carry across the joint with nothing to show where one link stops.
const float SEAM_CALM_LENGTH = 0.4;
// 0 at a shared node, 1 clear of it. Set once per pixel in main.
float seamCalm = 1.0;

// A pattern's loose end (a node only one link reaches) draws out to a point over this many blocks.
const float END_TAPER_LENGTH = 0.75;
// How wide the cut is here for being near a loose end: 1 everywhere else. Set once per pixel in main.
float endScale = 1.0;

// 0 at a loose end's very tip, easing to 1 at END_TAPER_LENGTH: a blade's point, not a blunt wedge.
float pointTaper(float fromTip) {
    float s = clamp(fromTip / END_TAPER_LENGTH, 0.0, 1.0);
    return s * (2.0 - s);
}

// How open the link is here: full width from node to node, pinched only toward the front of a cut still opening.
float linkTaper(float along, float cutIn) {
    return cutIn >= 0.999 ? 1.0 : smoothstep(0.0, 0.08, cutIn - along);
}

// The cut's half-width where it passes the point t blocks down the link. It is read on the centre line, never at the
// pixel being shaded, so every view ray meets the same torn walls and they stay put as the camera moves.
float cutEdge(float t, float taper) {
    float tear = 0.82 + 0.36 * fbm(vec2(t * 2.3, linkSeed * 17.0 + 4.0))
            + (noise(vec2(t * 9.0, linkSeed * 11.0 + 2.0)) - 0.5) * 0.2;
    return CUT_HALF * mix(1.0, tear, seamCalm) * mix(0.3, 1.0, taper) * endScale;
}

// How far down (0 at the lip, 1 at the floor) a ray entering at across meets the wall on the given side.
float wallDepth(float across, float sink, float edge, float sideSign) {
    return clamp((sideSign * edge - across) / (sink + sideSign * edge * (1.0 - FLOOR_SHARE) + 1.0e-4), 0.0, 1.0);
}

// A pixel-steady thin line: 1 within width of the path, fading over a pixel or so.
float thread(float gap, float width) {
    float aa = max(fwidth(gap), 1.0e-4);
    return 1.0 - smoothstep(width, width + aa * 1.2, gap);
}

// The ground veins on one side of the link, at beyond blocks past the lip. Each root sits somewhere along its stretch of
// lip, leans a little, writhes slowly and may fork once; it thins and fades to nothing at its own length.
float groundVeins(float t, float beyond, float sideSign, float reach) {
    float best = 0.0;
    float base = floor(t / VEIN_SPACING);
    for (int i = -1; i <= 1; i++) {
        float k = base + float(i);
        vec2 key = vec2(k, sideSign * 7.0 + linkSeed * 31.0);
        float pick = hash(key);
        // Some stretches of lip put out no vein, so most of the skirt stays clear.
        if (pick < 0.45) continue;
        float veinLength = reach * (0.55 + 0.45 * hash(key + 9.3));
        if (beyond > veinLength) continue;
        float root = (k + 0.2 + 0.6 * hash(key + 3.1)) * VEIN_SPACING;
        float lean = (hash(key + 5.7) - 0.5) * 0.7;
        float writhe = (noise(vec2(beyond * 3.0 + k * 1.7, HemoTime * 0.03 + pick * 10.0)) - 0.5) * 0.45;
        float path = root + (lean + writhe) * beyond;
        float life = 1.0 - beyond / max(veinLength, 1.0e-3);
        float width = 0.006 + 0.014 * life;
        float vein = thread(abs(t - path), width);
        float forkAt = veinLength * (0.3 + 0.3 * hash(key + 12.4));
        if (beyond > forkAt) {
            float turn = hash(key + 15.1) > 0.5 ? 0.7 : -0.7;
            float fork = path + (beyond - forkAt) * turn;
            vein = max(vein, thread(abs(t - fork), width * 0.6) * smoothstep(veinLength, veinLength * 0.6, beyond));
        }
        vein *= smoothstep(veinLength, veinLength * 0.7, beyond);
        best = max(best, vein);
    }
    return best;
}

void main() {
    vec3 ichor = vertexColor.rgb;
    float fade = vertexColor.a;
    float cutIn = clamp(stage.x, 0.0, 1.0);
    float fill = clamp(stage.y, 0.0, 1.0);
    float glow = clamp(stage.z, 0.0, 1.0);
    // The vein reach also carries, in steps of 8, which of the link's ends are loose ends of the pattern (1 its
    // first node, 2 its last). A loose end tapers to a point; any other end meets a neighbouring link.
    float looseEnds = floor(stage.w / 8.0 + 0.001);
    bool looseStart = mod(looseEnds, 2.0) > 0.5;
    bool looseEnd = looseEnds > 1.5;
    float veinReach = clamp(stage.w - looseEnds * 8.0, 0.0, 1.0);
    float along = clamp(linkAlong, 0.0, 1.0);
    float t = stripUv.y;
    float across = stripUv.x * STRIP_HALF;
    float acrossAbs = abs(across);
    if (looseStart) {
        endScale *= pointTaper(t);
    } else {
        seamCalm *= smoothstep(0.0, SEAM_CALM_LENGTH, t);
    }
    if (along > 0.5) {
        // t / along is the link's length, steady in its far half, so this is the distance left to its last node.
        float toEnd = t * (1.0 - along) / along;
        if (looseEnd) {
            endScale *= pointTaper(toEnd);
        } else {
            seamCalm *= smoothstep(0.0, SEAM_CALM_LENGTH, toEnd);
        }
    }

    // The cut opens down the link from the earlier node; its front is ragged rather than ruled.
    float ragged = (noise(vec2(across * 14.0, linkSeed * 23.0)) - 0.5) * 0.05;
    if (along > cutIn + ragged && cutIn < 0.999) {
        discard;
    }
    float taper = linkTaper(along, cutIn);
    float edge = cutEdge(t, taper);
    float lipOuter = edge + (LIP_OUTER - CUT_HALF) * mix(0.4, 1.0, taper) * endScale;
    vec3 hot = mix(ichor, vec3(1.0, 0.86, 0.78), 0.55);
    vec2 alongDir = vec2(acrossDir.y, -acrossDir.x);

    vec3 color = vec3(0.0);
    float alpha = 0.0;
    if (acrossAbs < edge) {
        // Follow the view ray down into the cut. Its sideways part decides which wall it meets or whether it reaches
        // the floor; its part along the link moves which stretch of wall is seen, held short so a long reach at a
        // grazing angle never carries the walls out of the straight run they follow.
        vec3 ray = normalize(viewOffset);
        float steep = max(-ray.y, 0.15);
        // The cut shallows as it narrows to a loose end's point.
        float depth = CUT_DEPTH * mix(0.4, 1.0, taper) * mix(0.25, 1.0, endScale);
        if (fill > 0.01) {
            // Blood risen from the floor to a little under the lip, leaving a band of dark wall above it so the pool
            // reads as sunk in the ground; the V widens as it rises, so the pool does too.
            float surfaceDepth = depth * (1.0 - POOL_RISE * fill);
            vec2 toSurface = ray.xz / steep * surfaceDepth;
            float poolAcross = across + dot(acrossDir, toSurface);
            float poolT = t + clamp(dot(alongDir, toSurface), -0.5, 0.5);
            float poolHalf = edge * (FLOOR_SHARE + (1.0 - FLOOR_SHARE) * (1.0 - surfaceDepth / max(depth, 1.0e-3)));
            if (abs(poolAcross) < poolHalf) {
                float flow = mix(0.5, fbm(vec2(poolT * 2.6 - HemoTime * 0.035, poolAcross * 7.0 + linkSeed * 13.0)),
                        seamCalm);
                float sheen = pow(noise(vec2(poolT * 5.0 + HemoTime * 0.02, poolAcross * 11.0)), 4.0);
                // Deep blood: the ichor's colour held dark, with slow flow and a glint of sheen.
                vec3 blood = ichor * (0.13 + 0.17 * flow) + hot * sheen * 0.12;
                // The glow lights the blood from within along its core. It rises as the cube of the glow, so the
                // settled pulse stays a low ember and the completion's front is the bold beat.
                float core = exp(-sq(poolAcross / max(poolHalf, 1.0e-3)) * 2.2);
                blood += hot * glow * glow * glow * (0.5 + 0.5 * flow) * (0.3 + 0.9 * core) * 1.6;
                // The meniscus darkens where the blood meets the walls.
                blood *= 1.0 - 0.45 * smoothstep(poolHalf * 0.7, poolHalf, abs(poolAcross));
                fragColor = linear_fog(vec4(blood * ColorModulator.rgb, fade * ColorModulator.a),
                        vertexDistance, FogStart, FogEnd, FogColor);
                return;
            }
        }
        vec2 drop = ray.xz / steep * depth;
        float sink = dot(acrossDir, drop);
        float shift = clamp(dot(alongDir, drop), -0.5, 0.5);
        float floorAcross = across + sink;
        float floorHalf = cutEdge(t + shift, taper) * FLOOR_SHARE;
        // The floor: dry clot in shadow, the ichor's colour barely there.
        float floorT = t + shift;
        vec3 bottom = ichor * (0.06 + 0.08 * fbm(vec2(floorT * 6.0, floorAcross * 9.0 + linkSeed * 5.0)));
        // The wall the ray meets, refined at the stretch of cut it actually reaches.
        float sideSign = floorAcross > 0.0 ? 1.0 : -1.0;
        float wallT = wallDepth(across, sink, edge, sideSign);
        wallT = wallDepth(across, sink, cutEdge(t + shift * wallT, taper), sideSign);
        float wallAlong = t + shift * wallT;
        float strata = mix(0.5, fbm(vec2(wallAlong * 4.0, wallT * 6.0 + sideSign * 3.0 + linkSeed * 7.0)), seamCalm);
        vec3 wall = mix(vec3(0.02, 0.008, 0.01), ichor * 0.1, wallT) * (0.6 + 0.8 * strata);
        // The ground overhangs the cut a little: a shadow tucked under the lip.
        wall *= 0.55 + 0.45 * smoothstep(0.0, 0.12, wallT);
        // A thin wet sheen on the bare walls while the cut is dry.
        float wet = pow(noise(vec2(wallAlong * 7.0, wallT * 10.0 + linkSeed * 19.0)), 6.0) * (1.0 - fill);
        wall += mix(ichor, vec3(1.0), 0.35) * wet * 0.35;
        // Blood below throws its glow up the walls, strongest nearest the surface.
        wall += hot * glow * glow * fill * pow(smoothstep(0.0, max(1.0 - POOL_RISE * fill, 0.05), wallT), 2.0) * 0.6;
        float onFloor = smoothstep(floorHalf * 1.15, floorHalf * 0.85, abs(floorAcross));
        color = mix(wall, bottom, onFloor);
        alpha = 1.0;
    } else if (acrossAbs < lipOuter) {
        // The lip: torn ground pushed up and bruised, darkest at the break, fading into the ground outward.
        float lipShare = (acrossAbs - edge) / max(lipOuter - edge, 1.0e-3);
        float bruise = mix(0.5, fbm(vec2(t * 5.0, acrossAbs * 8.0 + linkSeed * 9.0)), seamCalm);
        vec3 lip = mix(vec3(0.035, 0.008, 0.016), ichor * 0.22, bruise * (1.0 - lipShare));
        // The raised rim catches a hairline of light right at the break.
        lip += ichor * 0.18 * exp(-sq((acrossAbs - edge) / 0.018));
        color = lip;
        alpha = smoothstep(1.0, 0.0, lipShare) * 0.85;
        // Crumbs of torn ground scattered over the lip.
        float crumb = step(0.74, noise(vec2(t * 16.0, acrossAbs * 16.0 + linkSeed * 41.0)));
        color = mix(color, vec3(0.03, 0.012, 0.015), crumb * 0.8);
        alpha = max(alpha, crumb * 0.7 * smoothstep(1.0, 0.4, lipShare));
        // The glow spills a narrow way over the lip and no further.
        float spill = glow * glow * fill * exp(-(acrossAbs - edge) / 0.05);
        color += hot * spill * 0.6;
        alpha = max(alpha, spill * 0.7);
    } else {
        // Crumbs a little way beyond the lip.
        float beyond = acrossAbs - lipOuter;
        float crumb = step(0.8, noise(vec2(t * 16.0, acrossAbs * 16.0 + linkSeed * 41.0)))
                * smoothstep(0.1, 0.0, beyond);
        color = vec3(0.03, 0.012, 0.015);
        alpha = crumb * 0.55 * endScale;
        // The skirt: clear until the sigil wakes, then veins rooted at the lip creep outward.
        if (veinReach > 0.001) {
            float reach = veinReach * (STRIP_HALF - LIP_OUTER);
            float sideSign = across > 0.0 ? 1.0 : -1.0;
            float vein = groundVeins(t, beyond, sideSign, reach);
            // They thin out toward both link ends and are gone before the strip's edge.
            vein *= smoothstep(0.0, 0.12, along) * smoothstep(1.0, 0.88, along);
            vein *= smoothstep(STRIP_HALF, STRIP_HALF - 0.1, acrossAbs);
            if (vein > 0.001) {
                vec3 veinColor = mix(vec3(0.06, 0.01, 0.015), ichor * 0.55, 0.6) + hot * glow * 0.3;
                color = mix(color, veinColor, vein);
                alpha = max(alpha, vein * VEIN_MAX_ALPHA);
            }
        }
    }
    alpha *= fade;
    if (alpha <= 0.004) {
        discard;
    }
    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha * ColorModulator.a),
            vertexDistance, FogStart, FogEnd, FogColor);
}
