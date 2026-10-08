#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float HemoTime;
uniform vec4 ColorModulator;
in vec4 vertexColor;
in vec2 texCoord0;
out vec4 fragColor;
void main() {
    vec2 uv = vec2(texCoord0.x, 1.0 - texCoord0.y);
    vec2 ripple = vec2(sin(uv.y * 24.0 + HemoTime * .08), cos(uv.x * 21.0 - HemoTime * .07)) * .0012;
    vec3 world = texture(Sampler0, clamp(uv + ripple, .001, .999)).rgb;
    float luminance = dot(world, vec3(.299,.587,.114));
    vec3 red = vec3(pow(luminance,.85) * .62, .008 * luminance, .018 * luminance);
    vec4 route = texture(Sampler1, uv);
    float node = clamp(route.r,0.0,1.0);
    float fiber = clamp(route.g,0.0,1.0);
    vec2 pixel = 1.0 / vec2(textureSize(Sampler1,0));
    float halo = max(max(texture(Sampler1,uv+vec2(pixel.x*3,0)).r,texture(Sampler1,uv-vec2(pixel.x*3,0)).r),
                    max(texture(Sampler1,uv+vec2(0,pixel.y*3)).r,texture(Sampler1,uv-vec2(0,pixel.y*3)).r));
    red += vec3(1.0,.82,.08) * max(0.0,halo-node) * .22;
    red = mix(red,vec3(.68,.67,.63),fiber);
    red = mix(red,vec3(1.0,.88,.14),node);
    fragColor = vec4(red,1.0) * vertexColor * ColorModulator;
}
