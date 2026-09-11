#version 150
#moj_import <fog.glsl>
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
in float vertexDistance;
in vec4 vertexColor;
in vec2 flowUv;
flat in float flowShape;
flat in float flowSeed;
out vec4 fragColor;

float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float noise(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.0-2.0*f);return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)),f.x),f.y);}
float flow(vec2 p){return noise(p)*.6+noise(p*2.03+3.7)*.27+noise(p*4.11-2.3)*.13;}

void main(){
    vec2 uv=flowUv,p=uv*2.0-1.0;
    float t=HemoTime*.035,seed=flowSeed*1.719;
    float n=flow(vec2(uv.x*8.0-t*1.5,uv.y*3.0)+seed);
    float alpha=0.0,shine=0.0;
    if(flowShape<.5){
        float bend=sin(uv.x*8.0-t+seed)*.08*sin(uv.x*3.14159);
        float d=abs(p.y-bend);
        float width=.18+.10*n;
        alpha=(1.0-smoothstep(width,width+.14,d))*smoothstep(0.0,.035,uv.x)*(1.0-smoothstep(.88,1.0,uv.x));
        shine=exp(-pow((p.y-bend+.09)*22.0,2.0))*(.28+.72*n);
    }else if(abs(flowShape-1.0)<.5){
        float density=flow(p*3.0+vec2(-t*.4,t*.13)+seed);
        alpha=(1.0-smoothstep(.15,.98,length(p)))*smoothstep(.27,.72,density)*.48;
        shine=density*.12;
    }else if(abs(flowShape-2.0)<.5){
        float radius=length(p);float spin=t*.2+radius*4.0;
        vec2 current=mat2(cos(spin),-sin(spin),sin(spin),cos(spin))*p;
        float liquid=flow(current*5.0+seed+t*.1);
        alpha=(1.0-smoothstep(.66+liquid*.08,.97,radius))*.82;
        shine=smoothstep(.57,.77,liquid)*.8;
    }else if(abs(flowShape-6.0)<.5){
        float r=length(p);alpha=exp(-pow((r-.76)*28.0,2.0))*(.5+.5*n);shine=.3;
    }else if(abs(flowShape-9.0)<.5){
        alpha=1.0;shine=smoothstep(.53,.83,n)*.8;
    }else{
        vec2 drop=p;drop.x*=1.0+p.y*.25;
        float radius=length(drop);
        alpha=1.0-smoothstep(.65,.98,radius);
        shine=exp(-dot(drop-vec2(-.24,.28),drop-vec2(-.24,.28))*20.0)*.85;
    }
    vec3 blood=mix(vec3(.20,.012,.042),vec3(.78,.045,.13),.35+n*.55);
    blood=mix(blood,vec3(1.0,.38,.43),shine*.68);
    alpha*=vertexColor.a*ColorModulator.a;
    if(alpha<.003)discard;
    fragColor=linear_fog(vec4(blood*vertexColor.rgb*ColorModulator.rgb,alpha),vertexDistance,FogStart,FogEnd,FogColor);
}
