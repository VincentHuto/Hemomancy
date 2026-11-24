package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.entity.mob.aquatic.PrismCuttleEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.PrismCuttleRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class PrismCuttleModel extends HierarchicalModel<PrismCuttleEntity> {
	private static final String[] PANELS = {
			"shoulder_root", "fore_web", "broad_web", "aft_web", "taper_web", "tip_web"
	};
	private static final String[] ARMS = {
			"upper_inner", "upper_outer", "lower_outer", "lower_inner"
	};

	private final ModelPart root = PrismCuttleMesh.createRoot();
	private final ModelPart mantle = root.getChild("mantle");
	private final Side left = new Side(mantle, root.getChild("head").getChild("arms"), "left", -1.0F);
	private final Side right = new Side(mantle, root.getChild("head").getChild("arms"), "right", 1.0F);

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(PrismCuttleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
		float flashTicks = entity.flashTicksRemaining() - partialTick;
		pose(ageInTicks, limbSwingAmount, entity.isInWater() ? headPitch : 0.0F, flashTicks);
	}

	void pose(float age, float swimAmount, float pitch, float flashTicks) {
		root.getAllParts().forEach(ModelPart::resetPose);
		float flash = HemoEasing.smoothstep((PrismCuttleRules.FLASH_TICKS + 1.0F - flashTicks) / 3.0F)
				* HemoEasing.smoothstep(flashTicks / 9.0F);
		float stroke = Mth.clamp(swimAmount * 1.3F, 0.0F, 1.0F);
		float breath = Mth.sin(age * 0.19F);
		root.xRot += Mth.clamp(pitch, -24.0F, 24.0F) * Mth.DEG_TO_RAD;
		root.y += breath * 0.12F;
		mantle.xScale = 1.0F + breath * 0.025F + flash * 0.09F;
		mantle.yScale = 1.0F + breath * 0.035F + flash * 0.13F;
		left.animate(age, stroke, flash);
		right.animate(age, stroke, flash);
	}

	private static final class Side {
		private final float direction;
		private final ModelPart[] panels = new ModelPart[PANELS.length];
		private final ModelPart[] arms = new ModelPart[ARMS.length];
		private final ModelPart[] curls = new ModelPart[ARMS.length];
		private final ModelPart feeder;
		private final ModelPart club;

		private Side(ModelPart mantle, ModelPart crown, String name, float direction) {
			this.direction = direction;
			String prefix = name + "_";
			ModelPart fin = mantle.getChild(prefix + "fin");
			for (int i = 0; i < PANELS.length; i++) {
				panels[i] = fin.getChild(prefix + PANELS[i]);
			}
			for (int i = 0; i < ARMS.length; i++) {
				String arm = prefix + ARMS[i];
				arms[i] = crown.getChild(arm);
				curls[i] = arms[i].getChild(arm + "_curl");
			}
			feeder = crown.getChild(prefix + "feeding_tentacle");
			club = feeder.getChild(prefix + "tentacle_club");
		}

		private void animate(float age, float stroke, float flash) {
			// Delay the same stroke toward the tail, keeping each panel rooted in the mantle.
			float phase = age * 0.36F;
			float finStroke = 0.13F + stroke * 0.08F;
			for (int i = 0; i < panels.length; i++) {
				panels[i].zRot += direction * (Mth.sin(phase - i * 0.61F + direction * 0.22F) * finStroke
						- flash * 0.17F);
			}
			for (int i = 0; i < arms.length; i++) {
				float drift = age * 0.22F - i * 0.66F + direction * 0.42F;
				float spread = i < 2 ? -1.0F : 1.0F;
				arms[i].xRot += Mth.sin(drift) * (0.07F + stroke * 0.05F) + spread * flash * 0.20F;
				arms[i].yRot -= direction * (Mth.cos(drift) * 0.045F + flash * 0.17F);
				curls[i].xRot += Mth.sin(drift - 0.80F) * 0.17F + spread * flash * 0.15F;
			}
			float feelerWave = age * 0.19F + direction * 0.58F;
			feeder.xRot += Mth.sin(feelerWave) * 0.095F - flash * 0.10F;
			feeder.yRot -= direction * (Mth.cos(feelerWave) * 0.07F + flash * 0.18F);
			club.xRot += Mth.sin(feelerWave - 0.85F) * 0.18F + flash * 0.17F;
		}
	}
}
