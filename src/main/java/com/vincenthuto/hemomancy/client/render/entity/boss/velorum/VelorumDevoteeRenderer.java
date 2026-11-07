package com.vincenthuto.hemomancy.client.render.entity.boss.velorum;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumDevoteeModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumDevoteeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

/**
 * A woken devotee: the generated jointed body in the frozen devotee's robe, posed by its Java tracks. The frost lines
 * in its folds glow faintly while it is still rimed (thawing, stilled, kneeling home); a hostile one keeps the rime and
 * two pale eye glints, as the witness devotees have; an ally's light goes out as the rime falls from its face.
 */
public class VelorumDevoteeRenderer extends MobRenderer<VelorumDevoteeEntity, VelorumDevoteeModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/velorum/velorum_devotee.png");
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/boss/velorum/velorum_devotee_glow.png"));
	private static final float SHADOW = 0.45F;

	public VelorumDevoteeRenderer(EntityRendererProvider.Context context) {
		super(context, new VelorumDevoteeModel(context.bakeLayer(VelorumDevoteeModel.LAYER_LOCATION)), SHADOW);
		addLayer(new Rime(this));
	}

	@Override
	public ResourceLocation getTextureLocation(VelorumDevoteeEntity entity) {
		return TEXTURE;
	}

	@Override
	protected float getFlipDegrees(VelorumDevoteeEntity entity) {
		return 0.0F;
	}

	/** The glow pass: strong while rimed or hostile, faint on an awake ally. */
	private static final class Rime extends RenderLayer<VelorumDevoteeEntity, VelorumDevoteeModel> {
		private static final float RIMED = 0.6F;
		private static final float ALLY = 0.12F;

		Rime(RenderLayerParent<VelorumDevoteeEntity, VelorumDevoteeModel> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack poses, MultiBufferSource buffers, int light, VelorumDevoteeEntity entity, float limbSwing,
				float limbAmount, float partialTick, float age, float yaw, float pitch) {
			if (entity.isInvisible()) return;
			boolean rimed = entity.hostile() || entity.state() != VelorumDevoteeEntity.State.AWAKE;
			// the eyes render type is additive and ignores alpha: the strength scales the colour
			float s = rimed ? RIMED : ALLY;
			int color = FastColor.ARGB32.color(255, (int) (214 * s), (int) (236 * s), (int) (255 * s));
			getParentModel().renderToBuffer(poses, buffers.getBuffer(GLOW), 0x00F000F0, OverlayTexture.NO_OVERLAY, color);
		}
	}
}
