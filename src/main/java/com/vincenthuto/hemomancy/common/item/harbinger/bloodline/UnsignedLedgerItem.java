package com.vincenthuto.hemomancy.common.item.harbinger.bloodline;

import com.vincenthuto.hemomancy.common.util.ClientBridge;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineDisbandHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineMembership;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class UnsignedLedgerItem extends Item {

	public static String TAG_STATE = "state";
	public static String TAG_BLOODLINE = "bloodline";
	/** Overworld game time the ledger was written; a ledger with none reads as written at 0. */
	public static String TAG_MINTED = "minted";

	public UnsignedLedgerItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	/** Writes {@code line} onto {@code ledger} as a signed copy, stamped {@code mintedAt}. */
	public static void sign(ItemStack ledger, Bloodline line, long mintedAt) {
		CompoundTag compound = ledger.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		compound.putBoolean(TAG_STATE, true);
		compound.put(TAG_BLOODLINE, line.serialize());
		compound.putLong(TAG_MINTED, mintedAt);
		ledger.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
			if (!tag.getBoolean(TAG_STATE)) {
				tooltip.add(Component.translatable("tooltip.hemomancy.unsigned_ledger.unsigned").withStyle(ChatFormatting.GRAY));
			} else {
				if (tag.contains(TAG_BLOODLINE)) {
					Bloodline line = Bloodline.deserialize(tag.getCompound(TAG_BLOODLINE));
					tooltip.add(Component.translatable("tooltip.hemomancy.unsigned_ledger.signed_with_s", line.displayName())
							.withStyle(ChatFormatting.RED));
					tooltip.add(HemoText.translatable("tooltip.hemomancy.unsigned_ledger.members",
							line.getPlayerUUIDS().size()).withStyle(ChatFormatting.DARK_RED));
				}
			}
		}
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.has(DataComponents.CUSTOM_DATA) && stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(TAG_STATE);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);
		if (level.isClientSide || !(entity instanceof ServerPlayer serverPlayer) || stack.isEmpty()) {
			return;
		}
		BloodlineDisbandHelper.burnInvalidLedgerIfPresent(serverPlayer, stack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (!worldIn.isClientSide && UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(playerIn))) {
			playerIn.displayClientMessage(Component.translatable("message.hemomancy.unsigned_ledger_item.purification_bars_new_bloodline_bonds").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
			return InteractionResultHolder.fail(stack);
		}
		if (stack.getItem() instanceof UnsignedLedgerItem) {
			CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(playerIn);

			if (!compound.getBoolean(TAG_STATE)) {
				if (!worldIn.isClientSide) {
					playerIn.displayClientMessage(Component.translatable("message.hemomancy.unsigned_ledger_item.only_the_bloodline_founding_rite_can").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
				}
			} else if (worldIn.isClientSide) {
				// Only open the GUI if the player's current synced bloodline still matches this ledger.
				Bloodline savedLine = Bloodline.deserialize(compound.getCompound(TAG_BLOODLINE));
				Bloodline currentLine = volume.getBloodLine();
				if (savedLine.isValid() && currentLine.isValid()
						&& currentLine.getBloodlineUUID().equals(savedLine.getBloodlineUUID())
						&& currentLine.hasMember(playerIn.getUUID())) {
					ClientBridge.get().openLedger();
				}
			} else {
				ServerPlayer player = (ServerPlayer) playerIn;
				Bloodline savedLine = Bloodline.deserialize(compound.getCompound(TAG_BLOODLINE));
				if (!savedLine.isValid()) {
					playerIn.displayClientMessage(
							Component.translatable("message.hemomancy.unsigned_ledger_item.this_ledger_s_bloodline_is_corrupted").withStyle(ChatFormatting.RED),
							true);
					return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
				}
				if (BloodlineDisbandHelper.burnInvalidLedgerIfPresent(player, stack)) {
					return InteractionResultHolder.fail(stack);
				}

				// Membership is read from world data, never from the copy written on the item (PT-37).
				BloodlineSavedData savedData = BloodlineSavedData.get(player.server.overworld());
				Bloodline globalLine = savedData.getBloodline(savedLine.getBloodlineUUID());
				if (globalLine == null) {
					playerIn.displayClientMessage(
							Component.translatable("message.hemomancy.unsigned_ledger_item.bloodline_not_found_in_world_data").withStyle(ChatFormatting.RED),
							true);
				} else if (globalLine.hasMember(player.getUUID())) {
					BloodlineMembership.sendPetitions(player);
				} else {
					Bloodline current = savedData.getBloodlineForPlayer(player.getUUID());
					if (current != null && current.isValid()) {
						playerIn.displayClientMessage(
								HemoText.translatable("message.hemomancy.unsigned_ledger_item.you_already_belong_to_a_bloodline",
										current.displayName()).withStyle(ChatFormatting.DARK_RED),
								true);
						return InteractionResultHolder.fail(stack);
					}
					BloodlineMembership.petition(player, globalLine);
				}
			}
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}
}
