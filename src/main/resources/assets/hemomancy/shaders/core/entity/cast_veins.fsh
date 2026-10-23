#version 150
#moj_import <fog.glsl>
// Cast outpouring veins. The body atlas turns each skin texel into a point on a standing body, so the veins grow in
// body space from the vascular section: continuous across UV seams and across the armour shell. One vein value per
// texel keeps them at the skin's 16 px/block density.
uniform sampler2D Sampler0;
uniform float HemoTime;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
in float vertexDistance;
in vec2 skinUv;
flat in vec4 encoded;
flat in ivec2 overlayData;
flat in ivec2 lightData;
out vec4 fragColor;

#moj_import <hemomancy:noise/vein_hash3.glsl>
#moj_import <hemomancy:noise/vein_hash1.glsl>
#moj_import <hemomancy:noise/vein_noise.glsl>
float fbm(vec3 p) { return smoothNoise(p)*.57+smoothNoise(p*2.03+7.1)*.28+smoothNoise(p*4.11-3.7)*.15; }
vec3 axisOf(int code) {
    if(code==0) return vec3(1,0,0);
    if(code==1) return vec3(-1,0,0);
    if(code==2) return vec3(0,1,0);
    if(code==3) return vec3(0,-1,0);
    if(code==4) return vec3(0,0,1);
    return vec3(0,0,-1);
}

// Distance to the nearest cell point and the gap to the second: thin gaps trace a branching vessel network
// (the Cardinal floor membrane's cellular(), lifted into three dimensions).
vec2 cellular(vec3 p) {
    vec3 cell=floor(p),local=fract(p);
    float nearest=8.0,second=8.0;
    for(int x=-1;x<=1;x++) for(int y=-1;y<=1;y++) for(int z=-1;z<=1;z++) {
        vec3 o=vec3(x,y,z);
        float d=length(o+.15+.7*hash3(cell+o)-local);
        if(d<nearest){second=nearest;nearest=d;} else if(d<second) second=d;
    }
    return vec2(nearest,second-nearest);
}
float line(float gap,float width) { return 1.0-smoothstep(width,width+.06,gap); }

// Exit sites per vascular section (EnumVeinSections order: HEAD, ARMS, HEART, BODY, LEGS), in body pixels.
float exitDistance(int section,vec3 p,bool offHand,bool leftMain) {
    float d=1000.0;
    if(section==0) {
        d=min(length(p-vec3(-2,28,4)),length(p-vec3(2,28,4)));
        d=min(d,length(p-vec3(0,27,4)));
        d=min(d,min(length(p-vec3(-4,27,0)),length(p-vec3(4,27,0))));
    } else if(section==1) {
        float main=leftMain?6.0:-6.0;
        d=length(p-vec3(main,12.5,0));
        if(offHand) d=min(d,length(p-vec3(-main,12.5,0)));
    } else if(section==2) {
        d=length(p-vec3(1.5,19,2));
    } else if(section==3) {
        d=min(length(p-vec3(0,17,0)),min(length(p-vec3(-4,16,0)),length(p-vec3(4,16,0))));
    } else {
        d=min(min(length(p-vec3(-2,6,1)),length(p-vec3(2,6,1))),min(length(p-vec3(-2,2,1)),length(p-vec3(2,2,1))));
    }
    return d;
}

