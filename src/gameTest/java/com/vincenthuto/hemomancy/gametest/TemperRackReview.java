package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodVolumeOverlay;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Native review of every adaptive HUD combination, corners, scales, reserves and real-cursor tooltips. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class TemperRackReview {
	private static final String SAVE = "TemperRackReview";
	private static final int WARM_UP_TICKS = 60;
	private static final int SETTLE_TICKS = 20;
	private static final float UNSEEN = Float.NaN;
	private static final float[] TWO = { 20, 9, UNSEEN, UNSEEN, UNSEEN, UNSEEN, UNSEEN, UNSEEN };
	private static final float[] ALL = { 20, 14, 9, 4, 17, 0.5F, 12, 7 };
	private static final float[] NONE = { UNSEEN, UNSEEN, UNSEEN, UNSEEN, UNSEEN, UNSEEN, UNSEEN, UNSEEN };

	/** {@code hover} is the phial index the cursor rests on with chat open, or -1 for no tooltip. */
	private record Scene(String name, float[] pools, int parts, int hudLocation, int hover, int guiScale,
                         boolean terrain, int reserve, boolean morphling, int degree, float blood, boolean legacyIcon) {}

	private static final List<Scene> SCENES = List.of(
            new Scene("none", ALL, 0, 0, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("vessel", ALL, 1, 0, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("gourd", ALL, 2, 0, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("vessel_gourd", ALL, 3, 0, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("temper", ALL, 4, 0, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("vessel_temper", ALL, 5, 0, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("gourd_temper", ALL, 6, 0, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("all", ALL, 7, 0, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("two", TWO, 5, 0, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("bottom_right", ALL, 7, 3, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("top_right", ALL, 7, 1, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("bottom_left", ALL, 7, 2, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("tooltip_full", ALL, 7, 0, 0, 3, false, 0, false, 4, 0.7F, false),
            new Scene("tooltip_dry", ALL, 7, 0, 5, 3, false, 0, false, 4, 0.7F, false),
            new Scene("tooltip_temper_only", ALL, 4, 0, 6, 3, false, 0, false, 4, 0.7F, false),
            new Scene("all_scale2", ALL, 7, 0, -1, 2, false, 0, false, 4, 0.7F, false),
            new Scene("all_scale4", ALL, 7, 0, -1, 4, false, 0, false, 4, 0.7F, false),
            new Scene("bright_terrain", ALL, 7, 0, -1, 3, true, 0, false, 4, 0.7F, false),
            new Scene("horn", ALL, 7, 0, -1, 3, false, 1, false, 4, 0.7F, false),
            new Scene("rib", ALL, 7, 0, -1, 3, false, 2, false, 4, 0.7F, false),
            new Scene("morphling", ALL, 7, 0, -1, 3, false, 0, true, 4, 0.7F, false),
            new Scene("morphling_right", ALL, 7, 3, -1, 3, false, 0, true, 4, 0.7F, false),
            new Scene("morphling_no_gourd", ALL, 5, 0, -1, 3, false, 0, true, 4, 0.7F, false),
            new Scene("unequipped_gourd", ALL, 7, 0, -1, 3, false, -1, false, 4, 0.7F, false),
            new Scene("unseen_schools", NONE, 7, 0, -1, 3, false, 0, false, 4, 0.7F, false),
            new Scene("apotheos", ALL, 7, 0, -1, 3, false, 0, false, 8, 0.7F, false),
            new Scene("degree0", ALL, 7, 0, -1, 3, false, 0, false, 0, 0.7F, false),
            new Scene("degree3", ALL, 7, 0, -1, 3, false, 0, false, 3, 0.7F, false),
            new Scene("degree5", ALL, 7, 0, -1, 3, false, 0, false, 5, 0.7F, false),
            new Scene("degree7", ALL, 7, 0, -1, 3, false, 0, true, 7, 0.7F, false),
            new Scene("low_blood", ALL, 7, 0, -1, 3, false, 0, false, 7, 0.15F, false),
            new Scene("gourd_temper_right", ALL, 6, 1, -1, 3, false, 0, false, 7, 0.7F, false),
            new Scene("horn_temper", ALL, 6, 0, -1, 3, false, 1, false, 7, 0.7F, false),
            new Scene("horn_temper_right", ALL, 6, 1, -1, 3, false, 1, false, 7, 0.7F, false),
            new Scene("rib_temper", ALL, 6, 0, -1, 3, false, 2, false, 7, 0.7F, false),
            new Scene("rib_temper_right", ALL, 6, 1, -1, 3, false, 2, false, 7, 0.7F, false),
            new Scene("horn_all_right", ALL, 7, 1, -1, 3, false, 1, true, 7, 0.7F, false),
            new Scene("jar_vessel", ALL, 1, 0, -1, 3, false, 0, true, 7, 0.7F, false),
            new Scene("jar_reserve", ALL, 2, 0, -1, 3, false, 0, true, 7, 0.7F, false),
            new Scene("jar_reserve_temper", ALL, 6, 0, -1, 3, false, 0, true, 7, 0.7F, false),
            new Scene("jar_vials", ALL, 4, 0, -1, 3, false, 0, true, 7, 0.7F, false),
            new Scene("jar_only", ALL, 0, 0, -1, 3, false, 0, true, 7, 0.7F, false),
            new Scene("jar_legacy_icon", ALL, 7, 0, -1, 3, false, 0, true, 7, 0.7F, true),
            new Scene("all_morphling_scale4", ALL, 7, 3, -1, 4, false, 0, true, 7, 0.7F, false));

	private static int ticks;
	private static int scene = -1;
	private static int sceneTicks;
	private static int shots;
	private static boolean captureNextFrame;

	private TemperRackReview() {}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.temperRackReview")) return;
		var mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
		var server = mc.getSingleplayerServer();
		if (!SAVE.equals(server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().getFileName().toString()))
			throw new IllegalStateException("Temper rack review requires the disposable " + SAVE + " save");
		mc.options.pauseOnLostFocus = false;
		int tick = ticks++;
		if (tick == 0) {
			int[] windowX = new int[1], windowY = new int[1];
			GLFW.glfwGetWindowPos(mc.getWindow().getWindow(), windowX, windowY);
			Hemomancy.LOGGER.info("TEMPER_RACK_REVIEW native window origin {},{}", windowX[0], windowY[0]);
			// Large review windows can be centred offscreen on smaller desktops; keep real cursor targets reachable.
			GLFW.glfwSetWindowPos(mc.getWindow().getWindow(), 16, 16);
			mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
			mc.getToasts().clear();
			mc.options.guiScale().set(3);
			mc.resizeDisplay();
			var uuid = mc.player.getUUID();
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
				for (String cmd : new String[]{"time set 6000", "gamerule doDaylightCycle false", "weather clear",
						"gamerule doMobSpawning false", "effect clear @s", "tp @s 0 301 0",
						"fill -16 300 -16 16 300 16 minecraft:quartz_block"})
					server.getCommands().performPrefixedCommand(source, cmd);
				player.setGameMode(GameType.CREATIVE);
				player.setXRot(-30);
			});
		}
		if (tick < WARM_UP_TICKS || captureNextFrame) return;
		if (scene >= 0 && SCENES.get(scene).hover() >= 0) hover(mc, SCENES.get(scene).hover());
		if (scene >= 0 && ++sceneTicks < SETTLE_TICKS) return;
		if (scene >= 0) {
			captureNextFrame = true;
			return;
		}
		nextScene(mc);
	}

	private static void nextScene(Minecraft mc) {
		if (++scene == SCENES.size()) {
			HemoClientConfig.HUD_LOCATION.set(0);
            HemoClientConfig.SHOW_BLOOD_VOLUME_HUD.set(true);
            HemoClientConfig.SHOW_GOURD_HUD.set(true);
            HemoClientConfig.SHOW_TEMPER_HUD.set(true);
            HemoClientConfig.MORPHLING_HUD_MODE.set(HemoClientConfig.MorphlingHudMode.ATTACHED);
			Hemomancy.LOGGER.info("TEMPER_RACK_REVIEW captured {}", shots);
			mc.stop();
			return;
		}
		sceneTicks = 0;
		Scene current = SCENES.get(scene);
		mc.options.guiScale().set(current.guiScale());
		mc.resizeDisplay();
		if (mc.getWindow().getGuiScale() != current.guiScale()) {
			throw new IllegalStateException("Review window cannot display requested GUI scale " + current.guiScale());
		}
		HemoClientConfig.HUD_LOCATION.set(current.hudLocation());
        HemoClientConfig.SHOW_BLOOD_VOLUME_HUD.set((current.parts() & 1) != 0);
        HemoClientConfig.SHOW_GOURD_HUD.set((current.parts() & 2) != 0);
        HemoClientConfig.SHOW_TEMPER_HUD.set((current.parts() & 4) != 0);
        HemoClientConfig.MORPHLING_HUD_MODE.set(current.legacyIcon()
                ? HemoClientConfig.MorphlingHudMode.LEGACY_ICON : HemoClientConfig.MorphlingHudMode.ATTACHED);
		mc.setScreen(current.hover() >= 0 ? new ChatScreen("") : null);
		var server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), current));
		stage(mc.player, current);
	}

	/** The same state on both sides, so a server sync cannot undo the client's copy. */
	private static void stage(Player player, Scene scene) {
		player.setXRot(scene.terrain() ? 65 : -30);
		var blood = HemoCapabilityAccess.getBloodVolume(player);
		blood.setActive(true);
        blood.setBloodVolume(blood.getMaxBloodVolume() * scene.blood());
        var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
        degree.setDegreeNumber(scene.degree());
        degree.setArchonPath(scene.degree() == 8
                ? com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.APOTHEOS
                : com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.NONE);
		var equipment = HemoCapabilityAccess.getEquipment(player);
		equipment.setStackInSlot(5, new ItemStack(ItemInit.charm_of_vascularium.get()));
		equipment.setStackInSlot(HarbingerEquipmentMenu.BANDOLIER_SLOT_INDEX, new ItemStack(ItemInit.titration_bandolier.get()));
        ItemStack reserve = scene.reserve() < 0 ? ItemStack.EMPTY : new ItemStack(switch (scene.reserve()) {
            case 1 -> ItemInit.curved_horn.get();
            case 2 -> ItemInit.hemorath_rib.get();
            default -> ItemInit.blood_gourd_red.get();
        });
        HemoCapabilityAccess.getBloodVolume(reserve).ifPresent(v -> v.setBloodVolume(v.getMaxBloodVolume() * 0.65));
        equipment.setStackInSlot(HarbingerEquipmentMenu.GOURD_SLOT_INDEX, reserve);
        ItemStack morphling = scene.morphling() ? new ItemStack(ItemInit.morphling_sunburst.get()) : ItemStack.EMPTY;
        if (!morphling.isEmpty()) {
            var data = new net.minecraft.nbt.CompoundTag();
            data.putFloat("EnzymePower", 10);
            morphling.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.of(data));
        }
        HemoCapabilityAccess.getEquippedMorphling(player).setEquippedMorphling(morphling);
		var temper = HemoCapabilityAccess.getBloodTemper(player);
		// Reset only this disposable fixture, including discovery bits left by earlier review runs.
		temper.setFromSync(new float[EnumBloodTemper.values().length], 0);
		for (EnumBloodTemper school : EnumBloodTemper.values()) {
			float pool = scene.pools()[school.ordinal()];
			if (Float.isNaN(pool)) continue;
			temper.markSeen(school);
			temper.set(school, pool, TemperRules.MAX_CAP);
		}
	}

    /** Uses the shared rendered geometry; the tooltip itself still comes from a real GLFW hover. */
    private static void hover(Minecraft mc, int school) {
        var window = mc.getWindow();
        var anchor = BloodVolumeOverlay.placement(mc.player, window.getGuiScaledWidth(), window.getGuiScaledHeight());
        var assembly = BloodVolumeOverlay.assembly(mc.player);
        if (assembly == null) return;
        var seat = assembly.vials().stream().filter(v -> v.school() == school).findFirst();
        if (seat.isEmpty()) return;
        var sprite = assembly.vialArt().sprite(seat.get().pointsRight());
        int mid = sprite.dx().length / 2;
        float x = seat.get().mouthX() + sprite.dx()[mid] + 0.5f, y = seat.get().mouthY() + sprite.dy()[mid] + 0.5f;
        GLFW.glfwFocusWindow(window.getWindow());
        GLFW.glfwSetCursorPos(window.getWindow(), anchor.screenX(x) * window.getScreenWidth() / window.getGuiScaledWidth(),
                anchor.screenY(y) * window.getScreenHeight() / window.getGuiScaledHeight());
    }

	@SubscribeEvent
	public static void afterFrame(RenderFrameEvent.Post event) {
		if (!captureNextFrame) return;
		captureNextFrame = false;
		var mc = Minecraft.getInstance();
		Scene current = SCENES.get(scene);
		if (current.hover() >= 0) {
			var window = mc.getWindow();
			var anchor = BloodVolumeOverlay.placement(mc.player, window.getGuiScaledWidth(), window.getGuiScaledHeight());
			double x = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
			double y = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
			int hit = BloodVolumeOverlay.assembly(mc.player).schoolAt(anchor.localX(x), anchor.localY(y));
			Hemomancy.LOGGER.info("TEMPER_RACK_REVIEW hover {} expected {} actual {} mouse {},{}", current.name(), current.hover(), hit, x, y);
			if (hit != current.hover()) throw new IllegalStateException("Native cursor missed the review phial");
		}
		Screenshot.grab(mc.gameDirectory, "temper-rack-" + current.name() + ".png", mc.getMainRenderTarget(), message -> {});
		shots++;
		Hemomancy.LOGGER.info("TEMPER_RACK_REVIEW captured {}", current.name());
		nextScene(mc);
	}
}
