package com.huto.hemomancy.item.morphlings;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.container.ContainerMorphlingJar;
import com.huto.hemomancy.itemhandler.MorphlingJarItemHandler;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.jar.PacketToggleJarMessage;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.world.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemMorphlingJar extends Item {

	String name;
	Integer size;
	Rarity rarity;
	public static String TAG_SIZE = "size";

	public ItemMorphlingJar(String name, Integer size, Rarity rarity) {
		super(new Item.Properties().maxStackSize(1).group(com.huto.hemomancy.Hemomancy.HemomancyItemGroup.instance));
		this.name = name;
		this.size = size;
		this.rarity = rarity;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (entityIn instanceof PlayerEntity) {
			CompoundNBT compoundnbt = stack.getOrCreateTag();
			if (stack.hasTag()) {
				CompoundNBT items = (CompoundNBT) compoundnbt.get("Inventory");
				if (items != null) {
					if (items.contains("Items", 9)) {
						compoundnbt.putInt(TAG_SIZE, ((ListNBT) items.get("Items")).size());
					}
				}
			}
		}
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return rarity;
	}

	public ItemMorphlingJar setName() {
		setRegistryName(Hemomancy.MOD_ID, name);
		return this;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (worldIn.isRemote) {
			if (!playerIn.isSneaking()) {
				Hemomancy.proxy.openJarGui();
				playerIn.playSound(SoundEvents.BLOCK_GLASS_PLACE, 0.40f, 1F);
			}
		}

		if (!worldIn.isRemote) {
			if (playerIn.isSneaking()) {
				// open
				playerIn.openContainer(new INamedContainerProvider() {
					@Override
					public ITextComponent getDisplayName() {
						return playerIn.getHeldItem(handIn).getDisplayName();
					}

					@Nullable
					@Override
					public Container createMenu(int windowId, PlayerInventory p_createMenu_2_,
							PlayerEntity p_createMenu_3_) {
						return new ContainerMorphlingJar(windowId, p_createMenu_3_.world, p_createMenu_3_.getPosition(),
								p_createMenu_2_, p_createMenu_3_);
					}
				});

			}
		}
		return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));

	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
		return new MorphlingJarCaps(stack, size, nbt);
	}

	@SuppressWarnings("rawtypes")
	class MorphlingJarCaps implements ICapabilitySerializable {
		public MorphlingJarCaps(ItemStack stack, int size, CompoundNBT nbtIn) {
			itemStack = stack;
			this.size = size;
			inventory = new MorphlingJarItemHandler(itemStack, size);
			optional = LazyOptional.of(() -> inventory);
		}

		@SuppressWarnings("unused")
		private int size;
		private ItemStack itemStack;
		private MorphlingJarItemHandler inventory;
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

	public void togglePickup(PlayerEntity playerEntity, ItemStack stack) {
		CompoundNBT nbt = stack.getOrCreateTag();
		boolean Pickup = !nbt.getBoolean("Pickup");
		nbt.putBoolean("Pickup", Pickup);
		if (playerEntity instanceof ServerPlayerEntity)
			PacketHandler.CHANNELMORPHLINGJAR.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerEntity),
					new PacketToggleJarMessage(Pickup));
		else
			playerEntity.sendStatusMessage(
					new StringTextComponent(
							I18n.format(Pickup ? "Hemomancy.autopickupenabled" : "Hemomancy.autopickupdisabled")),
					true);

	}

	public boolean filterItem(ItemStack item, ItemStack packItem) {
		return item.getItem() instanceof ItemMorphling ? true : false;

	}

	public boolean pickupEvent(EntityItemPickupEvent event, ItemStack stack) {
		CompoundNBT nbt = stack.getTag();
		if (nbt == null)
			return false;

		if (!nbt.getBoolean("Pickup"))
			return false;

		LazyOptional<IItemHandler> stupidIdiot = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (!stupidIdiot.isPresent())
			return false;

		IItemHandler handler = stupidIdiot.orElse(null);
		if (handler == null || !(handler instanceof MorphlingJarItemHandler))
			return false;
		((MorphlingJarItemHandler) handler).loadIfNotLoaded();

		if (!filterItem(event.getItem().getItem(), stack))
			return false;

		ItemStack pickedUp = event.getItem().getItem();
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack slot = handler.getStackInSlot(i);
			if (slot.isEmpty() || (ItemHandlerHelper.canItemStacksStack(slot, pickedUp) && slot.getCount() < 1
					&& slot.getCount() < handler.getSlotLimit(i))) {
				int remainder = handler.insertItem(i, pickedUp.copy(), false).getCount();
				pickedUp.setCount(remainder);
				if (remainder == 0)
					break;
			}
		}
		return pickedUp.isEmpty();
	}

	@SuppressWarnings("unused")
	private boolean hasTranslation(String key) {
		return !I18n.format(key).equals(key);
	}

	private String fallbackString(String key, String fallback) {
		String tmp = I18n.format(key);
		return tmp.equals(key) ? fallback : tmp;
	}

	@SuppressWarnings("static-access")
	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		boolean pickupEnabled = stack.getOrCreateTag().getBoolean("Pickup");
		if (pickupEnabled)
			tooltip.add(new StringTextComponent(I18n.format("Hemomancy.autopickupenabled")));
		else
			tooltip.add(new StringTextComponent(I18n.format("Hemomancy.autopickupdisabled")));

		if (Screen.hasShiftDown()) {
			IItemHandler jarHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
					.orElseThrow(NullPointerException::new);
			if (jarHandler != null) {
				for (int i = 0; i < jarHandler.getSlots(); i++) {
					if (jarHandler.getStackInSlot(i).getItem() != Items.AIR) {
					}
				}
				CompoundNBT compoundnbt = stack.getOrCreateTag();
				CompoundNBT items = (CompoundNBT) compoundnbt.get("Inventory");
				if (items != null) {
					if (items.contains("Items", 9)) {
						for (int i = 0; i < ((ListNBT) items.get("Items")).size(); i++) {
							tooltip.add(stack.read(((ListNBT) items.get("Items")).getCompound(i)).getDisplayName());

						}
					}
				}
			}

		} else {
			tooltip.add(new StringTextComponent(fallbackString("Hemomancy.shift", "Press <�6�oShift�r> for info.")));
		}
	}
}