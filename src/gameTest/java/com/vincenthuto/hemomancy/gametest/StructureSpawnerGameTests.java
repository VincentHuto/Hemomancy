package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierMount;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.PlaceStructurePacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** The creative Structure Spawner: whole rite floors, and crafts with or without their Working floor. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StructureSpawnerGameTests {
	/** 32 x 12 x 24: room for a grand floor, its sockets and a raised subject. */
	private static final String TEMPLATE = "empty";

	private StructureSpawnerGameTests() {
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "structure_spawner")
	public static void everyRiteFloorSpawnsWhole(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		helper.assertTrue(CardinalRiteFloorRegistry.all().size() >= 16, "Every rite floor must load");
		for (CardinalRiteFloorDefinition floor : CardinalRiteFloorRegistry.all().values()) {
			clear(level, focus);
			helper.assertTrue(PlaceStructurePacket.placeRiteFloor(level, focus, floor) > 0, floor.id() + " placed nothing");
			var formed = CardinalRiteStationMatcher.formedFloor(level, focus);
			helper.assertTrue(formed.isPresent() && formed.get().floor().id().equals(floor.id()),
					floor.id() + " must form around its Focus, got " + formed.map(f -> f.floor().id().toString()).orElse("none"));
			var sockets = floor.brazierSockets();
			for (BlockPos socket : sockets.subList(0, Math.min(PlaceStructurePacket.SPAWNED_BRAZIERS, sockets.size()))) {
				BlockPos pos = focus.offset(socket.getX(), socket.getY(), -socket.getZ());
				helper.assertTrue(level.getBlockState(pos).is(BlockInit.iron_brazier.get())
						&& level.getBlockState(pos).getValue(BrazierBlock.RITUAL_PHASE) > 0
						&& level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier
						&& brazier.getOfferingForMatching().isEmpty(),
						floor.id() + " must spawn a lit, empty brazier on socket " + socket);
			}
		}
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "structure_spawner")
	public static void aSpawnedWorkingFloorTakesACraft(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		CardinalRiteRecipe loom = CardinalRiteRecipe.getRiteByLocation(level,
				Hemomancy.rloc("cardinal_rite/working/somatic_loom"));
		CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(Hemomancy.rloc("working_lesser")).orElseThrow();
		clear(level, focus);
		PlaceStructurePacket.placeRiteFloor(level, focus, floor);
		helper.assertTrue(PlaceStructurePacket.placeWorkingSubject(level, focus, loom) > 0, "The Loom frame must stand");
		helper.assertTrue(level.getBlockEntity(focus) instanceof CardinalFocusBlockEntity seat
				&& seat.insertMedium(null, loom.getMedium().getItems()[0].copyWithCount(1)), "The Focus must take the medium");
		int socket = 0;
		for (var requirement : loom.getBrazierSignature()) {
			BlockPos relative = floor.brazierSockets().get(socket++);
			var brazier = (IronBrazierBlockEntity) level.getBlockEntity(
					focus.offset(relative.getX(), relative.getY(), -relative.getZ()));
			helper.assertTrue(brazier.insertOffering(null, requirement.ingredient().getItems()[0].copyWithCount(1)),
					"A spawned brazier must accept an offering");
		}
		var resolution = CardinalRiteStationMatcher.resolve(level, focus, CardinalRiteRecipe.getAllRecipes(level));
		helper.assertTrue(resolution.status() == CardinalRiteStationMatcher.Status.MATCHED
				&& resolution.matches().getFirst().recipe().getId().equals(loom.getId()),
				"A spawned Working floor must take the Loom craft, got " + resolution.status());
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "structure_spawner")
	public static void braziersCountAnywhereAlongTheGash(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		CardinalRiteRecipe loom = CardinalRiteRecipe.getRiteByLocation(level,
				Hemomancy.rloc("cardinal_rite/working/somatic_loom"));
		CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(Hemomancy.rloc("working_lesser")).orElseThrow();
		clear(level, focus);
		PlaceStructurePacket.placeRiteFloor(level, focus, floor);
		PlaceStructurePacket.placeWorkingSubject(level, focus, loom);
		helper.assertTrue(level.getBlockEntity(focus) instanceof CardinalFocusBlockEntity seat
				&& seat.insertMedium(null, loom.getMedium().getItems()[0].copyWithCount(1)), "The Focus must take the medium");
		// Mid-ring cells, well past the spawned braziers and clear of the Loom on the front of the gash.
		int[] anywhere = {6, 11};
		int offering = 0;
		for (var requirement : loom.getBrazierSignature()) {
			BlockPos relative = floor.brazierSockets().get(anywhere[offering++]);
			BlockPos pos = focus.offset(relative.getX(), relative.getY(), -relative.getZ());
			level.setBlockAndUpdate(pos, BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.RITUAL_PHASE, 1));
			helper.assertTrue(((IronBrazierBlockEntity) level.getBlockEntity(pos))
					.insertOffering(null, requirement.ingredient().getItems()[0].copyWithCount(1)), "offering at " + relative);
		}
		var resolution = CardinalRiteStationMatcher.resolve(level, focus, CardinalRiteRecipe.getAllRecipes(level));
		helper.assertTrue(resolution.status() == CardinalRiteStationMatcher.Status.MATCHED
				&& resolution.matches().getFirst().recipe().getId().equals(loom.getId()),
				"Braziers standing anywhere along the gash must serve the craft, got " + resolution.status());
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "structure_spawner")
	public static void braziersCountAnywhereInTheCubeAboveTheFloor(GameTestHelper helper) {
		// Off the gash: one standing on the floor near the Focus, one raised on a pillar three blocks up.
		helper.assertTrue(loomResolves(helper, new BlockPos(-1, 1, 0), new BlockPos(1, 3, 1)),
				"Braziers anywhere in the space above the floor must serve the craft");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "structure_spawner")
	public static void sconcesAndHangingBraziersCountInTheCube(GameTestHelper helper) {
		// A sconce on a wall just off the floor's east edge, spout toward the Focus; a brazier hung from a block above.
		helper.assertTrue(loomResolves(helper, new Placed(new BlockPos(2, 2, 1), AttachFace.WALL, Direction.WEST),
				new Placed(new BlockPos(-1, 4, 1), AttachFace.CEILING, Direction.NORTH)),
				"A sconce and a hanging brazier in the space above the floor must serve the craft");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "structure_spawner")
	public static void aBrazierAboveTheCubeIsIgnored(GameTestHelper helper) {
		// A lesser floor's space is 5 high: y 5 is one above it.
		helper.assertFalse(loomResolves(helper, new BlockPos(-1, 1, 0), new BlockPos(1, 5, 1)),
				"A brazier above the floor's space must not count");
		helper.succeed();
	}

	/**
	 * Spawns a lesser Working floor with the Loom subject and medium, puts the Loom's two offerings in lit standing
	 * braziers at these socket-frame cells (each on a stone pillar down to the floor), and reports whether it resolves.
	 */
	private static boolean loomResolves(GameTestHelper helper, BlockPos... cells) {
		Placed[] placed = new Placed[cells.length];
		for (int i = 0; i < cells.length; i++) placed[i] = new Placed(cells[i], AttachFace.FLOOR, Direction.NORTH);
		return loomResolves(helper, placed);
	}

	/** A brazier at a socket-frame cell, in a mount; its support is built for it. */
	private record Placed(BlockPos cell, AttachFace face, Direction facing) {
	}

	private static boolean loomResolves(GameTestHelper helper, Placed... cells) {
		ServerLevel level = helper.getLevel();
		BlockPos focus = helper.absolutePos(new BlockPos(16, 3, 12));
		CardinalRiteRecipe loom = CardinalRiteRecipe.getRiteByLocation(level,
				Hemomancy.rloc("cardinal_rite/working/somatic_loom"));
		CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(Hemomancy.rloc("working_lesser")).orElseThrow();
		clear(level, focus);
		PlaceStructurePacket.placeRiteFloor(level, focus, floor);
		PlaceStructurePacket.placeWorkingSubject(level, focus, loom);
		helper.assertTrue(level.getBlockEntity(focus) instanceof CardinalFocusBlockEntity seat
				&& seat.insertMedium(null, loom.getMedium().getItems()[0].copyWithCount(1)), "The Focus must take the medium");
		int offering = 0;
		for (var requirement : loom.getBrazierSignature()) {
			Placed placed = cells[offering++];
			BlockPos relative = placed.cell();
			BlockPos pos = focus.offset(relative.getX(), relative.getY(), -relative.getZ());
			if (placed.face() == AttachFace.FLOOR) {
				for (int y = 1; y < relative.getY(); y++) {
					level.setBlockAndUpdate(focus.offset(relative.getX(), y, -relative.getZ()), Blocks.STONE.defaultBlockState());
				}
			} else {
				level.setBlockAndUpdate(pos.relative(BrazierMount.support(placed.face(), placed.facing())),
						Blocks.STONE.defaultBlockState());
			}
			level.setBlockAndUpdate(pos, BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.RITUAL_PHASE, 1)
					.setValue(BrazierBlock.FACE, placed.face()).setValue(BrazierBlock.FACING, placed.facing()));
			helper.assertTrue(((IronBrazierBlockEntity) level.getBlockEntity(pos))
					.insertOffering(null, requirement.ingredient().getItems()[0].copyWithCount(1)), "offering at " + relative);
		}
		var resolution = CardinalRiteStationMatcher.resolve(level, focus, CardinalRiteRecipe.getAllRecipes(level));
		return resolution.status() == CardinalRiteStationMatcher.Status.MATCHED
				&& resolution.matches().getFirst().recipe().getId().equals(loom.getId());
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "structure_spawner")
	public static void aCraftSpawnsOnlyItsStructureUnlessShiftClicked(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer player = player(helper, at);
		CardinalRiteRecipe loom = CardinalRiteRecipe.getRiteByLocation(level,
				Hemomancy.rloc("cardinal_rite/working/somatic_loom"));
		clear(level, at);
		int placed = PlaceStructurePacket.spawn(player, loom.getId(), PlaceStructurePacket.StructureType.CARDINAL_RITE, false);
		int frame = 0;
		for (String[] aisle : loom.getSubject().getPatternArray())
			for (String row : aisle) frame += row.replace(" ", "").length();
		helper.assertTrue(placed == frame, "A plain click must spawn just the Loom's " + frame + "-block frame, placed " + placed);
		helper.assertTrue(count(level, at, BlockInit.cardinal_focus.get()) == 0
				&& count(level, at, BlockInit.iron_brazier.get()) == 0, "A plain click must not raise a Working floor");

		clear(level, at);
		PlaceStructurePacket.spawn(player, loom.getId(), PlaceStructurePacket.StructureType.CARDINAL_RITE, true);
		var resolution = CardinalRiteStationMatcher.resolve(level, at, CardinalRiteRecipe.getAllRecipes(level));
		helper.assertTrue(resolution.status() == CardinalRiteStationMatcher.Status.MATCHED
				&& resolution.matches().getFirst().recipe().getId().equals(loom.getId()),
				"A shift-click must raise the Loom ready to craft: floor, medium and lit offerings, got " + resolution.status());
		player.discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 200, batch = "structure_spawner")
	public static void shiftChangesNothingForAFloorlessStructure(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = helper.absolutePos(new BlockPos(16, 3, 12));
		ServerPlayer player = player(helper, at);
		clear(level, at);
		helper.assertTrue(PlaceStructurePacket.spawn(player, Hemomancy.rloc("blood_structure/living_staff"),
				PlaceStructurePacket.StructureType.BLOOD_STRUCTURE, true) > 0, "The Living Staff structure must spawn");
		helper.assertTrue(count(level, at, BlockInit.cardinal_focus.get()) == 0
				&& count(level, at, BlockInit.iron_brazier.get()) == 0, "A floorless structure needs no floor or braziers");
		player.discard();
		helper.succeed();
	}

	@GameTest(template = TEMPLATE, timeoutTicks = 40, batch = "structure_spawner")
	public static void aShiftClickedSpawnKeepsItsFullSetupOverTheWire(GameTestHelper helper) {
		var loom = Hemomancy.rloc("cardinal_rite/working/somatic_loom");
		for (boolean fullSetup : new boolean[] {true, false}) {
			var buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
			PlaceStructurePacket.STREAM_CODEC.encode(buf,
					new PlaceStructurePacket(loom, PlaceStructurePacket.StructureType.CARDINAL_RITE, fullSetup));
			PlaceStructurePacket decoded = PlaceStructurePacket.STREAM_CODEC.decode(buf);
			helper.assertTrue(decoded.recipeId().equals(loom)
					&& decoded.structureType() == PlaceStructurePacket.StructureType.CARDINAL_RITE
					&& decoded.fullSetup() == fullSetup && buf.readableBytes() == 0,
					"The spawn packet must carry its shift-click flag, fullSetup=" + fullSetup);
		}
		helper.succeed();
	}

	private static int count(ServerLevel level, BlockPos at, Block block) {
		int found = 0;
		for (BlockPos pos : BlockPos.betweenClosed(at.offset(-8, -2, -8), at.offset(8, 4, 8))) {
			if (level.getBlockState(pos).is(block)) found++;
		}
		return found;
	}

	private static ServerPlayer player(GameTestHelper helper, BlockPos at) {
		var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "structure-spawner"), false);
		var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(),
				cookie.clientInformation());
		var connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		player.setPos(Vec3.atBottomCenterOf(at));
		return player;
	}

	private static void clear(ServerLevel level, BlockPos focus) {
		for (BlockPos pos : BlockPos.betweenClosed(focus.offset(-8, -2, -8), focus.offset(8, 8, 8))) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		}
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(focus).inflate(9.0D))) {
			item.discard();
		}
	}
}
