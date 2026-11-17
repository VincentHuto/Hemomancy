package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.data.StrandClientState;
import com.vincenthuto.hemomancy.client.data.VerbRefusalClientState;
import com.vincenthuto.hemomancy.client.screen.manips.ManipulationIconResolver;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.BrandRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * A thin readout beside the crosshair while the player channels a verb: the verb, and when a Sculpting is selected its
 * memory icon, name and price per second, with four ticks round the crosshair that brighten when the Sculpting will
 * claim what the player aims at. When the server refuses the claim's pulse (protected ground, nothing to burn, too many
 * walls) the ticks are barred in bone and the price line reads "Refused" until a pulse is paid again. Nothing is drawn
 * while no verb is channelled.
 */
public final class VerbChannelOverlay {
	private static final int CLAIMED = 0xFFC0273A;
	private static final int UNCLAIMED = 0x806E6266;
	/** A refused claim, in pale bone and a little clear: red means claimed. */
	private static final int REFUSED = 0xD8B8A888;
	private static final int VERB_TEXT = 0xFFD8505C;
	private static final int SCULPTING_TEXT = 0xFFE2D2D2;
	private static final int SCULPTING_IDLE_TEXT = 0xFF8E8486;
	private static final float TEXT_SCALE = 0.75F;
	private static final int ARC_FULL = 0xFFFFD2C4;
	private static final int ARC_TRACK = 0x405A4448;
	private static final int ARC_RADIUS = 11;

	private VerbChannelOverlay() {
	}

	public static void renderHUD(GuiGraphics graphics, int width, int height) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player == null || minecraft.options.hideGui) return;
		Optional<Verb> channelled = VerbChannel.channelling(player);
		if (channelled.isEmpty()) return;
		Verb verb = channelled.get();
		int cx = width / 2;
		int cy = height / 2;
		// The price shown is the Sculpting that claims the aim (a narrow one ahead of the main), else the default one.
		Optional<SculptingDispatcher.Claim> claim = SculptingDispatcher.currentClaim(player, verb);
		Optional<SculptingDispatcher.Selected> selected = claim.map(SculptingDispatcher.Claim::selected)
				.or(() -> SculptingDispatcher.selected(player, verb));
		boolean claimed = claim.isPresent();
		// A claim the server refused (protected ground, nothing to burn): a barred mark in bone, not the claimed red.
		boolean refused = claimed && VerbRefusalClientState.isRefused(player.level().getGameTime());
		if (selected.isPresent()) {
			if (refused) drawRefusedReticle(graphics, cx, cy);
			else drawReticle(graphics, cx, cy, claimed ? CLAIMED : UNCLAIMED);
		}
		drawStrandArc(graphics, player, cx, cy);

		Font font = minecraft.font;
		int x = cx + 10;
		int y = cy + 6;
		if (selected.isPresent()) {
			String id = selected.get().manipulation().getName();
			graphics.blit(ManipulationIconResolver.base(id), x, y, 10, 10, 0F, 0F, 16, 16, 16, 16);
			graphics.blit(ManipulationIconResolver.overlay(id), x, y, 10, 10, 0F, 0F, 16, 16, 16, 16);
			x += 12;
		}
		graphics.pose().pushPose();
		graphics.pose().translate(x, y, 0.0F);
		graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
		graphics.drawString(font, Component.translatable(verb.langKey()), 0, 0, VERB_TEXT, true);
		if (refused) {
			graphics.drawString(font, Component.translatable("hud.hemomancy.verb_channel.refused"), 0, font.lineHeight + 1,
					REFUSED, true);
		} else selected.ifPresent(sculpting -> {
			double perSecond = SculptingRules.bloodPerSecond(sculpting.sculpting().bloodPerPulse(player), sculpting.sculpting().cadence());
			Component line = HemoText.translatable("hud.hemomancy.verb_channel.sculpting",
					sculpting.manipulation().getDisplayName(), String.format(java.util.Locale.ROOT, "%.0f", perSecond));
			graphics.drawString(font, line, 0, font.lineHeight + 1, claimed ? SCULPTING_TEXT : SCULPTING_IDLE_TEXT, true);
		});
		graphics.pose().popPose();
	}

	/**
	 * A strand's build (core-loop Track 12) as one thin arc round the crosshair: a jet's pressure, a plant's sunk
	 * blood, a brand's hold. It brightens when full. Nothing for a lash or a spray, which have nothing to build.
	 */
	private static void drawStrandArc(GuiGraphics graphics, Player player, int cx, int cy) {
		StrandClientState.Strand strand = StrandClientState.of(player.getId());
		if (strand == null || strand.cue == null || strand.released >= 0) return;
		double fill = switch (strand.form()) {
			case JET -> strand.jet.pressure();
			case PLANT -> strand.cue.param();
			case BRAND -> BrandRules.progress(strand.brand);
			default -> -1.0D;
		};
		if (fill < 0.0D) return;
		int segments = 40;
		int lit = (int) Math.round(Math.max(0.0D, Math.min(1.0D, fill)) * segments);
		int colour = fill >= 0.999D ? ARC_FULL : CLAIMED;
		for (int i = 0; i < segments; i++) {
			double a = -Math.PI / 2 + i * Math.PI * 2 / segments;
			int x = cx + (int) Math.round(Math.cos(a) * ARC_RADIUS);
			int y = cy + (int) Math.round(Math.sin(a) * ARC_RADIUS);
			graphics.fill(x, y, x + 1, y + 1, i < lit ? colour : ARC_TRACK);
		}
	}

	/**
	 * The refused mark: the four ticks pulled out two pixels, each crossed by a short stroke across it, in pale bone.
	 * Thin and partly clear, as the claimed ticks are.
	 */
	private static void drawRefusedReticle(GuiGraphics graphics, int cx, int cy) {
		int inner = 7;
		int outer = 9;
		int bar = (inner + outer) / 2;
		graphics.fill(cx - outer, cy, cx - inner + 1, cy + 1, REFUSED);
		graphics.fill(cx - bar, cy - 1, cx - bar + 1, cy + 2, REFUSED);
		graphics.fill(cx + inner, cy, cx + outer + 1, cy + 1, REFUSED);
		graphics.fill(cx + bar, cy - 1, cx + bar + 1, cy + 2, REFUSED);
		graphics.fill(cx, cy - outer, cx + 1, cy - inner + 1, REFUSED);
		graphics.fill(cx - 1, cy - bar, cx + 2, cy - bar + 1, REFUSED);
		graphics.fill(cx, cy + inner, cx + 1, cy + outer + 1, REFUSED);
		graphics.fill(cx - 1, cy + bar, cx + 2, cy + bar + 1, REFUSED);
	}

	/** Four short ticks round the crosshair: a thin mark, never a bar across the screen. */
	private static void drawReticle(GuiGraphics graphics, int cx, int cy, int colour) {
		int inner = 5;
		int outer = 7;
		graphics.fill(cx - outer, cy, cx - inner + 1, cy + 1, colour);
		graphics.fill(cx + inner, cy, cx + outer + 1, cy + 1, colour);
		graphics.fill(cx, cy - outer, cx + 1, cy - inner + 1, colour);
		graphics.fill(cx, cy + inner, cx + 1, cy + outer + 1, colour);
	}
}
