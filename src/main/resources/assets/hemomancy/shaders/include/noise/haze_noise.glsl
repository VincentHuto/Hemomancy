// Hash, value noise and fractal noise for the Cardinal hazes.
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
