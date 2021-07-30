package com.huto.hemomancy.item.tool.living;

import java.util.List;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.huto.hemomancy.render.item.RenderItemLivingAxe;
import com.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import net.minecraft.world.item.Item.Properties;

public class ItemLivingAxe extends ItemLivingTool {

	public static String TAG_STATE = "state";

	public ItemLivingAxe(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn.setISTER(() -> RenderItemLivingAxe::new));
	}

	float count = 0.5f;

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (stack.getItem() instanceof ItemLivingAxe) {
				CompoundTag compound = stack.getOrCreateTag();
				if (compound.getBoolean(TAG_STATE)) {
					if (player.isOnGround()) {

						player.applyKnockback(2F,
								(double) MathHelper.sin(player.rotationPitch * ((float) Math.PI / 180F)),
								(double) (-MathHelper.cos(player.rotationPitch * ((float) Math.PI / 180F))));
						List<Entity> targets = player.world.getEntitiesWithinAABBExcludingEntity(player,
								player.getBoundingBox().grow(3.0));
						if (player.world.isRemote) {
							Vector3d pos = player.getPositionVec();
							PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f,
									(RegistryKey<World>) player.world.getDimensionKey());
						}
						if (targets.size() > 0) {

							for (int i = 0; i < targets.size(); ++i) {
								Entity target = targets.get(i);

								if (target instanceof LivingEntity) {
									LivingEntity livingTarget = (LivingEntity) target;
									float dam = 3f / targets.size();
									livingTarget.attackEntityFrom(ItemInit.bloodLoss, dam);
								}
							}
						}
					}
				}
			}

		}
		return super.onEntitySwing(stack, entity);
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		/*
		 * World world = entityIn.world; Vector3 centerVec =
		 * Vector3.fromEntityCenter(entityIn).add(0, -1, 0); double time =
		 * world.getGameTime();
		 * 
		 * if (world.isRemote) { int globalPartCount = 90; for (int i = 0; i < 16; i++)
		 * { count += 0.002; if (count > 2) { count = 0.5f; } } double cos =
		 * Math.cos(time) * count; double sin = Math.sin(time) * count;
		 * 
		 * for (int i = 0; i < globalPartCount; i++) {
		 * world.addParticle(DarkGlowParticleFactory.createData(ParticleColor.BLACK),
		 * centerVec.x + cos, centerVec.y + 1, centerVec.z - sin, 0, 0.00, 0);
		 * world.addParticle(DarkGlowParticleFactory.createData(ParticleColor.RED),
		 * centerVec.x - cos, centerVec.y + 1, centerVec.z + sin, 0, 0.00, 0); } }
		 */
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItemMainhand();
		if (stack.getItem() instanceof ItemLivingAxe) {
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
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hitEntity(stack, target, attacker);
		if (stack.getOrCreateTag().getBoolean(TAG_STATE)) {
			attacker.heal(this.getAttackDamage() / 2);
			if (!attacker.world.isRemote) {
				PlayerEntity playerIn = (PlayerEntity) attacker;
				IBloodVolume playerVolume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				float damageMod = this.getAttackDamage() * 75f;
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
				}

			}
		}
		return super.hitEntity(stack, target, attacker);
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
