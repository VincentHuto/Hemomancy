// Hash, value noise and the two fractal noises for the monolith shaders.
float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}
float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}
float fbm(vec2 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 5; i++) {
        v += noise(p) * amp;
        p = mat2(1.62, 1.15, -1.15, 1.62) * p + vec2(ShardSeed * 0.13, HemoTime * FBM_LAYER_DRIFT_RATE);
        amp *= 0.5;
    }
    return v;
}
float staticFbm(vec2 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
        v += noise(p) * amp;
        p = mat2(1.62, 1.15, -1.15, 1.62) * p + vec2(ShardSeed * 0.19, 0.37);
        amp *= 0.5;
    }
    return v;
}
