package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.AcrobatOrdealHits;
import com.vincenthuto.hemomancy.common.encounter.ordeal.AcrobatRigging;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusAcrobatEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

// Authored by tools/circus/build_true_acrobat.py; regenerate the model, atlas and BBModel together.
/**
 * The Acrobat's true self: hung by the knees from a trapeze in the rigging over their ring, watching upside down, the
 * body swaying about the bar and the head following the fighter. Each intervention is a gesture
 * ({@link CircusAcrobatEntity#gesture}) timed from its wind-up: their own body makes the motion on the trapeze, and
 * their rigging, made of their blood, does it in the ring ({@link #renderGhosts}): a spectral trapeze hauled back and
 * let go through the fighter's spot, a double of themselves rolling in the dark and plummeting onto the mark, a snare
 * line paid out from their hand to a loop round the fighter and hauled, and a net of lines falling. When they let go
 * and fly across the ring they tumble, and the trapeze is left behind.
 */
public final class CircusTrueAcrobatModel extends EntityModel<CircusAcrobatEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_acrobat"), "main");
    private final ModelPart root, bar, hang, body, waist, chest, neck, head, ghostLine;
    private final ModelPart[] lines = new ModelPart[2], tails = new ModelPart[2], tailEnds = new ModelPart[2],
            uppers = new ModelPart[2], fores = new ModelPart[2], hands = new ModelPart[2], streamers = new ModelPart[2],
            thumbs = new ModelPart[2], thumbTips = new ModelPart[2], thighs = new ModelPart[2], shins = new ModelPart[2],
            feet = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    /** This frame's body: where it hangs and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;
    /** This frame's gesture, for the spectral rigging. */
    private int gesture = -1;
    private float since;
    private Vec3 target = Vec3.ZERO;

    public CircusTrueAcrobatModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        bar = root.getChild("bar");
        ghostLine = root.getChild("ghost_line");
        hang = root.getChild("hang");
        body = hang.getChild("body");
        waist = body.getChild("waist");
        chest = waist.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            lines[i] = root.getChild(side + "_line");
            tails[i] = head.getChild(side + "_tail");
            tailEnds[i] = tails[i].getChild(side + "_tail_end");
            uppers[i] = chest.getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            streamers[i] = fores[i].getChild(side + "_streamer");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            thumbTips[i] = thumbs[i].getChild(side + "_thumb_tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
            thighs[i] = body.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
        }
        ghostLine.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition bar = root.addOrReplaceChild("bar", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -0.75F, -0.75F, 20.0F, 1.5F, 1.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition right_line = root.addOrReplaceChild("right_line", CubeListBuilder.create().texOffs(45, 0).addBox(-0.5F, -96.0F, -0.5F, 1.0F, 96.0F, 1.0F), PartPose.offset(-9.25F, 0.0F, 0.0F));
        PartDefinition left_line = root.addOrReplaceChild("left_line", CubeListBuilder.create().texOffs(50, 0).addBox(-0.5F, -96.0F, -0.5F, 1.0F, 96.0F, 1.0F), PartPose.offset(9.25F, 0.0F, 0.0F));
        PartDefinition ghost_line = root.addOrReplaceChild("ghost_line", CubeListBuilder.create().texOffs(55, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 16.0F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition hang = root.addOrReplaceChild("hang", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = hang.addOrReplaceChild("body", CubeListBuilder.create().texOffs(60, 0).addBox(-3.5F, -1.0F, -2.0F, 7.0F, 4.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.0F, 0.0F, 3.141592653589793F));
        PartDefinition waist = body.addOrReplaceChild("waist", CubeListBuilder.create().texOffs(83, 0).addBox(-3.0F, -6.0F, -1.75F, 6.0F, 6.0F, 3.5F), PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition chest = waist.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(104, 0).addBox(-4.0F, -7.0F, -2.25F, 8.0F, 7.0F, 4.5F).texOffs(131, 0).addBox(-2.5F, -6.5F, -2.75F, 5.0F, 5.5F, 0.5F).texOffs(144, 0).addBox(-1.5F, -5.25F, -3.25F, 3.0F, 3.0F, 0.5F), PartPose.offset(0.0F, -6.0F, 0.0F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(153, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(0.0F, -7.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(162, 0).addBox(-3.5F, -7.0F, -3.0F, 7.0F, 7.0F, 6.0F).texOffs(189, 0).addBox(-3.75F, -6.25F, -3.25F, 7.5F, 1.0F, 6.5F), PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition right_tail = head.addOrReplaceChild("right_tail", CubeListBuilder.create().texOffs(220, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(-1.75F, -5.5F, 3.0F, 0.4363323129985824F, -0.2617993877991494F, 0.0F));
        PartDefinition right_tail_end = right_tail.addOrReplaceChild("right_tail_end", CubeListBuilder.create().texOffs(233, 0).addBox(-0.75F, -0.75F, 0.0F, 1.5F, 1.5F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 5.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_upper = chest.addOrReplaceChild("right_upper", CubeListBuilder.create().texOffs(0, 98).addBox(-1.25F, -1.0F, -1.25F, 2.5F, 7.0F, 2.5F).texOffs(13, 98).addBox(-1.75F, -1.5F, -1.75F, 3.5F, 2.0F, 3.5F), PartPose.offset(-4.75F, -6.0F, 0.0F));
        PartDefinition right_fore = right_upper.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(30, 98).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F).texOffs(43, 98).addBox(-1.5F, 4.0F, -1.5F, 3.0F, 1.5F, 3.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition right_streamer = right_fore.addOrReplaceChild("right_streamer", CubeListBuilder.create().texOffs(56, 98).addBox(-0.25F, 0.0F, 0.0F, 0.5F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 1.25F, 0.20943951023931956F, 0.0F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(61, 98).addBox(-1.0F, 0.0F, -1.25F, 2.0F, 2.0F, 2.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(72, 98).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, 2.0F, -0.75F));
        PartDefinition right_finger_0_tip = right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create().texOffs(77, 98).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offset(0.0F, 1.25F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(82, 98).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, 2.0F, -0.25F));
        PartDefinition right_finger_1_tip = right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create().texOffs(87, 98).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offset(0.0F, 1.25F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(92, 98).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.25F));
        PartDefinition right_finger_2_tip = right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create().texOffs(97, 98).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offset(0.0F, 1.25F, 0.0F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create().texOffs(102, 98).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.75F));
        PartDefinition right_finger_3_tip = right_finger_3.addOrReplaceChild("right_finger_3_tip", CubeListBuilder.create().texOffs(107, 98).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offset(0.0F, 1.25F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(112, 98).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 0.5F, -1.25F, -0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_thumb_tip = right_thumb.addOrReplaceChild("right_thumb_tip", CubeListBuilder.create().texOffs(117, 98).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 0.75F, 0.5F), PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition right_thigh = body.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(122, 98).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(-1.75F, 3.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(135, 98).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 7.0F, 2.5F), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 2.181661564992912F, 0.0F, 0.0F));
        PartDefinition right_foot = right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(148, 98).addBox(-1.0F, 0.0F, -2.25F, 2.0F, 1.5F, 3.25F), PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition right_coat = body.addOrReplaceChild("right_coat", CubeListBuilder.create().texOffs(161, 98).addBox(-1.25F, 0.0F, -0.25F, 2.5F, 5.0F, 0.5F), PartPose.offsetAndRotation(-1.5F, 2.0F, 2.0F, 2.6179938779914944F, 0.0F, 0.0F));
        PartDefinition left_tail = head.addOrReplaceChild("left_tail", CubeListBuilder.create().texOffs(170, 98).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 5.0F), PartPose.offsetAndRotation(1.75F, -5.5F, 3.0F, 0.4363323129985824F, 0.2617993877991494F, 0.0F));
        PartDefinition left_tail_end = left_tail.addOrReplaceChild("left_tail_end", CubeListBuilder.create().texOffs(183, 98).addBox(-0.75F, -0.75F, 0.0F, 1.5F, 1.5F, 4.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 5.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition left_upper = chest.addOrReplaceChild("left_upper", CubeListBuilder.create().texOffs(196, 98).addBox(-1.25F, -1.0F, -1.25F, 2.5F, 7.0F, 2.5F).texOffs(209, 98).addBox(-1.75F, -1.5F, -1.75F, 3.5F, 2.0F, 3.5F), PartPose.offset(4.75F, -6.0F, 0.0F));
        PartDefinition left_fore = left_upper.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(226, 98).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F).texOffs(239, 98).addBox(-1.5F, 4.0F, -1.5F, 3.0F, 1.5F, 3.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition left_streamer = left_fore.addOrReplaceChild("left_streamer", CubeListBuilder.create().texOffs(252, 98).addBox(-0.25F, 0.0F, 0.0F, 0.5F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 1.25F, 0.20943951023931956F, 0.0F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(0, 110).addBox(-1.0F, 0.0F, -1.25F, 2.0F, 2.0F, 2.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(11, 110).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, 2.0F, -0.75F));
        PartDefinition left_finger_0_tip = left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create().texOffs(16, 110).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offset(0.0F, 1.25F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(21, 110).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, 2.0F, -0.25F));
        PartDefinition left_finger_1_tip = left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create().texOffs(26, 110).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offset(0.0F, 1.25F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(31, 110).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.25F));
        PartDefinition left_finger_2_tip = left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create().texOffs(36, 110).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offset(0.0F, 1.25F, 0.0F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create().texOffs(41, 110).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.75F));
        PartDefinition left_finger_3_tip = left_finger_3.addOrReplaceChild("left_finger_3_tip", CubeListBuilder.create().texOffs(46, 110).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offset(0.0F, 1.25F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(51, 110).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 0.5F, -1.25F, -0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_thumb_tip = left_thumb.addOrReplaceChild("left_thumb_tip", CubeListBuilder.create().texOffs(56, 110).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 0.75F, 0.5F), PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition left_thigh = body.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(61, 110).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F), PartPose.offset(1.75F, 3.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(74, 110).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 7.0F, 2.5F), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, 2.181661564992912F, 0.0F, 0.0F));
        PartDefinition left_foot = left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(87, 110).addBox(-1.0F, 0.0F, -2.25F, 2.0F, 1.5F, 3.25F), PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition left_coat = body.addOrReplaceChild("left_coat", CubeListBuilder.create().texOffs(100, 110).addBox(-1.25F, 0.0F, -0.25F, 2.5F, 5.0F, 0.5F), PartPose.offsetAndRotation(1.5F, 2.0F, 2.0F, 2.6179938779914944F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(CircusAcrobatEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        at = entity.getPosition(partialTick);
        bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        // Flying across the ring (let go of one trapeze, caught by the far one) reads from the body's own speed.
        double speed = Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo);
        float flying = HemoEasing.smoothstep(.3F, .5F, (float) speed);
        pose(age, yaw, pitch, flying, entity.gesture(), entity.gestureAge(partialTick), entity.gestureAt());
    }

    /** The whole pose; {@code gesture} is -1 at rest, {@code since} the ticks since its wind-up began. */
    public void pose(float age, float yaw, float pitch, float flying, int gesture, float since, Vec3 target) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        ghostLine.visible = false;
        bar.visible = lines[0].visible = lines[1].visible = true;
        this.gesture = gesture;
        this.since = since;
        this.target = target;
        float breath = Mth.sin(age * .06F);
        // Hanging easy: a slow sway about the bar, the chest breathing, the arms loose toward the floor.
        hang.xRot += Mth.sin(age * .09F) * .06F;
        hang.zRot += Mth.sin(age * .057F) * .03F;
        chest.xRot += breath * .03F;
        // Upside down the head's turn and tip run the other way.
        head.yRot -= Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .75F;
        neck.yRot -= Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .25F;
        head.xRot -= Mth.clamp(pitch, -60, 60) * Mth.DEG_TO_RAD * .8F;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            uppers[i].xRot = Mth.PI + Mth.sin(age * .07F + i) * .05F;
            uppers[i].zRot = sign * .2F;
            fores[i].xRot = -.25F;
            hands[i].yRot = sign * .3F;
            curl(i, .35F + .15F * Mth.sin(age * .05F + i * 2));
            thighs[i].zRot = -sign * .08F;
            feet[i].xRot = .35F;
            tails[i].xRot += Mth.sin(age * .2F + i) * .12F;
            tailEnds[i].xRot += Mth.sin(age * .2F + i + .8F) * .15F;
            streamers[i].xRot += Mth.sin(age * .17F + i * 1.7F) * .1F;
        }
        if (gesture >= 0) gesture(gesture, since, target);
        if (flying > 0.0F) fly(age, flying);
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = -sign * amount * (1.0F + f * .05F);
            tips[i][f].zRot = -sign * amount * 1.2F;
        }
        thumbs[i].zRot = sign * amount * .5F;
        thumbTips[i].zRot = -sign * amount * .4F;
    }

    /** Let go and flying across: tucked and tumbling about the hips, the trapeze left hanging behind. */
    private void fly(float age, float flying) {
        hang.xRot = Mth.lerp(flying, hang.xRot, age * .55F);
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            thighs[i].xRot = Mth.lerp(flying, thighs[i].xRot, -1.9F);
            shins[i].xRot = Mth.lerp(flying, shins[i].xRot, 2.3F);
            uppers[i].xRot = Mth.lerp(flying, uppers[i].xRot, -1.2F);
            uppers[i].zRot = Mth.lerp(flying, uppers[i].zRot, sign * .3F);
            fores[i].xRot = Mth.lerp(flying, fores[i].xRot, -1.6F);
            curl(i, Mth.lerp(flying, .4F, 1.2F));
        }
        boolean held = flying < .5F;
        bar.visible = held;
        lines[0].visible = lines[1].visible = held;
    }

    /** The wind-up of each move, in ticks; its blow lands on it. */
    private static float windup(int gesture) {
        return switch (gesture) {
            case CircusAcrobatEntity.GESTURE_SWING -> AcrobatOrdealHits.SWING_WINDUP;
            case CircusAcrobatEntity.GESTURE_DROP -> AcrobatOrdealHits.DROP_WINDUP;
            case CircusAcrobatEntity.GESTURE_SNARE -> AcrobatOrdealHits.SNARE_WINDUP;
            default -> AcrobatOrdealHits.NET_WINDUP;
        };
    }

    /** 0 until two and a half ticks before {@code at}, then 1 at {@code at}, accelerating. */
    private static float drop(float since, float at) {
        float t = Mth.clamp((since - (at - 2.5F)) / 2.5F, 0.0F, 1.0F);
        return t * t;
    }

    /** 1 through the gather and the blow, falling to 0 over a heavy recovery after a hit-stop. */
    private static float held(float since, float at, float hold) {
        return 1.0F - HemoEasing.smoothstep(at + hold, at + hold + 16.0F, since);
    }

    private void gesture(int gesture, float since, Vec3 target) {
        float w = windup(gesture);
        float gather = HemoEasing.smoothstep(0.0F, w - 6.0F, since);
        float tension = HemoEasing.smoothstep(w - 9.0F, w - 6.0F, since) * (1.0F - HemoEasing.smoothstep(w - 2.5F, w, since));
        float tremble = Mth.sin(since * 3.3F) * tension;
        float down = drop(since, w);
        switch (gesture) {
            case CircusAcrobatEntity.GESTURE_SWING -> {
                // Pumping the trapeze higher and higher, arms hauled back, then both thrown out over the ring.
                float weight = HemoEasing.smoothstep(0.0F, 5.0F, since) * held(since, w + AcrobatOrdealHits.SWING_TICKS, 4.0F);
                hang.xRot += weight * (Mth.sin(since * .38F) * .45F * gather * (1.0F - down) + .35F * down);
                chest.xRot += weight * (-.35F * gather * (1.0F - down) + .45F * down);
                for (int i = 0; i < 2; i++) {
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(down, Mth.PI - .8F + .05F * tremble, Mth.PI + 1.35F));
                    fores[i].xRot = Mth.lerp(weight, fores[i].xRot, Mth.lerp(down, -.9F, -.1F));
                    curl(i, Mth.lerp(weight, .4F, Mth.lerp(down, 1.3F, .1F)));
                }
            }
            case CircusAcrobatEntity.GESTURE_DROP -> {
                // Curled up to the bar, trembling, then snapped straight down: the dive their double makes.
                float weight = HemoEasing.smoothstep(0.0F, 5.0F, since) * held(since, w, 6.0F);
                float curlUp = gather * (1.0F - down);
                waist.xRot += weight * (1.1F * curlUp - .2F * down);
                chest.xRot += weight * (.7F * curlUp + .05F * tremble);
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(down, Mth.PI - 2.2F * curlUp, Mth.PI + .15F));
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, sign * Mth.lerp(down, .15F, .9F));
                    curl(i, Mth.lerp(weight, .4F, 1.2F * curlUp));
                }
            }
            case CircusAcrobatEntity.GESTURE_SNARE -> {
                // The right arm wound back and cast overhand, the line paying out; on the blow both hands haul.
                float weight = HemoEasing.smoothstep(0.0F, 4.0F, since) * held(since, w + 8.0F, 2.0F);
                float cast = HemoEasing.smoothstep(w - 9.0F, w - 6.0F, since);
                float haul = HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since);
                float jerk = Math.max(0.0F, Mth.sin((since - w) * .9F)) * haul;
                uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(cast, Mth.PI - 1.4F + .05F * tremble, Mth.PI + 1.2F));
                uppers[0].xRot = Mth.lerp(haul, uppers[0].xRot, Mth.PI - .9F - .3F * jerk);
                uppers[1].xRot = Mth.lerp(haul, uppers[1].xRot, Mth.PI - .7F - .3F * jerk);
                fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(cast, -1.2F, -.1F));
                for (int i = 0; i < 2; i++) {
                    fores[i].xRot = Mth.lerp(haul, fores[i].xRot, -1.5F);
                    curl(i, Mth.lerp(weight, .4F, .4F + .9F * Math.max(cast * (i == 0 ? 1 : 0), haul)));
                }
                chest.xRot += weight * (-.2F * (1.0F - cast) + .25F * cast - .35F * haul);
                hang.xRot += -.2F * jerk;
            }
            case CircusAcrobatEntity.GESTURE_NET -> {
                // Both arms spread wide, the net held between them, then flung down over the ring.
                float weight = HemoEasing.smoothstep(0.0F, 6.0F, since) * held(since, w, 8.0F);
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, sign * Mth.lerp(down, 1.3F + .05F * tremble, .25F));
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(down, Mth.PI - .4F, Mth.PI + 1.0F));
                    fores[i].xRot = Mth.lerp(weight, fores[i].xRot, Mth.lerp(down, -.6F, 0.0F));
                    curl(i, Mth.lerp(weight, .4F, Mth.lerp(down, 1.1F, .05F)));
                }
                chest.xRot += weight * (-.3F * gather * (1.0F - down) + .4F * down);
                hang.xRot += weight * .25F * down;
            }
            default -> {
            }
        }
    }

    /**
     * Draws the Acrobat's rigging in the ring for this frame's gesture: lines and bars of their blood, and their double.
     * Faded in with the gesture and out after its blow.
     */
    public void renderGhosts(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        if (gesture < 0) return;
        float w = windup(gesture);
        switch (gesture) {
            case CircusAcrobatEntity.GESTURE_SWING -> swingGhost(pose, consumer, light, overlay, color, w);
            case CircusAcrobatEntity.GESTURE_DROP -> dropGhost(pose, consumer, light, overlay, color, w);
            case CircusAcrobatEntity.GESTURE_SNARE -> snareGhost(pose, consumer, light, overlay, color, w);
            case CircusAcrobatEntity.GESTURE_NET -> netGhost(pose, consumer, light, overlay, color, w);
            default -> {
            }
        }
    }

    /** The spectral trapeze: hauled back over the lane's near end, trembling, then let go through the fighter's spot. */
    private void swingGhost(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float w) {
        float end = w + AcrobatOrdealHits.SWING_TICKS;
        float alpha = HemoEasing.smoothstep(0.0F, 6.0F, since) * (1.0F - HemoEasing.smoothstep(end + 2.0F, end + 10.0F, since));
        if (alpha <= .01F) return;
        Vec3 d = swingDirection();
        Vec3 across = new Vec3(-d.z, 0.0D, d.x);
        double along = since < w ? -AcrobatOrdealHits.SWING_HALF_LENGTH : AcrobatOrdealHits.swingAlong(since - w);
        float tremble = since < w ? Mth.sin(since * 3.3F) * HemoEasing.smoothstep(w - 9.0F, w - 3.0F, since) * .12F : 0.0F;
        Vec3 barAt = target.add(d.scale(along)).add(0.0D, AcrobatOrdealHits.barHeight(along) + (since < w ? .5D + tremble : 0.0D), 0.0D);
        trapeze(pose, consumer, light, overlay, color, alpha, barAt, across);
    }

    /** A trapeze at {@code barAt} across {@code across}: the bar and its two lines up into the dark. */
    private void trapeze(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float alpha, Vec3 barAt,
            Vec3 across) {
        Vec3 left = barAt.add(across.scale(1.15D)), right = barAt.subtract(across.scale(1.15D));
        Vec3 m = model(barAt), ml = model(left);
        Vec3 dir = ml.subtract(m);
        PartPose kept = bar.storePose();
        bar.setPos((float) m.x, (float) m.y, (float) m.z);
        bar.setRotation(0.0F, (float) Math.atan2(-dir.z, dir.x), 0.0F);
        bar.xScale = bar.yScale = bar.zScale = 1.4F;
        boolean shown = bar.visible;
        bar.visible = true;
        bar.render(pose, consumer, light, overlay, tint(color, alpha));
        bar.visible = shown;
        bar.loadPose(kept);
        bar.xScale = bar.yScale = bar.zScale = 1.0F;
        for (Vec3 foot : new Vec3[]{left, right}) line(pose, consumer, light, overlay, color, alpha, foot.add(0.0D, 14.0D, 0.0D), foot, 1.3F);
    }

    /** Their double, rolling slowly in the dark over the mark, then plummeting onto it head first. */
    private void dropGhost(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float w) {
        float alpha = HemoEasing.smoothstep(0.0F, 5.0F, since) * (1.0F - HemoEasing.smoothstep(w + 1.0F, w + 8.0F, since));
        if (alpha <= .01F) return;
        double height = since < w ? AcrobatOrdealHits.dropHeight(since) : 0.0D;
        float plummet = HemoEasing.smoothstep(w - 5.0F, w - 3.0F, since);
        Vec3 m = model(target.add(0.0D, height + .2D, 0.0D));
        PartPose[] kept = body.getAllParts().map(ModelPart::storePose).toArray(PartPose[]::new);
        // Tucked and turning while it hangs in the dark; stretched straight, head down, as it falls.
        float tuck = 1.0F - plummet;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            thighs[i].setRotation(-1.8F * tuck, 0.0F, -sign * .1F);
            shins[i].setRotation(2.2F * tuck, 0.0F, 0.0F);
            uppers[i].setRotation(Mth.lerp(plummet, -1.0F, Mth.PI), 0.0F, sign * Mth.lerp(plummet, .3F, .1F));
            fores[i].setRotation(Mth.lerp(plummet, -1.7F, 0.0F), 0.0F, 0.0F);
        }
        waist.setRotation(.5F * tuck, 0.0F, 0.0F);
        chest.setRotation(.4F * tuck, 0.0F, 0.0F);
        body.setPos((float) m.x, (float) m.y, (float) m.z);
        body.setRotation(Mth.lerp(plummet, since * .45F, 0.0F), 0.0F, Mth.lerp(plummet, 0.0F, Mth.PI));
        body.xScale = body.yScale = body.zScale = 1.05F;
        body.render(pose, consumer, light, overlay, tint(color, alpha));
        body.xScale = body.yScale = body.zScale = 1.0F;
        int k = 0;
        for (ModelPart part : (Iterable<ModelPart>) body.getAllParts()::iterator) part.loadPose(kept[k++]);
    }

    /** The snare: a line paid out from their hand down to a loop round the fighter's feet, then hauled taut. */
    private void snareGhost(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float w) {
        float alpha = HemoEasing.smoothstep(w - 9.0F, w - 6.0F, since) * (1.0F - HemoEasing.smoothstep(w + 8.0F, w + 12.0F, since));
        if (alpha <= .01F) return;
        Vec3 hand = handWorld(0);
        Vec3 loop = target.add(0.0D, .15D, 0.0D);
        // Paying out: the line's end runs from the hand to the loop through the cast.
        float out = HemoEasing.smoothstep(w - 8.0F, w - 2.0F, since);
        float haul = HemoEasing.smoothstep(w, w + 3.0F, since);
        Vec3 end = hand.lerp(loop, out).lerp(hand, .35D * haul).add(0.0D, 1.2D * haul, 0.0D);
        line(pose, consumer, light, overlay, color, alpha, hand, end, 1.0F + .5F * haul);
        // The loop itself, cinching on the blow.
        double radius = AcrobatOrdealHits.SNARE_RADIUS * (1.0D - .7D * haul);
        Vec3 centre = loop.lerp(end, haul);
        int segments = 12;
        for (int s = 0; s < segments; s++) {
            double a0 = Math.PI * 2.0D * s / segments, a1 = Math.PI * 2.0D * (s + 1) / segments;
            line(pose, consumer, light, overlay, color, alpha * out, centre.add(Math.cos(a0) * radius, 0.0D, Math.sin(a0) * radius),
                    centre.add(Math.cos(a1) * radius, 0.0D, Math.sin(a1) * radius), .9F);
        }
    }

    /** The net: a square of lines seven blocks across falling from high over the mark, slow and then fast. */
    private void netGhost(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float w) {
        float alpha = HemoEasing.smoothstep(0.0F, 6.0F, since) * (1.0F - HemoEasing.smoothstep(w + 6.0F, w + 24.0F, since));
        if (alpha <= .01F) return;
        float t = Mth.clamp(since / w, 0.0F, 1.0F);
        double height = .15D + AcrobatOrdealHits.NET_HEIGHT * (1.0D - t * t);
        double half = AcrobatOrdealHits.NET_RADIUS;
        Vec3 centre = target.add(0.0D, height, 0.0D);
        // A sag in the middle as it falls: the knots lag the edges.
        double sag = since < w ? .8D * t : 0.0D;
        for (int k = -3; k <= 3; k++) {
            double o = half * k / 3.0D;
            for (int s = -3; s < 3; s++) {
                double a = half * s / 3.0D, b = half * (s + 1) / 3.0D;
                double ya = -sag * (1.0D - (a * a + o * o) / (2.0D * half * half)), yb = -sag * (1.0D - (b * b + o * o) / (2.0D * half * half));
                line(pose, consumer, light, overlay, color, alpha, centre.add(a, ya, o), centre.add(b, yb, o), .8F);
                line(pose, consumer, light, overlay, color, alpha, centre.add(o, ya, a), centre.add(o, yb, b), .8F);
            }
        }
    }

    /** The swing's direction, as the server lays it: out from the ring's centre through the mark. */
    private Vec3 swingDirection() {
        Vec3 forward = new Vec3(-Math.sin(bodyYaw * Mth.DEG_TO_RAD), 0.0D, Math.cos(bodyYaw * Mth.DEG_TO_RAD));
        Vec3 centre = new Vec3(at.x, target.y, at.z).add(forward.scale(AcrobatRigging.RADIUS));
        Vec3 out = new Vec3(target.x - centre.x, 0.0D, target.z - centre.z);
        if (out.length() >= 1.5D) return out.normalize();
        return forward;
    }

    /** One line of the rigging from {@code a} to {@code b} in the world, {@code thickness} pixels across. */
    private void line(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float alpha, Vec3 a, Vec3 b,
            float thickness) {
        // A line passing by the eye would fill the view as a streak: it fades out within a few blocks of the camera.
        alpha *= HemoEasing.smoothstep(NEAR_FADE_START, NEAR_FADE_END, (float) cameraDistance(a, b));
        if (alpha <= .01F) return;
        Vec3 ma = model(a), mb = model(b);
        Vec3 d = mb.subtract(ma);
        double length = d.length();
        if (length < 1.0E-3D) return;
        d = d.scale(1.0D / length);
        ghostLine.setPos((float) ma.x, (float) ma.y, (float) ma.z);
        ghostLine.setRotation((float) Math.asin(Mth.clamp(d.z, -1.0D, 1.0D)), 0.0F, (float) Math.atan2(-d.x, d.y));
        ghostLine.xScale = ghostLine.zScale = thickness;
        ghostLine.yScale = (float) (length / 16.0D);
        ghostLine.visible = true;
        ghostLine.render(pose, consumer, light, overlay, tint(color, alpha));
        ghostLine.visible = false;
        ghostLine.xScale = ghostLine.yScale = ghostLine.zScale = 1.0F;
    }

    /** Within this many blocks of the camera a rigging line is gone; past the second it is whole. */
    private static final float NEAR_FADE_START = 2.5F, NEAR_FADE_END = 6.0F;

    /** The camera's nearest distance to the segment a to b, in world blocks. */
    private static double cameraDistance(Vec3 a, Vec3 b) {
        Vec3 eye = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 ab = b.subtract(a);
        double t = ab.lengthSqr() < 1.0E-6D ? 0.0D : Mth.clamp(eye.subtract(a).dot(ab) / ab.lengthSqr(), 0.0D, 1.0D);
        return eye.distanceTo(a.add(ab.scale(t)));
    }

    /**
     * The colour faded by {@code alpha}: in alpha for the translucent pass, and in the colour itself for the glow pass,
     * which is added light and ignores alpha.
     */
    private static int tint(int color, float alpha) {
        float a = Mth.clamp(alpha, 0.0F, 1.0F);
        int r = (int) (((color >> 16) & 255) * a), g = (int) (((color >> 8) & 255) * a), b = (int) ((color & 255) * a);
        return ((int) (a * ((color >>> 24) & 255)) << 24) | (r << 16) | (g << 8) | b;
    }

    /** Where hand {@code i} is in the world this frame, through the hanging body's joints. */
    private Vec3 handWorld(int i) {
        PoseStack stack = new PoseStack();
        root.translateAndRotate(stack);
        hang.translateAndRotate(stack);
        body.translateAndRotate(stack);
        waist.translateAndRotate(stack);
        chest.translateAndRotate(stack);
        uppers[i].translateAndRotate(stack);
        fores[i].translateAndRotate(stack);
        hands[i].translateAndRotate(stack);
        Vector3f p = stack.last().pose().transformPosition(0.0F, 2.0F / 16.0F, 0.0F, new Vector3f());
        return world(new Vec3(p.x * 16.0D, p.y * 16.0D, p.z * 16.0D));
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / AcrobatRigging.SCALE);
        double turn = -(180.0F - bodyYaw) * Mth.DEG_TO_RAD;
        double cos = Math.cos(turn), sin = Math.sin(turn);
        double x = d.x * cos + d.z * sin, z = -d.x * sin + d.z * cos;
        return new Vec3(-x * 16.0D, (-d.y + 1.501D) * 16.0D, z * 16.0D);
    }

    /** The inverse of {@link #model}: a point in this model's pixels in the world. */
    private Vec3 world(Vec3 model) {
        double x = -model.x / 16.0D, y = -(model.y / 16.0D - 1.501D), z = model.z / 16.0D;
        double turn = -(180.0F - bodyYaw) * Mth.DEG_TO_RAD;
        double cos = Math.cos(turn), sin = Math.sin(turn);
        double wx = x * cos - z * sin, wz = x * sin + z * cos;
        return at.add(new Vec3(wx, y, wz).scale(AcrobatRigging.SCALE));
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
