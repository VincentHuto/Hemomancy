package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.phys.Vec3;

/** Analytic drag keeps particle movement identical at every render frame rate. */
final class ThermalMotion {
    private ThermalMotion() {}
    static Vec3 drift(Vec3 velocity,double age,double drag,double gravity) {
        double t=Math.max(0,age);
        double travel=drag==0?t:-Math.expm1(-drag*t)/drag;
        return velocity.scale(travel).add(0,-gravity*t*t*.5,0);
    }
    static double random(int seed,int slot) {
        long n=(seed*0x9E3779B9L+slot*0x85EBCA6BL)^0xC2B2AE35L;
        n=(n^(n>>>16))*0x45d9f3bL;
        return ((n^(n>>>16))&0xffffff)/16777216.0;
    }
}
