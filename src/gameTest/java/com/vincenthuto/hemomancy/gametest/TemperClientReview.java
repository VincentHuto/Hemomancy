package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.item.ScryingDiagnosticsScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PlayerStateSync;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationDiagnosticsSync;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.menu.ScryingDiagnosticsMenuProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * With hemomancy.temperReview, stages Temper (core-loop Track 4) in a disposable world and captures it into
 * build/temper-client-review/screenshots, then stops: the eight Temper phials under the blood bar at mixed fills (a low
 * one with its dry rim), only three schools shown, a dry Blood Shot with its action-bar reason, a Blood Shot paid by a
 * vivacious enzyme in the off hand, the Scrying podium's Temper tab (grown alignment, scar alignment and Temper held)
 * and the Manipulations tab's cost tooltip with the dry Temper line. A real survival player. Run with
 * temperReviewClient and drop {"op":"create","name":"TemperReview","seed":"1"} into operator/inbox after startup.
 * Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class TemperClientReview {
	private static final String PROPERTY = "hemomancy.temperReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 70;
	private static int ticks;
	private static int shot;
	private static BlockPos row;

	private enum Kind { HUD, CAST, SCRYING_TEMPER, SCRYING_COST }

	/** One staged moment: the pools (school to fraction of its cap; absent schools never seen), and what happens. */
	private record Shot(String name, Kind kind, Map<EnumBloodTemper, Float> pools, boolean catalyst) {
	}

	private static final Map<EnumBloodTemper, Float> MIXED = Map.of(
			EnumBloodTemper.ANIMUS, 1.0F, EnumBloodTemper.FLAMMEUS, 0.75F, EnumBloodTemper.DUCTILIS, 0.5F,
			EnumBloodTemper.LUX, 0.02F, EnumBloodTemper.MORTEM, 0.25F, EnumBloodTemper.CONGEATIO, 0.9F,
			EnumBloodTemper.FERRIC, 0.0F, EnumBloodTemper.TENEBRIS, 0.6F);

	private static final List<Shot> SHOTS = List.of(
			new Shot("hud-eight-schools", Kind.HUD, MIXED, false),
			new Shot("hud-three-schools", Kind.HUD, Map.of(EnumBloodTemper.ANIMUS, 0.4F, EnumBloodTemper.FLAMMEUS, 1.0F,
					EnumBloodTemper.LUX, 0.15F), false),
			new Shot("cast-dry", Kind.CAST, Map.of(EnumBloodTemper.ANIMUS, 0.0F, EnumBloodTemper.FLAMMEUS, 0.6F), false),
			new Shot("cast-catalyst", Kind.CAST, Map.of(EnumBloodTemper.ANIMUS, 0.0F, EnumBloodTemper.FLAMMEUS, 0.6F), true),
			new Shot("scrying-temper-tab", Kind.SCRYING_TEMPER, MIXED, false),
			new Shot("scrying-cost-dry", Kind.SCRYING_COST, Map.of(EnumBloodTemper.ANIMUS, 0.0F), false));

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("temper-client-review")) return;
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
				Hemomancy.LOGGER.info("TEMPER_REVIEW captured {}", shot);
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
		if (local == 1) mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
		if (local >= 2 && local <= 50) mc.getToasts().clear();
		if (local == 20 && current.kind() == Kind.CAST) server.execute(() -> cast(server.getPlayerList().getPlayer(uuid)));
		if (local >= 12 && local <= 40 && mc.screen instanceof ScryingDiagnosticsScreen screen) {
			tab(screen, current.kind() == Kind.SCRYING_TEMPER ? "TEMPER" : "MANIPULATIONS");
			if (current.kind() == Kind.SCRYING_COST) cursor(mc, screen.getGuiLeft() + 16 + 30, screen.getGuiTop() + 56 + 49 + 4);
			else cursor(mc, 2, 2);
		}
		if (local == 40) {
			Screenshot.grab(mc.gameDirectory, "temper-" + shot + "-" + current.name() + ".png", mc.getMainRenderTarget(),
					message -> {});
			Hemomancy.LOGGER.info("TEMPER_REVIEW shot {} {} screen={}", shot, current.name(),
					mc.screen == null ? "none" : mc.screen.getClass().getSimpleName());
		}
		if (local == 50) shot++;
	}

	private static void tab(ScryingDiagnosticsScreen screen, String name) {
		try {
			Field field = ScryingDiagnosticsScreen.class.getDeclaredField("activeTab");
			field.setAccessible(true);
			Object[] tabs = field.getType().getEnumConstants();
			for (Object tab : tabs) if (((Enum<?>) tab).name().equals(name)) field.set(screen, tab);
		} catch (ReflectiveOperationException e) {
			Hemomancy.LOGGER.warn("TEMPER_REVIEW cannot set the Scrying tab", e);
		}
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
			Hemomancy.LOGGER.warn("TEMPER_REVIEW cannot move the cursor", e);
		}
	}

	/** A floor, a lit wall to look at, the Vascularium Charm worn (so the blood bar shows) and Blood Shot selected. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		var level = player.serverLevel();
		row = player.blockPosition();
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty peaceful", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-6, -1, -4), row.offset(6, 5, 8)))
			level.setBlock(pos, pos.getY() < row.getY() ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: pos.getZ() == row.getZ() + 8 ? BlockInit.polished_venous_stone_bricks.get().defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		HemoCapabilityAccess.getEquipment(player).setStackInSlot(5, new ItemStack(ItemInit.charm_of_vascularium.get()));
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(3);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(4000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		var shot = ManipulationInit.blood_shot.get();
		known.getKnownManips().put(shot, new ManipLevel(1, 0));
		known.setEquippedManipNames(new ArrayList<>(List.of(shot.getName())));
		known.setSelectedManip(shot);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		temperament.setTemperAlignment(EnumBloodTemper.ANIMUS, 40.0F);
		temperament.setTemperAlignment(EnumBloodTemper.FLAMMEUS, 25.0F);
		temperament.setTemperAlignment(EnumBloodTemper.CONGEATIO, 70.0F);
		temperament.setTemperAlignment(EnumBloodTemper.FERRIC, 30.0F);
		temperament.addTransientAlignment(EnumBloodTemper.FERRIC, 12.0F);
		for (PlayerStateSync.Entry entry : PlayerStateSync.Entry.values()) entry.send(player);
		Hemomancy.LOGGER.info("TEMPER_REVIEW staged at {}", row);
	}

	/** Sets the pools, faces the wall and opens what the shot looks at. */
	private static void pose(ServerPlayer player, Shot shot) {
		var level = player.serverLevel();
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		temper.setFromSync(new float[EnumBloodTemper.values().length], 0);
		for (var entry : shot.pools().entrySet()) {
			float cap = TemperCharge.cap(player, entry.getKey());
			temper.markSeen(entry.getKey());
			temper.set(entry.getKey(), cap * entry.getValue(), cap);
		}
		temper.sync(player, true);
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setItemInHand(InteractionHand.OFF_HAND, shot.catalyst()
				? new ItemStack(ItemInit.vivacious_enzyme.get(), 2) : ItemStack.EMPTY);
		player.teleportTo(level, row.getX() + 0.5D, row.getY(), row.getZ() + 0.5D, 0.0F, 10.0F);
		var shotManip = ManipulationInit.blood_shot.get();
		shotManip.capCooldown(player, 0L);
		switch (shot.kind()) {
			case SCRYING_TEMPER, SCRYING_COST -> {
				ManipulationDiagnosticsSync.sync(player);
				player.openMenu(new ScryingDiagnosticsMenuProvider());
			}
			default -> {
			}
		}
	}

	/** One Blood Shot through the real cast path; the action bar says what Temper did. */
	private static void cast(ServerPlayer player) {
		var shot = ManipulationInit.blood_shot.get();
		boolean cast = shot.tryPerformAction(player, player.level(), player.getMainHandItem(), player.blockPosition(), 0.0F);
		Hemomancy.LOGGER.info("TEMPER_REVIEW cast {} animus={} offhand={}", cast,
				HemoCapabilityAccess.getBloodTemper(player).get(EnumBloodTemper.ANIMUS), player.getOffhandItem());
	}
}
