package com.vincenthuto.hemomancy.common.particle;

import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

/** Particle colours shared across files. Verdigris is Unstained only; blood-side work never takes a copper tone. */
public final class HemoPalette {
    private HemoPalette() {}

    public static final ParticleColor VERDIGRIS = new ParticleColor(86, 160, 132);
    public static final ParticleColor STILL_WHITE = new ParticleColor(244, 246, 248);
    public static final ParticleColor STILL_SILVER = new ParticleColor(201, 209, 217);
    public static final ParticleColor PALE_GLOW = new ParticleColor(214, 232, 236);
}
