package com.vincenthuto.hemomancy.common.entity.npc.circus;

import net.minecraft.world.BossEvent;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossIntroWindow;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves.Move;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VampireBatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLock;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSides;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgressRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.mission.circus.CircusSchoolQuests;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;

/**
 * The Ringmaster: the trade speaker and conductor teacher on his perch, and the Circus finale's boss (D-18, D-33). The
 * finale runs in his Chamber of Will (2026-11-18): his dialogue begins it ({@code RingmasterFinaleDirector}), and his copy
 * on the stage there fights it ({@code RingmasterFinaleMoves}) while the real Ringmaster stands still on his perch. The
 * real one only waits, teaches and trades; after a player takes his place at the kneel he stays, retired.
 */
public final class CircusRingmasterEntity extends PathfinderMob
		implements com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSpeaker, TrueSelf {
	/** The finale phase, as {@link CircusPavilionStateRules.Phase#ordinal()}, so the model can pose for it. */
	private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(
			CircusRingmasterEntity.class, EntityDataSerializers.INT);
	/**
	 * His copy on the finale's stage in his Chamber (2026-11-18): driven by the finale, never by the real site; the
	 * finale's phase and its tick, for the client's merge and poses.
	 */
	private static final EntityDataAccessor<Boolean> STAGE_COPY = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> FINALE_PHASE = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> FINALE_TICK = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.INT);
	/** Whether his finale copy wears his true self: from the reveal on (TrueSelf). Visual only; he still fights. */
	private static final EntityDataAccessor<Boolean> TRUE_SELF = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.BOOLEAN);
	/** The professors whose echoes perform in his dark: {@code FinaleEchoes.roleMask} of those the player passed. */
	private static final EntityDataAccessor<Integer> ECHO_ROLES = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> MOVE = SynchedEntityData.defineId(
			CircusRingmasterEntity.class, EntityDataSerializers.INT);
	/** Ticks into the running move, or into the closing beat. */
	private static final EntityDataAccessor<Integer> MOVE_TICK = SynchedEntityData.defineId(
			CircusRingmasterEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> TOPPER = SynchedEntityData.defineId(
			CircusRingmasterEntity.class, EntityDataSerializers.BOOLEAN);
	/** Sleight's two doubles: where they stand, and the ticks they have left (0 when there are none). */
	private static final EntityDataAccessor<Vector3f> DOUBLE_A = SynchedEntityData.defineId(
			CircusRingmasterEntity.class, EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Vector3f> DOUBLE_B = SynchedEntityData.defineId(
			CircusRingmasterEntity.class, EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Integer> DOUBLES = SynchedEntityData.defineId(
			CircusRingmasterEntity.class, EntityDataSerializers.INT);
	/**
	 * His finale moves on the copy (2026-11-18): the running move ({@code RingmasterFinaleMoves.Move} ordinal) and its
	 * tick, two marks the client draws a move from, and which of his two fakes still stand (bit per fake).
	 */
	private static final EntityDataAccessor<Integer> FINALE_MOVE = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> FINALE_MOVE_TICK = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Vector3f> FINALE_MARK_A = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Vector3f> FINALE_MARK_B = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Integer> FAKES = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.INT);
	/**
	 * At his kneel: the first-time prompt naming the ways out (0 none, 1 both, 2 the kill only, absorbing closed to this
	 * player), and how far the owner has drawn his blood off (0 to 100).
	 */
	private static final EntityDataAccessor<Integer> KNEEL_PROMPT = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ABSORBED = SynchedEntityData.defineId(CircusRingmasterEntity.class,
			EntityDataSerializers.INT);
	/** His yield when a player takes his place at the kneel: "short and final". */
	public static final String YIELD_LINE = "hemomancy.circus.ringmaster.yield";
	/** His bar, shown by the finale director on his stage copy once the opening has played. */
	private final BossBarBinding bar = new BossBarBinding("entity.hemomancy.circus_ringmaster",
			BossPresentationIds.FRAME_CIRCUS_RINGMASTER, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
	private final BossReplayClock moveClock = new BossReplayClock(0);
	private final BossReplayClock finaleClock = new BossReplayClock(0);
	public static final String EVENT_TRIAL = "circus_begin_next_trial";
	public static final String EVENT_FINALE = "circus_begin_finale";
	private BlockPos perch;
	private BlockPos encounterOrigin;
	/** Set once an unsettled site has been released, so death, discard and unload settle it exactly once. */
	private boolean released;
	/** Saved: set when a player took his place at the kneel (2026-11-18). */
	private boolean retired;

	public CircusRingmasterEntity(EntityType<? extends CircusRingmasterEntity> type, Level level) {
		super(type, level);
		setPersistenceRequired();
		setNoGravity(true);
		noPhysics = true;
		xpReward = 0;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 80.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.30D).add(Attributes.ATTACK_DAMAGE, 7.0D)
				.add(Attributes.ARMOR, 12.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(PHASE, CircusPavilionStateRules.Phase.IDLE.ordinal());
		builder.define(STAGE_COPY, false);
		builder.define(FINALE_PHASE, 0);
		builder.define(FINALE_TICK, 0);
		builder.define(ECHO_ROLES, 0);
		builder.define(TRUE_SELF, false);
		builder.define(MOVE, Move.NONE.ordinal());
		builder.define(MOVE_TICK, 0);
		builder.define(TOPPER, true);
		builder.define(DOUBLE_A, new Vector3f());
		builder.define(DOUBLE_B, new Vector3f());
		builder.define(DOUBLES, 0);
		builder.define(FINALE_MOVE, 0);
		builder.define(FINALE_MOVE_TICK, 0);
		builder.define(FINALE_MARK_A, new Vector3f());
		builder.define(FINALE_MARK_B, new Vector3f());
		builder.define(FAKES, 3);
		builder.define(KNEEL_PROMPT, 0);
		builder.define(ABSORBED, 0);
	}

	/** The finale phase the server last synced; IDLE outside a finale. */
	/** Whether this is his copy on the finale's stage in his Chamber. */
	public boolean stageCopy() {
		return entityData.get(STAGE_COPY);
	}

	public void markStageCopy() {
		entityData.set(STAGE_COPY, true);
		setPersistenceRequired();
	}

	/** The finale's phase and its tick, set by the finale's director on his copy each tick. */
	public void syncFinale(com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase phase, int ticks) {
		entityData.set(FINALE_PHASE, phase.ordinal());
		entityData.set(FINALE_TICK, ticks);
	}

	public com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase finalePhase() {
		return com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase.values()[entityData.get(FINALE_PHASE)];
	}

	public int finaleTick() {
		return entityData.get(FINALE_TICK);
	}

	/** Ticks into the finale's phase, replayed on the client clock (the reveal and kneel poses read it). */
	public float finalePhaseTime(float partialTick) {
		return phaseClock.at(tickCount, partialTick);
	}

	private final com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock phaseClock =
			new com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock();

	@Override
	public boolean isTrueSelf() {
		return entityData.get(TRUE_SELF);
	}

	@Override
	public void setTrueSelf(boolean trueSelf) {
		entityData.set(TRUE_SELF, trueSelf);
	}

	public void setKneelPrompt(int prompt) {
		entityData.set(KNEEL_PROMPT, prompt);
	}

	/** The kneel's first-time prompt: 0 none, 1 both ways out, 2 the kill only. */
	public int kneelPrompt() {
		return entityData.get(KNEEL_PROMPT);
	}

	public void setAbsorbed(float progress) {
		entityData.set(ABSORBED, Math.round(progress));
	}

	/** How far his blood has been drawn off at the kneel, 0 to 100. */
	public int absorbed() {
		return entityData.get(ABSORBED);
	}

	/** The Ringmaster after a Succession: emptied of his Will, retired, still trading and teaching. */
	public boolean retired() {
		return retired;
	}

	/** The finale move his copy runs and its tick, set by {@code RingmasterFinaleMoves} each tick. */
	public void syncFinaleMove(int move, int tick) {
		entityData.set(FINALE_MOVE, move);
		entityData.set(FINALE_MOVE_TICK, tick);
	}

	/** The running finale move's ordinal ({@code RingmasterFinaleMoves.Move}); 0 between moves. */
	public int finaleMove() {
		return entityData.get(FINALE_MOVE);
	}

	/** Ticks into the running finale move: synced on the server, replayed on the client clock. */
	public float finaleMoveTime(float partialTick) {
		return level().isClientSide ? finaleClock.at(tickCount, partialTick) : entityData.get(FINALE_MOVE_TICK);
	}

	/** Two points a finale move is drawn from on the client (the audience's lanes: their middle and their heading). */
	public void setFinaleMarks(Vec3 a, Vec3 b) {
		entityData.set(FINALE_MARK_A, a.toVector3f());
		entityData.set(FINALE_MARK_B, b.toVector3f());
	}

	public Vec3 finaleMarkA() {
		return new Vec3(entityData.get(FINALE_MARK_A));
	}

	public Vec3 finaleMarkB() {
		return new Vec3(entityData.get(FINALE_MARK_B));
	}

	/** Plays one of his old move poses (its synced move and tick) as the finale's moves gesture: model only. */
	public void posePlay(Move pose, int tick) {
		entityData.set(MOVE, pose.ordinal());
		// Never held: the replay clock runs on, so a held value would be snapped back every few ticks. NONE runs on
		// its own counter; a pose before its start or past its end is clamped where it is read (moveTime).
		entityData.set(MOVE_TICK, pose == Move.NONE ? tickCount : tick);
	}

	/** His two fakes stand at {@code a} and {@code b} (his Sleight's doubles, reused): no shadow, no thread. */
	public void showFakes(Vec3 a, Vec3 b) {
		entityData.set(DOUBLE_A, a.toVector3f());
		entityData.set(DOUBLE_B, b.toVector3f());
		entityData.set(FAKES, 3);
		entityData.set(DOUBLES, 1);
	}

	/** Where fake {@code index} stands, or null once it has burst or there are none. */
	@Nullable
	public Vec3 fakeAt(int index) {
		if (entityData.get(DOUBLES) <= 0 || (entityData.get(FAKES) & (1 << index)) == 0) return null;
		Vector3f at = entityData.get(index == 0 ? DOUBLE_A : DOUBLE_B);
		return new Vec3(at.x(), at.y(), at.z());
	}

	/** A hit bursts a fake: it comes apart in bone spores and the thread it never had shows cut. */
	public void burstFake(int index) {
		Vec3 at = fakeAt(index);
		if (at == null || !(level() instanceof ServerLevel server)) return;
		BossTelegraphs.spores(server, at.add(0.0D, 1.0D, 0.0D), CircusEffects.BONE, 18, 0.5D, 0.05D);
		BossTelegraphs.glow(server, at.add(0.0D, 1.2D, 0.0D), CircusEffects.BONE, 10, 0.4D);
		server.playSound(null, at.x, at.y, at.z, net.minecraft.sounds.SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.HOSTILE,
				1.6F, 1.4F);
		int left = entityData.get(FAKES) & ~(1 << index);
		entityData.set(FAKES, left);
		if (left == 0) entityData.set(DOUBLES, 0);
	}

	/** His fakes go, bursting where they stand when {@code burst}. */
	public void clearFakes(boolean burst) {
		if (burst) {
			burstFake(0);
			burstFake(1);
		}
		entityData.set(DOUBLES, 0);
	}

	public void setEchoRoles(int mask) {
		entityData.set(ECHO_ROLES, mask);
	}

	public int echoRoles() {
		return entityData.get(ECHO_ROLES);
	}

	public CircusPavilionStateRules.Phase phase() {
		CircusPavilionStateRules.Phase[] phases = CircusPavilionStateRules.Phase.values();
		int index = entityData.get(PHASE);
		return index >= 0 && index < phases.length ? phases[index] : CircusPavilionStateRules.Phase.IDLE;
	}

	public Move move() {
		return Move.byId(entityData.get(MOVE));
	}

	/** Ticks into the running move (or the closing beat), replayed on the client clock. */
	public float moveTime(float partialTick) {
		float time = moveClock.at(tickCount, partialTick);
		Move move = move();
		if (move == Move.NONE) return time;
		return Math.max(0.0F, Math.min(time, move.timeline().total()));
	}

	/** Whether the topper still crowns his staff: it comes off when he yields, and the successor keeps it. */
	public boolean hasTopper() {
		return entityData.get(TOPPER);
	}

	/** Where Sleight's doubles stand, while they stand. */
	public List<Vec3> doubles() {
		if (entityData.get(DOUBLES) <= 0) return List.of();
		if (stageCopy()) {
			List<Vec3> standing = new java.util.ArrayList<>(2);
			for (int i = 0; i < 2; i++) {
				Vec3 at = fakeAt(i);
				if (at != null) standing.add(at);
			}
			return standing;
		}
		Vector3f a = entityData.get(DOUBLE_A), b = entityData.get(DOUBLE_B);
		return List.of(new Vec3(a.x(), a.y(), a.z()), new Vec3(b.x(), b.y(), b.z()));
	}

	@Override
	public AABB getBoundingBoxForCulling() {
		AABB box = super.getBoundingBoxForCulling();
		// His finale moves are drawn from him: his fakes on the far perches, the audience across the ring.
		if (stageCopy() && finaleMove() != 0) return box.inflate(32.0D);
		return box;
	}

	public BossBarBinding bar() {
		return bar;
	}

	@Override
	public void remove(RemovalReason reason) {
		bar.hideAll();
		if (stageCopy()) {
			super.remove(reason);
			return;
		}
		if (reason.shouldDestroy()) {
			releaseUnsettledSite(true);
			markVacant();
		}
		super.remove(reason);
	}

	/** Chunk unloads skip {@link #remove}; release the saved site only, as the troupe's chunks may be unloading too. */
	@Override
	public void onRemovedFromLevel() {
		if (stageCopy()) {
			super.onRemovedFromLevel();
			return;
		}
		super.onRemovedFromLevel();
		releaseUnsettledSite(false);
	}

	/**
	 * B-4: a Ringmaster lost (a death, a discard, an unload) while his site is owned with nobody else to settle it
	 * releases it once and tells the owner. A finale in his Chamber holds the site itself, and settles or releases it
	 * there; an act (PERFORMANCE) is left to its controller; a completed site has no owner.
	 */
	private void releaseUnsettledSite(boolean cleanUpEncounter) {
		if (released || encounterOrigin == null || !(level() instanceof ServerLevel server)) return;
		if (com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.holds(server.dimension(), encounterOrigin))
			return;
		CircusPavilionSavedData data = CircusPavilionSavedData.get(server);
		if (!data.hasSite(server, encounterOrigin)) return;
		CircusPavilionSavedData.Site site = data.site(server, encounterOrigin);
		UUID owner = site.activeOwner();
		CircusPavilionStateRules.Phase phase = site.phase();
		released = true;
		if (owner == null || phase == CircusPavilionStateRules.Phase.PERFORMANCE
				|| phase == CircusPavilionStateRules.Phase.IDLE || phase == CircusPavilionStateRules.Phase.COMPLETE) return;
		data.reset(server, encounterOrigin, owner);
		if (cleanUpEncounter) troupe().forEach(CircusPerformerEntity::resetFinale);
		if (server.getPlayerByUUID(owner) instanceof ServerPlayer player) {
			player.displayClientMessage(Component.translatable("hemomancy.circus.finale.ringmaster_lost")
					.withStyle(ChatFormatting.DARK_GRAY), false);
		}
	}

	/**
	 * D-36: a destroyed Ringmaster (a death or a discard, never an unload) leaves his site vacant, so the Waybill moves
	 * on. The release above and its line are unchanged.
	 */
	private void markVacant() {
		if (encounterOrigin == null || !(level() instanceof ServerLevel server)) return;
		CircusPavilionSavedData.get(server).setVacant(server, encounterOrigin, true);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundInit.ENTITY_CIRCUS_RINGMASTER_HURT.get();
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			moveClock.sync(entityData.get(MOVE) + 16 * entityData.get(PHASE), entityData.get(MOVE_TICK), tickCount, 1);
			finaleClock.sync(entityData.get(FINALE_MOVE), entityData.get(FINALE_MOVE_TICK), tickCount, 1);
			phaseClock.sync(entityData.get(FINALE_PHASE), entityData.get(FINALE_TICK), tickCount, 1);
			return;
		}
		// His copy in his Chamber is the finale's, never the real site's: its moves are the finale's.
		if (stageCopy()) {
			// A copy no finale owns (a crash's, loaded after its stage was cleared) removes itself.
			if (!com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.ownsCopy(this)) {
				discard();
				return;
			}
			// His moves decide when he walks (only down on the ring) and when he stands.
			com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleMoves.tick((ServerLevel) level(), this);
			return;
		}
		if (perch == null) perch = blockPosition();
		CircusCarouselEntity carousel = nearestCarousel();
		if (carousel != null) encounterOrigin = carousel.encounterOrigin();
		// The saved origin carries him when the carousel's chunk is not loaded yet.
		if (encounterOrigin == null) return;
		ServerLevel server = (ServerLevel) level();
		// While the finale runs in his Chamber the real Ringmaster stands still: nothing of the site moves.
		if (com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.holds(server.dimension(), encounterOrigin)) {
			getNavigation().stop();
			setTarget(null);
			return;
		}
		CircusPavilionSavedData data = CircusPavilionSavedData.get(server);
		CircusPavilionSavedData.Site site = data.site(server, encounterOrigin);
		// What a settled finale makes of him is read from the site, not handed to him: the site is usually unloaded when
		// the finale settles it (2026-11-18 fix pass).
		settleFromSite(site.outcome());
		if (!isAlive()) return;
		if (site.vacant()) data.setVacant(server, encounterOrigin, false);
		idle(site);
	}

	/** He keeps his perch: the finale is fought by his copy in his Chamber, never here. */
	private void idle(CircusPavilionSavedData.Site site) {
		entityData.set(PHASE, CircusPavilionStateRules.Phase.IDLE.ordinal());
		entityData.set(TOPPER, site.outcome() != CircusPavilionStateRules.Outcome.SUCCESSION);
		setTarget(null);
		getNavigation().stop();
		setNoGravity(true);
		noPhysics = true;
		setPos(perch.getX() + 0.5D, perch.getY(), perch.getZ() + 0.5D);
	}

	private CircusCarouselEntity nearestCarousel() {
		AABB search = encounterOrigin == null ? getBoundingBox().inflate(20.0D)
				: new AABB(encounterOrigin).inflate(20.0D);
		return level().getEntitiesOfClass(CircusCarouselEntity.class, search).stream()
				.min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
	}

	/** The finale cast, by membership of this ring (its {@link CircusArena}), not by who stands near. */
	private List<CircusPerformerEntity> troupe() {
		return encounterOrigin == null ? List.of() : CircusArena.of(encounterOrigin).troupe(level());
	}

	// --- Moves -----------------------------------------------------------------------------------------------------

	private void clearDoubles(boolean burst) {
		if (entityData.get(DOUBLES) <= 0) return;
		if (burst && level() instanceof ServerLevel server) {
			for (Vec3 decoy : doubles()) {
				BossTelegraphs.spores(server, decoy.add(0.0D, 1.0D, 0.0D), CircusEffects.BONE, 14, 0.5D, 0.04D);
				BossTelegraphs.tendril(server, decoy.add(0.0D, 1.4D, 0.0D), decoy.add(0.0D, 6.0D, 0.0D),
						CircusEffects.THREAD_CORE_ARGB, CircusEffects.THREAD_GLOW_ARGB, getId() * 43L + tickCount);
			}
		}
		entityData.set(DOUBLES, 0);
	}

	// --- The closing beat ------------------------------------------------------------------------------------------

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (stageCopy()) {
			// Kneeling, he takes no harm but the owner's own settled blow, which ends him.
			if (finalePhase() == com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase.KNEEL)
				return com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.kneelBlow(this, source);
			if (com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.catchCopy(this, amount)) return true;
			return super.hurt(source, amount);
		}
		// The real Ringmaster is never fought here: only an administrator's kill or a creative hand reaches him.
		boolean administrative = source.is(DamageTypes.GENERIC_KILL)
				|| source.getEntity() instanceof Player player && player.isCreative();
		return administrative && super.hurt(source, amount);
	}

	@Override
	public void die(DamageSource source) {
		if (stageCopy() && !source.is(DamageTypes.GENERIC_KILL)) {
			// His copy never dies on a hit: the kneel holds it (the finale's director ends it).
			setHealth(1.0F);
			return;
		}
		releaseUnsettledSite(true);
		markVacant();
		super.die(source);
	}

	/**
	 * What becomes of the real Ringmaster and his troupe once a finale in his Chamber has settled his site (2026-11-18),
	 * read from the site's saved outcome whenever he ticks, so it lands however long after the settlement he loads: a
	 * ruin frees the troupe (they leave) and he dies, once; a succession retires him on his perch. The settlement calls
	 * it at once for a Ringmaster already loaded.
	 */
	public void settleFromSite(CircusPavilionStateRules.Outcome outcome) {
		if (!(level() instanceof ServerLevel server) || !isAlive() || stageCopy()) return;
		if (outcome == CircusPavilionStateRules.Outcome.RUIN) {
			troupeLeaves(server, troupe());
			CircusEffects.collapse(server, position());
			server.playSound(null, blockPosition(), SoundInit.ENTITY_CIRCUS_RINGMASTER_FINALE_COLLAPSE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			kill();
		} else if (outcome == CircusPavilionStateRules.Outcome.SUCCESSION && !retired) {
			retired = true;
			troupe().forEach(performer -> { if (performer.isAlive()) performer.resetFinale(); });
			CircusEffects.collapse(server, position());
			server.playSound(null, blockPosition(), SoundInit.ENTITY_CIRCUS_RINGMASTER_FINALE_COLLAPSE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		}
	}

	public static void grantLiberationMemory(ServerPlayer player) {
		var result = KnownManipulationGrantHelper.grantMemory(player, ManipulationInit.thread_ripper.get(),
				ItemInit.memory_thread_ripper.get());
		if (!result.success() && result.status() != KnownManipulationGrantHelper.MemoryGrantStatus.ALREADY_KNOWN) {
			CircusPlayerProgress.oweRipperMemory(player);
			player.displayClientMessage(Component.translatable("message.hemomancy.circus_ringmaster_entity.thread_ripper_remains_in_its_memory").withStyle(ChatFormatting.DARK_RED), false);
		}
	}

	/**
	 * D-36, NQ-15-10 survivors A: after a Liberation the finale performers still standing leave the ruin in a burst of
	 * thread and spores; the faculty stay and teach.
	 */
	private void troupeLeaves(ServerLevel server, List<CircusPerformerEntity> troupe) {
		troupe.forEach(CircusPerformerEntity::leaveRing);
	}

	// --- Dialogue --------------------------------------------------------------------------------------------------

	public void handleChoice(ServerPlayer player, String eventId) {
		CircusCarouselEntity carousel = nearestCarousel();
		if (carousel == null || player.distanceToSqr(carousel) > 24.0D * 24.0D) return;
		if (EVENT_TRIAL.equals(eventId)) {
			CircusPerformanceController.beginNext(player, carousel.encounterOrigin());
		} else if (EVENT_FINALE.equals(eventId)) {
			if (CircusPlayerProgress.route(player) != CircusRouteRules.Route.NEUTRAL || refuseGate(player)) return;
			if (!CircusRouteRules.canBeginFinale(CircusPlayerProgress.challenges(player), pactGate(player))) {
				player.displayClientMessage(Component.translatable("hemomancy.circus.finale.not_ready")
						.withStyle(ChatFormatting.GRAY), false);
				return;
			}
			com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.begin(player, this, carousel);
		}
	}

	/**
	 * D-30, D-31: the pact gate for {@code route}, checked at the pact and again at the finale, so a forged event is
	 * refused as the dialogue would refuse it.
	 */
	public static CircusRouteRules.PactGate pactGate(ServerPlayer player) {
		return CircusRouteRules.pactGate(ProgressionGates.hasDegree(player, CircusRouteRules.PACT_DEGREE),
				CircusSchoolQuests.hasAnyReport(player), CircusSchoolQuests.hasUnderstudyReport(player),
				UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(player)));
	}

	public static boolean unstained(ServerPlayer player) {
		return UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(player));
	}

	private static String gateKey(CircusRouteRules.PactGate gate) {
		return "hemomancy.circus.ringmaster.gate." + gate.serializedName();
	}

	/** Tells the visitor what the pact still asks, and answers whether it refused. */
	private static boolean refuseGate(ServerPlayer player) {
		CircusRouteRules.PactGate gate = pactGate(player);
		if (gate == CircusRouteRules.PactGate.OPEN) return false;
		player.displayClientMessage(Component.translatable(gateKey(gate)).withStyle(ChatFormatting.GRAY), false);
		return true;
	}

	@Override
	public String tradeSpeakerId() {
		return NpcTradeSides.CIRCUS_RINGMASTER;
	}

	/** The Ringmaster keeps silent, and deals with no one, until the visitor can receive the pact. */
	@Override
	public boolean tradesWith(ServerPlayer player) {
		return CircusProgressRules.canReceivePact(CircusPlayerProgress.acclimation(player));
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
		if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
			if (stageCopy()) return InteractionResult.PASS;
			if (encounterOrigin != null && com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector
					.holds(level().dimension(), encounterOrigin)) {
				// His mind is elsewhere while someone fights him in his Chamber.
				player.displayClientMessage(Component.translatable("hemomancy.circus.finale.elsewhere")
						.withStyle(ChatFormatting.DARK_GRAY), false);
			} else if (!CircusProgressRules.canReceivePact(CircusPlayerProgress.acclimation(player))) {
				player.displayClientMessage(Component.translatable("hemomancy.circus.ringmaster.silent")
						.withStyle(ChatFormatting.DARK_GRAY), false);
			} else {
				PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(DialogueHubFactory.decorate(
						dialogue(serverPlayer), NpcTradeSides.CIRCUS_RINGMASTER, serverPlayer)));
			}
		}
		return InteractionResult.sidedSuccess(level().isClientSide);
	}

	/** Whether this ring's site has been settled by a finale (a ruin or a succession). */
	private boolean siteSettled(ServerPlayer player) {
		CircusCarouselEntity carousel = nearestCarousel();
		return carousel != null && CircusPavilionSavedData.get(player.serverLevel()).site(player.serverLevel(),
				carousel.encounterOrigin()).outcome() != CircusPavilionStateRules.Outcome.NEUTRAL;
	}

	/** NQ-15-10 tenancy: the successor's name, for a visitor who is not the successor of this settled pavilion. */
	private String tenancyHolder(ServerPlayer player) {
		CircusCarouselEntity carousel = nearestCarousel();
		if (carousel == null) return null;
		CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(player.serverLevel())
				.site(player.serverLevel(), carousel.encounterOrigin());
		return site.outcome() == CircusPavilionStateRules.Outcome.SUCCESSION && site.holderName() != null
				&& !player.getUUID().equals(site.completionOwner()) ? site.holderName() : null;
	}

	private DialogueTree dialogue(ServerPlayer player) {
		CircusRouteRules.Route route = CircusPlayerProgress.route(player);
		java.util.ArrayList<DialogueOption> options = new java.util.ArrayList<>();
		List<String> greeting = new java.util.ArrayList<>(List.of("hemomancy.circus.ringmaster.line." + route.serializedName()));
		String holder = tenancyHolder(player);
		if (holder != null) greeting.add(DialogueNode.translate("hemomancy.circus.ringmaster.tenancy", holder));
		if (retired) {
			// Retired: no finale on his bill, only the old man's greeting; the trade and the conductor's lessons stay.
			greeting = new java.util.ArrayList<>(List.of("hemomancy.circus.ringmaster.retired.greeting"));
			if (holder != null) greeting.add(DialogueNode.translate("hemomancy.circus.ringmaster.retired.tenancy", holder));
		} else if (route == CircusRouteRules.Route.NEUTRAL) {
			int challenges = CircusPlayerProgress.challenges(player);
			if ((challenges & 1) == 0) {
				options.add(new DialogueOption("hemomancy.circus.option.trial", null, EVENT_TRIAL).withQuest());
			} else {
				// 2026-11-18: no route is chosen here any more; the finale is offered, and the acts that earn the right to
				// take his place at the kneel stay open beside it.
				CircusRouteRules.PactGate gate = pactGate(player);
				if (gate == CircusRouteRules.PactGate.OPEN) {
					if (!unstained(player) && (challenges & CircusRouteRules.SUCCESSION_CHALLENGES)
							!= CircusRouteRules.SUCCESSION_CHALLENGES)
						options.add(new DialogueOption("hemomancy.circus.option.trial", null, EVENT_TRIAL).withQuest());
					// How to earn his hat at the kneel (canon, Lore §6.3b), beside his offer; never to the Unstained.
					if (!unstained(player)) greeting.add("hemomancy.circus.ringmaster.line.succession");
					// A settled ring offers no finale (its Ringmaster is retired or gone; this guards the tick between).
					if (!siteSettled(player))
						options.add(new DialogueOption("hemomancy.circus.option.finale", null, EVENT_FINALE).withQuest());
				} else {
					greeting = List.of(gateKey(gate));
				}
			}
		}
		options.add(new DialogueOption("hemomancy.circus.school.conductor", "school", null).withLore());
		options.add(new DialogueOption("hemomancy.dialogue.circus_performer.leave", null, null));
		return DialogueTree.builder(getType().getDescriptionId(),
				Hemomancy.rloc("textures/entity/npc/harbinger/circus/ringmaster.png"), getId())
				.theme(com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTheme.CIRCUS)
				.addNode(new DialogueNode("greeting", greeting, options))
				.addNode(new DialogueNode("school", List.of("hemomancy.circus.school.ringmaster.curriculum", "hemomancy.circus.school.ringmaster.faculty", com.vincenthuto.hemomancy.common.entity.npc.dialogue.CircusSchoolDialogue.aftermathKey("ringmaster", route)), List.of(new DialogueOption("hemomancy.circus.school.back", "greeting", null))))
				.build();
	}

	@Override public void push(double x, double y, double z) { }
	@Override public boolean isPushable() { return false; }
	@Override public boolean isPickable() { return true; }
	/** His copy in the finale takes the player's melee (its own rules decide what lands); the real one only when vulnerable. */
	/** His copy in the finale takes the player's melee (its own rules decide what lands); the real one is never fought. */
	@Override public boolean isAttackable() { return stageCopy(); }
	@Override public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (stageCopy()) tag.putBoolean("StageCopy", true);
		if (echoRoles() != 0) tag.putInt("EchoRoles", echoRoles());
		if (isTrueSelf()) tag.putBoolean("TrueSelf", true);
		if (perch != null) tag.putLong("CircusPerch", perch.asLong());
		if (encounterOrigin != null) tag.putLong("CircusOrigin", encounterOrigin.asLong());
		if (retired) tag.putBoolean("CircusRetired", true);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.getBoolean("StageCopy")) entityData.set(STAGE_COPY, true);
		entityData.set(ECHO_ROLES, tag.getInt("EchoRoles"));
		entityData.set(TRUE_SELF, tag.getBoolean("TrueSelf"));
		perch = tag.contains("CircusPerch") ? BlockPos.of(tag.getLong("CircusPerch")) : null;
		encounterOrigin = tag.contains("CircusOrigin") ? BlockPos.of(tag.getLong("CircusOrigin")) : null;
		retired = tag.getBoolean("CircusRetired");
	}
}
