package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.summon.KnownSummonsRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SummonsTabController implements IProgressTab {
	private static final float PREVIEW_AUTO_ROTATION_SPEED = 0.175f;
	private static final float PREVIEW_DRAG_ROTATION_SPEED = 0.02f;

	private final SummonsTabState state = new SummonsTabState();

	@Override
	public void onInit(ProgressScreenContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		refreshKnownSummons(mc);
		if (mc.player != null) {
			PacketHandler.sendToServer(new KnownSummonsRequestPacket());
		}
		state.rebuildTierMap();
		state.autoSelectFirstDegree();
	}

	@Override
	public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {
		refreshKnownSummons(Minecraft.getInstance());
		if (!state.previewDragging) {
			state.previewRotationAngle += partial * PREVIEW_AUTO_ROTATION_SPEED;
		}
		SummonsTabView.draw(gfx, ctx, state, mouseX, mouseY, partial);
	}

	private void refreshKnownSummons(Minecraft mc) {
		state.knownSummonNames.clear();
		if (mc.player != null) {
			HemoCapabilityAccess.getKnownSummons(mc.player)
					.ifPresent(known -> state.knownSummonNames.addAll(known.getKnownSummonNames()));
		}
	}

	@Override public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {}
	@Override public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {}

	@Override
	public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
		if (btn != 0) {
			return false;
		}
		Integer clickedDegree = SummonsTabView.degreeUnder(ctx, state, mx, my);
		if (clickedDegree != null) {
			if (clickedDegree.equals(state.selectedDegree)) {
				state.selectedDegree = null;
				state.selectedSummonIndex = 0;
			} else {
				state.selectedDegree = clickedDegree;
				state.selectedSummonIndex = 0;
			}
			state.infoScroll = 0;
			return true;
		}
		int clickedSummon = SummonsTabView.summonUnder(ctx, state, mx, my);
		if (clickedSummon >= 0) {
			state.selectedSummonIndex = clickedSummon;
			state.infoScroll = 0;
			return true;
		}
		if (SummonsTabView.isOverPreview(ctx, mx, my)) {
			state.previewDragging = true;
			state.previewDragLastX = mx;
			return true;
		}
		return true;
	}

	@Override
	public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) {
		if (btn == 0) {
			state.previewDragging = false;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) {
		if (state.previewDragging && btn == 0) {
			state.previewRotationAngle += (float) (mx - state.previewDragLastX) * PREVIEW_DRAG_ROTATION_SPEED;
			state.previewDragLastX = mx;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
		int scrollAmt = (int) (-delta * 14);
		if (ctx.isOverTierSidebar(mx, my)) {
			state.sidebarScroll = Math.max(0, state.sidebarScroll + scrollAmt);
			state.clampSidebarScroll(ctx.tierSidebarVisibleH());
		} else {
			state.infoScroll = Math.max(0, state.infoScroll + scrollAmt);
		}
		return true;
	}

	@Override public PanZoomState getPanZoomState() { return null; }
	public SummonsTabState getState() { return state; }
}
