package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.client.particle.factory.AbsrobedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.render.item.hematic.CellHandParticleEffects;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.itemhandler.LivingStaffItemHandler;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.menu.LivingStaffMenu;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class LivingStaffItem extends LivingItem implements IDispellable {
	private static final String UTILITY_SESSION_BLOOD_HANDLED_KEY = "HemomancyLivingStaffUtilityBloodHandled";

	private static int getSlotFor(Inventory inv, ItemStack stack) {
		if (inv.getSelected() == stack)
			return inv.selected;

		for (int i = 0; i < inv.items.size(); ++i) {
			ItemStack invStack = inv.items.get(i);
			if (invStack == stack) {
				return i;
			}
		}

		// Couldn't find the exact instance, can not ensure we have the right slot.
		return -1;
	}

	public LivingStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		Level world = context.level();
		if (world != null && world.isClientSide) {
			net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
			if (player != null) {
				HemoCapabilityAccess.getLivingStaffProgress(player).ifPresent(progress -> {
					if (progress.isVesperMemoryAwakened()) {
						tooltip.add(Component.translatable("item.hemomancy.living_staff.vesper_awakened.tooltip")
								.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
					}
				});
				HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
					ItemStack equipped = cap.getEquippedMorphling();
					if (!equipped.isEmpty()) {
						tooltip.add(equipped.getHoverName());
					}
				});
			}
		}
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000 / 2;
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		CompoundTag staffnbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!staffnbt.contains("Inventory")) {
			IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
			if (handler instanceof LivingStaffItemHandler) {
				LivingStaffItemHandler staffHandler = (LivingStaffItemHandler) handler;
				staffHandler.setDirty();
			}
		}
	}
	

	@Override
	public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
		super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
		if (pLivingEntity.level().isClientSide) {
			if (pLivingEntity.isShiftKeyDown()) {
				Random rand = new Random();
				Level worldIn = pLivingEntity.level();
				int radius = 2;
				BlockPos pos = pLivingEntity.blockPosition();
				// Absorbs from ground
				for (int i = -radius; i <= radius; ++i) {
					for (int j = -radius; j <= radius; ++j) {
						if (i > -radius && i < radius && j == -1) {
							j = radius;
						}
						if (rand.nextInt(3) == 0) {
							for (int k = 0; k <= 1; ++k) {
								Vec3 vec = pLivingEntity.position();
								BlockPos blockpos = pos.offset(i, k, j);
								if (worldIn.getBlockState(blockpos).getEnchantPowerBonus(worldIn, blockpos) == 0) {
									if (!worldIn.isEmptyBlock(pos.offset(i / radius, 0, j / radius))) {
										break;
									}

									worldIn.addParticle(
											AbsrobedBloodCellParticleFactory.createData(ParticleColor.genRandomColor()),
											vec.x(), vec.y() + 2D, vec.z(), i + rand.nextFloat() - 0.5D,
											k - rand.nextFloat() - 1.0F, j + rand.nextFloat() - 0.5D);

								}
							}
						}
					}
				}
			}
			if (pLivingEntity instanceof Player player
					&& (isSelectedStaffUtility(player, ManipulationEquipHelper.BLOOD_ABSORPTION)
					|| isSelectedStaffUtility(player, ManipulationEquipHelper.BLOOD_PROJECTION))) {
				spawnStaffUtilityParticles(player, pStack);
			}

		}
		if (!pLevel.isClientSide && pLivingEntity instanceof Player player) {
			if (dragNearestLoomOrb(pLevel, player)) {
				return;
			}
			if (isSelectedStaffUtility(player, ManipulationEquipHelper.BLOOD_ABSORPTION)) {
				ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
				LivingStaffFocusProfile focus = LivingStaffFocusProfile.fromPlayer(player, progress);
				int elapsed = getUseDuration(pStack, pLivingEntity) - pRemainingUseDuration;
				int interval = LivingStaffFocusRules.absorptionPulseIntervalTicks(focus);
				if (elapsed % interval == 0) {
					absorbWithStaff(pLevel, player, pStack);
				}
			} else if (isSelectedStaffUtility(player, ManipulationEquipHelper.BLOOD_PROJECTION)) {
				projectWithStaff(pLevel, player, pStack);
			}
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		if (entityLiving instanceof Player player) {
			boolean selectedUtility = isSelectedStaffUtility(player, ManipulationEquipHelper.BLOOD_ABSORPTION)
					|| isSelectedStaffUtility(player, ManipulationEquipHelper.BLOOD_PROJECTION);
			boolean handledUtilityBlood = !worldIn.isClientSide && hasUtilityBloodHandled(player);
			if (selectedUtility || handledUtilityBlood) {
				if (!worldIn.isClientSide) {
					commitUtilityBloodHandled(player);
				}
				player.awardStat(Stats.ITEM_USED.get(this));
				return;
			}
			IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player)
					.orElseThrow(NullPointerException::new);
			if (playerVolume.getBloodVolume() > 50f) {
				if (!worldIn.isClientSide) {

					/*
					 * if (worldIn.rand.nextInt(10) == 6) { player.sendStatusMessage(new
					 * TextComponent( ChatFormatting.DARK_PURPLE +
					 * "Abuse of Power does not come without consequence"), true); }
					 */
					if (!player.isCrouching()) {
						HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
							ItemStack selectedStack = cap.getEquippedMorphling();
							if (!selectedStack.isEmpty() && selectedStack.getItem() instanceof IMorphling) {
								IMorphling morphling = (IMorphling) selectedStack.getItem();
								morphling.use(player, player.getUsedItemHand(), stack, worldIn);
								playerVolume.drain(morphling.getBloodCost());

								PacketHandler.sendToPlayer((ServerPlayer) player, new BloodVolumeServerPacket(playerVolume));
							}
						});

					}
				} else {
					player.playSound(SoundEvents.HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.2F,
							0.8F + (float) Math.random() * 0.2F);
				}
				stack.hurtAndBreak(1, player,
						player.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND
								: EquipmentSlot.OFFHAND);
			} else {
				player.displayClientMessage(Component.literal("Not enough blood to be shed"), true);
			}
		}

		if (entityLiving instanceof Player player) {
			player.awardStat(Stats.ITEM_USED.get(this));
		}

	}

	@Override
	public boolean isFoil(ItemStack stack) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			return isClientPlayerVesperAwakened() || super.isFoil(stack);
		}
		return super.isFoil(stack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		if (!playerIn.isShiftKeyDown()) {
			playerIn.startUsingItem(handIn);
		}

		if (!worldIn.isClientSide) {
			if (!playerIn.isShiftKeyDown()) {
				clearUtilityBloodHandled(playerIn);
			}
			if (playerIn.isShiftKeyDown()) {
				playerIn.openMenu(new MenuProvider() {

					@Nullable

					@Override
					public AbstractContainerMenu createMenu(int windowId, Inventory p_createMenu_2_,
							Player p_createMenu_3_) {
						return new LivingStaffMenu(windowId, p_createMenu_3_.level(), p_createMenu_3_.blockPosition(),
								p_createMenu_2_, p_createMenu_3_);
					}

					@Override
					public Component getDisplayName() {
						return playerIn.getItemInHand(handIn).getHoverName();
					}
				});

			} else {
				return InteractionResultHolder.consume(itemstack);
			}
		}
		return playerIn.isShiftKeyDown()
				? InteractionResultHolder.success(itemstack)
				: InteractionResultHolder.consume(itemstack);

	}

	private static void absorbWithStaff(Level level, Player player, ItemStack stack) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(NullPointerException::new);
		if (!volume.isActive() || volume.isFull()) {
			return;
		}
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
		LivingStaffFocusProfile focus = LivingStaffFocusProfile.fromPlayer(player, progress);
		int targetCap = LivingStaffFocusRules.absorptionTargetCap(true, focus);
		double amountPerTarget = LivingStaffFocusRules.absorptionDamagePerTarget(focus);
		List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
						player.getBoundingBox().inflate(LivingStaffFocusRules.absorptionRange(focus)),
						target -> BloodAbsorptionItem.isValidAbsorptionTarget(player, target))
				.stream()
				.sorted(Comparator.comparingDouble(player::distanceToSqr))
				.limit(targetCap)
				.toList();
		double handled = 0.0D;
		for (LivingEntity target : targets) {
			handled += BloodAbsorptionItem.absorbFromTarget(level, player, target, amountPerTarget);
		}
		recordUtilityBloodHandled(player, handled);
	}

	private static void projectWithStaff(Level level, Player player, ItemStack stack) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(NullPointerException::new);
		if (!volume.isActive() || volume.isEmpty()) {
			return;
		}
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
		LivingStaffFocusProfile focus = LivingStaffFocusProfile.fromPlayer(player, progress);
		double handled = BloodProjectionItem.projectFromEntity(level, player,
				LivingStaffFocusRules.structureProjectionRate(true, focus),
				LivingStaffFocusRules.bloodTileProjectionRate(true, focus), true);
		recordUtilityBloodHandled(player, handled);
	}

	private static boolean dragNearestLoomOrb(Level level, Player player) {
		BlockPos origin = player.blockPosition();
		int radius = 10;
		boolean handled = false;
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius / 2, -radius),
				origin.offset(radius, radius / 2, radius))) {
			if (level.getBlockEntity(pos) instanceof SomaticLoomBlockEntity loom
					&& loom.isWeavingOrbs()
					&& player.distanceToSqr(Vec3.atCenterOf(pos)) <= 144.0D
					&& loom.dragSelectedOrb(player, 1.0D)) {
				handled = true;
				break;
			}
		}
		return handled;
	}

	private static void recordUtilityBloodHandled(Player player, double handled) {
		if (handled <= 0.0D) {
			return;
		}
		CompoundTag data = player.getPersistentData();
		data.putDouble(UTILITY_SESSION_BLOOD_HANDLED_KEY,
				data.getDouble(UTILITY_SESSION_BLOOD_HANDLED_KEY) + handled);
	}

	private static boolean hasUtilityBloodHandled(Player player) {
		return player.getPersistentData().getDouble(UTILITY_SESSION_BLOOD_HANDLED_KEY) > 0.0D;
	}

	private static void commitUtilityBloodHandled(Player player) {
		CompoundTag data = player.getPersistentData();
		double handled = data.getDouble(UTILITY_SESSION_BLOOD_HANDLED_KEY);
		if (handled > 0.0D) {
			HemoCapabilityAccess.getLivingStaffProgress(player).ifPresent(progress -> {
				progress.addBloodHandled(handled);
				if (player instanceof ServerPlayer serverPlayer) {
					LivingStaffBondHelper.syncProgress(serverPlayer);
				}
			});
		}
		data.remove(UTILITY_SESSION_BLOOD_HANDLED_KEY);
	}

	private static void clearUtilityBloodHandled(Player player) {
		player.getPersistentData().remove(UTILITY_SESSION_BLOOD_HANDLED_KEY);
	}

	public static boolean isLivingStaffUtilityUse(LivingEntity living, ItemStack stack) {
		return isLivingStaffAbsorptionUse(living, stack) || isLivingStaffProjectionUse(living, stack);
	}

	public static boolean isLivingStaffAbsorptionUse(LivingEntity living, ItemStack stack) {
		return isLivingStaffSelectedUtility(living, stack, ManipulationEquipHelper.BLOOD_ABSORPTION);
	}

	public static boolean isLivingStaffProjectionUse(LivingEntity living, ItemStack stack) {
		return isLivingStaffSelectedUtility(living, stack, ManipulationEquipHelper.BLOOD_PROJECTION);
	}

	private static boolean isLivingStaffSelectedUtility(LivingEntity living, ItemStack stack, String manipName) {
		return stack.getItem() instanceof LivingStaffItem
				&& living instanceof Player player
				&& living.isUsingItem()
				&& isSelectedStaffUtility(player, manipName);
	}

	public static boolean isSelectedStaffUtility(Player player, String manipName) {
		return HemoCapabilityAccess.getKnownManipulations(player).map(known -> {
			if (known.getManipList().isEmpty()) {
				return false;
			}
			BloodManipulation selected = known.getSelectedManip();
			return selected != null
					&& LivingStaffUtilitySelectionRules.isSelectedUtility(selected.getName(), manipName);
		}).orElse(false);
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean isClientPlayerVesperAwakened() {
		net.minecraft.client.player.LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
		return player != null && HemoCapabilityAccess.getLivingStaffProgress(player)
				.map(ILivingStaffProgress::isVesperMemoryAwakened)
				.orElse(false);
	}

	@OnlyIn(Dist.CLIENT)
	private static void spawnStaffUtilityParticles(Player player, ItemStack stack) {
		HumanoidArm activeArm = player.getUsedItemHand() == InteractionHand.MAIN_HAND
				? player.getMainArm()
				: player.getMainArm().getOpposite();
		CellHandParticleEffects.spawnFirstPersonParticlesForStack(stack, activeArm);
	}

}
