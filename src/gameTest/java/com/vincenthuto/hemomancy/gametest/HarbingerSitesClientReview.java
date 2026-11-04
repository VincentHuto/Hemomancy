package com.vincenthuto.hemomancy.gametest;

import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
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
 * With hemomancy.harbingerSitesReview, creates a fresh superflat world, places a Blood Temple and a Harbinger Outpost
 * with {@code /place structure}, and photographs each: four approaches from outside, then interiors aimed at the
 * blocks that matter (the heart and the Focus; each outpost station), by day and by night. Screenshots go to
 * {@code <gameDir>/screenshots/<site>/<pass>-<nn>-<stop>.png} (gameDir build/harbinger-sites-review). Excluded from
 * release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class HarbingerSitesClientReview {
	private static final String WORLD = "HarbingerSitesReview";
	private static final int FIRST_WAIT = 200;
	private static final int WAIT = 40;
	private static final BlockPos TEMPLE = new BlockPos(8, -60, 8);
	private static final BlockPos OUTPOST = new BlockPos(264, -60, 8);

	private record Shot(String folder, String name, Vec3 eye, Vec3 look, long time) {
	}

	private enum Phase { CREATE, ENTER, PLACE, SHOOT, DONE }

	private static Phase phase = Phase.CREATE;
	private static int ticks;
	private static int shot;
	private static int shotTick;
	private static volatile List<Shot> shots;

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.harbingerSitesReview")) return;
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
					phase = Phase.PLACE;
					server.execute(() -> {
						var player = server.getPlayerList().getPlayer(uuid);
						var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
						for (String cmd : new String[] {"gamemode spectator", "gamerule doDaylightCycle false",
								"weather clear", "gamerule doWeatherCycle false", "gamerule doMobSpawning false",
								"forceload add -32 -32 48 48", "forceload add 224 -32 304 48",
								"place structure hemomancy:blood_temple " + TEMPLE.getX() + " " + TEMPLE.getY() + " " + TEMPLE.getZ(),
								"place structure hemomancy:harbinger_outpost " + OUTPOST.getX() + " " + OUTPOST.getY() + " " + OUTPOST.getZ()})
							try {
								Hemomancy.LOGGER.info("HARBINGER_SITES_REVIEW {} -> {}", cmd,
										server.getCommands().getDispatcher().execute(cmd, source));
							} catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
								Hemomancy.LOGGER.error("HARBINGER_SITES_REVIEW {} failed: {}", cmd, exception.getMessage());
							}
						shots = plan(player.serverLevel());
						Hemomancy.LOGGER.info("HARBINGER_SITES_REVIEW {} shots queued", shots.size());
					});
				}
			}
			case PLACE -> {
				if (shots != null) {
					phase = shots.isEmpty() ? Phase.DONE : Phase.SHOOT;
					shotTick = 0;
				}
			}
			case SHOOT -> shoot(mc);
			case DONE -> {
				Hemomancy.LOGGER.info("HARBINGER_SITES_REVIEW captured {}", shot);
				mc.stop();
			}
		}
	}

	private static List<Shot> plan(ServerLevel level) {
		var out = new ArrayList<Shot>();
		BlockPos heart = find(level, TEMPLE, 24, BlockInit.mortal_display.get());
		BlockPos focus = find(level, TEMPLE, 24, BlockInit.cardinal_focus.get());
		Hemomancy.LOGGER.info("HARBINGER_SITES_REVIEW temple heart={} focus={}", heart, focus);
		if (heart != null && focus != null) {
			Vec3 h = Vec3.atCenterOf(heart), f = Vec3.atCenterOf(focus);
			Vec3 along = h.subtract(f).multiply(1, 0, 1).normalize();
			Vec3 across = new Vec3(-along.z, 0, along.x);
			Vec3 middle = f.add(along.scale(-4));
			List<Object[]> stops = List.of(
					new Object[] {"approach_front", middle.add(along.scale(-30)).add(0, 9, 0), middle.add(0, 4, 0)},
					new Object[] {"three_quarter", middle.add(along.scale(-20)).add(across.scale(20)).add(0, 12, 0), middle.add(0, 4, 0)},
					new Object[] {"side", middle.add(across.scale(-28)).add(0, 8, 0), middle.add(0, 4, 0)},
					new Object[] {"apse_end", middle.add(along.scale(24)).add(across.scale(-14)).add(0, 10, 0), middle.add(0, 4, 0)},
					new Object[] {"portico", f.add(along.scale(-16)).add(across.scale(4)).add(0, 1.2, 0), f.add(along.scale(-12)).add(0, 2, 0)},
					new Object[] {"nave_from_door", f.add(along.scale(-10)).add(0, 1.6, 0), h.add(0, 0.5, 0)},
					new Object[] {"heart_and_focus", f.add(along.scale(-3)).add(across.scale(2)).add(0, 1.8, 0), h},
					new Object[] {"vault", f.add(along.scale(-5)).add(0, 1.6, 0), f.add(along.scale(-5)).add(0, 8, 0.01)},
					new Object[] {"hermit_cell", f.add(along.scale(-5)).add(across.scale(-3)).add(0, 1.6, 0), f.add(along.scale(-5)).add(across.scale(-6)).add(0, 1, 0)});
			add(out, "blood_temple", stops);
		} else {
			Hemomancy.LOGGER.error("HARBINGER_SITES_REVIEW MISSING temple heart={} focus={}", heart, focus);
		}
		BlockPos brazier = find(level, OUTPOST, 20, BlockInit.iron_brazier.get());
		if (brazier != null) {
			Vec3 c = Vec3.atCenterOf(brazier);
			var stops = new ArrayList<Object[]>(List.of(
					new Object[] {"from_afar", c.add(-55, 26, 40), c},
					new Object[] {"corner", c.add(48, 18, 48), c},
					new Object[] {"overhead", c.add(4, 60, 6), c},
					new Object[] {"plaza", c.add(-9, 2.5, 9), c.add(0, 1, 0)},
					new Object[] {"wall_walk", c.add(-5, 1.5, -26), c.add(5, 1.5, -26)}));
			BlockPos basin = find(level, brazier, 6, BlockInit.venous_basin.get());
			if (basin != null) {
				Vec3 b = Vec3.atCenterOf(basin);
				Vec3 spout = b.subtract(c).multiply(1, 0, 1).normalize();
				Vec3 side = new Vec3(-spout.z, 0, spout.x);
				stops.add(new Object[] {"basin_pour", b.add(spout.scale(3)).add(side.scale(2)).add(0, 0.6, 0),
						b.add(spout.scale(0.8)).add(0, -0.6, 0)});
				stops.add(new Object[] {"gutter_run", b.add(spout.scale(1.5)).add(side.scale(1.2)).add(0, 1.6, 0),
						b.add(spout.scale(5)).add(0, -0.5, 0)});
			}
			String[] names = {"seat", "forge", "centrifuge", "loom"};
			Block[] stations = {BlockInit.somnolent_seat.get(), BlockInit.resonant_forge.get(),
					BlockInit.vial_centrifuge.get(), BlockInit.somatic_loom.get()};
			for (int i = 0; i < stations.length; i++) {
				BlockPos station = find(level, brazier, 34, stations[i]);
				if (station == null) continue;
				Vec3 s = Vec3.atCenterOf(station);
				Vec3 toPlaza = c.subtract(s).multiply(1, 0, 1).normalize();
				stops.add(new Object[] {names[i] + "_home", s.add(toPlaza.scale(18)).add(0, 9, 0), s.add(0, 3, 0)});
				stops.add(new Object[] {names[i] + "_inside", s.add(toPlaza.scale(3.5)).add(0, 1.4, 0), s});
			}
			BlockPos bed = find(level, brazier, 40, net.minecraft.world.level.block.Blocks.RED_BED);
			if (bed != null) stops.add(new Object[] {"barracks", Vec3.atCenterOf(bed).add(0, -4, 6), Vec3.atCenterOf(bed).add(0, -5, 0)});
			add(out, "harbinger_outpost", stops);
		} else {
			Hemomancy.LOGGER.error("HARBINGER_SITES_REVIEW MISSING outpost brazier");
		}
		return out;
	}

	private static BlockPos find(ServerLevel level, BlockPos around, int radius, Block block) {
		BlockPos best = null;
		for (BlockPos pos : BlockPos.betweenClosed(around.offset(-radius, -8, -radius), around.offset(radius, 24, radius))) {
			if (level.getBlockState(pos).is(block)
					&& (best == null || pos.distSqr(around) < best.distSqr(around))) best = pos.immutable();
		}
		return best;
	}

	private static void add(List<Shot> out, String folder, List<Object[]> stops) {
		for (var pass : List.of(new Object[] {"day", 6000L}, new Object[] {"night", 18000L})) {
			for (int i = 0; i < stops.size(); i++) {
				var stop = stops.get(i);
				out.add(new Shot(folder, String.format(Locale.ROOT, "%s-%02d-%s", pass[0], i, stop[0]),
						(Vec3) stop[1], (Vec3) stop[2], (Long) pass[1]));
			}
		}
	}

	private static void shoot(Minecraft mc) {
		Shot current = shots.get(shot);
		int wait = shot == 0 ? FIRST_WAIT : WAIT;
		if (shotTick++ == 0) {
			var server = mc.getSingleplayerServer();
			var uuid = mc.player.getUUID();
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				player.serverLevel().setDayTime(current.time());
				player.removeEffect(MobEffects.NIGHT_VISION);
				Vec3 eye = current.eye();
				Vec3 delta = current.look().subtract(eye);
				float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
				float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance()));
				player.teleportTo(player.serverLevel(), eye.x, eye.y - player.getEyeHeight(), eye.z, yaw, pitch);
			});
			return;
		}
		if (shotTick < wait) return;
		Path out = mc.gameDirectory.toPath().resolve("screenshots").resolve(current.folder()).resolve(current.name() + ".png");
		try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
			Files.createDirectories(out.getParent());
			image.writeToFile(out);
		} catch (IOException exception) {
			Hemomancy.LOGGER.error("HARBINGER_SITES_REVIEW could not save {}", out, exception);
		}
		Hemomancy.LOGGER.info("HARBINGER_SITES_REVIEW shot {}/{} eye={} player={}", current.folder(), current.name(),
				current.eye(), mc.player.position());
		shotTick = 0;
		if (++shot >= shots.size()) phase = Phase.DONE;
	}

	private static void createWorld(Minecraft mc) {
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
				new WorldOptions(42L, false, false),
				registries -> registries.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET)
						.getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
				mc.screen);
	}
}
