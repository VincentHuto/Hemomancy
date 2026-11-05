// What the Lidless void turns into near a Chamber of Will, by the owner's path: 0 the Qliphoth Chamber's violet greys,
// 1 a Silentium's grey fog with its great slabs standing in the distance, 2 an Apotheos's ember dark, patched with fungal crust. Used
// by the door panel and the crossing view so a portal and its walk are the same place. Importers pull in
// hemomancy:noise/lidless_void.glsl and the vein_* noise (through vein_cellular) first.

// The Silentium Chamber's distant monoliths, on a ray: tall slanted slabs on every side, near ones dark and far ones
// half lost in the fog. Returns how much slab this ray meets.
float lidlessPillars(vec3 ray, float seed) {
    float azimuth = atan(ray.x, ray.z);
    float elevation = ray.y / max(length(ray.xz), 0.001);
    float slab = 0.0;
    for (int layer = 0; layer < 2; layer++) {
        float near = layer == 0 ? 1.0 : 0.0;
        float count = mix(15.0, 9.0, near);
        float around = (azimuth + 3.14159265) / 6.2831853 * count;
        float cell = mod(floor(around), count);
        float within = fract(around) - 0.5;
        float a = lidlessHash3(vec3(cell, float(layer) + 1.0, seed));
        float b = lidlessHash3(vec3(cell, float(layer) + 11.0, seed));
        float c = lidlessHash3(vec3(cell, float(layer) + 23.0, seed));
        float across = within - (a - 0.5) * 0.45;
        float halfWidth = mix(0.05, 0.11, b) * mix(0.6, 1.0, near);
        float top = mix(0.22, 0.85, c) * mix(0.55, 1.0, near);
        float sides = smoothstep(halfWidth, halfWidth * 0.82, abs(across));
        float cap = smoothstep(top + 0.012, top - 0.012, elevation + across * (a - 0.5) * 0.9);
        // Their feet stand in the fog, and the far rank is mostly fog.
        float fog = smoothstep(-1.1, -0.1, elevation) * mix(0.42, 0.9, near);
        slab = max(slab, sides * cap * fog);
    }
    return slab;
}

// A fungal infestation over the dark: soft patches of crust that drift very slowly, deep red at their edges and orange
// inward, flecked with tiny fruiting specks of yellow where they are thickest. No strands: the void's own veins carry
// the lines, and most of the sky stays bare.
vec3 lidlessInfestation(vec3 base, vec3 ray, float time, float seed) {
    vec3 s = vec3(seed * 3.1, seed * 1.7, seed * 2.3);
    vec3 drift = vec3(time * 0.0004, -time * 0.0006, time * 0.0003);
    float field = fbm(ray * 3.2 + s + drift);
    float infest = smoothstep(0.5, 0.72, field);
    if (infest <= 0.0) return base;
    float crust = fbm(ray * 15.0 + s + 5.0);
    float thick = infest * (0.5 + 0.5 * crust);
    vec3 tint = mix(vec3(0.24, 0.022, 0.010), vec3(0.56, 0.17, 0.024), smoothstep(0.24, 0.62, thick));
    vec3 color = mix(base, tint, infest * (0.3 + 0.38 * crust));
    vec2 specks = cellular(ray * 26.0 + s * 2.0);
    float size = mix(0.05, 0.17, fbm(ray * 7.0 + s + 11.0));
    float speck = (1.0 - smoothstep(size * 0.55, size, specks.x)) * smoothstep(0.42, 0.8, infest);
    float glint = 0.72 + 0.28 * sin(time * 0.07 + specks.x * 40.0 + field * 30.0);
    return mix(color, vec3(0.92, 0.62, 0.14) * glint, speck * 0.85);
}

// The void's colour (with its eyes) carried into a theme: its dark stays dark, its veins become the theme's light.
vec3 lidlessThemeSky(vec3 color, vec3 ray, float theme, float time, float seed) {
    float lum = clamp(dot(color, vec3(0.6, 0.25, 0.15)) * 1.7, 0.0, 1.0);
    if (theme < 0.5) {
        vec3 sky = mix(vec3(0.172, 0.149, 0.208), vec3(0.427, 0.373, 0.525), smoothstep(0.03, 0.45, lum));
        return mix(sky, vec3(0.82, 0.043, 0.102), smoothstep(0.5, 0.95, lum) * 0.75);
    }
    if (theme < 1.5) {
        vec3 sky = mix(vec3(0.133, 0.188, 0.192), vec3(0.373, 0.455, 0.447), smoothstep(0.02, 0.32, lum));
        sky = mix(sky, vec3(0.549, 0.651, 0.635), smoothstep(0.3, 0.62, lum));
        sky = mix(sky, vec3(0.431, 0.2, 0.216), smoothstep(0.68, 0.98, lum) * 0.65);
        return mix(sky, vec3(0.05, 0.07, 0.075), lidlessPillars(ray, seed));
    }
    vec3 sky = mix(vec3(0.012, 0.006, 0.004), vec3(0.15, 0.028, 0.012), smoothstep(0.02, 0.4, lum));
    sky = mix(sky, vec3(0.62, 0.21, 0.034), smoothstep(0.42, 0.8, lum));
    sky = mix(sky, vec3(0.86, 0.60, 0.13), smoothstep(0.82, 1.0, lum) * 0.8);
    return lidlessInfestation(sky, ray, time, seed);
}

// The bright ring around the black hole.
vec3 lidlessThemeRing(float theme) {
    if (theme < 0.5) return vec3(0.82, 0.043, 0.102);
    if (theme < 1.5) return vec3(0.52, 0.22, 0.235);
    return vec3(0.80, 0.24, 0.04);
}

// The light gathered outside that ring.
vec3 lidlessThemeHalo(float theme) {
    if (theme < 0.5) return vec3(0.85, 0.71, 1.0);
    if (theme < 1.5) return vec3(0.68, 0.72, 0.72);
    return vec3(0.78, 0.30, 0.05);
}

// The black hole's own near-black.
vec3 lidlessThemeCore(float theme) {
    if (theme < 0.5) return vec3(0.05, 0.03, 0.075);
    if (theme < 1.5) return vec3(0.03, 0.042, 0.046);
    return vec3(0.028, 0.011, 0.006);
}
