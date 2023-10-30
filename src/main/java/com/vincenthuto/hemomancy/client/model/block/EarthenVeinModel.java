package com.vincenthuto.hemomancy.client.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.AnimationHelper;
import com.vincenthuto.hemomancy.client.model.IAnimatedModel;
import com.vincenthuto.hemomancy.client.render.tile.EarthenVeinRenderer;
import com.vincenthuto.hemomancy.client.render.tile.EarthenVeinRenderer.EarthenVeinAnimContext;

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

public class EarthenVeinModel extends Model implements IAnimatedModel<EarthenVeinRenderer.EarthenVeinAnimContext> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("earthenveinmodel"),
			"root");

	// super(RenderType::entityTranslucent);

	private final ModelPart root;

	public EarthenVeinModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.root = root.getChild("root");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(-14, 49).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone2 = root.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 26).addBox(-4.5F, -8.0F, -4.5F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, 0.0F));

		PartDefinition section = root.addOrReplaceChild("section", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -1.9F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone = root.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(27, 28).addBox(-4.5F, 1.0F, -4.5F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, 0.0F));

		PartDefinition section2 = root.addOrReplaceChild("section2", CubeListBuilder.create().texOffs(21, 39).addBox(-3.5F, -4.0F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition section3 = root.addOrReplaceChild("section3", CubeListBuilder.create().texOffs(0, 37).addBox(-3.5F, -4.0F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition section4 = root.addOrReplaceChild("section4", CubeListBuilder.create().texOffs(36, 0).addBox(-3.5F, -4.0F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

		PartDefinition section5 = root.addOrReplaceChild("section5", CubeListBuilder.create().texOffs(0, 14).addBox(-5.0F, -6.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

		PartDefinition section6 = root.addOrReplaceChild("section6", CubeListBuilder.create().texOffs(30, 14).addBox(-3.5F, -6.0F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

		PartDefinition eye = root.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 6).addBox(-1.0F, -1.5F, -2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -6.5F, 3.5F));

		PartDefinition eye2 = root.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -9.5F, -3.0F));

		PartDefinition stent = root.addOrReplaceChild("stent", CubeListBuilder.create().texOffs(21, 44).addBox(3.0F, -3.0F, -3.0F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -17.0F, 0.0F));

		PartDefinition cube_r1 = stent.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(21, 44).addBox(0.0F, -3.0F, -3.0F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r2 = stent.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(21, 44).addBox(3.0F, -3.0F, -6.0F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r3 = stent.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(21, 44).addBox(0.0F, -3.0F, -3.0F, 0.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition nametag = stent.addOrReplaceChild("nametag", CubeListBuilder.create().texOffs(14, 56).addBox(0.0943F, -1.0593F, -0.4637F, 3.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1224F, 1.4871F, 1.9209F, -0.4341F, 0.3843F, -0.2121F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnimation(Level level, float partialTicks, EarthenVeinAnimContext ctx) {
		this.getRoot().getAllParts().forEach(ModelPart::resetPose);
		AnimationState state = ctx.state();
		float time = (float) level.getGameTime() + partialTicks;
		AnimationHelper.animate(this, state, EARTHENVEINMODEL_WIGGLE, time, 1F);
		float animTime = AnimationHelper.getElapsedSeconds(EARTHENVEINMODEL_WIGGLE, state.getAccumulatedTime());

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

	public static final AnimationDefinition EARTHENVEINMODEL_WIGGLE = AnimationDefinition.Builder.withLength(2.25f)
			.looping()
			.addAnimation("section",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.3433333f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.625f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.7916766f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.9583434f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section2",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section2",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.375f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.6766666f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.8343334f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section3",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section3",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.4583433f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.75f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.9167666f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.0834333f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section4",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section4",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5416766f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.75f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.9167666f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.1676667f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section5",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section5",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.7916766f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.9583434f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.0834333f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("bone",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("bone",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.3433333f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.625f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.7916766f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.9583434f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section6",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0.041676664f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("section6",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0.041676664f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.7916766f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.9583434f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.0834333f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("eye",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 37.5f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.625f, KeyframeAnimations.degreeVec(11.45f, 32.2f, 20.82f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.125f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.625f, KeyframeAnimations.degreeVec(-11.45f, -32.2f, -20.82f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.9583433f, KeyframeAnimations.degreeVec(0f, -37.5f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("eye",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.6766666f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.8343334f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("eye2",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0f, KeyframeAnimations.degreeVec(11.45f, 32.2f, 20.82f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.625f, KeyframeAnimations.degreeVec(0f, 37.5f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, -37.5f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.4167667f, KeyframeAnimations.degreeVec(-11.45f, -32.2f, -20.82f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.875f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("eye2",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.4583433f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.75f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.9167666f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.0416767f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("bone2",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.2083435f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("bone2",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.4583433f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.75f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.9167666f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.0416767f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.2083435f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("stent", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2916767f, KeyframeAnimations.posVec(0f, -1f, 0f),
							AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.25f, KeyframeAnimations.posVec(0f, 0f, 0f),
							AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("stent",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("nametag",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.7916766f, KeyframeAnimations.degreeVec(7.5f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.build();
}