#version 150

// The Cardinal Rite daemon as a smoke volume, ray-marched inside its box: a round head on a teardrop body (the
// Humanity sprite, round from every side) of swirling red smoke, lit from above and shadowing itself, an ember
// breathing in its core, wrapped in a shell of black soot, with three Sanguine Monolith eyes on its face. In motion
// it lies along its path head first, its tail streaming out, coiling and fanning into a wake, and its smoke flowing
// back over it in streaks.

uniform float HemoTime;
uniform float HazeSeed;
uniform vec4 HazeState;  // emergence, strength, swallow flash, eyes open
uniform vec4 HazeTint;   // swallowed offering tint rgb, eye flare
uniform vec4 HazeLean;   // smoke trail x, y, z in world axes, recall progress
uniform vec3 HazeCamera; // camera relative to the daemon's centre, in blocks
uniform vec4 HazeShape;  // height in blocks, body yaw in radians, box half-width, box half-height
uniform float HazeHead;  // head yaw in radians, within half a turn of the body's
uniform vec4 HazeTilt;   // the body's tilt off upright: a level axis (world xyz) and the angle in radians
uniform vec2 HazeFlow;   // distance flowed in heights, and how far the tail streams (0 hovering, 1 at full speed)
uniform vec4 ColorModulator;

in vec3 localPos;

out vec4 fragColor;

const int MARCH_STEPS = 56;
// Streaming at full speed, the teardrop below the chest stretches this much longer, narrows by this much, and its
// end fans out by this much into a wake.
const float TAIL_STRETCH = 0.9;
const float TAIL_THIN = 0.35;
const float TAIL_FAN = 0.9;
// Empty space is crossed in strides of this share of the gap to where smoke could begin.
const float SKIP_SAFETY = 0.5;
// How far the head drifts ahead along the daemon's motion, as a share of its height per unit of trail.
const float HEAD_LEAD = 0.05;
// A fixed step in height units: short rays through the edge of the figure cost less than long ones through it.
const float STEP_HEIGHT = 0.022;
// Farther than this outside the figure there is neither smoke nor soot (nor a swelling lump, nor the fraying of a
// streaming outline), so the noise is skipped there.
const float EMPTY_DISTANCE = 0.27;
const float EXTINCTION = 16.0;
const float SOOT_EXTINCTION = 7.0;

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.zyx + 31.32);
    return fract((p.x + p.y) * p.z);
}

float valueNoise3(vec3 p) {
    vec3 cell = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);
    float n000 = hash13(cell);
    float n100 = hash13(cell + vec3(1.0, 0.0, 0.0));
    float n010 = hash13(cell + vec3(0.0, 1.0, 0.0));
    float n110 = hash13(cell + vec3(1.0, 1.0, 0.0));
    float n001 = hash13(cell + vec3(0.0, 0.0, 1.0));
    float n101 = hash13(cell + vec3(1.0, 0.0, 1.0));
    float n011 = hash13(cell + vec3(0.0, 1.0, 1.0));
    float n111 = hash13(cell + vec3(1.0, 1.0, 1.0));
    float lowerSheet = mix(mix(n000, n100, u.x), mix(n010, n110, u.x), u.y);
    float upperSheet = mix(mix(n001, n101, u.x), mix(n011, n111, u.x), u.y);
    return mix(lowerSheet, upperSheet, u.z);
}

float fbm3(vec3 p, int octaves) {
    float total = 0.0;
    float amp = 0.5;
    for (int octave = 0; octave < 4; octave++) {
        if (octave >= octaves) break;
        total += amp * valueNoise3(p);
        p = p * 2.03 + vec3(17.1, 9.2, 4.7);
        amp *= 0.5;
    }
    return total;
}

// Smooth minimum, so the head flows into the shoulders without a seam.
float softMin(float a, float b, float k) {
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
}

// The figure's signed distance in height units (foot at y 0, crown near 1, facing +z): the Humanity sprite's round
// head on a slim teardrop body with rounded shoulders, widest high at the chest and pinching sharply to its tip,
// revolved so it is round from every side.
float figure(vec3 f) {
    float head = length(f - vec3(0.0, 0.85, 0.0)) - 0.111;
    float tip = 0.03;
    float chest = 0.60;
    float crown = 0.79;
    float widest = 0.13;
    float below = clamp((f.y - tip) / (chest - tip), 0.0, 1.0);
    float teardrop = widest * pow(sin(below * 1.5707963), 1.5);
    float above = clamp((f.y - chest) / (crown - chest), 0.0, 1.0);
    float shoulders = widest * sqrt(max(0.0, 1.0 - above * above * above));
    float bodyWidth = f.y < chest ? teardrop : shoulders;
    float body = max(length(f.xz) - bodyWidth, max(tip - f.y, f.y - crown));
    return softMin(head, body, 0.02);
}

