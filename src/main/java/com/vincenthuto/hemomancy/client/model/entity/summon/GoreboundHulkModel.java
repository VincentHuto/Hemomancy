package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;

public class GoreboundHulkModel extends EntityModel<GoreboundHulkEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("gorebound_hulk"), "main");
    private final ModelPart root, chest, head, jaw;
    private final ModelPart[] arms = new ModelPart[2], forearms = new ModelPart[2], hands = new ModelPart[2];
    private final ModelPart[] thighs = new ModelPart[2], shins = new ModelPart[2], feet = new ModelPart[2];
    /** The strike windup replayed from its synced counter, so a late packet never stalls or skips the pose. */
    private final BossReplayClock strikeClock = new BossReplayClock();

    public GoreboundHulkModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        ModelPart pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        head = chest.getChild("head");
        jaw = head.getChild("jaw");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "left" : "right";
            arms[i] = chest.getChild(side + "_arm");
            forearms[i] = arms[i].getChild(side + "_forearm");
            hands[i] = forearms[i].getChild(side + "_hand");
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
        }
    }

    public static LayerDefinition createBodyLayer() {
        return GoreboundHulkMesh.createBodyLayer();
    }

    @Override
    public void setupAnim(GoreboundHulkEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        float partialTick = Mth.clamp(age - entity.tickCount, 0, 1);
        int windup = entity.getStrikeWindup();
        float replayed = windup > 0 ? strikeClock.sample(1, windup, entity.tickCount, partialTick, 1) : 0.0F;
        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch, replayed, attackTime);
    }

    /** Shared runtime and authoring pose; windup is the existing ten-tick strike counter. */
    public void setupPose(float limbSwing, float movement, float age, float yaw, float pitch,
            float windup, float strike) {
        root.getAllParts().forEach(ModelPart::resetPose);
        movement = Mth.clamp(movement, 0, 1);
        float lift = Mth.clamp(windup / 8, 0, 1);
        lift = lift * lift * (3 - 2 * lift);
        float swing = Mth.clamp(strike, 0, 1);
        float followThrough = 4 * swing * (1 - swing);
        float raised = windup > 0 ? lift : 1 - Mth.clamp(swing / .28F, 0, 1);
        if (windup <= 0 && swing == 0) raised = 0;
        float stride = movement * (1 - .8F * Math.max(raised, followThrough));
        float step = limbSwing * .6662F;
        chest.xRot += Mth.sin(age * .12F) * .022F - raised * .22F + followThrough * .24F;
        chest.zRot += Mth.cos(step) * stride * .065F;
        head.yRot += Mth.clamp(yaw, -45, 45) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -30, 30) * Mth.DEG_TO_RAD * .5F + raised * .12F;
        jaw.xRot += Mth.sin(age * .09F) * .025F + raised * .10F + followThrough * .07F;
        for (int i = 0; i < 2; i++) {
            float side = i == 0 ? 1 : -1;
            float gait = Mth.cos(step + i * Mth.PI) * stride;
            thighs[i].xRot += gait * .36F;
            shins[i].xRot += Math.max(0, -gait) * .52F;
            feet[i].xRot -= thighs[i].xRot + shins[i].xRot;
            arms[i].xRot -= raised * 1.40F + gait * .20F;
            arms[i].xRot += followThrough * .26F;
            arms[i].zRot -= side * raised * .13F;
            forearms[i].xRot -= raised * .55F;
            forearms[i].xRot += followThrough * .27F;
            hands[i].xRot -= raised * .12F - followThrough * .16F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
