package com.vincenthuto.hemomancy.common.mission.vicar;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.DegreeProgression;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class FirstBloodcraftAssignment {
	private static final String ABSORBED = "hemomancy:first_bloodcraft_absorbed";
	private static final String FORMATION = "hemomancy:first_bloodcraft_formation";
	private static final String VENOUS_STONE = "hemomancy:first_bloodcraft_venous_stone";
	private static final String STRUCTURE = "hemomancy:first_bloodcraft_structure";
	public static final ResourceLocation ADV_REWARD_CLAIMED =
			Hemomancy.rloc("hemomancy/first_bloodcraft_reward_claimed");

	private FirstBloodcraftAssignment() {
	}

	public static boolean canClaim(ServerPlayer player) {
		return HemoCapabilityAccess.getPlayerDegreeNumber(player) == 1 && progress(player).readyForVicar();
	}

	public static FirstBloodcraftLedgerProgress progress(ServerPlayer player) {
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 2)
			return new FirstBloodcraftLedgerProgress(500, true, true, true, true);
		CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
		return new FirstBloodcraftLedgerProgress(data.getDouble(ABSORBED), data.getBoolean(FORMATION),
				data.getBoolean(VENOUS_STONE), data.getBoolean(STRUCTURE),
				HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 2);
	}

	public static void recordAbsorption(ServerPlayer player, double gained) {
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) != 1 || gained <= 0) return;
		CompoundTag data = durableData(player);
		data.putDouble(ABSORBED, Math.min(500, data.getDouble(ABSORBED) + gained));
	}

	public static void recordFormation(ServerPlayer player) { mark(player, FORMATION); }
	public static void recordVenousStone(ServerPlayer player) { mark(player, VENOUS_STONE); }
	public static void recordStructure(ServerPlayer player, ItemStack result) {
		if (result.is(ItemInit.liber_sanguinum.get()) || result.is(BlockInit.hematic_iron_block.get().asItem()))
			mark(player, STRUCTURE);
	}

	private static void mark(ServerPlayer player, String key) {
		if (HemoCapabilityAccess.getPlayerDegreeNumber(player) == 1) durableData(player).putBoolean(key, true);
	}

	private static CompoundTag durableData(ServerPlayer player) {
		CompoundTag root = player.getPersistentData();
		CompoundTag data = root.getCompound(Player.PERSISTED_NBT_TAG);
		root.put(Player.PERSISTED_NBT_TAG, data);
		return data;
	}

	public static boolean promote(ServerPlayer player) {
		return canClaim(player) && DegreeProgression.advance(player, 2);
	}

	public static boolean isClaimed(ServerPlayer player) {
		return HarbingerAdvancementGranter.hasAdvancement(player, ADV_REWARD_CLAIMED);
	}

	public static boolean markClaimed(ServerPlayer player) {
		HarbingerAdvancementGranter.grantIfNotDone(player, ADV_REWARD_CLAIMED);
		return isClaimed(player);
	}

	public static List<ItemStack> rewardStacks() {
		return List.of(
				new ItemStack(ItemInit.hematic_iron_scrap.get(), 4),
				new ItemStack(BlockInit.befouling_ash_trail.get().asItem(), 8),
				new ItemStack(ItemInit.sanguine_formation.get(), 2));
	}
}
