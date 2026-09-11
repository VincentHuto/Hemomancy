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
    vec2 p=uv*2.0-1.0;
    float t=HemoTime*.022+seed*.83;
    vec2 q=p*1.35;
    q+=vec2(fbm(q+vec2(t*.6,-t))-.5,fbm(q+vec2(8.3,-t*.7))-.5)*.85;
    float stream=fbm(q+vec2(-t*.8,t*.23));
    float seam=fissure(q*1.2+vec2(t*.28,-t*.2));
    float hot=1.0-smoothstep(.025,.24,seam);
    float r=length(p);
    float border=1.0-smoothstep(.65+stream*.15,1.0,r);
    float wave=sin(q.x*3.0+q.y*1.5-t*3.0+stream*5.0)*.5+.5;
    vec3 blood=mix(vec3(.12,.003,.018),vec3(.56,.014,.024),stream);
    vec3 color=mix(blood,vec3(.96,.16,.007),hot*.68);
    color+=vec3(1.0,.36,.016)*pow(wave,6.0)*.19;
    vec2 cells=fract(q*1.4)-.5;
    float bubble=length(cells);
    float cycle=fract(t*.6+hash(floor(q*1.4)));
    float bubbleR=.07+cycle*.24;
    float lip=exp(-pow((bubble-bubbleR)*45.0,2.0))*(1.0-smoothstep(.76,1.0,cycle));
    color+=vec3(1,.28,.016)*lip*.42;
    if(shape>.5) {
        vec3 normal=normalize(cross(dFdx(viewPosition),dFdy(viewPosition)));
        float facing=abs(dot(normal,normalize(-viewPosition)));
        float glint=pow(facing,12.0)*(.25+hot*.5);
        if(shape>1.5)color=mix(vec3(.075,.012,.019),color,smoothstep(.21,.72,stream+hot*.42));
        finish(color+vec3(.95,.36,.03)*glint,1.0);
    } else finish(color,border*edge(uv)*.96);
}
