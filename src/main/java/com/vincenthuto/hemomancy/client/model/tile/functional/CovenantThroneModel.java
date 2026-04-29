package com.vincenthuto.hemomancy.client.model.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

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

/**
 * Model for the Covenant Throne — four black rectangles assembled into a
 * throne silhouette:
 * <ul>
 *   <li><b>Seat</b> — short, wide cube at the base (the sitting surface).</li>
 *   <li><b>Backrest</b> — tall, narrow slab at the rear.</li>
 *   <li><b>Left armrest</b> — medium-height side slab on the left.</li>
 *   <li><b>Right armrest</b> — medium-height side slab on the right.</li>
 * </ul>
 *
 * <p>All parts share the same offset so the throne sits flush with the base
 * block (Y=0 in world space) and reaches into the second block above. The
 * model is designed for <em>SOUTH</em> facing (front opens toward −Z); the
 * renderer applies a Y-axis rotation to handle other facing directions.</p>
 *
 * <p>Texture layout fits within 64×64: seat at (0,0), backrest at (0,16),
 * armrests at (0,41) and (26,41).  Because the throne is rendered entirely
 * black the precise texture sample is not visible.</p>
 */
public class CovenantThroneModel extends Model {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("modelcovenantthrone"), "main");

	private final ModelPart seat;
	private final ModelPart backrest;
	private final ModelPart armLeft;
	private final ModelPart armRight;

	public CovenantThroneModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.seat     = root.getChild("seat");
		this.backrest = root.getChild("backrest");
		this.armLeft  = root.getChild("armLeft");
		this.armRight = root.getChild("armRight");
	}

	/**
	 * Builds the layer definition.
	 *
	 * <p>All four parts share {@link PartPose#offset(float, float, float) offset(0, 24, 0)}
	 * so that Y=0 in box space maps to ground level after the 180° X-flip applied
	 * by the renderer, matching the convention used by
	 * {@link SanguineMonolithModel}.</p>
	 */
	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		// ── Seat: 10W × 6H × 10D, centred at base ──────────────────────────────
		root.addOrReplaceChild("seat",
				CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-5.0F, -6.0F, -5.0F, 10.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// ── Backrest: 12W × 22H × 3D, at back (positive Z = north of block) ────
		root.addOrReplaceChild("backrest",
				CubeListBuilder.create()
						.texOffs(0, 16)
						.addBox(-6.0F, -28.0F, 3.0F, 12.0F, 22.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// ── Left armrest: 3W × 12H × 10D, on −X side ───────────────────────────
		root.addOrReplaceChild("armLeft",
				CubeListBuilder.create()
						.texOffs(0, 41)
						.addBox(-8.0F, -18.0F, -5.0F, 3.0F, 12.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// ── Right armrest: 3W × 12H × 10D, on +X side ──────────────────────────
		root.addOrReplaceChild("armRight",
				CubeListBuilder.create()
						.texOffs(26, 41)
						.addBox(5.0F, -18.0F, -5.0F, 3.0F, 12.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int packedColor) {
		seat.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		backrest.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		armLeft.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		armRight.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
