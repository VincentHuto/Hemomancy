package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.GhastlyAlembicMenu;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.PallidRetortMenu;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import com.vincenthuto.hemomancy.common.recipe.DistillationRecipe;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hemomancy.common.tile.unstained.crafting.PallidRetortBlockEntity;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("distillation_validation")
@PrefixGameTestTemplate(false)
public final class DistillationExtractionGameTests {
	@GameTest(template = "empty")
	public static void ordinaryCapacityRetainsCurrentRules(GameTestHelper helper) {
		ItemStack current = new ItemStack(Items.STONE, 62);
		current.set(DataComponents.CUSTOM_NAME, Component.literal("existing"));
		helper.assertTrue(DistillationRecipe.canAcceptResult(new ItemStack(Items.STONE, 64), ItemStack.EMPTY, 1),
				"Empty output no longer accepts a nonempty result");
		helper.assertTrue(!DistillationRecipe.canAcceptResult(ItemStack.EMPTY, ItemStack.EMPTY, 64),
				"Empty recipe result was accepted");
		helper.assertTrue(DistillationRecipe.canAcceptResult(new ItemStack(Items.STONE, 2), current, 64),
				"Same item with different components was rejected");
		helper.assertTrue(!DistillationRecipe.canAcceptResult(new ItemStack(Items.STONE, 3), current, 64)
					&& !DistillationRecipe.canAcceptResult(new ItemStack(Items.STONE, 2), current, 63)
					&& !DistillationRecipe.canAcceptResult(new ItemStack(Items.DIRT), current, 64),
				"Occupied output count or item limit changed");
		helper.succeed();
	}

