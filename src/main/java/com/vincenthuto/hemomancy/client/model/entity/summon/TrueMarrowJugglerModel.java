package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.MarrowJugglerOrdealMoves;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

// Authored by tools/circus/build_true_marrow_juggler.py (the Juggler's true self, 2026-11-17); regenerate together.
/**
 * The Marrow Juggler's true self: it flies higher and wilder than its physical body, banking into its motion and
 * trailing its ribbons and blades, and juggles three daggers and three marrow bones at once. Its ordeal moves
 * ({@link MarrowJugglerOrdealMoves}) pose from an entity event, each a gather, a fast blow and a recovery: the bones
 * tossed high for the hail, three arms cocked and whipped through for the volley, and the spin of six blades for the
 * whirl.
 */
public class TrueMarrowJugglerModel extends EntityModel<MarrowSpitterEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("true_marrow_juggler"), "main");
    private final ModelPart root, body, hood;
    private final ModelPart[] arms = new ModelPart[6], forearms = new ModelPart[6], hands = new ModelPart[6];
    private final ModelPart[] knives = new ModelPart[3], bones = new ModelPart[3], ribbons = new ModelPart[4];
    private final ModelPart[] hems;

    public TrueMarrowJugglerModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        body = root.getChild("body");
        hood = body.getChild("hood");
        for (int i = 0; i < arms.length; i++) {
            arms[i] = body.getChild("arm_" + i);
            forearms[i] = arms[i].getChild("forearm_" + i);
            hands[i] = forearms[i].getChild("hand_" + i);
        }
        for (int i = 0; i < 3; i++) {
            knives[i] = body.getChild("knife_" + i);
            bones[i] = body.getChild("bone_" + i);
        }
        for (int i = 0; i < ribbons.length; i++) ribbons[i] = hood.getChild("ribbon_" + i);
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
        PartDefinition skirt = body.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(62, 12).addBox(-3.5F, 0.0F, -2.5F, 7.0F, 3.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition front_left = skirt.addOrReplaceChild("front_left", CubeListBuilder.create().texOffs(87, 12).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 11.0F, 0.5F), PartPose.offsetAndRotation(-1.75F, 2.0F, -2.5F, -0.13962634015954636F, 0.0F, 0.13962634015954636F));
        PartDefinition front_left_rag = front_left.addOrReplaceChild("front_left_rag", CubeListBuilder.create().texOffs(94, 12).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 4.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.0F, 0.0F, 0.13962634015954636F));
        PartDefinition rear_left = skirt.addOrReplaceChild("rear_left", CubeListBuilder.create().texOffs(99, 12).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 12.0F, 0.5F), PartPose.offsetAndRotation(-1.75F, 2.0F, 2.5F, 0.13962634015954636F, 0.0F, 0.13962634015954636F));
        PartDefinition rear_left_rag = rear_left.addOrReplaceChild("rear_left_rag", CubeListBuilder.create().texOffs(106, 12).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 4.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.13962634015954636F));
        PartDefinition side_left = skirt.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(111, 12).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 11.0F, 0.5F), PartPose.offsetAndRotation(-3.5F, 2.0F, 0.0F, 0.0F, 0.0F, 0.24434609527920614F));
        PartDefinition front_right = skirt.addOrReplaceChild("front_right", CubeListBuilder.create().texOffs(118, 12).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 11.0F, 0.5F), PartPose.offsetAndRotation(1.75F, 2.0F, -2.5F, -0.13962634015954636F, 0.0F, -0.13962634015954636F));
        PartDefinition front_right_rag = front_right.addOrReplaceChild("front_right_rag", CubeListBuilder.create().texOffs(0, 26).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 4.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 11.0F, 0.0F, 0.0F, 0.0F, -0.13962634015954636F));
        PartDefinition rear_right = skirt.addOrReplaceChild("rear_right", CubeListBuilder.create().texOffs(5, 26).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 12.0F, 0.5F), PartPose.offsetAndRotation(1.75F, 2.0F, 2.5F, 0.13962634015954636F, 0.0F, -0.13962634015954636F));
        PartDefinition rear_right_rag = rear_right.addOrReplaceChild("rear_right_rag", CubeListBuilder.create().texOffs(12, 26).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 4.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.13962634015954636F));
        PartDefinition side_right = skirt.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(17, 26).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 11.0F, 0.5F), PartPose.offsetAndRotation(3.5F, 2.0F, 0.0F, 0.0F, 0.0F, -0.24434609527920614F));
        PartDefinition sash = skirt.addOrReplaceChild("sash", CubeListBuilder.create().texOffs(24, 26).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 1.0F, -3.0F, -0.10471975511965978F, 0.0F, 0.0F));
        PartDefinition arm_0 = body.addOrReplaceChild("arm_0", CubeListBuilder.create().texOffs(31, 26).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(36, 26).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.5F, 2.0F), PartPose.offsetAndRotation(-4.0F, -8.0F, 0.0F, -0.3141592653589793F, 0.0F, 2.007128639793479F));
        PartDefinition forearm_0 = arm_0.addOrReplaceChild("forearm_0", CubeListBuilder.create().texOffs(45, 26).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(50, 26).addBox(-0.75F, 3.5F, -0.75F, 1.5F, 1.0F, 1.5F), PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_0 = forearm_0.addOrReplaceChild("hand_0", CubeListBuilder.create().texOffs(59, 26).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_0 = hand_0.addOrReplaceChild("fingers_0", CubeListBuilder.create().texOffs(66, 26).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(71, 26).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_0 = hand_0.addOrReplaceChild("thumb_0", CubeListBuilder.create().texOffs(76, 26).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, -0.5235987755982988F));
        PartDefinition arm_1 = body.addOrReplaceChild("arm_1", CubeListBuilder.create().texOffs(81, 26).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(86, 26).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.5F, 2.0F), PartPose.offsetAndRotation(4.0F, -8.0F, 0.0F, -0.3141592653589793F, 0.0F, -2.007128639793479F));
        PartDefinition forearm_1 = arm_1.addOrReplaceChild("forearm_1", CubeListBuilder.create().texOffs(95, 26).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(100, 26).addBox(-0.75F, 3.5F, -0.75F, 1.5F, 1.0F, 1.5F), PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_1 = forearm_1.addOrReplaceChild("hand_1", CubeListBuilder.create().texOffs(109, 26).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_1 = hand_1.addOrReplaceChild("fingers_1", CubeListBuilder.create().texOffs(116, 26).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(121, 26).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_1 = hand_1.addOrReplaceChild("thumb_1", CubeListBuilder.create().texOffs(0, 40).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(-1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, 0.5235987755982988F));
        PartDefinition arm_2 = body.addOrReplaceChild("arm_2", CubeListBuilder.create().texOffs(5, 40).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(10, 40).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.5F, 2.0F), PartPose.offsetAndRotation(-4.0F, -5.5F, 0.5F, -0.3141592653589793F, 0.0F, 1.3089969389957472F));
        PartDefinition forearm_2 = arm_2.addOrReplaceChild("forearm_2", CubeListBuilder.create().texOffs(19, 40).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(24, 40).addBox(-0.75F, 3.5F, -0.75F, 1.5F, 1.0F, 1.5F), PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_2 = forearm_2.addOrReplaceChild("hand_2", CubeListBuilder.create().texOffs(33, 40).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_2 = hand_2.addOrReplaceChild("fingers_2", CubeListBuilder.create().texOffs(40, 40).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(45, 40).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_2 = hand_2.addOrReplaceChild("thumb_2", CubeListBuilder.create().texOffs(50, 40).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, -0.5235987755982988F));
        PartDefinition arm_3 = body.addOrReplaceChild("arm_3", CubeListBuilder.create().texOffs(55, 40).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(60, 40).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.5F, 2.0F), PartPose.offsetAndRotation(4.0F, -5.5F, 0.5F, -0.3141592653589793F, 0.0F, -1.3089969389957472F));
        PartDefinition forearm_3 = arm_3.addOrReplaceChild("forearm_3", CubeListBuilder.create().texOffs(69, 40).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(74, 40).addBox(-0.75F, 3.5F, -0.75F, 1.5F, 1.0F, 1.5F), PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_3 = forearm_3.addOrReplaceChild("hand_3", CubeListBuilder.create().texOffs(83, 40).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_3 = hand_3.addOrReplaceChild("fingers_3", CubeListBuilder.create().texOffs(90, 40).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(95, 40).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_3 = hand_3.addOrReplaceChild("thumb_3", CubeListBuilder.create().texOffs(100, 40).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(-1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, 0.5235987755982988F));
        PartDefinition arm_4 = body.addOrReplaceChild("arm_4", CubeListBuilder.create().texOffs(105, 40).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(110, 40).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.5F, 2.0F), PartPose.offsetAndRotation(-3.0F, -2.5F, 0.5F, -0.3141592653589793F, 0.0F, 0.6108652381980153F));
        PartDefinition forearm_4 = arm_4.addOrReplaceChild("forearm_4", CubeListBuilder.create().texOffs(119, 40).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(0, 48).addBox(-0.75F, 3.5F, -0.75F, 1.5F, 1.0F, 1.5F), PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_4 = forearm_4.addOrReplaceChild("hand_4", CubeListBuilder.create().texOffs(9, 48).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_4 = hand_4.addOrReplaceChild("fingers_4", CubeListBuilder.create().texOffs(16, 48).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(21, 48).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_4 = hand_4.addOrReplaceChild("thumb_4", CubeListBuilder.create().texOffs(26, 48).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, -0.5235987755982988F));
        PartDefinition arm_5 = body.addOrReplaceChild("arm_5", CubeListBuilder.create().texOffs(31, 48).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(36, 48).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.5F, 2.0F), PartPose.offsetAndRotation(3.0F, -2.5F, 0.5F, -0.3141592653589793F, 0.0F, -0.6108652381980153F));
        PartDefinition forearm_5 = arm_5.addOrReplaceChild("forearm_5", CubeListBuilder.create().texOffs(45, 48).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(50, 48).addBox(-0.75F, 3.5F, -0.75F, 1.5F, 1.0F, 1.5F), PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_5 = forearm_5.addOrReplaceChild("hand_5", CubeListBuilder.create().texOffs(59, 48).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_5 = hand_5.addOrReplaceChild("fingers_5", CubeListBuilder.create().texOffs(66, 48).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F).texOffs(71, 48).addBox(0.5F, 0.0F, -0.5F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_5 = hand_5.addOrReplaceChild("thumb_5", CubeListBuilder.create().texOffs(76, 48).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(-1.0F, 0.5F, -0.25F, -0.4363323129985824F, 0.0F, 0.5235987755982988F));
        PartDefinition knife_0 = body.addOrReplaceChild("knife_0", CubeListBuilder.create().texOffs(81, 48).addBox(-0.5F, -5.0F, -0.25F, 1.0F, 4.0F, 0.5F).texOffs(86, 48).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F).texOffs(93, 48).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F).texOffs(98, 48).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -8.0F, -6.0F));
        PartDefinition knife_1 = body.addOrReplaceChild("knife_1", CubeListBuilder.create().texOffs(103, 48).addBox(-0.5F, -5.0F, -0.25F, 1.0F, 4.0F, 0.5F).texOffs(108, 48).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F).texOffs(115, 48).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F).texOffs(120, 48).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -8.0F, -6.0F));
        PartDefinition knife_2 = body.addOrReplaceChild("knife_2", CubeListBuilder.create().texOffs(0, 56).addBox(-0.5F, -5.0F, -0.25F, 1.0F, 4.0F, 0.5F).texOffs(5, 56).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 1.0F, 1.0F).texOffs(12, 56).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F).texOffs(17, 56).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -8.0F, -6.0F));
        PartDefinition marrow = body.addOrReplaceChild("marrow", CubeListBuilder.create().texOffs(22, 56).addBox(-1.5F, -2.5F, -0.25F, 3.0F, 4.0F, 0.5F), PartPose.offset(0.0F, -6.0F, -2.25F));
        PartDefinition ribs = body.addOrReplaceChild("ribs", CubeListBuilder.create().texOffs(31, 56).addBox(-0.5F, -3.0F, -0.25F, 1.0F, 4.5F, 0.5F).texOffs(36, 56).addBox(-2.5F, -2.5F, -0.25F, 5.0F, 0.5F, 0.5F).texOffs(49, 56).addBox(-2.5F, -1.0F, -0.25F, 5.0F, 0.5F, 0.5F).texOffs(62, 56).addBox(-2.0F, 0.5F, -0.25F, 4.0F, 0.5F, 0.5F), PartPose.offset(0.0F, -6.0F, -2.75F));
        PartDefinition ribbon_0 = hood.addOrReplaceChild("ribbon_0", CubeListBuilder.create().texOffs(73, 56).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offsetAndRotation(-2.0F, -1.0F, 2.5F, 0.5235987755982988F, 0.0F, -0.20943951023931956F));
        PartDefinition ribbon_1 = hood.addOrReplaceChild("ribbon_1", CubeListBuilder.create().texOffs(78, 56).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 8.0F, 0.5F), PartPose.offsetAndRotation(-0.75F, -1.0F, 2.5F, 0.5235987755982988F, 0.0F, -0.07853981633974483F));
        PartDefinition ribbon_2 = hood.addOrReplaceChild("ribbon_2", CubeListBuilder.create().texOffs(83, 56).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offsetAndRotation(0.75F, -1.0F, 2.5F, 0.5235987755982988F, 0.0F, 0.07853981633974483F));
        PartDefinition ribbon_3 = hood.addOrReplaceChild("ribbon_3", CubeListBuilder.create().texOffs(88, 56).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 8.0F, 0.5F), PartPose.offsetAndRotation(2.0F, -1.0F, 2.5F, 0.5235987755982988F, 0.0F, 0.20943951023931956F));
        PartDefinition bone_0 = body.addOrReplaceChild("bone_0", CubeListBuilder.create().texOffs(93, 56).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(98, 56).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 1.5F, 2.0F).texOffs(107, 56).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 1.5F, 2.0F), PartPose.offset(0.0F, -8.0F, -6.0F));
        PartDefinition bone_1 = body.addOrReplaceChild("bone_1", CubeListBuilder.create().texOffs(116, 56).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(0, 66).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 1.5F, 2.0F).texOffs(9, 66).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 1.5F, 2.0F), PartPose.offset(0.0F, -8.0F, -6.0F));
        PartDefinition bone_2 = body.addOrReplaceChild("bone_2", CubeListBuilder.create().texOffs(18, 66).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(23, 66).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 1.5F, 2.0F).texOffs(32, 66).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 1.5F, 2.0F), PartPose.offset(0.0F, -8.0F, -6.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(MarrowSpitterEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        float facing = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot) * Mth.DEG_TO_RAD;
        double dx = entity.getX() - entity.xo, dz = entity.getZ() - entity.zo;
        // Its motion in its own frame: forward along its facing, and to its side.
        float forward = (float) (-dx * Mth.sin(facing) + dz * Mth.cos(facing));
        float lateral = (float) (dx * Mth.cos(facing) + dz * Mth.sin(facing));
        setupPose(age, yaw, pitch, forward, lateral, entity.ordealMove(), entity.ordealMoveAge(partialTick));
    }

    /** Poses the rig; {@code move} is -1 between moves, {@code since} the ticks since its wind-up began. */
    public void setupPose(float age, float yaw, float pitch, float forward, float lateral, int move, float since) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.visible = true;
        });
        float lean = Mth.clamp(forward * 2.2F, -.45F, .55F), bank = Mth.clamp(lateral * 2.6F, -.55F, .55F);
        float speed = Math.min(1.0F, (Math.abs(forward) + Math.abs(lateral)) * 4.0F);
        root.y += -1.5F + Mth.sin(age * .09F) * .6F;
        body.xRot += lean;
        body.zRot += Mth.sin(age * .05F) * .04F - bank;
        hood.yRot += Mth.clamp(yaw, -40, 40) * Mth.DEG_TO_RAD * .6F;
        hood.xRot += Mth.clamp(pitch, -25, 25) * Mth.DEG_TO_RAD * .4F - lean * .5F;
        for (int i = 0; i < hems.length; i++) hems[i].xRot += Mth.sin(age * .11F + i * 1.1F) * .1F + .5F * speed;
        for (int i = 0; i < ribbons.length; i++) ribbons[i].xRot += Mth.sin(age * .23F + i * 1.7F) * .25F + .6F * speed;
        // The cascade, faster and wider than the physical body's, quicker still while it gathers a hail.
        float hurry = move == MarrowJugglerOrdealMoves.HAIL ? HemoEasing.smoothstep(0.0F, MarrowJugglerOrdealMoves.HAIL_TOSS, since) : 0.0F;
        float juggle = age * .2F + since * .25F * hurry;
        for (int i = 0; i < arms.length; i++) {
            int pair = i / 2;
            float side = i % 2 == 0 ? -1 : 1;
            float phase = juggle * .9F + pair * Mth.TWO_PI / 3;
            arms[i].zRot -= side * Mth.sin(phase) * .16F;
            arms[i].xRot += Mth.cos(phase + i % 2 * Mth.PI) * .14F;
            forearms[i].xRot -= Mth.cos(phase + i % 2 * Mth.PI) * .2F;
        }
        for (int i = 0; i < 3; i++) {
            toss(knives[i], juggle + i * Mth.TWO_PI / 3, i * 2, i * 2 + 1, 11 + i * 2);
            toss(bones[i], juggle + i * Mth.TWO_PI / 3 + Mth.PI, i * 2 + 1, i * 2, 9 + i * 2);
        }
        if (move >= 0) move(move, since);
    }

    /** One juggled thing in the cascade, from one hand's palm to another's in an arc {@code height} high. */
    private void toss(ModelPart thing, float phase, int from, int to, float height) {
        float t = (1 - Mth.cos(phase)) * .5F;
        float arc = Mth.sin(phase);
        Vector3f a = palm(from), b = palm(to);
        thing.x = Mth.lerp(t, a.x, b.x);
        thing.y = Mth.lerp(t, a.y, b.y) - arc * arc * height;
        thing.z = Mth.lerp(t, a.z, b.z);
        thing.zRot = phase * 2;
    }

    /** 0 until 2.5 ticks before {@code at}, then 1 at {@code at}, accelerating like a whip. */
    private static float whip(float since, float at) {
        float t = Mth.clamp((since - (at - 2.5F)) / 2.5F, 0.0F, 1.0F);
        return t * t;
    }

    private void move(int move, float since) {
        switch (move) {
            case MarrowJugglerOrdealMoves.HAIL -> {
                // Arms whirling faster, all six thrown up together to loose the bones, then brought down hard on the fall.
                float w = MarrowJugglerOrdealMoves.HAIL_WINDUP, at = MarrowJugglerOrdealMoves.HAIL_TOSS;
                float gather = HemoEasing.smoothstep(0.0F, at, since), tossed = whip(since, at), down = whip(since, w);
                float hold = 1.0F - HemoEasing.smoothstep(w + 4.0F, w + 16.0F, since);
                for (int i = 0; i < arms.length; i++) {
                    float target = Mth.lerp(down, Mth.lerp(tossed, -.4F * gather, -2.6F), .5F);
                    arms[i].xRot = Mth.lerp(hold, arms[i].xRot, arms[i].getInitialPose().xRot + target);
                    forearms[i].xRot = Mth.lerp(hold, forearms[i].xRot, Mth.lerp(tossed, forearms[i].xRot, -.15F));
                }
                body.xRot += hold * (-.25F * tossed * (1.0F - down) + .4F * down);
                hood.xRot += hold * (-.3F * tossed * (1.0F - down) + .2F * down);
                for (ModelPart bone : bones) bone.visible = since < at || since > w + 14.0F;
            }
            case MarrowJugglerOrdealMoves.VOLLEY -> {
                // Three arms on one side cocked back with a dagger each, whipped through one after another.
                float w = MarrowJugglerOrdealMoves.VOLLEY_WINDUP;
                float hold = 1.0F - HemoEasing.smoothstep(w + 2 * MarrowJugglerOrdealMoves.VOLLEY_GAP + 4.0F,
                        w + 2 * MarrowJugglerOrdealMoves.VOLLEY_GAP + 16.0F, since);
                float turn = HemoEasing.smoothstep(0.0F, 6.0F, since) * hold;
                body.yRot += .4F * turn;
                for (int j = 0; j < 3; j++) {
                    float release = w + j * MarrowJugglerOrdealMoves.VOLLEY_GAP;
                    float cock = HemoEasing.smoothstep(0.0F, w - 2.0F, since) * (1.0F - whip(since, release));
                    float snap = whip(since, release) * (1.0F - HemoEasing.smoothstep(release + 2.0F, release + 10.0F, since));
                    int a = j * 2;
                    arms[a].xRot += hold * (2.1F * cock - 1.4F * snap);
                    forearms[a].xRot += hold * (-.9F * cock + .4F * snap);
                    body.xRot += .08F * snap;
                    if (cock > .3F) hold(knives[j], a, 1.6F);
                    knives[j].visible = since < release || since > release + 14.0F;
                }
            }
            case MarrowJugglerOrdealMoves.WHIRL -> {
                // Arms spread straight out with a blade in each hand, the whole body spinning faster as it climbs,
                // tucking on the dive, whirling on the floor, then slowing as it climbs back.
                float w = MarrowJugglerOrdealMoves.WHIRL_WINDUP, dive = MarrowJugglerOrdealMoves.WHIRL_DIVE;
                float spin = MarrowJugglerOrdealMoves.WHIRL_SPIN, recover = MarrowJugglerOrdealMoves.WHIRL_RECOVER;
                float spread = HemoEasing.smoothstep(0.0F, 10.0F, since)
                        * (1.0F - HemoEasing.smoothstep(w + dive + spin, w + dive + spin + 12.0F, since));
                float diving = HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since) * (1.0F - HemoEasing.smoothstep(w + dive - 1.0F, w + dive, since));
                body.yRot += spinAngle(since, w, dive + spin, recover);
                body.xRot += .6F * diving;
                for (int i = 0; i < arms.length; i++) {
                    float side = i % 2 == 0 ? -1 : 1;
                    arms[i].zRot = Mth.lerp(spread, arms[i].zRot, -side * (1.5F + .1F * (i / 2 - 1)));
                    arms[i].xRot = Mth.lerp(spread, arms[i].xRot, (i / 2 - 1) * .35F);
                    forearms[i].xRot = Mth.lerp(spread, forearms[i].xRot, 0.0F);
                }
                if (spread > .3F) {
                    for (int i = 0; i < 3; i++) {
                        hold(knives[i], i * 2, 0.0F);
                        hold(bones[i], i * 2 + 1, 0.0F);
                    }
                }
            }
            default -> {
            }
        }
    }

    /** The whirl's turn so far: spun up through the climb, at full speed through the dive and spin, slowed after. */
    private static float spinAngle(float since, float climb, float floor, float recover) {
        float top = 1.3F;
        if (since < climb) return .5F * top * since * since / climb;
        float spun = .5F * top * climb;
        if (since < climb + floor) return spun + top * (since - climb);
        spun += top * floor;
        float t = Math.min(recover, since - climb - floor);
        return spun + top * t - .5F * top * t * t / recover;
    }

    /** Holds a juggled thing in one hand's palm, its point out along the arm, tipped {@code back} radians. */
    private void hold(ModelPart thing, int hand, float back) {
        Vector3f at = palm(hand);
        thing.x = at.x;
        thing.y = at.y;
        thing.z = at.z;
        thing.zRot = arms[hand].zRot;
        thing.xRot = arms[hand].xRot + back;
    }

    private Vector3f palm(int index) {
        PoseStack pose = new PoseStack();
        arms[index].translateAndRotate(pose);
        forearms[index].translateAndRotate(pose);
        hands[index].translateAndRotate(pose);
        return pose.last().pose().transformPosition(new Vector3f(0, .75F / 16, 0)).mul(16);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
