package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.world.entity.LivingEntity;

/**
 * Owns the Evening Star's fight: which act he is in, how he moves between strings, and which action comes next.
 * The action bodies (hits, telegraphs, effects) stay in {@link VesperPhaseTwoCombat}.
 */
public final class VesperEveningStarController {
	private final VesperTheEveningStarEntity boss;
	private int strafeSide = 1;
	private int strafeLeft;
	private int chainLength;
	private VesperSickleRules.SickleString sickleString;
	private VesperSickleRules.SickleString lastString = VesperSickleRules.SickleString.REAPING;
	private int stringStep;
	private int stringReaps;
	private final java.util.Map<VesperWeaponAction, Long> borrowedReadyAt = new java.util.EnumMap<>(VesperWeaponAction.class);
	private int castCooldown = 60;
	private String pendingSecond;
	private int pendingTicks;

	public VesperEveningStarController(VesperTheEveningStarEntity boss) {
		this.boss = boss;
	}

	public int strafeSide() {
		return strafeSide;
	}

	public void tick() {
		VesperBorrowedAttacks.tickPools(boss);
		VesperEveningStarAct act = VesperEveningStarAct.of(boss.getHealth(), boss.getMaxHealth());
		boss.setAct(act);
		if (act == VesperEveningStarAct.SICKLES && !boss.isRaging()) boss.enterRage();
		LivingEntity target = VesperPhaseTwoCombat.target(boss);
		if (target == null) {
			VesperPhaseTwoCombat.cancel(boss);
			endSickleString();
			return;
		}
		if (!boss.hasLineOfSight(target)) {
			if (boss.getWeaponAction() != VesperWeaponAction.NONE) {
				VesperPhaseTwoCombat.cancel(boss);
				endSickleString();
			}
			boss.getNavigation().moveTo(target, boss.isRaging() ? 1.35D : 1.0D);
			return;
		}
		// Hold one strafing side for a beat, then plant and pivot to the other, instead of jittering every few ticks.
		if (--strafeLeft <= 0) {
			strafeSide = -strafeSide;
			strafeLeft = VesperEveningStarAct.strafeHoldTicks(boss.getRandom());
		}
		if (boss.isRaging()) {
			tickSickles(target);
			return;
		}
		boss.ensureStance();
		int stanceTick = boss.advanceStanceTick();
		EnumBloodTendency tendency = boss.getActiveTendency();
		tickSpells(act, tendency, target);
		if (boss.getWeaponAction() == VesperWeaponAction.NONE) {
			VesperPhaseTwoCombat.positionForWeapon(boss, target, tendency, strafeSide);
			if (boss.getActionCooldown() > 0) {
				boss.setActionCooldown(boss.getActionCooldown() - 1);
			} else {
				VesperWeaponAction previousAction = boss.getLastWeaponAction();
				boolean stringOver = previousAction.tendency() == null
						|| previousAction.role() == VesperChainGraph.Role.FINISHER;
				if (stringOver && tryBorrowed(act, target)) return;
				VesperWeaponAction action = next(act, tendency, target);
				// Mid-string links commit at once; only openers wait for their range band.
				boolean link = action.role() != VesperChainGraph.Role.OPENER;
				if (action != VesperWeaponAction.NONE && (link || VesperPhaseTwoCombat.readyToCommit(
						action.tendency() == null ? tendency : action.tendency(), boss.distanceTo(target), action))) {
					boss.beginWeaponAction(action, VesperPhaseTwoCombat.predictedAim(target, action));
					boss.getNavigation().stop();
					chainLength++;
				}
			}
		} else {
			VesperPhaseTwoCombat.tickAction(boss, target);
		}
		if (VesperWeaponCombatRules.mayAdvanceStance(stanceTick,
				VesperCombatRules.stanceDuration(boss.getHealth(), boss.getMaxHealth()), boss.getWeaponAction())) {
			boss.advanceStance();
			chainLength = 0;
		}
	}

	/**
	 * Spells between the beats of a string. Locked: one of the stance school's own fight manipulations. Open: an
	 * authored cross-school synergy chain, the second cast landing a beat after the first.
	 */
	private void tickSpells(VesperEveningStarAct act, EnumBloodTendency tendency, LivingEntity target) {
		if (pendingSecond != null && --pendingTicks <= 0) {
			VesperManipulationCaster.cast(boss, pendingSecond, target);
			pendingSecond = null;
		}
		if (castCooldown > 0) {
			castCooldown--;
			return;
		}
		boolean betweenBeats = boss.getWeaponAction() == VesperWeaponAction.NONE && boss.getActionCooldown() > 0;
		if (!betweenBeats || boss.getRandom().nextFloat() > 0.35F) return;
		if (act == VesperEveningStarAct.LOCKED) {
			java.util.List<String> school = VesperManipulationCoverage.usable(tendency);
			if (school.isEmpty()) return;
			if (VesperManipulationCaster.cast(boss, school.get(boss.getRandom().nextInt(school.size())), target)) {
				castCooldown = 100;
			}
		} else if (act == VesperEveningStarAct.OPEN) {
			VesperSynergyChains.Chain chain = VesperSynergyChains.ALL.get(
					boss.getRandom().nextInt(VesperSynergyChains.ALL.size()));
			if (VesperManipulationCaster.cast(boss, chain.first(), target)) {
				pendingSecond = chain.second();
				pendingTicks = chain.gapTicks();
				castCooldown = 140;
			}
		}
	}