// One eye of the Sanguine Monolith, in the eye's own upright frame (unit = the Monolith's eye scale), made of
// smoke and light rather than paint: a soft dark socket shaped like the Monolith's almond, a dim lid line, a red
// iris whose radial spokes slowly turn, a dark pupil and a glint. Returns colour and coverage; `glow` is the light
// it spills into the haze around it.
vec4 monolithEye(vec2 e, float pulse, float glint, float spin, out float glow) {
    // The almond is a vesica: two circles a little apart, pointed at the top and bottom.
    float lens = max(length(e - vec2(-0.17, 0.0)), length(e - vec2(0.17, 0.0))) - 0.29;
    float socket = 1.0 - smoothstep(-0.05, 0.015, lens);
    float lidLine = (1.0 - smoothstep(0.0, 0.03, abs(lens))) * 0.6;
    glow = (1.0 - smoothstep(-0.05, 0.18, lens)) * pulse;

    vec2 irisAxis = e / vec2(0.088, 0.145);
    float irisDist = length(irisAxis);
    float iris = 1.0 - smoothstep(0.80, 1.0, irisDist);
    float turn = atan(irisAxis.y, irisAxis.x + 0.00001) + spin;
    float spokes = smoothstep(0.78, 0.96, cos(turn * 12.0)) * smoothstep(0.45, 0.58, irisDist)
            * (1.0 - smoothstep(0.84, 0.95, irisDist));
    float pupil = 1.0 - smoothstep(0.70, 1.0, length(e / vec2(0.040, 0.065)));
    float glintDot = 1.0 - smoothstep(0.5, 1.0, length((e - vec2(-0.014, 0.020)) / 0.013));

    vec3 eyeColour = vec3(0.02, 0.0, 0.004);
    eyeColour = mix(eyeColour, vec3(0.72, 0.04, 0.04) * pulse, iris);
    eyeColour = mix(eyeColour, vec3(0.98, 0.16, 0.07) * pulse, spokes * iris);
    eyeColour = mix(eyeColour, vec3(0.03, 0.0, 0.0), pupil);
    eyeColour = mix(eyeColour, vec3(1.0, 0.90, 0.82), glintDot * glint);
    eyeColour = mix(eyeColour, vec3(0.55, 0.04, 0.04) * pulse, lidLine * (1.0 - iris));
    return vec4(eyeColour, max(socket, lidLine) * 0.95);
}

// Rotates a point into an eye's upright frame.
vec2 eyeFrame(vec2 p, vec2 centre, float tilt, float size) {
    vec2 d = p - centre;
    float c = cos(tilt);
    float s = sin(tilt);
    return vec2(c * d.x + s * d.y, -s * d.x + c * d.y) / size;
}

// Slow, erratic swellings and hollows across the figure's surface, with a faster twitch over them.
float lumpsAt(vec3 f) {
    // In flight the swellings flow back along the body with the rest of the smoke.
    vec3 m = vec3(f.x * 4.0, f.y * 3.0 - HemoTime * 0.012 + HazeFlow.x * 1.8, f.z * 4.0) + HazeSeed * vec3(2.1, 5.7, 3.3);
    float slow = fbm3(m, 2);
    float twitch = valueNoise3(m * 2.3 + vec3(0.0, HemoTime * 0.04, 0.0));
    // The head stays mostly round, so the eyes keep their face.
    float headCalm = mix(1.0, 0.35, smoothstep(0.74, 0.80, f.y));
    return ((slow - 0.375) * 0.14 + (twitch - 0.5) * 0.03) * headCalm;
}

// Turns a point or direction about a unit axis (Rodrigues).
vec3 turnAbout(vec3 v, vec3 axis, float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return v * c + cross(axis, v) * s + axis * dot(axis, v) * (1.0 - c);
}

// A world point or direction in the frame of something turned by `yaw`, so it faces along +z.
vec3 unyaw(vec3 v, float yaw) {
    float c = cos(yaw);
    float s = sin(yaw);
    return vec3(v.x * c + v.z * s, v.y, -v.x * s + v.z * c);
}

