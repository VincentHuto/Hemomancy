package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.mission.alchemist.BloodResearch;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.BloodDrawRights;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The brood in the world (core-loop round two, P6); the numbers are {@link BroodRules}. Two kept creatures of one kind
 * in vessels side by side (touching north, east, south or west, at the same height; a joined vat along any of its
 * sides, {@link SpecimenVessel#beside}), with one keeper, may be paired by that
 * keeper or their bloodline from either vessel's screen. The pressed vessel bears. Each vessel second, after its
 * creature has eaten, the bearer's brood takes its share from the bearer's living buffer or its mate's, while both
 * parents stay above {@link BroodRules#BROOD_VIGOR}; at term the young is put into an empty vessel beside either
 * parent. With none there it waits at term at no further cost. It is never set loose, never an item, never a drop.
 *
 * Rule 2 holds: the brood is paid only from living buffers that only the keeper's Projection or suture fills, so with
 * the keeper gone the parents fade under the mark and it stalls; and each young needs a press and a vessel set down by
 * hand. A mate in an unloaded chunk pauses the brood and is never loaded for it.
 */
public final class SpecimenBrood {
	/** Kinds that never breed in glass, beyond the undead. */
	public static final TagKey<EntityType<?>> NO_BROOD = TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc("menagerie_no_brood"));
	/** Where a mate or a young's vessel may stand: beside, at the same height, in this order. */
	public static final List<Direction> AROUND = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
	private static final ParticleColor THREAD = new ParticleColor(214, 54, 72);
	private static final ParticleColor BURST = new ParticleColor(190, 24, 38);
	/** A brood's progress is resent to watching clients only when it crosses one of this many steps. */
	private static final int SYNC_STEPS = 20;

	private SpecimenBrood() {
	}

	/**
	 * Whether a kind breeds in glass at all: not the undead, and not one tagged {@link #NO_BROOD}. Not limited to
	 * ageable mobs on purpose, hostile kinds included: this is the keeper's way to make more of any living kind (CQ-29).
	 */
	public static boolean breeds(@Nullable EntityType<?> type) {
		return type != null && !type.is(EntityTypeTags.UNDEAD) && !type.is(NO_BROOD);
	}

	/** The vessel at {@code pos} (the whole vessel, for a joined vat) when its chunk is already loaded; never loads one. */
	@Nullable
	public static SpecimenVessel loaded(Level level, @Nullable BlockPos pos) {
		if (pos == null) return null;
		if (level instanceof ServerLevel server && server.getChunkSource()
				.getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ())) == null) {
			return null;
		}
		if (!level.isLoaded(pos)) return null;
		return level.getBlockEntity(pos) instanceof SpecimenVessel vessel ? vessel.vessel() : null;
	}

	private static BlockPos posOf(SpecimenVessel vessel) {
		return vessel.vesselPos();
	}

	// ── Pairing ──

	/** What a press would find: a mate, or why there is none. */
	public record Match(BroodRules.Refusal refusal, @Nullable SpecimenVessel mate) {
	}

	/** Whether {@code presser} may pair {@code bearer} now, and with which neighbour. Changes nothing. */
	public static Match find(Level level, SpecimenVessel bearer, Player presser) {
		KeptSpecimen kept = bearer.kept();
		BlockEntity blockEntity = (BlockEntity) bearer;
		UUID keeper = bearer.buffer().owner();
		long now = level.getGameTime();
		EntityType<?> type = kept.type();
		BroodRules.Refusal own = BroodRules.ownSide(keeper != null, BloodDrawRights.mayDraw(presser, blockEntity),
				kept.living(), type != null && type.is(EntityTypeTags.UNDEAD), type != null && type.is(NO_BROOD),
				kept.resting(now));
		if (own != BroodRules.Refusal.NONE) return new Match(own, null);
		BroodRules.Refusal first = null;
		for (BlockPos beside : bearer.beside()) {
			SpecimenVessel neighbour = loaded(level, beside);
			if (neighbour == null || neighbour == bearer || !neighbour.kept().occupied() || neighbour.kept().type() != type) continue;
			KeptSpecimen other = neighbour.kept();
			BroodRules.Refusal refusal = BroodRules.mateSide(Objects.equals(keeper, neighbour.buffer().owner()),
					other.living(), other.paired(), other.resting(now));
			if (refusal == BroodRules.Refusal.NONE) return new Match(refusal, neighbour);
			if (first == null) first = refusal;
		}
		return new Match(first == null ? BroodRules.Refusal.NO_MATE : first, null);
	}

	/**
	 * The keeper's press on a vessel's screen: pairs an unpaired vessel with its first eligible neighbour, or parts a
	 * paired one (what was paid is lost). Returns the refusal, {@link BroodRules.Refusal#NONE} when something changed.
	 */
	public static BroodRules.Refusal press(ServerLevel level, SpecimenVessel vessel, ServerPlayer presser) {
		KeptSpecimen kept = vessel.kept();
		if (kept.paired()) {
			if (!BloodDrawRights.mayDraw(presser, (BlockEntity) vessel)) return BroodRules.Refusal.NOT_YOURS;
			sever(level, vessel);
			level.playSound(null, posOf(vessel), SoundEvents.HONEY_BLOCK_SLIDE, SoundSource.BLOCKS, 0.7F, 0.6F);
			presser.displayClientMessage(Component.translatable("message.hemomancy.menagerie.brood.parted")
					.withStyle(ChatFormatting.GRAY), true);
			return BroodRules.Refusal.NONE;
		}
		Match match = find(level, vessel, presser);
		if (match.refusal() != BroodRules.Refusal.NONE) {
			presser.displayClientMessage(Component.translatable(match.refusal().key()).withStyle(ChatFormatting.GRAY), true);
			return match.refusal();
		}
		SpecimenVessel mate = match.mate();
		kept.pair(posOf(mate), true);
		mate.kept().pair(posOf(vessel), false);
		BlockEntitySync.markAndSync((BlockEntity) vessel);
		BlockEntitySync.markAndSync((BlockEntity) mate);
		level.playSound(null, posOf(vessel), SoundEvents.HONEY_DRINK, SoundSource.BLOCKS, 0.8F, 0.5F);
		presser.displayClientMessage(Component.translatable("message.hemomancy.menagerie.brood.paired",
				SpecimenJarData.getSpecimenName(kept.specimen()),
				Math.round(BroodRules.broodCost(kept.maxHealth()))), true);
		return BroodRules.Refusal.NONE;
	}

	/** Breaks {@code vessel}'s pairing on both sides; what was paid is lost. For parting, breaking and lifting. */
	public static void sever(Level level, SpecimenVessel vessel) {
		KeptSpecimen kept = vessel.kept();
		if (!kept.paired()) return;
		BlockPos mate = kept.broodMate();
		kept.unpair();
		BlockEntitySync.markAndSync((BlockEntity) vessel);
		releaseMate(level, mate, posOf(vessel));
	}

	/** The vessel at {@code mate}, when it is loaded and paired with {@code self}, is unpaired. */
	public static void releaseMate(Level level, @Nullable BlockPos mate, BlockPos self) {
		SpecimenVessel other = loaded(level, mate);
		if (other == null || !self.equals(other.kept().broodMate())) return;
		other.kept().unpair();
		BlockEntitySync.markAndSync((BlockEntity) other);
	}

	// ── The second ──

	/** The pair as it stands; null when {@code vessel} is not paired or its mate is not loaded. */
	@Nullable
	public static SpecimenVessel mateOf(Level level, SpecimenVessel vessel) {
		return vessel.kept().paired() ? loaded(level, vessel.kept().broodMate()) : null;
	}

	/** Where {@code vessel}'s brood stands, read from whichever of the pair bears. */
	public static BroodRules.State state(Level level, SpecimenVessel vessel) {
		KeptSpecimen kept = vessel.kept();
		long now = level.getGameTime();
		SpecimenVessel mate = mateOf(level, vessel);
		if (!kept.paired() || mate == null) return BroodRules.state(kept.paired(), 0.0D, 1.0D, false, now, kept.restUntil());
		KeptSpecimen bearer = kept.broodBearer() ? kept : mate.kept();
		double cost = BroodRules.broodCost(bearer.maxHealth());
		return BroodRules.state(true, bearer.broodPaid(), cost, BroodRules.gestates(kept.vigor(), mate.kept().vigor()),
				now, kept.restUntil());
	}

	/** The bearer's paid blood and the cost of its young, for a screen; {0, 0} when unpaired. */
	public static double[] paidAndCost(Level level, SpecimenVessel vessel) {
		KeptSpecimen kept = vessel.kept();
		if (!kept.paired()) return new double[]{0.0D, 0.0D};
		SpecimenVessel mate = mateOf(level, vessel);
		KeptSpecimen bearer = kept.broodBearer() || mate == null ? kept : mate.kept();
		return new double[]{bearer.broodPaid(), BroodRules.broodCost(bearer.maxHealth())};
	}

	/**
	 * One vessel second for a paired vessel, after its creature has eaten. Either side drops a pairing its mate no
	 * longer answers; the bearer then takes its share and, at term, delivers. Returns whether anything a client draws
	 * changed enough to resend.
	 */
	public static boolean second(ServerLevel level, BlockPos pos, SpecimenVessel vessel) {
		KeptSpecimen kept = vessel.kept();
		if (!kept.paired()) return false;
		BlockPos matePos = kept.broodMate();
		if (!kept.living()) {
			kept.unpair();
			return true;
		}
		if (level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(matePos.getX()),
				SectionPos.blockToSectionCoord(matePos.getZ())) == null) {
			return false;
		}
		SpecimenVessel mate = loaded(level, matePos);
		if (!answers(vessel, pos, mate)) {
			kept.unpair();
			return true;
		}
		if (!kept.broodBearer()) return false;
		double cost = BroodRules.broodCost(kept.maxHealth());
		if (BroodRules.atTerm(kept.broodPaid(), cost)) return deliver(level, pos, vessel, mate);
		long now = level.getGameTime();
		kept.settle(now, true);
		mate.kept().settle(now, true);
		if (!BroodRules.gestates(kept.vigor(), mate.kept().vigor())) return false;
		double share = BroodRules.share(kept.broodPaid(), cost);
		int stepBefore = step(kept.broodPaid(), cost);
		if (!kept.payBrood(vessel.buffer(), share) && !kept.payBrood(mate.buffer(), share)) return false;
		if (BroodRules.drainsVigor(BroodRules.paidSeconds(kept.broodPaid(), cost))) {
			kept.spendVigor(BroodRules.VIGOR_DRAIN, now);
			mate.kept().spendVigor(BroodRules.VIGOR_DRAIN, now);
		}
		motes(level, mate, vessel);
		if (BroodRules.atTerm(kept.broodPaid(), cost)) {
			deliver(level, pos, vessel, mate);
			return true;
		}
		boolean visible = step(kept.broodPaid(), cost) != stepBefore;
		if (visible) BlockEntitySync.markAndSync((BlockEntity) mate);
		else ((BlockEntity) mate).setChanged();
		return visible;
	}

	private static int step(double paid, double cost) {
		return (int) Math.floor(BroodRules.progress(paid, cost) * SYNC_STEPS);
	}

	/** Whether {@code mate} still holds up the other end of {@code vessel}'s pairing. */
	private static boolean answers(SpecimenVessel vessel, BlockPos pos, @Nullable SpecimenVessel mate) {
		if (mate == null || mate == vessel) return false;
		KeptSpecimen other = mate.kept();
		return other.living() && other.type() == vessel.kept().type() && pos.equals(other.broodMate())
				&& other.broodBearer() != vessel.kept().broodBearer()
				&& Objects.equals(vessel.buffer().owner(), mate.buffer().owner());
	}

	/** A few fine motes drift along the thread from the mate's vessel to the bearer's each paid second. */
	private static void motes(ServerLevel level, SpecimenVessel from, SpecimenVessel to) {
		Vec3 a = threadAnchor(from), b = threadAnchor(to);
		for (int i = 1; i <= 5; i++) {
			Vec3 at = threadPoint(a, b, i / 6.0D);
			level.sendParticles(HemoParticleData.glow(THREAD), at.x, at.y, at.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
		}
	}

	/** How high the brood's thread arcs over the gap between two vessels. */
	public static final double THREAD_ARC = 0.75D;

	/** Where the brood's thread leaves a vessel: the middle of its lid, above the glass and the creature. */
	public static Vec3 threadAnchor(SpecimenVessel vessel) {
		return vessel.lidCentre();
	}

	/** A point {@code t} of the way along the thread's arc from {@code from} to {@code to}. */
	public static Vec3 threadPoint(Vec3 from, Vec3 to, double t) {
		return from.lerp(to, t).add(0.0D, THREAD_ARC * Math.sin(Math.PI * t), 0.0D);
	}

	/** The middle of a vessel's glass. */
	public static Vec3 glassCentre(SpecimenVessel vessel) {
		return vessel.glassCentre();
	}

	// ── The birth ──

	/**
	 * At term: the young goes into the first empty vessel beside either parent that it fits and that is unowned or the
	 * keeper's. With none, the brood waits at term. Returns whether a young was born.
	 */
	public static boolean deliver(ServerLevel level, BlockPos pos, SpecimenVessel bearer, SpecimenVessel mate) {
		KeptSpecimen kept = bearer.kept();
		EntityType<?> type = kept.type();
		UUID keeper = bearer.buffer().owner();
		if (type == null || keeper == null) return false;
		Mob young = null;
		SpecimenVessel cradle = null;
		try {
			for (SpecimenVessel parent : List.of(bearer, mate)) {
				for (BlockPos beside : parent.beside()) {
					SpecimenVessel candidate = loaded(level, beside);
					if (candidate == null || candidate == bearer || candidate == mate || candidate.kept().occupied()) continue;
					UUID owner = candidate.buffer().owner();
					if (owner != null && !owner.equals(keeper)) continue;
					if (young == null) young = young(level, type, kept.inheritedSchool(), mate.kept().inheritedSchool());
					if (young == null) return false;
					if (candidate.fits(young)) {
						cradle = candidate;
						break;
					}
				}
				if (cradle != null) break;
			}
			if (cradle == null) return false;
			BlockPos cradlePos = posOf(cradle);
			Vec3 cradleAt = cradle.glassCentre();
			young.moveTo(cradleAt.x, cradlePos.getY(), cradleAt.z, 0.0F, 0.0F);
			CompoundTag saved = SpecimenJarData.captureEntity(young);
			if (saved.isEmpty()) return false;
			long now = level.getGameTime();
			cradle.kept().keep(saved, KeptSpecimen.maxHealthOf(young), BroodRules.YOUNG_VIGOR, now);
			if (cradle.buffer().owner() == null) cradle.buffer().setOwner(keeper);
			for (SpecimenVessel parent : List.of(bearer, mate)) {
				parent.kept().unpair();
				parent.kept().spendVigor(BroodRules.BIRTH_VIGOR, now);
				parent.kept().restUntil(now + BroodRules.REST_TICKS);
				BlockEntitySync.markAndSync((BlockEntity) parent);
			}
			BlockEntitySync.markAndSync((BlockEntity) cradle);
			born(level, cradle, keeper, type);
			return true;
		} finally {
			if (young != null) young.discard();
		}
	}

	/**
	 * A young of {@code type}, made fresh and never added to the level: no spawn finalising (no jockeys, gear or group),
	 * a baby where the kind has one, carrying a parent's inherited school by {@link BroodRules#inherits}.
	 */
	@Nullable
	private static Mob young(ServerLevel level, EntityType<?> type, @Nullable EnumBloodTemper bearer,
			@Nullable EnumBloodTemper mate) {
		Entity made = type.create(level);
		if (!(made instanceof Mob mob)) {
			if (made != null) made.discard();
			return null;
		}
		if (mob instanceof AgeableMob ageable) ageable.setAge(BroodRules.YOUNG_AGE);
		EnumBloodTemper school = BroodRules.inherits(bearer, mate, level.random.nextDouble());
		if (school != null) HemoCapabilityAccess.getAnimalTaint(mob).inherited = school;
		return mob;
	}

	/** The birth, seen and heard: the young quickens in its glass under a bold burst, and its keeper is told. */
	private static void born(ServerLevel level, SpecimenVessel cradle, UUID keeper, EntityType<?> type) {
		BlockEntity blockEntity = (BlockEntity) cradle;
		BlockPos pos = cradle.vesselPos();
		Vec3 centre = glassCentre(cradle);
		level.blockEvent(pos, blockEntity.getBlockState().getBlock(), 2, 0);
		level.sendParticles(HemoParticleData.bloodCell(BURST), centre.x, centre.y, centre.z, 60, 0.45D, 0.6D, 0.45D, 0.12D);
		level.sendParticles(HemoParticleData.glow(THREAD), centre.x, centre.y, centre.z, 40, 0.5D, 0.7D, 0.5D, 0.04D);
		level.sendParticles(HemoParticleData.darkGlow(new ParticleColor(90, 12, 20)), centre.x, centre.y, centre.z, 16,
				0.35D, 0.45D, 0.35D, 0.02D);
		Vec3 lid = threadAnchor(cradle);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.BLOOD_SPLASH, lid, lid.add(0.0D, 1.0D, 0.0D), 1.4D, 20, 6);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.ANIMUS_IMPACT, centre, centre.add(0.0D, 1.0D, 0.0D), 1.2D, 16);
		level.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 1.0F, 0.7F);
		level.playSound(null, pos, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 0.8F, 0.5F);
		if (level.getPlayerByUUID(keeper) instanceof ServerPlayer player) {
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_FIRST_BROOD);
			BloodResearch.recordMet(player, type);
			if (player.distanceToSqr(centre) < 48.0D * 48.0D) {
				player.displayClientMessage(Component.translatable("message.hemomancy.menagerie.brood.born",
						type.getDescription()), true);
			}
		}
	}
}
