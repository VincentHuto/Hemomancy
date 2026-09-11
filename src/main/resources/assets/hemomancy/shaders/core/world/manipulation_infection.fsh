#version 150
#moj_import <fog.glsl>
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
in float vertexDistance;
in vec4 vertexColor;
in vec2 bodyUv;
out vec4 fragColor;
float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float noise(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.0-2.0*f);return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)),f.x),f.y);}
void main(){
    // Red carries formation, green the status pattern. Alpha carries its server-timed presence.
    vec2 p=bodyUv*64.0;
    float style=floor(vertexColor.g*7.0+.5),growth=vertexColor.r;
    float n=noise(p*.32+style*3.1);
    vec2 warped=p+vec2(noise(p*.27+3.0),noise(p*.31+8.0))*2.2;
    float field=noise(warped*.70)*.60+noise(warped*1.43)*.28+noise(warped*2.91)*.12;
    float branches=(1.0-smoothstep(.014,.050,abs(field-.50)))*smoothstep(.22,.50,n);
    float fine=(1.0-smoothstep(.010,.025,abs(noise(warped*2.8)-.48)))*smoothstep(.42,.69,field);
    float wound=(1.0-smoothstep(.055,.21,abs(sin(p.x*.52+p.y*.16))))*smoothstep(.38,.68,n);
    float hypha=style>=3.0?max(branches,fine*.55):0.0;
    float pattern=style==1.0?smoothstep(.51,.72,field)*.82:style==2.0?branches*.75:wound*.72;
    float alpha=max(pattern,hypha*.85)*smoothstep(n-.25,n+.12,growth);
    float pulse=.82+.18*sin(HemoTime*(style==1.0?.14:.07)+p.y*.4);
    vec3 color=mix(vec3(.18,.018,.035),vec3(.45,.16,.085),n);
    color=mix(color,vec3(.63,.55,.34),hypha*.75);
    alpha*=vertexColor.a*pulse*ColorModulator.a;
    if(alpha<.008)discard;
    fragColor=linear_fog(vec4(color*ColorModulator.rgb,alpha),vertexDistance,FogStart,FogEnd,FogColor);
}
