package com.huto.hemomancy.item.tool.living;

import java.util.List;

import com.huto.hemomancy.entity.projectile.EntityBloodOrbDirected;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.render.item.RenderItemLivingSpear;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.math.Vector3;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ItemLivingSpear extends ItemLivingTool {

	public static String TAG_STATE = "state";

	public ItemLivingSpear(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn.setISTER(() -> RenderItemLivingSpear::new));
	}

	float count = 0.5f;

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getMainHandItem();
		playerIn.startUsingItem(handIn);

		if (stack.getItem() instanceof ItemLivingSpear) {
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
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		int i = this.getUseDuration(stack) - timeLeft;
		if (i >= 10) {
			if (entityLiving instanceof Player) {
				Player player = (Player) entityLiving;

				float f7 = player.yRot;
				float f = player.xRot;
				float f1 = -Mth.sin(f7 * ((float) Math.PI / 180F)) * Mth.cos(f * ((float) Math.PI / 180F));
				float f2 = -Mth.sin(f * ((float) Math.PI / 180F));
				float f3 = Mth.cos(f7 * ((float) Math.PI / 180F)) * Mth.cos(f * ((float) Math.PI / 180F));
				float f4 = Mth.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
				float f5 = 3.0F * ((1.0F + (float) 3) / 4.0F);
				f1 = f1 * (f5 / f4);
				f2 = f2 * (f5 / f4);
				f3 = f3 * (f5 / f4);
				player.push((double) f1, (double) f2, (double) f3);
				player.startAutoSpinAttack(20);
				if (player.isOnGround()) {
					player.move(MoverType.SELF, new Vec3(0.0D, (double) 1.1999999F, 0.0D));
				}
				SoundEvent soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
				worldIn.playSound((Player) null, player, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);

				Vec3 pos = player.position();
				PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f,
						(ResourceKey<Level>) player.level.dimension());

				/*
				 * IBloodVolume playerVolume =
				 * player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				 * .orElseThrow(NullPointerException::new); if (playerVolume.getBloodVolume() >
				 * 50f) { if (!worldIn.isRemote) { playerVolume.subtractBloodVolume(50f);
				 * PacketHandler.CHANNELBLOODVOLUME.send( PacketDistributor.PLAYER.with(() ->
				 * (ServerPlayer) player), new
				 * PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
				 * playerVolume.getBloodVolume()));
				 * 
				 * this.summonDirectedOrb(worldIn, player);
				 * 
				 * }
				 * 
				 * stack.damageItem(1, player, (p_220009_1_) -> {
				 * p_220009_1_.sendBreakAnimation(player.getActiveHand()); }); } else {
				 * player.sendStatusMessage(new
				 * TextComponent("Not enough blood to be shed"), true); }
				 */
			}

			((Player) entityLiving).awardStat(Stats.ITEM_USED.get(this));
		}
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

	public void summonDirectedOrb(Level worldIn, Player playerIn) {
		EntityBloodOrbDirected miss = new EntityBloodOrbDirected((Player) playerIn, false);
		Vector3 vec = Vector3.fromEntityCenter(playerIn);
		miss.setPos(vec.x - 0.5, vec.y + 1, vec.z - 0.5);
		miss.shootFromRotation(playerIn, playerIn.xRot, playerIn.yRot, 0.0F, 1.0F, 1.0F);
		worldIn.addFreshEntity(miss);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.SPEAR;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			if (stack.getTag().getBoolean(TAG_STATE)) {
				tooltip.add(new TranslatableComponent("State: Unleashed").withStyle(ChatFormatting.RED));
			} else {
				tooltip.add(new TranslatableComponent("State: Tame").withStyle(ChatFormatting.GRAY));
			}
		}
	}

}
