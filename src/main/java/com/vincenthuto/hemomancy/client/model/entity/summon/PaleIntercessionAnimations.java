package com.vincenthuto.hemomancy.client.model.entity.summon;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class PaleIntercessionAnimations {
	private PaleIntercessionAnimations() { }
	private static AnimationChannel rotation(float end, float x, float y, float z) {
		return new AnimationChannel(AnimationChannel.Targets.ROTATION,
				new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(end, KeyframeAnimations.degreeVec(x, y, z), AnimationChannel.Interpolations.CATMULLROM));
	}
	private static AnimationChannel position(float end, float x, float y, float z) {
		return new AnimationChannel(AnimationChannel.Targets.POSITION,
				new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(end, KeyframeAnimations.posVec(x, y, z), AnimationChannel.Interpolations.CATMULLROM));
	}
	public static final AnimationDefinition MANIFEST = AnimationDefinition.Builder.withLength(1f)
			.addAnimation("whole", position(1f, 0, -2, 0)).addAnimation("halo", rotation(1f, 0, 180, 0)).build();
	public static final AnimationDefinition STILL = AnimationDefinition.Builder.withLength(2f).looping()
			.addAnimation("whole", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1, KeyframeAnimations.posVec(0, -0.8f, 0), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM))).build();
	public static final AnimationDefinition GLIDE = AnimationDefinition.Builder.withLength(1f).looping()
			.addAnimation("robe", rotation(1f, -6, 0, 0)).build();
	public static final AnimationDefinition INTERPOSE = AnimationDefinition.Builder.withLength(.5f)
			.addAnimation("left_arm", rotation(.5f, -55, 0, -12)).addAnimation("right_arm", rotation(.5f, -55, 0, 12)).build();
	public static final AnimationDefinition STRIKE = AnimationDefinition.Builder.withLength(.6f)
			.addAnimation("right_arm", rotation(.35f, -92, 0, 0)).addAnimation("whole", rotation(.6f, 0, -8, 0)).build();
	public static final AnimationDefinition DISSOLVE = AnimationDefinition.Builder.withLength(1f)
			.addAnimation("whole", position(1f, 0, 4, 0)).addAnimation("halo", rotation(1f, 0, -180, 0)).build();
	public static final AnimationDefinition DISTORT = AnimationDefinition.Builder.withLength(.4f)
			.addAnimation("whole", rotation(.2f, 0, 0, 5)).addAnimation("veil", rotation(.4f, 0, 0, -7)).build();
}
