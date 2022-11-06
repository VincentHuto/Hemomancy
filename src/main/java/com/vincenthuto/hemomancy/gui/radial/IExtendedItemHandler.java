/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraftforge.items.IItemHandlerModifiable
 */
package com.vincenthuto.hemomancy.gui.radial;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.container.IInventoryListener;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IExtendedItemHandler extends IItemHandlerModifiable {
	public void addListener(IInventoryListener var1);

	public void deserialize(CompoundTag var1);

	public void enlarge(int var1);

	public long getCountInSlot(int var1);

	@Nullable
	default public RadialInventoryData<?> getInventoryData() {
		return null;
	}

	@Override
	default public int getSlots() {
		return this.size();
	}

	default public void markDirty() {
		RadialInventoryData<?> data = this.getInventoryData();
		if (data != null) {
			data.setDirty();
		}
	}

	public CompoundTag serialize();

	default public void setInventoryData(RadialInventoryData<?> data) {
	}

	public int size();
}
