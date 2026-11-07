#version 150

// A Scarlet Vanity's glass: old dark glass with smoke drifting in it and tarnish at the edges, and, when the
// reflection pass drew any, the players standing in front of it, read from that pass by screen position and seen
// through the smoke. UVs are the glass's block-pixel position (x 3..13, y from 17), so the arch matches the frame.

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float Live;
uniform vec2 ScreenSize;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

#moj_import <hemomancy:noise/haze_noise.glsl>

// The top of each pixel column of glass from x = 3, under the frame's top rail
// (tools/model_export/scarlet_vanity_mirror.py GLASS_TOP).
const float GLASS_TOP[10] = float[10](29.0, 28.5, 28.5, 29.5, 30.5, 30.5, 29.5, 28.5, 28.5, 29.0);

void main() {
    vec2 px = texCoord0;
    float top = GLASS_TOP[clamp(int(floor(px.x - 3.0)), 0, 9)];
    if (px.y > top) discard;

    vec2 uv = vec2((px.x - 3.0) / 10.0, (px.y - 17.0) / 13.5);
    float seconds = HemoTime / 20.0;
    // How far into the glass from the frame, in pixels: tarnish creeps in from there.
    float inset = min(min(px.x - 3.0, 13.0 - px.x), min(px.y - 17.0, top - px.y));
    float rim = 1.0 - smoothstep(0.0, 2.5, inset);

    // Two layers of smoke, the finer one rising faster.
    float broad = fbm3(vec3(uv * vec2(2.2, 3.0) + vec2(0.0, -seconds * 0.03), seconds * 0.05), 4);
    float fine = fbm3(vec3(uv * vec2(4.5, 6.0) + vec2(seconds * 0.02, -seconds * 0.06), 7.3 + seconds * 0.04), 3);
    float smoke = smoothstep(0.30, 0.85, broad * 0.65 + fine * 0.55);

    vec3 glass = vec3(0.030, 0.016, 0.018) + vec3(0.11, 0.050, 0.055) * smoke;
    // Foxing: rust-red spots where the silvering has gone, thickest at the edges.
    float fox = smoothstep(0.58, 0.82, valueNoise3(vec3(px * 1.7, 3.1))) * rim;
    glass = mix(glass, vec3(0.17, 0.035, 0.03), fox * 0.7);
    glass = mix(glass, vec3(0.012, 0.006, 0.006), rim * 0.5);
    // A faint streak of sheen across the face.
    float streak = exp(-pow((uv.x - uv.y * 0.45 - 0.25) * 7.0, 2.0)) * 0.05 * (1.0 - rim);
    glass += vec3(streak, streak * 0.8, streak * 0.8);
    glass *= vertexColor.rgb;

    vec3 color = glass;
    if (Live > 0.5) {
        vec4 seen = texture(Sampler0, gl_FragCoord.xy / ScreenSize);
        // Seen through the smoke and dimmed by old silvering, a little red.
        float clarity = (1.0 - smoke * 0.6) * (1.0 - rim * 0.75);
        vec3 tinted = seen.rgb * vec3(0.80, 0.64, 0.62);
        color = mix(glass, tinted + glass * 0.35, seen.a * clarity);
    }
    fragColor = linear_fog(vec4(color, 1.0) * ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}
