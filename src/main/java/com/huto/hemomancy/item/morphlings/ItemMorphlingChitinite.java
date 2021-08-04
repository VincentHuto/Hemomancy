package com.huto.hemomancy.item.morphlings;

import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMorphlingChitinite extends ItemMorphling implements IMorphling {

	public ItemMorphlingChitinite(Properties prop) {
		super(prop);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public int getBloodCost() {
		return 20;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		BlockPos pos = playerIn.blockPosition();
		if (!worldIn.isClientSide) {
			ServerLevel sLevel = (ServerLevel) worldIn;
			for (int i = 0; i < 50; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(255 * worldIn.random.nextFloat(), 0, 0)),
						pos.getX() + worldIn.random.nextDouble(), pos.getY() + worldIn.random.nextDouble() + 1,
						pos.getZ() + worldIn.random.nextDouble(), 10, 0f, 0.2f, 0f, sLevel.random.nextInt(3) * 0.015f);
			}

		}
	}
}
