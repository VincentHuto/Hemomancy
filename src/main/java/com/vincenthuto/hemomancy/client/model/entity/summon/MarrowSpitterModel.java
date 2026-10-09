package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

// Authored by tools/circus/build_marrow_juggler.py; regenerate the model, atlas and BBModel together.
public class MarrowSpitterModel extends EntityModel<MarrowSpitterEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "marrow_spitter"), "main");
    private final ModelPart root, body, hood;
    private final ModelPart[] arms = new ModelPart[6], forearms = new ModelPart[6], hands = new ModelPart[6];
    private final ModelPart[] knives = new ModelPart[3];
    private final ModelPart[] hems;

    public MarrowSpitterModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        body = root.getChild("body");
        hood = body.getChild("hood");
        for (int i = 0; i < arms.length; i++) {
            arms[i] = body.getChild("arm_" + i);
            forearms[i] = arms[i].getChild("forearm_" + i);
            hands[i] = forearms[i].getChild("hand_" + i);
        }
        for (int i = 0; i < knives.length; i++) knives[i] = body.getChild("knife_" + i);
        ModelPart skirt = body.getChild("skirt");
        hems = new ModelPart[]{skirt.getChild("front_left"), skirt.getChild("front_right"),
                skirt.getChild("rear_left"), skirt.getChild("rear_right"),
                skirt.getChild("side_left"), skirt.getChild("side_right"), skirt.getChild("sash")};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -9.0F, -2.5F, 8.0F, 6.0F, 5.0F).texOffs(27, 0).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 3.0F, 4.0F), PartPose.offsetAndRotation(0.0F, -13.0F, 0.0F, 0.13962634015954636F, 0.0F, 0.0F));
        PartDefinition collar = body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(48, 0).addBox(-4.5F, -1.0F, -3.0F, 9.0F, 2.0F, 6.0F).texOffs(79, 0).addBox(-1.0F, 0.5F, -3.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -9.0F, 0.0F));
        PartDefinition collar_fold = collar.addOrReplaceChild("collar_fold", CubeListBuilder.create().texOffs(86, 0).addBox(-3.5F, -1.5F, -3.5F, 7.0F, 1.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F, 0.0F));
        PartDefinition hood = body.addOrReplaceChild("hood", CubeListBuilder.create().texOffs(0, 12).addBox(-3.0F, -6.0F, -2.5F, 6.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, -10.0F, -0.5F, -0.17453292519943295F, 0.0F, 0.0F));
        PartDefinition mask_mourn = hood.addOrReplaceChild("mask_mourn", CubeListBuilder.create().texOffs(23, 12).addBox(-2.5F, -2.5F, -1.0F, 5.0F, 5.0F, 1.0F), PartPose.offset(0.0F, -3.0F, -2.5F));
        PartDefinition mask_laugh = hood.addOrReplaceChild("mask_laugh", CubeListBuilder.create().texOffs(36, 12).addBox(-2.5F, -2.5F, -1.0F, 5.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(-3.0F, -3.0F, 0.0F, 0.0F, 1.2217304763960306F, -0.13962634015954636F));
        PartDefinition mask_blank = hood.addOrReplaceChild("mask_blank", CubeListBuilder.create().texOffs(49, 12).addBox(-2.5F, -2.5F, -1.0F, 5.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(3.0F, -3.0F, 0.0F, 0.0F, -1.2217304763960306F, 0.13962634015954636F));
        PartDefinition hood_point_left = hood.addOrReplaceChild("hood_point_left", CubeListBuilder.create().texOffs(62, 12).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(-2.0F, -5.5F, 0.0F, 0.0F, 0.0F, -0.6108652381980153F));
        PartDefinition hood_tip_left = hood_point_left.addOrReplaceChild("hood_tip_left", CubeListBuilder.create().texOffs(71, 12).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(76, 12).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, -0.7853981633974483F));
        PartDefinition hood_point_right = hood.addOrReplaceChild("hood_point_right", CubeListBuilder.create().texOffs(85, 12).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(2.0F, -5.5F, 0.0F, 0.0F, 0.0F, 0.6108652381980153F));
        PartDefinition hood_tip_right = hood_point_right.addOrReplaceChild("hood_tip_right", CubeListBuilder.create().texOffs(94, 12).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(99, 12).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.7853981633974483F));
        PartDefinition skirt = body.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(0, 24).addBox(-3.5F, 0.0F, -2.5F, 7.0F, 3.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition front_left = skirt.addOrReplaceChild("front_left", CubeListBuilder.create().texOffs(25, 24).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(-1.75F, 2.0F, -2.5F, -0.13962634015954636F, 0.0F, 0.13962634015954636F));
        PartDefinition front_left_rag = front_left.addOrReplaceChild("front_left_rag", CubeListBuilder.create().texOffs(34, 24).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.0F, 0.0F, 0.13962634015954636F));
        PartDefinition rear_left = skirt.addOrReplaceChild("rear_left", CubeListBuilder.create().texOffs(43, 24).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 10.0F, 1.0F), PartPose.offsetAndRotation(-1.75F, 2.0F, 2.5F, 0.13962634015954636F, 0.0F, 0.13962634015954636F));
        PartDefinition rear_left_rag = rear_left.addOrReplaceChild("rear_left_rag", CubeListBuilder.create().texOffs(52, 24).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, 0.0F, 0.13962634015954636F));
        PartDefinition side_left = skirt.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(61, 24).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 9.0F, 4.0F), PartPose.offsetAndRotation(-3.5F, 2.0F, 0.0F, 0.0F, 0.0F, 0.24434609527920614F));
        PartDefinition front_right = skirt.addOrReplaceChild("front_right", CubeListBuilder.create().texOffs(72, 24).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(1.75F, 2.0F, -2.5F, -0.13962634015954636F, 0.0F, -0.13962634015954636F));
        PartDefinition front_right_rag = front_right.addOrReplaceChild("front_right_rag", CubeListBuilder.create().texOffs(81, 24).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.0F, 0.0F, -0.13962634015954636F));
        PartDefinition rear_right = skirt.addOrReplaceChild("rear_right", CubeListBuilder.create().texOffs(90, 24).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 10.0F, 1.0F), PartPose.offsetAndRotation(1.75F, 2.0F, 2.5F, 0.13962634015954636F, 0.0F, -0.13962634015954636F));
        PartDefinition rear_right_rag = rear_right.addOrReplaceChild("rear_right_rag", CubeListBuilder.create().texOffs(99, 24).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, 0.0F, -0.13962634015954636F));
        PartDefinition side_right = skirt.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(108, 24).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 9.0F, 4.0F), PartPose.offsetAndRotation(3.5F, 2.0F, 0.0F, 0.0F, 0.0F, -0.24434609527920614F));
        PartDefinition sash = skirt.addOrReplaceChild("sash", CubeListBuilder.create().texOffs(119, 24).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 1.0F, -3.0F, -0.10471975511965978F, 0.0F, 0.0F));
        PartDefinition arm_0 = body.addOrReplaceChild("arm_0", CubeListBuilder.create().texOffs(0, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(5, 38).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-4.0F, -8.0F, 0.0F, -0.3141592653589793F, 0.0F, 2.007128639793479F));
        PartDefinition forearm_0 = arm_0.addOrReplaceChild("forearm_0", CubeListBuilder.create().texOffs(14, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(19, 38).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 1.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_0 = forearm_0.addOrReplaceChild("hand_0", CubeListBuilder.create().texOffs(28, 38).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_0 = hand_0.addOrReplaceChild("fingers_0", CubeListBuilder.create().texOffs(35, 38).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(40, 38).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_0 = hand_0.addOrReplaceChild("thumb_0", CubeListBuilder.create().texOffs(45, 38).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, -0.5235987755982988F));
        PartDefinition arm_1 = body.addOrReplaceChild("arm_1", CubeListBuilder.create().texOffs(50, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(55, 38).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(4.0F, -8.0F, 0.0F, -0.3141592653589793F, 0.0F, -2.007128639793479F));
        PartDefinition forearm_1 = arm_1.addOrReplaceChild("forearm_1", CubeListBuilder.create().texOffs(64, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(69, 38).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 1.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_1 = forearm_1.addOrReplaceChild("hand_1", CubeListBuilder.create().texOffs(78, 38).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_1 = hand_1.addOrReplaceChild("fingers_1", CubeListBuilder.create().texOffs(85, 38).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(90, 38).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_1 = hand_1.addOrReplaceChild("thumb_1", CubeListBuilder.create().texOffs(95, 38).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(-1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, 0.5235987755982988F));
        PartDefinition arm_2 = body.addOrReplaceChild("arm_2", CubeListBuilder.create().texOffs(100, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(105, 38).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-4.0F, -5.5F, 0.5F, -0.3141592653589793F, 0.0F, 1.3089969389957472F));
        PartDefinition forearm_2 = arm_2.addOrReplaceChild("forearm_2", CubeListBuilder.create().texOffs(114, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(119, 38).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 1.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_2 = forearm_2.addOrReplaceChild("hand_2", CubeListBuilder.create().texOffs(0, 45).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_2 = hand_2.addOrReplaceChild("fingers_2", CubeListBuilder.create().texOffs(7, 45).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(12, 45).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_2 = hand_2.addOrReplaceChild("thumb_2", CubeListBuilder.create().texOffs(17, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, -0.5235987755982988F));
        PartDefinition arm_3 = body.addOrReplaceChild("arm_3", CubeListBuilder.create().texOffs(22, 45).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(27, 45).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(4.0F, -5.5F, 0.5F, -0.3141592653589793F, 0.0F, -1.3089969389957472F));
        PartDefinition forearm_3 = arm_3.addOrReplaceChild("forearm_3", CubeListBuilder.create().texOffs(36, 45).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(41, 45).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 1.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_3 = forearm_3.addOrReplaceChild("hand_3", CubeListBuilder.create().texOffs(50, 45).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_3 = hand_3.addOrReplaceChild("fingers_3", CubeListBuilder.create().texOffs(57, 45).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(62, 45).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_3 = hand_3.addOrReplaceChild("thumb_3", CubeListBuilder.create().texOffs(67, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(-1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, 0.5235987755982988F));
        PartDefinition arm_4 = body.addOrReplaceChild("arm_4", CubeListBuilder.create().texOffs(72, 45).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(77, 45).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-3.0F, -2.5F, 0.5F, -0.3141592653589793F, 0.0F, 0.6108652381980153F));
        PartDefinition forearm_4 = arm_4.addOrReplaceChild("forearm_4", CubeListBuilder.create().texOffs(86, 45).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(91, 45).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 1.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_4 = forearm_4.addOrReplaceChild("hand_4", CubeListBuilder.create().texOffs(100, 45).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_4 = hand_4.addOrReplaceChild("fingers_4", CubeListBuilder.create().texOffs(107, 45).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(112, 45).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_4 = hand_4.addOrReplaceChild("thumb_4", CubeListBuilder.create().texOffs(117, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, -0.5235987755982988F));
        PartDefinition arm_5 = body.addOrReplaceChild("arm_5", CubeListBuilder.create().texOffs(122, 45).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(0, 52).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(3.0F, -2.5F, 0.5F, -0.3141592653589793F, 0.0F, -0.6108652381980153F));
        PartDefinition forearm_5 = arm_5.addOrReplaceChild("forearm_5", CubeListBuilder.create().texOffs(9, 52).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F).texOffs(14, 52).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 1.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_5 = forearm_5.addOrReplaceChild("hand_5", CubeListBuilder.create().texOffs(23, 52).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_5 = hand_5.addOrReplaceChild("fingers_5", CubeListBuilder.create().texOffs(30, 52).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(35, 52).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_5 = hand_5.addOrReplaceChild("thumb_5", CubeListBuilder.create().texOffs(40, 52).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(-1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, 0.5235987755982988F));
        PartDefinition knife_0 = body.addOrReplaceChild("knife_0", CubeListBuilder.create().texOffs(45, 52).addBox(-0.5F, -5.0F, -0.25F, 1.0F, 4.0F, 0.5F).texOffs(50, 52).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F).texOffs(57, 52).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F).texOffs(62, 52).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -8.0F, -6.0F));
        PartDefinition knife_1 = body.addOrReplaceChild("knife_1", CubeListBuilder.create().texOffs(67, 52).addBox(-0.5F, -5.0F, -0.25F, 1.0F, 4.0F, 0.5F).texOffs(72, 52).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F).texOffs(79, 52).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F).texOffs(84, 52).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -8.0F, -6.0F));
        PartDefinition knife_2 = body.addOrReplaceChild("knife_2", CubeListBuilder.create().texOffs(89, 52).addBox(-0.5F, -5.0F, -0.25F, 1.0F, 4.0F, 0.5F).texOffs(94, 52).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F).texOffs(101, 52).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F).texOffs(106, 52).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -8.0F, -6.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(MarrowSpitterEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        setupPose(limbSwingAmount, age, yaw, pitch);
    }

    /** Poses the connected rig for both runtime rendering and authoring previews. */
    public void setupPose(float movement, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        root.y += -.75F + Mth.sin(age * .07F) * .35F;
        body.zRot += Mth.sin(age * .045F) * .025F;
        body.xRot += Mth.clamp(movement, 0, 1) * .06F;
        hood.yRot += Mth.clamp(yaw, -40, 40) * Mth.DEG_TO_RAD * .6F;
        hood.xRot += Mth.clamp(pitch, -25, 25) * Mth.DEG_TO_RAD * .4F;
        for (int i = 0; i < hems.length; i++) {
            hems[i].xRot += Mth.sin(age * .065F + i * 1.1F) * .045F;
        }
        for (int i = 0; i < arms.length; i++) {
            int pair = i / 2;
            float side = i % 2 == 0 ? -1 : 1;
            float phase = age * .13F + pair * Mth.TWO_PI / 3;
            arms[i].zRot -= side * Mth.sin(phase) * .1F;
            arms[i].xRot += Mth.cos(phase + i % 2 * Mth.PI) * .08F;
            forearms[i].xRot -= Mth.cos(phase + i % 2 * Mth.PI) * .12F;
            hands[i].xRot += Mth.sin(phase) * .1F;
        }
        for (int i = 0; i < knives.length; i++) {
            float phase = age * .13F + i * Mth.TWO_PI / 3;
            float t = (1 - Mth.cos(phase)) * .5F;
            float arc = Mth.sin(phase);
            Vector3f left = palm(i * 2);
            Vector3f right = palm(i * 2 + 1);
            ModelPart knife = knives[i];
            knife.x = Mth.lerp(t, left.x, right.x);
            knife.y = Mth.lerp(t, left.y, right.y) - arc * arc * (7 + i * 1.5F);
            knife.z = Mth.lerp(t, left.z, right.z);
            knife.zRot = phase * 2;
        }
    }

    private Vector3f palm(int index) {
        PoseStack pose = new PoseStack();
        arms[index].translateAndRotate(pose);
        forearms[index].translateAndRotate(pose);
        hands[index].translateAndRotate(pose);
        // The knife pivot is its grip, so catching follows the bent hand exactly.
        return pose.last().pose().transformPosition(new Vector3f(0, .75F / 16, 0)).mul(16);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
