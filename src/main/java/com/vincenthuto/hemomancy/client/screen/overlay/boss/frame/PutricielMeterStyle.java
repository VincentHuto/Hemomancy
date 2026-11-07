package com.vincenthuto.hemomancy.client.screen.overlay.boss.frame;

import com.vincenthuto.hemomancy.client.screen.overlay.GuiCanvas;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterContext;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.SaintMeterStyle;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielColonisationRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielKindlingRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielPhase;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;

/**
 * Putriciel's meter under the bar (design section 4): the Kindling as a pierced censer hanging on two chain strokes
 * under the bar's centre, and the challenger's own colonisation as a bracket fungus beside it. In the rot the censer's
 * bowl fills from the bottom with slow olive-grey smoke and cream glints, trembling a pixel at 75% and over; in the
 * interval its contents are fire (crimson body, pink-white heart, black soot at the top) draining downward past two
 * bone pins at 66% and 33%, and the censer swings toward the nodule row while an Offering is open. Nothing red outside
 * the interval. The bracket grows a tier at 5, 10, 15 and 20, sheds spore dots at 15, pulses at 20, and flakes away in
 * ash grey when the fire burns it off.
 */
public final class PutricielMeterStyle implements SaintMeterStyle {
	private static final int HEIGHT = 26;
	private static final int IRON = 0xFF2A2822, IRON_LIP = 0xFF5C5646, MOULD = 0xFF6A7A3A;
	private static final int SMOKE_LOW = 0xFF5A5E3A, SMOKE_HIGH = 0xFF8A8A5A, GLINT = 0xFFE8D8A0;
	private static final int FLAME = 0xFFD61E2E, HEART = 0xFFFFA8BE, SOOT = 0xFF16100C;
	private static final int BONE = 0xFFD8CDB0, WELL = 0xF0121008;
	private static final int CAP_OCHRE = 0xFFA98D4F, CAP_CREAM = 0xFFE3CC8B, GILLS = 0xFF76A85C, CHARCOAL = 0xFF201E18;
	private static final int ASH = 0xFF4A4744;
	private static final float BOWL_W = 22.0F, BOWL_H = 14.0F;

	@Override
	public int height() {
		return HEIGHT;
	}

	@Override
	public void paint(SaintMeterContext c) {
		GuiCanvas g = c.canvas();
		if (g == null) return;
		float reveal = HemoEasing.smoothstep(HemoEasing.clamp01(c.reveal()));
		if (reveal <= 0.01F) return;
		boolean fire = c.phase() == PutricielPhase.INTERVAL.number();
		float kindling = c.saintFraction();
		float time = c.time();
		float swing = c.openingId() == PutricielOpenings.OFFERING_ID ? Mth.sin(time * .15F) * 2.5F : 0.0F;
		float tremor = !fire && kindling >= PutricielKindlingRules.TREMOR ? Mth.sin(time * 1.7F) * .8F : 0.0F;
		float cx = c.centerX() + swing + tremor;
		float top = c.y() + 1;
		float rim = top + 6;
		float alpha = reveal;
		// the two chain strokes from the bar to the rim
		g.segment(c.centerX() - 6, top, cx - 8, rim, .8F, .8F, GuiCanvas.fade(IRON_LIP, alpha));
		g.segment(c.centerX() + 6, top, cx + 8, rim, .8F, .8F, GuiCanvas.fade(IRON_LIP, alpha));
		// the bowl: a well, the contents, then the iron over them
		float half = BOWL_W / 2.0F, bottomHalf = half - 4.0F, bottom = rim + BOWL_H;
		g.quad(cx - half, rim, GuiCanvas.fade(WELL, alpha), cx + half, rim, GuiCanvas.fade(WELL, alpha),
				cx + bottomHalf, bottom, GuiCanvas.fade(WELL, alpha), cx - bottomHalf, bottom, GuiCanvas.fade(WELL, alpha));
		float level = bottom - BOWL_H * kindling * reveal;
		if (kindling > 0.005F) {
			float t = (level - rim) / BOWL_H;
			float levelHalf = Mth.lerp(t, half, bottomHalf) - 1.0F;
			int low = fire ? FLAME : SMOKE_LOW, high = fire ? HEART : SMOKE_HIGH;
			g.quad(cx - levelHalf, level, GuiCanvas.fade(high, alpha), cx + levelHalf, level, GuiCanvas.fade(high, alpha),
					cx + bottomHalf - 1, bottom - 1, GuiCanvas.fade(low, alpha), cx - bottomHalf + 1, bottom - 1, GuiCanvas.fade(low, alpha));
			if (fire) {
				g.rect(cx - levelHalf, level, cx + levelHalf, level + 1.5F, GuiCanvas.fade(SOOT, alpha), GuiCanvas.fade(SOOT, alpha * .3F));
				g.disc(cx, (level + bottom) * .5F, 2.5F, 2.0F, 10, GuiCanvas.fade(HEART, alpha), GuiCanvas.fade(FLAME, 0.0F));
			} else {
				for (int i = 0; i < 4; i++) {
					float s = (time * .02F + i * .25F) % 1.0F;
					float y = bottom - 2 - s * (bottom - level - 2);
					float x = cx + (GuiCanvas.hash(29, i, 0) - .5F) * (BOWL_W - 8);
					g.mote(x, y, .6F, GuiCanvas.fade(GLINT, alpha * Mth.sin(s * Mth.PI) * .9F));
				}
			}
		}
		// the iron: rim, body lines, pierced lid holes, mould at the seams
		g.rect(cx - half - 1, rim - 1.5F, cx + half + 1, rim + .5F, GuiCanvas.fade(IRON_LIP, alpha), GuiCanvas.fade(IRON, alpha));
		g.segment(cx - half, rim, cx - bottomHalf, bottom, 1.0F, 1.0F, GuiCanvas.fade(IRON, alpha));
		g.segment(cx + half, rim, cx + bottomHalf, bottom, 1.0F, 1.0F, GuiCanvas.fade(IRON, alpha));
		g.rect(cx - bottomHalf, bottom, cx + bottomHalf, bottom + 1.5F, GuiCanvas.fade(IRON, alpha), GuiCanvas.fade(IRON, alpha));
		for (int i = 0; i < 4; i++) {
			float x = cx - 6 + i * 4;
			g.mote(x, rim - .5F, .7F, GuiCanvas.fade(fire ? HEART : SOOT, alpha * (fire ? .8F : .6F)));
		}
		g.mote(cx - half + 1, rim + 2, 1.0F, GuiCanvas.fade(MOULD, alpha * .8F));
		g.mote(cx + bottomHalf, bottom - 1, .9F, GuiCanvas.fade(MOULD, alpha * .8F));
		if (fire) {
			for (float pin : new float[]{PutricielKindlingRules.FIRST_OFFERING, PutricielKindlingRules.SECOND_OFFERING}) {
				float y = bottom - BOWL_H * pin;
				g.rect(cx + half - 1, y - .5F, cx + half + 3, y + .5F, GuiCanvas.fade(BONE, alpha), GuiCanvas.fade(BONE, alpha));
			}
		}
		bracket(g, c, c.centerX() - 34, rim + 2, alpha, time);
	}