// School order: ANIMUS, FLAMMEUS, DUCTILIS, LUX, MORTEM, CONGEATIO, FERRIC, TENEBRIS.
// Each vessel has a deep outer wall and a hot core where the blood runs fastest.
vec3 veinOuter(int school) {
    if(school==0) return vec3(.44,.0,.05);
    if(school==1) return vec3(.58,.07,.03);
    if(school==2) return vec3(.58,.32,.08);
    if(school==3) return vec3(.86,.16,.22);
    if(school==4) return vec3(.15,.17,.05);
    if(school==5) return vec3(.46,.05,.12);
    if(school==6) return vec3(.30,.08,.07);
    return vec3(.06,.0,.09);
}
vec3 veinInner(int school) {
    if(school==0) return vec3(1.0,.24,.20);
    if(school==1) return vec3(1.0,.80,.32);
    if(school==2) return vec3(1.0,.96,.68);
    if(school==3) return vec3(1.0,.74,.72);
    if(school==4) return vec3(.56,.62,.20);
    if(school==5) return vec3(.80,.93,1.0);
    if(school==6) return vec3(.54,.50,.52);
    return vec3(.50,.10,.58);
}
vec3 contactColor(int school,float grain) {
    if(school==0) return mix(vec3(.30,.0,.03),vec3(.62,.03,.07),grain);
    if(school==1) return grain>.86?vec3(1.0,.45,.12):vec3(.07,.03,.02);
    if(school==2) return grain>.9?vec3(1.0,.95,.75):vec3(.09,.08,.07);
    if(school==3) return mix(vec3(1.0,.70,.66),vec3(1.0,.96,.94),grain);
    if(school==4) return mix(vec3(.13,.16,.05),vec3(.29,.24,.08),grain);
    if(school==5) return mix(vec3(.62,.30,.36),vec3(.90,.95,1.0),smoothstep(.3,.7,grain));
    if(school==6) return mix(vec3(.16,.13,.13),vec3(.36,.12,.10),grain*grain);
    return vec3(.03,.0,.05);
}

