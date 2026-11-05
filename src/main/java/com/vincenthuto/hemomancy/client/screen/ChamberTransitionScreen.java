package com.vincenthuto.hemomancy.client.screen;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.vincenthuto.hemomancy.client.data.ChamberVeil;
import com.vincenthuto.hemomancy.client.data.ChamberVeilRules;
import com.vincenthuto.hemomancy.client.screen.overlay.LidlessPassageView;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.block.shared.LidlessTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

/**
 * The screen over a dimension change into or out of the Chamber of Will, in place of the dirt screen: the void and
 * its black hole, swallowing the view until the level is ready, so a gateway crossing runs straight on into the
 * corridor and a walk out runs straight on into the world. Closed by the level load like the screen it replaces.
 */
public final class ChamberTransitionScreen extends ReceivingLevelScreen {
	private final LidlessTheme theme;
	private final float seed;
	private final float chamberTurn;
	private final float holeStart;
	private final float eyeShape;
	private final BooleanSupplier levelReceived;
	@Nullable
	private final ChamberVeil.Armed armed;
	@Nullable
	private final TextureTarget kept;
	private final long openedAt = System.currentTimeMillis();

	public ChamberTransitionScreen(BooleanSupplier levelReceived, Reason reason, boolean intoChamber) {
		super(levelReceived, reason);
		this.levelReceived = levelReceived;
		ChamberVeil.Armed armed = ChamberVeil.peek();
		this.armed = armed;
		Minecraft mc = Minecraft.getInstance();
		this.theme = armed != null ? armed.theme() : mc.player != null ? LidlessTheme.of(mc.player) : LidlessTheme.QLIPHOTH;
		this.seed = armed != null ? armed.seed() : 0.37F;
		// Into the Chamber the void has turned to its colours; out of it the colours have drained back to the void.
		this.chamberTurn = armed != null ? (armed.mode() == LidlessPassageService.Mode.RETURN ? 0.0F : 1.0F) : intoChamber ? 1.0F : 0.0F;
		// A walk's veil draws on from the walk's own hole, with the walk's last frame in it; a plain veil swallows the view.
		this.holeStart = armed != null ? armed.holeRadius() : ChamberVeilRules.ARRIVAL_RADIUS;
		this.eyeShape = armed != null ? armed.eyeShape() : 0.0F;
		this.kept = armed != null ? armed.kept() : null;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		float hole = ChamberVeilRules.holeRadius(System.currentTimeMillis() - openedAt, holeStart);
		LidlessPassageView.renderVeil(graphics, width, height, partialTick, theme, seed, chamberTurn, hole, eyeShape,
				kept != null ? kept.getColorTextureId() : 0);
	}

	@Override
	public void removed() {
		super.removed();
		// The client builds this screen more than once over one change; only the one the level arrived under spends the veil.
		if (levelReceived.getAsBoolean()) ChamberVeil.release(armed);
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
	}

	@Nullable
	public LidlessTheme theme() {
		return theme;
	}
}
