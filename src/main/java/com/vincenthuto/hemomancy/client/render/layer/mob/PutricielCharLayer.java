package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

/**
 * The growths burned: the model again with {@code putriciel_charred.png} (charcoal caps, alligator cracks, pale ash
 * rims; the hem and lower robe scorched, the vestment remnants scorched only at their edges; everything else clear).
 * Zero in the rot. Each growth part draws at its own char (revision R1.8): the shelves, sacs and mushrooms blacken in
 * kindle order as the Ignition's fire reaches them and clear shelf by shelf as they regrow through the Embering; the
 * cloth follows the look's char. The ending's soot is the look's.
 */
public class PutricielCharLayer<T extends PutricielEntity> extends RenderLayer<T, PutricielModel<T>> {
	private static final RenderType CHAR = RenderType.entityTranslucent(
			Hemomancy.rloc("textures/entity/boss/putriciel/putriciel_charred.png"));

	public PutricielCharLayer(RenderLayerParent<T, PutricielModel<T>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity, float limbSwing, float limbAmount,
			float partialTick, float age, float yaw, float pitch) {
		if (entity.isInvisible()) return;
		var motion = PutricielAnimState.of(entity);
		if (motion.endingTime() < 0.0F && motion.stage() == PutricielStage.ROT) return;
		getParentModel().renderCharred(poseStack, buffer.getBuffer(CHAR), light,
				LivingEntityRenderer.getOverlayCoords(entity, 0.0F), motion);
	}
}
