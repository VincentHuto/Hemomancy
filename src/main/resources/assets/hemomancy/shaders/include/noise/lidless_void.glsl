// The Lidless void: what a ray sees through a Lidless Door, shared by the door panel (world/lidless_door_panel)
// and the crossing view (screen/lidless_passage_view) so both read as the same place. The ray is unit length in
// passage space: +z toward the exit, +y up. Every value is a parameter; time is in ticks. The field is 3D value
// noise read on the ray itself, so there is no projection and no pinch at the axis. Importers pull in
// hemomancy:noise/monolith_noise.glsl first for hash21.
float lidlessHash3(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

float lidlessNoise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float n000 = lidlessHash3(i);
    float n100 = lidlessHash3(i + vec3(1.0, 0.0, 0.0));
    float n010 = lidlessHash3(i + vec3(0.0, 1.0, 0.0));
    float n110 = lidlessHash3(i + vec3(1.0, 1.0, 0.0));
    float n001 = lidlessHash3(i + vec3(0.0, 0.0, 1.0));
    float n101 = lidlessHash3(i + vec3(1.0, 0.0, 1.0));
    float n011 = lidlessHash3(i + vec3(0.0, 1.0, 1.0));
    float n111 = lidlessHash3(i + vec3(1.0, 1.0, 1.0));
    float x00 = mix(n000, n100, f.x);
    float x10 = mix(n010, n110, f.x);
    float x01 = mix(n001, n101, f.x);
    float x11 = mix(n011, n111, f.x);
    return mix(mix(x00, x10, f.y), mix(x01, x11, f.y), f.z);
}

// Five octaves, each rotated so the layers do not line up, each drifting a little further along its own axis.
float lidlessFbm3(vec3 p, float drift) {
    const mat3 turn = mat3(0.00, 0.80, 0.60, -0.80, 0.36, -0.48, -0.60, -0.48, 0.64);
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 5; i++) {
        v += lidlessNoise3(p) * amp;
        p = turn * p * 2.02 + vec3(drift, 0.0, -drift);
        amp *= 0.5;
    }
    return v;
}

vec2 lidlessRotate(vec2 p, float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return mat2(c, -s, s, c) * p;
}

float lidlessCellHash(vec3 cell, float salt) {
    return hash21(vec2(cell.x + cell.z * 17.0 + salt, cell.y - cell.z * 7.0 + salt * 0.7));
}

