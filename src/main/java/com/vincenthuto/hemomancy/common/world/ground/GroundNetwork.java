package com.vincenthuto.hemomancy.common.world.ground;

import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodGougeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswer;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.GroundMarkBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.LivingVeinBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.LuxTraceBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.VeinNodeBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingRules;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.event.SanguineProjectionTargeting;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargeting;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.BloodDrawRights;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.harbinger.ground.CatalystFeederBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.ground.VeinNodeBlockEntity;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

/**
 * The ground network (core-loop Track 6): stains, gouges and living veins share one look and one set of rules, kept
 * here. Plain Projection lays a stain where nothing else in the chain claimed the blood ({@link #stain}, the chain's
 * last link); Projection into a gouge or vein sends a pulse along its run, lit for a time and given a function by the
 * catalyst in the off hand; Projection into a vein node grows tissue along its gouges; Absorption takes any tier back up
 * ({@link #tryAbsorb}). Lit cells keep their clock in {@link GroundRunSavedData} and do their work on their scheduled
 * ticks ({@link #tickCell}). Every edit a player causes goes through {@link PlayerBlockEdits}, including a hot run
 * lighting a campfire or candle or burning a furnace beside it ({@link FurnaceHeat#stoke}) and a cold run freezing
 * water, which are edits for the run's owner and happen only while
 * that owner is online, alive and in the run's level; the rest are the marks' own natural changes (ageing, a run going
 * dark, a vein withering).
 *
 * <p>Per-player pacing is three session maps keyed by UUID, cleared through {@code TransientServerStateEvents}.
 */
public final class GroundNetwork {
	public static final BloodAccounting.Source STAIN = BloodAccounting.Source.of("ground_stain", Category.WORLD, false);
	public static final BloodAccounting.Source PULSE = BloodAccounting.Source.of("ground_pulse", Category.WORLD, false);
	public static final BloodAccounting.Source VEIN_GROWTH = BloodAccounting.Source.of("vein_growth", Category.WORLD, false);
	public static final BloodAccounting.Source VEIN_UPKEEP = BloodAccounting.Source.of("vein_upkeep", Category.WORLD, true);
	public static final BloodAccounting.Source FEEDER = BloodAccounting.Source.of("ground_feeder", Category.WORLD, true);
	private static final BloodAccounting.Source ASH_TRAIL = BloodAccounting.Source.of("ash_trail_ignition", Category.SKILL, false);
	private static final double ASH_IGNITION_ML = 25.0D;
	private static final ParticleColor BLOOD = new ParticleColor(150, 10, 18);
	private static final ParticleColor BLOOD_BRIGHT = new ParticleColor(196, 24, 30);
	private static final ParticleColor TISSUE = new ParticleColor(170, 40, 52);

	private static final Map<UUID, Long> LAST_STAIN = new HashMap<>();
	private static final Map<UUID, Long> LAST_PULSE = new HashMap<>();
	private static final Map<UUID, Long> LAST_TAKE = new HashMap<>();

	private GroundNetwork() {
	}

	/** A mark found where a ray met a face: the air cell it lies in and the face it lies on. */
	public record Mark(BlockPos cell, Direction face, BlockState state) {
		public Block block() {
			return state.getBlock();
		}
	}

	/**
	 * The mark lying on the face {@code hit} struck, if one does. Marks have no shape, so a ray finds their face. A ray
	 * that stops at the water lying over a wet living vein (CQ-44) has struck the mark's own cell: the mark is
	 * the one in that cell, on the face across from the water's surface where it lies there, else its first face.
	 */
	@Nullable
	public static Mark markAt(Level level, BlockHitResult hit) {
		if (hit == null || hit.getType() != HitResult.Type.BLOCK) return null;
		Direction side = hit.getDirection();
		BlockPos cell = hit.getBlockPos().relative(side);
		BlockState state = level.getBlockState(cell);
		Direction face = side.getOpposite();
		if (GroundMarkBlock.lies(state, face)) return new Mark(cell.immutable(), face, state);
		BlockState struck = level.getBlockState(hit.getBlockPos());
		if (struck.getBlock() instanceof GroundMarkBlock && !struck.getFluidState().isEmpty()) {
			if (GroundMarkBlock.lies(struck, face)) return new Mark(hit.getBlockPos().immutable(), face, struck);
			for (Direction any : Direction.values()) {
				if (GroundMarkBlock.lies(struck, any)) return new Mark(hit.getBlockPos().immutable(), any, struck);
			}
		}
		return null;
	}

	/** Whether the cell at {@code pos} is part of a gouge run (a gouge or a living vein). */
	public static boolean isRunCell(Level level, BlockPos pos) {
		return level.isLoaded(pos) && level.getBlockState(pos).getBlock() instanceof BloodGougeBlock;
	}

	private static boolean due(Map<UUID, Long> last, Player player, long now, int interval) {
		Long at = last.get(player.getUUID());
		if (at != null && now - at < interval && now >= at) return false;
		last.put(player.getUUID(), now);
		return true;
	}

