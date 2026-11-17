package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.damage.SchoolDamageSource;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillOrigin;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillPhase;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbChannel;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandBrand;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandCue;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandForm;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandService;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Core-loop Track 12: strand combat with a real player through the real Projection chain. A held lash charges, is
 * thrown by a flick the caster's client reports, cracks on the first thing in its arc, sweeps once per ten ticks,
 * fades when left still, and answers the attack button; a tap strikes once down the look; every hit is the server's,
 * attributed to the caster and carrying the school; a jet pierces at full pressure; a spray lays its state on the
 * crowd in its cone; a plant erupts when let go; a brand marks a creature that Absorption then draws from at range.
 * Stations, rites and Wills still take the channel first. Absorption names the one drawing, and its riders read the
 * creature's school state.
 */
@GameTestHolder("strand_validation")
@PrefixGameTestTemplate(false)
public final class StrandGameTests {
	private static final String EMPTY = "empty";

	private StrandGameTests() {
	}

	// ── Fixtures ──

	/** A clear room: a stone floor at y 0 over x 0..20, z 0..14 and air above it to y 8. */
	private static void clear(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x <= 20; x++) {
			for (int z = 0; z <= 14; z++) {
				level.setBlock(at(helper, x, 0, z), Blocks.STONE.defaultBlockState(), 2);
				for (int y = 1; y <= 8; y++) level.setBlock(at(helper, x, y, z), Blocks.AIR.defaultBlockState(), 2);
			}
		}
	}

	private static BlockPos at(GameTestHelper helper, int x, int y, int z) {
		return helper.absolutePos(new BlockPos(x, y, z));
	}

	private static Vec3 feet(GameTestHelper helper, double x, double z) {
		BlockPos base = at(helper, 0, 1, 0);
		return new Vec3(base.getX() + x, base.getY(), base.getZ() + z);
	}

	/** Active blood, both verbs known, {@code manipulation} known, equipped and selected, with Temper carried. */
	private static void prepare(ServerPlayer player, String manipulation, double blood) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(Math.max(5_000.0D, blood));
		volume.setBloodVolume(blood);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		BloodManipulation registered = ManipulationInit.getByName(manipulation);
		known.getKnownManips().put(registered, new ManipLevel(1, 0));
		// A family form (the needle fan and lance, Ferric Spikes) is open once its baseline is known to the form's level.
		com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.form(manipulation).ifPresent(form ->
				known.getKnownManips().put(ManipulationInit.getByName(
						com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.baselineId(manipulation)),
						new ManipLevel(form.requiredLevel(), 0)));
		known.setEquippedManipNames(new ArrayList<>(List.of(manipulation)));
		known.setSelectedManip(registered);
		var temperament = HemoCapabilityAccess.getBloodTemperament(player);
		if (temperament.getAlignmentByTemper(registered.getTend()) < registered.getAlignLevel()) {
			temperament.setTemperAlignment(registered.getTend(), (float) registered.getAlignLevel() + 1.0F);
		}
		TemperFixtures.carry(player);
	}

	/** Stands the player at {@code feet} and turns them to a yaw and pitch, the old rotation following. */
	private static void face(ServerPlayer player, Vec3 feet, float yaw, float pitch) {
		player.setPos(feet.x, feet.y, feet.z);
		player.setYRot(yaw);
		player.setXRot(pitch);
		player.setYHeadRot(yaw);
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	private static void aim(ServerPlayer player, Vec3 feet, Vec3 at) {
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at);
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	private static double project(ServerPlayer player) {
		return BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
	}

	private static double blood(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	/** A still, sturdy husk with its feet at {@code feet}. */
	private static Husk dummy(GameTestHelper helper, Vec3 feet, double health) {
		Husk husk = EntityType.HUSK.create(helper.getLevel());
		husk.moveTo(feet.x, feet.y, feet.z, 0.0F, 0.0F);
		husk.setNoAi(true);
		husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
		husk.setHealth((float) health);
		husk.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		helper.getLevel().addFreshEntity(husk);
		return husk;
	}

	private static <T extends Mob> T still(GameTestHelper helper, EntityType<T> type, Vec3 feet) {
		T mob = type.create(helper.getLevel());
		mob.moveTo(feet.x, feet.y, feet.z, 0.0F, 0.0F);
		mob.setNoAi(true);
		helper.getLevel().addFreshEntity(mob);
		return mob;
	}

	/** Triangle-wave yaw around {@code centre}: {@code step} degrees a tick, {@code amplitude} each side. */
	private static float sweep(int tick, float centre, float step, float amplitude) {
		int half = Math.max(1, Math.round(2 * amplitude / step));
		int phase = Math.floorMod(tick + half / 2, 2 * half);
		float offset = phase < half ? -amplitude + phase * step : amplitude - (phase - half) * step;
		return centre + offset;
	}

	// ── The lash ──

	/** The ticks a script holds the orb before it flicks. */
	private static final int LASH_HOLD = 3;

	/**
	 * One tick of a lash worked the way a caster's client works it: the look held on {@code centre}, the orb held for
	 * {@link #LASH_HOLD} ticks, a flick to the right, then the whip aim swept from side to side across the centre.
	 */
	private static void workLash(ServerPlayer player, Vec3 stand, int tick, float centre) {
		face(player, stand, centre, 0.0F);
		if (tick == LASH_HOLD) StrandService.flick(player, centre, 0.0F, 1.0F, 0.0F);
		else if (tick > LASH_HOLD) StrandService.aim(player, sweep(tick, centre, 8.0F, 30.0F), 0.0F);
		project(player);
	}

	/** Holds the orb for {@code held} steps, flicks to the right, and runs the throw out, all inside this tick. */
	private static void throwLash(ServerPlayer player, int held) {
		for (int i = 0; i < held; i++) project(player);
		StrandService.flick(player, player.getYRot(), player.getXRot(), 1.0F, 0.0F);
		for (int i = 0; i <= LashRules.tuning().throwTicks(); i++) project(player);
	}

	/** {@link #throwLash}, but let go the moment the throw has cracked: no live lash is left swinging after it. */
	private static void throwAndLetGo(ServerPlayer player, int held) {
		for (int i = 0; i < held; i++) project(player);
		StrandService.flick(player, player.getYRot(), player.getXRot(), 1.0F, 0.0F);
		for (int i = 0; i <= LashRules.tuning().throwTicks() && phase(player) == LashRules.Phase.THROWING; i++) project(player);
		SculptingSessions.end(player, true);
	}

	private static LashRules.Phase phase(ServerPlayer player) {
		return StrandService.lashOf(player).phase();
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void aSweptLashHitsOncePerTenTicksAndNamesTheCaster(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "searing_lash", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		// Facing north (yaw 180); the husk stands six blocks ahead, in the outer half of a 7.5-block lash.
		Husk husk = dummy(helper, feet(helper, 10.5D, 6.5D), 300.0D);
		List<Integer> hits = new ArrayList<>();
		float[] last = {husk.getHealth()};
		DamageSource[] swept = new DamageSource[1];
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t >= 50) return;
			workLash(player, stand, t, 180.0F);
			if (husk.getHealth() < last[0]) {
				// Only sweeps are counted: a crack lands inside the lockout by right, and the Searing it lays burns between.
				if (husk.getLastDamageSource() instanceof SchoolDamageSource struck
						&& struck.context().role() == com.vincenthuto.hemomancy.common.damage.SchoolHitContext.Role.DAMAGE_ONLY
						&& struck.context().kind() == com.vincenthuto.hemomancy.common.damage.SchoolHitContext.Kind.DIRECT) {
					hits.add(t);
					swept[0] = struck;
				}
				last[0] = husk.getHealth();
			}
		});
		helper.runAtTickTime(55, () -> {
			try {
				helper.assertTrue(hits.size() >= 2, "a swept lash landed only " + hits.size() + " hits in 50 ticks");
				for (int i = 1; i < hits.size(); i++) {
					helper.assertTrue(hits.get(i) - hits.get(i - 1) >= 10,
							"two hits " + (hits.get(i) - hits.get(i - 1)) + " ticks apart: " + hits);
				}
				helper.assertTrue(husk.getLastHurtByMob() == player, "the lash's hit did not name the caster");
				DamageSource source = swept[0];
				helper.assertTrue(source instanceof SchoolDamageSource school
								&& school.context().primary() == EnumBloodTemper.FLAMMEUS
								&& school.context().ability().getPath().equals("searing_lash"),
						"the hit did not carry Searing Lash's school: " + source);
				helper.assertTrue(blood(player) < 4_000.0D, "the strand was out for free");
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** Held and never flicked, a lash is an orb: it charges, nothing is out, and nothing in front of it is hurt. */
	@GameTest(template = EMPTY, timeoutTicks = 70)
	public static void aLashChargesBeforeAnythingIsOut(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "searing_lash", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk husk = dummy(helper, feet(helper, 10.5D, 6.5D), 300.0D);
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t >= 50) return;
			// The look wanders: the server reads no flick from it.
			face(player, stand, sweep(t, 180.0F, 15.0F, 30.0F), 0.0F);
			project(player);
		});
		helper.runAtTickTime(55, () -> {
			try {
				helper.assertTrue(StrandService.isOpen(player), "the lash did not open at the husk");
				helper.assertTrue(phase(player) == LashRules.Phase.CHARGING, "an unflicked lash left the orb: " + phase(player));
				helper.assertTrue(!StrandService.lashOf(player).seeded(), "a strand is out while the orb gathers");
				helper.assertTrue(StrandService.lashOf(player).charge() == 1.0D,
						"fifty ticks did not fill Searing Lash's forty-tick charge: " + StrandService.lashOf(player).charge());
				helper.assertTrue(husk.getHealth() == 300.0F, "a charging lash hurt the husk: " + husk.getHealth());
				helper.assertTrue(blood(player) < 4_000.0D, "the orb gathered for free");
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** The throw cracks on the first body in its arc, and a full orb cracks harder and lays its state longer. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aFullChargeCrackHitsHarderThanAnEmptyOne(GameTestHelper helper) {
		clear(helper);
		float[] drop = new float[2];
		int[] searing = new int[2];
		for (int full = 0; full <= 1; full++) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, "searing_lash", 4_000.0D);
				Vec3 stand = feet(helper, 4.5D + full * 10.0D, 12.5D);
				face(player, stand, 180.0F, 0.0F);
				Husk husk = dummy(helper, stand.subtract(0.0D, 0.0D, 6.0D), 300.0D);
				try {
					throwLash(player, full == 1 ? 60 : 1);
					helper.assertTrue(phase(player) == LashRules.Phase.LIVE, "the throw did not end live: " + phase(player));
					drop[full] = 300.0F - husk.getHealth();
					var effect = husk.getEffect(SchoolState.SEARING.effect());
					searing[full] = effect == null ? 0 : effect.getDuration();
				} finally {
					SculptingSessions.end(player, true);
					husk.discard();
				}
			}
		}
		helper.assertTrue(drop[0] > 0.0F, "the throw never cracked on the husk in its arc");
		helper.assertTrue(drop[1] / drop[0] > 1.6F && drop[1] / drop[0] < 1.76F,
				"a full Searing Lash charge did not crack about 1.75 times harder: " + drop[0] + " against " + drop[1]);
		helper.assertTrue(searing[0] > 0 && searing[1] / (double) searing[0] > 1.6D,
				"a full charge did not lay Searing longer: " + searing[0] + " against " + searing[1]);
		helper.succeed();
	}

	/** A wall in the arc stops the throw there: it cracks at the wall and what stands behind is untouched. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theThrowCracksAtABlockItMeets(GameTestHelper helper) {
		clear(helper);
		for (int x = 4; x <= 17; x++) {
			for (int y = 1; y <= 4; y++) helper.getLevel().setBlock(at(helper, x, y, 9), Blocks.STONE.defaultBlockState(), 3);
		}
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 4_000.0D);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			face(player, stand, 180.0F, 0.0F);
			Husk husk = dummy(helper, feet(helper, 10.5D, 6.5D), 300.0D);
			// Pressed against the wall's far face, inside the crack's radius of the near one.
			Husk pressed = dummy(helper, feet(helper, 10.5D, 8.65D), 300.0D);
			try {
				project(player);
				StrandService.flick(player, 180.0F, 0.0F, 1.0F, 0.0F);
				project(player);
				helper.assertTrue(pressed.getHealth() == 300.0F, "the crack reached a body through the wall");
				helper.assertTrue(phase(player) == LashRules.Phase.LIVE, "a throw into a wall did not crack at once: " + phase(player));
				helper.assertTrue(StrandService.lashOf(player).lastCrack() == helper.getLevel().getGameTime(),
						"the wall drew no crack");
				for (int i = 0; i < 8; i++) project(player);
				LashRules.State lash = StrandService.lashOf(player);
				for (int i = 0; i < LashRules.NODES; i++) {
					helper.assertTrue(lash.node(i).z > at(helper, 0, 0, 9).getZ() + 0.9D, "node " + i + " passed the wall: " + lash.node(i));
				}
				helper.assertTrue(husk.getHealth() == 300.0F, "the lash struck through the wall");
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
				pressed.discard();
			}
		}
		helper.succeed();
	}

	/** The throw cracks on a body at arm's length as surely as on one out at the tip. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theThrowCracksOnABodyCloseToTheHand(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 4_000.0D);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			face(player, stand, 180.0F, 0.0F);
			Husk husk = dummy(helper, stand.subtract(0.0D, 0.0D, 2.0D), 300.0D);
			try {
				throwLash(player, 2);
				helper.assertTrue(husk.getHealth() < 300.0F, "the throw passed through a body two blocks from the hand");
				helper.assertTrue(SchoolStates.has(husk, SchoolState.SEARING), "the close crack laid no Searing");
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
			}
		}
		helper.succeed();
	}

	/** A tick on which no aim arrives is not a hand at rest: the stroke goes on, and still tires. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aTickWithNoAimDoesNotRestTheStroke(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 4_000.0D);
			face(player, feet(helper, 10.5D, 12.5D), 180.0F, -20.0F);
			throwLash(player, 2);
			float aim = StrandService.current(player).aimYaw();
			// One way round, an aim on every other tick only.
			for (int i = 0; i < 4; i++) {
				StrandService.aim(player, aim -= 30.0F, -20.0F);
				project(player);
				project(player);
			}
			helper.assertTrue(phase(player) == LashRules.Phase.LIVE, "a stroke with gaps in its aim went to rest: " + phase(player));
			helper.assertTrue(StrandService.lashOf(player).strokeTurn() > LashRules.FRESH_TURN,
					"gaps in the aim kept the stroke fresh: " + StrandService.lashOf(player).strokeTurn());
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	/** A whip left still dissolves back to the orb, and the fade is free. */
	@GameTest(template = EMPTY, timeoutTicks = 90)
	public static void aStillWhipFadesForNothingAndReturnsToTheOrb(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "searing_lash", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		var tuning = LashRules.tuning();
		// The throw lands, the whip rests, and by this tick it has been fading a while.
		int fading = LASH_HOLD + tuning.throwTicks() + tuning.restTicksToFade() + 4;
		int back = fading + tuning.fadeTicks() + 4;
		double[] bloodAt = new double[2];
		LashRules.Phase[] phaseAt = new LashRules.Phase[3];
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > back) return;
			face(player, stand, 180.0F, -20.0F);
			if (t == LASH_HOLD) StrandService.flick(player, 180.0F, -20.0F, 1.0F, 0.0F);
			project(player);
			// Past the pulse that still paid for the whip's last live ticks.
			if (t == fading + 4) bloodAt[0] = blood(player);
			if (t == fading) {
				phaseAt[0] = phase(player);
			}
			// A pulse later, still fading.
			if (t == fading + 21) {
				bloodAt[1] = blood(player);
				phaseAt[1] = phase(player);
			}
			if (t == back) phaseAt[2] = phase(player);
		});
		helper.runAtTickTime(back + 3, () -> {
			try {
				helper.assertTrue(phaseAt[0] == LashRules.Phase.FADING && phaseAt[1] == LashRules.Phase.FADING,
						"a whip at rest was not fading: " + phaseAt[0] + ", " + phaseAt[1]);
				helper.assertTrue(bloodAt[0] == bloodAt[1], "a fading whip cost " + (bloodAt[0] - bloodAt[1]) + " mL");
				helper.assertTrue(phaseAt[2] == LashRules.Phase.CHARGING, "the fade did not end at the orb: " + phaseAt[2]);
				helper.assertTrue(StrandService.isOpen(player), "the fade let go of the channel");
			} finally {
				SculptingSessions.end(player, true);
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aFlickDuringTheFadeRevivesIt(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 4_000.0D);
			face(player, feet(helper, 10.5D, 12.5D), 180.0F, -20.0F);
			throwLash(player, 2);
			for (int i = 0; i < LashRules.tuning().restTicksToFade() + 8; i++) project(player);
			helper.assertTrue(phase(player) == LashRules.Phase.FADING, "the whip did not fade: " + phase(player));
			double faded = StrandService.lashOf(player).extent();
			helper.assertTrue(faded < 1.0D && faded > 0.0D, "eight ticks of fade left " + faded);
			StrandService.aim(player, StrandService.current(player).aimYaw() - 12.0F, -20.0F);
			project(player);
			helper.assertTrue(phase(player) == LashRules.Phase.LIVE, "a flick did not revive the fading whip: " + phase(player));
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	/** Left click on the orb lets the charge go straight down the crosshair, for no more than the charge already paid. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void leftClickWhileChargingReleasesDownTheCrosshair(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 4_000.0D);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			face(player, stand, 180.0F, 0.0F);
			helper.assertTrue(!StrandService.snap(player), "a snap with no strand out did something");
			helper.assertTrue(blood(player) == 4_000.0D, "a refused snap cost blood");
			project(player);
			helper.assertTrue(StrandService.isOpen(player), "the lash did not open at open air");
			helper.assertTrue(!StrandService.snap(player), "an orb barely gathered was let go");
			for (int i = 0; i < 25; i++) project(player);
			helper.assertTrue(Math.abs(StrandService.lashOf(player).charge() - 0.65D) < 0.06D,
					"twenty-six ticks did not fill two thirds of the orb: " + StrandService.lashOf(player).charge());
			// Four blocks out, well short of the tip: the crack lands on the first body down the line.
			Husk husk = dummy(helper, stand.subtract(0.0D, 0.0D, 4.0D), 300.0D);
			double before = blood(player);
			helper.assertTrue(StrandService.snap(player), "the release was refused");
			helper.assertTrue(blood(player) == before, "letting the charge go cost " + (before - blood(player)) + " mL");
			helper.assertTrue(phase(player) == LashRules.Phase.LIVE, "the released lash is not live: " + phase(player));
			float base = LashRules.crackDamage(0.0D,
					((StrandSculpting) SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting()).spec().lash());
			helper.assertTrue(300.0F - husk.getHealth() > base, "a half charge cracked no harder than an empty one: "
					+ (300.0F - husk.getHealth()));
			helper.assertTrue(SchoolStates.has(husk, SchoolState.SEARING), "the crack laid no Searing");
			var searing = husk.getEffect(SchoolState.SEARING.effect());
			helper.assertTrue(searing != null && searing.getDuration() > SchoolState.SEARING.duration,
					"the crack's Searing is not long: " + searing);
			helper.assertTrue(!StrandService.snap(player), "a second snap came inside the cooldown");
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIRST_CRACK)
							&& com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_SCULPTED),
					"the strand's pulse and its crack did not grant Shaped and Whipcrack (core-loop Track 11)");
			SculptingSessions.end(player, true);
			husk.discard();
		}
		helper.succeed();
	}

	/** Left click on a whip that is out pays the snap, cracks down the crosshair and brings the whip aim back to it. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void leftClickWhileLivePaysTheSnapAndRecentresTheWhip(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "searing_lash", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk husk = dummy(helper, stand.subtract(0.0D, 0.0D, 4.0D), 300.0D);
		husk.setInvulnerable(true);
		int snapAt = LASH_HOLD + LashRules.tuning().throwTicks() + LashRules.SNAP_COOLDOWN + 6;
		int[] tick = {0};
		String[] failure = {null};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > snapAt) return;
			face(player, stand, 180.0F, 0.0F);
			if (t == LASH_HOLD) StrandService.flick(player, 180.0F, 0.0F, 1.0F, 0.0F);
			// The whip is carried well off to the right of the view and worked there.
			else if (t > LASH_HOLD) StrandService.aim(player, 245.0F + (t % 2 == 0 ? 6.0F : 0.0F), 0.0F);
			project(player);
			if (t != snapAt) return;
			if (Math.abs(StrandService.current(player).aimYaw() - 180.0F) < 40.0F) failure[0] = "the whip never left the view";
			husk.setInvulnerable(false);
			double before = blood(player);
			if (!StrandService.snap(player)) {
				failure[0] = "the snap was refused";
				return;
			}
			double price = 25.0D * com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger
					.collect(player, ManipulationInit.searing_lash.get(), 1.0D, null).totalMultiplier();
			if (Math.abs(before - blood(player) - price) > 1.0E-6D) failure[0] = "the snap cost " + (before - blood(player)) + ", not " + price;
			else if (StrandService.current(player).aimYaw() != 180.0F) failure[0] = "the whip aim was not brought back to the look";
			else if (husk.getHealth() >= 300.0F) failure[0] = "the crack missed the husk down the crosshair";
			else if (phase(player) != LashRules.Phase.LIVE) failure[0] = "the snapped whip is not live";
		});
		helper.runAtTickTime(snapAt + 3, () -> {
			try {
				helper.assertTrue(failure[0] == null, failure[0]);
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** The server holds the whip aim to its own limits, and takes nothing that is not a number or not yet due. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theServerHoldsTheWhipAimToItsLimits(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 4_000.0D);
			face(player, feet(helper, 10.5D, 12.5D), 180.0F, -20.0F);
			var tuning = LashRules.tuning();
			project(player);
			StrandService.aim(player, 90.0F, 0.0F);
			project(player);
			helper.assertTrue(StrandService.current(player).aimYaw() == 180.0F, "a charging lash took a whip aim");
			helper.assertTrue(!StrandService.flick(player, 180.0F, -20.0F, 0.0F, 0.0F), "a flick with no direction threw");
			helper.assertTrue(!StrandService.flick(player, Float.NaN, -20.0F, 1.0F, 0.0F), "a flick that is not a number threw");
			helper.assertTrue(phase(player) == LashRules.Phase.CHARGING, "a refused flick left the orb");
			// A centre far from the server's look is not believed: the arc is thrown round the look itself.
			helper.assertTrue(StrandService.flick(player, 20.0F, -20.0F, 1.0F, 0.0F), "the flick was refused");
			helper.assertTrue(Math.abs(StrandService.current(player).aimYaw() - (180.0F - tuning.arcDegrees() / 2)) < 1.0E-3D,
					"the throw was centred off the server's look: " + StrandService.current(player).aimYaw());
			for (int i = 0; i <= tuning.throwTicks(); i++) project(player);
			helper.assertTrue(phase(player) == LashRules.Phase.LIVE, "the throw did not land: " + phase(player));
			float landed = StrandService.current(player).aimYaw();
			StrandService.aim(player, Float.NaN, 0.0F);
			StrandService.aim(player, landed, Float.POSITIVE_INFINITY);
			helper.assertTrue(StrandService.current(player).aimYaw() == landed && StrandService.current(player).aimPitch() == -20.0F,
					"an aim that is not a number moved the whip");
			StrandService.aim(player, landed + 170.0F, -20.0F);
			float allowed = (float) Math.min(landed + tuning.maxAimStep(), 180.0D + tuning.maxAimOffset());
			helper.assertTrue(Math.abs(StrandService.current(player).aimYaw() - allowed) < 1.0E-3D,
					"one aim moved the whip further than a tick allows: " + StrandService.current(player).aimYaw());
			float held = StrandService.current(player).aimYaw();
			for (int i = 0; i < 10; i++) StrandService.aim(player, 180.0F + 179.0F, -20.0F);
			helper.assertTrue(StrandService.current(player).aimYaw() == held, "a flood of aims in one step moved the whip further");
			for (int i = 0; i < 12; i++) {
				project(player);
				StrandService.aim(player, 180.0F + 179.0F, -20.0F);
			}
			double off = Math.abs(net.minecraft.util.Mth.wrapDegrees(StrandService.current(player).aimYaw() - 180.0F));
			helper.assertTrue(Math.abs(off - tuning.maxAimOffset()) < 1.0E-3D,
					"the whip aim sits " + off + " degrees from the look, not held at " + tuning.maxAimOffset());
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	/** UN-56 (7): the caster's own client is ahead of the server, so the whip aim the server takes is relayed only to watchers. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theCasterIsNotSentTheirOwnWhipAim(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper).joinLevel()) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 4_000.0D);
			face(player, feet(helper, 10.5D, 12.5D), 180.0F, -20.0F);
			project(player);
			StrandService.flick(player, 180.0F, -20.0F, 1.0F, 0.0F);
			for (int i = 0; i <= LashRules.tuning().throwTicks(); i++) project(player);
			helper.assertTrue(phase(player) == LashRules.Phase.LIVE, "the throw did not land: " + phase(player));
			project(player);
			StrandService.aim(player, StrandService.current(player).aimYaw() + 5.0F, -20.0F);
			helper.assertTrue(captured.sent(com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.LashAimSyncPacket.class)
					.isEmpty(), "the caster was sent their own whip aim back");
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	/** UN-56 (8): a tap's cooldown and chain end with the body that struck them, at a death or a change of dimension. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aTapsCooldownEndsWithTheBody(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "blood_lash", 4_000.0D);
			holdProjection(player);
			face(player, feet(helper, 10.5D, 12.5D), 180.0F, 0.0F);
			helper.assertTrue(StrandService.tap(player), "the first tap was refused");
			helper.assertTrue(!StrandService.tap(player), "a second tap came inside the cooldown");
			StrandService.forgetTaps(player);
			helper.assertTrue(StrandService.tap(player), "the cooldown outlived the body that struck it");
		}
		helper.succeed();
	}

	/** Let go mid-throw, the lash is simply gone: no session, and a late aim or flick from the client finds nothing. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void releaseMidThrowClearsTheSession(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 4_000.0D);
			face(player, feet(helper, 10.5D, 12.5D), 180.0F, -20.0F);
			project(player);
			StrandService.flick(player, 180.0F, -20.0F, 1.0F, 0.0F);
			project(player);
			helper.assertTrue(phase(player) == LashRules.Phase.THROWING, "the lash is not mid-throw: " + phase(player));
			SculptingSessions.end(player, true);
			helper.assertTrue(!StrandService.isOpen(player) && StrandService.lashOf(player) == null, "the session outlived its release");
			StrandService.aim(player, 90.0F, 0.0F);
			helper.assertTrue(!StrandService.flick(player, 180.0F, -20.0F, 1.0F, 0.0F), "a flick threw a lash that is not held");
		}
		helper.succeed();
	}

	// ── The tap ──

	/** The Projection hand held and nothing channelled: what a tap needs. */
	private static void holdProjection(ServerPlayer player) {
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aTapHitsTheFirstBodyOnTheLineForItsBloodAndLaysNoState(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "blood_lash", 4_000.0D);
			holdProjection(player);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			face(player, stand, 180.0F, 0.0F);
			Husk near = dummy(helper, stand.subtract(0.0D, 0.0D, 4.0D), 300.0D);
			Husk far = dummy(helper, stand.subtract(0.0D, 0.0D, 6.0D), 300.0D);
			try {
				helper.assertTrue(StrandService.tap(player), "the tap was refused");
				double price = 8.0D * com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger
						.collect(player, ManipulationInit.blood_lash.get(), 1.0D, null).totalMultiplier();
				helper.assertTrue(Math.abs(4_000.0D - blood(player) - price) < 1.0E-6D,
						"the tap cost " + (4_000.0D - blood(player)) + ", not " + price);
				helper.assertTrue(near.getHealth() < 300.0F, "the tap missed the first husk on the line");
				helper.assertTrue(far.getHealth() == 300.0F, "the tap reached past the first husk");
				helper.assertTrue(!SchoolStates.has(near, SchoolStates.primaryState(EnumBloodTemper.ANIMUS)), "a Blood Lash tap laid its state");
				helper.assertTrue(near.getLastHurtByMob() == player && near.getLastDamageSource() instanceof SchoolDamageSource school
								&& school.context().ability().getPath().equals("blood_lash"),
						"the tap did not name the caster and the lash: " + near.getLastDamageSource());
				helper.assertTrue(!StrandService.isOpen(player), "a tap opened a channel");
			} finally {
				near.discard();
				far.discard();
			}
		}
		helper.succeed();
	}

	/**
	 * Gloam Laceration is a cat of nine tails: a tap lands three quick cuts, not one, and lays nothing; the third tap in
	 * a row is a small crack that lays a short Obscured. A paid tap is not swallowed by the hurt cooldown.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aGloamTapLandsThreeCutsAndTheThirdTapLaysObscured(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "gloam_laceration", 4_000.0D);
		holdProjection(player);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		face(player, stand, 180.0F, 0.0F);
		Husk husk = dummy(helper, stand.subtract(0.0D, 0.0D, 4.0D), 300.0D);
		SchoolState state = SchoolStates.primaryState(EnumBloodTemper.TENEBRIS);
		// A body still inside its hurt cooldown from a heavier blow takes the paid tap all the same.
		husk.hurt(helper.getLevel().damageSources().generic(), 40.0F);
		float held = husk.getHealth();
		List<Float> drops = new ArrayList<>();
		float[] last = {held};
		boolean[] early = {false};
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			face(player, stand, 180.0F, 0.0F);
			if (t == 0 || t == 8 || t == 16) helper.assertTrue(StrandService.tap(player), "tap " + t + " was refused");
			if (husk.getHealth() < last[0]) {
				if (t < 8) drops.add(last[0] - husk.getHealth());
				last[0] = husk.getHealth();
			}
			if (t == 15 && SchoolStates.has(husk, state)) early[0] = true;
		});
		helper.runAtTickTime(26, () -> {
			try {
				helper.assertTrue(drops.size() == 3, "one Gloam tap landed " + drops.size() + " cuts, not three: " + drops);
				helper.assertTrue(Math.abs(drops.get(0) - drops.get(2)) < 1.0E-3F, "the three tails did not cut alike: " + drops);
				helper.assertTrue(!early[0], "a tap before the chain's third laid " + state);
				var effect = husk.getEffect(state.effect());
				helper.assertTrue(effect != null && effect.getDuration() <= 60, "the third tap laid no short " + state + ": " + effect);
			} finally {
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** Gloam Laceration cuts harder from ambush: a body struck from behind takes 1.75 times what one struck from the front does. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void gloamCutsHarderFromBehind(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer[] captured = new CapturedPlayer[2];
		Husk[] husks = new Husk[2];
		for (int behind = 0; behind <= 1; behind++) {
			captured[behind] = CapturedPlayer.create(helper);
			ServerPlayer player = captured[behind].player;
			prepare(player, "gloam_laceration", 4_000.0D);
			holdProjection(player);
			Vec3 stand = feet(helper, 4.5D + behind * 10.0D, 12.5D);
			face(player, stand, 180.0F, 0.0F);
			// The caster stands south of the husk: yaw 0 faces them, yaw 180 shows them its back.
			husks[behind] = dummy(helper, stand.subtract(0.0D, 0.0D, 4.0D), 300.0D);
			float yaw = behind == 1 ? 180.0F : 0.0F;
			husks[behind].setYRot(yaw);
			husks[behind].setYHeadRot(yaw);
			husks[behind].setYBodyRot(yaw);
			helper.assertTrue(StrandService.ambushed(helper.getLevel(), player, husks[behind]) == (behind == 1),
					"ambush read wrong for a husk " + (behind == 1 ? "facing away" : "facing the caster") + " at light "
							+ helper.getLevel().getMaxLocalRawBrightness(husks[behind].blockPosition()));
			helper.assertTrue(StrandService.tap(player), "the tap was refused");
		}
		helper.runAtTickTime(8, () -> {
			try {
				float front = 300.0F - husks[0].getHealth(), back = 300.0F - husks[1].getHealth();
				helper.assertTrue(front > 0.0F && Math.abs(back / front - 1.75F) < 0.02F,
						"a cut from behind was not 1.75 times one from the front: " + front + " against " + back);
			} finally {
				for (int i = 0; i < 2; i++) {
					husks[i].discard();
					captured[i].close();
				}
			}
			helper.succeed();
		});
	}

	/** UN-8: a body already Obscured is no ambush. Facing the caster in the light, it takes the plain cut. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anObscuredBodyFacingTheCasterIsNoAmbush(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "gloam_laceration", 4_000.0D);
		holdProjection(player);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		face(player, stand, 180.0F, 0.0F);
		Husk husk = dummy(helper, stand.subtract(0.0D, 0.0D, 4.0D), 300.0D);
		husk.setYRot(0.0F);
		husk.setYHeadRot(0.0F);
		husk.setYBodyRot(0.0F);
		try {
			SchoolStates.apply(player, husk, SchoolState.OBSCURED, 200);
			helper.assertTrue(SchoolStates.has(husk, SchoolState.OBSCURED), "the husk was not Obscured for the check");
			helper.assertTrue(!StrandService.ambushed(helper.getLevel(), player, husk),
					"an Obscured husk facing the caster at light " + helper.getLevel().getMaxLocalRawBrightness(husk.blockPosition())
							+ " still read as an ambush");
		} finally {
			husk.discard();
			captured.close();
		}
		helper.succeed();
	}

	/** A charged Gloam throw cuts through every body on its arc; an uncharged one cracks on the first and stops. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aChargedGloamThrowCutsThroughItsArc(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer[] captured = new CapturedPlayer[2];
		Husk[][] husks = new Husk[2][2];
		for (int full = 0; full <= 1; full++) {
			captured[full] = CapturedPlayer.create(helper);
			ServerPlayer player = captured[full].player;
			prepare(player, "gloam_laceration", 4_000.0D);
			Vec3 stand = feet(helper, 4.5D + full * 10.0D, 12.5D);
			face(player, stand, 180.0F, 0.0F);
			husks[full][0] = dummy(helper, stand.subtract(0.0D, 0.0D, 2.6D), 300.0D);
			husks[full][1] = dummy(helper, stand.subtract(0.0D, 0.0D, 5.0D), 300.0D);
			throwAndLetGo(player, full == 1 ? 45 : 1);
		}
		helper.runAtTickTime(8, () -> {
			try {
				helper.assertTrue(husks[0][0].getHealth() < 300.0F, "an uncharged throw did not crack on the first husk");
				helper.assertTrue(husks[0][1].getHealth() == 300.0F, "an uncharged throw cut past the first husk");
				helper.assertTrue(husks[1][0].getHealth() < 300.0F && husks[1][1].getHealth() < 300.0F,
						"a charged throw did not cut both husks: " + husks[1][0].getHealth() + ", " + husks[1][1].getHealth());
			} finally {
				for (int i = 0; i < 2; i++) {
					husks[i][0].discard();
					husks[i][1].discard();
					captured[i].close();
				}
			}
			helper.succeed();
		});
	}

	/**
	 * Searing Lash is the heavy whip: a full charge widens its crack to a body an empty one cannot reach and sets what
	 * it strikes alight, and each crack in a row hits harder than the last.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void searingLashBurstsWideOnAChargeAndBuildsHeat(GameTestHelper helper) {
		clear(helper);
		Husk[][] husks = new Husk[2][2];
		for (int full = 0; full <= 1; full++) {
			try (CapturedPlayer wide = CapturedPlayer.create(helper)) {
				ServerPlayer player = wide.player;
				prepare(player, "searing_lash", 4_000.0D);
				Vec3 stand = feet(helper, 3.5D + full * 5.0D, 12.5D);
				face(player, stand, 180.0F, 0.0F);
				husks[full][0] = dummy(helper, stand.subtract(0.0D, 0.0D, 5.0D), 300.0D);
				husks[full][1] = dummy(helper, stand.subtract(0.0D, 0.0D, 7.2D), 300.0D);
				throwAndLetGo(player, full == 1 ? 60 : 1);
			}
		}
		helper.assertTrue(husks[0][0].getHealth() < 300.0F && husks[0][1].getHealth() == 300.0F,
				"an empty crack reached the husk 2.2 blocks behind: " + husks[0][1].getHealth());
		helper.assertTrue(husks[1][1].getHealth() < 300.0F, "a full crack did not burst wide enough to reach 2.2 blocks");
		helper.assertTrue(husks[1][0].getRemainingFireTicks() > 0 && husks[0][0].getRemainingFireTicks() <= 0,
				"only a charged crack sets alight: " + husks[0][0].getRemainingFireTicks() + ", " + husks[1][0].getRemainingFireTicks());
		for (Husk[] pair : husks) for (Husk husk : pair) husk.discard();

		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "searing_lash", 4_000.0D);
		Vec3 stand = feet(helper, 15.5D, 12.5D);
		face(player, stand, 180.0F, 0.0F);
		Husk husk = dummy(helper, stand.subtract(0.0D, 0.0D, 5.0D), 2_000.0D);
		float[] drop = new float[2];
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 30) return;
			face(player, stand, 180.0F, 0.0F);
			if (t == 0) throwLash(player, 1);
			else project(player);
			for (int snap = 0; snap < 2; snap++) {
				if (t != 12 + snap * 12) continue;
				float before = husk.getHealth();
				helper.assertTrue(StrandService.snap(player), "snap " + snap + " was refused");
				drop[snap] = before - husk.getHealth();
			}
		});
		helper.runAtTickTime(34, () -> {
			try {
				helper.assertTrue(drop[0] > 0.0F && Math.abs(drop[1] / drop[0] - 1.3F / 1.15F) < 0.02F,
						"the third crack in a row was not 1.30 against the second's 1.15: " + drop[0] + " then " + drop[1]);
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/**
	 * Blood Lash is the tether: a charged crack holds what it strikes in place, and a lash whose sweeps land costs less
	 * than one swung at nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void bloodLashRootsOnAChargeAndItsSweepsGiveBloodBack(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer rooted = CapturedPlayer.create(helper)) {
			ServerPlayer player = rooted.player;
			prepare(player, "blood_lash", 4_000.0D);
			Vec3 stand = feet(helper, 3.5D, 12.5D);
			face(player, stand, 180.0F, 0.0F);
			Husk husk = dummy(helper, stand.subtract(0.0D, 0.0D, 5.0D), 300.0D);
			try {
				throwLash(player, 30);
				var held = husk.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
				helper.assertTrue(held != null && held.getAmplifier() >= 6 && held.getDuration() <= 40,
						"a fully charged Blood Lash crack did not hold the husk: " + held);
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
			}
		}
		CapturedPlayer[] captured = new CapturedPlayer[2];
		Vec3[] stands = new Vec3[2];
		for (int i = 0; i < 2; i++) {
			captured[i] = CapturedPlayer.create(helper);
			prepare(captured[i].player, "blood_lash", 4_000.0D);
			stands[i] = feet(helper, 8.5D + i * 8.0D, 12.5D);
		}
		Husk husk = dummy(helper, stands[1].subtract(0.0D, 0.0D, 6.0D), 2_000.0D);
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t >= 50) return;
			for (int i = 0; i < 2; i++) workLash(captured[i].player, stands[i], t, 180.0F);
		});
		helper.runAtTickTime(55, () -> {
			try {
				double empty = 4_000.0D - blood(captured[0].player), landed = 4_000.0D - blood(captured[1].player);
				helper.assertTrue(husk.getHealth() < 2_000.0F, "the lash never landed a sweep");
				helper.assertTrue(landed < empty - 3.9D, "landed sweeps gave no blood back: " + landed + " against " + empty);
			} finally {
				for (int i = 0; i < 2; i++) {
					SculptingSessions.end(captured[i].player, true);
					captured[i].close();
				}
				husk.discard();
			}
			helper.succeed();
		});
	}

	/** No tap without the tool, without a lash selected, or while a channel is held; a refused tap is free. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aTapNeedsTheToolALashAndAFreeHand(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 4_000.0D);
			face(player, feet(helper, 10.5D, 12.5D), 180.0F, 0.0F);
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			helper.assertTrue(!StrandService.tap(player), "a bare hand tapped");
			holdProjection(player);
			player.startUsingItem(InteractionHand.MAIN_HAND);
			helper.assertTrue(!StrandService.tap(player), "a channelling hand tapped");
			player.stopUsingItem();
			prepare(player, jetMemory(), 4_000.0D);
			helper.assertTrue(!StrandService.tap(player), "a jet tapped");
			helper.assertTrue(blood(player) == 4_000.0D, "a refused tap cost blood");
			prepare(player, "blood_lash", 5.0D);
			helper.assertTrue(!StrandService.tap(player), "a tap went out on less blood than it costs");
			helper.assertTrue(blood(player) == 5.0D, "a tap that could not be paid took what there was");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void anAllyInTheSweepIsSparedAndShieldsNothing(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "searing_lash", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Wolf wolf = still(helper, EntityType.WOLF, feet(helper, 10.5D, 7.5D));
		wolf.tame(player);
		Husk husk = dummy(helper, feet(helper, 10.5D, 6.2D), 300.0D);
		float wolfHealth = wolf.getHealth();
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t >= 40) return;
			workLash(player, stand, t, 180.0F);
		});
		helper.runAtTickTime(45, () -> {
			try {
				helper.assertTrue(wolf.getHealth() == wolfHealth, "the lash struck the caster's own wolf");
				helper.assertTrue(husk.getHealth() < 300.0F, "the wolf shielded the husk behind it");
			} finally {
				SculptingSessions.end(player, true);
				wolf.discard();
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void aBossTakesAStrandAsOrdinarySchoolDamage(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "searing_lash", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		ElderGuardian guardian = still(helper, EntityType.ELDER_GUARDIAN, feet(helper, 10.5D, 6.5D));
		float before = guardian.getHealth();
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t >= 40) return;
			workLash(player, stand, t, 180.0F);
		});
		helper.runAtTickTime(45, () -> {
			try {
				helper.assertTrue(com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates.isBoss(guardian),
						"the fixture is not a boss");
				helper.assertTrue(guardian.getHealth() < before, "the lash never hurt the boss");
				helper.assertTrue(guardian.getLastDamageSource() instanceof SchoolDamageSource,
						"the boss took the lash as something other than school damage");
			} finally {
				SculptingSessions.end(player, true);
				guardian.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theStrandLetsGoWhenTheBloodRunsOut(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "searing_lash", 40.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ >= 15) return;
			face(player, stand, 180.0F, 0.0F);
			project(player);
		});
		helper.runAtTickTime(18, () -> {
			try {
				helper.assertTrue(!StrandService.isOpen(player), "the strand hung on unpaid with " + blood(player) + " mL");
				helper.assertTrue(blood(player) >= 0.0D && blood(player) < 40.0D, "the first pulse was not paid");
			} finally {
				SculptingSessions.end(player, true);
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── What takes the channel first ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aCauldronStillTakesTheBloodWithAStrandSelected(GameTestHelper helper) {
		clear(helper);
		BlockPos cauldron = at(helper, 10, 1, 9);
		helper.getLevel().setBlock(cauldron, Blocks.CAULDRON.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 2_000.0D);
			aim(player, feet(helper, 10.5D, 12.5D), Vec3.atCenterOf(cauldron));
			helper.assertTrue(StrandSculpting.takesBlood(helper.getLevel(), cauldron, helper.getLevel().getBlockState(cauldron)),
					"a cauldron should read as a block that takes blood");
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "the lash claims the cauldron");
			project(player);
			helper.assertTrue(!StrandService.isOpen(player), "a strand opened on a block that takes blood");
			helper.assertTrue(helper.getLevel().getBlockEntity(cauldron) instanceof SanguineCondenserBlockEntity,
					"the cauldron did not become a Condenser under the strand manipulation");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAshTrailStillLightsWithAStrandSelected(GameTestHelper helper) {
		clear(helper);
		BlockPos trail = at(helper, 10, 1, 9);
		helper.getLevel().setBlock(trail, BlockInit.smouldering_ash_trail.get().defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, plantMemory(), 2_000.0D);
			aim(player, feet(helper, 10.5D, 12.5D), Vec3.atBottomCenterOf(trail));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(),
					"the plant claims an unlit ash trail the ground network takes");
			project(player);
			helper.assertTrue(!StrandService.isOpen(player), "a strand opened on a ground mark that takes blood");
			helper.assertTrue(helper.getLevel().getBlockState(trail).is(BlockInit.active_smouldering_ash_trail.get()),
					"the ash trail did not light under the strand manipulation: " + helper.getLevel().getBlockState(trail));
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aJetSnapAtNothingIsRefusedForFree(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, jetMemory(), 2_000.0D);
			face(player, feet(helper, 10.5D, 12.5D), 180.0F, -30.0F);
			project(player);
			helper.assertTrue(StrandService.isOpen(player), "the jet did not open at open air");
			double before = blood(player);
			helper.assertTrue(!StrandService.snap(player), "a jet snap at open air did something");
			helper.assertTrue(blood(player) == before, "a jet snap at nothing cost " + (before - blood(player)) + " mL");
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anOpenStrandHoldsTheChannelAcrossAStation(GameTestHelper helper) {
		clear(helper);
		BlockPos cauldron = at(helper, 10, 1, 9);
		helper.getLevel().setBlock(cauldron, Blocks.CAULDRON.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 2_000.0D);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			face(player, stand, 90.0F, -20.0F);
			project(player);
			helper.assertTrue(StrandService.isOpen(player), "the lash did not open at open air");
			for (int i = 0; i < 3; i++) {
				aim(player, stand, Vec3.atCenterOf(cauldron));
				project(player);
			}
			helper.assertTrue(StrandService.isOpen(player), "sweeping across a cauldron let go of the strand");
			helper.assertTrue(helper.getLevel().getBlockState(cauldron).is(Blocks.CAULDRON),
					"the held strand handed its blood to the cauldron");
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aFalteringWillStillTakesTheProjectionFromAStrand(GameTestHelper helper) {
		clear(helper);
		WillEntity will = helper.spawn(EntityInit.will.get(), new BlockPos(10, 1, 8));
		will.configure(WillOrigin.BROKEN, EnumBloodTemper.LUX, 1, null, false);
		will.setPhase(WillPhase.FALTERING);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 2_000.0D);
			aim(player, feet(helper, 10.5D, 12.5D), will.getBoundingBox().getCenter());
			double spent = project(player);
			helper.assertTrue(!StrandService.isOpen(player), "a strand opened on a projection the faltering Will claims");
			helper.assertTrue(spent > 0.0D && will.getPhase() == WillPhase.DISSOLVING,
					"the Will was not banished ahead of the strand");
		} finally {
			will.discard();
		}
		helper.succeed();
	}

	// ── The other forms ──

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void aHeldJetBuildsPressureAndPierces(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, jetMemory(), 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk first = dummy(helper, feet(helper, 10.5D, 9.5D), 300.0D);
		Husk behind = dummy(helper, feet(helper, 10.5D, 6.5D), 300.0D);
		float[] behindAt20 = {0};
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t >= 70) return;
			aim(player, stand, first.getBoundingBox().getCenter());
			project(player);
			if (t == 20) behindAt20[0] = behind.getHealth();
		});
		helper.runAtTickTime(75, () -> {
			try {
				helper.assertTrue(first.getHealth() < 300.0F, "the jet never hurt the creature it was held on");
				helper.assertTrue(behindAt20[0] == 300.0F, "the jet pierced before its pressure built");
				helper.assertTrue(behind.getHealth() < 300.0F, "a full jet never pierced to the creature behind");
				helper.assertTrue(SchoolStates.has(first, SchoolState.ILLUMINATED), "a full jet laid no Illuminated");
			} finally {
				SculptingSessions.end(player, true);
				first.discard();
				behind.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aSprayLaysItsStateOnTheCrowdInItsCone(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, sprayMemory(), 4_000.0D);
			Vec3 stand = feet(helper, 10.5D, 10.5D);
			face(player, stand, 180.0F, 15.0F);
			List<Husk> crowd = List.of(dummy(helper, feet(helper, 10.5D, 8.2D), 300.0D),
					dummy(helper, feet(helper, 9.4D, 8.4D), 300.0D), dummy(helper, feet(helper, 11.6D, 8.4D), 300.0D));
			Husk outside = dummy(helper, feet(helper, 10.5D, 13.0D), 300.0D);
			project(player);
			helper.assertTrue(StrandService.isOpen(player), "the spray did not open");
			for (Husk husk : crowd) {
				helper.assertTrue(SchoolStates.has(husk, SchoolState.DISRUPTED), "a husk in the cone was not Disrupted");
				helper.assertTrue(husk.getHealth() < 300.0F, "a husk in the cone took no hit");
			}
			helper.assertTrue(!SchoolStates.has(outside, SchoolState.DISRUPTED), "the spray reached behind the player");
			SculptingSessions.end(player, true);
			crowd.forEach(Husk::discard);
			outside.discard();
		}
		helper.succeed();
	}

	// ── Blood Eclipse: a dome of dark the caster carries ──

	/** Held, the dome grows from two blocks to eight, Obscures what the caster may harm inside it, and moves with them. */
	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void bloodEclipseCarriesAGrowingDomeOfDark(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "blood_eclipse", 4_000.0D);
		Vec3 stand = feet(helper, 6.5D, 7.5D);
		// One husk beside the caster, one five blocks off (outside a new dome, inside a grown one), one far past it.
		Husk near = dummy(helper, feet(helper, 7.5D, 7.5D), 300.0D);
		Husk mid = dummy(helper, feet(helper, 11.5D, 7.5D), 300.0D);
		Husk far = dummy(helper, feet(helper, 18.5D, 7.5D), 300.0D);
		Wolf wolf = still(helper, EntityType.WOLF, feet(helper, 6.5D, 8.5D));
		wolf.tame(player);
		boolean[] midEarly = {false}, hidden = {false};
		double[] early = {0.0D};
		com.vincenthuto.hemomancy.common.world.ground.DarkFields.Carried[] grown = new com.vincenthuto.hemomancy.common.world.ground.DarkFields.Carried[1];
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 80) return;
			face(player, stand, 180.0F, 0.0F);
			project(player);
			if (t == 12) {
				midEarly[0] = SchoolStates.has(mid, SchoolState.OBSCURED);
				var dome = com.vincenthuto.hemomancy.common.world.ground.DarkFields.carried(player);
				early[0] = dome == null ? -1.0D : dome.radius();
			}
			if (t == 80) {
				grown[0] = com.vincenthuto.hemomancy.common.world.ground.DarkFields.carried(player);
				hidden[0] = com.vincenthuto.hemomancy.common.world.ground.DarkFields.covers(near);
			}
		});
		helper.runAtTickTime(84, () -> {
			try {
				var dome = grown[0];
				helper.assertTrue(early[0] > 0.0D && early[0] < 4.0D, "a new dome was " + early[0] + " blocks wide");
				helper.assertTrue(dome != null && dome.radius() == com.vincenthuto.hemomancy.common.manipulation.tenebris.EclipseDomeRules.MAX_RADIUS,
						"four seconds of holding did not grow the dome full: " + dome);
				helper.assertTrue(dome.centre().distanceTo(player.position().add(0.0D, 1.0D, 0.0D)) < 0.01D, "the dome is not on its caster");
				helper.assertTrue(!midEarly[0], "a new dome reached a husk five blocks off");
				helper.assertTrue(SchoolStates.has(near, SchoolState.OBSCURED) && SchoolStates.has(mid, SchoolState.OBSCURED),
						"the grown dome did not Obscure the husks inside it");
				helper.assertTrue(!SchoolStates.has(far, SchoolState.OBSCURED), "the dome reached a husk twelve blocks off");
				helper.assertTrue(!SchoolStates.has(wolf, SchoolState.OBSCURED), "the dome Obscured the caster's own wolf");
				helper.assertTrue(near.getHealth() == 300.0F, "the dome wounded before its flash");
				helper.assertTrue(hidden[0], "a body in the dome is not hidden");
				helper.assertTrue(blood(player) < 4_000.0D, "the dome was carried for free");
				SculptingSessions.end(player, true);
				helper.assertTrue(com.vincenthuto.hemomancy.common.world.ground.DarkFields.carried(player) == null, "the dome outlived its release");
				helper.assertTrue(!com.vincenthuto.hemomancy.common.world.ground.DarkFields.covers(near), "a let-go dome still hides what stood in it");
			} finally {
				SculptingSessions.end(player, true);
				near.discard();
				mid.discard();
				far.discard();
				wolf.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** The attack button ends the dome in a flash: harder from a grown dome, and no new dome for a moment after. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void bloodEclipseEndsInAFlashThatSpendsTheDark(GameTestHelper helper) {
		clear(helper);
		float[] drop = new float[2];
		for (int grown = 0; grown <= 1; grown++) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, "blood_eclipse", 4_000.0D);
				Vec3 stand = feet(helper, 4.5D + grown * 10.0D, 7.5D);
				face(player, stand, 180.0F, 0.0F);
				Husk husk = dummy(helper, stand.add(1.2D, 0.0D, 0.0D), 300.0D);
				Husk outside = dummy(helper, stand.add(0.0D, 0.0D, -9.5D), 300.0D);
				try {
					var sculpting = SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting();
					helper.assertTrue(sculpting.takesAccent() && !sculpting.accent(player), "a flash came from no dome");
					for (int i = 0; i < (grown == 1 ? 80 : 2); i++) project(player);
					double before = blood(player);
					helper.assertTrue(sculpting.accent(player), "the flash was refused");
					helper.assertTrue(blood(player) == before, "the flash cost blood beyond the dome's upkeep");
					drop[grown] = 300.0F - husk.getHealth();
					helper.assertTrue(SchoolStates.has(husk, SchoolState.OBSCURED), "the flash laid no Obscured");
					helper.assertTrue(outside.getHealth() == 300.0F, "the flash reached past the dome");
					helper.assertTrue(!SculptingSessions.isActive(player.getUUID())
									&& com.vincenthuto.hemomancy.common.world.ground.DarkFields.carried(player) == null,
							"the dome outlived its flash");
					double spent = blood(player);
					project(player);
					helper.assertTrue(blood(player) == spent && com.vincenthuto.hemomancy.common.world.ground.DarkFields.carried(player) == null,
							"a new dome opened while the dark was spent");
				} finally {
					SculptingSessions.end(player, true);
					husk.discard();
					outside.discard();
				}
			}
		}
		helper.assertTrue(drop[0] > 0.0F && drop[1] > drop[0] * 2.0F,
				"a grown dome's flash did not hit much harder than a new one's: " + drop[0] + " against " + drop[1]);
		helper.succeed();
	}

	/** With Blood Eclipse selected a cauldron still takes the blood: the dome claims no block that does. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void bloodEclipseLeavesAStationItsBlood(GameTestHelper helper) {
		clear(helper);
		BlockPos cauldron = at(helper, 10, 1, 9);
		helper.getLevel().setBlock(cauldron, Blocks.CAULDRON.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "blood_eclipse", 2_000.0D);
			aim(player, feet(helper, 10.5D, 12.5D), Vec3.atCenterOf(cauldron));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "the dome claims the cauldron");
			project(player);
			helper.assertTrue(com.vincenthuto.hemomancy.common.world.ground.DarkFields.carried(player) == null, "a dome opened on a station");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void aPlantEruptsWhenLetGo(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper).joinLevel();
		ServerPlayer player = captured.player;
		prepare(player, plantMemory(), 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		BlockPos ground = at(helper, 10, 0, 9);
		Husk husk = dummy(helper, feet(helper, 11.5D, 9.5D), 300.0D);
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t < 25) {
				aim(player, stand, Vec3.atCenterOf(ground).add(0, 0.5D, 0));
				project(player);
			} else if (t == 25) {
				helper.assertTrue(StrandService.isOpen(player), "the plant did not open on the ground");
				helper.assertTrue(husk.getHealth() == 300.0F, "the plant hurt before it erupted");
				SculptingSessions.end(player, true);
			}
		});
		helper.runAtTickTime(25 + com.vincenthuto.hemomancy.common.manipulation.verb.strand.PlantRules.ERUPTION_DELAY + 5, () -> {
			try {
				helper.assertTrue(husk.getHealth() < 300.0F, "the plant never erupted under the husk");
				helper.assertTrue(SchoolStates.has(husk, SchoolState.RIME), "Glacial Grasp's eruption laid no Rime");
			} finally {
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/**
	 * The memory the brand kit is lent to for these cases. No memory holds a brand strand of its own since the held
	 * forms that only repeated their R cast were cut (2026-11-12); the kit stays, for a manipulation that earns it. The
	 * loan is to a Mortem memory with no strand, made once and left for the suite, so cases running together share it.
	 */
	private static String brandMemory() {
		BloodManipulation memory = ManipulationInit.getByName("exsanguinate");
		if (memory.getSculpting(Verb.PROJECT).isEmpty()) {
			var brand = com.vincenthuto.hemomancy.common.manipulation.verb.strand.Strands.brand(20.0D, 1.0F);
			memory.setProjectionSculpting(() -> brand);
		}
		return "exsanguinate";
	}

	/**
	 * The memory the plant kit's own cases run on: lent to a Congeatio memory with no held form, as {@link #brandMemory}
	 * is. Glacial Grasp held a plant until it became a hand of ice (2026-11-12).
	 */
	private static String plantMemory() {
		BloodManipulation memory = ManipulationInit.getByName("cryogenic_pulse");
		if (memory.getSculpting(Verb.PROJECT).isEmpty()) {
			var plant = com.vincenthuto.hemomancy.common.manipulation.verb.strand.Strands.plant(20.0D, 1.0F);
			memory.setProjectionSculpting(() -> plant);
		}
		return "cryogenic_pulse";
	}

	// ── Cruor Javelin: drawn, then thrown ──

	private static List<com.vincenthuto.hemomancy.common.entity.projectile.CruorJavelinEntity> javelins(GameTestHelper helper, Vec3 near) {
		return helper.getLevel().getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.projectile.CruorJavelinEntity.class,
				new net.minecraft.world.phys.AABB(near, near).inflate(24.0D));
	}

	/** Letting go throws the javelin: it flies, strikes the body in its way as the caster's Congeatio hit, and breaks there. */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void cruorJavelinIsDrawnAndThrownOnRelease(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "cruor_javelin", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk husk = dummy(helper, feet(helper, 10.5D, 5.5D), 300.0D);
		Wolf wolf = still(helper, EntityType.WOLF, feet(helper, 10.5D, 9.5D));
		wolf.tame(player);
		float wolfHealth = wolf.getHealth();
		aim(player, stand, husk.getBoundingBox().getCenter());
		for (int i = 0; i < 35; i++) project(player);
		helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinThrowRules.stage(
				com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinThrowSculpting.drawnTicks(player)) == 3,
				"thirty-five ticks did not draw the javelin to its third stage");
		helper.assertTrue(javelins(helper, stand).isEmpty() && husk.getHealth() == 300.0F, "a javelin flew before it was let go");
		double drawn = blood(player);
		SculptingSessions.end(player, true);
		helper.assertTrue(javelins(helper, stand).size() == 1, "letting go threw " + javelins(helper, stand).size() + " javelins");
		helper.assertTrue(blood(player) == drawn, "the throw cost blood beyond the draw");
		helper.runAtTickTime(30, () -> {
			try {
				float third = (float) (com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinThrowRules.damage(3) * 0.9F);
				helper.assertTrue(300.0F - husk.getHealth() >= third, "a third-stage javelin took " + (300.0F - husk.getHealth()) + " off the husk");
				helper.assertTrue(SchoolStates.has(husk, SchoolState.RIME), "the javelin drove no Rime in");
				helper.assertTrue(husk.getLastHurtByMob() == player && husk.getLastDamageSource() instanceof SchoolDamageSource school
								&& school.context().primary() == EnumBloodTemper.CONGEATIO
								&& school.context().ability().getPath().equals("cruor_javelin"), "the javelin's hit is not the caster's Cruor Javelin");
				helper.assertTrue(wolf.getHealth() == wolfHealth, "the javelin struck the caster's own wolf on its way");
				helper.assertTrue(javelins(helper, stand).isEmpty(), "the javelin outlived the body it broke on");
			} finally {
				husk.discard();
				wolf.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** A draw let go too soon throws nothing. A javelin stuck in a block gives half its blood back to Absorption, once. */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void aStuckCruorJavelinGivesHalfItsBloodBack(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "cruor_javelin", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Vec3 floor = Vec3.atCenterOf(at(helper, 10, 0, 9)).add(0.0D, 0.5D, 0.0D);
		aim(player, stand, floor);
		for (int i = 0; i < 3; i++) project(player);
		SculptingSessions.end(player, true);
		helper.assertTrue(javelins(helper, stand).isEmpty(), "three ticks of drawing threw a javelin");
		double[] paid = new double[1];
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t < 25) {
				aim(player, stand, floor);
				project(player);
			} else if (t == 25) {
				paid[0] = 4_000.0D - blood(player);
				SculptingSessions.end(player, true);
			}
		});
		helper.runAtTickTime(60, () -> {
			try {
				var stuck = javelins(helper, stand);
				helper.assertTrue(stuck.size() == 1 && stuck.getFirst().stuck(), "the javelin is not stuck in the floor: " + stuck);
				var javelin = stuck.getFirst();
				helper.assertTrue(javelin.paid() > 0.0D && javelin.paid() <= paid[0], "the javelin carries " + javelin.paid() + " of " + paid[0]);
				player.lookAt(EntityAnchorArgument.Anchor.EYES, javelin.position());
				helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinRecallSculpting.aimed(player) == javelin,
						"Absorption does not find the caster's stuck javelin");
				helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.ABSORB).isPresent(), "Absorption does not claim the stuck javelin");
				double before = blood(player);
				helper.assertTrue(SculptingDispatcher.trySculpt(player, Verb.ABSORB).claimed(), "the recall was not taken");
				double back = blood(player) - before;
				helper.assertTrue(Math.abs(back - javelin.paid() * 0.5D) < 1.0E-6D, "the javelin gave back " + back + " of " + javelin.paid());
				helper.assertTrue(javelins(helper, stand).isEmpty(), "the recalled javelin still stands");
				helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.ABSORB).isEmpty(), "with no javelin, Absorption is still claimed");
			} finally {
				SculptingSessions.end(player, true);
				javelins(helper, stand).forEach(net.minecraft.world.entity.Entity::discard);
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── Ferric Spikes: a run of blades steered along the ground ──

	/** The run starts at the caster's feet, travels toward where they look, and impales what it passes under. */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void ferricSpikesRunsAlongTheGroundTowardTheAim(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "ferric_spikes", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk husk = dummy(helper, feet(helper, 10.5D, 8.5D), 300.0D);
		Wolf wolf = still(helper, EntityType.WOLF, feet(helper, 10.5D, 10.5D));
		wolf.tame(player);
		float wolfHealth = wolf.getHealth();
		Vec3 aimAt = Vec3.atCenterOf(at(helper, 10, 0, 4)).add(0.0D, 0.5D, 0.0D);
		Vec3[] early = new Vec3[1];
		List<Integer> bites = new ArrayList<>();
		float[] last = {300.0F};
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 40) return;
			aim(player, stand, aimAt);
			project(player);
			if (t == 2) early[0] = com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRunSculpting.at(player);
			if (husk.getHealth() < last[0]) {
				bites.add(t);
				last[0] = husk.getHealth();
			}
		});
		helper.runAtTickTime(43, () -> {
			try {
				Vec3 run = com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRunSculpting.at(player);
				helper.assertTrue(early[0] != null && early[0].distanceTo(stand) < 2.5D, "the run did not start at the caster's feet: " + early[0]);
				helper.assertTrue(run != null && Math.abs(run.x - aimAt.x) < 0.3D && Math.abs(run.z - aimAt.z) < 0.3D,
						"forty ticks did not carry the run eight blocks to the aim: " + run);
				helper.assertTrue(run.y == stand.y, "the run left the ground: " + run);
				helper.assertTrue(husk.getHealth() < 300.0F, "the run passed under the husk and did not impale it");
				for (int i = 1; i < bites.size(); i++) {
					helper.assertTrue(bites.get(i) - bites.get(i - 1) >= 10, "the run bit one body twice inside ten ticks: " + bites);
				}
				helper.assertTrue(SchoolStates.has(husk, SchoolState.LODESTONE), "the run laid no Lodestone");
				helper.assertTrue(husk.getLastHurtByMob() == player && husk.getLastDamageSource() instanceof SchoolDamageSource school
								&& school.context().primary() == EnumBloodTemper.FERRIC, "the run's bite is not the caster's Ferric hit");
				helper.assertTrue(wolf.getHealth() == wolfHealth, "the run impaled the caster's own wolf");
				helper.assertTrue(blood(player) < 4_000.0D, "the run was steered for free");
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
				wolf.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** The run needs ground: it stops at a pit and at a wall, and turns when the aim does. */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void ferricSpikesStopsAtAGapAndAWall(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		// A trench four deep across the room at z 8, and a two-high wall at x 15.
		for (int x = 0; x <= 20; x++) {
			for (int y = -3; y <= 0; y++) level.setBlock(at(helper, x, y, 8), Blocks.AIR.defaultBlockState(), 2);
		}
		for (int z = 9; z <= 14; z++) {
			for (int y = 1; y <= 2; y++) level.setBlock(at(helper, 15, y, z), Blocks.STONE.defaultBlockState(), 2);
		}
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "ferric_spikes", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Vec3 beyondTrench = Vec3.atCenterOf(at(helper, 10, 0, 4)).add(0.0D, 0.5D, 0.0D);
		Vec3 beyondWall = Vec3.atCenterOf(at(helper, 19, 0, 10)).add(0.0D, 0.5D, 0.0D);
		Vec3[] atTrench = new Vec3[1];
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 60) return;
			aim(player, stand, t <= 25 ? beyondTrench : beyondWall);
			project(player);
			if (t == 25) atTrench[0] = com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRunSculpting.at(player);
		});
		helper.runAtTickTime(63, () -> {
			try {
				double trench = at(helper, 0, 0, 8).getZ();
				helper.assertTrue(atTrench[0] != null && atTrench[0].z > trench + 1.0D, "the run crossed the trench: " + atTrench[0]);
				Vec3 run = com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRunSculpting.at(player);
				double wall = at(helper, 15, 0, 0).getX();
				helper.assertTrue(run != null && run.x < wall && run.x > wall - 1.5D, "the run did not turn and stop at the wall: " + run);
			} finally {
				SculptingSessions.end(player, true);
				captured.close();
			}
			helper.succeed();
		});
	}

	/** The attack button stands the run up as blades where it is, and the run is spent. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void ferricSpikesEruptsIntoStandingBlades(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "ferric_spikes", 4_000.0D);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			aim(player, stand, Vec3.atCenterOf(at(helper, 10, 0, 6)).add(0.0D, 0.5D, 0.0D));
			var sculpting = SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting();
			helper.assertTrue(!sculpting.accent(player), "blades erupted from no run");
			for (int i = 0; i < 8; i++) project(player);
			Vec3 run = com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRunSculpting.at(player);
			helper.assertTrue(run != null, "no run started");
			double before = blood(player);
			helper.assertTrue(sculpting.accent(player), "the eruption was refused");
			helper.assertTrue(blood(player) == before, "the eruption cost blood beyond the run's upkeep");
			var blades = helper.getLevel().getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike.class,
					new net.minecraft.world.phys.AABB(run, run).inflate(2.0D));
			helper.assertTrue(blades.size() == com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRunRules.ERUPTION_BLADES,
					"the eruption raised " + blades.size() + " blades");
			helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRunSculpting.at(player) == null
					&& !SculptingSessions.isActive(player.getUUID()), "the run outlived its eruption");
			double spent = blood(player);
			project(player);
			helper.assertTrue(blood(player) == spent, "a new run started at once after an eruption");
			blades.forEach(net.minecraft.world.entity.Entity::discard);
		}
		helper.succeed();
	}

	// ── Glacial Grasp: a hand of ice that closes ──

	/** Held on a walking creature, the hand forms over a second and then holds it where it stands, driving Rime in. */
	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void glacialGraspHoldsACreatureOnceTheHandHasFormed(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "glacial_grasp", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk husk = dummy(helper, feet(helper, 10.5D, 8.5D), 300.0D);
		Vec3[] at = new Vec3[2];
		boolean[] rimedEarly = {false};
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 70) return;
			// The caster looks away once the hand is on: it keeps its creature.
			if (t < 5) aim(player, stand, husk.getBoundingBox().getCenter());
			else face(player, stand, 90.0F, 0.0F);
			project(player);
			if (t == 12) rimedEarly[0] = SchoolStates.has(husk, SchoolState.RIME);
			if (t == 30) at[0] = husk.position();
			// Shoved while held.
			if (t > 30 && t < 70) husk.setDeltaMovement(0.4D, 0.0D, 0.0D);
			if (t == 70) at[1] = husk.position();
		});
		helper.runAtTickTime(74, () -> {
			try {
				helper.assertTrue(!rimedEarly[0], "the hand drove Rime in before it had formed");
				helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialGraspSculpting.gripped(player) == husk,
						"the hand let go when the caster looked away");
				helper.assertTrue(SchoolStates.has(husk, SchoolState.RIME), "a held creature took no Rime");
				helper.assertTrue(at[0].distanceTo(at[1]) < 0.6D, "a held creature was shoved " + at[0].distanceTo(at[1]) + " blocks");
				helper.assertTrue(husk.getHealth() == 300.0F, "the hand wounded before it squeezed");
				helper.assertTrue(blood(player) < 4_000.0D, "the hand held for free");
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** The squeeze: nothing from an unformed hand, more the longer it held, then the hand is gone and spent a moment. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void glacialGraspSqueezesHarderTheLongerItHeld(GameTestHelper helper) {
		clear(helper);
		float[] drop = new float[2];
		for (int longHold = 0; longHold <= 1; longHold++) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, "glacial_grasp", 4_000.0D);
				Vec3 stand = feet(helper, 4.5D + longHold * 10.0D, 12.5D);
				Husk husk = dummy(helper, stand.subtract(0.0D, 0.0D, 4.0D), 300.0D);
				try {
					aim(player, stand, husk.getBoundingBox().getCenter());
					var sculpting = SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting();
					for (int i = 0; i < 10; i++) project(player);
					helper.assertTrue(!sculpting.accent(player) && husk.getHealth() == 300.0F, "an unformed hand squeezed");
					for (int i = 0; i < (longHold == 1 ? 90 : 12); i++) project(player);
					helper.assertTrue(sculpting.accent(player), "the squeeze was refused");
					drop[longHold] = 300.0F - husk.getHealth();
					helper.assertTrue(SchoolStates.has(husk, SchoolState.RIME), "the squeeze drove no Rime in");
					helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialGraspSculpting.gripped(player) == null
							&& !SculptingSessions.isActive(player.getUUID()), "the hand outlived its squeeze");
					double spent = blood(player);
					project(player);
					helper.assertTrue(blood(player) == spent, "a new hand formed at once after a squeeze");
				} finally {
					SculptingSessions.end(player, true);
					husk.discard();
				}
			}
		}
		helper.assertTrue(drop[0] > 0.0F && drop[1] > drop[0] * 2.5F,
				"a long hold did not squeeze much harder: " + drop[0] + " against " + drop[1]);
		helper.succeed();
	}

	/** The hand takes no ally, and lets go of a creature carried out of reach. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void glacialGraspSparesAnAllyAndLetsGoOutOfReach(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "glacial_grasp", 4_000.0D);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			Wolf wolf = still(helper, EntityType.WOLF, feet(helper, 10.5D, 9.5D));
			wolf.tame(player);
			aim(player, stand, wolf.getBoundingBox().getCenter());
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "the hand claims the caster's own wolf");
			wolf.discard();
			Husk husk = dummy(helper, feet(helper, 10.5D, 9.5D), 300.0D);
			aim(player, stand, husk.getBoundingBox().getCenter());
			for (int i = 0; i < 30; i++) project(player);
			helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialGraspSculpting.gripped(player) == husk, "the hand did not take the husk");
			husk.teleportTo(stand.x, stand.y, stand.z - 30.0D);
			project(player);
			helper.assertTrue(!SculptingSessions.isActive(player.getUUID()), "the hand held a creature thirty blocks off");
			husk.discard();
		}
		helper.succeed();
	}

	/** A boss in the hand is slowed, never held. */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void glacialGraspOnlySlowsABoss(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "glacial_grasp", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		ElderGuardian boss = still(helper, EntityType.ELDER_GUARDIAN, feet(helper, 10.5D, 8.5D));
		int[] tick = {0};
		boolean[] moving = {false};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 40) return;
			aim(player, stand, boss.getBoundingBox().getCenter());
			if (t == 40) boss.setDeltaMovement(0.3D, 0.0D, 0.0D);
			project(player);
			if (t == 40) moving[0] = boss.getDeltaMovement().x > 0.0D;
		});
		helper.runAtTickTime(42, () -> {
			try {
				helper.assertTrue(moving[0], "a boss was held still");
				var slow = boss.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
				helper.assertTrue(slow != null && slow.getAmplifier() == com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialGraspRules.BOSS_SLOW_AMPLIFIER,
						"a boss in the hand is not merely slowed: " + slow);
			} finally {
				SculptingSessions.end(player, true);
				boss.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** The jet kit's own cases run on a Lux memory with no held form of its own, as {@link #plantMemory} does for the plant. */
	private static String jetMemory() {
		BloodManipulation memory = ManipulationInit.getByName("radiant_wake");
		if (memory.getSculpting(Verb.PROJECT).isEmpty()) {
			var jet = com.vincenthuto.hemomancy.common.manipulation.verb.strand.Strands.jet(20.0D, 1.0F);
			memory.setProjectionSculpting(() -> jet);
		}
		return "radiant_wake";
	}

	/** The spray kit's own cases run on a Ductilis memory with no held form of its own. */
	private static String sprayMemory() {
		BloodManipulation memory = ManipulationInit.getByName("activation_potential");
		if (memory.getSculpting(Verb.PROJECT).isEmpty()) {
			var spray = com.vincenthuto.hemomancy.common.manipulation.verb.strand.Strands.spray(20.0D, 1.0F);
			memory.setProjectionSculpting(() -> spray);
		}
		return "activation_potential";
	}

	// ── Hematic Flare: a searchlight ──

	/** The beam Illuminates and outlines what it crosses without wounding it, and turns with the caster. */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void hematicFlareSweepsABeamThatIlluminates(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "hematic_flare", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk ahead = dummy(helper, feet(helper, 10.5D, 5.5D), 300.0D);
		Husk aside = dummy(helper, feet(helper, 17.5D, 12.5D), 300.0D);
		Husk behind = dummy(helper, feet(helper, 3.5D, 12.5D), 300.0D);
		boolean[] asideEarly = {false};
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 30) return;
			// North for a second, then swept round to the east.
			face(player, stand, t < 20 ? 180.0F : 270.0F, 0.0F);
			project(player);
			if (t == 19) asideEarly[0] = SchoolStates.has(aside, SchoolState.ILLUMINATED);
		});
		helper.runAtTickTime(34, () -> {
			try {
				helper.assertTrue(SchoolStates.has(ahead, SchoolState.ILLUMINATED), "the beam did not Illuminate the husk it rested on");
				helper.assertTrue(ahead.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING), "a body the beam crossed is not outlined");
				helper.assertTrue(!asideEarly[0], "the beam lit a husk it had not turned to");
				helper.assertTrue(SchoolStates.has(aside, SchoolState.ILLUMINATED), "the swept beam did not light the husk to the east");
				helper.assertTrue(!SchoolStates.has(behind, SchoolState.ILLUMINATED), "the beam lit a husk it never crossed");
				helper.assertTrue(ahead.getHealth() == 300.0F && aside.getHealth() == 300.0F, "the beam itself wounded");
				helper.assertTrue(blood(player) < 4_000.0D, "the beam was held for free");
			} finally {
				SculptingSessions.end(player, true);
				ahead.discard();
				aside.discard();
				behind.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** Held steady on one body the beam builds heat; the attack button spends it as a flash that wounds and blinds. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void hematicFlareFlashesTheBodyItHasHeated(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "hematic_flare", 4_000.0D);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			Husk husk = dummy(helper, feet(helper, 10.5D, 6.5D), 300.0D);
			try {
				aim(player, stand, husk.getBoundingBox().getCenter());
				var sculpting = SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting();
				for (int i = 0; i < 5; i++) project(player);
				helper.assertTrue(!sculpting.accent(player) && husk.getHealth() == 300.0F, "a cold beam flashed");
				for (int i = 0; i < 60; i++) project(player);
				helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.lux.FlareBeamSculpting.heat(player) == 1.0D,
						"three seconds on one body did not bring the beam to full heat");
				helper.assertTrue(sculpting.accent(player), "the flash was refused");
				helper.assertTrue(husk.getHealth() < 300.0F, "the flash did not wound");
				helper.assertTrue(husk.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS), "the flash did not blind");
				helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.lux.FlareBeamSculpting.heat(player) == 0.0D, "the flash kept its heat");
				helper.assertTrue(SculptingSessions.isActive(player.getUUID()), "the flash put the beam out");
				helper.assertTrue(!sculpting.accent(player), "a second flash came at once");
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
			}
		}
		helper.succeed();
	}

	// ── Synaptic Jolt: live conductors ──

	/**
	 * Held on water the jolt claims the pool as a conductor and pays to keep it live; bare stone is not claimed, and a
	 * puddle is struck through to from the floor under it. What a live conductor then does to a body standing in it is
	 * the conduction system's own work, which finds its owner in the level's entity storage, where a captured test
	 * player never is; that half is the R cast's and is not re-proved here.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void synapticJoltClaimsAConductorAndPaysToKeepItLive(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		for (int x = 8; x <= 12; x++) {
			for (int z = 5; z <= 9; z++) {
				level.setBlock(at(helper, x, -1, z), Blocks.STONE.defaultBlockState(), 2);
				level.setBlock(at(helper, x, 0, z), Blocks.WATER.defaultBlockState(), 2);
			}
		}
		level.setBlock(at(helper, 15, 1, 9), Blocks.IRON_BLOCK.defaultBlockState(), 2);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "synaptic_jolt", 4_000.0D);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			aim(player, stand, Vec3.atCenterOf(at(helper, 6, 0, 12)).add(0.0D, 0.5D, 0.0D));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "the jolt claims bare stone");
			aim(player, stand, Vec3.atCenterOf(at(helper, 15, 1, 9)));
			var iron = SculptingDispatcher.currentClaim(player, Verb.PROJECT).orElse(null);
			helper.assertTrue(iron != null && iron.selected().sculpting() instanceof com.vincenthuto.hemomancy.common.manipulation.ductilis.SynapticLiveSculpting,
					"the jolt does not claim an iron block: " + iron);
			aim(player, stand, Vec3.atCenterOf(at(helper, 10, 0, 9)).add(0.0D, 0.4D, 0.0D));
			var water = SculptingDispatcher.currentClaim(player, Verb.PROJECT).orElse(null);
			helper.assertTrue(water != null && water.target() instanceof com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget.Block block
							&& com.vincenthuto.hemomancy.common.manipulation.ductilis.SynapticLiveSculpting.conductor(level, block) != null,
					"the jolt does not claim the pool: " + water);
			double before = blood(player);
			project(player);
			helper.assertTrue(blood(player) < before, "keeping the pool live cost nothing");
			SculptingSessions.end(player, true);
		} finally {
			level.setBlock(at(helper, 15, 1, 9), Blocks.AIR.defaultBlockState(), 2);
		}
		helper.succeed();
	}

	/** Held on a creature it jolts it each pulse, as the caster's Ductilis hit, and Disrupts it. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void synapticJoltJoltsACreatureEachPulse(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "synaptic_jolt", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk husk = dummy(helper, feet(helper, 10.5D, 9.5D), 300.0D);
		Wolf wolf = still(helper, EntityType.WOLF, feet(helper, 13.5D, 9.5D));
		wolf.tame(player);
		aim(player, stand, wolf.getBoundingBox().getCenter());
		helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "the jolt claims the caster's own wolf");
		List<Integer> hits = new ArrayList<>();
		float[] last = {300.0F};
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 32) return;
			aim(player, stand, husk.getBoundingBox().getCenter());
			project(player);
			if (husk.getHealth() < last[0]) {
				hits.add(t);
				last[0] = husk.getHealth();
			}
		});
		helper.runAtTickTime(36, () -> {
			try {
				helper.assertTrue(hits.size() >= 3, "three seconds of jolting landed " + hits.size() + " hits");
				helper.assertTrue(husk.getLastHurtByMob() == player && husk.getLastDamageSource() instanceof SchoolDamageSource school
								&& school.context().primary() == EnumBloodTemper.DUCTILIS, "the jolt is not the caster's Ductilis hit");
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
				wolf.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── Blood Needle Lance: a hovering volley ──

	private static List<com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity> needles(GameTestHelper helper, Vec3 near) {
		return helper.getLevel().getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity.class,
				new net.minecraft.world.phys.AABB(near, near).inflate(40.0D));
	}

	/** A needle forms each paid pulse up to six; a full volley hovers for nothing; letting go looses them all. */
	@GameTest(template = EMPTY, timeoutTicks = 140)
	public static void bloodNeedleLanceGathersAVolleyAndLoosesItOnRelease(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "blood_needle_lance", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk husk = dummy(helper, feet(helper, 10.5D, 6.5D), 300.0D);
		int[] count = new int[3];
		double[] blood = new double[2];
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 90) return;
			if (t < 90) {
				aim(player, stand, husk.getBoundingBox().getCenter());
				project(player);
			}
			if (t == 25) count[0] = com.vincenthuto.hemomancy.common.manipulation.ferric.NeedleVolleySculpting.hovering(player);
			if (t == 65) {
				count[1] = com.vincenthuto.hemomancy.common.manipulation.ferric.NeedleVolleySculpting.hovering(player);
				blood[0] = blood(player);
			}
			if (t == 89) {
				count[2] = com.vincenthuto.hemomancy.common.manipulation.ferric.NeedleVolleySculpting.hovering(player);
				blood[1] = blood(player);
			}
			if (t == 90) SculptingSessions.end(player, true);
		});
		helper.runAtTickTime(100, () -> {
			try {
				helper.assertTrue(count[0] == 3, "twenty-five ticks formed " + count[0] + " needles, not three");
				helper.assertTrue(count[1] == 6 && count[2] == 6, "the volley did not fill to six and stop: " + count[1] + ", " + count[2]);
				helper.assertTrue(blood[0] == blood[1], "a full volley cost " + (blood[0] - blood[1]) + " mL to hold");
				helper.assertTrue(husk.getHealth() < 300.0F, "the loosed volley did not strike the husk it was aimed at");
				helper.assertTrue(husk.getLastHurtByMob() == player, "the volley's hit is not the caster's");
				helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.ferric.NeedleVolleySculpting.hovering(player) == 0, "needles still hover after the release");
			} finally {
				SculptingSessions.end(player, true);
				husk.discard();
				needles(helper, stand).forEach(net.minecraft.world.entity.Entity::discard);
				captured.close();
			}
			helper.succeed();
		});
	}

	/** The attack button looses one needle early and keeps the rest; with none formed it does nothing. */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void bloodNeedleLanceLoosesOneNeedleOnTheAttackButton(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "blood_needle_lance", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		int[] tick = {0};
		String[] failure = {null};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 35) return;
			face(player, stand, 180.0F, -30.0F);
			var sculpting = SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting();
			if (t == 0 && sculpting.accent(player)) failure[0] = "a needle was loosed from no volley";
			project(player);
			if (t != 35) return;
			int before = com.vincenthuto.hemomancy.common.manipulation.ferric.NeedleVolleySculpting.hovering(player);
			int flying = needles(helper, stand).size();
			if (!sculpting.accent(player)) failure[0] = "the attack button loosed nothing";
			else if (com.vincenthuto.hemomancy.common.manipulation.ferric.NeedleVolleySculpting.hovering(player) != before - 1) failure[0] = "one click did not take one needle";
			else if (needles(helper, stand).size() != flying + 1) failure[0] = "one click did not loose one needle";
			else if (!SculptingSessions.isActive(player.getUUID())) failure[0] = "one needle ended the volley";
		});
		helper.runAtTickTime(38, () -> {
			try {
				helper.assertTrue(failure[0] == null, failure[0]);
			} finally {
				HOVER_CLEAR.run();
				SculptingSessions.end(player, true);
				needles(helper, stand).forEach(net.minecraft.world.entity.Entity::discard);
				captured.close();
			}
			helper.succeed();
		});
	}

	/** Drops any volley left hovering so a test's own release looses nothing into the next test's room. */
	private static final Runnable HOVER_CLEAR = com.vincenthuto.hemomancy.common.manipulation.ferric.NeedleVolleySculpting::clearSessionState;

	// ── Crimson Flame Conjuration: painted fire ──

	/** Flame is laid where the aim rests as it moves; each goes out by itself; the caster's edit, so it needs footing. */
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void crimsonFlamePaintsALineThatBurnsOut(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "crimson_flame_conjuration", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		int[] tick = {0};
		int[] painted = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > 12) return;
			// The aim walks along the floor from x 8 to x 12 at z 8.
			aim(player, stand, Vec3.atCenterOf(at(helper, 8 + Math.min(4, t / 2), 0, 8)).add(0.0D, 0.5D, 0.0D));
			project(player);
			if (t == 12) {
				painted[0] = com.vincenthuto.hemomancy.common.manipulation.flammeus.FlamePaintSculpting.standing(player);
				SculptingSessions.end(player, true);
			}
		});
		helper.runAtTickTime(14, () -> {
			int burning = 0;
			for (int x = 8; x <= 12; x++) if (helper.getLevel().getBlockState(at(helper, x, 1, 8)).is(BlockInit.crimson_flames.get())) burning++;
			helper.assertTrue(painted[0] == 5 && burning == 5, "five cells of aim painted " + painted[0] + " flames, " + burning + " burning");
		});
		helper.runAtTickTime(14 + com.vincenthuto.hemomancy.common.manipulation.flammeus.FlamePaintSculpting.FLAME_TICKS + 6, () -> {
			try {
				for (int x = 8; x <= 12; x++) {
					helper.assertTrue(!helper.getLevel().getBlockState(at(helper, x, 1, 8)).is(BlockInit.crimson_flames.get()), "a painted flame outlived its time at x " + x);
				}
				helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.flammeus.FlamePaintSculpting.standing(player) == 0, "burnt-out flames are still counted");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	/** The attack button flares the whole line: what stands in a flame is struck and seared, and the line is spent. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void crimsonFlameFlaresItsWholeLine(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "crimson_flame_conjuration", 4_000.0D);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			Husk outside = dummy(helper, feet(helper, 16.5D, 8.5D), 300.0D);
			try {
				var sculpting = SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting();
				for (int x = 9; x <= 11; x++) {
					aim(player, stand, Vec3.atCenterOf(at(helper, x, 0, 8)).add(0.0D, 0.5D, 0.0D));
					project(player);
				}
				helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.flammeus.FlamePaintSculpting.standing(player) == 3, "three cells did not take three flames");
				// It steps into the fire.
				Husk inFlame = dummy(helper, feet(helper, 10.5D, 8.5D), 300.0D);
				inFlame.setRemainingFireTicks(0);
				float before = inFlame.getHealth();
				helper.assertTrue(sculpting.accent(player), "the flare was refused");
				helper.assertTrue(before - inFlame.getHealth() >= com.vincenthuto.hemomancy.common.manipulation.flammeus.FlamePaintSculpting.FLARE_DAMAGE * 0.9F,
						"the flare took " + (before - inFlame.getHealth()) + " off the husk standing in it");
				helper.assertTrue(SchoolStates.has(inFlame, SchoolState.SEARING), "the flare did not sear");
				helper.assertTrue(outside.getHealth() == 300.0F, "the flare struck a husk six blocks from the line");
				for (int x = 9; x <= 11; x++) {
					helper.assertTrue(!helper.getLevel().getBlockState(at(helper, x, 1, 8)).is(BlockInit.crimson_flames.get()), "the flare left a flame standing");
				}
				helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.flammeus.FlamePaintSculpting.standing(player) == 0
						&& !sculpting.accent(player), "a spent line flared again");
				inFlame.discard();
			} finally {
				SculptingSessions.end(player, true);
				outside.discard();
			}
		}
		helper.succeed();
	}

	/** The six memories whose held form only repeated their R cast hold no strand: Projection with one selected is plain Projection. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theCutMemoriesHoldNoStrand(GameTestHelper helper) {
		clear(helper);
		for (String name : List.of("blood_needle_fan", "prismatic_reproof", "hemorrhage", "conductive_mark", "insatiable_hunger",
				"grave_debt")) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, name, 2_000.0D);
				helper.assertTrue(ManipulationInit.getByName(name).getSculpting(Verb.PROJECT).isEmpty(), name + " still sculpts Projection");
				face(player, feet(helper, 10.5D, 12.5D), 180.0F, -20.0F);
				project(player);
				helper.assertTrue(!StrandService.isOpen(player), name + " still puts out a strand");
				helper.assertTrue(blood(player) == 2_000.0D, name + " spent blood on open air under held Projection");
			}
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aBrandMarksACreatureThatAbsorptionDrawsFromAtRange(GameTestHelper helper) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, brandMemory(), 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Pig branded = still(helper, EntityType.PIG, feet(helper, 10.5D, 9.5D));
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ >= BrandRulesHolder.SET + 4) return;
			aim(player, stand, branded.getBoundingBox().getCenter());
			project(player);
		});
		helper.runAtTickTime(BrandRulesHolder.SET + 8, () -> {
			Pig near = null;
			try {
				StrandBrand brand = StrandService.brandFor(branded, player.getUUID());
				helper.assertTrue(brand != null && brand.school() == EnumBloodTemper.MORTEM, "the brand did not take");
				helper.assertTrue(SchoolStates.has(branded, SchoolState.NECROSIS), "the brand laid no Necrosis");
				SculptingSessions.end(player, true);
				// Step back past plain reach, with an unmarked pig close by: the brand is still drawn first.
				Vec3 far = feet(helper, 10.5D, 9.5D + 9.0D);
				player.setPos(far.x, far.y, far.z);
				near = still(helper, EntityType.PIG, feet(helper, 10.5D, 9.5D + 7.5D));
				double reach = com.vincenthuto.hemomancy.common.manipulation.verb.VerbReach.of(player, Verb.ABSORB);
				helper.assertTrue(player.distanceTo(branded) > reach, "the fixture stands within plain reach");
				LivingEntity drawn = BloodAbsorptionItem.findBareAbsorptionTarget(player, reach).orElse(null);
				helper.assertTrue(drawn == branded, "Absorption chose " + drawn + " over the branded pig");
				float before = branded.getHealth();
				helper.assertTrue(BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, branded, 1.0D) > 0.0D
						&& branded.getHealth() < before, "the branded pig could not be drawn at range");
			} finally {
				branded.discard();
				if (near != null) near.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/** The brand's hold time, readable in an annotation-free way. */
	private static final class BrandRulesHolder {
		static final int SET = com.vincenthuto.hemomancy.common.manipulation.verb.strand.BrandRules.SET_TICKS;
	}

	// ── Absorption's owner and riders (CL-24, CL-26) ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aDrawNamesTheDrawerAndAKillByDrawIsCreditedWithoutABounty(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "blood_shot", 1_000.0D);
			Pig pig = still(helper, EntityType.PIG, feet(helper, 10.5D, 9.5D));
			pig.setHealth(0.5F);
			double before = blood(player);
			double absorbed = BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, pig, 2.0D);
			helper.assertTrue(absorbed > 0.0D && !pig.isAlive(), "the draw did not kill the pig");
			helper.assertTrue(pig.getKillCredit() == player, "the kill by draw was not credited to the drawer");
			helper.assertTrue(pig.getLastDamageSource() != null && pig.getLastDamageSource().is(HemoDamageTypes.BLOOD_ABSORPTION)
					&& pig.getLastDamageSource().getEntity() == player, "the draw's damage did not name the drawer");
			helper.assertTrue(Math.abs(blood(player) - before - absorbed) < 1.0E-6D,
					"a kill by draw paid a kill bounty on top of the draw: " + (blood(player) - before) + " for " + absorbed);
			Pig angry = still(helper, EntityType.PIG, feet(helper, 12.5D, 9.5D));
			BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, angry, 1.0D);
			helper.assertTrue(angry.getLastHurtByMob() == player, "a drained creature does not know who drains it");
			angry.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void absorptionRidesTheSchoolStateOnTheCreature(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "blood_shot", 1_000.0D);
			HemoCapabilityAccess.getBloodVolume(player).setMaxBloodVolume(5_000.0D);
			Vec3 stand = feet(helper, 10.5D, 12.5D);
			player.setPos(stand.x, stand.y, stand.z);
			Pig plain = still(helper, EntityType.PIG, feet(helper, 6.5D, 9.5D));
			Pig pressed = still(helper, EntityType.PIG, feet(helper, 10.5D, 9.5D));
			Pig searing = still(helper, EntityType.PIG, feet(helper, 14.5D, 9.5D));
			Pig obscured = still(helper, EntityType.PIG, feet(helper, 10.5D, 6.5D));
			SchoolStates.apply(player, pressed, SchoolState.PRESSURE, 200);
			SchoolStates.apply(player, searing, SchoolState.SEARING, 200);
			SchoolStates.apply(player, obscured, SchoolState.OBSCURED, 200);
			helper.assertTrue(SchoolStates.has(pressed, SchoolState.PRESSURE) && SchoolStates.has(searing, SchoolState.SEARING)
					&& SchoolStates.has(obscured, SchoolState.OBSCURED), "the fixture states did not take");

			double plainBack = drawn(helper, player, plain);
			double pressedBack = drawn(helper, player, pressed);
			helper.assertTrue(Math.abs(pressedBack - plainBack * 1.25D) < 1.0E-4D,
					"Pressure gave back " + pressedBack + " against a plain " + plainBack);
			helper.assertTrue(pressed.getDeltaMovement().z > 0.0D, "Pressure did not pull the pig toward the drawer: "
					+ pressed.getDeltaMovement() + " pig at " + pressed.position() + " player at " + player.position());

			float before = searing.getHealth();
			double searingBack = drawn(helper, player, searing);
			helper.assertTrue(before - searing.getHealth() > 1.4F, "Searing did not draw harder: " + (before - searing.getHealth()));
			helper.assertTrue(searingBack < (before - searing.getHealth()), "Searing gave back all it drew");

			drawn(helper, player, obscured);
			helper.assertTrue(SchoolStates.has(player, SchoolState.VEILED), "drawing an Obscured creature did not Veil the drawer");
			for (Pig pig : List.of(plain, pressed, searing, obscured)) pig.discard();
		}
		helper.succeed();
	}

	/** One draw of 1 from {@code pig}, returning the blood that came back. */
	private static double drawn(GameTestHelper helper, ServerPlayer player, Pig pig) {
		pig.invulnerableTime = 0;
		double before = blood(player);
		BloodAbsorptionItem.absorbFromTarget(helper.getLevel(), player, pig, 1.0D);
		return blood(player) - before;
	}

	// ── Pace, the new lash, and the R flick ──

	/**
	 * Each strand paces the player while it is out (CQ-10): with the memory selected and Projection held there is no
	 * declared pace until a paid first pulse opens the strand, then the form's pace holds, and letting go ends it.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void eachStrandSetsThePlayersPace(GameTestHelper helper) {
		clear(helper);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Vec3 ground = Vec3.atCenterOf(at(helper, 10, 0, 9)).add(0.0D, 0.5D, 0.0D);
		for (var pin : List.of(new Object[] {"searing_lash", 0.8D},
				new Object[] {"blood_lash", 0.8D},
				// Round two P5: the second wave.
				new Object[] {"gloam_laceration", 0.8D},
				new Object[] {jetMemory(), 0.5D}, new Object[] {sprayMemory(), 1.0D},
				new Object[] {plantMemory(), 0.6D})) {
			// A player of their own each: the pulse clock is the player's, so a second strand opened in the same tick
			// would wait out the first one's cadence.
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, (String) pin[0], 1_000.0D);
				// A plant opens on a block; every other form opens at open air.
				var form = ((StrandSculpting) SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting()).form();
				if (form == StrandForm.PLANT) aim(player, stand, ground);
				else face(player, stand, 180.0F, -30.0F);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
				player.startUsingItem(InteractionHand.MAIN_HAND);
				helper.assertTrue(!VerbChannel.declaresMovement(player), pin[0] + " sets a pace before any strand is out");
				project(player);
				helper.assertTrue(StrandService.isOpen(player), pin[0] + " did not open where it was aimed");
				helper.assertTrue(VerbChannel.declaresMovement(player), pin[0] + " does not set the pace with its strand out");
				helper.assertTrue(VerbChannel.movementMultiplier(player) == (double) pin[1],
						pin[0] + " paces at " + VerbChannel.movementMultiplier(player));
				SculptingSessions.end(player, true);
				helper.assertTrue(!StrandService.isOpen(player) && !VerbChannel.declaresMovement(player),
						pin[0] + " kept its pace after the strand was let go");
				player.stopUsingItem();
			}
		}
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "blood_shot", 1_000.0D);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
			player.startUsingItem(InteractionHand.MAIN_HAND);
			helper.assertTrue(!VerbChannel.declaresMovement(player), "plain Projection now sets a pace");
			player.stopUsingItem();
		}
		helper.succeed();
	}

	/**
	 * CQ-10: a strand memory selected and Projection held on a reservoir the strand does not claim opens no strand, so
	 * the player moves by the plain rule (no declared pace), and the reservoir fills.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void projectionIntoAStationWithAStrandSelectedKeepsThePlainPace(GameTestHelper helper) {
		clear(helper);
		BlockPos station = at(helper, 10, 1, 9);
		helper.getLevel().setBlock(station, BlockInit.enzymatic_scriptorium.get().defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "searing_lash", 2_000.0D);
			aim(player, feet(helper, 10.5D, 12.5D), Vec3.atCenterOf(station));
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
			player.startUsingItem(InteractionHand.MAIN_HAND);
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "the lash claims the station");
			for (int i = 0; i < 3; i++) {
				project(player);
				helper.assertTrue(!VerbChannel.declaresMovement(player), "a strand's pace applied with no strand out");
			}
			helper.assertTrue(!StrandService.isOpen(player), "a strand opened on a station");
			double held = com.vincenthuto.hemomancy.common.tile.ReservoirBlood.settled(helper.getLevel().getBlockEntity(station))
					.getBloodVolume();
			helper.assertTrue(held > 0.0D, "the station did not fill under the strand manipulation");
			player.stopUsingItem();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void bloodLashIsAWeakLashWithNothingToCast(GameTestHelper helper) {
		clear(helper);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "blood_lash", 1_000.0D);
			var selected = SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow();
			helper.assertTrue(selected.sculpting() instanceof StrandSculpting strand && strand.form() == StrandForm.LASH
					&& strand.spec().damageScale() < 1.0F, "Blood Lash is not a weak lash");
			BloodManipulation lash = ManipulationInit.blood_lash.get();
			helper.assertTrue(lash.getTend() == EnumBloodTemper.ANIMUS
					&& lash.getRank() == com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.HUMILIS,
					"Blood Lash is not Animus Humilis");
			lash.tryPerformAction(player, player.level(), ItemStack.EMPTY, player.blockPosition(), 0.0F);
			helper.assertTrue(blood(player) == 1_000.0D, "Blood Lash's R cast spent blood");
			helper.assertTrue(captured.said().contains("nothing to cast"), "Blood Lash's R did not say why: " + captured.said());
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void eachStrandCostsAsPinned(GameTestHelper helper) {
		record Pin(String manipulation, StrandForm form, double bloodPerSecond) {
		}
		List<Pin> pins = List.of(new Pin("blood_lash", StrandForm.LASH, 15.0D), new Pin("searing_lash", StrandForm.LASH, 60.0D),
				// Round two P5: the second wave.
				new Pin("gloam_laceration", StrandForm.LASH, 40.0D),
				new Pin(jetMemory(), StrandForm.JET, 40.0D), new Pin(sprayMemory(), StrandForm.SPRAY, 40.0D),
				new Pin(plantMemory(), StrandForm.PLANT, 40.0D));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			for (Pin pin : pins) {
				prepare(player, pin.manipulation(), 1_000.0D);
				var sculpting = SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting();
				helper.assertTrue(sculpting instanceof StrandSculpting strand && strand.form() == pin.form()
								&& Math.abs(strand.spec().bloodPerSecond() - pin.bloodPerSecond()) < 1.0E-6D
								&& sculpting.cadence() == 10 && sculpting.holdsChannel(),
						pin.manipulation() + " drifted from its pin");
			}
		}
		helper.succeed();
	}

	// ── Core-loop round two P5: the second wave of strands ──

	/**
	 * Holds Projection with {@code manipulation} selected the way its form is worked (a lash swept, a jet or brand held on
	 * a husk, a spray fanned at one close by, a plant sunk beside one and let go), and checks the strand opened, kept its
	 * R cast, and that what it did to the husk carries the caster and the manipulation's school.
	 */
	private static void strandLands(GameTestHelper helper, String manipulation, EnumBloodTemper school) {
		clear(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper).joinLevel();
		ServerPlayer player = captured.player;
		prepare(player, manipulation, 4_000.0D);
		StrandSculpting sculpting = (StrandSculpting) SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting();
		StrandForm form = sculpting.form();
		BloodManipulation registered = ManipulationInit.getByName(manipulation);
		helper.assertTrue(registered.getTend() == school, manipulation + " is not " + school);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk husk = dummy(helper, switch (form) {
			case LASH -> feet(helper, 10.5D, 6.5D);
			case SPRAY -> feet(helper, 10.5D, 10.3D);
			case PLANT -> feet(helper, 11.5D, 9.5D);
			default -> feet(helper, 10.5D, 9.5D);
		}, 300.0D);
		int hold = switch (form) {
			case PLANT -> 25;
			case BRAND -> BrandRulesHolder.SET + 4;
			case JET -> 30;
			case SPRAY -> 12;
			case LASH -> 40;
		};
		boolean[] opened = {false};
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t < hold) {
				switch (form) {
					case LASH -> workLash(player, stand, t, 180.0F);
					case PLANT -> aim(player, stand, Vec3.atCenterOf(at(helper, 10, 0, 9)).add(0.0D, 0.5D, 0.0D));
					default -> aim(player, stand, husk.getBoundingBox().getCenter());
				}
				// A lash is worked whole by its helper, pulse and all.
				if (form != StrandForm.LASH) project(player);
				if (StrandService.isOpen(player)) opened[0] = true;
			} else if (t == hold) {
				SculptingSessions.end(player, true);
			}
		});
		helper.runAtTickTime(hold + com.vincenthuto.hemomancy.common.manipulation.verb.strand.PlantRules.ERUPTION_DELAY + 8, () -> {
			try {
				helper.assertTrue(opened[0], manipulation + " opened no " + form + " strand");
				if (form == StrandForm.BRAND) {
					StrandBrand brand = StrandService.brandFor(husk, player.getUUID());
					helper.assertTrue(brand != null && brand.school() == school, manipulation + "'s brand did not take: " + brand);
				} else {
					helper.assertTrue(husk.getHealth() < 300.0F, manipulation + "'s " + form + " never hurt the husk");
					helper.assertTrue(husk.getLastHurtByMob() == player, manipulation + "'s hit did not name the caster");
					DamageSource source = husk.getLastDamageSource();
					helper.assertTrue(source instanceof SchoolDamageSource hit && hit.context().primary() == school
									&& hit.context().ability().getPath().equals(manipulation),
							manipulation + "'s hit did not carry its school: " + source);
				}
			} finally {
				husk.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void gloamLacerationIsATenebrisLash(GameTestHelper helper) {
		strandLands(helper, "gloam_laceration", EnumBloodTemper.TENEBRIS);
	}

	/**
	 * Crimson Flame Conjuration's kindling is narrow and tried first: aimed near an unlit campfire it kindles and no
	 * flame is painted; aimed at bare stone, it paints (CQ-18).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void crimsonFlameKindlesFirstAndPaintsOnBareStone(GameTestHelper helper) {
		clear(helper);
		BlockPos campfire = at(helper, 10, 1, 9);
		helper.getLevel().setBlock(campfire, Blocks.CAMPFIRE.defaultBlockState()
				.setValue(net.minecraft.world.level.block.CampfireBlock.LIT, false), 3);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "crimson_flame_conjuration", 2_000.0D);
			aim(player, stand, Vec3.atCenterOf(campfire).add(0.0D, -0.2D, 0.0D));
			var claim = SculptingDispatcher.currentClaim(player, Verb.PROJECT).orElseThrow();
			helper.assertTrue(claim.selected().sculpting() instanceof com.vincenthuto.hemomancy.common.manipulation.flammeus.FlameKindleSculpting,
					"the painting claimed ahead of the kindling: " + claim.selected().sculpting());
			project(player);
			helper.assertTrue(helper.getLevel().getBlockState(campfire).getValue(net.minecraft.world.level.block.CampfireBlock.LIT),
					"the campfire was not kindled");
			helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.flammeus.FlamePaintSculpting.standing(player) == 0, "a flame was painted over the kindling");
			SculptingSessions.end(player, true);
		} finally {
			helper.getLevel().setBlock(campfire, Blocks.AIR.defaultBlockState(), 3);
		}
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "crimson_flame_conjuration", 2_000.0D);
			aim(player, stand, Vec3.atCenterOf(at(helper, 10, 0, 8)).add(0.0D, 0.5D, 0.0D));
			var claim = SculptingDispatcher.currentClaim(player, Verb.PROJECT).orElseThrow();
			helper.assertTrue(claim.selected().sculpting() instanceof com.vincenthuto.hemomancy.common.manipulation.flammeus.FlamePaintSculpting,
					"bare stone was not the painting's: " + claim.selected().sculpting());
			project(player);
			helper.assertTrue(helper.getLevel().getBlockState(at(helper, 10, 1, 8)).is(BlockInit.crimson_flames.get()), "no flame was painted on bare stone");
			SculptingSessions.end(player, true);
			helper.getLevel().setBlock(at(helper, 10, 1, 8), Blocks.AIR.defaultBlockState(), 3);
		}
		helper.succeed();
	}

	// ── The strand skill line ──

	private static void skill(ServerPlayer player, com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint skill,
			int level) {
		HemoCapabilityAccess.getSkillProgress(player).setSkill(skill,
				com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates.UNLOCKED, level);
	}

	/** Long Strand lengthens the strand and the pick it opens on; Free Hand gives back pace. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void longStrandAndFreeHandChangeLengthAndPace(GameTestHelper helper) {
		clear(helper);
		double[] lengths = new double[2], reaches = new double[2], paces = new double[2];
		for (int level = 0; level <= 1; level++) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, jetMemory(), 2_000.0D);
				skill(player, SkillPointInit.skill_long_strand, level * 3);
				skill(player, SkillPointInit.skill_free_hand, level * 3);
				face(player, feet(helper, 10.5D, 12.5D), 180.0F, -30.0F);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
				player.startUsingItem(InteractionHand.MAIN_HAND);
				var sculpting = SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting();
				reaches[level] = SculptingDispatcher.reach(player, Verb.PROJECT, sculpting);
				project(player);
				helper.assertTrue(StrandService.isOpen(player), "the jet did not open");
				lengths[level] = StrandService.length(player, StrandService.current(player));
				paces[level] = VerbChannel.movementMultiplier(player);
				SculptingSessions.end(player, true);
				player.stopUsingItem();
			}
		}
		helper.assertTrue(Math.abs(lengths[1] / lengths[0] - 1.3D) < 1.0E-6D,
				"Long Strand 3 did not lengthen the jet by 30%: " + lengths[0] + " to " + lengths[1]);
		helper.assertTrue(Math.abs(reaches[1] / reaches[0] - 1.3D) < 1.0E-6D,
				"Long Strand 3 did not reach the pick 30% further: " + reaches[0] + " to " + reaches[1]);
		helper.assertTrue(paces[0] == 0.5D && Math.abs(paces[1] - 0.8D) < 1.0E-9D,
				"Free Hand 3 did not raise the jet's pace from 0.5 to 0.8: " + paces[0] + ", " + paces[1]);
		helper.succeed();
	}

	/** Taut Line fills a jet's pressure faster; held the same number of ticks, the trained jet is fuller. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void tautLineFillsTheJetFaster(GameTestHelper helper) {
		clear(helper);
		double[] pressure = new double[2];
		Husk husk = dummy(helper, feet(helper, 10.5D, 9.5D), 300.0D);
		try {
			for (int level = 0; level <= 1; level++) {
				try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
					ServerPlayer player = captured.player;
					prepare(player, jetMemory(), 2_000.0D);
					skill(player, SkillPointInit.skill_taut_line, level * 2);
					aim(player, feet(helper, 10.5D, 12.5D), husk.getBoundingBox().getCenter());
					// Each pass of the held chain is one tick of the jet's hold: twenty of them, on one husk.
					for (int i = 0; i < 20; i++) project(player);
					pressure[level] = StrandService.current(player).jetPressure();
					SculptingSessions.end(player, true);
				}
			}
		} finally {
			husk.discard();
		}
		helper.assertTrue(Math.abs(pressure[1] / pressure[0] - 1.5D) < 0.02D,
				"Taut Line 2 did not fill the jet half again as fast: " + pressure[0] + " against " + pressure[1]);
		helper.succeed();
	}

	/** Whipcrack: a crack at full Whipcrack hits 45% harder and lays its Searing 60% longer. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void whipcrackHardensAndLengthensTheCrack(GameTestHelper helper) {
		clear(helper);
		float[] drop = new float[2];
		int[] searing = new int[2];
		for (int level = 0; level <= 1; level++) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, "searing_lash", 4_000.0D);
				skill(player, SkillPointInit.skill_whipcrack, level * 3);
				face(player, feet(helper, 4.5D + level * 10.0D, 12.5D), 180.0F, 0.0F);
				for (int i = 0; i < 30; i++) project(player);
				helper.assertTrue(StrandService.isOpen(player), "the lash did not open");
				double length = StrandService.length(player, StrandService.current(player));
				Vec3 tip = StrandCue.hand(player.getEyePosition(), player.getLookAngle(), player.getYRot(), false)
						.add(player.getLookAngle().scale(length));
				Husk husk = dummy(helper, tip.subtract(0, 1.0D, 0), 300.0D);
				try {
					helper.assertTrue(StrandService.snap(player), "the snap was refused");
					drop[level] = 300.0F - husk.getHealth();
					var effect = husk.getEffect(SchoolState.SEARING.effect());
					searing[level] = effect == null ? 0 : effect.getDuration();
				} finally {
					SculptingSessions.end(player, true);
					husk.discard();
				}
			}
		}
		helper.assertTrue(drop[0] > 0.0F && Math.abs(drop[1] / drop[0] - 1.45F) < 0.05F,
				"Whipcrack 3 did not crack 45% harder: " + drop[0] + " against " + drop[1]);
		helper.assertTrue(searing[0] > 0 && Math.abs(searing[1] / (double) searing[0] - 1.6D) < 0.05D,
				"Whipcrack 3 did not lay Searing 60% longer: " + searing[0] + " against " + searing[1]);
		helper.succeed();
	}

	/** The eight held manipulations that are not strands, by the memory that holds each. */
	private static final List<String> HELD_MANIPULATIONS = List.of("hematic_flare", "blood_eclipse", "glacial_grasp", "cruor_javelin",
			"blood_needle_lance", "synaptic_jolt", "crimson_flame_conjuration", "ferric_spikes");

	/**
	 * CQ-20 B: Long Strand reaches the eight held manipulations a tenth further a level, and Free Hand gives back a tenth
	 * of walking pace a level to the seven that set their own (Synaptic Jolt sets none).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void longStrandAndFreeHandTrainTheHeldManipulations(GameTestHelper helper) {
		clear(helper);
		for (String memory : HELD_MANIPULATIONS) {
			double[] reaches = new double[2];
			java.util.OptionalDouble[] paces = new java.util.OptionalDouble[2];
			for (int level = 0; level <= 1; level++) {
				try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
					ServerPlayer player = captured.player;
					prepare(player, memory, 2_000.0D);
					skill(player, SkillPointInit.skill_long_strand, level * 3);
					skill(player, SkillPointInit.skill_free_hand, level * 3);
					var sculpting = ManipulationInit.getByName(memory).getSculpting(Verb.PROJECT).orElseThrow();
					reaches[level] = SculptingDispatcher.reach(player, Verb.PROJECT, sculpting);
					paces[level] = sculpting.movement(player);
				}
			}
			helper.assertTrue(Math.abs(reaches[1] / reaches[0] - 1.3D) < 1.0E-6D,
					"Long Strand 3 did not reach " + memory + " 30% further: " + reaches[0] + " to " + reaches[1]);
			if (memory.equals("synaptic_jolt")) {
				helper.assertTrue(paces[0].isEmpty() && paces[1].isEmpty(), "Synaptic Jolt gained a pace of its own: " + paces[1]);
				continue;
			}
			double expected = Math.min(1.0D, paces[0].orElseThrow() + 0.3D);
			helper.assertTrue(Math.abs(paces[1].orElseThrow() - expected) < 1.0E-9D,
					"Free Hand 3 did not raise " + memory + "'s pace from " + paces[0].orElseThrow() + " to " + expected + ": " + paces[1]);
		}
		helper.succeed();
	}

	/** CQ-20 B: Whipcrack hardens the held manipulations' attack-button hit; a clicked lance needle hits 45% harder at 3. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void whipcrackHardensTheClickedLanceNeedle(GameTestHelper helper) {
		clear(helper);
		double[] damage = new double[2];
		for (int level = 0; level <= 1; level++) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, "blood_needle_lance", 4_000.0D);
				skill(player, SkillPointInit.skill_whipcrack, level * 3);
				face(player, feet(helper, 4.5D + level * 10.0D, 12.5D), 180.0F, -10.0F);
				for (int i = 0; i < 40; i++) project(player);
				helper.assertTrue(com.vincenthuto.hemomancy.common.manipulation.ferric.NeedleVolleySculpting.hovering(player) > 0,
						"no needle gathered");
				var sculpting = ManipulationInit.getByName("blood_needle_lance").getSculpting(Verb.PROJECT).orElseThrow();
				AABB around = player.getBoundingBox().inflate(4.0D);
				List<com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity> before = helper.getLevel()
						.getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity.class, around);
				helper.assertTrue(sculpting.accent(player), "the click loosed no needle");
				var loosed = helper.getLevel().getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity.class,
						around, needle -> !before.contains(needle));
				helper.assertTrue(loosed.size() == 1, "the click loosed " + loosed.size() + " needles");
				damage[level] = loosed.getFirst().getBaseDamage();
				loosed.forEach(net.minecraft.world.entity.Entity::discard);
				SculptingSessions.end(player, false);
				helper.getLevel().getEntitiesOfClass(com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity.class, around)
						.forEach(net.minecraft.world.entity.Entity::discard);
			}
		}
		helper.assertTrue(damage[0] > 0.0D && Math.abs(damage[1] / damage[0] - 1.45D) < 1.0E-4D,
				"Whipcrack 3 did not harden the clicked needle by 45%: " + damage[0] + " against " + damage[1]);
		helper.succeed();
	}

	/** Deep Brand: at full Deep Brand a brand takes in ten ticks of aim, and lasts a minute. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void deepBrandSetsFasterAndLastsLonger(GameTestHelper helper) {
		clear(helper);
		for (int level = 0; level <= 1; level++) {
			Pig pig = still(helper, EntityType.PIG, feet(helper, 4.5D + level * 10.0D, 9.5D));
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, brandMemory(), 2_000.0D);
				skill(player, SkillPointInit.skill_deep_brand, level * 2);
				aim(player, feet(helper, 4.5D + level * 10.0D, 12.5D), pig.getBoundingBox().getCenter());
				for (int i = 0; i < 10; i++) project(player);
				StrandBrand brand = StrandService.brandFor(pig, player.getUUID());
				if (level == 0) {
					helper.assertTrue(brand == null, "an untrained brand took in ten ticks");
				} else {
					helper.assertTrue(brand != null, "Deep Brand 2 did not set the brand in ten ticks");
					long lasts = brand.until() - helper.getLevel().getGameTime();
					helper.assertTrue(lasts == 1200L, "Deep Brand 2's brand lasts " + lasts + " ticks, not 1200");
				}
				SculptingSessions.end(player, true);
			} finally {
				pig.discard();
			}
		}
		helper.succeed();
	}

	/**
	 * Second Strand (CL-27): with the off hand empty the strand opens with its twin and costs half again; with an item in
	 * the off hand it opens alone at the plain price.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void secondStrandTwinsFromAFreeOffHandForHalfAgain(GameTestHelper helper) {
		clear(helper);
		double[] paid = new double[2];
		boolean[] twin = new boolean[2];
		for (int free = 0; free <= 1; free++) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, "searing_lash", 2_000.0D);
				skill(player, SkillPointInit.skill_second_strand, 1);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
				player.setItemInHand(InteractionHand.OFF_HAND, free == 1 ? ItemStack.EMPTY : new ItemStack(net.minecraft.world.item.Items.STICK));
				player.startUsingItem(InteractionHand.MAIN_HAND);
				face(player, feet(helper, 4.5D + free * 10.0D, 12.5D), 180.0F, 0.0F);
				paid[free] = project(player);
				helper.assertTrue(StrandService.isOpen(player), "the lash did not open");
				twin[free] = StrandService.current(player).twin();
				helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_TWIN_STRAND) == twin[free],
						"Both Hands followed " + (twin[free] ? "no twin" : "a lone strand"));
				SculptingSessions.end(player, true);
				player.stopUsingItem();
			}
		}
		helper.assertTrue(!twin[0], "an item in the off hand still poured a twin");
		helper.assertTrue(twin[1], "a free off hand poured no twin");
		helper.assertTrue(paid[0] > 0.0D && Math.abs(paid[1] / paid[0] - 1.5D) < 1.0E-6D,
				"the twin's first pulse cost " + paid[1] + " against the lone strand's " + paid[0]);
		helper.succeed();
	}

	/** Holds Projection with the main hand and leaves the off hand free, Second Strand learned: the twin pours. */
	private static void twinHands(ServerPlayer player) {
		skill(player, SkillPointInit.skill_second_strand, 1);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
		player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		player.startUsingItem(InteractionHand.MAIN_HAND);
	}

	/**
	 * What the twin adds, form by form, through the real chain: a twin brand takes on the nearest other body within four
	 * blocks as well as the one aimed at, and a twin plant sinks two pulses for one paid.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void secondStrandBrandsASecondBodyAndSinksTwice(GameTestHelper helper) {
		clear(helper);
		Pig aimed = still(helper, EntityType.PIG, feet(helper, 10.5D, 9.5D));
		Pig beside = still(helper, EntityType.PIG, feet(helper, 12.5D, 9.5D));
		Pig far = still(helper, EntityType.PIG, feet(helper, 18.5D, 9.5D));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, brandMemory(), 2_000.0D);
			twinHands(player);
			aim(player, feet(helper, 10.5D, 12.5D), aimed.getBoundingBox().getCenter());
			for (int i = 0; i < BrandRulesHolder.SET + 4; i++) project(player);
			helper.assertTrue(StrandService.isOpen(player) && StrandService.current(player).twin(), "the brand opened without its twin");
			helper.assertTrue(StrandService.brandFor(aimed, player.getUUID()) != null, "the aimed pig carries no brand");
			helper.assertTrue(StrandService.brandFor(beside, player.getUUID()) != null,
					"the twin did not brand the pig two blocks from the first");
			helper.assertTrue(StrandService.brandFor(far, player.getUUID()) == null, "the twin branded a pig eight blocks away");
			SculptingSessions.end(player, true);
			player.stopUsingItem();
		} finally {
			aimed.discard();
			beside.discard();
			far.discard();
		}
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, plantMemory(), 2_000.0D);
			twinHands(player);
			aim(player, feet(helper, 10.5D, 12.5D), Vec3.atCenterOf(at(helper, 10, 0, 8)).add(0.0D, 0.5D, 0.0D));
			helper.assertTrue(project(player) > 0.0D, "the twin plant's first pulse was not paid");
			helper.assertTrue(StrandService.isOpen(player) && StrandService.current(player).twin(), "the plant opened without its twin");
			int sunk = StrandService.current(player).sunkPulses();
			helper.assertTrue(sunk == 2, "one paid pulse of a twin plant sank " + sunk + ", not 2");
			SculptingSessions.end(player, true);
			player.stopUsingItem();
		}
		helper.succeed();
	}

	// ── A plant runs along a wet stain ──

	/**
	 * A plant sunk into a wet floor stain runs along it: the husk standing four cells down the stain is struck once, and
	 * the stain the plant sank into is no longer wet. A plant on bare stone still erupts as a ring
	 * ({@link #aPlantEruptsWhenLetGo}).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void aPlantOnAWetStainRunsAlongIt(GameTestHelper helper) {
		clear(helper);
		ServerLevel level = helper.getLevel();
		List<BlockPos> line = new ArrayList<>();
		for (int z = 9; z >= 2; z--) line.add(at(helper, 10, 1, z));
		var wet = BlockInit.blood_stain.get().defaultBlockState()
				.setValue(net.minecraft.world.level.block.MultifaceBlock.getFaceProperty(net.minecraft.core.Direction.DOWN), true);
		CapturedPlayer captured = CapturedPlayer.create(helper).joinLevel();
		ServerPlayer player = captured.player;
		prepare(player, plantMemory(), 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 12.5D);
		Husk husk = dummy(helper, feet(helper, 10.5D, 5.5D), 300.0D);
		int hold = 25;
		int[] tick = {0};
		int[] hurts = {0};
		float[] last = {300.0F};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t <= hold) {
				// The fixture keeps the stain wet until the plant goes off: a random tick would otherwise age it.
				for (BlockPos cell : line) level.setBlock(cell, wet, 2);
			}
			if (t < hold) {
				aim(player, stand, Vec3.atCenterOf(at(helper, 10, 0, 9)).add(0.0D, 0.5D, 0.0D));
				project(player);
			} else if (t == hold) {
				helper.assertTrue(StrandService.isOpen(player), "the plant did not open on the stained floor");
				SculptingSessions.end(player, true);
			}
			if (husk.getHealth() < last[0]) {
				hurts[0]++;
				last[0] = husk.getHealth();
			}
		});
		helper.runAtTickTime(hold + com.vincenthuto.hemomancy.common.manipulation.verb.strand.PlantRules.ERUPTION_DELAY + 30, () -> {
			try {
				helper.assertTrue(hurts[0] == 1, "the run struck the husk four cells down " + hurts[0] + " times, not once");
				helper.assertTrue(SchoolStates.has(husk, SchoolState.RIME), "the run laid no Rime");
				helper.assertTrue(!com.vincenthuto.hemomancy.common.world.ground.GroundNetwork.isWetStain(level, line.getFirst()),
						"the stain the plant sank into is still wet");
				helper.assertTrue(!com.vincenthuto.hemomancy.common.world.ground.GroundNetwork.isWetStain(level, line.get(4)),
						"the stain under the husk is still wet: the run did not spend it");
				helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_STAIN_RUN), "a plant run along a stain did not grant Along the Red");
			} finally {
				husk.discard();
				for (BlockPos cell : line) level.setBlock(cell, Blocks.AIR.defaultBlockState(), 2);
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── Blood Binding, the Animus brand ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aBoundCreatureCarriesTheBindersBrand(GameTestHelper helper) {
		clear(helper);
		Husk husk = dummy(helper, feet(helper, 10.5D, 9.5D), 300.0D);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "blood_binding", 2_000.0D);
			aim(player, feet(helper, 10.5D, 12.5D), husk.getBoundingBox().getCenter());
			helper.assertTrue(project(player) > 0.0D, "the binding was not paid");
			StrandBrand brand = StrandService.brandFor(husk, player.getUUID());
			helper.assertTrue(brand != null && brand.school() == EnumBloodTemper.ANIMUS, "the bound husk carries no Animus brand: " + brand);
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_BOUND_AND_BRANDED), "branding by binding did not grant Bound and Branded");
			SculptingSessions.end(player, true);
		} finally {
			husk.discard();
		}
		helper.succeed();
	}

	// ── The Saint claims ahead of a strand ──

	/**
	 * Rites, Saints and Wills claim ahead of a strand every tick: with Searing Lash selected, Projection on Hemorath's chest
	 * opens no strand, shut ribs taking nothing and an open Gorged taking his feed; and a strand already out at open air
	 * stops being fed the moment the aim meets him, its session gone within two ticks.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aSaintClaimsAheadOfAStrand(GameTestHelper helper) {
		clear(helper);
		var saint = EntityInit.hemorath.get().create(helper.getLevel());
		saint.moveTo(feet(helper, 10.5D, 6.5D));
		helper.getLevel().addFreshEntity(saint);
		// The challenger stands in the level (found by an entity search, as the arena reads its challengers) before the
		// Saint's next step: the Hemorath fixture's mock player.
		ServerPlayer player = DuctilisGameTests.player(helper);
		prepare(player, "searing_lash", 4_000.0D);
		Vec3 stand = feet(helper, 10.5D, 9.5D);
		player.setPos(stand.x, stand.y, stand.z);
		saint.aiStep();
		helper.assertTrue(saint.arena() != null && saint.arena().challengers().contains(player),
				"the Saint does not count the player as a challenger, so nothing here is his to claim");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
		player.startUsingItem(InteractionHand.MAIN_HAND);
		int[] tick = {0};
		long[] met = {0L};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t == 0) {
				// Facing away from him, out over open air: the lash opens.
				face(player, stand, 0.0F, 0.0F);
				project(player);
				helper.assertTrue(StrandService.isOpen(player), "the lash did not open at open air");
			} else if (t <= 3) {
				if (t == 1) met[0] = helper.getLevel().getGameTime();
				aim(player, stand, saint.chest());
				double spent = project(player);
				helper.assertTrue(spent == 0.0D, "with the aim on his shut ribs the chain still spent " + spent);
				SculptingSessions.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
				if (helper.getLevel().getGameTime() - met[0] >= 2)
					helper.assertTrue(!StrandService.isOpen(player), "the strand outlived the Saint's claim by two ticks");
			}
		});
		helper.runAtTickTime(6, () -> {
			try {
				helper.assertTrue(!StrandService.isOpen(player), "the strand was still open");
				// Not fed: every pass of the chain with the aim on him spent nothing (asserted each tick above), and the
				// strand's channel is gone, so no later pulse can come.
				helper.assertTrue(SculptingSessions.current(player) == null, "the strand's channel outlived the Saint's claim");
				player.stopUsingItem();
				aim(player, stand, saint.chest());
				double shut = project(player);
				helper.assertTrue(shut == 0.0D && !StrandService.isOpen(player), "shut ribs opened a strand or took " + shut);
				saint.openings().open(com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathOpenings.GORGED, 0);
				double fed = project(player);
				helper.assertTrue(fed == com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathOpenings.FEED_RATE,
						"Gorged took " + fed + ", not his feed");
				helper.assertTrue(!StrandService.isOpen(player), "a strand opened on the Saint's open Gorged");
			} finally {
				SculptingSessions.end(player, true);
				saint.discard();
				player.discard();
			}
			helper.succeed();
		});
	}
}
