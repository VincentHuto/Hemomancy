package com.vincenthuto.hemomancy.common.entity.npc.court;

import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Hydra's largest head rising from its chimney when the Head-tender calls it (Brined Court Track 10, Tier 1). A
 * scripted set piece, not a creature: no AI, no hitbox, never damaged, never a target, never saved. It rises out of
 * the chimney mouth, reaches over and lowers its polyp crown over the player it was called for, holds there, and
 * withdraws into the chimney, then is gone.
 *
 * <p>The server counts its age and syncs it; the renderer replays it through a {@link BossReplayClock}. Where the
 * crown is at any moment is {@link #pose}, a pure function of age the server and client share.
 */
public class HydraRisingHead extends Entity {
	public static final int RISE = 50;
	public static final int REACH = 40;
	public static final int HOLD = 50;
	public static final int WITHDRAW = 60;
	public static final int LENGTH = RISE + REACH + HOLD + WITHDRAW;
	/** How far above the chimney mouth the crown stands at the top of its rise. */
	public static final float RISE_HEIGHT = 11.0F;
	/** How far above the player's eyes the lowered crown hangs. */
	public static final float CROWN_CLEARANCE = 2.4F;

	private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(HydraRisingHead.class,
			EntityDataSerializers.INT);
	/** Where the crown lowers to, relative to the chimney mouth (the entity's position). */
	private static final EntityDataAccessor<Vector3f> REACH_TO = SynchedEntityData.defineId(HydraRisingHead.class,
			EntityDataSerializers.VECTOR3);

	private static final ParticleColor POLYP = new ParticleColor(236, 222, 206);
	private static final ParticleColor FLAME = new ParticleColor(240, 120, 48);

	private final BossReplayClock clock = new BossReplayClock();

	public HydraRisingHead(EntityType<? extends HydraRisingHead> type, Level level) {
		super(type, level);
		this.noPhysics = true;
		this.setNoGravity(true);
	}

	/** A head rising from the chimney mouth at {@code mouth}, called for {@code witness}. */
	public static HydraRisingHead rise(ServerLevel level, Vec3 mouth, Player witness) {
		HydraRisingHead head = new HydraRisingHead(EntityInit.hydra_rising_head.get(), level);
		head.moveTo(mouth.x, mouth.y, mouth.z, 0.0F, 0.0F);
		Vec3 over = witness.getEyePosition().add(0.0D, CROWN_CLEARANCE, 0.0D).subtract(mouth);
		head.entityData.set(REACH_TO, new Vector3f((float) over.x, (float) over.y, (float) over.z));
		level.addFreshEntity(head);
		return head;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(AGE, 0);
		builder.define(REACH_TO, new Vector3f(0.0F, RISE_HEIGHT, 0.0F));
	}

	@Override
	public void tick() {
		super.tick();
		setDeltaMovement(Vec3.ZERO);
		if (level().isClientSide) {
			crownMotes();
			return;
		}
		int age = age() + 1;
		entityData.set(AGE, age);
		if (age >= LENGTH) discard();
	}

	/** While the crown is over the player, polyp-pale and flame motes drift down from its tentacles round them. */
	private void crownMotes() {
		float t = age();
		if (t < RISE + REACH * 0.6F || t > RISE + REACH + HOLD) return;
		Vec3 crown = position().add(pose(t, reachTo()).crown());
		for (int i = 0; i < 2; i++) {
			double angle = random.nextDouble() * Math.PI * 2.0D;
			double r = 1.2D + random.nextDouble() * 0.9D;
			level().addParticle(HemoParticleData.glow(random.nextInt(3) == 0 ? FLAME : POLYP), crown.x + Math.cos(angle) * r,
					crown.y - 0.4D, crown.z + Math.sin(angle) * r, 0.0D, -0.03D - random.nextDouble() * 0.03D, 0.0D);
		}
	}

	public int age() {
		return entityData.get(AGE);
	}

	public Vec3 reachTo() {
		Vector3f to = entityData.get(REACH_TO);
		return new Vec3(to.x(), to.y(), to.z());
	}

	/** The replayed age at this frame (client). */
	public float replayedAge(float partialTick) {
		return clock.sample(0, age(), tickCount, partialTick, 1);
	}

	/**
	 * Where the crown is and how far it has turned over, at age {@code t}, relative to the chimney mouth.
	 *
	 * @param crown the crown's centre
	 * @param bend  0 while the neck stands straight up, 1 when it has arched over to the player
	 * @param turn  0 with the crown upright (tentacles up), 1 when it hangs inverted over the player
	 */
	public record Pose(Vec3 crown, float bend, float turn) {
	}

	public static Pose pose(float t, Vec3 reachTo) {
		Vec3 apex = new Vec3(0.0D, RISE_HEIGHT, 0.0D);
		if (t <= RISE) {
			float x = HemoEasing.smoothstep(HemoEasing.clamp01(t / RISE));
			return new Pose(apex.scale(x), 0.0F, 0.0F);
		}
		if (t <= RISE + REACH) {
			float x = HemoEasing.smoothstep(HemoEasing.clamp01((t - RISE) / REACH));
			return new Pose(arc(apex, reachTo, x), x, x);
		}
		if (t <= RISE + REACH + HOLD) {
			// The crown settles a little lower over the player, then lifts back to where it reached.
			float hold = (t - RISE - REACH) / HOLD;
			double settle = Math.sin(hold * Math.PI) * 0.6D;
			return new Pose(reachTo.add(0.0D, -settle, 0.0D), 1.0F, 1.0F);
		}
		float x = HemoEasing.smoothstep(HemoEasing.clamp01((t - RISE - REACH - HOLD) / WITHDRAW));
		if (x < 0.5F) {
			float back = x * 2.0F;
			return new Pose(arc(apex, reachTo, 1.0F - back), 1.0F - back, 1.0F - back);
		}
		return new Pose(apex.scale(1.0F - (x - 0.5F) * 2.0F), 0.0F, 0.0F);
	}

	/** From the apex over to the reach point along a high arc, so the crown comes down onto the player, not across. */
	private static Vec3 arc(Vec3 apex, Vec3 to, float x) {
		Vec3 control = new Vec3(to.x * 0.5D, Math.max(apex.y, to.y) + 3.0D, to.z * 0.5D);
		double a = (1 - x) * (1 - x), b = 2 * (1 - x) * x, c = x * x;
		return apex.scale(a).add(control.scale(b)).add(to.scale(c));
	}

	// --- Never a target, never hurt, never saved ---

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return true;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	public boolean skipAttackInteraction(Entity attacker) {
		return true;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return true;
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		return distance < 96.0D * 96.0D;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}
}
