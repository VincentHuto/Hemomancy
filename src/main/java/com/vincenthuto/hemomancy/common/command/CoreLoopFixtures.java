package com.vincenthuto.hemomancy.common.command;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenVatBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodGougeBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVatBlockEntity;
import com.vincenthuto.hemomancy.common.world.ground.GroundFunction;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunSavedData;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * The core loop's dev fixtures (core-loop Track 11), run by {@code /hemo temperpool}, {@code /hemo tank},
 * {@code /hemo vat} and {@code /hemo gouge}: a Temper pool carried full or run dry, a machine whose blood has clotted
 * through, a kept creature in a filled Specimen Vat, and a lit gouge run. World fixtures stand two blocks in front of
 * the player; each player has at most one, and placing another takes the first away.
 *
 * <p>What a player's fixture is and the Temper pool it replaced are session state keyed by UUID, cleared and removed
 * at logout through {@code TransientServerStateEvents}: a relog forgets the fixture (its blocks stay) and the pool to
 * restore. Feedback never names a position, so the command fingerprints stay the same wherever a run starts.
 */
public final class CoreLoopFixtures {
	/** Cells in the fixture gouge run. */
	public static final int RUN_LENGTH = 5;
	/** Vigor and buffer of the fixture vat's cow. */
	public static final double VAT_VIGOR = 60.0D;
	public static final double VAT_BUFFER = 400.0D;
	/** What the clotted tank was fed before it clotted through. */
	public static final double TANK_FED = 600.0D;
	/** How far in front of the player a world fixture stands. */
	public static final int AHEAD = 2;

	/** The kinds of world fixture; each branch clears only its own. */
	public enum Kind {
		TANK, VAT, GOUGE
	}

	/** A placed world fixture: where it starts, which way it runs, and how many cells long and tall it is. */
	public record Fixture(Kind kind, BlockPos anchor, Direction facing, int cells, int height) {
	}

	/** What a fixture command did: whether its check passed, and what to tell the operator. */
	public record Outcome(boolean passed, Component message) {
		static Outcome pass(String key, Object... args) {
			return new Outcome(true, Component.translatable(key, args));
		}

		static Outcome fail(String key, Object... args) {
			return new Outcome(false, Component.translatable(key, args));
		}
	}

	/**
	 * One fixture action of a branch: {@code name} runs {@code run} on the caller, or on the named {@code player}, and
	 * reports its outcome (a failed check is a command failure).
	 */
	static LiteralArgumentBuilder<CommandSourceStack> action(String name, Function<ServerPlayer, Outcome> run) {
		return Commands.literal(name)
				.executes(ctx -> report(ctx.getSource(), run.apply(ctx.getSource().getPlayerOrException())))
				.then(Commands.argument("player", EntityArgument.player())
						.executes(ctx -> report(ctx.getSource(), run.apply(EntityArgument.getPlayer(ctx, "player")))));
	}

	private static int report(CommandSourceStack source, Outcome outcome) {
		if (!outcome.passed()) {
			source.sendFailure(outcome.message());
			return 0;
		}
		source.sendSuccess(outcome::message, true);
		return 1;
	}

	private static final Map<UUID, Fixture> FIXTURES = new HashMap<>();
	private static final Map<UUID, float[]> TEMPER_SNAPSHOTS = new HashMap<>();

	private CoreLoopFixtures() {
	}

	// ── Temper pool ──

	/** Fills every school's Temper to the largest cap; the pool it replaced is kept for {@link #restoreTemper}. */
	public static Outcome carryTemper(ServerPlayer player) {
		setTemper(player, TemperRules.MAX_CAP);
		TemperCharge.Charge charge = humilisCast(player);
		return charge != null && charge.outcome() == TemperRules.Outcome.TEMPERED
				? Outcome.pass("commands.hemomancy.temperpool.carried", player.getDisplayName(), (int) TemperRules.MAX_CAP)
				: Outcome.fail("commands.hemomancy.temperpool.not_tempered", player.getDisplayName(), outcomeName(charge));
	}

	/** Empties every school's Temper; the pool it replaced is kept for {@link #restoreTemper}. */
	public static Outcome dryTemper(ServerPlayer player) {
		setTemper(player, 0.0F);
		TemperCharge.Charge charge = humilisCast(player);
		if (charge != null && charge.outcome() == TemperRules.Outcome.CATALYST) {
			return Outcome.pass("commands.hemomancy.temperpool.dry_catalyst", player.getDisplayName());
		}
		return charge != null && charge.dry()
				? Outcome.pass("commands.hemomancy.temperpool.dry", player.getDisplayName())
				: Outcome.fail("commands.hemomancy.temperpool.not_dry", player.getDisplayName(), outcomeName(charge));
	}

