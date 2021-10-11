package com.vincenthuto.hemomancy.effect;

import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.vincenthuto.hemomancy.util.ModEntityPredicates;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class BloodLossEffect extends MobEffect {

	public BloodLossEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);

	}

	@Override
	public Component getDisplayName() {
		return new TextComponent("Blood Loss");
	}

	@Override
	public boolean isBeneficial() {
		return false;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		// Entities without blood cant lose any...
		if (entity instanceof Player) {
			if (!entity.level.isClientSide) {
				Player playerIn = (Player) entity;
				IBloodVolume playerVolume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				playerVolume.subtractBloodVolume(0.5f * amplifier);
				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
						new PacketBloodVolumeServer(playerVolume));
			}
		} else if (!ModEntityPredicates.NOBLOOD.test(entity)) {
			entity.hurt(ItemInit.bloodLoss, 0.5F);
			if (entity.level.random.nextDouble() > 0.999) {
				if (!entity.level.isClientSide) {
					ServerLevel sLevel = (ServerLevel) entity.level;
					sLevel.addFreshEntity(new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(),
							new ItemStack(ItemInit.sanguine_formation.get())));
				}
			}
		}
	}

}