// The body in motion, in the figure's frame. Hovering, its lower half only sways; streaming, its tail stretches out
// behind and narrows, a coil runs down it from the head (quicker the farther it has flowed), and its end fans out
// into a loose wake.
vec3 streaming(vec3 f) {
    float s = HazeFlow.y;
    float y = f.y < 0.6 ? 0.6 - (0.6 - f.y) / (1.0 + TAIL_STRETCH * s) : f.y;
    float tail = 1.0 - smoothstep(0.2, 0.7, y);
    float phase = y * 7.0 + HazeFlow.x * 2.5 + HemoTime * 0.03 + HazeSeed;
    float swing = (0.02 + 0.045 * s) * tail;
    vec2 coil = vec2(sin(phase), cos(phase) * 0.6) * swing;
    float fan = 1.0 - smoothstep(0.03, 0.3, y);
    float thin = (1.0 + TAIL_THIN * s * tail) / (1.0 + TAIL_FAN * s * fan);
    return vec3((f.x - coil.x) * thin, y, (f.z - coil.y) * thin);
}

// A box-local point (blocks) in the figure's frame: height units up from the foot, turned so its face looks along
// +z. The body is stood back up from its tilt along its path, turned with its own yaw, and the head with its quicker
// one, the smoke twisting between them; then the moving smoke streams.
vec3 toFigure(vec3 v) {
    vec3 upright = unyaw(turnAbout(v / HazeShape.x, HazeTilt.xyz, -HazeTilt.w), HazeShape.y);
    float up = upright.y + 0.5;
    // The head's quicker turn twists the upper body about its own axis.
    float twist = (HazeHead - HazeShape.y) * smoothstep(0.35, 0.80, up);
    float tc = cos(twist);
    float ts = sin(twist);
    return streaming(vec3(upright.x * tc + upright.z * ts, up, -upright.x * ts + upright.z * tc));
}

// A box-local point in the head's frame: the head's centre at (0, 0.85, 0) and its face looking along +z, the way the
// head is turned. The head rides the tilted body, but its face stays level, looking along its heading, so in flight
// the eyes look where it goes.
vec3 toHead(vec3 v) {
    vec3 centre = turnAbout(vec3(0.0, 0.35, 0.0), HazeTilt.xyz, HazeTilt.w);
    return unyaw(v / HazeShape.x - centre, HazeHead) + vec3(0.0, 0.85, 0.0);
}

// The direction of a ray in the head's frame.
vec3 headDirection(vec3 d) {
    return normalize(unyaw(d, HazeHead));
}

// Smoke and soot density at a point of the figure, with the shape's distance and the smoke value there.
float smokeAt(vec3 f, out float soot, out float shapeDist, out float smoke, out vec3 shaped) {
    // Rising slowly at rest; in flight the smoke flows back over the body, drawn out into streaks along it.
    float s = HazeFlow.y;
    vec3 n = vec3(f.x * 9.0, (f.y * 6.0 + HazeFlow.x * 3.6) / (1.0 + 1.5 * HazeFlow.y) - HemoTime * 0.05, f.z * 9.0)
            + HazeSeed * vec3(7.31, 3.17, 5.3);
    float warp = fbm3(n, 3);
    smoke = fbm3(n + vec3(warp * 2.5, warp * 1.5, -warp * 2.5), 4);
    shaped = f + vec3(warp - 0.5) * 0.03;
    shapeDist = figure(shaped) + lumpsAt(shaped);
    // Streaming frays the whole outline, and the ragged lower band reaches higher.
    float lowerBand = 1.0 - smoothstep(0.0, 0.38 + 0.17 * s, f.y);
    float eroded = shapeDist + (smoke - 0.5) * (0.11 + 0.05 * s + 0.06 * lowerBand * (1.0 + s));
    float density = 1.0 - smoothstep(-0.02, 0.025, eroded);
    // Thicker and thinner smoke inside, and the lower third breaks into rising wisps.
    density *= (0.45 + 0.55 * smoke) * (1.0 - lowerBand * smoothstep(0.35, 0.75, 1.0 - smoke) * 0.9);
    // A shell of black soot just outside the surface.
    soot = smoothstep(-0.01, 0.015, eroded) * (1.0 - smoothstep(0.015, 0.055, eroded));
    // Emergence lifts the smoke out of the staff from the bottom up.
    float top = HazeState.x * 1.15;
    float emerged = 1.0 - smoothstep(top - 0.15, top, f.y);
    density *= emerged * HazeState.y * (1.0 + HazeState.z * 0.25);
    soot *= emerged * HazeState.y;
    return density;
}

