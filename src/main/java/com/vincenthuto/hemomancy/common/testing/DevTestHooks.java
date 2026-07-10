package com.vincenthuto.hemomancy.common.testing;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.lang.reflect.InvocationTargetException;

/**
 * Release-safe bridge to optional classes in {@code src/gameTest}. Custom
 * source-set classes are on dev run classpaths but are not part of the mod
 * annotation scan, so main supplies the discovery hook without packaging the
 * actual test harness.
 */
public final class DevTestHooks {
	private static final String GAME_TEST_CLASS =
			"com.vincenthuto.hemomancy.gametest.HarbingerPilotGameTests";
	private static final String COMMAND_CLASS =
			"com.vincenthuto.hemomancy.gametest.HemoTestCommands";

	private DevTestHooks() {
	}

	public static void registerGameTests(RegisterGameTestsEvent event) {
		try {
			event.register(Class.forName(GAME_TEST_CLASS));
		} catch (ClassNotFoundException ignored) {
			// Expected in packaged release jars where the gameTest source set is absent.
		}
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
