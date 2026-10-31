package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.entity.utility.MorphlingFieldEntity;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.*;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import com.vincenthuto.hemomancy.common.network.morphling.SyncEquippedMorphlingPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Brined Court Track 2, the full strain matrix: every strain at every stage (Unfed to Primal), in a built home habitat
 * and out of it, alone and with a teammate. Each test builds its habitat once, then runs the six stages one
 * after another on the same host, driving the equipped tick the way {@code EquippedMorphlingEvents} does and checking
 * which powers answer at that stage. The home predicate is asserted against the built habitat at every stage, so each
 * strain's home condition is proven in the world, not only in {@code MorphlingHomeConditionsTest}.
 *
 * <p>Habitats: Hushbell standing still (away: shifting every tick); Marrowplume a bone block in reach; Mermaid's Purse a
 * pool to sprint-swim in (solo) or a teammate within 8 blocks (with an ally; away keeps the ally at 10); Ink Cap a
 * sealed stone room; Holdfast stone underfoot (away: planks); Dead Man's Rope a pool deep enough to be under water;
 * Kinbud a magma block within 4; Sunburst open noon sky (away: a stone roof).
 */
@GameTestHolder("morphling_strain_validation")
@PrefixGameTestTemplate(false)
public final class MorphlingStrainMatrixGameTests {
	private static final String BATCH = "morphling_strain_matrix";
	private static final int WARMUP = 10;
	private static final int STAGES = 6;
	private static final float EPS = 0.01F;
	/** Habitats sit above the template's floor: the host stands on {@link #PAD} at {@link #HOST}. */
	private static final Vec3 HOST = new Vec3(12.5D, 5.0D, 12.5D);
	private static final BlockPos PAD = new BlockPos(12, 4, 12);

	private MorphlingStrainMatrixGameTests() {
	}

