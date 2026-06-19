package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.LatchingVeinHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.VeinMinerHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.VasculariumCharmRules;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.AnastocordycepsNexusItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.SanguifloraeCadensItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.SaprovittaVestigiumItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.ThanomycesResurgensItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class ScarsEntityEventHandler {

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

    private static void dropItemsAt(Player player, Collection<ItemEntity> drops) {
        HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
            for (int i = 0; i < equipment.getSlots(); ++i) {
                ItemStack stack = equipment.getStackInSlot(i);
                if (!stack.isEmpty() && VasculariumCharmRules.shouldDropEquippedSlot(
                        stack.is(ItemInit.charm_of_vascularium.get()))) {
                    ItemEntity ei = new ItemEntity(player.level(), player.getX(), player.getY() + player.getEyeHeight(),
                            player.getZ(), stack.copy());
                    ei.setPickUpDelay(40);
                    drops.add(ei);
                    equipment.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        });
    }

    // --- Capability lifecycle ---

    @SubscribeEvent
    public static void playerHurt(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !event.getEntity().level().isClientSide) {
            // Split Husk (Thanomyces resurgens) — death prevention in slot 0
            if (!event.isCanceled()) {
                HemoCapabilityAccess.getScarState(player).ifPresent(scarState -> {
                    ItemStack fungalSlot = scarState.getFungalScar();
                    if (fungalSlot.getItem() instanceof ThanomycesResurgensItem splitHusk) {
                        long gameTime = player.level().getGameTime();
                        if (splitHusk.isReady(fungalSlot, gameTime)) {
                            IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
                            if (blood != null && blood.isActive() && blood.getBloodVolume() > 0) {
                                blood.drain(blood.getBloodVolume());
                                splitHusk.startCooldown(fungalSlot, gameTime);
                                int symbiosis = SkillPointHelper.getFungalSymbiosisLevel(player);
                                float reformFraction = Math.min(0.45F,
                                        ThanomycesResurgensItem.REFORM_FRACTION + symbiosis * 0.03F);
                                player.setHealth(player.getMaxHealth() * reformFraction);
                                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                                        60 + symbiosis * 10, 4, true, false));
                                ServerLevel world = (ServerLevel) player.level();
                                world.playSound(player, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.6f, 0.7f);
                                event.setCanceled(true);
                            }
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerTickScars(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // tick scar-related handlers
        HemoCapabilityAccess.getScarState(player).ifPresent(com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars::tick);

        // Scar tendency synergy check (server-side, every 20 ticks)
        if (!player.level().isClientSide && player.tickCount % 20 == 0) {
            checkScarSynergy(player);
        }

        // Feeding Wake (Saprovitta vestigium) — damaging blood-fungal trail while moving
        if (!player.level().isClientSide && player.tickCount % SaprovittaVestigiumItem.TRAIL_INTERVAL_TICKS == 0) {
            HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
                ItemStack fungalSlot = scars.getFungalScar();
                if (fungalSlot.getItem() instanceof SaprovittaVestigiumItem) {
                    double movSq = player.getDeltaMovement().horizontalDistanceSqr();
                    if (movSq > SaprovittaVestigiumItem.MOVEMENT_THRESHOLD_SQ) {
                        int symbiosis = SkillPointHelper.getFungalSymbiosisLevel(player);
                        AABB area = player.getBoundingBox().inflate(SaprovittaVestigiumItem.TRAIL_RADIUS
                                + symbiosis * 0.4D);
                        float damage = SaprovittaVestigiumItem.TRAIL_DAMAGE * (1.0F + symbiosis * 0.08F);
                        List<Monster> mobs = player.level().getEntitiesOfClass(Monster.class, area);
                        for (Monster mob : mobs) {
                            mob.hurt(player.damageSources().magic(), damage);
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity harmed = event.getEntity();
        Entity sourceEntity = event.getContainer().getSource().getEntity();

        // Player attacks another entity
        if (sourceEntity instanceof Player player && !player.level().isClientSide) {
            HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
                scars.forEachActiveCerebralScar(scar -> scar.onPlayerAttack(player, harmed));

                // Latching Vein (Anastocordyceps nexus) - tether struck enemy and nearby mobs
                ItemStack fungalSlot = scars.getFungalScar();
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
                HemoCapabilityAccess.getScarState(player).ifPresent(scars ->
                        scars.forEachActiveCerebralScar(scar -> scar.onPlayerDefend(player, attacker)));
            }
        }
    }

    // --- Combat event handlers for scar effects ---

    @SubscribeEvent
    public static void onEntityKilledByPlayer(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player && !player.level().isClientSide) {
            LivingEntity killed = event.getEntity();
            HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
                scars.forEachActiveCerebralScar(scar -> scar.onPlayerKill(player, killed));

                // Vein Orchard (Sanguiflora cadens) - chance to drop blood resources at kill site
                ItemStack fungalSlot = scars.getFungalScar();
                int symbiosis = SkillPointHelper.getFungalSymbiosisLevel(player);
                if (fungalSlot.getItem() instanceof SanguifloraeCadensItem
                        && player.level().random.nextFloat() < Math.min(1.0F,
                        SanguifloraeCadensItem.ORCHARD_CHANCE + symbiosis * 0.04F)) {
                    double x = killed.getX(), y = killed.getY(), z = killed.getZ();
                    int sporeCount = 1 + player.level().random.nextInt(2) + (symbiosis >= 2 ? 1 : 0);
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

    private static void checkScarSynergy(Player player) {
        HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
            EnumMap<EnumBloodTendency, Integer> counts = new EnumMap<>(EnumBloodTendency.class);
            scars.forEachActiveCerebralScar(scar -> counts.merge(scar.getAssignedTendency(), 1, Integer::sum));

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

    // --- Synergy logic ---

    @SubscribeEvent
    public static void onBlockBreak(BreakEvent event) {
        HemoCapabilityAccess.getScarState(event.getPlayer()).ifPresent(scars -> {
            HemoCapabilityAccess.getKnownManipulations(event.getPlayer()).ifPresent(manips -> {

                if (scars.getFungalScar().getItem() == ItemInit.talaromyces_minus.get()
                        && event.getPlayer().isShiftKeyDown()) {
                    if (manips.getLastVeinMineStart() == BlockPos.ZERO && event.getState().is(Tags.Blocks.ORES)) {
                        VeinMinerHelper.tryVeinMine(event.getPlayer().getMainHandItem(), event.getPlayer(),
                                event.getPos());
                    }
                }
            });
        });
    }

    // --- Block break (vein mining) ---

    /**
         * scar slots 1–4 hold regular (non-fungal) scars.
         */
        // --- Synergy bonus definitions (one per tendency) ---

        private record SynergyBonus(Holder<Attribute> attribute, ResourceLocation modifierId, double amount,
                                    AttributeModifier.Operation operation) {
    }


}