// Three-octave smooth noise for the vein shaders; needs vein_noise.
float fbm(vec3 p) { return smoothNoise(p)*.57+smoothNoise(p*2.03+7.1)*.28+smoothNoise(p*4.11-3.7)*.15; }
