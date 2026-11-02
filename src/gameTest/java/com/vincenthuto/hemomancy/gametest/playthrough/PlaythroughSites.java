package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.ScarStationBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MasonsEffigyBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MortalDisplayBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MortalDisplayBlockEntity;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyFixtures;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.QliphothBloomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import com.vincenthuto.hemomancy.gametest.MausoleumAssembly;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The places a player plays in, laid out the way their structures lay them out. Every position is absolute. Blocks a
 * site places are removed, and its NPCs discarded, when the test ends.
 */
public final class PlaythroughSites {
	private static final TicketType<ChunkPos> TICKET = TicketType.create("hemomancy_playthrough",
			Comparator.comparingLong(ChunkPos::toLong));

	/**
	 * The relative y a player or NPC stands at in the suite's {@code empty} template (32 x 12 x 24): its stone floor is
	 * the layer at relative y 4, with air below it and seven blocks of headroom above.
	 */
	public static final int STAND_Y = 5;

	private PlaythroughSites() {
	}

	/** The absolute standing spot at relative ({@code x}, {@link #STAND_Y}, {@code z}) in the test's template. */
	public static BlockPos stand(GameTestHelper helper, int x, int z) {
		return helper.absolutePos(new BlockPos(x, STAND_Y, z));
	}

	// --- The Blood Temple ------------------------------------------------------------------------------------------

	/**
	 * A Blood Temple's working parts: the Mortal Display at {@code origin}, the Cardinal Focus three blocks south, the
	 * Hermit two blocks east, and the three links {@code BloodTempleStructure#spawnHermit} makes (the Display to the
	 * Hermit, the Focus to the Display and to the Hermit). The Display is never linked to a Vicar. {@code stand} is
	 * two blocks west of the Display, within reach of both.
	 */
	public record BloodTemple(BlockPos display, BlockPos focus, HarbingerHermitEntity hermit, BlockPos stand) {
		public MortalDisplayBlockEntity displayEntity(ServerLevel level) {
			return (MortalDisplayBlockEntity) level.getBlockEntity(display);
		}
	}

	public static BloodTemple bloodTemple(GameTestHelper helper, BlockPos origin) {
		ServerLevel level = helper.getLevel();
		BlockPos display = origin.immutable();
		BlockPos focus = origin.south(3);
		set(helper, display, BlockInit.mortal_display.get().defaultBlockState().setValue(MortalDisplayBlock.FACING,
				Direction.WEST));
		set(helper, focus, BlockInit.cardinal_focus.get().defaultBlockState());
		HarbingerHermitEntity hermit = PlaythroughNpcs.hermit(helper, origin.east(2));
		helper.assertTrue(level.getBlockEntity(display) instanceof MortalDisplayBlockEntity, "the Display has its block entity");
		((MortalDisplayBlockEntity) level.getBlockEntity(display)).linkHermit(hermit.getUUID());
		if (level.getBlockEntity(focus) instanceof CardinalFocusBlockEntity focusEntity) {
			focusEntity.linkTempleDisplay(display);
			focusEntity.linkTempleHermit(hermit.getUUID());
		} else {
			helper.fail("the Cardinal Focus has its block entity");
		}
		return new BloodTemple(display, focus, hermit, origin.west(2));
	}

	// --- An outpost wing -------------------------------------------------------------------------------------------

	/**
	 * Outpost residents standing round {@code playerSpot}: each within 8 blocks of it (inside dialogue and trade
	 * reach), in the order given.
	 */
	public record OutpostWing(BlockPos playerSpot, Map<PlaythroughNpcs.Role, Entity> residents) {
		public Entity get(PlaythroughNpcs.Role role) {
			Entity entity = residents.get(role);
			if (entity == null) throw new IllegalStateException(role + " was not placed in this wing");
			return entity;
		}

		/** A spot 9 blocks from {@code role}'s resident where it stands now, on the far side from the player spot. */
		public BlockPos farSpot(PlaythroughNpcs.Role role) {
			return farFrom(get(role), playerSpot);
		}
	}

	private static final int[][] WING_OFFSETS = {{3, 0}, {-3, 0}, {0, 3}, {0, -3}, {3, 3}, {-3, -3}, {3, -3}, {-3, 3}};

	public static OutpostWing outpostWing(GameTestHelper helper, BlockPos playerSpot, PlaythroughNpcs.Role... roles) {
		helper.assertTrue(roles.length <= WING_OFFSETS.length, "a wing holds at most " + WING_OFFSETS.length + " residents");
		Map<PlaythroughNpcs.Role, Entity> residents = new EnumMap<>(PlaythroughNpcs.Role.class);
		for (int i = 0; i < roles.length; i++) {
			BlockPos at = playerSpot.offset(WING_OFFSETS[i][0], 0, WING_OFFSETS[i][1]);
			residents.put(roles[i], PlaythroughNpcs.spawn(helper, roles[i], at));
		}
		return new OutpostWing(playerSpot.immutable(), residents);
	}

