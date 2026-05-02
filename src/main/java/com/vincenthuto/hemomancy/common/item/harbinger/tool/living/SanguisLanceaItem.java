package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;

import java.util.List;

import com.vincenthuto.hemomancy.client.render.item.hematic.SanguisLanceaItemRenderer;
import com.vincenthuto.hemomancy.common.entity.projectile.SanguisLanceaEntity;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class SanguisLanceaItem extends LivingToolItem implements HemoClientItemExtensionsProvider {

	public static String TAG_STATE = "state";

	public SanguisLanceaItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			if (stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(TAG_STATE)) {
				tooltip.add(Component.literal("State: Unleashed").withStyle(ChatFormatting.RED));
			} else {
				tooltip.add(Component.literal("State: Tame").withStyle(ChatFormatting.GRAY));
			}
		}
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.SPEAR;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000 / 2;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		/*
		 * if (stack.getOrCreateTag().getBoolean(TAG_STATE)) {
		 * attacker.heal(this.getAttackDamage() / 2); if (!attacker.world.isRemote) {
		 * Player playerIn = (Player) attacker; IBloodVolume playerVolume =
		 * HemoCapabilityAccess.getBloodVolume(playerIn)
		 * .orElseThrow(NullPointerException::new); float damageMod =
		 * this.getAttackDamage() * 75f; if (playerVolume.getBloodVolume() > damageMod)
		 * { playerVolume.subtractBloodVolume(damageMod);
		 * PacketHandler.sendToPlayer(* (ServerPlayer) playerIn, new
		 * PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
		 * playerVolume.getBloodVolume())); } else {
		 * playerVolume.subtractBloodVolume(damageMod);
		 * PacketHandler.sendToPlayer(* (ServerPlayer) playerIn, new
		 * PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
		 * playerVolume.getBloodVolume())); stack.damageItem(getMaxDamage() + 10,
		 * attacker, (p_220017_1_) -> {
		 * p_220017_1_.sendBreakAnimation(attacker.getActiveHand()); }); }
		 *
		 * } }
		 */
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return RenderPropSanguisLancea.INSTANCE;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		int i = this.getUseDuration(stack, entityLiving) - timeLeft;
		if (i >= 10) {
			if (entityLiving instanceof Player player) {
				SanguisLanceaEntity throwntrident = new SanguisLanceaEntity(worldIn, player, stack);
				throwntrident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
						2.5F + (float) 0 * 0.5F, 1.0F);
					throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
				worldIn.addFreshEntity(throwntrident);
				worldIn.playSound((Player) null, throwntrident, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F,
						1.0F);

				Vec3 pos = player.position();
				if (player.level() instanceof ServerLevel serverLevel) {
					PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f, serverLevel);
				}
			}

			((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		playerIn.startUsingItem(handIn);

		if (stack.getItem() instanceof SanguisLanceaItem) {
			CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (!compound.getBoolean(TAG_STATE)) {
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			} else {
				playerIn.playSound(SoundEvents.BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			}
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
		}
        return InteractionResultHolder.consume(stack);
	}
}

class RenderPropSanguisLancea implements IClientItemExtensions {

	public static RenderPropSanguisLancea INSTANCE = new RenderPropSanguisLancea();

	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new SanguisLanceaItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
