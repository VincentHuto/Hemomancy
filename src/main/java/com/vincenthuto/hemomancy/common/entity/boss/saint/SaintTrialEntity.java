package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * O-1: the entombed Saints fought in a trial chamber (Hemorath, Putriciel, Velorum). They share a persistent body,
 * a boss bar, a synced visual state that the renderer reads (a state id and the ticks it has left), the floor-ring
 * telegraph and the chat lines sent to everyone in the fight. Each Saint keeps its own mechanics, goals and drops.
 */
public abstract class SaintTrialEntity extends Monster {
	private static final EntityDataAccessor<Integer> DATA_VISUAL_STATE =
			SynchedEntityData.defineId(SaintTrialEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_VISUAL_TICKS =
			SynchedEntityData.defineId(SaintTrialEntity.class, EntityDataSerializers.INT);

	public static final int VISUAL_NONE = 0;

	protected final BossBarBinding bossBar;

	protected SaintTrialEntity(EntityType<? extends SaintTrialEntity> type, Level level, int xpReward,
			BossBarBinding bossBar) {
		super(type, level);
		this.bossBar = bossBar;
		this.setPersistenceRequired();
		this.xpReward = xpReward;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_VISUAL_STATE, VISUAL_NONE);
		builder.define(DATA_VISUAL_TICKS, 0);
	}

	/** The melee trial goals; only the stroll speed differs between Saints that use them. */
	protected void registerTrialGoals(double strollSpeed) {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, strollSpeed));
		this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 24.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public int getVisualState() {
		return this.entityData.get(DATA_VISUAL_STATE);
	}

	public int getVisualTicks() {
		return this.entityData.get(DATA_VISUAL_TICKS);
	}

	public void setVisualState(int visualState, int ticks) {
		this.entityData.set(DATA_VISUAL_STATE, visualState);
		this.entityData.set(DATA_VISUAL_TICKS, Math.max(0, ticks));
	}

	/** Counts the visual state down and clears it on its last tick, unless {@link #holdsVisualState} keeps it. */
	protected void tickVisualState() {
		int ticks = getVisualTicks();
		if (ticks > 0) {
			this.entityData.set(DATA_VISUAL_TICKS, ticks - 1);
			if (ticks == 1 && !holdsVisualState(getVisualState())) {
				this.entityData.set(DATA_VISUAL_STATE, VISUAL_NONE);
			}
		}
	}

	/** A state that stays after its ticks run out (Hemorath's overload). */
	protected boolean holdsVisualState(int visualState) {
		return false;
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		bossBar.show(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossBar.hide(player);
	}

	/** A ring of single particles on the floor around the Saint: the telegraph of an area attack. */
	protected void sendFloorRing(ServerLevel server, double radius, SimpleParticleType particle, int points, double y) {
		for (int i = 0; i < points; i++) {
			double angle = i * Math.PI * 2.0D / points;
			double x = this.getX() + Math.cos(angle) * radius;
			double z = this.getZ() + Math.sin(angle) * radius;
			server.sendParticles(particle, x, y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
		}
	}

	/** Sends a chat line to everyone in the fight. */
	protected void broadcastMessage(String text, ChatFormatting color) {
		bossBar.broadcast(Component.literal(text).withStyle(color));
	}
}
