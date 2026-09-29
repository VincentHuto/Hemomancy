package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class SpecimenJarLesson {
	private static final String TAUGHT = "hemomancy:specimen_jar_lesson";

	private SpecimenJarLesson() {}

	public static boolean canTeach(ServerPlayer player) {
		return ClinicalBloodKnowledge.eligible(player) && !persisted(player).getBoolean(TAUGHT);
	}

	public static boolean teach(ServerPlayer player, Entity npc) {
		if (!(npc instanceof HarbingerAlchemistEntity) || !EarlyInitiation.near(player, npc)
				|| !canTeach(player)) return false;
		boolean alreadyStudying = HemoCapabilityAccess.requireSpecimenBestiary(player).recordedSpecimenCount() > 0
				|| HarbingerAdvancementGranter.isRedTaxonomyComplete(player);
		CompoundTag persisted = persisted(player);
		persisted.putBoolean(TAUGHT, true);
		player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
		if (!alreadyStudying) {
			ItemStack jars = new ItemStack(BlockInit.specimen_jar.get(), 2);
			if (!player.getInventory().add(jars)) player.drop(jars, false);
		}
		player.server.getRecipeManager().byKey(Hemomancy.rloc("specimen_jar"))
				.ifPresent(recipe -> player.awardRecipes(List.of(recipe)));
		return true;
	}

	private static CompoundTag persisted(ServerPlayer player) {
		return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
	}
}