void main() {
    // The atlas holds each texel's centre on the body and the body axes its u and v run along, so the exact point
    // inside the texel is rebuilt here: veins resolve at screen resolution, not skin pixels.
    vec2 texel=skinUv*64.0;
    vec2 cell=floor(texel);
    vec4 atlas=texture(Sampler0,(cell+.5)/64.0);
    if(atlas.a<.002) discard;
    int axes=int(atlas.a*255.0+.5)-1;
    vec2 inside=texel-cell-.5;
    vec3 body=vec3(atlas.r*16.0-8.0,atlas.g*32.0,atlas.b*8.0-4.0)+axisOf(axes/6)*inside.x+axisOf(axes-(axes/6)*6)*inside.y;
    vec4 c=floor(encoded*255.0+.5);
    float fade=c.a/255.0, strength=c.r/255.0;
    int school=int(c.g)/8, section=int(c.g)-school*8;
    int secondary=int(c.b)/8, rank=int(c.b)-secondary*8;
    float contact=float(overlayData.x)/1000.0, seed=float(overlayData.y);
    bool offHand=(lightData.x&1)==1, leftMain=(lightData.x&2)==2;

    float reach=rank==0?.25:rank==1?.4:rank==2?.6:rank==3?.8:1.0;
    float d=exitDistance(section,body,offHand,leftMain);
    float radius=strength*(4.0+26.0*reach);
    float growth=1.0-smoothstep(radius-2.0,radius,d);

    // Arteries and capillaries as the floor membrane draws them: a warped web that swells and pinches along its length,
    // thinning toward the edge of its reach, with a dark vessel wall under each line.
    // The cast's seed picks the whole network: where the warp folds, how it turns and how long the vessels run. Vessels
    // stretch along the limbs and are warped twice, so they wander like real veins rather than tiling.
    vec3 offset3=vec3(seed*.731,seed*.419,seed*.257);
    float turn=seed*.0377;
    mat3 spin=mat3(cos(turn),0.0,sin(turn), 0.0,1.0,0.0, -sin(turn),0.0,cos(turn));
    vec3 warp=vec3(fbm(body*.07+offset3+3.1),fbm(body*.07+offset3+7.7),fbm(body*.07+offset3+11.3))-.5;
    vec3 fine=vec3(fbm(body*.23+offset3.zxy),fbm(body*.23+offset3.yzx+5.0),fbm(body*.23+offset3+9.0))-.5;
    float stretch=.055+.025*fract(seed*.0071);
    vec3 tissue=spin*(body*vec3(.09,stretch,.09))+warp*1.7+fine*.45+offset3*.1;
    vec2 vessels=cellular(tissue);
    vec2 capillaries=cellular(tissue*2.6+11.0);
    float girth=fbm(tissue*1.3+4.0);
    float thin=clamp(d/max(radius,1.0),0.0,1.0);
    float thinning=mix(1.0,.45,thin);
    float arteryWidth=(.008+girth*.05)*thinning;
    float artery=line(vessels.y,arteryWidth);
    float capillary=line(capillaries.y,(.006+girth*.02)*thinning)*smoothstep(.3,.62,fbm(tissue*.55+9.0))*.6*step(d,radius*.75);
    float vein=clamp(artery+capillary,0.0,1.0)*growth;
    float wall=(1.0-smoothstep(0.0,.14,vessels.y))*growth*(1.0-artery);
    // Blood pushes outward from the exit in beats.
    float beat=.5+.5*sin(d*.55-HemoTime*(school==0?1.6:school==3?.55:school==2?2.6:.9));
    float bloodGrain=.8+.4*fbm(tissue*8.0);

    // The core sits in the middle of the arteries, swells with each beat and streams outward from the exit;
    // capillaries are mostly wall.
    float flow=.7+.3*smoothNoise(tissue*5.0-vec3(0,HemoTime*.18,0)+d*.08);
    float core=(1.0-smoothstep(0.0,(arteryWidth+.06)*(.42+.12*beat),vessels.y))*flow*(1.0-thin*.5);
    core=clamp(max(core,capillary*.25),0.0,1.0);
    vec3 outer=veinOuter(school), inner=veinInner(school);
    // Lux veins stay blood-red and throb white at each beat rather than washing the body out.
    if(school==3) inner=mix(inner,vec3(1.0,.97,.95),pow(beat,3.0)*.8);
    if(school==5) outer=mix(outer,vec3(.62,.30,.36),smoothstep(.4,.6,fbm(tissue*4.0+3.0))*.5);
    if(school==6) outer=mix(outer,vec3(.56,.10,.12),.3*beat);
    if(secondary<8 && fbm(tissue*1.7+20.0)<.38) {
        outer=mix(outer,veinOuter(secondary),.7);
        inner=mix(inner,veinInner(secondary),.7);
    }
    vec3 colour=mix(outer,inner,core)*bloodGrain;
    float glow=.8+.2*beat;
    if(school==1) glow=.6+.4*fbm(tissue*3.0+vec3(0,HemoTime*.12,0));
    if(school==2) glow=.35+.65*step(.82,fract(d*.31-HemoTime*.45));
    if(school==3) glow=1.0;
    if(school==7) glow=.55+.45*fbm(body*.4+vec3(0,HemoTime*.06,0));
    // Only the core carries the light; the wall stays dark so the line reads as a tube.
    glow=mix(.85,glow,core);

    // Frost, scorch, corrosion and the rest creep over the body from the same section during the release.
    float creep=contact*9.0;
    float edge=fbm(body*.35+seed*.01)*4.0;
    float cover=(1.0-smoothstep(creep-3.0,creep,d+edge))*step(.001,contact);
    // The crust lands in patches, thickest at the exit, so the skin still shows through.
    float clump=smoothstep(.52,.68,fbm(body*.45+seed*.017)+(1.0-clamp(d/max(creep,1.0),0.0,1.0))*.25);
    float grain=fbm(body*.9+seed*.03);
    vec3 crust=contactColor(school,grain);
    float crustAlpha=cover*clump*(school==3?.12:school==7?.5:.4);

    float veinAlpha=vein*(school==7?.85:.95)*(1.0-thin*.35);
    float shadow=wall*.28;
    float alpha=max(max(veinAlpha,crustAlpha),shadow)*fade*ColorModulator.a;
    if(alpha<.01) discard;
    vec3 under=crustAlpha>shadow?crust:vec3(.14,.0,.03);
    vec3 rgb=mix(under,colour*glow,clamp(veinAlpha/max(alpha,.001),0.0,1.0));
    fragColor=linear_fog(vec4(rgb*ColorModulator.rgb,alpha),vertexDistance,FogStart,FogEnd,FogColor);
}
