#version 150

uniform vec4 ColorModulator;
in vec2 glintUv;
in float chargeAlpha;
out vec4 fragColor;

void main() {
    vec2 uv = glintUv * 42.0;
    float current = sin(uv.x + uv.y + sin(uv.y * .65) * .9);
    float filament = pow(.5 + .5 * current, 14.0);
    float sheen = pow(.5 + .5 * sin(uv.x * .43 - uv.y * .7), 5.0);
    float alpha = chargeAlpha * (.045 + filament * .75 + sheen * .20);
    fragColor = vec4(vec3(1.0), alpha * ColorModulator.a);
}
