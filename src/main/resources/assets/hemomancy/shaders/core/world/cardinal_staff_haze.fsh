#version 150

// Black and red haze curling off the crook of the planted Living Staff while its daemon is abroad: smoke that spins
// as it climbs, glowing deep red close to the crook and darkening to soot as it rises and thins. It stays around the
// staff; it does not reach for the daemon. Ray-marched in a box like the daemon's, from the same vertex shader.

uniform float HemoTime;
uniform float HazeSeed;
uniform vec4 HazeState;  // emergence, strength
uniform vec3 HazeCamera; // camera relative to the box's centre, in blocks
uniform vec4 HazeShape;  // unused, unused, box half-width, box half-height
uniform vec4 ColorModulator;

in vec3 localPos;

out vec4 fragColor;

const int MARCH_STEPS = 40;
// A fixed step in blocks.
const float STEP = 0.035;
// Opacity per block of full-density smoke.
const float EXTINCTION = 9.0;
// The crook's centre sits this far below the box's centre; the box is lifted for the rising wisps.
const float CROOK_DROP = 0.25;

#moj_import <hemomancy:noise/haze_noise.glsl>

// Haze density at a point relative to the crook's centre (blocks), how near the crook it is, and its height.
float hazeAt(vec3 p, out float nearCrook, out float smoke) {
    float rise = p.y + 0.4;
    // The smoke curls as it rises: the point turns about the staff as it climbs.
    float turn = rise * 3.0 + HemoTime * 0.02;
    float c = cos(turn);
    float s = sin(turn);
    vec3 q = vec3(p.x * c - p.z * s, p.y, p.x * s + p.z * c);
    vec3 n = vec3(q.x * 4.4, q.y * 3.0 - HemoTime * 0.06, q.z * 4.4) + HazeSeed * vec3(3.1, 7.7, 1.9);
    float warp = fbm3(n, 2);
    smoke = fbm3(n + vec3(warp * 2.0, warp, -warp * 2.0), 4);
    // An envelope hugging the crook and stretching up above it, flattened toward the crook's plane.
    vec3 envelopeAxis = (p - vec3(0.0, 0.1, 0.0)) / vec3(0.45, 0.65, 0.40);
    float envelope = 1.0 - smoothstep(0.55, 1.0, length(envelopeAxis));
    nearCrook = 1.0 - smoothstep(0.2, 0.9, length((p - vec3(0.0, -0.05, 0.0)) / vec3(0.35, 0.40, 0.30)));
    // Wisps, not a fog: only the denser threads of the noise show, thicker close to the crook.
    float wisps = smoothstep(0.34, 0.64, smoke + 0.18 * nearCrook);
    return envelope * wisps * HazeState.x * HazeState.y;
}

void main() {
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
    // Outside the box only the near faces start rays; inside it, the far faces do.
    if (inside == gl_FrontFacing) discard;
    tNear = max(tNear, 0.0);
    if (tFar <= tNear) discard;

    float jitter = hash13(vec3(gl_FragCoord.xy, fract(HemoTime * 0.13) * 97.0));
    vec3 soot = vec3(0.015, 0.004, 0.006);
    vec3 emberRed = vec3(0.75, 0.04, 0.06);
    vec3 gathered = vec3(0.0);
    float transmit = 1.0;
    for (int i = 0; i < MARCH_STEPS; i++) {
        float t = tNear + (float(i) + jitter) * STEP;
        if (t > tFar) break;
        vec3 p = ro + rd * t + vec3(0.0, CROOK_DROP, 0.0);
        float nearCrook;
        float smoke;
        float density = hazeAt(p, nearCrook, smoke);
        if (density < 0.003) continue;
        // Red where it pours off the crook, soot as it climbs away; the top of the box fades it out.
        float climbed = smoothstep(-0.1, 0.6, p.y);
        vec3 colour = mix(soot, emberRed * (0.7 + 0.6 * smoke), nearCrook * (1.0 - climbed * 0.7));
        density *= 1.0 - smoothstep(halfSize.y - 0.3 + CROOK_DROP, halfSize.y + CROOK_DROP, p.y);
        float absorb = 1.0 - exp(-density * EXTINCTION * STEP);
        gathered += transmit * absorb * colour;
        transmit *= 1.0 - absorb;
        if (transmit < 0.02) break;
    }

    float alpha = 1.0 - transmit;
    if (alpha <= 0.004) {
        discard;
    }
    fragColor = vec4(gathered / alpha, alpha) * ColorModulator;
}
