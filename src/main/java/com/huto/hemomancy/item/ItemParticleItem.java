package com.huto.hemomancy.item;

import java.util.List;
import java.util.stream.Collectors;

import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.particle.ParticleUtil;
import com.huto.hemomancy.render.item.RenderParticleItem;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ItemParticleItem extends Item {

	public ItemParticleItem(Properties prop) {
		super(prop.setISTER(() -> RenderParticleItem::new));
		prop.maxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (playerIn.world.isRemote) {
			return new ActionResult<>(ActionResultType.SUCCESS, stack);
		}

		List<MobEntity> targets = playerIn.world
				.getEntitiesWithinAABB(MobEntity.class, playerIn.getBoundingBox().grow(5.0)).stream()
				.filter(e -> e.canEntityBeSeen((Entity) playerIn)).collect(Collectors.toList());
		if (targets.size() <= 0) {
			return new ActionResult<>(ActionResultType.PASS, stack);
		}
		for (int i = 0; i < 3; ++i) {
			MobEntity target = (MobEntity) targets.get((int) Math.floor(Math.random() * (double) targets.size()));
			Vector3d translation = new Vector3d(0.0, 1, 0);
			Vector3d speedVec = new Vector3d(target.getPosition().getX(),
					(float) target.getPosition().getY() + target.getHeight() / 2.0f, target.getPosition().getZ());
			PacketHandler.sendLightningSpawn(playerIn.getPositionVec().add(translation), speedVec, 64.0f,
					ParticleUtil.BLUE, (RegistryKey<World>) playerIn.world.getDimensionKey());
			PacketHandler.sendLightningSpawn(playerIn.getPositionVec().add(translation), speedVec, 64.0f,
					ParticleUtil.RED, 2, 2, (RegistryKey<World>) playerIn.world.getDimensionKey());
			PacketHandler.sendLightningSpawn(playerIn.getPositionVec().add(translation), speedVec, 64.0f,
					ParticleUtil.RED, 2, 2, (RegistryKey<World>) playerIn.world.getDimensionKey());
			PacketHandler.sendLightningSpawn(playerIn.getPositionVec().add(translation), speedVec, 64.0f,
					ParticleUtil.RED, 2, 2, (RegistryKey<World>) playerIn.world.getDimensionKey());
			target.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) playerIn), 5.0f);

		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("Flashy"));
	}

}
