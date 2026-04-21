package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressScreen;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import net.minecraft.util.Mth;

/**
 * Encapsulates all pan/zoom state for a single scrollable content view inside
 * a GUI viewport. Both {@link HarbingerProgressScreen} and {@link UnstainedProgressScreen}
 * use one or more of these per-tab.
 *
 * <p>Usage pattern:
 * <pre>
 *   PanZoomState state = new PanZoomState();
 *   state.centreOn(contentW, contentH, guiWidth, guiHeight);
 *
 *   // in render:
 *   int screenX = state.sx(guiLeft, contentX);
 *
 *   // in mouseScrolled:
 *   state.applyScroll(guiLeft, guiTop, mouseX, mouseY, delta);
 *
 *   // in mouseDragged:
 *   state.applyDrag(dx, dy);
 * </pre>
 */
public final class PanZoomState {

	public static final float ZOOM_MIN = 0.35f;
	public static final float ZOOM_MAX = 3.0f;
	/** How far (screen pixels) a content edge may be dragged past the viewport edge. */
	private static final int PAN_MARGIN = 200;

	public double panX;
	public double panY;
	public float  zoom = 1.0f;

	// ── Coordinate transforms ────────────────────────────────────

	/** Content-space X → screen-space X, given the GUI's left edge. */
	public int sx(int guiLeft, int contentX) {
		return (int)(guiLeft + panX + contentX * zoom);
	}

	/** Content-space Y → screen-space Y, given the GUI's top edge. */
	public int sy(int guiTop, int contentY) {
		return (int)(guiTop + panY + contentY * zoom);
	}

	/** Screen-space X → content-space X. */
	public double cx(int guiLeft, double screenX) {
		return (screenX - guiLeft - panX) / zoom;
	}

	/** Screen-space Y → content-space Y. */
	public double cy(int guiTop, double screenY) {
		return (screenY - guiTop - panY) / zoom;
	}

	/** Scaled half-node size on screen. */
	public int halfNode(int nodeSize) {
		return Math.max(4, (int)(nodeSize * zoom / 2));
	}

	// ── Pan / zoom operations ────────────────────────────────────

	/**
	 * Centres the view so that content of the given size fills the viewport.
	 */
	public void centreOn(int contentW, int contentH, int guiWidth, int guiHeight) {
		zoom = 1.0f;
		panX = (guiWidth  - contentW * zoom) / 2.0;
		panY = (guiHeight - contentH * zoom) / 2.0;
	}

	/**
	 * Applies a scroll-wheel delta, zooming towards the mouse cursor position.
	 * Call this from {@code mouseScrolled}.
	 */
	public void applyScroll(int guiLeft, int guiTop, double mouseX, double mouseY, double delta) {
		double cxBefore = cx(guiLeft, mouseX);
		double cyBefore = cy(guiTop, mouseY);
		float oldZoom = zoom;
		zoom = Mth.clamp(zoom + (float) delta * 0.15f, ZOOM_MIN, ZOOM_MAX);
		panX += cxBefore * (oldZoom - zoom);
		panY += cyBefore * (oldZoom - zoom);
	}

	/**
	 * Applies a drag delta (from {@code mouseDragged}).
	 */
	public void applyDrag(double dx, double dy) {
		panX += dx;
		panY += dy;
	}

	/**
	 * Clamps pan so content can never be scrolled entirely off-screen.
	 */
	public void clamp(int contentW, int contentH, int guiWidth, int guiHeight) {
		double scaledW = contentW * zoom;
		double scaledH = contentH * zoom;
		panX = Mth.clamp(panX, -scaledW - PAN_MARGIN, guiWidth + PAN_MARGIN);
		panY = Mth.clamp(panY, -scaledH - PAN_MARGIN, guiHeight + PAN_MARGIN);
	}

	/**
	 * Saves state from another instance (for per-tab save/restore).
	 */
	public void copyFrom(PanZoomState other) {
		this.panX = other.panX;
		this.panY = other.panY;
		this.zoom = other.zoom;
	}

	/**
	 * Restores state to another instance (for per-tab save/restore).
	 */
	public void copyTo(PanZoomState other) {
		other.panX = this.panX;
		other.panY = this.panY;
		other.zoom = this.zoom;
	}
}
