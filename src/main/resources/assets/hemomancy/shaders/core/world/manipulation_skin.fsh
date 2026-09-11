#version 150
#moj_import <fog.glsl>
uniform float HemoTime;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
in float vertexDistance;
in vec4 vertexColor;
in vec2 bodyUv;
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
void main() {
    // Authored model UVs keep veins fixed to posed limbs as the body and camera move.
    vec2 p=bodyUv*48.0;
    float progress=vertexColor.r;
    float hot=vertexColor.g;
    float n=fbm(p*.65);
    float vein=fissure(p+vec2(n*.4,0));
    float lines=1.0-smoothstep(.008,.045,vein);
    float reveal=smoothstep(n*.6,n*.6+.35,progress);
    float powder=smoothstep(.55,.8,fbm(p*3.0)+progress*.14);
    vec3 cold=mix(vec3(.16,.012,.05),vec3(.76,.88,.9),powder*.8+lines*.2);
    float seal=1.0-smoothstep(.55,1.0,progress);
    vec3 fire=mix(vec3(.2,.004,.014),vec3(1,.32,.014),seal);
    float alpha=mix(lines*.6+powder*.32,lines*(.18+seal*.7),hot)*reveal*vertexColor.a*ColorModulator.a;
    if(alpha<.004)discard;
    fragColor=linear_fog(vec4(mix(cold,fire,hot)*ColorModulator.rgb,alpha),vertexDistance,FogStart,FogEnd,FogColor);
}
