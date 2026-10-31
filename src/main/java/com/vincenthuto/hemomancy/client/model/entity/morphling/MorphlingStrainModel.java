package com.vincenthuto.hemomancy.client.model.entity.morphling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingActiveState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A generated strain attachment (see {@code tools/model_export/morphling_strains.py}). The part table says when each
 * part grows, which limb segment it belongs to and how it moves; {@link #pose} applies that for a frame and
 * {@link #draw} renders the skin pass, then the glow pass in its pulse groups. The model draws in the space of the
 * part it is anchored to, so the player layer and Track 9's NPC rig can both wear it.
 */
public abstract class MorphlingStrainModel extends Model {
	/** How the glow texture lights. */
	public enum Glow {
		NONE, SUNLIGHT, VEIN_PULSE, SAIL_FLUSH, BREATH, FLAME
	}

	private final ModelPart root;
	private final List<MorphlingPartSpec> specs;
	private final ModelPart[] parts;
	private final Glow glow;
	private final int glowGroups;
	private final RenderType skinType;
	@Nullable
	private final RenderType glowType;

	protected MorphlingStrainModel(ModelPart root, List<MorphlingPartSpec> specs, ResourceLocation texture,
			@Nullable ResourceLocation glowTexture, Glow glow, boolean translucent) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
		this.specs = specs;
		this.parts = new ModelPart[specs.size()];
		Map<String, ModelPart> byName = new HashMap<>();
		byName.put("root", root);
		for (int i = 0; i < specs.size(); i++) {
			MorphlingPartSpec spec = specs.get(i);
			ModelPart part = byName.get(spec.parent()).getChild(spec.name());
			byName.put(spec.name(), part);
			parts[i] = part;
		}
		this.glow = glowTexture == null ? Glow.NONE : glow;
		this.glowGroups = specs.stream().mapToInt(MorphlingPartSpec::glowGroup).max().orElse(0) + 1;
		this.skinType = translucent ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture);
		this.glowType = glowTexture == null ? null : RenderType.entityTranslucentEmissive(glowTexture);
	}

	public List<MorphlingPartSpec> specs() {
		return specs;
	}

	/** Grows, hides and moves every part for this frame. */
	public void pose(MorphlingAnimContext ctx) {
		for (int i = 0; i < parts.length; i++) {
			ModelPart part = parts[i];
			MorphlingPartSpec spec = specs.get(i);
			part.resetPose();
			part.skipDraw = false;
			part.visible = ctx.maturity() >= spec.stage();
			if (!part.visible) continue;
			float scale = spec.grows() ? MorphlingStrainMotion.growth(spec.stage(), ctx.maturity()) : 1.0F;
			float yScale = 1.0F, zScale = 1.0F;
			float t = ctx.time();
			float a = spec.amp(), w = spec.speed(), p = spec.phase();
			switch (spec.motion()) {
				case NONE -> {
				}
				case SWAY -> {
					part.xRot += a * Mth.sin(t * w + p);
					part.zRot += a * 0.6F * Mth.sin(t * w * 0.77F + p * 1.3F + 1.1F);
				}
				case WAG -> part.yRot += a * Mth.sin(t * w + p);
				case RIPPLE -> {
					part.xRot += a * Mth.sin(t * w - p);
					part.zRot += a * 0.35F * Mth.sin(t * w * 0.6F - p + 0.8F);
				}
				case FLAME -> {
					float kindled = ctx.has(MorphlingActiveState.KINDLED) ? 1.5F : 1.0F;
					part.xRot += a * kindled * (Mth.sin(t * w - p) + 0.35F * Mth.sin(t * w * 2.7F + p * 1.9F));
					part.zRot += a * 0.4F * Mth.sin(t * w * 1.3F - p);
				}
				case FROND -> {
					float open = MorphlingStrainMotion.frondOpen(ctx.daylight(), ctx.settled(spec.flags()));
					part.xRot += -a * open + 0.04F * Mth.sin(t * 0.05F + p);
					part.zRot *= 0.75F + 0.5F * open;
				}
				case BREATHE -> scale *= 1.0F + a * MorphlingStrainMotion.breath(t, p);
				case SAIL -> {
					float raised = ctx.settled(spec.flags());
					part.yRot += a * (1.0F - raised);
					zScale = 0.6F + 0.4F * raised;
				}
				case POUCH -> scale *= (0.65F + 0.55F * ctx.fill()) * (1.0F + 0.03F * Mth.sin(t * 0.1F + p));
				case DRIP -> yScale = 0.5F + 1.2F * MorphlingStrainMotion.drip(t, w, p);
				case GROW_ON -> {
					float grown = ctx.settled(spec.flags());
					if (grown < 0.02F) {
						part.visible = false;
						continue;
					}
					scale *= grown;
				}
				case SPLAY_ON -> {
					float splay = ctx.settled(spec.flags());
					part.zRot += a * splay;
					part.xRot += a * 0.4F * splay;
				}
			}
			part.xScale = scale;
			part.yScale = scale * yScale;
			part.zScale = scale * zScale;
		}
	}

	/** The skin pass, then the glow pass. Call after {@link #pose}, in the anchor part's space. */
	public void draw(PoseStack poseStack, MultiBufferSource buffers, int packedLight, MorphlingAnimContext ctx) {
		drawFiltered(poseStack, buffers, packedLight, ctx, null);
	}

	/**
	 * Skin and glow for the parts on one limb segment only, for a jointed rig that bends between them (Track 9's Court
	 * members draw the UPPER parts on the arm or thigh and the LOWER ones on the forearm or shin).
	 */
	public void drawLimbSegment(PoseStack poseStack, MultiBufferSource buffers, int packedLight, MorphlingAnimContext ctx,
			MorphlingPartSpec.Limb limb) {
		drawFiltered(poseStack, buffers, packedLight, ctx, limb);
	}

	private void drawFiltered(PoseStack poseStack, MultiBufferSource buffers, int packedLight, MorphlingAnimContext ctx,
			@Nullable MorphlingPartSpec.Limb limb) {
		for (int i = 0; i < parts.length; i++) parts[i].skipDraw = filtered(i, limb);
		root.render(poseStack, buffers.getBuffer(skinType), packedLight, OverlayTexture.NO_OVERLAY);
		if (glowType != null) {
			VertexConsumer glowing = buffers.getBuffer(glowType);
			if (glow == Glow.VEIN_PULSE || glow == Glow.FLAME) {
				for (int group = 0; group < glowGroups; group++) {
					for (int i = 0; i < parts.length; i++) {
						parts[i].skipDraw = filtered(i, limb) || specs.get(i).glowGroup() != group;
					}
					renderGlow(poseStack, glowing, glowAlpha(ctx, group));
				}
			} else {
				renderGlow(poseStack, glowing, glowAlpha(ctx, 0));
			}
		}
		for (ModelPart part : parts) part.skipDraw = false;
	}

	private boolean filtered(int index, @Nullable MorphlingPartSpec.Limb limb) {
		return limb != null && specs.get(index).limb() != limb;
	}

	private void renderGlow(PoseStack poseStack, VertexConsumer consumer, float alpha) {
		if (alpha <= 0.01F) return;
		int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
		root.render(poseStack, consumer, 0xF000F0, OverlayTexture.NO_OVERLAY, (a << 24) | 0xFFFFFF);
	}

	private float glowAlpha(MorphlingAnimContext ctx, int group) {
		float t = ctx.time();
		boolean home = ctx.has(MorphlingActiveState.HOME);
		return switch (glow) {
			case NONE -> 0.0F;
			case SUNLIGHT -> {
				float open = ctx.settled(MorphlingActiveState.LANTERN_FRONDS | MorphlingActiveState.BLOOM);
				yield Mth.clamp(0.15F + 0.65F * ctx.daylight() + 0.5F * open + (home ? 0.15F : 0.0F)
						+ 0.08F * Mth.sin(t * 0.05F), 0.0F, 1.0F);
			}
			case VEIN_PULSE -> MorphlingStrainMotion.veinPulse(t, group) * (home ? 1.0F : 0.8F);
			case SAIL_FLUSH -> 0.18F + 0.82F * ctx.settled(MorphlingActiveState.SAIL_RAISED)
					+ 0.06F * Mth.sin(t * 0.21F);
			case BREATH -> 0.12F + 0.5F * MorphlingStrainMotion.breath(t, 0.0F)
					+ (ctx.has(MorphlingActiveState.FOLD_SHUT | MorphlingActiveState.CRYPTOBIOSIS) ? 0.3F : 0.0F);
			case FLAME -> MorphlingStrainMotion.flame(t, group, ctx.has(MorphlingActiveState.KINDLED));
		};
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color) {
		root.render(poseStack, consumer, packedLight, packedOverlay, color);
	}
}
