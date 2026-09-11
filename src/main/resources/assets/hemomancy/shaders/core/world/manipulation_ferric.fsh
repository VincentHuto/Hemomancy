#version 150
#moj_import <fog.glsl>
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
in float vertexDistance;
in vec4 vertexColor;
in vec2 uv;
in vec3 viewPosition;
flat in float shape;
flat in float seed;
out vec4 fragColor;
float hash(vec2 p) { return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453); }
float noise(vec2 p) {
    vec2 i=floor(p), f=fract(p);f=f*f*(3.0-2.0*f);
    return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+1.0),f.x),f.y);
}
void main() {
    float grain=noise(uv*17.0+seed), bloom=noise(uv*4.5+seed*.71);
    float formation=vertexColor.a;
    float edge=min(min(uv.x,1.0-uv.x),min(uv.y,1.0-uv.y));
    float threshold=uv.y*.74+noise(uv*8.0+seed)*.24;
    // The rigid rim continues to identify the collision boundary while blood fills or leaves the face.
    if(formation<.995 && threshold>formation && edge>.012)discard;
    vec3 n=normalize(cross(dFdx(viewPosition),dFdy(viewPosition)));
    float lit=.47+.53*abs(dot(n,normalize(vec3(.3,.85,-.4))));
    float rust=(1.0-smoothstep(.012,.15,edge))*(.35+bloom*.5)+smoothstep(.68,.86,bloom)*.22;
    float scratch=pow(max(0.0,sin(uv.y*150.0+noise(uv*3.0+seed)*6.0)),24.0)*.065;
    vec3 metal=mix(vec3(.15,.17,.18),vec3(.28,.30,.32),grain*.4+scratch);
    vec3 color=mix(metal,vec3(.31,.095,.045)*( .65+grain*.5),rust)*lit;
    float rim=1.0-smoothstep(.005,.025,edge);
    color+=vec3(.24,.25,.24)*rim*lit;
    float seam=1.0-smoothstep(.009,.027,abs(uv.x-.5-noise(vec2(uv.y*4.0,seed))*.07));
    color=mix(color,vec3(.25,.018,.033),seam*.55);
    float wet=(1.0-smoothstep(.0,.12,formation-threshold))*(1.0-step(.995,formation));
    wet*=.94+.06*sin(HemoTime*.14+uv.y*9.0+seed);
    color=mix(color,vec3(.44,.015,.033),wet*.75);
    color+=vec3(.32,.21,.16)*pow(abs(dot(n,normalize(-viewPosition))),18.0)*(.08+wet*.4);
    fragColor=linear_fog(vec4(color*vertexColor.rgb*ColorModulator.rgb,1.0),vertexDistance,FogStart,FogEnd,FogColor);
}
