package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_cinder_bellows.py; regenerate all three assets together.
public class CinderBellowsModel extends EntityModel<CinderBellowsEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "cinder_bellows"), "main");
    private final ModelPart root, pelvis, abdomen, chest, neck, head, jaw;
    private final ModelPart[] tail = new ModelPart[10];
    private final ModelPart[] lungs, arms, forearms, hands;

    public CinderBellowsModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        abdomen = pelvis.getChild("abdomen");
        chest = abdomen.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        ModelPart parent = root.getChild("coil");
        for (int i = 0; i < tail.length; i++) {
            tail[i] = parent.getChild("tail_" + i);
            parent = tail[i];
        }
        lungs = new ModelPart[]{chest.getChild("left_lung"), chest.getChild("right_lung")};
        arms = new ModelPart[]{chest.getChild("left_arm"), chest.getChild("right_arm")};
        forearms = new ModelPart[]{arms[0].getChild("left_forearm"), arms[1].getChild("right_forearm")};
        hands = new ModelPart[]{forearms[0].getChild("left_hand"), forearms[1].getChild("right_hand")};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition coil = root.addOrReplaceChild("coil", CubeListBuilder.create().texOffs(0, 0).addBox(-3.25F, -1.5F, -1.75F, 6.5F, 3.0F, 4.0F).texOffs(23, 0).addBox(-2.5F, -2.25F, -1.75F, 5.0F, 4.5F, 4.0F), PartPose.offsetAndRotation(0.0F, -2.25F, -1.5F, 0.0F, -0.3490658503988659F, 0.0F));
        PartDefinition tail_0 = coil.addOrReplaceChild("tail_0", CubeListBuilder.create().texOffs(42, 0).addBox(-3.0F, -1.75F, -0.75F, 6.0F, 3.5F, 4.0F).texOffs(63, 0).addBox(-2.75F, -2.0F, -0.75F, 5.5F, 4.0F, 4.0F), PartPose.offset(0.0F, 0.25F, 1.75F));
        PartDefinition tail_1 = tail_0.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(84, 0).addBox(-2.75F, -1.5F, -0.75F, 5.5F, 3.0F, 4.0F).texOffs(105, 0).addBox(-2.5F, -1.75F, -0.75F, 5.0F, 3.5F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.25F, 3.0F, 0.0F, 0.6108652381980153F, 0.0F));
        PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, -1.5F, -0.75F, 5.0F, 3.0F, 4.0F).texOffs(19, 10).addBox(-2.25F, -1.75F, -0.75F, 4.5F, 3.5F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.6108652381980153F, 0.0F));
        PartDefinition tail_3 = tail_2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(38, 10).addBox(-2.25F, -1.25F, -0.75F, 4.5F, 2.5F, 4.0F).texOffs(57, 10).addBox(-2.0F, -1.5F, -0.75F, 4.0F, 3.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.25F, 3.0F, 0.0F, 0.6108652381980153F, 0.0F));
        PartDefinition tail_4 = tail_3.addOrReplaceChild("tail_4", CubeListBuilder.create().texOffs(74, 10).addBox(-2.0F, -1.25F, -0.75F, 4.0F, 2.5F, 4.0F).texOffs(91, 10).addBox(-1.75F, -1.5F, -0.75F, 3.5F, 3.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.6108652381980153F, 0.0F));
        PartDefinition tail_5 = tail_4.addOrReplaceChild("tail_5", CubeListBuilder.create().texOffs(108, 10).addBox(-1.75F, -1.0F, -0.75F, 3.5F, 2.0F, 4.0F).texOffs(0, 19).addBox(-1.5F, -1.25F, -0.75F, 3.0F, 2.5F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.25F, 3.0F, 0.0F, 0.6108652381980153F, 0.0F));
        PartDefinition tail_6 = tail_5.addOrReplaceChild("tail_6", CubeListBuilder.create().texOffs(15, 19).addBox(-1.5F, -0.75F, -0.75F, 3.0F, 1.5F, 4.0F).texOffs(30, 19).addBox(-1.25F, -1.0F, -0.75F, 2.5F, 2.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.25F, 3.0F, 0.0F, 0.5235987755982988F, 0.0F));
        PartDefinition tail_7 = tail_6.addOrReplaceChild("tail_7", CubeListBuilder.create().texOffs(45, 19).addBox(-1.25F, -0.5F, -0.75F, 2.5F, 1.0F, 4.0F).texOffs(60, 19).addBox(-1.0F, -0.75F, -0.75F, 2.0F, 1.5F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.25F, 3.0F, 0.0F, 0.4363323129985824F, 0.0F));
        PartDefinition tail_8 = tail_7.addOrReplaceChild("tail_8", CubeListBuilder.create().texOffs(73, 19).addBox(-1.0F, -0.25F, -0.75F, 2.0F, 0.5F, 4.0F).texOffs(86, 19).addBox(-0.75F, -0.5F, -0.75F, 1.5F, 1.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.25F, 3.0F, 0.0F, 0.17453292519943295F, 0.0F));
        PartDefinition tail_9 = tail_8.addOrReplaceChild("tail_9", CubeListBuilder.create().texOffs(99, 19).addBox(-0.5F, -0.5F, -0.75F, 1.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, -0.2617993877991494F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(108, 19).addBox(-2.25F, -1.0F, -2.0F, 4.5F, 4.5F, 3.75F).texOffs(0, 29).addBox(-2.75F, 2.0F, -2.25F, 5.5F, 4.0F, 4.5F).texOffs(23, 29).addBox(-2.0F, 1.5F, -2.5F, 3.75F, 4.5F, 0.5F), PartPose.offsetAndRotation(0.0F, -8.0F, -1.5F, -0.20943951023931956F, 0.0F, 0.0F));
        PartDefinition abdomen = pelvis.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(34, 29).addBox(-2.0F, -5.0F, -1.5F, 3.75F, 9.5F, 3.0F).texOffs(49, 29).addBox(-0.5F, -5.25F, 1.25F, 1.25F, 10.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -3.75F, 0.0F, 0.10471975511965978F, 0.0F, 0.0F));
        PartDefinition chest = abdomen.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(56, 29).addBox(-3.5F, -4.5F, -1.0F, 7.0F, 7.75F, 3.0F).texOffs(77, 29).addBox(-0.5F, -4.75F, 2.0F, 1.25F, 8.5F, 1.0F), PartPose.offsetAndRotation(0.0F, -8.75F, 0.0F, 0.08726646259971647F, 0.0F, 0.0F));
        PartDefinition left_lung = chest.addOrReplaceChild("left_lung", CubeListBuilder.create().texOffs(84, 29).addBox(-1.25F, -3.5F, -1.5F, 2.5F, 5.5F, 2.75F), PartPose.offset(-1.5F, -0.25F, -1.5F));
        PartDefinition left_clavicle = chest.addOrReplaceChild("left_clavicle", CubeListBuilder.create().texOffs(97, 29).addBox(-3.5F, -0.25F, -0.25F, 3.5F, 0.5F, 1.0F), PartPose.offsetAndRotation(-0.25F, -4.0F, -2.0F, 0.0F, 0.0F, -0.20943951023931956F));
        PartDefinition left_rib_0 = chest.addOrReplaceChild("left_rib_0", CubeListBuilder.create().texOffs(108, 29).addBox(0.0F, -0.25F, -0.25F, 2.25F, 0.5F, 0.5F), PartPose.offsetAndRotation(-2.75F, -2.5F, -3.5F, 0.0F, 0.20943951023931956F, -0.2617993877991494F));
        PartDefinition left_rib_1 = chest.addOrReplaceChild("left_rib_1", CubeListBuilder.create().texOffs(117, 29).addBox(0.0F, -0.25F, -0.25F, 2.25F, 0.5F, 0.5F), PartPose.offsetAndRotation(-2.75F, -0.5F, -3.5F, 0.0F, 0.20943951023931956F, -0.3490658503988659F));
        PartDefinition left_rib_2 = chest.addOrReplaceChild("left_rib_2", CubeListBuilder.create().texOffs(0, 43).addBox(0.0F, -0.25F, -0.25F, 2.25F, 0.5F, 0.5F), PartPose.offsetAndRotation(-2.75F, 1.25F, -3.5F, 0.0F, 0.20943951023931956F, -0.4363323129985824F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(9, 43).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 8.0F, 2.0F).texOffs(18, 43).addBox(-1.25F, -0.5F, -1.25F, 2.5F, 2.0F, 2.5F), PartPose.offsetAndRotation(-3.75F, -3.5F, 0.25F, -0.13962634015954636F, 0.0F, 0.17453292519943295F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(31, 43).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 7.0F, 2.0F).texOffs(40, 43).addBox(-0.5F, 4.0F, -0.5F, 1.25F, 2.75F, 1.25F), PartPose.offsetAndRotation(0.0F, 7.25F, 0.0F, -0.2792526803190927F, 0.0F, -0.06981317007977318F));
        PartDefinition left_hand = left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(49, 43).addBox(-1.0F, -0.25F, -0.5F, 2.0F, 2.5F, 1.25F), PartPose.offsetAndRotation(0.0F, 6.0F, -0.25F, 0.13962634015954636F, 0.0F, 0.0F));
        PartDefinition left_fingers = left_hand.addOrReplaceChild("left_fingers", CubeListBuilder.create().texOffs(58, 43).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 2.25F, 0.5F).texOffs(63, 43).addBox(0.25F, 0.0F, -0.5F, 0.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.3141592653589793F, 0.0F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(68, 43).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(1.0F, 0.5F, -0.25F, 0.0F, 0.0F, -0.4363323129985824F));
        PartDefinition right_lung = chest.addOrReplaceChild("right_lung", CubeListBuilder.create().texOffs(73, 43).addBox(-1.25F, -3.5F, -1.5F, 2.5F, 5.5F, 2.75F), PartPose.offset(1.5F, -0.25F, -1.5F));
        PartDefinition right_clavicle = chest.addOrReplaceChild("right_clavicle", CubeListBuilder.create().texOffs(86, 43).addBox(0.0F, -0.25F, -0.25F, 3.5F, 0.5F, 1.0F), PartPose.offsetAndRotation(0.25F, -4.0F, -2.0F, 0.0F, 0.0F, 0.20943951023931956F));
        PartDefinition right_rib_0 = chest.addOrReplaceChild("right_rib_0", CubeListBuilder.create().texOffs(97, 43).addBox(-2.25F, -0.25F, -0.25F, 2.25F, 0.5F, 0.5F), PartPose.offsetAndRotation(2.75F, -2.5F, -3.5F, 0.0F, -0.20943951023931956F, 0.2617993877991494F));
        PartDefinition right_rib_1 = chest.addOrReplaceChild("right_rib_1", CubeListBuilder.create().texOffs(106, 43).addBox(-2.25F, -0.25F, -0.25F, 2.25F, 0.5F, 0.5F), PartPose.offsetAndRotation(2.75F, -0.5F, -3.5F, 0.0F, -0.20943951023931956F, 0.3490658503988659F));
        PartDefinition right_rib_2 = chest.addOrReplaceChild("right_rib_2", CubeListBuilder.create().texOffs(115, 43).addBox(-2.25F, -0.25F, -0.25F, 2.25F, 0.5F, 0.5F), PartPose.offsetAndRotation(2.75F, 1.25F, -3.5F, 0.0F, -0.20943951023931956F, 0.4363323129985824F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 8.0F, 2.0F).texOffs(9, 54).addBox(-1.25F, -0.5F, -1.25F, 2.5F, 2.0F, 2.5F), PartPose.offsetAndRotation(3.75F, -3.5F, 0.25F, -0.13962634015954636F, 0.0F, -0.17453292519943295F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(22, 54).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 7.0F, 2.0F).texOffs(31, 54).addBox(-0.5F, 4.0F, -0.5F, 1.25F, 2.75F, 1.25F), PartPose.offsetAndRotation(0.0F, 7.25F, 0.0F, -0.2792526803190927F, 0.0F, 0.06981317007977318F));
        PartDefinition right_hand = right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(40, 54).addBox(-1.0F, -0.25F, -0.5F, 2.0F, 2.5F, 1.25F), PartPose.offsetAndRotation(0.0F, 6.0F, -0.25F, 0.13962634015954636F, 0.0F, 0.0F));
        PartDefinition right_fingers = right_hand.addOrReplaceChild("right_fingers", CubeListBuilder.create().texOffs(49, 54).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 2.25F, 0.5F).texOffs(54, 54).addBox(0.25F, 0.0F, -0.5F, 0.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.3141592653589793F, 0.0F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(59, 54).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(-1.0F, 0.5F, -0.25F, 0.0F, 0.0F, 0.4363323129985824F));
        PartDefinition sternum = chest.addOrReplaceChild("sternum", CubeListBuilder.create().texOffs(64, 54).addBox(-0.25F, -3.5F, -0.25F, 0.5F, 7.5F, 1.0F), PartPose.offset(0.0F, -0.5F, -3.5F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(69, 54).addBox(-1.25F, -4.5F, -1.0F, 2.5F, 5.0F, 2.5F).texOffs(82, 54).addBox(-0.5F, -4.0F, -1.25F, 1.25F, 4.5F, 0.5F), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, -0.13962634015954636F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(89, 54).addBox(-2.75F, -6.0F, -2.25F, 5.5F, 5.0F, 4.5F).texOffs(0, 65).addBox(-2.0F, -2.25F, -2.5F, 3.75F, 2.0F, 4.5F), PartPose.offsetAndRotation(0.0F, -3.75F, -0.25F, 0.17453292519943295F, 0.0F, 0.0F));
        PartDefinition brow = head.addOrReplaceChild("brow", CubeListBuilder.create().texOffs(19, 65).addBox(-2.75F, -0.25F, -0.25F, 5.5F, 0.5F, 0.5F), PartPose.offset(0.0F, -4.75F, -2.25F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(34, 65).addBox(-2.0F, -0.25F, -3.5F, 3.75F, 1.25F, 3.75F).texOffs(51, 65).addBox(-2.0F, -2.0F, -0.5F, 0.5F, 2.0F, 1.0F).texOffs(56, 65).addBox(1.25F, -2.0F, -0.5F, 0.5F, 2.0F, 1.0F).texOffs(61, 65).addBox(-1.5F, -0.5F, -3.5F, 3.0F, 0.5F, 0.5F), PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, 0.10471975511965978F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(CinderBellowsEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        int cycle = entity.getBreathCycle();
        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                cycle == 0 ? 0 : Math.min(100, cycle + Mth.frac(age)));
    }

    /** Shared posing entry point for runtime rendering and authoring previews. */
    public void setupPose(float limbSwing, float movement, float age, float yaw, float pitch, float cycle) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float inhale = smooth(cycle <= 20 ? cycle / 20 : cycle <= 60 ? 1 : (100 - cycle) / 40);
        float exhale = smooth((cycle - 20) / 5) * (1 - smooth((cycle - 60) / 12));
        float travel = Mth.clamp(movement, 0, 1);
        float wave = age * .075F + limbSwing * .6F;
        for (int i = 0; i < tail.length; i++) {
            // Local curvature propagates through the parents, as on Scarlet Serpent.
            tail[i].yRot += Mth.sin(wave - i * .65F) * (.035F + travel * .12F)
                    * (1 - exhale * .65F);
        }
        pelvis.zRot += Mth.sin(wave) * travel * .035F;
        abdomen.xRot -= inhale * .055F;
        chest.xRot += inhale * .06F + exhale * .07F;
        chest.y -= inhale * .25F;
        for (ModelPart lung : lungs) {
            lung.xScale = 1 + inhale * .12F;
            lung.yScale = 1 + inhale * .06F;
            lung.zScale = 1 + inhale * .22F;
        }
        neck.xRot -= inhale * .06F;
        head.yRot += Mth.clamp(yaw, -35, 35) * Mth.DEG_TO_RAD;
        head.xRot += Mth.clamp(pitch, -25, 25) * Mth.DEG_TO_RAD - exhale * .1F;
        jaw.xRot += exhale * .65F + inhale * .025F;
        for (int i = 0; i < arms.length; i++) {
            float sign = i == 0 ? -1 : 1;
            arms[i].zRot += sign * inhale * -.08F;
            arms[i].xRot += Mth.sin(age * .045F + i * Mth.PI) * .025F - exhale * .1F;
            forearms[i].xRot -= inhale * .08F;
            hands[i].xRot += Mth.sin(age * .05F + i * Mth.PI) * .035F;
        }
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
