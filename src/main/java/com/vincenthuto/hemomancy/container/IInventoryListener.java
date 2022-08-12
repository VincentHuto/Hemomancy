/*
 * Decompiled with CFR 0.151.
 */
package com.vincenthuto.hemomancy.container;

import com.vincenthuto.hemomancy.gui.radial.IExtendedItemHandler;

@FunctionalInterface
public interface IInventoryListener {
    public void inventoryChanged(IExtendedItemHandler var1, int var2);
}

