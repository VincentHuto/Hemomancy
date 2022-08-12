package com.vincenthuto.hemomancy.item.bloodline;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.Bloodline;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.BloodVolumeServerPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (stack.getItem() instanceof UnsignedLedgerItem) {
			String bloodLineName = playerIn.getName().getContents() + "'s Blood Line";
			ArrayList<UUID> uuids = new ArrayList<UUID>();
			// Created using the leaders UUID and the gametime at time of creation
			UUID bloodLineUUID = new UUID(playerIn.getUUID().getMostSignificantBits(), playerIn.level.getGameTime());
			Bloodline playerLine = new Bloodline(bloodLineName, playerIn.getUUID(), bloodLineUUID, uuids);
			CompoundTag compound = stack.getOrCreateTag();
			IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			if (!compound.getBoolean(TAG_STATE)) {
				playerIn.playSound(SoundEvents.BOOK_PUT, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
				compound.put(TAG_BLOODLINE, playerLine.serialize());
				stack.setTag(compound);
				if (!worldIn.isClientSide) {
					volume.setBloodLine(playerLine);
					PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
							new BloodVolumeServerPacket(volume));
				}
			} else {
				if (!worldIn.isClientSide) {
					Bloodline savedLine = Bloodline.deserialize(compound.getCompound(TAG_BLOODLINE));
					if (volume.getBloodLine().getName().equals(savedLine.getName())) {
						playerIn.displayClientMessage(new TextComponent("Already in this bloodline"), true);
					} else {
						volume.setBloodLine(Bloodline.deserialize(compound.getCompound(TAG_BLOODLINE)));
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
								new BloodVolumeServerPacket(volume));
					}
				}
			}

		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			if (!stack.getTag().getBoolean(TAG_STATE)) {
				tooltip.add(new TranslatableComponent("Unsigned").withStyle(ChatFormatting.GRAY));
			} else {
				if (stack.getTag().contains(TAG_BLOODLINE)) {
					tooltip.add(new TranslatableComponent("Signed with: "
							+ Bloodline.deserialize(stack.getTag().getCompound(TAG_BLOODLINE)).getName())
									.withStyle(ChatFormatting.RED));
				}
			}
		}
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.hasTag() && stack.getTag().getBoolean(TAG_STATE);
	}
}
