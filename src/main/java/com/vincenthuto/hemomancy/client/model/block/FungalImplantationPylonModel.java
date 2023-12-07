package com.vincenthuto.hemomancy.client.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.AnimationHelper;
import com.vincenthuto.hemomancy.client.model.IAnimatedModel;
import com.vincenthuto.hemomancy.client.render.tile.FungalImplantationPylonRenderer;
import com.vincenthuto.hemomancy.client.render.tile.FungalImplantationPylonRenderer.FungalImplantationPylonAnimContext;

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

public class FungalImplantationPylonModel extends Model
		implements IAnimatedModel<FungalImplantationPylonRenderer.FungalImplantationPylonAnimContext> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("fungalimplantationdevicemodel"), "main");
	private final ModelPart root;

	public FungalImplantationPylonModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.root = root.getChild("root");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition mainMushroom = root.addOrReplaceChild("mainMushroom", CubeListBuilder.create().texOffs(-40, 152).addBox(-19.0F, 3.9F, -20.0F, 40.0F, 0.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition arms = mainMushroom.addOrReplaceChild("arms", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 0.0F));

		PartDefinition arm = arms.addOrReplaceChild("arm", CubeListBuilder.create().texOffs(72, 34).addBox(-9.5F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 34).addBox(-8.5F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.5F, -27.0F, 0.0F));

		PartDefinition elbow = arm.addOrReplaceChild("elbow", CubeListBuilder.create().texOffs(0, 38).addBox(-10.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 68).addBox(-11.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.5F, 0.0F, 0.0F));

		PartDefinition forarm = elbow.addOrReplaceChild("forarm", CubeListBuilder.create().texOffs(12, 68).addBox(-12.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 24).addBox(-10.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 0.0F));

		PartDefinition hand = forarm.addOrReplaceChild("hand", CubeListBuilder.create().texOffs(72, 70).addBox(-2.0F, -1.5F, -1.75F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(10, 76).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0F, 0.0F, 0.0F));

		PartDefinition finger2 = hand.addOrReplaceChild("finger2", CubeListBuilder.create().texOffs(10, 80).addBox(-4.0F, -1.5F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -0.5F, -1.0F));

		PartDefinition nail2 = finger2.addOrReplaceChild("nail2", CubeListBuilder.create().texOffs(7, 52).addBox(-4.0F, 0.0F, -0.5F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -1.0F, -0.5F));

		PartDefinition finger1 = hand.addOrReplaceChild("finger1", CubeListBuilder.create().texOffs(80, 36).addBox(-4.0F, -0.5F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 1.5F, -1.0F));

		PartDefinition nail1 = finger1.addOrReplaceChild("nail1", CubeListBuilder.create().texOffs(7, 60).addBox(-4.0F, 0.0F, -0.5F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 0.0F, -0.5F));

		PartDefinition finger3 = hand.addOrReplaceChild("finger3", CubeListBuilder.create().texOffs(80, 34).addBox(-4.0F, -1.5F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 1.0F, 1.0F));

		PartDefinition nail3 = finger3.addOrReplaceChild("nail3", CubeListBuilder.create().texOffs(7, 53).addBox(-4.0F, 0.0F, -0.5F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -1.0F, 0.5F));

		PartDefinition arm2 = arms.addOrReplaceChild("arm2", CubeListBuilder.create().texOffs(12, 60).addBox(7.5F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).addBox(-1.5F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.75F, -27.0F, 6.0F, 0.0F, -1.0036F, 0.0F));

		PartDefinition elbow2 = arm2.addOrReplaceChild("elbow2", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(9.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(8.5F, 0.0F, 0.0F));

		PartDefinition forarm2 = elbow2.addOrReplaceChild("forarm2", CubeListBuilder.create().texOffs(12, 50).addBox(10.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 12).addBox(0.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 0.0F, 0.0F));

		PartDefinition hand2 = forarm2.addOrReplaceChild("hand2", CubeListBuilder.create().texOffs(72, 48).addBox(0.0F, -1.5F, -1.75F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 76).addBox(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(12.0F, 0.0F, 0.0F));

		PartDefinition finger4 = hand2.addOrReplaceChild("finger4", CubeListBuilder.create().texOffs(0, 80).addBox(0.0F, -1.5F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -0.5F, -1.0F));

		PartDefinition nail4 = finger4.addOrReplaceChild("nail4", CubeListBuilder.create().texOffs(7, 51).addBox(0.0F, 0.0F, -0.5F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -1.0F, -0.5F));

		PartDefinition finger5 = hand2.addOrReplaceChild("finger5", CubeListBuilder.create().texOffs(79, 70).addBox(0.0F, -0.5F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 1.5F, -1.0F));

		PartDefinition nail5 = finger5.addOrReplaceChild("nail5", CubeListBuilder.create().texOffs(7, 50).addBox(0.0F, 0.0F, -0.5F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 0.0F, -0.5F));

		PartDefinition finger6 = hand2.addOrReplaceChild("finger6", CubeListBuilder.create().texOffs(79, 48).addBox(0.0F, -1.5F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 1.0F, 1.0F));

		PartDefinition nail6 = finger6.addOrReplaceChild("nail6", CubeListBuilder.create().texOffs(7, 45).addBox(0.0F, 0.0F, -0.5F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -1.0F, 0.5F));

		PartDefinition arm3 = arms.addOrReplaceChild("arm3", CubeListBuilder.create().texOffs(0, 50).addBox(-9.5F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 8).addBox(-8.5F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, -27.0F, -7.0F, 0.0F, -2.2689F, 0.0F));

		PartDefinition elbow3 = arm3.addOrReplaceChild("elbow3", CubeListBuilder.create().texOffs(12, 42).addBox(-11.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 4).addBox(-10.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.5F, 0.0F, 0.0F));

		PartDefinition forarm3 = elbow3.addOrReplaceChild("forarm3", CubeListBuilder.create().texOffs(0, 42).addBox(-12.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-10.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.0F, 0.0F, 0.0F));

		PartDefinition hand3 = forarm3.addOrReplaceChild("hand3", CubeListBuilder.create().texOffs(72, 42).addBox(-2.0F, -1.5F, -1.75F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(72, 54).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0F, 0.0F, 0.0F));

		PartDefinition finger7 = hand3.addOrReplaceChild("finger7", CubeListBuilder.create().texOffs(79, 42).addBox(-4.0F, -1.5F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -0.5F, -1.0F));

		PartDefinition nail7 = finger7.addOrReplaceChild("nail7", CubeListBuilder.create().texOffs(7, 44).addBox(-4.0F, 0.0F, -0.5F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -1.0F, -0.5F));

		PartDefinition finger8 = hand3.addOrReplaceChild("finger8", CubeListBuilder.create().texOffs(72, 78).addBox(-4.0F, -0.5F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 1.5F, -1.0F));

		PartDefinition nail8 = finger8.addOrReplaceChild("nail8", CubeListBuilder.create().texOffs(7, 43).addBox(-4.0F, 0.0F, -0.5F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, 0.0F, -0.5F));

		PartDefinition finger9 = hand3.addOrReplaceChild("finger9", CubeListBuilder.create().texOffs(72, 76).addBox(-4.0F, -1.5F, 0.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 1.0F, 1.0F));

		PartDefinition nail9 = finger9.addOrReplaceChild("nail9", CubeListBuilder.create().texOffs(7, 42).addBox(-4.0F, 0.0F, -0.5F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -1.0F, 0.5F));

		PartDefinition shroom = mainMushroom.addOrReplaceChild("shroom", CubeListBuilder.create().texOffs(84, 74).addBox(-6.0F, -44.0F, -6.0F, 12.0F, 34.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 86).addBox(-8.0F, -44.0F, -8.0F, 16.0F, 10.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));

		PartDefinition cap = shroom.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 34).addBox(-12.0F, -44.0F, -11.0F, 24.0F, 2.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-14.0F, -50.0F, -13.0F, 28.0F, 6.0F, 28.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-12.0F, -52.0F, -11.0F, 24.0F, 2.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -1.0F));

		PartDefinition base = mainMushroom.addOrReplaceChild("base", CubeListBuilder.create().texOffs(76, 40).addBox(-10.0F, -10.0F, -10.0F, 20.0F, 10.0F, 20.0F, new CubeDeformation(0.0F))
		.texOffs(84, 0).addBox(-9.0F, -13.0F, -9.0F, 18.0F, 3.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}
	@Override
	public void setupAnimation(Level level, float partialTicks, FungalImplantationPylonAnimContext ctx) {
		this.getRoot().getAllParts().forEach(ModelPart::resetPose);
		AnimationState state = ctx.state();
		float time = (float) level.getGameTime() + partialTicks;
		AnimationHelper.animate(this, state, wiggle, time, 1F);
		float animTime = AnimationHelper.getElapsedSeconds(wiggle, state.getAccumulatedTime());

	}

	@Override
	public ModelPart getRoot() {
		return this.root;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public static final AnimationDefinition wiggle = AnimationDefinition.Builder.withLength(2).looping()
			.addAnimation("arm3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 35.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-5.6985F, 0.0F, 52.9879F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 35.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("elbow3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-5.6985F, 0.0F, -2.0121F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("forarm3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(11.397F, 0.0F, 5.0302F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("arm2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 35.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9583F, KeyframeAnimations.degreeVec(-5.6985F, 0.0F, 52.9879F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 35.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("elbow2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-5.6985F, 0.0F, -2.0121F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("forarm2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(11.397F, 0.0F, 5.0302F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("arm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -35.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9583F, KeyframeAnimations.degreeVec(5.6985F, -14.0F, -52.9879F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -35.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("elbow", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(-5.6985F, 0.0F, -2.0121F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("forarm", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(11.397F, 0.0F, 5.0302F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("hand", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("finger2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, -30.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("nail2", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 57.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("finger1", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, -30.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("nail1", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 57.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("finger3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 30.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.addAnimation("nail3", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
					new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, -57.5F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
				))
				.build();
}