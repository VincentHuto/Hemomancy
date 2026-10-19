#version 150

#moj_import <fog.glsl>

// The infection alive under the Latent's skin: branching ichor veins that crawl slowly through the flesh and pump
// in heartbeats, and a wet red sheen where the flesh turns away from the eye. Locked to the skin texture's texels
// so it keeps the model's resolution, and kept off bone, rag, claw and the ichor already painted by reading the
// skin's own colour. Vertex red is the attack surge, vertex alpha the overall presence.

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 viewNormal;
in vec3 viewPosition;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 value) {
    vec2 cell = floor(value);
    vec2 local = fract(value);
    vec2 curve = local * local * (3.0 - 2.0 * local);
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), curve.x),
            mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), curve.x), curve.y);
}

float fbm(vec2 value) {
    float total = 0.0;
    float amplitude = 0.5;
    mat2 turn = mat2(0.78, -0.63, 0.63, 0.78);
    for (int i = 0; i < 4; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.03 + vec2(1.71, -0.46);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    vec2 size = vec2(textureSize(Sampler0, 0));
    vec2 texel = floor(texCoord0 * size) + 0.5;
    vec4 skin = texture(Sampler0, texel / size);
    if (skin.a < 0.1) {
        discard;
    }
    // Only the red flesh carries it.
    float flesh = smoothstep(0.1, 0.26, skin.r - max(skin.g, skin.b)) * smoothstep(0.08, 0.2, skin.r);

    vec2 p = texel * 0.5;
    float drift = fbm(p * 0.12 + vec2(HemoTime * 0.003, -HemoTime * 0.002));
    float branching = fbm(p * 0.24 + 7.3 + drift * 1.8);
    float ridge = 1.0 - abs(2.0 * branching - 1.0);
    // Veins only where the infection has taken hold, in patches that spread and recede very slowly.
    float taken = smoothstep(0.42, 0.56, fbm(p * 0.07 + vec2(3.0, HemoTime * 0.0008)));
    float vein = smoothstep(0.955, 0.985, ridge) * taken;

    // Two thumps every two seconds, and blood running along the veins between them.
    float beat = fract(HemoTime / 40.0);
    float thump = exp(-pow((beat - 0.06) / 0.03, 2.0)) + 0.7 * exp(-pow((beat - 0.19) / 0.035, 2.0));
    float flow = 0.5 + 0.5 * sin(branching * 46.0 - HemoTime * 0.22);
    float surge = vertexColor.r;
    float pump = (0.3 + 0.4 * flow * flow + 0.7 * thump) * (1.0 + surge * 1.6);

    vec3 eye = normalize(-viewPosition);
    float facing = abs(dot(normalize(viewNormal), eye));
    float rim = pow(1.0 - facing, 3.0) * (0.75 + 0.25 * thump);

    vec3 ichor = mix(vec3(0.62, 0.3, 0.06), vec3(1.0, 0.72, 0.28), clamp(pump - 0.4, 0.0, 1.0));
    vec3 sheen = vec3(0.6, 0.06, 0.08);
    vec3 color = mix(sheen, ichor, vein);
    float alpha = max(vein * clamp(pump, 0.0, 1.0) * 0.75, rim * 0.3) * flesh;
    alpha *= vertexColor.a * ColorModulator.a;
    if (alpha < 0.01) {
        discard;
    }
    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