void main() {
    float height = HazeShape.x;
    vec3 halfSize = vec3(HazeShape.z, HazeShape.w, HazeShape.z);
    vec3 ro = HazeCamera;
    vec3 rd = normalize(localPos - ro);
    vec3 sgn = step(0.0, rd) * 2.0 - 1.0;
    vec3 inv = sgn / max(abs(rd), vec3(0.000001));
    vec3 t0 = (-halfSize - ro) * inv;
    vec3 t1 = (halfSize - ro) * inv;
    vec3 tLow = min(t0, t1);
    vec3 tHigh = max(t0, t1);
    float tNear = max(max(tLow.x, tLow.y), tLow.z);
    float tFar = min(min(tHigh.x, tHigh.y), tHigh.z);
    bool inside = all(lessThan(abs(ro), halfSize + vec3(0.15)));
    // Outside the box only the near faces start rays, so each pixel is marched once; inside it, the far faces do.
    if (inside == gl_FrontFacing) discard;
    tNear = max(tNear, 0.0);
    if (tFar <= tNear) discard;

    // The eyes: where the ray first meets the head sphere, on its forward-facing surface.
    // The head turns with its own yaw and leads the body a little along its motion.
    vec3 roF = toHead(ro + HazeLean.xyz * HEAD_LEAD * height);
    vec3 rdF = headDirection(rd);
    vec3 headCentre = vec3(0.0, 0.85, 0.0);
    vec3 oc = roF - headCentre;
    float b = dot(oc, rdF);
    float cc = dot(oc, oc) - 0.111 * 0.111;
    float disc = b * b - cc;
    float bright = 1.0 + HazeTint.a * 0.4;
    float eyeT = -1.0;
    vec3 eyeColour = vec3(0.0);
    float eyeCover = 0.0;
    float eyeSpill = 0.0;
    if (cc > 0.0 && disc > 0.0) {
        float hitF = -b - sqrt(disc);
        vec3 hit = roF + rdF * hitF;
        vec3 normal = (hit - headCentre) / 0.111;
        float facing = smoothstep(0.25, 0.65, normal.z);
        if (hitF > 0.0 && facing > 0.0) {
            eyeT = hitF * height;
            float blinkCycle = fract(HemoTime / 173.0 + HazeSeed * 0.13);
            float lid = smoothstep(0.0, 0.008, abs(blinkCycle - 0.5) - 0.004);
            float flicker = mix(1.0, step(0.35, valueNoise3(vec3(HemoTime * 0.9, HazeSeed, 0.5))), HazeLean.w);
            float seen = HazeState.w * flicker * facing;
            // Laid out as on the Monolith's face: a larger centre eye, and two smaller ones a little lower, their top
            // tips tilted outward. A blink squashes them shut; each pulses on its own phase.
            float spin = HemoTime * 0.003;
            float squash = max(lid, 0.06);
            vec2 centreAt = eyeFrame(hit.xy, vec2(0.0, 0.865), 0.0, 0.198);
            vec2 leftAt = eyeFrame(hit.xy, vec2(-0.054, 0.850), 0.384, 0.158);
            vec2 rightAt = eyeFrame(hit.xy, vec2(0.054, 0.850), -0.384, 0.158);
            centreAt.y /= squash;
            leftAt.y /= squash;
            rightAt.y /= squash;
            float centreGlow;
            float leftGlow;
            float rightGlow;
            vec4 centreEye = monolithEye(centreAt, 0.75 + 0.25 * sin(HemoTime * 0.05),
                    0.6 + 0.4 * sin(HemoTime * 0.12 + 1.2), spin, centreGlow);
            vec4 leftEye = monolithEye(leftAt, 0.75 + 0.25 * sin(HemoTime * 0.05 + 1.1),
                    0.6 + 0.4 * sin(HemoTime * 0.12 + 2.3), spin, leftGlow);
            vec4 rightEye = monolithEye(rightAt, 0.75 + 0.25 * sin(HemoTime * 0.05 + 2.2),
                    0.6 + 0.4 * sin(HemoTime * 0.12 + 0.5), spin, rightGlow);
            eyeSpill = clamp((centreGlow + leftGlow + rightGlow) * seen, 0.0, 1.0);
            vec3 eyeSum = vec3(0.0);
            for (int eye = 0; eye < 3; eye++) {
                vec4 shown = eye == 0 ? leftEye : eye == 1 ? rightEye : centreEye;
                float cover = clamp(shown.a * seen, 0.0, 1.0);
                eyeSum += min(shown.rgb * bright, vec3(1.0)) * cover;
                eyeCover = max(eyeCover, cover);
            }
            eyeColour = eyeSum / max(eyeCover, 0.0001);
        }
    }

    float stepLen = STEP_HEIGHT * height;
    float jitter = hash13(vec3(gl_FragCoord.xy, fract(HemoTime * 0.13) * 97.0));
    vec3 light = normalize(vec3(0.3, 1.0, 0.25));
    // The sky's light in the figure's frame, so a body lying along its path is lit on its back.
    vec3 lightF = unyaw(turnAbout(light, HazeTilt.xyz, -HazeTilt.w), HazeShape.y);
    vec3 deepRed = vec3(0.16, 0.012, 0.024);
    vec3 crimson = vec3(0.80, 0.07, 0.09);
    vec3 ember = vec3(1.0, 0.25, 0.12);
    vec3 sootColour = vec3(0.012, 0.003, 0.005);
    float breath = 0.5 + 0.5 * sin(HemoTime * 0.31 + HazeSeed);
    float swallow = HazeState.z;
    vec3 gathered = vec3(0.0);
    float transmit = 1.0;
    bool eyesDone = eyeT < 0.0;
    float tNext = tNear + jitter * stepLen;
    for (int i = 0; i < MARCH_STEPS; i++) {
        float t = tNext;
        if (t > tFar) break;
        tNext = t + stepLen;
        if (!eyesDone && t >= eyeT) {
            // The eyes' red light, then the eyes, at the depth of the face, veiled by the smoke in front of them.
            gathered += transmit * eyeSpill * 0.35 * vec3(0.80, 0.05, 0.05) * bright;
            gathered += transmit * eyeCover * eyeColour;
            transmit *= 1.0 - eyeCover;
            eyesDone = true;
        }
        vec3 v = ro + rd * t;
        // Moving smoke lags behind: the foot trails furthest and the head leads, along the body, standing or prone.
        float rise = clamp(toFigure(v).y, 0.0, 1.0);
        v -= HazeLean.xyz * (1.0 - rise) * 0.15 * height;
        v += HazeLean.xyz * smoothstep(0.6, 0.9, rise) * HEAD_LEAD * height;
        vec3 f = toFigure(v);
        float clearance = figure(f);
        if (clearance > EMPTY_DISTANCE) {
            // Empty space: stride across it, never past where the smoke could begin, so a long streaming box still
            // reaches the body within the march.
            tNext = t + max(stepLen, (clearance - EMPTY_DISTANCE) * SKIP_SAFETY * height);
            continue;
        }
        vec3 g = toHead(v);
        float soot;
        float shapeDist;
        float smoke;
        vec3 shaped;
        float density = smokeAt(f, soot, shapeDist, smoke, shaped);
        if (density + soot < 0.003) continue;
        vec3 towardLight = shaped + lightF * 0.03;
        float lit = clamp((figure(towardLight) + lumpsAt(towardLight) - shapeDist) / 0.03, -1.0, 1.0);
        float interior = 1.0 - smoothstep(-0.08, 0.0, shapeDist);
        // Light wraps a little past the terminator, and thicker smoke catches more of it.
        float shade = clamp((lit + 0.45) / 1.45 * (0.7 + 0.6 * smoke), 0.0, 1.0);
        vec3 colour = mix(deepRed, crimson, shade);
        colour *= 1.0 - 0.35 * interior;
        colour += ember * pow(smoke, 3.0) * interior * (0.35 + 0.2 * breath);
        colour = mix(colour, HazeTint.rgb, swallow * 0.45) * (1.0 + swallow * 0.8);
        // A dark cavity in the face, like the Monolith's black stone, for the eyes to look out of.
        float face = (1.0 - smoothstep(0.40, 1.10, length((g.xy - vec2(0.0, 0.857)) / 0.115)))
                * smoothstep(0.0, 0.06, g.z);
        colour = mix(colour, vec3(0.025, 0.0, 0.006), 0.9 * face);
        float dt = stepLen / height;
        float absorb = 1.0 - exp(-(density * EXTINCTION + soot * SOOT_EXTINCTION) * dt);
        vec3 sampleColour = mix(colour, sootColour, soot / (density + soot + 0.0001));
        gathered += transmit * absorb * sampleColour;
        transmit *= 1.0 - absorb;
        if (transmit < 0.02) break;
    }
    if (!eyesDone) {
        gathered += transmit * eyeCover * eyeColour;
        transmit *= 1.0 - eyeCover;
    }

    float alpha = 1.0 - transmit;
    if (alpha <= 0.004) {
        discard;
    }
    fragColor = vec4(gathered / alpha, alpha) * ColorModulator;
}
