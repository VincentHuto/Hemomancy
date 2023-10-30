package com.vincenthuto.hemomancy.client.model.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.AnimationHelper;
import com.vincenthuto.hemomancy.client.model.IAnimatedModel;
import com.vincenthuto.hemomancy.client.render.item.living.SanguisLanceaItemRenderer.SanguisLanceaAnimContext;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.Level;

public class SanguisLanceaModel extends Model
		implements IAnimatedModel<SanguisLanceaAnimContext> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("sanguislanceamodel"),
			"root");

	private final ModelPart root;

	public SanguisLanceaModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.root = root.getChild("root");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-21.0F, -6.6667F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition handle = root.addOrReplaceChild("handle",
				CubeListBuilder.create().texOffs(12, 24)
						.addBox(-0.5F, 9.6667F, -0.5F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 6)
						.addBox(-0.75F, -9.3333F, -0.75F, 1.5F, 19.0F, 1.5F, new CubeDeformation(0.0F)).texOffs(12, 0)
						.addBox(-1.0F, -31.3333F, -1.0F, 2.0F, 22.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 21.0F));

		PartDefinition blade = root.addOrReplaceChild("blade", CubeListBuilder.create(),
				PartPose.offset(0.0F, -30.3333F, 21.0F));

		PartDefinition forks = blade.addOrReplaceChild("forks", CubeListBuilder.create(),
				PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition lForkP = forks.addOrReplaceChild("lForkP", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition rFork2 = lForkP.addOrReplaceChild("rFork2", CubeListBuilder.create(),
				PartPose.offsetAndRotation(2.0749F, -2.4749F, -0.125F, 0.1745F, 0.0F, -0.7854F));

		PartDefinition bone14 = rFork2.addOrReplaceChild("bone14",
				CubeListBuilder.create().texOffs(0, 35).mirror()
						.addBox(3.5F, -70.6418F, 21.0938F, 2.0F, 8.0F, 1.25F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(29, 18).mirror()
						.addBox(1.5F, -65.6418F, 21.0938F, 2.0F, 3.0F, 1.5F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-3.5F, 64.8936F, -21.8408F));

		PartDefinition bone15 = bone14.addOrReplaceChild("bone15",
				CubeListBuilder.create().texOffs(0, 36).mirror()
						.addBox(-1.0F, -2.8208F, -0.4146F, 2.0F, 4.0F, 1.25F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(4.5F, -71.6418F, 21.8438F, -0.3054F, 0.0F, 0.0F));

		PartDefinition bone16 = bone15.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(24, 3).mirror()
				.addBox(-2.5943F, -1.0403F, -0.1518F, 7.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(1.5F, -3.7395F, 0.3473F, -0.1745F, -0.1745F, 0.0F));

		PartDefinition bone17 = bone16.addOrReplaceChild("bone17", CubeListBuilder.create().texOffs(23, 3).mirror()
				.addBox(-3.3375F, -0.5605F, -1.2304F, 5.0F, 2.0F, 0.75F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(7.7681F, -0.4797F, 0.5948F, 0.0F, 0.2182F, 0.0F));

		PartDefinition bone18 = bone17.addOrReplaceChild("bone18", CubeListBuilder.create().texOffs(0, 0).mirror()
				.addBox(-0.4499F, -32.1759F, -1.079F, 2.0F, 34.0F, 0.5F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(2.0F, -1.0F, -0.5F, 0.1775F, -0.1715F, 0.8116F));

		PartDefinition rForkP = forks.addOrReplaceChild("rForkP", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition rFork = rForkP.addOrReplaceChild("rFork", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-2.0749F, -2.4749F, 0.125F, -0.1745F, 0.0F, 0.7854F));

		PartDefinition bone9 = rFork.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(48, 23)
				.addBox(-5.5F, -70.8918F, -22.3438F, 2.0F, 8.0F, 1.25F, new CubeDeformation(0.0F)).texOffs(53, 42)
				.addBox(-3.5F, -65.8918F, -22.5938F, 2.0F, 3.0F, 1.5F, new CubeDeformation(0.0F)),
				PartPose.offset(3.5F, 64.8936F, 21.8408F));

		PartDefinition bone10 = bone9.addOrReplaceChild("bone10",
				CubeListBuilder.create().texOffs(8, 50).addBox(-1.0F, -2.8208F, -0.8354F, 2.0F, 4.0F, 1.25F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.5F, -71.6418F, -21.8438F, 0.3054F, 0.0F, 0.0F));

		PartDefinition bone11 = bone10.addOrReplaceChild("bone11",
				CubeListBuilder.create().texOffs(9, 51).addBox(-4.4057F, -1.0403F, -0.8482F, 7.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5F, -3.7395F, -0.3473F, 0.1745F, -0.1745F, 0.0F));

		PartDefinition bone12 = bone11.addOrReplaceChild("bone12",
				CubeListBuilder.create().texOffs(9, 51).addBox(-1.6625F, -0.5605F, 0.4804F, 5.0F, 2.0F, 0.75F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-7.7681F, -0.4797F, -0.5948F, 0.0F, 0.2182F, 0.0F));

		PartDefinition bone13 = bone12.addOrReplaceChild("bone13",
				CubeListBuilder.create().texOffs(52, 22).addBox(-1.5501F, -32.1759F, 0.579F, 2.0F, 34.0F, 0.5F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, -1.0F, 0.5F, -0.1789F, -0.2151F, -0.8116F));

		PartDefinition jewelP = blade.addOrReplaceChild("jewelP", CubeListBuilder.create(),
				PartPose.offset(0.0464F, -9.896F, 0.0F));

		PartDefinition medialJewles = jewelP.addOrReplaceChild("medialJewles",
				CubeListBuilder.create().texOffs(29, 12)
						.addBox(-0.1667F, 0.0833F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(29, 12)
						.addBox(-6.6667F, -6.9167F, -0.75F, 3.0F, 3.0F, 1.5F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(4.3333F, 4.3333F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnimation(Level level, float partialTicks, SanguisLanceaAnimContext ctx) {
		this.getRoot().getAllParts().forEach(ModelPart::resetPose);
		AnimationState state = ctx.state();
		float time = (float) level.getGameTime() + partialTicks;
		AnimationHelper.animate(this, state, SANGUISLANCEAMODEL_NEW, time, 1F);
		float animTime = AnimationHelper.getElapsedSeconds(SANGUISLANCEAMODEL_NEW, state.getAccumulatedTime());

	}

	@Override
	public ModelPart getRoot() {
		return this.root;
	}

	@Override
	public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay,
			float pRed, float pGreen, float pBlue, float pAlpha) {
		root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
	}

	public static final AnimationDefinition SANGUISLANCEAMODEL_NEW = AnimationDefinition.Builder.withLength(1f)
			.looping()
			.addAnimation("handle",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, -180f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, -360f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("forks",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 180f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 360f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("jewelP", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.posVec(0f, 0f, 0f),
							AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("jewelP",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.build();
}