#version 150

uniform vec4 ColorModulator;
uniform float GameTime;
uniform float Speed;
uniform float Intensity;
uniform float BorderWidth;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    // UV goes 0..1 across the quad
    vec2 uv = texCoord0;

    // Distance from edge (0 at edge, 0.5 at center)
    float dx = min(uv.x, 1.0 - uv.x);
    float dy = min(uv.y, 1.0 - uv.y);
    float edgeDist = min(dx, dy);

    // Create a border mask — 1.0 at the edge, fading to 0 toward center
    float border = 1.0 - smoothstep(0.0, BorderWidth, edgeDist);

    // Pulsing animation
    float time = GameTime * Speed;
    float pulse = 0.7 + 0.3 * sin(time);

    // Corner emphasis — glow brighter at corners
    float cornerDist = length(vec2(0.5) - uv) * 1.4;
    float cornerBoost = smoothstep(0.3, 0.8, cornerDist);
    float glow = border * (1.0 + cornerBoost * 0.5);

    // Apply pulse, intensity, and vertex color
    float alpha = glow * pulse * Intensity;
    vec4 baseColor = vertexColor * ColorModulator;
    fragColor = vec4(baseColor.rgb, baseColor.a * alpha);
}

