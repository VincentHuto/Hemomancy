package com.huto.hemomancy.item.tool;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.containers.ContainerLivingStaff;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbDirected;
import com.huto.hemomancy.item.morphlings.IMorphling;
import com.huto.hemomancy.itemhandler.LivingStaffItemHandler;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.BloodVolumePacketServer;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemLivingStaff extends Item {

	public ItemLivingStaff(Properties properties) {
		super(properties);
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
	public ITextComponent getDisplayName(ItemStack stack) {
		return new StringTextComponent("Living Staff").mergeStyle(TextFormatting.DARK_RED)
				.mergeStyle(TextFormatting.ITALIC);
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		CompoundNBT staffnbt = stack.getOrCreateTag();
		if (!staffnbt.contains("Inventory")) {
			IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElseThrow(NullPointerException::new);
			if (handler instanceof LivingStaffItemHandler) {
				LivingStaffItemHandler staffHandler = (LivingStaffItemHandler) handler;
				staffHandler.setDirty();
			}
		}
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
											new BloodVolumePacketServer(playerVolume.getBloodVolume()));
								}
							}
						}

					} else {
						this.summonDirectedOrb(worldIn, player);
						playerVolume.subtractBloodVolume(50f);
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
								new BloodVolumePacketServer(playerVolume.getBloodVolume()));
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
		miss.setDirectionMotion(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.0F, 1.0F);
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
