package com.huto.hemomancy.item.tool.living;

import java.util.List;

import com.huto.hemomancy.entity.projectile.EntityBloodOrbDirected;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.render.item.RenderItemLivingSpear;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.math.Vector3;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ItemLivingSpear extends ItemLivingTool {

	public static String TAG_STATE = "state";

	public ItemLivingSpear(float speedIn, float attackDamageIn, IItemTier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn.setISTER(() -> RenderItemLivingSpear::new));
	}

	float count = 0.5f;

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItemMainhand();
		playerIn.setActiveHand(handIn);

		if (stack.getItem() instanceof ItemLivingSpear) {
			CompoundNBT compound = stack.getOrCreateTag();
			if (!compound.getBoolean(TAG_STATE)) {
				playerIn.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			} else {
				playerIn.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			}
			stack.setTag(compound);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		int i = this.getUseDuration(stack) - timeLeft;
		if (i >= 10) {
			if (entityLiving instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) entityLiving;

				float f7 = player.rotationYaw;
				float f = player.rotationPitch;
				float f1 = -MathHelper.sin(f7 * ((float) Math.PI / 180F))
						* MathHelper.cos(f * ((float) Math.PI / 180F));
				float f2 = -MathHelper.sin(f * ((float) Math.PI / 180F));
				float f3 = MathHelper.cos(f7 * ((float) Math.PI / 180F)) * MathHelper.cos(f * ((float) Math.PI / 180F));
				float f4 = MathHelper.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
				float f5 = 3.0F * ((1.0F + (float) 3) / 4.0F);
				f1 = f1 * (f5 / f4);
				f2 = f2 * (f5 / f4);
				f3 = f3 * (f5 / f4);
				player.addVelocity((double) f1, (double) f2, (double) f3);
				player.startSpinAttack(20);
				if (player.isOnGround()) {
					player.move(MoverType.SELF, new Vector3d(0.0D, (double) 1.1999999F, 0.0D));
				}
				SoundEvent soundevent = SoundEvents.ITEM_TRIDENT_RIPTIDE_1;
				worldIn.playMovingSound((PlayerEntity) null, player, soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);

				Vector3d pos = player.getPositionVec();
				PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f,
						(RegistryKey<World>) player.world.getDimensionKey());

				/*
				 * IBloodVolume playerVolume =
				 * player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				 * .orElseThrow(NullPointerException::new); if (playerVolume.getBloodVolume() >
				 * 50f) { if (!worldIn.isRemote) { playerVolume.subtractBloodVolume(50f);
				 * PacketHandler.CHANNELBLOODVOLUME.send( PacketDistributor.PLAYER.with(() ->
				 * (ServerPlayerEntity) player), new
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
				 * StringTextComponent("Not enough blood to be shed"), true); }
				 */
			}

			((PlayerEntity) entityLiving).addStat(Stats.ITEM_USED.get(this));
		}
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hitEntity(stack, target, attacker);
		/*
		 * if (stack.getOrCreateTag().getBoolean(TAG_STATE)) {
		 * attacker.heal(this.getAttackDamage() / 2); if (!attacker.world.isRemote) {
		 * PlayerEntity playerIn = (PlayerEntity) attacker; IBloodVolume playerVolume =
		 * playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
		 * .orElseThrow(NullPointerException::new); float damageMod =
		 * this.getAttackDamage() * 75f; if (playerVolume.getBloodVolume() > damageMod)
		 * { playerVolume.subtractBloodVolume(damageMod);
		 * PacketHandler.CHANNELBLOODVOLUME.send( PacketDistributor.PLAYER.with(() ->
		 * (ServerPlayerEntity) playerIn), new
		 * PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
		 * playerVolume.getBloodVolume())); } else {
		 * playerVolume.subtractBloodVolume(damageMod);
		 * PacketHandler.CHANNELBLOODVOLUME.send( PacketDistributor.PLAYER.with(() ->
		 * (ServerPlayerEntity) playerIn), new
		 * PacketBloodVolumeServer(playerVolume.getMaxBloodVolume(),
		 * playerVolume.getBloodVolume())); stack.damageItem(getMaxDamage() + 10,
		 * attacker, (p_220017_1_) -> {
		 * p_220017_1_.sendBreakAnimation(attacker.getActiveHand()); }); }
		 * 
		 * } }
		 */
		return super.hitEntity(stack, target, attacker);
	}

	public void summonDirectedOrb(World worldIn, PlayerEntity playerIn) {
		EntityBloodOrbDirected miss = new EntityBloodOrbDirected((PlayerEntity) playerIn, false);
		Vector3 vec = Vector3.fromEntityCenter(playerIn);
		miss.setPosition(vec.x - 0.5, vec.y + 1, vec.z - 0.5);
		miss.setDirectionAndMovement(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.0F, 1.0F);
		worldIn.addEntity(miss);
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.SPEAR;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			if (stack.getTag().getBoolean(TAG_STATE)) {
				tooltip.add(new TranslationTextComponent("State: Unleashed").mergeStyle(TextFormatting.RED));
			} else {
				tooltip.add(new TranslationTextComponent("State: Tame").mergeStyle(TextFormatting.GRAY));
			}
		}
	}

}
