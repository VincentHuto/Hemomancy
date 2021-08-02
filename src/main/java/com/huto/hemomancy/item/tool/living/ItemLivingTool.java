package com.huto.hemomancy.item.tool.living;

import java.util.Set;

import com.google.common.collect.Sets;
import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.hutoslib.client.TextUtils;
import com.hutoslib.client.particle.util.ParticleColor;
import com.mojang.math.Vector3d;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ItemLivingTool extends DiggerItem implements IDispellable {
	private static final Set<Block> EFFECTIVE_ON = Sets.newHashSet(Blocks.COBWEB);
	private float speed;

	public ItemLivingTool(float speedIn, float attackDamageIn, float attackSpeedIn, Tier tier,
			Properties builderIn) {
		super(attackDamageIn, attackSpeedIn, tier, EFFECTIVE_ON, builderIn);
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getDisplayName(ItemStack stack) {
		return new TextComponent(TextUtils
				.stringToBloody(TextUtils.convertInitToLang(stack.getItem().getRegistryName().getPath())))
						.mergeStyle(TextFormatting.DARK_RED);
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		return super.onEntitySwing(stack, entity);
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (target.world.rand.nextBoolean()) {
			if (attacker instanceof PlayerEntity) {
				if (!attacker.level.isClientSide) {
					PlayerEntity playerIn = (PlayerEntity) attacker;
					IBloodVolume playerVolume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					float damageMod = this.getAttackDamage() * 25f;
					if (playerVolume.getBloodVolume() > damageMod) {
						playerVolume.subtractBloodVolume(damageMod);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
								new PacketBloodVolumeServer(playerVolume));
					} else {
						playerVolume.subtractBloodVolume(damageMod);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn),
								new PacketBloodVolumeServer(playerVolume));
						stack.damageItem(getMaxDamage() + 10, attacker, (p_220017_1_) -> {
							p_220017_1_.sendBreakAnimation(attacker.getActiveHand());
						});
						Vector3d pos = playerIn.getPositionVec();
						PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f,
								(RegistryKey<World>) attacker.world.getDimensionKey());
					}

				}
			}
		}

		if (target.world.rand.nextDouble() > 0.75) {
			attacker.addPotionEffect(new EffectInstance(PotionInit.blood_rush.get(), 50, 2));
			target.addPotionEffect(new EffectInstance(PotionInit.blood_loss.get(), 50, 2));
		}
		return super.hitEntity(stack, target, attacker);
	}

	@Override
	public boolean canHarvestBlock(BlockState blockIn) {
		if (EFFECTIVE_ON.contains(blockIn.getBlock())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (EFFECTIVE_ON.contains(state.getBlock())) {
			return speed;
		} else {
			return 0.5f;
		}
	}
}
