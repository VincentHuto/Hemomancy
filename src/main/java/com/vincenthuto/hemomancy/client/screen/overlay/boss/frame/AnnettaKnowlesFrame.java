package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFrameContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.BossBarFramePainter;
import net.minecraft.util.Mth;

/**
 * Annetta in her broken church: weathered pew-wood rails with chipped edges and plank seams, a pointed arch at each end
 * with pale glass, ichor seeping from the lower rail, and a small black-iron reliquary under the middle.
 */
public final class AnnettaKnowlesFrame implements BossBarFrame {
	private static final int WOOD_DARK = 0xFF2A221C, WOOD = 0xFF6B5A4A, WOOD_LIP = 0xFFA8957C;
	private static final int PALE = 0xD6EAF6, ICHOR = 0xD69630;

	@Override
	public int fillTop(int phase) {
		return 0xFFB0202E;
	}

	@Override
	public int fillBottom(int phase) {
		return 0xFF5C0A14;
	}

	@Override
	public int nameColor(int phase) {
		return PALE;
	}

	@Override
	public void paintBack(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		BossBarFramePainter.rails(c, 0, WOOD_DARK, WOOD, WOOD_LIP);
		for (int i = 1; i < 8; i++) {
			float sx = c.x() - 4 + i * 24.0F + GuiCanvas.hash(3, i, 0) * 6.0F;
			g.rect(sx, c.y() + 1, sx + 1, c.y() + 2, WOOD_DARK, WOOD_DARK);
			g.rect(sx, c.y() + 11, sx + 1, c.y() + 12, WOOD_DARK, WOOD_DARK);
		}
		for (int i = 0; i < 6; i++) {
			float cx = c.x() + 8 + GuiCanvas.hash(7, i, 1) * (BossBarFrame.WIDTH - 16);
			g.tri(cx - 2.0F, c.y(), WOOD_DARK, cx + 2.0F, c.y(), WOOD_DARK, cx + 0.5F, c.y() + 1.6F, WOOD_DARK);
		}
		arch(g, c.x() - 9.0F, c);
		arch(g, c.right() + 9.0F, c);
		reliquary(g, c);
	}

	@Override
	public void paintFront(BossBarFrameContext c) {
		GuiCanvas g = c.canvas();
		for (int i = 0; i < 5; i++) {
			float dx = c.x() + 14 + GuiCanvas.hash(11, i, 2) * (BossBarFrame.WIDTH - 28);
			float cycle = (c.time() * 0.012F + GuiCanvas.hash(13, i, 3)) % 1.0F;
			float length = 1.0F + Mth.clamp(cycle / 0.7F, 0.0F, 1.0F) * 3.5F;
			g.rect(dx, c.y() + 12.5F, dx + 1.2F, c.y() + 12.5F + length, GuiCanvas.argb(0.95F, ICHOR),
					GuiCanvas.argb(0.85F, 0xA86A20));
			if (cycle > 0.7F) {
				float fall = (cycle - 0.7F) / 0.3F;
				g.mote(dx + 0.6F, c.y() + 13.0F + length + fall * 4.0F, 0.9F, GuiCanvas.argb(1.0F - fall, ICHOR));
			}
		}
	}

	/** A pointed arch window: dark wood around pale glass, with a mullion and transom. */
	private static void arch(GuiCanvas g, float cx, BossBarFrameContext c) {
		float base = c.y() + 14.0F, top = c.y() - 5.0F;
		int glass = GuiCanvas.argb(0.85F, PALE);
		g.rect(cx - 5.0F, c.y() + 2.0F, cx + 5.0F, base, WOOD_DARK, WOOD_DARK);
		g.tri(cx - 5.0F, c.y() + 2.0F, WOOD_DARK, cx + 5.0F, c.y() + 2.0F, WOOD_DARK, cx, top, WOOD_DARK);
		g.rect(cx - 3.0F, c.y() + 3.0F, cx + 3.0F, base - 2.0F, glass, GuiCanvas.argb(0.6F, 0x8AA0B4));
		g.tri(cx - 3.0F, c.y() + 3.0F, glass, cx + 3.0F, c.y() + 3.0F, glass, cx, top + 2.5F, GuiCanvas.argb(0.95F, 0xFFFFFF));
		g.rect(cx - 0.5F, top + 2.5F, cx + 0.5F, base - 2.0F, WOOD_DARK, WOOD_DARK);
		g.rect(cx - 3.0F, c.y() + 7.0F, cx + 3.0F, c.y() + 8.0F, WOOD_DARK, WOOD_DARK);
	}

	/** A small black-iron reliquary with a pale glint behind its window. */
	private static void reliquary(GuiCanvas g, BossBarFrameContext c) {
		float cx = c.centerX(), top = c.y() + 11.0F;
		g.rect(cx - 4.0F, top, cx + 4.0F, top + 6.0F, 0xFF1A1A1E, 0xFF0C0C10);
		g.tri(cx - 4.0F, top, 0xFF1A1A1E, cx + 4.0F, top, 0xFF1A1A1E, cx, top - 2.5F, 0xFF26262C);
		float glint = 0.55F + Mth.sin(c.time() * 0.06F) * 0.25F;
		g.rect(cx - 1.5F, top + 1.5F, cx + 1.5F, top + 4.5F, GuiCanvas.argb(glint, PALE),
				GuiCanvas.argb(glint * 0.6F, PALE));
	}
}
