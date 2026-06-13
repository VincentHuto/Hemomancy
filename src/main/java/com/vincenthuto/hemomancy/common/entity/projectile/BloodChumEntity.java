package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.effect.ChummedWatersAreaManager;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BloodChumEntity extends ThrowableItemProjectile {
	public BloodChumEntity(EntityType<? extends BloodChumEntity> type, Level level) {
		super(type, level);
	}

	public BloodChumEntity(Level level, LivingEntity owner) {
		super(EntityInit.blood_chum_projectile.get(), owner, level);
	}

	@Override
	protected Item getDefaultItem() {
		return ItemInit.blood_chum.get();
	}

	@Override
	protected void onHit(HitResult result) {
		if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
			Vec3 impact = result.getLocation();
			BlockPos waterPos = findWater(serverLevel, BlockPos.containing(impact));
			if (waterPos != null) {
				Vec3 center = Vec3.atCenterOf(waterPos);
				ChummedWatersAreaManager.addArea(serverLevel, center);
				serverLevel.sendParticles(ParticleTypes.FISHING, center.x, center.y + 0.1D, center.z,
						24, 1.1D, 0.1D, 1.1D, 0.02D);
				serverLevel.sendParticles(ParticleTypes.BUBBLE, center.x, center.y + 0.1D, center.z,
						18, 0.8D, 0.15D, 0.8D, 0.01D);
				level().playSound(null, waterPos, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.85F, 0.75F);
			} else {
				level().playSound(null, blockPosition(), SoundEvents.SLIME_BLOCK_BREAK, SoundSource.PLAYERS, 0.45F, 0.75F);
			}
		}
		this.remove(RemovalReason.KILLED);
	}

	private static BlockPos findWater(ServerLevel level, BlockPos origin) {
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
			if (level.getFluidState(pos).is(FluidTags.WATER)) {
				return pos.immutable();
			}
		}
		return null;
	}
}
