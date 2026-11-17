package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * With hemomancy.strandReview, stages core-loop Track 12's strands in a disposable world, captures them into
 * build/strand-client-review/screenshots and stops. A real survival player holds Projection with each strand
 * manipulation selected while the harness turns their mouse: Searing Lash held as an orb, thrown by a flick and swept
 * across a husk in first and third person, left to fade, and tapped; Blood Lash's orb let go by a click; Hematic Flare's jet held on a husk until it pierces the one behind; Blood
 * Eclipse's spray over a crowd; Glacial Grasp's plant sunk into the ground and erupting; Hemorrhage branding a pig.
 * Round two P5 adds the second wave, each in first and third person (Gloam Laceration's dark lash, the Cruor and needle
 * jets, the needle, arc and prism sprays, the fire and iron plants, the three brands), a twin lash from a free off hand,
 * a fire plant running along a wet stain, and the skills tab on the strand skill line, surface and deep. UN-42 adds the
 * cases no client had shown: the whip swung up and down, a stuck javelin recalled by Absorption, Gloam's pierce accent
 * on a tap from behind and its charged throw through a row, Searing Lash cracking three times in a row as its heat
 * builds, flame paint refused by a claim, and the ice hand and the flare's flash on a husk that keeps moving. Run with
 * strandReviewClient and drop {"op":"create","name":"StrandReview","seed":"1"} into operator/inbox after startup;
 * {@code -PstrandOnly=a,b} keeps only the shots whose names start with one of those words. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class StrandClientReview {
	private static final String PROPERTY = "hemomancy.strandReview";
	private static final int SETUP_TICK = 40;
	private static final int FIRST_SHOT_TICK = 80;
	private static final int SHOT_TICKS = 90;
	private static final int PRESS_TICK = 12;
	private static int ticks;
	private static int shot;
	private static int captured;
	private static BlockPos row;
	private static float baseYaw;
	/** The sweep's offset last tick: the harness turns the mouse by the difference, as a hand would. */
	private static float lastOffset;
	/** The side camera's entity id for a third-person shot, or -1. Written on the server thread, read on the client. */
	private static volatile int cameraId = -1;

	/**
	 * One staged moment. {@code stand} and {@code aim} are offsets from the row; the use key is held from
	 * {@link #PRESS_TICK} until {@code release}; while held the mouse sweeps {@code sweep} degrees each side at
	 * {@code step} a tick (0 for a held aim); {@code snapAt} clicks attack once (-1 for none); {@code captures} are the
	 * shot's local ticks to grab a frame at. The mouse goes through {@code LashCamera}, so a lash reads the first sweep
	 * as its flick and swings its whip aim with the rest while the view keeps its capped share. A shot named
	 * {@code lash-fade} stops sweeping at tick 40 and lets the whip dissolve; one named {@code lash-tap} never holds
	 * the use key, so its click is a tap. A name with {@code vertical} sweeps the pitch instead of the yaw, one with
	 * {@code charged} holds the orb thirty ticks before the sweep throws it, one with {@code recall} holds Absorption,
	 * one with {@code claimed} stands on land whose claim refuses the caster's blocks, one with {@code moving} keeps its
	 * husk walking side to side, and {@code searing-heat} clicks three times, twenty ticks apart.
	 */
	private record Shot(String name, String manipulation, Vec3 stand, Vec3 aim, float sweep, float step, int release,
			int snapAt, boolean thirdPerson, int[] captures, BiConsumer<ServerLevel, ServerPlayer> scene) {
	}

	private static final Vec3 LASH_HUSK = new Vec3(0.5, 0, 7.5);
	private static final Vec3 JET_FIRST = new Vec3(5.5, 0, 5.0);
	private static final Vec3 JET_BEHIND = new Vec3(5.5, 0, 8.0);
	private static final Vec3 SPRAY_CENTRE = new Vec3(-5.5, 0, 4.2);
	private static final Vec3 PLANT_POINT = new Vec3(10.5, -0.5, 5.5);
	private static final Vec3 PLANT_HUSK = new Vec3(11.8, 0, 5.8);
	/**
	 * The top face of the plant's floor block, where its first stain lies: aimed at the block's middle, a ray from the
	 * caster's eye strikes the top of the block in front.
	 */
	private static final Vec3 STAIN_AIM = PLANT_POINT.add(0, 0.5, 0);

	private static final String ONLY = "hemomancy.strandReview.only";
	/** The searing-heat shot's clicks, twenty ticks apart, past the crack cooldown. */
	private static final int[] HEAT_SNAPS = {30, 50, 70};
	/** Whether the shot's claim refuses the caster's blocks. Set on the server thread, read by the place event. */
	private static volatile boolean claimed;
	/** The husk a moving shot keeps walking, or -1. */
	private static volatile int movingId = -1;
	private static List<Shot> shots;

	private static final List<Shot> ALL = List.of(
			new Shot("lash-sweep", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 70, -1, false,
					new int[]{30, 33}, StrandClientReview::lashScene),
			new Shot("lash-third-person", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 70, -1, true,
					new int[]{31, 35}, StrandClientReview::lashScene),
			new Shot("lash-orb", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 0, 0, 80, -1, false,
					new int[]{24, 40, 62}, StrandClientReview::lashScene),
			new Shot("lash-orb-third-person", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 0, 0, 80, -1, true,
					new int[]{30, 62}, StrandClientReview::lashScene),
			new Shot("lash-throw", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 70, -1, false,
					new int[]{13, 14, 15, 16, 17, 18}, StrandClientReview::lashScene),
			new Shot("lash-throw-third-person", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 70, -1, true,
					new int[]{14, 15, 16, 17}, StrandClientReview::lashScene),
			new Shot("lash-fade", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 85, -1, false,
					new int[]{38, 52, 62, 72}, StrandClientReview::lashScene),
			new Shot("lash-fade-third-person", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 85, -1, true,
					new int[]{52, 64}, StrandClientReview::lashScene),
			new Shot("lash-tap", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 0, 0, 0, 30, false,
					new int[]{31, 33, 35, 37, 39}, StrandClientReview::lashScene),
			new Shot("lash-tap-third-person", "gloam_laceration", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 0, 0, 0, 30, true,
					new int[]{32, 34, 36, 38}, StrandClientReview::lashScene),
			new Shot("lash-tap-gloam", "gloam_laceration", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 0, 0, 0, 30, false,
					new int[]{31, 32, 33, 34, 36}, StrandClientReview::lashScene),
			new Shot("blood-lash-crack", "blood_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(1.4, 1.6, 0), 0, 0, 60, 34, false,
					new int[]{30, 35, 37}, StrandClientReview::lashScene),
			new Shot("flare-beam", "hematic_flare", new Vec3(5.5, 0, 1.5), JET_FIRST.add(0, 1.0, 0), 20, 4, 86, 82, false,
					new int[]{20, 26, 60, 83, 85}, StrandClientReview::jetScene),
			new Shot("flare-beam-third-person", "hematic_flare", new Vec3(5.5, 0, 1.5), JET_FIRST.add(0, 1.0, 0), 0, 0, 86, 80, true,
					new int[]{30, 70, 81, 83}, StrandClientReview::jetScene),
			new Shot("eclipse-dome", "blood_eclipse", new Vec3(-5.5, 0, 1.0), SPRAY_CENTRE.add(0, 0.8, 0), 0, 0, 85, 78, false,
					new int[]{20, 45, 74, 80, 83}, StrandClientReview::sprayScene),
			new Shot("eclipse-dome-third-person", "blood_eclipse", new Vec3(-5.5, 0, 1.0), SPRAY_CENTRE.add(0, 0.8, 0), 0, 0, 85, 78, true,
					new int[]{20, 74, 80, 83}, StrandClientReview::sprayScene),
			new Shot("ice-hand", "glacial_grasp", new Vec3(10.5, 0, 1.5), PLANT_HUSK.add(0, 1.0, 0), 0, 0, 85, 70, false,
					new int[]{20, 30, 60, 71, 74}, StrandClientReview::plantScene),
			new Shot("ice-hand-third-person", "glacial_grasp", new Vec3(10.5, 0, 1.5), PLANT_HUSK.add(0, 1.0, 0), 0, 0, 85, 70, true,
					new int[]{22, 32, 60, 72}, StrandClientReview::plantScene),
			// ── Round two P5: the second wave, each from the caster's eyes and then from the side ──
			new Shot("gloam-lash", "gloam_laceration", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 70, -1, false,
					new int[]{30, 33}, StrandClientReview::lashScene),
			new Shot("gloam-lash-third-person", "gloam_laceration", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 70, 46,
					true, new int[]{31, 47}, StrandClientReview::lashScene),
			new Shot("javelin-throw", "cruor_javelin", new Vec3(5.5, 0, 1.5), JET_FIRST.add(0, 1.0, 0), 0, 0, 52, -1, false,
					new int[]{20, 34, 50, 53, 54, 56}, StrandClientReview::jetScene),
			new Shot("javelin-throw-third-person", "cruor_javelin", new Vec3(5.5, 0, 1.5), JET_FIRST.add(0, 1.0, 0), 0, 0, 52, -1, true,
					new int[]{34, 50, 53, 54, 55}, StrandClientReview::jetScene),
			new Shot("javelin-stuck", "cruor_javelin", new Vec3(5.5, 0, 1.5), new Vec3(8.5, -0.5, 6.0), 0, 0, 40, -1, true,
					new int[]{44, 50, 70}, StrandClientReview::jetScene),
			new Shot("needle-volley", "blood_needle_lance", new Vec3(5.5, 0, 1.5), JET_FIRST.add(0, 1.0, 0), 0, 0, 80, 50, false,
					new int[]{30, 48, 52, 78, 81, 82}, StrandClientReview::jetScene),
			new Shot("needle-volley-third-person", "blood_needle_lance", new Vec3(5.5, 0, 1.5), JET_FIRST.add(0, 1.0, 0), 0, 0, 80, -1, true,
					new int[]{40, 78, 81, 82}, StrandClientReview::jetScene),
			new Shot("jolt-creature", "synaptic_jolt", new Vec3(-5.5, 0, 1.0), SPRAY_CENTRE.add(0, 0.8, 0), 0, 0, 70, -1, false,
					new int[]{24, 34}, StrandClientReview::sprayScene),
			new Shot("jolt-pool-third-person", "synaptic_jolt", new Vec3(10.5, 0, 1.5), PLANT_POINT.add(0, 0.6, 0), 0, 0, 80, -1, true,
					new int[]{30, 44, 58}, StrandClientReview::poolScene),
			new Shot("flame-paint", "crimson_flame_conjuration", new Vec3(10.5, 0, 1.5), PLANT_POINT.add(-2.0, 0, 0), 4, 1, 60, 50, false,
					new int[]{24, 40, 51, 54}, StrandClientReview::plantScene),
			new Shot("flame-paint-third-person", "crimson_flame_conjuration", new Vec3(10.5, 0, 1.5), PLANT_POINT.add(-2.0, 0, 0), 4, 1, 60, 50, true,
					new int[]{40, 51, 54, 58}, StrandClientReview::plantScene),
			new Shot("blade-run", "ferric_spikes", new Vec3(10.5, 0, 1.5), PLANT_POINT.add(0, 0, 4.0), 0, 0, 80, 62, false,
					new int[]{20, 30, 40, 63, 66, 76}, StrandClientReview::plantScene),
			new Shot("blade-run-third-person", "ferric_spikes", new Vec3(10.5, 0, 1.5), PLANT_POINT.add(0, 0, 4.0), 0, 0, 80, 62, true,
					new int[]{20, 30, 40, 64, 76}, StrandClientReview::plantScene),
			// A twin lash from a free off hand (Second Strand), from the eyes and from the side.
			new Shot("twin-lash", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 70, -1, false,
					new int[]{30, 33}, StrandClientReview::twinScene),
			new Shot("twin-lash-third-person", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 70, -1, true,
					new int[]{31, 35}, StrandClientReview::twinScene),
									// The strand skill line on the skills tab: Long Strand's surface nodes, then the deep ones.
			new Shot("skills-surface", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 0, 0, 0, -1, false,
					new int[]{50}, StrandClientReview::skillsScene),
			new Shot("skills-deep", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 0, 0, 0, -1, false,
					new int[]{60}, StrandClientReview::skillsScene),
			// ── UN-42: the single-player cases no client had shown ──
			new Shot("lash-vertical", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 70, -1, false,
					new int[]{30, 33, 36, 60}, StrandClientReview::lashScene),
			new Shot("lash-vertical-third-person", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 70, -1,
					true, new int[]{31, 35, 60}, StrandClientReview::lashScene),
			new Shot("javelin-recall-stuck", "cruor_javelin", new Vec3(5.5, 0, 1.5), new Vec3(8.5, -0.5, 6.0), 0, 0, 40, -1, true,
					new int[]{50, 70}, StrandClientReview::jetScene),
			new Shot("javelin-recall-third-person", "cruor_javelin", new Vec3(5.5, 0, 1.0), new Vec3(8.5, -0.5, 6.0), 0, 0, 50, -1,
					true, new int[]{10, 16, 20, 26, 40}, null),
			new Shot("lash-tap-gloam-ambush", "gloam_laceration", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 0, 0, 0, 30,
					false, new int[]{31, 32, 33, 34, 36}, StrandClientReview::backScene),
			new Shot("lash-tap-gloam-ambush-third-person", "gloam_laceration", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 0, 0,
					0, 30, true, new int[]{31, 32, 33, 35}, StrandClientReview::backScene),
			new Shot("gloam-charged-third-person", "gloam_laceration", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 85,
					-1, true, new int[]{40, 43, 44, 45, 47, 52}, StrandClientReview::rowScene),
			new Shot("searing-heat", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 86, -1, false,
					new int[]{31, 32, 51, 52, 71, 72}, StrandClientReview::lashScene),
			new Shot("searing-heat-third-person", "searing_lash", new Vec3(0.5, 0, 1.5), LASH_HUSK.add(0, 1.2, 0), 32, 16, 86, -1,
					true, new int[]{31, 51, 71}, StrandClientReview::lashScene),
			new Shot("flame-paint-claimed", "crimson_flame_conjuration", new Vec3(10.5, 0, 1.5), PLANT_POINT.add(-2.0, 0, 0), 4, 1,
					60, 50, false, new int[]{24, 40, 51, 54}, StrandClientReview::plantScene),
			new Shot("flame-paint-claimed-third-person", "crimson_flame_conjuration", new Vec3(10.5, 0, 1.5),
					PLANT_POINT.add(-2.0, 0, 0), 4, 1, 60, 50, true, new int[]{40, 51, 54}, StrandClientReview::plantScene),
			new Shot("ice-hand-moving", "glacial_grasp", new Vec3(10.5, 0, 1.5), PLANT_HUSK.add(0, 1.0, 0), 0, 0, 85, 70, false,
					new int[]{20, 30, 45, 60, 71, 74}, StrandClientReview::movingPlantScene),
			new Shot("ice-hand-moving-third-person", "glacial_grasp", new Vec3(10.5, 0, 1.5), PLANT_HUSK.add(0, 1.0, 0), 0, 0, 85,
					70, true, new int[]{22, 32, 45, 60, 72}, StrandClientReview::movingPlantScene),
			new Shot("flare-flash-moving", "hematic_flare", new Vec3(5.5, 0, 1.5), JET_FIRST.add(0, 1.0, 0), 0, 0, 86, 82, false,
					new int[]{30, 60, 83, 85}, StrandClientReview::movingJetScene),
			new Shot("flare-flash-moving-third-person", "hematic_flare", new Vec3(5.5, 0, 1.5), JET_FIRST.add(0, 1.0, 0), 0, 0, 86,
					80, true, new int[]{30, 70, 81, 83}, StrandClientReview::movingJetScene));

	private static List<Shot> shots() {
		if (shots == null) {
			String only = System.getProperty(ONLY, "").trim();
			List<String> prefixes = only.isEmpty() ? List.of() : List.of(only.split(","));
			shots = prefixes.isEmpty() ? ALL
					: ALL.stream().filter(s -> prefixes.stream().anyMatch(p -> s.name().startsWith(p.trim()))).toList();
		}
		return shots;
	}

	/** A claim on the stage that refuses the caster's blocks while a {@code claimed} shot runs. */
	@SubscribeEvent
	public static void claim(net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent event) {
		if (claimed && Boolean.getBoolean(PROPERTY) && event.getEntity() instanceof ServerPlayer) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean(PROPERTY)) return;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("strand-client-review")) return;
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
			if (shot == shots().size()) {
				Hemomancy.LOGGER.info("STRAND_REVIEW captured {}", captured);
				mc.stop();
				return;
			}
			Shot current = shots().get(shot);
			mc.options.setCameraType(CameraType.FIRST_PERSON);
			mc.options.hideGui = false;
			server.execute(() -> pose(server.getPlayerList().getPlayer(uuid), current));
		}
		if (shot >= shots().size()) return;
		Shot current = shots().get(shot);
		if (local == 2) {
			baseYaw = mc.player.getYRot();
			lastOffset = 0.0F;
		}
		if (local == 6 && current.thirdPerson() && cameraId >= 0 && mc.level.getEntity(cameraId) != null) {
			mc.setCameraEntity(mc.level.getEntity(cameraId));
			// The side camera is not the caster's eyes: no first-person hand over the shot.
			mc.gameRenderer.setRenderHand(false);
		}
		boolean skills = current.name().startsWith("skills");
		if (skills) {
			// The skills tab, focused on Long Strand on the surface and on Whipcrack in the deep layer.
			if (local == 14) com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen.openScreen(
					current.name().endsWith("deep") ? 77 : 74);
			// The pointer off to a corner, so no tooltip hides the nodes round the focus.
			if (local == 20) org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), 4.0D, mc.getWindow().getScreenHeight() - 4.0D);
			// Zoomed out on the surface so the whole strand line shows; the deep layer stays put, since zooming out leaves it.
			if (local == 24 && !current.name().endsWith("deep") && mc.screen != null)
				mc.screen.mouseScrolled(mc.getWindow().getGuiScaledWidth() / 2.0D, mc.getWindow().getGuiScaledHeight() / 2.0D, 0.0D, -2.0D);
		}
		boolean tap = current.name().startsWith("lash-tap");
		if (local == PRESS_TICK && !skills && !tap) {
			KeyMapping.click(mc.options.keyUse.getKey());
			KeyMapping.set(mc.options.keyUse.getKey(), true);
		}
		boolean stilled = current.name().startsWith("lash-fade") && local >= 40;
		int sweepFrom = current.name().contains("charged") ? PRESS_TICK + 30 : PRESS_TICK;
		if (current.sweep() > 0 && local > sweepFrom && local < current.release() && !stilled) {
			int t = local - sweepFrom;
			int half = Math.max(1, Math.round(2 * current.sweep() / current.step()));
			int phase = Math.floorMod(t + half / 2, 2 * half);
			float offset = phase < half ? -current.sweep() + phase * current.step() : current.sweep() - (phase - half) * current.step();
			// One tick of mouse, in the units the mouse handler passes: a lash takes it whole for its whip aim.
			com.vincenthuto.hemomancy.client.player.LashCamera.frame(0.05D);
			double units = (offset - lastOffset) / 0.15D;
			boolean vertical = current.name().contains("vertical");
			com.vincenthuto.hemomancy.client.player.LashCamera.turn(mc.player, vertical ? 0.0D : units, vertical ? units : 0.0D);
			mc.player.setYHeadRot(mc.player.getYRot());
			lastOffset = offset;
		}
		for (int snapAt : current.name().startsWith("searing-heat") ? HEAT_SNAPS : new int[]{current.snapAt()}) {
			if (snapAt < 0) continue;
			// A tap is a click the game hands out as one; a snap is read from the held button.
			if (local == snapAt && tap) KeyMapping.click(mc.options.keyAttack.getKey());
			if (local == snapAt) KeyMapping.set(mc.options.keyAttack.getKey(), true);
			if (local == snapAt + 1) KeyMapping.set(mc.options.keyAttack.getKey(), false);
		}
		if (current.name().contains("moving") && movingId >= 0) {
			// Side to side across the caster's look, a block and a half each way.
			double pace = (local / 30) % 2 == 0 ? 0.1D : -0.1D;
			int id = movingId;
			server.execute(() -> {
				ServerPlayer caster = server.getPlayerList().getPlayer(uuid);
				Entity mover = caster == null ? null : caster.serverLevel().getEntity(id);
				if (mover != null) mover.setDeltaMovement(pace, mover.getDeltaMovement().y, 0.0D);
			});
		}
		if (local == current.release()) KeyMapping.set(mc.options.keyUse.getKey(), false);
		for (int at : current.captures()) {
			if (local != at) continue;
			Screenshot.grab(mc.gameDirectory, "strand-" + shot + "-" + current.name() + "-" + at + ".png",
					mc.getMainRenderTarget(), message -> {});
			captured++;
			var strand = com.vincenthuto.hemomancy.client.data.StrandClientState.of(mc.player.getId());
			Hemomancy.LOGGER.info("STRAND_REVIEW shot {} {} at {} using={} strand={} phase={} charge={} extent={} whipOff={}", shot,
					current.name(), at, mc.player.isUsingItem(), strand == null || strand.cue == null ? "none" : strand.form(),
					strand == null ? "-" : strand.lash.phase(), strand == null ? 0 : strand.lash.charge(),
					strand == null ? 0 : strand.lash.extent(),
					strand == null ? 0 : net.minecraft.util.Mth.wrapDegrees(strand.aimYaw - mc.player.getYRot()));
		}
		if (local == SHOT_TICKS - 1) {
			if (skills) mc.setScreen(null);
			claimed = false;
			movingId = -1;
			mc.setCameraEntity(mc.player);
			mc.gameRenderer.setRenderHand(true);
			KeyMapping.set(mc.options.keyUse.getKey(), false);
			KeyMapping.set(mc.options.keyAttack.getKey(), false);
			shot++;
		}
	}

	/** A clear floor, the player's blood and every strand memory, equipped. */
	private static void stage(ServerPlayer player, MinecraftServer server) {
		ServerLevel level = player.serverLevel();
		// High above the save's coast, so the sea cannot run back over the stage between shots.
		row = player.blockPosition().atY(Math.max(player.getBlockY(), 180));
		var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
		for (String cmd : new String[]{"gamemode survival", "difficulty easy", "time set 6000",
				"gamerule doDaylightCycle false", "weather clear", "gamerule doMobSpawning false"})
			server.getCommands().performPrefixedCommand(source, cmd);
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-14, -1, -3), row.offset(14, 9, 13)))
			level.setBlock(pos, pos.getY() < row.getY() ? Blocks.STONE_BRICKS.defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 2);
		level.getEntitiesOfClass(Entity.class, new AABB(row).inflate(20.0D), entity -> !(entity instanceof ServerPlayer))
				.forEach(Entity::discard);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5000.0D);
		volume.setBloodVolume(5000.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		for (EnumBloodTemper school : EnumBloodTemper.values()) temperament.setTemperAlignment(school, 20.0F);
		BloodTemperamentEvents.syncTemperament(player, temperament);
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		List<String> strands = List.of("searing_lash", "blood_lash", "hematic_flare", "blood_eclipse", "glacial_grasp",
				"gloam_laceration", "cruor_javelin", "blood_needle_lance", "synaptic_jolt",
				"crimson_flame_conjuration", "ferric_spikes");
		for (String name : strands) {
			known.getKnownManips().put(ManipulationInit.getByName(name), new ManipLevel(1, 0));
			// A family form opens once its baseline is known to the form's level.
			com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.form(name).ifPresent(form ->
					known.getKnownManips().merge(ManipulationInit.getByName(
							com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.baselineId(name)),
							new ManipLevel(form.requiredLevel(), 0),
							(had, now) -> had.getCurrentLevel() >= now.getCurrentLevel() ? had : now));
		}
		known.setEquippedManipNames(new ArrayList<>(strands));
		known.setActiveVerb(Verb.PROJECT);
		KnownManipulationEvents.syncPlayerEvent(player);
		Hemomancy.LOGGER.info("STRAND_REVIEW staged at {}", row);
	}

	/** Selects, arms, places and aims the player for one shot, and restages its scene. */
	private static void pose(ServerPlayer player, Shot shot) {
		ServerLevel level = player.serverLevel();
		// Every scene starts on a clean floor: no ice, stains or fire left by the last one, and no twin unless it asks.
		boolean recall = shot.name().contains("recall") && !shot.name().contains("stuck");
		// A recall reaches for the javelin the shot before left stuck in the floor, so its floor is left alone.
		if (!recall) clearGround(level);
		twin(player, false);
		movingId = -1;
		if (shot.scene() != null) shot.scene().accept(level, player);
		claimed = shot.name().contains("claimed");
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		known.setSelectedManip(ManipulationInit.getByName(shot.manipulation()));
		known.setActiveVerb(recall ? Verb.ABSORB : Verb.PROJECT);
		KnownManipulationEvents.syncPlayerEvent(player);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setBloodVolume(4500.0D);
		BloodVolumeEvents.syncVolume(player, volume);
		TemperFixtures.carry(player);
		player.setHealth(player.getMaxHealth());
		player.getInventory().selected = 0;
		player.setItemInHand(InteractionHand.MAIN_HAND,
				new ItemStack(recall ? ItemInit.blood_absorption.get() : ItemInit.blood_projection.get()));
		Vec3 eye = at(shot.stand()).add(0.0D, 1.62D, 0.0D);
		Vec3 target = at(shot.aim());
		if (recall) {
			// Aimed at wherever the last shot's javelin really stuck, with no husk left between.
			level.getEntitiesOfClass(Mob.class, new AABB(row).inflate(20.0D)).forEach(Entity::discard);
			target = level.getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.projectile.CruorJavelinEntity.class,
					new AABB(row).inflate(20.0D)).stream().findFirst().map(Entity::position).orElse(target);
		}
		double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
		float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
		player.teleportTo(level, eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
		cameraId = -1;
		level.getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class, new AABB(row).inflate(24.0D))
				.forEach(Entity::discard);
		if (shot.thirdPerson()) {
			// Off to the caster's left and a little behind, looking across at the middle of the strand.
			Vec3 forward = target.subtract(eye).multiply(1, 0, 1).normalize();
			Vec3 left = new Vec3(forward.z, 0, -forward.x);
			Vec3 middle = eye.lerp(target, 0.45D);
			Vec3 camera = middle.add(left.scale(-5.5D)).add(forward.scale(-1.5D)).add(0, 1.2D, 0);
			var stand = new net.minecraft.world.entity.decoration.ArmorStand(level, camera.x, camera.y, camera.z);
			stand.setInvisible(true);
			stand.setNoGravity(true);
			double cx = middle.x - camera.x, cy = middle.y - camera.y - 0.6D, cz = middle.z - camera.z;
			float camYaw = (float) Math.toDegrees(Math.atan2(-cx, cz));
			float camPitch = (float) -Math.toDegrees(Math.atan2(cy, Math.hypot(cx, cz)));
			stand.moveTo(camera.x, camera.y - stand.getEyeHeight(), camera.z, camYaw, camPitch);
			stand.setYHeadRot(camYaw);
			stand.yHeadRotO = camYaw;
			stand.setXRot(camPitch);
			stand.xRotO = camPitch;
			level.addFreshEntity(stand);
			cameraId = stand.getId();
		}
	}

	/** The stage's floor relaid and the air above it cleared. */
	private static void clearGround(ServerLevel level) {
		for (BlockPos pos : BlockPos.betweenClosed(row.offset(-14, -1, -3), row.offset(14, 3, 13)))
			level.setBlock(pos, pos.getY() < row.getY() ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
	}

	/** Second Strand on or off for the shot; the off hand stays empty either way. */
	private static void twin(ServerPlayer player, boolean on) {
		HemoCapabilityAccess.getSkillProgress(player).setSkill(com.vincenthuto.hemomancy.common.init.SkillPointInit.skill_second_strand,
				on ? com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates.UNLOCKED
						: com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates.LOCKED, on ? 1 : 0);
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
	}

	private static void twinScene(ServerLevel level, ServerPlayer player) {
		lashScene(level, player);
		twin(player, true);
	}

	/** Every strand skill at its top level, at Degree 6 so the deep layer opens, synced for the skills tab. */
	private static void skillsScene(ServerLevel level, ServerPlayer player) {
		clearMobs(level);
		var progress = HemoCapabilityAccess.getSkillProgress(player);
		var unlocked = com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates.UNLOCKED;
		progress.setSkill(com.vincenthuto.hemomancy.common.init.SkillPointInit.skill_living_conduit, unlocked, 3);
		progress.setSkill(com.vincenthuto.hemomancy.common.init.SkillPointInit.skill_long_strand, unlocked, 3);
		progress.setSkill(com.vincenthuto.hemomancy.common.init.SkillPointInit.skill_taut_line, unlocked, 2);
		progress.setSkill(com.vincenthuto.hemomancy.common.init.SkillPointInit.skill_free_hand, unlocked, 1);
		progress.setSkill(com.vincenthuto.hemomancy.common.init.SkillPointInit.skill_whipcrack, unlocked, 2);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(6);
		for (com.vincenthuto.hemomancy.common.capability.PlayerStateSync.Entry entry
				: com.vincenthuto.hemomancy.common.capability.PlayerStateSync.Entry.values()) entry.send(player);
	}

	private static void clearMobs(ServerLevel level) {
		level.getEntitiesOfClass(Mob.class, new AABB(row).inflate(20.0D)).forEach(Entity::discard);
		level.getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class, new AABB(row).inflate(30.0D)).forEach(Entity::discard);
	}

	private static Mob still(ServerLevel level, EntityType<? extends Mob> type, Vec3 offset, float yaw) {
		Mob mob = type.create(level);
		if (mob == null) return null;
		Vec3 at = at(offset);
		mob.moveTo(at.x, row.getY(), at.z, yaw, 0.0F);
		mob.setNoAi(true);
		mob.setPersistenceRequired();
		mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(300.0D);
		mob.setHealth(300.0F);
		level.addFreshEntity(mob);
		return mob;
	}

	/** A shallow pool sunk into the floor at the plant point, a husk wading in the far end of it. */
	private static void poolScene(ServerLevel level, ServerPlayer player) {
		clearMobs(level);
		BlockPos centre = BlockPos.containing(at(PLANT_POINT));
		for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-2, 0, -1), centre.offset(2, 0, 3)))
			level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
		for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-2, -1, -1), centre.offset(2, -1, 3)))
			level.setBlock(pos, Blocks.STONE_BRICKS.defaultBlockState(), 2);
		Mob wader = still(level, EntityType.HUSK, new Vec3(PLANT_POINT.x, -1.0, PLANT_POINT.z + 2.5), 180.0F);
		// A still mob does not sink: stood on the pool's floor by hand.
		if (wader != null) wader.setPos(wader.getX(), row.getY() - 1.0D, wader.getZ());
	}

	private static void lashScene(ServerLevel level, ServerPlayer player) {
		clearMobs(level);
		still(level, EntityType.HUSK, LASH_HUSK, 180.0F);
	}

	private static void jetScene(ServerLevel level, ServerPlayer player) {
		clearMobs(level);
		still(level, EntityType.HUSK, JET_FIRST, 180.0F);
		still(level, EntityType.HUSK, JET_BEHIND, 180.0F);
	}

	private static void sprayScene(ServerLevel level, ServerPlayer player) {
		clearMobs(level);
		still(level, EntityType.HUSK, SPRAY_CENTRE, 180.0F);
		still(level, EntityType.HUSK, SPRAY_CENTRE.add(-1.3, 0, 0.3), 180.0F);
		still(level, EntityType.HUSK, SPRAY_CENTRE.add(1.3, 0, 0.3), 180.0F);
	}

	private static void plantScene(ServerLevel level, ServerPlayer player) {
		clearMobs(level);
		still(level, EntityType.HUSK, PLANT_HUSK, 180.0F);
	}

	/** The lash husk with its back to the caster: every Gloam cut on it is an ambush. */
	private static void backScene(ServerLevel level, ServerPlayer player) {
		clearMobs(level);
		still(level, EntityType.HUSK, LASH_HUSK, 0.0F);
	}

	/** Three husks side by side across the lash's arc, for a charged Gloam throw to cut through. */
	private static void rowScene(ServerLevel level, ServerPlayer player) {
		clearMobs(level);
		still(level, EntityType.HUSK, LASH_HUSK.add(-1.6, 0, -1.0), 180.0F);
		still(level, EntityType.HUSK, LASH_HUSK.add(0, 0, -1.0), 180.0F);
		still(level, EntityType.HUSK, LASH_HUSK.add(1.6, 0, -1.0), 180.0F);
	}

	private static void movingPlantScene(ServerLevel level, ServerPlayer player) {
		clearMobs(level);
		Mob mover = still(level, EntityType.HUSK, PLANT_HUSK, 180.0F);
		if (mover != null) movingId = mover.getId();
	}

	private static void movingJetScene(ServerLevel level, ServerPlayer player) {
		clearMobs(level);
		Mob mover = still(level, EntityType.HUSK, JET_FIRST, 180.0F);
		if (mover != null) movingId = mover.getId();
	}

	private static Vec3 at(Vec3 offset) {
		return new Vec3(row.getX() + offset.x, row.getY() + offset.y, row.getZ() + offset.z);
	}
}
