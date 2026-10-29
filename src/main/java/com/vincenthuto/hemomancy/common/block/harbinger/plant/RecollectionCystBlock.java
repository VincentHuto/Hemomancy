package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A cyst in the Gardens' Archive where the hive keeps what it took from a Saint. Only the eighth degree can draw the
 * Recollection out: an Apotheos on foot, or a Silentium through a possessed facsimile. It holds one, then withers.
 */
public class RecollectionCystBlock extends Block {
	/** Which Saint's memory it holds: 0 Hemorath, 1 Seraphae, 2 Putriciel, 3 Velorum. */
	public static final IntegerProperty SAINT = IntegerProperty.create("saint", 0, 3);
	private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 9, 13);
	private static final ParticleColor PALE = new ParticleColor(226, 206, 200);

	public RecollectionCystBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(SAINT, 0));
	}

	public static Item recollection(int saint) {
		return switch (saint) {
			case 1 -> ItemInit.recollection_seraphae.get();
			case 2 -> ItemInit.recollection_putriciel.get();
			case 3 -> ItemInit.recollection_velorum.get();
			default -> ItemInit.recollection_hemorath.get();
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SAINT);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.below()).is(RootedStateHelper.FUNGAL_NETWORK_GROUND);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		return direction == Direction.DOWN && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : state;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (level.isClientSide || !(entity instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()) return;
		if (!RecollectionRules.mayTake(ApotheosBodyEvents.isApotheos(player), HemoCapabilityAccess.isPossessing(player))) return;
		ItemStack memory = new ItemStack(recollection(state.getValue(SAINT)));
		if (!player.getInventory().add(memory) && !memory.isEmpty()) player.drop(memory, false);
		ServerLevel server = (ServerLevel) level;
		server.sendParticles(HemoParticleData.glow(PALE), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30,
				0.3, 0.4, 0.3, 0.03);
		server.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 1f, 0.6f);
		player.displayClientMessage(Component.translatable("message.hemomancy.recollection.taken")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
		level.removeBlock(pos, false);
	}
}
