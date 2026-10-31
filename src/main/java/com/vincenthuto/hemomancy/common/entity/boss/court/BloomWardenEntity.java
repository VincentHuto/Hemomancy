package com.vincenthuto.hemomancy.common.entity.boss.court;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.WardenBloomBlock;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules.Move;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules.Outcome;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules.Phase;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossIntroWindow;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.entity.npc.ProtectedNpc;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtPresences;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.MarimoHeartBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Bloom Warden (Brined Court Track 10, Tier 2): an Arbor elder of the undercroft's middle tiers, more moss than
 * person, that wakes when a player wrongs the Giving Light ({@code CourtTransgressions}). It is a Court member on the
 * jointed rig with Sunburst at Primal, and it fights through the room: rooted, it sows blooms over the tier floor that
 * shield it and heal it while shafts of light reach them; every so often it uproots and walks, and then it can be hurt
 * in earnest. A player holding a Giving Light offering is never attacked, and handing it over makes amends.
 *
 * <p>It never dies to the player. Worn down, it re-anchors: Marimo's moss pulses through the walls and the chorus names
 * the wrong. The transgression stays recorded either way, until amends. A player who dies to it has lost (T4). Its
 * blooms are cleared when the fight ends, and wither by themselves with no Warden near.
 */
