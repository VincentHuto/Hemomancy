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
    vec3 tint = abs(shape - 3.0) < .5 ? vec3(1.0) : vertexColor.rgb;
    fragColor=linear_fog(vec4(color*tint*ColorModulator.rgb,clamp(alpha,0.0,1.0)),vertexDistance,FogStart,FogEnd,FogColor);
}
void main() {
    vec2 p=uv*2.0-1.0;
    float t=HemoTime*.065+seed*2.73;
    bool torch = abs(shape - 3.0) < .5;
    if(shape>1.5 && !torch) {
        float d=length(p*vec2(1.0,.7));
        finish(mix(vec3(.24,.003,.018),vec3(.82,.055,.065),exp(-d*d*9.0)),exp(-d*d*6.0)*edge(uv));
        return;
    }
    vec2 q=p*vec2(2.0,2.7)+vec2(seed*.51,-t*1.9);
    float curl=(fbm(q*.7+vec2(t*.18,0))-.5)*(.28+uv.y*.85);
    q.x+=curl*2.0;
    float n=fbm(q),fine=fbm(q*2.4+vec2(.4,-t*.65));
    if(shape>.5 && !torch) {
        float density=fbm(vec2(p.x*2.5+curl,p.y*2.1-t*.42));
        float mask=exp(-pow((p.x+curl)*1.7,2.0))*smoothstep(.26,.73,density);
        finish(mix(vec3(.012,.008,.012),vec3(.085,.018,.027),density),mask*edge(uv)*.65*(1.0-uv.y*.6));
        return;
    }
    float width=(.76-uv.y*.56)*(0.6+n*.55);
    // Curve inward at the root instead of exposing the billboard's bottom edge.
    float root=sqrt(smoothstep(0.0,.24,uv.y));
    float body=width-abs(p.x+curl*.6)+(n-.5)*.35;
    float tongues=body+(fine-.5)*.28-uv.y*.14;
    float rootMask=smoothstep(-.035,.035,width*root-abs(p.x+curl*.6*root));
    float alpha=smoothstep(-.06,.12,tongues)*rootMask*smoothstep(.08,.28,1.0-uv.y)*edge(uv);
    float heat=clamp(body*1.5+(1.0-uv.y)*.46+(fine-.5)*.25,0.0,1.0);
    vec3 color=mix(vec3(.16,.002,.018),vec3(.65,.012,.035),smoothstep(.0,.3,heat));
    color=mix(color,vec3(.9,.055,.075),smoothstep(.24,.65,heat));
    color=mix(color,vec3(1.0,.70,.76),pow(heat,6.0)*.55);
    if (torch) {
        color=mix(vec3(.20,.002,.03),vec3(.98,.04,.16),smoothstep(.05,.55,heat));
        color=mix(color,vec3(1.0,.29,.41),smoothstep(.45,.8,heat));
        color=mix(color,vec3(1.0,.70,.76),pow(heat,6.0)*.75);
    }
    float soot=smoothstep(.44,.82,uv.y+(fine-.5)*.18);
    if (torch) soot=max(soot, smoothstep(.52,.94,vertexColor.r));
    color=mix(color,vec3(.014,.006,.011),soot*.98);
    finish(color,alpha*.94);
}
