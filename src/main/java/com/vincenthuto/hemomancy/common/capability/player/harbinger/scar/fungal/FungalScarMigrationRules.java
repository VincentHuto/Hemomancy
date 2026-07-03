package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public final class FungalScarMigrationRules {
	private static final Map<String, String> MIGRATIONS = Map.of(
			"respergillus", "noctifly_agaric",
			"lumina_devorans", "oculiflora_reticularis",
			"anastocordyceps_nexus", "oculiflora_reticularis",
			"thanomyces_resurgens", "cryostroma_perdurans",
			"sanguiflora_cadens", "putrivora_resolvens");

	private FungalScarMigrationRules() {
	}

	public static String migrationTargetPath(String path) {
		return MIGRATIONS.get(path);
	}

	public static ItemStack migrate(ItemStack stack) {
		if (stack.isEmpty()) {
			return stack;
		}
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		if (!Hemomancy.MOD_ID.equals(id.getNamespace())) {
			return stack;
		}
		String targetPath = migrationTargetPath(id.getPath());
		if (targetPath == null) {
			return stack;
		}
		Item target = BuiltInRegistries.ITEM.get(Hemomancy.rloc(targetPath));
		if (target == Items.AIR) {
			return stack;
		}
		return new ItemStack(target, stack.getCount());
	}
}
