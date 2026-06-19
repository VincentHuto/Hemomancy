package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.Set;
import java.util.function.Consumer;

public interface IScars extends IItemHandlerModifiable {
	int FUNGAL_SLOT = 0;

	void bindHolder(LivingEntity holder);

	Set<ResourceLocation> getKnownCerebralScars();

	Set<ResourceLocation> getActiveCerebralScars();

	boolean knowsCerebralScar(ResourceLocation scarId);

	void addKnownCerebralScar(ResourceLocation scarId);

	boolean activateCerebralScar(ResourceLocation scarId);

	boolean deactivateCerebralScar(ResourceLocation scarId);

	ItemStack getFungalScar();

	void setFungalScar(ItemStack stack);

	void forEachActiveCerebralScar(Consumer<ScarDefinition> consumer);

	void tick();
}
