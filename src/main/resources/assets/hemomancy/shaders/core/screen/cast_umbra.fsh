#version 150
// Tenebris outpouring: the light drains from around the caster and black-violet fog rolls through, hiding them and the
// blocks near them. LightsA/LightsB hold one camera-relative sphere per column (xyz, radius); ShadeInfo holds
// (strength, self) pairs. The caster's own first-person view gets SelfShade strength and stays clear but for the corners.
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 InvProj;
uniform mat4 InvView;
uniform mat4 LightsA;
uniform mat4 LightsB;
uniform mat4 ShadeInfo;
uniform float Count;
uniform float SelfShade;
uniform float HemoTime;
in vec2 texCoord0;
out vec4 fragColor;

vec3 worldAt(vec2 uv,float depth) {
    vec4 view=InvProj*vec4(uv*2.0-1.0,depth*2.0-1.0,1.0);
    view/=view.w;
    return (InvView*vec4(view.xyz,1.0)).xyz;
}
float hash1(vec3 p) { return fract(sin(dot(p,vec3(12.9898,78.233,37.719)))*43758.5453); }
float smoothNoise(vec3 p) {
    vec3 i=floor(p),f=fract(p); f=f*f*(3.0-2.0*f);
    float a=mix(mix(hash1(i),hash1(i+vec3(1,0,0)),f.x),mix(hash1(i+vec3(0,1,0)),hash1(i+vec3(1,1,0)),f.x),f.y);
    float b=mix(mix(hash1(i+vec3(0,0,1)),hash1(i+vec3(1,0,1)),f.x),mix(hash1(i+vec3(0,1,1)),hash1(i+vec3(1,1,1)),f.x),f.y);
    return mix(a,b,f.z);
}
float fbm(vec3 p) { return smoothNoise(p)*.55+smoothNoise(p*2.03+5.1)*.3+smoothNoise(p*4.07-2.3)*.15; }

void main() {
    vec4 scene=texture(Sampler0,texCoord0);
    float depth=texture(Sampler1,texCoord0).r;
    vec3 p=worldAt(texCoord0,depth);
    float sceneReach=depth>=.99999?512.0:length(p);
    vec3 dir=normalize(p);
    // The caster must still see to fight: their own fog keeps to the corners as a thin vignette.
    float clearCentre=smoothstep(.3,.72,length(texCoord0-.5));
    vec3 rgb=scene.rgb;
    int count=int(Count+.5);
    for(int i=0;i<8;i++) {
        if(i>=count) break;
        vec4 sphere=i<4?LightsA[i]:LightsB[i-4];
        vec4 info=ShadeInfo[i/2];
        float strength=(i-(i/2)*2)==0?info.x:info.z;
        bool self=((i-(i/2)*2)==0?info.y:info.w)>.5;
        float scale=self?SelfShade*clearCentre:1.0;
        float radius=sphere.w;
        // Surfaces inside the sphere lose their light and colour.
        float dark=(1.0-smoothstep(radius*.35,radius,length(p-sphere.xyz)))*strength*.88*scale;
        float grey=dot(rgb,vec3(.299,.587,.114));
        rgb=mix(rgb,mix(vec3(grey),vec3(.02,0.0,.04),.7),dark);
        // Fog: march the stretch of the view ray inside the sphere, stopping at the scene.
        float b=dot(dir,sphere.xyz), c=dot(sphere.xyz,sphere.xyz)-radius*radius, disc=b*b-c;
        if(disc>0.0) {
            float root=sqrt(disc);
            float enter=max(b-root,0.0), leave=min(b+root,sceneReach);
            if(leave>enter) {
                float fog=0.0, stepLength=(leave-enter)/8.0;
                for(int s=0;s<8;s++) {
                    vec3 at=dir*(enter+(float(s)+.5)*stepLength);
                    vec3 local=at-sphere.xyz;
                    // Heavy fog: it sinks to the ground around the caster and rolls slowly downward and outward.
                    float body=max(0.0,1.0-length(local*vec3(1.0,1.4,1.0))/radius)*smoothstep(radius*.55,-radius*.45,local.y);
                    float roll=fbm(local*.9+vec3(0.0,HemoTime*.012,0.0));
                    fog+=body*smoothstep(.35,.8,roll)*stepLength;
                }
                float amount=clamp(fog*1.3*strength*scale,0.0,.8);
                // Thin fog at the edges glows the crimson of the blood it came from; the dense heart stays black-violet.
                vec3 ink=mix(vec3(.42,.03,.07),vec3(.05,.01,.08),smoothstep(.12,.55,amount));
                rgb=mix(rgb,ink,amount);
            }
        }
    }
    fragColor=vec4(rgb,1.0);
}