	/** Puts back the pool the first carried or dry fixture replaced. */
	public static Outcome restoreTemper(ServerPlayer player) {
		float[] snapshot = TEMPER_SNAPSHOTS.remove(player.getUUID());
		if (snapshot == null) return Outcome.fail("commands.hemomancy.temperpool.nothing_to_restore", player.getDisplayName());
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		for (EnumBloodTemper school : EnumBloodTemper.values()) {
			temper.set(school, snapshot[school.ordinal()], TemperRules.MAX_CAP);
		}
		return Outcome.pass("commands.hemomancy.temperpool.restored", player.getDisplayName());
	}

	private static void setTemper(ServerPlayer player, float amount) {
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		TEMPER_SNAPSHOTS.computeIfAbsent(player.getUUID(), id -> {
			float[] snapshot = new float[EnumBloodTemper.values().length];
			for (EnumBloodTemper school : EnumBloodTemper.values()) snapshot[school.ordinal()] = temper.get(school);
			return snapshot;
		});
		for (EnumBloodTemper school : EnumBloodTemper.values()) temper.set(school, amount, TemperRules.MAX_CAP);
	}

	private static TemperCharge.Charge humilisCast(ServerPlayer player) {
		return TemperCharge.plan(player, EnumBloodTemper.ANIMUS, TemperRules.Kind.CAST,
				TemperRules.castCost(EnumManipulationRank.HUMILIS));
	}

	private static String outcomeName(TemperCharge.Charge charge) {
		return charge == null ? "-" : charge.outcome().name().toLowerCase(java.util.Locale.ROOT);
	}

	// ── World fixtures ──

	/** An Enzymatic Scriptorium fed {@link #TANK_FED} mL a full clot ago: no fresh blood, all clot. */
	public static Outcome placeClottedTank(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		BlockPos at = anchor(player, Kind.TANK, 1, 1);
		level.setBlock(at.below(), Blocks.STONE.defaultBlockState(), 3);
		level.setBlock(at, BlockInit.enzymatic_scriptorium.get().defaultBlockState(), 3);
		if (level.getBlockEntity(at) == null) return Outcome.fail("commands.hemomancy.tank.failed", 0, 0);
		BlockBloodVolume tank = ReservoirBlood.settled(level.getBlockEntity(at));
		tank.fill(TANK_FED);
		tank.backdate(level.getGameTime(), CoagulationRules.FULL_CLOT_TICKS);
		BlockBloodVolume settled = ReservoirBlood.settled(level.getBlockEntity(at));
		long fresh = Math.round(settled.getBloodVolume());
		long clot = Math.round(settled.clotted());
		return fresh == 0 && settled.clotted() >= TANK_FED - 1.0D
				? Outcome.pass("commands.hemomancy.tank.clot", clot)
				: Outcome.fail("commands.hemomancy.tank.failed", fresh, clot);
	}

	/** A Specimen Vat, the player's, keeping a cow at {@link #VAT_VIGOR} over a {@link #VAT_BUFFER} mL buffer. */
	public static Outcome placeFilledVat(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		BlockPos at = anchor(player, Kind.VAT, 1, 2);
		level.setBlock(at.below(), Blocks.STONE.defaultBlockState(), 3);
		BlockState lower = BlockInit.specimen_vat.get().defaultBlockState()
				.setValue(SpecimenVatBlock.FACING, player.getDirection().getOpposite());
		// The lower half goes in without a shape update, so it does not break off for want of its upper half.
		level.setBlock(at, lower, net.minecraft.world.level.block.Block.UPDATE_CLIENTS
				| net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE);
		level.setBlock(at.above(), lower.setValue(SpecimenVatBlock.HALF, DoubleBlockHalf.UPPER), 3);
		Mob cow = EntityType.COW.create(level);
		if (!(level.getBlockEntity(at) instanceof SpecimenVatBlockEntity vat) || cow == null) {
			return Outcome.fail("commands.hemomancy.vat.failed");
		}
		vat.buffer().setOwner(player.getUUID());
		vat.buffer().fill(VAT_BUFFER);
		vat.kept().keep(SpecimenJarData.captureEntity(cow), cow.getMaxHealth(), VAT_VIGOR, level.getGameTime());
		cow.discard();
		vat.setChanged();
		boolean kept = vat.kept().occupied() && vat.kept().living() && vat.kept().type() == EntityType.COW;
		return kept && Math.abs(vat.kept().vigor() - VAT_VIGOR) < 1.0D && vat.buffer().getBloodVolume() > 0.0D
				? Outcome.pass("commands.hemomancy.vat.fill", Math.round(vat.kept().vigor()),
						Math.round(vat.buffer().getBloodVolume()))
				: Outcome.fail("commands.hemomancy.vat.failed");
	}