	// --- The matrix: one test per strain, home or away, alone or with a teammate ---------------------------------

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 670)
	public static void hushbellAwayAlone(GameTestHelper h) {
		run(h, new Hushbell(), false, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 670)
	public static void hushbellAwayWithAlly(GameTestHelper h) {
		run(h, new Hushbell(), false, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 670)
	public static void hushbellHomeAlone(GameTestHelper h) {
		run(h, new Hushbell(), true, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 670)
	public static void hushbellHomeWithAlly(GameTestHelper h) {
		run(h, new Hushbell(), true, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 544)
	public static void marrowplumeAwayAlone(GameTestHelper h) {
		run(h, new Marrowplume(), false, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 544)
	public static void marrowplumeAwayWithAlly(GameTestHelper h) {
		run(h, new Marrowplume(), false, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 544)
	public static void marrowplumeHomeAlone(GameTestHelper h) {
		run(h, new Marrowplume(), true, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 544)
	public static void marrowplumeHomeWithAlly(GameTestHelper h) {
		run(h, new Marrowplume(), true, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 310)
	public static void mermaidsPurseAwayAlone(GameTestHelper h) {
		run(h, new MermaidsPurse(), false, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 310)
	public static void mermaidsPurseAwayWithAlly(GameTestHelper h) {
		run(h, new MermaidsPurse(), false, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 310)
	public static void mermaidsPurseHomeAlone(GameTestHelper h) {
		run(h, new MermaidsPurse(), true, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 310)
	public static void mermaidsPurseHomeWithAlly(GameTestHelper h) {
		run(h, new MermaidsPurse(), true, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 370)
	public static void inkCapAwayAlone(GameTestHelper h) {
		run(h, new InkCap(), false, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 370)
	public static void inkCapAwayWithAlly(GameTestHelper h) {
		run(h, new InkCap(), false, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 370)
	public static void inkCapHomeAlone(GameTestHelper h) {
		run(h, new InkCap(), true, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 370)
	public static void inkCapHomeWithAlly(GameTestHelper h) {
		run(h, new InkCap(), true, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 190)
	public static void holdfastAwayAlone(GameTestHelper h) {
		run(h, new Holdfast(), false, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 190)
	public static void holdfastAwayWithAlly(GameTestHelper h) {
		run(h, new Holdfast(), false, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 190)
	public static void holdfastHomeAlone(GameTestHelper h) {
		run(h, new Holdfast(), true, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 190)
	public static void holdfastHomeWithAlly(GameTestHelper h) {
		run(h, new Holdfast(), true, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 310)
	public static void deadMansRopeAwayAlone(GameTestHelper h) {
		run(h, new DeadMansRope(), false, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 310)
	public static void deadMansRopeAwayWithAlly(GameTestHelper h) {
		run(h, new DeadMansRope(), false, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 310)
	public static void deadMansRopeHomeAlone(GameTestHelper h) {
		run(h, new DeadMansRope(), true, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 310)
	public static void deadMansRopeHomeWithAlly(GameTestHelper h) {
		run(h, new DeadMansRope(), true, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 94)
	public static void kinbudAwayAlone(GameTestHelper h) {
		run(h, new Kinbud(), false, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 94)
	public static void kinbudAwayWithAlly(GameTestHelper h) {
		run(h, new Kinbud(), false, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 94)
	public static void kinbudHomeAlone(GameTestHelper h) {
		run(h, new Kinbud(), true, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 94)
	public static void kinbudHomeWithAlly(GameTestHelper h) {
		run(h, new Kinbud(), true, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 544)
	public static void sunburstAwayAlone(GameTestHelper h) {
		run(h, new Sunburst(), false, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 544)
	public static void sunburstAwayWithAlly(GameTestHelper h) {
		run(h, new Sunburst(), false, true);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 544)
	public static void sunburstHomeAlone(GameTestHelper h) {
		run(h, new Sunburst(), true, false);
	}

	@GameTest(template = "empty", batch = BATCH, timeoutTicks = 544)
	public static void sunburstHomeWithAlly(GameTestHelper h) {
		run(h, new Sunburst(), true, true);
	}

	// --- The runner -----------------------------------------------------------------------------------------------

	private static final class Rig {
		final GameTestHelper h;
		final Probe probe;
		final boolean home;
		final ServerPlayer host;
		final ServerPlayer ally;
		final List<Entity> spawned = new ArrayList<>();
		ItemStack stack = ItemStack.EMPTY;
		int stage;
		float scratch;

		Rig(GameTestHelper h, Probe probe, boolean home, ServerPlayer host, ServerPlayer ally) {
			this.h = h;
			this.probe = probe;
			this.home = home;
			this.host = host;
			this.ally = ally;
		}

		IMorphling morphling() {
			return (IMorphling) stack.getItem();
		}

		void check(boolean condition, String what) {
			h.assertTrue(condition, probe.strain().id() + " stage " + stage + (home ? " home" : " away")
					+ (ally == null ? " solo" : " ally") + ": " + what);
		}

		Vec3 at(double dx, double dz) {
			return h.absoluteVec(HOST.add(dx, 0.0D, dz));
		}

		BlockPos block(int dx, int dy, int dz) {
			return h.absolutePos(PAD.offset(dx, dy, dz));
		}

		Mob husk(double dx, double dz) {
			Mob husk = EntityType.HUSK.create(h.getLevel());
			husk.setNoAi(true);
			husk.moveTo(at(dx, dz));
			h.getLevel().addFreshEntity(husk);
			spawned.add(husk);
			return husk;
		}

		void placeAlly(double dx, double dz) {
			if (ally != null) ally.moveTo(at(dx, dz));
		}

		void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
			for (BlockPos pos : BlockPos.betweenClosed(x0, y0, z0, x1, y1, z1)) {
				h.setBlock(PAD.offset(pos.getX(), pos.getY(), pos.getZ()), block);
			}
		}

		void pad(Block block) {
			fill(-1, 0, -1, 1, 0, 1, block);
		}

		/** A glass-walled pool two blocks deep (relative x0..x1, z0..z1), its surface open to the sky. */
		void pool(int x0, int z0, int x1, int z1) {
			fill(x0 - 1, 0, z0 - 1, x1 + 1, 2, z1 + 1, Blocks.GLASS);
			fill(x0, 1, z0, x1, 2, z1, Blocks.WATER);
		}

		/** Updates the host's water state the way its own tick would (a test player is never ticked). */
		void wet() {
			host.baseTick();
			host.baseTick();
		}
	}

	private static void run(GameTestHelper h, Probe probe, boolean home, boolean withAlly) {
		ServerPlayer host = MorphlingPowerGameTests.host(h);
		ServerPlayer ally = withAlly ? MorphlingPowerGameTests.host(h) : null;
		if (ally != null) MorphlingPowerGameTests.team(h, host, ally);
		Rig r = new Rig(h, probe, home, host, ally);
		host.moveTo(r.at(0.0D, 0.0D));
		host.setYRot(0.0F);
		host.setYHeadRot(0.0F);
		host.setXRot(0.0F);
		probe.build(r);
		for (int t = 1; t < WARMUP; t++) h.runAfterDelay(t, () -> MorphlingAnchorState.update(host));
		int k = probe.ticks();
		for (int s = 0; s < STAGES; s++) {
			int stage = s;
			int start = WARMUP + s * (k + 2);
			h.runAfterDelay(start, () -> prepare(r, stage));
			for (int t = 1; t <= k; t++) {
				int tick = t;
				h.runAfterDelay(start + t, () -> drive(r, tick));
			}
			h.runAfterDelay(start + k + 1, () -> {
				MorphlingHomeConditions.forget(host);
				r.check(MorphlingHomeConditions.isHome(host, probe.strain()) == home, "home predicate in the built habitat");
				probe.check(r, stage);
				cleanup(r);
			});
		}
		h.runAfterDelay(WARMUP + STAGES * (k + 2) + 1, () -> {
			if (ally != null) MorphlingPowerGameTests.done(h, host, ally);
			else MorphlingPowerGameTests.done(h, host);
		});
	}

	private static void prepare(Rig r, int stage) {
		r.stage = stage;
		reset(r.host);
		if (r.ally != null) reset(r.ally);
		MorphlingPowers.clearHost(r.host);
		MorphlingSession.clear(r.host.getUUID());
		MorphlingFeeding.clear(r.host.getUUID());
		MorphlingHomeConditions.forget(r.host);
		BorrowedBloodReserve.drainToCover(r.host, BorrowedBloodReserve.get(r.host));
		r.stack = MorphlingPowerGameTests.equip(r.host, r.probe.strain(), stage);
		r.probe.prepare(r, stage);
	}

	private static void reset(ServerPlayer player) {
		player.removeAllEffects();
		player.setHealth(player.getMaxHealth());
		player.invulnerableTime = 0;
		player.clearFire();
		player.getFoodData().setFoodLevel(20);
		player.getFoodData().setSaturation(5.0F);
		var blood = HemoCapabilityAccess.getBloodVolume(player);
		blood.setBloodVolume(blood.getMaxBloodVolume());
		var guardrails = HemoCapabilityAccess.getPowerGuardrails(player);
		guardrails.setCirculationWindowStart(player.level().getGameTime());
		guardrails.setCirculationWindowUsed(0.0D);
	}

	private static void drive(Rig r, int tick) {
		r.probe.beforeTick(r, tick);
		if (r.probe.jiggles(r)) r.host.moveTo(r.at(tick % 2 == 0 ? 0.25D : 0.0D, 0.0D));
		MorphlingAnchorState.update(r.host);
		r.morphling().onEquippedServerTick(r.host, r.stack);
	}

	private static void cleanup(Rig r) {
		for (Entity entity : r.spawned) entity.discard();
		r.spawned.clear();
		var bounds = r.h.getBounds();
		for (MorphlingFieldEntity field : r.h.getLevel().getEntitiesOfClass(MorphlingFieldEntity.class, bounds,
				field -> r.host.getUUID().equals(field.owner()))) field.discard();
		for (ItemEntity item : r.h.getLevel().getEntitiesOfClass(ItemEntity.class, bounds)) item.discard();
	}

	private static boolean near(double a, double b) {
		return Math.abs(a - b) < EPS;
	}

	private static int amplifier(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
		var instance = player.getEffect(effect);
		return instance == null ? -1 : instance.getAmplifier();
	}

	// --- The eight probes -----------------------------------------------------------------------------------------

	private abstract static class Probe {
		abstract MorphlingStrain strain();

		/** Equipped ticks driven per stage before the checks. */
		abstract int ticks();

		abstract void build(Rig r);

		void prepare(Rig r, int stage) {
		}

		abstract void check(Rig r, int stage);

		boolean jiggles(Rig r) {
			return false;
		}

		/** Runs before each driven tick, for a stance a power reads only part of the time. */
		void beforeTick(Rig r, int tick) {
		}
	}

	/** Hush, Cryptobiotic Hide, Cold Cleanse, Fold Shut. Home is stillness; away shifts every tick. */
	private static final class Hushbell extends Probe {
		MorphlingStrain strain() {
			return MorphlingStrains.HUSHBELL;
		}

		int ticks() {
			return HushbellRules.COLD_CLEANSE_TICKS + 1;
		}

		boolean jiggles(Rig r) {
			return !r.home;
		}

		void build(Rig r) {
			r.pad(Blocks.OAK_PLANKS);
			r.placeAlly(3.0D, 0.0D);
		}

		void prepare(Rig r, int stage) {
			r.host.setHealth(8.0F);
			r.host.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 2000, 0));
			r.host.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WEAKNESS, 2000, 0));
		}

		void check(Rig r, int s) {
			IMorphling m = r.morphling();
			r.check(near(m.exhaustionMultiplier(r.host, r.stack), 1.0D - HushbellRules.hush(s)), "Hush slows hunger");
			r.check(near(m.airLossSkipChance(r.host, r.stack), HushbellRules.hush(s)), "Hush slows air loss");
			r.check(!r.host.hasEffect(MobEffects.POISON), "poison stays off");
			r.check((amplifier(r.host, MobEffects.DAMAGE_RESISTANCE) == 0) == (s >= 2), "Cryptobiotic Hide from Developing");
			r.check(r.host.hasEffect(MobEffects.WEAKNESS) == (s < 3), "Cold Cleanse from Mature");
			r.host.removeEffect(MobEffects.DAMAGE_RESISTANCE);
			m.onEquippedHurt(r.host, r.stack, r.host.damageSources().generic(), 1.0F, true);
			r.check(MorphlingLock.isPinned(r.host) == (s >= 4), "Fold Shut from Apex");
			if (s >= 4) {
				int expected = r.home ? HushbellRules.FOLD_HOME_TICKS : HushbellRules.FOLD_TICKS;
				r.check(r.host.getEffect(EffectInit.pinned).getDuration() == expected, "Fold Shut lasts " + expected);
				r.check(amplifier(r.host, MobEffects.DAMAGE_RESISTANCE) == 3 && Paralysis.blocksActions(r.host),
						"Fold Shut is Resistance IV and seals");
			}
			if (r.ally != null) r.check(!r.ally.hasEffect(MobEffects.DAMAGE_RESISTANCE), "Hushbell shares nothing");
		}
	}

	/** Marrow Feeding, Plume Cloud, Root Network (and its solo fallback), Fall's Inheritance, Whale Fall. */
	private static final class Marrowplume extends Probe {
		MorphlingStrain strain() {
			return MorphlingStrains.MARROWPLUME;
		}

		int ticks() {
			return MarrowplumeRules.ROOT_NETWORK_TICKS;
		}

		void build(Rig r) {
			r.pad(Blocks.OAK_PLANKS);
			if (r.home) r.h.setBlock(PAD.offset(3, 0, 3), Blocks.BONE_BLOCK);
			r.placeAlly(-3.0D, 0.0D);
		}

		void prepare(Rig r, int stage) {
			r.host.setHealth(10.0F);
			if (r.ally != null) r.ally.setHealth(15.0F);
		}

		void check(Rig r, int s) {
			float feed = MarrowplumeRules.feeding(s, r.home);
			float gain = r.host.getHealth() - 10.0F;
			if (r.ally == null && s >= 3) {
				r.check(gain >= 2 * feed + MarrowplumeRules.ALONE_FEEDING_BONUS - EPS
						&& gain <= 2 * feed + 2 * MarrowplumeRules.ALONE_FEEDING_BONUS + EPS,
						"Marrow Feeding twice plus the alone bonus: " + gain);
			} else {
				r.check(near(gain, 2 * feed), "Marrow Feeding twice: " + gain);
			}
			if (r.ally != null) {
				r.check(near(r.ally.getHealth(), s >= 3 ? 15.0F + MarrowplumeRules.ROOT_NETWORK_HEAL : 15.0F),
						"Root Network heals the ally from Mature: " + r.ally.getHealth());
			}
			Mob husk = r.husk(0.0D, 3.0D);
			r.host.setHealth(10.0F);
			r.morphling().onEquippedHurt(r.host, r.stack, r.host.damageSources().generic(), 1.0F, true);
			r.check(husk.hasEffect(MobEffects.WEAKNESS) == (s >= 2), "Plume Cloud from Developing");
			r.check(near(r.host.getHealth(), s >= 2 ? 10.0F + MarrowplumeRules.plumeHeal(1) : 10.0F), "Plume Cloud feeds");
			if (r.ally != null) r.check(!r.ally.hasEffect(MobEffects.WEAKNESS), "the plume spares the ally");
			r.morphling().onEquippedKill(r.host, r.stack, husk, true);
			boolean bone = !r.h.getLevel().getEntitiesOfClass(ItemEntity.class, r.h.getBounds(),
					item -> item.getItem().is(Items.BONE)).isEmpty();
			r.check(bone == (s >= 4), "Fall's Inheritance from Apex");
			List<MorphlingFieldEntity> fields = r.h.getLevel().getEntitiesOfClass(MorphlingFieldEntity.class,
					r.h.getBounds(), field -> r.host.getUUID().equals(field.owner()));
			r.check(fields.size() == (s >= 5 ? 1 : 0), "Whale Fall at Primal");
			if (s >= 5) {
				MorphlingFieldEntity field = fields.getFirst();
				r.check(field.kind() == MorphlingFieldEntity.Kind.WHALE_FALL
						&& near(field.radius(), MorphlingHomeBonus.radius(MarrowplumeRules.WHALE_FALL_RADIUS, r.home))
						&& field.lifetime() == MorphlingHomeBonus.duration(MarrowplumeRules.WHALE_FALL_TICKS, r.home),
						"Whale Fall's patch takes the home bonus");
			}
		}
	}

	/** Purse Siphon's swim speed, Feed Banking, School's Share, Raise the Sail and its bill strike, Shared Wake. */
	private static final class MermaidsPurse extends Probe {
		MorphlingStrain strain() {
			return MorphlingStrains.MERMAIDS_PURSE;
		}

		int ticks() {
			return MermaidsPurseRules.PROC_TICKS + 1;
		}

		private static boolean wet(Rig r) {
			return r.home && r.ally == null;
		}

		void build(Rig r) {
			if (wet(r)) r.pool(-1, -1, 1, 1);
			else r.pad(Blocks.OAK_PLANKS);
			r.placeAlly(r.home ? 3.0D : 10.0D, 0.0D);
		}

		void prepare(Rig r, int stage) {
			if (wet(r)) {
				r.host.setSprinting(true);
				r.wet();
				r.host.setSwimming(true);
			}
			if (r.ally != null) r.ally.setHealth(5.0F);
			else r.host.setHealth(5.0F);
		}

		void check(Rig r, int s) {
			AttributeModifier swim = r.host.getAttribute(NeoForgeMod.SWIM_SPEED).getModifier(MermaidsPurseMorphlingItem.SWIM_SPEED);
			if (wet(r) && s >= 1) {
				r.check(swim != null && near(swim.amount(), MermaidsPurseRules.swimBonus(s, true))
						&& swim.amount() <= MermaidsPurseRules.SWIM_CAP, "swim speed while submerged, under the cap");
			} else {
				r.check(swim == null, "no swim bonus out of the water or at Unfed");
			}
			float heal = MermaidsPurseRules.schoolHeal(s);
			if (r.ally != null) {
				boolean reach = r.home;
				r.check(near(r.ally.getHealth(), 5.0F + (s >= 3 && reach ? heal : 0.0F)),
						"School's Share reaches a failing ally in 8 blocks from Mature: " + r.ally.getHealth());
			} else {
				float gain = r.host.getHealth() - 5.0F;
				if (s >= 3) {
					// Host healing shares the circulation window with Purse Siphon (balance rule 5), so a degree-0 host
					// can get less than the full heal; it never gets more.
					r.check(gain > 0.5F && gain <= heal + EPS, "School's Share alone: " + gain);
				} else {
					r.check(near(gain, 0.0F), "no School's Share before Mature");
				}
			}
			r.check(r.host.hasEffect(MobEffects.DOLPHINS_GRACE) == (s >= 4 && wet(r)), "Raise the Sail sprint-swimming from Apex");
			r.check(MorphlingActiveState.has(r.morphling().activeFlags(r.host, r.stack), MorphlingActiveState.SAIL_RAISED)
					== (s >= 4 && wet(r)), "SAIL_RAISED bit");
			Mob husk = r.husk(0.0D, 3.0D);
			double banked = BorrowedBloodReserve.get(r.host);
			r.morphling().onEquippedAttack(r.host, r.stack, husk, 5.0F);
			r.check((BorrowedBloodReserve.get(r.host) > banked) == (s >= 2), "Feed Banking from Developing");
			r.check((husk.getHealth() <= husk.getMaxHealth() - MermaidsPurseRules.BILL_DAMAGE + EPS) == (s >= 4 && wet(r)),
					"the bill strike lands only from a sprint-swim at Apex");
			boolean wake = r.morphling().tryUse(r.host, InteractionHand.MAIN_HAND, r.stack, r.h.getLevel());
			r.check(wake == (s >= 5), "Shared Wake at Primal");
			if (wake) {
				r.check(MorphlingActiveState.has(r.morphling().activeFlags(r.host, r.stack), MorphlingActiveState.SHARED_WAKE),
						"SHARED_WAKE bit");
			}
		}
	}

	/** Veil, Ink Wake, Folded Away (home only), Deliquesce. Home is a sealed stone room. */
	private static final class InkCap extends Probe {
		MorphlingStrain strain() {
			return MorphlingStrains.INK_CAP;
		}

		int ticks() {
			return InkCapRules.FOLDED_STILL_TICKS + 11;
		}

		void build(Rig r) {
			if (r.home) {
				r.fill(-3, 0, -2, 3, 4, 2, Blocks.STONE);
				r.fill(-2, 1, -1, 2, 3, 1, Blocks.AIR);
			} else {
				r.pad(Blocks.OAK_PLANKS);
			}
			r.placeAlly(-2.0D, 0.0D);
		}

		void prepare(Rig r, int stage) {
			r.husk(2.0D, 0.0D);
		}

		/** Sprints only for the last ten ticks, so the wake's 1.5-second blindness is still on at the check. */
		@Override
		void beforeTick(Rig r, int tick) {
			r.host.setSprinting(tick > ticks() - 10);
		}

		void check(Rig r, int s) {
			IMorphling m = r.morphling();
			r.check(near(m.visibilityMultiplier(r.host, r.stack), 1.0D - InkCapRules.veilReduction(s, r.home)), "Veil");
			Mob husk = (Mob) r.spawned.getFirst();
			var blind = husk.getEffect(MobEffects.BLINDNESS);
			r.check((blind != null) == (s >= 2), "Ink Wake from Developing");
			if (blind != null) {
				int expected = MorphlingHomeBonus.duration(InkCapRules.inkWakeTicks(s), r.home);
				r.check(blind.getDuration() <= expected && blind.getDuration() >= expected - ticks() - 2,
						"Ink Wake blinds for " + expected + ": " + blind.getDuration());
			}
			if (r.ally != null) r.check(!r.ally.hasEffect(MobEffects.BLINDNESS), "the wake spares the ally");
			r.check(r.host.hasEffect(MobEffects.INVISIBILITY) == (s >= 3 && r.home), "Folded Away, still at home, from Mature");
			r.check(MorphlingActiveState.has(m.activeFlags(r.host, r.stack), MorphlingActiveState.FOLDED_AWAY)
					== (s >= 3 && r.home), "FOLDED_AWAY bit");
			boolean dissolved = m.tryUse(r.host, InteractionHand.MAIN_HAND, r.stack, r.h.getLevel());
			r.check(dissolved == (s >= 5), "Deliquesce at Primal");
			if (dissolved) {
				r.check(InkCapMorphlingItem.isDissolved(r.host) && Paralysis.blocksActions(r.host)
						&& !MorphlingLock.isPinned(r.host), "dissolved: sealed but never rooted");
				MorphlingLock.onBlockedAttack(r.host);
				r.check(!InkCapMorphlingItem.isDissolved(r.host), "a swing ends Deliquesce");
			}
			r.host.setSprinting(false);
		}
	}

	/** Grip, Cling, Brace, Breakwater (its solo Resistance and its share), The Holding. Home is stone underfoot. */
	private static final class Holdfast extends Probe {
		MorphlingStrain strain() {
			return MorphlingStrains.HOLDFAST;
		}

		int ticks() {
			return 21;
		}

		void build(Rig r) {
			r.pad(r.home ? Blocks.STONE : Blocks.OAK_PLANKS);
			r.placeAlly(0.0D, -2.0D);
		}

		void prepare(Rig r, int stage) {
			r.host.setShiftKeyDown(true);
			MorphlingAnchorState.settle(r.host, true, HoldfastRules.BREAKWATER_ANCHOR_TICKS);
		}

		void check(Rig r, int s) {
			IMorphling m = r.morphling();
			r.check(near(r.host.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), HoldfastRules.gripKnockback(s)), "Grip");
			r.check(near(m.breakSpeedMultiplier(r.host, r.stack, Blocks.STONE.defaultBlockState()),
					1.0D + HoldfastRules.gripBreakSpeed(s)), "Grip breaks stone faster");
			r.check(m.breakSpeedMultiplier(r.host, r.stack, Blocks.DIRT.defaultBlockState()) == 1.0F, "but not dirt");
			r.host.horizontalCollision = true;
			r.check(HoldfastMorphlingItem.clings(r.host, r.stack) == (s >= 2), "Cling from Developing");
			r.host.horizontalCollision = false;
			r.check(HoldfastMorphlingItem.isBreakwater(r.host, r.stack) == (s >= 4), "Breakwater from Apex");
			r.check(r.host.hasEffect(MobEffects.DAMAGE_RESISTANCE) == (s >= 4), "an anchored Breakwater hardens its host alone");
			if (r.ally != null) {
				r.ally.hurt(r.h.getLevel().damageSources().generic(), 8.0F);
				r.check(near(r.host.getHealth(), s >= 4 ? 18.0F : 20.0F) && near(r.ally.getHealth(), s >= 4 ? 14.0F : 12.0F),
						"Breakwater takes a quarter of the ally's hit: " + r.host.getHealth() + "/" + r.ally.getHealth());
				r.ally.removeAllEffects();
			}
			r.host.removeEffect(MobEffects.DAMAGE_RESISTANCE);
			m.onEquippedHurt(r.host, r.stack, r.host.damageSources().generic(), 1.0F, true);
			int brace = amplifier(r.host, MobEffects.DAMAGE_RESISTANCE);
			r.check(s >= 3 ? brace == (r.home ? 1 : 0) : brace < 0, "Brace from Mature, Resistance II at home: " + brace);
			if (r.ally != null) {
				r.check((amplifier(r.ally, MobEffects.DAMAGE_RESISTANCE) == 0) == (s >= 3), "Brace covers the ally behind");
			}
			Mob husk = r.husk(0.0D, 9.0D);
			boolean holding = m.tryUse(r.host, InteractionHand.MAIN_HAND, r.stack, r.h.getLevel());
			r.check(holding == (s >= 5), "The Holding at Primal");
			if (holding) {
				r.check(MorphlingLock.isPinned(husk) == r.home, "The Holding reaches 9 blocks only at home (10 vs 8)");
				r.check(MorphlingLock.isPinned(r.host), "The Holding roots its host");
				if (r.ally != null) r.check(amplifier(r.ally, MobEffects.DAMAGE_RESISTANCE) >= 1, "allies are held safe");
			}
			r.host.setShiftKeyDown(false);
		}
	}

	/** Lateral Strands, Lifeline, Strand Link (ally or block), Colony, Red Rope (hostile pull and ally rescue). */
	private static final class DeadMansRope extends Probe {
		MorphlingStrain strain() {
			return MorphlingStrains.DEAD_MANS_ROPE;
		}

		int ticks() {
			return DeadMansRopeRules.PROC_TICKS + 1;
		}

		void build(Rig r) {
			if (r.home) r.pool(-1, -3, 7, 3);
			else r.pad(Blocks.OAK_PLANKS);
			r.placeAlly(0.0D, 3.0D);
		}

		void prepare(Rig r, int stage) {
			if (r.home) r.wet();
			r.husk(6.0D, 0.0D);
		}

		void check(Rig r, int s) {
			IMorphling m = r.morphling();
			Mob husk = (Mob) r.spawned.getFirst();
			r.check(husk.hasEffect(MobEffects.GLOWING), "Lateral Strands reveal the living");
			r.check(m.onEquippedFall(r.host, r.stack, 12.0F) == (s >= 2), "Lifeline holds 12 blocks from Developing");
			r.check(m.onEquippedFall(r.host, r.stack, 20.0F) == (s >= 3), "24 blocks from Mature");
			r.check(m.onEquippedFall(r.host, r.stack, 100.0F) == (s >= 4), "any height from Apex");
			boolean tied = r.ally != null ? m.onEquippedSneakUse(r.host, r.stack, r.ally, null)
					: m.onEquippedSneakUse(r.host, r.stack, null, r.block(0, 0, -2));
			r.check(tied == (s >= 3) && MorphlingLinks.count(r.host) == (s >= 3 ? 1 : 0), "Strand Link from Mature");
			if (s >= 4) {
				m.onEquippedSneakUse(r.host, r.stack, null, r.block(-1, 0, -2));
				r.check(MorphlingLinks.count(r.host) == 2, "Colony holds more strands");
			}
			if (s >= 3) {
				MorphlingHomeConditions.forget(r.host);
				r.check(MorphlingHomeConditions.isHome(r.host, strain()), "a strand makes the rope at home");
			}
			if (r.ally != null && s >= 3) {
				r.ally.hurt(r.h.getLevel().damageSources().generic(), 10.0F);
				r.check(near(r.host.getHealth(), s >= 4 ? 18.0F : 20.0F) && near(r.ally.getHealth(), s >= 4 ? 12.0F : 10.0F),
						"Colony shares a fifth from Apex: " + r.host.getHealth() + "/" + r.ally.getHealth());
			}
			r.host.lookAt(EntityAnchorArgument.Anchor.EYES, husk.getEyePosition());
			boolean pulled = m.tryUse(r.host, InteractionHand.MAIN_HAND, r.stack, r.h.getLevel());
			r.check(pulled == (s >= 5), "Red Rope at Primal");
			if (pulled) {
				r.check(MorphlingLock.isPinned(husk)
						&& husk.getEffect(EffectInit.pinned).getDuration() == DeadMansRopeRules.RED_ROPE_ROOT_TICKS
						&& husk.getDeltaMovement().x < 0.0D, "Red Rope pulls a hostile in and roots it");
				if (r.ally != null) {
					ItemStack fresh = MorphlingPowerGameTests.equip(r.host, strain(), 5);
					r.host.lookAt(EntityAnchorArgument.Anchor.EYES, r.ally.getEyePosition());
					r.check(r.morphling().tryUse(r.host, InteractionHand.MAIN_HAND, fresh, r.h.getLevel())
							&& r.ally.getDeltaMovement().z < 0.0D && !MorphlingLock.isPinned(r.ally),
							"Red Rope hauls an ally out without rooting it");
					r.stack = fresh;
				}
			}
			r.host.setYRot(0.0F);
			r.host.setXRot(0.0F);
		}
	}

	/** Cultivated Mending, Nematocyst Sting, Grasping Crown, Second Head, Kindled. Home is a magma block in reach. */
	private static final class Kinbud extends Probe {
		MorphlingStrain strain() {
			return MorphlingStrains.KINBUD;
		}

		int ticks() {
			return 5;
		}

		void build(Rig r) {
			r.pad(Blocks.OAK_PLANKS);
			if (r.home) r.h.setBlock(PAD.offset(2, 0, 3), Blocks.MAGMA_BLOCK);
			r.placeAlly(-2.0D, 0.0D);
		}

		void prepare(Rig r, int s) {
			MorphlingHomeConditions.forget(r.host);
			r.check(MorphlingHomeConditions.isHome(r.host, strain()) == r.home, "home predicate in the built habitat");
			IMorphling m = r.morphling();
			r.host.setHealth(15.0F);
			Mob husk = r.husk(2.0D, 0.0D);
			m.onEquippedHurt(r.host, r.stack, r.host.damageSources().generic(), 5.0F, true);
			float queued = MorphlingRegrowthQueue.peek(r.host.getUUID()).pending();
			r.check(near(queued, 5.0F * (float) KinbudRules.mendingShare(s, r.home)), "Cultivated Mending queues " + queued);
			m.onEquippedAttack(r.host, r.stack, husk, 2.0F);
			var stung = husk.getEffect(EffectInit.stung);
			r.check((stung != null) == (s >= 2), "Nematocyst Sting from Developing");
			if (stung != null) {
				r.check(stung.getDuration() == KinbudRules.stingTicks(s, r.home), "Stung lasts " + KinbudRules.stingTicks(s, r.home));
			}
		}

		void check(Rig r, int s) {
			IMorphling m = r.morphling();
			Mob husk = (Mob) r.spawned.getFirst();
			r.check(KinbudSecondHead.heads(r.host) == (s >= 4 ? 1 : 0), "Second Head buds in a fight from Apex");
			r.check(MorphlingActiveState.has(m.activeFlags(r.host, r.stack), MorphlingActiveState.SECOND_HEAD) == (s >= 4),
					"SECOND_HEAD bit");
			if (r.ally != null) {
				r.check(near(r.ally.getHealth(), r.ally.getMaxHealth()) && !r.ally.hasEffect(EffectInit.stung),
						"the head never strikes an ally");
			}
			m.onEquippedAttack(r.host, r.stack, husk, 1.0F);
			m.onEquippedAttack(r.host, r.stack, husk, 1.0F);
			r.check(MorphlingLock.isPinned(husk) == (s >= 3), "Grasping Crown seizes a thrice-struck target from Mature");
			boolean kindled = m.tryUse(r.host, InteractionHand.MAIN_HAND, r.stack, r.h.getLevel());
			r.check(kindled == (s >= 5), "Kindled at Primal");
			if (kindled) {
				r.check(MorphlingRegrowthQueue.peek(r.host.getUUID()).kindledBudget() == KinbudRules.kindledCap(r.home)
						&& KinbudSecondHead.heads(r.host) == KinbudRules.kindledHeads(r.home),
						"Kindled caps at " + KinbudRules.kindledCap(r.home) + " with " + KinbudRules.kindledHeads(r.home) + " heads");
			}
		}
	}

	/** Sun-Fed, Shared Table (ally gift or solo food), Lantern Fronds, Bloom (home only), Arbor. Away is under a roof. */
	private static final class Sunburst extends Probe {
		MorphlingStrain strain() {
			return MorphlingStrains.SUNBURST;
		}

		int ticks() {
			return 2 * SunburstRules.PROC_TICKS;
		}

		void build(Rig r) {
			r.pad(Blocks.OAK_PLANKS);
			if (!r.home) r.fill(-1, 4, -1, 1, 4, 1, Blocks.STONE);
			r.placeAlly(3.0D, 0.0D);
		}

		void prepare(Rig r, int stage) {
			r.host.getFoodData().setFoodLevel(10);
			r.host.getFoodData().setSaturation(0.0F);
			if (r.ally != null) r.ally.setHealth(5.0F);
			else r.host.setHealth(5.0F);
		}

		void check(Rig r, int s) {
			IMorphling m = r.morphling();
			r.check(m.waivesUpkeep(r.host, r.stack, false) == r.home && !m.waivesUpkeep(r.host, r.stack, true),
					"Sun-Fed waives upkeep at home unless Puppet Interference");
			int table = r.ally == null && s >= 2 ? SunburstRules.SHARED_TABLE_ALONE_FOOD : 0;
			int sun = r.home ? (int) Math.floor(2 * SunburstRules.sunFedHunger(s) + 1.0E-9D) : 0;
			int food = r.host.getFoodData().getFoodLevel();
			if (r.home && s >= 4) r.check(food >= 10 + table + sun, "Sun-Fed (and the Bloom) feed: " + food);
			else r.check(food == 10 + table + sun, "Sun-Fed at home, Shared Table alone from Developing: " + food);
			if (r.ally != null) {
				float gift = s >= 2 ? SunburstRules.sharedTableGift(s) : 0.0F;
				r.check(near(r.ally.getHealth(), 5.0F + gift) && near(r.host.getHealth(), 20.0F - gift),
						"Shared Table gives " + gift + ": " + r.ally.getHealth() + "/" + r.host.getHealth());
			}
			boolean lit = r.host.hasEffect(MobEffects.WATER_BREATHING) && r.host.hasEffect(MobEffects.NIGHT_VISION);
			r.check(lit == (s >= 3), "Lantern Fronds from Mature");
			if (r.ally != null) r.check(r.ally.hasEffect(MobEffects.WATER_BREATHING) == (s >= 3), "the fronds light the ally");
			boolean bloom = !r.h.getLevel().getEntitiesOfClass(MorphlingFieldEntity.class, r.h.getBounds(),
					field -> r.host.getUUID().equals(field.owner()) && field.kind() == MorphlingFieldEntity.Kind.BLOOM).isEmpty();
			r.check(bloom == (s >= 4 && r.home), "Bloom from Apex, at home only");
			r.check(MorphlingActiveState.has(m.activeFlags(r.host, r.stack), MorphlingActiveState.BLOOM) == bloom, "BLOOM bit");
			r.check(m.tryUse(r.host, InteractionHand.MAIN_HAND, r.stack, r.h.getLevel()) == (s >= 5), "Arbor at Primal");
		}
	}

	// --- Single cases the matrix leaves out -----------------------------------------------------------------------

	/** Ink Reprieve is the Apex death-save: 500 mL, 8 health, a blinding burst; Mature has none. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void inkReprieveSavesAtApexOnly(GameTestHelper h) {
		ServerPlayer player = MorphlingPowerGameTests.host(h);
		Mob husk = MorphlingPowerGameTests.mob(h, EntityType.HUSK, new Vec3(4.5D, 2.0D, 2.5D));
		player.moveTo(h.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
		ItemStack mature = MorphlingPowerGameTests.equip(player, MorphlingStrains.INK_CAP, 3);
		LastRiteHelper.armForMorphling(player, mature);
		player.setHealth(0.0F);
		((IMorphling) mature.getItem()).onEquippedHurt(player, mature, player.damageSources().generic(), 30.0F, true);
		h.assertTrue(player.getHealth() <= 0.0F, "no reprieve before Apex");
		ItemStack apex = MorphlingPowerGameTests.equip(player, MorphlingStrains.INK_CAP, 4);
		LastRiteHelper.armForMorphling(player, apex);
		double before = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
		((IMorphling) apex.getItem()).onEquippedHurt(player, apex, player.damageSources().generic(), 30.0F, true);
		h.assertTrue(near(player.getHealth(), InkCapRules.REPRIEVE_HEALTH), "Ink Reprieve restores 8 health");
		h.assertTrue(before - HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() >= InkCapRules.REPRIEVE_COST - 1.0D,
				"Ink Reprieve pays 500 mL");
		h.assertTrue(husk.hasEffect(MobEffects.BLINDNESS) && player.hasEffect(MobEffects.INVISIBILITY),
				"the ink burst blinds what is near and hides the host");
		player.setHealth(0.0F);
		((IMorphling) apex.getItem()).onEquippedHurt(player, apex, player.damageSources().generic(), 30.0F, true);
		h.assertTrue(player.getHealth() <= 0.0F, "the shared Last Rite cooldown refuses a second save");
		player.setHealth(player.getMaxHealth());
		husk.discard();
		MorphlingPowerGameTests.done(h, player);
	}

	/** The full Prismatic set already sits at +35% swim speed, so a Purse worn with it adds nothing on top. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void purseSwimSpeedNeverStacksPastThePrismaticSet(GameTestHelper h) {
		for (int x = 1; x <= 3; x++) for (int z = 1; z <= 3; z++) for (int y = 1; y <= 3; y++)
			h.setBlock(new BlockPos(x, y, z), x == 2 && z == 2 && y > 1 ? Blocks.WATER : Blocks.GLASS);
		ServerPlayer player = MorphlingPowerGameTests.host(h);
		player.moveTo(h.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
		player.baseTick();
		player.baseTick();
		ItemStack purse = MorphlingPowerGameTests.equip(player, MorphlingStrains.MERMAIDS_PURSE, 5);
		((IMorphling) purse.getItem()).onEquippedServerTick(player, purse);
		AttributeModifier bare = player.getAttribute(NeoForgeMod.SWIM_SPEED).getModifier(MermaidsPurseMorphlingItem.SWIM_SPEED);
		h.assertTrue(player.isInWater() && bare != null && bare.amount() > 0.0D, "the Purse speeds a submerged host");
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.prismatic_helm.get()));
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.prismatic_chestplate.get()));
		player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.prismatic_leggings.get()));
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.prismatic_boots.get()));
		((IMorphling) purse.getItem()).onEquippedServerTick(player, purse);
		h.assertTrue(player.getAttribute(NeoForgeMod.SWIM_SPEED).getModifier(MermaidsPurseMorphlingItem.SWIM_SPEED) == null,
				"inside the full Prismatic set the strain adds no swim speed");
		for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
				EquipmentSlot.FEET }) player.setItemSlot(slot, ItemStack.EMPTY);
		MorphlingPowerGameTests.done(h, player);
	}

	/** Red Rope roots a boss for 2 s at most and never pulls it (spec: bosses capped, knockback flags untouched). */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void redRopeRootsABossBrieflyWithoutPulling(GameTestHelper h) {
		ServerPlayer player = MorphlingPowerGameTests.host(h);
		player.moveTo(h.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
		Mob guardian = MorphlingPowerGameTests.mob(h, EntityType.ELDER_GUARDIAN, new Vec3(8.5D, 2.0D, 2.5D));
		guardian.setDeltaMovement(Vec3.ZERO);
		ItemStack rope = MorphlingPowerGameTests.equip(player, MorphlingStrains.DEAD_MANS_ROPE, 5);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, guardian.getBoundingBox().getCenter());
		h.assertTrue(((IMorphling) rope.getItem()).tryUse(player, InteractionHand.MAIN_HAND, rope, h.getLevel()),
				"Red Rope strings the boss");
		h.assertTrue(MorphlingLock.isPinned(guardian)
				&& guardian.getEffect(EffectInit.pinned).getDuration() == DeadMansRopeRules.RED_ROPE_BOSS_ROOT_TICKS,
				"a boss is rooted for 2 s");
		h.assertTrue(guardian.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6D, "a boss is never pulled");
		guardian.discard();
		MorphlingPowerGameTests.done(h, player);
	}

	/**
	 * The strands and the Purse fill reach another player: the real equipped tick sends the host's sync to its
	 * trackers, and the payload carries the linked ids, block anchors and fill bits through the codec. This is the
	 * server half of the two-client {@code /hemoverify strands} check; drawing them is Track 3's.
	 */
	@GameTest(template = "empty", timeoutTicks = 60)
	public static void strandsAndPurseFillReachTrackingPlayers(GameTestHelper h) {
		List<SyncEquippedMorphlingPacket> seen = new ArrayList<>();
		ServerPlayer watcher = DuctilisGameTests.player(h, packet -> {
			if (packet instanceof ClientboundCustomPayloadPacket custom
					&& custom.payload() instanceof SyncEquippedMorphlingPacket sync) seen.add(sync);
		});
		ServerPlayer rope = MorphlingPowerGameTests.host(h);
		ServerPlayer purse = MorphlingPowerGameTests.host(h);
		MorphlingPowerGameTests.team(h, rope, watcher, purse);
		rope.moveTo(h.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
		watcher.moveTo(h.absoluteVec(new Vec3(4.5D, 2.0D, 2.5D)));
		purse.moveTo(h.absoluteVec(new Vec3(6.5D, 2.0D, 2.5D)));
		ItemStack ropeStack = MorphlingPowerGameTests.equip(rope, MorphlingStrains.DEAD_MANS_ROPE, 4);
		ItemStack purseStack = MorphlingPowerGameTests.equip(purse, MorphlingStrains.MERMAIDS_PURSE, 3);
		h.runAfterDelay(5, () -> {
			// A test player's connection is never ticked, so stand in for its client: take the chunk batches and
			// acknowledge them, then let the chunk map re-check what the watcher can see.
			for (int i = 0; i < 30; i++) {
				watcher.connection.chunkSender.sendNextChunks(watcher);
				watcher.connection.chunkSender.onChunkBatchReceivedByClient(64.0F);
			}
			h.getLevel().getChunkSource().move(watcher);
		});
		h.runAfterDelay(10, () -> {
			BlockPos anchor = h.absolutePos(new BlockPos(2, 1, 4));
			IMorphling ropeStrain = (IMorphling) ropeStack.getItem();
			h.assertTrue(ropeStrain.onEquippedSneakUse(rope, ropeStack, watcher, null)
					&& ropeStrain.onEquippedSneakUse(rope, ropeStack, null, anchor), "two strands tie");
			Mob husk = MorphlingPowerGameTests.mob(h, EntityType.HUSK, new Vec3(8.5D, 2.0D, 2.5D));
			for (int i = 0; i < 4; i++) ((IMorphling) purseStack.getItem()).onEquippedAttack(purse, purseStack, husk, 8.0F);
			husk.discard();
			h.assertTrue(BorrowedBloodReserve.get(purse) > 0.0D, "the purse holds banked blood");
			seen.clear();
			EquippedMorphlingEvents.playerTick(new PlayerTickEvent.Post(rope));
			EquippedMorphlingEvents.playerTick(new PlayerTickEvent.Post(purse));
		});
		h.runAfterDelay(12, () -> {
			SyncEquippedMorphlingPacket ropeSync = roundTrip(h, seen, rope);
			h.assertTrue(ropeSync != null, "the watcher receives the rope host's sync");
			h.assertTrue(java.util.Arrays.stream(ropeSync.linkedEntityIds()).anyMatch(id -> id == watcher.getId()),
					"the strand to the watcher survives the codec");
			h.assertTrue(java.util.Arrays.stream(ropeSync.linkedAnchors())
					.anyMatch(pos -> pos == h.absolutePos(new BlockPos(2, 1, 4)).asLong()), "the block anchor survives");
			SyncEquippedMorphlingPacket purseSync = roundTrip(h, seen, purse);
			h.assertTrue(purseSync != null && MorphlingActiveState.fill(purseSync.activeFlags()) > 0.0D,
					"the watcher sees the purse's fill");
			MorphlingPowerGameTests.done(h, rope, purse, watcher);
		});
	}

	private static SyncEquippedMorphlingPacket roundTrip(GameTestHelper h, List<SyncEquippedMorphlingPacket> seen,
			ServerPlayer source) {
		SyncEquippedMorphlingPacket sent = null;
		for (SyncEquippedMorphlingPacket packet : seen) {
			if (packet.playerUUID().equals(source.getUUID())) sent = packet;
		}
		if (sent == null) return null;
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
		SyncEquippedMorphlingPacket.encode(buf, sent);
		return SyncEquippedMorphlingPacket.decode(buf);
	}
}
