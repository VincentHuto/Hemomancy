package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.TempleOathRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.tile.functional.MortalDisplayBlockEntity;
import com.vincenthuto.hemomancy.common.tile.functional.CardinalFocusBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The reusable rite focus. Before the player has a Living Staff, a temple
 * focus accepts one recipe-authored item as a disposable rite medium.
 */
public class CardinalFocusBlock extends Block implements EntityBlock {
	private static final int INITIATION_TICKS = 100;
	private static final int TEMPLE_LINK_RADIUS = 20;

	public CardinalFocusBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CardinalFocusBlockEntity(pos, state);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (!player.isShiftKeyDown() && isActivationTool(stack)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (level.isClientSide) return ItemInteractionResult.SUCCESS;
		CardinalFocusBlockEntity focus = level.getBlockEntity(pos) instanceof CardinalFocusBlockEntity found
				? found : null;
		if (focus == null) return ItemInteractionResult.FAIL;
		CardinalRiteSavedData rites = CardinalRiteSavedData.get((ServerLevel) level);
		if (player.isShiftKeyDown() && focus.hasMedium()) {
			if (rites.hasRiteAt(pos)) {
				player.displayClientMessage(Component.literal("The active rite holds its medium fast.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
				return ItemInteractionResult.SUCCESS;
			}
			ItemStack extracted = focus.extractMedium();
			if (!player.getInventory().add(extracted)) player.drop(extracted, false);
			return ItemInteractionResult.SUCCESS;
		}
		if (rites.hasRiteAt(pos)) {
			player.displayClientMessage(Component.literal("The active rite will not accept another medium.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return ItemInteractionResult.SUCCESS;
		}
		if (focus.hasMedium()) {
			player.displayClientMessage(Component.literal("The Cardinal Focus already holds a medium.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return ItemInteractionResult.SUCCESS;
		}
		if (!focus.insertMedium(player, stack)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		player.displayClientMessage(Component.literal("The item seats in the Cardinal Focus as a rite medium.")
				.withStyle(ChatFormatting.DARK_RED), false);

		ServerLevel server = (ServerLevel) level;
		boolean claimedHere = hasClaimedTempleHeart(server, pos, player.getUUID(), focus);
		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> volume.isActive()).orElse(false);
		if (bloodActive && !rites.hasActiveRite(player.getUUID())) {
			BloodCraftingKeyPressPacket.tryStartCardinalRite(player, pos,
					CardinalRiteActivationRules.Trigger.HEMATIC_MEDIUM_BLOCK_USE);
			return ItemInteractionResult.SUCCESS;
		}
		CardinalRiteRecipe initiation = CardinalRiteRecipe.getRiteByLocation(level,
				Hemomancy.rloc("cardinal_rite/sanguine_initiation"));
		if (initiation == null || !initiation.getMedium().test(focus.getMediumForMatching())) {
			player.displayClientMessage(Component.literal("This medium does not answer the temple's initiation rite.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return ItemInteractionResult.SUCCESS;
		}
		if (!TempleOathRules.canBeginInitiation(player.getHealth(), claimedHere,
				bloodActive, rites.hasActiveRite(player.getUUID()))) {
			player.displayClientMessage(Component.literal(initiationFailure(player, claimedHere,
							bloodActive, rites.hasActiveRite(player.getUUID())))
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return ItemInteractionResult.SUCCESS;
		}

		player.hurt(server.damageSources().magic(), 4.0F);
		rites.startRite(new ActiveCardinalRite(player.getUUID(), pos,
				Hemomancy.rloc("cardinal_rite/sanguine_initiation"),
				INITIATION_TICKS, 3));
		player.displayClientMessage(Component.literal(
				"The nugget catches in the focus. Stand within the temple ring and endure the calling.")
				.withStyle(ChatFormatting.DARK_RED), false);
		return ItemInteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
			Player player, BlockHitResult hit) {
		if (!player.isShiftKeyDown()) return InteractionResult.PASS;
		return useItemOn(ItemStack.EMPTY, state, level, pos, player, InteractionHand.MAIN_HAND, hit).result();
	}

	private static boolean isActivationTool(ItemStack stack) {
		return stack.is(ItemInit.living_staff.get()) || stack.is(ItemInit.sanguine_formation.get());
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
			boolean movedByPiston) {
		if (!state.is(newState.getBlock())
				&& level.getBlockEntity(pos) instanceof CardinalFocusBlockEntity focus) {
			ItemStack medium = focus.extractMedium();
			if (!medium.isEmpty()) {
				Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.75D,
						pos.getZ() + 0.5D, medium);
			}
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	private static boolean hasClaimedTempleHeart(ServerLevel level, BlockPos focusPos, java.util.UUID player,
			CardinalFocusBlockEntity focus) {
		if (focus != null && focus.getTempleDisplay() != null
				&& level.getBlockEntity(focus.getTempleDisplay()) instanceof MortalDisplayBlockEntity display) {
			return display.isClaimedBy(player);
		}
		for (BlockPos candidate : BlockPos.betweenClosed(
				focusPos.offset(-TEMPLE_LINK_RADIUS, -8, -TEMPLE_LINK_RADIUS),
				focusPos.offset(TEMPLE_LINK_RADIUS, 8, TEMPLE_LINK_RADIUS))) {
			if (level.getBlockEntity(candidate) instanceof MortalDisplayBlockEntity display
					&& display.isClaimedBy(player)) return true;
		}
		return false;
	}

	private static String initiationFailure(Player player, boolean claimedHere,
			boolean bloodActive, boolean riteActive) {
		if (!claimedHere) return "This focus answers only the heart claimed from its own temple.";
		if (bloodActive) return "Your blood has already answered initiation.";
		if (riteActive) return "Another cardinal rite already holds your attention.";
		if (player.getHealth() < 6.0F) return "The focus requires six health and will take four.";
		return "The focus remains still.";
	}
}
