package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BrinedVotaryEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentSpawner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVotaryWayfarerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVoyagerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.ProfessionalHarbingerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.succession.SuccessorRecord;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.gametest.SuccessionTestFixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.function.Supplier;

/**
 * The Harbinger NPCs as the world spawns them: the real entity type, created, placed, {@code finalizeSpawn} as a
 * STRUCTURE spawn and made persistent, exactly as {@code HarbingerOutpostStructure#spawnMob},
 * {@code BloodTempleStructure#spawnHermit} and {@code MasonsRespiteStructure#spawnMob} do. AI stays on and nothing is
 * made invulnerable beyond what the type does itself. Outpost residents carry an outpost key for the test's bounds,
 * and the Vicar is leashed to its home as the outpost leashes it. Positions are absolute. Each NPC is discarded when
 * the test ends.
 */
public final class PlaythroughNpcs {
	/** The eight Harbinger speakers. */
	public enum Role {
		HERMIT(EntityInit.harbinger_hermit::get, false),
		VICAR(EntityInit.harbinger_vicar::get, true),
		ALCHEMIST(EntityInit.harbinger_alchemist::get, true),
		ARTIFICER(EntityInit.harbinger_artificer::get, true),
		MNEMONIST(EntityInit.harbinger_mnemonist::get, true),
		ANCHORITE(EntityInit.harbinger_cicatrix_anchorite::get, false),
		VOYAGER(EntityInit.harbinger_voyager::get, false),
		VOTARY_WAYFARER(EntityInit.harbinger_votary_wayfarer::get, false);

		private final Supplier<? extends EntityType<? extends Entity>> type;
		private final boolean outpost;

		Role(Supplier<? extends EntityType<? extends Entity>> type, boolean outpost) {
			this.type = type;
			this.outpost = outpost;
		}

		public EntityType<? extends Entity> type() {
			return type.get();
		}

		/** Whether the outpost spawns this resident (and marks its outpost key). */
		public boolean outpostResident() {
			return outpost;
		}
	}

	private PlaythroughNpcs() {
	}

	public static HarbingerHermitEntity hermit(GameTestHelper helper, BlockPos pos) {
		return (HarbingerHermitEntity) spawn(helper, Role.HERMIT, pos);
	}

	public static HarbingerVicarEntity vicar(GameTestHelper helper, BlockPos pos) {
		return (HarbingerVicarEntity) spawn(helper, Role.VICAR, pos);
	}

	public static HarbingerAlchemistEntity alchemist(GameTestHelper helper, BlockPos pos) {
		return (HarbingerAlchemistEntity) spawn(helper, Role.ALCHEMIST, pos);
	}

	public static HarbingerArtificerEntity artificer(GameTestHelper helper, BlockPos pos) {
		return (HarbingerArtificerEntity) spawn(helper, Role.ARTIFICER, pos);
	}

	public static HarbingerMnemonistEntity mnemonist(GameTestHelper helper, BlockPos pos) {
		return (HarbingerMnemonistEntity) spawn(helper, Role.MNEMONIST, pos);
	}

	public static HarbingerCicatrixAnchoriteEntity anchorite(GameTestHelper helper, BlockPos pos) {
		return (HarbingerCicatrixAnchoriteEntity) spawn(helper, Role.ANCHORITE, pos);
	}

	public static HarbingerVoyagerEntity voyager(GameTestHelper helper, BlockPos pos) {
		return (HarbingerVoyagerEntity) spawn(helper, Role.VOYAGER, pos);
	}

	public static HarbingerVotaryWayfarerEntity votaryWayfarer(GameTestHelper helper, BlockPos pos) {
		return (HarbingerVotaryWayfarerEntity) spawn(helper, Role.VOTARY_WAYFARER, pos);
	}

