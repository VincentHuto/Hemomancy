// Smooth 3D value noise for the cast veins; needs vein_hash1.
float smoothNoise(vec3 p) {
    vec3 i=floor(p),f=fract(p); f=f*f*(3.0-2.0*f);
    float a=mix(mix(hash1(i),hash1(i+vec3(1,0,0)),f.x),mix(hash1(i+vec3(0,1,0)),hash1(i+vec3(1,1,0)),f.x),f.y);
    float b=mix(mix(hash1(i+vec3(0,0,1)),hash1(i+vec3(1,0,1)),f.x),mix(hash1(i+vec3(0,1,1)),hash1(i+vec3(1,1,1)),f.x),f.y);
    return mix(a,b,f.z);
}
