package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.DirectBloodLinkData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Field;
import java.util.List;

/**
 * With hemomancy.livingBloodReview, stages living blood (core-loop Track 3) in a disposable world and captures it into
 * build/living-blood-client-review/screenshots, then stops: a Scriptorium's tank just fed, turning (the colour going out
 * of it), clotting (fresh blood on a band of clot) and clotted through, each with the bar's tooltip under the cursor;
 * the Vascular Status Gauge used on a sutured machine whose owner is below the third Degree (a stalled feed); and a
 * part-clotted Bloody Flask's tooltip. The player is a real survival player. Run with livingBloodReviewClient and drop
 * {"op":"create","name":"LivingBlood","seed":"1"} into operator/inbox after startup. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class LivingBloodClientReview {
	private static final String PROPERTY = "hemomancy.livingBloodReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 60;
	private static int ticks;
	private static int shot;
	private static BlockPos row;

	private enum Kind { TANK, GAUGE, FLASK }

	/** One staged moment: how long ago the tank was fed (or the flask bottled), and what is on screen. */
	private record Shot(String name, Kind kind, long fedTicksAgo) {
	}

	private static final List<Shot> SHOTS = List.of(
			new Shot("tank-fresh", Kind.TANK, 0L),
			new Shot("tank-turning", Kind.TANK, 4_800L),
			new Shot("tank-clotting", Kind.TANK, CoagulationRules.FRESH_WINDOW_TICKS + 10 * CoagulationRules.MINUTE_TICKS),
			new Shot("tank-clotted-through", Kind.TANK, CoagulationRules.FULL_CLOT_TICKS),
			new Shot("gauge-stalled-clotting", Kind.GAUGE, CoagulationRules.FRESH_WINDOW_TICKS + 4 * CoagulationRules.MINUTE_TICKS),
			new Shot("flask-clotting", Kind.FLASK, CoagulationRules.FRESH_WINDOW_TICKS + 5 * CoagulationRules.MINUTE_TICKS));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("living-blood-client-review")) return;
		if (mc.level == null && mc.screen instanceof AccessibilityOnboardingScreen) {
			mc.options.onboardAccessibility = false;
			mc.options.save();
			mc.setScreen(new TitleScreen());
		}
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		MinecraftServer server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == SETUP_TICK) server.execute(() -> stage(server.getPlayerList().getPlayer(uuid), server));
		if (ticks < FIRST_SHOT_TICK || row == null) return;
		int local = (ticks - FIRST_SHOT_TICK) % SHOT_TICKS;
		if (local == 0) {
			if (shot == SHOTS.size()) {
				Hemomancy.LOGGER.info("LIVING_BLOOD_REVIEW captured {}", shot);
				mc.stop();
				return;
			}
			Shot current = SHOTS.get(shot);
			mc.options.hideGui = false;
			if (mc.screen != null) mc.player.closeContainer();
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), current));
		}
		if (shot >= SHOTS.size()) return;
		Shot current = SHOTS.get(shot);
		if (local == 10 && current.kind() == Kind.GAUGE) KeyMapping.click(mc.options.keyUse.getKey());
		if (local == 10 && current.kind() == Kind.FLASK) mc.setScreen(new InventoryScreen(mc.player));
		if (local >= 14 && local <= 24) park(mc);
		// Tutorial and advancement toasts sit over the bar's top corner.
		if (local >= 14 && local <= 40) mc.getToasts().clear();
		if (local == 1) mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
		if (local == 24 && current.kind() == Kind.TANK) {
			Screenshot.grab(mc.gameDirectory, "living-blood-" + shot + "-" + current.name() + "-bar.png",
					mc.getMainRenderTarget(), message -> {});
		}
		if (local >= 26 && local <= 40) hover(mc, current);
		if (local == 40) {
			Screenshot.grab(mc.gameDirectory, "living-blood-" + shot + "-" + current.name() + ".png",
					mc.getMainRenderTarget(), message -> {});
			Hemomancy.LOGGER.info("LIVING_BLOOD_REVIEW shot {} {} screen={}", shot, current.name(),
					mc.screen == null ? "none" : mc.screen.getClass().getSimpleName());
		}
		if (local == 50) shot++;
	}

	/** Moves the cursor off the screen's panels, so the bar is seen whole. */
	private static void park(Minecraft mc) {
		cursor(mc, 4, 4);
	}

	/** Puts the cursor on the Scriptorium's blood bar or on hotbar slot one of the inventory. */
	private static void hover(Minecraft mc, Shot current) {
		if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
		int guiX = current.kind() == Kind.FLASK ? screen.getGuiLeft() + 16 : screen.getGuiLeft() + 205;
		int guiY = current.kind() == Kind.FLASK ? screen.getGuiTop() + 150 : screen.getGuiTop() + 80;
		cursor(mc, guiX, guiY);
	}

	private static void cursor(Minecraft mc, int guiX, int guiY) {
		var window = mc.getWindow();
		double x = guiX * (double) window.getScreenWidth() / window.getGuiScaledWidth();
		double y = guiY * (double) window.getScreenHeight() / window.getGuiScaledHeight();
		try {
			Field xpos = MouseHandler.class.getDeclaredField("xpos");
			Field ypos = MouseHandler.class.getDeclaredField("ypos");
			xpos.setAccessible(true);
			ypos.setAccessible(true);
			xpos.setDouble(mc.mouseHandler, x);
			ypos.setDouble(mc.mouseHandler, y);
		} catch (ReflectiveOperationException e) {
			Hemomancy.LOGGER.warn("LIVING_BLOOD_REVIEW cannot move the cursor", e);
		}
	}

	/** A floor and one Enzymatic Scriptorium in front of the player. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		var level = player.serverLevel();
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-6, -1, -4), row.offset(6, 5, 8)))
			level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		level.setBlock(station(), BlockInit.enzymatic_scriptorium.get().defaultBlockState(), 3);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(4000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		Hemomancy.LOGGER.info("LIVING_BLOOD_REVIEW staged at {}", row);
	}

	private static BlockPos station() {
		return row.offset(0, 0, 3);
	}

	/** Refills the tank, ages it by the shot's amount and opens what the shot looks at. */
	private static void pose(ServerPlayer player, Shot shot) {
		var level = player.serverLevel();
		var station = (EnzymaticScriptoriumBlockEntity) level.getBlockEntity(station());
		BlockBloodVolume tank = ReservoirBlood.settled(station);
		tank.scrapeClot(tank.clotted());
		tank.setBloodVolume(0.0D);
		tank.fill(500.0D);
		tank.backdate(level.getGameTime(), shot.kind() == Kind.FLASK ? 0L : shot.fedTicksAgo());
		station.sendUpdates();
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(shot.kind() == Kind.GAUGE ? 2 : 3);
		if (shot.kind() == Kind.GAUGE)
			BloodRoutingSavedData.get(level).setLink(station(), DirectBloodLinkData.nearby(player.getUUID()));
		else
			BloodRoutingSavedData.get(level).removeLink(station());

		Vec3 eye = Vec3.atBottomCenterOf(row).add(0.0D, 1.62D, 0.0D);
		Vec3 target = Vec3.atCenterOf(station());
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
		player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
		player.getInventory().selected = 0;
		switch (shot.kind()) {
			case TANK -> {
				player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				player.openMenu(station, station());
			}
			case GAUGE -> player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.vascular_status_gauge.get()));
			case FLASK -> {
				ItemStack flask = new ItemStack(ItemInit.bloody_flask.get());
				flask.set(DataComponentInit.BOTTLED_AT.get(), level.getGameTime() - shot.fedTicksAgo());
				player.setItemInHand(InteractionHand.MAIN_HAND, flask);
			}
		}
	}
}
