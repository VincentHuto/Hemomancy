package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.VampireSquidEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class VampireSquidModel extends HierarchicalModel<VampireSquidEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("vampire_squid"), "main");
    private final ModelPart root, body;
    private final ModelPart[][] arms = new ModelPart[8][4];

    public VampireSquidModel(ModelPart root) {
        this.root = root;
        body = root.getChild("body");
        for (int arm = 0; arm < arms.length; arm++) {
            var parent = body.getChild("ring" + arm);
            for (int joint = 0; joint < arms[arm].length; joint++) {
                parent = parent.getChild("arm" + arm + "_" + joint);
                arms[arm][joint] = parent;
            }
        }
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(VampireSquidEntity entity, float limbSwing, float limbAmount,
                                    float age, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float partialTick = Mth.clamp(age - entity.tickCount, 0, 1);
        float cloak = entity.cloakAmount(partialTick);
        body.xRot = entity.swimmingPose().leanRadians(partialTick);
        body.yRot = Mth.wrapDegrees(entity.swimmingPose().headingDegrees(partialTick)
                - Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot)) * Mth.DEG_TO_RAD;
        body.y += Mth.sin(age * .08F) * .25F * (1 - cloak);
        float turn = Mth.clamp(Mth.wrapDegrees(entity.yBodyRotO - entity.yBodyRot) * Mth.DEG_TO_RAD, -.3F, .3F);
        body.getChild("fin_left").zRot += Mth.sin(age * .18F) * .22F;
        body.getChild("fin_right").zRot -= Mth.sin(age * .18F) * .22F;
        for (int arm = 0; arm < arms.length; arm++) for (int joint = 0; joint < arms[arm].length; joint++) {
            // Naeglerophaeon's traveling curl: each connected joint receives the pulse later.
            float wave = VampireSquidPose.wave(age, arm, joint);
            arms[arm][joint].xRot = VampireSquidPose.armAngle(age, arm, joint, cloak);
            arms[arm][joint].zRot = (wave * .035F + turn * joint * .2F) * (1 - cloak);
        }
    }
}
