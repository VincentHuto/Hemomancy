package com.vincenthuto.hemomancy.client.screen.living;

import java.util.HashMap;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.morphling.EquippedMorphlingProvider;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.common.item.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.itemhandler.MorphlingJarItemHandler;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.morphling.PacketUpdateLivingStaffMorph;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

/**
 * Morph-selection overlay opened by right-clicking the Morphling Jar.
 * Each stored morphling floats and swims around inside a jar-shaped region.
 * Hover to see a glow + tooltip.  Click to equip it on the player.
 * The currently-equipped morphling gets a gold glow + star.
 */
@OnlyIn(Dist.CLIENT)
public class MorphlingJarViewerScreen extends Screen {

	// ── Static open helpers (always create a fresh instance) ─────────────────────

	public static void openScreenViaItem() {
		openScreen();
	}

	public static void openScreen() {
		Minecraft.getInstance().setScreen(new MorphlingJarViewerScreen());
	}

	// ── Layout constants ─────────────────────────────────────────────────────────

	private static final int JAR_W = 120;
	private static final int JAR_H = 90;
	private static final int ICON  = 16;
	private static final int HIT_R = 18;

	// ── Pixel-outline cache (lazily built per item) ──────────────────────────────

	private static final Map<Item, boolean[][]> PIXEL_MASK_CACHE = new HashMap<>();

	/**
	 * Gets or builds a 16x16 boolean mask where true = non-transparent pixel
	 * by reading the item's baked model sprite.
	 */
	private static boolean[][] getPixelMask(ItemStack stack) {
		Item item = stack.getItem();
		if (PIXEL_MASK_CACHE.containsKey(item)) {
			return PIXEL_MASK_CACHE.get(item);
		}
		boolean[][] mask = new boolean[16][16];
		boolean anyOpaque = false;
		try {
			Minecraft mc = Minecraft.getInstance();
			BakedModel model = mc.getItemRenderer().getModel(stack, null, null, 0);
			TextureAtlasSprite sprite = model.getParticleIcon();
			int spriteW = sprite.contents().width();
			int spriteH = sprite.contents().height();
			for (int px = 0; px < 16; px++) {
				for (int py = 0; py < 16; py++) {
					int sampleX = Math.min((px * spriteW) / 16, spriteW - 1);
					int sampleY = Math.min((py * spriteH) / 16, spriteH - 1);
					int pixel = sprite.getPixelRGBA(0, sampleX, sampleY);
					// getPixelRGBA returns ABGR; alpha in bits 24-31
					// but also check bits 0-7 in case format differs
					int alphaHigh = (pixel >> 24) & 0xFF;
					int alphaLow = pixel & 0xFF;
					boolean opaque = alphaHigh > 10 || (alphaHigh == 0 && alphaLow > 10 && pixel != 0);
					mask[px][py] = opaque;
					if (opaque) anyOpaque = true;
				}
			}
		} catch (Exception e) {
			anyOpaque = false;
		}
		// If nothing was detected as opaque, the sprite read probably failed — fill all
		if (!anyOpaque) {
			for (int px = 0; px < 16; px++)
				for (int py = 0; py < 16; py++)
					mask[px][py] = true;
		}
		PIXEL_MASK_CACHE.put(item, mask);
		return mask;
	}

	/**
	 * Draws a colored outline just outside the non-transparent pixels of the item.
	 * For each transparent pixel that has at least one opaque 8-neighbor, draws a colored dot.
	 * Also draws on out-of-bounds neighbors (the 1px ring around the 16x16 area).
	 */
	private static void drawPixelOutline(GuiGraphics g, int screenX, int screenY, boolean[][] mask, int color) {
		// Check the 18x18 area (1px border around the 16x16 icon)
		for (int ox = -1; ox <= 16; ox++) {
			for (int oy = -1; oy <= 16; oy++) {
				// This position must be transparent (or outside the icon)
				boolean thisOpaque = (ox >= 0 && ox < 16 && oy >= 0 && oy < 16) && mask[ox][oy];
				if (thisOpaque) continue;

				// Check if any 8-neighbor is opaque
				boolean adjacentToOpaque = false;
				for (int dx = -1; dx <= 1 && !adjacentToOpaque; dx++) {
					for (int dy = -1; dy <= 1 && !adjacentToOpaque; dy++) {
						if (dx == 0 && dy == 0) continue;
						int nx = ox + dx;
						int ny = oy + dy;
						if (nx >= 0 && nx < 16 && ny >= 0 && ny < 16 && mask[nx][ny]) {
							adjacentToOpaque = true;
						}
					}
				}
				if (adjacentToOpaque) {
					g.fill(screenX + ox, screenY + oy, screenX + ox + 1, screenY + oy + 1, color);
				}
			}
		}
	}

