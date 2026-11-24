package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CinderBellowsModel extends EntityModel<CinderBellowsEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("cinder_bellows"), "main");
    private final ModelPart root, pelvis, abdomen, chest, neck, head, jaw, tailTip;
    private final ModelPart[] tail = new ModelPart[11];
    private final ModelPart[] lungs, hoods, arms, forearms, hands;

    public CinderBellowsModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        abdomen = pelvis.getChild("abdomen");
        chest = abdomen.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("hinged_jaw");
        ModelPart parent = root;
        for (int i = 0; i < tail.length; i++) {
            int index = i + 1;
            tail[i] = parent.getChild(index < 10 ? "coil_0" + index : "coil_" + index);
            parent = tail[i];
        }
        tailTip = parent.getChild("raised_tail_tip");
        lungs = new ModelPart[]{chest.getChild("left_bellows_lung"), chest.getChild("right_bellows_lung")};
        hoods = new ModelPart[]{head.getChild("left_folded_hood"), head.getChild("right_folded_hood")};
        arms = new ModelPart[]{chest.getChild("left_upper_arm"), chest.getChild("right_upper_arm")};
        forearms = new ModelPart[]{arms[0].getChild("left_forearm"), arms[1].getChild("right_forearm")};
        hands = new ModelPart[]{forearms[0].getChild("left_hand"), forearms[1].getChild("right_hand")};
    }

    public static LayerDefinition createBodyLayer() {
        return CinderBellowsMesh.createBodyLayer();
    }

    @Override
    public void setupAnim(CinderBellowsEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        int cycle = entity.getBreathCycle();
        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                cycle == 0 ? 0 : Math.min(100, cycle + Mth.frac(age)));
    }

    /** Ember heat for the glow layer: banked at rest, rising through the inhale, full during the breath. */
    public static float emberHeat(float cycle) {
        return Math.max(inhale(cycle) * .55F, exhale(cycle));
    }

    private static float inhale(float cycle) {
        return HemoEasing.smoothstep(cycle <= 20 ? cycle / 20 : cycle <= 60 ? 1 : (100 - cycle) / 40);
    }

    private static float exhale(float cycle) {
        return HemoEasing.smoothstep((cycle - 20) / 5) * (1 - HemoEasing.smoothstep((cycle - 60) / 12));
    }

    /** Shared posing entry point for runtime rendering and authoring previews. */
    public void setupPose(float limbSwing, float movement, float age, float yaw, float pitch, float cycle) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float inhale = inhale(cycle);
        float exhale = exhale(cycle);
        // Rear back while drawing breath, then lunge the neck into the cone.
        float draw = inhale * (1 - exhale);
        float travel = Mth.clamp(movement, 0, 1);
        float wave = age * .075F + limbSwing * .6F;
        for (int i = 0; i < tail.length; i++) {
            // Local curvature propagates through the parents, as on Scarlet Serpent.
            tail[i].yRot += Mth.sin(wave - i * .65F) * (.035F + travel * .12F)
                    * (1 - exhale * .65F);
        }
        tailTip.xRot += Mth.sin(age * .11F) * .12F + inhale * .3F;
        pelvis.zRot += Mth.sin(wave) * travel * .035F;
        abdomen.xRot -= draw * .12F;
        chest.xRot += exhale * .12F - draw * .16F;
        chest.y -= inhale * .25F;
        for (ModelPart lung : lungs) {
            lung.xScale = 1 + inhale * .12F;
            lung.yScale = 1 + inhale * .06F;
            lung.zScale = 1 + inhale * .22F;
        }
        neck.xRot += exhale * .32F - draw * .18F;
        head.yRot += Mth.clamp(yaw, -35, 35) * Mth.DEG_TO_RAD;
        // The head counters the neck lunge so the open jaw stays aimed down the cone.
        head.xRot += Mth.clamp(pitch, -25, 25) * Mth.DEG_TO_RAD - exhale * .3F + draw * .1F;
        jaw.xRot += exhale * .9F + inhale * .03F;
        for (int i = 0; i < arms.length; i++) {
            float sign = i == 0 ? 1 : -1;
            hoods[i].yRot += sign * (draw * .18F + exhale * .1F);
            hoods[i].zRot += sign * inhale * .12F;
            // Elbows haul back like bellows handles on the draw, then pump forward on the breath.
            arms[i].zRot += sign * draw * .25F;
            arms[i].xRot += Mth.sin(age * .045F + i * Mth.PI) * .025F + draw * .35F - exhale * .45F;
            forearms[i].xRot -= exhale * .3F;
            hands[i].xRot += Mth.sin(age * .05F + i * Mth.PI) * .035F;
        }
    }

    /** World-space exit of the posed snout in the living renderer's coordinate system. */
    public Vec3 breathOutlet(CinderBellowsEntity entity, float partialTicks) {
        PoseStack pose = new PoseStack();
        root.translateAndRotate(pose);
        pelvis.translateAndRotate(pose);
        abdomen.translateAndRotate(pose);
        chest.translateAndRotate(pose);
        neck.translateAndRotate(pose);
        head.translateAndRotate(pose);
        // The authored mouth is at (0, 22, -7), relative to the head pivot at (0, 24, -.5).
        Vector3f mouth = pose.last().pose().transformPosition(0, 2F / 16, -6.5F / 16, new Vector3f());
        float bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        Vec3 offset = new Vec3(-mouth.x, 1.501F - mouth.y, mouth.z)
                .yRot((180 - bodyYaw) * Mth.DEG_TO_RAD);
        return entity.getPosition(partialTicks).add(offset);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
