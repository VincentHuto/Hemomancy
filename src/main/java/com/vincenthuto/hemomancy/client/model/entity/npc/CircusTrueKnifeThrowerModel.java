package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.KnifeOrdealMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusKnifeThrowerEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

// Authored by tools/circus/build_true_knife_thrower.py; regenerate the model, atlas and BBModel together.
/**
 * The Dagger Thrower's true self: the hooded figure pacing the throwing gallery over their ring, a side-step that
 * crosses the feet, the knife hand loose with its fan, head on the fighter. Each intervention is a gesture
 * ({@link CircusKnifeThrowerEntity#gesture}) timed from its wind-up: the arm gathers slowly back, whips through in a few
 * accelerating ticks on the release, holds its follow-through, recovers. Their real arm throws where they stand; the
 * spectral blades that cross the ring ({@link #renderGhosts}) fly from the hand to the gesture's synced point and the
 * marks the moves work out from it ({@link KnifeOrdealMoves}), and stay in the board until the move is done.
 */
public final class CircusTrueKnifeThrowerModel extends EntityModel<CircusKnifeThrowerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_knife_thrower"), "main");
    private static final int MAX_BLADES = 16;
    private final ModelPart root, pelvis, chest, neck, head, fan, blade;
    private final ModelPart[] shoulders = new ModelPart[2], uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thumbs = new ModelPart[2], thumbTips = new ModelPart[2], thighs = new ModelPart[2],
            shins = new ModelPart[2], feet = new ModelPart[2], tails = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    /** This frame's body: where it stands and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;
    /** This frame's spectral blades: where each is (model pixels), the way its point goes, how strongly it shows. */
    private int blades;
    private final Vec3[] bladeAt = new Vec3[MAX_BLADES], bladeDir = new Vec3[MAX_BLADES];
    private final float[] bladeAlpha = new float[MAX_BLADES];

    public CircusTrueKnifeThrowerModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        blade = bakedRoot.getChild("spectral_blade");
        pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            shoulders[i] = chest.getChild(side + "_shoulder");
            uppers[i] = shoulders[i].getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            thumbTips[i] = thumbs[i].getChild(side + "_thumb_tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
            tails[i] = pelvis.getChild(side + "_tail");
        }
        fan = hands[0].getChild("fan");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -4.0F, -3.5F, 12.0F, 5.0F, 7.0F).texOffs(39, 0).addBox(-6.5F, -2.0F, -4.0F, 13.0F, 2.0F, 8.0F), PartPose.offset(0.0F, -30.0F, 0.0F));
        PartDefinition belt_knife_0 = pelvis.addOrReplaceChild("belt_knife_0", CubeListBuilder.create().texOffs(82, 0).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(87, 0).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(94, 0).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 3.0F, 0.5F), PartPose.offsetAndRotation(-4.5F, -1.5F, -4.25F, 0.0F, 0.0F, -0.15707963267948966F));
        PartDefinition belt_knife_1 = pelvis.addOrReplaceChild("belt_knife_1", CubeListBuilder.create().texOffs(99, 0).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(104, 0).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(111, 0).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 3.0F, 0.5F), PartPose.offsetAndRotation(-2.5F, -1.5F, -4.25F, 0.0F, 0.0F, -0.05235987755982989F));
        PartDefinition belt_knife_2 = pelvis.addOrReplaceChild("belt_knife_2", CubeListBuilder.create().texOffs(116, 0).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(121, 0).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(128, 0).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 3.0F, 0.5F), PartPose.offsetAndRotation(2.5F, -1.5F, -4.25F, 0.0F, 0.0F, 0.05235987755982989F));
        PartDefinition belt_knife_3 = pelvis.addOrReplaceChild("belt_knife_3", CubeListBuilder.create().texOffs(133, 0).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(138, 0).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(145, 0).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 3.0F, 0.5F), PartPose.offsetAndRotation(4.5F, -1.5F, -4.25F, 0.0F, 0.0F, 0.15707963267948966F));
        PartDefinition right_thigh = pelvis.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(150, 0).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 15.0F, 5.0F), PartPose.offset(-3.25F, 0.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(171, 0).addBox(-2.25F, 0.0F, -2.25F, 4.5F, 12.0F, 4.5F).texOffs(192, 0).addBox(-2.75F, 0.0F, -2.75F, 5.5F, 2.0F, 5.5F), PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition right_foot = right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(217, 0).addBox(-2.25F, 0.0F, -5.0F, 4.5F, 3.0F, 7.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition right_tail = pelvis.addOrReplaceChild("right_tail", CubeListBuilder.create().texOffs(242, 0).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 18.0F, 1.0F), PartPose.offsetAndRotation(-2.75F, 0.0F, 3.25F, 0.13962634015954636F, 0.0F, 0.08726646259971647F));
        PartDefinition left_thigh = pelvis.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(0, 21).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 15.0F, 5.0F), PartPose.offset(3.25F, 0.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(21, 21).addBox(-2.25F, 0.0F, -2.25F, 4.5F, 12.0F, 4.5F).texOffs(42, 21).addBox(-2.75F, 0.0F, -2.75F, 5.5F, 2.0F, 5.5F), PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition left_foot = left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(67, 21).addBox(-2.25F, 0.0F, -5.0F, 4.5F, 3.0F, 7.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition left_tail = pelvis.addOrReplaceChild("left_tail", CubeListBuilder.create().texOffs(92, 21).addBox(-3.0F, 0.0F, -0.5F, 6.0F, 18.0F, 1.0F), PartPose.offsetAndRotation(2.75F, 0.0F, 3.25F, 0.13962634015954636F, 0.0F, -0.08726646259971647F));
        PartDefinition chest = pelvis.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(107, 21).addBox(-7.0F, -16.0F, -4.0F, 14.0F, 16.0F, 8.0F).texOffs(152, 21).addBox(-4.5F, -14.5F, -4.5F, 9.0F, 14.0F, 0.5F), PartPose.offset(0.0F, -4.0F, 0.0F));
        PartDefinition right_lapel = chest.addOrReplaceChild("right_lapel", CubeListBuilder.create().texOffs(173, 21).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 10.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, -15.0F, -4.25F, 0.0F, 0.0F, 0.17453292519943295F));
        PartDefinition left_lapel = chest.addOrReplaceChild("left_lapel", CubeListBuilder.create().texOffs(180, 21).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 10.0F, 1.0F), PartPose.offsetAndRotation(3.5F, -15.0F, -4.25F, 0.0F, 0.0F, -0.17453292519943295F));
        PartDefinition clasps = chest.addOrReplaceChild("clasps", CubeListBuilder.create().texOffs(187, 21).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.0F, 0.5F).texOffs(192, 21).addBox(-0.5F, 3.0F, -0.25F, 1.0F, 1.0F, 0.5F).texOffs(197, 21).addBox(-0.5F, 6.0F, -0.25F, 1.0F, 1.0F, 0.5F), PartPose.offset(0.0F, -11.0F, -4.75F));
        PartDefinition bandolier = chest.addOrReplaceChild("bandolier", CubeListBuilder.create().texOffs(202, 21).addBox(-1.5F, -11.0F, -0.5F, 3.0F, 22.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -8.0F, -4.75F, 0.0F, 0.0F, 0.6632251157578453F));
        PartDefinition bandolier_knife_0 = bandolier.addOrReplaceChild("bandolier_knife_0", CubeListBuilder.create().texOffs(211, 21).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(216, 21).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(223, 21).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 3.0F, 0.5F), PartPose.offset(0.0F, -8.0F, -0.75F));
        PartDefinition bandolier_knife_1 = bandolier.addOrReplaceChild("bandolier_knife_1", CubeListBuilder.create().texOffs(228, 21).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(233, 21).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(240, 21).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 3.0F, 0.5F), PartPose.offset(0.0F, -4.0F, -0.75F));
        PartDefinition bandolier_knife_2 = bandolier.addOrReplaceChild("bandolier_knife_2", CubeListBuilder.create().texOffs(245, 21).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(250, 21).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(0, 46).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 3.0F, 0.5F), PartPose.offset(0.0F, 0.0F, -0.75F));
        PartDefinition bandolier_knife_3 = bandolier.addOrReplaceChild("bandolier_knife_3", CubeListBuilder.create().texOffs(5, 46).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(10, 46).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(17, 46).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 3.0F, 0.5F), PartPose.offset(0.0F, 4.0F, -0.75F));
        PartDefinition bandolier_knife_4 = bandolier.addOrReplaceChild("bandolier_knife_4", CubeListBuilder.create().texOffs(22, 46).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(27, 46).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(34, 46).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 3.0F, 0.5F), PartPose.offset(0.0F, 8.0F, -0.75F));
        PartDefinition back_bandolier = chest.addOrReplaceChild("back_bandolier", CubeListBuilder.create().texOffs(39, 46).addBox(-1.5F, -11.0F, -0.5F, 3.0F, 22.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -8.0F, 4.75F, 0.0F, 0.0F, -0.6632251157578453F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(48, 46).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F), PartPose.offset(0.0F, -16.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(65, 46).addBox(-4.5F, -10.0F, -4.5F, 9.0F, 10.0F, 9.0F), PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition hood = head.addOrReplaceChild("hood", CubeListBuilder.create().texOffs(102, 46).addBox(-5.0F, -11.0F, -5.0F, 10.0F, 2.0F, 10.0F).texOffs(143, 46).addBox(-5.0F, -9.0F, -5.0F, 1.0F, 10.0F, 10.0F).texOffs(166, 46).addBox(4.0F, -9.0F, -5.0F, 1.0F, 10.0F, 10.0F).texOffs(189, 46).addBox(-4.0F, -9.0F, 4.0F, 8.0F, 10.0F, 1.0F).texOffs(208, 46).addBox(-4.0F, -9.5F, -5.5F, 8.0F, 1.5F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition hood_tail = hood.addOrReplaceChild("hood_tail", CubeListBuilder.create().texOffs(227, 46).addBox(-2.5F, 0.0F, -1.0F, 5.0F, 8.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -9.0F, 5.0F, 0.3839724354387525F, 0.0F, 0.0F));
        PartDefinition cowl = hood.addOrReplaceChild("cowl", CubeListBuilder.create().texOffs(0, 70).addBox(-2.5F, -1.0F, -4.0F, 5.0F, 3.0F, 8.0F).texOffs(27, 70).addBox(-2.0F, 2.0F, -3.0F, 2.0F, 5.0F, 6.0F), PartPose.offsetAndRotation(-4.5F, -9.5F, 0.0F, 0.0F, 0.0F, 0.3839724354387525F));
        PartDefinition right_shoulder = chest.addOrReplaceChild("right_shoulder", CubeListBuilder.create().texOffs(44, 70).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 5.0F, 6.0F), PartPose.offset(-7.5F, -14.0F, 0.0F));
        PartDefinition right_upper = right_shoulder.addOrReplaceChild("right_upper", CubeListBuilder.create().texOffs(67, 70).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F), PartPose.offset(-0.5F, 2.0F, 0.0F));
        PartDefinition right_fore = right_upper.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(84, 70).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 11.0F, 3.5F).texOffs(101, 70).addBox(-2.25F, 8.5F, -2.25F, 4.5F, 2.5F, 4.5F), PartPose.offset(0.0F, 13.0F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(122, 70).addBox(-1.25F, 0.0F, -2.0F, 2.5F, 4.0F, 4.0F), PartPose.offset(0.0F, 11.0F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(137, 70).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 4.0F, -1.5F));
        PartDefinition right_finger_0_tip = right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create().texOffs(142, 70).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(147, 70).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.5F, 1.0F), PartPose.offset(0.0F, 4.0F, -0.5F));
        PartDefinition right_finger_1_tip = right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create().texOffs(152, 70).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offset(0.0F, 3.5F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(157, 70).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.25F, 1.0F), PartPose.offset(0.0F, 4.0F, 0.5F));
        PartDefinition right_finger_2_tip = right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create().texOffs(162, 70).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offset(0.0F, 3.25F, 0.0F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create().texOffs(167, 70).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.75F, 1.0F), PartPose.offset(0.0F, 4.0F, 1.5F));
        PartDefinition right_finger_3_tip = right_finger_3.addOrReplaceChild("right_finger_3_tip", CubeListBuilder.create().texOffs(172, 70).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offset(0.0F, 2.75F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(177, 70).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offsetAndRotation(1.0F, 1.0F, -1.75F, -0.4363323129985824F, 0.0F, 0.3490658503988659F));
        PartDefinition right_thumb_tip = right_thumb.addOrReplaceChild("right_thumb_tip", CubeListBuilder.create().texOffs(182, 70).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition left_shoulder = chest.addOrReplaceChild("left_shoulder", CubeListBuilder.create().texOffs(187, 70).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 5.0F, 6.0F), PartPose.offset(7.5F, -14.0F, 0.0F));
        PartDefinition left_upper = left_shoulder.addOrReplaceChild("left_upper", CubeListBuilder.create().texOffs(210, 70).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F), PartPose.offset(0.5F, 2.0F, 0.0F));
        PartDefinition left_fore = left_upper.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(227, 70).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 11.0F, 3.5F).texOffs(0, 88).addBox(-2.25F, 8.5F, -2.25F, 4.5F, 2.5F, 4.5F), PartPose.offset(0.0F, 13.0F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(21, 88).addBox(-1.25F, 0.0F, -2.0F, 2.5F, 4.0F, 4.0F), PartPose.offset(0.0F, 11.0F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(36, 88).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 4.0F, -1.5F));
        PartDefinition left_finger_0_tip = left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create().texOffs(41, 88).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(46, 88).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.5F, 1.0F), PartPose.offset(0.0F, 4.0F, -0.5F));
        PartDefinition left_finger_1_tip = left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create().texOffs(51, 88).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offset(0.0F, 3.5F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(56, 88).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.25F, 1.0F), PartPose.offset(0.0F, 4.0F, 0.5F));
        PartDefinition left_finger_2_tip = left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create().texOffs(61, 88).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offset(0.0F, 3.25F, 0.0F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create().texOffs(66, 88).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.75F, 1.0F), PartPose.offset(0.0F, 4.0F, 1.5F));
        PartDefinition left_finger_3_tip = left_finger_3.addOrReplaceChild("left_finger_3_tip", CubeListBuilder.create().texOffs(71, 88).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offset(0.0F, 2.75F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(76, 88).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offsetAndRotation(-1.0F, 1.0F, -1.75F, -0.4363323129985824F, 0.0F, -0.3490658503988659F));
        PartDefinition left_thumb_tip = left_thumb.addOrReplaceChild("left_thumb_tip", CubeListBuilder.create().texOffs(81, 88).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition fan = right_hand.addOrReplaceChild("fan", CubeListBuilder.create(), PartPose.offset(0.0F, 3.5F, -0.5F));
        PartDefinition fan_knife_0 = fan.addOrReplaceChild("fan_knife_0", CubeListBuilder.create().texOffs(86, 88).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(91, 88).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(98, 88).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5585053606381855F));
        PartDefinition fan_knife_1 = fan.addOrReplaceChild("fan_knife_1", CubeListBuilder.create().texOffs(103, 88).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(108, 88).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(115, 88).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2792526803190927F));
        PartDefinition fan_knife_2 = fan.addOrReplaceChild("fan_knife_2", CubeListBuilder.create().texOffs(120, 88).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(125, 88).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(132, 88).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition fan_knife_3 = fan.addOrReplaceChild("fan_knife_3", CubeListBuilder.create().texOffs(137, 88).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(142, 88).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(149, 88).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2792526803190927F));
        PartDefinition fan_knife_4 = fan.addOrReplaceChild("fan_knife_4", CubeListBuilder.create().texOffs(154, 88).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 2.5F, 1.0F).texOffs(159, 88).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 0.5F, 1.0F).texOffs(166, 88).addBox(-0.5F, 0.5F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5585053606381855F));
        PartDefinition spectral_blade = mesh.getRoot().addOrReplaceChild("spectral_blade", CubeListBuilder.create().texOffs(171, 88).addBox(-0.75F, -4.0F, -0.75F, 1.5F, 4.0F, 1.5F).texOffs(180, 88).addBox(-2.5F, 0.0F, -1.0F, 5.0F, 1.0F, 2.0F).texOffs(195, 88).addBox(-1.0F, 1.0F, -0.5F, 2.0F, 12.0F, 1.0F).texOffs(202, 88).addBox(-0.5F, 13.0F, -0.25F, 1.0F, 2.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(CircusKnifeThrowerEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        at = entity.getPosition(partialTick);
        bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        pose(limbSwing, limbSwingAmount, age, yaw, pitch, entity.gesture(), entity.gestureAge(partialTick), entity.gestureAt());
    }

    /** The whole pose; {@code gesture} is -1 at rest, {@code since} the ticks since its wind-up began. */
    public void pose(float limbSwing, float movement, float age, float yaw, float pitch, int gesture, float since, Vec3 target) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        blades = 0;
        fan.visible = true;
        float breath = Mth.sin(age * .06F);
        chest.xRot += .04F + breath * .015F;
        neck.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .3F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -35, 35) * Mth.DEG_TO_RAD;
        // At rest: weight on the left leg, the knife hand low and loose with its fan, the other hand at the waist.
        pelvis.zRot += .04F;
        thighs[0].zRot += .07F;
        thighs[1].zRot -= .02F;
        uppers[0].xRot = -.2F + breath * .02F;
        uppers[0].zRot = .14F;
        fores[0].xRot = -.5F;
        hands[0].yRot = -.4F;
        uppers[1].xRot = .05F;
        uppers[1].zRot = -.22F;
        fores[1].xRot = -1.05F;
        curl(0, .9F);
        curl(1, .45F + .1F * Mth.sin(age * .08F));
        for (int i = 0; i < 2; i++) tails[i].xRot += Mth.sin(age * .05F + i) * .04F;
        // Pacing the gallery: a side-step that crosses the feet, the body dipping on each step.
        float step = Mth.clamp(movement * 5.0F, 0.0F, 1.0F);
        float phase = limbSwing * 1.1F;
        root.y += Math.abs(Mth.sin(phase)) * .8F * step;
        for (int i = 0; i < 2; i++) {
            float swing = Mth.sin(phase + i * Mth.PI);
            thighs[i].zRot += swing * .22F * step;
            shins[i].xRot += Math.max(0.0F, swing) * .45F * step;
            thighs[i].xRot -= Math.max(0.0F, swing) * .3F * step;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            tails[i].zRot -= swing * .08F * step;
        }
        if (gesture >= 0) gesture(gesture, since, target);
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .06F);
            tips[i][f].zRot = sign * amount * 1.2F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .6F;
    }

    /** 0 until 2.5 ticks before {@code at}, then 1 at {@code at}, accelerating like a whip. */
    private static float whip(float since, float at) {
        float t = Mth.clamp((since - (at - 2.5F)) / 2.5F, 0.0F, 1.0F);
        return t * t;
    }

    private void gesture(int gesture, float since, Vec3 target) {
        switch (gesture) {
            case KnifeOrdealMoves.GESTURE_FAN -> fanThrow(since, target);
            case KnifeOrdealMoves.GESTURE_RING -> ringThrow(since, target);
            case KnifeOrdealMoves.GESTURE_PIN -> pinThrow(since, target);
            case KnifeOrdealMoves.GESTURE_BOARD -> boardThrow(since, target);
            default -> {
            }
        }
    }

    /** The fan: the knife arm drawn back over the shoulder, the other thrown out at the mark, then the whip across. */
    private void fanThrow(float since, Vec3 target) {
        float w = KnifeOrdealMoves.FAN_WINDUP;
        float gather = HemoEasing.smoothstep(0.0F, w - 4.0F, since);
        float tension = HemoEasing.smoothstep(w - 6.0F, w - 3.0F, since) * (1.0F - whip(since, w));
        float tremble = Mth.sin(since * 3.1F) * tension;
        float snap = whip(since, w);
        float weight = HemoEasing.smoothstep(0.0F, 5.0F, since) * (1.0F - HemoEasing.smoothstep(w + 8.0F, w + 22.0F, since));
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(snap, Mth.lerp(gather, -.4F, -2.7F) + .05F * tremble, -.7F));
        uppers[0].zRot = Mth.lerp(weight, uppers[0].zRot, Mth.lerp(snap, .5F, -.4F));
        uppers[0].yRot = Mth.lerp(weight, 0.0F, Mth.lerp(snap, .45F * gather, -.45F));
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(snap, Mth.lerp(gather, -.5F, -1.5F), -.1F));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, -1.45F);
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, -.1F);
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, -.1F);
        curl(1, Mth.lerp(weight, .45F, .05F));
        chest.yRot += weight * Mth.lerp(snap, .45F * gather, -.35F);
        chest.xRot += weight * Mth.lerp(snap, -.12F * gather, .24F);
        pelvis.yRot += weight * Mth.lerp(snap, .15F * gather, -.12F);
        thighs[1].xRot -= .4F * weight * snap;
        shins[1].xRot += .35F * weight * snap;
        thighs[0].xRot += .3F * weight * snap;
        for (int i = 0; i < 2; i++) feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
        // The fan leaves the hand on the whip and is back in it once they recover.
        fan.visible = since < w || since > w + 26.0F;
        if (since < w) return;
        // Five blades down the five lanes, from the hand at chest height across the ring and out past it.
        Vec3 origin = new Vec3(at.x, target.y, at.z);
        Vec3 hand = hand(0);
        double reach = Math.hypot(target.x - origin.x, target.z - origin.z);
        float flown = (since - w) / KnifeOrdealMoves.FLIGHT;
        float alpha = 1.0F - HemoEasing.smoothstep(1.5F, 3.0F, flown);
        if (alpha <= 0.0F) return;
        for (int lane = 0; lane < KnifeOrdealMoves.FAN_LANES; lane++) {
            Vec3 dir = KnifeOrdealMoves.fanDirection(origin, target, lane);
            Vec3 end = model(origin.add(dir.scale(reach)).add(0.0D, 1.2D, 0.0D));
            Vec3 path = end.subtract(hand);
            ghost(hand.add(path.scale(flown)), path, alpha);
        }
    }

    /** The ring: both arms flung up and out to throw the ring of blades high, held wide, then swept in together. */
    private void ringThrow(float since, Vec3 target) {
        float w = KnifeOrdealMoves.RING_WINDUP;
        float fling = HemoEasing.smoothstep(0.0F, 4.0F, since);
        float close = whip(since, w);
        float tension = HemoEasing.smoothstep(w - 6.0F, w - 3.0F, since) * (1.0F - close);
        float tremble = Mth.sin(since * 3.1F) * tension;
        float weight = fling * (1.0F - HemoEasing.smoothstep(w + 6.0F, w + 22.0F, since));
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(close, -2.3F + .05F * tremble, -1.35F));
            uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, Mth.lerp(close, -sign * 1.05F, sign * .3F));
            fores[i].xRot = Mth.lerp(weight, fores[i].xRot, Mth.lerp(close, -.3F, -.2F));
            curl(i, Mth.lerp(weight, .5F, Mth.lerp(close, .02F, 1.3F)));
        }
        chest.xRot += weight * (-.14F * fling * (1.0F - close) + .26F * close);
        head.xRot += .12F * weight * close;
        // The blades fall out of the dark point-first into a ring round the mark, then slide in together.
        float land = KnifeOrdealMoves.FLIGHT + 4.0F;
        float fall = Mth.clamp((since - 4.0F) / (land - 4.0F), 0.0F, 1.0F);
        if (since < 4.0F) return;
        float sliding = Math.max(0.0F, since - w) / KnifeOrdealMoves.RING_CLOSE;
        float alpha = 1.0F - HemoEasing.smoothstep(1.0F, 1.6F, sliding);
        if (alpha <= 0.0F) return;
        double radius = KnifeOrdealMoves.RING_RADIUS * (1.0D - Math.min(1.0D, sliding));
        for (int k = 0; k < KnifeOrdealMoves.RING_BLADES; k++) {
            Vec3 foot = KnifeOrdealMoves.ringBlade(target, k, radius);
            if (sliding <= 0.0F) {
                // Point-down, leaning out a little, falling then standing in the board.
                Vec3 lean = new Vec3(foot.x - target.x, 0.0D, foot.z - target.z).normalize().scale(.25D);
                Vec3 down = model(foot).subtract(model(foot.add(lean.x, 1.0D, lean.z)));
                Vec3 standing = model(foot.add(0.0D, 6.0D * (1.0F - fall * fall), 0.0D));
                ghost(standing.subtract(down.normalize().scale(11.0D)), down, alpha);
            } else {
                // Sliding in along the floor at shin height, points toward the mark.
                Vec3 in = model(target.add(0.0D, .4D, 0.0D)).subtract(model(foot.add(0.0D, .4D, 0.0D)));
                Vec3 shin = model(foot.add(0.0D, .4D, 0.0D));
                ghost(shin.subtract(in.lengthSqr() < 1.0E-4D ? Vec3.ZERO : in.normalize().scale(8.0D)), in, alpha);
            }
        }
    }

    /** The pin: one heavy blade raised high overhead, then hurled down in an arc onto the mark. */
    private void pinThrow(float since, Vec3 target) {
        float w = KnifeOrdealMoves.PIN_WINDUP, release = w - KnifeOrdealMoves.PIN_FLIGHT;
        float raise = HemoEasing.smoothstep(0.0F, release - 2.0F, since);
        float tension = HemoEasing.smoothstep(release - 4.0F, release - 1.0F, since) * (1.0F - whip(since, release));
        float tremble = Mth.sin(since * 3.1F) * tension;
        float snap = whip(since, release);
        float weight = HemoEasing.smoothstep(0.0F, 4.0F, since) * (1.0F - HemoEasing.smoothstep(w + 6.0F, w + 20.0F, since));
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(snap, Mth.lerp(raise, -.3F, -3.0F) + .05F * tremble, -.55F));
        uppers[0].zRot = Mth.lerp(weight, uppers[0].zRot, Mth.lerp(snap, .1F, -.2F));
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(snap, -.3F, -.05F));
        curl(0, Mth.lerp(weight, .9F, Mth.lerp(snap, 1.35F, .1F)));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, -1.1F);
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, -.3F);
        chest.xRot += weight * (-.24F * raise * (1.0F - snap) + .38F * snap);
        thighs[1].xRot -= .35F * weight * snap;
        shins[1].xRot += .3F * weight * snap;
        feet[1].xRot = -thighs[1].xRot - shins[1].xRot;
        fan.visible = false;
        Vec3 hand = hand(0);
        Vec3 mark = model(target);
        float alpha = 1.0F - HemoEasing.smoothstep(w + KnifeOrdealMoves.PIN_STUCK - 6.0F, w + KnifeOrdealMoves.PIN_STUCK, since);
        if (since < release) {
            // The blade manifests in the raised hand, point up.
            ghost(hand, new Vec3(0.0D, -1.0D, 0.0D), HemoEasing.smoothstep(1.0F, 6.0F, since));
            return;
        }
        if (alpha <= 0.0F) return;
        float t = Mth.clamp((since - release) / KnifeOrdealMoves.PIN_FLIGHT, 0.0F, 1.0F);
        if (t < 1.0F) {
            // A high lob: up out of the hand, over, and down point-first.
            Vec3 lift = new Vec3(0.0D, -16.0D * 4.0D * t * (1.0F - t), 0.0D);
            Vec3 now = hand.lerp(mark, t).add(lift);
            Vec3 later = hand.lerp(mark, Math.min(1.0F, t + .1F)).add(new Vec3(0.0D, -16.0D * 4.0D * (t + .1F) * (.9F - t), 0.0D));
            ghost(now, later.subtract(now), alpha);
            return;
        }
        Vec3 down = new Vec3(mark.x - hand.x, 0.0D, mark.z - hand.z).normalize().scale(.4D).add(0.0D, 1.0D, 0.0D);
        ghost(mark.subtract(down.normalize().scale(10.0D)), down, alpha);
    }

    /** The board: the knife arm raised and sighting, then six quick flicks round the outline and a last hard throw. */
    private void boardThrow(float since, Vec3 target) {
        int last = KnifeOrdealMoves.BULLSEYE_AT;
        float ready = HemoEasing.smoothstep(0.0F, 12.0F, since) * (1.0F - HemoEasing.smoothstep(last + 4.0F, last + 20.0F, since));
        float back = 0.0F, snap = 0.0F;
        for (int k = 0; k <= KnifeOrdealMoves.BOARD_KNIVES; k++) {
            float release = (k < KnifeOrdealMoves.BOARD_KNIVES ? KnifeOrdealMoves.boardLanding(k) : last) - KnifeOrdealMoves.FLIGHT;
            float big = k == KnifeOrdealMoves.BOARD_KNIVES ? 1.8F : 1.0F;
            float d = since - release;
            if (d < -3.0F || d > 4.0F) continue;
            back = Math.max(back, big * HemoEasing.smoothstep(-3.0F, -.8F, d) * (1.0F - whip(d, 0.0F)));
            snap = Math.max(snap, big * (d < 0.0F ? whip(d, 0.0F) : 1.0F - HemoEasing.smoothstep(0.0F, 4.0F, d)));
        }
        uppers[0].xRot = Mth.lerp(ready, uppers[0].xRot, -1.75F - .55F * back + .6F * snap);
        uppers[0].zRot = Mth.lerp(ready, uppers[0].zRot, .2F);
        fores[0].xRot = Mth.lerp(ready, fores[0].xRot, -.75F - .6F * back + .55F * snap);
        uppers[1].xRot = Mth.lerp(ready, uppers[1].xRot, -.85F);
        uppers[1].zRot = Mth.lerp(ready, uppers[1].zRot, .15F);
        fores[1].xRot = Mth.lerp(ready, fores[1].xRot, -1.35F);
        chest.yRot += ready * (.2F + .1F * back - .12F * snap);
        chest.xRot += ready * (.06F * snap);
        head.xRot += ready * .1F;
        // Each blade from the hand to its place on the outline, the last to the bullseye; all stay until the act ends.
        Vec3 hand = hand(0);
        float alpha = 1.0F - HemoEasing.smoothstep(last + KnifeOrdealMoves.BOARD_LINGER - 6.0F, last + KnifeOrdealMoves.BOARD_LINGER, since);
        if (alpha <= 0.0F) return;
        for (int k = 0; k <= KnifeOrdealMoves.BOARD_KNIVES; k++) {
            boolean bullseye = k == KnifeOrdealMoves.BOARD_KNIVES;
            float landing = bullseye ? last : KnifeOrdealMoves.boardLanding(k);
            float t = (since - (landing - KnifeOrdealMoves.FLIGHT)) / KnifeOrdealMoves.FLIGHT;
            if (t < 0.0F) continue;
            Vec3 spot = model(bullseye ? target : KnifeOrdealMoves.boardSpot(target, k));
            if (t < 1.0F) {
                ghost(hand.lerp(spot, t * t), spot.subtract(hand), alpha);
            } else {
                Vec3 down = new Vec3(spot.x - hand.x, 0.0D, spot.z - hand.z).normalize().scale(.5D).add(0.0D, 1.0D, 0.0D);
                ghost(spot.subtract(down.normalize().scale(10.0D)), down, alpha);
            }
        }
    }

    /** A hand's palm this frame, in model pixels. */
    private Vec3 hand(int i) {
        PoseStack pose = new PoseStack();
        for (ModelPart part : new ModelPart[]{root, pelvis, chest, shoulders[i], uppers[i], fores[i], hands[i]})
            part.translateAndRotate(pose);
        Vector3f palm = pose.last().pose().transformPosition(new Vector3f(0.0F, 4.0F / 16.0F, 0.0F)).mul(16.0F);
        return new Vec3(palm.x, palm.y, palm.z);
    }

    private void ghost(Vec3 position, Vec3 direction, float alpha) {
        if (blades >= MAX_BLADES || direction.lengthSqr() < 1.0E-6D) return;
        bladeAt[blades] = position;
        bladeDir[blades] = direction.normalize();
        bladeAlpha[blades] = Mth.clamp(alpha, 0.0F, 1.0F);
        blades++;
    }

    /** Draws this frame's spectral blades, each its own part laid along its flight with its point leading. */
    public void renderGhosts(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        for (int i = 0; i < blades; i++) {
            if (bladeAlpha[i] <= .01F) continue;
            Vec3 dir = bladeDir[i], p = bladeAt[i];
            blade.setPos((float) p.x, (float) p.y, (float) p.z);
            blade.setRotation((float) Math.asin(Mth.clamp(dir.z, -1.0D, 1.0D)), 0.0F, (float) Math.atan2(-dir.x, dir.y));
            int alpha = (int) (bladeAlpha[i] * ((color >>> 24) & 255));
            blade.render(pose, consumer, light, overlay, (alpha << 24) | (color & 0xFFFFFF));
        }
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / KnifeOrdealMoves.SCALE);
        double turn = -(180.0F - bodyYaw) * Mth.DEG_TO_RAD;
        double cos = Math.cos(turn), sin = Math.sin(turn);
        double x = d.x * cos + d.z * sin, z = -d.x * sin + d.z * cos;
        return new Vec3(-x * 16.0D, (-d.y + 1.501D) * 16.0D, z * 16.0D);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
