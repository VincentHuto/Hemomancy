package com.vincenthuto.hemomancy.client.render.entity.npc.court;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingAnimContext;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingPartSpec;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingStrainModel;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingStrainModels;
import com.vincenthuto.hemomancy.client.model.entity.npc.court.CourtHumanoidModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.court.CourtMemberModel;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingActiveState;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A Court member's house strain at its role's stage, on the jointed rig: Track 3's attachment models, fed from the
 * role instead of an equipped item. This is the anchor adapter. HEAD and BODY growths sit on the head and torso as on
 * the player; on the arms and legs, UPPER growths ride the upper arm or thigh and LOWER growths (Holdfast roots,
 * Marrowplume clubs, Purse finlets) ride the forearm or shin, so they bend with the elbow and knee. The right side is
 * the left side mirrored, as on the player.
 */
public class CourtStrainLayer extends RenderLayer<CourtMemberEntity, CourtMemberModel> {
	private final EntityModelSet models;
	private final Map<MorphlingStrainModels.Anchor, MorphlingStrainModel> baked = new IdentityHashMap<>();

	public CourtStrainLayer(RenderLayerParent<CourtMemberEntity, CourtMemberModel> parent, EntityRendererProvider.Context context) {
		super(parent);
		this.models = context.getModelSet();
	}

	/**
	 * The active bits a role shows: the Lightkeeper's open lantern fronds, an arbor holding (until it withdraws), a
	 * Braced elder's anchor strands spread into the stone (until it lets go), an Edge-swimmer's sail raised while
	 * it swims, a Lamp-colony anchored in the network (until it lets go to speak), a Veiled resident folded away while
	 * still, and a Sleeper sealed in its bell.
	 */
	public static int flags(CourtMemberEntity entity) {
		CourtRole role = entity.role();
		int flags = MorphlingActiveState.HOME;
		if (role == CourtRole.LIGHTKEEPER) flags |= MorphlingActiveState.LANTERN_FRONDS | MorphlingActiveState.STILL;
		if (entity instanceof com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenEntity warden) {
			// The Bloom Warden holds its canopy open only while rooted among its blooms.
			return warden.phase() == com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules.Phase.ROOTED
					? flags | MorphlingActiveState.ARBOR : flags;
		}
		if (role == CourtRole.ARBOR && !entity.isWithdrawn()) flags |= MorphlingActiveState.ARBOR | MorphlingActiveState.STILL;
		if (role == CourtRole.BRACED_ELDER && !entity.isWithdrawn()) flags |= MorphlingActiveState.ANCHORED | MorphlingActiveState.STILL;
		if (role == CourtRole.EDGE_SWIMMER && CourtHumanoidModel.swimming(entity)) flags |= MorphlingActiveState.SAIL_RAISED;
		if (role == CourtRole.LAMP_COLONY && !entity.isWithdrawn() && entity.talkAmount(0.0F) <= 0.0F) {
			flags |= MorphlingActiveState.ANCHORED | MorphlingActiveState.STILL;
		}
		if (role == CourtRole.VEILED_RESIDENT && entity.isWithdrawn()) flags |= MorphlingActiveState.FOLDED_AWAY | MorphlingActiveState.STILL;
		if (role == CourtRole.SLEEPER) flags |= MorphlingActiveState.CRYPTOBIOSIS | MorphlingActiveState.STILL;
		return flags;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, CourtMemberEntity entity,
			float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible() || CourtMemberRenderer.folded(entity) || !(getParentModel() instanceof CourtHumanoidModel rig)) return;
		CourtRole role = entity.role();
		MorphlingAnimContext ctx = MorphlingAnimContext.of(entity, role.stage(), flags(entity), ageInTicks);
		for (MorphlingStrainModels.Anchor anchor : MorphlingStrainModels.forStrain(role.strain().id())) {
			if (role.stage() < anchor.visibleFrom()) continue;
			MorphlingStrainModel model = baked.computeIfAbsent(anchor, a -> a.factory().apply(models.bakeLayer(a.layer())));
			model.pose(ctx);
			switch (anchor.point()) {
				case HEAD -> on(poseStack, rig::toHead, false, () -> model.draw(poseStack, buffers, packedLight, ctx));
				case BODY -> on(poseStack, rig::toBody, false, () -> model.draw(poseStack, buffers, packedLight, ctx));
				case ARMS -> {
					for (boolean right : new boolean[] {true, false}) {
						on(poseStack, ps -> rig.toArm(ps, right), right, () -> model.drawLimbSegment(poseStack, buffers,
								packedLight, ctx, MorphlingPartSpec.Limb.UPPER));
						on(poseStack, ps -> rig.toForearm(ps, right), right, () -> model.drawLimbSegment(poseStack, buffers,
								packedLight, ctx, MorphlingPartSpec.Limb.LOWER));
					}
				}
				case LEGS -> {
					for (boolean right : new boolean[] {true, false}) {
						on(poseStack, ps -> rig.toLeg(ps, right), right, () -> model.drawLimbSegment(poseStack, buffers,
								packedLight, ctx, MorphlingPartSpec.Limb.UPPER));
						on(poseStack, ps -> rig.toShin(ps, right), right, () -> model.drawLimbSegment(poseStack, buffers,
								packedLight, ctx, MorphlingPartSpec.Limb.LOWER));
					}
				}
			}
		}
	}

	private static void on(PoseStack poseStack, Consumer<PoseStack> segment, boolean mirror, Runnable draw) {
		poseStack.pushPose();
		segment.accept(poseStack);
		if (mirror) poseStack.scale(-1.0F, 1.0F, 1.0F);
		draw.run();
		poseStack.popPose();
	}
}
