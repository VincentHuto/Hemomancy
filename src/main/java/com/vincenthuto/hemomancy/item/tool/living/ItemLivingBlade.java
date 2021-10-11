package com.vincenthuto.hemomancy.item.tool.living;

import java.util.List;
import java.util.function.Consumer;

import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.vincenthuto.hemomancy.render.item.RenderItemLivingBlade;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class ItemLivingBlade extends ItemLivingTool {

	public static String TAG_STATE = "state";

	public ItemLivingBlade(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getMainHandItem();
		if (stack.getItem() instanceof ItemLivingBlade) {
			CompoundTag compound = stack.getOrCreateTag();
			if (!compound.getBoolean(TAG_STATE)) {
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			} else {
				playerIn.playSound(SoundEvents.BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			}
			stack.setTag(compound);
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (stack.getOrCreateTag().getBoolean(TAG_STATE)) {
			attacker.heal(this.getAttackDamage() / 2);
			target.hurt(new DamageSource("Living Blade"), 20);
			if (!attacker.level.isClientSide) {
				Player playerIn = (Player) attacker;
				IBloodVolume playerVolume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				float damageMod = this.getAttackDamage() * 75f;
				if (playerVolume.getBloodVolume() > damageMod) {
					playerVolume.subtractBloodVolume(damageMod);
					PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
							new PacketBloodVolumeServer(playerVolume));
				} else {
					playerVolume.subtractBloodVolume(damageMod);
					PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
							new PacketBloodVolumeServer(playerVolume));
					stack.hurtAndBreak(getMaxDamage() + 10, attacker, (p_220017_1_) -> {
						p_220017_1_.broadcastBreakEvent(attacker.getUsedItemHand());
					});
				}

			}
		} else {
			target.hurt(ItemInit.bloodLoss, 5);

		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			if (stack.getTag().getBoolean(TAG_STATE)) {
				tooltip.add(new TranslatableComponent("State: Unleashed").withStyle(ChatFormatting.RED));
				tooltip.add(new TranslatableComponent("+20 Blood Damage").withStyle(ChatFormatting.RED));

			} else {
				tooltip.add(new TranslatableComponent("State: Tame").withStyle(ChatFormatting.GRAY));
				tooltip.add(new TranslatableComponent("+5 Blood Damage").withStyle(ChatFormatting.RED));

			}
		}
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		super.initializeClient(consumer);
		consumer.accept(RenderPropLivingBlade.INSTANCE);

	}
}

class RenderPropLivingBlade implements IItemRenderProperties {

	public static RenderPropLivingBlade INSTANCE = new RenderPropLivingBlade();

	@Override
	public Font getFont(ItemStack stack) {
		return Minecraft.getInstance().font;
	}

	@Override
	public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
		return new RenderItemLivingBlade(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}