package com.huto.hemomancy.item.morphlings;

import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.factory.GlowParticleFactory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMorphlingChitinite extends ItemMorphling implements IMorphling {

	public ItemMorphlingChitinite(Properties prop) {
		super(prop);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public int getBloodCost() {
		return 20;
	}

	@Override
	public void use(PlayerEntity playerIn, Hand handIn, ItemStack itemStack, World worldIn) {
		BlockPos pos = playerIn.getPosition();
		if (!worldIn.isRemote) {
			ServerWorld sWorld = (ServerWorld) worldIn;
			for (int i = 0; i < 50; i++) {
				sWorld.spawnParticle(
						GlowParticleFactory.createData(new ParticleColor(255 * worldIn.rand.nextFloat(), 0, 0)),
						pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
						pos.getZ() + random.nextDouble(), 10, 0f, 0.2f, 0f, sWorld.rand.nextInt(3) * 0.015f);
			}

		}
	}
}
