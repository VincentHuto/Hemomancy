package com.vincenthuto.hemomancy.capa.player.rune;

import java.util.Collection;
import java.util.Collections;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.init.PotionInit;
import com.vincenthuto.hemomancy.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.PacketCurvedHornAnimation;
import com.vincenthuto.hemomancy.network.capa.PacketGourdRuneSync;
import com.vincenthuto.hemomancy.network.capa.PacketRuneSync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RuneEntityEventHandler {

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

	@SubscribeEvent
	public static void attachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "runecontainer"),
					new RunesContainerProvider((Player) event.getObject()));
		}
	}

	@SubscribeEvent
	public static void playerJoin(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) entity;
			syncSlots(player, Collections.singletonList(player));
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		Entity target = event.getTarget();
		if (target instanceof ServerPlayer) {
			syncSlots((ServerPlayer) target, Collections.singletonList(event.getPlayer()));
		}
	}

	@SubscribeEvent
	public static void playerHurt(LivingDeathEvent event) {

		if (event.getEntityLiving()instanceof Player player && !event.getEntityLiving().level.isClientSide) {

			player.getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
				// player events
				ItemStack itemstack = runes.getStackInSlot(5);
				if (itemstack.getItem() == ItemInit.curved_horn.get()) {
					itemstack.hurtAndBreak(1, player, (p_220017_1_) -> {
						p_220017_1_.broadcastBreakEvent(player.getUsedItemHand());
					});
					player.addEffect(new MobEffectInstance(PotionInit.blood_rush.get(), 200, 1));
					player.setHealth(1.0f);
					ServerLevel world = (ServerLevel) player.level;
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
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		Player player = event.player;
		if (event.phase == TickEvent.Phase.END) {
			player.getCapability(RunesCapabilities.RUNES).ifPresent(IRunesItemHandler::tick);
		}
	}

	private static void syncSlots(Player player, Collection<? extends Player> receivers) {
		player.getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
			for (byte i = 0; i < runes.getSlots(); i++) {
				syncSlot(player, i, runes.getStackInSlot(i), receivers);
			}
		});
	}

	public static void syncSlot(Player player, byte slot, ItemStack stack, Collection<? extends Player> receivers) {
		
		if (stack.getItem() instanceof BloodGourdItem gourd) {
			IBloodVolume bloodVolume = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
		//	System.out.println( gourd +" "+ bloodVolume.getBloodVolume());
			PacketGourdRuneSync pkt = new PacketGourdRuneSync(player.getId(), slot, stack, bloodVolume.getBloodVolume());
			for (Player receiver : receivers) {
				PacketHandler.CHANNELRUNES.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), pkt);
			}
		}else {
			PacketRuneSync pkt = new PacketRuneSync(player.getId(), slot, stack);
			for (Player receiver : receivers) {
				PacketHandler.CHANNELRUNES.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), pkt);
			}
		}

		
	
	}

	@SubscribeEvent
	public static void playerDeath(LivingDropsEvent event) {
		if (event.getEntity() instanceof Player && !event.getEntity().level.isClientSide
				&& !event.getEntity().level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			dropItemsAt((Player) event.getEntity(), event.getDrops());
		}
	}

	private static void dropItemsAt(Player player, Collection<ItemEntity> drops) {
		player.getCapability(RunesCapabilities.RUNES).ifPresent(runes -> {
			for (int i = 0; i < runes.getSlots(); ++i) {
				if (!runes.getStackInSlot(i).isEmpty()) {
					ItemEntity ei = new ItemEntity(player.level, player.getX(), player.getY() + player.getEyeHeight(),
							player.getZ(), runes.getStackInSlot(i).copy());
					ei.setPickUpDelay(40);
					drops.add(ei);
					runes.setStackInSlot(i, ItemStack.EMPTY);
				}
			}
		});
	}
}