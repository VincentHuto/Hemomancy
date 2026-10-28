// Layered decay field; requires flow_noise.glsl.
float rot(vec2 p){return noise(p)*.58+noise(p*2.07+3.1)*.28+noise(p*4.17)*.14;}
