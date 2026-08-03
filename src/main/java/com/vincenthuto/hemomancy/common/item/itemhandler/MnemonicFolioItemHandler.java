package com.vincenthuto.hemomancy.common.item.itemhandler;

import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

public final class MnemonicFolioItemHandler extends ItemStackHandler {
	private static final String INVENTORY_TAG = "MnemonicFolioInventory";
	private final ItemStack folio;
	private boolean dirty;
	private boolean loaded;

	public MnemonicFolioItemHandler(ItemStack folio, int slots) {
		super(slots);
		this.folio = folio;
	}

	public void loadIfNotLoaded() {
		if (loaded) return;
		CompoundTag root = folio.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (root.contains(INVENTORY_TAG)) deserializeNBT(provider(), root.getCompound(INVENTORY_TAG));
		loaded = true;
		dirty = false;
	}

	public void save() {
		if (!dirty) return;
		CompoundTag root = folio.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		root.put(INVENTORY_TAG, serializeNBT(provider()));
		folio.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
		dirty = false;
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return stack.getItem() instanceof MnemonicBlueprintItem;
	}

	@Override
	protected void onContentsChanged(int slot) {
		dirty = true;
	}

	private HolderLookup.Provider provider() {
		if (ServerLifecycleHooks.getCurrentServer() != null) return ServerLifecycleHooks.getCurrentServer().registryAccess();
		if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) return clientProvider();
		return RegistryAccess.EMPTY;
	}

	@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
	private HolderLookup.Provider clientProvider() {
		net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
		return minecraft != null && minecraft.level != null ? minecraft.level.registryAccess() : RegistryAccess.EMPTY;
	}
}
