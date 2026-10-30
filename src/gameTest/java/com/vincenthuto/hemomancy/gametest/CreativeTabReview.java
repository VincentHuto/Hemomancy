package com.vincenthuto.hemomancy.gametest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.CreativeTabGroup;
import com.vincenthuto.hemomancy.common.init.CreativeTabInit;
import com.vincenthuto.hemomancy.common.init.CreativeTabSection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

/**
 * With hemomancy.creativeTabReview, opens the creative inventory on the main Hemomancy tab and captures it at the top
 * and with each section's banner on the first row, as {@code creative-<label>-<n>-<section>.png} in the game
 * directory's screenshots folder. Needs a disposable CreativeTabReview save. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CreativeTabReview {
	private static final String SAVE = "CreativeTabReview";
	private static final int WARM_UP_TICKS = 60;
	private static final int SHOT_SPACING = 10;

	private static int ticks;
	private static int shot = -1;
	private static final List<Float> SCROLLS = new ArrayList<>();
	private static final List<String> NAMES = new ArrayList<>();

	private CreativeTabReview() {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.creativeTabReview")) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
		if (!mc.getSingleplayerServer().getWorldData().getLevelName().equals(SAVE))
			throw new IllegalStateException("Creative tab review requires the disposable " + SAVE + " save");
		mc.options.pauseOnLostFocus = false;
		int tick = ticks++;
		if (tick == 0) {
			mc.options.guiScale().set(3);
			mc.resizeDisplay();
			var server = mc.getSingleplayerServer();
			var uuid = mc.player.getUUID();
			server.execute(() -> server.getPlayerList().getPlayer(uuid).setGameMode(GameType.CREATIVE));
		}
		if (tick == WARM_UP_TICKS) {
			try {
				Field selected = CreativeModeInventoryScreen.class.getDeclaredField("selectedTab");
				selected.setAccessible(true);
				selected.set(null, CreativeTabInit.hemomancytab.get());
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException(e);
			}
			mc.setScreen(new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), true));
			return;
		}
		if (tick < WARM_UP_TICKS + SHOT_SPACING || (tick - WARM_UP_TICKS) % SHOT_SPACING != 0) return;
		if (!(mc.screen instanceof CreativeModeInventoryScreen screen)) {
			Hemomancy.LOGGER.warn("CREATIVE_TAB_REVIEW screen closed");
			mc.stop();
			return;
		}
		if (shot < 0) {
			plan(screen);
			shot = 0;
		}
		if (shot >= SCROLLS.size()) {
			Hemomancy.LOGGER.info("CREATIVE_TAB_REVIEW captured {}", SCROLLS.size());
			mc.stop();
			return;
		}
		scroll(screen, SCROLLS.get(shot));
		capture(screen, shot + "-" + NAMES.get(shot));
		shot++;
	}

	/** The top of the tab, then each section's banner on the first visible row. */
	private static void plan(CreativeModeInventoryScreen screen) {
		List<ItemStack> items = screen.getMenu().items;
		int rows = Mth.positiveCeilDiv(items.size(), 9) - 5;
		Hemomancy.LOGGER.info("CREATIVE_TAB_REVIEW {} slots, {} scroll rows", items.size(), rows);
		SCROLLS.add(0f);
		NAMES.add("top");
		CreativeTabSection last = null;
		int lastRun = Integer.MIN_VALUE;
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).isEmpty()) continue;
			CreativeTabSection section = CreativeTabSection.of(items.get(i));
			int run = CreativeTabSection.run(items.get(i));
			if (section != last) {
				add(rows, i / 9 - 1, section.id());
			} else if (section == CreativeTabSection.HEMATIC && run != lastRun && run >= 0) {
				add(rows, i / 9, "hematic-" + CreativeTabGroup.values()[run].name().toLowerCase(java.util.Locale.ROOT));
			}
			last = section;
			lastRun = run;
		}
		SCROLLS.add(1f);
		NAMES.add("bottom");
	}

	private static void add(int rows, int row, String name) {
		SCROLLS.add(rows <= 0 ? 0f : Mth.clamp((float) row / rows, 0f, 1f));
		NAMES.add(name);
	}

	private static void scroll(CreativeModeInventoryScreen screen, float offset) {
		try {
			Field scroll = CreativeModeInventoryScreen.class.getDeclaredField("scrollOffs");
			scroll.setAccessible(true);
			scroll.setFloat(screen, offset);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
		screen.getMenu().scrollTo(offset);
	}

	private static void capture(CreativeModeInventoryScreen screen, String name) {
		var mc = Minecraft.getInstance();
		var window = mc.getWindow();
		var target = mc.getMainRenderTarget();
		target.bindWrite(true);
		RenderSystem.clearColor(0.18f, 0.2f, 0.24f, 1);
		RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
		float far = ClientHooks.getGuiFarPlane();
		RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, (float) (window.getWidth() / window.getGuiScale()),
				(float) (window.getHeight() / window.getGuiScale()), 0, 1000, far), VertexSorting.ORTHOGRAPHIC_Z);
		var modelView = RenderSystem.getModelViewStack();
		modelView.pushMatrix();
		modelView.translation(0, 0, 10000F - far);
		RenderSystem.applyModelViewMatrix();
		Lighting.setupFor3DItems();
		var graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
		screen.renderWithTooltip(graphics, -1000, -1000, 0);
		graphics.flush();
		modelView.popMatrix();
		RenderSystem.applyModelViewMatrix();
		String label = System.getProperty("hemomancy.creativeTabReviewLabel", "after");
		Screenshot.grab(mc.gameDirectory, "creative-" + label + "-" + name + ".png", target, message -> {});
		Hemomancy.LOGGER.info("CREATIVE_TAB_REVIEW shot {}", name);
	}
}
