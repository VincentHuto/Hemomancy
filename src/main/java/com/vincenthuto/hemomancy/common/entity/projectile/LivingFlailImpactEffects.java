package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.TendencyWeaponHelper;
import com.vincenthuto.hemomancy.common.network.particle.LivingFlailImpactPacket;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;

public final class LivingFlailImpactEffects {
	private static final ParticleColor ICE_BLUE = new ParticleColor(95, 205, 255);
	private static final ParticleColor PALE_ICE = new ParticleColor(205, 245, 255);

	private LivingFlailImpactEffects() {
	}

	public static void impact(ServerLevel level, LivingFlailHeadProjectileEntity projectile, Vec3 center,
			boolean timeout) {
		if (!(projectile.getOwner() instanceof LivingEntity owner)) return;
		float impactScale = LivingFlailImpactRules.timeoutImpactScale(timeout);
		float charge = projectile.getCharge();
		float radius = LivingFlailRules.impactRadius(charge) * impactScale;
		AABB area = new AABB(center, center).inflate(radius);
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
			boolean valid = LivingFlailImpactRules.isValidTarget(target == owner, target.isAlliedTo(owner),
					target.isAlive(), owner.canAttack(target));
			if (!valid || target.distanceToSqr(center) > radius * radius) continue;
			float tendencyMultiplier = owner instanceof net.minecraft.world.entity.player.Player player
					? TendencyWeaponHelper.getDamageMultiplier(player, target, projectile.getPrimaryTendency(),
							projectile.getSecondaryTendency()) : 1.0F;
			target.hurt(HemoDamageTypes.livingFlailFreeze(level, projectile, owner),
					LivingFlailRules.damage(charge) * impactScale * tendencyMultiplier);
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
					Math.round(LivingFlailRules.slownessTicks(charge) * impactScale),
					LivingFlailRules.slownessAmplifier(charge)));
			Vec3 outward = target.position().subtract(center).multiply(1.0D, 0.0D, 1.0D);
			if (outward.lengthSqr() > 1.0E-5D) {
				outward = outward.normalize().scale(LivingFlailRules.knockback(charge) * impactScale);
				target.push(outward.x, 0.16D + 0.18D * charge, outward.z);
			}
		}
		mutateTerrain(level, owner, center, charge, impactScale);
		emitImpact(level, center, charge, impactScale, projectile.getUUID().getLeastSignificantBits());
		PacketDistributor.sendToPlayersNear(level, null, center.x, center.y, center.z, 24.0D,
				new LivingFlailImpactPacket(charge * impactScale, projectile.getId()));
		level.playSound(null, BlockPos.containing(center), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS,
				0.8F + charge * 1.4F, 0.95F - charge * 0.35F);
		level.playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS,
				0.35F + charge * 0.75F, 1.2F - charge * 0.35F);
	}

	public static void emitProjectileTrail(ServerLevel level, LivingFlailHeadProjectileEntity projectile) {
		float charge = projectile.getCharge();
		int glowCount = 1 + Math.round(charge * 2.0F);
		level.sendParticles(GlowParticleFactory.createData(ICE_BLUE), projectile.getX(), projectile.getY(), projectile.getZ(),
				glowCount, 0.08D, 0.08D, 0.08D, 0.01D);
		if (projectile.tickCount % 4 == 0) {
			level.sendParticles(BloodCellParticleFactory.createData(PALE_ICE), projectile.getX(), projectile.getY(),
					projectile.getZ(), 1, 0.06D, 0.06D, 0.06D, 0.005D);
		}
		if (projectile.tickCount % 6 == 0) {
			Vec3 end = projectile.position().subtract(projectile.getDeltaMovement().scale(1.8D));
			spawnTendril(level, projectile.position(), end, projectile.getUUID().getLeastSignificantBits() ^ projectile.tickCount,
					0.45F + charge * 0.45F);
		}
	}

	private static void mutateTerrain(ServerLevel level, LivingEntity owner, Vec3 center, float charge,
			float impactScale) {
		int waterRadius = Math.max(1, Math.round(LivingFlailRules.impactRadius(charge) * impactScale));
		int snowRadius = Math.max(1, Math.round(LivingFlailRules.snowRadius(charge) * impactScale));
		BlockPos origin = BlockPos.containing(center);
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-waterRadius, -waterRadius, -waterRadius),
				origin.offset(waterRadius, waterRadius, waterRadius))) {
			if (pos.distSqr(origin) > waterRadius * waterRadius) continue;
			BlockState state = level.getBlockState(pos);
			boolean sourceWater = state.getFluidState().is(Fluids.WATER) && state.getFluidState().isSource();
			if (sourceWater && state.is(Blocks.WATER) && LivingFlailTerrainRules.mayFreezeWater(true, true,
					level.getBlockEntity(pos) != null, protectionAllows(level, owner, pos))) {
				level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
			}
		}
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-snowRadius, -2, -snowRadius),
				origin.offset(snowRadius, 2, snowRadius))) {
			if (pos.getCenter().multiply(1.0D, 0.0D, 1.0D).distanceToSqr(
					origin.getCenter().multiply(1.0D, 0.0D, 1.0D)) > snowRadius * snowRadius) continue;
			BlockState target = level.getBlockState(pos);
			boolean snow = target.is(Blocks.SNOW);
			int layers = snow ? target.getValue(SnowLayerBlock.LAYERS) : 0;
			boolean air = target.isAir();
			boolean sturdy = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
			if (!sturdy || !(air || snow) || snow && layers >= 8 || level.getBlockEntity(pos) != null) continue;
			if (!LivingFlailTerrainRules.mayPlaceSnow(sturdy, air, snow, layers,
					false, protectionAllows(level, owner, pos))) continue;
			BlockState placed = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,
					LivingFlailTerrainRules.nextSnowLayers(layers));
			if (placed.canSurvive(level, pos)) level.setBlock(pos, placed, 3);
		}
	}

	private static boolean protectionAllows(ServerLevel level, LivingEntity owner, BlockPos pos) {
		BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos, 3);
		return !EventHooks.onBlockPlace(owner, snapshot, Direction.UP);
	}

	private static void emitImpact(ServerLevel level, Vec3 center, float charge, float impactScale, long seed) {
		int population = Math.max(6, Math.round((12.0F + 32.0F * charge) * impactScale));
		level.sendParticles(GlowParticleFactory.createData(ICE_BLUE), center.x, center.y, center.z,
				population, 0.4D + charge, 0.2D + charge * 0.5D, 0.4D + charge, 0.08D + charge * 0.08D);
		level.sendParticles(BloodCellParticleFactory.createData(PALE_ICE), center.x, center.y + 0.1D, center.z,
				Math.max(2, population / 5), 0.3D + charge, 0.25D, 0.3D + charge, 0.04D);
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState()),
				center.x, center.y, center.z, population, 0.6D + charge, 0.35D, 0.6D + charge, 0.12D);
		int tendrils = 3 + Math.round(charge * 5.0F);
		for (int i = 0; i < tendrils; i++) {
			double angle = Math.PI * 2.0D * i / tendrils;
			Vec3 end = center.add(Math.cos(angle) * (1.2D + charge * 2.2D), 0.08D,
					Math.sin(angle) * (1.2D + charge * 2.2D));
			spawnTendril(level, center.add(0.0D, 0.2D, 0.0D), end, seed + i, 0.7F + charge);
		}
	}

	private static void spawnTendril(ServerLevel level, Vec3 start, Vec3 end, long seed, float range) {
		TendrilEffectConfig config = TendrilEffectConfig.defaults()
				.withColors(0xE060D8FF, 0xB0D8FAFF).withRange(range + 3.0F)
				.withLifecycle(2, 8, 5).withShape(14, 2, 0.055F, 0.04F)
				.withBranching(2, 1, 0.16F, 0.6F).withWrithe(0.045F, 0.04F, 0.24F, 0.035F)
				.withBlendColors(true).withFixedSeed(true, seed);
		TendrilEffectSpawner.spawn(level, new TendrilAnchor.Point(start), new TendrilAnchor.Point(end), config);
	}
}