	/** A {@link #RUN_LENGTH}-cell gouge run on a stone floor, pulsed with the light function for the player. */
	public static Outcome placeLitRun(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		Direction facing = player.getDirection();
		BlockPos at = anchor(player, Kind.GOUGE, RUN_LENGTH, 1);
		BlockState gouge = BlockInit.blood_gouge.get().defaultBlockState()
				.setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);
		for (int i = 0; i < RUN_LENGTH; i++) {
			BlockPos cell = at.relative(facing, i);
			level.setBlock(cell.below(), Blocks.STONE.defaultBlockState(), 3);
			level.setBlock(cell, gouge, 3);
		}
		GroundNetwork.pulse(level, at, GroundFunction.LIGHT, 6_000, null, player.getUUID());
		int clocked = litCells(level, at, facing);
		return clocked == RUN_LENGTH
				? Outcome.pass("commands.hemomancy.gouge.light", RUN_LENGTH)
				: Outcome.fail("commands.hemomancy.gouge.failed", clocked, RUN_LENGTH);
	}

	/** How many cells of a run starting at {@code at} carry a light clock. */
	public static int litCells(ServerLevel level, BlockPos at, Direction facing) {
		GroundRunSavedData clocks = GroundRunSavedData.get(level);
		int clocked = 0;
		for (int i = 0; i < RUN_LENGTH; i++) {
			BlockPos cell = at.relative(facing, i);
			GroundRunSavedData.Lit clock = clocks.get(cell);
			BlockState state = level.getBlockState(cell);
			if (clock != null && clock.function() == GroundFunction.LIGHT && state.getBlock() instanceof BloodGougeBlock) {
				clocked++;
			}
		}
		return clocked;
	}

	/** The player's standing fixture, if any. */
	public static Fixture fixture(Player player) {
		return FIXTURES.get(player.getUUID());
	}

	/** Takes away the player's fixture of {@code kind}; reports when none of that kind stands. */
	public static Outcome clear(ServerPlayer player, Kind kind) {
		Fixture fixture = FIXTURES.get(player.getUUID());
		if (fixture == null || fixture.kind() != kind) {
			return Outcome.fail("commands.hemomancy.fixture.none", player.getDisplayName());
		}
		clear(player);
		return Outcome.pass("commands.hemomancy.fixture.cleared", player.getDisplayName());
	}

	/** Takes away whatever fixture the player has standing, and forgets its Temper snapshot. */
	public static void clear(ServerPlayer player) {
		TEMPER_SNAPSHOTS.remove(player.getUUID());
		removeFixture(player);
	}

	private static void removeFixture(ServerPlayer player) {
		Fixture fixture = FIXTURES.remove(player.getUUID());
		if (fixture == null) return;
		ServerLevel level = player.serverLevel();
		for (int i = 0; i < fixture.cells(); i++) {
			BlockPos cell = fixture.anchor().relative(fixture.facing(), i);
			// The fixture's cow goes with its vat; it is not released into the world.
			if (level.getBlockEntity(cell) instanceof SpecimenVatBlockEntity vat) vat.kept().clear();
			// Top down and without shape updates, so a two-high vessel never breaks into a drop.
			for (int y = fixture.height() - 1; y >= 0; y--) {
				level.setBlock(cell.above(y), Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS
						| net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE);
			}
			GroundNetwork.forgetCell(level, cell);
		}
	}

	private static BlockPos anchor(ServerPlayer player, Kind kind, int cells, int height) {
		removeFixture(player);
		Direction facing = player.getDirection();
		BlockPos at = player.blockPosition().relative(facing, AHEAD);
		FIXTURES.put(player.getUUID(), new Fixture(kind, at, facing, cells, height));
		return at;
	}

	public static void clearSessionState() {
		FIXTURES.clear();
		TEMPER_SNAPSHOTS.clear();
	}

	public static void logout(Player player) {
		FIXTURES.remove(player.getUUID());
		TEMPER_SNAPSHOTS.remove(player.getUUID());
	}
}
