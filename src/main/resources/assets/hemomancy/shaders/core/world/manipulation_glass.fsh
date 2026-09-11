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
    vec2 i=floor(p),f=fract(p); f=f*f*(3.0-2.0*f);
    return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+1.0),f.x),f.y);
}
float fbm(vec2 p) { return noise(p)*.57+noise(p*2.03+7.1)*.28+noise(p*4.09-3.2)*.15; }
float edge(vec2 p) { return smoothstep(0.0,.08,min(min(p.x,1.0-p.x),min(p.y,1.0-p.y))); }
float fissure(vec2 p) {
    vec2 cell=floor(p),f=fract(p); float a=10.0,b=10.0;
    for(int x=-1;x<=1;x++) for(int y=-1;y<=1;y++) {
        vec2 offset=vec2(x,y),id=cell+offset;
        vec2 center=offset+vec2(hash(id),hash(id+18.7));
        float d=length(center-f);
        if(d<a){b=a;a=d;}else b=min(b,d);
    }
    return b-a;
}
void finish(vec3 color,float alpha) {
    alpha*=vertexColor.a*ColorModulator.a;
    if(alpha<.003)discard;
    fragColor=linear_fog(vec4(color*vertexColor.rgb*ColorModulator.rgb,clamp(alpha,0.0,1.0)),vertexDistance,FogStart,FogEnd,FogColor);
}
void main() {
    vec3 normal=normalize(cross(dFdx(viewPosition),dFdy(viewPosition)));
    float facing=abs(dot(normal,normalize(-viewPosition)));
    float rim=pow(1.0-facing,3.0);
    vec3 light=normalize(vec3(-.3,.85,.5));
    float glint=pow(abs(dot(reflect(normalize(viewPosition),normal),light)),44.0);
    float vein=fissure(uv*3.4+seed);
    float crack=1.0-smoothstep(.007,.028,vein);
    float bevel=shape>.5?1.0:0.0;
    float n=fbm(uv*5.0+seed);
    vec3 color=mix(vec3(.11,.005,.022),vec3(.64,.035,.07),n);
    color+=vec3(.85,.49,.35)*(rim*.55+glint*.85+bevel*.3+crack*.22);
    finish(color,shape>.5?.9:.26+rim*.42+bevel*.23+crack*.16);
}
