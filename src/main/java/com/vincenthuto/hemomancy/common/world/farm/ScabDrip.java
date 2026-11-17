package com.vincenthuto.hemomancy.common.world.farm;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.BloodScabPointedDripstoneBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.BloodGutterBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.VenousBasinBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.SanguineCondenserBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.FungalStrains;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.PlantingBasinBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.BloodDrawRights;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.VenousBasinBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.ground.VeinNodeBlockEntity;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Tier 1 of fed ground (core-loop Track 7): a hanging scab spike drips when blood stands fresh just above its root, in
 * the ceiling block it hangs from or in what sits on that block (a basin, a machine's tank, a vein node's tank, a wet
 * worked gutter). Each drip takes a little from that source and falls to the first thing below: soil (and the ring
 * round it) is fed for a while; an empty cauldron becomes a Sanguine Condenser and a Condenser takes the drop as clot,
 * to set into Formations. A Condenser a drip seats belongs to whoever owns the tank the drop fell from (nobody, for a
 * tank nobody owns), and like any Condenser goes back to a cauldron once it has given up everything it held. A fed
 * spike now and then grows a block longer, and a drop on bare scab or stone now and then raises a spike from the floor
 * (round two, P7), which grows toward the tip but never meets it. All of it is the spike's natural tick, not a player's
 * edit. A spike drips only from a tank its placer may draw (CQ-3); one nobody placed drips only from tanks nobody owns.
 */
public final class ScabDrip {
	private static final ParticleColor DROP = new ParticleColor(150, 10, 18);
	/** How long a drop's visual lasts: its fall and its spreading where it lands. */
	private static final int DRIP_VISUAL_TICKS = 26;

	private ScabDrip() {
	}

	/** What landed where. */
	public enum Landing {
		NONE, SOIL, CONDENSER, ELSEWHERE,
		/** On a scab spike standing up from the floor, or on bare floor that raised one: it feeds nothing. */
		FLOOR_SPIKE
	}

	/** Bare floor a drip can raise a scab spike from (core-loop round two, P7). */
	public static final TagKey<Block> SCAB_FLOOR = TagKey.create(Registries.BLOCK, Hemomancy.rloc("scab_floor"));

	/** One drip from the hanging tip at {@code tip}, if blood feeds it. Returns where it landed. */
	public static Landing drip(ServerLevel level, BlockPos tip, BlockState state, RandomSource random) {
		if (!isHangingTip(state)) return Landing.NONE;
		BlockPos root = rootOf(level, tip, state.getBlock());
		Source source = sourceAbove(level, root, placer(level, root));
		if (source == null || source.take(BloodFedGrowthRules.DRIP_ML) <= 0.0D) return Landing.NONE;
		BlockPos below = tip.below();
		Landing landing = Landing.ELSEWHERE;
		BlockPos landed = null;
		for (int i = 0; i < BloodFedGrowthRules.DRIP_REACH; i++, below = below.below()) {
			BlockState there = level.getBlockState(below);
			if (there.isAir()) continue;
			landed = below;
			landing = land(level, tip, below, there, source.owner(), random);
			break;
		}
		show(level, tip, landed);
		if (random.nextFloat() < BloodFedGrowthRules.DRIP_GROW_CHANCE) grow(level, tip, state, root);
		return landing;
	}

	/** The spike's lowest block, pointing down. */
	public static boolean isHangingTip(BlockState state) {
		return state.getBlock() instanceof PointedDripstoneBlock
				&& state.getValue(PointedDripstoneBlock.TIP_DIRECTION) == Direction.DOWN
				&& state.getValue(PointedDripstoneBlock.THICKNESS) == DripstoneThickness.TIP;
	}

