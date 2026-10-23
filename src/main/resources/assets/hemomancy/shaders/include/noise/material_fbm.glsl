// Three-octave noise for the solid school materials; needs material_noise.
float fbm(vec2 p) { return noise(p)*.57+noise(p*2.03+7.1)*.28+noise(p*4.09-3.2)*.15; }
