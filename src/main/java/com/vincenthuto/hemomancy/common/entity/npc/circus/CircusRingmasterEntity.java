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
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves.LentAct;
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
 * finale runs on the generic boss kit: a saved {@link BossIntroWindow}, moves as {@link BossMoveTimeline}s replayed on
 * the client through {@link BossReplayClock}, {@link BossFootprint} shapes that the hit test and the telegraph share,
 * {@link BossBands} for picking, one {@link CircusArena} for the ring, and this class as the one writer of the phase.
 * His moves are {@link CircusRingmasterMoves}. The last blow starts the closing beat (CURTAIN): Succession's yield, after
 * which he goes back to his perch and keeps teaching and trading (NQ-15-11), or Liberation's collapse.
 */
public final class CircusRingmasterEntity extends PathfinderMob
		implements com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSpeaker {
	private static final EntityDataAccessor<Boolean> VULNERABLE = SynchedEntityData.defineId(
			CircusRingmasterEntity.class, EntityDataSerializers.BOOLEAN);
	/** The finale phase, as {@link CircusPavilionStateRules.Phase#ordinal()}, so the model can pose for it. */
	private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(
			CircusRingmasterEntity.class, EntityDataSerializers.INT);
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
	/** Ticks of the finale opening before the bar shows and the fight starts; the opening scene runs this long. */
	public static final int INTRO_TICKS = 60;
	/** Succession's yield: he kneels, the topper comes off, he speaks; the Pattern is granted when it ends. */
	public static final int YIELD_TICKS = 60;
	/** Liberation's collapse: the threads let go of him one by one before the ruin. */
	public static final int COLLAPSE_TICKS = 40;
	/** After the yield he stands this long before going back up to his perch. */
	public static final int RETURN_TICKS = 30;
	public static final String YIELD_LINE = "hemomancy.circus.ringmaster.yield";
	private final BossBarBinding bar = new BossBarBinding("entity.hemomancy.circus_ringmaster",
			BossPresentationIds.FRAME_CIRCUS_RINGMASTER, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
	private final BossIntroWindow intro = new BossIntroWindow(INTRO_TICKS);
	private final BossReplayClock moveClock = new BossReplayClock(0);
	public static final String EVENT_ACCEPT = "circus_accept_pact";
	public static final String EVENT_REJECT = "circus_reject_pact";
	public static final String EVENT_REPAIR = "circus_repair_route";
	public static final String EVENT_TRIAL = "circus_begin_next_trial";
	public static final String EVENT_FINALE = "circus_begin_finale";
	private BlockPos perch;
	private BlockPos encounterOrigin;
	private int phaseTicks;
	/** Set once an unsettled site has been released, so death, discard and unload settle it exactly once. */
	private boolean released;
	private Vec3 aim = Vec3.ZERO;
	/** A fresh or reloaded Ringmaster waits a second before his first move. */
	private int moveCooldown = 20;
	private int beat;
	private boolean spotlightRooted;
	/** The acts downed performers lend him in DESCENT (Strings Cut), each used once; filled as DESCENT opens. */
	private final ArrayDeque<LentAct> lent = new ArrayDeque<>();
	private boolean lentFilled;
	@Nullable
	private LentAct lentNow;
	private int returnTicks;

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
		builder.define(VULNERABLE, false);
		builder.define(PHASE, CircusPavilionStateRules.Phase.IDLE.ordinal());
		builder.define(MOVE, Move.NONE.ordinal());
		builder.define(MOVE_TICK, 0);
		builder.define(TOPPER, true);
		builder.define(DOUBLE_A, new Vector3f());
		builder.define(DOUBLE_B, new Vector3f());
		builder.define(DOUBLES, 0);
	}

	/** The finale phase the server last synced; IDLE outside a finale. */
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
		return moveClock.at(tickCount, partialTick);
	}

	/** Whether the topper still crowns his staff: it comes off when he yields, and the successor keeps it. */
	public boolean hasTopper() {
		return entityData.get(TOPPER);
	}

	/** Where Sleight's doubles stand, while they stand. */
	public List<Vec3> doubles() {
		if (entityData.get(DOUBLES) <= 0) return List.of();
		Vector3f a = entityData.get(DOUBLE_A), b = entityData.get(DOUBLE_B);
		return List.of(new Vec3(a.x(), a.y(), a.z()), new Vec3(b.x(), b.y(), b.z()));
	}

	public BossBarBinding bar() {
		return bar;
	}

	public boolean inIntro() {
		return intro.active();
	}

	/** The acts the downed troupe still lends him. */
	public List<LentAct> lentActs() {
		return List.copyOf(lent);
	}

	@Override
	public AABB getBoundingBoxForCulling() {
		AABB box = super.getBoundingBoxForCulling();
		return entityData.get(DOUBLES) > 0 ? box.inflate(CircusRingmasterMoves.SLEIGHT_RING * 2.0D + 1.0D) : box;
	}

	/** The bar reaches the owner once the opening is over, and only once. */
	private void showBar(ServerPlayer owner) {
		if (!bar.viewers().contains(owner)) bar.show(owner);
	}

	@Override
	public void remove(RemovalReason reason) {
		if (reason.shouldDestroy()) {
			releaseUnsettledSite(true);
			markVacant();
		}
		bar.hideAll();
		super.remove(reason);
	}

	/** Chunk unloads skip {@link #remove}; release the saved site only, as the troupe's chunks may be unloading too. */
	@Override
	public void onRemovedFromLevel() {
		super.onRemovedFromLevel();
		releaseUnsettledSite(false);
	}

	/**
	 * B-4: a Ringmaster lost mid-finale (a death that skipped {@link #hurt}, a discard, an unload) would leave the site
	 * owned with nobody to finish it. Release it once, as a walked-out finale is released, and tell the owner. An act
	 * (PERFORMANCE) does not need him and is left to its controller. A completed finale has no owner, so this is a no-op.
	 * An unload during the closing beat keeps the site: the beat is saved on him and settles it when he loads again.
	 */
	private void releaseUnsettledSite(boolean cleanUpEncounter) {
		if (released || encounterOrigin == null || !(level() instanceof ServerLevel server)) return;
		CircusPavilionSavedData data = CircusPavilionSavedData.get(server);
		if (!data.hasSite(server, encounterOrigin)) return;
		CircusPavilionSavedData.Site site = data.site(server, encounterOrigin);
		UUID owner = site.activeOwner();
		CircusPavilionStateRules.Phase phase = site.phase();
		if (!cleanUpEncounter && phase == CircusPavilionStateRules.Phase.CURTAIN) return;
		released = true;
		if (owner == null || phase == CircusPavilionStateRules.Phase.PERFORMANCE
				|| phase == CircusPavilionStateRules.Phase.IDLE || phase == CircusPavilionStateRules.Phase.COMPLETE) return;
		data.reset(server, encounterOrigin, owner);
		if (cleanUpEncounter) {
			troupe().forEach(CircusPerformerEntity::resetFinale);
			removeSwarm();
		}
		bar.hideAll();
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

	/** D-34: his knives and staff land through the shared targeting rule, and in a finale only on its owner's side. */
	public boolean canStrike(LivingEntity victim) {
		if (victim == this || !ManipulationCombatHelper.canHarmFor(this, victim)) return false;
		if (victim instanceof CircusPerformerEntity) return false;
		if (encounterOrigin == null || !(level() instanceof ServerLevel server)) return true;
		UUID owner = CircusPavilionSavedData.get(server).site(server, encounterOrigin).activeOwner();
		return owner == null || owner.equals(CircusPavilionStateRules.actingFor(victim));
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
			return;
		}
		if (perch == null) perch = blockPosition();
		CircusCarouselEntity carousel = nearestCarousel();
		if (carousel == null) return;
		encounterOrigin = carousel.encounterOrigin();
		ServerLevel server = (ServerLevel) level();
		CircusPavilionSavedData data = CircusPavilionSavedData.get(server);
		CircusPavilionSavedData.Site site = data.site(server, encounterOrigin);
		if (site.vacant() && isAlive()) data.setVacant(server, encounterOrigin, false);
		tickDoubles(server);
		if (site.activeOwner() == null) {
			idle(site);
			return;
		}
		if (site.phase() == CircusPavilionStateRules.Phase.CURTAIN) {
			tickCurtain(server, data, site);
			return;
		}
		Player foundOwner = server.getPlayerByUUID(site.activeOwner());
		if (!(foundOwner instanceof ServerPlayer owner) || !owner.isAlive()) return;
		entityData.set(PHASE, site.phase().ordinal());
		if (site.phase() == CircusPavilionStateRules.Phase.PERFORMANCE) {
			entityData.set(VULNERABLE, false);
			bar.hideAll();
			setTarget(null);
			return;
		}
		List<CircusPerformerEntity> troupe = troupe();
		boolean liberation = site.route() == CircusRouteRules.Route.LIBERATION;
		if (site.phase() == CircusPavilionStateRules.Phase.RAFTERS) {
			setNoGravity(true);
			noPhysics = true;
			setPos(perch.getX() + 0.5D, perch.getY(), perch.getZ() + 0.5D);
			setTarget(null);
			getLookControl().setLookAt(owner, 30.0F, 30.0F);
			phaseTicks++;
			if (phaseTicks == 1) {
				intro.begin();
				BossScenes.playTo(owner, this, BossPresentationIds.SCENE_CIRCUS_RINGMASTER, 0);
			}
			if (intro.active()) {
				// The opening holds everything: no knives, no swarm, no troupe, no bar until it ends (audit B-6).
				if (intro.tick()) {
					showBar(owner);
					spawnSwarm(owner);
					moveCooldown = 1;
				}
			} else {
				troupe.forEach(performer -> performer.beginFinale(owner, liberation));
				showBar(owner);
				bar.setName(Component.translatable("entity.hemomancy.circus_ringmaster"));
				bar.setProgress(1.0F);
				if (phaseTicks % 100 == 0 && !hasSwarm(owner)) spawnSwarm(owner);
				stepMove(server, owner, troupe, site.phase());
			}
		} else if (site.phase() == CircusPavilionStateRules.Phase.CAROUSEL) {
			troupe.forEach(performer -> performer.beginFinale(owner, liberation));
			setTarget(null);
			getNavigation().stop();
			getLookControl().setLookAt(owner, 30.0F, 30.0F);
			showBar(owner);
			showObjective(site, troupe);
			stepMove(server, owner, troupe, site.phase());
		} else if (site.phase() == CircusPavilionStateRules.Phase.DESCENT) {
			troupe.forEach(performer -> performer.beginFinale(owner, liberation));
			if (!lentFilled) {
				lentFilled = true;
				for (CircusPerformerEntity performer : troupe) {
					LentAct act = performer.isDowned() ? LentAct.of(performer) : null;
					if (act != null) lent.add(act);
				}
			}
			entityData.set(VULNERABLE, true);
			showBar(owner);
			bar.setName(Component.translatable("entity.hemomancy.circus_ringmaster"));
			bar.trackHealth(this);
			noPhysics = false;
			setNoGravity(false);
			if (distanceToSqr(carousel) > 12.0D * 12.0D) {
				teleportTo(carousel.getX(), carousel.getY() + 1.0D, carousel.getZ() + 4.0D);
			}
			setTarget(owner);
			stepMove(server, owner, troupe, site.phase());
		}
		// B-4: performers killed in a Liberation run never come back, and the troupe is the pavilion's, not the player's.
		// CAROUSEL counts only the finale performers still standing in the ring, so an emptied troupe reads as downed
		// and Succession can always reach DESCENT; the owner is told the ring was empty. The carousel only reports
		// itself destroyed: this is the one writer of the phase, so Liberation's DESCENT card plays here as well.
		boolean troupeDowned = troupe.stream().allMatch(CircusPerformerEntity::isDowned);
		CircusPavilionStateRules.Phase next = CircusFinaleRules.nextPhase(site.route(), site.phase(), phaseTicks,
				troupeDowned, carousel.isDestroyed());
		if (next != site.phase()) {
			data.setPhase(server, encounterOrigin, next);
			phaseTicks = 0;
			endMove(0);
			server.playSound(null, blockPosition(), SoundInit.ENTITY_CIRCUS_RINGMASTER_PHASE_CHANGE.get(),
					SoundSource.HOSTILE, 1.0F, 1.0F);
			if (next == CircusPavilionStateRules.Phase.CAROUSEL) {
				BossScenes.playTo(owner, this, BossPresentationIds.SCENE_CIRCUS_CAROUSEL_CARD, 0);
			} else if (next == CircusPavilionStateRules.Phase.DESCENT) {
				if (troupe.isEmpty()) owner.displayClientMessage(Component.translatable(
						"hemomancy.circus.finale.empty_troupe").withStyle(ChatFormatting.DARK_GRAY), false);
				BossScenes.playTo(owner, this, BossPresentationIds.SCENE_CIRCUS_DESCENT_CARD, 0);
			}
		}
	}

	/** No finale: he keeps his perch, after a yield first standing a moment and then vaulting back up to it. */
	private void idle(CircusPavilionSavedData.Site site) {
		entityData.set(VULNERABLE, false);
		entityData.set(PHASE, CircusPavilionStateRules.Phase.IDLE.ordinal());
		entityData.set(TOPPER, site.outcome() != CircusPavilionStateRules.Outcome.SUCCESSION);
		bar.hideAll();
		troupe().forEach(CircusPerformerEntity::resetFinale);
		setTarget(null);
		getNavigation().stop();
		phaseTicks = 0;
		endMove(0);
		clearDoubles(false);
		lent.clear();
		lentFilled = false;
		intro.load(new CompoundTag(), "Intro");
		removeSwarm();
		if (returnTicks > 0) {
			if (--returnTicks > 0) return;
			CircusEffects.vault((ServerLevel) level(), position());
			CircusEffects.vault((ServerLevel) level(), Vec3.atBottomCenterOf(perch));
		}
		setNoGravity(true);
		noPhysics = true;
		setPos(perch.getX() + 0.5D, perch.getY(), perch.getZ() + 0.5D);
	}

	/** CAROUSEL's objective on the bar: the troupe still standing (Succession) or the anchors left (Liberation). */
	private void showObjective(CircusPavilionSavedData.Site site, List<CircusPerformerEntity> troupe) {
		if (site.route() == CircusRouteRules.Route.LIBERATION) {
			int left = 3 - Integer.bitCount(site.brokenMask() & 7);
			bar.setName(Component.translatable("hemomancy.circus.finale.bar.anchors", left, 3));
			bar.setProgress(left / 3.0F);
		} else {
			int standing = (int) troupe.stream().filter(performer -> !performer.isDowned()).count();
			bar.setName(Component.translatable("hemomancy.circus.finale.bar.troupe", standing, troupe.size()));
			bar.setProgress(troupe.isEmpty() ? 0.0F : standing / (float) troupe.size());
		}
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

	private void stepMove(ServerLevel server, ServerPlayer owner, List<CircusPerformerEntity> troupe,
			CircusPavilionStateRules.Phase phase) {
		Move move = move();
		if (move == Move.NONE) {
			if (phase == CircusPavilionStateRules.Phase.DESCENT) {
				if (distanceTo(owner) > 2.5D) getNavigation().moveTo(owner, 1.05D);
				else getNavigation().stop();
			}
			if (--moveCooldown > 0) return;
			Move next = pick(owner, troupe, phase);
			if (next != Move.NONE) startMove(server, next, owner, troupe);
			else moveCooldown = 8;
			return;
		}
		BossMoveTimeline timeline = move.timeline();
		int tick = entityData.get(MOVE_TICK);
		if (tick < timeline.commitTick() && tracksAim(move)) aim = owner.position();
		if (tick < timeline.commitTick()) getLookControl().setLookAt(aim.x, aim.y + 1.0D, aim.z);
		if (timeline.stageAt(tick) == BossMoveTimeline.Stage.WINDUP) {
			telegraph(server, move, owner, tick / (float) timeline.windup(), tick);
			if (move == Move.SLEIGHT && tick == CircusRingmasterMoves.SLEIGHT_SWAP_TICK) swapWithDouble(server);
		}
		if (tick == timeline.commitTick()) commit(server, move, owner);
		if (move == Move.SPOTLIGHT && timeline.stageAt(tick) == BossMoveTimeline.Stage.ACTIVE) spotlight(server, owner, tick);
		if (timeline.stageAt(tick + 1) == BossMoveTimeline.Stage.COMPLETE) {
			beat++;
			endMove(switch (phase) {
				case RAFTERS -> Math.max(1, CircusRingmasterMoves.RAFTERS_BEAT - timeline.total());
				case CAROUSEL -> Math.max(1, CircusRingmasterMoves.CAROUSEL_BEAT - timeline.total());
				default -> CircusRingmasterMoves.DESCENT_COOLDOWN;
			});
			return;
		}
		entityData.set(MOVE_TICK, tick + 1);
	}

	private Move pick(ServerPlayer owner, List<CircusPerformerEntity> troupe, CircusPavilionStateRules.Phase phase) {
		int standing = (int) troupe.stream().filter(performer -> !performer.isDowned()).count();
		return switch (phase) {
			case RAFTERS -> CircusRingmasterMoves.rafters(beat, standing);
			case CAROUSEL -> CircusRingmasterMoves.carousel(beat, standing);
			case DESCENT -> {
				double horizontal = Math.sqrt(Math.pow(owner.getX() - getX(), 2) + Math.pow(owner.getZ() - getZ(), 2));
				yield CircusRingmasterMoves.descent(BossBands.STANDARD.band(horizontal), canReach(owner), lent.size(),
						random.nextLong());
			}
			default -> Move.NONE;
		};
	}

	/** Whether he can walk to the player; a player on a pillar or a gallery he cannot reach gets knives instead. */
	private boolean canReach(ServerPlayer owner) {
		if (distanceTo(owner) <= 3.0D) return true;
		Path path = getNavigation().createPath(owner, 1);
		return path != null && path.canReach() || owner.getY() - getY() < 2.0D && distanceTo(owner) < 6.0D;
	}

	private static boolean tracksAim(Move move) {
		return move == Move.KNIFE_THROW || move == Move.STAFF_STRIKE || move == Move.SLEIGHT || move == Move.STRINGS_CUT
				|| move == Move.SPOTLIGHT;
	}

	/** For the review client and the tests: starts {@code move} on {@code target} now. */
	public void forceMove(Move move, ServerPlayer target) {
		if (level() instanceof ServerLevel server) startMove(server, move, target, troupe());
	}

	/** For the tests: a downed performer's act waiting for Strings Cut. */
	public void lend(LentAct act) {
		lent.addFirst(act);
		lentFilled = true;
	}

	private void startMove(ServerLevel server, Move move, ServerPlayer owner, List<CircusPerformerEntity> troupe) {
		entityData.set(MOVE, move.ordinal());
		entityData.set(MOVE_TICK, 0);
		aim = owner.position();
		spotlightRooted = false;
		getNavigation().stop();
		server.playSound(null, blockPosition(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(), SoundSource.HOSTILE, 1.0F, 0.7F);
		switch (move) {
			case KNIFE_THROW -> BossTelegraphs.tendril(server, handPoint(), owner.position().add(0.0D, 1.0D, 0.0D),
					CircusEffects.THREAD_CORE_ARGB, CircusEffects.THREAD_GLOW_ARGB, getId() * 17L + tickCount);
			case CUE -> {
				for (CircusPerformerEntity performer : troupe) {
					if (performer.answerCue(move.timeline().windup())) {
						BossTelegraphs.tendril(server, staffPoint(), performer.position().add(0.0D, 1.6D, 0.0D),
								CircusEffects.THREAD_CORE_ARGB, CircusEffects.THREAD_GLOW_ARGB, performer.getId() * 29L + tickCount);
					}
				}
			}
			case SLEIGHT -> beginSleight(server, owner);
			case STRINGS_CUT -> {
				lentNow = lent.poll();
				CircusPerformerEntity lender = lender(troupe, lentNow);
				if (lender != null) CircusEffects.pullThread(server, lender.position().add(0.0D, 0.6D, 0.0D), handPoint(),
						lender.getId() * 37L + tickCount);
			}
			default -> {
			}
		}
	}

	private void endMove(int cooldown) {
		entityData.set(MOVE, Move.NONE.ordinal());
		entityData.set(MOVE_TICK, 0);
		moveCooldown = cooldown;
		lentNow = null;
	}

	@Nullable
	private static CircusPerformerEntity lender(List<CircusPerformerEntity> troupe, @Nullable LentAct act) {
		if (act == null) return null;
		return troupe.stream().filter(performer -> performer.isDowned() && LentAct.of(performer) == act).findFirst().orElse(null);
	}

	/** Where a move's footprint stands this tick; aimed moves sit on the aim's floor, his own on his. */
	private FootprintFrame frame(Move move, Vec3 origin) {
		boolean aimed = move == Move.SPOTLIGHT || move == Move.KNIFE_BOARD
				|| move == Move.STRINGS_CUT && (lentNow == LentAct.VAULT || lentNow == LentAct.STILTS);
		return FootprintFrame.simple(origin, aim, aim, aimed ? aim.y : origin.y);
	}

	@Nullable
	private BossFootprint footprint(Move move) {
		return CircusRingmasterMoves.footprint(move, lentNow);
	}

	private void telegraph(ServerLevel server, Move move, ServerPlayer owner, float charge, int tick) {
		BossFootprint shape = footprint(move);
		boolean draw = tick % 2 == 0;
		switch (move) {
			case KNIFE_THROW -> {
				CircusEffects.gather(server, handPoint(), CircusEffects.BONE, charge);
				CircusEffects.ringFootprint(server, owner.position(), 1.1D, charge);
			}
			case STAFF_STRIKE -> {
				CircusEffects.gather(server, staffPoint(), CircusEffects.CRIMSON, charge);
				if (draw) CircusEffects.footprint(server, shape, frame(move, position()), position(), 3.0D, charge);
			}
			case SPOTLIGHT -> {
				CircusEffects.spotlight(server, aim, 2.5D, charge);
				if (draw) CircusEffects.footprint(server, shape, frame(move, position()), aim, 3.0D, charge);
				CircusEffects.gather(server, staffPoint(), CircusEffects.BONE, charge);
			}
			case KNIFE_BOARD -> {
				if (draw) CircusEffects.footprint(server, shape, frame(move, position()), aim, 2.2D, charge);
				boardKnives(server, charge);
				CircusEffects.gather(server, handPoint(), CircusEffects.BONE, charge);
			}
			case SLEIGHT -> {
				if (draw) {
					CircusEffects.footprint(server, shape, frame(move, position()), position(), 3.6D, charge);
					for (Vec3 decoy : doubles()) {
						CircusEffects.footprint(server, shape, FootprintFrame.simple(decoy, aim, aim, decoy.y), decoy, 3.6D, charge);
					}
				}
			}
			case CUE -> {
				CircusEffects.gather(server, staffPoint(), CircusEffects.CRIMSON, charge);
				if (tick % 5 == 0) BossTelegraphs.ring(server, position().add(0.0D, 0.1D, 0.0D), 1.0D + charge * 2.0D,
						CircusEffects.CRIMSON, 18);
			}
			case STRINGS_CUT -> {
				if (shape != null && draw) {
					boolean aimed = lentNow == LentAct.VAULT || lentNow == LentAct.STILTS;
					CircusEffects.footprint(server, shape, frame(move, position()), aimed ? aim : position(),
							lentNow == LentAct.KNIVES ? 12.0D : lentNow == LentAct.FLAME ? 5.4D : 3.4D, charge);
				}
				CircusEffects.gather(server, handPoint(), CircusEffects.CRIMSON, charge);
			}
			default -> {
			}
		}
	}

	private void commit(ServerLevel server, Move move, ServerPlayer owner) {
		switch (move) {
			case KNIFE_THROW -> throwKnife(owner);
			case STAFF_STRIKE -> {
				swing(InteractionHand.MAIN_HAND);
				strike(server, move, position(), CircusRingmasterMoves.STAFF_DAMAGE);
			}
			case KNIFE_BOARD -> {
				for (int i = 0; i < 8; i++) {
					double angle = Math.PI * 2.0D * i / 8.0D;
					Vec3 from = aim.add(Math.cos(angle) * 1.8D, 1.0D, Math.sin(angle) * 1.8D);
					CircusKnifeProjectileEntity knife = new CircusKnifeProjectileEntity(level(), this, 0.0F);
					knife.setHarmless();
					knife.setPos(from);
					knife.shoot(aim.x - from.x, 0.0D, aim.z - from.z, 0.5F, 0.0F);
					level().addFreshEntity(knife);
				}
				strike(server, move, position(), CircusRingmasterMoves.BOARD_DAMAGE);
			}
			case SLEIGHT -> {
				swing(InteractionHand.MAIN_HAND);
				strike(server, move, position(), CircusRingmasterMoves.SLEIGHT_DAMAGE);
				for (Vec3 decoy : doubles()) CircusEffects.vault(server, decoy);
			}
			case CUE -> {
				swing(InteractionHand.MAIN_HAND);
				CircusEffects.impact(server, position().add(0.0D, 0.2D, 0.0D), 2.4D);
			}
			case STRINGS_CUT -> commitLent(server, owner);
			default -> {
			}
		}
	}

	/** Lands a move's footprint on everyone in it he may strike: the owner and the bodies they hold. */
	private void strike(ServerLevel server, Move move, Vec3 origin, float damage) {
		BossFootprint shape = footprint(move);
		if (shape == null) return;
		FootprintFrame frame = frame(move, origin);
		AABB area = new AABB(origin, aim).inflate(13.0D, 3.0D, 13.0D);
		for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, area, this::canStrike)) {
			if (!shape.hits(frame, victim.position(), victim.onGround(), 0.0D)) continue;
			if (victim.hurt(damageSources().mobAttack(this), damage)) {
				CircusEffects.impact(server, victim.position().add(0.0D, 1.0D, 0.0D), 1.0D);
				if (move == Move.STRINGS_CUT && lentNow == LentAct.FLAME) victim.setRemainingFireTicks(CircusRingmasterMoves.FLAME_TICKS);
			}
		}
	}

	/** Strings Cut's commit: he performs the act the downed performer lent him. */
	private void commitLent(ServerLevel server, ServerPlayer owner) {
		if (lentNow == null) return;
		switch (lentNow) {
			case VAULT -> {
				CircusEffects.vault(server, position());
				if (phase() == CircusPavilionStateRules.Phase.DESCENT) teleportTo(aim.x, aim.y, aim.z);
				CircusEffects.vault(server, aim);
			}
			case KNIVES -> {
				Vec3 to = aim.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
				for (int i = -1; i <= 1; i++) {
					Vec3 heading = to.yRot((float) Math.toRadians(8.0D * i));
					CircusKnifeProjectileEntity knife = new CircusKnifeProjectileEntity(level(), this, 0.0F);
					knife.setHarmless();
					knife.setPos(getX(), getEyeY() - 0.2D, getZ());
					knife.shoot(heading.x, 0.0D, heading.z, 1.4F, 0.0F);
					level().addFreshEntity(knife);
				}
			}
			case FLAME -> {
				Vec3 mouth = getEyePosition();
				CircusEffects.flameStream(server, mouth, aim.add(0.0D, 1.0D, 0.0D).subtract(mouth), 10, 0.5D, 3, 1.2D);
			}
			case STILTS -> CircusEffects.impact(server, aim.add(0.0D, 0.2D, 0.0D), 3.0D);
		}
		swing(InteractionHand.MAIN_HAND);
		strike(server, Move.STRINGS_CUT, position(), CircusRingmasterMoves.LENT_DAMAGE);
	}

	/** The Knife Board's knives stand in a ring round the locked aim, rising as the charge builds. */
	private void boardKnives(ServerLevel server, float charge) {
		for (int i = 0; i < 8; i++) {
			double angle = Math.PI * 2.0D * i / 8.0D;
			Vec3 foot = aim.add(Math.cos(angle) * 1.8D, 0.0D, Math.sin(angle) * 1.8D);
			BossTelegraphs.line(server, foot, foot.add(0.0D, 0.4D + 1.2D * charge, 0.0D), CircusEffects.BONE);
		}
	}

	/** The Spotlight's pool follows the player slowly; looking away from him in it roots them, once a move. */
	private void spotlight(ServerLevel server, ServerPlayer owner, int tick) {
		aim = aim.add(owner.position().subtract(aim).scale(0.12D));
		if (tick % 2 == 0) {
			CircusEffects.spotlight(server, aim, 2.5D, 1.0F);
			CircusEffects.footprint(server, footprint(Move.SPOTLIGHT), frame(Move.SPOTLIGHT, position()), aim, 3.0D, 1.0F);
		}
		if (spotlightRooted || !canStrike(owner)
				|| !footprint(Move.SPOTLIGHT).hits(frame(Move.SPOTLIGHT, position()), owner.position(), owner.onGround(), 0.0D))
			return;
		if (!CircusRingmasterMoves.looksAway(owner.getLookAngle(), getEyePosition().subtract(owner.getEyePosition()))) return;
		spotlightRooted = true;
		MorphlingLock.pin(owner, CircusRingmasterMoves.SPOTLIGHT_ROOT_TICKS);
		BossTelegraphs.tendril(server, staffPoint(), owner.position().add(0.0D, 1.0D, 0.0D),
				CircusEffects.THREAD_CORE_ARGB, CircusEffects.THREAD_GLOW_ARGB, getId() * 41L + tickCount);
		CircusEffects.impact(server, owner.position().add(0.0D, 0.2D, 0.0D), 1.2D);
		owner.displayClientMessage(Component.translatable("hemomancy.circus.finale.spotlight")
				.withStyle(ChatFormatting.DARK_RED), true);
	}

	/** Sleight: he vaults onto a ring round the player and two doubles stand with him; he alone is flesh. */
	private void beginSleight(ServerLevel server, ServerPlayer owner) {
		if (phase() != CircusPavilionStateRules.Phase.DESCENT) return;
		double base = random.nextDouble() * Math.PI * 2.0D;
		Vec3[] points = new Vec3[3];
		for (int i = 0; i < 3; i++) {
			double angle = base + Math.PI * 2.0D * i / 3.0D;
			points[i] = owner.position().add(Math.cos(angle) * CircusRingmasterMoves.SLEIGHT_RING, 0.0D,
					Math.sin(angle) * CircusRingmasterMoves.SLEIGHT_RING);
		}
		CircusEffects.vault(server, position());
		teleportTo(points[0].x, points[0].y, points[0].z);
		for (Vec3 point : points) CircusEffects.vault(server, point);
		entityData.set(DOUBLE_A, points[1].toVector3f());
		entityData.set(DOUBLE_B, points[2].toVector3f());
		entityData.set(DOUBLES, CircusRingmasterMoves.DOUBLES_TICKS);
	}

	/** Midway through the wind-up he trades places with a double, threads flashing at both. */
	private void swapWithDouble(ServerLevel server) {
		if (entityData.get(DOUBLES) <= 0) return;
		Vector3f a = entityData.get(DOUBLE_A);
		Vec3 was = position();
		CircusEffects.vault(server, was);
		teleportTo(a.x(), a.y(), a.z());
		CircusEffects.vault(server, position());
		entityData.set(DOUBLE_A, was.toVector3f());
	}

	private void tickDoubles(ServerLevel server) {
		int left = entityData.get(DOUBLES);
		if (left <= 0) return;
		if (left == 1) clearDoubles(true);
		else entityData.set(DOUBLES, left - 1);
	}

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

	private Vec3 handPoint() {
		Vec3 side = getLookAngle().yRot((float) Math.toRadians(70.0D)).multiply(1.0D, 0.0D, 1.0D);
		return new Vec3(getX(), getEyeY() - 0.2D, getZ()).add(side.scale(0.45D));
	}

	private Vec3 staffPoint() {
		return position().add(0.0D, 2.4D, 0.0D);
	}

	private void throwKnife(ServerPlayer owner) {
		CircusKnifeProjectileEntity knife = new CircusKnifeProjectileEntity(level(), this, 4.0F);
		knife.setPos(getX(), getEyeY() - 0.2D, getZ());
		knife.shoot(owner.getX() - getX(), owner.getEyeY() - getEyeY(), owner.getZ() - getZ(), 1.4F, 3.0F);
		level().addFreshEntity(knife);
	}

	private void spawnSwarm(ServerPlayer owner) {
		VampireBatEntity swarm = EntityInit.vampire_bat.get().create(level());
		if (swarm == null) return;
		swarm.setPos(getX(), getY() + 1.0D, getZ());
		swarm.setCustomName(Component.translatable("entity.hemomancy.manifested_vampire_bat_swarm"));
		swarm.makeManifestedAggregate(owner.getUUID());
		level().addFreshEntity(swarm);
	}

	private void removeSwarm() {
		level().getEntitiesOfClass(VampireBatEntity.class, getBoundingBox().inflate(32.0D),
				bat -> bat.getPersistentData().hasUUID("CircusSwarmOwner")).forEach(VampireBatEntity::discard);
	}

	private boolean hasSwarm(ServerPlayer owner) {
		return !level().getEntitiesOfClass(VampireBatEntity.class, getBoundingBox().inflate(32.0D),
				bat -> bat.getPersistentData().hasUUID("CircusSwarmOwner")
						&& owner.getUUID().equals(bat.getPersistentData().getUUID("CircusSwarmOwner"))).isEmpty();
	}

	// --- The closing beat ------------------------------------------------------------------------------------------

	/**
	 * The last blow opens the closing beat (chunk 15 T14). Succession: he kneels, the topper comes off and he speaks
	 * one line; Liberation: the threads let go of him one by one. Either way the site settles when the beat ends.
	 */
	private void beginCurtain(ServerPlayer owner, CircusRouteRules.Route route) {
		if (!(level() instanceof ServerLevel server)) return;
		CircusPavilionSavedData.get(server).setPhase(server, encounterOrigin, CircusPavilionStateRules.Phase.CURTAIN);
		entityData.set(PHASE, CircusPavilionStateRules.Phase.CURTAIN.ordinal());
		entityData.set(VULNERABLE, false);
		phaseTicks = 0;
		endMove(0);
		entityData.set(MOVE_TICK, 0);
		clearDoubles(true);
		getNavigation().stop();
		setTarget(null);
		bar.hideAll();
		removeSwarm();
		server.playSound(null, blockPosition(), SoundInit.ENTITY_CIRCUS_RINGMASTER_PHASE_CHANGE.get(), SoundSource.HOSTILE, 1.0F, 0.8F);
		if (route == CircusRouteRules.Route.SUCCESSION) {
			entityData.set(TOPPER, false);
			BossTelegraphs.bloodCells(server, staffPoint(), CircusEffects.CRIMSON, 24, 0.4D, 0.1D);
			BossTelegraphs.glow(server, staffPoint(), CircusEffects.BONE, 10, 0.35D);
			BossScenes.playTo(owner, this, BossPresentationIds.SCENE_CIRCUS_YIELD, 0);
		} else {
			BossScenes.playTo(owner, this, BossPresentationIds.SCENE_CIRCUS_COLLAPSE_CARD, 0);
		}
	}

	private void tickCurtain(ServerLevel server, CircusPavilionSavedData data, CircusPavilionSavedData.Site site) {
		CircusRouteRules.Route route = site.route();
		boolean succession = route == CircusRouteRules.Route.SUCCESSION;
		int length = succession ? YIELD_TICKS : COLLAPSE_TICKS;
		entityData.set(PHASE, CircusPavilionStateRules.Phase.CURTAIN.ordinal());
		entityData.set(VULNERABLE, false);
		if (succession) entityData.set(TOPPER, false);
		noPhysics = false;
		setNoGravity(false);
		getNavigation().stop();
		setTarget(null);
		if (phaseTicks < length) phaseTicks++;
		entityData.set(MOVE_TICK, phaseTicks);
		ServerPlayer owner = server.getPlayerByUUID(site.activeOwner()) instanceof ServerPlayer player && player.isAlive()
				? player : null;
		if (succession && phaseTicks == 12 && owner != null) {
			owner.displayClientMessage(Component.translatable("hemomancy.circus.ringmaster.says",
					Component.translatable(YIELD_LINE)).withStyle(ChatFormatting.GRAY), false);
		}
		if (!succession && phaseTicks % 8 == 0 && phaseTicks < length) {
			List<CircusPerformerEntity> troupe = troupe();
			Vec3 to = troupe.isEmpty() ? position().add(0.0D, 6.0D, 0.0D)
					: troupe.get(Math.floorMod(phaseTicks / 8, troupe.size())).position().add(0.0D, 1.4D, 0.0D);
			BossTelegraphs.tendril(server, position().add(0.0D, 1.6D, 0.0D), to, CircusEffects.THREAD_CORE_ARGB,
					CircusEffects.THREAD_GLOW_ARGB, getId() * 47L + phaseTicks);
			BossTelegraphs.bloodCells(server, position().add(0.0D, 1.2D, 0.0D), CircusEffects.CRIMSON, 12, 0.3D, 0.08D);
		}
		// The beat settles the site only for its owner, alive and here; a relog or a death waits on its last tick.
		if (phaseTicks < length || owner == null) return;
		complete(owner, route);
		if (!succession) kill();
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean administrative = source.is(DamageTypes.GENERIC_KILL)
				|| source.getEntity() instanceof Player player && player.isCreative();
		if (administrative) return super.hurt(source, amount);
		UUID actor = CircusPavilionStateRules.actingFor(source);
		if (actor == null || !(level() instanceof ServerLevel server) || encounterOrigin == null) return false;
		CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(server).site(server, encounterOrigin);
		if (site.activeOwner() != null && !site.activeOwner().equals(actor))
			CircusPavilionStateRules.refuseBystander(this, actor, site.activeOwner());
		if (site.phase() != CircusPavilionStateRules.Phase.DESCENT
				|| !CircusPavilionStateRules.canAct(site.activeOwner(), actor)
				|| !(server.getPlayerByUUID(actor) instanceof ServerPlayer player)) return false;
		if (amount >= getHealth()) {
			setHealth(1.0F);
			beginCurtain(player, site.route());
			return true;
		}
		// Found him among his doubles mid-Sleight: the trick breaks and the doubles come apart.
		if (move() == Move.SLEIGHT && entityData.get(MOVE_TICK) < Move.SLEIGHT.timeline().commitTick()) {
			clearDoubles(true);
			endMove(CircusRingmasterMoves.DESCENT_COOLDOWN);
		}
		return super.hurt(source, amount);
	}

	@Override
	public void die(DamageSource source) {
		if (encounterOrigin != null && level() instanceof ServerLevel server
				&& CircusPavilionSavedData.get(server).hasSite(server, encounterOrigin)) {
			CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(server).site(server, encounterOrigin);
			UUID actor = CircusPavilionStateRules.actingFor(source);
			ServerPlayer owner = CircusPavilionStateRules.canAct(site.activeOwner(), actor)
					&& server.getPlayerByUUID(actor) instanceof ServerPlayer player ? player : null;
			if (site.activeOwner() != null && !source.is(DamageTypes.GENERIC_KILL)
					&& (site.route() == CircusRouteRules.Route.SUCCESSION
					|| site.route() == CircusRouteRules.Route.LIBERATION && owner != null)) {
				// A death that skipped hurt() is refused, as a Saint refuses it; from the owner in DESCENT it opens the
				// closing beat hurt() would have opened. Only /kill gets past, and releases the site.
				setHealth(Math.max(1.0F, getHealth()));
				if (owner != null && site.phase() == CircusPavilionStateRules.Phase.DESCENT) beginCurtain(owner, site.route());
				return;
			}
		}
		releaseUnsettledSite(true);
		markVacant();
		super.die(source);
	}

	private void complete(ServerPlayer player, CircusRouteRules.Route route) {
		ServerLevel server = player.serverLevel();
		CircusPavilionStateRules.Outcome outcome = route == CircusRouteRules.Route.SUCCESSION
				? CircusPavilionStateRules.Outcome.SUCCESSION : CircusPavilionStateRules.Outcome.RUIN;
		if (!CircusPavilionSavedData.get(server).complete(server, encounterOrigin, player.getUUID(), outcome)) return;
		CircusPlayerProgress.completeRoute(player, route);
		if (route == CircusRouteRules.Route.SUCCESSION) {
			PuppeteerSummonDefinitions.byName(PuppeteerSummonDefinitions.RINGMASTER_PATTERN)
					.ifPresent(definition -> KnownSummonEvents.grantSummon(player, definition));
			CircusSiteStateService.begin(server, encounterOrigin, CircusSiteStateService.State.TENANCY,
					player.getGameProfile().getName());
			returnTicks = RETURN_TICKS;
		} else {
			grantLiberationMemory(player);
			CircusSiteStateService.begin(server, encounterOrigin, CircusSiteStateService.State.RUIN, null);
		}
		CircusPlayerProgress.oweTopper(player);
		List<CircusPerformerEntity> troupe = troupe();
		troupe.forEach(performer -> { if (performer.isAlive()) performer.resetFinale(); });
		if (route == CircusRouteRules.Route.LIBERATION) troupeLeaves(server, troupe);
		removeSwarm();
		bar.hideAll();
		// The finale's close: the threads let go of him all at once, then blood and bone burst out round him.
		for (CircusPerformerEntity performer : troupe) {
			if (performer.isRemoved()) continue;
			BossTelegraphs.tendril(server, position().add(0.0D, 1.6D, 0.0D), performer.position().add(0.0D, 1.4D, 0.0D),
					CircusEffects.THREAD_CORE_ARGB, CircusEffects.THREAD_GLOW_ARGB, performer.getId() * 13L + tickCount);
		}
		CircusEffects.collapse(server, position());
		BossTelegraphs.spores(server, position().add(0.0D, 1.0D, 0.0D), CircusEffects.CRIMSON, 40, 1.5D, 0.08D);
		server.playSound(null, blockPosition(), SoundInit.ENTITY_CIRCUS_RINGMASTER_FINALE_COLLAPSE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
		player.displayClientMessage(Component.translatable("hemomancy.circus.finale.complete." + route.serializedName())
				.withStyle(ChatFormatting.DARK_RED), false);
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
		for (CircusPerformerEntity performer : troupe) {
			if (!performer.isAlive()) continue;
			BossTelegraphs.spores(server, performer.position().add(0.0D, 1.0D, 0.0D), CircusEffects.CRIMSON, 18, 0.6D, 0.04D);
			performer.discard();
		}
	}

	// --- Dialogue --------------------------------------------------------------------------------------------------

	public void handleChoice(ServerPlayer player, String eventId) {
		CircusCarouselEntity carousel = nearestCarousel();
		if (carousel == null || player.distanceToSqr(carousel) > 24.0D * 24.0D) return;
		boolean attentionComplete = (CircusPlayerProgress.challenges(player) & 1) != 0;
		if (EVENT_ACCEPT.equals(eventId) && attentionComplete
				&& CircusProgressRules.canReceivePact(CircusPlayerProgress.acclimation(player))) {
			if (refuseGate(player, CircusRouteRules.Route.SUCCESSION)) return;
			if (CircusPlayerProgress.chooseRoute(player, CircusRouteRules.Route.SUCCESSION)) {
				CircusPlayerProgress.completeChallenge(player, 1, 50);
				player.displayClientMessage(Component.translatable("message.hemomancy.circus.route.chosen.succession")
						.withStyle(ChatFormatting.DARK_RED), false);
			}
		} else if (EVENT_REJECT.equals(eventId) && attentionComplete
				&& CircusProgressRules.canReceivePact(CircusPlayerProgress.acclimation(player))) {
			if (refuseGate(player, CircusRouteRules.Route.LIBERATION)) return;
			if (CircusPlayerProgress.chooseRoute(player, CircusRouteRules.Route.LIBERATION)) {
				CircusPlayerProgress.completeChallenge(player, 1, 50);
				player.displayClientMessage(Component.translatable("message.hemomancy.circus.route.chosen.liberation")
						.withStyle(ChatFormatting.DARK_RED), false);
			}
		} else if (EVENT_REPAIR.equals(eventId)) {
			CircusPavilionSavedData.Site site = CircusPavilionSavedData.get(player.serverLevel())
					.site(player.serverLevel(), carousel.encounterOrigin());
			if (CircusPlayerProgress.canRepairRoute(player) && refuseGate(player, repairTarget(player))) return;
			if (site.activeOwner() == null && site.outcome() == CircusPavilionStateRules.Outcome.NEUTRAL
					&& CircusPlayerProgress.repairRoute(player)) {
				player.displayClientMessage(Component.translatable("message.hemomancy.circus.route.repaired."
						+ (CircusPlayerProgress.route(player) == CircusRouteRules.Route.SUCCESSION ? "succession" : "liberation"))
						.withStyle(ChatFormatting.DARK_RED), false);
			}
		} else if (EVENT_TRIAL.equals(eventId)) {
			CircusPerformanceController.beginNext(player, carousel.encounterOrigin());
		} else if (EVENT_FINALE.equals(eventId)) {
			beginFinale(player, carousel);
		}
	}

	private void beginFinale(ServerPlayer player, CircusCarouselEntity carousel) {
		CircusRouteRules.Route route = CircusPlayerProgress.route(player);
		if (!CircusRouteRules.canBeginFinale(route, CircusPlayerProgress.acclimation(player),
				CircusPlayerProgress.challenges(player))) {
			player.displayClientMessage(Component.translatable(route == CircusRouteRules.Route.SUCCESSION
					? "hemomancy.circus.finale.not_ready" : "hemomancy.circus.finale.no_route")
					.withStyle(ChatFormatting.GRAY), false);
			return;
		}
		if (refuseGate(player, route)) return;
		CircusPavilionSavedData data = CircusPavilionSavedData.get(player.serverLevel());
		if (!data.begin(player.serverLevel(), carousel.encounterOrigin(), player.getUUID(), route)) {
			player.displayClientMessage(Component.translatable(CircusPavilionStateRules.refusalKey(
					data.site(player.serverLevel(), carousel.encounterOrigin()).outcome()))
					.withStyle(ChatFormatting.DARK_GRAY), false);
			return;
		}
		setHealth(getMaxHealth());
		phaseTicks = 0;
	}

	/**
	 * D-30, D-31: the pact gate for {@code route}, checked at the pact and again at the finale, so a forged event is
	 * refused as the dialogue would refuse it.
	 */
	public static CircusRouteRules.PactGate pactGate(ServerPlayer player, CircusRouteRules.Route route) {
		return CircusRouteRules.pactGate(route, ProgressionGates.hasDegree(player, CircusRouteRules.PACT_DEGREE),
				CircusSchoolQuests.hasAnyReport(player), CircusSchoolQuests.hasUnderstudyReport(player),
				UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(player)));
	}

	private static String gateKey(CircusRouteRules.PactGate gate) {
		return "hemomancy.circus.ringmaster.gate." + gate.serializedName();
	}

	/** Tells the visitor what the pact still asks, and answers whether it refused. */
	private static boolean refuseGate(ServerPlayer player, CircusRouteRules.Route route) {
		CircusRouteRules.PactGate gate = pactGate(player, route);
		if (gate == CircusRouteRules.PactGate.OPEN) return false;
		player.displayClientMessage(Component.translatable(gateKey(gate)).withStyle(ChatFormatting.GRAY), false);
		return true;
	}

	private static CircusRouteRules.Route repairTarget(ServerPlayer player) {
		return CircusPlayerProgress.route(player) == CircusRouteRules.Route.SUCCESSION
				? CircusRouteRules.Route.LIBERATION : CircusRouteRules.Route.SUCCESSION;
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
			if (!CircusProgressRules.canReceivePact(CircusPlayerProgress.acclimation(player))) {
				player.displayClientMessage(Component.translatable("hemomancy.circus.ringmaster.silent")
						.withStyle(ChatFormatting.DARK_GRAY), false);
			} else {
				PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(DialogueHubFactory.decorate(
						dialogue(serverPlayer), NpcTradeSides.CIRCUS_RINGMASTER, serverPlayer)));
			}
		}
		return InteractionResult.sidedSuccess(level().isClientSide);
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
		if (route == CircusRouteRules.Route.NEUTRAL) {
			if ((CircusPlayerProgress.challenges(player) & 1) == 0) {
				options.add(new DialogueOption("hemomancy.circus.option.trial", null, EVENT_TRIAL).withQuest());
			} else {
				CircusRouteRules.PactGate succession = pactGate(player, CircusRouteRules.Route.SUCCESSION);
				CircusRouteRules.PactGate liberation = pactGate(player, CircusRouteRules.Route.LIBERATION);
				if (succession == CircusRouteRules.PactGate.OPEN)
					options.add(new DialogueOption("hemomancy.circus.option.accept", "succession_warning", null).withQuest());
				if (liberation == CircusRouteRules.PactGate.OPEN)
					options.add(new DialogueOption("hemomancy.circus.option.reject", "liberation_warning", null).withQuest());
				if (succession != CircusRouteRules.PactGate.OPEN && liberation != CircusRouteRules.PactGate.OPEN)
					greeting = List.of(gateKey(liberation == CircusRouteRules.PactGate.NEEDS_UNDERSTUDY ? liberation : succession));
				else if (succession != CircusRouteRules.PactGate.OPEN)
					greeting.add(gateKey(succession));
			}
		} else if (route == CircusRouteRules.Route.SUCCESSION || route == CircusRouteRules.Route.LIBERATION) {
			if (route == CircusRouteRules.Route.SUCCESSION
					&& (CircusPlayerProgress.challenges(player) & CircusRouteRules.ALL_CHALLENGES)
					!= CircusRouteRules.ALL_CHALLENGES)
				options.add(new DialogueOption("hemomancy.circus.option.trial", null, EVENT_TRIAL).withQuest());
			options.add(new DialogueOption("hemomancy.circus.option.finale", null, EVENT_FINALE).withQuest());
			if (CircusPlayerProgress.canRepairRoute(player)
					&& pactGate(player, repairTarget(player)) == CircusRouteRules.PactGate.OPEN)
				options.add(new DialogueOption("hemomancy.circus.option.repair", null, EVENT_REPAIR).withQuest());
		}
		options.add(new DialogueOption("hemomancy.circus.school.conductor", "school", null).withLore());
		options.add(new DialogueOption("hemomancy.dialogue.circus_performer.leave", null, null));
		return DialogueTree.builder(getType().getDescriptionId(),
				Hemomancy.rloc("textures/entity/npc/harbinger/circus/ringmaster.png"), getId())
				.theme(com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTheme.CIRCUS)
				.addNode(new DialogueNode("greeting", greeting, options))
				.addNode(new DialogueNode("school", List.of("hemomancy.circus.school.ringmaster.curriculum", "hemomancy.circus.school.ringmaster.faculty", com.vincenthuto.hemomancy.common.entity.npc.dialogue.CircusSchoolDialogue.aftermathKey("ringmaster", route)), List.of(new DialogueOption("hemomancy.circus.school.back", "greeting", null))))
				.addNode(new DialogueNode("succession_warning",
						List.of("hemomancy.circus.ringmaster.warning.succession"),
						List.of(new DialogueOption("hemomancy.circus.option.confirm_succession", null, EVENT_ACCEPT),
								new DialogueOption("hemomancy.circus.option.not_yet", "greeting", null))))
				.addNode(new DialogueNode("liberation_warning",
						List.of("hemomancy.circus.ringmaster.warning.liberation"),
						List.of(new DialogueOption("hemomancy.circus.option.confirm_liberation", null, EVENT_REJECT),
								new DialogueOption("hemomancy.circus.option.not_yet", "greeting", null))))
				.build();
	}

	@Override public void push(double x, double y, double z) { }
	@Override public boolean isPushable() { return false; }
	@Override public boolean isPickable() { return true; }
	@Override public boolean isAttackable() { return entityData.get(VULNERABLE); }
	@Override public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (perch != null) tag.putLong("CircusPerch", perch.asLong());
		if (encounterOrigin != null) tag.putLong("CircusOrigin", encounterOrigin.asLong());
		tag.putInt("CircusPhaseTicks", phaseTicks);
		intro.save(tag, "CircusIntro");
		tag.putInt("CircusBeat", beat);
		tag.putBoolean("CircusLentFilled", lentFilled);
		ListTag acts = new ListTag();
		for (LentAct act : lent) acts.add(StringTag.valueOf(act.name()));
		tag.put("CircusLent", acts);
		if (returnTicks > 0) tag.putInt("CircusReturn", returnTicks);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		perch = tag.contains("CircusPerch") ? BlockPos.of(tag.getLong("CircusPerch")) : null;
		encounterOrigin = tag.contains("CircusOrigin") ? BlockPos.of(tag.getLong("CircusOrigin")) : null;
		phaseTicks = tag.getInt("CircusPhaseTicks");
		intro.load(tag, "CircusIntro");
		beat = tag.getInt("CircusBeat");
		lentFilled = tag.getBoolean("CircusLentFilled");
		lent.clear();
		for (Tag entry : tag.getList("CircusLent", Tag.TAG_STRING)) {
			LentAct act = LentAct.byName(entry.getAsString());
			if (act != null) lent.add(act);
		}
		returnTicks = tag.getInt("CircusReturn");
	}
}