	/** A spot exactly 9 blocks (horizontally) from {@code npc}, on the far side from {@code from}. */
	public static BlockPos farFrom(Entity npc, BlockPos from) {
		Vec3 centre = npc.position();
		Vec3 away = centre.subtract(Vec3.atBottomCenterOf(from)).multiply(1, 0, 1);
		Vec3 direction = away.lengthSqr() < 1.0E-4D ? new Vec3(1, 0, 0) : away.normalize();
		Vec3 far = centre.add(direction.scale(9.0D));
		return BlockPos.containing(far.x, centre.y, far.z);
	}

	// --- Mason's Respite -------------------------------------------------------------------------------------------

	/**
	 * The Mason's Respite teaching yard at the offsets {@code tools/worldgen/masons_respite.py} authors, measured from
	 * the template centre (7, 1, 7), which is {@code centre} here and where the Anchorite stands: the scarring station
	 * at (+5, 0, +2) facing west with its filler cell above, the Mason's Effigy at (+5, 0, +4) facing west, and the
	 * Iron Brazier on the floor at (0, 0, +3) facing south, unlit.
	 */
	public record MasonsRespite(BlockPos station, BlockPos effigy, BlockPos brazier,
			HarbingerCicatrixAnchoriteEntity anchorite, BlockPos centre) {
	}

