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
// Casters holds up to four camera-relative caster feet positions (xyz, unused w), one per column. The caster's own body
// stays readable: 1 on a standing player's silhouette, easing to 0 a little beyond it.
uniform mat4 Casters;
uniform float CasterCount;
float bodyCover(vec3 q) {
    float best=0.0;
    int casters=int(CasterCount+.5);
    for(int i=0;i<4;i++) {
        if(i>=casters) break;
        vec3 d=q-Casters[i].xyz;
        float spine=clamp(d.y,0.0,1.85);
        float r=length(vec3(d.x,d.y-spine,d.z));
        best=max(best,1.0-smoothstep(.4,.75,r));
    }
    return best;
}

out vec4 fragColor;

// Rolls highlights off toward white instead of clipping them, so relit skin keeps its shading.
float knee(float c) { return c<.75?c:.75+.25*(1.0-exp(-(c-.75)/.25)); }
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
    // The orbs bud from the caster's own veins: relighting that body bleached it white, so it takes only a trace.
    float body=sky?0.0:bodyCover(p);
    surface*=1.0-.85*body;
    haze*=1.0-.75*body;
    vec3 lit=scene.rgb+scene.rgb*surface*1.4+surface*.08+haze;
    lit=vec3(knee(lit.r),knee(lit.g),knee(lit.b));
    fragColor=vec4(lit,1.0);
}
