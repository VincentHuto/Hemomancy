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
void main() {
    float time=HemoTime*.055;
    float alpha,core;
    if(shape<.5) {
        float flow=fract(uv.x-time+seed*.173);
        float pulse=exp(-pow((flow-.5)*9.0,2.0));
        float center=.5+sin(uv.x*16.0+time*.8+seed)*.045;
        float d=abs(uv.y-center);
        float aa=max(.008,fwidth(uv.y)*.7);
        core=1.0-smoothstep(.028-aa,.028+aa,d);
        alpha=(core*.78+exp(-d*d*70.0)*(.10+pulse*.3))
            *smoothstep(0.0,.035,uv.x)*smoothstep(0.0,.035,1.0-uv.x);
        alpha*=.68+pulse*.5;
    } else if(shape<1.5) {
        vec2 p=uv*2.0-1.0;
        float density=noise(p*2.8+vec2(time*.18,-time*.24)+seed)*.65+noise(p*6.5-vec2(time*.2,time*.12)+seed)*.35;
        alpha=(1.0-smoothstep(.1,1.0,length(p)))*smoothstep(.28,.65,density)*.28;
        core=0.0;
    } else if(shape<2.5) {
        float x=uv.x+noise(vec2(uv.y*7.0,seed+floor(time*3.0)))*.19;
        float d=abs(fract(x*3.0)-.5);
        core=exp(-d*d*1800.0);
        alpha=(core*.8+exp(-d*d*65.0)*.12)*(.35+.65*pow(max(0.0,sin(time*3.0+uv.y*11.0+seed)),4.0));
        alpha*=smoothstep(0.0,.05,min(min(uv.x,1.0-uv.x),min(uv.y,1.0-uv.y)));
    } else if(shape<3.5) {
        vec2 p=uv*2.0-1.0;
        float d=length(p);
        core=exp(-d*d*90.0);alpha=core+exp(-d*d*9.0)*.28;
    } else {
        float flow=noise(vec2(uv.x*7.0-time*2.0,uv.y*2.0)+seed);
        float center=.5+sin(uv.x*11.0-time+seed)*.08;
        alpha=exp(-pow((uv.y-center)*5.5,2.0))*(.3+flow*.7);
        alpha*=smoothstep(0.0,.08,uv.x)*smoothstep(0.0,.08,1.0-uv.x);
        core=0.0;
    }
    alpha*=vertexColor.a*ColorModulator.a;
    if(alpha<.002)discard;
    vec3 color=mix(vertexColor.rgb,vec3(1.0,.98,.87),core*.65)*ColorModulator.rgb;
    fragColor=linear_fog(vec4(color,clamp(alpha,0.0,1.0)),vertexDistance,FogStart,FogEnd,FogColor);
}