	public static MasonsRespite masonsRespite(GameTestHelper helper, BlockPos centre) {
		ServerLevel level = helper.getLevel();
		BlockPos station = centre.offset(5, 0, 2);
		BlockPos effigy = centre.offset(5, 0, 4);
		BlockPos brazier = centre.offset(0, 0, 3);
		set(helper, station, BlockInit.scar_station.get().defaultBlockState().setValue(ScarStationBlock.FACING, Direction.WEST));
		set(helper, station.above(), BlockInit.filler_block.get().defaultBlockState());
		if (level.getBlockEntity(station.above()) instanceof FillerBlockEntity filler) filler.setMainBlockPos(station);
		set(helper, effigy, BlockInit.mason_effigy.get().defaultBlockState().setValue(MasonsEffigyBlock.FACING, Direction.WEST));
		set(helper, brazier, BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.FACE, AttachFace.FLOOR)
				.setValue(BrazierBlock.FACING, Direction.SOUTH).setValue(BrazierBlock.RITUAL_PHASE, 0));
		HarbingerCicatrixAnchoriteEntity anchorite = PlaythroughNpcs.anchorite(helper, centre);
		return new MasonsRespite(station, effigy, brazier, anchorite, centre.immutable());
	}

	// --- Rite floors -----------------------------------------------------------------------------------------------

	/**
	 * A rite's ground, ARRANGE only. {@code focus} is where the Staff is planted; {@code brazierSockets} are where a
	 * layered floor wants its braziers (empty for an unlayered rite). Nothing about the rite's medium, offerings,
	 * pillars or Staff is placed here: the player does those.
	 */
	public record RiteFloor(CardinalRiteRecipe recipe, BlockPos origin, BlockPos focus, List<BlockPos> brazierSockets) {
	}

	/**
	 * ARRANGE: lays the floor and focus of {@code cardinal_rite/<recipePath>} at {@code origin} (from
	 * {@code HemoJourneyFixtures.findClearOrigin}, normally) and keeps its chunk ticking. A layered rank-up floor is laid
	 * alone; an unlayered rite's pattern comes from {@code HemoJourneyFixtures.prepareCardinalRite} (which also puts a
	 * Living Staff in the player's main hand). The ground is cleared with {@code HemoJourneyFixtures.cleanup} when the
	 * test ends.
	 */
	public static RiteFloor riteFloor(PlaythroughPlayer player, BlockPos origin, String recipePath) {
		GameTestHelper helper = player.helper();
		ServerLevel level = player.player().serverLevel();
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, Hemomancy.rloc("cardinal_rite/" + recipePath));
		helper.assertTrue(recipe != null, "cardinal_rite/" + recipePath + " is loaded");
		player.player().getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, level.dimension().location().toString());
		keepTicking(helper, level, origin);
		BlockPos focus = origin.above();
		List<BlockPos> sockets = new ArrayList<>();
		if (recipe.hasLayeredStation()) {
			CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElse(null);
			helper.assertTrue(floor != null, "the rite floor " + recipe.getFloorId() + " is loaded");
			List<BlockPos> laid = placePattern(level, floor.pattern(), focus, floor.focus().getX(), floor.focus().getY(),
					floor.focus().getZ());
			PlaythroughCleanup.onEnd(helper, () -> {
				for (BlockPos pos : laid) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			});
			for (BlockPos socket : floor.brazierSockets()) sockets.add(focus.offset(socket.getX(), socket.getY(), -socket.getZ()));
		} else {
			HemoJourneyFixtures.prepareCardinalRite(player.player(), origin, recipePath);
			PlaythroughCleanup.onEnd(helper, () -> HemoJourneyFixtures.cleanup(player.player(), origin));
		}
		return new RiteFloor(recipe, origin.immutable(), focus, List.copyOf(sockets));
	}

	// --- Synthetic structure sites ---------------------------------------------------------------------------------

	/**
	 * Registers {@code structure} as a generated start over {@code box}: one empty pool piece covering the box,
	 * written into its origin chunk's starts and every covered chunk's references the way generation would, so
	 * {@code structureManager().getStructureWithPieceAt} finds it. Nothing is placed. The returned action puts each
	 * chunk's own structure data back.
	 */
	public static Runnable installStructure(ServerLevel level, Structure structure, BoundingBox box) {
		var piece = new PoolElementStructurePiece(level.getStructureManager(), EmptyPoolElement.INSTANCE,
				new BlockPos(box.minX(), box.minY(), box.minZ()), 0, Rotation.NONE, box, LiquidSettings.IGNORE_WATERLOGGING);
		ChunkPos origin = new ChunkPos(SectionPos.blockToSectionCoord(box.minX()), SectionPos.blockToSectionCoord(box.minZ()));
		List<Runnable> restore = new ArrayList<>();
		var originChunk = level.getChunk(origin.x, origin.z);
		Map<Structure, StructureStart> starts = new HashMap<>(originChunk.getAllStarts());
		restore.add(() -> originChunk.setAllStarts(starts));
		originChunk.setStartForStructure(structure, new StructureStart(structure, origin, 0, new PiecesContainer(List.of(piece))));
		ChunkPos.rangeClosed(origin, new ChunkPos(SectionPos.blockToSectionCoord(box.maxX()),
				SectionPos.blockToSectionCoord(box.maxZ()))).forEach(chunk -> {
			var access = level.getChunk(chunk.x, chunk.z);
			var references = new HashMap<>(access.getAllReferences());
			restore.add(() -> access.setAllReferences(references));
			access.addReferenceForStructure(structure, origin.toLong());
		});
		boolean[] done = new boolean[1];
		return () -> {
			if (done[0]) return;
			done[0] = true;
			restore.forEach(Runnable::run);
		};
	}

	/**
	 * ARRANGE: {@link #installStructure} for the registered structure {@code structureKey} over {@code box}, put back
	 * when the test ends. Returns the restore action, should the test want the site gone sooner.
	 */
	public static Runnable structureSite(GameTestHelper helper, ResourceKey<Structure> structureKey, BoundingBox box) {
		ServerLevel level = helper.getLevel();
		Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(structureKey);
		helper.assertTrue(structure != null, "the structure " + structureKey.location() + " is registered");
		Runnable restore = installStructure(level, structure, box);
		PlaythroughCleanup.onEnd(helper, restore);
		return restore;
	}

	/** {@link #structureSite} for {@code hemomancy:<path>}. */
	public static Runnable structureSite(GameTestHelper helper, String path, BoundingBox box) {
		return structureSite(helper, ResourceKey.create(Registries.STRUCTURE, Hemomancy.rloc(path)), box);
	}

	/** The test's own bounds as a block box (for a test whose template is the site itself). */
	public static BoundingBox bounds(GameTestHelper helper) {
		var aabb = helper.getBounds();
		return new BoundingBox((int) Math.floor(aabb.minX), (int) Math.floor(aabb.minY), (int) Math.floor(aabb.minZ),
				(int) Math.floor(aabb.maxX) - 1, (int) Math.floor(aabb.maxY) - 1, (int) Math.floor(aabb.maxZ) - 1);
	}

	/**
	 * ARRANGE: a Brined Court site registered over {@code box}, so being inside it is being at that house's Seat or
	 * outpost ({@code CourtLife.houseAt}, Court discovery, the visit act). Its residents are placed with
	 * {@link PlaythroughNpcs#court}.
	 */
	public static Runnable courtSite(GameTestHelper helper, CourtSite site, BoundingBox box) {
		return structureSite(helper, site.id(), box);
	}

	/** A Court site of {@code site} round {@code centre}: 25 x 13 x 25, from 4 below to 8 above it. */
	public static Runnable courtSite(GameTestHelper helper, CourtSite site, BlockPos centre) {
		return courtSite(helper, site, BoundingBox.fromCorners(centre.offset(-12, -4, -12), centre.offset(12, 8, 12)));
	}

	/** A mod structure template placed by {@link #template}: where it stands and the box it fills. */
	public record TemplateSite(ResourceLocation id, BlockPos origin, BoundingBox box) {
		public BlockPos centre() {
			return box.getCenter();
		}

		/** The absolute position of the template-relative ({@code x}, {@code y}, {@code z}). */
		public BlockPos at(int x, int y, int z) {
			return origin.offset(x, y, z);
		}

		/**
		 * Whether every chunk under the box is entity-ticking. Entities the template placed (and NPCs placed in its box
		 * right after) are not visible to {@code getEntitiesOfClass} until then, so wait on this before looking up the
		 * site's actors: {@code clock.until(200, () -> site.ready(level), ...)}.
		 */
		public boolean ready(net.minecraft.server.level.ServerLevel level) {
			for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++) {
				for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) {
					if (!level.isPositionEntityTicking(new BlockPos(x << 4, box.minY(), z << 4))) return false;
				}
			}
			return true;
		}
	}

	/**
	 * ARRANGE: places the mod's structure template {@code hemomancy:<path>} (blocks and the actors saved in it) with
	 * its corner at {@code origin}, its chunks kept loaded and ticking. Everything it placed (blocks and entities in
	 * its box) is cleared when the test ends. Tests here must use the suite's own {@code empty} template (the launcher
	 * enables only the {@code playthrough_validation} template namespace), so a real site is laid this way.
	 */
	public static TemplateSite template(GameTestHelper helper, String path, BlockPos origin) {
		ServerLevel level = helper.getLevel();
		ResourceLocation id = Hemomancy.rloc(path);
		var template = level.getStructureManager().get(id);
		helper.assertTrue(template.isPresent(), "the template " + id + " is shipped");
		var size = template.get().getSize();
		BoundingBox box = BoundingBox.fromCorners(origin, origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
		ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(box.minX()), SectionPos.blockToSectionCoord(box.minZ()));
		ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(box.maxX()), SectionPos.blockToSectionCoord(box.maxZ()));
		ChunkPos.rangeClosed(min, max).forEach(chunk -> {
			level.getChunk(chunk.x, chunk.z);
			level.getChunkSource().addRegionTicket(TICKET, chunk, 2, chunk);
		});
		template.get().placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
		PlaythroughCleanup.onEnd(helper, () -> {
			for (Entity entity : level.getEntities((Entity) null, AABB.of(box).inflate(2.0D),
					entity -> !(entity instanceof net.minecraft.world.entity.player.Player))) entity.discard();
			for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
				if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
			ChunkPos.rangeClosed(min, max).forEach(chunk -> level.getChunkSource().removeRegionTicket(TICKET, chunk, 2, chunk));
		});
		return new TemplateSite(id, origin.immutable(), box);
	}

	/**
	 * Where a test lays a site too big for its own template: {@code blocksEast} east of the test's corner, on its
	 * floor layer. Tests in one batch sit side by side, so a site that is not alone in its batch picks a distinct
	 * distance.
	 */
	public static BlockPos farOrigin(GameTestHelper helper, int blocksEast) {
		return helper.absolutePos(new BlockPos(0, STAND_Y - 1, 0)).east(blocksEast);
	}

	/**
	 * ARRANGE: the real Crimson Troupe pavilion ({@code crimson_troupe_pavilion}, with its troupe) placed at
	 * {@code origin} ({@link #farOrigin}, in an own batch) and registered as a {@code circus_pavilion} structure over
	 * its box, so it is a pavilion for discovery and acclimation. Pavilion sites saved there are removed at the end.
	 */
	public static TemplateSite pavilion(GameTestHelper helper, BlockPos origin) {
		TemplateSite site = template(helper, "crimson_troupe_pavilion", origin);
		forgetPavilionSites(helper, site.box());
		structureSite(helper, "circus_pavilion", site.box());
		return site;
	}

	/** As {@link #pavilion}, for a {@code troupe_camp_<variant>} (air, blades, embers, memory, pageantry). */
	public static TemplateSite camp(GameTestHelper helper, String variant, BlockPos origin) {
		TemplateSite site = template(helper, "troupe_camp_" + variant, origin);
		forgetPavilionSites(helper, site.box());
		structureSite(helper, "troupe_camp_" + variant, site.box());
		return site;
	}

	/** As {@link #pavilion}, for the {@code troupe_abandoned_stage}. */
	public static TemplateSite abandonedStage(GameTestHelper helper, BlockPos origin) {
		TemplateSite site = template(helper, "troupe_abandoned_stage", origin);
		forgetPavilionSites(helper, site.box());
		structureSite(helper, "troupe_abandoned_stage", site.box());
		return site;
	}

	/**
	 * A Brined Court site from its real template ({@code brined_court/<site>/<piece>}, e.g. TIDE_REFUGE + "refuge",
	 * LIGHTHOUSE_GARDEN + "garden") placed at {@code origin} and registered as {@code site}'s structure over its box.
	 * Its residents are placed with {@link PlaythroughNpcs#court}.
	 */
	public static TemplateSite courtTemplate(GameTestHelper helper, CourtSite site, String piece, BlockPos origin) {
		TemplateSite placed = template(helper, "brined_court/" + site.id() + "/" + piece, origin);
		courtSite(helper, site, placed.box());
		return placed;
	}

	@SuppressWarnings("unchecked")
	private static void forgetPavilionSites(GameTestHelper helper, BoundingBox box) {
		ServerLevel level = helper.getLevel();
		var bounds = AABB.of(box).inflate(8.0D);
		PlaythroughCleanup.onEnd(helper, () -> {
			var data = CircusPavilionSavedData.get(level);
			try {
				var field = CircusPavilionSavedData.class.getDeclaredField("sites");
				field.setAccessible(true);
				var sites = (Map<String, CircusPavilionSavedData.Site>) field.get(data);
				List<BlockPos> inside = new ArrayList<>();
				for (var site : sites.values()) if (bounds.contains(Vec3.atCenterOf(site.origin()))) inside.add(site.origin());
				for (BlockPos origin : inside) data.removeSite(level, origin);
			} catch (ReflectiveOperationException exception) {
				PlaythroughLog.note(helper, "could not clear pavilion sites: " + exception);
			}
		});
	}

	// --- The Fane and the late blocks ------------------------------------------------------------------------------

	/**
	 * ARRANGE: a consecrated Fane for {@code owner}'s line at {@code heart}: the Consecrated Bloodwell set there and
	 * the heart registered ({@code FoundingFaneSavedData.consecrateHeart}) for the line's leader (the owner when they
	 * lead none). Removed when the test ends. Nothing about the Founding rite is played: use {@link #faneFloor} for that.
	 */
	public static BlockPos fane(PlaythroughPlayer owner, BlockPos heart) {
		GameTestHelper helper = owner.helper();
		ServerLevel level = owner.player().serverLevel();
		var line = HemoCapabilityAccess.getBloodVolume(owner.player()).getBloodLine();
		java.util.UUID leader = line.isValid() ? line.getLeaderUUID() : owner.uuid();
		// A sweep may consecrate many players' Fanes at one shared heart: the Bloodwell is placed once.
		if (!level.getBlockState(heart).is(BlockInit.consecrated_bloodwell.get())) {
			set(helper, heart, BlockInit.consecrated_bloodwell.get().defaultBlockState());
		}
		FoundingFaneSavedData data = FoundingFaneSavedData.get(level);
		data.consecrateHeart(leader, heart);
		PlaythroughCleanup.onEnd(helper, () -> data.remove(leader));
		keepTicking(helper, level, heart);
		return heart.immutable();
	}

	/**
	 * ARRANGE: the {@code founding_fane} rite's floor at {@code origin} for the played rite, as {@link #riteFloor}
	 * lays it; asserts the heart cell (three above the Focus) is clear for the Bloodwell the rite will make.
	 */
	public static RiteFloor faneFloor(PlaythroughPlayer player, BlockPos origin) {
		RiteFloor floor = riteFloor(player, origin, "founding_fane");
		BlockPos heart = floor.focus().above(3);
		player.helper().assertTrue(player.player().serverLevel().getBlockState(heart).canBeReplaced(),
				"the Fane's heart cell " + heart.toShortString() + " is clear");
		return floor;
	}

	/** Where the Founding Fane rite on {@code floor} consecrates its Bloodwell: three above the Focus. */
	public static BlockPos faneHeart(RiteFloor floor) {
		return floor.focus().above(3);
	}

	/**
	 * ARRANGE: a Sanguine Monolith at {@code pos}, placed by {@code placer} through its BlockItem (which lays its filler
	 * and starts its rise), with the placer credited with crafting one. The item is supplied when the placer has none.
	 * Removed when the test ends.
	 */
	public static BlockPos monolith(PlaythroughPlayer placer, BlockPos pos) {
		return monolith(placer, pos, true);
	}

	/**
	 * {@link #monolith}; with {@code credited} the placer also has the personal-craft credit its Working craft gives
	 * ({@code MachineAccessEvents.awardMachineCrafted}), without which the Monolith answers only "craft one yourself".
	 */
	public static BlockPos monolith(PlaythroughPlayer placer, BlockPos pos, boolean credited) {
		if (credited) com.vincenthuto.hemomancy.common.event.MachineAccessEvents.awardMachineCrafted(placer.player(),
				BlockInit.sanguine_monolith.get());
		placeItem(placer, pos, BlockInit.sanguine_monolith.get());
		placer.helper().assertTrue(placer.player().serverLevel().getBlockEntity(pos) instanceof SanguineMonolithBlockEntity,
				"a Sanguine Monolith stands at " + pos.toShortString());
		return pos.immutable();
	}

	/** ARRANGE: a Covenant Throne at {@code pos}, placed by {@code placer} through its BlockItem. */
	public static BlockPos covenantThrone(PlaythroughPlayer placer, BlockPos pos) {
		return covenantThrone(placer, pos, true);
	}

	/** {@link #covenantThrone}; {@code credited} as for {@link #monolith(PlaythroughPlayer, BlockPos, boolean)}. */
	public static BlockPos covenantThrone(PlaythroughPlayer placer, BlockPos pos, boolean credited) {
		if (credited) com.vincenthuto.hemomancy.common.event.MachineAccessEvents.awardMachineCrafted(placer.player(),
				BlockInit.covenant_throne.get());
		placeItem(placer, pos, BlockInit.covenant_throne.get());
		placer.helper().assertTrue(placer.player().serverLevel().getBlockState(pos).is(BlockInit.covenant_throne.get()),
				"a Covenant Throne stands at " + pos.toShortString());
		return pos.immutable();
	}

	private static void placeItem(PlaythroughPlayer placer, BlockPos pos, Block block) {
		if (placer.count(block) == 0) placer.give(new ItemStack(block)); // ARRANGE: made elsewhere
		Vec3 standing = placer.player().position();
		placer.moveTo(pos.south(2)); // ARRANGE: stand within reach, facing it
		Outcome placed = placer.place(pos, block);
		placer.moveTo(standing);
		placer.helper().assertTrue(placer.player().serverLevel().getBlockState(pos).is(block),
				block + " is placed at " + pos.toShortString() + "; " + placed);
	}

	/**
	 * ARRANGE: a Qliphoth Bloom owned by {@code owner} rooted at {@code root} (its seven fillers above), registered in
	 * {@code QliphothBloomSavedData} with a chunk radius of 3 and synced, as {@code HemoJourneyFixtures} prepares the
	 * Communion. Fails when it would overlap another Bloom (Blooms are level-global: use an own batch). The Bloom, its
	 * entry and its pomes are removed when the test ends.
	 */
	public static QliphothBloomSavedData.BloomEntry qliphothBloom(PlaythroughPlayer owner, BlockPos root) {
		GameTestHelper helper = owner.helper();
		ServerLevel level = owner.player().serverLevel();
		String dimension = level.dimension().location().toString();
		QliphothBloomSavedData blooms = QliphothBloomSavedData.get(level.getServer().overworld());
		helper.assertTrue(blooms.getOverlappingBloom(root, dimension, 3) == null,
				"the Bloom at " + root.toShortString() + " overlaps another Bloom (give its test an own batch)");
		set(helper, root, BlockInit.qliphoth_bloom.get().defaultBlockState());
		helper.assertTrue(level.getBlockEntity(root) instanceof QliphothBloomBlockEntity, "the Bloom has its block entity");
		QliphothBloomBlockEntity entity = (QliphothBloomBlockEntity) level.getBlockEntity(root);
		entity.setOwnerUUID(owner.uuid());
		entity.setChunkRadius(3);
		((IMultiBlock) BlockInit.qliphoth_bloom.get()).placeFillers(level, root, BlockInit.qliphoth_bloom.get().defaultBlockState());
		var bloom = new QliphothBloomSavedData.BloomEntry(owner.uuid(), root.immutable(), dimension, 3, level.getGameTime());
		blooms.addBloom(bloom);
		HarbingerCardinalRiteEvents.syncQliphothBlooms(level.getServer());
		keepTicking(helper, level, root);
		PlaythroughCleanup.onEnd(helper, () -> {
			for (int up = 1; up <= 7; up++) {
				if (level.getBlockState(root.above(up)).is(BlockInit.filler_block.get())) {
					level.setBlock(root.above(up), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				}
			}
			blooms.removeBloomInChunk(root, dimension);
			HarbingerCardinalRiteEvents.syncQliphothBlooms(level.getServer());
		});
		return bloom;
	}

	/** ARRANGE: a floor Iron Brazier at {@code pos} facing south, lit or not. Removed when the test ends. */
	public static BlockPos ironBrazier(GameTestHelper helper, BlockPos pos, boolean lit) {
		set(helper, pos, BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.FACE, AttachFace.FLOOR)
				.setValue(BrazierBlock.FACING, Direction.SOUTH).setValue(BrazierBlock.RITUAL_PHASE, lit ? 1 : 0));
		return pos.immutable();
	}

	/** ARRANGE: a pool of Morphic Nectar source blocks filling {@code box}. Removed when the test ends. */
	public static BoundingBox morphicNectarPool(GameTestHelper helper, BoundingBox box) {
		ServerLevel level = helper.getLevel();
		BlockState nectar = BlockInit.MORPHIC_NECTAR_BLOCK.get().defaultBlockState();
		List<BlockPos> filled = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
			level.setBlock(pos, nectar, Block.UPDATE_CLIENTS);
			filled.add(pos.immutable());
		}
		PlaythroughCleanup.onEnd(helper, () -> {
			for (BlockPos pos : filled) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		});
		return box;
	}

	/** ARRANGE: {@link #biome} with {@code hemomancy:fungal_gardens}. */
	public static BoundingBox gardensBiome(GameTestHelper helper, BoundingBox box) {
		return biome(helper, box, ResourceKey.create(Registries.BIOME, Hemomancy.rloc("fungal_gardens")));
	}

	/**
	 * ARRANGE: every biome cell (4 x 4 x 4) inside {@code box} becomes {@code biome}; each covered chunk's old biomes
	 * are put back when the test ends. Clients are not told (the GameTest server has none).
	 */
	public static BoundingBox biome(GameTestHelper helper, BoundingBox box, ResourceKey<Biome> biome) {
		ServerLevel level = helper.getLevel();
		Holder<Biome> holder = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biome);
		var sampler = level.getChunkSource().randomState().sampler();
		ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(box.minX()), SectionPos.blockToSectionCoord(box.minZ()));
		ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(box.maxX()), SectionPos.blockToSectionCoord(box.maxZ()));
		ChunkPos.rangeClosed(min, max).forEach(chunkPos -> {
			var chunk = level.getChunk(chunkPos.x, chunkPos.z);
			Map<Long, Holder<Biome>> before = new HashMap<>();
			chunk.fillBiomesFromNoise((x, y, z, noise) -> {
				Holder<Biome> old = chunk.getNoiseBiome(x, y, z);
				before.put(BlockPos.asLong(x, y, z), old);
				int bx = QuartPos.toBlock(x), by = QuartPos.toBlock(y), bz = QuartPos.toBlock(z);
				boolean inside = bx + 3 >= box.minX() && bx <= box.maxX() && by + 3 >= box.minY() && by <= box.maxY()
						&& bz + 3 >= box.minZ() && bz <= box.maxZ();
				return inside ? holder : old;
			}, sampler);
			chunk.setUnsaved(true);
			PlaythroughCleanup.onEnd(helper, () -> {
				chunk.fillBiomesFromNoise((x, y, z, noise) -> before.getOrDefault(BlockPos.asLong(x, y, z),
						chunk.getNoiseBiome(x, y, z)), sampler);
				chunk.setUnsaved(true);
			});
		});
		return box;
	}

	/** One Saint's mausoleum complex built by {@link #mausoleum}: its pieces and its whole box. */
	public record Mausoleum(EnumSaintType saint, MausoleumAssembly.Layout layout, BoundingBox box) {
	}

	/**
	 * ARRANGE: {@code saint}'s full mausoleum complex as its jigsaw lays it for {@code seed}, built with
	 * {@link MausoleumAssembly#build} far from every other test (its own batch), registered as a structure start so its
	 * ward, Chapels, ring door and tomb behave as generated. Its chunks stay loaded; the blocks, the start and the
	 * references are cleared when the test ends. Fails when the complex does not lay out.
	 */
	public static Mausoleum mausoleum(GameTestHelper helper, EnumSaintType saint, long seed) {
		ServerLevel level = helper.getLevel();
		BlockPos test = helper.absolutePos(BlockPos.ZERO);
		BlockPos surface = new BlockPos(test.getX() + 4096, 120, test.getZ() + 4096);
		var layout = MausoleumAssembly.layout(level, saint, surface, seed);
		helper.assertTrue(layout.isPresent(), "the " + saint + " mausoleum lays out for seed " + seed);
		StructureStart probe = new StructureStart(layout.get().structure(), layout.get().origin(), 0, layout.get().pieces());
		BoundingBox box = probe.getBoundingBox();
		ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(box.minX()), SectionPos.blockToSectionCoord(box.minZ()));
		ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(box.maxX()), SectionPos.blockToSectionCoord(box.maxZ()));
		List<Runnable> restore = new ArrayList<>();
		ChunkPos.rangeClosed(min, max).forEach(chunk -> {
			level.getChunkSource().addRegionTicket(TICKET, chunk, 2, chunk);
			var access = level.getChunk(chunk.x, chunk.z);
			var starts = new HashMap<>(access.getAllStarts());
			var references = new HashMap<>(access.getAllReferences());
			restore.add(() -> {
				access.setAllStarts(starts);
				access.setAllReferences(references);
				level.getChunkSource().removeRegionTicket(TICKET, chunk, 2, chunk);
			});
		});
		MausoleumAssembly.build(level, layout.get());
		PlaythroughCleanup.onEnd(helper, () -> {
			for (Entity entity : level.getEntities((Entity) null, AABB.of(box).inflate(4.0D),
					entity -> !(entity instanceof net.minecraft.world.entity.player.Player))) entity.discard();
			for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
				if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
			restore.forEach(Runnable::run);
		});
		PlaythroughLog.note(helper, "ARRANGE built the " + saint + " mausoleum over " + box);
		return new Mausoleum(saint, layout.get(), box);
	}

	/**
	 * ARRANGE: {@code station}'s block on the seat a station-upgrade rite reads ({@code StationUpgradeRites.seat}: above
	 * the Focus, {@code seatDistance} blocks behind it on a north-facing floor), placed by {@code player} through its
	 * BlockItem (supplied when absent), or set when it cannot be placed by hand. Removed when the test ends.
	 */
	public static BlockPos stationBehindFocus(PlaythroughPlayer player, RiteFloor floor, UpgradeStation station) {
		BlockPos seat = floor.focus().above().south(station.seatDistance());
		Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(station.blockId());
		ServerLevel level = player.player().serverLevel();
		if (level.getBlockState(seat.below()).isAir()) set(player.helper(), seat.below(), Blocks.STONE.defaultBlockState());
		if (block.asItem() != net.minecraft.world.item.Items.AIR) {
			if (player.count(block) == 0) player.give(new ItemStack(block)); // ARRANGE: the station is built elsewhere
			Vec3 standing = player.player().position();
			player.moveTo(seat.south(2));
			player.place(seat, block);
			player.moveTo(standing);
		}
		if (!level.getBlockState(seat).is(block)) {
			set(player.helper(), seat, block.defaultBlockState()); // ARRANGE fallback
			PlaythroughLog.note(player.helper(), "could not place " + block + " behind the Focus by hand; set it");
		}
		return seat.immutable();
	}

	// --- Between bands ---------------------------------------------------------------------------------------------

	/**
	 * ARRANGE, for a test that plays several bands on one template: clears what the band before left, so the next band
	 * can lay its own sites on the same floor. Every block above the floor layer and below it becomes air, the floor
	 * layer is restored to the template's floor block, pending blood crafts here are dropped, and every entity that is
	 * not a player (speakers, mobs, items left lying) is discarded. The player's own inventory is not touched.
	 */
	public static void clearForNextBand(GameTestHelper helper) {
		clearForNextBand(helper, java.util.Set.of(), java.util.Set.of());
	}

	/**
	 * As {@link #clearForNextBand(GameTestHelper)}, but the blocks at {@code keepBlocks} and the entities whose UUID is
	 * in {@code keepEntities} stay: what a player carries over between bands in the world (their Fane's heart, a
	 * pledged recruit). Used by the late run, where the D5 Fane and its recruit must outlast the floors after them.
	 */
	public static void clearForNextBand(GameTestHelper helper, java.util.Collection<BlockPos> keepBlocks,
			java.util.Collection<java.util.UUID> keepEntities) {
		ServerLevel level = helper.getLevel();
		net.minecraft.world.phys.AABB bounds = helper.getBounds();
		com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager.cancelWithin(level, bounds.inflate(8.0D));
		int floorY = helper.absolutePos(new BlockPos(0, STAND_Y - 1, 0)).getY();
		BlockState floor = level.getBlockState(helper.absolutePos(new BlockPos(0, STAND_Y - 1, 0)));
		if (floor.isAir()) floor = Blocks.STONE.defaultBlockState();
		BlockPos min = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
		BlockPos max = BlockPos.containing(bounds.maxX - 1.0E-4D, bounds.maxY - 1.0E-4D, bounds.maxZ - 1.0E-4D);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			if (keepBlocks.contains(pos)) continue;
			BlockState want = pos.getY() == floorY ? floor : Blocks.AIR.defaultBlockState();
			if (!level.getBlockState(pos).equals(want)) level.setBlock(pos, want, Block.UPDATE_CLIENTS);
		}
		for (Entity entity : level.getEntities((Entity) null, bounds.inflate(4.0D),
				entity -> !(entity instanceof net.minecraft.world.entity.player.Player)
						&& !keepEntities.contains(entity.getUUID()))) {
			entity.discard();
		}
	}

	/** Keeps the chunk round {@code pos} entity-ticking until the test ends, for sites outside the test structure. */
	public static void keepTicking(GameTestHelper helper, ServerLevel level, BlockPos pos) {
		ChunkPos chunk = new ChunkPos(pos);
		level.getChunkSource().addRegionTicket(TICKET, chunk, 2, chunk);
		PlaythroughCleanup.onEnd(helper, () -> level.getChunkSource().removeRegionTicket(TICKET, chunk, 2, chunk));
	}

	private static List<BlockPos> placePattern(ServerLevel level, com.vincenthuto.hutoslib.math.MultiblockPattern pattern,
			BlockPos targetCell, int cellX, int cellY, int cellZ) {
		int physicalCellY = pattern.getBlockPattern().getHeight() - cellY - 1;
		List<BlockPos> placed = new ArrayList<>();
		var pairs = pattern.getBlockPosBlockList();
		pairs.sort(Comparator.comparingInt(pair -> pair.getPos().getY()));
		for (var pair : pairs) {
			Block block = pair.getBlock();
			if (block == null || block == Blocks.AIR) continue;
			BlockPos relative = pair.getPos();
			BlockPos worldPos = targetCell.offset(relative.getX() - cellX, relative.getY() - physicalCellY,
					cellZ - relative.getZ());
			level.setBlock(worldPos, block.defaultBlockState(), Block.UPDATE_CLIENTS);
			placed.add(worldPos);
		}
		for (BlockPos pos : placed) {
			level.blockUpdated(pos, level.getBlockState(pos).getBlock());
			level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
		}
		return placed;
	}

	private static void set(GameTestHelper helper, BlockPos pos, BlockState state) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(level.setBlock(pos, state, Block.UPDATE_ALL), "placed " + state.getBlock() + " at " + pos.toShortString());
		PlaythroughCleanup.onEnd(helper, () -> {
			if (level.getBlockState(pos).is(state.getBlock())) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		});
	}
}
