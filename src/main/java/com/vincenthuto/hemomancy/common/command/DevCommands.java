package com.vincenthuto.hemomancy.common.command;

import net.minecraft.commands.CommandSourceStack;
import net.neoforged.fml.loading.FMLEnvironment;

/** Fixture commands that force progression or stage fights: present in a dev run, absent from a released jar. */
public final class DevCommands {
	private DevCommands() {
	}

	public static boolean available(CommandSourceStack source) {
		return !FMLEnvironment.production;
	}
}