	private static void refused(ServerPlayer player, String key) {
		player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_RED), true);
	}

	// ── Projection ──

	/**
	 * The ground's claims in the projection chain, after the block endpoints: an unlit ash trail lights; a vein node
	 * grows; a gouge or vein run pulses. Empty when the ground does not claim the aimed face. A claim on its pacing
	 * cooldown is still a claim (0 mL), so the blood is not spent somewhere else meanwhile.
	 */
	public static OptionalDouble tryProject(ServerPlayer player, double reach) {
		if (!(player.level() instanceof ServerLevel level)) return OptionalDouble.empty();
		HitResult trace = SanguineProjectionTargeting.pick(level, player, reach, false);
		if (!(trace instanceof BlockHitResult hit) || trace.getType() != HitResult.Type.BLOCK) return OptionalDouble.empty();
		OptionalDouble ash = igniteAshTrail(level, player, hit);
		if (ash.isPresent()) return ash;
		BlockPos pos = hit.getBlockPos();
		if (level.getBlockState(pos).getBlock() instanceof VeinNodeBlock) return OptionalDouble.of(growVein(player, pos));
		Mark mark = markAt(level, hit);
		if (mark != null && mark.block() instanceof BloodGougeBlock) return OptionalDouble.of(pulseFromPlayer(player, level, mark, hit));
		return OptionalDouble.empty();
	}

	/**
	 * Whether the ground claims projection on the face {@code hit} struck, as {@link #tryProject} would: an unlit ash
	 * trail, a vein node, or a gouge run lying on the face. Has no effect; decidable on both sides.
	 */
	public static boolean takesProjection(Level level, BlockHitResult hit) {
		if (hit == null || hit.getType() != HitResult.Type.BLOCK) return false;
		BlockPos pos = hit.getBlockPos();
		Block struck = level.getBlockState(pos).getBlock();
		if (litAshTrail(struck) != null || struck instanceof VeinNodeBlock) return true;
		if (hit.getDirection() == Direction.UP && litAshTrail(level.getBlockState(pos.above()).getBlock()) != null) return true;
		Mark mark = markAt(level, hit);
		return mark != null && mark.block() instanceof BloodGougeBlock;
	}

	/** Projection lights an unlit ash trail, struck or lying on the struck top face, for 25 mL. */
	private static OptionalDouble igniteAshTrail(ServerLevel level, ServerPlayer player, BlockHitResult hit) {
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		Block lit = litAshTrail(state.getBlock());
		if (lit == null && hit.getDirection() == Direction.UP) {
			pos = pos.above();
			state = level.getBlockState(pos);
			lit = litAshTrail(state.getBlock());
		}
		if (lit == null) return OptionalDouble.empty();
		if (!due(LAST_PULSE, player, level.getGameTime(), GroundRunRules.PULSE_INTERVAL)) return OptionalDouble.of(0.0D);
		if (!BloodAccounting.canPayInFull(player, ASH_IGNITION_ML, ASH_TRAIL)) {
			refused(player, "message.hemomancy.ground.too_little_blood");
			return OptionalDouble.of(0.0D);
		}
		if (!PlayerBlockEdits.replace(player, pos, lit.defaultBlockState())) {
			refused(player, "message.hemomancy.player_block_edits.refused");
			return OptionalDouble.of(0.0D);
		}
		BloodAccounting.pay(player, ASH_IGNITION_ML, ASH_TRAIL);
		Vec3 at = Vec3.atBottomCenterOf(pos).add(0.0D, 0.1D, 0.0D);
		level.sendParticles(HemoParticleData.glow(new ParticleColor(255, 100, 0)), at.x, at.y, at.z, 6, 0.25D, 0.02D, 0.25D, 0.005D);
		level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.4F, 1.4F);
		return OptionalDouble.of(ASH_IGNITION_ML);
	}

	@Nullable
	private static Block litAshTrail(Block block) {
		if (block == BlockInit.smouldering_ash_trail.get()) return BlockInit.active_smouldering_ash_trail.get();
		if (block == BlockInit.befouling_ash_trail.get()) return BlockInit.active_befouling_ash_trail.get();
		return null;
	}

	/** A pulse into a run, with whatever gouge catalyst the off hand holds dissolved into it. */
	private static double pulseFromPlayer(ServerPlayer player, ServerLevel level, Mark mark, BlockHitResult hit) {
		if (!due(LAST_PULSE, player, level.getGameTime(), GroundRunRules.PULSE_INTERVAL)) return 0.0D;
		if (!BloodAccounting.canPayInFull(player, GroundRunRules.PULSE_ML, PULSE)) {
			refused(player, "message.hemomancy.ground.too_little_blood");
			return 0.0D;
		}
		ItemStack off = player.getOffhandItem();
		GougeCatalysts.Catalyst catalyst = GougeCatalysts.of(off);
		GroundFunction function = catalyst == null ? GroundFunction.NONE : catalyst.function();
		int ticks = GroundRunRules.litTicks(catalyst == null ? 0 : catalyst.ticks());
		GroundRunSavedData.Lit lit = GroundRunSavedData.get(level).get(mark.cell());
		if (catalyst == null && lit != null && lit.function() != GroundFunction.NONE && lit.until() > level.getGameTime()) {
			// A bare pulse into a run that is already doing something never strips its function or shortens it.
			function = lit.function();
			ticks = (int) Math.max(ticks, lit.until() - level.getGameTime());
		}
		BlockPos target = function == GroundFunction.POINT && catalyst != null ? compassTarget(level, off) : null;
		BloodAccounting.pay(player, GroundRunRules.PULSE_ML, PULSE);
		if (catalyst != null && !player.getAbilities().instabuild) off.shrink(1);
		int cells = pulse(level, mark.cell(), function, ticks, target, player.getUUID());
		if (cells > 0) HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_CUT_AND_LIT);
		Vec3 at = hit.getLocation();
		level.sendParticles(HemoParticleData.bloodCell(BLOOD_BRIGHT), at.x, at.y, at.z, 8, 0.15D, 0.15D, 0.15D, 0.02D);
		level.playSound(null, mark.cell(), SoundEvents.SCULK_VEIN_PLACE, SoundSource.BLOCKS, 0.6F, 0.7F);
		Component name = Component.translatable("ground_function.hemomancy." + function.getSerializedName());
		player.displayClientMessage((cells == 1 ? Component.translatable("message.hemomancy.ground.pulse.one", name)
				: Component.translatable("message.hemomancy.ground.pulse", name, cells))
				.withStyle(ChatFormatting.DARK_RED), true);
		return GroundRunRules.PULSE_ML;
	}

	/** Where a compass in hand points in this level: its lodestone, or the world's spawn for a plain compass. */
	@Nullable
	public static BlockPos compassTarget(Level level, ItemStack compass) {
		if (!compass.is(Items.COMPASS)) return null;
		var tracker = compass.get(DataComponents.LODESTONE_TRACKER);
		Optional<GlobalPos> target = tracker != null ? tracker.target() : Optional.ofNullable(CompassItem.getSpawnPosition(level));
		return target.filter(global -> global.dimension() == level.dimension()).map(GlobalPos::pos).orElse(null);
	}

	/**
	 * Sends a pulse along the run that holds {@code start}: every connected cell (at most {@link GroundRunRules#MAX_RUN})
	 * lights a few cells a tick outward and stays lit {@code ticks}. A pointing run flares at the cell nearest
	 * {@code pointTarget}. Returns how many cells the pulse reached.
	 */
	public static int pulse(ServerLevel level, BlockPos start, GroundFunction function, int ticks, @Nullable BlockPos pointTarget) {
		return pulse(level, start, function, ticks, pointTarget, null);
	}

	/**
	 * {@link #pulse(ServerLevel, BlockPos, GroundFunction, int, BlockPos)} for {@code owner}: the player whose blood (or
	 * whose run's feeder) lit it, for whom a hot or cold run edits the blocks beside it. Null lights a run nobody owns,
	 * which still smelts, pulls, feeds and darkens but lights and freezes nothing.
	 */
	public static int pulse(ServerLevel level, BlockPos start, GroundFunction function, int ticks, @Nullable BlockPos pointTarget,
			@Nullable UUID owner) {
		List<GroundRunRules.Step> run = GroundRunRules.walk(start, pos -> isRunCell(level, pos), GroundRunRules.MAX_RUN);
		if (run.isEmpty()) return 0;
		long now = level.getGameTime();
		BlockPos flare = pointTarget != null ? GroundRunRules.farEnd(run, pointTarget) : null;
		// A hot run burns only its nearest few furnaces; one set down later joins at the next pulse or top-up.
		Map<BlockPos, Integer> furnaces = function == GroundFunction.HOT
				? GroundRunRules.furnaceSides(run, pos -> level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity, GroundRunRules.HOT_FURNACE_MAX)
				: Map.of();
		GroundRunSavedData data = GroundRunSavedData.get(level);
		for (GroundRunRules.Step step : run) {
			long litAt = now + GroundRunRules.lightDelay(step.distance());
			data.put(step.pos(), new GroundRunSavedData.Lit(litAt, litAt + Math.max(1, ticks), step.parent().asLong(),
					function, step.pos().equals(flare), owner, furnaces.getOrDefault(step.pos(), 0)));
			level.scheduleTick(step.pos(), level.getBlockState(step.pos()).getBlock(), (int) (litAt - now));
		}
		return run.size();
	}

	// ── A lit cell ──

	/** A cell's scheduled tick: light on time, do its function's work, and go dark when its time is up. */
	public static void tickCell(ServerLevel level, BlockPos pos, BlockState state) {
		GroundRunSavedData data = GroundRunSavedData.get(level);
		GroundRunSavedData.Lit lit = data.get(pos);
		long now = level.getGameTime();
		if (lit == null || now >= lit.until()) {
			if (state.getValue(BloodGougeBlock.LIT) || state.getValue(BloodGougeBlock.FUNCTION) != GroundFunction.NONE) {
				level.setBlock(pos, state.setValue(BloodGougeBlock.LIT, false).setValue(BloodGougeBlock.FUNCTION, GroundFunction.NONE),
						Block.UPDATE_CLIENTS);
			}
			if (lit != null) data.remove(pos);
			return;
		}
		if (now < lit.litAt()) {
			level.scheduleTick(pos, state.getBlock(), (int) (lit.litAt() - now));
			return;
		}
		BlockState lighted = state.setValue(BloodGougeBlock.LIT, true).setValue(BloodGougeBlock.FUNCTION, lit.function());
		if (lighted != state) level.setBlock(pos, lighted, Block.UPDATE_CLIENTS);
		work(level, pos, lighted, lit);
		level.scheduleTick(pos, state.getBlock(), (int) Math.max(1L, Math.min(GroundRunRules.EFFECT_INTERVAL, lit.until() - now)));
	}

	/** The cell at {@code pos} is no longer a gouge: forget its clock. */
	public static void forgetCell(ServerLevel level, BlockPos pos) {
		GroundRunSavedData.get(level).remove(pos);
	}

	/** A lit cell's work, once every {@link GroundRunRules#EFFECT_INTERVAL} ticks. */
	private static void work(ServerLevel level, BlockPos pos, BlockState state, GroundRunSavedData.Lit lit) {
		switch (lit.function()) {
			case HOT -> heat(level, pos, owner(level, lit.owner()), lit.furnaceSides());
			case POINT -> {
				if (lit.flare()) flare(level, pos);
			}
			case COLD -> chill(level, pos, owner(level, lit.owner()));
			case PULL -> pull(level, pos, lit.towardPos());
			case FEED -> feed(level, pos);
			case DARK -> darken(level, pos);
			default -> {
			}
		}
	}

	/** Whether this effect tick of the cell at {@code pos} is one of every {@code every}. */
	private static boolean beat(ServerLevel level, BlockPos pos, int every) {
		return Math.floorMod(level.getGameTime() / GroundRunRules.EFFECT_INTERVAL + pos.asLong(), every) == 0L;
	}

	/**
	 * The run's owner when they may edit this level for it now: online, alive and standing in it. {@code PlayerBlockEdits}
	 * writes into the player's own level, so an owner elsewhere would edit the wrong world.
	 */
	@Nullable
	private static ServerPlayer owner(ServerLevel level, @Nullable UUID owner) {
		if (owner == null || level.getServer() == null) return null;
		ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
		return player != null && player.isAlive() && player.level() == level ? player : null;
	}

	private static void heat(ServerLevel level, BlockPos pos, @Nullable ServerPlayer owner, int furnaceSides) {
		boolean smelt = beat(level, pos, GroundRunRules.HOT_SMELT_EVERY);
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos))) {
			if (!smelt) break;
			ItemStack stack = item.getItem();
			Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager()
					.getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
			if (recipe.isEmpty()) continue;
			ItemStack result = recipe.get().value().getResultItem(level.registryAccess()).copy();
			if (result.isEmpty()) continue;
			ItemStack left = stack.copy();
			left.shrink(1);
			if (left.isEmpty()) item.discard();
			else item.setItem(left);
			ItemEntity made = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), result);
			made.setDeltaMovement(Vec3.ZERO);
			level.addFreshEntity(made);
			level.sendParticles(HemoParticleData.ember(new ParticleColor(255, 120, 30), 0.12F, 0.8F, 16),
					item.getX(), item.getY() + 0.1D, item.getZ(), 3, 0.1D, 0.05D, 0.1D, 0.01D);
			break;
		}
		// Lighting is a block edit for the run's owner; a run with no owner present lights nothing.
		if (owner == null) return;
		for (Direction side : Direction.values()) {
			BlockPos next = pos.relative(side);
			SwitchableLights.kindled(level.getBlockState(next)).ifPresent(lit -> PlayerBlockEdits.replace(owner, next, lit));
			// A furnace, smoker or blast furnace beside the run burns on its heat while it holds (round two P8), if it is
			// one of the run's nearest four (CQ-43).
			if (GroundRunRules.burnsSide(furnaceSides, side) && FurnaceHeat.stoke(owner, level, next, GroundRunRules.HOT_FURNACE_BURN)) {
				HarbingerAdvancementGranter.grantIfNotDone(owner, HarbingerAdvancementGranter.ADV_STOKED);
			}
		}
	}

	private static void flare(ServerLevel level, BlockPos pos) {
		Vec3 at = Vec3.atCenterOf(pos);
		level.sendParticles(HemoParticleData.glow(new ParticleColor(214, 196, 255)), at.x, at.y + 0.6D, at.z, 6, 0.08D, 0.8D, 0.08D, 0.01D);
		level.sendParticles(HemoParticleData.glow(new ParticleColor(196, 24, 30)), at.x, at.y, at.z, 3, 0.2D, 0.05D, 0.2D, 0.0D);
	}

	private static void chill(ServerLevel level, BlockPos pos, @Nullable ServerPlayer owner) {
		// Freezing is a block edit for the run's owner; crops beside a cold run are held back without one
		// (chilledBeside reads the lit cell, not the owner).
		if (owner == null) return;
		for (Direction side : Direction.values()) {
			BlockPos next = pos.relative(side);
			BlockState beside = level.getBlockState(next);
			if (beside.is(Blocks.WATER) && level.getFluidState(next).isSourceOfType(Fluids.WATER)) {
				if (!PlayerBlockEdits.replace(owner, next, Blocks.FROSTED_ICE.defaultBlockState())) continue;
				level.scheduleTick(next, Blocks.FROSTED_ICE, 60 + level.random.nextInt(60));
			}
		}
	}

	private static void pull(ServerLevel level, BlockPos pos, BlockPos toward) {
		Vec3 goal = Vec3.atBottomCenterOf(toward);
		for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(0.1D))) {
			Vec3 step = goal.subtract(item.position());
			double length = step.horizontalDistance();
			if (length < 0.05D) continue;
			item.setDeltaMovement(step.x / length * 0.14D, item.getDeltaMovement().y, step.z / length * 0.14D);
			item.hurtMarked = true;
		}
	}

	private static void feed(ServerLevel level, BlockPos pos) {
		for (Direction side : Direction.values()) {
			BlockPos next = pos.relative(side);
			// The beat is the crop's, and only its lowest feeding cell grows it, so two cells beside it grow it once.
			if (!beat(level, next, GroundRunRules.FEED_EVERY) || !feedsFirst(level, pos, next)) continue;
			BlockState beside = level.getBlockState(next);
			if (beside.getBlock() instanceof CropBlock crop) {
				if (!crop.isMaxAge(beside)) level.setBlock(next, crop.getStateForAge(crop.getAge(beside) + 1), Block.UPDATE_CLIENTS);
			} else if (beside.getBlock() instanceof BonemealableBlock && beside.isRandomlyTicking()) {
				beside.randomTick(level, next, level.random);
			}
		}
	}

	/** Whether {@code cell} is the lit feeding cell beside {@code crop} that comes first by position. */
	private static boolean feedsFirst(ServerLevel level, BlockPos cell, BlockPos crop) {
		for (Direction side : Direction.values()) {
			BlockPos other = crop.relative(side);
			if (other.asLong() >= cell.asLong()) continue;
			BlockState state = level.getBlockState(other);
			if (state.getBlock() instanceof BloodGougeBlock && state.getValue(BloodGougeBlock.LIT)
					&& state.getValue(BloodGougeBlock.FUNCTION) == GroundFunction.FEED) return false;
		}
		return true;
	}

	private static void darken(ServerLevel level, BlockPos pos) {
		for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(1.0D))) {
			SchoolStates.apply(null, mob, SchoolState.OBSCURED, 40, 1);
		}
	}

	/** Whether a lit cold run lies beside {@code pos}: crops there do not grow. */
	public static boolean chilledBeside(Level level, BlockPos pos) {
		for (Direction side : Direction.values()) {
			BlockState beside = level.getBlockState(pos.relative(side));
			if (beside.getBlock() instanceof BloodGougeBlock && beside.getValue(BloodGougeBlock.LIT)
					&& beside.getValue(BloodGougeBlock.FUNCTION) == GroundFunction.COLD) return true;
		}
		return false;
	}

	// ── The catalyst feeder ──

	/**
	 * A feeder beside a lit run dissolves one catalyst of the run's function into it when the run is about to go dark,
	 * for {@link GroundRunRules#FEEDER_ML} of the blood of whoever lit the run (CQ-7, NQ-02-1, NQ-02-2). With that player
	 * away, dead, out of this level or short, it feeds nothing and the run goes dark on time. It never lights a dark run.
	 */
	public static void feed(ServerLevel level, CatalystFeederBlockEntity feeder) {
		ItemStack stock = feeder.stock().getStackInSlot(0);
		GougeCatalysts.Catalyst catalyst = GougeCatalysts.of(stock);
		if (catalyst == null) return;
		long now = level.getGameTime();
		GroundRunSavedData data = GroundRunSavedData.get(level);
		for (Direction side : Direction.values()) {
			BlockPos cell = feeder.getBlockPos().relative(side);
			BlockState state = level.getBlockState(cell);
			if (!(state.getBlock() instanceof BloodGougeBlock) || !state.getValue(BloodGougeBlock.LIT)
					|| state.getValue(BloodGougeBlock.FUNCTION) != catalyst.function()) continue;
			GroundRunSavedData.Lit lit = data.get(cell);
			if (lit == null || lit.until() - now > GroundRunRules.FEEDER_MARGIN_TICKS) continue;
			ServerPlayer payer = owner(level, lit.owner());
			if (payer == null || !HemoCapabilityAccess.getBloodVolume(payer).isActive()
					|| !BloodAccounting.canPayInFull(payer, GroundRunRules.FEEDER_ML, FEEDER)) return;
			ItemStack used = feeder.stock().extractItem(0, 1, false);
			if (used.isEmpty()) return;
			BloodAccounting.pay(payer, GroundRunRules.FEEDER_ML, FEEDER);
			BlockPos target = catalyst.function() == GroundFunction.POINT ? compassTarget(level, used) : null;
			// The feeder keeps the run going for whoever lit it.
			pulse(level, cell, catalyst.function(), catalyst.ticks(), target, lit.owner());
			level.sendParticles(HemoParticleData.glow(new ParticleColor(196, 24, 30)), cell.getX() + 0.5D, cell.getY() + 0.3D,
					cell.getZ() + 0.5D, 4, 0.2D, 0.1D, 0.2D, 0.0D);
			return;
		}
	}

	// ── Stains ──

	/**
	 * The projection chain's last link: blood that nothing else took lands on the struck face as a stain, a few mL a
	 * face. A wet stain already there takes nothing; a tacky or dry one is wetted again for less.
	 */
	public static double stain(ServerPlayer player, HitResult trace) {
		if (!(trace instanceof BlockHitResult hit) || trace.getType() != HitResult.Type.BLOCK
				|| !(player.level() instanceof ServerLevel level)) return 0.0D;
		Direction side = hit.getDirection();
		BlockPos cell = hit.getBlockPos().relative(side);
		Direction face = side.getOpposite();
		BlockState there = level.getBlockState(cell);
		BlockState stain;
		double price;
		if (there.getBlock() instanceof BloodStainBlock) {
			if (MultifaceBlock.hasFace(there, face) && there.getValue(BloodStainBlock.AGE) == GroundRunRules.WET) return 0.0D;
			price = MultifaceBlock.hasFace(there, face) ? GroundRunRules.REWET_ML : GroundRunRules.STAIN_ML;
			stain = there.setValue(MultifaceBlock.getFaceProperty(face), true).setValue(BloodStainBlock.AGE, GroundRunRules.WET);
		} else if (there.isAir() && MultifaceBlock.canAttachTo(level, face, hit.getBlockPos(), level.getBlockState(hit.getBlockPos()))) {
			price = GroundRunRules.STAIN_ML;
			stain = BlockInit.blood_stain.get().defaultBlockState().setValue(MultifaceBlock.getFaceProperty(face), true);
		} else {
			return 0.0D;
		}
		if (!due(LAST_STAIN, player, level.getGameTime(), GroundRunRules.STAIN_INTERVAL)
				|| !BloodAccounting.canPayInFull(player, price, STAIN)) return 0.0D;
		boolean placed = there.isAir() ? PlayerBlockEdits.place(player, cell, stain) : PlayerBlockEdits.replace(player, cell, stain);
		if (!placed) return 0.0D;
		BloodAccounting.pay(player, price, STAIN);
		WorldAnswer.stainLaid(level, cell, price);
		Vec3 at = hit.getLocation();
		level.sendParticles(HemoParticleData.bloodCell(BLOOD), at.x, at.y, at.z, 3, 0.12D, 0.04D, 0.12D, 0.01D);
		return price;
	}

	/**
	 * The wet stain lying on the face {@code hit} struck, as its cell, or null. A plant strand sunk there runs along the
	 * stain (core-loop round two P5).
	 */
	@Nullable
	public static BlockPos wetStainAt(Level level, BlockHitResult hit) {
		Mark mark = markAt(level, hit);
		return mark != null && isWetStain(level, mark.cell()) ? mark.cell() : null;
	}

	/** Whether the cell at {@code pos} holds a wet stain. */
	public static boolean isWetStain(Level level, BlockPos pos) {
		if (!level.isLoaded(pos)) return false;
		BlockState state = level.getBlockState(pos);
		return state.getBlock() instanceof BloodStainBlock && state.getValue(BloodStainBlock.AGE) == GroundRunRules.WET;
	}

	/**
	 * A strand's eruption running along a stain spends the stain's wet blood: the cell goes tacky, as {@code player}'s
	 * edit. Returns whether it did.
	 */
	public static boolean spendStain(ServerPlayer player, BlockPos pos) {
		Level level = player.level();
		if (!isWetStain(level, pos)) return false;
		return PlayerBlockEdits.replace(player, pos, level.getBlockState(pos).setValue(BloodStainBlock.AGE, GroundRunRules.TACKY));
	}

	/** A random tick of a stain: rain or water beside it washes it; otherwise it ages a step, or a dry one wears away. */
	public static void ageStain(ServerLevel level, BlockPos pos, BlockState state, float roll) {
		if (washes(level, pos)) {
			level.removeBlock(pos, false);
			return;
		}
		int next = GroundRunRules.nextStainAge(state.getValue(BloodStainBlock.AGE), roll);
		if (next < 0) {
			level.removeBlock(pos, false);
			// The world's answer (Track 10): in a heavy region the grass under a stain that wore away may stay stained.
			WorldAnswer.stainFaded(level, pos, state, level.random.nextFloat());
		}
		else if (next != state.getValue(BloodStainBlock.AGE)) level.setBlock(pos, state.setValue(BloodStainBlock.AGE, next), Block.UPDATE_CLIENTS);
	}

	/** Whether rain falls on the cell or water stands beside it. */
	public static boolean washes(Level level, BlockPos pos) {
		if (level.isRainingAt(pos)) return true;
		for (Direction side : Direction.values()) {
			if (level.getFluidState(pos.relative(side)).is(net.minecraft.tags.FluidTags.WATER)) return true;
		}
		return false;
	}

	/** A stain on the floor under a creature that died or is bleeding hard: not a player's edit, the body's. */
	public static void leaveStain(ServerLevel level, BlockPos at) {
		BlockPos floor = at.below();
		BlockState there = level.getBlockState(at);
		if (!MultifaceBlock.canAttachTo(level, Direction.DOWN, floor, level.getBlockState(floor)) || washes(level, at)) return;
		if (there.isAir()) {
			level.setBlockAndUpdate(at, BlockInit.blood_stain.get().defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true));
		} else if (there.getBlock() instanceof BloodStainBlock) {
			level.setBlockAndUpdate(at, there.setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true).setValue(BloodStainBlock.AGE, GroundRunRules.WET));
		} else {
			return;
		}
		WorldAnswer.stainLaid(level, at, GroundRunRules.STAIN_ML);
	}

	// ── Absorption ──

	/**
	 * Absorption on a mark takes it back up, one face every few ticks: part of a wet stain returns to the player and an
	 * older one is cleaned; a gouge or vein is filled in; a Lux trace is put out. False when no mark lies on the face.
	 */
	public static boolean tryAbsorb(ServerPlayer player, double reach) {
		if (!(player.level() instanceof ServerLevel level)) return false;
		// A creature in front of the mark is what the player is drawing from: the living-target pulse takes it.
		Optional<VerbTarget> aimed = VerbTargeting.pick(level, player, reach, EnumSet.of(VerbTargetKind.BLOCK, VerbTargetKind.CREATURE));
		if (aimed.isEmpty() || !(aimed.get() instanceof VerbTarget.Block block)) return false;
		HitResult trace = block.hit();
		Mark mark = markAt(level, block.hit());
		if (mark == null) return false;
		if (!due(LAST_TAKE, player, level.getGameTime(), GroundRunRules.STAIN_INTERVAL)) return true;
		double refund = mark.block() instanceof BloodStainBlock ? GroundRunRules.stainRefund(mark.state().getValue(BloodStainBlock.AGE)) : 0.0D;
		BlockState less = GroundMarkBlock.without(mark.state(), mark.face());
		boolean taken = less.isAir() ? PlayerBlockEdits.lift(player, mark.cell()) != null
				: PlayerBlockEdits.replace(player, mark.cell(), less);
		if (!taken) {
			refused(player, "message.hemomancy.player_block_edits.refused");
			return true;
		}
		if (mark.block() instanceof BloodStainBlock) WorldAnswer.stainCleaned(level, mark.cell());
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		if (refund > 0.0D && volume.isActive()) {
			BloodFlowLedger.applyDirectIncome(player, volume, "stain_reabsorb", "blood_source.hemomancy.stain_reabsorb",
					Category.WORLD, refund, 1);
		}
		Vec3 at = trace.getLocation();
		ParticleColor color = mark.block() instanceof LuxTraceBlock ? new ParticleColor(255, 230, 168)
				: mark.block() instanceof LivingVeinBlock ? TISSUE : BLOOD;
		level.sendParticles(HemoParticleData.absorbedBloodCell(color), at.x, at.y, at.z, 4, 0.12D, 0.04D, 0.12D, 0.02D);
		return true;
	}

	// ── Living veins ──

	/**
	 * Projection into a vein node: tissue grows into the nearest gouge cells of its run, a few a pulse, each paid and
	 * counted against the owner's allowance by degree. Returns the blood spent.
	 */
	public static double growVein(ServerPlayer player, BlockPos nodePos) {
		ServerLevel level = player.serverLevel();
		if (!(level.getBlockEntity(nodePos) instanceof VeinNodeBlockEntity node)) return 0.0D;
		if (!due(LAST_PULSE, player, level.getGameTime(), GroundRunRules.PULSE_INTERVAL / 2)) return 0.0D;
		if (node.owner() != null && !node.owner().equals(player.getUUID())) {
			refused(player, "message.hemomancy.ground.node_not_yours");
			return 0.0D;
		}
		if (node.owner() == null) node.setOwner(player.getUUID(), level.getGameTime());
		int allowance = veinAllowance(player);
		VeinLedger ledger = VeinLedger.get(level);
		int room = allowance - ledger.length(player.getUUID());
		if (allowance <= 0) {
			refused(player, "message.hemomancy.ground.vein_needs_degree");
			return 0.0D;
		}
		if (room <= 0) {
			player.displayClientMessage(Component.translatable("message.hemomancy.ground.vein_allowance", allowance)
					.withStyle(ChatFormatting.DARK_RED), true);
			return 0.0D;
		}
		List<GroundRunRules.Step> run = runFromNode(level, nodePos);
		double spent = 0.0D;
		int grown = 0;
		for (GroundRunRules.Step step : run) {
			if (grown >= Math.min(GroundRunRules.VEIN_GROWTH_PER_PULSE, room)) break;
			BlockState state = level.getBlockState(step.pos());
			if (!(state.getBlock() instanceof BloodGougeBlock) || state.getBlock() instanceof LivingVeinBlock) continue;
			if (!BloodAccounting.canPayInFull(player, GroundRunRules.VEIN_GROWTH_ML, VEIN_GROWTH)) {
				refused(player, "message.hemomancy.ground.too_little_blood");
				break;
			}
			if (!PlayerBlockEdits.replace(player, step.pos(), asVein(state))) continue;
			BloodAccounting.pay(player, GroundRunRules.VEIN_GROWTH_ML, VEIN_GROWTH);
			ledger.claim(level, step.pos(), player.getUUID());
			spent += GroundRunRules.VEIN_GROWTH_ML;
			grown++;
			Vec3 at = Vec3.atCenterOf(step.pos());
			level.sendParticles(HemoParticleData.bloodCell(TISSUE), at.x, at.y - 0.3D, at.z, 4, 0.2D, 0.05D, 0.2D, 0.01D);
		}
		node.tended(level.getGameTime());
		if (grown > 0) level.playSound(null, nodePos, SoundEvents.SCULK_VEIN_PLACE, SoundSource.BLOCKS, 0.8F, 0.6F);
		else if (run.isEmpty()) refused(player, "message.hemomancy.ground.node_no_run");
		return spent;
	}

	/** The total vein length {@code player} may keep grown, by degree. */
	public static int veinAllowance(Player player) {
		for (int degree = 7; degree >= 3; degree--) {
			if (ProgressionGates.hasDegree(player, degree)) return GroundRunRules.veinAllowance(degree);
		}
		return 0;
	}

	private static BlockState asVein(BlockState gouge) {
		BlockState vein = BlockInit.living_vein.get().defaultBlockState();
		for (Direction face : Direction.values()) {
			vein = vein.setValue(MultifaceBlock.getFaceProperty(face), MultifaceBlock.hasFace(gouge, face));
		}
		// A new vein starts dry: a gouge never holds water (CQ-44).
		return vein.setValue(BloodGougeBlock.FUNCTION, gouge.getValue(BloodGougeBlock.FUNCTION))
				.setValue(BloodGougeBlock.LIT, gouge.getValue(BloodGougeBlock.LIT));
	}

	private static BlockState asGouge(BlockState vein) {
		BlockState gouge = BlockInit.blood_gouge.get().defaultBlockState();
		for (Direction face : Direction.values()) {
			gouge = gouge.setValue(MultifaceBlock.getFaceProperty(face), MultifaceBlock.hasFace(vein, face));
		}
		gouge = gouge.setValue(BloodGougeBlock.FUNCTION, vein.getValue(BloodGougeBlock.FUNCTION))
				.setValue(BloodGougeBlock.LIT, vein.getValue(BloodGougeBlock.LIT));
		// A vein under water withers into a gouge the water washes out: the cell keeps only its water.
		return LivingVeinBlock.withered(vein, gouge);
	}

	/**
	 * Whether one living run joins the blocks at {@code a} and {@code b} (two Earthen Veins, core-loop round two P8):
	 * walking living vein cells out from {@code a}, as a run joins (faces and edges, at most {@link GroundRunRules#MAX_RUN}
	 * cells), one of them touches {@code b} the same way. Gouge that has not taken tissue, or has withered back, does not
	 * carry. Never loads a chunk.
	 */
	public static boolean livingRunJoins(Level level, BlockPos a, BlockPos b) {
		if (a.equals(b)) return false;
		List<GroundRunRules.Step> walked = GroundRunRules.walk(a, pos -> pos.equals(a) || livingCell(level, pos),
				GroundRunRules.MAX_RUN + 1);
		for (GroundRunRules.Step step : walked) {
			if (!step.pos().equals(a) && GroundRunRules.joined(step.pos(), b)) return true;
		}
		return false;
	}

	private static boolean livingCell(Level level, BlockPos pos) {
		return level.isLoaded(pos) && level.getBlockState(pos).getBlock() instanceof LivingVeinBlock;
	}

	/** The run cells touching a node and those connected to them, nearest first; the node itself is left out. */
	public static List<GroundRunRules.Step> runFromNode(Level level, BlockPos nodePos) {
		List<GroundRunRules.Step> walked = GroundRunRules.walk(nodePos,
				pos -> pos.equals(nodePos) || isRunCell(level, pos), GroundRunRules.MAX_RUN + 1);
		return walked.isEmpty() ? walked : walked.subList(1, walked.size());
	}

	/**
	 * A node's second: while its owner stands near it the vein is tended and, with a second node at the run's other end,
	 * a little blood runs toward the end that is drawing ({@link GroundRunRules#veinTransfer}, CL-14), the owner paying a
	 * small upkeep while it moves. Left untended for {@link GroundRunRules#VEIN_WITHER_TICKS} the whole run withers back
	 * to gouge.
	 */
	public static void tickNode(ServerLevel level, VeinNodeBlockEntity node) {
		BlockPos pos = node.getBlockPos();
		long now = level.getGameTime();
		List<GroundRunRules.Step> run = runFromNode(level, pos);
		List<GroundRunRules.Step> veins = run.stream().filter(step -> level.getBlockState(step.pos()).getBlock() instanceof LivingVeinBlock).toList();
		if (veins.isEmpty()) return;
		ServerPlayer tender = tender(level, node.owner(), pos);
		if (tender == null) {
			if (now - node.lastTended(now) > GroundRunRules.VEIN_WITHER_TICKS) wither(level, veins);
			return;
		}
		node.tended(now);
		VeinNodeBlockEntity partner = partner(level, pos, veins);
		if (partner == null) return;
		partner.tended(now);
		// One node of a pair does the moving, so the blood is not moved twice.
		if (pos.asLong() > partner.getBlockPos().asLong()) return;
		BlockEntity a = reservoirBeside(level, pos);
		BlockEntity b = reservoirBeside(level, partner.getBlockPos());
		if (a == null || b == null || a == b) return;
		UUID owner = tender.getUUID();
		if (!BloodDrawRights.mayDraw(level, owner, a) || !BloodDrawRights.mayDraw(level, owner, b)) return;
		BlockBloodVolume va = ReservoirBlood.settled(a);
		BlockBloodVolume vb = ReservoirBlood.settled(b);
		double amount = GroundRunRules.veinTransfer(va.getBloodVolume(), room(va), va.capacity(), demand(level, a, va),
				vb.getBloodVolume(), room(vb), vb.capacity(), demand(level, b, vb), GroundRunRules.VEIN_FLOW_PER_SECOND);
		// Nothing draws at either end: nothing moves and no upkeep is paid (CL-14).
		if (Math.abs(amount) < 0.01D) return;
		IBloodReservoir from = (IBloodReservoir) (amount > 0 ? a : b);
		IBloodReservoir to = (IBloodReservoir) (amount > 0 ? b : a);
		BlockBloodVolume source = amount > 0 ? va : vb;
		// Never take more than the far tank has room for now, so no blood is lost between the two.
		double room = Math.max(0.0D, to.getMaxBloodVolume() - to.getBloodVolume());
		if (!from.canProvideBlood() || !to.canReceiveBlood() || room < 0.01D) return;
		if (!BloodAccounting.payUpkeep(tender, GroundRunRules.VEIN_UPKEEP_ML, VEIN_UPKEEP, 20, true).satisfied()) return;
		// The vein's own move is no draw on the source, or the source would start drawing back.
		double taken = source.takeAlongVein(Math.min(Math.abs(amount), room));
		double given = to.receiveBlood(taken);
		if (given < taken) source.fill(taken - given);
		from.sendUpdates();
		if (given > 0.0D) {
			BlockPos mid = veins.get(veins.size() / 2).pos();
			level.sendParticles(HemoParticleData.bloodCell(TISSUE), mid.getX() + 0.5D, mid.getY() + 0.1D, mid.getZ() + 0.5D,
					2, 0.15D, 0.02D, 0.15D, 0.0D);
		}
	}

	private static double room(BlockBloodVolume volume) {
		return Math.max(0.0D, volume.getMaxBloodVolume() - volume.getBloodVolume());
	}

	/** How much the tank at one end of a vein is drawing ({@link GroundRunRules#drawDemand}). */
	private static double demand(ServerLevel level, BlockEntity tank, BlockBloodVolume volume) {
		boolean machine = !((IBloodReservoir) tank).isBloodStore();
		return GroundRunRules.drawDemand(level.getGameTime(), volume.lastDrawn(), volume.getBloodVolume(), room(volume), machine,
				BloodRoutingRules.DEFAULT_WORKING_RESERVE);
	}

	/** The owner, when they are online, in this level, near the node and able to feed a vein. */
	@Nullable
	private static ServerPlayer tender(ServerLevel level, @Nullable UUID owner, BlockPos node) {
		if (owner == null) return null;
		ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
		if (player == null || player.level() != level || !player.isAlive()
				|| player.distanceToSqr(Vec3.atCenterOf(node)) > GroundRunRules.VEIN_OWNER_RANGE * GroundRunRules.VEIN_OWNER_RANGE
				|| !HemoCapabilityAccess.getBloodVolume(player).isActive() || veinAllowance(player) <= 0) return null;
		return player;
	}

	@Nullable
	private static VeinNodeBlockEntity partner(ServerLevel level, BlockPos self, List<GroundRunRules.Step> veins) {
		for (GroundRunRules.Step step : veins) {
			for (BlockPos near : BlockPos.betweenClosed(step.pos().offset(-1, -1, -1), step.pos().offset(1, 1, 1))) {
				if (near.equals(self)) continue;
				if (level.getBlockEntity(near) instanceof VeinNodeBlockEntity other) return other;
			}
		}
		return null;
	}

	/** The tank beside a node: the first block entity next to it that keeps blood. */
	@Nullable
	public static BlockEntity reservoirBeside(ServerLevel level, BlockPos node) {
		for (Direction side : Direction.values()) {
			BlockEntity blockEntity = level.getBlockEntity(node.relative(side));
			if (blockEntity instanceof IBloodReservoir reservoir && reservoir.holdsBlood()
					&& !(blockEntity instanceof VeinNodeBlockEntity)) return blockEntity;
		}
		return null;
	}

	/** A vein left untended goes back to gouge, keeping its faces and any light; one under water is washed out. */
	public static void wither(ServerLevel level, List<GroundRunRules.Step> veins) {
		List<BlockPos> withered = new ArrayList<>();
		for (GroundRunRules.Step step : veins) {
			BlockState state = level.getBlockState(step.pos());
			if (!(state.getBlock() instanceof LivingVeinBlock)) continue;
			level.setBlock(step.pos(), asGouge(state), Block.UPDATE_CLIENTS);
			withered.add(step.pos());
		}
		if (!withered.isEmpty()) {
			BlockPos at = withered.getFirst();
			level.playSound(null, at, SoundEvents.SCULK_VEIN_BREAK, SoundSource.BLOCKS, 0.6F, 0.6F);
		}
	}

	// ── Session state ──

	public static void clearSessionState() {
		LAST_STAIN.clear();
		LAST_PULSE.clear();
		LAST_TAKE.clear();
	}

	public static void logout(Player player) {
		LAST_STAIN.remove(player.getUUID());
		LAST_PULSE.remove(player.getUUID());
		LAST_TAKE.remove(player.getUUID());
	}
}
