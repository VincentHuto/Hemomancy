package com.vincenthuto.hemomancy.common.entity.summon.puppeteer;

import java.util.Locale;
import java.util.Optional;

/**
 * D-37, NQ-15-8 A: the one act each taught body performs when its owner fires it from the Crossbar radial, and the
 * Ringmaster Pattern's group act (D-38, NQ-15-9 A). An act is not a strand and takes no strand form. Thread costs and
 * cooldowns are starting values, to be tuned in play.
 */
public enum PuppetAct {
	/** The Mummer pulls every threat on the owner or the owner's bodies within its reach onto itself. */
	SPOTLIGHT(PuppeteerSummonDefinitions.SCARLET_MUMMER, 8, 300),
	/** The owner and the Vulture swap places, when the Vulture's place has room for the owner. */
	TRAPEZE(PuppeteerSummonDefinitions.VEINWING_VULTURE, 10, 400),
	/** The Juggler draws the swings meant for its owner, then pins the first of them with a dagger. */
	FEINT(PuppeteerSummonDefinitions.MARROW_SPITTER, 6, 240),
	/** The Bellows breathes a cloud that hides its owner: the threats near it lose their mark. */
	SMOKE(PuppeteerSummonDefinitions.CINDER_BELLOWS, 8, 400),
	/** The Mnemonist replays its remembered wound now, without waiting for its beat. */
	ENCORE(PuppeteerSummonDefinitions.MNEMONIST_PUPPET, 10, 300),
	/** The Hound marks every threat within its nose's range so it shows through walls. */
	SCENT(PuppeteerSummonDefinitions.SANGUINE_HOUND, 6, 600),
	/** The Hulk throws its owner up and forward. */
	LIFT(PuppeteerSummonDefinitions.GOREBOUND_HULK, 8, 300),
	/** Every body within the Pattern's range performs its own act at once. */
	RING(PuppeteerSummonDefinitions.RINGMASTER_PATTERN, 30, 1200);

	private final String summon;
	private final int threadCost;
	private final int cooldownTicks;

	PuppetAct(String summon, int threadCost, int cooldownTicks) {
		this.summon = summon;
		this.threadCost = threadCost;
		this.cooldownTicks = cooldownTicks;
	}

	public String summon() {
		return summon;
	}

	public int threadCost() {
		return threadCost;
	}

	public int cooldownTicks() {
		return cooldownTicks;
	}

	public String serializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	public String nameKey() {
		return "hemomancy.summon.act." + serializedName();
	}

	public static Optional<PuppetAct> forSummon(String summon) {
		for (PuppetAct act : values()) {
			if (act.summon.equals(summon)) return Optional.of(act);
		}
		return Optional.empty();
	}
}
