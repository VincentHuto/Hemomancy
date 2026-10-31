// Cellular distance for the vein shaders; needs vein_hash3. The distance to the nearest cell point and the gap to the
// second: thin gaps trace a branching vessel network (the Cardinal floor membrane's cellular(), lifted into 3D).
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
