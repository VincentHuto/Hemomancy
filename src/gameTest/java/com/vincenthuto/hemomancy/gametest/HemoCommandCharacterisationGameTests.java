package com.vincenthuto.hemomancy.gametest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Characterisation of every {@code /hemo} branch before the command is split into one class per subcommand
 * (step 11, item 3). The syntax itself is pinned by the JVM test {@code HemoCommandTreeTest}; this runs every
 * executable path the tree offers, each on a fresh Degree 3 player with argument values taken from the tree (each
 * argument's bounds or first suggestion, the player as {@code @s}), and fingerprints the return code or syntax error,
 * the feedback, the change to the player's saved data and any entity it spawned.
 *
 * <p>{@code /hemo qliphoth tree} is left out: it raises a Qliphoth Bloom into the shared world. The Blood Moon is
 * cancelled again afterwards so it cannot leak into other tests.
 */
@GameTestHolder("hemo_command_validation")
@PrefixGameTestTemplate(false)
public final class HemoCommandCharacterisationGameTests {
	private static final String GOLDEN = "/command/hemo-command-fingerprints.txt";
	private static final Set<String> SKIPPED = Set.of("hemo qliphoth tree",
			// starts the real encounter: a capture, then a transfer into the Chamber, which would carry the probe away
			"hemo mycophant", "hemo vesper");

	private HemoCommandCharacterisationGameTests() {
	}

	@GameTest(template = "empty", timeoutTicks = 200, batch = "hemo_command")
	public static void everyHemoBranchKeepsItsOutcome(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		CommandDispatcher<CommandSourceStack> dispatcher = level.getServer().getCommands().getDispatcher();
		List<String> commands = new ArrayList<>();
		try (CapturedPlayer probe = CapturedPlayer.create(helper)) {
			CommandSourceStack source = probe.player.createCommandSourceStack().withPermission(2);
			for (CommandNode<CommandSourceStack> branch : sorted(dispatcher.getRoot().getChild("hemo").getChildren())) {
				// /hemo test belongs to the GameTest harness, not to HemoCommand.
				if (branch.getName().equals("test")) continue;
				collect(dispatcher, source, branch, "hemo " + branch.getName(), commands);
			}
		}
		List<String> lines = new ArrayList<>();
		for (String command : commands) lines.add(fingerprint(helper, dispatcher, command));
		try (CapturedPlayer cleanup = CapturedPlayer.create(helper)) {
			run(dispatcher, cleanup.player.createCommandSourceStack().withPermission(2), "hemo bloodmoon cancel");
		}
		PlayerFingerprints.assertGolden(helper, GOLDEN, "hemo-command-fingerprints.actual.txt",
				String.join("\n", lines) + "\n");
		helper.succeed();
	}

	private static List<CommandNode<CommandSourceStack>> sorted(java.util.Collection<CommandNode<CommandSourceStack>> nodes) {
		return nodes.stream().sorted(java.util.Comparator.comparing(CommandNode::getName)).toList();
	}

	/** Every executable path below a node, with a value chosen for each argument. */
	private static void collect(CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack source,
			CommandNode<CommandSourceStack> node, String command, List<String> out) {
		if (SKIPPED.stream().anyMatch(command::startsWith)) return;
		if (node.getCommand() != null) out.add(command);
		for (CommandNode<CommandSourceStack> child : sorted(node.getChildren())) {
			String token = child instanceof ArgumentCommandNode<CommandSourceStack, ?> argument
					? value(dispatcher, source, command, argument.getType()) : child.getName();
			collect(dispatcher, source, child, command + " " + token, out);
		}
	}

	private static String value(CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack source,
			String command, ArgumentType<?> type) {
		if (type instanceof IntegerArgumentType integer) {
			int low = Math.max(integer.getMinimum(), 0);
			int high = Math.min(integer.getMaximum(), low + 10);
			return Integer.toString((low + high) / 2);
		}
		if (type instanceof DoubleArgumentType real) {
			double low = Math.max(real.getMinimum(), 0.0D);
			return Double.toString(Math.min(real.getMaximum(), low + 50.0D));
		}
		if (type instanceof FloatArgumentType real) {
			float low = Math.max(real.getMinimum(), 0.0F);
			return Float.toString(Math.min(real.getMaximum(), low + 50.0F));
		}
		if (type instanceof BoolArgumentType) return "true";
		if (type instanceof net.minecraft.commands.arguments.EntityArgument) return "@s";
		var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(command + " ", source)).join().getList();
		return suggestions.isEmpty() ? "unknown" : suggestions.stream().map(s -> s.getText()).sorted().findFirst().orElseThrow();
	}

	private static String fingerprint(GameTestHelper helper, CommandDispatcher<CommandSourceStack> dispatcher,
			String command) {
		ServerLevel level = helper.getLevel();
		BlockPos stand = helper.absolutePos(new BlockPos(8, 2, 8));
		try (CapturedPlayer captured = CapturedPlayer.create(helper).online()) {
			ServerPlayer player = captured.player;
			player.setPos(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D);
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(3);
			var blood = HemoCapabilityAccess.getBloodVolume(player);
			blood.setActive(true);
			blood.setMaxBloodVolume(5000.0D);
			blood.setBloodVolume(2500.0D);
			Set<Integer> present = new HashSet<>();
			for (Entity entity : level.getEntities((Entity) null, new AABB(stand).inflate(24.0D), e -> true)) {
				present.add(entity.getId());
			}
			CompoundTag before = PlayerFingerprints.saved(player);
			String result = run(dispatcher, player.createCommandSourceStack().withPermission(2), command);
			CompoundTag after = PlayerFingerprints.saved(player);
			TreeMap<String, Integer> spawned = new TreeMap<>();
			for (Entity entity : level.getEntities((Entity) null, new AABB(stand).inflate(24.0D),
					e -> !present.contains(e.getId()) && !(e instanceof Player))) {
				spawned.merge(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(),
						1, Integer::sum);
				entity.discard();
			}
			String line = command + " -> " + result + " said=" + captured.chat
					+ " saved=" + PlayerFingerprints.diff(before, after) + " spawned=" + spawned;
			return PlayerFingerprints.normalise(line, player, null);
		}
	}

	private static String run(CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack source, String command) {
		try {
			return "returned " + dispatcher.execute(command, source);
		} catch (CommandSyntaxException exception) {
			return "syntax error: " + exception.getRawMessage().getString();
		}
	}
}
