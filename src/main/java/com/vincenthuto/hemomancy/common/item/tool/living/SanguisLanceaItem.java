package com.vincenthuto.hemomancy.common.item.tool.living;

import java.util.List;
import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.render.item.living.SanguisLanceaItemRenderer;
import com.vincenthuto.hemomancy.common.entity.blood.DirectedBloodOrbEntity;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class SanguisLanceaItem extends LivingToolItem {

	public static String TAG_STATE = "state";

	public SanguisLanceaItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			if (stack.getTag().getBoolean(TAG_STATE)) {
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
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		/*
		 * if (stack.getOrCreateTag().getBoolean(TAG_STATE)) {
		 * attacker.heal(this.getAttackDamage() / 2); if (!attacker.world.isRemote) {
		 * Player playerIn = (Player) attacker; IBloodVolume playerVolume =
		 * playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
		 * .orElseThrow(NullPointerException::new); float damageMod =
		 * this.getAttackDamage() * 75f; if (playerVolume.getBloodVolume() > damageMod)
		 * { playerVolume.subtractBloodVolume(damageMod);
		 * PacketHandler.CHANNELBLOODVOLUME.send( PacketDistributor.PLAYER.with(() ->
		 * (ServerPlayer) playerIn), new
		 * PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
		 * playerVolume.getBloodVolume())); } else {
		 * playerVolume.subtractBloodVolume(damageMod);
		 * PacketHandler.CHANNELBLOODVOLUME.send( PacketDistributor.PLAYER.with(() ->
		 * (ServerPlayer) playerIn), new
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
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		super.initializeClient(consumer);
		consumer.accept(RenderPropSanguisLancea.INSTANCE);

	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		int i = this.getUseDuration(stack) - timeLeft;
		if (i >= 10) {
			if (entityLiving instanceof Player player) {
				ThrownTrident throwntrident = new ThrownTrident(worldIn, player, stack);
				throwntrident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
						2.5F + (float) 0 * 0.5F, 1.0F);
					throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

				worldIn.addFreshEntity(throwntrident);
				worldIn.playSound((Player) null, throwntrident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F,
						1.0F);

				Vec3 pos = player.position();
				PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f, player.level().dimension());
			}

			((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
		}
	}

	public void summonDirectedOrb(Level worldIn, Player playerIn) {
		DirectedBloodOrbEntity miss = new DirectedBloodOrbEntity(playerIn, false);
		Vector3 vec = Vector3.fromEntityCenter(playerIn);
		miss.setPos(vec.x - 0.5, vec.y + 1, vec.z - 0.5);
		miss.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.0F, 1.0F);
		worldIn.addFreshEntity(miss);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getMainHandItem();
		playerIn.startUsingItem(handIn);

		if (stack.getItem() instanceof SanguisLanceaItem) {
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
