package com.vincenthuto.hemomancy.common.entity.mob.monster;

import net.minecraft.world.entity.Entity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * The Gardens' immune response: a small Y-shaped binder summoned against one body the network has read as foreign. It
 * hunts only that body, and dissolves when its work is done, the body is gone, or its time runs out.
 */
public class AntibodyEntity extends Monster {
	private static final ParticleColor PALE = new ParticleColor(236, 226, 214);

	private UUID summoner;
	private int life;

	public AntibodyEntity(EntityType<? extends AntibodyEntity> type, Level level) {
		super(type, level);
		moveControl = new FlyingMoveControl(this, 20, true);
		setNoGravity(true);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 10.0D)
				.add(Attributes.ATTACK_DAMAGE, 4.0D)
				.add(Attributes.FLYING_SPEED, 0.6D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.FOLLOW_RANGE, 32.0D);
	}

	/** Summons {@code count} antibodies around {@code player}, each bound to them alone. */
	public static void summonFor(ServerPlayer player, int count) {
		ServerLevel level = player.serverLevel();
		for (int i = 0; i < count; i++) {
			AntibodyEntity antibody = EntityInit.antibody.get().create(level);
			if (antibody == null || !placeInOpenAir(antibody, player)) continue;
			antibody.summoner = player.getUUID();
			level.addFreshEntity(antibody);
			level.sendParticles(HemoParticleData.glow(PALE), antibody.getX(), antibody.getY() + 0.4, antibody.getZ(),
					12, 0.25, 0.25, 0.25, 0.02);
		}
	}

	/** A few open spots around the body, then beside its eyes; an antibody never forms inside stone. */
	private static boolean placeInOpenAir(AntibodyEntity antibody, ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		for (int attempt = 0; attempt < 8; attempt++) {
			double angle = level.random.nextDouble() * Math.PI * 2;
			double distance = 3.0 + level.random.nextDouble() * 3.0;
			antibody.moveTo(player.getX() + Math.cos(angle) * distance, player.getY() + 1.0 + level.random.nextInt(2),
					player.getZ() + Math.sin(angle) * distance, level.random.nextFloat() * 360f, 0f);
			if (level.noCollision(antibody)) return true;
		}
		antibody.moveTo(player.getX(), player.getEyeY() - 0.45, player.getZ(), player.getYRot(), 0f);
		return level.noCollision(antibody);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
		navigation.setCanOpenDoors(false);
		navigation.setCanFloat(true);
		return navigation;
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
		goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		// An antibody from a spawn egg answers whoever stands nearest.
		if (summoner == null) {
			Player nearest = level().getNearestPlayer(this, 16.0D);
			if (nearest != null) summoner = nearest.getUUID();
		}
		Player target = summoner == null ? null : level().getPlayerByUUID(summoner);
		if (AntibodyRules.shouldDissolve(++life, target != null, target != null && target.isAlive(),
				target != null && target.level() == level(), target == null ? 0.0 : distanceToSqr(target))) {
			dissolve();
			return;
		}
		if (getTarget() != target) setTarget(target);
	}

	private void dissolve() {
		if (level() instanceof ServerLevel server) {
			server.sendParticles(HemoParticleData.glow(PALE), getX(), getY() + 0.4, getZ(), 8, 0.2, 0.2, 0.2, 0.01);
		}
		discard();
	}

	/** It binds only what it was made for. */
	@Override
	public boolean doHurtTarget(Entity target) {
		return AntibodyRules.mayStrike(summoner, target.getUUID()) && super.doHurtTarget(target);
	}

	public UUID summoner() {
		return summoner;
	}

	public void bindTo(UUID player) {
		summoner = player;
	}

	@Override
	public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state,
			net.minecraft.core.BlockPos pos) {
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (summoner != null) tag.putUUID("Summoner", summoner);
		tag.putInt("Life", life);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		summoner = tag.hasUUID("Summoner") ? tag.getUUID("Summoner") : null;
		life = tag.getInt("Life");
	}
}
