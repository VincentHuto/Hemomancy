package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperManipulationCoverage;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.DrudgeAction;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.saint.BloomOfRotManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.BloomOfRotRules;
import com.vincenthuto.hemomancy.common.manipulation.saint.CanonGiftEvents;
import com.vincenthuto.hemomancy.common.manipulation.saint.CanonGiftState;
import com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.EndlessHourRules;
import com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheRules;
import com.vincenthuto.hemomancy.common.manipulation.saint.UnclosingEyeManip;
import com.vincenthuto.hemomancy.common.manipulation.saint.UnclosingEyeRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * D-1 (canon memory rework spec 3.1 and 3.2): Crimson Tithe and the Unclosing Eye as the player's gifts. Each test
 * runs in its own batch, since a ten-block chain or a 24-block beam would reach a neighbour's mobs.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CanonGiftGameTests {
	private static final String EMPTY = "bastion/mobs/empty";

	private CanonGiftGameTests() {
	}

	/** Spec 2.7, NQ-08-1 A and NQ-08-5: the gifts' registrations, player only. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "canon_gift_registry")
	public static void theGiftsArePerfectusAtDegreeEightAndPlayerOnly(GameTestHelper h) {
		for (String name : List.of("crimson_tithe", "unclosing_eye", "bloom_of_rot", "endless_hour")) {
			BloodManipulation gift = ManipulationInit.getByName(name);
			h.assertTrue(gift.getRank() == EnumManipulationRank.PERFECTUS, name + " is " + gift.getRank());
			h.assertTrue(gift.getRequiredDegree() == 8, name + " keeps its D8 floor");
		}
		BloodManipulation tithe = ManipulationInit.crimson_tithe.get(), eye = ManipulationInit.unclosing_eye.get();
		h.assertTrue(tithe.getType() == EnumManipulationType.CONTINUOUS && tithe.getCooldownTicks() == CrimsonTitheRules.COOLDOWN_TICKS,
				"the Tithe is a held drink with a 400-tick cooldown");
		h.assertTrue(eye.getType() == EnumManipulationType.CHARGED && eye.getSection() == EnumVeinSections.HEART
				&& eye.getRequiredChargeTicks() == UnclosingEyeRules.FULL_CHARGE, "the Eye is charged, in the heart");
		BloodManipulation bloom = ManipulationInit.bloom_of_rot.get();
		h.assertTrue(bloom.getType() == EnumManipulationType.QUICK && bloom.getCost() == BloomOfRotRules.SOW_COST
				&& bloom.getCooldownTicks() == BloomOfRotRules.COOLDOWN_TICKS, "the Bloom is a quick sowing at 450 with a 600-tick cooldown");
		BloodManipulation hour = ManipulationInit.endless_hour.get();
		h.assertTrue(hour.getType() == EnumManipulationType.QUICK && hour.getCost() == EndlessHourRules.PLANT_COST
				&& hour.getCooldownTicks() == EndlessHourRules.COOLDOWN_TICKS, "the Hour is a quick planting at 600 with a 900-tick cooldown");
		for (BloodManipulation gift : List.of(tithe, eye, bloom, hour)) {
			h.assertTrue(gift.getEntityCast().isEmpty(), gift.getName() + " has no entity cast");
			h.assertTrue(gift.getDrudgeAction().orElse(null) == DrudgeAction.DRUDGE_UNSUPPORTED, gift.getName() + " is no Drudge's");
			var entry = VesperManipulationCoverage.entry(gift.getName());
			h.assertTrue(entry != null && entry.coverage() == VesperManipulationCoverage.Coverage.EXCLUDED,
					gift.getName() + " is excluded from Vesper's coverage");
		}
		h.succeed();
	}

	/** Spec 3.1: latch, draw, leak, then the Vessel Empties into its lanes and spares the wedges. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "canon_gift_tithe", timeoutTicks = 40)
	public static void crimsonTitheLatchesDrawsLeaksAndEmptiesIntoItsLanes(GameTestHelper h) {
		ServerPlayer p = player(h);
		CrimsonTitheManip tithe = (CrimsonTitheManip) ManipulationInit.crimson_tithe.get();
		ready(p, tithe);
		// facing +Z: the first lane runs straight ahead, and due east sits in the wedge between two lanes
		Zombie ahead = zombie(h, p.position().add(0, 0, 3));
		Zombie wedge = zombie(h, p.position().add(2.5D, 0, 0));
		try {
			var blood = HemoCapabilityAccess.getBloodVolume(p);
			double before = blood.getBloodVolume();
			ManipulationChannelManager.start(p);
			CanonGiftState.TitheHold hold = CanonGiftState.tithe(p.getUUID());
			h.assertTrue(hold != null && hold.chains.size() == 2, "both bodies were chained: " + (hold == null ? 0 : hold.chains.size()));
			h.assertTrue(before - blood.getBloodVolume() >= CrimsonTitheRules.OPEN_COST, "the vessel cost its opening");
			h.assertTrue(ahead.getHealth() < ahead.getMaxHealth() && SchoolStates.has(ahead, SchoolState.PRESSURE),
					"the latch struck and laid Pressure");
			for (int i = 0; i < 20; i++) ManipulationChannelManager.onPlayerTick(new PlayerTickEvent.Post(p));
			h.assertTrue(hold.vessel > 0 && hold.leaked > 0, "the chains drank and the vessel leaked: " + hold.vessel + " / " + hold.leaked);
			h.assertTrue(hold.vessel < 4 * 2 * CrimsonTitheRules.BLOOD_DRAW, "it leaked while it drank");
			float aheadBefore = ahead.getHealth(), wedgeBefore = wedge.getHealth();
			double kept = blood.getBloodVolume();
			ManipulationChannelManager.stop(p, true);
			h.assertTrue(CanonGiftState.tithe(p.getUUID()) == null, "letting go empties the vessel");
			h.assertTrue(ahead.getHealth() < aheadBefore && SchoolStates.has(ahead, SchoolState.LODESTONE),
					"the lane ahead struck and marked Lodestone");
			h.assertTrue(!SchoolStates.has(ahead, SchoolState.PRESSURE), "the release consumed the chains' Pressure");
			h.assertTrue(wedge.getHealth() == wedgeBefore, "the wedge between two lanes is safe");
			h.assertTrue(blood.getBloodVolume() <= kept, "no blood comes back to the caster (NQ-08-6)");
			h.assertTrue(tithe.isOnCooldown(p), "the cooldown starts from the release");
			h.succeed();
		} finally {
			CanonGiftState.end(p.getUUID());
			ahead.discard();
			wedge.discard();
			p.discard();
		}
	}

	/** Spec 3.1 item 6: a full vessel held twenty ticks bursts on the caster. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "canon_gift_overfill", timeoutTicks = 40)
	public static void anOverfilledVesselBurstsOnTheCaster(GameTestHelper h) {
		ServerPlayer p = player(h);
		CrimsonTitheManip tithe = (CrimsonTitheManip) ManipulationInit.crimson_tithe.get();
		ready(p, tithe);
		Zombie fed = zombie(h, p.position().add(0, 0, 3));
		try {
			ManipulationChannelManager.start(p);
			CanonGiftState.TitheHold hold = CanonGiftState.tithe(p.getUUID());
			h.assertTrue(hold != null, "the vessel opened");
			hold.vessel = CrimsonTitheRules.CAPACITY;
			var blood = HemoCapabilityAccess.getBloodVolume(p);
			double before = blood.getBloodVolume();
			for (int i = 0; i < CrimsonTitheRules.OVERFILL_TICKS + 5 && CanonGiftState.tithe(p.getUUID()) != null; i++)
				ManipulationChannelManager.onPlayerTick(new PlayerTickEvent.Post(p));
			h.assertTrue(CanonGiftState.tithe(p.getUUID()) == null, "the overfilled vessel released itself");
			double burst = blood.getMaxBloodVolume() * CrimsonTitheRules.OVERFILL_BLOOD_SHARE;
			h.assertTrue(before - blood.getBloodVolume() >= burst - 0.01D, "the burst cost 5% of max blood: " + (before - blood.getBloodVolume()));
			h.assertTrue(p.getHealth() < p.getMaxHealth(), "and struck the caster");
			h.succeed();
		} finally {
			CanonGiftState.end(p.getUUID());
			fed.discard();
			p.discard();
		}
	}

	/** NQ-08-12: the drink takes a blood-active player's blood and an inactive player's health, only where canHarm allows. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "canon_gift_pvp", timeoutTicks = 40)
	public static void theDrawTakesBloodFromTheActiveAndHealthFromTheInactive(GameTestHelper h) {
		var server = h.getLevel().getServer();
		boolean pvp = server.isPvpAllowed();
		ServerPlayer caster = player(h);
		ServerPlayer active = player(h), inactive = player(h);
		CrimsonTitheManip tithe = (CrimsonTitheManip) ManipulationInit.crimson_tithe.get();
		try {
			server.setPvpAllowed(true);
			var activeBlood = HemoCapabilityAccess.getBloodVolume(active);
			activeBlood.setBloodVolume(1000);
			HemoCapabilityAccess.getBloodVolume(inactive).setActive(false);
			float activeHealth = active.getHealth(), inactiveHealth = inactive.getHealth();
			double inactiveBlood = HemoCapabilityAccess.getBloodVolume(inactive).getBloodVolume();
			h.assertTrue(tithe.drink(caster, active) > 0, "the chain drank from the blood-active player");
			h.assertTrue(Math.abs(1000 - CrimsonTitheRules.PLAYER_BLOOD_TAKEN - activeBlood.getBloodVolume()) < 0.01D
					&& active.getHealth() == activeHealth, "a blood-active player pays in blood, not health");
			h.assertTrue(tithe.drink(caster, inactive) > 0, "the chain drank from the inactive player");
			h.assertTrue(inactive.getHealth() < inactiveHealth
					&& HemoCapabilityAccess.getBloodVolume(inactive).getBloodVolume() == inactiveBlood,
					"an inactive player pays in health, never blood");
			server.setPvpAllowed(false);
			double held = activeBlood.getBloodVolume();
			h.assertTrue(tithe.drink(caster, active) == 0 && activeBlood.getBloodVolume() == held, "no drink where canHarm refuses");
			h.succeed();
		} finally {
			server.setPvpAllowed(pvp);
			caster.discard();
			active.discard();
			inactive.discard();
		}
	}

	/**
	 * Spec 3.2: the beam burns and grows an enemy to its Burst, heals a hurt ally, then the eye lapses: Illuminated,
	 * no Lux casts, and the cooldown from the beam's end.
	 */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "canon_gift_eye", timeoutTicks = 80)
	public static void theUnclosingEyeGrowsBurstsHealsAndLapses(GameTestHelper h) {
		ServerPlayer p = player(h);
		UnclosingEyeManip eye = (UnclosingEyeManip) ManipulationInit.unclosing_eye.get();
		ready(p, eye);
		Zombie enemy = zombie(h, p.position().add(0, 0, 8));
		Wolf ally = EntityType.WOLF.create(h.getLevel());
		ally.setNoAi(true);
		ally.setNoGravity(true);
		ally.tame(p);
		ally.setPos(p.position().add(0, 0.5D, 4));
		h.getLevel().addFreshEntity(ally);
		ally.setHealth(3);
		eye.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), UnclosingEyeRules.FULL_CHARGE);
		h.assertTrue(CanonGiftState.beam(p.getUUID()) != null, "the beam opened");
		h.onEachTick(() -> CanonGiftEvents.onPlayerTick(new PlayerTickEvent.Post(p)));
		h.runAtTickTime(UnclosingEyeRules.BEAM_TICKS + 10, () -> {
			try {
				h.assertTrue(CanonGiftState.beam(p.getUUID()) == null, "the beam closed after 40 ticks");
				h.assertTrue(enemy.getHealth() < 100, "the beam burned the enemy");
				h.assertTrue(UnclosingEyeManip.growthOf(enemy) == UnclosingEyeRules.AFTER_BURST,
						"eight pulses grew it to its Burst and back to 2: " + UnclosingEyeManip.growthOf(enemy));
				h.assertTrue(ally.getHealth() > 3, "the hurt ally was healed: " + ally.getHealth());
				h.assertTrue(UnclosingEyeManip.inLapse(p) && SchoolStates.has(p, SchoolState.ILLUMINATED),
						"the caster lapses and is laid bare");
				h.assertTrue(eye.isOnCooldown(p), "the cooldown runs from the beam's end");
				BloodManipulation flare = ManipulationInit.hematic_flare.get();
				ready(p, flare);
				double blood = HemoCapabilityAccess.getBloodVolume(p).getBloodVolume();
				h.assertTrue(!flare.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0)
						&& HemoCapabilityAccess.getBloodVolume(p).getBloodVolume() == blood, "no Lux cast answers in the Lapse");
				h.succeed();
			} finally {
				CanonGiftState.end(p.getUUID());
				enemy.discard();
				ally.discard();
				p.discard();
			}
		});
	}

	/**
	 * Spec 3.3: the sowing costs 450 and starts no cooldown while the field lives; the rot colonises a body on it and
	 * the caster too; a press below 40 Kindling is refused free; the Ignition is free, burns the colonisation off with
	 * Searing, and the cooldown runs from the field's end.
	 */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "canon_gift_bloom", timeoutTicks = 200)
	public static void bloomOfRotColonisesThenBurnsUnderTheSecondPress(GameTestHelper h) {
		ServerPlayer p = player(h);
		BloomOfRotManip bloom = (BloomOfRotManip) ManipulationInit.bloom_of_rot.get();
		ready(p, bloom);
		Zombie standing = zombie(h, p.position().add(0, 0, 3));
		Wolf ally = EntityType.WOLF.create(h.getLevel());
		ally.setNoAi(true);
		ally.tame(p);
		ally.setPos(p.position().add(2, 0, 0));
		h.getLevel().addFreshEntity(ally);
		var blood = HemoCapabilityAccess.getBloodVolume(p);
		double before = blood.getBloodVolume();
		h.assertTrue(bloom.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "the rot was sown");
		h.assertTrue(before - blood.getBloodVolume() >= BloomOfRotRules.SOW_COST - 0.01D, "the sowing cost 450");
		h.assertTrue(CanonGiftState.bloom(p.getUUID()) != null && !bloom.isOnCooldown(p), "no cooldown while the field lives");
		h.onEachTick(() -> CanonGiftEvents.onPlayerTick(new PlayerTickEvent.Post(p)));
		h.runAtTickTime(85, () -> {
			h.assertTrue(BloomOfRotManip.colonisationOf(standing) >= 4, "four pulses on a still body colonised it: "
					+ BloomOfRotManip.colonisationOf(standing));
			h.assertTrue(standing.getHealth() < 100 && SchoolStates.has(standing, SchoolState.NECROSIS), "the rot struck and laid Necrosis");
			h.assertTrue(BloomOfRotManip.colonisationOf(p) >= 1, "the caster is colonised by their own field (NQ-08-8)");
			h.assertTrue(ally.getHealth() == ally.getMaxHealth() && BloomOfRotManip.colonisationOf(ally) == 0,
					"an owned wolf is spared the rot (canHarm)");
			CanonGiftState.BloomField field = CanonGiftState.bloom(p.getUUID());
			field.kindling = 10;
			double held = blood.getBloodVolume();
			h.assertTrue(!bloom.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0) && !field.lit(),
					"the press below 40 Kindling does not light it");
			field.kindling = 60;
			h.assertTrue(!bloom.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0) && field.lit(),
					"the second press lights the field");
			h.assertTrue(blood.getBloodVolume() == held, "the presses on a live field are free");
		});
		h.runAtTickTime(85 + BloomOfRotRules.IGNITION_SPREAD + 2, () -> {
			h.assertTrue(BloomOfRotManip.colonisationOf(standing) == 0 && SchoolStates.has(standing, SchoolState.SEARING),
					"the fire burned the colonisation off and seared the body");
			h.assertTrue(BloomOfRotManip.colonisationOf(p) == 0, "the caster's own rot burned off with it");
		});
		h.runAtTickTime(85 + BloomOfRotRules.CHAR_TICKS + 2, () -> {
			try {
				h.assertTrue(CanonGiftState.bloom(p.getUUID()) == null, "the field charred out");
				h.assertTrue(bloom.isOnCooldown(p), "the cooldown runs from the field's end");
				h.succeed();
			} finally {
				CanonGiftState.end(p.getUUID());
				standing.discard();
				ally.discard();
				p.discard();
			}
		});
	}

	/** Spec 3.3 items 4 and 6: fire on the field feeds Kindling two and a half times over; an unlit field reclaims its caster. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "canon_gift_bloom_reclaim", timeoutTicks = 40)
	public static void fireFeedsTheKindlingAndAnUnlitFieldReclaimsItsCaster(GameTestHelper h) {
		ServerPlayer p = player(h);
		BloomOfRotManip bloom = (BloomOfRotManip) ManipulationInit.bloom_of_rot.get();
		ready(p, bloom);
		Zombie burning = zombie(h, p.position().add(0, 0, 3));
		try {
			long now = h.getLevel().getGameTime();
			var field = new CanonGiftState.BloomField(p.position(), now - BloomOfRotRules.FIELD_TICKS + 1, h.getLevel().dimension());
			field.lastCasterGain = now;
			CanonGiftState.sow(p.getUUID(), field);
			burning.invulnerableTime = 0;
			burning.hurt(h.getLevel().damageSources().onFire(), 3.0F);
			h.assertTrue(field.kindling > 2.0D && field.kindling <= BloomOfRotRules.FLAMMEUS_KINDLING + 1.0E-6D,
					"about three fire damage on the field fed about 2.5 Kindling, not 1: " + field.kindling);
			h.runAtTickTime(2, () -> {
				CanonGiftEvents.onPlayerTick(new PlayerTickEvent.Post(p));
				h.assertTrue(CanonGiftState.bloom(p.getUUID()) == null, "the field ended unlit");
				h.assertTrue(BloomOfRotManip.colonisationOf(p) >= BloomOfRotRules.RECLAIM_COLONISATION
						&& SchoolStates.has(p, SchoolState.NECROSIS), "the rot reclaimed its caster");
				h.assertTrue(bloom.isOnCooldown(p), "the cooldown runs from the field's end");
				h.succeed();
			});
		} finally {
			h.runAtTickTime(3, () -> {
				CanonGiftState.end(p.getUUID());
				burning.discard();
				p.discard();
			});
		}
	}

	/**
	 * Spec 3.4: the planting costs 600 and 15 a second; a still body fills and is set in its cruor; the dark rim lays
	 * Obscured; the planted caster is guarded; at the close the still are set and the caster sets in their own cruor,
	 * and the cooldown runs from the end.
	 */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "canon_gift_hour", timeoutTicks = 260)
	public static void theEndlessHourSetsTheStillAndThenItsCaster(GameTestHelper h) {
		ServerPlayer p = player(h);
		EndlessHourManip hour = (EndlessHourManip) ManipulationInit.endless_hour.get();
		ready(p, hour);
		Zombie still = zombie(h, p.position().add(0, 0, 3));
		Zombie rim = zombie(h, p.position().add(0, 0, 7.5D));
		var blood = HemoCapabilityAccess.getBloodVolume(p);
		double before = blood.getBloodVolume();
		h.assertTrue(hour.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0), "the Hour was planted");
		h.assertTrue(before - blood.getBloodVolume() >= EndlessHourRules.PLANT_COST - 0.01D, "the planting cost 600");
		h.assertTrue(CanonGiftState.hour(p.getUUID()) != null && !hour.isOnCooldown(p), "no cooldown while the Hour holds");
		double planted = blood.getBloodVolume();
		h.onEachTick(() -> CanonGiftEvents.onPlayerTick(new PlayerTickEvent.Post(p)));
		h.runAtTickTime(41, () -> {
			h.assertTrue(planted - blood.getBloodVolume() >= 2 * EndlessHourRules.UPKEEP - 0.01D, "the Hour paid its upkeep");
			h.assertTrue(SchoolStates.has(rim, SchoolState.OBSCURED), "the dark rim obscured the body standing in it");
			float health = p.getHealth();
			p.invulnerableTime = 0;
			p.hurt(p.damageSources().magic(), 5.0F);
			h.assertTrue(Math.abs(health - p.getHealth() - 5.0F * (1.0F - EndlessHourRules.STILL_GUARD)) < 0.05F,
					"the planted caster took 40% less: " + (health - p.getHealth()));
			h.assertTrue(CanonGiftState.hour(p.getUUID()) != null, "a light wound does not bow the caster");
		});
		h.runAtTickTime(100, () -> {
			h.assertTrue(still.getHealth() < 100 && SchoolStates.levels(still, SchoolState.RIME) == EndlessHourRules.PRESERVE_RIME
					&& still.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.paralysis),
					"a body that stood still was set in its cruor: Rime 3, Paralysis and the cut");
			h.assertTrue(EndlessHourManip.stillnessOf(still) < EndlessHourRules.MAX_STILLNESS, "its stillness fell back after the set");
		});
		h.runAtTickTime(EndlessHourRules.HOUR_TICKS + 3, () -> {
			try {
				h.assertTrue(CanonGiftState.hour(p.getUUID()) == null, "the Hour closed");
				h.assertTrue(p.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.paralysis),
						"the caster sets in their own cruor (NQ-08-10)");
				h.assertTrue(EndlessHourManip.taken(p) < 0.5D, "and takes 70% less while set");
				h.assertTrue(hour.isOnCooldown(p), "the cooldown runs from the Hour's end");
				h.succeed();
			} finally {
				CanonGiftState.end(p.getUUID());
				still.discard();
				rim.discard();
				p.discard();
			}
		});
	}

	/** Spec 3.4 items 1 and 7: stepping off the point ends the Hour with no close; a heavy wound breaks it into the Bow. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY, batch = "canon_gift_hour_break", timeoutTicks = 40)
	public static void steppingOffEndsTheHourAndAHeavyWoundBowsTheCaster(GameTestHelper h) {
		ServerPlayer p = player(h);
		EndlessHourManip hour = (EndlessHourManip) ManipulationInit.endless_hour.get();
		ready(p, hour);
		try {
			hour.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
			p.moveTo(p.position().add(2, 0, 0));
			CanonGiftEvents.onPlayerTick(new PlayerTickEvent.Post(p));
			h.assertTrue(CanonGiftState.hourRecord(p.getUUID()) == null, "stepping off the point ended the Hour at once");
			h.assertTrue(!p.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.paralysis), "with no close");
			h.assertTrue(hour.isOnCooldown(p), "and the cooldown runs from then");
			hour.getAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition());
			p.invulnerableTime = 0;
			p.hurt(p.damageSources().magic(), 10.0F);
			h.assertTrue(CanonGiftState.hour(p.getUUID()) == null && EndlessHourManip.bowing(p),
					"a wound of 30% of the caster's health broke the Hour into the Bow");
			h.assertTrue(EndlessHourManip.taken(p) > 1.0D, "the bowed caster takes more damage");
			BloodManipulation flare = ManipulationInit.hematic_flare.get();
			ready(p, flare);
			double held = HemoCapabilityAccess.getBloodVolume(p).getBloodVolume();
			h.assertTrue(!flare.tryPerformAction(p, h.getLevel(), ItemStack.EMPTY, p.blockPosition(), 0)
					&& HemoCapabilityAccess.getBloodVolume(p).getBloodVolume() == held, "the bowed caster cannot cast");
			h.succeed();
		} finally {
			CanonGiftState.end(p.getUUID());
			p.discard();
		}
	}

	private static ServerPlayer player(GameTestHelper h) {
		ServerPlayer p = TemperFixtures.carry(DuctilisGameTests.player(h));
		p.moveTo(h.absoluteVec(new Vec3(2.5D, 2, 2.5D)), 0.0F, 0.0F);
		p.setYHeadRot(0.0F);
		clearSpawnInvulnerability(p);
		HemoCapabilityAccess.getInitiatoryDegree(p).setDegreeNumber(8);
		var blood = HemoCapabilityAccess.getBloodVolume(p);
		blood.setActive(true);
		blood.setMaxBloodVolume(10_000.0D);
		blood.setBloodVolume(5_000.0D);
		return p;
	}

	static void ready(ServerPlayer p, BloodManipulation m) {
		var known = HemoCapabilityAccess.getKnownManipulations(p);
		known.getKnownManips().put(m, new ManipLevel(0, 0));
		known.setSelectedManip(m);
		known.setEquippedManipNames(List.of(m.getName()));
		HemoCapabilityAccess.getBloodTemperament(p).setTemperAlignment(m.getTend(), (float) m.getAlignLevel());
	}

	private static Zombie zombie(GameTestHelper h, Vec3 at) {
		Zombie zombie = EntityType.ZOMBIE.create(h.getLevel());
		zombie.setNoAi(true);
		zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
		zombie.setHealth(100);
		zombie.setPos(at);
		h.getLevel().addFreshEntity(zombie);
		return zombie;
	}

	/** A fresh player ignores all damage for its first 60 ticks, and these players are never ticked. */
	private static void clearSpawnInvulnerability(ServerPlayer player) {
		try {
			var field = ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");
			field.setAccessible(true);
			field.setInt(player, 0);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
