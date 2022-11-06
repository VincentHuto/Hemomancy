package com.vincenthuto.hemomancy.item.tool.living;

import java.util.List;
import java.util.function.Consumer;

import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.render.item.living.LivingAxeItemRenderer;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.PacketDistributor;

public class LivingAxeItem extends LivingToolItem {

	public static String TAG_STATE = "state";

	float count = 0.5f;

	public LivingAxeItem(float speedIn, float attackDamageIn, Tier tier, Properties builderIn) {
		super(speedIn, attackDamageIn, -2.3f, tier, builderIn);
	}
//
//	@Override
//		public void initializeClient(Consumer<IClientItemExtensions> consumer) {
//		consumer.accept(new IClientItemExtensions() {
//			final BlockEntityWithoutLevelRenderer myRenderer = new RenderItemLivingAxe();
//
//			@Override
//			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
//				return myRenderer;
//			}
//		});
//	}

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
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.hurtEnemy(stack, target, attacker);
		if (stack.getOrCreateTag().getBoolean(TAG_STATE)) {
			attacker.heal(this.getAttackDamage() / 2);
			if (!attacker.level.isClientSide) {
				Player playerIn = (Player) attacker;
				IBloodVolume playerVolume = playerIn.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				float damageMod = this.getAttackDamage() * 75f;
				if (playerVolume.getBloodVolume() > damageMod) {
					playerVolume.drain(damageMod);

					PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
							new BloodVolumeServerPacket(playerVolume));
				} else {
					playerVolume.drain(damageMod);
					PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) playerIn),
							new BloodVolumeServerPacket(playerVolume));
					stack.hurtAndBreak(getMaxDamage() + 10, attacker, (p_220017_1_) -> {
						p_220017_1_.broadcastBreakEvent(attacker.getUsedItemHand());
					});
				}

			}
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		super.initializeClient(consumer);
		consumer.accept(RenderPropLivingAxe.INSTANCE);

	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		/*
		 * Level world = entityIn.world; Vector3 centerVec =
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
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			if (stack.getItem() instanceof LivingAxeItem) {
				CompoundTag compound = stack.getOrCreateTag();
				if (compound.getBoolean(TAG_STATE)) {
					if (player.isOnGround()) {

//						player.knockback(2F, Mth.sin(player.getXRot() * ((float) Math.PI / 180F)),
//								(-Mth.cos(player.getXRot() * ((float) Math.PI / 180F))));
						List<Entity> targets = player.level.getEntities(player, player.getBoundingBox().inflate(3.0));
						if (player.level.isClientSide) {
							Vec3 pos = player.position();
							PacketHandler.sendLivingToolBreakParticles(pos, ParticleColor.BLOOD, 64f,
									player.level.dimension());
						}
						if (targets.size() > 0) {

							for (Entity target : targets) {
								if (target instanceof LivingEntity) {
									LivingEntity livingTarget = (LivingEntity) target;
									float dam = 3f / targets.size();
									livingTarget.hurt(ItemInit.bloodLoss, dam);
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
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getMainHandItem();
		if (stack.getItem() instanceof LivingAxeItem) {
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
}

class RenderPropLivingAxe implements IClientItemExtensions {

	public static RenderPropLivingAxe INSTANCE = new RenderPropLivingAxe();



	@Override
	public BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return new LivingAxeItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
				Minecraft.getInstance().getEntityModels());
	}
}
