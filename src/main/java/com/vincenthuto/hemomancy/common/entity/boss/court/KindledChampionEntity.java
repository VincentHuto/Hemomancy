package com.vincenthuto.hemomancy.common.entity.boss.court;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionRules.Move;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionRules.Outcome;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossIntroWindow;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.entity.npc.ProtectedNpc;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtPresences;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.court.HydraRisingHead;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentSpawner;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Kindled Champion (Brined Court Track 10, Tier 2): the title's holder, a Kindled elder in the mature Hydra form
 * who has not surrendered, called out of the elders when a worthy challenger asks the Head-tender to let them usurp it
 * ({@link KindledChampionTrial}). It fights in the vent water on the mound on Track 9's Kindled elder body: neck sweeps,
 * a stinging crown (Stung), crown grabs, and Kinbud's regrowth at a miniboss's scale, so the challenger has to burst it
 * past what it grows back. At each threshold it buds more of its crown. The chimneys vent heat through the fight.
 *
 * <p>Worn down, it yields and lives: it kneels, the title passes to the victor, Hydra's largest head rises and lowers
 * its crown over them, and it steps back among the elders as an ordinary Kindled elder. A challenger who dies has lost
 * and waits a day ({@code CourtLife.recordUsurpLoss}); the Champion keeps the title.
 */
