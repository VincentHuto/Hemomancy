package com.vincenthuto.hemomancy.gametest;

import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperEncounterPuppetEvents;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseOneAttack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPuppetRoster;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;

/**
 * Explicit opt-in visual fixture for the Vesper overhaul: stills of the regenerated mount, its wings and wounds and
 * the Evening Star's acts, then live scenes against an invulnerable dummy for every new mechanic (ground controller,
 * burrows, sortie, muster, wound drain, each phase-two act, borrowed attacks).
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VesperClientReview {
	private record Scene(String name, double distance, double height, double yaw, double lift, int[] shots, int length) {
		Scene(String name, double distance, double height, double yaw) {
			this(name, distance, height, yaw, 0.0D, new int[] { 40 }, 50);
		}
	}

	private static final Scene[] SCENES = {
			new Scene("mount_front", 13.0, 3.0, 0.0), new Scene("mount_side", 14.0, 2.5, 1.57),
			new Scene("mount_three_quarter", 13.0, 5.0, 0.8), new Scene("wings_folded", 13.0, 6.0, 2.4),
			new Scene("throne_back", 7.0, 7.5, 2.9), new Scene("throne_back_side", 7.5, 6.0, 2.1),
			new Scene("wings_flared", 16.0, 3.0, 0.6, 3.0, new int[] { 30, 45 }, 50),
			// Held aloft: the tail hooked under the belly while circling, then aimed down at the mark through a fusillade.
			new Scene("flight_side", 24.0, 0.0, 1.57, 0.0, new int[] { 40 }, 50),
			new Scene("flight_low", 20.0, -6.0, 0.7, 0.0, new int[] { 40 }, 50),
			new Scene("flight_front", 18.0, -3.0, 0.35, 0.0, new int[] { 40 }, 50),
			new Scene("flight_fusillade", 24.0, -1.0, 1.57, 0.0, new int[] { 34, 40, 42, 44, 47, 54 }, 60),
			new Scene("flight_dive", 22.0, -1.0, 1.1, 0.0, new int[] { 44, 52, 56, 62 }, 64),
			new Scene("wound_carapace", 9.0, 5.0, 2.6, 1.0, new int[] { 30, 45 }, 50),
			new Scene("wound_neck", 8.0, 1.0, 0.3, 0.0, new int[] { 30, 45 }, 50),
			new Scene("wound_tail", 8.0, 1.5, 3.0, 0.0, new int[] { 30, 45 }, 50),
			new Scene("act_locked", 5.0, 0.8, 0.5), new Scene("act_open", 5.0, 0.8, 0.5),
			new Scene("act_sickles", 5.0, 0.8, 0.5),
			// Side-on stills of the grips and elbows: sickles (rage) and a one-handed school weapon.
			new Scene("act_side_sickles", 7.0, 0.4, 1.57), new Scene("act_side_locked", 7.0, 0.4, 1.57),
			new Scene("live_ground", 16.0, 6.0, 0.6, 0.0, new int[] { 40, 80, 120, 160, 200, 240 }, 250),
			new Scene("live_burrow", 18.0, 10.0, 0.6, 0.0, new int[] { 20, 60, 120, 180, 240, 280, 300, 320, 340 }, 360),
			new Scene("live_dive_burrow", 18.0, 10.0, 0.6, 0.0, new int[] { 15, 40, 80, 120, 160, 200 }, 210),
			new Scene("live_sortie", 22.0, 8.0, 0.6, 3.0, new int[] { 20, 40, 60, 80, 100, 120, 140, 160 }, 170),
			new Scene("live_muster", 16.0, 6.0, 0.6, 0.0, new int[] { 15, 30, 45, 60, 90 }, 100),
			new Scene("live_recall", 9.5, 3.5, 1.2, 0.0, new int[] { 52, 58, 64, 68, 72, 76, 80, 84 }, 95),
			new Scene("live_wound_drain", 9.0, 3.0, 2.6, 1.0, new int[] { 20, 60, 100, 140 }, 150),
			new Scene("live_locked", 9.0, 2.5, 0.6, 0.0, new int[] { 40, 80, 120, 160, 200, 240 }, 250),
			new Scene("live_open", 9.0, 2.5, 0.6, 0.0, new int[] { 40, 80, 120, 160, 200, 240 }, 250),
			new Scene("live_sickles", 9.0, 2.5, 0.6, 0.0, new int[] { 30, 60, 90, 120, 150, 180, 210, 240, 270 }, 280),
			new Scene("live_borrowed", 10.0, 3.0, 0.6, 0.0, new int[] { 60, 120, 180, 240, 300, 360 }, 370),
			// Rough ground: legs reaching onto steps, the body tilting to the slope, and the gait over bumps.
			new Scene("terrain_side", 12.0, 1.5, 1.57), new Scene("terrain_front", 12.0, 2.5, 0.25),
			new Scene("live_terrain", 15.0, 5.0, 0.9, 0.0, new int[] { 40, 42, 44, 46, 48, 50, 100, 150, 200, 250 }, 260),
			// Back-to-back moves in tight frame sequences, to check each wind-up, hit and recovery flows.
			new Scene("live_moves", 12.5, 3.5, 1.2, 0.0, new int[] { 14, 20, 23, 25, 27, 32, 37, 39, 41, 50,
					84, 96, 100, 103, 106, 110, 114, 117, 120, 130,
					164, 175, 179, 181, 184, 190, 195, 200, 206, 214 }, 230),
			// The Evening Star on foot: strafing and striking across rough ground, then the sickle act on it, with
			// runs of consecutive frames to check the gait, foot planting and each move's flow.
			new Scene("live_es_terrain", 7.5, 1.6, 0.75, 0.0, new int[] { 40, 60, 80, 100, 101, 102, 103, 104, 105, 106,
					107, 108, 140, 180, 220, 260 }, 270),
			new Scene("live_es_terrain_sickles", 6.5, 1.2, 0.75, 0.0, new int[] { 40, 50, 60, 70, 80, 100, 130, 131, 132, 133, 134,
					135, 136, 160, 190, 220 }, 230),
			new Scene("live_es_moves", 7.0, 1.4, 0.75, 0.0, new int[] { 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52,
					90, 92, 94, 96, 98, 100, 102, 104, 106, 108, 150, 152, 154, 156, 158, 160, 162, 164 }, 170),
			// Every Evening Star stance and a run of his moves, frozen on each wind-up, contact and follow-through.
			new Scene("es_gallery", 6.5, 0.1, 1.2, 0.0, VesperEveningStarGallery.shots(), VesperEveningStarGallery.LENGTH),
	};
	private static final BlockPos CENTER = new BlockPos(768, 100, 768);
	private static int scene, tick, captured;
	private static boolean started;
	private static volatile boolean ready;
	private static Mob actor;
	private static Villager dummy;

	private VesperClientReview() {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.vesperReview")) return;
		var client = Minecraft.getInstance();
		client.options.pauseOnLostFocus = false;
		if (client.player == null || client.getSingleplayerServer() == null) return;
		try {
			if (scene == SCENES.length) {
				Files.writeString(client.gameDirectory.toPath().resolve("vesper-visuals.txt"),
						"Native fixtures for the Vesper overhaul: mount stills plus live AI scenes against a dummy.\n");
				LogUtils.getLogger().info("VESPER_REVIEW captured {}", captured);
				client.stop();
				return;
			}
			var server = client.getSingleplayerServer();
			var uuid = client.player.getUUID();
			Scene current = SCENES[scene];
			// -Dhemomancy.vesperReview.only=live_recall,mount_front runs just those scenes.
			String only = System.getProperty("hemomancy.vesperReview.only", "");
			if (!only.isBlank() && !java.util.Arrays.asList(only.split(",")).contains(current.name())) {
				scene++;
				return;
			}
			if (!started) {
				started = true;
				ready = false;
				client.options.hideGui = true;
				server.execute(() -> setup(server.getPlayerList().getPlayer(uuid), current));
				return;
			}
			if (!ready) return;
			tick++;
			int at = tick;
			server.execute(() -> drive(server.getPlayerList().getPlayer(uuid), current, at));
			for (int shot : current.shots()) {
				if (tick == shot) {
					String suffix = current.name().equals("es_gallery") ? "-" + VesperEveningStarGallery.label(shot)
							: current.shots().length > 1 ? "-" + shot : "";
					Screenshot.grab(client.gameDirectory, "vesper-" + current.name() + suffix + ".png",
							client.getMainRenderTarget(), message -> LogUtils.getLogger().info("VESPER_SCREENSHOT {}",
									message.getString()));
					captured++;
				}
			}
			if (tick > current.length()) {
				scene++;
				tick = 0;
				started = false;
			}
		} catch (Exception exception) {
			LogUtils.getLogger().error("Vesper native review failed", exception);
			client.stop();
		}
	}

	private static void setup(ServerPlayer player, Scene scene) {
		ServerLevel level = player.server.getLevel(Level.OVERWORLD);
		var source = player.createCommandSourceStack().withLevel(level).withPermission(4).withSuppressedOutput();
		for (String cmd : new String[] { "gamemode spectator", "gamerule doMobSpawning false",
				"gamerule doDaylightCycle false", "time set 6000", "weather clear", "kill @e[type=minecraft:item]" })
			player.server.getCommands().performPrefixedCommand(source, cmd);
		level.getEntities((Entity) null, new AABB(CENTER).inflate(64), e -> !(e instanceof net.minecraft.world.entity.player.Player))
				.forEach(Entity::discard);
		String name = scene.name();
		boolean rough = name.startsWith("terrain") || name.equals("live_terrain") || name.startsWith("live_es_terrain");
		for (BlockPos pos : BlockPos.betweenClosed(CENTER.offset(-26, -1, -26), CENTER.offset(26, 20, 26))) {
			int above = pos.getY() - CENTER.getY();
			boolean solid = above == -1 || rough && above < roughHeight(pos.getX() - CENTER.getX(), pos.getZ() - CENTER.getZ());
			level.setBlock(pos, solid ? Blocks.POLISHED_BLACKSTONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
		}

		boolean live = name.startsWith("live");
		dummy = null;
		if (live) {
			dummy = EntityType.VILLAGER.create(level);
			dummy.setNoAi(true);
			dummy.setInvulnerable(true);
			dummy.setPersistenceRequired();
			dummy.moveTo(Vec3.atBottomCenterOf(CENTER.offset(0, rough ? roughHeight(0, -9) : 0, -9)), 0.0F, 0.0F);
			level.addFreshEntity(dummy);
		}
		boolean eveningStar = name.startsWith("act_") || name.equals("live_locked") || name.equals("live_open")
				|| name.equals("live_sickles") || name.equals("live_borrowed") || name.startsWith("live_es_") || name.equals("es_gallery");
		Mob mob;
		if (eveningStar) {
			VesperTheEveningStarEntity vesper = EntityInit.vesper_evening_star.get().create(level);
			float fraction = name.endsWith("open") ? 0.55F : name.endsWith("sickles") ? 0.25F : 0.95F;
			vesper.setHealth(vesper.getMaxHealth() * fraction);
			if (name.equals("live_borrowed")) vesper.setInheritedPuppets(VesperPuppetRoster.SHAPES);
			mob = vesper;
		} else {
			VesperTheCrownedRefusalEntity crowned = EntityInit.vesper_crowned_refusal.get().create(level);
			if (name.startsWith("wings") || name.startsWith("flight") || name.equals("live_sortie")
					|| name.equals("live_dive_burrow")) {
				setData(crowned, "DATA_WINGS_GROWN", true);
			}
			mob = crowned;
		}
		mob.setPersistenceRequired();
		int standOn = 0;
		if (rough) {
			// A no-AI mob does not fall: stand it on the highest block under its footprint, as it would settle.
			for (int dx = -2; dx <= 1; dx++) for (int dz = -2; dz <= 1; dz++) standOn = Math.max(standOn, roughHeight(dx, dz));
		}
		if (name.startsWith("flight")) {
			standOn = 8;
			mob.setNoGravity(true);
		}
		mob.moveTo(Vec3.atBottomCenterOf(CENTER.above(standOn)), 180.0F, 0.0F);
		mob.setYHeadRot(180.0F);
		mob.yBodyRot = 180.0F;
		if (!live) mob.setNoAi(true);
		level.addFreshEntity(mob);
		actor = mob;

		if (mob instanceof VesperTheCrownedRefusalEntity crowned) {
			crowned.ensureSummonedFlightArena();
			switch (name) {
				case "wings_flared" -> {
					setData(crowned, "DATA_FLIGHT_MODE", VesperWingedFlightRules.FlightMode.WING_GROWTH.ordinal());
					setData(crowned, "DATA_FLIGHT_TICK", 50);
				}
				case "wound_carapace" -> invoke(crowned, "exposeAnchor", new Class<?>[] { int.class }, 0);
				case "wound_neck" -> invoke(crowned, "exposeAnchor", new Class<?>[] { int.class }, 1);
				case "wound_tail" -> invoke(crowned, "exposeAnchor", new Class<?>[] { int.class }, 2);
				case "live_wound_drain" -> invoke(crowned, "exposeAnchor", new Class<?>[] { int.class }, 0);
				default -> { }
			}
		}
		Scene placed = scene;
		double eye = mob.getBbHeight() * 0.55 + placed.lift();
		Vec3 focus = mob.position().add(0, eye, 0);
		double x = focus.x + Math.sin(placed.yaw()) * placed.distance();
		double z = focus.z - Math.cos(placed.yaw()) * placed.distance();
		double y = focus.y + placed.height();
		float yaw = (float) Math.toDegrees(Math.atan2(-(focus.x - x), focus.z - z));
		float pitch = (float) -Math.toDegrees(Math.atan2(focus.y - y, Math.hypot(focus.x - x, focus.z - z)));
		player.teleportTo(level, x, y - player.getEyeHeight(), z, yaw, pitch);
		LogUtils.getLogger().info("VESPER_CLIENT_REVIEW {}", name);
		ready = true;
	}

	/** Keeps the live fights on the dummy and forces the scene's mechanic at its cue. */
	private static void drive(ServerPlayer player, Scene scene, int tick) {
		if (player == null || actor == null) return;
		if (dummy != null) actor.setTarget(dummy);
		String name = scene.name();
		if (actor instanceof VesperTheCrownedRefusalEntity crowned) {
			if (name.equals("wings_flared")) {
				// Held near the end of wing growth: fully spread, still shivering, never landing the still.
				setData(crowned, "DATA_FLIGHT_MODE", VesperWingedFlightRules.FlightMode.WING_GROWTH.ordinal());
				setData(crowned, "DATA_FLIGHT_TICK", 50);
			}
			if (name.startsWith("flight")) {
				// Facing north, the mark sits on the floor a few blocks ahead, as at the start of a strafing run.
				boolean firing = name.equals("flight_fusillade") && tick >= 30;
				boolean diving = name.equals("flight_dive") && tick >= 30;
				VesperWingedFlightRules.FlightMode mode = firing ? VesperWingedFlightRules.FlightMode.TAIL_FUSILLADE
						: !diving ? VesperWingedFlightRules.FlightMode.CIRCLING
						: tick < 50 ? VesperWingedFlightRules.FlightMode.DIVE_TELEGRAPH
						: VesperWingedFlightRules.FlightMode.DIVING_REND;
				setData(crowned, "DATA_FLIGHT_MODE", mode.ordinal());
				setData(crowned, "DATA_FLIGHT_TICK", firing || diving ? tick - (tick < 50 || firing ? 30 : 50) : 20);
				invoke(crowned, "setLockedFlightAim", new Class<?>[] { Vec3.class },
						Vec3.atBottomCenterOf(CENTER.offset(0, 0, -6)));
			}
			if (name.equals("live_burrow") && tick == 10) invoke(crowned, "startBurrow", new Class<?>[] { boolean.class }, false);
			if (name.equals("live_dive_burrow") && tick == 5) {
				invoke(crowned, "startBurrow", new Class<?>[] { boolean.class }, true);
			}
			if (name.equals("live_sortie") && tick == 5) {
				crowned.summonedFlightArena().ifPresent(arena -> invoke(crowned, "startTakeoff",
						new Class<?>[] { arena.getClass() }, arena));
			}
			if (name.equals("live_muster") && tick == 5) {
				Object controller = field(crowned, "controller");
				invoke(controller, "begin", new Class<?>[] { VesperPhaseOneAttack.class, LivingEntity.class },
						VesperPhaseOneAttack.PUPPET_MUSTER, dummy);
			}
			if (name.equals("live_recall") && tick == 5) {
				Object controller = field(crowned, "controller");
				invoke(controller, "begin", new Class<?>[] { VesperPhaseOneAttack.class, LivingEntity.class },
						VesperPhaseOneAttack.PUPPET_MUSTER, dummy);
			}
			if (name.equals("live_recall") && tick == 48) {
				// Age the fresh muster past the recall gate, then have him take them back.
				long aged = crowned.level().getGameTime() - 1000L;
				crowned.level().getEntitiesOfClass(Mob.class, crowned.getBoundingBox().inflate(32.0D),
						mob -> mob.getPersistentData().getBoolean(VesperEncounterPuppetEvents.PUPPET_KEY))
						.forEach(mob -> mob.getPersistentData().putLong(VesperEncounterPuppetEvents.SINCE_KEY, aged));
				Object controller = field(crowned, "controller");
				invoke(controller, "begin", new Class<?>[] { VesperPhaseOneAttack.class, LivingEntity.class },
						VesperPhaseOneAttack.RECALL, dummy);
			}
			if (name.equals("live_wound_drain") && tick > 10) crowned.drainWound(player, 1.0F);
			if (name.equals("live_moves") && (tick == 2 || tick == 72 || tick == 152)) {
				Object controller = field(crowned, "controller");
				VesperPhaseOneAttack move = tick == 2 ? VesperPhaseOneAttack.PINCER_VICE
						: tick == 72 ? VesperPhaseOneAttack.BROOD_TRAMPLE : VesperPhaseOneAttack.ROYAL_SCUTTLE;
				invoke(controller, "begin", new Class<?>[] { VesperPhaseOneAttack.class, LivingEntity.class }, move, dummy);
			}
			if (tick % 20 == 0) {
				LogUtils.getLogger().info("VESPER_LIVE {} t={} attack={} flight={} burrow={} anchor={} drained={}",
						name, tick, crowned.getAttack(), crowned.getFlightMode(), crowned.getBurrowStage(),
						crowned.getActiveAnchor(), crowned.getWoundDrained());
			}
		} else if (actor instanceof VesperTheEveningStarEntity vesper && name.equals("es_gallery")) {
			VesperEveningStarGallery.drive(vesper, tick);
		} else if (actor instanceof VesperTheEveningStarEntity vesper && tick % 20 == 0) {
			LogUtils.getLogger().info("VESPER_LIVE {} t={} act={} tendency={} action={}", name, tick, vesper.getAct(),
					vesper.getActiveTendency(), vesper.getWeaponAction());
		}
	}

	/** Blocks of rough ground stacked at an offset from the centre: ridges and hollows up to two high. */
	private static int roughHeight(int dx, int dz) {
		double wave = 0.95D * Math.sin(dx * 0.45D) + 0.8D * Math.cos(dz * 0.38D + 0.7D) + 0.35D * Math.sin((dx + dz) * 0.9D);
		return Math.max(0, Math.min(2, (int) Math.round(wave + 0.6D)));
	}

	@SuppressWarnings("unchecked")
	private static void setData(Entity entity, String accessorName, Object value) {
		try {
			Field field = entity.getClass().getDeclaredField(accessorName);
			field.setAccessible(true);
			entity.getEntityData().set((EntityDataAccessor<Object>) field.get(null), value);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Review data " + accessorName + " unavailable", exception);
		}
	}

	private static Object field(Object target, String name) {
		try {
			Field field = target.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(target);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Review field " + name + " unavailable", exception);
		}
	}

	private static void invoke(Object target, String name, Class<?>[] types, Object... values) {
		try {
			Method method = target.getClass().getDeclaredMethod(name, types);
			method.setAccessible(true);
			method.invoke(target, values);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Review hook " + name + " unavailable", exception);
		}
	}
}
