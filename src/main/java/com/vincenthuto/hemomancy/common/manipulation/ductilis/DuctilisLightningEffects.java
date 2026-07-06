package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class DuctilisLightningEffects {
	private static final int OUTER_YELLOW = 0xE8FFE84A;
	private static final int INNER_WHITE = 0xFFFFFFFF;
	private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));

	private DuctilisLightningEffects() {
	}

	public static void activationPotential(Player player, LivingEntity target, int index) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}

		Vec3 start = player.getEyePosition().subtract(0.0D, 0.35D, 0.0D);
		Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
		long seed = seed(level, player, target.getId() * 31L + index);
		spawn(level, player, start, end, seed, 64.0F, 2.0F, 7, 7, 0.11F, 0.045F);

		if (index < 6) {
			RandomSource random = level.random;
			Vec3 forkEnd = end.add(randomSigned(random, 0.28D), random.nextDouble() * 0.25D - 0.05D,
					randomSigned(random, 0.28D));
			spawn(level, player, end, forkEnd, seed ^ 0x5DEECE66DL, 48.0F, 1.6F, 5, 4, 0.06F, 0.023F);
		}
	}

	public static void synapticJolt(LivingEntity caster, LivingEntity target) {
		if (!(caster.level() instanceof ServerLevel level)) {
			return;
		}

		Vec3 start = caster.getEyePosition().subtract(0.0D, 0.18D, 0.0D);
		Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.62D, 0.0D);
		long seed = seed(level, caster, target.getId() * 17L);
		spawn(level, caster, start, end, seed, 72.0F, 2.4F, 6, 8, 0.09F, 0.038F);
		spawn(level, caster, end, end.add(randomSigned(level.random, 0.35D), 0.18D,
				randomSigned(level.random, 0.35D)), seed ^ 0xBADC0FFEE0DDF00DL, 56.0F, 2.0F, 4, 5, 0.05F, 0.026F);
	}

	public static void conductiveMark(LivingEntity caster, LivingEntity target) {
		if (!(caster.level() instanceof ServerLevel level)) {
			return;
		}

		Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
		double rotation = level.getGameTime() * 0.19D + target.getId();
		for (int i = 0; i < 5; i++) {
			double angle = rotation + i * Math.PI * 2.0D / 5.0D;
			Vec3 start = center.add(Math.cos(angle) * 0.52D, -0.25D + (i % 2) * 0.24D, Math.sin(angle) * 0.52D);
			Vec3 end = center.add(Math.cos(angle + 0.95D) * 0.52D, 0.25D - (i % 2) * 0.24D,
					Math.sin(angle + 0.95D) * 0.52D);
			spawn(level, caster, start, end, seed(level, caster, target.getId() * 53L + i), 44.0F, 1.6F, 5, 5,
					0.055F, 0.024F);
		}
	}

	public static void conductiveArc(LivingEntity source, LivingEntity target, int index) {
		if (!(source.level() instanceof ServerLevel level)) {
			return;
		}

		Vec3 start = source.position().add(0.0D, source.getBbHeight() * 0.58D, 0.0D);
		Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
		spawn(level, source, start, end, seed(level, source, target.getId() * 97L + index), 62.0F, 2.1F, 6, 7,
				0.085F, 0.033F);
	}

	public static void hemolymphalPulse(Player player) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}

		Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.58D, 0.0D);
		double rotation = level.getGameTime() * 0.13D + player.getId();
		int count = 12;
		for (int i = 0; i < count; i++) {
			Vec3 direction = sphericalDirection(i, count, rotation);
			double distance = 1.35D + level.random.nextDouble() * 0.95D;
			Vec3 start = center.add(direction.scale(0.24D));
			Vec3 end = center.add(direction.scale(distance));
			spawn(level, player, start, end, seed(level, player, i), 40.0F, 1.8F, 8, 5, 0.09F, 0.03F);
		}
	}

	public static void sanguineWard(Player player) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}

		Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.56D, 0.0D);
		double rotation = level.getGameTime() * 0.21D + player.getYRot() * Math.PI / 180.0D;
		for (int i = 0; i < 6; i++) {
			double angle = rotation + i * Math.PI / 3.0D;
			double nextAngle = angle + Math.PI * 0.38D;
			Vec3 start = center.add(Math.cos(angle) * 0.72D, -0.25D + (i % 2) * 0.5D, Math.sin(angle) * 0.72D);
			Vec3 end = center.add(Math.cos(nextAngle) * 0.72D, 0.25D - (i % 2) * 0.5D,
					Math.sin(nextAngle) * 0.72D);
			spawn(level, player, start, end, seed(level, player, 80L + i), 36.0F, 1.4F, 8, 5, 0.08F, 0.032F);
		}
	}

	public static void crimsonHarvest(Player player, BlockPos target, int index) {
		if (!(player.level() instanceof ServerLevel level)) {
			return;
		}

		Vec3 start = player.position().add(0.0D, 0.95D, 0.0D);
		Vec3 end = Vec3.atCenterOf(target).add(0.0D, 0.08D + level.random.nextDouble() * 0.22D, 0.0D);
		spawn(level, player, start, end, seed(level, player, target.asLong() + index), 48.0F, 1.8F, 6, 5, 0.08F,
				0.025F);
	}

	private static Vec3 sphericalDirection(int index, int count, double rotation) {
		double y = 1.0D - (index + 0.5D) * 2.0D / count;
		double horizontalRadius = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
		double theta = index * GOLDEN_ANGLE + rotation;
		return new Vec3(Math.cos(theta) * horizontalRadius, y, Math.sin(theta) * horizontalRadius);
	}

	private static void spawn(ServerLevel level, Player player, Vec3 start, Vec3 end, long seed, float ticksPerMeter,
			float speed, int maxAge, int fract, float maxOffset, float size) {
		LightningTesterSpawner.spawn(level, serverPlayer(player), start, end,
				config(start.distanceTo(end), seed, ticksPerMeter, speed, maxAge, fract, maxOffset, size));
	}

	private static void spawn(ServerLevel level, LivingEntity caster, Vec3 start, Vec3 end, long seed,
			float ticksPerMeter, float speed, int maxAge, int fract, float maxOffset, float size) {
		LightningTesterSpawner.spawn(level, caster instanceof ServerPlayer serverPlayer ? serverPlayer : null, start, end,
				config(start.distanceTo(end), seed, ticksPerMeter, speed, maxAge, fract, maxOffset, size));
	}

	private static LightningTestConfig config(double range, long seed, float ticksPerMeter, float speed, int maxAge,
			int fract, float maxOffset, float size) {
		return new LightningTestConfig(LightningTestConfig.Backend.BOLT, OUTER_YELLOW, OUTER_YELLOW, INNER_WHITE,
				(float) Math.max(8.0D, range + 4.0D), 0.0F, 0.0F, 0.0F, ticksPerMeter, speed, maxAge, fract,
				maxOffset, size, true, seed, false, 20);
	}

	@Nullable
	private static ServerPlayer serverPlayer(Player player) {
		return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
	}

	private static long seed(ServerLevel level, Player player, long salt) {
		return level.random.nextLong() ^ player.getUUID().getLeastSignificantBits() ^ (level.getGameTime() << 16)
				^ salt;
	}

	private static long seed(ServerLevel level, LivingEntity caster, long salt) {
		return level.random.nextLong() ^ caster.getUUID().getLeastSignificantBits() ^ (level.getGameTime() << 16)
				^ salt;
	}

	private static double randomSigned(RandomSource random, double amount) {
		return (random.nextDouble() * 2.0D - 1.0D) * amount;
	}
}
