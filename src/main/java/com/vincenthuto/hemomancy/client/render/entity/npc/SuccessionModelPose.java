package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.common.succession.ProfessionalHarbingerEntity;
import net.minecraft.client.model.geom.ModelPart;

public final class SuccessionModelPose {
    private SuccessionModelPose() {}
    public static void apply(net.minecraft.world.entity.Entity entity, ModelPart head, ModelPart body,
            ModelPart leftArm, ModelPart rightArm, ModelPart leftLeg, ModelPart rightLeg) {
        if (!(entity instanceof ProfessionalHarbingerEntity npc)) return;
        long seed = npc.appearanceSeed();
        float breadth = npc.isSuccessor() ? .94f + Math.floorMod(seed, 5) * .03f : 1;
        head.xScale = breadth; head.zScale = breadth; body.xScale = 2 - breadth;
        leftArm.yScale = rightArm.yScale = leftLeg.yScale = rightLeg.yScale = 1;
        body.zRot = 0; head.zRot = 0; body.xRot = 0;
        if (npc.isMisbegotten()) {
            leftArm.yScale = 1.28f; rightArm.yScale = .78f; leftLeg.yScale = 1.13f; rightLeg.yScale = .92f;
            body.zRot = .09f; head.zRot = -.17f;
            if (npc.isShiftKeyDown()) body.xRot = .22f;
            if (!npc.getMainHandItem().isEmpty()) rightArm.xRot = -.7f;
            if (npc.swinging) { rightArm.xRot = -1.8f; leftArm.zRot = .55f; }
        }
    }
}
