package com.vincenthuto.hemomancy.common.item.shared;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class TendonLineItem extends Item {
	private static final String TAG_HAS_ANCHOR = "TendonLineHasAnchor";
	private static final String TAG_ANCHOR_X = "TendonLineAnchorX";
	private static final String TAG_ANCHOR_Y = "TendonLineAnchorY";
	private static final String TAG_ANCHOR_Z = "TendonLineAnchorZ";
	private static final String TAG_ANCHOR_FACE = "TendonLineAnchorFace";
	private static final String TAG_ANCHOR_DIMENSION = "TendonLineAnchorDimension";

	public TendonLineItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}

		ItemStack stack = context.getItemInHand();
		if (player.isShiftKeyDown()) {
			clearAnchor(stack);
			if (!level.isClientSide) {
				player.displayClientMessage(Component.translatable("item.hemomancy.tendon_line.clear")
						.withStyle(ChatFormatting.GRAY), true);
				level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOOL_BREAK,
						SoundSource.PLAYERS, 0.45F, 0.85F);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}

		BlockPos pos = context.getClickedPos();
		Direction face = context.getClickedFace();
		BlockState state = level.getBlockState(pos);
		if (!state.isFaceSturdy(level, pos, face)) {
			if (!level.isClientSide) {
				player.displayClientMessage(Component.translatable("item.hemomancy.tendon_line.no_anchor")
						.withStyle(ChatFormatting.DARK_RED), true);
			}
			return InteractionResult.FAIL;
		}

		double distance = player.position().distanceTo(Vec3.atCenterOf(pos));
		if (!TendonLineRules.canAnchor(distance)) {
			if (!level.isClientSide) {
				player.displayClientMessage(Component.translatable("item.hemomancy.tendon_line.too_far")
						.withStyle(ChatFormatting.RED), true);
			}
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide) {
			storeAnchor(stack, level, pos, face);
			player.getCooldowns().addCooldown(this, TendonLineRules.USE_COOLDOWN_TICKS);
			player.displayClientMessage(Component.translatable("item.hemomancy.tendon_line.anchor",
					pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.DARK_RED), true);
			level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 0.65F, 0.75F);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		Optional<Anchor> anchor = readAnchor(stack);
		if (anchor.isEmpty()) {
			if (!level.isClientSide) {
				player.displayClientMessage(Component.translatable("item.hemomancy.tendon_line.no_stored_anchor")
						.withStyle(ChatFormatting.GRAY), true);
			}
			return InteractionResultHolder.fail(stack);
		}
		if (!isSameDimension(level, anchor.get())) {
			if (!level.isClientSide) {
				clearAnchor(stack);
				player.displayClientMessage(Component.translatable("item.hemomancy.tendon_line.lost")
						.withStyle(ChatFormatting.GRAY), true);
			}
			return InteractionResultHolder.fail(stack);
		}

		player.startUsingItem(hand);
		player.getCooldowns().addCooldown(this, TendonLineRules.USE_COOLDOWN_TICKS);
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResultHolder.consume(stack);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return TendonLineRules.USE_DURATION_TICKS;
	}

	@Override
	public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
		if (level.isClientSide || !(living instanceof Player player)) {
			return;
		}

		Optional<Anchor> anchor = readAnchor(stack);
		if (anchor.isEmpty() || !isSameDimension(level, anchor.get())) {
			clearAndStop(player, stack);
			return;
		}

		Anchor stored = anchor.get();
		BlockState state = level.getBlockState(stored.pos());
		if (!state.isFaceSturdy(level, stored.pos(), stored.face())) {
			clearAndStop(player, stack);
			return;
		}

		Vec3 target = stored.target();
		Vec3 playerCenter = player.position().add(0.0D, player.getBbHeight() * 0.55D, 0.0D);
		double distance = playerCenter.distanceTo(target);
		if (TendonLineRules.shouldDetach(distance) || TendonLineRules.hasArrived(distance)) {
			clearAndStop(player, stack);
			return;
		}

		TendonLineRules.Motion pull = TendonLineRules.pullMotion(playerCenter.x, playerCenter.y, playerCenter.z,
				target.x, target.y, target.z);
		Vec3 existing = player.getDeltaMovement().scale(TendonLineRules.EXISTING_MOTION_BLEND);
		player.setDeltaMovement(existing.add(pull.x(), pull.y(), pull.z()));
		player.hasImpulse = true;
		player.hurtMarked = true;
		player.fallDistance = 0.0F;
		player.resetFallDistance();
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return readAnchor(stack).isPresent();
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		readAnchor(stack).ifPresent(anchor -> tooltip.add(Component.translatable("item.hemomancy.tendon_line.tooltip.anchor",
				anchor.pos().getX(), anchor.pos().getY(), anchor.pos().getZ()).withStyle(ChatFormatting.DARK_RED)));
	}

	private static void storeAnchor(ItemStack stack, Level level, BlockPos pos, Direction face) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putBoolean(TAG_HAS_ANCHOR, true);
		tag.putInt(TAG_ANCHOR_X, pos.getX());
		tag.putInt(TAG_ANCHOR_Y, pos.getY());
		tag.putInt(TAG_ANCHOR_Z, pos.getZ());
		tag.putString(TAG_ANCHOR_FACE, face.getName());
		tag.putString(TAG_ANCHOR_DIMENSION, level.dimension().location().toString());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static Optional<Anchor> readAnchor(ItemStack stack) {
		CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
		if (customData == null) {
			return Optional.empty();
		}
		CompoundTag tag = customData.copyTag();
		if (!tag.getBoolean(TAG_HAS_ANCHOR)) {
			return Optional.empty();
		}
		Direction face = Direction.byName(tag.getString(TAG_ANCHOR_FACE));
		if (face == null) {
			face = Direction.UP;
		}
		return Optional.of(new Anchor(new BlockPos(tag.getInt(TAG_ANCHOR_X), tag.getInt(TAG_ANCHOR_Y),
				tag.getInt(TAG_ANCHOR_Z)), face, tag.getString(TAG_ANCHOR_DIMENSION)));
	}

	private static void clearAnchor(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putBoolean(TAG_HAS_ANCHOR, false);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	private static boolean isSameDimension(Level level, Anchor anchor) {
		return level.dimension().location().toString().equals(anchor.dimension());
	}

	private static void clearAndStop(Player player, ItemStack stack) {
		clearAnchor(stack);
		player.stopUsingItem();
	}

	public record Anchor(BlockPos pos, Direction face, String dimension) {
		public Vec3 target() {
			return Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.58D));
		}
	}
}
