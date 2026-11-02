package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTargetGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A player readying a Cardinal Rite's ground by hand, lifted from {@code PlaythroughAdeptGameTests} so every band
 * shares one copy: the required structure (pillars) set block by block, an Iron Brazier on each offering socket with
 * its offering clicked in and lit by held Projection, the medium seated in the Focus with a real {@code useItemOn},
 * and a Working craft's subject built beside the Focus. Building stock the player would have brought is supplied
 * (labelled ARRANGE in the log); everything else goes through the entry points a client reaches. Play the rite itself
 * with {@link PlaythroughClock#rite(PlaythroughPlayer, BlockPos, RitePlan)}.
 */
public final class PlaythroughRites {
	/** Default timeouts by rite size, in server ticks. */
	public static final int LESSER_TICKS = 6000;
	public static final int GREATER_TICKS = 9000;
	public static final int GRAND_TICKS = 14000;

	private PlaythroughRites() {
	}

	/** The default timeout for {@code recipe}: 6000 for minor and lesser rites, 9000 for greater, 14000 for grand. */
	public static int defaultTicks(CardinalRiteRecipe recipe) {
		if (recipe == null) return GREATER_TICKS;
		CardinalRiteType type = recipe.getRiteType();
		return type == CardinalRiteType.GRAND ? GRAND_TICKS : type == CardinalRiteType.GREATER ? GREATER_TICKS : LESSER_TICKS;
	}

	// --- Standing and structure ------------------------------------------------------------------------------------

	/** Where the player stands for a rite: on the floor two blocks off the Focus diagonally, inside reach of its parts. */
	public static Vec3 riteStand(PlaythroughSites.RiteFloor floor) {
		BlockPos focus = floor.focus();
		return new Vec3(focus.getX() + 2.5D, focus.getY() + 1.0D, focus.getZ() + 2.5D);
	}

	/**
	 * The world cells of the rite's {@code required_structure}, lowest first, with the offsets
	 * {@code HemoJourneyFixtures.prepareLayeredRankupRite} uses (the structure stands on the cell above the Focus).
	 * The block for each cell is added to {@code blocks}.
	 */
	public static List<BlockPos> structureCells(CardinalRiteRecipe recipe, BlockPos focus, List<Block> blocks) {
		List<BlockPos> cells = new ArrayList<>();
		var structure = recipe.getRequiredStructure();
		if (structure == null) return cells;
		var pattern = structure.getBlockPattern();
		int cellX = pattern.getWidth() / 2;
		int cellZ = pattern.getDepth() / 2;
		var pairs = new ArrayList<>(structure.getBlockPosBlockList());
		pairs.sort(Comparator.comparingInt(pair -> pair.getPos().getY()));
		for (var pair : pairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos relative = pair.getPos();
			cells.add(focus.above().offset(relative.getX() - cellX, relative.getY(), cellZ - relative.getZ()));
			blocks.add(block);
		}
		return cells;
	}

	/** Adds the player setting each block of the rite's required structure (its pillars) by hand, lowest first. */
	public static void buildStructure(PlaythroughClock clock, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) {
		clock.then(() -> buildStructureNow(p, floor));
	}

	/** {@link #buildStructure} done now, outside a clock. */
	public static void buildStructureNow(PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) {
		List<Block> blocks = new ArrayList<>();
		List<BlockPos> cells = structureCells(floor.recipe(), floor.focus(), blocks);
		for (int i = 0; i < cells.size(); i++) build(p, cells.get(i), blocks.get(i));
		PlaythroughLog.note(p.helper(), "set " + cells.size() + " structure blocks for " + floor.recipe().getId());
	}

	/**
	 * Places {@code block} at {@code pos} by hand (the item supplied when the player has none, ARRANGE); when a
	 * player could not place it there (no item, nothing to place it on), sets it directly and logs that.
	 */
	public static void build(PlaythroughPlayer p, BlockPos pos, Block block) {
		ServerLevel level = p.player().serverLevel();
		if (level.getBlockState(pos).is(block)) return;
		Item item = block.asItem();
		if (item != Items.AIR && !level.getBlockState(pos.below()).isAir()) {
			if (p.count(item) == 0) p.give(new ItemStack(item, 16)); // ARRANGE: building stock
			p.place(pos, item);
		}
		if (!level.getBlockState(pos).is(block)) {
			set(p.helper(), pos, block); // ARRANGE fallback
			PlaythroughLog.note(p.helper(), "could not place " + block + " at " + pos.toShortString() + " by hand; set it");
		}
	}

	// --- Braziers and offerings ------------------------------------------------------------------------------------

	/** One stack per offering the recipe's brazier signature asks for, in signature order. */
	public static List<ItemStack> offerings(CardinalRiteRecipe recipe) {
		List<ItemStack> stacks = new ArrayList<>();
		for (CardinalRiteRecipe.BrazierRequirement requirement : recipe.getBrazierSignature()) {
			for (int copy = 0; copy < requirement.count(); copy++) {
				stacks.add(requirement.ingredient().getItems()[0].copyWithCount(1));
			}
		}
		return stacks;
	}

	/** The sockets the recipe's offerings fill, first to last. */
	public static List<BlockPos> usedSockets(PlaythroughSites.RiteFloor floor) {
		int used = offerings(floor.recipe()).size();
		return floor.brazierSockets().subList(0, Math.min(used, floor.brazierSockets().size()));
	}

	public static void braziers(PlaythroughClock clock, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) {
		braziers(clock, p, floor, offerings(floor.recipe()));
	}

	/**
	 * Adds, for each offering, the player setting an Iron Brazier on the next socket (or reusing the one there and
	 * taking back its old offering), clicking the offering in and lighting it with held Projection.
	 */
	public static void braziers(PlaythroughClock clock, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor,
			List<ItemStack> offerings) {
		GameTestHelper helper = p.helper();
		helper.assertTrue(floor.brazierSockets().size() >= offerings.size(),
				floor.recipe().getId() + " has a socket for each of its " + offerings.size() + " offerings");
		for (int i = 0; i < offerings.size(); i++) {
			BlockPos socket = floor.brazierSockets().get(i);
			ItemStack offering = offerings.get(i);
			clock.then(() -> {
				p.moveTo(besideSocket(floor, socket)); // ARRANGE: walk to the socket (Projection reaches 5.5 blocks)
				build(p, socket, BlockInit.iron_brazier.get());
				IronBrazierBlockEntity brazier = brazier(p, socket);
				if (brazier.hasOffering() && !ItemStack.isSameItem(brazier.getOfferingForMatching(), offering)) {
					emptyHand(p);
					p.sneakUseOn(socket);
				}
				if (!brazier.hasOffering()) {
					p.give(offering.copy()); // ARRANGE: the offering is in the player's pack
					insertOffering(p, socket, stack -> ItemStack.isSameItem(stack, offering), offering.getHoverName().getString());
				}
			}).step(() -> lightBrazier(p, socket))
					.then(() -> helper.assertTrue(lit(p, socket),
							"the offering brazier at " + socket.toShortString() + " lights; " + p.last()));
		}
		clock.then(() -> p.moveTo(riteStand(floor))); // ARRANGE: back to the Focus
	}

	/** A spot on the floor 1.5 blocks outside a brazier socket, on the side away from the Focus. */
	public static Vec3 besideSocket(PlaythroughSites.RiteFloor floor, BlockPos socket) {
		Vec3 centre = Vec3.atBottomCenterOf(socket);
		Vec3 out = centre.subtract(Vec3.atBottomCenterOf(floor.focus())).multiply(1.0D, 0.0D, 1.0D);
		out = out.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : out.normalize();
		return centre.add(out.scale(1.5D));
	}

	/** Clicks the first carried stack matching {@code offering} into the Brazier at {@code brazierPos}. */
	public static void insertOffering(PlaythroughPlayer p, BlockPos brazierPos, Predicate<ItemStack> offering, String label) {
		wieldStack(p, offering, label);
		Outcome put = p.useOn(brazierPos);
		p.helper().assertTrue(brazier(p, brazierPos).hasOffering(), label + " goes into the Brazier; " + put);
	}

	/** Holds Blood Projection on the Brazier's bowl until it lights (at most 160 ticks). */
	public static PlaythroughClock.Step lightBrazier(PlaythroughPlayer p, BlockPos brazierPos) {
		return p.holdUntil(ItemInit.blood_projection.get(), 160, () -> bowl(brazierPos), () -> lit(p, brazierPos));
	}

	/** A point inside the bowl of a floor brazier, so the look ray meets the brazier and not the floor behind it. */
	public static Vec3 bowl(BlockPos pos) {
		return Vec3.atBottomCenterOf(pos).add(0.0D, 0.3D, 0.0D);
	}

	public static boolean lit(PlaythroughPlayer p, BlockPos pos) {
		return BrazierBlock.isLit(p.player().serverLevel().getBlockState(pos));
	}

	public static IronBrazierBlockEntity brazier(PlaythroughPlayer p, BlockPos pos) {
		if (p.player().serverLevel().getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier) return brazier;
		p.helper().fail("no Iron Brazier at " + pos.toShortString());
		return null;
	}

	// --- The medium ------------------------------------------------------------------------------------------------

	/**
	 * Seats {@code medium} in the floor's Cardinal Focus the way a player does: takes it in the main hand and
	 * right-clicks the Focus ({@code useItemOn}). The player must carry it. Asserts the Focus now holds a medium.
	 */
	public static Outcome seatMedium(PlaythroughPlayer p, PlaythroughSites.RiteFloor floor, ItemLike medium) {
		p.helper().assertTrue(p.count(medium) > 0, "the player carries " + medium.asItem() + " to seat in the Focus");
		p.wield(medium);
		// A Sanguine Formation (or a Staff) is also an activation tool: a plain right-click on the Focus tries the
		// floor instead, so it seats only with sneak (CardinalFocusBlock.useItemOn). See D6-6.
		boolean activationTool = medium.asItem() == ItemInit.sanguine_formation.get() || medium.asItem() == ItemInit.living_staff.get();
		Outcome seated = activationTool ? p.sneakUseOn(floor.focus()) : p.useOn(floor.focus());
		PlaythroughLog.note(p.helper(), (activationTool ? "sneak-seat " : "seat ") + medium.asItem() + " in the Focus: " + seated.describe());
		p.helper().assertTrue(p.player().serverLevel().getBlockEntity(floor.focus()) instanceof CardinalFocusBlockEntity focus
				&& focus.hasMedium(), "the Focus takes " + medium.asItem() + "; " + seated);
		return seated;
	}

	/** The item the recipe's medium ingredient names first, or null for a rite without one. */
	public static Item mediumOf(CardinalRiteRecipe recipe) {
		return recipe.hasMedium() ? recipe.getMedium().getItems()[0].getItem() : null;
	}

	/**
	 * Readies the floor for the played rite: the required structure built by hand, the offering braziers set, filled
	 * and lit, then the medium seated (supplied first when the player has none, ARRANGE: made elsewhere). The player
	 * ends standing at {@link #riteStand}.
	 */
	public static void prepare(PlaythroughClock clock, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) {
		clock.then(() -> p.moveTo(riteStand(floor)));
		buildStructure(clock, p, floor);
		braziers(clock, p, floor);
		Item medium = mediumOf(floor.recipe());
		if (medium != null) {
			clock.then(() -> {
				if (p.count(medium) == 0) {
					p.give(new ItemStack(medium)); // ARRANGE: the medium is made elsewhere
					PlaythroughLog.note(p.helper(), "ARRANGE supplied the medium " + medium);
				}
				seatMedium(p, floor, medium);
			});
		}
		clock.then(() -> p.moveTo(riteStand(floor)));
	}

	// --- A Working craft's subject ---------------------------------------------------------------------------------

	/**
	 * Builds the Working craft {@code cardinal_rite/<recipePath>}'s subject on the bed south of the Focus, where
	 * {@code PlaceStructurePacket.placeWorkingSubject} lays it: block by block, lowest first, by the player's own
	 * placement (a multiblock's item lays its fillers, so the Sanguine Monolith BlockItem fills its upper cell and a
	 * Covenant Throne's subject takes a placed Monolith). Filler cells are skipped; a block a player could not place
	 * is set and logged. Returns the subject's cells.
	 */
	public static List<BlockPos> workingSubject(PlaythroughPlayer p, PlaythroughSites.RiteFloor floor, String recipePath) {
		ServerLevel level = p.player().serverLevel();
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, Hemomancy.rloc("cardinal_rite/" + recipePath));
		p.helper().assertTrue(recipe != null && recipe.isWorkingCraft(), "cardinal_rite/" + recipePath + " is a Working craft");
		MultiblockPattern subject = recipe.getSubject();
		var pattern = subject.getBlockPattern();
		BlockPos target = floor.focus().above().south();
		int cellX = pattern.getWidth() / 2;
		// placeWorkingSubject's cell is the pattern's top row, which puts the physical bottom row on the target cell.
		int physicalCellY = 0;
		int cellZ = pattern.getDepth() - 1;
		var pairs = new ArrayList<>(subject.getBlockPosBlockList());
		pairs.sort(Comparator.comparingInt(pair -> pair.getPos().getY()));
		List<BlockPos> cells = new ArrayList<>();
		Set<BlockPos> laidByMultiblock = new HashSet<>();
		for (var pair : pairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR || block == BlockInit.filler_block.get()) continue;
			BlockPos relative = pair.getPos();
			BlockPos pos = target.offset(relative.getX() - cellX, relative.getY() - physicalCellY, cellZ - relative.getZ());
			cells.add(pos);
			if (laidByMultiblock.contains(pos)) continue;
			build(p, pos, block);
			if (block instanceof IMultiBlock multi) {
				for (BlockPos offset : multi.getFillerOffsets()) laidByMultiblock.add(pos.offset(offset));
			}
		}
		PlaythroughCleanup.onEnd(p.helper(), () -> {
			for (BlockPos pos : cells) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			for (BlockPos pos : laidByMultiblock) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		});
		PlaythroughLog.note(p.helper(), "built the " + recipePath + " subject: " + cells.size() + " cells");
		return cells;
	}

	// --- Rite aims ---------------------------------------------------------------------------------------------------

	/**
	 * The {@code extraAim} for {@link PlaythroughClock#rite}: during inscription, the next unpaid node of each required
	 * support sigil (the Crimson Lodge's Bastion), placed as {@code CardinalRiteInteractionHandler.supportSigils} places
	 * it and aimed as {@code CardinalRiteTargetGeometry.sigilAimPoint} aims.
	 */
	public static BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> supportSigilNodes(PlaythroughPlayer p) {
		return (rite, recipe) -> {
			if (recipe == null || recipe.getCeremony() == null || rite.getPhase() != CardinalRitePhase.INSCRIPTION) return null;
			ServerLevel level = p.player().serverLevel();
			Set<BlockPos> occupied = new HashSet<>();
			for (var anchor : recipe.getCeremony().anchors()) occupied.add(new BlockPos(anchor.x(), 0, anchor.z()));
			for (var socket : recipe.getCeremony().supportSockets()) {
				ResourceLocation id = Hemomancy.rloc(socket.suggestedSigil());
				IchorianSigilDefinition sigil = IchorianSigilRegistry.get(id);
				if (sigil == null || sigil.nodes().isEmpty()) continue;
				BlockPos placement = CardinalRiteSigilPlacementRules.resolveSupportPlacement(
						new BlockPos(socket.x(), 0, socket.z()), sigil.nodes(), occupied);
				occupied.addAll(CardinalRiteSigilPlacementRules.footprint(placement, sigil.nodes()));
				if (!socket.required() || rite.isSigilAwakened(id.toString())) continue;
				int paid = rite.getSigilProgress().getOrDefault(id.toString(), 0);
				if (paid >= sigil.nodes().size()) continue;
				IchorianSigilDefinition.Node node = sigil.nodes().get(paid);
				BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(level,
						rite.getCenterPos().offset(0, socket.y(), 0),
						placement.getX() + (int) Math.round(node.x()), placement.getZ() + (int) Math.round(node.z()));
				return CardinalRiteTargetGeometry.sigilAimPoint(rite.getCenterPos(), surface,
						placement.getX(), placement.getZ(), node.x(), node.z());
			}
			return null;
		};
	}

	/**
	 * Wraps {@code base} for an ORDEAL deadlock: when every anchor that still asks for blood is also flickering or
	 * broken, each second's deficit pressure breaks them again faster than bare Projection (5 mL a tick, 50 mL a repair)
	 * can mend them, and a repair always comes before a fill. Then the aim is the next unpaid node of a support sigil that
	 * has not woken (an optional one too, such as a grand floor's Hematic Lattice), whose stability on waking buys the
	 * window to refill. Otherwise {@code base}. Logged the first time it steps in.
	 */
	public static BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> stabilizeWhenDeadlocked(PlaythroughPlayer p,
			BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> base) {
		return stabilizeWhenDeadlocked(p, base, null);
	}

	/** Ticks a deadlock may last, with no sleeping support sigil left, before {@code rescues} steps in. */
	public static final int DEADLOCK_RESCUE_TICKS = 400;

	/**
	 * As {@link #stabilizeWhenDeadlocked(PlaythroughPlayer, BiFunction)}; when {@code rescues} is not null and the
	 * deadlock outlasts {@link #DEADLOCK_RESCUE_TICKS} with no sleeping support sigil left, the dry anchors are filled
	 * (ARRANGE, logged DEGRADED: decision CHAIN-1, a solo caster cannot climb out) and {@code rescues[0]} counts it, so a
	 * long run can go on to what comes after the rite. The rite still completes through its own code.
	 */
	public static BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> stabilizeWhenDeadlocked(PlaythroughPlayer p,
			BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> base, int[] rescues) {
		boolean[] logged = new boolean[1];
		long[] stuckSince = {-1L};
		return (rite, recipe) -> {
			if (recipe != null && recipe.getCeremony() != null && rite.getPhase() == CardinalRitePhase.ORDEAL
					&& anchorsDeadlocked(rite, recipe)) {
				Vec3 node = sleepingSupportNode(p, rite, recipe);
				if (node != null) {
					if (!logged[0]) {
						logged[0] = true;
						PlaythroughLog.note(p.helper(), "ORDEAL deadlock (every hungry anchor is also damaged, instability "
								+ rite.getInstability() + "): tracing a sleeping support sigil for its stability");
					}
					return node;
				}
				long now = p.helper().getTick();
				if (stuckSince[0] < 0) stuckSince[0] = now;
				if (rescues != null && now - stuckSince[0] >= DEADLOCK_RESCUE_TICKS) {
					List<String> filled = new ArrayList<>();
					for (int index = 0; index < recipe.getCeremony().anchors().size(); index++) {
						int need = rite.bloodNeededForAnchor(index);
						if (need > 0 && rite.fillAnchor(index, need)) filled.add(index + ":" + need);
					}
					rescues[0]++;
					stuckSince[0] = -1L;
					PlaythroughNotRun.degraded(p.helper(), "ARRANGE filled the dry anchors " + filled + " after a "
							+ DEADLOCK_RESCUE_TICKS + "-tick ORDEAL deadlock with no support sigil left (decision CHAIN-1); rescue "
							+ rescues[0]);
				}
			} else {
				stuckSince[0] = -1L;
			}
			return base == null ? null : base.apply(rite, recipe);
		};
	}

	/** The waves that drain anchors (the source of the CHAIN-1 deadlock): left out of a pinned deck. */
	public static final Set<String> DRAINING_WAVES = Set.of("bloodlicker_siphon", "covenant_pressure");

	/**
	 * ARRANGE: {@code plan} with the rite's wave deck pinned, the first time it is seen in ORDEAL, to the recipe's own
	 * pool less the {@link #DRAINING_WAVES} (dealt in pool order; guaranteed waves kept), so a required test does not
	 * hang on the random deck question CHAIN-1 leaves open. Waves already begun are left alone. Any ORDEAL hook the plan
	 * carries still runs. The deck before and after is logged.
	 */
	public static RitePlan withoutDrainingWaves(RitePlan plan, PlaythroughPlayer p) {
		BiConsumer<ActiveCardinalRite, Integer> prior = plan.onPhase().get(CardinalRitePhase.ORDEAL);
		boolean[] pinned = new boolean[1];
		return plan.onPhase(CardinalRitePhase.ORDEAL, (rite, ticks) -> {
			if (!pinned[0]) {
				pinned[0] = true;
				CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(p.player().level(), rite.getRecipeId());
				if (recipe != null && recipe.getCeremony() != null) {
					List<String> safe = new ArrayList<>(recipe.getCeremony().waves());
					safe.removeAll(DRAINING_WAVES);
					List<String> deck = rite.getWaveDeck();
					List<String> before = List.copyOf(deck);
					int guaranteed = recipe.getCeremony().guaranteedWaves().size();
					int from = Math.max(guaranteed, rite.getPhaseTicks() <= 1 ? rite.getCurrentWave() : rite.getCurrentWave() + 1);
					if (!safe.isEmpty()) for (int i = from; i < deck.size(); i++) deck.set(i, safe.get((i - from) % safe.size()));
					PlaythroughLog.note(p.helper(), "ARRANGE pinned the wave deck without the draining waves (decision CHAIN-1 keeps"
							+ " the random deck open): " + before + " -> " + deck);
				}
			}
			if (prior != null) prior.accept(rite, ticks);
		});
	}

	/** Some anchor still asks for blood, and every such anchor also needs an instability repair first. */
	public static boolean anchorsDeadlocked(ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		boolean hungry = false;
		for (int index = 0; index < recipe.getCeremony().anchors().size(); index++) {
			if (rite.bloodNeededForAnchor(index) <= 0) continue;
			hungry = true;
			if (rite.instabilityRepairBloodNeeded(index) <= 0) return false;
		}
		return hungry;
	}

	/** The next unpaid node of the first support sigil (required or not) that has not woken, or null. */
	private static Vec3 sleepingSupportNode(PlaythroughPlayer p, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		ServerLevel level = p.player().serverLevel();
		Set<BlockPos> occupied = new HashSet<>();
		for (var anchor : recipe.getCeremony().anchors()) occupied.add(new BlockPos(anchor.x(), 0, anchor.z()));
		for (var socket : recipe.getCeremony().supportSockets()) {
			ResourceLocation id = Hemomancy.rloc(socket.suggestedSigil());
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(id);
			if (sigil == null || sigil.nodes().isEmpty()) continue;
			BlockPos placement = CardinalRiteSigilPlacementRules.resolveSupportPlacement(
					new BlockPos(socket.x(), 0, socket.z()), sigil.nodes(), occupied);
			occupied.addAll(CardinalRiteSigilPlacementRules.footprint(placement, sigil.nodes()));
			if (rite.isSigilAwakened(id.toString())) continue;
			int paid = rite.getSigilProgress().getOrDefault(id.toString(), 0);
			if (paid >= sigil.nodes().size()) continue;
			IchorianSigilDefinition.Node node = sigil.nodes().get(paid);
			BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(level, rite.getCenterPos().offset(0, socket.y(), 0),
					placement.getX() + (int) Math.round(node.x()), placement.getZ() + (int) Math.round(node.z()));
			return CardinalRiteTargetGeometry.sigilAimPoint(rite.getCenterPos(), surface, placement.getX(), placement.getZ(),
					node.x(), node.z());
		}
		return null;
	}

	/**
	 * Diagnostics for a rite that stalls in inscription: whether Projection is held, where the player looks, the block
	 * the projection ray hits, and each support sigil node's surface with its aim error against the look ray.
	 */
	public static String describeAim(PlaythroughPlayer p, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		var player = p.player();
		StringBuilder out = new StringBuilder("using=" + player.isUsingItem() + " hand=" + player.getMainHandItem().getItem()
				+ " eye=" + player.getEyePosition() + " look=" + player.getLookAngle());
		var hit = com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting.pick(player.serverLevel(), player,
				com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting.PROJECTION_REACH, true);
		out.append(" hit=").append(hit instanceof net.minecraft.world.phys.BlockHitResult block
				? block.getBlockPos().toShortString() : hit.getType().toString());
		if (recipe == null || recipe.getCeremony() == null) return out.toString();
		Set<BlockPos> occupied = new HashSet<>();
		for (var anchor : recipe.getCeremony().anchors()) occupied.add(new BlockPos(anchor.x(), 0, anchor.z()));
		for (var socket : recipe.getCeremony().supportSockets()) {
			IchorianSigilDefinition sigil = IchorianSigilRegistry.get(Hemomancy.rloc(socket.suggestedSigil()));
			if (sigil == null) continue;
			BlockPos placement = CardinalRiteSigilPlacementRules.resolveSupportPlacement(
					new BlockPos(socket.x(), 0, socket.z()), sigil.nodes(), occupied);
			occupied.addAll(CardinalRiteSigilPlacementRules.footprint(placement, sigil.nodes()));
			out.append(" ").append(socket.suggestedSigil()).append("@").append(placement.toShortString()).append("[");
			for (IchorianSigilDefinition.Node node : sigil.nodes()) {
				BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(player.serverLevel(),
						rite.getCenterPos().offset(0, socket.y(), 0),
						placement.getX() + (int) Math.round(node.x()), placement.getZ() + (int) Math.round(node.z()));
				Vec3 aim = CardinalRiteTargetGeometry.sigilAimPoint(rite.getCenterPos(), surface,
						placement.getX(), placement.getZ(), node.x(), node.z());
				Vec3 offset = aim.subtract(player.getEyePosition());
				double along = offset.dot(player.getLookAngle());
				double perp = player.getEyePosition().add(player.getLookAngle().scale(along)).distanceTo(aim);
				out.append(surface.subtract(rite.getCenterPos()).toShortString()).append(String.format(" e%.3f; ", perp));
			}
			out.append("]");
		}
		return out.toString();
	}

	// --- Hands -------------------------------------------------------------------------------------------------------

	/** Selects an empty hotbar slot, or moves what is held into a free inventory slot. */
	public static void emptyHand(PlaythroughPlayer p) {
		var inventory = p.player().getInventory();
		if (inventory.getSelected().isEmpty()) return;
		for (int slot = 0; slot < 9; slot++) {
			if (inventory.items.get(slot).isEmpty()) {
				inventory.selected = slot;
				return;
			}
		}
		int free = inventory.getFreeSlot();
		p.helper().assertTrue(free >= 0, "the player has a free slot to empty their hand into");
		inventory.items.set(free, inventory.getSelected().copy());
		inventory.items.set(inventory.selected, ItemStack.EMPTY);
	}

	/** Brings the first stack matching {@code what} into the main hand, as {@code wield} does for an item. */
	public static ItemStack wieldStack(PlaythroughPlayer p, Predicate<ItemStack> what, String label) {
		var inventory = p.player().getInventory();
		if (!inventory.getSelected().isEmpty() && what.test(inventory.getSelected())) return inventory.getSelected();
		for (int slot = 0; slot < inventory.items.size(); slot++) {
			ItemStack stack = inventory.items.get(slot);
			if (stack.isEmpty() || !what.test(stack)) continue;
			if (slot < 9) {
				inventory.selected = slot;
			} else {
				ItemStack held = inventory.getSelected();
				inventory.items.set(inventory.selected, stack);
				inventory.items.set(slot, held);
			}
			return inventory.getSelected();
		}
		p.helper().fail("the player has no " + label + " to hold");
		return ItemStack.EMPTY;
	}

	/** ARRANGE: a block set directly (a structure piece the test does not play), removed when the test ends. */
	public static void set(GameTestHelper helper, BlockPos pos, Block block) {
		ServerLevel level = helper.getLevel();
		level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
		PlaythroughCleanup.onEnd(helper, () -> {
			if (level.getBlockState(pos).is(block)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		});
	}
}