public class BloomWardenEntity extends CourtMemberEntity {
	private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(BloomWardenEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> MOVE = SynchedEntityData.defineId(BloomWardenEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> MOVE_TICK = SynchedEntityData.defineId(BloomWardenEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> INTRO = SynchedEntityData.defineId(BloomWardenEntity.class, EntityDataSerializers.INT);
	static final ParticleColor GOLD = new ParticleColor(252, 196, 72);
	static final ParticleColor FROND = new ParticleColor(150, 190, 70);
	private static final int GOLD_ARGB = 0xFFFCC448, MOSS_ARGB = 0xFF6E9A3C;

	private final BossIntroWindow intro = new BossIntroWindow(BloomWardenRules.INTRO);
	private final BossBarBinding bar = new BossBarBinding("entity.hemomancy.bloom_warden",
			BossPresentationIds.FRAME_BLOOM_WARDEN, BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
	private final BossReplayClock moveClock = new BossReplayClock(0);
	private final BossReplayClock introClock = new BossReplayClock();
	/** Each bloom it sowed, with the seconds it has gone without light. */
	private final Map<BlockPos, Integer> blooms = new LinkedHashMap<>();
	@Nullable
	private UUID target;
	private String wrong = BloomWardenRules.GROWTH;
	private int phaseTicks;
	private int moveCooldown = 20;
	private int missing;
	private int fedBlooms;
	private Vec3 aim = Vec3.ZERO;
	@Nullable
	private Outcome outcome;
	// Client-side easing between the rooted kneel and the uprooted stance.
	private float uprootO, uproot;

	public BloomWardenEntity(EntityType<? extends BloomWardenEntity> type, Level level) {
		super(type, level, CourtRole.ARBOR);
		this.setInvulnerable(false);
		this.goalSelector.removeAllGoals(goal -> true);
		this.targetSelector.removeAllGoals(goal -> true);
		this.xpReward = 0;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, BloomWardenRules.MAX_HEALTH)
				.add(Attributes.MOVEMENT_SPEED, 0.24D)
				.add(Attributes.ATTACK_DAMAGE, BloomWardenRules.ROOT_LASH_DAMAGE)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
				.add(Attributes.FOLLOW_RANGE, BloomWardenRules.LEASH);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(PHASE, Phase.ROOTED.ordinal());
		builder.define(MOVE, Move.NONE.ordinal());
		builder.define(MOVE_TICK, 0);
		builder.define(INTRO, -1);
	}

	// --- Waking --------------------------------------------------------------------------------------------------

	/**
	 * Wakes the Warden for {@code player}'s wrong {@code act} near {@code where}: the one already hunting them takes the
	 * new wrong as its own, otherwise one uproots beside the nearest Arbor (or in front of the player) and its opening
	 * scene plays.
	 */
	public static BloomWardenEntity wake(ServerPlayer player, String act, BlockPos where) {
		ServerLevel level = player.serverLevel();
		List<BloomWardenEntity> awake = level.getEntitiesOfClass(BloomWardenEntity.class,
				player.getBoundingBox().inflate(BloomWardenRules.LEASH * 2), warden -> warden.isHunting(player));
		if (!awake.isEmpty()) {
			awake.get(0).wrong = act;
			return awake.get(0);
		}
		BloomWardenEntity warden = new BloomWardenEntity(EntityInit.bloom_warden.get(), level);
		Vec3 at = spawnPoint(level, player, where);
		warden.moveTo(at.x, at.y, at.z, yawToward(at, player.position()), 0.0F);
		warden.begin(player, act);
		level.addFreshEntity(warden);
		BossScenes.playTo(player, warden, BossPresentationIds.SCENE_BLOOM_WARDEN, 0);
		return warden;
	}

	private static Vec3 spawnPoint(ServerLevel level, ServerPlayer player, BlockPos where) {
		List<CourtMemberEntity> arbors = level.getEntitiesOfClass(CourtMemberEntity.class, new AABB(where).inflate(32.0D),
				member -> member.role() == CourtRole.ARBOR && !(member instanceof BloomWardenEntity));
		CourtMemberEntity nearest = arbors.stream().min((a, b) -> Double.compare(a.distanceToSqr(player), b.distanceToSqr(player)))
				.orElse(null);
		Vec3 from = nearest != null ? nearest.position() : player.position();
		Vec3 toward = player.position().subtract(from).multiply(1.0D, 0.0D, 1.0D);
		Vec3 offset = nearest != null ? (toward.lengthSqr() > 1.0E-4D ? toward.normalize().scale(1.5D) : new Vec3(1.5D, 0, 0))
				: player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize().scale(4.0D);
		BlockPos base = BlockPos.containing(from.add(offset));
		for (int dy = 3; dy >= -4; dy--) {
			BlockPos pos = base.above(dy);
			if (level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above()) && level.getBlockState(pos.below()).isSolid()) {
				return Vec3.atBottomCenterOf(pos);
			}
		}
		return player.position();
	}

	private static float yawToward(Vec3 from, Vec3 to) {
		return (float) Math.toDegrees(Math.atan2(-(to.x - from.x), to.z - from.z));
	}

	/** Starts the fight against {@code player} for {@code act}: the opening, then the bar. */
	public void begin(ServerPlayer player, String act) {
		this.target = player.getUUID();
		this.wrong = act;
		this.intro.begin();
		this.entityData.set(INTRO, 0);
		this.bar.setVisible(false);
		this.setTarget(player);
	}

	public boolean isHunting(Player player) {
		return outcome == null && player.getUUID().equals(target);
	}

	@Nullable
	public UUID huntedId() {
		return target;
	}

	/** The wrong the Warden is answering, which the chorus names if the player wins. */
	public String wrong() {
		return wrong;
	}

	@Nullable
	public Outcome outcome() {
		return outcome;
	}

	@Nullable
	private ServerPlayer hunted() {
		if (target == null || !(level() instanceof ServerLevel server)) return null;
		return server.getServer().getPlayerList().getPlayer(target) instanceof ServerPlayer player
				&& player.level() == level() ? player : null;
	}

	// --- State for the client and the tests ----------------------------------------------------------------------

	public Phase phase() {
		return Phase.byId(entityData.get(PHASE));
	}

	public Move move() {
		return Move.byId(entityData.get(MOVE));
	}

	public int moveTick() {
		return entityData.get(MOVE_TICK);
	}

	public boolean inIntro() {
		return entityData.get(INTRO) >= 0;
	}

	/** Whether the Warden can be hurt now: in its fight, past the opening, not yet sinking back. */
	public boolean fighting() {
		return outcome == null && !intro.active() && target != null;
	}

	public int bloomCount() {
		return blooms.size();
	}

	public List<BlockPos> blooms() {
		return List.copyOf(blooms.keySet());
	}

	public int fedBlooms() {
		return fedBlooms;
	}

	/** The replayed tick into the current move (or into the re-anchoring), for the model. */
	public float moveTime(float partialTick) {
		return moveClock.at(this.tickCount, partialTick);
	}

	/** Eased 0 (rooted, kneeling) to 1 (uprooted, standing), for the model. */
	public float uprootAmount(float partialTick) {
		return net.minecraft.util.Mth.lerp(partialTick, uprootO, uproot);
	}

	/** Ticks into the opening scene's uprooting, replayed; negative once it is over. */
	public float introTime(float partialTick) {
		int synced = entityData.get(INTRO);
		return synced < 0 ? -1.0F : introClock.sample(0, synced, this.tickCount, partialTick, 1);
	}

	// --- The fight -----------------------------------------------------------------------------------------------

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			moveClock.sync(clockKey(), entityData.get(MOVE_TICK), this.tickCount, 1);
			uprootO = uproot;
			uproot = net.minecraft.util.Mth.approach(uproot, phase() == Phase.UPROOTED ? 1.0F : 0.0F, 0.08F);
			return;
		}
		if (phase() == Phase.REANCHORING) {
			entityData.set(MOVE_TICK, ++phaseTicks);
			getNavigation().stop();
			if (phaseTicks >= BloomWardenRules.REANCHOR_TICKS) discard();
			return;
		}
		ServerPlayer player = hunted();
		if (player == null || player.distanceToSqr(this) > BloomWardenRules.LEASH * BloomWardenRules.LEASH * 4) {
			if (++missing > BloomWardenRules.TARGET_GRACE) end(Outcome.GONE);
			return;
		}
		missing = 0;
		if (!player.isAlive()) {
			end(Outcome.PLAYER_DIED);
			return;
		}
		setTarget(player);
		getLookControl().setLookAt(player);
		if (intro.active()) {
			boolean done = intro.tick();
			entityData.set(INTRO, intro.active() ? intro.elapsed() : -1);
			if (tickCount % 4 == 0 && level() instanceof ServerLevel server) {
				BossTelegraphs.spores(server, position().add(0, 0.2D, 0), FROND, 6, 0.6D, 0.02D);
			}
			if (done) {
				bar.setVisible(true);
				bar.show(player);
			}
			return;
		}
		bar.trackHealth(this);
		if (tickCount % 20 == 0) tendBlooms();
		stepPhase(player);
		stepMove(player);
	}

