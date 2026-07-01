package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.bestiary.PacketRequestSpecimenBestiary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Set;

public class BestiaryTabController implements IProgressTab {
	private final BestiaryTabState state = new BestiaryTabState();

	@Override
	public void onInit(ProgressScreenContext ctx) {
		refreshFromClient();
		if (Minecraft.getInstance().player != null) {
			PacketHandler.sendToServer(new PacketRequestSpecimenBestiary());
		}
	}

	@Override
	public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {
		refreshFromClient();
		state.listScroll = Math.min(state.listScroll, Math.max(0, BestiaryTabView.contentHeight(state) - BestiaryTabView.listHeight(ctx)));
		BestiaryTabView.draw(gfx, ctx, state, mouseX, mouseY);
	}

	private void refreshFromClient() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			state.rebuild(Set.of(), Set.of(), Set.of());
			return;
		}
		HemoCapabilityAccess.getSpecimenBestiary(mc.player)
				.ifPresentOrElse(progress -> state.rebuild(progress.recordedSpecimens(),
								progress.surrenderedSpecimens(), progress.recordedMorphlingLayers()),
						() -> state.rebuild(Set.of(), Set.of(), Set.of()));
	}

	@Override
	public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
	}

	@Override
	public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
	}

	@Override
	public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
		if (btn != 0) {
			return false;
		}
		BestiaryTabState.Entry entry = BestiaryTabView.entryUnder(ctx, state, mx, my);
		if (entry != null) {
			state.select(entry);
		}
		return true;
	}

	@Override
	public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) {
		return false;
	}

	@Override
	public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) {
		return false;
	}

	@Override
	public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
		int scrollAmount = (int) (-delta * 16);
		if (BestiaryTabView.isOverList(ctx, mx, my)) {
			int max = Math.max(0, BestiaryTabView.contentHeight(state) - BestiaryTabView.listHeight(ctx));
			state.listScroll = Math.max(0, Math.min(max, state.listScroll + scrollAmount));
		} else {
			state.infoScroll = Math.max(0, state.infoScroll + scrollAmount);
		}
		return true;
	}

	@Override
	public PanZoomState getPanZoomState() {
		return null;
	}
}
