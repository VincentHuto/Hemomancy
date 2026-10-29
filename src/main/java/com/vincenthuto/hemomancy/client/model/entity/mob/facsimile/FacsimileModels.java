package com.vincenthuto.hemomancy.client.model.entity.mob.facsimile;

import com.google.common.collect.Iterables;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.BlazeModel;
import net.minecraft.client.model.ChestedHorseModel;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;

import java.util.List;

/**
 * The vanilla models the hive copied, posed as vanilla poses them and then scaled wrong per entity. Each runs
 * {@link FacsimileDeformation} after the vanilla animation, over every part but the root.
 */
public final class FacsimileModels {
	private FacsimileModels() {}

	private static Iterable<ModelPart> belowRoot(ModelPart root) {
		return () -> root.getAllParts().skip(1).iterator();
	}

	public static class FacsimileBatModel extends BatModel {
		public FacsimileBatModel(ModelPart root) {
			super(root);
		}

		@Override
		public void setupAnim(Bat entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
				float headPitch) {
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			FacsimileDeformation.apply(belowRoot(root()), entity);
		}
	}

	public static class FacsimileBlazeModel extends BlazeModel<Blaze> {
		public FacsimileBlazeModel(ModelPart root) {
			super(root);
		}

		@Override
		public void setupAnim(Blaze entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
				float headPitch) {
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			FacsimileDeformation.apply(belowRoot(root()), entity);
		}
	}

	public static class FacsimileGolemModel extends IronGolemModel<IronGolem> {
		public FacsimileGolemModel(ModelPart root) {
			super(root);
		}

		@Override
		public void setupAnim(IronGolem entity, float limbSwing, float limbSwingAmount, float ageInTicks,
				float netHeadYaw, float headPitch) {
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			FacsimileDeformation.apply(belowRoot(root()), entity);
		}
	}

	public static class FacsimileSpiderModel extends SpiderModel<Spider> {
		public FacsimileSpiderModel(ModelPart root) {
			super(root);
		}

		@Override
		public void setupAnim(Spider entity, float limbSwing, float limbSwingAmount, float ageInTicks,
				float netHeadYaw, float headPitch) {
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			FacsimileDeformation.apply(belowRoot(root()), entity);
		}
	}

	public static class FacsimileEndermanModel extends EndermanModel<EnderMan> {
		public FacsimileEndermanModel(ModelPart root) {
			super(root);
		}

		@Override
		public void setupAnim(EnderMan entity, float limbSwing, float limbSwingAmount, float ageInTicks,
				float netHeadYaw, float headPitch) {
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			FacsimileDeformation.apply(List.of(head, body, rightArm, leftArm, rightLeg, leftLeg), entity);
			// The hat layer follows the head it was copied from.
			hat.xScale = head.xScale;
			hat.yScale = head.yScale;
			hat.zScale = head.zScale;
		}
	}

	public static class FacsimileHorseModel extends HorseModel<Horse> {
		public FacsimileHorseModel(ModelPart root) {
			super(root);
		}

		@Override
		public void setupAnim(Horse entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
				float headPitch) {
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			FacsimileDeformation.apply(Iterables.concat(headParts(), bodyParts()), entity);
		}
	}

	public static class FacsimileDonkeyModel extends ChestedHorseModel<Donkey> {
		public FacsimileDonkeyModel(ModelPart root) {
			super(root);
		}

		@Override
		public void setupAnim(Donkey entity, float limbSwing, float limbSwingAmount, float ageInTicks,
				float netHeadYaw, float headPitch) {
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			FacsimileDeformation.apply(Iterables.concat(headParts(), bodyParts()), entity);
		}
	}

	public static class FacsimileWolfModel extends WolfModel<Wolf> {
		public FacsimileWolfModel(ModelPart root) {
			super(root);
		}

		@Override
		public void setupAnim(Wolf entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
				float headPitch) {
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			FacsimileDeformation.apply(Iterables.concat(headParts(), bodyParts()), entity);
		}
	}
}
