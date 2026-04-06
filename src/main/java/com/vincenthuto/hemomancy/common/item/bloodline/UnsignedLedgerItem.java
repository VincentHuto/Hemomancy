package com.vincenthuto.hemomancy.common.item.bloodline;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;

import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class UnsignedLedgerItem extends Item {

	public static String TAG_STATE = "state";
	public static String TAG_BLOODLINE = "bloodline";

	public UnsignedLedgerItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			if (!stack.getTag().getBoolean(TAG_STATE)) {
				tooltip.add(Component.literal("Unsigned").withStyle(ChatFormatting.GRAY));
			} else {
				if (stack.getTag().contains(TAG_BLOODLINE)) {
					Bloodline line = Bloodline.deserialize(stack.getTag().getCompound(TAG_BLOODLINE));
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
		return stack.hasTag() && stack.getTag().getBoolean(TAG_STATE);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (stack.getItem() instanceof UnsignedLedgerItem) {
			CompoundTag compound = stack.getOrCreateTag();
			IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);

			if (!compound.getBoolean(TAG_STATE)) {
				// First use: leader signs the ledger, creating a new bloodline
				String bloodLineName = playerIn.getName().getString() + "'s Blood Line";
				ArrayList<UUID> uuids = new ArrayList<>();
				UUID bloodLineUUID = new UUID(playerIn.getUUID().getMostSignificantBits(),
						playerIn.level().getGameTime());
				Bloodline playerLine = new Bloodline(bloodLineName, playerIn.getUUID(), bloodLineUUID, uuids);

				playerIn.playSound(SoundEvents.BOOK_PUT, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, true);
				compound.put(TAG_BLOODLINE, playerLine.serialize());
				stack.setTag(compound);

				if (!worldIn.isClientSide) {
					volume.setBloodLine(playerLine);
					// Register in world-level saved data
					ServerLevel overworld = ((ServerLevel) worldIn).getServer().overworld();
					BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
					savedData.registerBloodline(playerLine);

					PacketHandler.CHANNELBLOODVOLUME.send(
							PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
							new BloodVolumeServerPacket(volume));
					playerIn.displayClientMessage(
							Component.literal("You have founded: " + playerLine.getName())
									.withStyle(ChatFormatting.DARK_RED),
							true);
				}
			} else {
				// Second use: another player signs to join the bloodline
				if (!worldIn.isClientSide) {
					Bloodline savedLine = Bloodline.deserialize(compound.getCompound(TAG_BLOODLINE));
					if (!savedLine.isValid()) {
						playerIn.displayClientMessage(
								Component.literal("This ledger's bloodline is corrupted")
										.withStyle(ChatFormatting.RED),
								true);
						return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
					}

					if (savedLine.hasMember(playerIn.getUUID())) {
						playerIn.displayClientMessage(Component.literal("Already in this bloodline"), true);
					} else {
						// Add to global saved data
						ServerLevel overworld = ((ServerLevel) worldIn).getServer().overworld();
						BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
						Bloodline globalLine = savedData.addMember(savedLine.getBloodlineUUID(),
								playerIn.getUUID());

						if (globalLine != null) {
							volume.setBloodLine(globalLine);
							// Update the item tag with the latest bloodline state
							compound.put(TAG_BLOODLINE, globalLine.serialize());
							stack.setTag(compound);

							playerIn.playSound(SoundEvents.BOOK_PUT, 0.40f, 1F);
							PacketHandler.CHANNELBLOODVOLUME.send(
									PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
									new BloodVolumeServerPacket(volume));
							playerIn.displayClientMessage(
									Component.literal("You have joined: " + globalLine.getName())
											.withStyle(ChatFormatting.DARK_RED),
									true);

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
