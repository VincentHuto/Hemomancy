package com.huto.hemomancy.effect;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.huto.hemomancy.util.ModEntityPredicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

public class BloodLossEffect extends Effect {

	public BloodLossEffect(EffectType typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);

	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent("Blood Loss");
	}

	@Override
	public boolean isBeneficial() {
		return false;
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}

	@Override
	public void affectEntity(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn, int amplifier,
			double health) {
		super.affectEntity(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public void performEffect(LivingEntity entity, int amplifier) {
		// Entities without blood cant lose any...
		if (entity instanceof PlayerEntity) {
			if (!entity.world.isRemote) {
				PlayerEntity playerIn = (PlayerEntity) entity;
				IBloodVolume playerVolume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				playerVolume.subtractBloodVolume(0.5f * amplifier);
				PacketHandler.CHANNELBLOODVOLUME.send(
						PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
						new PacketBloodVolumeServer(playerVolume));
			}
		} else if (!ModEntityPredicates.NOBLOOD.test(entity)) {
			entity.attackEntityFrom(ItemInit.bloodLoss, 0.5F);
			if (entity.world.rand.nextDouble() > 0.999) {
				if (!entity.world.isRemote) {
					ServerWorld sWorld = (ServerWorld) entity.world;
					sWorld.addEntity(new ItemEntity(entity.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(),
							new ItemStack(ItemInit.sanguine_formation.get())));
				}
			}
		}
	}

}