	private static ServerPlayer player(GameTestHelper helper) {
		var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "distill-test"), false);
		var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), cookie.clientInformation());
		var connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(player.server, connection, player, cookie) {
			@Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
		};
		player.setGameMode(GameType.SURVIVAL);
		player.setPos(helper.absolutePos(new BlockPos(4, 3, 4)).getCenter());
		return player;
	}

	@GameTest(template = "empty")
	public static void retortPickupAndShiftClickAwardSolution(GameTestHelper helper) {
		var level = helper.getLevel();
		var player = player(helper);
		var progress = HemoCapabilityAccess.requireUnstainedProgress(player);
		progress.setAcceptedObservances(progress.getAcceptedObservances()
				| UnstainedObservances.Observance.NOVITIATE_GENTLE_SEPARATION.mask());
		BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
		level.setBlockAndUpdate(pos, BlockInit.pallid_retort.get().defaultBlockState());
		var retort = (PallidRetortBlockEntity) level.getBlockEntity(pos);
		var recipe = level.getRecipeManager().byKey(ResourceLocation.parse("hemomancy:distillation/hemolytic_solution")).orElseThrow();
		var menu = new PallidRetortMenu(1, player.getInventory(), retort);
		retort.setItem(PallidRetortBlockEntity.SLOT_RESULT, new ItemStack(ItemInit.hemolytic_solution.get(), 2));
		retort.setRecipeUsed(recipe);
		ItemStack pickedUp = menu.getSlot(PallidRetortMenu.RESULT_SLOT).remove(2);
		menu.getSlot(PallidRetortMenu.RESULT_SLOT).onTake(player, pickedUp);
		helper.assertTrue(pickedUp.getCount() == 2 && retort.saveWithoutMetadata(level.registryAccess())
				.getCompound("RecipesUsed").isEmpty(), "Pickup did not award and clear the recipe ledger");
		helper.assertTrue(HemoCapabilityAccess.requireUnstainedProgress(player).isNovitiateRetortComplete(),
				"Pickup did not record the Retort observance");
		retort.setItem(PallidRetortBlockEntity.SLOT_RESULT, new ItemStack(ItemInit.hemolytic_solution.get(), 2));
		retort.setRecipeUsed(recipe);
		ItemStack moved = menu.quickMoveStack(player, PallidRetortMenu.RESULT_SLOT);
		helper.assertTrue(moved.getCount() == 2 && player.getInventory().countItem(ItemInit.hemolytic_solution.get()) == 2,
				"Shift-click lost or duplicated the solution");
		helper.assertTrue(retort.saveWithoutMetadata(level.registryAccess()).getCompound("RecipesUsed").isEmpty(),
				"Shift-click did not clear the recipe ledger");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void alembicPickupAndShiftClickPreserveOutput(GameTestHelper helper) {
		var level = helper.getLevel();
		var player = player(helper);
		BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
		level.setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
		var alembic = (GhastlyAlembicBlockEntity) level.getBlockEntity(pos);
		var recipe = level.getRecipeManager().byKey(ResourceLocation.parse("hemomancy:distillation/test")).orElseThrow();
		var menu = new GhastlyAlembicMenu(1, player.getInventory(), alembic);
		ItemStack output = recipe.value().getResultItem(level.registryAccess()).copy();
		output.set(DataComponents.CUSTOM_NAME, Component.literal("kept"));
		alembic.setItem(GhastlyAlembicBlockEntity.SLOT_RESULT, output.copy());
		alembic.setRecipeUsed(recipe);
		ItemStack pickedUp = menu.getSlot(GhastlyAlembicMenu.RESULT_SLOT).remove(1);
		menu.getSlot(GhastlyAlembicMenu.RESULT_SLOT).onTake(player, pickedUp);
		helper.assertTrue(ItemStack.isSameItemSameComponents(pickedUp, output)
				&& alembic.saveWithoutMetadata(level.registryAccess()).getCompound("RecipesUsed").isEmpty(),
				"Pickup changed output components or retained the ledger");
		alembic.setItem(GhastlyAlembicBlockEntity.SLOT_RESULT, output.copy());
		alembic.setRecipeUsed(recipe);
		ItemStack moved = menu.quickMoveStack(player, GhastlyAlembicMenu.RESULT_SLOT);
		helper.assertTrue(ItemStack.isSameItemSameComponents(moved, output) && moved.getCount() == 1,
				"Shift-click changed output components or count");
		helper.assertTrue(alembic.saveWithoutMetadata(level.registryAccess()).getCompound("RecipesUsed").isEmpty(),
				"Shift-click retained the recipe ledger");
		int experience = level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos).inflate(3))
				.stream().mapToInt(ExperienceOrb::getValue).sum();
		helper.assertTrue(experience == 2, "Pickup and shift-click did not release one XP each");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void hopperExtractionLeavesAwardsForBlockRemoval(GameTestHelper helper) {
		var level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
		level.setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
		var alembic = (GhastlyAlembicBlockEntity) level.getBlockEntity(pos);
		var recipe = level.getRecipeManager().byKey(ResourceLocation.parse("hemomancy:distillation/test")).orElseThrow();
		ItemStack output = recipe.value().getResultItem(level.registryAccess()).copy();
		alembic.setItem(GhastlyAlembicBlockEntity.SLOT_RESULT, output.copy());
		alembic.setRecipeUsed(recipe);
		helper.assertTrue(alembic.canTakeItemThroughFace(GhastlyAlembicBlockEntity.SLOT_RESULT, output, Direction.DOWN),
				"Hopper output access changed");
		ItemStack removed = alembic.removeItemNoUpdate(GhastlyAlembicBlockEntity.SLOT_RESULT);
		helper.assertTrue(removed.getCount() == 1 && !alembic.saveWithoutMetadata(level.registryAccess())
				.getCompound("RecipesUsed").isEmpty(), "Automation consumed the recipe award ledger");
		level.removeBlock(pos, false);
		int experience = level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos).inflate(3))
				.stream().mapToInt(ExperienceOrb::getValue).sum();
		helper.assertTrue(experience == 1, "Block removal did not release the remaining XP");
		helper.succeed();
	}

	@GameTest(template = "empty")
	public static void advancedOutputPickupAndShiftClickRecordProgress(GameTestHelper helper) {
		var level = helper.getLevel();
		var player = player(helper);
		BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
		level.setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
		var alembic = (GhastlyAlembicBlockEntity) level.getBlockEntity(pos);
		var menu = new GhastlyAlembicMenu(1, player.getInventory(), alembic);
		ItemStack output = new ItemStack(ItemInit.sanguine_formation.get());
		alembic.setItem(GhastlyAlembicBlockEntity.SLOT_RESULT, output.copy());
		var saved = alembic.saveWithoutMetadata(level.registryAccess());
		saved.putString("CompletedOperation", "refine");
		saved.put("CompletedOutput", output.save(level.registryAccess()));
		alembic.loadWithComponents(saved, level.registryAccess());
		ItemStack pickedUp = menu.getSlot(GhastlyAlembicMenu.RESULT_SLOT).remove(1);
		menu.getSlot(GhastlyAlembicMenu.RESULT_SLOT).onTake(player, pickedUp);
		helper.assertTrue(HemoCapabilityAccess.advancedBrewing(player).refined()
				&& alembic.saveWithoutMetadata(level.registryAccess()).getString("CompletedOperation").isEmpty(),
				"Advanced pickup did not record and consume the completion");
		alembic.setItem(GhastlyAlembicBlockEntity.SLOT_RESULT, output.copy());
		saved = alembic.saveWithoutMetadata(level.registryAccess());
		saved.putString("CompletedOperation", "compound");
		saved.put("CompletedOutput", output.save(level.registryAccess()));
		alembic.loadWithComponents(saved, level.registryAccess());
		menu.quickMoveStack(player, GhastlyAlembicMenu.RESULT_SLOT);
		helper.assertTrue(HemoCapabilityAccess.advancedBrewing(player).compounded()
				&& alembic.saveWithoutMetadata(level.registryAccess()).getString("CompletedOperation").isEmpty(),
				"Advanced shift-click did not record and consume the completion");
		helper.succeed();
	}
}
