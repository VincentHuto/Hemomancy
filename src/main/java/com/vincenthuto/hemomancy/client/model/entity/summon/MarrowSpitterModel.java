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

// Authored by tools/circus/build_marrow_juggler.py; regenerate model and atlas together.
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
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -5.0F, -2.5F, 9.0F, 11.0F, 6.0F).texOffs(31, 0).addBox(-4.0F, -8.0F, -0.25F, 8.0F, 5.0F, 5.5F), PartPose.offsetAndRotation(0.0F, 9.0F, 0.0F, 0.17453292519943295F, 0.0F, 0.0F));
        PartDefinition hood = body.addOrReplaceChild("hood", CubeListBuilder.create().texOffs(60, 0).addBox(-4.5F, -4.0F, 0.25F, 9.0F, 5.75F, 3.25F).texOffs(87, 0).addBox(-5.0F, -2.5F, -1.0F, 1.5F, 4.5F, 2.5F).texOffs(98, 0).addBox(3.5F, -2.5F, -1.0F, 1.5F, 4.5F, 2.5F), PartPose.offsetAndRotation(0.0F, -7.0F, -1.5F, -0.13962634015954636F, 0.0F, 0.0F));
        PartDefinition mask_mourn = hood.addOrReplaceChild("mask_mourn", CubeListBuilder.create().texOffs(109, 0).addBox(-2.5F, -3.0F, -0.75F, 5.0F, 6.0F, 1.25F).texOffs(0, 18).addBox(-1.5F, 2.75F, -0.5F, 3.0F, 1.0F, 1.0F).texOffs(9, 18).addBox(-0.25F, -0.5F, -1.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, -1.75F, -1.5F));
        PartDefinition mask_laugh = hood.addOrReplaceChild("mask_laugh", CubeListBuilder.create().texOffs(14, 18).addBox(-2.25F, -2.75F, -0.75F, 4.5F, 5.0F, 1.25F).texOffs(29, 18).addBox(-1.5F, 2.0F, -0.5F, 3.0F, 1.0F, 1.0F).texOffs(38, 18).addBox(-0.25F, -0.75F, -1.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(-3.75F, 0.25F, -0.75F, 0.0F, -0.4188790204786391F, -0.20943951023931956F));
        PartDefinition mask_blank = hood.addOrReplaceChild("mask_blank", CubeListBuilder.create().texOffs(43, 18).addBox(-2.25F, -2.5F, -0.75F, 4.5F, 5.0F, 1.25F).texOffs(58, 18).addBox(-1.5F, 2.25F, -0.5F, 3.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.75F, 0.25F, -0.75F, 0.0F, 0.4188790204786391F, 0.20943951023931956F));
        PartDefinition collar = body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(67, 18).addBox(-3.5F, -0.25F, -0.25F, 7.0F, 0.5F, 0.5F).texOffs(84, 18).addBox(-2.75F, -0.5F, -0.5F, 1.0F, 1.0F, 0.75F).texOffs(89, 18).addBox(-1.25F, 0.25F, -0.5F, 1.0F, 1.0F, 0.75F).texOffs(94, 18).addBox(0.25F, 0.25F, -0.5F, 1.0F, 1.0F, 0.75F).texOffs(99, 18).addBox(1.75F, -0.5F, -0.5F, 1.0F, 1.0F, 0.75F).texOffs(104, 18).addBox(-0.75F, 1.25F, -0.5F, 1.5F, 1.5F, 0.75F), PartPose.offset(0.0F, -3.0F, -2.75F));
        PartDefinition skirt = body.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(0, 26).addBox(-3.75F, -0.5F, -2.25F, 7.5F, 5.5F, 5.5F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition front_left = skirt.addOrReplaceChild("front_left", CubeListBuilder.create().texOffs(29, 26).addBox(-2.0F, -0.25F, -0.5F, 4.0F, 7.5F, 1.5F), PartPose.offsetAndRotation(-2.25F, 1.0F, -3.0F, -0.17453292519943295F, 0.0F, 0.24434609527920614F));
        PartDefinition front_left_rag = front_left.addOrReplaceChild("front_left_rag", CubeListBuilder.create().texOffs(42, 26).addBox(-0.5F, -0.25F, -0.25F, 1.0F, 1.75F, 0.75F), PartPose.offsetAndRotation(-1.25F, 7.0F, 0.0F, 0.0F, 0.0F, 0.13962634015954636F));
        PartDefinition rear_left = skirt.addOrReplaceChild("rear_left", CubeListBuilder.create().texOffs(47, 26).addBox(-2.0F, -0.25F, -0.5F, 4.0F, 8.0F, 1.5F), PartPose.offsetAndRotation(-2.25F, 1.0F, 3.0F, 0.17453292519943295F, 0.0F, 0.24434609527920614F));
        PartDefinition rear_left_rag = rear_left.addOrReplaceChild("rear_left_rag", CubeListBuilder.create().texOffs(60, 26).addBox(-0.5F, -0.25F, -0.25F, 1.0F, 1.75F, 0.75F), PartPose.offsetAndRotation(-1.25F, 7.0F, 0.0F, 0.0F, 0.0F, 0.13962634015954636F));
        PartDefinition side_left = skirt.addOrReplaceChild("side_left", CubeListBuilder.create().texOffs(65, 26).addBox(-0.5F, 0.0F, -2.0F, 1.25F, 7.0F, 4.0F), PartPose.offsetAndRotation(-4.25F, 1.0F, 0.0F, 0.0F, 0.0F, 0.2792526803190927F));
        PartDefinition front_right = skirt.addOrReplaceChild("front_right", CubeListBuilder.create().texOffs(78, 26).addBox(-2.0F, -0.25F, -0.5F, 4.0F, 7.5F, 1.5F), PartPose.offsetAndRotation(2.25F, 1.0F, -3.0F, -0.17453292519943295F, 0.0F, -0.24434609527920614F));
        PartDefinition front_right_rag = front_right.addOrReplaceChild("front_right_rag", CubeListBuilder.create().texOffs(91, 26).addBox(-0.5F, -0.25F, -0.25F, 1.0F, 1.75F, 0.75F), PartPose.offsetAndRotation(1.25F, 7.0F, 0.0F, 0.0F, 0.0F, -0.13962634015954636F));
        PartDefinition rear_right = skirt.addOrReplaceChild("rear_right", CubeListBuilder.create().texOffs(96, 26).addBox(-2.0F, -0.25F, -0.5F, 4.0F, 8.0F, 1.5F), PartPose.offsetAndRotation(2.25F, 1.0F, 3.0F, 0.17453292519943295F, 0.0F, -0.24434609527920614F));
        PartDefinition rear_right_rag = rear_right.addOrReplaceChild("rear_right_rag", CubeListBuilder.create().texOffs(109, 26).addBox(-0.5F, -0.25F, -0.25F, 1.0F, 1.75F, 0.75F), PartPose.offsetAndRotation(1.25F, 7.0F, 0.0F, 0.0F, 0.0F, -0.13962634015954636F));
        PartDefinition side_right = skirt.addOrReplaceChild("side_right", CubeListBuilder.create().texOffs(114, 26).addBox(-0.5F, 0.0F, -2.0F, 1.25F, 7.0F, 4.0F), PartPose.offsetAndRotation(4.25F, 1.0F, 0.0F, 0.0F, 0.0F, -0.2792526803190927F));
        PartDefinition sash = skirt.addOrReplaceChild("sash", CubeListBuilder.create().texOffs(0, 39).addBox(-1.25F, -0.5F, -0.25F, 2.5F, 8.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 0.0F, -3.25F, -0.13962634015954636F, 0.0F, 0.0F));
        PartDefinition arm_0 = body.addOrReplaceChild("arm_0", CubeListBuilder.create().texOffs(9, 39).addBox(-0.75F, -0.5F, -0.75F, 1.5F, 5.25F, 1.5F).texOffs(18, 39).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(-4.5F, -3.5F, 0.25F, -0.3141592653589793F, 0.0F, 2.007128639793479F));
        PartDefinition forearm_0 = arm_0.addOrReplaceChild("forearm_0", CubeListBuilder.create().texOffs(27, 39).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 4.75F, 1.0F).texOffs(32, 39).addBox(-0.75F, 3.25F, -0.75F, 1.5F, 1.25F, 1.5F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_0 = forearm_0.addOrReplaceChild("hand_0", CubeListBuilder.create().texOffs(41, 39).addBox(-0.75F, -0.25F, -0.5F, 1.5F, 1.5F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_0 = hand_0.addOrReplaceChild("fingers_0", CubeListBuilder.create().texOffs(48, 39).addBox(-0.75F, 0.0F, -0.5F, 0.5F, 1.25F, 0.5F).texOffs(53, 39).addBox(0.25F, 0.0F, -0.5F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_0 = hand_0.addOrReplaceChild("thumb_0", CubeListBuilder.create().texOffs(58, 39).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.75F, 0.25F, -0.25F, -0.4363323129985824F, 0.0F, -0.5235987755982988F));
        PartDefinition arm_1 = body.addOrReplaceChild("arm_1", CubeListBuilder.create().texOffs(63, 39).addBox(-0.75F, -0.5F, -0.75F, 1.5F, 5.25F, 1.5F).texOffs(72, 39).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(4.5F, -3.5F, 0.25F, -0.3141592653589793F, 0.0F, -2.007128639793479F));
        PartDefinition forearm_1 = arm_1.addOrReplaceChild("forearm_1", CubeListBuilder.create().texOffs(81, 39).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 4.75F, 1.0F).texOffs(86, 39).addBox(-0.75F, 3.25F, -0.75F, 1.5F, 1.25F, 1.5F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_1 = forearm_1.addOrReplaceChild("hand_1", CubeListBuilder.create().texOffs(95, 39).addBox(-0.75F, -0.25F, -0.5F, 1.5F, 1.5F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_1 = hand_1.addOrReplaceChild("fingers_1", CubeListBuilder.create().texOffs(102, 39).addBox(-0.75F, 0.0F, -0.5F, 0.5F, 1.25F, 0.5F).texOffs(107, 39).addBox(0.25F, 0.0F, -0.5F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_1 = hand_1.addOrReplaceChild("thumb_1", CubeListBuilder.create().texOffs(112, 39).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(-0.75F, 0.25F, -0.25F, -0.4363323129985824F, 0.0F, 0.5235987755982988F));
        PartDefinition arm_2 = body.addOrReplaceChild("arm_2", CubeListBuilder.create().texOffs(117, 39).addBox(-0.75F, -0.5F, -0.75F, 1.5F, 5.25F, 1.5F).texOffs(0, 49).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(-4.5F, 0.0F, 0.25F, -0.3141592653589793F, 0.0F, 1.3089969389957472F));
        PartDefinition forearm_2 = arm_2.addOrReplaceChild("forearm_2", CubeListBuilder.create().texOffs(9, 49).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 4.75F, 1.0F).texOffs(14, 49).addBox(-0.75F, 3.25F, -0.75F, 1.5F, 1.25F, 1.5F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_2 = forearm_2.addOrReplaceChild("hand_2", CubeListBuilder.create().texOffs(23, 49).addBox(-0.75F, -0.25F, -0.5F, 1.5F, 1.5F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_2 = hand_2.addOrReplaceChild("fingers_2", CubeListBuilder.create().texOffs(30, 49).addBox(-0.75F, 0.0F, -0.5F, 0.5F, 1.25F, 0.5F).texOffs(35, 49).addBox(0.25F, 0.0F, -0.5F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_2 = hand_2.addOrReplaceChild("thumb_2", CubeListBuilder.create().texOffs(40, 49).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.75F, 0.25F, -0.25F, -0.4363323129985824F, 0.0F, -0.5235987755982988F));
        PartDefinition arm_3 = body.addOrReplaceChild("arm_3", CubeListBuilder.create().texOffs(45, 49).addBox(-0.75F, -0.5F, -0.75F, 1.5F, 5.25F, 1.5F).texOffs(54, 49).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(4.5F, 0.0F, 0.25F, -0.3141592653589793F, 0.0F, -1.3089969389957472F));
        PartDefinition forearm_3 = arm_3.addOrReplaceChild("forearm_3", CubeListBuilder.create().texOffs(63, 49).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 4.75F, 1.0F).texOffs(68, 49).addBox(-0.75F, 3.25F, -0.75F, 1.5F, 1.25F, 1.5F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_3 = forearm_3.addOrReplaceChild("hand_3", CubeListBuilder.create().texOffs(77, 49).addBox(-0.75F, -0.25F, -0.5F, 1.5F, 1.5F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_3 = hand_3.addOrReplaceChild("fingers_3", CubeListBuilder.create().texOffs(84, 49).addBox(-0.75F, 0.0F, -0.5F, 0.5F, 1.25F, 0.5F).texOffs(89, 49).addBox(0.25F, 0.0F, -0.5F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_3 = hand_3.addOrReplaceChild("thumb_3", CubeListBuilder.create().texOffs(94, 49).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(-0.75F, 0.25F, -0.25F, -0.4363323129985824F, 0.0F, 0.5235987755982988F));
        PartDefinition arm_4 = body.addOrReplaceChild("arm_4", CubeListBuilder.create().texOffs(99, 49).addBox(-0.75F, -0.5F, -0.75F, 1.5F, 5.25F, 1.5F).texOffs(108, 49).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(-4.5F, 3.0F, 0.25F, -0.3141592653589793F, 0.0F, 0.6108652381980153F));
        PartDefinition forearm_4 = arm_4.addOrReplaceChild("forearm_4", CubeListBuilder.create().texOffs(117, 49).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 4.75F, 1.0F).texOffs(0, 58).addBox(-0.75F, 3.25F, -0.75F, 1.5F, 1.25F, 1.5F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_4 = forearm_4.addOrReplaceChild("hand_4", CubeListBuilder.create().texOffs(9, 58).addBox(-0.75F, -0.25F, -0.5F, 1.5F, 1.5F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_4 = hand_4.addOrReplaceChild("fingers_4", CubeListBuilder.create().texOffs(16, 58).addBox(-0.75F, 0.0F, -0.5F, 0.5F, 1.25F, 0.5F).texOffs(21, 58).addBox(0.25F, 0.0F, -0.5F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_4 = hand_4.addOrReplaceChild("thumb_4", CubeListBuilder.create().texOffs(26, 58).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.75F, 0.25F, -0.25F, -0.4363323129985824F, 0.0F, -0.5235987755982988F));
        PartDefinition arm_5 = body.addOrReplaceChild("arm_5", CubeListBuilder.create().texOffs(31, 58).addBox(-0.75F, -0.5F, -0.75F, 1.5F, 5.25F, 1.5F).texOffs(40, 58).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(4.5F, 3.0F, 0.25F, -0.3141592653589793F, 0.0F, -0.6108652381980153F));
        PartDefinition forearm_5 = arm_5.addOrReplaceChild("forearm_5", CubeListBuilder.create().texOffs(49, 58).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 4.75F, 1.0F).texOffs(54, 58).addBox(-0.75F, 3.25F, -0.75F, 1.5F, 1.25F, 1.5F), PartPose.offsetAndRotation(0.0F, 4.5F, 0.0F, -0.9599310885968813F, 0.0F, 0.0F));
        PartDefinition hand_5 = forearm_5.addOrReplaceChild("hand_5", CubeListBuilder.create().texOffs(63, 58).addBox(-0.75F, -0.25F, -0.5F, 1.5F, 1.5F, 1.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition fingers_5 = hand_5.addOrReplaceChild("fingers_5", CubeListBuilder.create().texOffs(70, 58).addBox(-0.75F, 0.0F, -0.5F, 0.5F, 1.25F, 0.5F).texOffs(75, 58).addBox(0.25F, 0.0F, -0.5F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition thumb_5 = hand_5.addOrReplaceChild("thumb_5", CubeListBuilder.create().texOffs(80, 58).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(-0.75F, 0.25F, -0.25F, -0.4363323129985824F, 0.0F, 0.5235987755982988F));
        PartDefinition knife_0 = body.addOrReplaceChild("knife_0", CubeListBuilder.create().texOffs(85, 58).addBox(-0.5F, -4.5F, -0.25F, 1.0F, 3.5F, 0.5F).texOffs(90, 58).addBox(-0.25F, -5.5F, -0.25F, 0.5F, 1.0F, 0.5F).texOffs(95, 58).addBox(-1.0F, -1.0F, -0.25F, 2.0F, 0.5F, 0.5F).texOffs(102, 58).addBox(-0.25F, -0.75F, -0.25F, 0.5F, 1.5F, 0.5F).texOffs(107, 58).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 0.5F, 0.5F), PartPose.offset(0.0F, -8.0F, -6.0F));
        PartDefinition knife_1 = body.addOrReplaceChild("knife_1", CubeListBuilder.create().texOffs(112, 58).addBox(-0.5F, -4.5F, -0.25F, 1.0F, 3.5F, 0.5F).texOffs(117, 58).addBox(-0.25F, -5.5F, -0.25F, 0.5F, 1.0F, 0.5F).texOffs(122, 58).addBox(-1.0F, -1.0F, -0.25F, 2.0F, 0.5F, 0.5F).texOffs(0, 67).addBox(-0.25F, -0.75F, -0.25F, 0.5F, 1.5F, 0.5F).texOffs(5, 67).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 0.5F, 0.5F), PartPose.offset(0.0F, -8.0F, -6.0F));
        PartDefinition knife_2 = body.addOrReplaceChild("knife_2", CubeListBuilder.create().texOffs(10, 67).addBox(-0.5F, -4.5F, -0.25F, 1.0F, 3.5F, 0.5F).texOffs(15, 67).addBox(-0.25F, -5.5F, -0.25F, 0.5F, 1.0F, 0.5F).texOffs(20, 67).addBox(-1.0F, -1.0F, -0.25F, 2.0F, 0.5F, 0.5F).texOffs(27, 67).addBox(-0.25F, -0.75F, -0.25F, 0.5F, 1.5F, 0.5F).texOffs(32, 67).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 0.5F, 0.5F), PartPose.offset(0.0F, -8.0F, -6.0F));
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
        root.y = -.75F + Mth.sin(age * .07F) * .35F;
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
