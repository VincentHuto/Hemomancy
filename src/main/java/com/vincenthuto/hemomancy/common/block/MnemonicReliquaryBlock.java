package com.vincenthuto.hemomancy.common.block;

import com.vincenthuto.hemomancy.common.menu.MnemonicReliquaryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class MnemonicReliquaryBlock extends Block implements MenuProvider {

	public MnemonicReliquaryBlock() {
		super(BlockBehaviour.Properties.of()
				.strength(2.0F, 6.0F)
				.sound(SoundType.STONE)
				.requiresCorrectToolForDrops()
				.noOcclusion());
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide) {
			NetworkHooks.openScreen((ServerPlayer) player, this, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("container.hemomancy.mnemonic_reliquary");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
		return new MnemonicReliquaryMenu(id, playerInv);
	}
}
