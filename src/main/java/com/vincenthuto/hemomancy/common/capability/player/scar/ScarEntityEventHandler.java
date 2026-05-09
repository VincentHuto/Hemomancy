package com.vincenthuto.hemomancy.common.capability.player.scar;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.AnastocordycepsNexusItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.SanguifloraeCadensItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.SaprovittaVestigiumItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.ThanomycesResurgensItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketCurvedHornAnimation;
import com.vincenthuto.hemomancy.common.network.capa.PacketGourdScarSync;
import com.vincenthuto.hemomancy.common.network.capa.scars.PacketScarSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class ScarEntityEventHandler {

	/** scar slots 1â€“4 hold regular (non-fungal) scars. */
	private static final int SCAR_SLOT_MIN = 1;
	private static final int SCAR_SLOT_MAX = 4;

	/** Effective max scar slot, expanded by the Scar Resonance skill (up to +3). */
	private static int getEffectiveScarSlotMax(Player player) {
		return SCAR_SLOT_MAX + SkillPointHelper.getScarResonanceSlots(player);
	}

	// --- Synergy bonus definitions (one per tendency) ---

	private static final class SynergyBonus {
		private final Holder<Attribute> attribute;
		private final ResourceLocation modifierId;
		private final double amount;
		private final AttributeModifier.Operation operation;

		private SynergyBonus(Holder<Attribute> attribute, ResourceLocation modifierId, double amount,
				AttributeModifier.Operation operation) {
			this.attribute = attribute;
			this.modifierId = modifierId;
			this.amount = amount;
			this.operation = operation;
		}

		private Holder<Attribute> attribute() { return attribute; }
		private ResourceLocation modifierId() { return modifierId; }
		private double amount() { return amount; }
		private AttributeModifier.Operation operation() { return operation; }
	}

	private static final EnumMap<EnumBloodTendency, SynergyBonus> SYNERGY_BONUSES = new EnumMap<>(
			EnumBloodTendency.class);

	static {
		SYNERGY_BONUSES.put(EnumBloodTendency.ANIMUS, new SynergyBonus(Attributes.MAX_HEALTH,
				Hemomancy.rloc("synergy_animus"), 2.0, AttributeModifier.Operation.ADD_VALUE));
		SYNERGY_BONUSES.put(EnumBloodTendency.FLAMMEUS, new SynergyBonus(Attributes.ATTACK_DAMAGE,
				Hemomancy.rloc("synergy_flammeus"), 1.0, AttributeModifier.Operation.ADD_VALUE));
		SYNERGY_BONUSES.put(EnumBloodTendency.MORTEM, new SynergyBonus(Attributes.ATTACK_DAMAGE,
				Hemomancy.rloc("synergy_mortem"), 1.0, AttributeModifier.Operation.ADD_VALUE));
		SYNERGY_BONUSES.put(EnumBloodTendency.CONGEATIO, new SynergyBonus(Attributes.MOVEMENT_SPEED,
				Hemomancy.rloc("synergy_congeatio"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		SYNERGY_BONUSES.put(EnumBloodTendency.DUCTILIS, new SynergyBonus(Attributes.ATTACK_SPEED,
				Hemomancy.rloc("synergy_ductilis"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		SYNERGY_BONUSES.put(EnumBloodTendency.LUX, new SynergyBonus(Attributes.ARMOR_TOUGHNESS,
				Hemomancy.rloc("synergy_lux"), 1.0, AttributeModifier.Operation.ADD_VALUE));
		SYNERGY_BONUSES.put(EnumBloodTendency.FERRIC, new SynergyBonus(Attributes.ARMOR,
				Hemomancy.rloc("synergy_ferric"), 1.0, AttributeModifier.Operation.ADD_VALUE));
		SYNERGY_BONUSES.put(EnumBloodTendency.TENEBRIS, new SynergyBonus(Attributes.MOVEMENT_SPEED,
				Hemomancy.rloc("synergy_tenebris"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}

	// --- Capability lifecycle ---

	private static void dropItemsAt(Player player, Collection<ItemEntity> drops) {
		HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
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

			HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
				// slot 5 is the special curved-horn slot, not a regular scar slot
				ItemStack itemstack = scars.getStackInSlot(5);
				if (itemstack.getItem() == ItemInit.curved_horn.get()) {
					itemstack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
					player.addEffect(new MobEffectInstance(EffectInit.blood_rush, 200, 1));
					player.setHealth(1.0f);
					ServerLevel world = (ServerLevel) player.level();
					world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE,
							SoundSource.AMBIENT, 0.5f, 0.5f);
					PacketHandler.sendToPlayer((ServerPlayer) player, new PacketCurvedHornAnimation());

					event.setCanceled(true);
				}

				// Split Husk (Thanomyces resurgens) — death prevention in slot 0
				if (!event.isCanceled()) {
					ItemStack fungalSlot = scars.getStackInSlot(0);
					if (fungalSlot.getItem() instanceof ThanomycesResurgensItem splitHusk) {
						long gameTime = player.level().getGameTime();
						if (splitHusk.isReady(fungalSlot, gameTime)) {
							IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
							if (blood != null && blood.isActive() && blood.getBloodVolume() > 0) {
								blood.drain(blood.getBloodVolume());
								splitHusk.startCooldown(fungalSlot, gameTime);
								player.setHealth(player.getMaxHealth() * ThanomycesResurgensItem.REFORM_FRACTION);
								player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 4, true, false));
								ServerLevel world = (ServerLevel) player.level();
								world.playSound(player, player.getX(), player.getY(), player.getZ(),
										SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.6f, 0.7f);
								event.setCanceled(true);
							}
						}
					}
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
	public static void onGlideTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!player.hasEffect(EffectInit.fungal_elytra)) return;

		// Ensure FALL_FLYING attribute modifier is present so canFallFly() returns ALLOW
		AttributeInstance attributeInstance = player.getAttribute(AttributeInit.getFlightAttribute());
		if (attributeInstance != null
				&& !attributeInstance.hasModifier(AttributeInit.getElytraModifier().id()))
			attributeInstance.addTransientModifier(AttributeInit.getElytraModifier());

		// This runs AFTER vanilla updateFallFlying (which cancels fall-flying when no
		// elytra is equipped). Re-enable elytra gliding every tick while the player is
		// airborne and not using the creative-hover mode.
		boolean airborne = !player.onGround() && !player.isInWater()
				&& !player.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION);
		boolean creativeHovering = player.getAbilities().flying;

		if (airborne && !creativeHovering) {
			double vertY = player.getDeltaMovement().y;
			boolean alreadyGliding = player.isFallFlying();
			// Maintain when already gliding, or initiate only when falling fast enough
			// (threshold avoids triggering on normal jumps or 1-block step-downs).
			boolean initiateGlide = vertY < -0.3;
			if (alreadyGliding || initiateGlide) {
				player.startFallFlying();
			}
		}
	}
	

	@SubscribeEvent
	public static void playerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		HemoCapabilityAccess.getScars(player).ifPresent(IScarsItemHandler::tick);
		AttributeInstance attributeInstance = player.getAttribute(AttributeInit.getFlightAttribute());
		if (attributeInstance != null) {
			AttributeModifier elytraModifier = AttributeInit.getElytraModifier();
			attributeInstance.removeModifier(elytraModifier.id());
			ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);

			if (stack.canElytraFly(player) && !attributeInstance.hasModifier(elytraModifier.id())) {
				attributeInstance.addTransientModifier(elytraModifier);
			}
		}

		// Scar tendency synergy check (server-side, every 20 ticks)
		if (!player.level().isClientSide && player.tickCount % 20 == 0) {
			checkScarSynergy(player);
		}

		// Feeding Wake (Saprovitta vestigium) — damaging blood-fungal trail while moving
		if (!player.level().isClientSide && player.tickCount % SaprovittaVestigiumItem.TRAIL_INTERVAL_TICKS == 0) {
			HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
				ItemStack fungalSlot = scars.getStackInSlot(0);
				if (fungalSlot.getItem() instanceof SaprovittaVestigiumItem) {
					double movSq = player.getDeltaMovement().horizontalDistanceSqr();
					if (movSq > SaprovittaVestigiumItem.MOVEMENT_THRESHOLD_SQ) {
						AABB area = player.getBoundingBox().inflate(SaprovittaVestigiumItem.TRAIL_RADIUS);
						List<Monster> mobs = player.level().getEntitiesOfClass(Monster.class, area);
						for (Monster mob : mobs) {
							mob.hurt(player.damageSources().magic(), SaprovittaVestigiumItem.TRAIL_DAMAGE);
						}
					}
				}
			});
		}
	}

	// --- Combat event handlers for scar effects ---

	@SubscribeEvent
	public static void onLivingHurt(LivingDamageEvent.Pre event) {
		LivingEntity harmed = event.getEntity();
		Entity sourceEntity = event.getContainer().getSource().getEntity();

		// Player attacks another entity
		if (sourceEntity instanceof Player player && !player.level().isClientSide) {
			HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
				for (int i = SCAR_SLOT_MIN; i <= getEffectiveScarSlotMax(player); i++) {
					ItemStack stack = scars.getStackInSlot(i);
					if (stack.getItem() instanceof ItemScar scar) {
						scar.onPlayerAttack(player, harmed);
					}
				}

				// Latching Vein (Anastocordyceps nexus) — tether struck enemy and nearby mobs
				ItemStack fungalSlot = scars.getStackInSlot(0);
				if (fungalSlot.getItem() instanceof AnastocordycepsNexusItem && harmed instanceof LivingEntity) {
					LatchingVeinHandler.applyTether(player, harmed, player.level().getGameTime());
				}
			});
		}

		// Latching Vein echo: propagate a fraction of any damage to other tethered entities
		if (!harmed.level().isClientSide) {
			String sourceId = event.getContainer().getSource().getMsgId();
			LatchingVeinHandler.onEntityDamaged(harmed, event.getContainer().getNewDamage(),
					sourceId, harmed.level().getGameTime());
		}

		// Player is attacked by another entity
		if (harmed instanceof Player player && !player.level().isClientSide) {
			if (sourceEntity instanceof LivingEntity attacker) {
				HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
					for (int i = SCAR_SLOT_MIN; i <= getEffectiveScarSlotMax(player); i++) {
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
			HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
				for (int i = SCAR_SLOT_MIN; i <= getEffectiveScarSlotMax(player); i++) {
					ItemStack stack = scars.getStackInSlot(i);
					if (stack.getItem() instanceof ItemScar scar) {
						scar.onPlayerKill(player, killed);
					}
				}

				// Vein Orchard (Sanguiflora cadens) — chance to drop blood resources at kill site
				ItemStack fungalSlot = scars.getStackInSlot(0);
				if (fungalSlot.getItem() instanceof SanguifloraeCadensItem
						&& player.level().random.nextFloat() < SanguifloraeCadensItem.ORCHARD_CHANCE) {
					double x = killed.getX(), y = killed.getY(), z = killed.getZ();
					int sporeCount = 1 + player.level().random.nextInt(2);
					for (int n = 0; n < sporeCount; n++) {
						player.level().addFreshEntity(
								new ItemEntity(player.level(), x, y, z, new ItemStack(ItemInit.spore_sac.get())));
					}
					if (player.level().random.nextFloat() < 0.4f) {
						player.level().addFreshEntity(
								new ItemEntity(player.level(), x, y, z,
										new ItemStack(ItemInit.hematic_iron_scrap.get())));
					}
				}
			});
		}
	}

	// --- Synergy logic ---

	private static void checkScarSynergy(Player player) {
		HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
			EnumMap<EnumBloodTendency, Integer> counts = new EnumMap<>(EnumBloodTendency.class);
			for (int i = SCAR_SLOT_MIN; i <= getEffectiveScarSlotMax(player); i++) {
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
				boolean hasModifier = attr.getModifier(bonus.modifierId()) != null;
				double scaledAmount = bonus.amount() * SkillPointHelper.getScarAffinityMultiplier(player);

				if (hasSynergy) {
					// Remove and re-add so Scar Affinity level changes take effect immediately
					if (hasModifier) attr.removeModifier(bonus.modifierId());
					attr.addPermanentModifier(new AttributeModifier(
							bonus.modifierId(), scaledAmount, bonus.operation()));
				} else if (hasModifier) {
					attr.removeModifier(bonus.modifierId());
				}
			}
		});
	}

	// --- Block break (vein mining) ---

	@SubscribeEvent
	public static void onBlockBreak(BreakEvent event) {

		HemoCapabilityAccess.getScars(event.getPlayer()).ifPresent(scars -> {
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

		if (stack.getItem() instanceof BloodGourdItem) {
			IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(stack).orElse(null);
			PacketGourdScarSync pkt = new PacketGourdScarSync(player.getId(), slot, stack,
					bloodVolume != null ? bloodVolume.getBloodVolume() : 0);
			for (Player receiver : receivers) {
				PacketHandler.sendToPlayer((ServerPlayer) receiver, pkt);
			}
		} else {
			PacketScarSync pkt = new PacketScarSync(player.getId(), slot, stack);
			for (Player receiver : receivers) {
				PacketHandler.sendToPlayer((ServerPlayer) receiver, pkt);
			}
		}

	}

	private static void syncSlots(Player player, Collection<? extends Player> receivers) {
		HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
			for (byte i = 0; i < scars.getSlots(); i++) {
				syncSlot(player, i, scars.getStackInSlot(i), receivers);
			}
		});
	}
}
