package com.vincenthuto.hemomancy.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.vincenthuto.hemomancy.support.RepoPaths;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The syntax of {@code /hemo}, node by node: every literal and argument (with its type and bounds), whether it
 * executes, whether it suggests, and what permission each node demands. Recorded before the command was split into
 * one class per subcommand (step 11); the split must keep it byte for byte.
 *
 * <p>A missing snapshot is written to {@code build/command/hemo-tree.txt} and the test fails; review it and copy it to
 * {@code src/test/resources/command/}.
 */
class HemoCommandTreeTest {
	@Test
	void hemoCommandSyntaxIsUnchanged() throws IOException {
		CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
		HemoCommand.register(dispatcher);
		StringBuilder out = new StringBuilder();
		dump(dispatcher.getRoot().getChild("hemo"), "", out);

		Path golden = RepoPaths.resolve("src/test/resources/command/hemo-tree.txt");
		String actual = out.toString();
		if (!Files.exists(golden)) {
			Path recorded = RepoPaths.resolve("build/command/hemo-tree.txt");
			Files.createDirectories(recorded.getParent());
			Files.writeString(recorded, actual, StandardCharsets.UTF_8);
			throw new AssertionError("No /hemo snapshot yet; recorded " + recorded);
		}
		assertEquals(Files.readString(golden, StandardCharsets.UTF_8).replace("\r\n", "\n"), actual,
				"/hemo command syntax changed");
	}

	private static void dump(CommandNode<CommandSourceStack> node, String indent, StringBuilder out) {
		out.append(indent);
		if (node instanceof ArgumentCommandNode<CommandSourceStack, ?> argument) {
			out.append('<').append(argument.getName()).append(':').append(describe(argument.getType())).append('>');
			if (argument.getCustomSuggestions() != null) out.append(" suggests");
		} else {
			out.append(node.getName());
		}
		if (node.getCommand() != null) out.append(" executes");
		if (node.getRedirect() != null) out.append(" redirects ").append(node.getRedirect().getName());
		out.append(" permission").append(lowestPermission(node));
		out.append('\n');
		node.getChildren().stream()
				.sorted(java.util.Comparator.comparing(CommandNode::getName))
				.forEach(child -> dump(child, indent + "  ", out));
	}

	private static String describe(ArgumentType<?> type) {
		if (type instanceof StringArgumentType string) return "string(" + string.getType() + ")";
		if (type instanceof BoolArgumentType) return "bool()";
		if (type instanceof net.minecraft.commands.arguments.EntityArgument) {
			try {
				var single = type.getClass().getDeclaredField("single");
				var playersOnly = type.getClass().getDeclaredField("playersOnly");
				single.setAccessible(true);
				playersOnly.setAccessible(true);
				return "entity(single=" + single.getBoolean(type) + ", playersOnly=" + playersOnly.getBoolean(type) + ")";
			} catch (ReflectiveOperationException exception) {
				throw new AssertionError(exception);
			}
		}
		if (type instanceof IntegerArgumentType || type instanceof DoubleArgumentType
				|| type instanceof FloatArgumentType || type instanceof BoolArgumentType
				|| type instanceof StringArgumentType) {
			return type.toString();
		}
		return type.getClass().getSimpleName();
	}

	/** The lowest permission level this node's own requirement accepts. */
	private static int lowestPermission(CommandNode<CommandSourceStack> node) {
		for (int level = 0; level <= 4; level++) {
			if (node.getRequirement().test(source(level))) return level;
		}
		return -1;
	}

	private static CommandSourceStack source(int permission) {
		return new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, permission, "test",
				Component.literal("test"), null, null);
	}
}
