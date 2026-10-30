package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Whose Blood Binding holds a would-be surrogate, kept on the mob: the Rite of Possession takes only a surrogate its
 * caster has bound. A binding cast on a Qliphoth grand floor also names the floor's Focus, and lasts until that floor
 * is broken or a rite run on it ends ({@code ritePassed} records that one began).
 */
public final class SurrogateTether implements INBTSerializable<CompoundTag> {
	@Nullable
	private UUID binder;
	@Nullable
	private BlockPos focus;
	private boolean ritePassed;

	@Nullable
	public UUID binder() {
		return binder;
	}

	/** The Focus of the Qliphoth grand floor holding this binding open, or null for an ordinary timed binding. */
	@Nullable
	public BlockPos focus() {
		return focus;
	}

	public boolean ritePassed() {
		return ritePassed;
	}

	public void bind(UUID binder, @Nullable BlockPos focus) {
		this.binder = binder;
		this.focus = focus;
		this.ritePassed = false;
	}

	public void markRite() {
		ritePassed = true;
	}

	public void clear() {
		binder = null;
		focus = null;
		ritePassed = false;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		if (binder != null) tag.putUUID("binder", binder);
		if (focus != null) tag.putLong("focus", focus.asLong());
		if (ritePassed) tag.putBoolean("rite", true);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		binder = tag.hasUUID("binder") ? tag.getUUID("binder") : null;
		focus = tag.contains("focus") ? BlockPos.of(tag.getLong("focus")) : null;
		ritePassed = tag.getBoolean("rite");
	}
}
