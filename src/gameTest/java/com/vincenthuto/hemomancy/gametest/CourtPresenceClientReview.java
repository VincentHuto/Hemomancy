package com.vincenthuto.hemomancy.gametest;

import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenEntity;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionEntity;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionRules;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtPresences;
import com.vincenthuto.hemomancy.common.entity.npc.court.HydraRisingHead;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.MarimoHeartBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatCandidates;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatLayout;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.VentSanctumHeads;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicWorldgen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * With hemomancy.courtPresenceReview (Brined Court Track 10), creates a fresh seeded world, generates the nearest
 * Lighthouse Garden and Vent Sanctum, and shoots their presences: Marimo's moss mass and heart in the undercroft at
 * rest, through his first-sight scene (title card in), and in a pulse; then Hydra's crowned chimneys, and its largest
 * head rising from the tallest chimney through its scene, reaching over and hanging its crown over the player, and
 * withdrawing. Then each follower's fight against the review player (Tier 2): the Bloom Warden's opening, rooted among
 * its blooms, each of its moves, uprooted and re-anchoring; and the Kindled Champion's opening, each move, both bud
 * stages, the vents, its yielding and the head crowning the victor. Screenshots go to {@code <gameDir>/screenshots/<site>/<nn>-<name>.png} (gameDir
 * build/court-presence-review). Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CourtPresenceClientReview {
	private static final String WORLD = "CourtPresenceReview";
	private static final int SETTLE = 120;

	/** One screenshot: where the camera stands (null to stay), what it does first, and how long it waits to shoot. */
	private record Step(String folder, String name, @Nullable Vec3 eye, @Nullable Vec3 look, boolean nightVision,
			boolean gui, int delay, @Nullable Consumer<ServerPlayer> action) {
	}

	private enum Phase { CREATE, ENTER, BUILD, SHOOT, DONE }

	private static Phase phase = Phase.CREATE;
	private static int ticks;
	private static int step;
	private static int stepTick;
	private static volatile List<Step> steps;

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.courtPresenceReview")) return;
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
					var server = mc.getSingleplayerServer();
					var uuid = mc.player.getUUID();
					phase = Phase.BUILD;
					server.execute(() -> {
						var player = server.getPlayerList().getPlayer(uuid);
						var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
						for (String cmd : new String[] {"gamemode spectator", "gamerule doDaylightCycle false", "time set 18000",
								"weather clear", "gamerule doWeatherCycle false", "gamerule doMobSpawning false"})
							server.getCommands().performPrefixedCommand(source, cmd);
						steps = plan(player.serverLevel());
						Hemomancy.LOGGER.info("COURT_PRESENCE_REVIEW {} shots queued", steps.size());
					});
				}
			}
			case BUILD -> {
				if (steps != null) {
					phase = steps.isEmpty() ? Phase.DONE : Phase.SHOOT;
					stepTick = 0;
				}
			}
			case SHOOT -> shoot(mc);
			case DONE -> {
				Hemomancy.LOGGER.info("COURT_PRESENCE_REVIEW captured {}", step);
				mc.stop();
			}
		}
	}

	private static void shoot(Minecraft mc) {
		Step current = steps.get(step);
		if (stepTick++ == 0) {
			mc.options.hideGui = !current.gui();
			var server = mc.getSingleplayerServer();
			var uuid = mc.player.getUUID();
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				if (current.nightVision()) {
					player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 120, 0, false, false));
				} else {
					player.removeEffect(MobEffects.NIGHT_VISION);
				}
				if (current.eye() != null && current.look() != null) {
					Vec3 eye = current.eye();
					Vec3 delta = current.look().subtract(eye);
					float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
					float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance()));
					player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
				}
				if (current.action() != null) current.action().accept(player);
			});
			return;
		}
		if (stepTick < current.delay()) return;
		Path out = mc.gameDirectory.toPath().resolve("screenshots").resolve(current.folder())
				.resolve(String.format(java.util.Locale.ROOT, "%02d-%s.png", step, current.name()));
		try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
			Files.createDirectories(out.getParent());
			image.writeToFile(out);
		} catch (IOException exception) {
			Hemomancy.LOGGER.error("COURT_PRESENCE_REVIEW could not save {}", out, exception);
		}
		Hemomancy.LOGGER.info("COURT_PRESENCE_REVIEW shot {}/{}", current.folder(), current.name());
		stepTick = 0;
		if (++step >= steps.size()) phase = Phase.DONE;
	}

	private static void createWorld(Minecraft mc) {
		Path probe = mc.gameDirectory.toPath().resolve("config/theoneprobe-common.toml");
		try {
			if (Files.exists(probe)) Files.writeString(probe, Files.readString(probe).replace("needsProbe = 3", "needsProbe = 2"));
		} catch (IOException exception) {
			Hemomancy.LOGGER.warn("COURT_PRESENCE_REVIEW could not hide The One Probe's tooltip", exception);
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
				new WorldOptions(Long.getLong("hemomancy.courtPresenceReview.seed", 42L), true, false),
				WorldPresets::createNormalWorldDimensions, mc.screen);
	}

	private static List<Step> plan(ServerLevel level) {
		var out = new ArrayList<Step>();
		if (PelagicWorldgen.context(level.getChunkSource().randomState()) == null) {
			Hemomancy.LOGGER.error("COURT_PRESENCE_REVIEW MISSING Pelagic worldgen is off in the review world");
			return out;
		}
		BoundingBox garden = generate(level, CourtSite.LIGHTHOUSE_GARDEN);
		if (garden != null) marimo(level, garden, out);
		BoundingBox sanctum = generate(level, CourtSite.VENT_SANCTUM);
		if (sanctum != null) hydra(level, sanctum, out);
		return out;
	}

	@Nullable
	private static BoundingBox generate(ServerLevel level, CourtSite site) {
		var marker = CourtSeatCandidates.nearest(level, site, BlockPos.ZERO, 32).orElse(null);
		if (marker == null) {
			Hemomancy.LOGGER.error("COURT_PRESENCE_REVIEW MISSING no {} within 32 rings", site.id());
			return null;
		}
		var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
				.getOrThrow(ResourceKey.create(Registries.STRUCTURE, Hemomancy.rloc(site.id())));
		level.getChunk(marker);
		StructureStart start = level.structureManager().getStructureWithPieceAt(marker, structure);
		if (!start.isValid()) {
			Hemomancy.LOGGER.error("COURT_PRESENCE_REVIEW MISSING {} start round {}", site.id(), marker);
			return null;
		}
		BoundingBox box = start.getBoundingBox();
		for (int cx = (box.minX() >> 4) - 2; cx <= (box.maxX() >> 4) + 2; cx++)
			for (int cz = (box.minZ() >> 4) - 2; cz <= (box.maxZ() >> 4) + 2; cz++) level.getChunk(cx, cz);
		return start.getPieces().getFirst().getBoundingBox();
	}

	/** Marimo at rest, his scene as it plays (the card lands at about tick 30), and a pulse through the moss. */
	private static void marimo(ServerLevel level, BoundingBox pieceBox, List<Step> out) {
		int cx = (pieceBox.minX() + pieceBox.maxX()) / 2, cz = (pieceBox.minZ() + pieceBox.maxZ()) / 2;
		BlockPos heart = null;
		for (BlockPos pos : BlockPos.betweenClosed(cx - 6, CourtSeatLayout.GARDEN_WATERLINE_Y - 16, cz - 6, cx + 6,
				CourtSeatLayout.GARDEN_WATERLINE_Y - 4, cz + 6)) {
			if (level.getBlockEntity(pos) instanceof MarimoHeartBlockEntity) heart = pos.immutable();
		}
		if (heart == null) {
			Hemomancy.LOGGER.error("COURT_PRESENCE_REVIEW MISSING Marimo's Heart near {} {}", cx, cz);
			return;
		}
		BlockPos at = heart;
		Vec3 centre = Vec3.atCenterOf(heart);
		Vec3 gallery = centre.add(1.5D, 4.5D, 8.0D);
		Vec3 close = centre.add(0.5D, 1.2D, 5.0D);
		out.add(new Step("marimo", "mass_from_the_gallery", gallery, centre.add(0, -1, 0), true, false, SETTLE, null));
		out.add(new Step("marimo", "mass_by_night_unaided", gallery, centre.add(0, -1, 0), false, false, 30, null));
		out.add(new Step("marimo", "threads_up_the_wall", centre.add(-3.0D, 1.0D, -2.0D), centre.add(8.0D, 8.0D, 7.0D),
				true, false, 30, null));
		out.add(new Step("marimo", "scene_open", close, centre, true, true, 12,
				player -> BossScenes.playTo(player, BossPresentationIds.SCENE_MARIMO_PRESENCE)));
		out.add(new Step("marimo", "scene_card", null, null, true, true, 40, null));
		out.add(new Step("marimo", "scene_settled", null, null, true, true, 40, null));
		out.add(new Step("marimo", "pulse", close, centre, true, false, 40, player -> {
			if (player.level().getBlockEntity(at) instanceof MarimoHeartBlockEntity entity) entity.pulse();
		}));
		out.add(new Step("marimo", "pulse_from_above", centre.add(0.3D, 3.0D, 0.3D), centre, true, false, 30, player -> {
			if (player.level().getBlockEntity(at) instanceof MarimoHeartBlockEntity entity) entity.pulse();
		}));
		out.add(new Step("marimo", "chorus", close, centre, true, true, 20, player -> CourtPresences.chorus(player, at)));
		warden(close, centre, out);
	}

	private static BloomWardenEntity warden;

	/** The Bloom Warden's fight, from its uprooting to its re-anchoring, shot from where the chorus was heard. */
	private static void warden(Vec3 eye, Vec3 centre, List<Step> out) {
		Vec3 wide = eye.add(eye.subtract(centre).normalize().scale(3.0D)).add(0.0D, 1.5D, 0.0D);
		out.add(new Step("warden", "opening", eye, centre, true, true, 24, player -> {
			warden = BloomWardenEntity.wake(player, BloomWardenRules.GROWTH, BlockPos.containing(centre));
			// Wake puts it beside the nearest Arbor, often off to one side of this view: bring it onto the line
			// between the camera and the heart so every later shot frames it in the middle.
			Vec3 toward = eye.subtract(centre).multiply(1.0D, 0.0D, 1.0D).normalize().scale(2.5D);
			BlockPos base = BlockPos.containing(centre.add(toward));
			for (int dy = 3; dy >= -4; dy--) {
				BlockPos pos = base.above(dy);
				if (player.level().isEmptyBlock(pos) && player.level().isEmptyBlock(pos.above())
						&& player.level().getBlockState(pos.below()).isSolid()) {
					warden.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, warden.getYRot(), 0.0F);
					break;
				}
			}
		}));
		out.add(new Step("warden", "opening_card", null, null, true, true, 24, null));
		out.add(new Step("warden", "rooted_among_blooms", wide, centre, true, false, 30, player -> {
			warden.forcePose(BloomWardenRules.Phase.ROOTED, BloomWardenRules.Move.NONE);
			BlockPos root = warden.blockPosition();
			for (int i = 0; i < 8; i++) {
				double a = Math.PI * 2 * i / 8;
				for (int dy = 2; dy >= -3; dy--) {
					if (warden.sowAt(root.offset((int) Math.round(Math.cos(a) * 4), dy, (int) Math.round(Math.sin(a) * 4)))) break;
				}
			}
		}));
		out.add(new Step("warden", "root_lash_windup", wide, centre, true, false, 16,
				player -> warden.forcePose(BloomWardenRules.Phase.ROOTED, BloomWardenRules.Move.ROOT_LASH)));
		out.add(new Step("warden", "root_lash", null, null, true, false, 6, null));
		out.add(new Step("warden", "frond_spear_draw", wide, centre, true, false, 20,
				player -> warden.forcePose(BloomWardenRules.Phase.ROOTED, BloomWardenRules.Move.FROND_SPEAR)));
		out.add(new Step("warden", "frond_spear_throw", null, null, true, false, 6, null));
		out.add(new Step("warden", "sowing", wide, centre, true, false, 18,
				player -> warden.forcePose(BloomWardenRules.Phase.ROOTED, BloomWardenRules.Move.SOW)));
		out.add(new Step("warden", "uprooted", wide, centre, true, false, 30,
				player -> warden.forcePose(BloomWardenRules.Phase.UPROOTED, BloomWardenRules.Move.NONE)));
		out.add(new Step("warden", "reanchoring", wide, centre, true, true, 14,
				player -> warden.end(BloomWardenRules.Outcome.REANCHORED)));
		out.add(new Step("warden", "reanchored", null, null, true, false, BloomWardenRules.REANCHOR_TICKS, null));
	}

	/** The crowned chimneys, then the largest head rising, reaching, crowning the player and going back down. */
	private static void hydra(ServerLevel level, BoundingBox pieceBox, List<Step> out) {
		var geology = PelagicWorldgen.context(level.getChunkSource().randomState());
		var spots = CourtStructure.heads(geology, pieceBox);
		var crown = spots.stream().filter(s -> s.kind() == VentSanctumHeads.Kind.CROWN)
				.max(Comparator.comparingInt(s -> s.pos().getY())).map(VentSanctumHeads.Spot::pos).orElse(null);
		var walk = spots.stream().filter(s -> s.kind() == VentSanctumHeads.Kind.WALK_TOP)
				.max(Comparator.comparingInt(s -> s.pos().getY())).map(VentSanctumHeads.Spot::pos).orElse(null);
		if (crown == null || walk == null) {
			Hemomancy.LOGGER.error("COURT_PRESENCE_REVIEW MISSING the Sanctum's crowns or threshold walkway");
			return;
		}
		Vec3 mouth = Vec3.atBottomCenterOf(crown);
		Vec3 stand = Vec3.atBottomCenterOf(walk).add(0.0D, 1.62D, 0.0D);
		Vec3 wide = stand.add(stand.subtract(mouth).normalize().scale(10.0D)).add(0.0D, 6.0D, 0.0D);
		out.add(new Step("hydra", "crowned_chimneys", wide, mouth, true, false, SETTLE, null));
		out.add(new Step("hydra", "the_head_from_the_walkway", stand, mouth.add(0, 1, 0), true, false, 30, null));
		out.add(new Step("hydra", "rise_scene_open", stand, mouth, true, true, 20, player -> {
			HydraRisingHead head = HydraRisingHead.rise(player.serverLevel(), mouth, player);
			BossScenes.playTo(player, head, BossPresentationIds.SCENE_HYDRA_RISE, 0);
		}));
		out.add(new Step("hydra", "rising", null, null, true, true, 20, null));
		out.add(new Step("hydra", "top_of_the_rise_card", null, null, true, true, HydraRisingHead.RISE - 40 + 6, null));
		out.add(new Step("hydra", "arching_over", null, null, true, true, HydraRisingHead.REACH / 2, null));
		out.add(new Step("hydra", "crown_overhead", null, null, true, true, HydraRisingHead.REACH / 2 + 10, null));
		out.add(new Step("hydra", "crown_settled", null, null, true, false, HydraRisingHead.HOLD / 2, null));
		out.add(new Step("hydra", "withdrawing_from_the_side", wide, mouth.add(0, 6, 0), true, false,
				HydraRisingHead.HOLD / 2 + HydraRisingHead.WITHDRAW / 3, null));
		out.add(new Step("hydra", "back_in_the_chimney", null, null, true, false, HydraRisingHead.WITHDRAW, null));
		champion(walk, stand, mouth, out);
	}

	private static KindledChampionEntity champion;

	/** The Kindled Champion's fight on the threshold walkway: opening, moves, buds, vents, yielding and the crowning. */
	private static void champion(BlockPos walk, Vec3 stand, Vec3 mouth, List<Step> out) {
		Vec3 toward = mouth.subtract(stand).multiply(1.0D, 0.0D, 1.0D).normalize();
		BlockPos home = BlockPos.containing(Vec3.atBottomCenterOf(walk).add(toward.scale(3.0D)));
		Vec3 body = Vec3.atBottomCenterOf(home).add(0.0D, 1.6D, 0.0D);
		Vec3 side = body.add(new Vec3(-toward.z, 0.0D, toward.x).scale(7.0D)).add(0.0D, 1.5D, 0.0D);
		out.add(new Step("champion", "opening", stand, body, true, true, 30, player -> {
			champion = KindledChampionEntity.callOut(player.serverLevel(), home, 0.0F, "kindled_elder", player);
		}));
		out.add(new Step("champion", "opening_card", null, null, true, true, 30, null));
		out.add(new Step("champion", "neck_sweep_coil", side, body, true, false, 20,
				player -> champion.forcePose(0, KindledChampionRules.Move.NECK_SWEEP)));
		out.add(new Step("champion", "neck_sweep", null, null, true, false, 5, null));
		out.add(new Step("champion", "crown_sting", side, body, true, false, 19,
				player -> champion.forcePose(0, KindledChampionRules.Move.CROWN_STING)));
		out.add(new Step("champion", "crown_grab", side, body, true, false, 34,
				player -> champion.forcePose(0, KindledChampionRules.Move.CROWN_GRAB)));
		out.add(new Step("champion", "budded_once", side, body, true, false, 20,
				player -> champion.forcePose(1, KindledChampionRules.Move.NONE)));
		out.add(new Step("champion", "budded_twice", side, body, true, true, 20,
				player -> champion.forcePose(2, KindledChampionRules.Move.NONE)));
		out.add(new Step("champion", "vents", side, body, true, false, KindledChampionRules.VENT_INTERVAL - 30, null));
		out.add(new Step("champion", "yielding", side, body, true, true, 30,
				player -> champion.end(KindledChampionRules.Outcome.YIELDED, player)));
		out.add(new Step("champion", "crowning_the_victor", null, null, true, true, HydraRisingHead.RISE + HydraRisingHead.REACH, null));
		out.add(new Step("champion", "back_among_the_elders", side, body, true, false, KindledChampionRules.YIELD_TICKS, null));
	}
}