public class KindledChampionEntity extends CourtMemberEntity {
	public static final int FIGHT = 0, YIELDING = 1, RETURNING = 2;
	private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(KindledChampionEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> MOVE = SynchedEntityData.defineId(KindledChampionEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> MOVE_TICK = SynchedEntityData.defineId(KindledChampionEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> INTRO = SynchedEntityData.defineId(KindledChampionEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> BUDS = SynchedEntityData.defineId(KindledChampionEntity.class, EntityDataSerializers.INT);
	static final ParticleColor FLAME = new ParticleColor(238, 98, 30);
	static final ParticleColor AMBER = new ParticleColor(228, 152, 44);
	static final ParticleColor VESSEL = new ParticleColor(176, 40, 44);
	private static final int FLAME_ARGB = 0xFFEE621E, CRIMSON_ARGB = 0xFFB01A1E;
	private static final int RETURN_TICKS = 40;

	private final BossIntroWindow intro = new BossIntroWindow(KindledChampionRules.INTRO);
	private final BossBarBinding bar = new BossBarBinding("entity.hemomancy.kindled_champion",
			BossPresentationIds.FRAME_KINDLED_CHAMPION, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_6);
	private final BossReplayClock moveClock = new BossReplayClock(0);
	private final BossReplayClock introClock = new BossReplayClock();
	/** Vents about to erupt: where, and the tick they go. */
	private final List<Vent> vents = new ArrayList<>();
	@Nullable
	private UUID challenger;
	@Nullable
	private BlockPos elderHome;
	private float elderYaw;
	private String elderMark = "kindled_elder";
	private int stateTicks;
	private int moveCooldown = 30;
	private int missing;
	private int ventClock;
	private Vec3 aim = Vec3.ZERO;
	@Nullable
	private Outcome outcome;

	private record Vent(BlockPos pos, int eruptAt) {
	}

	public KindledChampionEntity(EntityType<? extends KindledChampionEntity> type, Level level) {
		super(type, level, CourtRole.KINDLED_ELDER);
		this.setInvulnerable(false);
		this.goalSelector.removeAllGoals(goal -> true);
		this.targetSelector.removeAllGoals(goal -> true);
		this.xpReward = 0;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, KindledChampionRules.MAX_HEALTH)
				.add(Attributes.MOVEMENT_SPEED, 0.27D)
				.add(Attributes.ATTACK_DAMAGE, KindledChampionRules.SWEEP_DAMAGE)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
				.add(Attributes.ARMOR, 6.0D)
				.add(Attributes.FOLLOW_RANGE, KindledChampionRules.LEASH);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STATE, FIGHT);
		builder.define(MOVE, Move.NONE.ordinal());
		builder.define(MOVE_TICK, 0);
		builder.define(INTRO, -1);
		builder.define(BUDS, 0);
	}

	/**
	 * Calls the Champion out where an elder stood ({@code home}, {@code yaw}, {@code mark}, so it can step back there)
	 * to face {@code player}; its opening scene plays for the players around it.
	 */
	public static KindledChampionEntity callOut(ServerLevel level, BlockPos home, float yaw, String mark, ServerPlayer player) {
		KindledChampionEntity champion = new KindledChampionEntity(
				com.vincenthuto.hemomancy.common.init.EntityInit.kindled_champion.get(), level);
		champion.moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, yaw, 0.0F);
		champion.elderHome = home.immutable();
		champion.elderYaw = yaw;
		champion.elderMark = mark;
		champion.challenger = player.getUUID();
		champion.restrictTo(home, KindledChampionRules.ARENA_RADIUS);
		champion.intro.begin();
		champion.entityData.set(INTRO, 0);
		champion.bar.setVisible(false);
		champion.setTarget(player);
		level.addFreshEntity(champion);
		BossScenes.play(champion, BossPresentationIds.SCENE_KINDLED_CHAMPION);
		return champion;
	}

	public boolean isFacing(Player player) {
		return outcome == null && player.getUUID().equals(challenger);
	}

	@Nullable
	public UUID challengerId() {
		return challenger;
	}

	@Nullable
	public Outcome outcome() {
		return outcome;
	}

	@Nullable
	private ServerPlayer challenger() {
		if (challenger == null || !(level() instanceof ServerLevel server)) return null;
		return server.getServer().getPlayerList().getPlayer(challenger) instanceof ServerPlayer player
				&& player.level() == level() ? player : null;
	}

	public int state() {
		return entityData.get(STATE);
	}

	public Move move() {
		return Move.byId(entityData.get(MOVE));
	}

	public int moveTick() {
		return entityData.get(MOVE_TICK);
	}

	/** The crown stage (0 to 2): how many rings of buds it has grown. */
	public int budStage() {
		return entityData.get(BUDS);
	}

	public boolean inIntro() {
		return entityData.get(INTRO) >= 0;
	}

	public boolean fighting() {
		return outcome == null && !intro.active() && challenger != null;
	}

	public int pendingVents() {
		return vents.size();
	}

	public float moveTime(float partialTick) {
		return moveClock.at(this.tickCount, partialTick);
	}

	public float introTime(float partialTick) {
		int synced = entityData.get(INTRO);
		return synced < 0 ? -1.0F : introClock.sample(0, synced, this.tickCount, partialTick, 1);
	}

	// --- The fight -----------------------------------------------------------------------------------------------

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			moveClock.sync(entityData.get(MOVE) + 16 * entityData.get(STATE), entityData.get(MOVE_TICK), this.tickCount, 1);
			return;
		}
		if (state() != FIGHT) {
			entityData.set(MOVE_TICK, ++stateTicks);
			getNavigation().stop();
			if (stateTicks >= (state() == YIELDING ? KindledChampionRules.YIELD_TICKS : RETURN_TICKS)) returnToElders();
			return;
		}
		ServerPlayer player = challenger();
		if (player == null || player.distanceToSqr(this) > KindledChampionRules.LEASH * KindledChampionRules.LEASH * 4) {
			if (++missing > KindledChampionRules.TARGET_GRACE) end(Outcome.GONE, null);
			return;
		}
		missing = 0;
		if (!player.isAlive()) {
			end(Outcome.CHALLENGER_DIED, player);
			return;
		}
		setTarget(player);
		getLookControl().setLookAt(player, 30.0F, 30.0F);
		if (intro.active()) {
			boolean done = intro.tick();
			entityData.set(INTRO, intro.active() ? intro.elapsed() : -1);
			if (tickCount % 5 == 0 && level() instanceof ServerLevel server) {
				BossTelegraphs.glow(server, position().add(0, 2.6D, 0), FLAME, 3, 0.4D);
			}
			if (done) {
				bar.setVisible(true);
				bar.show(player);
			}
			return;
		}
		bar.trackHealth(this);
		if (tickCount % 20 == 0) regrow();
		stepVents(player);
		stepMove(player);
	}

	private void regrow() {
		if (getHealth() >= getMaxHealth() || !(level() instanceof ServerLevel server)) return;
		heal(KindledChampionRules.regrowthPerSecond(budStage()));
		server.sendParticles(HemoParticleData.glow(VESSEL), getX(), getY() + 1.6D, getZ(), 3, 0.3D, 0.5D, 0.3D, 0.0D);
	}

	private void stepMove(ServerPlayer player) {
		Move move = move();
		if (move == Move.NONE) {
			double distance = distanceTo(player);
			if (distance > 2.5D) getNavigation().moveTo(player, 1.0D);
			else getNavigation().stop();
			if (--moveCooldown > 0) return;
			Move next = KindledChampionRules.choose(distance, budStage(), random.nextLong());
			if (next != Move.NONE) {
				entityData.set(MOVE, next.ordinal());
				entityData.set(MOVE_TICK, 0);
				aim = player.position();
				getNavigation().stop();
			}
			moveCooldown = 8;
			return;
		}
		BossMoveTimeline timeline = move.timeline();
		int tick = entityData.get(MOVE_TICK);
		if (tick < timeline.commitTick()) {
			aim = player.position();
			getLookControl().setLookAt(player, 30.0F, 30.0F);
		}
		if (level() instanceof ServerLevel server) {
			if (timeline.stageAt(tick) == BossMoveTimeline.Stage.WINDUP && tick % 4 == 0) telegraph(server, move);
			if (tick == timeline.windup()) strike(server, move, player);
			if (move == Move.CROWN_GRAB && timeline.stageAt(tick) == BossMoveTimeline.Stage.ACTIVE) hold(server, player, tick);
		}
		if (timeline.stageAt(tick + 1) == BossMoveTimeline.Stage.COMPLETE) {
			entityData.set(MOVE, Move.NONE.ordinal());
			entityData.set(MOVE_TICK, 0);
			moveCooldown = 16;
			return;
		}
		entityData.set(MOVE_TICK, tick + 1);
	}

	private Vec3 facing() {
		Vec3 flat = aim.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
		return flat.lengthSqr() < 1.0E-4D ? getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize() : flat.normalize();
	}

	private Vec3 crownPoint() {
		return position().add(facing().scale(1.4D)).add(0.0D, 2.6D, 0.0D);
	}

	private void telegraph(ServerLevel server, Move move) {
		switch (move) {
			case NECK_SWEEP -> BossTelegraphs.cone(server, position(), facing(), KindledChampionRules.SWEEP_RADIUS,
					KindledChampionRules.SWEEP_DEGREES, FLAME);
			case CROWN_STING -> BossTelegraphs.ring(server, aim, 1.2D, FLAME, 14);
			case CROWN_GRAB -> BossTelegraphs.line(server, position(), position().add(facing().scale(KindledChampionRules.GRAB_REACH)), AMBER);
			default -> {
			}
		}
		BossTelegraphs.glow(server, crownPoint(), FLAME, 2, 0.4D);
	}

	private void strike(ServerLevel server, Move move, ServerPlayer player) {
		switch (move) {
			case NECK_SWEEP -> {
				Vec3 facing = facing();
				double half = Math.toRadians(KindledChampionRules.SWEEP_DEGREES * 0.5D);
				for (Player near : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(KindledChampionRules.SWEEP_RADIUS))) {
					Vec3 to = near.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
					if (to.length() > KindledChampionRules.SWEEP_RADIUS || !ManipulationCombatHelper.canHarmFor(this, near)) continue;
					if (to.lengthSqr() > 0.25D && Math.acos(Math.max(-1.0D, Math.min(1.0D, to.normalize().dot(facing)))) > half) continue;
					near.hurt(damageSources().mobAttack(this), KindledChampionRules.SWEEP_DAMAGE);
					near.knockback(1.2D, -to.x, -to.z);
				}
				for (int i = 0; i <= 10; i++) {
					double angle = Math.atan2(facing.z, facing.x) - half + 2.0D * half * i / 10.0D;
					Vec3 point = position().add(Math.cos(angle) * 3.5D, 1.2D, Math.sin(angle) * 3.5D);
					server.sendParticles(HemoParticleData.emberSpark(FLAME, 0.4F, 0.9F, 12), point.x, point.y, point.z, 2, 0.1D, 0.1D, 0.1D, 0.02D);
				}
			}
			case CROWN_STING -> {
				if (player.distanceTo(this) <= KindledChampionRules.STING_REACH + 1.0D && ManipulationCombatHelper.canHarmFor(this, player)) {
					player.hurt(damageSources().mobAttack(this), KindledChampionRules.STING_DAMAGE);
					player.addEffect(new MobEffectInstance(EffectInit.stung, KindledChampionRules.STING_TICKS, 0), this);
					BossTelegraphs.tendril(server, crownPoint(), player.position().add(0, 1.2D, 0), FLAME_ARGB, CRIMSON_ARGB, tickCount * 13L);
				}
				BossTelegraphs.bloodCells(server, aim.add(0, 0.6D, 0), FLAME, 10, 0.4D, 0.05D);
			}
			default -> {
			}
		}
	}

	/** The grab's hold: the crown keeps the challenger drawn in under the head, stinging as it holds. */
	private void hold(ServerLevel server, ServerPlayer player, int tick) {
		Vec3 head = crownPoint().subtract(0.0D, 1.4D, 0.0D);
		if (player.distanceTo(this) > KindledChampionRules.GRAB_REACH + 1.0D || !ManipulationCombatHelper.canHarmFor(this, player)) return;
		Vec3 pull = head.subtract(player.position());
		if (pull.length() > 0.6D) {
			player.setDeltaMovement(pull.normalize().scale(Math.min(0.45D, pull.length() * 0.2D)));
			player.hurtMarked = true;
		}
		if (tick % KindledChampionRules.GRAB_HIT_INTERVAL == 0) {
			player.hurt(damageSources().mobAttack(this), KindledChampionRules.GRAB_DAMAGE);
		}
		if (tick % 4 == 0) {
			BossTelegraphs.tendril(server, crownPoint(), player.position().add(0, 1.0D, 0), FLAME_ARGB, CRIMSON_ARGB,
					tickCount * 17L + tick);
		}
	}

	/** The chimneys: every so often three vents warn and then scald, one of them under the challenger. */
	private void stepVents(ServerPlayer player) {
		if (!(level() instanceof ServerLevel server)) return;
		if (++ventClock >= KindledChampionRules.VENT_INTERVAL) {
			ventClock = 0;
			BlockPos centre = elderHome != null ? elderHome : blockPosition();
			for (int i = 0; i < KindledChampionRules.VENTS_PER_BURST; i++) {
				BlockPos at = i == 0 ? player.blockPosition()
						: centre.offset(random.nextInt(KindledChampionRules.ARENA_RADIUS * 2) - KindledChampionRules.ARENA_RADIUS, 0,
								random.nextInt(KindledChampionRules.ARENA_RADIUS * 2) - KindledChampionRules.ARENA_RADIUS);
				vents.add(new Vent(floor(server, at), tickCount + KindledChampionRules.VENT_WARNING));
			}
		}
		vents.removeIf(vent -> {
			Vec3 base = Vec3.atBottomCenterOf(vent.pos());
			if (tickCount < vent.eruptAt()) {
				if (tickCount % 5 == 0) BossTelegraphs.ring(server, base, KindledChampionRules.VENT_RADIUS, AMBER, 16);
				return false;
			}
			erupt(server, base);
			return true;
		});
	}

	private void erupt(ServerLevel server, Vec3 base) {
		for (int i = 0; i < 6; i++) {
			server.sendParticles(HemoParticleData.emberSpark(i % 2 == 0 ? FLAME : AMBER, 0.6F, 0.9F, 18), base.x, base.y + i * 0.6D,
					base.z, 3, 0.25D, 0.2D, 0.25D, 0.04D);
		}
		BossTelegraphs.glow(server, base.add(0, 1.0D, 0), FLAME, 6, 0.4D);
		for (Player near : server.getEntitiesOfClass(Player.class, new net.minecraft.world.phys.AABB(base, base)
				.inflate(KindledChampionRules.VENT_RADIUS, 3.0D, KindledChampionRules.VENT_RADIUS))) {
			if (!ManipulationCombatHelper.canHarmFor(this, near)) continue;
			near.hurt(damageSources().hotFloor(), KindledChampionRules.VENT_DAMAGE);
			near.setDeltaMovement(near.getDeltaMovement().add(0.0D, 0.6D, 0.0D));
			near.hurtMarked = true;
		}
	}

	/** The top of the floor in the column at {@code at}, near its height. */
	private static BlockPos floor(ServerLevel level, BlockPos at) {
		for (int dy = 3; dy >= -5; dy--) {
			BlockPos pos = at.above(dy);
			if (!level.getBlockState(pos).blocksMotion() && level.getBlockState(pos.below()).blocksMotion()) return pos;
		}
		return at;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (level().isClientSide()) return false;
		if (ProtectedNpc.isKillCommand(source)) return hurtAsFighter(source, amount);
		if (!fighting() || state() != FIGHT) return false;
		if (KindledChampionRules.yieldsAt(getHealth() - amount, getMaxHealth())) {
			setHealth(getMaxHealth() * KindledChampionRules.YIELD_AT);
			end(Outcome.YIELDED, challenger());
			return true;
		}
		boolean hurt = hurtAsFighter(source, amount);
		int stage = KindledChampionRules.budStage(getHealth() / getMaxHealth());
		if (hurt && stage > budStage()) bud(stage);
		return hurt;
	}

	/** It buds another ring of its crown at a threshold. */
	private void bud(int stage) {
		entityData.set(BUDS, stage);
		if (!(level() instanceof ServerLevel server)) return;
		BossTelegraphs.bloodCells(server, crownPoint(), FLAME, 24, 0.7D, 0.08D);
		BossTelegraphs.glow(server, crownPoint(), AMBER, 10, 0.6D);
		bar.broadcast(Component.translatable("hemomancy.court.champion.buds").withStyle(ChatFormatting.GOLD));
	}

	/** The Champion answers no questions mid-fight. */
	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
	}

	/**
	 * Ends the challenge. Yielded: the title passes to the victor, Hydra's largest head crowns them and the Head-tender
	 * names them. The challenger died: the wait starts. Either way the Champion steps back among the elders.
	 */
	public void end(Outcome how, @Nullable ServerPlayer player) {
		if (outcome != null) return;
		outcome = how;
		vents.clear();
		bar.hideAll();
		entityData.set(MOVE, Move.NONE.ordinal());
		entityData.set(INTRO, -1);
		entityData.set(STATE, how == Outcome.YIELDED ? YIELDING : RETURNING);
		stateTicks = 0;
		getNavigation().stop();
		setTarget(null);
		if (!(level() instanceof ServerLevel server)) return;
		if (how == Outcome.CHALLENGER_DIED && player != null) {
			CourtLife.recordUsurpLoss(player);
		} else if (how == Outcome.YIELDED && player != null) {
			CourtLife.grantChampion(player);
			BossTelegraphs.glow(server, position().add(0, 1.5D, 0), AMBER, 16, 0.8D);
			crownVictor(server, player);
		}
	}

	private void crownVictor(ServerLevel server, ServerPlayer victor) {
		CourtMemberEntity tender = server.getEntitiesOfClass(CourtMemberEntity.class, getBoundingBox().inflate(48.0D),
				member -> member.role() == CourtRole.HEAD_TENDER && member.isAlive()).stream().findFirst().orElse(null);
		BlockPos crown = CourtPresences.tallestCrown(server, elderHome != null ? elderHome : blockPosition());
		if (crown != null) {
			HydraRisingHead head = HydraRisingHead.rise(server, Vec3.atBottomCenterOf(crown), victor);
			BossScenes.playTo(victor, head, BossPresentationIds.SCENE_HYDRA_RISE, 0);
		}
		Component speaker = tender != null ? tender.getDisplayName() : CourtRole.HEAD_TENDER.displayName();
		if (tender != null) tender.speakTo(victor);
		CourtPresences.say(victor, speaker, Component.translatable("hemomancy.court.champion.named"));
		victor.displayClientMessage(Component.translatable("hemomancy.court.champion.won").withStyle(ChatFormatting.GOLD), false);
	}

	/** It steps back among the elders: an ordinary Kindled elder on the mark it was called from. */
	private void returnToElders() {
		if (isRemoved() || !(level() instanceof ServerLevel server)) return;
		BlockPos home = elderHome != null ? elderHome : blockPosition();
		CourtResidentSpawner.place(server, CourtRole.KINDLED_ELDER, home, elderYaw, elderMark);
		discard();
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		if (outcome == null && !intro.active() && player.getUUID().equals(challenger)) bar.show(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bar.hide(player);
	}

	@Override
	public void remove(RemovalReason reason) {
		bar.hideAll();
		super.remove(reason);
	}

	@Override
	protected boolean opensVacancy() {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (challenger != null) tag.putUUID("Challenger", challenger);
		if (elderHome != null) tag.put("ElderHome", NbtUtils.writeBlockPos(elderHome));
		tag.putFloat("ElderYaw", elderYaw);
		tag.putString("ElderMark", elderMark);
		tag.putInt("Buds", budStage());
		intro.save(tag, "Intro");
		if (outcome != null) tag.putBoolean("Ended", true);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		challenger = tag.hasUUID("Challenger") ? tag.getUUID("Challenger") : null;
		elderHome = NbtUtils.readBlockPos(tag, "ElderHome").orElse(null);
		elderYaw = tag.getFloat("ElderYaw");
		if (tag.contains("ElderMark")) elderMark = tag.getString("ElderMark");
		entityData.set(BUDS, tag.getInt("Buds"));
		intro.load(tag, "Intro");
		entityData.set(INTRO, intro.active() ? intro.elapsed() : -1);
		if (elderHome != null) restrictTo(elderHome, KindledChampionRules.ARENA_RADIUS);
		// A challenge interrupted by a reload is over: the Champion goes back to the elders.
		if (tag.getBoolean("Ended")) {
			outcome = Outcome.GONE;
			entityData.set(STATE, RETURNING);
		}
	}

	/** For the review client: holds the Champion at a crown stage and in a move so each can be shot. */
	public void forcePose(int buds, Move move) {
		intro.load(new CompoundTag(), "Intro");
		entityData.set(INTRO, -1);
		entityData.set(BUDS, buds);
		entityData.set(MOVE, move.ordinal());
		entityData.set(MOVE_TICK, 0);
	}
}
