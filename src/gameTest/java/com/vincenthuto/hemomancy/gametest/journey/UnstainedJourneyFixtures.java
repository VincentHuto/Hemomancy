package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class UnstainedJourneyFixtures {
	private UnstainedJourneyFixtures() { }

	public static void prepare(ServerPlayer player, UnstainedJourneyStage stage, BlockPos origin) {
		try {
			HemoJourneyFixtures.cleanup(player, origin);
			player.getPersistentData().put(HemoJourneyFixtures.OWNED_BLOCKS_KEY, new ListTag());
			HemoJourneyFixtures.buildPlatform(player, origin);
			player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
			player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
			switch (stage) {
			case NOVITIATE_GATHER_REMEDIES -> {
				spawn(player.serverLevel(), origin, EntityInit.unstained_zealot.get(), -3);
				spawn(player.serverLevel(), origin, EntityInit.unstained_acolyte.get(), 3);
				give(player, new ItemStack(BlockInit.ghost_pipe.get(), 4));
				give(player, new ItemStack(BlockInit.lethean_poppy.get(), 4));
			}
			case NOVITIATE_GENTLE_SEPARATION -> {
				spawn(player.serverLevel(), origin, EntityInit.unstained_acolyte.get(), -3);
				HemoJourneyFixtures.set(player, origin.above(), BlockInit.pallid_retort.get());
				give(player, new ItemStack(ItemInit.lethean_dew.get()));
				give(player, new ItemStack(BlockInit.ghost_pipe.get()));
				give(player, new ItemStack(Items.COAL, 2));
			}
			case NOVITIATE_STILLWATER_LABOR -> {
				spawn(player.serverLevel(), origin, EntityInit.unstained_zealot.get(), -3);
				BlockPos condenser = origin.above();
				HemoJourneyFixtures.set(player, condenser, BlockInit.stillwater_condenser.get());
				HemoJourneyFixtures.set(player, condenser.north(), Blocks.WATER);
				HemoJourneyFixtures.set(player, condenser.east(2), BlockInit.ghost_pipe.get());
				HemoJourneyFixtures.set(player, condenser.west(2), BlockInit.verdigris_lattice.get());
				give(player, new ItemStack(Items.GLASS_BOTTLE, 4));
			}
			case NOVITIATE_CLEAN_LABOR -> {
				spawn(player.serverLevel(), origin, EntityInit.unstained_zealot.get(), -3);
				for (int i = 0; i < 8; i++) HemoJourneyFixtures.set(player,
						origin.offset((i % 4) - 2, 1, 2 + i / 4), BlockInit.venous_stone.get());
				give(player, new ItemStack(ItemInit.consecrated_copper_ingot.get(), 8));
			}
			case NOVITIATE_SHELTER_AFFLICTED -> {
				spawn(player.serverLevel(), origin, EntityInit.unstained_guardian.get(), 3);
				rite(player, origin, "still_waters");
			}
			case PODIUM_SUPPRESSION -> {
				HemoJourneyFixtures.set(player, origin.above(), BlockInit.unstained_podium.get());
				player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.hemolytic_solution.get()));
			}
			case LETHEAN_BAPTISM -> rite(player, origin, "lethean_baptism");
			case GHOST_PIPE_OBSERVANCE -> {
				spawn(player.serverLevel(), origin, EntityInit.unstained_acolyte.get(), -2);
				give(player, new ItemStack(BlockInit.ghost_pipe.get(), 4));
			}
			case TAINTED_ACOLYTE_OBSERVANCES -> {
				purityAtLeast(player, 25f);
				spawn(player.serverLevel(), origin, EntityInit.unstained_acolyte.get(), -2);
				give(player, new ItemStack(BlockInit.lethean_poppy_wreath.get()));
				give(player, new ItemStack(ItemInit.hemolytic_solution.get(), 2));
			}
			case SILVER_VEIL -> rite(player, origin, "silver_veil");
			case CLEANSING_OBSERVANCES -> {
				purityAtLeast(player, 50f);
				BlockPos condenser = origin.above();
				HemoJourneyFixtures.set(player, condenser, BlockInit.stillwater_condenser.get());
				HemoJourneyFixtures.set(player, condenser.north(), Blocks.WATER);
				HemoJourneyFixtures.set(player, condenser.east(2), BlockInit.ghost_pipe.get());
				HemoJourneyFixtures.set(player, condenser.west(2), BlockInit.verdigris_lattice.get());
				spawn(player.serverLevel(), origin, EntityInit.unstained_zealot.get(), -3);
				spawn(player.serverLevel(), origin, EntityInit.unstained_guardian.get(), 3);
				give(player, new ItemStack(Items.GLASS_BOTTLE, 2));
				give(player, new ItemStack(ItemInit.hemolytic_plating.get(), 4));
			}
			case PALLID_ICON_OBSERVANCE -> {
				purityAtLeast(player, 75f);
				spawn(player.serverLevel(), origin, EntityInit.unstained_zealot.get(), -2);
				give(player, new ItemStack(ItemInit.pallid_icon.get()));
			}
			case SILTHMERE_REMEMBRANCE -> {
				purityAtLeast(player, 95f);
				rite(player, origin, "silthmeres_remembrance");
			}
			case CLOSED_VEIN -> rite(player, origin, "closed_vein");
			case CONSECRATED_COPPER_OBSERVANCE -> {
				spawn(player.serverLevel(), origin, EntityInit.unstained_acolyte.get(), -2);
				give(player, new ItemStack(ItemInit.consecrated_copper_ingot.get(), 4));
			}
			case CLARITY_PREPARED -> {
				HemoJourneyFixtures.set(player, origin.above(), BlockInit.unstained_podium.get());
				player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.consecrated_copper_ingot.get()));
			}
			case CLARITY_ASCENSION -> rite(player, origin, "clarity_ascension");
			case GLASS_LUNGS -> {
				clarityAtLeast(player, 50f);
				rite(player, origin, "glass_lungs");
			}
			case CHALICE_OBSERVANCE -> {
				spawn(player.serverLevel(), origin, EntityInit.unstained_acolyte.get(), -2);
			}
			case DISCERNING -> { }
			case PALE_VIGIL -> {
				clarityAtLeast(player, 65f);
				rite(player, origin, "pale_vigil");
			}
			case MOON_WASHED_COPPER -> rite(player, origin, "moon_washed_copper");
			case PALE_WATCH_OBSERVANCE -> {
				spawn(player.serverLevel(), origin, EntityInit.unstained_guardian.get(), 2);
			}
			case RESOLUTE -> { }
			case ENLIGHTENED -> {
				HemoJourneyFixtures.set(player, origin.above(), BlockInit.unstained_podium.get());
				player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.hemolytic_plating.get()));
			}
			case LETHEAN_FONT -> {
				player.removeEffect(EffectInit.silver_ward);
				player.removeEffect(EffectInit.verdigris_aura);
				rite(player, origin, "lethean_font");
			}
			case COMPLETE -> { }
			}
		} catch (RuntimeException exception) {
			HemoJourneyFixtures.cleanup(player, origin);
			throw exception;
		}
	}

	private static void rite(ServerPlayer player, BlockPos origin, String id) {
		HemoJourneyFixtures.prepareCardinalRite(player, origin, id);
	}

	private static void spawn(ServerLevel level, BlockPos origin, EntityType<? extends Mob> type, int x) {
		Mob npc = type.create(level);
		if (npc == null) throw new IllegalStateException("Unstained journey NPC creation returned null");
		BlockPos pos = origin.offset(x, 1, 2);
		npc.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
		npc.setNoAi(true);
		npc.setInvulnerable(true);
		npc.addTag(HemoJourneyFixtures.entityMarker(origin));
		if (!level.addFreshEntity(npc)) throw new IllegalStateException("Unstained journey NPC could not be spawned");
	}

	private static void purityAtLeast(ServerPlayer player, float value) {
		var progress = HemoCapabilityAccess.requireUnstainedProgress(player);
		if (progress.getPurity() < value) progress.setPurity(value);
	}

	private static void clarityAtLeast(ServerPlayer player, float value) {
		var progress = HemoCapabilityAccess.requireUnstainedProgress(player);
		if (progress.getClarity() < value) progress.setClarity(value);
	}

	private static void give(ServerPlayer player, ItemStack stack) {
		if (!player.addItem(stack)) player.drop(stack, false);
	}
}
