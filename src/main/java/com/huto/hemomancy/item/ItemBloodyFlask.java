package com.huto.hemomancy.item;

import java.util.List;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.hutoslib.client.particle.ParticleColor;
import com.hutoslib.client.particles.factory.GlowParticleFactory;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

public class ItemBloodyFlask extends Item {

	float amount;

	public ItemBloodyFlask(Properties prop, float amount) {
		super(prop.maxStackSize(16));
		this.amount = amount;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		BlockPos pos = playerIn.getPosition();
		if (!worldIn.isRemote) {
			IBloodVolume volume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			float curr = volume.getBloodVolume();
			if (curr >= volume.getMaxBloodVolume()) {
				playerIn.sendStatusMessage(new StringTextComponent("Blood Volume Full"), true);
			} else {
				volume.addBloodVolume(amount);
				ServerWorld sWorld = (ServerWorld) worldIn;
				for (int i = 0; i < 30; i++) {
					sWorld.spawnParticle(
							GlowParticleFactory.createData(new ParticleColor(255 * worldIn.rand.nextFloat(), 0, 0)),
							pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
							pos.getZ() + random.nextDouble(), 1, 0f, 0.2f, 0f, sWorld.rand.nextInt(3) * 0.015f);
				}
				PacketHandler.CHANNELBLOODVOLUME.send(
						PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
						new PacketBloodVolumeServer(volume));
				stack.shrink(1);
				playerIn.sendBreakAnimation(handIn);
			}
		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		tooltip.add(new StringTextComponent("Used to Quickly Gain " + amount + "ml of Blood"));

	}

}