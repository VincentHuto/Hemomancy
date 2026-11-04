package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class HemolymphopodaEntity extends PathfinderMob {


	public HemolymphopodaEntity(EntityType<? extends HemolymphopodaEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.12D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new PanicGoal(this, 1.4D));
		this.goalSelector.addGoal(1, new com.vincenthuto.hemomancy.common.entity.mob.aquatic.PelagicBottomForageGoal(this,
				com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicHabitatRules.Species.HEMOLYMPHOPODA, () -> isInWater()));
		this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.5D));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	public static boolean canSpawnHere(EntityType<? extends HemolymphopodaEntity> type, LevelAccessor world,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		BlockPos below = pos.below();
		if (com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicHabitat.layer(world, pos) == com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer.SHORE)
			return com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicHabitat.suitable(world, pos,
					com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicHabitatRules.Species.HEMOLYMPHOPODA);
		return world.getBlockState(below).isSolidRender(world, below)
				&& pos.getY() <= world.getSeaLevel() + 16 && nearWater(world, pos);
	}

	/** Tide-pool fauna: off the shore it still needs standing water within two blocks, not a dry gorge or cave. */
	private static boolean nearWater(LevelAccessor world, BlockPos pos) {
		for (BlockPos probe : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
			if (world.getFluidState(probe).is(net.minecraft.tags.FluidTags.WATER)) return true;
		}
		return false;
	}


	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_HEMOLYMPHOPODA_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_HEMOLYMPHOPODA_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_HEMOLYMPHOPODA_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.3F;
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public int getMaxAirSupply() {
		return 4800;
	}

	@Override protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
		return new net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation(this, level);
	}
	@Override public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader level) { return level.isUnobstructed(this); }

	@Override
	public boolean isPushedByFluid() {
		return false;
	}
}
