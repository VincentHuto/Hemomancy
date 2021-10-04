package com.vincenthuto.hemomancy.item.tool.living;

import java.util.HashSet;

import com.google.common.collect.Sets;
import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.PotionInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.vincenthuto.hutoslib.client.TextUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ItemLivingTool extends DiggerItem implements IDispellable {
	private static final HashSet<Block> EFFECTIVE_ON = Sets.newHashSet(Blocks.COBWEB);
	private float speed;

	public ItemLivingTool(float speedIn, float attackDamageIn, float attackSpeedIn, Tier tier, Properties builderIn) {
		super(attackDamageIn, attackSpeedIn, tier, BlockTags.WART_BLOCKS, builderIn);
	}
//	@Override
//	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
//		consumer.accept(new IItemRenderProperties() {
//			final BlockEntityWithoutLevelRenderer myRenderer = new RenderItemLivingSpear();
//
//			@Override
//			public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
//				return myRenderer;
//			}
//		});
//	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, Level world) {
		return 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getName(ItemStack stack) {
		return new TextComponent(
				TextUtils.stringToBloody(TextUtils.convertInitToLang(stack.getItem().getRegistryName().getPath())))
						.withStyle(ChatFormatting.DARK_RED);
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		return super.onEntitySwing(stack, entity);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (target.level.random.nextBoolean()) {
			if (attacker instanceof Player) {
				if (!attacker.level.isClientSide) {
					Player playerIn = (Player) attacker;
					IBloodVolume playerVolume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					float damageMod = this.getAttackDamage() * 25f;
					if (playerVolume.getBloodVolume() > damageMod) {
						playerVolume.subtractBloodVolume(damageMod);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
								new PacketBloodVolumeServer(playerVolume));
					} else {
						playerVolume.subtractBloodVolume(damageMod);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
								new PacketBloodVolumeServer(playerVolume));
						stack.hurtAndBreak(getMaxDamage() + 10, attacker, (p_220017_1_) -> {
							p_220017_1_.broadcastBreakEvent(attacker.getUsedItemHand());
						});
						Vec3 pos = playerIn.position();
						PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f,
								attacker.level.dimension());
					}

				}
			}
		}

		if (target.level.random.nextDouble() > 0.75) {
			attacker.addEffect(new MobEffectInstance(PotionInit.blood_rush.get(), 50, 2));
			target.addEffect(new MobEffectInstance(PotionInit.blood_loss.get(), 50, 2));
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState blockIn) {
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
