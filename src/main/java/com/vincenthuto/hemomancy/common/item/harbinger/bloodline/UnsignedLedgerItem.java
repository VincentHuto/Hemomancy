package com.vincenthuto.hemomancy.common.item.harbinger.bloodline;

import com.vincenthuto.hemomancy.client.screen.item.LedgerScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineDisbandHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnsignedLedgerItem extends Item {

	public static String TAG_STATE = "state";
	public static String TAG_BLOODLINE = "bloodline";

	public UnsignedLedgerItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
			if (!tag.getBoolean(TAG_STATE)) {
				tooltip.add(Component.literal("Unsigned").withStyle(ChatFormatting.GRAY));
			} else {
				if (tag.contains(TAG_BLOODLINE)) {
					Bloodline line = Bloodline.deserialize(tag.getCompound(TAG_BLOODLINE));
					tooltip.add(Component.literal("Signed with: " + line.getName())
							.withStyle(ChatFormatting.RED));
					tooltip.add(Component.literal("Members: " + line.getPlayerUUIDS().size())
							.withStyle(ChatFormatting.DARK_RED));
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
		if (stack.getItem() instanceof UnsignedLedgerItem) {
			CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(playerIn)
					.orElseThrow(NullPointerException::new);

			if (!compound.getBoolean(TAG_STATE)) {
				if (!worldIn.isClientSide) {
					playerIn.displayClientMessage(Component.literal(
							"Only the Bloodline Founding rite can write the first name into this ledger.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
				}
			} else {
				// Signed ledger: open the ledger GUI for lodge actions,
				// or if another player uses it, they join the bloodline.
				if (worldIn.isClientSide) {
					// Only open the GUI if the player's current synced bloodline still matches this ledger.
					Bloodline savedLine = Bloodline.deserialize(compound.getCompound(TAG_BLOODLINE));
					Bloodline currentLine = volume.getBloodLine();
					if (savedLine.isValid() && currentLine.isValid()
							&& currentLine.getBloodlineUUID().equals(savedLine.getBloodlineUUID())
							&& currentLine.hasMember(playerIn.getUUID())) {
						LedgerScreen.openScreen();
					}
					// If not a member yet, the server side handles joining below
				} else {
					Bloodline savedLine = Bloodline.deserialize(compound.getCompound(TAG_BLOODLINE));
					if (!savedLine.isValid()) {
						playerIn.displayClientMessage(
								Component.literal("This ledger's bloodline is corrupted")
										.withStyle(ChatFormatting.RED),
								true);
						return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
					}

					if (savedLine.hasMember(playerIn.getUUID())) {
						// Already a member — GUI opened on client side, no server action needed
					} else {
						// New player joins the bloodline
						ServerLevel overworld = ((ServerLevel) worldIn).getServer().overworld();
						BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
						Bloodline globalLine = savedData.addMember(savedLine.getBloodlineUUID(),
								playerIn.getUUID());

						if (globalLine != null) {
							volume.setBloodLine(globalLine);
							// Update the item tag with the latest bloodline state
							compound.put(TAG_BLOODLINE, globalLine.serialize());
							stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));

							playerIn.playSound(SoundEvents.BOOK_PUT, 0.40f, 1F);
							PacketHandler.sendToPlayer((ServerPlayer) playerIn, new BloodVolumeServerPacket(volume));
							playerIn.displayClientMessage(
									Component.literal("You have joined: " + globalLine.getName())
											.withStyle(ChatFormatting.DARK_RED),
									true);

							// Award bloodline milestone
							SkillPointGainEvents.onBloodlineJoined((ServerPlayer) playerIn);

							// Notify online bloodline members
							for (Player member : globalLine.getPlayers(worldIn)) {
								if (!member.getUUID().equals(playerIn.getUUID())) {
									member.displayClientMessage(
											Component.literal(playerIn.getName().getString()
													+ " has joined your bloodline!")
													.withStyle(ChatFormatting.DARK_RED),
											false);
								}
							}
						} else {
							playerIn.displayClientMessage(
									Component.literal("Bloodline not found in world data")
											.withStyle(ChatFormatting.RED),
									true);
						}
					}
				}
			}

		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}
}
