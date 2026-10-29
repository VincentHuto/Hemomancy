package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import java.util.function.Predicate;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.world.GardensTerrainRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Pale lamellae hanging from a cap or a cave roof. Open, they part for anything; touched by a player who is not an
 * Apotheos, they bite, and every gill joined to the one touched snaps shut into flesh until the stranger has gone.
 */
public class FungalGillBlock extends Block {
	public static final BooleanProperty OPEN = BooleanProperty.create("open");
	private static final VoxelShape OPEN_SHAPE = Block.box(2, 0, 2, 14, 16, 14);
	private static final ParticleColor CRIMSON_SPORE = new ParticleColor(150, 40, 58);

	public FungalGillBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(OPEN, true));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(OPEN);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(OPEN) ? OPEN_SHAPE : Shapes.block();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(OPEN) ? Shapes.empty() : Shapes.block();
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos above = pos.above();
		BlockState ceiling = level.getBlockState(above);
		return ceiling.is(this) || ceiling.isFaceSturdy(level, above, Direction.DOWN);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		if (direction == Direction.UP && !canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
		return state;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (level.isClientSide || !state.getValue(OPEN) || !(entity instanceof ServerPlayer player)
				|| !isStranger(player)) return;
		ServerLevel server = (ServerLevel) level;
		snap(server, pos);
		player.hurt(HemoDamageTypes.gillBite(level), GardensTerrainRules.GILL_BITE);
		player.knockback(0.6, pos.getX() + 0.5 - player.getX(), pos.getZ() + 0.5 - player.getZ());
		player.hurtMarked = true;
		server.sendParticles(HemoParticleData.sporiticSpore(CRIMSON_SPORE), pos.getX() + 0.5, pos.getY() + 0.5,
				pos.getZ() + 0.5, 20, 0.4, 0.5, 0.4, 0.03);
		server.playSound(null, pos, SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.BLOCKS, 0.8f, 0.7f);
	}

	/** Snaps the gills joined to {@code origin}, sparing (and not spreading through) any an Apotheos stands in. */
	public static int snap(ServerLevel level, BlockPos origin) {
		return snap(level, origin, pos -> !level.getEntitiesOfClass(Player.class, new AABB(pos),
				player -> player.isAlive() && ApotheosBodyEvents.isApotheos(player)).isEmpty());
	}

	/** Shuts the open gills joined to {@code origin}, up to the spread limit, except {@code spared} ones; returns how many. */
	public static int snap(ServerLevel level, BlockPos origin, Predicate<BlockPos> spared) {
		Block gill = level.getBlockState(origin).getBlock();
		if (!(gill instanceof FungalGillBlock)) return 0;
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> seen = new HashSet<>();
		queue.add(origin);
		seen.add(origin);
		int shut = 0;
		while (!queue.isEmpty() && shut < GardensTerrainRules.GILL_SPREAD) {
			BlockPos pos = queue.poll();
			BlockState state = level.getBlockState(pos);
			if (!state.is(gill) || !state.getValue(OPEN) || spared.test(pos)) continue;
			level.setBlock(pos, state.setValue(OPEN, false), Block.UPDATE_CLIENTS);
			level.scheduleTick(pos, gill, GardensTerrainRules.GILL_REOPEN_TICKS);
			shut++;
			for (Direction direction : Direction.values()) {
				BlockPos next = pos.relative(direction);
				if (seen.add(next)) queue.add(next);
			}
		}
		return shut;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(OPEN)) return;
		boolean strangerNear = !level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(2.0),
				FungalGillBlock::isStranger).isEmpty();
		if (strangerNear) {
			level.scheduleTick(pos, this, GardensTerrainRules.GILL_REOPEN_WAIT);
		} else {
			level.setBlock(pos, state.setValue(OPEN, true), Block.UPDATE_CLIENTS);
		}
	}

	private static boolean isStranger(Player player) {
		return GardensTerrainRules.gillSnaps(ApotheosBodyEvents.isApotheos(player),
				player.isCreative() || player.isSpectator(), player.isAlive());
	}
}
