package com.vincenthuto.hemomancy.common.block.unstained.crafting;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Passive ward fixture: shelters Unstained players and suppresses blood-born creatures. */
public class VerdigrisLatticeBlock extends Block {
	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(1, 0, 1, 15, 2, 15),
			Block.box(2, 2, 7, 4, 15, 9),
			Block.box(12, 2, 7, 14, 15, 9),
			Block.box(4, 3, 7, 12, 5, 9),
			Block.box(4, 13, 7, 12, 15, 9),
			Block.box(6, 7, 6, 10, 11, 10));
	public VerdigrisLatticeBlock(Properties properties) { super(properties); }

	@Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}
	@Override public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) { return false; }

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		applyWard(level, pos);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		applyWard(level, pos);
		level.scheduleTick(pos, this, 80);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (!level.isClientSide && !state.is(oldState.getBlock())) level.scheduleTick(pos, this, 20);
	}

	private static void applyWard(ServerLevel level, BlockPos pos) {
		AABB ward = new AABB(pos).inflate(8.0);
		for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, ward)) {
			if (living instanceof Player player && HemoCapabilityAccess.getUnstainedProgress(player)
					.map(progress -> progress.hasBegunPurification()).orElse(false)) {
				living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 0, true, false));
			} else if (living.getType().is(EntityInit.HEMOMANCY_MOB)) {
				living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 0, true, true));
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 0, true, false));
			}
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextFloat() > 0.65f) return;
		double angle = random.nextDouble() * Math.PI * 2.0;
		double radius = 0.42;
		level.addParticle(ParticleTypes.WAX_OFF,
				pos.getX() + 0.5 + Math.cos(angle) * radius,
				pos.getY() + 0.55 + random.nextDouble() * 0.45,
				pos.getZ() + 0.5 + Math.sin(angle) * radius,
				-Math.sin(angle) * 0.01, 0.006, Math.cos(angle) * 0.01);
		if (random.nextFloat() < 0.2f) level.addParticle(ParticleTypes.END_ROD,
				pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 0, 0.01, 0);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		player.displayClientMessage(Component.literal(
				"Verdigris ward: 8 block shelter; nearby condensers work twice as fast and yield twice the dew."), true);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	public static boolean hasActiveLattice(Level level, BlockPos pos, int radius) {
		for (BlockPos check : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius),
				pos.offset(radius, radius, radius))) {
			if (level.getBlockState(check).is(BlockInit.verdigris_lattice.get())) return true;
		}
		return false;
	}
}
