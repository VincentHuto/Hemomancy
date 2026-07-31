package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HarbingerPilotGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private HarbingerPilotGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodStructureLocked(GameTestHelper helper) {
		runScenario(helper, "blood_structure_locked");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodStructureUnlocked(GameTestHelper helper) {
		runScenario(helper, "blood_structure_unlocked");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void artificerAssignmentReady(GameTestHelper helper) {
		runScenario(helper, "artificer_assignment_ready");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void artificerRewardClaimed(GameTestHelper helper) {
		runScenario(helper, "artificer_reward_claimed");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void uninitiatedCannotPassBloodcraftDegreeGate(GameTestHelper helper) {
		runScenario(helper, "uninitiated_cannot_pass_bloodcraft_degree_gate");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineInitiationRecipeLoaded(GameTestHelper helper) {
		runScenario(helper, "sanguine_initiation_recipe_loaded");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineInitiationDegreeMapping(GameTestHelper helper) {
		runScenario(helper, "sanguine_initiation_degree_mapping");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void administrativeKillRemovesProtectedNpcs(GameTestHelper helper) {
		List<Mob> npcs = List.of(
				EntityInit.unstained_zealot.get().create(helper.getLevel()),
				EntityInit.unstained_guardian.get().create(helper.getLevel()),
				EntityInit.unstained_acolyte.get().create(helper.getLevel()),
				EntityInit.unstained_scout.get().create(helper.getLevel()),
				EntityInit.harbinger_hermit.get().create(helper.getLevel()),
				EntityInit.harbinger_alchemist.get().create(helper.getLevel()),
				EntityInit.harbinger_artificer.get().create(helper.getLevel()),
				EntityInit.harbinger_cicatrix_anchorite.get().create(helper.getLevel()),
				EntityInit.harbinger_mnemonist.get().create(helper.getLevel()),
				EntityInit.harbinger_vicar.get().create(helper.getLevel()),
				EntityInit.harbinger_voyager.get().create(helper.getLevel()),
				EntityInit.harbinger_votary_wayfarer.get().create(helper.getLevel()));
		try {
			for (Mob npc : npcs) {
				helper.getLevel().addFreshEntity(npc);
				npc.kill();
				helper.assertTrue(npc.isDeadOrDying() || npc.isRemoved(),
						npc.getType() + " must honor administrative kill commands");
			}
			helper.succeed();
		} finally {
			npcs.forEach(Mob::discard);
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void creativeStaffEscrowRemovesAndSynchronizesHeldStaff(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		try {
			player.getAbilities().instabuild = true;
			player.getInventory().selected = 0;
			player.getInventory().setItem(0, new ItemStack(ItemInit.living_staff.get()));
			StaffRemovalListener listener = new StaffRemovalListener();
			player.inventoryMenu.addSlotListener(listener);
			listener.reset();

			ItemStack captured = CardinalRiteStaffEscrow.capture(player);

			helper.assertTrue(captured.is(ItemInit.living_staff.get()),
					"The exact Living Staff must enter rite escrow");
			helper.assertTrue(player.getInventory().getItem(0).isEmpty(),
					"Creative mode must not retain the planted Living Staff");
			helper.assertTrue(listener.sawEmptySlot,
					"Staff removal must be synchronized immediately to the creative client");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	private static void runScenario(GameTestHelper helper, String id) {
		HemoTestScenario scenario = HemoTestScenarioCatalog.find(id)
				.orElseThrow(() -> new IllegalArgumentException("Unknown scenario " + id));
		ServerPlayer player = detachedTestPlayer(helper);
		try {
			scenario.setup().apply(player);
			HemoTestResult result = scenario.verify().check(player);
			helper.assertTrue(result.passed(), result.message());
			helper.succeed();
		} finally {
			scenario.clear().apply(player);
			player.discard();
		}
	}

	private static ServerPlayer detachedTestPlayer(GameTestHelper helper) {
		return new ServerPlayer(
				helper.getLevel().getServer(),
				helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "hemomancy-test-player"),
				ClientInformation.createDefault());
	}

	private static final class StaffRemovalListener implements ContainerListener {
		private boolean sawEmptySlot;

		@Override
		public void slotChanged(AbstractContainerMenu menu, int slot, ItemStack stack) {
			if (stack.isEmpty()) sawEmptySlot = true;
		}

		@Override
		public void dataChanged(AbstractContainerMenu menu, int slot, int value) {
		}

		private void reset() {
			sawEmptySlot = false;
		}
	}

}
