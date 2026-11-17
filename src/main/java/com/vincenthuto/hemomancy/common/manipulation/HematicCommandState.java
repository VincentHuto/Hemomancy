package com.vincenthuto.hemomancy.common.manipulation;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The Hematic Command a mob is under, kept on the mob as an attachment so it survives the mob's chunk unloading and the
 * server restarting: who commands it, the game time the command runs out, how, and whether the mob's own targeting was
 * already switched off before the command began (so release puts back what was there). {@link HematicCommandManager}
 * owns every read and write.
 */
public final class HematicCommandState implements INBTSerializable<CompoundTag> {
	public enum Mode { REBUKED, IMPRESSED, REDIRECTED, MARIONETTE }

	/** One command. {@code redirectedTarget} is set for {@link Mode#REDIRECTED} only. */
	public record Command(UUID caster, long until, Mode mode, @Nullable UUID redirectedTarget, boolean targetDisabled) {
		public boolean controlled() {
			return mode == Mode.IMPRESSED || mode == Mode.MARIONETTE;
		}
	}

	@Nullable
	private Command command;

	@Nullable
	public Command command() {
		return command;
	}

	public void set(@Nullable Command command) {
		this.command = command;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		if (command == null) return tag;
		tag.putUUID("caster", command.caster());
		tag.putLong("until", command.until());
		tag.putString("mode", command.mode().name());
		if (command.redirectedTarget() != null) tag.putUUID("redirected_target", command.redirectedTarget());
		tag.putBoolean("target_disabled", command.targetDisabled());
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		command = null;
		if (!tag.hasUUID("caster")) return;
		Mode mode;
		try {
			mode = Mode.valueOf(tag.getString("mode"));
		} catch (IllegalArgumentException unknown) {
			return;
		}
		command = new Command(tag.getUUID("caster"), tag.getLong("until"), mode,
				tag.hasUUID("redirected_target") ? tag.getUUID("redirected_target") : null,
				tag.getBoolean("target_disabled"));
	}
}