	/**
	 * Records {@code placer} as the one who placed the hanging spike at {@code pos}, under its root (CQ-3, NQ-01-15). A
	 * spike set on a ceiling is new and takes the record; a block added below a spike keeps the spike's first placer.
	 */
	public static void recordPlacer(ServerLevel level, BlockPos pos, UUID placer) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof BloodScabPointedDripstoneBlock)
				|| state.getValue(PointedDripstoneBlock.TIP_DIRECTION) != Direction.DOWN) return;
		BlockPos root = rootOf(level, pos, state.getBlock());
		LevelChunk chunk = level.getChunkAt(root);
		ScabSpikePlacers placers = HemoCapabilityAccess.getScabSpikePlacers(chunk);
		if (!root.equals(pos) && placers.placer(root) != null) return;
		placers.record(root, placer);
		chunk.setUnsaved(true);
	}

	/** Forgets the placer of the spike rooted at {@code pos}, when its root block goes. */
	public static void forgetPlacer(ServerLevel level, BlockPos pos) {
		LevelChunk chunk = level.getChunkAt(pos);
		ScabSpikePlacers placers = HemoCapabilityAccess.findScabSpikePlacers(chunk);
		if (placers != null && placers.forget(pos)) chunk.setUnsaved(true);
	}

	/** Who placed the spike rooted at {@code root}, or null when nobody did. */
	@Nullable
	public static UUID placer(ServerLevel level, BlockPos root) {
		ScabSpikePlacers placers = HemoCapabilityAccess.findScabSpikePlacers(level.getChunkAt(root));
		return placers == null ? null : placers.placer(root);
	}

	private static BlockPos rootOf(ServerLevel level, BlockPos tip, Block spike) {
		BlockPos root = tip;
		for (int i = 0; i < 16; i++) {
			BlockState above = level.getBlockState(root.above());
			if (!above.is(spike) || above.getValue(PointedDripstoneBlock.TIP_DIRECTION) != Direction.DOWN) break;
			root = root.above();
		}
		return root;
	}

	private static Landing land(ServerLevel level, BlockPos tip, BlockPos pos, BlockState there, @Nullable UUID owner, RandomSource random) {
		if (isFloorSpike(there)) {
			raiseFloorSpike(level, tip, pos, random);
			return Landing.FLOOR_SPIKE;
		}
		if (there.is(Blocks.CAULDRON)) {
			SanguineCondenserBlockEntity condenser = SanguineCondenserBlock.convertCauldronByDrip(level, pos, owner);
			if (condenser != null) collect(condenser);
			return Landing.CONDENSER;
		}
		if (level.getBlockEntity(pos) instanceof SanguineCondenserBlockEntity condenser) {
			collect(condenser);
			return Landing.CONDENSER;
		}
		BlockPos soil = isSoil(there) ? pos : isSoil(level.getBlockState(pos.below())) ? pos.below() : null;
		if (soil == null) {
			if (there.is(SCAB_FLOOR) && there.isFaceSturdy(level, pos, Direction.UP) && raiseFloorSpike(level, tip, pos, random)) {
				return Landing.FLOOR_SPIKE;
			}
			return Landing.ELSEWHERE;
		}
		BloodFedGround.splash(level, soil, BloodFedGrowthRules.DRIP_SPLASH_RADIUS, BloodFedGrowthRules.DRIP_FEED_TICKS);
		creditFirstDrop(level, owner);
		return Landing.SOIL;
	}

	/** The first drop to feed soil from a tank is its owner's first, when they are here with their blood awake. */
	private static void creditFirstDrop(ServerLevel level, @Nullable UUID owner) {
		if (owner == null || !(level.getServer().getPlayerList().getPlayer(owner) instanceof ServerPlayer player)) return;
		if (player.level() != level || !player.isAlive() || !HemoCapabilityAccess.getBloodVolume(player).isActive()) return;
		HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_FIRST_DRIP);
	}

	/** The cauldron takes the drop and lets it set: clot for the Condenser to make Formations of. */
	private static void collect(SanguineCondenserBlockEntity condenser) {
		condenser.receiveDrip(BloodFedGrowthRules.DRIP_ML);
	}

	/** Ground a crop grows in, and the substrates the fungal strains stand on. */
	public static boolean isSoil(BlockState state) {
		return state.getBlock() instanceof FarmBlock || state.getBlock() instanceof PlantingBasinBlock || state.is(BlockTags.DIRT)
				|| state.is(FungalStrains.ANY_SUBSTRATE);
	}

	/** A scab spike pointing up: a stalagmite a drip has raised, or one placed. */
	public static boolean isFloorSpike(BlockState state) {
		return state.getBlock() instanceof BloodScabPointedDripstoneBlock && state.getValue(PointedDripstoneBlock.TIP_DIRECTION) == Direction.UP;
	}

	/**
	 * Under a drip, bare floor now and then raises a scab spike, and a floor spike grows a block taller, one in
	 * {@link BloodFedGrowthRules#FLOOR_GROW_CHANCE} drops, up to {@link BloodFedGrowthRules#MAX_FLOOR_SPIKE}. It always
	 * stops at least {@link BloodFedGrowthRules#FLOOR_SPIKE_GAP} empty blocks short of the hanging tip, so the two never
	 * meet. {@code landed} is the floor block or any block of the floor spike. Returns whether it raised a block.
	 */
	static boolean raiseFloorSpike(ServerLevel level, BlockPos tip, BlockPos landed, RandomSource random) {
		BlockPos base = landed;
		if (isFloorSpike(level.getBlockState(landed))) {
			while (isFloorSpike(level.getBlockState(base.below()))) base = base.below();
			base = base.below();
		}
		BlockPos top = base;
		while (isFloorSpike(level.getBlockState(top.above()))) top = top.above();
		int length = top.getY() - base.getY();
		BlockPos next = top.above();
		if (!BloodFedGrowthRules.floorSpikeMayRise(length, tip.getY() - next.getY() - 1, random.nextFloat())) return false;
		if (!level.getBlockState(next).isAir()) return false;
		level.setBlock(next, BlockInit.pointed_blood_scorched_scab.get().defaultBlockState()
				.setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.UP)
				.setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP), Block.UPDATE_ALL);
		return true;
	}

	/**
	 * A fed spike grows one block longer, up to {@link BloodFedGrowthRules#MAX_SPIKE}, while there is room below and it
	 * keeps {@link BloodFedGrowthRules#FLOOR_SPIKE_GAP} empty blocks above any floor spike.
	 */
	private static void grow(ServerLevel level, BlockPos tip, BlockState state, BlockPos root) {
		int length = root.getY() - tip.getY() + 1;
		BlockPos next = tip.below();
		if (length >= BloodFedGrowthRules.MAX_SPIKE || !level.getBlockState(next).isAir()
				|| !level.getBlockState(next.below()).isAir()) return;
		// Nor closer to a floor spike than the floor spike may come to it.
		if (isFloorSpike(level.getBlockState(next.below(BloodFedGrowthRules.FLOOR_SPIKE_GAP)))) return;
		level.setBlock(next, state.getBlock().defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
				.setValue(PointedDripstoneBlock.THICKNESS, DripstoneThickness.TIP), Block.UPDATE_ALL);
	}

	/** A drop swelling at the tip and falling to where it lands, spreading there (the toolkit's {@code SCAB_DRIP}). */
	private static void show(ServerLevel level, BlockPos tip, @Nullable BlockPos landed) {
		Vec3 from = new Vec3(tip.getX() + 0.5D, tip.getY() + 0.05D, tip.getZ() + 0.5D);
		double floor = landed == null ? tip.getY() - BloodFedGrowthRules.DRIP_REACH : landed.getY() + surfaceHeight(level, landed);
		Vec3 to = new Vec3(from.x, floor, from.z);
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.SCAB_DRIP, from, to, 0.3D, DRIP_VISUAL_TICKS);
		if (landed == null) return;
		level.sendParticles(HemoParticleData.bloodCell(DROP), to.x, to.y + 0.05D, to.z, 4, 0.14D, 0.02D, 0.14D, 0.02D);
		level.playSound(null, landed, SoundEvents.POINTED_DRIPSTONE_DRIP_LAVA, SoundSource.BLOCKS, 0.5F, 0.6F);
	}

	/** Where a drop meets the block it lands on: the top of its shape, or its full height when it has none. */
	private static double surfaceHeight(ServerLevel level, BlockPos pos) {
		var shape = level.getBlockState(pos).getCollisionShape(level, pos);
		return shape.isEmpty() ? 0.1D : shape.max(net.minecraft.core.Direction.Axis.Y);
	}

	// ── Sources ──

	/** Fresh blood a spike can take from, and who owns the tank it stands in (null for nobody). */
	private interface Source {
		double take(double amount);

		@Nullable
		UUID owner();
	}

	/**
	 * What feeds a spike hanging from {@code root}: the ceiling block it hangs from, or what stands on that block, of
	 * the tanks {@code placer} may draw.
	 */
	@Nullable
	private static Source sourceAbove(ServerLevel level, BlockPos root, @Nullable UUID placer) {
		BlockPos ceiling = root.above();
		Source source = sourceAt(level, ceiling, placer);
		return source != null ? source : sourceAt(level, ceiling.above(), placer);
	}

	/** Whether fresh blood its placer may draw stands just above the spike whose root is {@code root}. */
	public static boolean fed(ServerLevel level, BlockPos root) {
		return sourceAbove(level, root, placer(level, root)) != null;
	}

	@Nullable
	private static Source sourceAt(ServerLevel level, BlockPos pos, @Nullable UUID placer) {
		if (!level.isLoaded(pos)) return null;
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof VeinNodeBlockEntity) blockEntity = GroundNetwork.reservoirBeside(level, pos);
		if (blockEntity instanceof IBloodReservoir reservoir && reservoir.holdsBlood() && reservoir.canProvideBlood()) {
			return fromTank(level, blockEntity, placer);
		}
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof BloodGutterBlock && state.getValue(BloodGutterBlock.WORKED)
				&& state.getValue(BloodGutterBlock.FILL) > 0) {
			VenousBasinBlockEntity basin = VenousBasinBlock.feedingBasin(level, pos);
			if (basin != null) return fromTank(level, basin, placer);
		}
		return null;
	}

	@Nullable
	private static Source fromTank(ServerLevel level, BlockEntity tank, @Nullable UUID placer) {
		if (!BloodDrawRights.mayDraw(level, placer, tank)) return null;
		BlockBloodVolume volume = ReservoirBlood.settled(tank);
		if (volume.getBloodVolume() < BloodFedGrowthRules.DRIP_ML) return null;
		return new Source() {
			@Override
			public double take(double amount) {
				double taken = Math.min(amount, volume.getBloodVolume());
				if (taken <= 0.0D || !volume.drain(taken)) return 0.0D;
				BlockEntitySync.markAndSync(ReservoirBlood.tank(tank));
				return taken;
			}

			@Override
			public UUID owner() {
				return volume.owner();
			}
		};
	}
}