	/** Spawns {@code role} at {@code pos} (absolute) the way its structure does. */
	public static Entity spawn(GameTestHelper helper, Role role, BlockPos pos) {
		var level = helper.getLevel();
		Entity entity = role.type().create(level);
		helper.assertTrue(entity != null, role + " could be created");
		entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
		if (role.outpostResident()) HarbingerRecruitmentRules.markOutpostOrigin(entity, outpostKey(helper));
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
			mob.setPersistenceRequired();
		}
		if (entity instanceof HarbingerVicarEntity vicar) vicar.setOutpostHome(pos);
		helper.assertTrue(level.addFreshEntity(entity), role + " joined the level at " + pos.toShortString());
		PlaythroughCleanup.onEnd(helper, entity::discard);
		return entity;
	}

	/**
	 * A misbegotten {@code role} (a succession that went wrong): it neither talks business nor trades. Its officiant is
	 * {@code officiant}.
	 */
	public static ProfessionalHarbingerEntity misbegotten(GameTestHelper helper, Role role, BlockPos pos,
			PlaythroughPlayer officiant) {
		Entity entity = spawn(helper, role, pos);
		helper.assertTrue(entity instanceof ProfessionalHarbingerEntity, role + " can be misbegotten");
		ProfessionalHarbingerEntity npc = (ProfessionalHarbingerEntity) entity;
		npc.initializeMisbegotten(officiant.uuid(), pos.asLong(), "Playthrough Teacher");
		return npc;
	}

	/**
	 * A successor {@code role} born to {@code officiant}'s bloodline through {@code SuccessionTestFixtures.resident}
	 * (the officiant needs a bloodline). Whether it serves another player is the succession rules' call.
	 */
	public static ProfessionalHarbingerEntity successor(GameTestHelper helper, Role role, BlockPos pos,
			PlaythroughPlayer officiant) {
		Entity entity = spawn(helper, role, pos);
		helper.assertTrue(entity instanceof ProfessionalHarbingerEntity, role + " can be a successor");
		ProfessionalHarbingerEntity npc = (ProfessionalHarbingerEntity) entity;
		SuccessorRecord record = SuccessionTestFixtures.resident(officiant.player(), npc, pos);
		helper.assertTrue(record != null && npc.isSuccessor(), role + " became a successor");
		return npc;
	}

	// --- Late speakers -------------------------------------------------------------------------------------------------

	/** The Crimson Troupe's speakers and pieces, by the entity type each is. */
	public enum CircusRole {
		RINGMASTER(EntityInit.circus_ringmaster::get),
		CAROUSEL(EntityInit.circus_carousel::get),
		FIRE_EATER(EntityInit.circus_fire_eater::get),
		STILT_WALKER(EntityInit.circus_stilt_walker::get),
		ACROBAT(EntityInit.circus_acrobat::get),
		KNIFE_THROWER(EntityInit.circus_knife_thrower::get),
		STRONGMAN(EntityInit.circus_strongman::get),
		BEAST_TAMER(EntityInit.circus_beast_tamer::get),
		THREADKEEPER(EntityInit.circus_threadkeeper::get),
		UNDERSTUDY(EntityInit.circus_understudy::get);

		private final Supplier<? extends EntityType<? extends Entity>> type;

		CircusRole(Supplier<? extends EntityType<? extends Entity>> type) {
			this.type = type;
		}

		public EntityType<? extends Entity> type() {
			return type.get();
		}

		/** The eight performers (the Ringmaster and the carousel excepted). */
		public static List<CircusRole> performers() {
			return List.of(FIRE_EATER, STILT_WALKER, ACROBAT, KNIFE_THROWER, STRONGMAN, BEAST_TAMER, THREADKEEPER, UNDERSTUDY);
		}
	}

	/**
	 * A Brined Court member placed on its mark the way a Seat or outpost places it
	 * ({@code CourtResidentSpawner.place}: {@code settle}, a STRUCTURE {@code finalizeSpawn}). {@code mark} null takes
	 * the role's first mark. Discarded when the test ends. Lay the site with {@link PlaythroughSites#courtSite} so the
	 * member stands at home.
	 */
	public static CourtMemberEntity court(GameTestHelper helper, CourtRole role, BlockPos pos, float yaw, @Nullable String mark) {
		var level = helper.getLevel();
		var holder = EntityInit.court_members.get(role);
		helper.assertTrue(holder != null, role + " has a member type");
		String settled = mark != null ? mark : role.marks().isEmpty() ? role.id() : role.marks().getFirst();
		Set<java.util.UUID> before = new HashSet<>();
		AABB around = new AABB(pos).inflate(2.0D);
		for (CourtMemberEntity existing : level.getEntitiesOfClass(CourtMemberEntity.class, around)) before.add(existing.getUUID());
		helper.assertTrue(CourtResidentSpawner.place(level, role, pos, yaw, settled), role + " was placed at " + pos.toShortString());
		CourtMemberEntity member = level.getEntitiesOfClass(CourtMemberEntity.class, around,
				entity -> !before.contains(entity.getUUID()) && entity.getType() == holder.get()).stream().findFirst().orElse(null);
		helper.assertTrue(member != null, role + " joined the level at " + pos.toShortString());
		member.setPersistenceRequired();
		PlaythroughCleanup.onEnd(helper, member::discard);
		return member;
	}

	public static CourtMemberEntity court(GameTestHelper helper, CourtRole role, BlockPos pos) {
		return court(helper, role, pos, 0.0F, null);
	}

	/**
	 * A Crimson Troupe speaker or piece at {@code pos} (absolute), created, {@code finalizeSpawn} as a STRUCTURE spawn
	 * and made persistent, for tests that do not load the pavilion template (which places its own). Discarded when the
	 * test ends.
	 */
	public static Entity circus(GameTestHelper helper, CircusRole role, BlockPos pos) {
		var level = helper.getLevel();
		Entity entity = role.type().create(level);
		helper.assertTrue(entity != null, role + " could be created");
		entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
			mob.setPersistenceRequired();
		}
		helper.assertTrue(level.addFreshEntity(entity), role + " joined the level at " + pos.toShortString());
		PlaythroughCleanup.onEnd(helper, entity::discard);
		return entity;
	}

	/** A Brined Votary (the letter-carrier the Court discovery route kills) at {@code pos}, as a natural spawn. */
	public static BrinedVotaryEntity brinedVotary(GameTestHelper helper, BlockPos pos) {
		var level = helper.getLevel();
		BrinedVotaryEntity votary = EntityInit.brined_votary.get().create(level);
		helper.assertTrue(votary != null, "a Brined Votary could be created");
		votary.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
		votary.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
		votary.setPersistenceRequired();
		helper.assertTrue(level.addFreshEntity(votary), "the Brined Votary joined the level at " + pos.toShortString());
		PlaythroughCleanup.onEnd(helper, votary::discard);
		return votary;
	}

	/**
	 * ARRANGE: the Spawn Mother already fighting {@code owner} at {@code pos} in the test level (the Nursery is in
	 * the Chamber of Will, which this server does not load): the encounter owner set, the owner's Mycophant ACTIVE
	 * flag and Chamber return point recorded as {@code MycophantEncounterManager.enterNursery} records them, and the
	 * waking intro skipped (she is idle, harmable). The flag is cleared and the boss discarded when the test ends.
	 */
	public static MycophantEntity ownedMycophant(GameTestHelper helper, PlaythroughPlayer owner, BlockPos pos) {
		var level = helper.getLevel();
		MycophantEntity boss = EntityInit.mycophant.get().create(level);
		helper.assertTrue(boss != null, "the Mycophant could be created");
		boss.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
		boss.setEncounterOwner(owner.uuid());
		boss.setRematchIntro(true);
		boss.setPersistenceRequired();
		skipIntro(boss);
		helper.assertTrue(level.addFreshEntity(boss), "the Mycophant joined the level at " + pos.toShortString());
		owner.player().getPersistentData().putBoolean(MYCOPHANT_ACTIVE_KEY, true);
		ChamberOfWillManager.get(level.getServer()).rememberReturnPoint(owner.player());
		PlaythroughCleanup.onEnd(helper, () -> {
			boss.discard();
			owner.player().getPersistentData().remove(MYCOPHANT_ACTIVE_KEY);
		});
		return boss;
	}

	/** {@code MycophantEncounterManager}'s owner flag. */
	public static final String MYCOPHANT_ACTIVE_KEY = "hemomancy:mycophant_active";

	private static void skipIntro(MycophantEntity boss) {
		try {
			Method finish = MycophantEntity.class.getDeclaredMethod("finishIntro");
			finish.setAccessible(true);
			finish.invoke(boss);
			for (Method method : MycophantEntity.class.getDeclaredMethods()) {
				if (!method.getName().equals("setState") || method.getParameterCount() != 1) continue;
				Class<?> state = method.getParameterTypes()[0];
				if (!state.isEnum()) continue;
				for (Object constant : state.getEnumConstants()) {
					if (((Enum<?>) constant).name().equals("IDLE")) {
						method.setAccessible(true);
						method.invoke(boss, constant);
					}
				}
			}
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not skip the Mycophant's intro", exception);
		}
	}

	/**
	 * A distinct outpost key for this test: {@link #outpostKey(GameTestHelper)} for {@code n == 0}, else the test's
	 * bounds shifted {@code n * 64} blocks east, so the type and outpost recruiting limits can be tested with two
	 * outposts. Stamp it with {@link #stampOutpost}.
	 */
	public static String outpostKey(GameTestHelper helper, int n) {
		var bounds = helper.getBounds();
		int shift = n * 64;
		BoundingBox box = new BoundingBox((int) Math.floor(bounds.minX) + shift, (int) Math.floor(bounds.minY),
				(int) Math.floor(bounds.minZ), (int) Math.floor(bounds.maxX) + shift, (int) Math.floor(bounds.maxY),
				(int) Math.floor(bounds.maxZ));
		return HarbingerRecruitmentRules.createOutpostKey(helper.getLevel().dimension().location(), box);
	}

	/** ARRANGE: marks {@code npc} as a resident of outpost {@code n} ({@link #outpostKey(GameTestHelper, int)}). */
	public static Entity stampOutpost(GameTestHelper helper, Entity npc, int n) {
		HarbingerRecruitmentRules.markOutpostOrigin(npc, outpostKey(helper, n));
		return npc;
	}

	/**
	 * ARRANGE: kills {@code npc} with out-of-world damage (nothing the player did), for a unique NPC's loss. Its
	 * death runs as any death does ({@code die}, drops, the recruit and succession hooks).
	 */
	public static void kill(Entity npc) {
		if (npc instanceof LivingEntity living) {
			living.invulnerableTime = 0;
			living.hurt(npc.level().damageSources().fellOutOfWorld(), Float.MAX_VALUE);
			if (living.isAlive()) living.kill();
		} else {
			npc.kill();
		}
	}

	/** The outpost key the outpost residents of this test carry: the test's own bounds in its dimension. */
	public static String outpostKey(GameTestHelper helper) {
		var bounds = helper.getBounds();
		BoundingBox box = new BoundingBox((int) Math.floor(bounds.minX), (int) Math.floor(bounds.minY),
				(int) Math.floor(bounds.minZ), (int) Math.floor(bounds.maxX), (int) Math.floor(bounds.maxY),
				(int) Math.floor(bounds.maxZ));
		return HarbingerRecruitmentRules.createOutpostKey(helper.getLevel().dimension().location(), box);
	}
}
