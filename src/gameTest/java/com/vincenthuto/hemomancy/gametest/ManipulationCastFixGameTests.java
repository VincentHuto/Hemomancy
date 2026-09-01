package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.summon.EntityWretchedWill;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestationManager;
import com.vincenthuto.hemomancy.common.manipulation.animus.SummonAvatarManip;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationCastFixGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private ManipulationCastFixGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineWardGrantsARealContinuousWardPulse(GameTestHelper helper) {
		ServerPlayer player = player(helper, "sanguine-ward-test");
		try {
			helper.assertTrue(ManipulationInit.sanguine_ward.get().getType() == EnumManipulationType.CONTINUOUS,
					"Sanguine Ward is not classified as a held ward");
			ManipulationInit.sanguine_ward.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
					player.blockPosition());
			helper.assertTrue(!player.hasEffect(MobEffects.DAMAGE_RESISTANCE),
					"Sanguine Ward still grants generic Resistance");
			LivingIncomingDamageEvent damage = new LivingIncomingDamageEvent(player,
					new DamageContainer(helper.getLevel().damageSources().generic(), 10.0F));
			ManipulationReactiveEvents.onIncomingDamage(damage);
			helper.assertTrue(Math.abs(damage.getAmount() - 4.0F) < 0.001F,
					"Initial Sanguine Ward pulse did not absorb its six-point pool: " + damage.getAmount());
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void venousTravelUsesTheSharedCastEconomy(GameTestHelper helper) {
		ServerPlayer player = player(helper, "venous-travel-test");
		try {
			BloodManipulation.clearSessionState();
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(2_000.0D);
			var known = HemoCapabilityAccess.requireKnownManipulations(player);
			ManipLevel level = new ManipLevel(0, 0);
			known.getKnownManips().put(ManipulationInit.venous_travel.get(), level);

			boolean cast = ManipulationInit.venous_travel.get().tryPerformAction(player, helper.getLevel(),
					ItemStack.EMPTY, player.blockPosition(), 0.0F);

			helper.assertTrue(cast, "Venous Travel did not pass the shared cast economy");
			helper.assertTrue(Math.abs(blood.getBloodVolume() - 1_000.0D) < 0.001D,
					"Venous Travel did not spend its registered 1000 blood");
			helper.assertTrue(level.getXp() == 1.0D, "Venous Travel did not gain manipulation mastery");
			helper.assertTrue(ManipulationInit.venous_travel.get().getRemainingCooldownTicks(player) == 20L,
					"Venous Travel did not start its registered cooldown");
			helper.succeed();
		} finally {
			BloodManipulation.clearSessionState();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void registryTypesMatchUnifiedInputSemantics(GameTestHelper helper) {
		var manipulations = ManipulationInit.MANIPS.getEntries().stream().map(holder -> holder.get()).toList();
		helper.assertTrue(manipulations.size() == 111, "Expected 111 registered manipulations, got " + manipulations.size());
		helper.assertTrue(manipulations.stream().filter(m -> m.getType() == EnumManipulationType.QUICK).count() == 74,
				"Quick manipulation count changed");
		helper.assertTrue(names(manipulations, EnumManipulationType.CHARGED).equals(Set.of(
				"blood_needle", "activation_potential", "blood_aneurysm", "ironhearted", "vitric_combustion",
				"deadly_gaze", "crimson_coronation", "synaptic_storm", "white_verdict", "rimebound_sentence",
				"funeral_bell", "eclipse_well", "blood_needle_fan", "blood_needle_lance", "hematic_mortar")),
				"Charged manipulation set is incorrect");
		helper.assertTrue(names(manipulations, EnumManipulationType.CONTINUOUS).equals(Set.of(
				"sanguine_ward", "sanguine_mending", "vascular_dowsing", "living_circuit",
				"furnace_veins", "absolute_stillness", "iron_choir", "carrion_communion",
				"penumbral_drift", "lignum_mortis", "canopy_mortis", "worked_lignum")),
				"Continuous manipulation set is incorrect");
		helper.assertTrue(names(manipulations, EnumManipulationType.PASSIVE).equals(Set.of(
				"blackhearted", "sovereign_instinct", "vigil_of_glass", "phoenix_debt", "hematic_ballast",
				"summon_avatar", "summon_avatar_arms", "summon_avatar_armor", "summon_avatar_legs",
				"summon_avatar_complete")),
				"Passive manipulation set is incorrect");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void familyFormsUnlockFromSharedMasteryWithoutReplacingTheBaseline(GameTestHelper helper) {
		var baseline = ManipulationInit.blood_shot.get();
		var mastery = new ManipLevel(0, 9);
		var known = new java.util.LinkedHashMap<BloodManipulation, ManipLevel>();
		known.put(baseline, mastery);

		ManipulationFamilyRegistry.unlockEligibleForms(known);
		helper.assertTrue(known.size() == 1, "A Blood Shot form unlocked before mastery level one");
		mastery.setXp(10);
		mastery.tryLevelUp();
		ManipulationFamilyRegistry.unlockEligibleForms(known);
		helper.assertTrue(known.keySet().stream().anyMatch(manip -> "guided_blood_shot".equals(manip.getName())),
				"Guided Blood Shot did not unlock at mastery level one");
		helper.assertTrue(known.keySet().stream().noneMatch(manip -> "hematic_mortar".equals(manip.getName())
				|| "sanguine_halo".equals(manip.getName())),
				"A higher Blood Shot form unlocked too early");

		mastery.setXp(185);
		mastery.tryLevelUp();
		ManipulationFamilyRegistry.unlockEligibleForms(known);
		helper.assertTrue(known.size() == 4, "Blood Shot family did not retain all four selectable forms");
		helper.assertTrue(known.values().stream().allMatch(level -> level == mastery),
				"Blood Shot family forms do not share one mastery state");
		helper.assertTrue(known.containsKey(baseline), "Blood Shot baseline was replaced by its stronger forms");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void retirementAndAvatarContractsMatchRosterDecision(GameTestHelper helper) {
		helper.assertTrue(ManipulationRetirementRules.isRetiredManipulation("ferric_resonance"),
				"Ferric Resonance remains active");
		helper.assertTrue(ManipulationRetirementRules.isRetiredManipulation("glacial_bastion"),
				"Glacial Bastion remains active");
		helper.assertTrue(ManipulationRetirementRules.isRetiredManipulation("blood_eclipse_mantle"),
				"Blood Eclipse Mantle remains active");
		for (String id : Set.of("crimson_sight", "glacial_circulation", "ferric_transmutation",
				"vigil_of_glass", "hematic_ballast", "summon_thrall")) {
			helper.assertTrue(ManipulationRetirementRules.isRetiredManipulation(id), id + " remains active");
		}
		long active = ManipulationInit.MANIPS.getEntries().stream().map(holder -> holder.get())
				.filter(manipulation -> !ManipulationRetirementRules.isRetiredManipulation(manipulation)).count();
		helper.assertTrue(active == 97, "Expected 97 active manipulations after pruning, got " + active);
		helper.assertTrue(!ManipulationRetirementRules.isRetiredManipulation("summon_avatar"),
				"Summon Avatar was retired");
		helper.assertTrue(ManipulationInit.deadly_gaze.get().getType() == EnumManipulationType.CHARGED
				&& ManipulationInit.deadly_gaze.get().getRequiredChargeTicks() == 40,
				"Deadly Gaze is not a 40-tick charged manipulation");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodBindingRootsAnAimedTargetThroughTheSharedEconomy(GameTestHelper helper) {
		ServerPlayer player = player(helper, "blood-binding-cast-test");
		player.setPos(helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
		player.setYRot(0.0F);
		player.setXRot(0.0F);
		Zombie target = zombie(helper, player.position().add(0.0D, 0.0D, 4.0D));
		try {
			BloodManipulation.clearSessionState();
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(500.0D);
			select(player, ManipulationInit.blood_binding.get());

			boolean cast = ManipulationInit.blood_binding.get().tryPerformAction(player, helper.getLevel(),
					ItemStack.EMPTY, player.blockPosition(), 0.0F);

			helper.assertTrue(cast, "Blood Binding rejected a valid aimed target");
			helper.assertTrue(target.hasEffect(EffectInit.blood_binding),
					"Blood Binding did not apply its shared immobilizing effect");
			helper.assertTrue(target.getEffect(EffectInit.blood_binding).getDuration() == 120,
					"Blood Binding did not use its 120-tick normal duration");
			helper.assertTrue(Math.abs(blood.getBloodVolume() - 375.0D) < 0.001D,
					"Blood Binding did not spend exactly 125 blood");
			helper.assertTrue(ManipulationInit.blood_binding.get().getRemainingCooldownTicks(player) == 60L,
					"Blood Binding did not start its 60-tick cooldown");
			target.setDeltaMovement(0.8D, 0.2D, -0.4D);
			EffectInit.blood_binding.get().applyEffectTick(target, 0);
			helper.assertTrue(target.getDeltaMovement().lengthSqr() == 0.0D,
					"The shared Blood Binding effect did not immobilize the target");
			helper.succeed();
		} finally {
			BloodManipulation.clearSessionState();
			target.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodBindingMissDoesNotSpendOrStartCooldown(GameTestHelper helper) {
		ServerPlayer player = player(helper, "blood-binding-miss-test");
		player.setPos(helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
		try {
			BloodManipulation.clearSessionState();
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(500.0D);
			select(player, ManipulationInit.blood_binding.get());

			boolean cast = ManipulationInit.blood_binding.get().tryPerformAction(player, helper.getLevel(),
					ItemStack.EMPTY, player.blockPosition(), 0.0F);

			helper.assertTrue(!cast, "Blood Binding accepted a cast without an aimed target");
			helper.assertTrue(Math.abs(blood.getBloodVolume() - 500.0D) < 0.001D,
					"A missed Blood Binding cast still spent blood");
			helper.assertTrue(ManipulationInit.blood_binding.get().getRemainingCooldownTicks(player) == 0L,
					"A missed Blood Binding cast still started cooldown");
			helper.succeed();
		} finally {
			BloodManipulation.clearSessionState();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodBindingDoesNotReplaceAnEqualBinding(GameTestHelper helper) {
		ServerPlayer player = player(helper, "blood-binding-existing-test");
		player.setPos(helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
		player.setYRot(0.0F);
		player.setXRot(0.0F);
		Zombie target = zombie(helper, player.position().add(0.0D, 0.0D, 4.0D));
		try {
			BloodManipulation.clearSessionState();
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(500.0D);
			select(player, ManipulationInit.blood_binding.get());
			target.addEffect(new MobEffectInstance(EffectInit.blood_binding, 120, 0, false, false, true));

			boolean cast = ManipulationInit.blood_binding.get().tryPerformAction(player, helper.getLevel(),
					ItemStack.EMPTY, player.blockPosition(), 0.0F);

			helper.assertTrue(!cast, "Blood Binding replaced an equal existing bind");
			helper.assertTrue(Math.abs(blood.getBloodVolume() - 500.0D) < 0.001D,
					"Rejected Blood Binding still spent blood");
			helper.assertTrue(ManipulationInit.blood_binding.get().getRemainingCooldownTicks(player) == 0L,
					"Rejected Blood Binding still started cooldown");
			helper.succeed();
		} finally {
			BloodManipulation.clearSessionState();
			target.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodBindingHalvesRecognizedBossDuration(GameTestHelper helper) {
		ServerPlayer player = player(helper, "blood-binding-boss-test");
		player.setPos(helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
		player.setYRot(0.0F);
		player.setXRot(0.0F);
		WitherBoss target = EntityType.WITHER.create(helper.getLevel());
		if (target == null) throw new IllegalStateException("Could not create test wither");
		target.setNoAi(true);
		target.setPos(player.getX(), player.getEyeY() - target.getEyeHeight(), player.getZ() + 4.0D);
		helper.getLevel().addFreshEntity(target);
		try {
			ManipulationInit.blood_binding.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
					player.blockPosition());
			helper.assertTrue(target.hasEffect(EffectInit.blood_binding),
					"Blood Binding did not affect the recognized boss");
			helper.assertTrue(target.getEffect(EffectInit.blood_binding).getDuration() == 60,
					"Blood Binding did not halve its boss duration");
			helper.succeed();
		} finally {
			target.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void hemorrhagePrimesMortemStatusConsumers(GameTestHelper helper) {
		ServerPlayer player = player(helper, "hemorrhage-primer-test");
		Zombie target = zombie(helper, player.position().add(2, 0, 0));
		try {
			ManipulationInit.hemorrhage.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
					player.blockPosition());
			helper.assertTrue(target.hasEffect(EffectInit.blood_loss),
					"Hemorrhage did not apply the shared Blood Loss primer");
			helper.assertTrue(!target.hasEffect(MobEffects.WITHER),
					"Hemorrhage still duplicates a generic Wither applicator");
			helper.succeed();
		} finally {
			target.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 60)
	public static void hematicBeaconRefreshesItsRallyZone(GameTestHelper helper) {
		ServerPlayer player = player(helper, "hematic-beacon-zone-test");
		Vec3 center = player.getEyePosition().add(player.getLookAngle().scale(20.0D));
		Zombie target = zombie(helper, center);
		ManipulationReactiveEvents.clearSessionState();
		ManipulationInit.hematic_beacon.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
				player.blockPosition());
		helper.assertTrue(target.hasEffect(MobEffects.GLOWING), "Hematic Beacon did not pulse immediately");
		target.removeEffect(MobEffects.GLOWING);
		helper.runAtTickTime(21, () -> {
			try {
				ManipulationReactiveEvents.onLevelTick(new LevelTickEvent.Post(() -> true, helper.getLevel()));
				helper.assertTrue(target.hasEffect(MobEffects.GLOWING),
						"Hematic Beacon did not refresh its persistent rally zone");
				helper.succeed();
			} finally {
				ManipulationReactiveEvents.clearSessionState();
				target.discard();
				player.discard();
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void glacialGraspFreezesWetTargetsAlongItsAimedPath(GameTestHelper helper) {
		ServerPlayer player = player(helper, "glacial-grasp-control-test");
		Vec3 targetPosition = player.position().add(player.getLookAngle().multiply(5.0D, 0.0D, 5.0D));
		Zombie target = zombie(helper, targetPosition);
		helper.getLevel().setBlockAndUpdate(target.blockPosition(),
				net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
		try {
			ManipulationInit.glacial_grasp.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
					player.blockPosition());
			helper.assertTrue(target.getTicksFrozen() > 0 && target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
					"Glacial Grasp did not freeze and hinder a wet target in its aimed path");
			helper.succeed();
		} finally {
			target.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void crimsonTitheUsesTheBorrowedReserveAndCollectsAutomatically(GameTestHelper helper) {
		ServerPlayer player = player(helper, "crimson-tithe-debt-test");
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(2_000.0D);
		BorrowedBloodReserve.drainToCover(player, Double.MAX_VALUE);
		ManipulationInit.crimson_tithe.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
				player.blockPosition());
		helper.assertTrue(Math.abs(BorrowedBloodReserve.get(player) - 500.0D) < 0.001D,
				"Crimson Tithe did not issue its blood through the borrowed reserve");
		helper.assertTrue(Math.abs(blood.getBloodVolume() - 2_000.0D) < 0.001D,
				"Crimson Tithe still filled ordinary blood volume directly");
		BorrowedBloodReserve.drainToCover(player, 500.0D);
		player.getPersistentData().putLong("hemomancy:crimson_tithe_expiry",
				helper.getLevel().getGameTime() - 1L);
		ManipulationReactiveEvents.onPlayerTick(new PlayerTickEvent.Post(player));
		helper.assertTrue(!player.getPersistentData().contains("hemomancy:crimson_tithe_expiry"),
				"Crimson Tithe debt was not collected automatically at its deadline");
		helper.assertTrue(Math.abs(blood.getBloodVolume() - 1_000.0D) < 0.001D,
				"Crimson Tithe did not collect double the spent loan from owned blood");
		player.discard();
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodRushNoLongerSummonsAWretchedWill(GameTestHelper helper) {
		ServerPlayer player = player(helper, "blood-rush-rework-test");
		try {
			ManipulationInit.blood_rush.get().getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition());
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityWretchedWill.class,
					player.getBoundingBox().inflate(8)).isEmpty(), "Blood Rush still summoned a Wretched Will");
			helper.assertTrue(player.getDeltaMovement().horizontalDistanceSqr() > 0.25D,
					"Blood Rush did not surge the caster forward");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void hemolymphalPulseMarksWoundedBloodButIgnoresBloodless(GameTestHelper helper) {
		ServerPlayer player = player(helper, "hemolymphal-rework-test");
		Zombie wounded = zombie(helper, player.position().add(2, 0, 0));
		Skeleton bloodless = EntityType.SKELETON.create(helper.getLevel());
		if (bloodless == null) throw new IllegalStateException("Could not create skeleton");
		bloodless.setNoAi(true);
		bloodless.setPos(player.position().add(3, 0, 0));
		helper.getLevel().addFreshEntity(bloodless);
		try {
			wounded.setHealth(wounded.getMaxHealth() * .4F);
			ManipulationInit.hemolymphal_pulse.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
					player.blockPosition());
			helper.assertTrue(wounded.hasEffect(MobEffects.GLOWING)
					&& wounded.hasEffect(EffectInit.conductive_mark),
					"Wounded blood-bearing target was not physiologically marked");
			helper.assertTrue(!bloodless.hasEffect(MobEffects.GLOWING), "Bloodless target was still revealed");
			helper.succeed();
		} finally {
			wounded.discard();
			bloodless.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void unclosingEyeContinuouslySuppressesConcealment(GameTestHelper helper) {
		ServerPlayer player = player(helper, "unclosing-eye-rework-test");
		Zombie target = zombie(helper, player.position().add(2, 0, 0));
		try {
			ManipulationInit.unclosing_eye.get().getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition());
			target.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.INVISIBILITY, 200));
			player.tickCount = 10;
			ManipulationReactiveEvents.onPlayerTick(new PlayerTickEvent.Post(player));
			helper.assertTrue(!target.hasEffect(MobEffects.INVISIBILITY) && target.hasEffect(MobEffects.GLOWING),
					"Unclosing Eye did not suppress concealment after the initial cast");
			helper.succeed();
		} finally {
			target.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void cauterizingRebukePurgesWithoutRepeatingIgnition(GameTestHelper helper) {
		ServerPlayer player = player(helper, "cauterizing-rework-test");
		Zombie nearby = zombie(helper, player.position().add(2, 0, 0));
		try {
			player.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 200));
			player.addEffect(new net.minecraft.world.effect.MobEffectInstance(EffectInit.blood_loss, 200));
			float before = player.getHealth();
			ManipulationInit.cauterizing_rebuke.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
					player.blockPosition());
			helper.assertTrue(!player.hasEffect(MobEffects.POISON) && !player.hasEffect(EffectInit.blood_loss),
					"Cauterizing Rebuke did not purge blood ailments");
			helper.assertTrue(player.getHealth() < before, "Cauterizing Rebuke did not impose its self-damage tradeoff");
			helper.assertTrue(!nearby.isOnFire() && nearby.getHealth() == nearby.getMaxHealth(),
					"Cauterizing Rebuke still duplicated Sanguine Ignition");
			helper.succeed();
		} finally {
			nearby.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void funeralBellDetonatesMortemStatusesInsteadOfExecuting(GameTestHelper helper) {
		ServerPlayer player = player(helper, "funeral-bell-rework-test");
		Zombie marked = zombie(helper, player.position().add(2, 0, 0));
		Zombie unmarked = zombie(helper, player.position().add(3, 0, 0));
		try {
			marked.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WITHER, 200));
			marked.addEffect(new net.minecraft.world.effect.MobEffectInstance(EffectInit.grave_debt, 200));
			ManipulationInit.funeral_bell.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
					player.blockPosition(), 80.0F);
			float markedDamage = marked.getMaxHealth() - marked.getHealth();
			float unmarkedDamage = unmarked.getMaxHealth() - unmarked.getHealth();
			helper.assertTrue(markedDamage > unmarkedDamage && unmarkedDamage > 0,
					"Funeral Bell did not reward existing Mortem statuses");
			helper.assertTrue(marked.isAlive(), "Funeral Bell retained its direct execution behavior");
			helper.succeed();
		} finally {
			marked.discard();
			unmarked.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void crouchingGlacialRampartRaisesTheRetiredBastionShape(GameTestHelper helper) {
		ServerPlayer player = player(helper, "rampart-bastion-replacement-test");
		try {
			player.setShiftKeyDown(true);
			ManipulationInit.glacial_rampart.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
					player.blockPosition());
			long ice = BlockPos.betweenClosedStream(player.blockPosition().offset(-2, 0, -2),
					player.blockPosition().offset(2, 2, 2))
					.filter(pos -> helper.getLevel().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.PACKED_ICE))
					.count();
			helper.assertTrue(ice >= 8, "Crouch-cast Rampart did not raise a surrounding bastion");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void blackVeilCarriesTheRetiredMantleTradeoff(GameTestHelper helper) {
		ServerPlayer player = player(helper, "black-veil-mantle-replacement-test");
		try {
			ManipulationInit.black_veil_covenant.get().getAction(player, helper.getLevel(), ItemStack.EMPTY,
					player.blockPosition());
			NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
			helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_RESISTANCE)
					&& player.hasEffect(MobEffects.FIRE_RESISTANCE) && player.hasEffect(MobEffects.WEAKNESS),
					"Black Veil did not carry Blood Eclipse Mantle's defensive tradeoff");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void phoenixDebtUsesSharedLastRiteArming(GameTestHelper helper) {
		ServerPlayer player = player(helper, "phoenix-last-rite-test");
		try {
			var known = HemoCapabilityAccess.requireKnownManipulations(player);
			known.setEquippedManipNames(java.util.List.of("phoenix_debt"));
			known.togglePassive("phoenix_debt");
			ManipulationReactiveEvents.onPlayerTick(new PlayerTickEvent.Post(player));
			helper.assertTrue("hemomancy:phoenix_debt".equals(LastRiteHelper.getArmedSource(player)),
					"Active Phoenix Debt did not arm the shared Last Rite source");
			known.togglePassive("phoenix_debt");
			ManipulationReactiveEvents.onPlayerTick(new PlayerTickEvent.Post(player));
			helper.assertTrue(!"hemomancy:phoenix_debt".equals(LastRiteHelper.getArmedSource(player)),
					"Disabling Phoenix Debt left its shared Last Rite source armed");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void manipulationMetadataAndImplementationsMatchDesign(GameTestHelper helper) {
		assertImplementation(helper, "crimson_coronation", "CrimsonCoronationManip");
		assertImplementation(helper, "sovereign_instinct", "SovereignInstinctManip");
		assertImplementation(helper, "synaptic_storm", "SynapticStormManip");
		assertImplementation(helper, "living_circuit", "LivingCircuitManip");
		assertImplementation(helper, "white_verdict", "WhiteVerdictManip");
		assertImplementation(helper, "vigil_of_glass", "VigilOfGlassManip");
		assertImplementation(helper, "furnace_veins", "FurnaceVeinsManip");
		assertImplementation(helper, "phoenix_debt", "PhoenixDebtManip");
		assertImplementation(helper, "absolute_stillness", "AbsoluteStillnessManip");
		assertImplementation(helper, "rimebound_sentence", "RimeboundSentenceManip");
		assertImplementation(helper, "hematic_ballast", "HematicBallastManip");
		assertImplementation(helper, "iron_choir", "IronChoirManip");
		assertImplementation(helper, "funeral_bell", "FuneralBellManip");
		assertImplementation(helper, "carrion_communion", "CarrionCommunionManip");
		assertImplementation(helper, "penumbral_drift", "PenumbralDriftManip");
		assertImplementation(helper, "eclipse_well", "EclipseWellManip");
		assertMeta(helper, "crimson_coronation", 1000, 70, EnumManipulationType.CHARGED,
				EnumManipulationRank.PERFECTUS, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD, 80, 120);
		assertMeta(helper, "sovereign_instinct", 450, 60, EnumManipulationType.PASSIVE,
				EnumManipulationRank.MAGISTER, EnumBloodTendency.ANIMUS, EnumVeinSections.HEAD, 0, 0);
		assertMeta(helper, "synaptic_storm", 900, 65, EnumManipulationType.CHARGED,
				EnumManipulationRank.PERFECTUS, EnumBloodTendency.DUCTILIS, EnumVeinSections.HEAD, 60, 100);
		assertMeta(helper, "living_circuit", 180, 55, EnumManipulationType.CONTINUOUS,
				EnumManipulationRank.MAGISTER, EnumBloodTendency.DUCTILIS, EnumVeinSections.BODY, 0, 80);
		assertMeta(helper, "white_verdict", 800, 65, EnumManipulationType.CHARGED,
				EnumManipulationRank.PERFECTUS, EnumBloodTendency.LUX, EnumVeinSections.HEAD, 60, 100);
		assertMeta(helper, "vigil_of_glass", 300, 55, EnumManipulationType.PASSIVE,
				EnumManipulationRank.MAGISTER, EnumBloodTendency.LUX, EnumVeinSections.HEAD, 0, 0);
		assertMeta(helper, "furnace_veins", 250, 55, EnumManipulationType.CONTINUOUS,
				EnumManipulationRank.MAGISTER, EnumBloodTendency.FLAMMEUS, EnumVeinSections.BODY, 0, 100);
		assertMeta(helper, "phoenix_debt", 2000, 75, EnumManipulationType.PASSIVE,
				EnumManipulationRank.PERFECTUS, EnumBloodTendency.FLAMMEUS, EnumVeinSections.HEART, 0, 0);
		assertMeta(helper, "absolute_stillness", 300, 65, EnumManipulationType.CONTINUOUS,
				EnumManipulationRank.PERFECTUS, EnumBloodTendency.CONGEATIO, EnumVeinSections.BODY, 0, 160);
		assertMeta(helper, "rimebound_sentence", 750, 55, EnumManipulationType.CHARGED,
				EnumManipulationRank.MAGISTER, EnumBloodTendency.CONGEATIO, EnumVeinSections.ARMS, 70, 120);
		assertMeta(helper, "hematic_ballast", 200, 55, EnumManipulationType.PASSIVE,
				EnumManipulationRank.MAGISTER, EnumBloodTendency.FERRIC, EnumVeinSections.LEGS, 0, 0);
		assertMeta(helper, "iron_choir", 250, 60, EnumManipulationType.CONTINUOUS,
				EnumManipulationRank.MAGISTER, EnumBloodTendency.FERRIC, EnumVeinSections.BODY, 0, 100);
		assertMeta(helper, "funeral_bell", 1000, 75, EnumManipulationType.CHARGED,
				EnumManipulationRank.PERFECTUS, EnumBloodTendency.MORTEM, EnumVeinSections.HEART, 80, 160);
		assertMeta(helper, "carrion_communion", 220, 60, EnumManipulationType.CONTINUOUS,
				EnumManipulationRank.MAGISTER, EnumBloodTendency.MORTEM, EnumVeinSections.BODY, 0, 120);
		assertMeta(helper, "penumbral_drift", 175, 55, EnumManipulationType.CONTINUOUS,
				EnumManipulationRank.MAGISTER, EnumBloodTendency.TENEBRIS, EnumVeinSections.LEGS, 0, 60);
		assertMeta(helper, "eclipse_well", 900, 65, EnumManipulationType.CHARGED,
				EnumManipulationRank.PERFECTUS, EnumBloodTendency.TENEBRIS, EnumVeinSections.HEAD, 80, 140);
		helper.succeed();
	}

	private static void assertImplementation(GameTestHelper helper, String id, String expectedClass) {
		BloodManipulation manipulation = ManipulationInit.MANIPS.getEntries().stream()
				.map(holder -> holder.get()).filter(candidate -> candidate.getName().equals(id)).findFirst()
				.orElseThrow();
		helper.assertTrue(manipulation.getClass().getSimpleName().equals(expectedClass),
				id + " should use ordinary " + expectedClass + " implementation, got "
						+ manipulation.getClass().getSimpleName());
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void ironheartedRejectsPartialCharge(GameTestHelper helper) {
		ServerPlayer player = player(helper, "partial-ironheart-test");
		try {
			BloodManipulation.clearSessionState();
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(1_000.0D);
			var tendency = HemoCapabilityAccess.requireBloodTendency(player);
			tendency.setTendencyAlignment(ManipulationInit.ironhearted.get().getTend(), 45.0F);
			var known = HemoCapabilityAccess.requireKnownManipulations(player);
			known.getKnownManips().put(ManipulationInit.ironhearted.get(), new ManipLevel(0, 0));

			double bloodBefore = blood.getBloodVolume();
			boolean cast = ManipulationInit.ironhearted.get().tryPerformAction(player, helper.getLevel(),
					ItemStack.EMPTY, player.blockPosition(), 20.0F);

			helper.assertTrue(!cast, "Ironhearted accepted a partial charge");
			helper.assertTrue(HemoCapabilityAccess.getPowerGuardrails(player).getIronHeartHealth() == 0.0F,
					"Partial charge left incomplete Ironheart health behind");
			helper.assertTrue(blood.getBloodVolume() == bloodBefore,
					"Rejected partial charge consumed blood");
			helper.assertTrue(ManipulationInit.ironhearted.get().getRemainingCooldownTicks(player) == 0L,
					"Rejected partial charge started a cooldown");
			helper.succeed();
		} finally {
			BloodManipulation.clearSessionState();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodNeedleScalesFromPartialToFullVolley(GameTestHelper helper) {
		ServerPlayer player = player(helper, "blood-needle-charge-test");
		try {
			var manipulation = ManipulationInit.blood_needle.get();
			AABB area = new AABB(player.blockPosition()).inflate(64.0D);
			manipulation.getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition(), 10.0F);
			var partial = helper.getLevel().getEntitiesOfClass(BloodNeedleEntity.class, area,
					needle -> needle.getOwner() == player);
			helper.assertTrue(partial.size() >= 5 && partial.size() <= 10,
					"Half-charged Blood Needle volley was outside 5-10: " + partial.size());
			partial.forEach(BloodNeedleEntity::discard);

			manipulation.getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition(), 20.0F);
			int full = helper.getLevel().getEntitiesOfClass(BloodNeedleEntity.class, area,
					needle -> needle.getOwner() == player).size();
			helper.assertTrue(full >= 10 && full <= 20,
					"Full Blood Needle volley was outside 10-20: " + full);
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void activationPotentialScalesDamageFromPartialToFull(GameTestHelper helper) {
		ServerPlayer player = player(helper, "activation-potential-charge-test");
		Zombie target = zombie(helper, player.position().add(2.0D, 0.0D, 0.0D));
		try {
			var manipulation = ManipulationInit.activation_potential.get();
			manipulation.getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition(), 15.0F);
			float partial = target.getMaxHealth() - target.getHealth();
			target.setHealth(target.getMaxHealth());
			target.invulnerableTime = 0;
			manipulation.getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition(), 30.0F);
			float full = target.getMaxHealth() - target.getHealth();
			helper.assertTrue(partial > 0.0F && full > partial,
					"Activation Potential damage did not increase: partial=" + partial + ", full=" + full);
			helper.succeed();
		} finally {
			target.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodAneurysmScalesDamageAndLaunchFromPartialToFull(GameTestHelper helper) {
		ServerPlayer player = player(helper, "blood-aneurysm-charge-test");
		Zombie target = zombie(helper, player.position().add(2.0D, 0.0D, 0.0D));
		try {
			var manipulation = ManipulationInit.blood_aneurysm.get();
			manipulation.getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition(), 20.0F);
			float partialDamage = target.getMaxHealth() - target.getHealth();
			double partialLaunch = target.getDeltaMovement().y;
			target.setHealth(target.getMaxHealth());
			target.invulnerableTime = 0;
			target.setDeltaMovement(Vec3.ZERO);
			manipulation.getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition(), 40.0F);
			float fullDamage = target.getMaxHealth() - target.getHealth();
			double fullLaunch = target.getDeltaMovement().y;
			helper.assertTrue(partialDamage > 0.0F && fullDamage > partialDamage,
					"Blood Aneurysm damage did not increase with charge");
			helper.assertTrue(fullLaunch > partialLaunch,
					"Blood Aneurysm launch did not increase with charge");
			helper.succeed();
		} finally {
			target.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void vitricCombustionScalesDamageFromPartialToFull(GameTestHelper helper) {
		ServerPlayer player = player(helper, "vitric-combustion-charge-test");
		Vec3 targetPosition = player.getEyePosition(1.0F).add(player.getViewVector(1.0F).scale(22.0D));
		Zombie target = zombie(helper, targetPosition);
		try {
			var manipulation = ManipulationInit.vitric_combustion.get();
			manipulation.getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition(), 30.0F);
			float partial = target.getMaxHealth() - target.getHealth();
			target.setHealth(target.getMaxHealth());
			target.invulnerableTime = 0;
			target.clearFire();
			target.setDeltaMovement(Vec3.ZERO);
			manipulation.getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition(), 60.0F);
			float full = target.getMaxHealth() - target.getHealth();
			helper.assertTrue(partial > 0.0F && full > partial,
					"Vitric Combustion damage did not increase: partial=" + partial + ", full=" + full);
			helper.succeed();
		} finally {
			target.discard();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 130,
			batch = "manipulationChannelWard")
	public static void continuousWardPaysUpkeepAndCoolsDownOnStop(GameTestHelper helper) {
		ServerPlayer player = player(helper, "continuous-ward-test");
		BloodManipulation.clearSessionState();
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(100.0D);
		HemoCapabilityAccess.requireBloodTendency(player)
				.setTendencyAlignment(ManipulationInit.sanguine_ward.get().getTend(), 10.0F);
		var known = HemoCapabilityAccess.requireKnownManipulations(player);
		ManipLevel mastery = new ManipLevel(0, 0);
		known.getKnownManips().put(ManipulationInit.sanguine_ward.get(), mastery);
		known.setEquippedManipNames(java.util.List.of("sanguine_ward"));
		known.setSelectedManip(ManipulationInit.sanguine_ward.get());

		ManipulationChannelManager.start(player);
		helper.assertTrue(ManipulationChannelManager.isChanneling(player.getUUID()),
				"Continuous ward did not start");
		helper.assertTrue(Math.abs(blood.getBloodVolume() - 90.0D) < 0.001D,
				"Continuous ward did not pay its immediate pulse");
		helper.assertTrue(mastery.getXp() == 0.0D,
				"Continuous ward awarded mastery when the channel began");

		for (int tick : new int[] { 20, 40, 60, 80 }) {
			helper.runAtTickTime(tick, () -> {
				ManipulationChannelManager.onPlayerTick(new PlayerTickEvent.Post(player));
				helper.assertTrue(mastery.getXp() == 0.0D,
						"Continuous ward awarded mastery before five uninterrupted seconds");
			});
		}
		helper.runAtTickTime(99, () -> helper.assertTrue(mastery.getXp() == 0.0D,
				"Continuous ward awarded mastery before tick 100"));
		helper.runAtTickTime(100, () -> {
			try {
				ManipulationChannelManager.onPlayerTick(new PlayerTickEvent.Post(player));
				helper.assertTrue(mastery.getXp() == 1.0D,
						"Continuous ward did not award mastery after five uninterrupted seconds");
				ManipulationChannelManager.stop(player);
				helper.assertTrue(!ManipulationChannelManager.isChanneling(player.getUUID()),
						"Continuous ward remained active after stop");
				helper.assertTrue(ManipulationInit.sanguine_ward.get().getRemainingCooldownTicks(player) == 20L,
						"Continuous ward cooldown did not begin on stop");
				helper.succeed();
			} finally {
				BloodManipulation.clearSessionState();
				player.discard();
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 130,
			batch = "avatarManifestationMastery")
	public static void avatarPaysUpkeepAndAwardsMasteryEveryFiveSeconds(GameTestHelper helper) {
		ServerPlayer player = player(helper, "avatar-mastery-test");
		var avatar = (SummonAvatarManip) ManipulationInit.summon_avatar.get();
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(525.0D);
		HemoCapabilityAccess.requireBloodTendency(player)
				.setTendencyAlignment(avatar.getTend(), 50.0F);
		var known = HemoCapabilityAccess.requireKnownManipulations(player);
		ManipLevel mastery = new ManipLevel(0, 0);
		known.getKnownManips().put(avatar, mastery);
		known.setEquippedManipNames(java.util.List.of(avatar.getName()));

		helper.assertTrue(AvatarManifestationManager.toggle(player, avatar),
				"Avatar did not manifest");
		helper.assertTrue(avatar.getName().equals(known.getActiveAvatarForm()),
				"Avatar form was not recorded");
		helper.assertTrue(mastery.getXp() == 0.0D,
				"Avatar awarded mastery when first manifested");

		for (int tick : new int[] { 20, 40, 60, 80 }) {
			helper.runAtTickTime(tick, () -> {
				AvatarManifestationManager.onPlayerTick(new PlayerTickEvent.Post(player));
				helper.assertTrue(mastery.getXp() == 0.0D,
						"Avatar awarded mastery before five uninterrupted seconds");
			});
		}
		helper.runAtTickTime(100, () -> {
			try {
				AvatarManifestationManager.onPlayerTick(new PlayerTickEvent.Post(player));
				helper.assertTrue(mastery.getXp() == 1.0D,
						"Avatar did not award mastery after five uninterrupted seconds");
				helper.assertTrue(Math.abs(blood.getBloodVolume() - 75.0D) < 0.001D,
						"Avatar did not pay its initial cost and five upkeep pulses");
				AvatarManifestationManager.dismiss(player);
				helper.assertTrue(known.getActiveAvatarForm().isBlank(),
						"Avatar remained active after dismissal");
				helper.succeed();
			} finally {
				AvatarManifestationManager.dismiss(player);
				player.discard();
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void ferventHuskHasNoRightClickMasteryEffect(GameTestHelper helper) {
		ServerPlayer player = player(helper, "fervent-husk-mastery-test");
		try {
			var known = HemoCapabilityAccess.requireKnownManipulations(player);
			BloodManipulation manipulation = ManipulationInit.blood_shot.get();
			ManipLevel mastery = new ManipLevel(1, 10);
			known.getKnownManips().put(manipulation, mastery);
			known.setSelectedManip(manipulation);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.fervent_husk.get()));

			ItemInit.fervent_husk.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);

			helper.assertTrue(mastery.getCurrentLevel() == 1,
					"Fervent Husk changed the selected manipulation's mastery level");
			helper.assertTrue(mastery.getXp() == 10.0D,
					"Fervent Husk changed the selected manipulation's mastery XP");
			helper.assertTrue(player.getMainHandItem().getCount() == 1,
					"Fervent Husk was consumed by its obsolete right-click behavior");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 60,
			batch = "manipulationChannelMending")
	public static void sanguineMendingRepairsPerPulseAndCoolsDownOnStop(GameTestHelper helper) {
		ServerPlayer player = player(helper, "continuous-mending-test");
		BloodManipulation.clearSessionState();
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(500.0D);
		ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
		pickaxe.setDamageValue(200);
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pickaxe);
		select(player, ManipulationInit.sanguine_mending.get());

		ManipulationChannelManager.start(player);
		helper.assertTrue(pickaxe.getDamageValue() == 150, "Mending did not repair 50 durability immediately");
		helper.assertTrue(Math.abs(blood.getBloodVolume() - 350.0D) < 0.001D,
				"Mending did not pay its immediate upkeep");
		helper.runAtTickTime(21, () -> {
			try {
				ManipulationChannelManager.onPlayerTick(new PlayerTickEvent.Post(player));
				helper.assertTrue(pickaxe.getDamageValue() == 100, "Mending did not repair its second pulse");
				ManipulationChannelManager.stop(player);
				helper.assertTrue(ManipulationInit.sanguine_mending.get().getRemainingCooldownTicks(player) == 30L,
						"Mending cooldown did not begin on stop");
				helper.succeed();
			} finally {
				BloodManipulation.clearSessionState();
				player.discard();
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 60,
			batch = "manipulationChannelDowsing")
	public static void vascularDowsingStopsWhenTheNextUpkeepCannotBePaid(GameTestHelper helper) {
		ServerPlayer player = player(helper, "continuous-dowsing-test");
		BloodManipulation.clearSessionState();
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setActive(true);
		blood.setBloodVolume(750.0D);
		select(player, ManipulationInit.vascular_dowsing.get());

		ManipulationChannelManager.start(player);
		helper.assertTrue(ManipulationChannelManager.isChanneling(player.getUUID()),
				"Vascular Dowsing did not perform its immediate scan");
		helper.assertTrue(Math.abs(blood.getBloodVolume() - 250.0D) < 0.001D,
				"Vascular Dowsing did not pay its immediate scan cost");
		helper.runAtTickTime(21, () -> {
			try {
				ManipulationChannelManager.onPlayerTick(new PlayerTickEvent.Post(player));
				helper.assertTrue(!ManipulationChannelManager.isChanneling(player.getUUID()),
						"Vascular Dowsing continued without enough blood");
				long cooldown = ManipulationInit.vascular_dowsing.get().getRemainingCooldownTicks(player);
				helper.assertTrue(cooldown > 0L && cooldown <= 400L,
						"Forced Dowsing stop did not apply its cooldown");
				helper.succeed();
			} finally {
				BloodManipulation.clearSessionState();
				player.discard();
			}
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 60,
			batch = "manipulationChannelLignumMortisRelease")
	public static void lignumMortisWaitsForReleaseBeforeDismantling(GameTestHelper helper) {
		ServerPlayer player = player(helper, "lignum-mortis-release-test");
		player.setPos(helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
		player.setYRot(0.0F);
		player.setXRot(0.0F);
		BlockPos origin = helper.absolutePos(new BlockPos(2, 3, 5));
		BlockPos branch = origin.above();
		helper.getLevel().setBlockAndUpdate(origin, net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState());
		helper.getLevel().setBlockAndUpdate(branch, net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState());
		try {
			BloodManipulation.clearSessionState();
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(1_000.0D);
			select(player, ManipulationInit.lignum_mortis.get());

			ManipulationChannelManager.start(player);
			helper.assertTrue(ManipulationChannelManager.isChanneling(player.getUUID()),
					"Lignum Mortis did not begin on an aimed log");
			for (int tick = 0; tick < 12; tick++) {
				ManipulationInit.lignum_mortis.get().tickContinuousAction(player, helper.getLevel());
			}
			helper.assertTrue(helper.getLevel().getBlockState(origin).is(net.minecraft.world.level.block.Blocks.OAK_LOG)
					&& helper.getLevel().getBlockState(branch).is(net.minecraft.world.level.block.Blocks.OAK_LOG),
					"Lignum Mortis broke marked logs before release");

			ManipulationChannelManager.stop(player, true);
			helper.assertTrue(helper.getLevel().getBlockState(origin).isAir()
					&& helper.getLevel().getBlockState(branch).isAir(),
					"Lignum Mortis did not dismantle its marked logs on release");
			helper.succeed();
		} finally {
			BloodManipulation.clearSessionState();
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 60,
			batch = "manipulationChannelLignumMortisCancel")
	public static void lignumMortisForcedStopPreservesMushrooms(GameTestHelper helper) {
		ServerPlayer player = player(helper, "lignum-mortis-cancel-test");
		player.setPos(helper.absoluteVec(new Vec3(2.5D, 2.0D, 2.5D)));
		player.setYRot(0.0F);
		player.setXRot(0.0F);
		BlockPos stem = helper.absolutePos(new BlockPos(2, 3, 5));
		BlockPos cap = stem.above();
		helper.getLevel().setBlockAndUpdate(stem,
				net.minecraft.world.level.block.Blocks.MUSHROOM_STEM.defaultBlockState());
		helper.getLevel().setBlockAndUpdate(cap,
				net.minecraft.world.level.block.Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState());
		try {
			BloodManipulation.clearSessionState();
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(1_000.0D);
			select(player, ManipulationInit.lignum_mortis.get());

			ManipulationChannelManager.start(player);
			for (int tick = 0; tick < 12; tick++) {
				ManipulationInit.lignum_mortis.get().tickContinuousAction(player, helper.getLevel());
			}
			ManipulationChannelManager.stop(player, false);
			helper.assertTrue(helper.getLevel().getBlockState(stem)
						.is(net.minecraft.world.level.block.Blocks.MUSHROOM_STEM)
					&& helper.getLevel().getBlockState(cap)
						.is(net.minecraft.world.level.block.Blocks.BROWN_MUSHROOM_BLOCK),
					"A forced Lignum Mortis stop dismantled the mushroom structure");
			helper.succeed();
		} finally {
			BloodManipulation.clearSessionState();
			player.discard();
		}
	}

	private static Set<String> names(java.util.List<? extends BloodManipulation> manipulations,
			EnumManipulationType type) {
		return manipulations.stream().filter(manipulation -> manipulation.getType() == type)
				.map(BloodManipulation::getName).collect(Collectors.toSet());
	}

	private static void assertMeta(GameTestHelper helper, String name, double cost, double alignment,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section, int chargeTicks, int cooldownTicks) {
		BloodManipulation manipulation = ManipulationInit.getByName(name);
		helper.assertTrue(manipulation != null, "Missing manipulation " + name);
		helper.assertTrue(manipulation.getCost() == cost, name + " cost changed");
		helper.assertTrue(manipulation.getAlignLevel() == alignment, name + " alignment changed");
		helper.assertTrue(manipulation.getType() == type, name + " casting type changed");
		helper.assertTrue(manipulation.getRank() == rank, name + " rank changed");
		helper.assertTrue(manipulation.getTend() == tendency, name + " tendency changed");
		helper.assertTrue(manipulation.getSection() == section, name + " vein section changed");
		helper.assertTrue(manipulation.getRequiredChargeTicks() == chargeTicks, name + " charge changed");
		helper.assertTrue(manipulation.getCooldownTicks() == cooldownTicks, name + " cooldown changed");
	}

	private static void select(ServerPlayer player, BloodManipulation manipulation) {
		var known = HemoCapabilityAccess.requireKnownManipulations(player);
		known.getKnownManips().put(manipulation, new ManipLevel(0, 0));
		known.setEquippedManipNames(java.util.List.of(manipulation.getName()));
		known.setSelectedManip(manipulation);
	}

	private static Zombie zombie(GameTestHelper helper, Vec3 position) {
		Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
		if (zombie == null) throw new IllegalStateException("Could not create test zombie");
		zombie.setNoAi(true);
		zombie.setPos(position);
		helper.getLevel().addFreshEntity(zombie);
		return zombie;
	}

	private static ServerPlayer player(GameTestHelper helper, String name) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), name), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		return player;
	}
}
