#version 150
// Lux outpouring light: red-white light from each orb falls on the surfaces around it. No block light is placed; the
// scene is relit from its own depth. LightsA/LightsB hold one camera-relative orb per column (xyz, strength).
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 InvProj;
uniform mat4 InvView;
uniform mat4 LightsA;
uniform mat4 LightsB;
uniform float Count;
uniform float HemoTime;
in vec2 texCoord0;
out vec4 fragColor;

vec3 worldAt(vec2 uv,float depth) {
    vec4 view=InvProj*vec4(uv*2.0-1.0,depth*2.0-1.0,1.0);
    view/=view.w;
    return (InvView*vec4(view.xyz,1.0)).xyz;
}

void main() {
    vec4 scene=texture(Sampler0,texCoord0);
    float depth=texture(Sampler1,texCoord0).r;
    vec3 p=worldAt(texCoord0,depth);
    bool sky=depth>=.99999;
    vec3 n=normalize(cross(dFdx(p),dFdy(p)));
    if(dot(n,-p)<0.0) n=-n;
    vec3 surface=vec3(0), haze=vec3(0);
    int count=int(Count+.5);
    for(int i=0;i<8;i++) {
        if(i>=count) break;
        vec4 light=i<4?LightsA[i]:LightsB[i-4];
        vec3 toLight=light.xyz-p;
        float reach=length(toLight);
        float pulse=.5+.5*sin(HemoTime*.35+float(i)*1.7);
        vec3 colour=mix(vec3(1.0,.18,.20),vec3(1.0,.95,.92),pulse);
        if(!sky) {
            float atten=light.w/(1.0+reach*reach*1.2)*smoothstep(6.0,4.0,reach);
            float lambert=clamp((dot(n,toLight/max(reach,1e-4))+.35)/1.35,0.0,1.0);
            surface+=colour*atten*lambert;
        }
        // A soft bloom around the orb itself, along the view ray.
        vec3 dir=normalize(p);
        float along=max(dot(light.xyz,dir),0.0);
        float miss=length(light.xyz-dir*along);
        haze+=colour*light.w*exp(-miss*miss*6.0)*.18*step(along,length(p)+.5);
    }
    vec3 lit=scene.rgb+scene.rgb*surface*1.4+surface*.08+haze;
    fragColor=vec4(lit,1.0);
}
