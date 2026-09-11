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
    if(shape<.5 && hash(floor(gl_FragCoord.xy))>vertexColor.a)discard;
    vec2 p=uv*2.0-1.0;
    float t=HemoTime*.007+seed*.77;
    if(abs(shape-1.0)<.5) {
        vec2 q=p*3.0+vec2(t*.3,-t*.18);
        float powder=fbm(q*3.0);
        float grains=noise(q*29.0);
        float alpha=(1.0-smoothstep(.12,1.0,length(p)))*smoothstep(.28,.63,powder)*(.3+grains*.7);
        finish(mix(vec3(.45,.28,.36),vec3(.86,.92,.93),powder),alpha*edge(uv)*.64);return;
    }
    float vein=fissure(uv*2.6+seed);
    float crack=1.0-smoothstep(.008,.055,vein);
    float grain=fbm(uv*3.2+seed);
    float dust=noise(uv*39.0+seed);
    float frost=smoothstep(.51,.75,grain+crack*.09+(dust-.5)*.19);
    float branch=(1.0-smoothstep(.006,.028,fissure(uv*7.8+seed+23.0)))
            *(1.0-smoothstep(.03,.18,vein));
    if(abs(shape-2.0)<.5) {
        float reveal=smoothstep(.1,.72,vertexColor.a);
        float spread=1.0-smoothstep(reveal*.82,reveal+.12,length(p));
        finish(mix(vec3(.25,.035,.09),vec3(.8,.89,.91),frost*.8+crack*.2),spread*edge(uv)*(crack*.75+frost*.27));return;
    }
    vec3 normal=normalize(cross(dFdx(viewPosition),dFdy(viewPosition)));
    float facing=abs(dot(normal,normalize(-viewPosition)));
    float lighting=.5+.5*abs(dot(normal,normalize(vec3(.3,.85,-.2))));
    vec3 color=mix(vec3(.065,.009,.026),vec3(.27,.035,.078),grain)*lighting;
    vec3 frozenPowder=mix(vec3(.32,.41,.46),vec3(.82,.88,.9),dust)*lighting;
    color=mix(color,frozenPowder,frost*.9);
    color+=vec3(.33,.42,.47)*(crack*.3+branch*.17);
    color+=vec3(.25,.38,.43)*pow(1.0-facing,4.0)*.3;
    finish(color,shape>3.5?.18+frost*.38+crack*.22:1.0);
}