// One layer of eyes on the far sphere, laid out on a 3D lattice over the view direction so their spread and size
// are the same in every direction. A cube of the lattice owns the sphere point in its own direction, and only
// that cube may hold an eye, so no two eyes share a spot; the eight cubes around the ray are checked so no eye is
// clipped by a cube edge, and the radius stays under the cube spacing so neighbours never overlap. An eye is an
// organic oval, the upper lid higher than the lower and both wobbling, around a round iris of three nested red
// rings on a round black pupil. Each eye lives on its own clock: it fades in, lingers, fades out and is gone for a
// while, so the crowd shifts around rather than moving as one; while present it wanders slowly in its cell, its
// tilt sways, its gaze drifts, and it blinks on a slow cycle; some stay shut for long stretches. Straight ahead eyes
// are rarer, unless stare is 1 (the door panel seen from outside). The lids feather into the field and a halo
// bleeds out. Returns colour and opacity to composite over what lies behind.
vec4 lidlessEyeLayer(vec3 ray, float time, float seed, float stare, float cells, float baseRadius, float opacity,
        float brightness) {
    vec3 turned = ray;
    vec3 base = floor(turned * cells - 0.5);
    float grain = lidlessNoise3(ray * 5.0 + seed);
    vec4 result = vec4(0.0);
    for (int i = 0; i < 8; i++) {
        vec3 cell = base + vec3(float(i & 1), float((i >> 1) & 1), float((i >> 2) & 1));
        vec3 cubeCentre = (cell + 0.5) / cells;
        vec3 onSphere = normalize(cubeCentre);
        if (any(notEqual(floor(onSphere * cells), cell))) continue;
        float h1 = lidlessCellHash(cell, 3.1 + seed);
        float h2 = lidlessCellHash(cell, 7.7 + seed);
        float h3 = lidlessCellHash(cell, 9.1);
        // A slow private wander: two incommensurate sines per axis, never the same path twice.
        vec3 sway = vec3(sin(time * 0.0017 + h1 * 6.3) + 0.5 * sin(time * 0.0041 + h2 * 2.0),
                sin(time * 0.0013 + h2 * 6.3) + 0.5 * sin(time * 0.0037 + h3 * 2.0),
                sin(time * 0.0019 + h3 * 6.3) + 0.5 * sin(time * 0.0029 + h1 * 2.0));
        vec3 jitter = (vec3(h1, h2, h3) - 0.5) * 0.10 + sway * 0.08;
        vec3 centre = normalize(cubeCentre + jitter / cells);
        float ahead = smoothstep(0.85, 0.95, centre.z) * (1.0 - stare);
        float chance = mix(mix(0.42, 0.18, ahead), 0.60, stare);
        if (lidlessCellHash(cell, seed * 0.37) > chance) continue;
        // Its life: present for part of a 20 to 60 second cycle, fading in and out at the ends.
        float lifeTicks = (20.0 + 40.0 * lidlessCellHash(cell, 8.8)) * 20.0;
        float lifePhase = fract(time / lifeTicks + lidlessCellHash(cell, 12.1));
        float duty = 0.45 + 0.25 * lidlessCellHash(cell, 13.7);
        float presence = smoothstep(0.0, 0.08, lifePhase) * (1.0 - smoothstep(duty - 0.08, duty, lifePhase));
        if (presence < 0.02) continue;
        float cosA = dot(turned, centre);
        if (cosA < 0.95) continue;
        float cycleTicks = (8.0 + 12.0 * lidlessCellHash(cell, 5.5 + seed)) * 20.0;
        float phase = fract(time / cycleTicks + lidlessCellHash(cell, 2.2));
        float sleeper = step(0.85, lidlessCellHash(cell, 9.9 + seed));
        float openUntil = mix(0.88, 0.30, sleeper);
        float open = smoothstep(0.0, 0.08, phase) * (1.0 - smoothstep(openUntil - 0.08, openUntil, phase));
        if (open < 0.02) continue;
        vec3 upRef = abs(centre.y) > 0.9 ? vec3(1.0, 0.0, 0.0) : vec3(0.0, 1.0, 0.0);
        vec3 tx = normalize(cross(upRef, centre));
        vec3 ty = cross(centre, tx);
        vec2 local = vec2(dot(turned, tx), dot(turned, ty)) / cosA;
        float radius = baseRadius * (0.7 + 0.6 * lidlessCellHash(cell, 4.4));
        float tilt = (lidlessCellHash(cell, 11.3) - 0.5) * 0.8 + 0.15 * sin(time * 0.006 + h2 * 9.0);
        vec2 d = lidlessRotate(local, tilt);
        float across = d.x / radius;
        if (abs(across) >= 1.3) continue;
        float lens = max(1.0 - across * across, 0.0);
        float wobble = lidlessNoise3(vec3(d / radius * 4.0, h3 * 9.0 + time * 0.002));
        float upper = radius * (0.52 + 0.14 * wobble) * lens * open;
        float lower = radius * (0.34 + 0.10 * (1.0 - wobble)) * lens * open;
        float lidEdge = d.y > 0.0 ? upper : lower;
        float feather = radius * 0.12;
        float eye = smoothstep(lidEdge + feather, lidEdge - feather * 0.5, abs(d.y)) * step(abs(across), 1.0);
        float halo = smoothstep(lidEdge + radius * 0.6, lidEdge, abs(d.y)) * smoothstep(1.3, 0.9, abs(across)) * 0.25;
        result = mix(result, vec4(vec3(0.34, 0.03, 0.02) * brightness, 1.0), halo * (0.5 + 0.5 * grain) * opacity * presence);
        if (eye <= 0.0) continue;
        vec2 gaze = radius * 0.16 * vec2(sin(time * 0.011 + h1 * 6.3), cos(time * 0.007 + h2 * 4.1));
        vec2 g = d - gaze;
        float irisRadius = radius * 0.36;
        float r = length(g) + (lidlessNoise3(vec3(g / radius * 9.0, h3 * 7.0)) - 0.5) * irisRadius * 0.10;
        float iris = smoothstep(irisRadius + radius * 0.03, irisRadius - radius * 0.03, r);
        float k = fract(clamp(r / irisRadius, 0.0, 0.999) * 3.0);
        float ring = smoothstep(0.30, 0.42, k) * (1.0 - smoothstep(0.58, 0.70, k));
        float pupil = smoothstep(irisRadius * 0.24, irisRadius * 0.18, r);
        float lid = smoothstep(lidEdge - radius * 0.12, lidEdge, abs(d.y));
        // The eyeball: a dark red white, lighter toward the iris, shadowed under the lids, with a dark lid rim.
        float toIris = smoothstep(radius, irisRadius, length(g));
        vec3 color = mix(vec3(0.24, 0.015, 0.015), vec3(0.50, 0.05, 0.04), toIris);
        color *= 1.0 - 0.35 * smoothstep(lidEdge - radius * 0.3, lidEdge, abs(d.y));
        color = mix(color, vec3(0.46, 0.03, 0.02), iris);
        color = mix(color, vec3(0.95, 0.10, 0.06), iris * ring);
        color = mix(color, vec3(0.0), pupil);
        color = mix(color, vec3(0.12, 0.01, 0.01), lid * (1.0 - iris));
        float alpha = eye * opacity * (0.92 + 0.08 * grain) * presence;
        result = mix(result, vec4(color * brightness, 1.0), alpha);
    }
    return result;
}

