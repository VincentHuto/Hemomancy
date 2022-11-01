/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.level.saveddata.SavedData
 */
package com.vincenthuto.hemomancy.gui.radial;

import java.io.File;
import java.util.UUID;
import java.util.function.Function;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

public class RadialInventoryData<T extends IExtendedItemHandler> extends SavedData {
	private UUID id;
	private final T inventory;
	private int size;

	public static String ID(UUID id) {
		return "inventory-" + id.toString();
	}

	public RadialInventoryData(UUID id, int size, Function<Integer, T> builder) {
		this.id = id;
		this.size = size;
		this.inventory = builder.apply(this.size);
	}

	public T getInventory() {
		return this.inventory;
	}

	public UUID getUUID() {
		return this.id;
	}

	public CompoundTag save(CompoundTag compound) {
		compound.putInt("size", this.size);
		compound.putUUID("id", this.id);
		if (this.inventory != null) {
			compound.put("inventory", (Tag) this.inventory.serialize());
		} else {
			Hemomancy.LOGGER.error("InventoryData inventory is null - writing empty inventory.");
			compound.put("inventory", (Tag) new CompoundTag());
		}
		return compound;
	}

	public void save(File fileIn) {
		super.save(fileIn);
	}

	public void setDirty() {
		super.setDirty();
	}
}
