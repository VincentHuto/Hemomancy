package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

/** Still Art casting accents. Harbinger casts draw through the outpouring package. */
public final class CastingAnatomyRenderer {
    private CastingAnatomyRenderer() {}
    /**
     * A thin pale ring around the casting hand: white core, silver edge, no blood tint. It pulses once across the release
     * and dissolves (fading, then breaking into gaps) through the settle. First person faces the camera; third person
     * circles the wrist.
     */
    public static void stillHand(PoseStack p,MultiBufferSource buffer,CastingAnimationPacket packet,float time,float alpha,boolean first) {
        float fade=alpha*Math.clamp(time/2,0,1)*(1-Math.clamp((time-8)/22,0,1));
        if(fade<=.01F)return;
        // Untextured and unlit: the textured LUX material sampled a dark texel and turned the ring charcoal.
        StillArtRings.hand(p,buffer.getBuffer(StillArtRings.renderType()),time,fade,first);
    }
    /**
     * A flat still-water ring at the feet for Discerning and above: radius 0.6 + 0.25 per stage, expanding once over the
     * first 8 ticks and fading. Decorative, so Minimal particles and distance remove it; the hand ring stays.
     */
    public static void stillGround(PoseStack p,MultiBufferSource buffer,CastingAnimationPacket packet,float time,float alpha,double distanceSq) {
        if(alpha<=0 || packet.stage()==null || packet.stage()==EnumClarityStage.AWAKENED)return;
        int setting=Minecraft.getInstance().options.particles().get().getId();
        if(setting==2 || distanceSq>32*32)return;
        float fade=alpha*.7F*Math.clamp(time/1.5F,0,1)*(1-Math.clamp((time-6)/20,0,1));
        if(fade<=.01F)return;
        StillArtRings.ground(p,buffer.getBuffer(StillArtRings.renderType()),packet.stage().ordinal(),time,fade);
    }
}
