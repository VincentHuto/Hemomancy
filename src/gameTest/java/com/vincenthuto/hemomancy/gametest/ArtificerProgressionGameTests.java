package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.ArmorSetAbilityRegistry;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.ArmorSetBonusHandler;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ArtificerProgressSnapshot;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerArtificerDialogueTrees;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponGraftRecipeUnlockEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponMemoryUnlocks;
import com.vincenthuto.hemomancy.common.mission.ArtificerProgressionRules.D7Lineage;
import com.vincenthuto.hemomancy.common.mission.ArtificerProgressionRules.ForkFamily;
import com.vincenthuto.hemomancy.common.mission.ArtificerProgressionRules.Step;
import com.vincenthuto.hemomancy.common.mission.HarbingerArtificerAssignmentHelper;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRules.ArmatureTier;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;
import io.netty.channel.embedded.EmbeddedChannel;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ArtificerProgressionGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private ArtificerProgressionGameTests() {}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void wornVowRequiresBriefingAndInspection(GameTestHelper helper) {
		ServerPlayer player = player(helper, "worn-vow");
		HarbingerArtificerAssignmentHelper.onArmaturePlaced(player);
		HarbingerArtificerAssignmentHelper.onArmatureUpgrade(player, new ItemStack(ItemInit.hematic_iron_helm.get()), 2);
		helper.assertTrue(HarbingerArtificerAssignmentHelper.claimWornVowInspection(player).isEmpty(),
				"pre-briefing inspection should fail");
		HarbingerArtificerAssignmentHelper.brief(player, HarbingerArtificerAssignmentHelper.WORN_VOW_BRIEFED);
		ItemStack reward = HarbingerArtificerAssignmentHelper.claimWornVowInspection(player);
		helper.assertTrue(reward.is(ItemInit.hematic_iron_scrap.get()) && reward.getCount() == 4,
				"inspection did not issue four Hematic Iron Scrap");
		helper.assertTrue(HarbingerArtificerAssignmentHelper.claimWornVowInspection(player).isEmpty(),
				"Worn Vow inspection reward repeated");
		equip(player, ItemInit.hematic_iron_helm.get(), ItemInit.hematic_iron_chestplate.get(),
				ItemInit.hematic_iron_leggings.get(), ItemInit.hematic_iron_boots.get());
		helper.assertTrue(HarbingerArtificerAssignmentHelper.tryGrantHematicIronFitting(player)
				.is(ItemInit.worn_vow_fitting.get()), "Worn Vow fitting was not granted");
		player.discard();
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void milestonesDoNotToastBeforeBriefingOrAcceptNonD7Armor(GameTestHelper helper) {
		ServerPlayer player = player(helper, "artificer-gates");
		HarbingerArtificerAssignmentHelper.onArmatureUpgrade(player,
				new ItemStack(ItemInit.hematic_iron_helm.get()), 7);
		helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(player,
				HarbingerAdvancementGranter.ADV_ARTIFICER_WORN_VOW_LESSON_READY),
				"pre-briefing Hematic Iron upgrade emitted a return-ready toast");
		helper.assertTrue(!HarbingerAdvancementGranter.isArtificerFirstD7Upgrade(player),
				"a non-D7 degree-seven recipe satisfied the D7 upgrade milestone");
		player.discard();
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void dialogueOnlyExposesReadyProgressionActions(GameTestHelper helper) {
		ArtificerProgressSnapshot progress = new ArtificerProgressSnapshot(7, true, false, false, true,
				ForkFamily.NONE, D7Lineage.NONE, Step.FULL_SET, Step.RECOVER_BRANCH, Step.LOCKED,
				Step.LOCKED, Step.RECOVER_BRANCH, false, false, false, false, false);
		DialogueNode assignments = HarbingerArtificerDialogueTrees.forState(1, progress).getNode("assignments");
		helper.assertTrue(!hasEvent(assignments, HarbingerArtificerDialogueTrees.EVENT_CLAIM_HEMATIC_IRON_FITTING),
				"Worn Vow exposed its fitting before the full-set objective");
		helper.assertTrue(!hasEvent(assignments, HarbingerArtificerDialogueTrees.EVENT_INSPECT_THREE_ANSWERS),
				"fork recovery exposed an invalid inspection action");
		helper.assertTrue(!hasEvent(assignments, HarbingerArtificerDialogueTrees.EVENT_INSPECT_WEIGHT_OF_FRAME),
				"D7 recovery exposed an invalid inspection action");
		helper.assertTrue(assignments.options().stream().anyMatch(option -> option.eventId() != null
				&& option.eventId().startsWith(HarbingerArtificerDialogueTrees.EVENT_RECOVER_FORK_PREFIX)),
				"fork recovery choices were missing");
		helper.assertTrue(assignments.options().stream().anyMatch(option -> option.eventId() != null
				&& option.eventId().startsWith(HarbingerArtificerDialogueTrees.EVENT_RECOVER_D7_PREFIX)),
				"D7 recovery choices were missing");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 60)
	public static void eachForkUsesRecordedReagentAndRealSetResponse(GameTestHelper helper) {
		testFork(helper, ForkFamily.BARBED, ItemInit.barbed_helm.get(), ItemInit.aculeate_vitriol.get());
		testFork(helper, ForkFamily.CHITINITE, ItemInit.chitinite_helm.get(), ItemInit.sclerotic_oleum.get());
		testFork(helper, ForkFamily.PRISMATIC, ItemInit.prismatic_helm.get(), ItemInit.chromatic_sublimate.get());
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodLustAndLivingArsenalUseSuccessfulGameplayHooks(GameTestHelper helper) {
		ServerPlayer player = player(helper, "vestment");
		HarbingerArtificerAssignmentHelper.brief(player, HarbingerArtificerAssignmentHelper.CRIMSON_VESTMENT_BRIEFED);
		HarbingerArtificerAssignmentHelper.onArmatureTierApplied(player, ArmatureTier.VICAR_CONSECRATED);
		helper.assertTrue(HarbingerArtificerAssignmentHelper.inspectCrimsonVestment(player), "consecrated frame inspection failed");
		helper.assertTrue(HarbingerArtificerAssignmentHelper.counselCrimsonVestment(player).is(ItemInit.crimson_lacquer.get()),
				"Alchemist did not issue Crimson Lacquer");
		HarbingerArtificerAssignmentHelper.onArmatureUpgrade(player, new ItemStack(ItemInit.blood_lust_helm.get()), 5);
		equip(player, ItemInit.blood_lust_helm.get(), ItemInit.blood_lust_chest.get(),
				ItemInit.blood_lust_legs.get(), ItemInit.blood_lust_boots.get());
		Zombie target = EntityType.ZOMBIE.create(helper.getLevel());
		DamageContainer damage = new DamageContainer(target.damageSources().playerAttack(player), 8.0F);
		ArmorSetBonusHandler.onLivingDamage(new LivingDamageEvent.Post(target, damage));
		helper.assertTrue(HarbingerArtificerAssignmentHelper.has(player,
				HarbingerArtificerAssignmentHelper.CRIMSON_VESTMENT_DEMONSTRATED), "Blood Lust hit was not recorded");

		HarbingerArtificerAssignmentHelper.brief(player, HarbingerArtificerAssignmentHelper.ASSUMED_LIMB_BRIEFED);
		HarbingerArtificerAssignmentHelper.onLivingWeaponGraftComplete(player);
		helper.assertTrue(HarbingerArtificerAssignmentHelper.claimAssumedLimbInspection(player).is(ItemInit.hematic_memory.get()),
				"graft inspection did not issue a Blank Hematic Memory");
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_blade.get()));
		LivingWeaponGraftRecipeUnlockEvents.onLivingDeath(new LivingDeathEvent(target,
				target.damageSources().playerAttack(player)));
		helper.assertTrue(HarbingerArtificerAssignmentHelper.has(player,
				HarbingerArtificerAssignmentHelper.ASSUMED_LIMB_DEMONSTRATED), "Living Arsenal kill was not recorded");
		HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(7);
		for (LivingWeaponForm form : LivingWeaponForm.values()) {
			LivingWeaponMemoryUnlocks.grantFormMemory(player, form);
		}
		helper.assertTrue(HarbingerArtificerAssignmentHelper.tryGrantLivingArsenalFitting(player)
				.is(ItemInit.assumed_limb_fitting.get()), "Assumed Limb fitting was not granted after seven forms");
		helper.assertTrue(HarbingerArtificerAssignmentHelper.tryGrantBloodLustFitting(player)
				.is(ItemInit.crimson_vestment_fitting.get()), "Crimson Vestment fitting was not granted");
		player.discard();
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 100)
	public static void eachD7LineageIssuesMaterialAndActivatesRegisteredAbility(GameTestHelper helper) {
		testD7(helper, D7Lineage.SILENT_ARCHON, ItemInit.silent_archon_helm.get(), ItemInit.monolith_imbued_cloth.get(),
				ArmorSetAbilityRegistry.SILENT_ARCHON_SEVERANCE);
		testD7(helper, D7Lineage.EDACIOUS, ItemInit.edacious_blood_lust_helm.get(), ItemInit.fargone_proboscis.get(),
				ArmorSetAbilityRegistry.EDACIOUS_BLOODBURST);
		testD7(helper, D7Lineage.SHEOLIC, ItemInit.sheolic_blood_lust_helm.get(), ItemInit.fervent_husk.get(),
				ArmorSetAbilityRegistry.SHEOLIC_BASTION_STANCE);
		testD7(helper, D7Lineage.PHANTASMAL, ItemInit.phantasmal_blood_lust_helm.get(), ItemInit.mnemonic_ambergris.get(),
				ArmorSetAbilityRegistry.MASQUERADE_OF_THE_FORGOTTEN);
		helper.succeed();
	}

	private static void testFork(GameTestHelper helper, ForkFamily family, Item firstPiece, Item reagent) {
		ServerPlayer player = player(helper, "fork-" + family.serializedName());
		HarbingerArtificerAssignmentHelper.brief(player, HarbingerArtificerAssignmentHelper.THREE_ANSWERS_BRIEFED);
		HarbingerArtificerAssignmentHelper.onArmatureUpgrade(player, new ItemStack(firstPiece), 3);
		HarbingerArtificerAssignmentHelper.onArmatureUpgrade(player, new ItemStack(ItemInit.prismatic_boots.get()), 3);
		helper.assertTrue(HarbingerArtificerAssignmentHelper.firstForkFamily(player) == family,
				"later fork upgrade changed the recorded family");
		helper.assertTrue(HarbingerArtificerAssignmentHelper.inspectThreeAnswers(player), "fork inspection failed");
		ItemStack reward = HarbingerArtificerAssignmentHelper.counselThreeAnswers(player);
		helper.assertTrue(reward.is(reagent), "recorded fork returned the wrong reagent");
		equipFork(player, family == ForkFamily.PRISMATIC ? ForkFamily.BARBED : ForkFamily.PRISMATIC);
		Zombie wrongAttacker = EntityType.ZOMBIE.create(helper.getLevel());
		ArmorSetBonusHandler.onPlayerHurt(new LivingDamageEvent.Pre(player,
				new DamageContainer(player.damageSources().mobAttack(wrongAttacker), 8.0F)));
		helper.assertTrue(!HarbingerArtificerAssignmentHelper.has(player,
				HarbingerArtificerAssignmentHelper.THREE_ANSWERS_DEMONSTRATED),
				"an unrelated armor family satisfied the recorded demonstration");
		equipFork(player, family);
		Zombie attacker = EntityType.ZOMBIE.create(helper.getLevel());
		DamageContainer damage;
		if (family == ForkFamily.CHITINITE) {
			Arrow arrow = EntityType.ARROW.create(helper.getLevel());
			damage = new DamageContainer(player.damageSources().arrow(arrow, attacker), 8.0F);
		} else {
			damage = new DamageContainer(player.damageSources().mobAttack(attacker), 8.0F);
		}
		ArmorSetBonusHandler.onPlayerHurt(new LivingDamageEvent.Pre(player, damage));
		helper.assertTrue(HarbingerArtificerAssignmentHelper.has(player,
				HarbingerArtificerAssignmentHelper.THREE_ANSWERS_DEMONSTRATED),
				"real " + family.serializedName() + " set response did not record its demonstration");
		helper.assertTrue(HarbingerArtificerAssignmentHelper.tryGrantForkFitting(player).is(fittingFor(family)),
				"recorded fork fitting was not granted");
		player.discard();
	}

	private static void testD7(GameTestHelper helper, D7Lineage lineage, Item firstPiece, Item reagent,
			net.minecraft.resources.ResourceLocation ability) {
		ServerPlayer player = player(helper, "d7-" + lineage.serializedName());
		HemoCapabilityAccess.getBloodVolume(player).ifPresent(blood -> {
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
		});
		HarbingerArtificerAssignmentHelper.brief(player, HarbingerArtificerAssignmentHelper.WEIGHT_OF_FRAME_BRIEFED);
		HarbingerArtificerAssignmentHelper.onArmatureTierApplied(player, ArmatureTier.MONOLITHIC);
		HarbingerArtificerAssignmentHelper.onArmatureUpgrade(player, new ItemStack(firstPiece), 7);
		helper.assertTrue(HarbingerArtificerAssignmentHelper.inspectWeightOfFrame(player).is(reagent),
				"D7 inspection did not issue the recorded lineage material");
		helper.assertTrue(HarbingerArtificerAssignmentHelper.claimD7Material(player).isEmpty(),
				"D7 inspection material was issued twice");
		equipD7(player, lineage);
		helper.assertTrue(ArmorSetAbilityRegistry.tryActivate(player, ability), "registered D7 ability did not activate");
		helper.assertTrue(HarbingerArtificerAssignmentHelper.has(player,
				HarbingerArtificerAssignmentHelper.WEIGHT_OF_FRAME_DEMONSTRATED), "D7 ability was not recorded");
		helper.assertTrue(HarbingerArtificerAssignmentHelper.tryGrantD7Fitting(player)
				.is(ItemInit.monolithic_frame_fitting.get()), "D7 fitting was not granted");
		player.discard();
	}

	private static ServerPlayer player(GameTestHelper helper, String name) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), name), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override public void send(Packet<?> packet) { }
		};
		return player;
	}

	private static void equipFork(ServerPlayer player, ForkFamily family) {
		switch (family) {
			case BARBED -> equip(player, ItemInit.barbed_helm.get(), ItemInit.barbed_chestplate.get(), ItemInit.barbed_leggings.get(), ItemInit.barbed_boots.get());
			case CHITINITE -> equip(player, ItemInit.chitinite_helm.get(), ItemInit.chitinite_chestplate.get(), ItemInit.chitinite_leggings.get(), ItemInit.chitinite_boots.get());
			case PRISMATIC -> equip(player, ItemInit.prismatic_helm.get(), ItemInit.prismatic_chestplate.get(), ItemInit.prismatic_leggings.get(), ItemInit.prismatic_boots.get());
			case NONE -> { }
		}
	}

	private static Item fittingFor(ForkFamily family) {
		return switch (family) {
			case BARBED -> ItemInit.barbed_fitting.get();
			case CHITINITE -> ItemInit.chitinite_fitting.get();
			case PRISMATIC -> ItemInit.prismatic_fitting.get();
			case NONE -> ItemInit.worn_vow_fitting.get();
		};
	}

	private static void equipD7(ServerPlayer player, D7Lineage lineage) {
		switch (lineage) {
			case SILENT_ARCHON -> equip(player, ItemInit.silent_archon_helm.get(), ItemInit.silent_archon_chestplate.get(), ItemInit.silent_archon_leggings.get(), ItemInit.silent_archon_boots.get());
			case EDACIOUS -> equip(player, ItemInit.edacious_blood_lust_helm.get(), ItemInit.edacious_blood_lust_chest.get(), ItemInit.edacious_blood_lust_legs.get(), ItemInit.edacious_blood_lust_boots.get());
			case SHEOLIC -> equip(player, ItemInit.sheolic_blood_lust_helm.get(), ItemInit.sheolic_blood_lust_chest.get(), ItemInit.sheolic_blood_lust_legs.get(), ItemInit.sheolic_blood_lust_boots.get());
			case PHANTASMAL -> equip(player, ItemInit.phantasmal_blood_lust_helm.get(), ItemInit.phantasmal_blood_lust_chest.get(), ItemInit.phantasmal_blood_lust_legs.get(), ItemInit.phantasmal_blood_lust_boots.get());
			case NONE -> { }
		}
	}

	private static void equip(ServerPlayer player, Item head, Item chest, Item legs, Item feet) {
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(head));
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(chest));
		player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(legs));
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(feet));
	}

	private static boolean hasEvent(DialogueNode node, String event) {
		return node.options().stream().anyMatch(option -> event.equals(option.eventId()));
	}
}
