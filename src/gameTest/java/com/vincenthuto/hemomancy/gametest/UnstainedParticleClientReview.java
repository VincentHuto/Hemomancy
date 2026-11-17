package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.unstained.ViridSalisTrailBlock;
import com.vincenthuto.hemomancy.common.block.unstained.crafting.PallidRetortBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.unstained.UnstainedRiteParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * With hemomancy.unstainedParticleReview, places the Altar of Cleansing, the Unstained Podium, the Pale Silver Bells,
 * a lit Pallid Retort, a Stillwater Condenser, a Verdigris Lattice and a Virid Salis trail fed by a redstone block on a
 * dark floor, then fires one rite completion of each visual family (Lethean Baptism, Silver Dawn, Glass Lungs,
 * Moon-Washed Copper by day and by night) through {@link UnstainedRiteParticles}, so no rite's gameplay runs. Shots land
 * in build/unstained-particle-review/screenshots. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class UnstainedParticleClientReview {
	private static final UUID KNEELER = UUID.fromString("2c7e4b19-8d3a-4f60-b5e2-91a0c6d4e7f8");
	private static final int RITE_X = 30;
	private static int ticks;
	private static int shot;
	private static BlockPos row;
	/** Eye offset from the row origin, the point looked at, and the scene action fired just before the capture. */
	private static final Object[][] SHOTS = {
			{new double[]{11.0, 6.0, -12.0, 11.0, 0.5, 0.5}, "none"},
			{new double[]{0.5, 2.6, -3.4, 0.5, 1.3, 0.5}, "altar"},
			{new double[]{4.5, 2.6, -3.4, 4.5, 1.3, 0.5}, "podium"},
			{new double[]{8.5, 2.2, -3.0, 8.5, 1.0, 0.5}, "bells"},
			{new double[]{13.5, 2.2, -2.6, 13.5, 1.0, 0.5}, "none"},
			{new double[]{19.5, 2.0, -2.6, 19.5, 0.4, 0.5}, "none"},
			{new double[]{RITE_X + 0.5, 2.2, -3.0, RITE_X + 0.5, 1.4, 0.5}, "baptism"},
			{new double[]{RITE_X + 0.5, 2.6, -6.0, RITE_X + 0.5, 1.6, 0.5}, "silver_dawn"},
			{new double[]{RITE_X + 0.5, 2.6, -7.0, RITE_X + 0.5, 1.8, 0.5}, "glass_lungs"},
			{new double[]{RITE_X + 0.5, 2.6, -7.0, RITE_X + 0.5, 1.8, 0.5}, "moon_washed_copper_day"},
			{new double[]{RITE_X + 0.5, 2.6, -7.0, RITE_X + 0.5, 1.8, 0.5}, "moon_washed_copper_night"}};

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.unstainedParticleReview")) return;
		var mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("unstained-particle-review")) return;
		if (mc.player == null || mc.getSingleplayerServer() == null) return;
		mc.options.pauseOnLostFocus = false;
		mc.options.hideGui = true;
		var server = mc.getSingleplayerServer();
		var uuid = mc.player.getUUID();
		if (++ticks == 40) {
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				var level = player.serverLevel();
				row = player.blockPosition().offset(0, 0, 8);
				var source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();
				for (String cmd : new String[]{"gamemode spectator", "time set 6000", "gamerule doDaylightCycle false",
						"weather clear", "gamerule doMobSpawning false"})
					server.getCommands().performPrefixedCommand(source, cmd);
				for (BlockPos pos : BlockPos.betweenClosed(row.offset(-4, -1, -24), row.offset(RITE_X + 14, 12, 14)))
					level.setBlock(pos, pos.getY() < row.getY() ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
							: Blocks.AIR.defaultBlockState(), 2);

				place(level, row, BlockInit.altar_of_cleansing.get());
				place(level, row.offset(4, 0, 0), BlockInit.unstained_podium.get());
				place(level, row.offset(8, 0, 0), BlockInit.pale_silver_bells.get());
				place(level, row.offset(12, 0, 0), BlockInit.pallid_retort.get());
				level.setBlock(row.offset(12, 0, 0), level.getBlockState(row.offset(12, 0, 0))
						.setValue(PallidRetortBlock.LIT, true), 2);
				place(level, row.offset(15, 0, 0), BlockInit.stillwater_condenser.get());
				place(level, row.offset(18, 0, 0), BlockInit.verdigris_lattice.get());
				BlockState trail = BlockInit.virid_salis_trail.get().defaultBlockState()
						.setValue(ViridSalisTrailBlock.EAST, RedstoneSide.SIDE)
						.setValue(ViridSalisTrailBlock.WEST, RedstoneSide.SIDE);
				for (int x = 20; x <= 23; x++) level.setBlock(row.offset(x, 0, 0), trail, 2);
				level.setBlockAndUpdate(row.offset(24, 0, 0), Blocks.REDSTONE_BLOCK.defaultBlockState());
				Hemomancy.LOGGER.info("UNSTAINED_PARTICLE_REVIEW placed altar {}, retort {}, trail {}",
						level.getBlockState(row).getBlock(), level.getBlockState(row.offset(12, 0, 0)),
						level.getBlockState(row.offset(21, 0, 0)));
			});
		}
		if (ticks < 80 || row == null) return;
		int local = (ticks - 80) % 60;
		if (local == 0) {
			if (shot == SHOTS.length) { mc.stop(); return; }
			double[] at = (double[]) SHOTS[shot][0];
			server.execute(() -> {
				var player = server.getPlayerList().getPlayer(uuid);
				double x = row.getX() + .5 + at[0], y = row.getY() + at[1], z = row.getZ() + .5 + at[2];
				double fx = row.getX() + at[3], fy = row.getY() + at[4], fz = row.getZ() + at[5];
				float yaw = (float) Math.toDegrees(Math.atan2(-(fx - x), fz - z));
				float pitch = (float) -Math.toDegrees(Math.atan2(fy - y, Math.hypot(fx - x, fz - z)));
				player.teleportTo(player.serverLevel(), x, y - player.getEyeHeight(), z, yaw, pitch);
			});
		}
		if (shot < SHOTS.length && local == 32) {
			String action = (String) SHOTS[shot][1];
			server.execute(() -> act(server.overworld(), action));
		}
		if (local == 40 && shot < SHOTS.length) {
			Screenshot.grab(mc.gameDirectory, "unstained-particle-" + shot + "-" + SHOTS[shot][1] + ".png",
					mc.getMainRenderTarget(), message -> {});
			Hemomancy.LOGGER.info("UNSTAINED_PARTICLE_REVIEW captured {}", shot);
			shot++;
		}
	}

	private static void act(ServerLevel level, String action) {
		BlockPos rite = row.offset(RITE_X, 0, 0);
		switch (action) {
			case "altar" -> {
				for (String effect : new String[]{"spawnBlessingParticles", "spawnPurityParticles", "spawnClarityParticles"})
					blockEffect(level, row, effect);
			}
			case "podium" -> {
				blockEffect(level, row.offset(4, 0, 0), "spawnPurityParticles");
				blockEffect(level, row.offset(4, 0, 0), "spawnClarityParticles");
			}
			case "bells" -> {
				BlockPos bells = row.offset(8, 0, 0);
				ServerPlayer ringer = kneeler(level, bells);
				HemoCapabilityAccess.getUnstainedProgress(ringer).setBegunPurification(true);
				var result = level.getBlockState(bells).useWithoutItem(level, ringer,
						new BlockHitResult(Vec3.atCenterOf(bells), Direction.NORTH, bells, false));
				Hemomancy.LOGGER.info("UNSTAINED_PARTICLE_REVIEW bells rung: {}", result);
			}
			case "baptism" -> UnstainedRiteParticles.letheanBaptism(level, kneeler(level, rite));
			case "silver_dawn" -> UnstainedRiteParticles.silverDawn(level, rite, 8);
			case "glass_lungs" -> UnstainedRiteParticles.glassLungs(level, rite, 24);
			case "moon_washed_copper_day" -> UnstainedRiteParticles.moonWashedCopper(level, rite, 40, false);
			case "moon_washed_copper_night" -> UnstainedRiteParticles.moonWashedCopper(level, rite, 40, true);
			default -> {}
		}
	}

	private static void place(ServerLevel level, BlockPos pos, Block block) {
		BlockState state = block.defaultBlockState();
		level.setBlockAndUpdate(pos, state);
		block.setPlacedBy(level, pos, state, null, new ItemStack(block));
	}

	/** A stand-in at the given block, since the bells and the baptism act on a body. */
	private static ServerPlayer kneeler(ServerLevel level, BlockPos near) {
		ServerPlayer kneeler = FakePlayerFactory.get(level, new GameProfile(KNEELER, "StillKneeler"));
		kneeler.moveTo(near.getX() + .5, near.getY(), near.getZ() - .8, 0, 0);
		return kneeler;
	}

	/** Calls the block's own private particle method, so the scene shows exactly what a reading spawns. */
	private static void blockEffect(ServerLevel level, BlockPos pos, String effect) {
		Block block = level.getBlockState(pos).getBlock();
		try {
			Method method = block.getClass().getDeclaredMethod(effect, Level.class, BlockPos.class);
			method.setAccessible(true);
			method.invoke(block, level, pos);
		} catch (ReflectiveOperationException e) {
			Hemomancy.LOGGER.error("UNSTAINED_PARTICLE_REVIEW could not run {} on {}", effect, block, e);
		}
	}
}
