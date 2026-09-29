package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryContext;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;

public final class FirstDrawsAssignment {
	private static final String DATA = "hemomancy:first_draws";
	private static final String BRIEFED = "Briefed";
	private static final String SAMPLES = "Samples";
	private static final String SPECIES = "Species";

	private FirstDrawsAssignment() {}

	public static boolean eligible(ServerPlayer player) {
		var context = ItemInquiryContext.from(player);
		return context.degree() >= 1 && context.activeBlood()
				&& !context.purifying() && !context.clarityUnlocked();
	}

	public static boolean isBriefed(ServerPlayer player) {
		return data(player).getBoolean(BRIEFED);
	}

	public static boolean canBrief(ServerPlayer player) {
		return eligible(player) && !isBriefed(player);
	}

	public static boolean brief(ServerPlayer player, Entity npc) {
		if (!(npc instanceof HarbingerAlchemistEntity) || !EarlyInitiation.near(player, npc)
				|| !canBrief(player)) return false;
		CompoundTag data = data(player);
		data.putBoolean(BRIEFED, true);
		save(player, data);
		ItemStack vials = new ItemStack(ItemInit.bloody_vial.get(), 5);
		if (!player.getInventory().add(vials)) player.drop(vials, false);
		player.server.getRecipeManager().byKey(Hemomancy.rloc("bloody_vial"))
				.ifPresent(recipe -> player.awardRecipes(List.of(recipe)));
		return true;
	}

	public static FirstDrawsProgress progress(ServerPlayer player) {
		CompoundTag data = data(player);
		var species = new HashSet<ResourceLocation>();
		ListTag list = data.getList(SPECIES, 8);
		for (int i = 0; i < list.size(); i++) {
			ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
			if (id != null) species.add(id);
		}
		return new FirstDrawsProgress(data.getInt(SAMPLES), species);
	}

	public static void recordCollection(ServerPlayer player, LivingEntity source) {
		if (!isBriefed(player) || !eligible(player) || source == player) return;
		ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(source.getType());
		if (!BloodProfileData.snapshot(false).json().containsKey(id)
				|| BloodProfileData.profile(source.getType(), false).requiresLivingSyringe()) return;
		FirstDrawsProgress before = progress(player);
		if (before.complete()) return;
		FirstDrawsProgress after = before.record(id);
		if (after == before) return;
		CompoundTag data = data(player);
		data.putInt(SAMPLES, after.samples());
		ListTag species = new ListTag();
		after.species().stream().map(ResourceLocation::toString).sorted()
				.forEach(value -> species.add(StringTag.valueOf(value)));
		data.put(SPECIES, species);
		save(player, data);
	}

	private static CompoundTag data(ServerPlayer player) {
		return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(DATA);
	}

	private static void save(ServerPlayer player, CompoundTag data) {
		CompoundTag root = player.getPersistentData();
		CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
		persisted.put(DATA, data);
		root.put(Player.PERSISTED_NBT_TAG, persisted);
	}
}
