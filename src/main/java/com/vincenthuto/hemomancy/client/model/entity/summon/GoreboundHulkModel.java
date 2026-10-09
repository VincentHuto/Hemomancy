package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_gorebound_hulk.py; regenerate the model, atlas and BBModel together.
public class GoreboundHulkModel extends EntityModel<GoreboundHulkEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "gorebound_hulk"), "main");
    private final ModelPart root, chest, head, jaw;
    private final ModelPart[] arms = new ModelPart[2], forearms = new ModelPart[2], hands = new ModelPart[2];
    private final ModelPart[] thighs = new ModelPart[2], shins = new ModelPart[2], feet = new ModelPart[2];

    public GoreboundHulkModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        ModelPart pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        head = chest.getChild("neck").getChild("head");
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
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -2.0F, -3.0F, 10.0F, 4.0F, 6.0F).texOffs(33, 0).addBox(-4.5F, -9.0F, -3.0F, 9.0F, 7.0F, 6.0F), PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition belt = pelvis.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(64, 0).addBox(-5.5F, -1.5F, -3.5F, 11.0F, 3.0F, 7.0F), PartPose.offset(0.0F, -2.5F, 0.0F));
        PartDefinition buckle = belt.addOrReplaceChild("buckle", CubeListBuilder.create().texOffs(101, 0).addBox(-2.0F, -1.5F, -0.75F, 4.0F, 3.0F, 1.0F).texOffs(112, 0).addBox(-1.0F, -1.0F, -1.25F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 0.0F, -3.5F));
        PartDefinition chest = pelvis.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 14).addBox(-7.0F, -11.0F, -4.0F, 14.0F, 11.0F, 8.0F).texOffs(45, 14).addBox(-6.0F, -13.0F, -1.0F, 12.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.2792526803190927F, 0.0F, 0.0F));
        PartDefinition left_pectoral = chest.addOrReplaceChild("left_pectoral", CubeListBuilder.create().texOffs(82, 14).addBox(-3.0F, -2.5F, -1.0F, 6.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(-3.5F, -8.0F, -4.0F, 0.0F, 0.0F, 0.10471975511965978F));
        PartDefinition left_lat = chest.addOrReplaceChild("left_lat", CubeListBuilder.create().texOffs(99, 14).addBox(-1.5F, -4.0F, -2.0F, 3.0F, 8.0F, 4.0F), PartPose.offsetAndRotation(-6.5F, -5.0F, 1.0F, 0.0F, 0.0F, -0.20943951023931956F));
        PartDefinition left_strap = chest.addOrReplaceChild("left_strap", CubeListBuilder.create().texOffs(114, 14).addBox(-1.0F, -5.0F, -0.5F, 2.0F, 11.0F, 1.0F), PartPose.offsetAndRotation(-4.0F, -6.0F, -5.0F, 0.0F, 0.0F, 0.10471975511965978F));
        PartDefinition left_trap = chest.addOrReplaceChild("left_trap", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(-3.5F, -11.0F, 0.0F, 0.0F, 0.0F, -0.3490658503988659F));
        PartDefinition right_pectoral = chest.addOrReplaceChild("right_pectoral", CubeListBuilder.create().texOffs(25, 34).addBox(-3.0F, -2.5F, -1.0F, 6.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(3.5F, -8.0F, -4.0F, 0.0F, 0.0F, -0.10471975511965978F));
        PartDefinition right_lat = chest.addOrReplaceChild("right_lat", CubeListBuilder.create().texOffs(42, 34).addBox(-1.5F, -4.0F, -2.0F, 3.0F, 8.0F, 4.0F), PartPose.offsetAndRotation(6.5F, -5.0F, 1.0F, 0.0F, 0.0F, 0.20943951023931956F));
        PartDefinition right_strap = chest.addOrReplaceChild("right_strap", CubeListBuilder.create().texOffs(57, 34).addBox(-1.0F, -5.0F, -0.5F, 2.0F, 11.0F, 1.0F), PartPose.offsetAndRotation(4.0F, -6.0F, -5.0F, 0.0F, 0.0F, -0.10471975511965978F));
        PartDefinition right_trap = chest.addOrReplaceChild("right_trap", CubeListBuilder.create().texOffs(64, 34).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(3.5F, -11.0F, 0.0F, 0.0F, 0.0F, 0.3490658503988659F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(89, 34).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 3.0F, 4.0F), PartPose.offset(0.0F, -12.0F, -2.5F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(106, 34).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F).texOffs(0, 47).addBox(-3.0F, -4.0F, -3.5F, 6.0F, 1.0F, 1.0F).texOffs(15, 47).addBox(-3.5F, -3.0F, -0.5F, 1.0F, 1.0F, 1.0F).texOffs(20, 47).addBox(2.5F, -3.0F, -0.5F, 1.0F, 1.0F, 1.0F).texOffs(25, 47).addBox(-2.5F, -1.5F, -3.25F, 5.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -2.0F, -0.5F, -0.17453292519943295F, 0.0F, 0.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(38, 47).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 2.0F, 4.0F).texOffs(55, 47).addBox(-2.0F, -1.0F, -3.5F, 1.0F, 1.0F, 1.0F).texOffs(60, 47).addBox(1.0F, -1.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.06981317007977318F, 0.0F, 0.0F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(65, 47).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 7.0F, 5.0F).texOffs(86, 47).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 6.0F).texOffs(111, 47).addBox(-2.0F, 1.0F, -3.5F, 4.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(-8.0F, -9.0F, 0.0F, -0.20943951023931956F, 0.0F, 0.17453292519943295F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(0, 60).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 7.0F, 7.0F).texOffs(29, 60).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.3839724354387525F, 0.0F, -0.06981317007977318F));
        PartDefinition left_binding = left_forearm.addOrReplaceChild("left_binding", CubeListBuilder.create().texOffs(54, 60).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 2.0F, 8.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition left_hand = left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(87, 60).addBox(-4.0F, 0.0F, -3.5F, 8.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.17453292519943295F, 0.0F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(118, 60).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-3.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(0, 75).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-1.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(9, 75).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(1.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create().texOffs(18, 75).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(3.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(27, 75).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(4.0F, 1.0F, -2.0F, 0.3490658503988659F, 0.0F, -0.4363323129985824F));
        PartDefinition left_thigh = pelvis.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(36, 75).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 5.0F, 5.0F).texOffs(57, 75).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F), PartPose.offset(-3.0F, 0.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(82, 75).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F).texOffs(99, 75).addBox(-1.5F, -0.5F, -2.5F, 3.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition left_foot = left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(0, 86).addBox(-2.5F, -1.0F, -4.0F, 5.0F, 3.0F, 6.0F).texOffs(23, 86).addBox(-2.0F, 0.0F, -4.5F, 4.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(34, 86).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 7.0F, 5.0F).texOffs(55, 86).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 6.0F).texOffs(80, 86).addBox(-2.0F, 1.0F, -3.5F, 4.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(8.0F, -9.0F, 0.0F, -0.20943951023931956F, 0.0F, -0.17453292519943295F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(91, 86).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 7.0F, 7.0F).texOffs(0, 101).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.3839724354387525F, 0.0F, 0.06981317007977318F));
        PartDefinition right_binding = right_forearm.addOrReplaceChild("right_binding", CubeListBuilder.create().texOffs(25, 101).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 2.0F, 8.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition right_hand = right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(58, 101).addBox(-4.0F, 0.0F, -3.5F, 8.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.17453292519943295F, 0.0F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(89, 101).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-3.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(98, 101).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-1.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(107, 101).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(1.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create().texOffs(116, 101).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(3.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(0, 113).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-4.0F, 1.0F, -2.0F, 0.3490658503988659F, 0.0F, 0.4363323129985824F));
        PartDefinition right_thigh = pelvis.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(9, 113).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 5.0F, 5.0F).texOffs(30, 113).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F), PartPose.offset(3.0F, 0.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(55, 113).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F).texOffs(72, 113).addBox(-1.5F, -0.5F, -2.5F, 3.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition right_foot = right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(81, 113).addBox(-2.5F, -1.0F, -4.0F, 5.0F, 3.0F, 6.0F).texOffs(104, 113).addBox(-2.0F, 0.0F, -4.5F, 4.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(GoreboundHulkEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        float partialTick = Mth.clamp(age - entity.tickCount, 0, 1);
        float windup = entity.getStrikeWindup();
        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                windup > 0 ? windup + partialTick : 0, attackTime);
    }

    /** Shared runtime and authoring pose; windup is the existing ten-tick strike counter. */
    public void setupPose(float limbSwing, float movement, float age, float yaw, float pitch,
            float windup, float strike) {
        root.getAllParts().forEach(ModelPart::resetPose);
        movement = Mth.clamp(movement, 0, 1);
        float lift = Mth.clamp(windup / 8, 0, 1);
        lift = lift * lift * (3 - 2 * lift);
        float recovery = Mth.clamp(strike * 2.5F, 0, 1);
        float impact = Mth.sin(Mth.clamp(strike, 0, 1) * Mth.PI);
        chest.xRot += -lift * .18F + impact * .14F;
        head.yRot += Mth.clamp(yaw, -45, 45) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -30, 30) * Mth.DEG_TO_RAD * .5F;
        jaw.xRot += Mth.sin(age * .09F) * .025F + lift * .05F;
        for (int i = 0; i < 2; i++) {
            float side = i == 0 ? -1 : 1;
            float gait = Mth.cos(limbSwing * .6662F + i * Mth.PI) * movement;
            thighs[i].xRot = gait * .45F;
            shins[i].xRot = Math.max(0, -gait) * .55F;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            arms[i].xRot -= lift * 1.1F;
            forearms[i].xRot -= lift * .65F;
            if (windup <= 0 && strike > 0) {
                arms[i].xRot = Mth.lerp(recovery, -1.25F, arms[i].xRot);
                forearms[i].xRot = Mth.lerp(recovery, -.85F, forearms[i].xRot);
            }
            arms[i].xRot -= gait * .12F * (1 - lift);
            arms[i].zRot -= side * lift * .12F;
            hands[i].xRot -= lift * .12F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
