package com.vincenthuto.hemomancy.client.screen.radial;

import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class TextRadialMenuItem extends RadialMenuItem {
	private final Component text;
	private final int color;
	private float scale = 1.0f;
	private int maxWidth;

	public Component getText() {
		return text;
	}

	public int getColor() {
		return color;
	}

	public TextRadialMenuItem(GenericRadialMenu owner, Component text) {
		super(owner);
		this.text = text;
		this.color = 0xFFFFFFFF;
	}

	public TextRadialMenuItem(GenericRadialMenu owner, Component text, int color) {
		super(owner);
		this.text = text;
		this.color = color;
	}

	public TextRadialMenuItem setTextLayout(float scale, int maxWidth) {
		this.scale = Math.max(0.5f, Math.min(1.0f, scale));
		this.maxWidth = Math.max(0, maxWidth);
		return this;
	}

	@Override
	public void draw(DrawingContext context) {
		int wrapWidth = maxWidth > 0 ? Math.max(1, (int) (maxWidth / scale)) : Integer.MAX_VALUE;
		List<FormattedCharSequence> lines = context.font.split(text, wrapWidth);
		var pose = context.graphics.pose();
		pose.pushPose();
		pose.translate(context.x, context.y, 0);
		pose.scale(scale, scale, 1.0f);
		float top = -context.font.lineHeight * lines.size() / 2.0f;
		for (int i = 0; i < lines.size(); i++) {
			FormattedCharSequence line = lines.get(i);
			context.graphics.drawString(context.font, line, -context.font.width(line) / 2.0f,
					top + context.font.lineHeight * i, color, true);
		}
		pose.popPose();
	}

	@Override
	public void drawTooltips(DrawingContext context) {
		// nothing to do (yet)
	}
}
