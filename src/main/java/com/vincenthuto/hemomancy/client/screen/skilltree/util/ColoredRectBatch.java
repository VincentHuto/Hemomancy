package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

/** Collects GUI-colored quads in the current GUI buffer and ends them with one flush. */
public final class ColoredRectBatch {
	private final GuiGraphics graphics;
	private final Matrix4f pose;
	private final VertexConsumer buffer;

	public ColoredRectBatch(GuiGraphics graphics) {
		this.graphics = graphics;
		this.pose = graphics.pose().last().pose();
		this.buffer = graphics.bufferSource().getBuffer(RenderType.gui());
	}

	public void fill(int left, int top, int right, int bottom, int color) {
		if (right <= left || bottom <= top) return;
		buffer.addVertex(pose, right, top, 0.0F).setColor(color);
		buffer.addVertex(pose, left, top, 0.0F).setColor(color);
		buffer.addVertex(pose, left, bottom, 0.0F).setColor(color);
		buffer.addVertex(pose, right, bottom, 0.0F).setColor(color);
	}

	public void flush() {
		graphics.flush();
	}
}
