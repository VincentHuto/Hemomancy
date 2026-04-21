package com.vincenthuto.hemomancy.common.capability.player.scar;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper;

import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketCurvedHornAnimation;
import com.vincenthuto.hemomancy.common.network.capa.PacketGourdScarSync;
import com.vincenthuto.hemomancy.common.network.capa.scars.PacketScarSync;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.NetworkDirection;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.GAME)
public class ScarEntityEventHandler {

	/** scar slots 1–4 hold regular (non-fungal) scars. */
	private static final int SCAR_SLOT_MIN = 1;
	private static final int SCAR_SLOT_MAX = 4;

	/** Effective max scar slot, expanded by the Scar Resonance skill (up to +3). */
	private static int getEffectiveScarSlotMax() {
		return SCAR_SLOT_MAX + SkillPointHelper.getScarResonanceSlots();
	}

	// --- Synergy bonus definitions (one per tendency) ---

	private record SynergyBonus(Attribute attribute, UUID uuid, String name, double amount,
			AttributeModifier.Operation operation) {
	}

	private static final EnumMap<EnumBloodTendency, SynergyBonus> SYNERGY_BONUSES = new EnumMap<>(
			EnumBloodTendency.class);

	static {
		SYNERGY_BONUSES.put(EnumBloodTendency.ANIMUS, makeSynergy(EnumBloodTendency.ANIMUS,
				Attributes.MAX_HEALTH, 2.0, AttributeModifier.Operation.ADDITION));
		SYNERGY_BONUSES.put(EnumBloodTendency.FLAMMEUS, makeSynergy(EnumBloodTendency.FLAMMEUS,
				Attributes.ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADDITION));
		SYNERGY_BONUSES.put(EnumBloodTendency.MORTEM, makeSynergy(EnumBloodTendency.MORTEM,
				Attributes.ATTACK_DAMAGE, 1.0, AttributeModifier.Operation.ADDITION));
		SYNERGY_BONUSES.put(EnumBloodTendency.CONGEATIO, makeSynergy(EnumBloodTendency.CONGEATIO,
				Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.MULTIPLY_TOTAL));
		SYNERGY_BONUSES.put(EnumBloodTendency.DUCTILIS, makeSynergy(EnumBloodTendency.DUCTILIS,
				Attributes.ATTACK_SPEED, 0.05, AttributeModifier.Operation.MULTIPLY_TOTAL));
		SYNERGY_BONUSES.put(EnumBloodTendency.LUX, makeSynergy(EnumBloodTendency.LUX,
				Attributes.ARMOR_TOUGHNESS, 1.0, AttributeModifier.Operation.ADDITION));
		SYNERGY_BONUSES.put(EnumBloodTendency.FERRIC, makeSynergy(EnumBloodTendency.FERRIC,
				Attributes.ARMOR, 1.0, AttributeModifier.Operation.ADDITION));
		SYNERGY_BONUSES.put(EnumBloodTendency.TENEBRIS, makeSynergy(EnumBloodTendency.TENEBRIS,
				Attributes.MOVEMENT_SPEED, 0.05, AttributeModifier.Operation.MULTIPLY_TOTAL));
	}

	private static SynergyBonus makeSynergy(EnumBloodTendency tendency, Attribute attribute,
			double amount, AttributeModifier.Operation operation) {
		UUID uuid = UUID.nameUUIDFromBytes(("hemomancy:synergy:" + tendency.name().toLowerCase()).getBytes());
		return new SynergyBonus(attribute, uuid, "Scar Synergy " + tendency.name(), amount, operation);
	}

	// --- Capability lifecycle ---