	// ── Per-morphling swim parameters (set once in init) ─────────────────────────

	private float[] speedX;
	private float[] speedY;
	private float[] phaseX;
	private float[] phaseY;

	// ── State ────────────────────────────────────────────────────────────────────

	private MorphlingJarItemHandler jarHandler;
	private int slotCount;
	private int activeIndex = -1;
	private int jarLeft;
	private int jarTop;
	private int hoveredIndex = -1;
	private long openTick;

	// ── Constructor ──────────────────────────────────────────────────────────────

	public MorphlingJarViewerScreen() {
		super(Component.translatable("hemomancy.screen.morphling_viewer"));
	}

	// ── Lifecycle ────────────────────────────────────────────────────────────────

	@Override
	protected void init() {
		super.init();

		Minecraft mc = Minecraft.getInstance();
		Player player = HLClientUtils.getClientPlayer();
		if (player == null || mc.level == null) {
			onClose();
			return;
		}

		openTick = mc.level.getGameTime();

		// Find the jar (inventory first, then rune slot 7)
		ItemStack jarStack = Hemomancy.findItemInPlayerInv(player, ItemMorphlingJar.class);
		if (jarStack.isEmpty()) {
			jarStack = player.getCapability(RunesCapabilities.RUNES)
					.map(r -> r.getStackInSlot(7))
					.filter(s -> s.getItem() instanceof ItemMorphlingJar)
					.orElse(ItemStack.EMPTY);
		}
		IItemHandler raw = jarStack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
		if (!(raw instanceof MorphlingJarItemHandler handler)) {
			onClose();
			return;
		}
		jarHandler = handler;
		jarHandler.load();
		slotCount = jarHandler.getSlots();

		// Discover which jar slot matches the player's currently equipped morphling
		activeIndex = -1;
		player.getCapability(EquippedMorphlingProvider.MORPHLING_CAPA).ifPresent(cap -> {
			ItemStack equipped = cap.getEquippedMorphling();
			if (!equipped.isEmpty()) {
				for (int i = 0; i < slotCount; i++) {
					if (ItemStack.isSameItemSameTags(jarHandler.getStackInSlot(i), equipped)) {
						activeIndex = i;
						break;
					}
				}
			}
		});

		// Centre the swim area
		jarLeft = (width - JAR_W) / 2;
		jarTop  = (height - JAR_H) / 2;

		// Per-morphling orbit parameters
		speedX = new float[slotCount];
		speedY = new float[slotCount];
		phaseX = new float[slotCount];
		phaseY = new float[slotCount];
		for (int i = 0; i < slotCount; i++) {
			speedX[i] = 0.4f + i * 0.15f;
			speedY[i] = 0.3f + i * 0.12f;
			phaseX[i] = i * 2.399f;
			phaseY[i] = i * 3.883f;
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	// ── Animation helpers ────────────────────────────────────────────────────────

	private float getAnimTime(float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return 0f;
		long elapsed = mc.level.getGameTime() - openTick;
		return (elapsed + partialTicks) / 20.0f;
	}

	private int[] getMorphPos(int i, float time) {
		float sx = Mth.sin(time * speedX[i] + phaseX[i]);
		float sy = Mth.sin(time * speedY[i] + phaseY[i]);
		float wx = 0.03f * Mth.sin(time * speedX[i] * 2.0f + phaseX[i] + 1.0f);
		float wy = 0.03f * Mth.sin(time * speedY[i] * 1.8f + phaseY[i] + 2.0f);
		float nx = Mth.clamp(0.5f + 0.42f * sx + wx, 0.02f, 0.98f);
		float ny = Mth.clamp(0.5f + 0.40f * sy + wy, 0.02f, 0.98f);
		int cx = jarLeft + (int) (nx * (JAR_W - ICON));
		int cy = jarTop  + (int) (ny * (JAR_H - ICON));
		return new int[]{cx, cy};
	}

	// ── Rendering ────────────────────────────────────────────────────────────────

	@Override
	public void render(GuiGraphics g, int mx, int my, float partialTicks) {
		renderBackground(g);

		if (jarHandler == null || speedX == null) return;

		float time = getAnimTime(partialTicks);

		// Panel
		int pad = 16;
		int px = jarLeft - pad;
		int py = jarTop  - pad - 14;
		int pw = JAR_W + pad * 2;
		int ph = JAR_H + pad * 2 + 28;

		g.fill(px, py, px + pw, py + ph, 0xDD10808);
		g.renderOutline(px, py, pw, ph, 0xFF5C1010);
		g.drawCenteredString(font, getTitle(), width / 2, py + 5, 0xFFBB4444);

		// Jar belly
		g.renderOutline(jarLeft - 1, jarTop - 1, JAR_W + 2, JAR_H + 2, 0x44888888);
		g.fill(jarLeft, jarTop, jarLeft + JAR_W, jarTop + JAR_H, 0x22115511);

		// Swimming morphlings
		hoveredIndex = -1;
		for (int i = 0; i < slotCount; i++) {
			ItemStack morphStack = jarHandler.getStackInSlot(i);
			if (morphStack.isEmpty()) continue;

			int[] pos = getMorphPos(i, time);
			int cx = pos[0];
			int cy = pos[1];

			float dx = mx - (cx + ICON / 2f);
			float dy = my - (cy + ICON / 2f);
			boolean hovered = (dx * dx + dy * dy) <= (HIT_R * HIT_R);
			boolean active  = (i == activeIndex);

			if (hovered) hoveredIndex = i;

			g.renderItem(morphStack, cx, cy);

			if (active || hovered) {
				int borderColor = active ? 0xFFFFD700 : 0xFF33CC33;
				boolean[][] mask = getPixelMask(morphStack);
				drawPixelOutline(g, cx, cy, mask, borderColor);
			}

			if (active) {
				g.drawString(font, "\u2605", cx + ICON - 2, cy - 5, 0xFFFFD700, true);
			}
		}

		// Tooltip (drawn last, on top)
		if (hoveredIndex >= 0) {
			ItemStack hoverStack = jarHandler.getStackInSlot(hoveredIndex);
			if (!hoverStack.isEmpty()) {
				g.renderTooltip(font, hoverStack, mx, my);
			}
		}

		g.drawCenteredString(font, "Click a morphling to equip", width / 2, py + ph - 12, 0xFF666666);
	}

	// ── Input ────────────────────────────────────────────────────────────────────

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (jarHandler == null || speedX == null)
			return super.mouseClicked(mx, my, button);

		float time = getAnimTime(Minecraft.getInstance().getPartialTick());

		for (int i = 0; i < slotCount; i++) {
			ItemStack morphStack = jarHandler.getStackInSlot(i);
			if (morphStack.isEmpty() || !(morphStack.getItem() instanceof MorphlingItem))
				continue;

			int[] pos = getMorphPos(i, time);
			int cx = pos[0] + ICON / 2;
			int cy = pos[1] + ICON / 2;

			float dx = (float) (mx - cx);
			float dy = (float) (my - cy);
			if (dx * dx + dy * dy <= HIT_R * HIT_R) {
				if (i == activeIndex) {
					// Clicking the already-equipped morphling unequips it
					PacketHandler.CHANNELMORPHLINGJAR.sendToServer(new PacketUpdateLivingStaffMorph(-1));
				} else {
					PacketHandler.CHANNELMORPHLINGJAR.sendToServer(new PacketUpdateLivingStaffMorph(i));
				}
				Player player = Minecraft.getInstance().player;
				if (player != null)
					player.playSound(SoundEvents.GLASS_PLACE, 0.4f, 1.2f);
				onClose();
				return true;
			}
		}

		// Click outside panel → close
		int pad = 16;
		int ppx = jarLeft - pad;
		int ppy = jarTop  - pad - 14;
		int ppw = JAR_W + pad * 2;
		int pph = JAR_H + pad * 2 + 28;
		if (mx < ppx || mx > ppx + ppw || my < ppy || my > ppy + pph) {
			onClose();
			return true;
		}

		return super.mouseClicked(mx, my, button);
	}
}

