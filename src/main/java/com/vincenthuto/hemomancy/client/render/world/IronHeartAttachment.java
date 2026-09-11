package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

final class IronHeartAttachment {
    private IronHeartAttachment() {}

    static void apply(PoseStack poses, float bodyYaw, float scale, boolean crouching) {
        poses.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
        poses.scale(scale, scale, scale);
        // Positive local X is the wearer's left when facing Minecraft's positive Z.
        poses.translate(.12, crouching ? 1.12 : 1.35, crouching ? .42 : .15);
    }
}
