package com.vincenthuto.hemomancy.client.player;

/** A pose that can be interpolated and offset; keeps track sampling free of Minecraft types. */
public interface Blendable<P extends Blendable<P>> {
    P blend(P other, float t);
    /** this + other * scale, component-wise. */
    P offset(P other, float scale);
}