	private int clockKey() {
		return entityData.get(MOVE) + 16 * entityData.get(PHASE);
	}

	private void setPhase(Phase phase) {
		entityData.set(PHASE, phase.ordinal());
		phaseTicks = 0;
	}

	private void stepPhase(ServerPlayer player) {
		phaseTicks++;
		Phase phase = phase();
		if (phase == Phase.ROOTED) {
			getNavigation().stop();
			if (phaseTicks >= BloomWardenRules.ROOTED_TICKS && move() == Move.NONE) {
				setPhase(Phase.UPROOTED);
				if (level() instanceof ServerLevel server) {
					BossTelegraphs.bloodCells(server, position().add(0, 0.3D, 0), FROND, 14, 0.5D, 0.06D);
				}
			}
		} else if (phase == Phase.UPROOTED) {
			if (move() == Move.NONE && distanceToSqr(player) > 4.0D) getNavigation().moveTo(player, 1.0D);
			if (phaseTicks >= BloomWardenRules.UPROOTED_TICKS && move() == Move.NONE) {
				getNavigation().stop();
				setPhase(Phase.ROOTED);
				startMove(Move.SOW, player);
			}
		}
	}

	private void startMove(Move move, ServerPlayer player) {
		entityData.set(MOVE, move.ordinal());
		entityData.set(MOVE_TICK, 0);
		aim = player.position();
	}

	private void stepMove(ServerPlayer player) {
		Move move = move();
		boolean offering = CourtLife.holdsOffering(player);
		if (move == Move.NONE) {
			if (--moveCooldown > 0) return;
			Move next = BloomWardenRules.choose(phase(), offering, distanceTo(player), blooms.size(), random.nextLong());
			if (next != Move.NONE) startMove(next, player);
			moveCooldown = 10;
			return;
		}
		BossMoveTimeline timeline = move.timeline();
		int tick = entityData.get(MOVE_TICK);
		// An offering raised stops the attack in its wind-up: the Warden lowers and waits for amends.
		if (offering && move != Move.SOW && timeline.stageAt(tick) == BossMoveTimeline.Stage.WINDUP) {
			endMove();
			return;
		}
		if (tick < timeline.commitTick()) aim = player.position();
		if (level() instanceof ServerLevel server) telegraph(server, move, tick, timeline);
		if (tick == timeline.windup() && level() instanceof ServerLevel server) strike(server, move, player);
		if (timeline.stageAt(tick + 1) == BossMoveTimeline.Stage.COMPLETE) {
			endMove();
			return;
		}
		entityData.set(MOVE_TICK, tick + 1);
	}

