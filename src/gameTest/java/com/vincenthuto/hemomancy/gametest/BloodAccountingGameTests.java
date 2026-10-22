package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.ArmorSetAbilityRegistry;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingAxeItem;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodFlowDiagnostics;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.ProjectedBloodSink;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Step 7: representative payments through {@code BloodAccounting}, each asserting the exact volume before and after,
 * so the migration provably kept every source's balance. Each payment also syncs the owner.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BloodAccountingGameTests {
	private BloodAccountingGameTests() {
	}

	private static void blood(ServerPlayer player, double volume, boolean active) {
		var blood = HemoCapabilityAccess.getBloodVolume(player);
		blood.setMaxBloodVolume(5000.0D);
		blood.setBloodVolume(volume);
		blood.setActive(active);
	}

	private static double volume(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	private static void assertSynced(GameTestHelper helper, CapturedPlayer captured, String what) {
		helper.assertTrue(!captured.sent(BloodVolumeServerPacket.class).isEmpty()
				&& !captured.sent(PacketSyncBloodFlowDiagnostics.class).isEmpty(), what + " did not end in syncVolume");
		captured.payloads.clear();
	}

	/**
	 * Item: a tame Living Axe strike costs 75 mL, plus the 25 mL Living Tool price it inherits on half of all hits.
	 * When short it still empties the volume (F-15 is pending).
	 */
	@GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "blood_accounting", timeoutTicks = 20)
	public static void livingAxeStrikePaysItsExactPrice(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			blood(player, 5000.0D, true);
			ItemStack axe = new ItemStack(ItemInit.living_axe.get());
			CompoundTag tame = new CompoundTag();
			tame.putBoolean(LivingAxeItem.TAG_STATE, true);
			axe.set(DataComponents.CUSTOM_DATA, CustomData.of(tame));
			var target = EntityType.ZOMBIE.create(helper.getLevel());

			axe.getItem().hurtEnemy(axe, target, player);
			helper.assertTrue(volume(player) == 4925.0D || volume(player) == 4900.0D,
					"a strike must cost exactly 75 mL, or 100 mL with the inherited tool price, left " + volume(player));
			assertSynced(helper, captured, "a Living Axe strike");

			blood(player, 40.0D, false);
			axe.getItem().hurtEnemy(axe, target, player);
			helper.assertTrue(volume(player) == 0.0D,
					"a short, dormant wielder still pays everything they hold, left " + volume(player));
			helper.assertTrue(axe.isEmpty(), "an unpaid strike must break the axe when no staff is restored");
			helper.succeed();
		}
	}

	/** Machine: projection into a reservoir moves exactly the rate, from the player into the station's tank. */
	@GameTest(template = "empty", timeoutTicks = 40, batch = "blood_accounting")
	public static void projectionIntoAStationPaysExactlyWhatTheTankTakes(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			VialCentrifugeBlockEntity machine = centrifuge(helper);
			machine.getBloodCapability().setBloodVolume(0.0D);
			blood(player, 1000.0D, true);

			double taken = ((ProjectedBloodSink) machine).takeProjectedBlood(player, machine.getBlockPos(), 100.0D, false);
			helper.assertTrue(taken == 100.0D && volume(player) == 900.0D && machine.getBloodVolume() == 100.0D,
					"projection must move exactly 100 mL, took " + taken + ", player " + volume(player)
							+ ", tank " + machine.getBloodVolume());
			assertSynced(helper, captured, "a projection payment");

			blood(player, 30.0D, true);
			taken = ((ProjectedBloodSink) machine).takeProjectedBlood(player, machine.getBlockPos(), 100.0D, false);
			helper.assertTrue(taken == 30.0D && volume(player) == 0.0D && machine.getBloodVolume() == 130.0D,
					"projection never asks for more than the player holds");
			helper.succeed();
		}
	}

	/**
	 * F-12 is pending: a rite-locked station refuses {@code receiveBlood} but still takes projected blood, as before
	 * step 7. Approving F-12 flips this test.
	 */
	@GameTest(template = "empty", timeoutTicks = 40, batch = "blood_accounting")
	public static void riteLockedStationStillTakesProjectedBloodUntilF12(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			VialCentrifugeBlockEntity machine = centrifuge(helper);
			machine.getBloodCapability().setBloodVolume(0.0D);
			machine.setRiteLocked(true);
			blood(player, 1000.0D, true);
			helper.assertTrue(machine.receiveBlood(50.0D) == 0.0D, "the rite lock must refuse ordinary blood");
			double taken = ((ProjectedBloodSink) machine).takeProjectedBlood(player, machine.getBlockPos(), 100.0D, false);
			helper.assertTrue(taken == 100.0D && volume(player) == 900.0D,
					"projection kept its pre-F-12 behaviour, took " + taken);
			helper.succeed();
		}
	}

	/** Rite: lighting a brazier pours 2.5 mL a tick and counts it as Hemorath blood spend. */
	@GameTest(template = "empty", timeoutTicks = 40, batch = "blood_accounting")
	public static void brazierLightingPaysAndCountsItsPour(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
			helper.getLevel().setBlockAndUpdate(pos, BlockInit.iron_brazier.get().defaultBlockState());
			BlockState state = helper.getLevel().getBlockState(pos);
			BrazierBlock brazier = (BrazierBlock) state.getBlock();
			blood(player, 1000.0D, true);
			double debtBefore = HemoCapabilityAccess.getBloodVolume(player).getBloodDebt();

			double poured = brazier.projectBloodIntoBlock(helper.getLevel(), pos, state, player, 10.0D);
			helper.assertTrue(poured == 2.5D && volume(player) == 997.5D,
					"one pour must cost exactly 2.5 mL, poured " + poured + ", left " + volume(player));
			helper.assertTrue(HemoCapabilityAccess.getBloodVolume(player).getBloodDebt() - debtBefore == 2.5D,
					"the pour must count as blood spend");
			assertSynced(helper, captured, "a brazier pour");

			blood(player, 1000.0D, false);
			helper.assertTrue(brazier.projectBloodIntoBlock(helper.getLevel(), pos, state, player, 10.0D) == 0.0D
					&& volume(player) == 1000.0D, "a dormant volume must not pour");
			helper.succeed();
		}
	}

	/** Armour: Bloodburst costs exactly 250 mL once, and refuses a wearer who cannot pay without touching their blood. */
	@GameTest(template = "empty", timeoutTicks = 40, batch = "blood_accounting")
	public static void armourAbilityPaysItsExactCostOnce(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.edacious_blood_lust_helm.get()));
			player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.edacious_blood_lust_chest.get()));
			player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.edacious_blood_lust_legs.get()));
			player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.edacious_blood_lust_boots.get()));

			blood(player, 200.0D, true);
			helper.assertFalse(ArmorSetAbilityRegistry.tryActivate(player, ArmorSetAbilityRegistry.EDACIOUS_BLOODBURST),
					"Bloodburst must refuse a wearer holding less than its cost");
			helper.assertTrue(volume(player) == 200.0D, "a refused ability must not take blood");

			blood(player, 5000.0D, true);
			helper.assertTrue(ArmorSetAbilityRegistry.tryActivate(player, ArmorSetAbilityRegistry.EDACIOUS_BLOODBURST),
					"Bloodburst must activate for a paying wearer");
			helper.assertTrue(volume(player) == 4750.0D, "Bloodburst must cost exactly 250 mL, left " + volume(player));
			assertSynced(helper, captured, "an armour ability");
			helper.assertFalse(ArmorSetAbilityRegistry.tryActivate(player, ArmorSetAbilityRegistry.EDACIOUS_BLOODBURST),
					"the cooldown must refuse a second activation");
			helper.assertTrue(volume(player) == 4750.0D, "a cooldown refusal must not take blood");
			helper.succeed();
		}
	}

	private static VialCentrifugeBlockEntity centrifuge(GameTestHelper helper) {
		BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
		helper.getLevel().setBlockAndUpdate(pos,
				BlockInit.vial_centrifuge.get().defaultBlockState().setValue(StationTierProperty.STAGE, 1));
		return (VialCentrifugeBlockEntity) helper.getLevel().getBlockEntity(pos);
	}
}
