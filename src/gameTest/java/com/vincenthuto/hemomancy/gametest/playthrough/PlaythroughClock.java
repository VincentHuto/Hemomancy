package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteWaveRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTargetGeometry;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * The order of a test that spans server ticks. Steps run one after another on real server ticks, driven by one
 * {@code helper.onEachTick} task; game time is never jumped. A step that finishes on its first tick hands over to the
 * next in the same tick, so instant checks between timed steps cost nothing.
 *
 * <pre>{@code
 * PlaythroughClock.start(helper)
 *     .then(() -> player.choose(vicar, "vicar_begin_initiation"))
 *     .step(player.tickPlayer(EarlyInitiation.DURATION + 1))
 *     .then(() -> helper.assertTrue(player.degree() == 1, "initiated"))
 *     .run();
 * }</pre>
 *
 * Pending blood crafts and infusions tick on the level's own tick ({@code HarbingerCardinalRiteEvents#onLevelTick}),
 * exactly as in play; nothing here ticks them again.
 *
 * {@link #run()} must be called from the test method itself: the GameTest framework schedules tick tasks in a map it
 * is iterating while ticks run, so nothing here schedules from inside a tick. When the last step finishes the test
 * succeeds; a step that throws fails it; a clock still running at the test's timeout fails with the step it was on.
 */
public final class PlaythroughClock {
	/** One stretch of a test. {@code tick} is called once per server tick with the ticks spent in the step so far. */
	@FunctionalInterface
	public interface Step {
		/** Does this tick's work; true once the step is finished. */
		boolean tick(int ticks);

		default String label() {
			return "step";
		}
	}

	private record Labelled(String label, Step step) implements Step {
		@Override public boolean tick(int ticks) { return step.tick(ticks); }
	}

	private final GameTestHelper helper;
	private final List<Step> steps = new ArrayList<>();
	private boolean started;
	/** Where a running segment inserts the steps it builds; -1 outside a segment. */
	private int insertAt = -1;
	private int index;
	private int stepTicks;
	private boolean finished;

	private PlaythroughClock(GameTestHelper helper) {
		this.helper = helper;
	}

	public static PlaythroughClock start(GameTestHelper helper) {
		return new PlaythroughClock(helper);
	}

	/** An instant step. */
	public PlaythroughClock then(Runnable action) {
		add(new Labelled("then", ticks -> {
			action.run();
			return true;
		}));
		return this;
	}

	/** A timed step built now. */
	public PlaythroughClock step(Step step) {
		add(step);
		return this;
	}

	/** A timed step built when the clock reaches it, so it can use what earlier steps made. */
	public PlaythroughClock step(Supplier<? extends Step> factory) {
		Step[] built = new Step[1];
		add(new Labelled("lazy step", ticks -> {
			if (built[0] == null) built[0] = factory.get();
			return built[0].tick(ticks);
		}));
		return this;
	}

	/** Waits {@code ticks} server ticks. */
	public PlaythroughClock idle(int ticks) {
		add(new Labelled("idle " + ticks, spent -> spent >= ticks));
		return this;
	}

	/** Runs {@code action} once per tick for {@code ticks} ticks, given the tick index. */
	public PlaythroughClock everyTick(int ticks, IntConsumer action) {
		add(new Labelled("every tick x" + ticks, spent -> {
			if (spent >= ticks) return true;
			action.accept(spent);
			return spent + 1 >= ticks;
		}));
		return this;
	}

	/** Waits until {@code done}, checked once per tick, failing with {@code what} after {@code maxTicks}. */
	public PlaythroughClock until(int maxTicks, BooleanSupplier done, String what) {
		add(new Labelled("until " + what, spent -> {
			if (done.getAsBoolean()) return true;
			if (spent >= maxTicks) helper.fail("Still waiting after " + maxTicks + " ticks: " + what);
			return false;
		}));
		return this;
	}

	/**
	 * A stretch whose steps are built when the clock reaches it: {@code build} runs then, may set things up (spawn the
	 * band's speakers, lay its blocks) and adds its steps to this clock, and they run next, before anything added after
	 * the segment. This is how one long test plays several band chains in turn, each set up only when its turn comes.
	 */
	public PlaythroughClock segment(String label, Consumer<PlaythroughClock> build) {
		add(new Labelled("segment " + label, ticks -> {
			insertAt = index + 1;
			try {
				build.accept(this);
			} finally {
				insertAt = -1;
			}
			return true;
		}));
		return this;
	}

	private void add(Step step) {
		if (insertAt >= 0) steps.add(insertAt++, step);
		else steps.add(step);
	}

	/** Starts the clock. Call once, from the test method. The test succeeds when the last step finishes. */
	public void run() {
		if (started) throw new IllegalStateException("The clock is already running");
		started = true;
		int timeout = helper.testInfo.getTimeoutTicks();
		helper.onEachTick(() -> {
			if (finished) return;
			while (index < steps.size()) {
				Step step = steps.get(index);
				boolean done = step.tick(stepTicks);
				if (!done) {
					stepTicks++;
					break;
				}
				boolean instant = stepTicks == 0;
				index++;
				stepTicks = 0;
				if (!instant) break;
			}
			if (index >= steps.size()) {
				finished = true;
				helper.succeed();
				return;
			}
			if (helper.getTick() >= timeout - 1) {
				helper.fail("Timed out at step " + (index + 1) + "/" + steps.size() + " ("
						+ steps.get(index).label() + ") after " + stepTicks + " ticks in it");
			}
		});
	}

	// --- Static conveniences ---------------------------------------------------------------------------------------

	/** Runs {@code check} after {@code ticks} ticks. Call from the test method only. */
	public static void afterTicks(GameTestHelper helper, int ticks, Runnable check) {
		helper.runAfterDelay(ticks, check);
	}

	/** Runs {@code action} each tick for {@code ticks} ticks, then {@code then}. Call from the test method only. */
	public static void everyTick(GameTestHelper helper, int ticks, IntConsumer action, Runnable then) {
		start(helper).everyTick(ticks, action).then(then).run();
	}

	// --- Cardinal Rites --------------------------------------------------------------------------------------------

	/** A rite driven as {@link #rite(PlaythroughPlayer, BlockPos, int, BiFunction)} with no extra targets. */
	public static Step rite(PlaythroughPlayer player, BlockPos focusPos) {
		return rite(player, focusPos, 12000, (rite, recipe) -> null);
	}

	/**
	 * A Cardinal Rite played through: the player plants the Living Staff they hold on the focus (a real
	 * {@code useItemOn}; the rite must start), then holds Blood Projection (it must be in their inventory) at whatever
	 * the rite hungers for, each tick: an open anchor first, then {@code extraAim} (a sigil node or another target a
	 * band test knows), then the rite's daemon to seal it. Between targets the projection is released and the rite
	 * ticks on its own. The step ends when {@link CardinalRiteSavedData} holds no rite for the player, and fails after
	 * {@code maxTicks}. It never calls {@code CardinalRiteCompletion.complete}. The rite, its threats and the escrowed
	 * Staff are cleared when the test ends.
	 */
	public static Step rite(PlaythroughPlayer player, BlockPos focusPos, int maxTicks,
			BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> extraAim) {
		return rite(player, focusPos, maxTicks, extraAim, true, () -> false);
	}

	/**
	 * {@link #rite(PlaythroughPlayer, BlockPos, int, BiFunction)} with two controls for interruption tests: with
	 * {@code plant} false the step takes up a rite already running for the player (after a relog, say) instead of
	 * planting the Staff, and when {@code pauseWhen} turns true (checked each tick once the rite runs) the projection
	 * is let go and the step ends with the rite still running.
	 */
	public static Step rite(PlaythroughPlayer player, BlockPos focusPos, int maxTicks,
			BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> extraAim, boolean plant, BooleanSupplier pauseWhen) {
		RitePlan plan = RitePlan.standard().maxTicks(maxTicks).extraAim(extraAim).pauseWhen(pauseWhen);
		return rite(player, focusPos, plant ? plan : plan.takeUp());
	}

	/**
	 * A Cardinal Rite played through as {@code plan} says (see {@link RitePlan}). Each tick while the rite runs, in
	 * this order: the {@code onPhase} hook for the current phase; let go during a still interval once no anchor needs
	 * repair ({@code releaseOnStill}); attack the trial body ({@code trialPuppet}); Projection into an anchor that needs
	 * blood (siphoned anchors included); attack the nearest live rite threat ({@code fightThreats}); Projection at
	 * {@code extraAim} (by default the required support sigils' nodes); Projection at the daemon. The step ends when
	 * no rite is left for the player, and fails after {@code maxTicks} (by default {@link PlaythroughRites#defaultTicks}).
	 * It never calls {@code CardinalRiteCompletion.complete}. The rite, its threats and the escrowed Staff are cleared
	 * when the test ends.
	 */
	/** Past this many blocks from the eye a rite target is walked up to before projecting (Projection reaches 5.5). */
	private static final double WALK_IN_RANGE = 5.0D;

	public static Step rite(PlaythroughPlayer player, BlockPos focusPos, RitePlan plan) {
		GameTestHelper helper = player.helper();
		PlaythroughCleanup.onEnd(helper, () -> clearRite(player));
		BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> extraAim = plan.extraAim() != null ? plan.extraAim()
				: PlaythroughRites.supportSigilNodes(player);
		return new Step() {
			private boolean planted;
			private boolean projecting;
			private Outcome.Probe probe;
			private int limit = plan.maxTicks();
			private CardinalRitePhase phase;
			private int phaseTicks;
			private boolean walkedIn;

			private void release() {
				if (projecting) player.releaseHold();
				projecting = false;
			}

			@Override
			public boolean tick(int ticks) {
				CardinalRiteSavedData saved = CardinalRiteSavedData.get(player.player().serverLevel());
				if (!planted && !plan.plant()) {
					planted = true;
					probe = Outcome.begin(player, null, "rite at " + focusPos.toShortString() + " (taken up)");
				}
				if (!planted) {
					planted = true;
					probe = Outcome.begin(player, null, "rite at " + focusPos.toShortString());
					helper.assertTrue(player.player().getMainHandItem().is(ItemInit.living_staff.get()),
							"the player holds a Living Staff to plant on the focus");
					Outcome use = player.useOn(focusPos);
					helper.assertTrue(saved.getRite(player.uuid()) != null,
							"planting the Staff starts a rite at " + focusPos.toShortString() + "; " + use.describe());
					return false;
				}
				ActiveCardinalRite rite = saved.getRite(player.uuid());
				if (rite == null) {
					release();
					probe.finish();
					return true;
				}
				if (plan.pauseWhen().getAsBoolean()) {
					release();
					probe.finish();
					return true;
				}
				CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(player.player().level(), rite.getRecipeId());
				if (limit <= 0) limit = PlaythroughRites.defaultTicks(recipe);
				if (ticks > limit) {
					HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(player.player().serverLevel(),
							player.uuid(), rite.getCenterPos());
					helper.fail("The rite at " + focusPos.toShortString() + " is still running after " + limit
							+ " ticks, phase " + rite.getPhase() + " (anchors consecrated=" + rite.areAnchorsConsecrated()
							+ ", sigil progress " + rite.getSigilProgress() + ", awakened " + rite.getAwakenedSigils()
							+ ", sacrifice " + rite.getSacrificeMl() + " mL, allies " + rite.getAllyRoles()
							+ ", daemon " + (daemon == null ? "none" : daemon.position()) + ", blood " + player.blood()
							+ ", at " + player.player().position() + ", next aim " + extraAim.apply(rite, recipe)
							+ ", hungry anchors " + hungryAnchors(rite, recipe) + ", threats " + rite.getRiteThreats().size()
							+ ", instability " + rite.getInstability() + ", "
							+ PlaythroughRites.describeAim(player, rite, recipe) + "); "
							+ player.chat());
				}
				if (rite.getPhase() != phase) {
					phase = rite.getPhase();
					phaseTicks = 0;
				}
				var hook = plan.onPhase().get(phase);
				int inPhase = phaseTicks++;
				if (hook != null) hook.accept(rite, inPhase);
				if (plan.releasesOnStill() && phase == CardinalRitePhase.STILL_INTERVAL && anchorAim(player, rite, recipe) == null) {
					release();
					return false;
				}
				if (plan.attacksTrialPuppet() && phase == CardinalRitePhase.PUPPET_TRIAL) {
					var trial = rite.puppeteerTrial();
					if (trial != null && trial.isManifested() && !trial.isDefeated() && trial.entityId() != null) {
						Entity body = player.player().serverLevel().getEntity(trial.entityId());
						if (body != null && body.isAlive()) {
							release();
							strike(player, body, plan);
							return false;
						}
					}
				}
				if (plan.fightsThreats() && phase == CardinalRitePhase.ORDEAL && defeatWave(rite)) {
					// "Defeat the puppeteers within 18s or the rite fails": a player kills them before tending anchors.
					Entity threat = nearestThreat(player, rite);
					if (threat != null) {
						release();
						strike(player, threat, plan);
						if (inPhase % 100 == 0) {
							PlaythroughLog.note(helper, "DEFEAT wave: struck " + PlaythroughPlayer.describe(threat) + " at "
									+ Math.round(threat.distanceTo(player.player())) + " blocks, health "
									+ (threat instanceof net.minecraft.world.entity.LivingEntity living ? living.getHealth() : -1)
									+ ", " + rite.getRiteThreats().size() + " threats left");
						}
						return false;
					}
					if (!rite.getRiteThreats().isEmpty() && inPhase % 100 == 0) {
						PlaythroughLog.note(helper, rite.getRiteThreats().size() + " rite threats of a DEFEAT wave are not live in "
								+ "the level (unloaded or gone); nothing to strike");
					}
				}
				Vec3 aim = plan.tracesFirst() && phase == CardinalRitePhase.ORDEAL ? extraAim.apply(rite, recipe) : null;
				if (aim == null) aim = anchorAim(player, rite, recipe);
				if (aim == null && plan.fightsThreats()) {
					Entity threat = nearestThreat(player, rite);
					if (threat != null) {
						release();
						strike(player, threat, plan);
						return false;
					}
				}
				if (aim == null) aim = extraAim.apply(rite, recipe);
				if (aim == null) {
					HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(player.player().serverLevel(),
							player.uuid(), rite.getCenterPos());
					aim = daemon == null ? null : daemon.position();
				}
				if (aim == null) {
					release();
					return false;
				}
				if (player.player().getEyePosition().distanceTo(aim) > WALK_IN_RANGE) {
					// ARRANGE: a target past Projection's reach (5.5 blocks) is walked up to, along the line from the
					// target toward the rite's centre so the player stays inside the circle.
					Vec3 centre = Vec3.atBottomCenterOf(rite.getCenterPos());
					Vec3 inward = new Vec3(centre.x - aim.x, 0.0D, centre.z - aim.z);
					// A target over the centre (the daemon) is walked up to from the player's own side instead.
					double reach = 2.5D;
					if (inward.lengthSqr() <= 1.0E-6D) {
						inward = new Vec3(player.player().getX() - aim.x, 0.0D, player.player().getZ() - aim.z);
						reach = 2.0D;
					}
					if (inward.lengthSqr() > 1.0E-6D) {
						Vec3 spot = aim.add(inward.normalize().scale(reach));
						player.moveTo(new Vec3(spot.x, player.player().getY(), spot.z));
						if (!walkedIn) PlaythroughLog.note(helper, "walked within reach of the rite target at " + aim);
						walkedIn = true;
					}
				}
				if (!projecting) {
					player.startHold(ItemInit.blood_projection.get(), aim);
					projecting = true;
				} else {
					player.aim(aim);
				}
				player.holdTick();
				return false;
			}

			@Override
			public String label() {
				return "rite at " + focusPos.toShortString();
			}
		};
	}

	/** One swing at {@code target}, with the plan's weapon when the player carries it. */
	private static void strike(PlaythroughPlayer player, Entity target, RitePlan plan) {
		if (plan.weapon() != null && player.count(plan.weapon()) > 0) player.wield(plan.weapon());
		player.attack(target);
	}

	/** Whether the ordeal's current wave must be won by killing its threats (the false omens' puppeteers). */
	private static boolean defeatWave(ActiveCardinalRite rite) {
		return rite.getCurrentWave() < rite.getWaveDeck().size() && CardinalRiteWaveRules.objective(
				rite.getWaveDeck().get(rite.getCurrentWave())) == CardinalRiteWaveRules.Objective.DEFEAT;
	}

	/** The live rite threat nearest the player, or null. */
	private static Entity nearestThreat(PlaythroughPlayer player, ActiveCardinalRite rite) {
		var level = player.player().serverLevel();
		Entity nearest = null;
		double best = Double.MAX_VALUE;
		for (java.util.UUID id : rite.getRiteThreats()) {
			Entity entity = level.getEntity(id);
			if (entity == null || !entity.isAlive()) continue;
			double distance = entity.distanceToSqr(player.player());
			if (distance < best) {
				best = distance;
				nearest = entity;
			}
		}
		return nearest;
	}

	/** An open anchor's aim point (an anchor that needs blood or instability repair), or null. */
	private static Vec3 anchorAim(PlaythroughPlayer player, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (recipe == null || recipe.getCeremony() == null) return null;
		var anchors = recipe.getCeremony().anchors();
		// Dry anchors that are not flickering first: while stations run dry the deficit keeps pressing instability
		// back onto the threatened anchor, so repairing it alone never ends (seen on the Rite of Apotheos's ring).
		for (int index = 0; index < anchors.size(); index++) {
			if (rite.bloodNeededForAnchor(index) > 0 && rite.instabilityRepairBloodNeeded(index) <= 0) {
				return CardinalRiteTargetGeometry.anchorAimPoint(rite.getCenterPos(), anchors.get(index).offset());
			}
		}
		for (int index = 0; index < anchors.size(); index++) {
			if (rite.bloodNeededForAnchor(index) > 0 || rite.instabilityRepairBloodNeeded(index) > 0) {
				return CardinalRiteTargetGeometry.anchorAimPoint(rite.getCenterPos(), anchors.get(index).offset());
			}
		}
		return null;
	}

	/** Each anchor that still asks for blood: index@offset need/repair (stall diagnostics). */
	private static String hungryAnchors(ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (recipe == null || recipe.getCeremony() == null) return "[]";
		List<String> out = new java.util.ArrayList<>();
		var anchors = recipe.getCeremony().anchors();
		for (int index = 0; index < anchors.size(); index++) {
			int need = rite.bloodNeededForAnchor(index);
			int repair = rite.instabilityRepairBloodNeeded(index);
			if (need > 0 || repair > 0) out.add(index + "@" + anchors.get(index).offset().toShortString() + " " + need + "/" + repair);
		}
		return out.toString();
	}

	private static void clearRite(PlaythroughPlayer player) {
		var level = player.player().serverLevel();
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(level);
		ActiveCardinalRite rite = saved.getRite(player.uuid());
		if (rite == null) return;
		CardinalRiteOrdealEngine.clearThreats(level, rite);
		CardinalRiteStaffEscrow.restore(player.player(), rite);
		saved.removeRite(player.uuid());
		HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(level, player.uuid(), rite.getCenterPos());
		if (daemon != null) daemon.discard();
	}
}
