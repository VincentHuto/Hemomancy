package com.vincenthuto.hemomancy.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;

public final class ItemInventoryClientAccess {
    private ItemInventoryClientAccess() {}

    public static HolderLookup.Provider registryAccess() {
        var level = Minecraft.getInstance().level;
        return level == null ? RegistryAccess.EMPTY : level.registryAccess();
    }
}
