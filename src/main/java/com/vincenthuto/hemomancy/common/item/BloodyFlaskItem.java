package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class BloodyFlaskItem extends Item {

	float amount;

	public BloodyFlaskItem(Properties prop, float amount) {
		super(prop.stacksTo(16));
		this.amount = amount;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		// TODO Auto-generated method stub
		tooltip.add(Component.literal("Used to Quickly Gain " + amount + "ml of Blood"));
	}

	public float getAmount() {
		return amount;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (!worldIn.isClientSide) {
			IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);

			if (volume.isFull()) {
				playerIn.displayClientMessage(Component.literal("Blood Volume Full"), true);
			} else {
				volume.fill(amount);
				for (int i = 0; i < 30; i++) {
					PacketHandler.sendBloodFlaskParticles(playerIn.position(), ParticleColor.BLOOD, 64f,
							worldIn.dimension());
				}
				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
						new BloodVolumeServerPacket(volume));
				stack.shrink(1);
				playerIn.broadcastBreakEvent(handIn);
			}

		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

}