	private VesperWeaponAction next(VesperEveningStarAct act, EnumBloodTendency tendency, LivingEntity target) {
		VesperWeaponAction previous = boss.getLastWeaponAction();
		if (previous.tendency() == null || previous.role() == VesperChainGraph.Role.FINISHER) chainLength = 0;
		VesperChainGraph.Context context = new VesperChainGraph.Context(boss.distanceTo(target),
				boss.lastActionLanded(), target.isBlocking(), chainLength, boss.getRandom());
		VesperWeaponAction next = VesperChainGraph.next(previous, tendency, act, context);
		if (next.role() == VesperChainGraph.Role.OPENER) chainLength = 0;
		if (act.castsAnySchool() && next.tendency() != null && next.tendency() != tendency) boss.morphTo(next.tendency());
		return next;
	}

	private void tickSickles(LivingEntity target) {
		if (boss.getWeaponAction() != VesperWeaponAction.NONE) {
			VesperPhaseTwoCombat.tickAction(boss, target);
			return;
		}
		VesperPhaseTwoCombat.positionForRage(boss, target, strafeSide);
		if (boss.distanceTo(target) > 18.0D) return;
		if (boss.getActionCooldown() > 0) {
			boss.setActionCooldown(boss.getActionCooldown() - 1);
			return;
		}
		if (sickleString == null && tryBorrowed(VesperEveningStarAct.SICKLES, target)) return;
		VesperWeaponAction next = nextSickleMove(target);
		if (next == VesperWeaponAction.NONE) return;
		boss.beginWeaponAction(next, VesperPhaseTwoCombat.predictedAim(target, next));
		boss.getNavigation().stop();
	}

	/** Continues the current string, chains a move's own follow-up, or ends the string and picks the next one. */
	private VesperWeaponAction nextSickleMove(LivingEntity target) {
		if (VesperSickleRules.harvestReady(boss.getHealth(), boss.getMaxHealth(), boss.harvestUsed())) {
			boss.markHarvestUsed();
			startString(VesperSickleRules.SickleString.HARVEST);
		}
		VesperWeaponAction previous = boss.getLastWeaponAction();
		if (sickleString != null && stringStep > 0) {
			VesperWeaponAction follow = VesperSickleRules.followUp(previous, boss.lastActionLanded());
			if (follow != VesperWeaponAction.NONE && follow != previous) {
				stringStep++;
				return follow;
			}
		}
		if (sickleString != null) {
			VesperWeaponAction step = VesperSickleRules.branch(sickleString, stringStep, stringReaps,
					boss.lastActionLanded(), target.isBlocking(), boss.distanceTo(target), boss.getRandom());
			if (step != VesperWeaponAction.NONE) {
				stringStep++;
				return step;
			}
			// The string is over: a readable recovery that grows with its length; Harvest leaves him spent.
			boss.setActionCooldown(sickleString == VesperSickleRules.SickleString.HARVEST
					? VesperSickleRules.HARVEST_RECOVERY_TICKS : VesperSickleRules.recoveryTicks(stringStep));
			lastString = sickleString;
			sickleString = null;
			return VesperWeaponAction.NONE;
		}
		startString(VesperSickleRules.choose(boss.distanceTo(target), target.isBlocking(), cornered(target),
				lastString, boss.getRandom()));
		return nextSickleMove(target);
	}

	/** A cancelled move ends its string: the next one starts fresh instead of reeling a target he has lost. */
	private void endSickleString() {
		if (sickleString != null) lastString = sickleString;
		sickleString = null;
		stringStep = 0;
	}

	private void startString(VesperSickleRules.SickleString string) {
		sickleString = string;
		stringStep = 0;
		stringReaps = VesperSickleRules.reapCount(boss.getRandom());
	}

	/** Between strings, sometimes one of the attacks he inherited from the surviving puppets. */
	private boolean tryBorrowed(VesperEveningStarAct act, LivingEntity target) {
		java.util.List<VesperWeaponAction> attacks = VesperBorrowedAttacks.available(boss.getInheritedPuppets());
		if (attacks.isEmpty() || boss.getRandom().nextFloat() > VesperBorrowedAttacks.weight(act)) return false;
		long now = boss.level().getGameTime();
		java.util.List<VesperWeaponAction> ready = attacks.stream()
				.filter(attack -> borrowedReadyAt.getOrDefault(attack, 0L) <= now).toList();
		if (ready.isEmpty()) return false;
		VesperWeaponAction attack = ready.get(boss.getRandom().nextInt(ready.size()));
		borrowedReadyAt.put(attack, now + VesperBorrowedAttacks.COOLDOWN_TICKS);
		boss.beginWeaponAction(attack, VesperPhaseTwoCombat.predictedAim(target, attack));
		boss.getNavigation().stop();
		return true;
	}

	/** Backed against something: a wall or edge three blocks behind him. */
	private boolean cornered(LivingEntity target) {
		net.minecraft.world.phys.Vec3 back = boss.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
		if (back.lengthSqr() < 1.0E-4D) return false;
		back = back.normalize().scale(3.0D);
		return !boss.level().noCollision(boss, boss.getBoundingBox().move(back.x, 0.2D, back.z));
	}
}
