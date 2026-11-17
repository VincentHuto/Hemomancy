package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.item.EntityMorphlingPolypItem;
import com.vincenthuto.hemomancy.common.entity.projectile.DirectedBloodOrbEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.HemolyticVialEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BloodThrallEntity;
import com.vincenthuto.hemomancy.common.entity.summon.PhantasmalEchoEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.lang.reflect.Method;

/**
 * With hemomancy.summonParticleReview, stands three Phantasmal Echoes and two Blood Thralls on a dark floor, then shows
 * an Echo's ambient shimmer, its smoke burst and its quiet dissolve, a Thrall carrying blood and a Thrall's death
 * splatter. The moments are the entities' own public calls and entity event, so no armour ability or Thrall item runs.
 * Then an Iron Spike and Pillar raised by no player (as Vesper's and the Weave's are), so their eruption, seep and
 * flakes show, a Directed Blood Orb's trail, a Hemolytic Vial's burst (its own private method, by reflection) and a
 * dropped Morphling Polyp's motes. Shots land in build/summon-particle-review/screenshots. Excluded from release jars.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class SummonParticleClientReview {
	private static final byte THRALL_SPLATTER_EVENT = 60;
	private static int ticks;
	private static int shot;
	private static BlockPos row;
	private static final PhantasmalEchoEntity[] ECHOES = new PhantasmalEchoEntity[3];
	private static final BloodThrallEntity[] THRALLS = new BloodThrallEntity[2];
	/** Eye offset from the row origin, the point looked at, and the scene action fired just before the capture. */
	private static final Object[][] SHOTS = {
			{new double[]{0.5, 2.0, -3.4, 0.5, 1.0, 0.5}, "none"},
			{new double[]{4.5, 2.4, -4.4, 4.5, 1.0, 0.5}, "echo_burst"},
			{new double[]{8.5, 2.0, -3.4, 8.5, 1.0, 0.5}, "echo_dissolve"},
			{new double[]{12.5, 1.8, -2.8, 12.5, 0.7, 0.5}, "none"},
			{new double[]{16.5, 1.8, -2.8, 16.5, 0.7, 0.5}, "thrall_splatter"},
			{new double[]{21.5, 2.6, -5.4, 21.5, 1.6, 0.5}, "ferric_raise"},
			{new double[]{21.5, 2.6, -5.4, 21.5, 1.4, 0.5}, "none"},
			{new double[]{28.5, 1.8, -2.8, 28.5, 1.2, 0.5}, "orb"},
			{new double[]{32.5, 2.2, -3.4, 32.5, 0.8, 0.5}, "vial_burst"},
			{new double[]{36.5, 1.2, -1.8, 36.5, 0.3, 0.5}, "polyp"}};

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		if (!Boolean.getBoolean("hemomancy.summonParticleReview")) return;
		var mc = Minecraft.getInstance();
		if (!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("summon-particle-review")) return;
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
				for (BlockPos pos : BlockPos.betweenClosed(row.offset(-4, -1, -8), row.offset(40, 8, 6)))
					level.setBlock(pos, pos.getY() < row.getY() ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
							: Blocks.AIR.defaultBlockState(), 2);
				for (int i = 0; i < ECHOES.length; i++) ECHOES[i] = echo(level, player, row.offset(i * 4, 0, 0));
				for (int i = 0; i < THRALLS.length; i++) {
					THRALLS[i] = place(level, EntityInit.blood_thrall.get().create(level), row.offset(12 + i * 4, 0, 0));
					THRALLS[i].setOwnerUUID(player.getUUID());
					THRALLS[i].setCarriedBlood(BloodThrallEntity.MAX_CARRY);
				}
				Hemomancy.LOGGER.info("SUMMON_PARTICLE_REVIEW placed {} echoes, {} thralls", ECHOES.length, THRALLS.length);
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
			Screenshot.grab(mc.gameDirectory, "summon-particle-" + shot + "-" + SHOTS[shot][1] + ".png",
					mc.getMainRenderTarget(), message -> {});
			Hemomancy.LOGGER.info("SUMMON_PARTICLE_REVIEW captured {}", shot);
			shot++;
		}
	}

	private static void act(ServerLevel level, String action) {
		switch (action) {
			case "echo_burst" -> ECHOES[1].burstSmokeCloud();
			case "echo_dissolve" -> ECHOES[2].dissolveQuietly();
			case "thrall_splatter" -> level.broadcastEntityEvent(THRALLS[1], THRALL_SPLATTER_EVENT);
			case "ferric_raise" -> {
				var spike = EntityInit.iron_spike.get().create(level);
				spike.moveTo(row.getX() + 20.5, row.getY(), row.getZ() + .5, 0, 0);
				level.addFreshEntity(spike);
				var pillar = EntityInit.iron_pillar.get().create(level);
				pillar.moveTo(row.getX() + 22.5, row.getY(), row.getZ() + .5, 0, 0);
				level.addFreshEntity(pillar);
			}
			case "orb" -> {
				var orb = new DirectedBloodOrbEntity(level);
				orb.moveTo(row.getX() + 28.5, row.getY() + 1.2, row.getZ() + .5, 0, 0);
				orb.setNoGravity(true);
				level.addFreshEntity(orb);
			}
			case "vial_burst" -> {
				try {
					Method burst = HemolyticVialEntity.class.getDeclaredMethod("spawnBurstParticles", ServerLevel.class, Vec3.class);
					burst.setAccessible(true);
					burst.invoke(EntityInit.hemolytic_vial_projectile.get().create(level), level,
							new Vec3(row.getX() + 32.5, row.getY() + .8, row.getZ() + .5));
				} catch (ReflectiveOperationException e) {
					Hemomancy.LOGGER.error("SUMMON_PARTICLE_REVIEW could not burst the vial", e);
				}
			}
			case "polyp" -> {
				var polyp = new EntityMorphlingPolypItem(EntityInit.morphling_polyp_item.get(), level,
						row.getX() + 36.5, row.getY() + .1, row.getZ() + .5, new ItemStack(ItemInit.morphling_polyp.get()));
				polyp.setPickUpDelay(32767);
				polyp.setDeltaMovement(Vec3.ZERO);
				level.addFreshEntity(polyp);
			}
			default -> {}
		}
		Hemomancy.LOGGER.info("SUMMON_PARTICLE_REVIEW {} fired", action);
	}

	private static PhantasmalEchoEntity echo(ServerLevel level, ServerPlayer owner, BlockPos at) {
		PhantasmalEchoEntity echo = place(level, EntityInit.phantasmal_echo.get().create(level), at);
		echo.configureFromOwner(owner, PhantasmalEchoEntity.BehaviorMode.AGGRESSIVE, 24000, Vec3.ZERO);
		return echo;
	}

	private static <T extends Mob> T place(ServerLevel level, T mob, BlockPos at) {
		mob.moveTo(at.getX() + .5, at.getY(), at.getZ() + .5, 180, 0);
		mob.setNoAi(true);
		mob.setPersistenceRequired();
		level.addFreshEntity(mob);
		return mob;
	}
}
