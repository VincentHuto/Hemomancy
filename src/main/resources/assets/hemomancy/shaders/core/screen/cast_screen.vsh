#version 150
// Full-screen pass: the quad is already in clip space.
in vec3 Position;
in vec2 UV0;
out vec2 texCoord0;
void main() {
    gl_Position=vec4(Position.xy,0.0,1.0);
    texCoord0=UV0;
}
