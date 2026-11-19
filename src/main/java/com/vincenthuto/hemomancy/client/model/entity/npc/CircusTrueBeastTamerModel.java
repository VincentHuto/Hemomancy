package com.vincenthuto.hemomancy.client.model.entity.npc;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.BeastTamerMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Authored by tools/circus/build_true_beast_tamer.py; regenerate the model, atlas and BBModel together.
/**
 * The Beast Tamer's true self: the same person, the pelt of a beast over their shoulders and its skull on their crown,
 * walking the dark outside the cage with the whip ready in the right fist and the left hand open toward the ring. Each
 * intervention is a gesture ({@link CircusFacultyEntity#gesture}) timed from its wind-up: the lash drawn back and cracked
 * down its lane, twirled overhead and cracked down to light the hoop, flicked over the Hound to goad it, and both arms
 * flung up to call the beasts out of the dark. What reaches into the ring is drawn by {@link #renderLash} (the lash, a
 * strand of their blood, from the fist to the gesture's synced point) and {@link #renderBeasts}.
 */
public final class CircusTrueBeastTamerModel extends EntityModel<CircusFacultyEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_beast_tamer"), "main");
    /** Segments of the drawn lash, and how far past the mark the whipcrack runs, in blocks. */
    private static final int SEGMENTS = 28;
    private final ModelPart root, hips, chest, neck, head, handle, ghostLash, beast, beastJaw;
    private final ModelPart[] uppers = new ModelPart[2], fores = new ModelPart[2], hands = new ModelPart[2],
            thumbs = new ModelPart[2], thumbTips = new ModelPart[2], thighs = new ModelPart[2], shins = new ModelPart[2],
            feet = new ModelPart[2], tails = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    private final ModelPart[] lash = new ModelPart[4];
    /** This frame's body: where it stands and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;
    /** This frame's lash (model pixels, fist to tip) and how strongly it shows; and each beast's place, turn and showing. */
    private final Vec3[] lashPoints = new Vec3[SEGMENTS + 1];
    private float lashWeight;
    private final Vec3[] beastAt = new Vec3[BeastTamerMoves.BEASTS];
    private final float[] beastYaw = new float[BeastTamerMoves.BEASTS], beastWeight = new float[BeastTamerMoves.BEASTS],
            beastGape = new float[BeastTamerMoves.BEASTS];

    public CircusTrueBeastTamerModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        hips = root.getChild("hips");
        chest = hips.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            uppers[i] = chest.getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            thumbTips[i] = thumbs[i].getChild(side + "_thumb_tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
            thighs[i] = hips.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
            tails[i] = hips.getChild(side + "_tail");
        }
        handle = hands[0].getChild("whip_handle");
        ModelPart segment = handle;
        for (int i = 0; i < lash.length; i++) {
            segment = segment.getChild("lash_" + i);
            lash[i] = segment;
        }
        ghostLash = bakedRoot.getChild("lash_ghost");
        beast = bakedRoot.getChild("beast");
        beastJaw = beast.getChild("beast_jaw");
        for (int i = 0; i <= SEGMENTS; i++) lashPoints[i] = Vec3.ZERO;
        for (int i = 0; i < beastAt.length; i++) beastAt[i] = Vec3.ZERO;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition hips = root.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -3.0F, -2.5F, 9.0F, 4.0F, 5.0F), PartPose.offset(0.0F, -16.0F, 0.0F));
        PartDefinition belt = hips.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(29, 0).addBox(-5.0F, -1.0F, -3.0F, 10.0F, 2.0F, 6.0F), PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition buckle = belt.addOrReplaceChild("buckle", CubeListBuilder.create().texOffs(62, 0).addBox(-1.5F, -1.25F, -0.5F, 3.0F, 2.5F, 0.5F), PartPose.offset(0.0F, 0.0F, -3.0F));
        PartDefinition keys = belt.addOrReplaceChild("keys", CubeListBuilder.create().texOffs(71, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F).texOffs(78, 0).addBox(-0.25F, 2.0F, -0.75F, 0.5F, 3.0F, 0.5F).texOffs(83, 0).addBox(-0.25F, 2.0F, 0.25F, 0.5F, 2.5F, 0.5F), PartPose.offsetAndRotation(4.5F, 1.0F, -1.0F, 0.0F, 0.0F, -0.17453292519943295F));
        PartDefinition chest = hips.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(88, 0).addBox(-5.0F, -12.0F, -3.0F, 10.0F, 12.0F, 6.0F), PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition mantle = chest.addOrReplaceChild("mantle", CubeListBuilder.create().texOffs(121, 0).addBox(-6.5F, -1.5F, -3.5F, 13.0F, 4.0F, 7.5F).texOffs(164, 0).addBox(-5.5F, 2.5F, 2.0F, 11.0F, 7.0F, 2.0F), PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(191, 0).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -12.0F, -0.5F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(204, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition tail_of_hair = head.addOrReplaceChild("tail_of_hair", CubeListBuilder.create().texOffs(237, 0).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 5.0F, 1.5F), PartPose.offsetAndRotation(0.0F, -3.0F, 4.0F, 0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition skull = head.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(0, 19).addBox(-4.5F, -2.5F, -3.5F, 9.0F, 3.0F, 8.0F).texOffs(35, 19).addBox(-2.5F, -1.75F, -7.5F, 5.0F, 2.5F, 4.0F), PartPose.offset(0.0F, -8.0F, 0.0F));
        PartDefinition right_fang = skull.addOrReplaceChild("right_fang", CubeListBuilder.create().texOffs(54, 19).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(-1.5F, 0.75F, -7.0F));
        PartDefinition right_pelt_ear = skull.addOrReplaceChild("right_pelt_ear", CubeListBuilder.create().texOffs(59, 19).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 2.5F, 1.5F), PartPose.offsetAndRotation(-3.5F, -2.5F, 0.0F, 0.0F, 0.0F, -0.20943951023931956F));
        PartDefinition left_fang = skull.addOrReplaceChild("left_fang", CubeListBuilder.create().texOffs(68, 19).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(1.5F, 0.75F, -7.0F));
        PartDefinition left_pelt_ear = skull.addOrReplaceChild("left_pelt_ear", CubeListBuilder.create().texOffs(73, 19).addBox(-1.0F, -2.5F, -0.5F, 2.0F, 2.5F, 1.5F), PartPose.offsetAndRotation(3.5F, -2.5F, 0.0F, 0.0F, 0.0F, 0.20943951023931956F));
        PartDefinition hide = skull.addOrReplaceChild("hide", CubeListBuilder.create().texOffs(82, 19).addBox(-4.5F, 0.0F, -0.5F, 9.0F, 9.0F, 1.5F), PartPose.offsetAndRotation(0.0F, -1.0F, 4.5F, 0.20943951023931956F, 0.0F, 0.0F));
        PartDefinition right_upper = chest.addOrReplaceChild("right_upper", CubeListBuilder.create().texOffs(105, 19).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.offset(-6.5F, -11.0F, 0.0F));
        PartDefinition right_epaulette = right_upper.addOrReplaceChild("right_epaulette", CubeListBuilder.create().texOffs(122, 19).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 1.5F, 5.0F).texOffs(143, 19).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 1.5F, 5.0F), PartPose.offset(0.0F, -1.5F, 0.0F));
        PartDefinition right_fore = right_upper.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(164, 19).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 6.0F, 3.5F).texOffs(181, 19).addBox(-2.25F, 4.0F, -2.25F, 4.5F, 2.0F, 4.5F), PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(202, 19).addBox(-1.0F, 0.0F, -1.75F, 2.0F, 3.0F, 3.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(215, 19).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.75F, 0.75F), PartPose.offset(0.0F, 3.0F, -1.25F));
        PartDefinition right_finger_0_tip = right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create().texOffs(220, 19).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 1.75F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(225, 19).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, -0.5F));
        PartDefinition right_finger_1_tip = right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create().texOffs(230, 19).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(235, 19).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.25F));
        PartDefinition right_finger_2_tip = right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create().texOffs(240, 19).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create().texOffs(245, 19).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.75F, 0.75F), PartPose.offset(0.0F, 3.0F, 1.0F));
        PartDefinition right_finger_3_tip = right_finger_3.addOrReplaceChild("right_finger_3_tip", CubeListBuilder.create().texOffs(250, 19).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 1.75F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(0, 32).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.75F, 0.5F, -1.75F, -0.4363323129985824F, 0.0F, 0.3490658503988659F));
        PartDefinition right_thumb_tip = right_thumb.addOrReplaceChild("right_thumb_tip", CubeListBuilder.create().texOffs(5, 32).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition right_thigh = hips.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(10, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F), PartPose.offset(-2.25F, 1.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(27, 32).addBox(-2.0F, 0.0F, -2.25F, 4.0F, 6.0F, 4.5F).texOffs(46, 32).addBox(-2.5F, -0.5F, -2.75F, 5.0F, 2.0F, 5.5F), PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition right_foot = right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(69, 32).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 5.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition right_tail = hips.addOrReplaceChild("right_tail", CubeListBuilder.create().texOffs(90, 32).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 13.0F, 1.0F), PartPose.offsetAndRotation(-2.25F, 1.0F, 2.5F, 0.17453292519943295F, 0.0F, 0.08726646259971647F));
        PartDefinition right_skirt = hips.addOrReplaceChild("right_skirt", CubeListBuilder.create().texOffs(101, 32).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(-2.5F, 1.0F, -2.75F, -0.10471975511965978F, 0.0F, 0.06981317007977318F));
        PartDefinition left_upper = chest.addOrReplaceChild("left_upper", CubeListBuilder.create().texOffs(112, 32).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.offset(6.5F, -11.0F, 0.0F));
        PartDefinition left_epaulette = left_upper.addOrReplaceChild("left_epaulette", CubeListBuilder.create().texOffs(129, 32).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 1.5F, 5.0F).texOffs(150, 32).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 1.5F, 5.0F), PartPose.offset(0.0F, -1.5F, 0.0F));
        PartDefinition left_fore = left_upper.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(171, 32).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 6.0F, 3.5F).texOffs(188, 32).addBox(-2.25F, 4.0F, -2.25F, 4.5F, 2.0F, 4.5F), PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition shackle = left_fore.addOrReplaceChild("shackle", CubeListBuilder.create().texOffs(209, 32).addBox(-2.5F, -0.75F, -2.5F, 5.0F, 1.5F, 5.0F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition shackle_link = shackle.addOrReplaceChild("shackle_link", CubeListBuilder.create().texOffs(230, 32).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(2.0F, 0.75F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(235, 32).addBox(-1.0F, 0.0F, -1.75F, 2.0F, 3.0F, 3.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(248, 32).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.75F, 0.75F), PartPose.offset(0.0F, 3.0F, -1.25F));
        PartDefinition left_finger_0_tip = left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create().texOffs(0, 47).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 1.75F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(5, 47).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, -0.5F));
        PartDefinition left_finger_1_tip = left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create().texOffs(10, 47).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(15, 47).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.25F));
        PartDefinition left_finger_2_tip = left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create().texOffs(20, 47).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create().texOffs(25, 47).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.75F, 0.75F), PartPose.offset(0.0F, 3.0F, 1.0F));
        PartDefinition left_finger_3_tip = left_finger_3.addOrReplaceChild("left_finger_3_tip", CubeListBuilder.create().texOffs(30, 47).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 1.75F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(35, 47).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(-0.75F, 0.5F, -1.75F, -0.4363323129985824F, 0.0F, -0.3490658503988659F));
        PartDefinition left_thumb_tip = left_thumb.addOrReplaceChild("left_thumb_tip", CubeListBuilder.create().texOffs(40, 47).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition left_thigh = hips.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(45, 47).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F), PartPose.offset(2.25F, 1.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(62, 47).addBox(-2.0F, 0.0F, -2.25F, 4.0F, 6.0F, 4.5F).texOffs(81, 47).addBox(-2.5F, -0.5F, -2.75F, 5.0F, 2.0F, 5.5F), PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition left_foot = left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(104, 47).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 5.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition left_tail = hips.addOrReplaceChild("left_tail", CubeListBuilder.create().texOffs(125, 47).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 13.0F, 1.0F), PartPose.offsetAndRotation(2.25F, 1.0F, 2.5F, 0.17453292519943295F, 0.0F, -0.08726646259971647F));
        PartDefinition left_skirt = hips.addOrReplaceChild("left_skirt", CubeListBuilder.create().texOffs(136, 47).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(2.5F, 1.0F, -2.75F, -0.10471975511965978F, 0.0F, -0.06981317007977318F));
        PartDefinition whip_handle = right_hand.addOrReplaceChild("whip_handle", CubeListBuilder.create().texOffs(147, 47).addBox(-0.75F, -0.75F, -5.0F, 1.5F, 1.5F, 6.0F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition lash_0 = whip_handle.addOrReplaceChild("lash_0", CubeListBuilder.create().texOffs(164, 47).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 0.0F, -5.0F));
        PartDefinition lash_1 = lash_0.addOrReplaceChild("lash_1", CubeListBuilder.create().texOffs(169, 47).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition lash_2 = lash_1.addOrReplaceChild("lash_2", CubeListBuilder.create().texOffs(174, 47).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition lash_3 = lash_2.addOrReplaceChild("lash_3", CubeListBuilder.create().texOffs(179, 47).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition lash_ghost = mesh.getRoot().addOrReplaceChild("lash_ghost", CubeListBuilder.create().texOffs(184, 47).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition beast = mesh.getRoot().addOrReplaceChild("beast", CubeListBuilder.create().texOffs(197, 47).addBox(-4.5F, -4.0F, -4.0F, 9.0F, 8.0F, 8.0F).texOffs(232, 47).addBox(-4.5F, -5.0F, -4.5F, 9.0F, 1.5F, 3.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition beast_mane = beast.addOrReplaceChild("beast_mane", CubeListBuilder.create().texOffs(0, 67).addBox(-7.0F, -7.0F, -1.5F, 14.0F, 14.0F, 3.0F).texOffs(35, 67).addBox(-6.0F, -6.0F, 1.5F, 12.0F, 12.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 1.0F));
        PartDefinition beast_muzzle = beast.addOrReplaceChild("beast_muzzle", CubeListBuilder.create().texOffs(68, 67).addBox(-3.0F, -2.0F, -5.0F, 6.0F, 4.0F, 5.0F).texOffs(91, 67).addBox(-1.5F, -2.5F, -5.5F, 3.0F, 1.5F, 1.5F), PartPose.offset(0.0F, 1.0F, -4.0F));
        PartDefinition beast_right_fang = beast_muzzle.addOrReplaceChild("beast_right_fang", CubeListBuilder.create().texOffs(102, 67).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(-2.0F, 2.0F, -4.0F));
        PartDefinition beast_right_ear = beast.addOrReplaceChild("beast_right_ear", CubeListBuilder.create().texOffs(107, 67).addBox(-1.0F, -2.5F, -0.75F, 2.0F, 2.5F, 1.5F), PartPose.offset(-3.5F, -4.0F, 0.0F));
        PartDefinition beast_left_fang = beast_muzzle.addOrReplaceChild("beast_left_fang", CubeListBuilder.create().texOffs(116, 67).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(2.0F, 2.0F, -4.0F));
        PartDefinition beast_left_ear = beast.addOrReplaceChild("beast_left_ear", CubeListBuilder.create().texOffs(121, 67).addBox(-1.0F, -2.5F, -0.75F, 2.0F, 2.5F, 1.5F), PartPose.offset(3.5F, -4.0F, 0.0F));
        PartDefinition beast_jaw = beast.addOrReplaceChild("beast_jaw", CubeListBuilder.create().texOffs(130, 67).addBox(-2.5F, 0.0F, -5.5F, 5.0F, 2.0F, 5.5F), PartPose.offset(0.0F, 3.0F, -3.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(CircusFacultyEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
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
        movement = Mth.clamp(movement, 0.0F, 1.0F);
        float breath = Mth.sin(age * .06F);
        float stride = limbSwing * .6662F;
        // A watchful walk: long strides, the coat tails swinging, the shoulders easy.
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            float swing = Mth.cos(stride + i * Mth.PI) * movement;
            thighs[i].xRot = swing * .62F;
            shins[i].xRot = Math.max(0.0F, -Mth.cos(stride + i * Mth.PI + .9F)) * .9F * movement;
            feet[i].xRot = -thighs[i].xRot * .5F - shins[i].xRot * .6F;
            tails[i].xRot += movement * .35F + Math.abs(swing) * .15F + breath * .02F;
            uppers[i].xRot = -swing * .35F;
            uppers[i].zRot = -sign * .1F;
        }
        hips.y += Math.abs(Mth.sin(stride)) * -.6F * movement;
        chest.xRot += .04F + breath * .015F;
        chest.yRot += Mth.cos(stride) * .05F * movement;
        // At rest: the whip down at the right side, the left hand open toward the ring.
        uppers[0].xRot += -.15F;
        fores[0].xRot = -.55F;
        curl(0, 1.35F);
        uppers[1].xRot += -.6F;
        uppers[1].zRot = -.05F;
        fores[1].xRot = -.45F;
        hands[1].zRot = -.2F;
        curl(1, .25F + .2F * HemoEasing.smoothstep(.85F, 1.0F, Mth.sin(age * .023F)));
        for (int k = 0; k < lash.length; k++)
            lash[k].xRot = (k == 0 ? 1.2F : .12F) + Mth.sin(age * .09F - k * .8F) * .12F + movement * .1F;
        neck.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .25F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .75F;
        head.xRot += Mth.clamp(pitch, -35, 35) * Mth.DEG_TO_RAD;
        lashWeight = 0.0F;
        for (int i = 0; i < beastWeight.length; i++) beastWeight[i] = 0.0F;
        if (gesture >= 0) gesture(gesture, since, target, age);
        for (ModelPart segment : lash) segment.visible = lashWeight < .35F;
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .05F);
            tips[i][f].zRot = sign * amount * 1.2F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .6F;
    }

    private static int windup(int gesture) {
        return switch (gesture) {
            case BeastTamerMoves.GESTURE_CRACK -> BeastTamerMoves.CRACK_WINDUP;
            case BeastTamerMoves.GESTURE_HOOP -> BeastTamerMoves.HOOP_WINDUP;
            case BeastTamerMoves.GESTURE_GOAD -> BeastTamerMoves.GOAD_WINDUP;
            default -> BeastTamerMoves.BEASTS_WINDUP;
        };
    }

    /** The whip's crack takes the last three ticks of the wind-up, accelerating: 0 until then, 1 on the blow. */
    private static float crack(float since, float windup) {
        float t = Mth.clamp((since - (windup - 3.0F)) / 3.0F, 0.0F, 1.0F);
        return t * t;
    }

    private void gesture(int gesture, float since, Vec3 target, float age) {
        float w = windup(gesture);
        float end = gesture == BeastTamerMoves.GESTURE_BEASTS
                ? BeastTamerMoves.beastLands(BeastTamerMoves.BEASTS - 1) + BeastTamerMoves.BEAST_RUN
                : gesture == BeastTamerMoves.GESTURE_HOOP ? w + 10.0F : w + BeastTamerMoves.HELD;
        float weight = HemoEasing.smoothstep(0.0F, 5.0F, since) * (1.0F - HemoEasing.smoothstep(end, end + BeastTamerMoves.RECOVER, since));
        if (weight <= 0.0F) return;
        float gather = HemoEasing.smoothstep(0.0F, w - 4.0F, since);
        float tension = HemoEasing.smoothstep(w - 7.0F, w - 3.0F, since) * (1.0F - HemoEasing.smoothstep(w - 3.0F, w, since));
        float tremble = Mth.sin(since * 3.3F) * tension;
        float down = crack(since, w);
        Vec3 hand = handleTip();
        Vec3 mark = model(target);
        if (gesture == BeastTamerMoves.GESTURE_BEASTS) {
            // Both arms flung up and wide, the head thrown back: calling them out of the dark. Each leap a jab of the fists.
            float jab = 0.0F;
            for (int i = 0; i < BeastTamerMoves.BEASTS; i++) {
                float s = since - BeastTamerMoves.beastLands(i);
                jab = Math.max(jab, s >= 0.0F && s < 6.0F ? 1.0F - s / 6.0F : 0.0F);
            }
            for (int i = 0; i < 2; i++) {
                float sign = i == 0 ? -1 : 1;
                uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, -2.55F + .5F * jab + .06F * tremble);
                uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, -sign * (.75F - .25F * jab));
                fores[i].xRot = Mth.lerp(weight, fores[i].xRot, -.25F - .4F * jab);
            }
            curl(1, Mth.lerp(weight, .3F, .05F + .9F * jab));
            head.xRot += weight * (-.45F + .25F * jab);
            chest.xRot += weight * (-.18F + .2F * jab);
            hand = handleTip();
            lashWeight = weight;
            for (int s = 0; s <= SEGMENTS; s++) {
                float f = s / (float) SEGMENTS;
                // The lash raised high and snaking over their head.
                lashPoints[s] = hand.add(Mth.sin(age * .25F + f * 5.0F) * 7.0F * f, -42.0F * f, Mth.cos(age * .2F + f * 4.0F) * 5.0F * f);
            }
            beasts(since, target);
            return;
        }
        float back = gather * (1.0F - down);
        boolean hoop = gesture == BeastTamerMoves.GESTURE_HOOP;
        // The whip arm: hauled back overhead through the gather (twirling it, for the hoop), trembling at the top, then
        // cracked forward and down in three ticks; held, then a slow recovery.
        float up = hoop ? -2.9F : Mth.lerp(gather, -.6F, -2.75F);
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(down, up + .05F * tremble, gesture == BeastTamerMoves.GESTURE_GOAD ? -1.5F : -1.05F));
        uppers[0].zRot = Mth.lerp(weight, uppers[0].zRot, .22F);
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(down, hoop ? -.3F : Mth.lerp(gather, -.55F, -1.6F), -.1F));
        if (hoop) fores[0].zRot += Mth.sin(since * .9F) * .45F * back * weight;
        chest.yRot += weight * (.38F * back - .32F * down);
        chest.xRot += weight * (-.12F * back + .24F * down);
        head.xRot += weight * .12F * down;
        // The left arm out for balance, the stance set: right foot back.
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, -.4F - .6F * back);
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, -.75F);
        curl(1, Mth.lerp(weight, .3F, .1F));
        thighs[0].xRot += weight * .35F;
        thighs[1].xRot -= weight * .3F;
        shins[0].xRot += weight * .25F;
        feet[0].xRot = -thighs[0].xRot * .5F - shins[0].xRot * .6F;
        feet[1].xRot = -thighs[1].xRot * .5F - shins[1].xRot * .6F;
        hand = handleTip();
        lashWeight = weight;
        // Where the lash lands: down its lane past the mark, onto the hoop, or cracked in the air over the Hound.
        Vec3 flat = new Vec3(mark.x - hand.x, 0.0D, mark.z - hand.z);
        flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, -1.0D) : flat.normalize();
        Vec3 tip = switch (gesture) {
            case BeastTamerMoves.GESTURE_CRACK -> mark.add(flat.scale(BeastTamerMoves.CRACK_OVERSHOOT * 16.0D / BeastTamerMoves.SCALE));
            case BeastTamerMoves.GESTURE_GOAD -> mark.add(0.0D, -2.2D * 16.0D / BeastTamerMoves.SCALE, 0.0D);
            default -> mark;
        };
        boolean air = gesture == BeastTamerMoves.GESTURE_GOAD;
        float retract = HemoEasing.smoothstep(end, end + BeastTamerMoves.RECOVER, since);
        for (int s = 0; s <= SEGMENTS; s++) {
            float f = s / (float) SEGMENTS;
            Vec3 drawn = hoop
                    // Twirled overhead in a widening spiral.
                    ? hand.add(Mth.cos(since * .9F + f * 2.6F) * (5.0F + 20.0F * f), -6.0F - 4.0F * f,
                            Mth.sin(since * .9F + f * 2.6F) * (5.0F + 20.0F * f))
                    // Drawn back over the shoulder and coiled behind, swaying with the breath of the gather.
                    : hand.add(0.0D, -5.0F - 12.0F * f + Mth.sin(f * 6.0F + since * .4F) * 2.5F * f, 8.0F + 28.0F * f);
            Vec3 laid = hand.lerp(tip, f);
            laid = air ? laid.add(0.0D, -Mth.sin(f * Mth.PI) * 8.0F, 0.0D)
                    // Down to the floor fast from the fist, then along it.
                    : new Vec3(laid.x, tip.y + (hand.y - tip.y) * Math.pow(1.0F - f, 3.0F), laid.z);
            // The whip unrolls: its base goes first, its tip last.
            float unroll = Mth.clamp(down * 1.6F - f * .6F, 0.0F, 1.0F);
            Vec3 point = drawn.lerp(laid, unroll);
            lashPoints[s] = hand.add(point.subtract(hand).scale(1.0F - .85F * retract));
        }
    }

    /** Each beast: crouched in the dark, then bounding down its lane, jaws wide, then fading at its end. */
    private void beasts(float since, Vec3 target) {
        for (int i = 0; i < BeastTamerMoves.BEASTS; i++) {
            float s = since - BeastTamerMoves.beastLands(i);
            float run = BeastTamerMoves.BEAST_RUN;
            if (s < -10.0F || s > run + 6.0F) continue;
            Vec3 dir = BeastTamerMoves.beastDir(target, i);
            Vec3 world;
            if (s < 0.0F) {
                world = BeastTamerMoves.beastFrom(target, i).add(0.0D, .7D, 0.0D);
                beastWeight[i] = HemoEasing.smoothstep(-10.0F, -2.0F, s) * .75F;
                beastGape[i] = .15F;
            } else {
                float t = Math.min(s, run);
                world = BeastTamerMoves.beastAt(target, i, t).add(0.0D, 1.0D + Math.sin(Math.PI * t / run) * 1.1D, 0.0D);
                beastWeight[i] = 1.0F - HemoEasing.smoothstep(run, run + 6.0F, s);
                beastGape[i] = s <= run ? 1.0F : 1.0F - (s - run) / 6.0F;
            }
            beastAt[i] = model(world);
            Vec3 ahead = model(world.add(dir)).subtract(beastAt[i]);
            beastYaw[i] = (float) Math.atan2(-ahead.x, -ahead.z);
        }
    }

    /** Where the whip's handle ends in the fist, in this model's pixels. */
    private Vec3 handleTip() {
        PoseStack stack = new PoseStack();
        for (ModelPart part : new ModelPart[]{root, hips, chest, uppers[0], fores[0], hands[0], handle})
            part.translateAndRotate(stack);
        Vector3f p = stack.last().pose().transformPosition(0.0F, 0.0F, -5.0F / 16.0F, new Vector3f());
        return new Vec3(p.x * 16.0D, p.y * 16.0D, p.z * 16.0D);
    }

    /** Draws the reaching lash: a strand of their blood from the fist to this frame's tip, tapering, faded with its move. */
    public void renderLash(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        if (lashWeight <= .01F) return;
        int alpha = (int) (Mth.clamp(lashWeight, 0.0F, 1.0F) * ((color >>> 24) & 255));
        for (int s = 0; s < SEGMENTS; s++) {
            Vec3 a = lashPoints[s], d = lashPoints[s + 1].subtract(a);
            double length = d.length();
            if (length < 1.0E-3D) continue;
            Vec3 dir = d.scale(1.0D / length);
            ghostLash.setPos((float) a.x, (float) a.y, (float) a.z);
            ghostLash.setRotation((float) Math.asin(Mth.clamp(dir.z, -1.0D, 1.0D)), 0.0F, (float) Math.atan2(-dir.x, dir.y));
            float taper = Mth.lerp(s / (float) SEGMENTS, 1.0F, .35F);
            ghostLash.xScale = ghostLash.zScale = taper;
            ghostLash.yScale = (float) (length / 16.0D) * 1.05F;
            ghostLash.render(pose, consumer, light, overlay, (alpha << 24) | (color & 0xFFFFFF));
        }
    }

    /** Draws the beasts out of the dark: each a caged animal's head in red light, its eyes lit, its jaws open. */
    public void renderBeasts(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        for (int i = 0; i < BeastTamerMoves.BEASTS; i++) {
            if (beastWeight[i] <= .01F) continue;
            beast.setPos((float) beastAt[i].x, (float) beastAt[i].y, (float) beastAt[i].z);
            beast.setRotation(0.0F, beastYaw[i], 0.0F);
            beast.xScale = beast.yScale = beast.zScale = 1.3F;
            beastJaw.xRot = beastGape[i] * .75F;
            int alpha = (int) (Mth.clamp(beastWeight[i], 0.0F, 1.0F) * ((color >>> 24) & 255));
            beast.render(pose, consumer, light, overlay, (alpha << 24) | (color & 0xFFFFFF));
        }
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / BeastTamerMoves.SCALE);
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
