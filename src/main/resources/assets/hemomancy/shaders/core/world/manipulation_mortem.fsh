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
float rot(vec2 p){return noise(p)*.58+noise(p*2.07+3.1)*.28+noise(p*4.17)*.14;}
void main(){
    vec2 uv=flowUv,p=uv*2.0-1.0;
    float t=HemoTime*.011,seed=flowSeed*1.719;
    float n=rot(p*4.0+vec2(-t*.7,t*.12)+seed);
    float crust=smoothstep(.46,.68,rot(p*10.0+seed));
    float alpha=0.0,hypha=0.0;
    if(flowShape<.5){
        float d=abs(p.y-(n-.5)*.18);
        alpha=(1.0-smoothstep(.15,.42,d))*(.65+.35*n)*smoothstep(0.0,.035,uv.x)*(1.0-smoothstep(.87,1.0,uv.x));
    }else if(abs(flowShape-1.0)<.5){
        vec2 warp=vec2(n,rot(p*2.0-t*.2+seed+4.0))-.5;
        alpha=(1.0-smoothstep(.25,.99,length(p+warp*.25)))*smoothstep(.27,.73,n)*.54;
    }else if(abs(flowShape-2.0)<.5 || abs(flowShape-8.0)<.5){
        float r=length(p);alpha=(1.0-smoothstep(.57+n*.2,.98,r))*(.80+.18*n);
        // Fine irregular capillaries grow over the sludge; there is no raised tube geometry.
        vec2 root=p*10.0+seed;
        float branch=abs(rot(root+vec2(noise(root*.6),noise(root*.7+8.0)))-.5);
        hypha=(1.0-smoothstep(.018,.06,branch))*smoothstep(.40,.62,n);
        if(flowShape<7.5)hypha*=.12;
    }else if(abs(flowShape-6.0)<.5){
        alpha=exp(-pow((length(p)-.75-(n-.5)*.07)*25.0,2.0))*(.5+.5*n);
    }else if(abs(flowShape-7.0)<.5){
        float height=uv.y;
        float radius=.23+.55*pow(1.0-height,2.2);
        float shell=1.0-smoothstep(.025,.095,abs(abs(p.x)-radius));
        float rim=exp(-pow((height-.09)*45.0,2.0))*(1.0-smoothstep(.7,.91,abs(p.x)));
        alpha=max(shell*.8,rim)*(.40+.6*n);
        hypha=smoothstep(.53,.7,n)*.45;
    }else{
        alpha=(1.0-smoothstep(.3,.96,length(p)))*(.55+.45*n);
        hypha=flowShape>9.5?.8:0.0;
    }
    vec3 color=mix(vec3(.095,.023,.025),vec3(.34,.13,.075),crust*.75+n*.25);
    color=mix(color,vec3(.59,.52,.32),hypha*.65);
    alpha*=vertexColor.a*ColorModulator.a;
    if(alpha<.003)discard;
    fragColor=linear_fog(vec4(color*vertexColor.rgb*ColorModulator.rgb,alpha),vertexDistance,FogStart,FogEnd,FogColor);
}
