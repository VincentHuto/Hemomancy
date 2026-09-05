package com.vincenthuto.hemomancy.common.entity.mob.animal;

import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public final class PeacockSpiderEntity extends Spider {
	private static final DustParticleOptions CYAN =
			new DustParticleOptions(new Vector3f(0.06F, 0.72F, 0.82F), 0.8F);
	private static final DustParticleOptions MAGENTA =
			new DustParticleOptions(new Vector3f(0.88F, 0.12F, 0.55F), 0.8F);

	public PeacockSpiderEntity(EntityType<? extends PeacockSpiderEntity> type, Level level) {
		super(type, level);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new FloatGoal(this));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	public static boolean canSpawnHere(EntityType<PeacockSpiderEntity> type, LevelAccessor level,
			MobSpawnType reason, BlockPos pos, RandomSource random) {
		long time = level.getLevelData().getDayTime() % 24000L;
		return time < 12000L && level.canSeeSky(pos) && level.getMaxLocalRawBrightness(pos) >= 9
				&& level.getBlockState(pos.below()).is(BlockTags.DIRT)
				&& level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel server) || tickCount % 40 != 0
				|| level().getNearestPlayer(this, 6.0D) == null) return;
		for (int i = 0; i < 9; i++) {
			double angle = Math.PI * i / 8.0D;
			server.sendParticles((i & 1) == 0 ? CYAN : MAGENTA,
					getX() + Math.cos(angle) * 0.65D, getY() + 0.25D + Math.sin(angle) * 0.65D,
					getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
		}
		server.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
				SoundSource.NEUTRAL, 0.18F, 1.8F);
	}

	@Override
	public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
		return false;
	}
}
