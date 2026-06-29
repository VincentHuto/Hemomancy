package com.vincenthuto.hemomancy.common.manipulation;

import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectSpawner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class HemomancyTendrilEffects {
	private static final int SUTURE_CORE = 0xF8FFF1C8;
	private static final int SUTURE_GLOW = 0xB8FF77C8;
	private static final int VOID_CORE = 0xF8000000;
	private static final int VOID_GLOW = 0xB87518A8;
	private static final int BLOOD_CORE = 0xF80B0102;
	private static final int BLOOD_GLOW = 0xC8B80B18;
	private static final int ICE_CORE = 0xE8F4FBFF;
	private static final int ICE_GLOW = 0xA870CFFF;
	private static final int BONE_CORE = 0xE8EFE5C8;
	private static final int BONE_GLOW = 0xA8B5F4FF;
	private static final int QLIPHOTH_SEED_ROOT_CORE = 0xF0050003;
	private static final int QLIPHOTH_SEED_ROOT_GLOW = 0xB8D10B1A;
	private static final int SILENT_SEVERANCE_TENDRILS = 14;
	private static final double SILENT_SEVERANCE_TENDRIL_SPIN = Math.PI * (3.0D - Math.sqrt(5.0D));

	private HemomancyTendrilEffects() {
	}

	public static TendrilEffectConfig sutureConfig(double range, long seed) {
		return TendrilEffectConfig.defaults()
				.withColors(SUTURE_CORE, SUTURE_GLOW)
				.withRange(range(range))
				.withLifecycle(6, 12, 10)
				.withShape(18, 2, 0.075F, 0.08F)
				.withBranching(2, 1, 0.22F, 0.55F)
				.withWrithe(0.08F, 0.055F, 0.45F, 0.08F)
				.withBlendColors(true)
				.withFixedSeed(true, seed);
	}

	public static TendrilEffectConfig voidConfig(double range, long seed) {
		return TendrilEffectConfig.defaults()
				.withColors(VOID_CORE, VOID_GLOW)
				.withRange(range(range))
				.withLifecycle(5, 10, 13)
				.withShape(20, 2, 0.11F, 0.06F)
				.withBranching(5, 2, 0.32F, 1.05F)
				.withWrithe(0.18F, 0.075F, 0.95F, 0.05F)
				.withBlendColors(false)
				.withFixedSeed(true, seed);
	}

	public static TendrilEffectConfig voidSurfaceConfig(double range, long seed) {
		return voidConfig(range, seed)
				.withMode(TendrilEffectConfig.Mode.SURFACE)
				.withShape(18, 2, 0.085F, 0.06F)
				.withSurface(4.0F, 0.08F);
	}

	public static TendrilEffectConfig bloodDrainConfig(double range, long seed) {
		return TendrilEffectConfig.defaults()
				.withColors(BLOOD_CORE, BLOOD_GLOW)
				.withRange(range(range))
				.withLifecycle(4, 10, 10)
				.withShape(18, 2, 0.095F, 0.07F)
				.withBranching(3, 2, 0.28F, 0.85F)
				.withWrithe(0.12F, 0.07F, 0.7F, 0.1F)
				.withBlendColors(false)
				.withFixedSeed(true, seed);
	}

	public static TendrilEffectConfig iceConfig(double range, long seed) {
		return TendrilEffectConfig.defaults()
				.withColors(ICE_CORE, ICE_GLOW)
				.withRange(range(range))
				.withLifecycle(5, 8, 11)
				.withShape(14, 1, 0.055F, 0.05F)
				.withBranching(2, 1, 0.2F, 0.65F)
				.withWrithe(0.05F, 0.035F, 0.25F, 0.02F)
				.withBlendColors(true)
				.withFixedSeed(true, seed);
	}

	public static TendrilEffectConfig boneConfig(double range, long seed) {
		return TendrilEffectConfig.defaults()
				.withColors(BONE_CORE, BONE_GLOW)
				.withRange(range(range))
				.withLifecycle(5, 12, 12)
				.withShape(16, 2, 0.08F, 0.05F)
				.withBranching(3, 1, 0.26F, 0.7F)
				.withWrithe(0.07F, 0.045F, 0.35F, 0.04F)
				.withBlendColors(true)
				.withFixedSeed(true, seed);
	}

	public static TendrilEffectConfig rootConfig(double range, long seed) {
		return TendrilEffectConfig.defaults()
				.withColors(QLIPHOTH_SEED_ROOT_CORE, QLIPHOTH_SEED_ROOT_GLOW)
				.withRange(range(range))
				.withLifecycle(6, 12, 12)
				.withShape(18, 2, 0.075F, 0.06F)
				.withBranching(4, 2, 0.3F, 0.95F)
				.withWrithe(0.14F, 0.055F, 0.85F, 0.08F)
				.withBlendColors(false)
				.withFixedSeed(true, seed);
	}

	public static TendrilEffectConfig silentSeveranceConfig(double range, long seed) {
		return TendrilEffectConfig.defaults()
				.withMode(TendrilEffectConfig.Mode.FREEFORM)
				.withColors(QLIPHOTH_SEED_ROOT_CORE, QLIPHOTH_SEED_ROOT_GLOW)
				.withRange(range(range))
				.withLifecycle(3, 8, 10)
				.withShape(18, 2, 0.052F, 0.055F)
				.withBranching(2, 1, 0.16F, 0.72F)
				.withWrithe(0.085F, 0.09F, 0.22F, 0.0F)
				.withBlendColors(false)
				.withFixedSeed(true, seed);
	}

	public static void lumenSuture(Player caster, Player target) {
		if (!(caster.level() instanceof ServerLevel level)) {
			return;
		}
		long seed = worldSeed(level, caster, target.getId());
		spawn(level, caster, entity(caster, TendrilAnchor.AnchorPoint.CENTER, 0.0D, 0.18D, 0.0D),
				entity(target, TendrilAnchor.AnchorPoint.CENTER, 0.0D, 0.08D, 0.0D),
				sutureConfig(caster.distanceTo(target) + 4.0D, seed));
	}

	public static void bloodEclipse(Player caster, LivingEntity target, int index) {
		if (!(caster.level() instanceof ServerLevel level)) {
			return;
		}
		long seed = worldSeed(level, caster, target.getId() * 37L + index);
		spawn(level, caster, entity(caster, TendrilAnchor.AnchorPoint.CENTER, 0.0D, 0.12D, 0.0D),
				entity(target, TendrilAnchor.AnchorPoint.CENTER, 0.0D, 0.0D, 0.0D),
				voidConfig(caster.distanceTo(target) + 5.0D, seed));
	}

	public static void blackVeil(ServerLevel level, Player caster, BlockPos center, double radius) {
		Vec3 origin = Vec3.atCenterOf(center).add(0.0D, 0.18D, 0.0D);
		int count = 8;
		double rotation = level.getGameTime() * 0.17D + caster.getId();
		for (int i = 0; i < count; i++) {
			double angle = rotation + i * Math.PI * 2.0D / count;
			double distance = radius * (0.45D + 0.45D * level.random.nextDouble());
			Vec3 end = origin.add(Math.cos(angle) * distance, level.random.nextDouble() * 0.75D,
					Math.sin(angle) * distance);
			spawn(level, caster, new TendrilAnchor.Point(origin), new TendrilAnchor.Point(end),
					voidSurfaceConfig(radius + 4.0D, worldSeed(level, caster, center.asLong() + i)));
		}
	}

	public static void silentSeverance(ServerPlayer player, double radius) {
		ServerLevel level = player.serverLevel();
		Vec3 origin = player.position().add(0.0D, player.getBbHeight() * 0.58D, 0.0D);
		double rotation = level.getGameTime() * 0.29D + player.getId();
		for (int i = 0; i < SILENT_SEVERANCE_TENDRILS; i++) {
			double angle = rotation + i * SILENT_SEVERANCE_TENDRIL_SPIN;
			double reach = radius * (0.52D + 0.35D * level.random.nextDouble());
			double height = -0.45D + 1.45D * level.random.nextDouble();
			Vec3 end = origin.add(Math.cos(angle) * reach, height, Math.sin(angle) * reach);
			long seed = worldSeed(level, player, i * 0x51F15EEDL + Double.doubleToLongBits(reach));
			spawn(level, player, entity(player, TendrilAnchor.AnchorPoint.CENTER, 0.0D, 0.16D, 0.0D),
					new TendrilAnchor.Point(end), silentSeveranceConfig(radius + 4.0D, seed));
		}
	}

	public static void hemorrhage(Player caster, LivingEntity target) {
		if (!(caster.level() instanceof ServerLevel level)) {
			return;
		}
		long seed = worldSeed(level, caster, target.getId());
		spawn(level, caster, entity(caster, TendrilAnchor.AnchorPoint.CENTER, 0.0D, 0.1D, 0.0D),
				entity(target, TendrilAnchor.AnchorPoint.CENTER, 0.0D, 0.0D, 0.0D),
				bloodDrainConfig(caster.distanceTo(target) + 4.0D, seed));
	}

	public static void exsanguinate(Player caster, LivingEntity target) {
		if (!(caster.level() instanceof ServerLevel level)) {
			return;
		}
		long seed = worldSeed(level, caster, target.getId() ^ 0x51F15EEDL);
		spawn(level, caster, entity(target, TendrilAnchor.AnchorPoint.CENTER, 0.0D, 0.0D, 0.0D),
				entity(caster, TendrilAnchor.AnchorPoint.CENTER, 0.0D, 0.1D, 0.0D),
				bloodDrainConfig(caster.distanceTo(target) + 4.0D, seed));
	}

	public static void osseousBloom(Player caster, List<LivingEntity> targets) {
		if (!(caster.level() instanceof ServerLevel level) || targets.isEmpty()) {
			return;
		}
		Vec3 origin = caster.position().add(0.0D, 0.22D, 0.0D);
		int count = Math.min(6, targets.size());
		for (int i = 0; i < count; i++) {
			LivingEntity target = targets.get((int) Math.floor(i * targets.size() / (double) count));
			spawn(level, caster, new TendrilAnchor.Point(origin),
					entity(target, TendrilAnchor.AnchorPoint.FEET, 0.0D, 0.08D, 0.0D),
					boneConfig(caster.distanceTo(target) + 4.0D, worldSeed(level, caster, target.getId() + i)));
		}
	}

	public static void glacialGrasp(Player caster, BlockPos center, List<BlockPos> frozenTargets) {
		if (!(caster.level() instanceof ServerLevel level) || frozenTargets.isEmpty()) {
			return;
		}
		Vec3 origin = Vec3.atCenterOf(center).add(0.0D, 0.08D, 0.0D);
		int count = Math.min(7, frozenTargets.size());
		for (int i = 0; i < count; i++) {
			BlockPos target = frozenTargets.get((int) Math.floor(i * frozenTargets.size() / (double) count));
			Vec3 end = Vec3.atCenterOf(target).add(0.0D, 0.1D, 0.0D);
			spawn(level, caster, new TendrilAnchor.Point(origin), new TendrilAnchor.Point(end),
					iceConfig(origin.distanceTo(end) + 3.0D, worldSeed(level, caster, target.asLong() + i)));
		}
	}

	private static long worldSeed(ServerLevel level, Player player, long salt) {
		return seed(player.getUUID().getLeastSignificantBits(), level.getGameTime(), salt);
	}

	public static long seed(long playerSeed, long time, long salt) {
		long value = playerSeed ^ Long.rotateLeft(time, 17) ^ Long.rotateLeft(salt, 41) ^ 0x9E3779B97F4A7C15L;
		value ^= value >>> 33;
		value *= 0xff51afd7ed558ccdL;
		value ^= value >>> 33;
		value *= 0xc4ceb9fe1a85ec53L;
		value ^= value >>> 33;
		return value;
	}

	private static float range(double distance) {
		return (float) Math.max(8.0D, distance + 4.0D);
	}

	private static TendrilAnchor.Entity entity(LivingEntity entity, TendrilAnchor.AnchorPoint point, double x, double y,
			double z) {
		return new TendrilAnchor.Entity(entity.getId(), point, new Vec3(x, y, z));
	}

	private static void spawn(ServerLevel level, Player player, TendrilAnchor start, TendrilAnchor end,
			TendrilEffectConfig config) {
		TendrilEffectSpawner.spawn(level, serverPlayer(player), start, end, config);
	}

	@Nullable
	private static ServerPlayer serverPlayer(Player player) {
		return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
	}
}
