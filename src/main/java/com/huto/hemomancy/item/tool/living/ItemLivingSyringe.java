package com.huto.hemomancy.item.tool.living;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.container.ContainerLivingStaff;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbDirected;
import com.huto.hemomancy.item.morphlings.IMorphling;
import com.huto.hemomancy.itemhandler.LivingStaffItemHandler;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.PacketBloodVolumeServer;
import com.huto.hemomancy.network.particle.PacketAirBloodDraw;
import com.huto.hemomancy.particle.factory.AbsrobedBloodCellParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.ClientUtils;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemLivingSyringe extends ItemLivingItem {

	public ItemLivingSyringe(Properties properties) {
		super(properties);
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
		super.onUsingTick(stack, player, count);
		if (player.world.isRemote) {
			if (player.isSneaking()) {
				Random rand = new Random();
				World worldIn = player.world;
				int radius = 2;
				BlockPos pos = player.getPosition();
				// Absorbs from ground
				for (int i = -radius; i <= radius; ++i) {
					for (int j = -radius; j <= radius; ++j) {
						if (i > -radius && i < radius && j == -1) {
							j = radius;
						}
						if (rand.nextInt(3) == 0) {
							for (int k = 0; k <= 1; ++k) {
								Vector3d vec = player.getPositionVec();
								BlockPos blockpos = pos.add(i, k, j);
								if (worldIn.getBlockState(blockpos).getEnchantPowerBonus(worldIn, blockpos) == 0) {
									if (!worldIn.isAirBlock(pos.add(i / radius, 0, j / radius))) {
										break;
									}

									worldIn.addParticle(
											AbsrobedBloodCellParticleFactory.createData(ParticleColor.genRandomColor()),
											(double) vec.getX(), (double) vec.getY() + 2D, (double) vec.getZ(),
											(double) ((float) i + rand.nextFloat()) - 0.5D,
											(double) ((float) k - rand.nextFloat() - 1.0F),
											(double) ((float) j + rand.nextFloat()) - 0.5D);

								}
							}
						}
					}
				}
			}

			/*
			 * // Absorb from Entity Vector3 vec = Vector3.fromEntityCenter(player);
			 * List<Entity> targets = player.world
			 * .getEntitiesWithinAABBExcludingEntity(player,
			 * player.getBoundingBox().grow(5.0)).stream() .filter(e -> ((LivingEntity)
			 * e).canEntityBeSeen((Entity) player)).collect(Collectors.toList()); if
			 * (targets.size() > 0) { for (int i = 0; i < targets.size(); ++i) { Entity
			 * target = targets.get(i); Vector3 targetVec =
			 * Vector3.fromEntityCenter(target); Vector3 finalPos =
			 * vec.subtract(targetVec).multiply(-1);
			 * 
			 * worldIn.addParticle(AbsrobedBloodCellParticleFactory.createData(ParticleColor
			 * .RED), (double) vec.x, (double) vec.y + 1.05D, (double) vec.z, (double)
			 * ((float) finalPos.x + rand.nextFloat()) - 0.5D, (double) ((float) finalPos.y
			 * - rand.nextFloat() - 0F), (double) ((float) finalPos.z + rand.nextFloat()) -
			 * 0.5D);
			 * 
			 * } }
			 */
			// draw

			PacketHandler.CHANNELBLOODVOLUME
					.sendToServer(new PacketAirBloodDraw(ClientUtils.getPartialTicks()));

		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {

		if (!worldIn.isRemote) {
			if (playerIn.isSneaking()) {
				playerIn.openContainer(new INamedContainerProvider() {

					@Override
					public ITextComponent getDisplayName() {
						return playerIn.getHeldItem(handIn).getDisplayName();
					}

					@Nullable

					@Override
					public Container createMenu(int windowId, PlayerInventory p_createMenu_2_,
							PlayerEntity p_createMenu_3_) {
						return new ContainerLivingStaff(windowId, p_createMenu_3_.world, p_createMenu_3_.getPosition(),
								p_createMenu_2_, p_createMenu_3_);
					}
				});

			} else {
				ItemStack itemstack = playerIn.getHeldItem(handIn);
				playerIn.setActiveHand(handIn);
				return ActionResult.resultConsume(itemstack);
			}
		}
		return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));

	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entityLiving;
			IBloodVolume playerVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			if (playerVolume.getBloodVolume() > 50f) {
				if (!worldIn.isRemote) {

					/*
					 * if (worldIn.rand.nextInt(10) == 6) { player.sendStatusMessage(new
					 * StringTextComponent( TextFormatting.DARK_PURPLE +
					 * "Abuse of Power does not come without consequence"), true); }
					 */
					if (!player.isCrouching()) {
						CompoundNBT compoundnbt = stack.getOrCreateTag();
						CompoundNBT items = (CompoundNBT) compoundnbt.get("Inventory");
						if (items != null) {
							if (items.contains("Items", 9)) {
								@SuppressWarnings("static-access")
								ItemStack selectedStack = stack.read(((ListNBT) items.get("Items")).getCompound(0));
								if (selectedStack.getItem() instanceof IMorphling) {
									IMorphling morphling = (IMorphling) selectedStack.getItem();
									morphling.use(player, player.getActiveHand(), stack, worldIn);
									playerVolume.subtractBloodVolume(morphling.getBloodCost());
									PacketHandler.CHANNELBLOODVOLUME.send(
											PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
											new PacketBloodVolumeServer(playerVolume));
								}
							}
						}

					} else {
						this.summonDirectedOrb(worldIn, player);
						playerVolume.subtractBloodVolume(50f);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
								new PacketBloodVolumeServer(playerVolume));
					}
				} else {
					player.playSound(SoundEvents.ENTITY_HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.2F,
							0.8F + (float) Math.random() * 0.2F);
				}
				stack.damageItem(1, player, (p_220009_1_) -> {
					p_220009_1_.sendBreakAnimation(player.getActiveHand());
				});
			} else {
				player.sendStatusMessage(new StringTextComponent("Not enough blood to be shed"), true);
			}
		}

		((PlayerEntity) entityLiving).addStat(Stats.ITEM_USED.get(this));

	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000 / 2;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	public void summonDirectedOrb(World worldIn, PlayerEntity playerIn) {
		EntityBloodOrbDirected miss = new EntityBloodOrbDirected((PlayerEntity) playerIn, false);
		miss.setPosition(playerIn.getPosX() - 0.5, playerIn.getPosY() + 0.6, playerIn.getPosZ() - 0.5);
		miss.setDirectionAndMovement(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.0F, 1.0F);
		worldIn.addEntity(miss);
	}

	@SuppressWarnings("static-access")
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		CompoundNBT compoundnbt = stack.getOrCreateTag();
		CompoundNBT items = (CompoundNBT) compoundnbt.get("Inventory");
		if (items != null) {
			if (items.contains("Items", 9)) {
				tooltip.add(stack.read(((ListNBT) items.get("Items")).getCompound(0)).getDisplayName());
			}
		}
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
		return new LivingStaffInventoryCap(stack, 1, nbt);
	}

	@SuppressWarnings("rawtypes")
	class LivingStaffInventoryCap implements ICapabilitySerializable {
		public LivingStaffInventoryCap(ItemStack stack, int size, CompoundNBT nbtIn) {
			itemStack = stack;
			this.size = size;
			inventory = new LivingStaffItemHandler(itemStack, size);
			optional = LazyOptional.of(() -> inventory);
		}

		@SuppressWarnings("unused")
		private int size;
		private ItemStack itemStack;
		private LivingStaffItemHandler inventory;
		private LazyOptional<IItemHandler> optional;

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
			if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
				return optional.cast();
			} else
				return LazyOptional.empty();
		}

		@Override
		public INBT serializeNBT() {
			inventory.save();
			return new CompoundNBT();
		}

		@Override
		public void deserializeNBT(INBT nbt) {
			inventory.load();
		}
	}

}
