package com.vincenthuto.hemomancy.radial;

import java.util.Optional;
import java.util.function.IntFunction;

import com.google.gson.JsonElement;
import com.vincenthuto.hemomancy.item.ItemVasculariumCharm;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public abstract class CharmFinder {
	private static NonNullList<CharmFinder> instances = NonNullList.create();

	public static synchronized void addFinder(CharmFinder finder) {
		instances.add(0, finder);
	}

	public static Optional<? extends CharmGetter> findCharm(LivingEntity player) {
		return findCharm(player, false);
	}

	public static Optional<? extends CharmGetter> findCharm(LivingEntity player, boolean allowCosmetic) {
		return instances.stream().map(f -> f.findStack(player, allowCosmetic)).filter(Optional::isPresent).findFirst()
				.orElseGet(Optional::empty);
	}

	public static void sendSync(Player player) {
		findCharm(player).ifPresent(CharmGetter::syncToClients);
	}

	public static void setCharmFromPacket(Player player, String where, JsonElement slot, ItemStack stack) {
		for (CharmFinder finder : instances) {
			if (finder.getName().equals(where)) {
				finder.getSlotFromId(player, slot).ifPresent(getter -> getter.setCharm(stack));
			}
		}
	}

	public abstract String getName();

	public abstract Optional<? extends CharmGetter> findStack(LivingEntity player, boolean allowCosmetic);

	protected abstract Optional<CharmGetter> getSlotFromId(Player player, JsonElement slot);

	protected final Optional<? extends CharmGetter> findCharmInInventory(IItemHandler inventory,
			IntFunction<? extends CharmGetter> getterFactory) {
		for (int i = 0; i < inventory.getSlots(); i++) {
			ItemStack inSlot = inventory.getStackInSlot(i);
			if (inSlot.getCount() > 0) {
				if (inSlot.getItem() instanceof ItemVasculariumCharm) {
					return Optional.of(getterFactory.apply(i));
				}
			}
		}
		return Optional.empty();
	}

	public interface CharmGetter {
		ItemStack getCharm();

		default void setCharm(ItemStack stack) {
			// Defaults to "do nothing"
		}

		default boolean isHidden() {
			return false;
		}

		void syncToClients();
	}
}
