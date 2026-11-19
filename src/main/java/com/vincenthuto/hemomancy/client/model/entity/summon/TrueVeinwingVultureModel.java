package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.VultureOrdealMoves;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_true_veinwing_vulture.py (the Vulture's true self, 2026-11-17).
/**
 * The Veinwing Vulture's true self in the Acrobat's Chamber: a bird of prey that flies with weight, a fast downstroke
 * lifting the body and a slow folding recovery, banking into its turns and pitching with its climbs. Its ordeal moves
 * ({@link VultureOrdealMoves}) pose from an entity event: the stoop's climb, fold and head-first dive and its sprawl on
 * the floor after, the rake's drawn-up talons and swept-back glide, the downdraft's raised wings slammed down, and the
 * carry's laboured climb with its talons clenched.
 */
public class TrueVeinwingVultureModel extends EntityModel<VeinwingVultureEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("true_veinwing_vulture"), "main");

	private final ModelPart root, head, tail, tailCord;
	private final ModelPart[] wings = new ModelPart[2], forewings = new ModelPart[2], talons = new ModelPart[2],
			hooks = new ModelPart[2];
	private final ModelPart[][] membranes = new ModelPart[2][3];

	public TrueVeinwingVultureModel(ModelPart bakedRoot) {
		root = bakedRoot.getChild("root");
		head = root.getChild("head");
		tail = root.getChild("tail");
		tailCord = tail.getChild("tail_cord");
		for (int i = 0; i < 2; i++) {
			String side = i == 0 ? "left" : "right";
			wings[i] = root.getChild(side + "_wing");
			forewings[i] = wings[i].getChild(side + "_forewing");
			talons[i] = root.getChild(side + "_talon");
			hooks[i] = talons[i].getChild(side + "_hook");
			for (int m = 0; m < 3; m++) membranes[i][m] = forewings[i].getChild(side + "_membrane_" + m);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 7.0F, 4.0F).texOffs(17, 0).addBox(-3.0F, -7.0F, 0.0F, 6.0F, 3.0F, 3.0F).texOffs(36, 0).addBox(-0.5F, -5.0F, -3.0F, 1.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition ruff = root.addOrReplaceChild("ruff", CubeListBuilder.create().texOffs(41, 0).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F).texOffs(62, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 1.0F, 4.0F), PartPose.offset(0.0F, -6.0F, -0.5F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(79, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F).texOffs(88, 0).addBox(-1.5F, -5.0F, -3.0F, 3.0F, 3.0F, 3.0F).texOffs(101, 0).addBox(-1.0F, -4.0F, -5.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(0.0F, -7.0F, -2.5F));
        PartDefinition beak_hook = head.addOrReplaceChild("beak_hook", CubeListBuilder.create().texOffs(110, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -2.5F, -5.0F, 0.2792526803190927F, 0.0F, 0.0F));
        PartDefinition left_wing = root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(115, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(124, 0).addBox(0.0F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F).texOffs(139, 0).addBox(1.0F, 0.5F, 0.0F, 6.0F, 5.0F, 1.0F), PartPose.offset(2.5F, -4.0F, 0.0F));
        PartDefinition left_forewing = left_wing.addOrReplaceChild("left_forewing", CubeListBuilder.create().texOffs(154, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(163, 0).addBox(0.0F, -0.5F, -0.5F, 9.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, -0.10471975511965978F, -0.29670597283903605F));
        PartDefinition left_membrane_0 = left_forewing.addOrReplaceChild("left_membrane_0", CubeListBuilder.create().texOffs(184, 0).addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F).texOffs(193, 0).addBox(2.0F, -0.5F, -0.5F, 1.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(0.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.08726646259971647F));
        PartDefinition left_membrane_1 = left_forewing.addOrReplaceChild("left_membrane_1", CubeListBuilder.create().texOffs(198, 0).addBox(0.0F, 0.0F, 0.0F, 3.0F, 11.0F, 1.0F).texOffs(207, 0).addBox(2.0F, -0.5F, -0.5F, 1.0F, 12.0F, 1.0F), PartPose.offsetAndRotation(3.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.19198621771937624F));
        PartDefinition left_membrane_2 = left_forewing.addOrReplaceChild("left_membrane_2", CubeListBuilder.create().texOffs(212, 0).addBox(0.0F, 0.0F, 0.0F, 3.0F, 14.0F, 1.0F).texOffs(221, 0).addBox(2.0F, -0.5F, -0.5F, 1.0F, 15.0F, 1.0F), PartPose.offsetAndRotation(6.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.29670597283903605F));
        PartDefinition left_talon = root.addOrReplaceChild("left_talon", CubeListBuilder.create().texOffs(226, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(231, 0).addBox(-1.5F, 3.0F, -3.5F, 1.0F, 1.0F, 4.0F).texOffs(242, 0).addBox(0.5F, 3.0F, -3.5F, 1.0F, 1.0F, 4.0F).texOffs(0, 17).addBox(-0.5F, 3.0F, 0.5F, 1.0F, 1.0F, 2.0F), PartPose.offset(1.25F, 2.0F, 0.0F));
        PartDefinition right_wing = root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(7, 17).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(16, 17).addBox(-6.0F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F).texOffs(31, 17).addBox(-7.0F, 0.5F, 0.0F, 6.0F, 5.0F, 1.0F), PartPose.offset(-2.5F, -4.0F, 0.0F));
        PartDefinition right_forewing = right_wing.addOrReplaceChild("right_forewing", CubeListBuilder.create().texOffs(46, 17).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(55, 17).addBox(-9.0F, -0.5F, -0.5F, 9.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.0F, 0.10471975511965978F, 0.29670597283903605F));
        PartDefinition right_membrane_0 = right_forewing.addOrReplaceChild("right_membrane_0", CubeListBuilder.create().texOffs(76, 17).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F).texOffs(85, 17).addBox(-3.0F, -0.5F, -0.5F, 1.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.08726646259971647F));
        PartDefinition right_membrane_1 = right_forewing.addOrReplaceChild("right_membrane_1", CubeListBuilder.create().texOffs(90, 17).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 11.0F, 1.0F).texOffs(99, 17).addBox(-3.0F, -0.5F, -0.5F, 1.0F, 12.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.19198621771937624F));
        PartDefinition right_membrane_2 = right_forewing.addOrReplaceChild("right_membrane_2", CubeListBuilder.create().texOffs(104, 17).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 14.0F, 1.0F).texOffs(113, 17).addBox(-3.0F, -0.5F, -0.5F, 1.0F, 15.0F, 1.0F), PartPose.offsetAndRotation(-6.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.29670597283903605F));
        PartDefinition right_talon = root.addOrReplaceChild("right_talon", CubeListBuilder.create().texOffs(118, 17).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(123, 17).addBox(-1.5F, 3.0F, -3.5F, 1.0F, 1.0F, 4.0F).texOffs(134, 17).addBox(0.5F, 3.0F, -3.5F, 1.0F, 1.0F, 4.0F).texOffs(145, 17).addBox(-0.5F, 3.0F, 0.5F, 1.0F, 1.0F, 2.0F), PartPose.offset(-1.25F, 2.0F, 0.0F));
        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(152, 17).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 2.0F, 2.0F));
        PartDefinition tail_cord = tail.addOrReplaceChild("tail_cord", CubeListBuilder.create().texOffs(163, 17).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, 0.19198621771937624F, 0.0F, 0.0F));
        PartDefinition tail_needle = tail_cord.addOrReplaceChild("tail_needle", CubeListBuilder.create().texOffs(168, 17).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, 0.29670597283903605F, 0.0F, 0.0F));
        PartDefinition quill_0 = ruff.addOrReplaceChild("quill_0", CubeListBuilder.create().texOffs(173, 17).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(-2.5F, -1.0F, -1.0F, 0.17453292519943295F, 0.0F, -0.6108652381980153F));
        PartDefinition quill_1 = ruff.addOrReplaceChild("quill_1", CubeListBuilder.create().texOffs(178, 17).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(2.5F, -1.0F, -1.0F, 0.17453292519943295F, 0.0F, 0.6108652381980153F));
        PartDefinition quill_2 = ruff.addOrReplaceChild("quill_2", CubeListBuilder.create().texOffs(183, 17).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(-2.0F, -1.0F, 2.0F, -0.3490658503988659F, 0.0F, -0.4363323129985824F));
        PartDefinition quill_3 = ruff.addOrReplaceChild("quill_3", CubeListBuilder.create().texOffs(188, 17).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(2.0F, -1.0F, 2.0F, -0.3490658503988659F, 0.0F, 0.4363323129985824F));
        PartDefinition quill_4 = ruff.addOrReplaceChild("quill_4", CubeListBuilder.create().texOffs(193, 17).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -1.0F, 2.5F, -0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition left_spur = left_forewing.addOrReplaceChild("left_spur", CubeListBuilder.create().texOffs(198, 17).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.4363323129985824F));
        PartDefinition left_elbow_spur = left_wing.addOrReplaceChild("left_elbow_spur", CubeListBuilder.create().texOffs(203, 17).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, -0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_hook = left_talon.addOrReplaceChild("left_hook", CubeListBuilder.create().texOffs(208, 17).addBox(-1.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F).texOffs(213, 17).addBox(0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 3.0F, -3.5F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_spur = right_forewing.addOrReplaceChild("right_spur", CubeListBuilder.create().texOffs(218, 17).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.4363323129985824F));
        PartDefinition right_elbow_spur = right_wing.addOrReplaceChild("right_elbow_spur", CubeListBuilder.create().texOffs(223, 17).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, -0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_hook = right_talon.addOrReplaceChild("right_hook", CubeListBuilder.create().texOffs(228, 17).addBox(-1.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F).texOffs(233, 17).addBox(0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 3.0F, -3.5F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition strap = torso.addOrReplaceChild("strap", CubeListBuilder.create().texOffs(238, 17).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -3.0F, 2.0F, -0.2617993877991494F, 0.0F, 0.17453292519943295F));
        return LayerDefinition.create(mesh, 256, 256);
	}

	@Override
	public void setupAnim(VeinwingVultureEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
			float pitch) {
		float partialTick = age - entity.tickCount;
		float turn = Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO);
		float climb = (float) (entity.getY() - entity.yo);
		pose(age, yaw, pitch, turn, climb, entity.ordealMove(), entity.ordealMoveAge(partialTick));
	}

	/** The whole pose; {@code move} is -1 between moves, {@code since} the ticks since its wind-up began. */
	public void pose(float age, float yaw, float pitch, float turn, float climb, int move, float since) {
		root.getAllParts().forEach(ModelPart::resetPose);
		// A beat with weight: a fast downstroke over a third of the cycle, a slow folding recovery over the rest.
		float rate = move == VultureOrdealMoves.STOOP && since < VultureOrdealMoves.STOOP_WINDUP - 6 ? .7F
				: move == VultureOrdealMoves.CARRY && since > VultureOrdealMoves.CARRY_WINDUP ? .32F : .45F;
		float beat = beat(age * rate);
		boolean down = (age * rate / Mth.TWO_PI) % 1.0F < .35F;
		root.y += -1.1F * beat;
		root.zRot += Mth.clamp(turn * .06F, -.55F, .55F);
		root.xRot += Mth.clamp(-climb * 1.4F, -.6F, .6F);
		head.yRot = Mth.clamp(yaw, -60, 60) * Mth.DEG_TO_RAD;
		head.xRot = Mth.clamp(pitch, -40, 40) * Mth.DEG_TO_RAD * .7F;
		for (int i = 0; i < 2; i++) {
			float sign = i == 0 ? 1 : -1;
			wings[i].zRot = sign * (.15F + beat);
			wings[i].yRot = -sign * .18F;
			// The hand of the wing folds in on the way up and opens through the stroke.
			forewings[i].zRot += -sign * (down ? .1F : .55F) - sign * beat * .2F;
			for (int m = 0; m < 3; m++) membranes[i][m].zRot += -sign * (down ? 0.0F : .15F * (m + 1));
			talons[i].xRot = .35F + Mth.cos(age * .18F + i) * .1F;
		}
		tail.xRot = .25F + Mth.sin(age * .22F) * .1F;
		tailCord.xRot += Mth.sin(age * .22F - .7F) * .15F;
		if (move >= 0) move(move, since, age);
	}

	/** The wing's angle through a beat: up at -0.9, down at +0.6. */
	private static float beat(float phase) {
		float w = (phase / Mth.TWO_PI) % 1.0F;
		if (w < 0.0F) w += 1.0F;
		if (w < .35F) return Mth.lerp(HemoEasing.smoothstep(w / .35F), -.9F, .6F);
		return Mth.lerp(HemoEasing.smoothstep((w - .35F) / .65F), .6F, -.9F);
	}

	/** 0 until two ticks before {@code at}, then 1 at {@code at}, accelerating. */
	private static float fall(float since, float at) {
		float t = Mth.clamp((since - (at - 2.0F)) / 2.0F, 0.0F, 1.0F);
		return t * t;
	}

	private void wingsTo(float weight, float z, float y, float fore) {
		for (int i = 0; i < 2; i++) {
			float sign = i == 0 ? 1 : -1;
			wings[i].zRot = Mth.lerp(weight, wings[i].zRot, sign * z);
			wings[i].yRot = Mth.lerp(weight, wings[i].yRot, -sign * y);
			forewings[i].zRot = Mth.lerp(weight, forewings[i].zRot, -sign * fore);
		}
	}

	private void talonsTo(float weight, float x) {
		for (int i = 0; i < 2; i++) {
			talons[i].xRot = Mth.lerp(weight, talons[i].xRot, x);
			hooks[i].xRot = Mth.lerp(weight, hooks[i].xRot, x < -.5F ? -.3F : .6F);
		}
	}

	private void move(int move, float since, float age) {
		switch (move) {
			case VultureOrdealMoves.STOOP -> {
				float w = VultureOrdealMoves.STOOP_WINDUP, dive = VultureOrdealMoves.STOOP_DIVE, down = VultureOrdealMoves.STOOP_DOWN;
				float climbing = 1.0F - HemoEasing.smoothstep(w - 8.0F, w - 5.0F, since);
				float fold = HemoEasing.smoothstep(w - 7.0F, w - 1.0F, since) * (1.0F - HemoEasing.smoothstep(w + dive - 1.0F, w + dive, since));
				float diving = HemoEasing.smoothstep(w - 1.0F, w, since) * (1.0F - HemoEasing.smoothstep(w + dive - 1.0F, w + dive, since));
				float sprawl = HemoEasing.smoothstep(w + dive - 1.0F, w + dive, since)
						* (1.0F - HemoEasing.smoothstep(w + dive + down, w + dive + down + 8.0F, since));
				root.xRot = Mth.lerp(climbing * HemoEasing.smoothstep(0.0F, 4.0F, since), root.xRot, -.45F);
				// Wings folding back tight, head tipping down, then head first.
				wingsTo(fold, -1.1F, 1.0F, .9F);
				root.xRot = Mth.lerp(fold, root.xRot, Mth.lerp(diving, .9F, 1.45F));
				head.xRot = Mth.lerp(fold, head.xRot, .5F);
				talonsTo(diving, -1.3F);
				// Sprawled on the floor where it hit, the wings flung out flat, beating weakly.
				float weak = Mth.sin(age * .5F) * .15F;
				wingsTo(sprawl, .35F + weak, .05F, -.2F);
				root.xRot = Mth.lerp(sprawl, root.xRot, .25F);
				root.y = Mth.lerp(sprawl, root.y, 17.5F);
				head.xRot = Mth.lerp(sprawl, head.xRot, .6F);
				talonsTo(sprawl, .9F);
			}
			case VultureOrdealMoves.RAKE -> {
				float w = VultureOrdealMoves.RAKE_WINDUP, run = VultureOrdealMoves.RAKE_RUN;
				float drawn = HemoEasing.smoothstep(0.0F, w - 3.0F, since) * (1.0F - HemoEasing.smoothstep(w - 1.0F, w, since));
				float gliding = HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since) * (1.0F - HemoEasing.smoothstep(w + run, w + run + 2.0F, since));
				float flare = HemoEasing.smoothstep(w + run, w + run + 2.0F, since) * (1.0F - HemoEasing.smoothstep(w + run + 4.0F, w + run + 12.0F, since));
				root.xRot = Mth.lerp(drawn, root.xRot, -.55F);
				talonsTo(drawn, -1.7F);
				wingsTo(drawn, -.55F + .1F * Mth.sin(since * 1.4F), .2F, .3F);
				// The pass: wings swept back in a glide, body level, talons thrust low ahead.
				wingsTo(gliding, .1F, .75F, .2F);
				root.xRot = Mth.lerp(gliding, root.xRot, .15F);
				talonsTo(gliding, -1.1F);
				head.xRot = Mth.lerp(gliding, head.xRot, .35F);
				// The flare out of it: wings thrown up, body reared.
				wingsTo(flare, -1.1F, -.1F, .5F);
				root.xRot = Mth.lerp(flare, root.xRot, -.6F);
				talonsTo(flare, .2F);
			}
			case VultureOrdealMoves.DOWNDRAFT -> {
				float w = VultureOrdealMoves.DOWNDRAFT_WINDUP;
				float raise = HemoEasing.smoothstep(0.0F, w - 6.0F, since), slam = fall(since, w);
				float held = 1.0F - HemoEasing.smoothstep(w + 6.0F, w + 16.0F, since);
				float tremble = Mth.sin(since * 3.2F) * HemoEasing.smoothstep(w - 8.0F, w - 5.0F, since) * (1.0F - slam);
				wingsTo(raise * held, Mth.lerp(slam, -1.4F + .06F * tremble, 1.05F), Mth.lerp(slam, -.15F, .1F), Mth.lerp(slam, .9F, -.35F));
				root.xRot = Mth.lerp(held, root.xRot, Mth.lerp(slam, -.35F * raise, .35F));
				root.y += held * slam * 1.5F;
				head.xRot = Mth.lerp(held, head.xRot, Mth.lerp(slam, -.3F * raise, .55F));
				talonsTo(raise * held, Mth.lerp(slam, .6F, -.4F));
			}
			case VultureOrdealMoves.CARRY -> {
				float w = VultureOrdealMoves.CARRY_WINDUP, hold = VultureOrdealMoves.CARRY_HOLD;
				float reach = HemoEasing.smoothstep(0.0F, w - 2.0F, since) * (1.0F - HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since));
				float clutch = HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since) * (1.0F - HemoEasing.smoothstep(w + hold, w + hold + 3.0F, since));
				float open = HemoEasing.smoothstep(w + hold, w + hold + 2.0F, since) * (1.0F - HemoEasing.smoothstep(w + hold + 3.0F, w + hold + 10.0F, since));
				root.xRot = Mth.lerp(reach, root.xRot, -.5F);
				talonsTo(reach, -1.6F);
				wingsTo(reach, -.3F, .1F, .2F);
				// Labouring up with the fighter clutched under it: talons down and closed, the body hanging back.
				root.xRot = Mth.lerp(clutch, root.xRot, -.25F);
				talonsTo(clutch, .15F);
				for (int i = 0; i < 2; i++) hooks[i].xRot = Mth.lerp(clutch, hooks[i].xRot, 1.1F);
				talonsTo(open, -.9F);
			}
			default -> {
			}
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
