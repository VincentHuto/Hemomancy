#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;
in float rippleShade;

out vec4 fragColor;

void main() {
    vec4 parchment = texture(Sampler0, texCoord0);
    float windLight = 1.0 + rippleShade * 0.055;
    vec3 color = parchment.rgb * vertexColor.rgb * ColorModulator.rgb * windLight;
    float alpha = parchment.a * vertexColor.a * ColorModulator.a;
    fragColor = vec4(color, alpha);
}
