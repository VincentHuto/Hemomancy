package com.vincenthuto.hemomancy.common.capability.player.rune;

import java.util.Collection;
import java.util.Collections;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketCurvedHornAnimation;
import com.vincenthuto.hemomancy.common.network.capa.PacketGourdRuneSync;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketRuneSync;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RuneEntityEventHandler {

	@SubscribeEvent
	public static void attachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Hemomancy.rloc("runecontainer"),
					new RunesContainerProvider((Player) event.getObject()));
		}
	}

	@SubscribeEvent
	public static void cloneCapabilitiesEvent(PlayerEvent.Clone event) {
		try {
			event.getOriginal().getCapability(RunesCapabilities.RUNES).ifPresent(bco -> {
				CompoundTag nbt = ((RunesContainer) bco).serializeNBT();
				event.getOriginal().getCapability(RunesCapabilities.RUNES).ifPresent(bcn -> {
					((RunesContainer) bcn).deserializeNBT(nbt);
				});
			});
		} catch (Exception e) {
			System.out.println(
					"Could not clone player [" + event.getOriginal().getName() + "] runes when changing dimensions");
		}
	}

	private static void dropItemsAt(Player player, Collection<ItemEntity> drops) {
		player.getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
			for (int i = 0; i < runes.getSlots(); ++i) {
				if (!runes.getStackInSlot(i).isEmpty()) {
					ItemEntity ei = new ItemEntity(player.level(), player.getX(), player.getY() + player.getEyeHeight(),
							player.getZ(), runes.getStackInSlot(i).copy());
					ei.setPickUpDelay(40);
					drops.add(ei);
					runes.setStackInSlot(i, ItemStack.EMPTY);
				}
			}
		});
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		Entity target = event.getTarget();
		if (target instanceof ServerPlayer) {
			syncSlots((ServerPlayer) target, Collections.singletonList(event.getEntity()));
		}
	}

	@SubscribeEvent
	public static void playerDeath(LivingDropsEvent event) {
		if (event.getEntity() instanceof Player && !event.getEntity().level().isClientSide
				&& !event.getEntity().level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			dropItemsAt((Player) event.getEntity(), event.getDrops());
		}
	}

	@SubscribeEvent
	public static void playerHurt(LivingDeathEvent event) {

		if (event.getEntity() instanceof Player player && !event.getEntity().level().isClientSide) {

			player.getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
				// player events
				ItemStack itemstack = runes.getStackInSlot(5);
				if (itemstack.getItem() == ItemInit.curved_horn.get()) {
					itemstack.hurtAndBreak(1, player, (p_220017_1_) -> {
						p_220017_1_.broadcastBreakEvent(player.getUsedItemHand());
					});
					player.addEffect(new MobEffectInstance(EffectInit.blood_rush.get(), 200, 1));
					player.setHealth(1.0f);
					ServerLevel world = (ServerLevel) player.level();
					world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE,
							SoundSource.AMBIENT, 0.5f, 0.5f);
					PacketHandler.CHANNELRUNES.sendTo(new PacketCurvedHornAnimation(),
							((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);

					event.setCanceled(true);
				}
			});
		}

	}

	@SubscribeEvent
	public static void playerJoin(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) entity;
			syncSlots(player, Collections.singletonList(player));
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onGlideTick(TickEvent.PlayerTickEvent event) {
		if (event.player.hasEffect(EffectInit.fungal_elytra.get())) {
			AttributeInstance attributeInstance = event.player
					.getAttribute(AttributeInit.getFlightAttribute());
			if (attributeInstance != null
					&& !attributeInstance.hasModifier(AttributeInit.getElytraModifier()))
				attributeInstance.addTransientModifier(AttributeInit.getElytraModifier());
		}
	}
	

	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		Player player = event.player;
		player.getCapability(RunesCapabilities.RUNES).ifPresent(IRunesItemHandler::tick);
		AttributeInstance attributeInstance = player.getAttribute(AttributeInit.getFlightAttribute());
		if (attributeInstance != null) {
			AttributeModifier elytraModifier = AttributeInit.getElytraModifier();
			attributeInstance.removeModifier(elytraModifier);
			ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);

			if (stack.canElytraFly(player) && !attributeInstance.hasModifier(elytraModifier)) {
				attributeInstance.addTransientModifier(elytraModifier);
			}
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BreakEvent event) {

		event.getPlayer().getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
			event.getPlayer().getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manips -> {

				if (runes.getStackInSlot(0).getItem() == ItemInit.talaromyces_minus.get()
						&& event.getPlayer().isShiftKeyDown()) {
					if (manips.getLastVeinMineStart() == BlockPos.ZERO && event.getState().is(Tags.Blocks.ORES)) {
						VeinMinerHelper.tryVeinMine(event.getPlayer().getMainHandItem(), event.getPlayer(),
								event.getPos());
					}
				}
			});
		});
	}

	public static void syncSlot(Player player, byte slot, ItemStack stack, Collection<? extends Player> receivers) {

		if (stack.getItem() instanceof BloodGourdItem gourd) {
			IBloodVolume bloodVolume = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			PacketGourdRuneSync pkt = new PacketGourdRuneSync(player.getId(), slot, stack,
					bloodVolume.getBloodVolume());
			for (Player receiver : receivers) {
				PacketHandler.CHANNELRUNES.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), pkt);
			}
		} else {
			PacketRuneSync pkt = new PacketRuneSync(player.getId(), slot, stack);
			for (Player receiver : receivers) {
				PacketHandler.CHANNELRUNES.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), pkt);
			}
		}

	}

	private static void syncSlots(Player player, Collection<? extends Player> receivers) {
		player.getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
			for (byte i = 0; i < runes.getSlots(); i++) {
				syncSlot(player, i, runes.getStackInSlot(i), receivers);
			}
		});
	}
}