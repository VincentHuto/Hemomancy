package com.vincenthuto.hemomancy.client.model.item.book;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

/**
 * Opens, closes and flips any of the {@link HemoBookLayers} books with vanilla {@code BookModel}'s inputs and opening
 * curve. Each board turns on the spine line with its half of the text block; the spine's back slides back and
 * narrows so it always spans the two boards' hinge corners. The {@code *_closed} fittings show only while the book is
 * shut and the {@code *_open} ones only while it is open.
 */
public final class HemoBookModel extends Model {
	/** Below this open amount the clasp is still fastened. */
	private static final float FASTENED = 0.15F;

	private final ModelPart root;
	private final ModelPart leftLid;
	private final ModelPart rightLid;
	private final ModelPart leftPages;
	private final ModelPart rightPages;
	private final ModelPart spine;
	/** Half the shut book's thickness at the spine: where each board's hinge corner sits, in model units. */
	private final float spineReach;
	private final ModelPart flipPage1;
	private final ModelPart flipPage2;
	private final ModelPart leftClosed;
	private final ModelPart leftOpen;
	private final ModelPart rightClosed;
	private final ModelPart rightOpen;

	public HemoBookModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
		this.leftLid = root.getChild("left_lid");
		this.rightLid = root.getChild("right_lid");
		this.leftPages = root.getChild("left_pages");
		this.rightPages = root.getChild("right_pages");
		this.spine = root.getChild("spine");
		float reach = 0;
		for (ModelPart.Cube cube : spine.cubes) reach = Math.max(reach, cube.maxZ);
		this.spineReach = reach;
		this.flipPage1 = root.getChild("flip_page1");
		this.flipPage2 = root.getChild("flip_page2");
		this.leftClosed = leftLid.getChild("left_closed");
		this.leftOpen = leftLid.getChild("left_open");
		this.rightClosed = rightLid.getChild("right_closed");
		this.rightOpen = rightLid.getChild("right_open");
	}

	/**
	 * @param time        animation time in ticks; drives the slight breathing of an open book
	 * @param rightFlip   0 to 1, how far the first loose leaf has crossed from the left side to the right
	 * @param leftFlip    the same for the second leaf
	 * @param open        0 shut, 1 fully open
	 */
	public void setupAnim(float time, float rightFlip, float leftFlip, float open) {
		float angle = (Mth.sin(time * 0.02F) * 0.1F + 1.25F) * open;
		leftLid.yRot = angle;
		leftPages.yRot = angle;
		rightLid.yRot = -angle;
		rightPages.yRot = -angle;
		// A board's hinge corner at (0, reach) swings to (-reach sin, reach cos); keep the spine's back between them.
		spine.x = -spineReach * Mth.sin(angle);
		spine.zScale = Mth.cos(angle);
		flipPage1.yRot = angle - angle * 2.0F * rightFlip;
		flipPage2.yRot = angle - angle * 2.0F * leftFlip;
		boolean fastened = open < FASTENED;
		leftClosed.visible = fastened;
		rightClosed.visible = fastened;
		leftOpen.visible = !fastened;
		rightOpen.visible = !fastened;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
