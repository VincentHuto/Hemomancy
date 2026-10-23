package com.vincenthuto.hemomancy.common.testing;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import java.lang.reflect.InvocationTargetException;

/**
 * Release-safe bridge to the dev test commands in {@code src/gameTest}, which packaged release jars do not
 * contain. GameTests themselves are found only by the mod annotation scan: the gameTest source set is part of
 * the mod in dev runs, so every test class carries a {@code @GameTestHolder}.
 */
public final class DevTestHooks {
	private static final String COMMAND_CLASS =
			"com.vincenthuto.hemomancy.gametest.HemoTestCommands";

	private DevTestHooks() {
	}

	public static void registerCommandsIfPresent(CommandDispatcher<CommandSourceStack> dispatcher) {
		try {
			Class.forName(COMMAND_CLASS).getMethod("register", CommandDispatcher.class).invoke(null, dispatcher);
		} catch (ClassNotFoundException ignored) {
			// Expected in packaged release jars where the gameTest source set is absent.
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
			throw new IllegalStateException("Unable to register Hemomancy dev test commands", exception);
		}
	}
}
