package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.entity.mob.aquatic.ChalybeateSnailEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class ChalybeateSnailModel extends HierarchicalModel<ChalybeateSnailEntity> {
	private final ModelPart root = ChalybeateSnailMesh.createRoot();
	private final ModelPart foot = root.getChild("creeping_foot");
	private final ModelPart shell = root.getChild("coiled_shell");
	private final ModelPart head = foot.getChild("head");
	private final ModelPart leftTentacle = head.getChild("left_cephalic_tentacle");
	private final ModelPart rightTentacle = head.getChild("right_cephalic_tentacle");
	private final ModelPart leftPalp = head.getChild("left_oral_tentacle");
	private final ModelPart rightPalp = head.getChild("right_oral_tentacle");
	private final ModelPart sclerites = foot.getChild("iron_sulfide_sclerites");
	private final ModelPart leftSkirt = sclerites.getChild("left_sclerite_skirt");
	private final ModelPart rightSkirt = sclerites.getChild("right_sclerite_skirt");
	private final ModelPart tailFan = sclerites.getChild("rear_sclerite_fan");

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(ChalybeateSnailEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
		float climb = entity.climbPose().amount(partialTick);
		pose(ageInTicks, limbSwingAmount, climb, netHeadYaw, entity.withdrawal(partialTick));
	}

	void pose(float ageInTicks, float limbSwingAmount, float climb, float netHeadYaw, float withdrawal) {
		root.getAllParts().forEach(ModelPart::resetPose);
		float tuck = HemoEasing.smoothstep(withdrawal);
		float exposed = 1.0F - tuck;
		float crawl = Mth.clamp(limbSwingAmount * 3.0F + climb * 0.55F, 0.0F, 1.0F) * exposed;
		float phase = ageInTicks * 0.29F;
		float wave = Mth.sin(phase) * crawl;

		// The muscular sole lengthens over the substrate while the heavy shell barely rocks.
		foot.xScale = 1.0F - tuck * 0.13F - wave * 0.025F;
		foot.zScale = 1.0F - tuck * 0.47F + wave * 0.065F;
		foot.yScale = 1.0F - tuck * 0.14F;
		foot.z += tuck * 0.9F;
		shell.xRot += wave * 0.022F;
		shell.zRot += Mth.sin(phase * 0.5F) * 0.018F * crawl;
		shell.y += tuck * 2.2F - wave * 0.12F;
		shell.yScale = 1.0F - tuck * 0.08F;

		head.z += 5.1F * tuck - wave * 0.45F;
		head.y += 0.45F * tuck;
		head.yRot += Mth.clamp(netHeadYaw, -20.0F, 20.0F) * Mth.DEG_TO_RAD * exposed * (1.0F - climb);
		head.xRot += wave * 0.045F;
		float feelerTuck = HemoEasing.smoothstep(withdrawal * 1.75F);
		probe(leftTentacle, ageInTicks, 0.0F, -1.0F, feelerTuck);
		probe(rightTentacle, ageInTicks, 1.6F, 1.0F, feelerTuck);
		probe(leftPalp, ageInTicks, 0.8F, -1.0F, feelerTuck);
		probe(rightPalp, ageInTicks, 2.5F, 1.0F, feelerTuck);

		leftSkirt.zRot += Mth.sin(phase - 1.0F) * crawl * 0.075F + tuck * 0.17F;
		rightSkirt.zRot -= Mth.sin(phase - 1.5F) * crawl * 0.075F + tuck * 0.17F;
		tailFan.xRot += Mth.sin(phase - 2.1F) * crawl * 0.085F - tuck * 0.18F;
	}

	private static void probe(ModelPart feeler, float age, float phase, float side, float tuck) {
		float reach = 1.0F - tuck;
		feeler.xRot += Mth.sin(age * 0.135F + phase) * 0.11F * reach - tuck * 0.48F;
		feeler.yRot += Mth.sin(age * 0.1F + phase) * 0.13F * reach + side * tuck * 0.35F;
		feeler.yScale = 0.22F + 0.78F * reach;
		feeler.zScale = 0.22F + 0.78F * reach;
	}
}
