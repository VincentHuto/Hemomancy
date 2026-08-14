package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BloodyFlaskItem extends Item {

	double amount;

	public BloodyFlaskItem(Properties prop, double amount) {
		super(prop.stacksTo(16));
		this.amount = amount;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.literal("Used to quickly gain " + amount + " mL of blood."));
	}

	public double getAmount() {
		return amount;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (!worldIn.isClientSide) {
			IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(playerIn)
					.orElseThrow(NullPointerException::new);

			if (volume.isFull()) {
				playerIn.displayClientMessage(Component.literal("Blood volume is full."), true);
			} else {
				volume.fill(amount);
				for (int i = 0; i < 30; i++) {
					PacketHandler.sendBloodFlaskParticles(playerIn.position(), ParticleColor.BLOOD, 64f,
							(ServerLevel) worldIn);
				}
				PacketHandler.sendToPlayer((ServerPlayer) playerIn, new BloodVolumeServerPacket(volume));
				CheapBloodInfusionHelper.applySuccessfulInfusion(playerIn, stack.getItem());
				stack.shrink(1);
			}

		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

}