	@SubscribeEvent
	public static void attachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Hemomancy.rloc("scarcontainer"),
					new ScarsContainerProvider((Player) event.getObject()));
		}
	}

	@SubscribeEvent
	public static void cloneCapabilitiesEvent(PlayerEvent.Clone event) {
		try {
			event.getOriginal().getCapability(ScarsCapabilities.SCARS).ifPresent(bco -> {
				CompoundTag nbt = ((ScarsContainer) bco).serializeNBT();
				event.getOriginal().getCapability(ScarsCapabilities.SCARS).ifPresent(bcn -> {
					((ScarsContainer) bcn).deserializeNBT(nbt);
				});
			});
		} catch (Exception e) {
			System.out.println(
					"Could not clone player [" + event.getOriginal().getName() + "] scars when changing dimensions");
		}
	}

	private static void dropItemsAt(Player player, Collection<ItemEntity> drops) {
		player.getCapability(ScarsCapabilities.SCARS).ifPresent(scars -> {
			for (int i = 0; i < scars.getSlots(); ++i) {
				if (!scars.getStackInSlot(i).isEmpty()) {
					ItemEntity ei = new ItemEntity(player.level(), player.getX(), player.getY() + player.getEyeHeight(),
							player.getZ(), scars.getStackInSlot(i).copy());
					ei.setPickUpDelay(40);
					drops.add(ei);
					scars.setStackInSlot(i, ItemStack.EMPTY);
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

			player.getCapability(ScarsCapabilities.SCARS).ifPresent(scars -> {
				// slot 5 is the special curved-horn slot, not a regular scar slot
				ItemStack itemstack = scars.getStackInSlot(5);
				if (itemstack.getItem() == ItemInit.curved_horn.get()) {
					itemstack.hurtAndBreak(1, player, (p_220017_1_) -> {
						p_220017_1_.broadcastBreakEvent(player.getUsedItemHand());
					});
					player.addEffect(new MobEffectInstance(EffectInit.blood_rush.get(), 200, 1));
					player.setHealth(1.0f);
					ServerLevel world = (ServerLevel) player.level();
					world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE,
							SoundSource.AMBIENT, 0.5f, 0.5f);
					PacketHandler.CHANNELSCARS.sendTo(new PacketCurvedHornAnimation(),
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
		player.getCapability(ScarsCapabilities.SCARS).ifPresent(IScarsItemHandler::tick);
		AttributeInstance attributeInstance = player.getAttribute(AttributeInit.getFlightAttribute());
		if (attributeInstance != null) {
			AttributeModifier elytraModifier = AttributeInit.getElytraModifier();
			attributeInstance.removeModifier(elytraModifier);
			ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);

			if (stack.canElytraFly(player) && !attributeInstance.hasModifier(elytraModifier)) {
				attributeInstance.addTransientModifier(elytraModifier);
			}
		}

		// Scar tendency synergy check (server-side, every 20 ticks)
		if (!player.level().isClientSide && player.tickCount % 20 == 0) {
			checkScarSynergy(player);
		}
	}

	// --- Combat event handlers for scar effects ---

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		// Player attacks another entity
	if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide) {
			LivingEntity target = event.getEntity();
			player.getCapability(ScarsCapabilities.SCARS).ifPresent(scars -> {
				for (int i = SCAR_SLOT_MIN; i <= getEffectiveScarSlotMax(); i++) {
					ItemStack stack = scars.getStackInSlot(i);
					if (stack.getItem() instanceof ItemScar scar) {
						scar.onPlayerAttack(player, target);
					}
				}
			});
		}

		// Player is attacked by another entity
		if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
			if (event.getSource().getEntity() instanceof LivingEntity attacker) {
				player.getCapability(ScarsCapabilities.SCARS).ifPresent(scars -> {
					for (int i = SCAR_SLOT_MIN; i <= getEffectiveScarSlotMax(); i++) {
						ItemStack stack = scars.getStackInSlot(i);
						if (stack.getItem() instanceof ItemScar scar) {
							scar.onPlayerDefend(player, attacker);
						}
					}
				});
			}
		}
	}

	@SubscribeEvent
	public static void onEntityKilledByPlayer(LivingDeathEvent event) {
		if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide) {
			LivingEntity killed = event.getEntity();
			player.getCapability(ScarsCapabilities.SCARS).ifPresent(scars -> {
				for (int i = SCAR_SLOT_MIN; i <= getEffectiveScarSlotMax(); i++) {
					ItemStack stack = scars.getStackInSlot(i);
					if (stack.getItem() instanceof ItemScar scar) {
						scar.onPlayerKill(player, killed);
					}
				}
			});
		}
	}

	// --- Synergy logic ---

	private static void checkScarSynergy(Player player) {
		player.getCapability(ScarsCapabilities.SCARS).ifPresent(scars -> {
			EnumMap<EnumBloodTendency, Integer> counts = new EnumMap<>(EnumBloodTendency.class);
			for (int i = SCAR_SLOT_MIN; i <= getEffectiveScarSlotMax(); i++) {
				ItemStack stack = scars.getStackInSlot(i);
				if (stack.getItem() instanceof ItemScar scar) {
					counts.merge(scar.getAssignedTendency(), 1, Integer::sum);
				}
			}

			for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
				SynergyBonus bonus = SYNERGY_BONUSES.get(tendency);
				if (bonus == null)
					continue;

				AttributeInstance attr = player.getAttribute(bonus.attribute());
				if (attr == null)
					continue;

				boolean hasSynergy = counts.getOrDefault(tendency, 0) >= 2;
				boolean hasModifier = attr.getModifier(bonus.uuid()) != null;
				double scaledAmount = bonus.amount() * SkillPointHelper.getScarAffinityMultiplier();

				if (hasSynergy) {
					// Remove and re-add so Scar Affinity level changes take effect immediately
					if (hasModifier) attr.removePermanentModifier(bonus.uuid());
					attr.addPermanentModifier(new AttributeModifier(
							bonus.uuid(), bonus.name(), scaledAmount, bonus.operation()));
				} else if (hasModifier) {
					attr.removePermanentModifier(bonus.uuid());
				}
			}
		});
	}

	// --- Block break (vein mining) ---

	@SubscribeEvent
	public static void onBlockBreak(BreakEvent event) {

		event.getPlayer().getCapability(ScarsCapabilities.SCARS).ifPresent(scars -> {
			HemoCapabilityAccess.getKnownManipulations(event.getPlayer()).ifPresent(manips -> {

				if (scars.getStackInSlot(0).getItem() == ItemInit.talaromyces_minus.get()
						&& event.getPlayer().isShiftKeyDown()) {
					if (manips.getLastVeinMineStart() == BlockPos.ZERO && event.getState().is(Tags.Blocks.ORES)) {
						VeinMinerHelper.tryVeinMine(event.getPlayer().getMainHandItem(), event.getPlayer(),
								event.getPos());
					}
				}
			});
		});
	}

	// --- Network sync ---

	public static void syncSlot(Player player, byte slot, ItemStack stack, Collection<? extends Player> receivers) {

		if (stack.getItem() instanceof BloodGourdItem gourd) {
			IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(stack)
					.orElseThrow(NullPointerException::new);
			PacketGourdScarSync pkt = new PacketGourdScarSync(player.getId(), slot, stack,
					bloodVolume.getBloodVolume());
			for (Player receiver : receivers) {
				PacketHandler.CHANNELSCARS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), pkt);
			}
		} else {
			PacketScarSync pkt = new PacketScarSync(player.getId(), slot, stack);
			for (Player receiver : receivers) {
				PacketHandler.CHANNELSCARS.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), pkt);
			}
		}

	}

	private static void syncSlots(Player player, Collection<? extends Player> receivers) {
		player.getCapability(ScarsCapabilities.SCARS).ifPresent(scars -> {
			for (byte i = 0; i < scars.getSlots(); i++) {
				syncSlot(player, i, scars.getStackInSlot(i), receivers);
			}
		});
	}
}