// Three layers of eyes for depth: small, dim, half-seen ones far off; a middle crowd over them; and a few large
// close ones drawn over everything. Each layer keeps its own radius under its own cube spacing (with room for the
// wander), so eyes never overlap within a layer and only a nearer eye ever covers a farther one.
vec4 lidlessEyes(vec3 ray, float time, float seed, float stare) {
    float dense = mix(1.0, 1.3, stare);
    vec4 far = lidlessEyeLayer(ray, time, seed + 31.0, stare, 9.0 * dense, 0.024, 0.72, 0.70);
    vec4 mid = lidlessEyeLayer(ray, time, seed, stare, 5.0 * dense, 0.043, 0.90, 0.92);
    vec4 close = lidlessEyeLayer(ray, time, seed + 59.0, stare, 3.0 * dense, 0.072, 1.0, 1.0);
    vec4 layered = mix(far, mid, mid.a);
    return mix(layered, close, close.a);
}

// The void along a ray: near-black ground with a dark red grain at three depths, each creeping through its own
// noise, red veins through the grain, a dark throat straight ahead, and the eyes. twist is 0 to 1
// along the crossing (it only deepens the throat); stare is 1 for the door panel seen from outside (no throat,
// eyes meet the viewer) and 0 for the crossing.
vec3 lidlessVoid(vec3 ray, float time, float seed, float twist, float stare) {
    // Nothing turns: each layer only creeps through its own noise in its own direction, so the grain breathes and
    // crawls without any wheel, funnel or shared motion.
    vec3 da = ray * 1.7 + seed + vec3(time * 0.0009, time * 0.0004, -time * 0.0006);
    vec3 db = ray * 4.1 - seed * 1.3 + vec3(-time * 0.0011, time * 0.0016, time * 0.0007);
    vec3 dc = ray * 9.0 + vec3(seed * 0.7, -seed, seed * 0.3) + vec3(time * 0.0014, -time * 0.0021, time * 0.0012);
    float a = lidlessFbm3(da, time * 0.0015);
    float b = lidlessFbm3(db, time * 0.0035);
    float c = lidlessFbm3(dc, time * 0.006);
    float depth = a * 0.55 + b * 0.30 + c * 0.15;
    vec3 black = vec3(0.0015, 0.0001, 0.0001);
    vec3 red = vec3(0.62, 0.015, 0.010);
    float vein = smoothstep(0.052, 0.010, abs(a - 0.5)) * (0.4 + 0.6 * b);
    float strand = smoothstep(0.030, 0.006, abs(b - 0.48)) * 0.6;
    float pulse = 0.65 + 0.35 * sin(time * 0.12 + seed * 2.6 + c * 4.0);
    vec3 color = mix(black, red * 0.22, smoothstep(0.35, 0.75, depth));
    color += vec3(0.82, 0.018, 0.010) * (vein * 0.9 + strand * 0.5) * pulse;
    float throat = smoothstep(0.6, 1.0, ray.z) * (1.0 - stare) * (0.5 + 0.5 * twist);
    color *= 1.0 - throat * 0.5;
    vec4 eyes = lidlessEyes(ray, time, seed, stare);
    color = mix(color, eyes.rgb, eyes.a);
    return color;
}