	/** The challenger's colonisation: a bracket fungus of four tiers. */
	private static void bracket(GuiCanvas g, SaintMeterContext c, float x, float y, float alpha, float time) {
		float value = c.ownSecondaryMax() > 0.0F ? c.ownSecondary() : PutricielColonisationRules.value(c.flags());
		int tier = PutricielColonisationRules.tier(value);
		boolean burning = PutricielColonisationRules.burning(c.flags());
		g.rect(x - 1, y - 2, x + 1, y + 14, GuiCanvas.fade(CHARCOAL, alpha * .8F), GuiCanvas.fade(CHARCOAL, alpha * .8F));
		float pulse = tier >= 4 ? .85F + .15F * Mth.sin(time * .3F) : 1.0F;
		for (int i = 0; i < 4; i++) {
			float cy = y + 11 - i * 3.5F;
			boolean grown = i < tier;
			float width = (5.0F + i * .6F) * (grown ? pulse : .6F);
			int cap = grown ? GuiCanvas.mix(CAP_OCHRE, CAP_CREAM, i / 3.0F) : CHARCOAL;
			if (burning && grown && i == tier - 1) cap = GuiCanvas.mix(cap, ASH, .6F);
			float a = grown ? alpha : alpha * .35F;
			g.disc(x + width * .5F + 1, cy, width, 1.6F, 10, GuiCanvas.fade(cap, a), GuiCanvas.fade(CHARCOAL, a));
			if (grown) g.rect(x + 1, cy + 1, x + width + 1, cy + 1.6F, GuiCanvas.fade(GILLS, a * .7F), GuiCanvas.fade(GILLS, 0.0F));
		}
		if (tier >= 3) {
			for (int i = 0; i < 3; i++) {
				float s = (time * .03F + i / 3.0F) % 1.0F;
				g.mote(x + 6 + i * 2, y + 12 + s * 6, .5F, GuiCanvas.fade(CAP_CREAM, alpha * (1 - s) * .8F));
			}
		}
		if (burning) {
			for (int i = 0; i < 3; i++) {
				float s = (time * .06F + i / 3.0F) % 1.0F;
				g.mote(x + 3 + i * 3, y + 2 - s * 6, .6F, GuiCanvas.fade(ASH, alpha * (1 - s)));
			}
		}
	}
}
