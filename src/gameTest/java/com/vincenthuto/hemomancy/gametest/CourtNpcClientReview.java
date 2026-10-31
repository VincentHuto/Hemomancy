package com.vincenthuto.hemomancy.gametest;

import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentMarkers;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentSpawner;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatCandidates;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatLayout;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtStructure;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicWorldgen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * With hemomancy.courtNpcReview, creates a fresh seeded normal world (Pelagic worldgen on) and captures every built
 * Court role. The stage pass builds a platform with a water tank high over the origin and sets the roles down shallow
 * to deep: the lineup (by day and by night, for the biological light), then each role from the front, side and back,
 * speaking, walking, standing on the tank floor and swimming in open water, plus the anchored and withdrawn arbors. The
 * home pass generates the nearest Lighthouse Garden and Vent Sanctum where they stand and shoots each resident on its
 * mark, as the structure spawner set it down. Screenshots go to {@code <gameDir>/screenshots/<folder>/<nn>-<name>.png}
 * (gameDir build/court-npc-review). Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CourtNpcClientReview {
	private static final String WORLD = "CourtNpcReview";
	private static final int FIRST_WAIT = 160;
	private static final int WAIT = 30;
	private static final int STAGE_Y = 150;
	/** Shallow to deep: every house's roles in depth order, the Giving Light first and the Kindling last. */
	private static final List<CourtRole> LINEUP = List.of(CourtRole.GARDENER, CourtRole.ARBOR, CourtRole.LIGHTKEEPER,
			CourtRole.TIDEWARDEN, CourtRole.BRACED_ELDER, CourtRole.EDGE_SWIMMER, CourtRole.NURSERY_TENDER,
			CourtRole.LINESMAN, CourtRole.LAMP_COLONY, CourtRole.HOST, CourtRole.VEILED_RESIDENT, CourtRole.BELLWARDEN,
			CourtRole.SLEEPER, CourtRole.MARROW_TENDER, CourtRole.ROOTED_ELDER,
			CourtRole.OFFERING_BEARER, CourtRole.HEAD_TENDER, CourtRole.KINDLED_ELDER);
	/** The school's open-water tank, west of the stage: a loop this wide fits inside it. */
	private static final int SCHOOL_RADIUS = 18;

	/** One capture; {@code action} runs on the server before it (posing, speaking, moving), or null. */
	private record Shot(String folder, String name, Vec3 eye, Vec3 look, long time, boolean nightVision, Runnable action) {
	}

	private enum Phase { CREATE, ENTER, BUILD, SHOOT, DONE }

	private static Phase phase = Phase.CREATE;
	private static int ticks;
	private static int shot;
	private static int shotTick;
	private static volatile List<Shot> shots;
	/** Members walking a beat on the stage; nudged back and forth from the server thread. */
	private static final List<CourtMemberEntity> walkers = new ArrayList<>();

	private static long seed() {
		return Long.getLong("hemomancy.courtNpcReview.seed", 42L);
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.courtNpcReview")) return;
		var mc = Minecraft.getInstance();
		switch (phase) {
			case CREATE -> {
				if (mc.level == null && mc.getOverlay() == null
						&& (mc.screen instanceof TitleScreen || mc.screen instanceof AccessibilityOnboardingScreen)) {
					mc.options.onboardAccessibility = false;
					mc.options.save();
					createWorld(mc);
					phase = Phase.ENTER;
				}
			}
			case ENTER -> {
				if (mc.player != null && mc.getSingleplayerServer() != null && ++ticks > 40) {
					mc.options.pauseOnLostFocus = false;
					mc.options.hideGui = true;
					var server = mc.getSingleplayerServer();
					var uuid = mc.player.getUUID();
					phase = Phase.BUILD;
					server.execute(() -> {
						var player = server.getPlayerList().getPlayer(uuid);
						var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
						for (String cmd : new String[] {"gamemode spectator", "gamerule doDaylightCycle false",
								"weather clear", "gamerule doWeatherCycle false", "gamerule doMobSpawning false"})
							server.getCommands().performPrefixedCommand(source, cmd);
						var out = new ArrayList<Shot>();
						stage(player.serverLevel(), player, out);
						school(player.serverLevel(), player, out);
						deepPoses(player.serverLevel(), out);
						home(player.serverLevel(), out);
						shots = out;
						Hemomancy.LOGGER.info("COURT_NPC_REVIEW {} shots queued", out.size());
					});
				}
			}
			case BUILD -> {
				if (shots != null) {
					phase = shots.isEmpty() ? Phase.DONE : Phase.SHOOT;
					shotTick = 0;
				}
			}
			case SHOOT -> shoot(mc);
			case DONE -> {
				Hemomancy.LOGGER.info("COURT_NPC_REVIEW captured {}", shot);
				mc.stop();
			}
		}
	}

	private static void shoot(Minecraft mc) {
		Shot current = shots.get(shot);
		boolean first = shot == 0 || !shots.get(shot - 1).folder().equals(current.folder());
		int wait = first ? FIRST_WAIT : WAIT;
		var server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (shotTick % 10 == 0) server.execute(CourtNpcClientReview::pace);
		if (shotTick++ == 0) {
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				player.serverLevel().setDayTime(current.time());
				if (current.nightVision()) {
					player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60, 0, false, false));
				} else {
					player.removeEffect(MobEffects.NIGHT_VISION);
				}
				Vec3 eye = current.eye();
				Vec3 delta = current.look().subtract(eye);
				float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
				float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance()));
				player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
				if (current.action() != null) current.action().run();
			});
			return;
		}
		if (shotTick < wait) return;
		Path out = mc.gameDirectory.toPath().resolve("screenshots").resolve(current.folder()).resolve(current.name() + ".png");
		try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
			Files.createDirectories(out.getParent());
			image.writeToFile(out);
		} catch (IOException exception) {
			Hemomancy.LOGGER.error("COURT_NPC_REVIEW could not save {}", out, exception);
		}
		Hemomancy.LOGGER.info("COURT_NPC_REVIEW shot {}/{}", current.folder(), current.name());
		shotTick = 0;
		if (++shot >= shots.size()) phase = Phase.DONE;
	}

	/** Walkers turn at the ends of their beat. */
	private static void pace() {
		for (CourtMemberEntity walker : walkers) {
			if (walker.isRemoved() || !walker.getNavigation().isDone()) continue;
			BlockPos home = walker.home();
			double target = walker.getX() > home.getX() + 0.5D ? home.getX() - 4.5D : home.getX() + 5.5D;
			walker.getNavigation().moveTo(target, walker.getY(), home.getZ() + 0.5D, 0.7D);
		}
	}

	// ---------------------------------------------------------------- the stage

	/**
	 * A stone platform 4 wide per role with a three-deep glass tank of water behind it, high in clear air. Each role
	 * stands on the platform; a copy stands on the tank floor; the mobile roles get a walker on a beat in front and a
	 * swimmer held off the floor in the tank.
	 */
	private static void stage(ServerLevel level, Player camera, List<Shot> out) {
		int x0 = 0, z0 = 0, width = LINEUP.size() * 5 + 6, walkX = x0 + width + 14;
		// Each mobile role walks its own 13-block beat east of the stage.
		int walkEnd = walkX + (int) LINEUP.stream().filter(role -> !role.anchored()).count() * 13 + 8;
		for (int cx = (x0 - 8) >> 4; cx <= (walkEnd + 4) >> 4; cx++) for (int cz = (z0 - 16) >> 4; cz <= (z0 + 24) >> 4; cz++) {
			level.getChunk(cx, cz);
		}
		for (int x = x0 - 2; x <= walkEnd; x++) {
			for (int z = z0 - 12; z <= z0 + 14; z++) level.setBlock(new BlockPos(x, STAGE_Y - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
		}
		for (int x = x0 - 2; x <= x0 + width; x++) {
			for (int z = z0 + 6; z <= z0 + 12; z++) for (int y = STAGE_Y; y <= STAGE_Y + 4; y++) {
				boolean wall = z == z0 + 6 || z == z0 + 12 || x == x0 - 2 || x == x0 + width;
				level.setBlock(new BlockPos(x, y, z), wall ? Blocks.GLASS.defaultBlockState() : Blocks.WATER.defaultBlockState(), 2);
			}
		}
		Vec3 mid = new Vec3(x0 + width / 2.0D, STAGE_Y, z0 + 0.5D);
		List<CourtMemberEntity> lineup = new ArrayList<>();
		for (int i = 0; i < LINEUP.size(); i++) {
			CourtRole role = LINEUP.get(i);
			if (!EntityInit.court_members.containsKey(role)) continue;
			int x = x0 + width - 3 - i * 5;
			lineup.add(member(level, role, new BlockPos(x, STAGE_Y, z0), Direction.NORTH, true));
			member(level, role, new BlockPos(x, STAGE_Y, z0 + 9), Direction.NORTH, true);
		}
		// The lineup, shallow to deep, the camera far enough back to frame the whole row.
		double back = Math.max(13.0D, width * 0.5D);
		for (var pass : List.of(new Object[] {"day", 6000L, false}, new Object[] {"night", 18000L, false},
				new Object[] {"night_vision", 18000L, true})) {
			out.add(new Shot("lineup", pass[0] + "-front", mid.add(0, 2.2 + back * 0.08, -back), mid.add(0, 1.2, 0), (Long) pass[1], (Boolean) pass[2], null));
		}
		out.add(new Shot("lineup", "day-back", mid.add(0, 2.4, 5), mid.add(0, 1.2, 0), 6000L, false, null));
		out.add(new Shot("lineup", "day-in_water", mid.add(0, 3.0, 2.5), mid.add(0, 1.0, 9), 6000L, false, null));
		// Each role close up: front, side, back, then speaking.
		for (CourtMemberEntity member : lineup) {
			String folder = "role_" + member.role().id();
			Vec3 feet = member.position();
			double tall = member.getBbHeight();
			Vec3 chest = feet.add(0, tall * 0.62, 0);
			double reach = 2.4 + tall * 0.8;
			// A Veiled resident folds away when still, so the close-ups stir it first; the folded pose has its own shot.
			Runnable stir = member.role() == CourtRole.VEILED_RESIDENT ? () -> member.stir(FIRST_WAIT + 40) : null;
			out.add(new Shot(folder, "00-front", feet.add(0, tall * 0.7, -reach), chest, 6000L, false, stir));
			out.add(new Shot(folder, "01-side", feet.add(reach, tall * 0.7, 0), chest, 6000L, false, stir));
			out.add(new Shot(folder, "02-back", feet.add(0, tall * 0.85, reach), chest, 6000L, false, stir));
			out.add(new Shot(folder, "03-speaking", feet.add(-reach * 0.6, tall * 0.7, -reach * 0.8), chest, 6000L, false,
					() -> member.speakTo(camera)));
			out.add(new Shot(folder, "04-night_glow", feet.add(0, tall * 0.7, -reach), chest, 18000L, false, stir));
			out.add(new Shot(folder, "05-tank_floor", feet.add(0, tall * 0.6 + 0.4, 9 - reach), feet.add(0, tall * 0.5, 9), 6000L, false, null));
		}
		// Walking on land and swimming in open water, for the mobile roles.
		int beat = 0;
		for (CourtMemberEntity member : lineup) {
			if (member.role().anchored()) continue;
			BlockPos walkHome = new BlockPos(walkX + beat * 13, STAGE_Y, z0);
			CourtMemberEntity walker = member(level, member.role(), walkHome, Direction.EAST, false);
			walkers.add(walker);
			Vec3 at = Vec3.atBottomCenterOf(walkHome);
			out.add(new Shot("moving", String.format(Locale.ROOT, "%02d-walk-%s", beat, member.role().id()),
					at.add(0, 2.0, -6.5), at.add(0, 1.0, 0), 6000L, false, null));
			beat++;
		}
		int swim = 0;
		for (CourtMemberEntity member : lineup) {
			if (member.role().anchored()) continue;
			int x = x0 + 2 + swim * 5;
			CourtMemberEntity swimmer = member(level, member.role(), new BlockPos(x, STAGE_Y + 2, z0 + 10), Direction.EAST, false);
			swimmer.setNoGravity(true);
			swimmer.restrictTo(swimmer.home(), 3);
			Vec3 at = swimmer.position();
			out.add(new Shot("moving", String.format(Locale.ROOT, "%02d-swim-%s", 10 + swim, member.role().id()),
					at.add(0.5, 0.8, -4.0), at.add(0, 0.5, 0), 6000L, false,
					() -> swimmer.getMoveControl().setWantedPosition(swimmer.getX() + 2.5D, swimmer.getY(), swimmer.getZ(), 0.6D)));
			swim++;
		}
		// The anchored arbor and one that has withdrawn.
		if (EntityInit.court_members.containsKey(CourtRole.ARBOR)) {
			BlockPos at = new BlockPos(x0 + width - 4, STAGE_Y, z0 - 4);
			CourtMemberEntity withdrawn = member(level, CourtRole.ARBOR, at, Direction.NORTH, true);
			withdrawn.settle(at, Direction.NORTH.toYRot(), "arbor_withdrawing");
			withdrawn.tickCount = CourtMemberEntity.WITHDRAW_CYCLE + 1;
			Vec3 feet = Vec3.atBottomCenterOf(at);
			out.add(new Shot("anchored", "00-withdrawn", feet.add(0.8, 1.6, -3.2), feet.add(0, 0.9, 0), 6000L, false, null));
			out.add(new Shot("anchored", "01-withdrawn_side", feet.add(3.0, 1.4, 0.2), feet.add(0, 0.9, 0), 6000L, false, null));
		}
		// The Braced elder holding, and letting go (folded across the doorway, as near a shunned visitor). Its mark is
		// given the withdrawing suffix here only, so the stage can show the closed pose without an Unstained player.
		if (EntityInit.court_members.containsKey(CourtRole.BRACED_ELDER)) {
			BlockPos at = new BlockPos(x0 + width - 9, STAGE_Y, z0 - 4);
			CourtMemberEntity closed = member(level, CourtRole.BRACED_ELDER, at, Direction.NORTH, true);
			closed.settle(at, Direction.NORTH.toYRot(), "holdfast_braced_withdrawing");
			closed.tickCount = CourtMemberEntity.WITHDRAW_CYCLE + 1;
			Vec3 feet = Vec3.atBottomCenterOf(at);
			out.add(new Shot("anchored", "02-braced_elder_closed", feet.add(0.8, 1.8, -3.4), feet.add(0, 1.0, 0), 6000L, false, null));
			out.add(new Shot("anchored", "03-braced_elder_closed_side", feet.add(3.2, 1.6, 0.2), feet.add(0, 1.0, 0), 6000L, false, null));
		}
	}

	/**
	 * The deeper houses' held poses: a Veiled resident left still until it folds away (drawn faint), a Sleeper hung
	 * sealed from a bone hook, and a Linesman tied in to a Lamp-colony hung in the lines, the strand between them.
	 */
	private static void deepPoses(ServerLevel level, List<Shot> out) {
		int x0 = -30, z0 = -20;
		for (int cx = (x0 - 6) >> 4; cx <= (x0 + 22) >> 4; cx++) for (int cz = (z0 - 6) >> 4; cz <= (z0 + 6) >> 4; cz++) level.getChunk(cx, cz);
		for (int x = x0 - 6; x <= x0 + 22; x++) for (int z = z0 - 6; z <= z0 + 6; z++) {
			level.setBlock(new BlockPos(x, STAGE_Y - 1, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
		}
		if (EntityInit.court_members.containsKey(CourtRole.VEILED_RESIDENT)) {
			BlockPos at = new BlockPos(x0, STAGE_Y, z0);
			member(level, CourtRole.VEILED_RESIDENT, at, Direction.NORTH, true);
			Vec3 feet = Vec3.atBottomCenterOf(at);
			out.add(new Shot("deep", "00-veiled_resident_folded", feet.add(0.6, 1.6, -3.2), feet.add(0, 0.7, 0), 18000L, true, null));
			out.add(new Shot("deep", "01-veiled_resident_folded_side", feet.add(3.0, 1.3, 0.2), feet.add(0, 0.7, 0), 6000L, false, null));
		}
		if (EntityInit.court_members.containsKey(CourtRole.SLEEPER)) {
			BlockPos hook = new BlockPos(x0 + 8, STAGE_Y + 3, z0);
			level.setBlock(hook.above(), Blocks.BONE_BLOCK.defaultBlockState(), 2);
			member(level, CourtRole.SLEEPER, hook, Direction.NORTH, true);
			Vec3 feet = Vec3.atBottomCenterOf(hook);
			out.add(new Shot("deep", "02-sleeper_hung", feet.add(0.6, 0.6, -3.4), feet.add(0, 0.4, 0), 6000L, false, null));
			out.add(new Shot("deep", "03-sleeper_hung_side", feet.add(3.2, 0.2, 0.4), feet.add(0, 0.4, 0), 18000L, true, null));
		}
		if (EntityInit.court_members.containsKey(CourtRole.LAMP_COLONY) && EntityInit.court_members.containsKey(CourtRole.LINESMAN)) {
			BlockPos pod = new BlockPos(x0 + 16, STAGE_Y + 3, z0);
			member(level, CourtRole.LAMP_COLONY, pod, Direction.WEST, true);
			BlockPos stand = new BlockPos(x0 + 12, STAGE_Y, z0 - 2);
			member(level, CourtRole.LINESMAN, stand, Direction.EAST, true);
			Vec3 mid = Vec3.atBottomCenterOf(pod).add(-2.0, -1.0, 0);
			out.add(new Shot("deep", "04-woven_link", mid.add(0, 1.0, -7.0), mid.add(0, 0.8, 0), 18000L, false, null));
			out.add(new Shot("deep", "05-woven_link_day", mid.add(-1.5, 1.4, -6.5), mid.add(0, 0.8, 0), 6000L, false, null));
		}
	}

	/**
	 * The Shared Wake's school in motion: a wide, shallow tank of open water west of the stage and a school launched in
	 * it on a small loop. Under-water shots from three corners a little apart show it going round together; chase shots
	 * put the camera on the loop ahead of the leader, in the water, looking back at the school coming on.
	 */
	private static void school(ServerLevel level, net.minecraft.server.level.ServerPlayer camera, List<Shot> out) {
		if (!EntityInit.court_members.containsKey(CourtRole.EDGE_SWIMMER)) return;
		int cx = -70, cz = 0, half = SCHOOL_RADIUS + 8, floor = STAGE_Y - 6;
		for (int x = (cx - half - 1) >> 4; x <= (cx + half + 1) >> 4; x++) for (int z = (cz - half - 1) >> 4; z <= (cz + half + 1) >> 4; z++) {
			level.getChunk(x, z);
		}
		for (int x = cx - half - 1; x <= cx + half + 1; x++) for (int z = cz - half - 1; z <= cz + half + 1; z++) {
			boolean wall = Math.abs(x - cx) > half || Math.abs(z - cz) > half;
			level.setBlock(new BlockPos(x, floor - 1, z), Blocks.SAND.defaultBlockState(), 2);
			for (int y = floor; y <= STAGE_Y + 2; y++) {
				level.setBlock(new BlockPos(x, y, z), wall ? Blocks.GLASS.defaultBlockState() : Blocks.WATER.defaultBlockState(), 2);
			}
		}
		BlockPos centre = new BlockPos(cx, STAGE_Y - 2, cz);
		var school = com.vincenthuto.hemomancy.common.entity.npc.court.CourtSchools.launch(level, centre, SCHOOL_RADIUS,
				level.getRandom(), net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED);
		Hemomancy.LOGGER.info("COURT_NPC_REVIEW SCHOOL launched {} at {}", school.size(), centre);
		if (school.isEmpty()) return;
		CourtMemberEntity leader = school.getFirst();
		Vec3 mid = Vec3.atCenterOf(centre);
		// Under water from three corners of the tank, a little apart in time (night vision for the water's murk).
		double corner = SCHOOL_RADIUS + 6;
		double[][] corners = {{-corner, -corner}, {corner, -corner}, {corner, corner}};
		for (int i = 0; i < 3; i++) {
			out.add(new Shot("school", String.format(Locale.ROOT, "%02d-tank_corner", i), mid.add(corners[i][0], 0.5, corners[i][1]),
					mid.add(0, -0.5, 0), 6000L, true, null));
		}
		for (int i = 0; i < 4; i++) {
			out.add(new Shot("school", String.format(Locale.ROOT, "%02d-chase", 3 + i), mid.add(0, -1, -20), mid, 6000L, true,
					() -> {
						// The next waypoint on from the leader, at its depth, looking back at it.
						double[] ahead = com.vincenthuto.hemomancy.common.entity.npc.court.CourtSchoolRules.waypoint(
								leader.schoolCentre().getX() + 0.5D, leader.schoolCentre().getZ() + 0.5D, leader.schoolRadius(),
								leader.schoolWaypoint() + 1);
						Vec3 eye = new Vec3(ahead[0], leader.getY() + 1.5D, ahead[1]);
						Vec3 delta = leader.position().subtract(eye);
						float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
						float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance()));
						camera.teleportTo(level, eye.x, eye.y - camera.getEyeHeight(), eye.z, yaw, pitch);
					}));
		}
		out.add(new Shot("school", "07-underwater", mid.add(-SCHOOL_RADIUS - 4, -1.5, 0), mid.add(0, -1.5, 0), 6000L, true, null));
	}

	private static CourtMemberEntity member(ServerLevel level, CourtRole role, BlockPos at, Direction facing, boolean still) {
		CourtResidentSpawner.place(level, role, at, facing.toYRot(), role.marks().isEmpty() ? "threshold" : role.marks().getFirst());
		CourtMemberEntity member = level.getEntitiesOfClass(CourtMemberEntity.class, new net.minecraft.world.phys.AABB(at).inflate(1.0D),
				m -> m.role() == role).stream().max(Comparator.comparingInt(CourtMemberEntity::getId)).orElseThrow();
		// Still members hold their spot for the camera; the pose animation keeps running without AI.
		if (still && !role.anchored()) member.setNoAi(true);
		return member;
	}

	// ---------------------------------------------------------------- the Seats

	/** Generates the nearest Garden and Sanctum and shoots each resident on its mark (plus the Head-tender). */
	private static void home(ServerLevel level, List<Shot> out) {
		if (PelagicWorldgen.context(level.getChunkSource().randomState()) == null) {
			Hemomancy.LOGGER.error("COURT_NPC_REVIEW MISSING Pelagic worldgen is off in the review world");
			return;
		}
		for (var site : List.of(CourtSite.LIGHTHOUSE_GARDEN, CourtSite.VENT_SANCTUM)) {
			var marker = CourtSeatCandidates.nearest(level, site, BlockPos.ZERO, 32).orElse(null);
			if (marker == null) {
				Hemomancy.LOGGER.error("COURT_NPC_REVIEW MISSING no {} within 32 rings", site.id());
				continue;
			}
			var structureKey = ResourceKey.create(Registries.STRUCTURE, Hemomancy.rloc(site.id()));
			var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getOrThrow(structureKey);
			level.getChunk(marker);
			StructureStart start = level.structureManager().getStructureWithPieceAt(marker, structure);
			if (!start.isValid()) {
				Hemomancy.LOGGER.error("COURT_NPC_REVIEW MISSING {} start round {}", site.id(), marker);
				continue;
			}
			var box = start.getBoundingBox();
			for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++)
				for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) level.getChunk(cx, cz);
			String folder = "home_" + site.id();
			int n = 0;
			var marks = new ArrayList<>(CourtResidentMarkers.read(level.getStructureManager(), start.getPieces()).stream()
					.filter(CourtResidentMarkers.Marker::resident).filter(m -> CourtRole.forMark(m.name()) != null).toList());
			marks.sort(Comparator.comparing(CourtResidentMarkers.Marker::name).thenComparing(m -> m.pos().getY()));
			for (var mark : marks) {
				out.add(resident(folder, n++, mark.name(), mark.pos(), mark.facing()));
			}
			if (site == CourtSite.VENT_SANCTUM) {
				var geology = PelagicWorldgen.context(level.getChunkSource().randomState());
				var startPiece = start.getPieces().getFirst().getBoundingBox();
				var heads = CourtStructure.heads(geology, startPiece);
				BlockPos threshold = CourtResidentSpawner.threshold(heads, CourtSeatLayout.sanctumMarker(startPiece));
				if (threshold != null) {
					BlockPos centre = CourtSeatLayout.sanctumMarker(startPiece);
					Direction toward = Direction.getNearest(centre.getX() - threshold.getX(), 0, centre.getZ() - threshold.getZ());
					out.add(resident(folder, n++, "head_tender_threshold", threshold, toward));
				}
			}
			Hemomancy.LOGGER.info("COURT_NPC_REVIEW HOME {} marker={} residents={}", site.id(), marker, n);
		}
		for (var site : CourtSite.values()) {
			if (site.tier() == CourtSite.Tier.OUTPOST && CourtRole.of(com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse
					.byId(site.house())).stream().anyMatch(EntityInit.court_members::containsKey)) {
				outpostHome(level, site, out);
			}
		}
	}

	/**
	 * The nearest naturally generated outpost of a house whose roles are built: predicted outpost cells nearest first,
	 * each confirmed against the real start (the set's exclusion zone can refuse a predicted cell), then every resident
	 * on its mark. Outposts placed by hand would skip the structure's afterPlace and stand empty, so a site with no
	 * natural start in range is reported missing instead.
	 */
	private static void outpostHome(ServerLevel level, CourtSite site, List<Shot> out) {
		var source = level.getChunkSource();
		var probe = com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtWorldProbe.of(level.registryAccess(),
				source.getGenerator().getBiomeSource(), source.randomState(), level.getSeed());
		var set = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(CourtSeatCandidates.OUTPOSTS);
		if (probe == null || set == null) return;
		var spread = com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtLandmarkCells.Spread.of(
				(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement) set.placement(),
				level.getSeed(), 0, 0);
		var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
				.getOrThrow(ResourceKey.create(Registries.STRUCTURE, Hemomancy.rloc(site.id())));
		int tries = 0;
		for (int ring = 0; ring <= 24 && tries < 8; ring++) {
			for (int i = -ring; i <= ring && tries < 8; i++) for (int j = -ring; j <= ring && tries < 8; j++) {
				if (Math.max(Math.abs(i), Math.abs(j)) != ring) continue;
				var chunk = spread.potential().of(i, j);
				var stub = com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtPlacementRules.stub(site, probe,
						level.getSeed(), chunk.getMiddleBlockX(), chunk.getMiddleBlockZ());
				if (stub.isEmpty()) continue;
				tries++;
				level.getChunk(stub.get());
				StructureStart start = level.structureManager().getStructureWithPieceAt(stub.get(), structure);
				if (!start.isValid()) continue;
				var box = start.getBoundingBox();
				for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++)
					for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) level.getChunk(cx, cz);
				String folder = "home_" + site.id();
				int n = 0;
				var marks = new ArrayList<>(CourtResidentMarkers.read(level.getStructureManager(), start.getPieces()).stream()
						.filter(CourtResidentMarkers.Marker::resident).filter(m -> CourtRole.forMark(m.name()) != null).toList());
				marks.sort(Comparator.comparing(CourtResidentMarkers.Marker::name).thenComparing(m -> m.pos().getY()));
				for (var mark : marks) out.add(resident(folder, n++, mark.name(), mark.pos(), mark.facing()));
				Hemomancy.LOGGER.info("COURT_NPC_REVIEW HOME {} marker={} residents={}", site.id(), stub.get(), n);
				return;
			}
		}
		Hemomancy.LOGGER.error("COURT_NPC_REVIEW MISSING no natural {} within range", site.id());
	}

	private static Shot resident(String folder, int n, String name, BlockPos pos, Direction facing) {
		Vec3 feet = Vec3.atBottomCenterOf(pos);
		Vec3 eye = feet.add(facing.getStepX() * 3.2D, 1.7D, facing.getStepZ() * 3.2D)
				.add(facing.getClockWise().getStepX() * 0.8D, 0, facing.getClockWise().getStepZ() * 0.8D);
		return new Shot(folder, String.format(Locale.ROOT, "%02d-%s", n, name), eye, feet.add(0, 1.1D, 0), 6000L, true, null);
	}

	private static void createWorld(Minecraft mc) {
		Path probe = mc.gameDirectory.toPath().resolve("config/theoneprobe-common.toml");
		try {
			if (Files.exists(probe)) Files.writeString(probe, Files.readString(probe).replace("needsProbe = 3", "needsProbe = 2"));
		} catch (IOException exception) {
			Hemomancy.LOGGER.warn("COURT_NPC_REVIEW could not hide The One Probe's tooltip", exception);
		}
		Path save = mc.gameDirectory.toPath().resolve("saves").resolve(WORLD);
		if (Files.exists(save)) {
			try (Stream<Path> files = Files.walk(save)) {
				for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
			} catch (IOException exception) {
				throw new IllegalStateException("Could not clear the old review world " + save, exception);
			}
		}
		mc.createWorldOpenFlows().createFreshLevel(WORLD,
				new LevelSettings(WORLD, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, new GameRules(),
						WorldDataConfiguration.DEFAULT),
				new WorldOptions(seed(), true, false), WorldPresets::createNormalWorldDimensions, mc.screen);
	}
}