	private void endMove() {
		entityData.set(MOVE, Move.NONE.ordinal());
		entityData.set(MOVE_TICK, 0);
		moveCooldown = 20;
	}

	private void telegraph(ServerLevel server, Move move, int tick, BossMoveTimeline timeline) {
		if (timeline.stageAt(tick) != BossMoveTimeline.Stage.WINDUP || tick % 4 != 0) return;
		switch (move) {
			case ROOT_LASH -> BossTelegraphs.ring(server, position(), BloomWardenRules.ROOT_LASH_RADIUS, GOLD, 28);
			case FROND_SPEAR -> BossTelegraphs.line(server, position(), spearEnd(), GOLD);
			case SOW -> BossTelegraphs.glow(server, position().add(0, 2.2D, 0), GOLD, 4, 0.5D);
			default -> {
			}
		}
	}

	private Vec3 spearEnd() {
		Vec3 flat = aim.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
		if (flat.lengthSqr() < 1.0E-4D) flat = getLookAngle().multiply(1.0D, 0.0D, 1.0D);
		return position().add(flat.normalize().scale(BloomWardenRules.FROND_SPEAR_RANGE));
	}

	private void strike(ServerLevel server, Move move, ServerPlayer player) {
		switch (move) {
			case ROOT_LASH -> {
				for (Player near : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(BloomWardenRules.ROOT_LASH_RADIUS),
						p -> p.distanceToSqr(this) <= BloomWardenRules.ROOT_LASH_RADIUS * BloomWardenRules.ROOT_LASH_RADIUS)) {
					if (!ManipulationCombatHelper.canHarmFor(this, near)) continue;
					near.hurt(damageSources().mobAttack(this), BloomWardenRules.ROOT_LASH_DAMAGE);
					BossTelegraphs.tendril(server, position().add(0, 0.1D, 0), near.position().add(0, 0.9D, 0), MOSS_ARGB, GOLD_ARGB,
							near.getId() * 31L + tickCount);
				}
				BossTelegraphs.ring(server, position(), BloomWardenRules.ROOT_LASH_RADIUS, FROND, 36);
				BossTelegraphs.bloodCells(server, position().add(0, 0.2D, 0), FROND, 18, BloomWardenRules.ROOT_LASH_RADIUS * 0.4D, 0.08D);
			}
			case FROND_SPEAR -> {
				Vec3 start = position().add(0, 1.0D, 0);
				Vec3 end = spearEnd().add(0, 1.0D, 0);
				BossTelegraphs.tendril(server, position().add(0, 2.0D, 0), end, GOLD_ARGB, 0xFFFFF0B0, tickCount * 7L);
				for (Player near : server.getEntitiesOfClass(Player.class, new AABB(start, end).inflate(BloomWardenRules.FROND_SPEAR_WIDTH))) {
					if (!ManipulationCombatHelper.canHarmFor(this, near)) continue;
					Vec3 point = closestOnSegment(start, end, near.position().add(0, 0.9D, 0));
					if (point.distanceTo(near.position().add(0, 0.9D, 0)) > BloomWardenRules.FROND_SPEAR_WIDTH) continue;
					near.hurt(damageSources().mobAttack(this), BloomWardenRules.FROND_SPEAR_DAMAGE);
				}
				for (int i = 1; i <= 8; i++) {
					BossTelegraphs.glow(server, start.lerp(end, i / 8.0D), GOLD, 2, 0.15D);
				}
			}
			case SOW -> sow(server);
			default -> {
			}
		}
	}

	private static Vec3 closestOnSegment(Vec3 a, Vec3 b, Vec3 p) {
		Vec3 ab = b.subtract(a);
		double t = ab.lengthSqr() < 1.0E-6D ? 0.0D : Math.max(0.0D, Math.min(1.0D, p.subtract(a).dot(ab) / ab.lengthSqr()));
		return a.add(ab.scale(t));
	}

	/** Throws up to {@link BloomWardenRules#SOW_COUNT} blooms over the floor round where it roots. */
	private void sow(ServerLevel server) {
		int sown = 0;
		BlockPos root = blockPosition();
		for (int attempt = 0; attempt < 24 && sown < BloomWardenRules.SOW_COUNT && blooms.size() < BloomWardenRules.MAX_BLOOMS; attempt++) {
			int dx = random.nextInt(BloomWardenRules.BLOOM_RADIUS * 2 + 1) - BloomWardenRules.BLOOM_RADIUS;
			int dz = random.nextInt(BloomWardenRules.BLOOM_RADIUS * 2 + 1) - BloomWardenRules.BLOOM_RADIUS;
			if (dx * dx + dz * dz < 4) continue;
			for (int dy = 2; dy >= -3; dy--) {
				BlockPos pos = root.offset(dx, dy, dz);
				if (!sowAt(pos)) continue;
				sown++;
				break;
			}
		}
	}

	/** Sows one bloom at {@code pos} if it can grow there and the Warden has room for another; true when it did. */
	public boolean sowAt(BlockPos pos) {
		if (!(level() instanceof ServerLevel server) || blooms.size() >= BloomWardenRules.MAX_BLOOMS
				|| !WardenBloomBlock.canSow(server, pos)) {
			return false;
		}
		server.setBlock(pos, BlockInit.warden_bloom.get().defaultBlockState().setValue(WardenBloomBlock.TURN, random.nextInt(4)), 3);
		blooms.put(pos.immutable(), 0);
		BossTelegraphs.tendril(server, position().add(0, 0.1D, 0), Vec3.atBottomCenterOf(pos), MOSS_ARGB, GOLD_ARGB, pos.asLong());
		BossTelegraphs.glow(server, Vec3.atBottomCenterOf(pos).add(0, 0.2D, 0), GOLD, 5, 0.3D);
		return true;
	}

	/** Once a second: lit blooms feed it, dark ones wither, and cleared ones are forgotten. */
	private void tendBlooms() {
		if (!(level() instanceof ServerLevel server)) return;
		int fed = 0;
		List<BlockPos> gone = new ArrayList<>();
		for (Map.Entry<BlockPos, Integer> entry : blooms.entrySet()) {
			BlockPos pos = entry.getKey();
			if (!server.getBlockState(pos).is(BlockInit.warden_bloom.get())) {
				gone.add(pos);
				continue;
			}
			if (WardenBloomBlock.fed(server, pos)) {
				fed++;
				entry.setValue(0);
				BossTelegraphs.glow(server, Vec3.atBottomCenterOf(pos).add(0, 0.4D, 0), GOLD, 1, 0.2D);
			} else if (entry.getValue() + 1 >= BloomWardenRules.WITHER_SECONDS) {
				server.removeBlock(pos, false);
				gone.add(pos);
			} else {
				entry.setValue(entry.getValue() + 1);
			}
		}
		gone.forEach(blooms::remove);
		fedBlooms = fed;
		float heal = BloomWardenRules.healPerSecond(fed);
		if (heal > 0.0F && getHealth() < getMaxHealth()) {
			heal(heal);
			blooms.keySet().stream().skip(random.nextInt(Math.max(1, blooms.size()))).findFirst()
					.ifPresent(pos -> BossTelegraphs.tendril(server, Vec3.atBottomCenterOf(pos), position().add(0, 1.0D, 0),
							GOLD_ARGB, 0xFFFFF0B0, pos.asLong() ^ tickCount));
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (level().isClientSide()) return false;
		if (ProtectedNpc.isKillCommand(source)) return hurtAsFighter(source, amount);
		if (!fighting() || phase() == Phase.REANCHORING) return false;
		float dealt = BloomWardenRules.damageTaken(amount, phase(), fedBlooms);
		if (BloomWardenRules.reanchorsAt(getHealth(), dealt)) {
			setHealth(1.0F);
			end(Outcome.REANCHORED);
			return true;
		}
		return hurtAsFighter(source, dealt);
	}

	/** Amends: a transgressor hands over a Giving Light offering, and the fight is over. */
	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand != InteractionHand.MAIN_HAND || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.sidedSuccess(player.level().isClientSide());
		}
		if (outcome == null && CourtLife.reconcile(serverPlayer)) {
			if (isHunting(serverPlayer)) end(Outcome.RECONCILED);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	/**
	 * Ends the fight: its blooms come up, the bar goes, and it sinks back into the tier. Worn down, Marimo pulses
	 * through the walls and the chorus names the wrong; reconciled, the chorus takes the amends.
	 */
	public void end(Outcome how) {
		if (outcome != null) return;
		outcome = how;
		clearBlooms();
		bar.hideAll();
		endMove();
		setPhase(Phase.REANCHORING);
		entityData.set(INTRO, -1);
		getNavigation().stop();
		setTarget(null);
		ServerPlayer player = hunted();
		if (!(level() instanceof ServerLevel server)) return;
		BossTelegraphs.spores(server, position().add(0, 0.5D, 0), FROND, 20, 0.8D, 0.03D);
		Component chorus = Component.translatable("hemomancy.court.presence.marimo.chorus");
		if (how == Outcome.REANCHORED) {
			pulseMarimo(server, blockPosition());
			if (player != null) {
				CourtPresences.say(player, chorus, Component.translatable(BloomWardenRules.namingKey(wrong)));
				player.displayClientMessage(Component.translatable("hemomancy.court.warden.turned_away")
						.withStyle(ChatFormatting.GRAY), false);
			}
		} else if (how == Outcome.RECONCILED && player != null) {
			pulseMarimo(server, blockPosition());
			CourtPresences.say(player, chorus, Component.translatable("hemomancy.court.warden.amends"));
		}
	}

	public boolean ending() {
		return outcome != null;
	}

	private void clearBlooms() {
		for (BlockPos pos : blooms.keySet()) {
			if (level().getBlockState(pos).is(BlockInit.warden_bloom.get())) level().removeBlock(pos, false);
		}
		blooms.clear();
		fedBlooms = 0;
	}

	/** One pulse through every Marimo's Heart within three chunks: the moss answering through the walls. */
	public static int pulseMarimo(ServerLevel level, BlockPos near) {
		int pulsed = 0;
		int cx = near.getX() >> 4, cz = near.getZ() >> 4;
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				if (!level.hasChunk(cx + dx, cz + dz)) continue;
				LevelChunk chunk = level.getChunk(cx + dx, cz + dz);
				for (BlockEntity entity : chunk.getBlockEntities().values()) {
					if (entity instanceof MarimoHeartBlockEntity heart) {
						heart.pulse();
						pulsed++;
					}
				}
			}
		}
		return pulsed;
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		if (outcome == null && !intro.active() && player.getUUID().equals(target)) bar.show(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bar.hide(player);
	}

	@Override
	public void remove(RemovalReason reason) {
		if (reason.shouldDestroy() && !level().isClientSide()) clearBlooms();
		bar.hideAll();
		super.remove(reason);
	}

	@Override
	protected boolean opensVacancy() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return phase() == Phase.UPROOTED;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (target != null) tag.putUUID("Hunted", target);
		tag.putString("Wrong", wrong);
		tag.putInt("WardenPhase", entityData.get(PHASE));
		intro.save(tag, "Intro");
		ListTag list = new ListTag();
		for (BlockPos pos : blooms.keySet()) list.add(NbtUtils.writeBlockPos(pos));
		tag.put("Blooms", list);
		if (outcome != null) tag.putBoolean("Ended", true);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		target = tag.hasUUID("Hunted") ? tag.getUUID("Hunted") : null;
		if (tag.contains("Wrong")) wrong = tag.getString("Wrong");
		entityData.set(PHASE, Phase.byId(tag.getInt("WardenPhase")).ordinal());
		intro.load(tag, "Intro");
		entityData.set(INTRO, intro.active() ? intro.elapsed() : -1);
		blooms.clear();
		for (Tag entry : tag.getList("Blooms", Tag.TAG_INT_ARRAY)) {
			NbtUtils.readBlockPos(wrapped(entry), "P").ifPresent(pos -> blooms.put(pos, 0));
		}
		if (tag.getBoolean("Ended")) outcome = Outcome.GONE;
	}

	private static CompoundTag wrapped(Tag entry) {
		CompoundTag wrapper = new CompoundTag();
		wrapper.put("P", entry);
		return wrapper;
	}

	/** For the review client: holds the Warden in a phase and move so each can be shot. */
	public void forcePose(Phase phase, Move move) {
		intro.load(new CompoundTag(), "Intro");
		entityData.set(INTRO, -1);
		setPhase(phase);
		entityData.set(MOVE, move.ordinal());
		entityData.set(MOVE_TICK, 0);
	}
}
