package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealStiltWalk;
import com.vincenthuto.hemomancy.common.encounter.ordeal.StiltWalkerMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusStiltWalkerEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Authored by tools/circus/build_true_stilt_walker.py; regenerate the model, atlas and BBModel together.
/**
 * The Stilt Walker's true self: about twelve blocks tall, their legs stilt from the knee down, walking round the
 * Mummer's ring one long stride at a time (the synced stride count drives the legs). Each intervention is a gesture
 * ({@link CircusStiltWalkerEntity#gesture}) timed from its wind-up; their own body makes the motion where it stands,
 * and spectral copies of it, drawn by {@link #renderGhosts}, do it in the ring: a stilt-leg planted like a spear, two
 * walking four footfalls across, two stilts laid across the ring and spun, and their whole shadow falling.
 */
public final class CircusTrueStiltWalkerModel extends EntityModel<CircusStiltWalkerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_stilt_walker"), "main");
    /** A leg's length in pixels, thigh and stilt-shin; and a stilt-shin's alone. */
    private static final float LEG = 61.0F, SHIN = 39.0F;
    /** The model's height from the stilts' tips to the plume, in pixels. */
    private static final float HEIGHT = 121.0F;
    private final ModelPart root, hips, waist, chest, neck, head, hat;
    private final ModelPart[] thighs = new ModelPart[2], shins = new ModelPart[2], tails = new ModelPart[2],
            uppers = new ModelPart[2], fores = new ModelPart[2], hands = new ModelPart[2], thumbs = new ModelPart[2],
            thumbTips = new ModelPart[2], cords = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    /** This frame's body: where it stands and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;

    public CircusTrueStiltWalkerModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        hips = root.getChild("hips");
        waist = hips.getChild("waist");
        chest = waist.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        hat = head.getChild("hat");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            thighs[i] = hips.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            tails[i] = waist.getChild(side + "_tail");
            cords[i] = chest.getChild(side + "_cord");
            uppers[i] = chest.getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            thumbTips[i] = thumbs[i].getChild(side + "_thumb_tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition hips = root.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -2.0F, -3.5F, 12.0F, 5.0F, 7.0F), PartPose.offset(0.0F, -62.0F, 0.0F));
        PartDefinition right_thigh = hips.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(39, 0).addBox(-2.25F, 0.0F, -2.25F, 4.5F, 22.0F, 4.5F), PartPose.offset(-3.5F, 1.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(60, 0).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 4.0F, 5.0F).texOffs(81, 0).addBox(-1.5F, 3.0F, -1.5F, 3.0F, 30.0F, 3.0F).texOffs(94, 0).addBox(-1.75F, 10.0F, -1.75F, 3.5F, 1.5F, 3.5F).texOffs(111, 0).addBox(-1.75F, 23.0F, -1.75F, 3.5F, 1.5F, 3.5F).texOffs(128, 0).addBox(-1.0F, 33.0F, -1.0F, 2.0F, 4.0F, 2.0F).texOffs(137, 0).addBox(-1.25F, 37.0F, -1.25F, 2.5F, 2.0F, 2.5F), PartPose.offset(0.0F, 22.0F, 0.0F));
        PartDefinition left_thigh = hips.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(150, 0).addBox(-2.25F, 0.0F, -2.25F, 4.5F, 22.0F, 4.5F), PartPose.offset(3.5F, 1.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(171, 0).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 4.0F, 5.0F).texOffs(192, 0).addBox(-1.5F, 3.0F, -1.5F, 3.0F, 30.0F, 3.0F).texOffs(205, 0).addBox(-1.75F, 10.0F, -1.75F, 3.5F, 1.5F, 3.5F).texOffs(222, 0).addBox(-1.75F, 23.0F, -1.75F, 3.5F, 1.5F, 3.5F).texOffs(239, 0).addBox(-1.0F, 33.0F, -1.0F, 2.0F, 4.0F, 2.0F).texOffs(0, 34).addBox(-1.25F, 37.0F, -1.25F, 2.5F, 2.0F, 2.5F), PartPose.offset(0.0F, 22.0F, 0.0F));
        PartDefinition waist = hips.addOrReplaceChild("waist", CubeListBuilder.create().texOffs(13, 34).addBox(-5.0F, -11.0F, -3.0F, 10.0F, 11.0F, 6.0F).texOffs(46, 34).addBox(-5.5F, -11.0F, 1.0F, 11.0F, 12.0F, 2.5F).texOffs(75, 34).addBox(-5.75F, -11.0F, -3.25F, 1.75F, 12.0F, 4.5F).texOffs(90, 34).addBox(4.0F, -11.0F, -3.25F, 1.75F, 12.0F, 4.5F), PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition right_tail = waist.addOrReplaceChild("right_tail", CubeListBuilder.create().texOffs(105, 34).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 24.0F, 1.0F), PartPose.offsetAndRotation(-2.75F, 0.0F, 3.0F, 0.10471975511965978F, 0.0F, -0.06981317007977318F));
        PartDefinition left_tail = waist.addOrReplaceChild("left_tail", CubeListBuilder.create().texOffs(118, 34).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 24.0F, 1.0F), PartPose.offsetAndRotation(2.75F, 0.0F, 3.0F, 0.10471975511965978F, 0.0F, 0.06981317007977318F));
        PartDefinition chest = waist.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(131, 34).addBox(-7.0F, -12.0F, -3.5F, 14.0F, 12.0F, 7.0F).texOffs(174, 34).addBox(-3.0F, -11.0F, -4.0F, 6.0F, 11.0F, 1.0F), PartPose.offset(0.0F, -11.0F, 0.0F));
        PartDefinition right_lapel = chest.addOrReplaceChild("right_lapel", CubeListBuilder.create().texOffs(189, 34).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, -11.0F, -4.0F, 0.0F, 0.0F, 0.17453292519943295F));
        PartDefinition right_cord = chest.addOrReplaceChild("right_cord", CubeListBuilder.create().texOffs(196, 34).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 20.0F, 0.5F), PartPose.offset(-4.0F, -10.0F, 3.5F));
        PartDefinition left_lapel = chest.addOrReplaceChild("left_lapel", CubeListBuilder.create().texOffs(201, 34).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(3.5F, -11.0F, -4.0F, 0.0F, 0.0F, -0.17453292519943295F));
        PartDefinition left_cord = chest.addOrReplaceChild("left_cord", CubeListBuilder.create().texOffs(208, 34).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 20.0F, 0.5F), PartPose.offset(4.0F, -10.0F, 3.5F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(213, 34).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 60).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(33, 60).addBox(-6.5F, -1.0F, -6.5F, 13.0F, 1.0F, 13.0F).texOffs(86, 60).addBox(-4.5F, -15.0F, -4.5F, 9.0F, 14.0F, 9.0F).texOffs(123, 60).addBox(-4.75F, -4.0F, -4.75F, 9.5F, 2.5F, 9.5F), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.08726646259971647F, 0.0F, 0.06981317007977318F));
        PartDefinition plume = hat.addOrReplaceChild("plume", CubeListBuilder.create().texOffs(164, 60).addBox(-0.5F, -8.0F, -1.0F, 1.0F, 8.0F, 4.0F), PartPose.offsetAndRotation(2.5F, -13.0F, 2.0F, -0.3839724354387525F, 0.0F, 0.17453292519943295F));
        PartDefinition right_upper = chest.addOrReplaceChild("right_upper", CubeListBuilder.create().texOffs(175, 60).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 3.0F, 5.0F).texOffs(196, 60).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 16.0F, 3.5F), PartPose.offset(-8.5F, -10.0F, 0.0F));
        PartDefinition right_fore = right_upper.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(213, 60).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 13.0F, 3.0F).texOffs(226, 60).addBox(-1.75F, 11.0F, -1.75F, 3.5F, 3.0F, 3.5F), PartPose.offset(0.0F, 16.0F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(243, 60).addBox(-0.75F, 0.0F, -1.75F, 1.5F, 4.0F, 3.5F), PartPose.offset(0.0F, 14.0F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(0, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 3.0F, 0.75F), PartPose.offset(0.0F, 4.0F, -1.25F));
        PartDefinition right_finger_0_tip = right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create().texOffs(5, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(10, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 3.0F, 0.75F), PartPose.offset(0.0F, 4.0F, -0.5F));
        PartDefinition right_finger_1_tip = right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create().texOffs(15, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(20, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 3.0F, 0.75F), PartPose.offset(0.0F, 4.0F, 0.25F));
        PartDefinition right_finger_2_tip = right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create().texOffs(25, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create().texOffs(30, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 3.0F, 0.75F), PartPose.offset(0.0F, 4.0F, 1.0F));
        PartDefinition right_finger_3_tip = right_finger_3.addOrReplaceChild("right_finger_3_tip", CubeListBuilder.create().texOffs(35, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(40, 84).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offsetAndRotation(0.0F, 1.0F, -1.75F, -0.4363323129985824F, 0.0F, 0.3490658503988659F));
        PartDefinition right_thumb_tip = right_thumb.addOrReplaceChild("right_thumb_tip", CubeListBuilder.create().texOffs(45, 84).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.75F, 1.0F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition left_upper = chest.addOrReplaceChild("left_upper", CubeListBuilder.create().texOffs(50, 84).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 3.0F, 5.0F).texOffs(71, 84).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 16.0F, 3.5F), PartPose.offset(8.5F, -10.0F, 0.0F));
        PartDefinition left_fore = left_upper.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(88, 84).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 13.0F, 3.0F).texOffs(101, 84).addBox(-1.75F, 11.0F, -1.75F, 3.5F, 3.0F, 3.5F), PartPose.offset(0.0F, 16.0F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(118, 84).addBox(-0.75F, 0.0F, -1.75F, 1.5F, 4.0F, 3.5F), PartPose.offset(0.0F, 14.0F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(131, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 3.0F, 0.75F), PartPose.offset(0.0F, 4.0F, -1.25F));
        PartDefinition left_finger_0_tip = left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create().texOffs(136, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(141, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 3.0F, 0.75F), PartPose.offset(0.0F, 4.0F, -0.5F));
        PartDefinition left_finger_1_tip = left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create().texOffs(146, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(151, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 3.0F, 0.75F), PartPose.offset(0.0F, 4.0F, 0.25F));
        PartDefinition left_finger_2_tip = left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create().texOffs(156, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create().texOffs(161, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 3.0F, 0.75F), PartPose.offset(0.0F, 4.0F, 1.0F));
        PartDefinition left_finger_3_tip = left_finger_3.addOrReplaceChild("left_finger_3_tip", CubeListBuilder.create().texOffs(166, 84).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(171, 84).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.5F, 1.0F), PartPose.offsetAndRotation(0.0F, 1.0F, -1.75F, -0.4363323129985824F, 0.0F, -0.3490658503988659F));
        PartDefinition left_thumb_tip = left_thumb.addOrReplaceChild("left_thumb_tip", CubeListBuilder.create().texOffs(176, 84).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.75F, 1.0F), PartPose.offset(0.0F, 2.5F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(CircusStiltWalkerEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        at = entity.getPosition(partialTick);
        bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        pose(age, yaw, pitch, entity.stride(partialTick), entity.gesture(), entity.gestureAge(partialTick),
                entity.gestureAt(), entity.gestureAim());
    }

    /** The whole pose: the walk from {@code stride}, then gesture {@code gesture} (-1 at rest) {@code since} ticks in. */
    public void pose(float age, float yaw, float pitch, float stride, int gesture, float since, Vec3 target, Vec3 aim) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        ghostLegs[0] = ghostLegs[1] = ghostSpin = ghostShadow = 0.0F;
        float weight = gesture >= 0 ? gestureWeight(gesture, since) : 0.0F;
        walk(age, stride, 1.0F - weight);
        neck.yRot += Mth.clamp(yaw, -75, 75) * Mth.DEG_TO_RAD * .3F;
        head.yRot += Mth.clamp(yaw, -75, 75) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -15, 55) * Mth.DEG_TO_RAD * .8F;
        neck.xRot += Mth.clamp(pitch, -15, 55) * Mth.DEG_TO_RAD * .2F;
        if (gesture >= 0 && weight > 0.0F) gesture(gesture, since, weight, target, aim, age);
    }

    /** How strongly a gesture holds the body: in over six ticks, out over a heavy recovery after the move ends. */
    private static float gestureWeight(int gesture, float since) {
        float end = switch (gesture) {
            case CircusStiltWalkerEntity.GESTURE_PLANT -> StiltWalkerMoves.PLANT_WINDUP + StiltWalkerMoves.PLANT_HOLD;
            case CircusStiltWalkerEntity.GESTURE_STRIDE -> StiltWalkerMoves.strideLands(StiltWalkerMoves.STRIDE_STEPS - 1) + 6;
            case CircusStiltWalkerEntity.GESTURE_SPIN -> StiltWalkerMoves.SPIN_WINDUP + StiltWalkerMoves.SPIN_TICKS;
            default -> StiltWalkerMoves.SHADOW_WINDUP + StiltWalkerMoves.SHADOW_REST;
        };
        return HemoEasing.smoothstep(0.0F, 6.0F, since) * (1.0F - HemoEasing.smoothstep(end, end + 18.0F, since));
    }

    /**
     * One stride: the swinging stilt lifted from behind, its knee bending, and planted in front as the body passes over
     * the other; the hips dip where both are splayed, the coat tails and arms swing against it.
     */
    private void walk(float age, float stride, float amount) {
        int step = Mth.floor(stride);
        float e = HemoEasing.smoothstep(stride - step);
        float lift = Mth.sin(e * Mth.PI);
        int swing = Math.floorMod(step, 2);
        float reach = .28F * amount;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            if (i == swing) {
                thighs[i].xRot = Mth.lerp(e, reach, -reach) - .3F * lift * amount;
                shins[i].xRot = .55F * lift * amount;
            } else {
                thighs[i].xRot = Mth.lerp(e, -reach, reach);
            }
            thighs[i].zRot = sign * .03F;
            uppers[i].xRot = -thighs[i].xRot * .6F;
            uppers[i].zRot = -sign * (.12F + .03F * Mth.sin(age * .05F + i));
            fores[i].xRot = -.25F;
            hands[i].yRot = sign * 1.2F;
            curl(i, .45F);
            tails[i].xRot += .12F * lift * amount + .04F * Mth.sin(age * .07F + i * 1.7F);
            tails[i].zRot += .05F * Mth.sin(age * .05F + i);
        }
        hips.y += (1.0F - lift) * 2.4F * amount;
        float toward = swing == 0 ? 1 : -1;
        hips.zRot += toward * .035F * lift * amount;
        chest.zRot -= toward * .05F * lift * amount;
        chest.xRot += .05F + .02F * Mth.sin(age * .05F);
        head.zRot += toward * .02F * lift * amount;
        for (ModelPart cord : cords) cord.zRot = Mth.sin(age * .06F + cord.x) * .08F;
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

    /** This frame's spectral limbs: each ghost leg's weight and the point its tip stands on (model pixels). */
    private final float[] ghostLegs = new float[2];
    private final Vec3[] legTips = {Vec3.ZERO, Vec3.ZERO};
    /** The spun stilts: their weight, the ring's centre and the direction they lie along (model space). */
    private float ghostSpin;
    private Vec3 spinCentre = Vec3.ZERO, spinDir = new Vec3(1.0D, 0.0D, 0.0D);
    /** The long shadow: its weight, where it stands (model pixels), the way it falls (model space), how far it has tipped. */
    private float ghostShadow, shadowTip;
    private Vec3 shadowFoot = Vec3.ZERO, shadowDir = new Vec3(0.0D, 0.0D, -1.0D);

    /** 0 until {@code length} ticks before {@code at}, then 1 at {@code at}, accelerating like a falling weight. */
    private static float drop(float since, float at, float length) {
        float t = Mth.clamp((since - (at - length)) / length, 0.0F, 1.0F);
        return t * t;
    }

    private void gesture(int gesture, float since, float weight, Vec3 target, Vec3 aim, float age) {
        switch (gesture) {
            case CircusStiltWalkerEntity.GESTURE_PLANT -> {
                // Their right leg hauled up high toward the ring, trembling at the top, then driven down like a spear;
                // its spectral copy comes down out of the dark and stands in the floor where it struck.
                float w = StiltWalkerMoves.PLANT_WINDUP;
                float gather = HemoEasing.smoothstep(0.0F, w - 4.0F, since), down = drop(since, w, 2.5F);
                float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 6.0F, w - 3.0F, since) * (1.0F - down);
                thighs[0].xRot = Mth.lerp(weight, thighs[0].xRot, Mth.lerp(down, -1.55F + .05F * tremble, -.7F));
                shins[0].xRot = Mth.lerp(weight, shins[0].xRot, Mth.lerp(down, .9F * gather, 0.0F));
                thighs[1].xRot = Mth.lerp(weight, thighs[1].xRot, .12F * gather);
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, -sign * Mth.lerp(down, 1.25F, .7F));
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, -.2F);
                    curl(i, Mth.lerp(weight, .45F, .1F));
                }
                chest.xRot += weight * (-.18F * gather * (1.0F - down) + .3F * down);
                hips.xRot += weight * .12F * down;
                head.xRot += weight * .15F * down;
                ghostLeg(0, target.add(0.0D, Mth.lerp(down, 7.0D + .2D * tremble, 0.0D) * (1.0F - gather * .25F), 0.0D), weight);
            }
            case CircusStiltWalkerEntity.GESTURE_STRIDE -> {
                // Their body rocks with each footfall as the two spectral legs walk the chord, one planted while the
                // other swings over to the next footfall.
                for (int leg = 0; leg < 2; leg++) {
                    Vec3 tip = strideTip(leg, since, target, aim);
                    if (tip != null) ghostLeg(leg, tip, weight);
                }
                float beat = 0.0F;
                for (int k = 0; k < StiltWalkerMoves.STRIDE_STEPS; k++)
                    beat = Math.max(beat, 1.0F - Mth.clamp(Math.abs(since - StiltWalkerMoves.strideLands(k)) / 4.0F, 0.0F, 1.0F));
                int next = Mth.clamp((int) ((since - StiltWalkerMoves.STRIDE_WINDUP) / StiltWalkerMoves.STRIDE_GAP) + 1, 0,
                        StiltWalkerMoves.STRIDE_STEPS - 1);
                int lifting = next % 2;
                thighs[lifting].xRot = Mth.lerp(weight, thighs[lifting].xRot, -.75F * (1.0F - beat));
                shins[lifting].xRot = Mth.lerp(weight, shins[lifting].xRot, .7F * (1.0F - beat));
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, -sign * .6F);
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, (i == lifting ? .5F : -.5F));
                }
                chest.xRot += weight * (.2F + .15F * beat);
                hips.y += weight * 1.5F * beat;
            }
            case CircusStiltWalkerEntity.GESTURE_SPIN -> {
                // Up onto the right stilt, the left swung out level, arms wide; wound back through the gather, then a
                // whole pirouette while the spectral stilts in the ring turn half a circle.
                float w = StiltWalkerMoves.SPIN_WINDUP;
                float gather = HemoEasing.smoothstep(0.0F, w - 2.0F, since);
                float turning = HemoEasing.smoothstep(HemoEasing.clamp01((since - w) / StiltWalkerMoves.SPIN_TICKS));
                thighs[0].xRot = Mth.lerp(weight, thighs[0].xRot, 0.0F);
                shins[0].xRot = Mth.lerp(weight, shins[0].xRot, 0.0F);
                thighs[1].xRot = Mth.lerp(weight, thighs[1].xRot, 0.0F);
                thighs[1].zRot = Mth.lerp(weight, thighs[1].zRot, -1.45F * gather);
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, -sign * 1.5F);
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, 0.0F);
                    fores[i].xRot = Mth.lerp(weight, fores[i].xRot, 0.0F);
                    curl(i, Mth.lerp(weight, .45F, .05F));
                    tails[i].zRot += sign * .9F * turning * (1.0F - turning) * 4.0F * weight;
                }
                root.x += 3.5F * weight;
                root.yRot += weight * (.7F * gather * (1.0F - turning) - Mth.TWO_PI * turning);
                hips.y -= 1.5F * gather * weight;
                ghostSpin = weight * Math.max(gather, 1.0F - HemoEasing.smoothstep(w + StiltWalkerMoves.SPIN_TICKS,
                        w + StiltWalkerMoves.SPIN_TICKS + 10.0F, since));
                double angle = StiltWalkerMoves.spinAngle(Math.atan2(aim.z, aim.x), Math.max(0.0F, since - w));
                spinCentre = model(target.add(0.0D, .5D, 0.0D));
                spinDir = model(target.add(Math.cos(angle), .5D, Math.sin(angle))).subtract(spinCentre).normalize();
            }
            case CircusStiltWalkerEntity.GESTURE_FALL -> {
                // They lean out over the ring and push their own shadow over: it stands up at the rim, sways, and
                // falls across the ring like a felled pole.
                float w = StiltWalkerMoves.SHADOW_WINDUP;
                float lean = HemoEasing.smoothstep(0.0F, w, since);
                float shove = drop(since, w, 4.0F);
                chest.xRot += weight * (.35F * lean + .2F * shove);
                hips.xRot += weight * .1F * lean;
                for (int i = 0; i < 2; i++) {
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(shove, -1.3F * lean, -1.75F));
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, (i == 0 ? 1 : -1) * .12F);
                    fores[i].xRot = Mth.lerp(weight, fores[i].xRot, -.15F);
                    curl(i, Mth.lerp(weight, .45F, .05F));
                }
                ghostShadow = weight * HemoEasing.smoothstep(0.0F, 8.0F, since);
                shadowTip = StiltWalkerMoves.shadowFall(since) + (since < w ? Mth.sin(since * .4F) * .04F * (1.0F - lean) : 0.0F);
                shadowFoot = model(target);
                shadowDir = model(target.add(aim)).subtract(shadowFoot).multiply(1.0D, 0.0D, 1.0D).normalize();
            }
            default -> {
            }
        }
    }

    /**
     * Where spectral leg {@code leg} stands on the stride: the leg for footfall k is k % 2; it comes down out of the dark
     * onto its first footfall, then swings over to its next one while the other leg is planted.
     */
    private static Vec3 strideTip(int leg, float since, Vec3 start, Vec3 chord) {
        Vec3 planted = null;
        for (int k = leg; k < StiltWalkerMoves.STRIDE_STEPS; k += 2) {
            float lands = StiltWalkerMoves.strideLands(k);
            Vec3 step = StiltWalkerMoves.strideStep(start, chord, k);
            float from = k < 2 ? 0.0F : StiltWalkerMoves.strideLands(k - 1);
            if (since >= lands) {
                planted = step;
                continue;
            }
            if (since < from) return planted;
            float t = Mth.clamp((since - from) / Math.max(1.0F, lands - from), 0.0F, 1.0F);
            float fall = t * t;
            if (planted == null) return step.add(0.0D, 8.0D * (1.0F - fall), 0.0D);
            return planted.lerp(step, HemoEasing.smoothstep(t)).add(0.0D, 4.0D * Mth.sin(t * Mth.PI), 0.0D);
        }
        return planted;
    }

    private void ghostLeg(int i, Vec3 world, float weight) {
        ghostLegs[i] = weight;
        legTips[i] = model(world);
    }

    /** The spectral leg against their own, and its length then, in pixels; the spun stilts' and the shadow's sizes. */
    private static final float GHOST_LEG = 1.35F, GHOST_LEG_LENGTH = LEG * GHOST_LEG;
    private static final float GHOST_STILT = (float) (StiltWalkerMoves.RIM * 16.0D / OrdealStiltWalk.SCALE / SHIN);
    private static final float GHOST_SHADOW = (float) ((StiltWalkerMoves.SHADOW_LENGTH + .5D) * 16.0D / OrdealStiltWalk.SCALE / HEIGHT);

    /**
     * Draws the spectral limbs in {@code legTint}: copies of their leg reaching into the ring from above and from their
     * side, the tip standing on this frame's target; the two stilts spun across the ring; and, in {@code shadowTint},
     * their whole shadow standing at the rim and falling. Their real body stays where it is.
     */
    public void renderGhosts(PoseStack pose, VertexConsumer consumer, int light, int overlay, int legTint, int shadowTint) {
        for (int i = 0; i < 2; i++) {
            if (ghostLegs[i] <= .01F) continue;
            Vec3 tip = legTips[i];
            Vec3 toward = new Vec3(-tip.x, 0.0D, -tip.z);
            toward = toward.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : toward.normalize();
            // From above and from their side of the ring: in model space up is -y.
            Vec3 dir = toward.scale(-.45D).add(0.0D, .89D, 0.0D).normalize();
            Vec3 origin = tip.subtract(dir.scale(GHOST_LEG_LENGTH));
            PartPose thigh = thighs[i].storePose(), shin = shins[i].storePose();
            thighs[i].setPos((float) origin.x, (float) origin.y, (float) origin.z);
            aim(thighs[i], dir);
            thighs[i].xScale = thighs[i].yScale = thighs[i].zScale = GHOST_LEG;
            shins[i].setRotation(0.0F, 0.0F, 0.0F);
            thighs[i].render(pose, consumer, light, overlay, tint(legTint, ghostLegs[i]));
            thighs[i].loadPose(thigh);
            shins[i].loadPose(shin);
            thighs[i].xScale = thighs[i].yScale = thighs[i].zScale = 1.0F;
        }
        if (ghostSpin > .01F) {
            // One stilt out each way from the centre, knee to knee: together the ring's whole width.
            for (int i = 0; i < 2; i++) {
                PartPose shin = shins[i].storePose();
                shins[i].setPos((float) spinCentre.x, (float) spinCentre.y, (float) spinCentre.z);
                aim(shins[i], spinDir.scale(i == 0 ? -1.0D : 1.0D));
                shins[i].xScale = shins[i].zScale = GHOST_STILT * .9F;
                shins[i].yScale = GHOST_STILT;
                shins[i].render(pose, consumer, light, overlay, tint(legTint, ghostSpin));
                shins[i].loadPose(shin);
                shins[i].xScale = shins[i].yScale = shins[i].zScale = 1.0F;
            }
        }
        if (ghostShadow > .01F) {
            PartPose body = root.storePose();
            root.setPos((float) shadowFoot.x, (float) shadowFoot.y, (float) shadowFoot.z);
            root.setRotation(shadowTip, (float) Math.atan2(-shadowDir.x, -shadowDir.z), 0.0F);
            root.xScale = root.yScale = root.zScale = GHOST_SHADOW;
            root.render(pose, consumer, light, overlay, tint(shadowTint, ghostShadow));
            root.loadPose(body);
            root.xScale = root.yScale = root.zScale = 1.0F;
        }
    }

    /** Turns a part so its own +y (down its length) runs along {@code dir}, in its parent's space. */
    private static void aim(ModelPart part, Vec3 dir) {
        part.setRotation((float) Math.asin(Mth.clamp(dir.z, -1.0D, 1.0D)), 0.0F, (float) Math.atan2(-dir.x, dir.y));
    }

    private static int tint(int color, float weight) {
        int alpha = (int) (Mth.clamp(weight, 0.0F, 1.0F) * ((color >>> 24) & 255));
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / OrdealStiltWalk.SCALE);
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
