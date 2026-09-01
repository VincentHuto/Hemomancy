package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MuscleMemoryWorldEvents {
    private static final ResourceLocation LABORING_ID = Hemomancy.rloc("laboring_arms_break_speed");
    private static final ResourceLocation COURSING_ID = Hemomancy.rloc("coursing_legs_movement_speed");
    private static final ResourceLocation COURSING_STEP_ID = Hemomancy.rloc("coursing_legs_step_height");
    private static final TagKey<Item> CARRION_FOODS = TagKey.create(Registries.ITEM, Hemomancy.rloc("carrion_foods"));
    private static final TagKey<Block> LABORING_RESISTANT = TagKey.create(Registries.BLOCK, Hemomancy.rloc("laboring_arms_resistant"));
    private static final Map<UUID, TickSnapshot> SNAPSHOTS = new HashMap<>();
    private static final Map<UUID, Double> SPRINT_DISTANCE = new HashMap<>();
    private static final Map<UUID, CarrionSnapshot> CARRION = new HashMap<>();
    private static final Map<UUID, Long> EMBERFANG_MOMENTUM = new HashMap<>();

    private MuscleMemoryWorldEvents() {}

	public static void clearSessionState() {
		SNAPSHOTS.clear();
		SPRINT_DISTANCE.clear();
		CARRION.clear();
		EMBERFANG_MOMENTUM.clear();
	}

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || event.isCanceled()) return;
        float hardness = event.getState().getDestroySpeed(player.level(), event.getPos());
        if (!MuscleMemoryPowerRules.laboringEligible(player.isCreative(), hardness,
                player.hasCorrectToolForDrops(event.getState(), player.level(), event.getPos()))) return;
        if (MuscleMemoryActivationService.tryTriggerDetailed(
                player, MuscleMemory.LABORING_ARMS, 80, 160).accepted()) updateModifiers(player);
    }

    @SubscribeEvent
    public static void irontoothBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MuscleMemoryState state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        if (state.isActive(MuscleMemory.LABORING_ARMS, player.level().getGameTime())
                && event.getState().is(LABORING_RESISTANT)
                && MuscleMemoryActivationService.hasSignatureMorphling(player, IrontoothMorphlingItem.class)) {
            event.setNewSpeed(event.getNewSpeed() * 1.15F);
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        long now = player.level().getGameTime();
        MuscleMemoryState memoryState = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        memoryState.migrateLegacyPriming(now);
        boolean reserveChanged = memoryState.tickActiveReserves();
        if (reserveChanged && (player.tickCount % 20 == 0 || !memoryState.hasEnabledMemories())) {
            MuscleMemoryEvents.sync(player);
        }
        TickSnapshot previous = SNAPSHOTS.get(player.getUUID());
        Vec3 current = player.position();
        FoodData food = player.getFoodData();

        if (previous != null) {
            double horizontal = Math.hypot(current.x - previous.position.x, current.z - previous.position.z);
            handleCoursing(player, horizontal);
            handleHushed(player, horizontal, now);
            handleEnduring(player, previous, food);
            handleWinterShroud(player, previous, food, horizontal, now);
        }
        if (player.tickCount % 20 == 0) handlePredatoryEyes(player);
        updateModifiers(player);
        SNAPSHOTS.put(player.getUUID(), new TickSnapshot(current, food.getFoodLevel(), food.getSaturationLevel(), food.getExhaustionLevel()));
    }

    private static void handleCoursing(ServerPlayer player, double horizontal) {
        if (!player.isSprinting() || !player.onGround() || horizontal <= 0D) return;
        double stored = SPRINT_DISTANCE.getOrDefault(player.getUUID(), 0D);
        MuscleMemoryPowerRules.DistanceResult result = MuscleMemoryPowerRules.coursingDistance(stored, horizontal);
        if (result.triggers() > 0) {
            var trigger = MuscleMemoryActivationService.tryTriggerDetailed(
                    player, MuscleMemory.COURSING_LEGS, 80, 120);
            if (trigger.accepted()) {
                SPRINT_DISTANCE.put(player.getUUID(), result.remainder());
                if (MuscleMemoryActivationService.hasSignatureMorphling(player, EmberfangMorphlingItem.class)) {
                    EMBERFANG_MOMENTUM.put(player.getUUID(), player.level().getGameTime() + 60L);
                }
            } else {
                SPRINT_DISTANCE.put(player.getUUID(), Math.min(11.999D, stored + horizontal));
            }
        } else {
            SPRINT_DISTANCE.put(player.getUUID(), result.remainder());
        }
    }

    private static void handleHushed(ServerPlayer player, double horizontal, long now) {
        boolean eligible = MuscleMemoryPowerRules.hushedEligible(
                player.level().getMaxLocalRawBrightness(player.blockPosition()), player.isSprinting(), horizontal > .001D);
        MuscleMemoryState state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        if (eligible && state.isEnabled(MuscleMemory.HUSHED_GAIT)
                && !state.isActive(MuscleMemory.HUSHED_GAIT, now)) {
            MuscleMemoryActivationService.tryTriggerDetailed(player, MuscleMemory.HUSHED_GAIT, 40, 120);
        }
    }

    private static void handlePredatoryEyes(ServerPlayer player) {
        if (player.level().getMaxLocalRawBrightness(player.blockPosition()) > 4) return;
        boolean witch = MuscleMemoryActivationService.hasSignatureMorphling(player, WitchsEarMorphlingItem.class);
        MuscleMemoryState state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        boolean armed = state.isOverexertionArmed(MuscleMemory.PREDATORY_EYES, player.level().getGameTime());
        double radius = armed ? 24D : (witch ? 12D : 16D);
        List<Monster> targets = player.level().getEntitiesOfClass(Monster.class,
                new AABB(player.blockPosition()).inflate(radius), target -> target.isAlive()
                        && target.getDeltaMovement().horizontalDistanceSqr() > .0004D
                        && (armed || witch || player.hasLineOfSight(target)));
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        if (targets.isEmpty()) return;
        var trigger = MuscleMemoryActivationService.tryTriggerDetailed(
                player, MuscleMemory.PREDATORY_EYES, 100, 200);
        if (!trigger.accepted()) return;
        int limit = trigger.overexerted() ? 8 : (witch ? 5 : 3);
        for (int i = 0; i < Math.min(limit, targets.size()); i++) {
            targets.get(i).addEffect(new MobEffectInstance(MobEffects.GLOWING,
                    trigger.overexerted() ? 200 : 100, 0, false, false));
        }
    }

    private static void handleEnduring(ServerPlayer player, TickSnapshot previous, FoodData food) {
        if (MuscleMemoryPowerRules.enduringEligible(previous.food, food.getFoodLevel(), previous.saturation, food.getSaturationLevel())) {
            var trigger = MuscleMemoryActivationService.tryTriggerDetailed(
                    player, MuscleMemory.ENDURING_VISCERA, 100, 200);
            if (trigger.accepted()) food.setFoodLevel(Math.min(20,
                    food.getFoodLevel() + (trigger.overexerted() ? 2 : 1)));
        }
    }

    private static void handleWinterShroud(ServerPlayer player, TickSnapshot previous, FoodData food,
            double horizontal, long now) {
        MuscleMemoryState state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        boolean signature = MuscleMemoryActivationService.hasSignatureMorphling(player, WinterShroudMorphlingItem.class);
        boolean empowered = state.isEmpowered(MuscleMemory.ENDURING_VISCERA, now);
        if (!signature && !empowered) return;
        if (!empowered
                && (horizontal > .001D || player.isSprinting())) return;
        if (!state.isActive(MuscleMemory.ENDURING_VISCERA, now)) return;
        float added = food.getExhaustionLevel() - previous.exhaustion;
        if (added > 0F) food.setExhaustion(previous.exhaustion + added * .5F);
    }

    @SubscribeEvent
    public static void quietGameEvents(VanillaGameEvent event) {
        if (!(event.getCause() instanceof ServerPlayer player)) return;
        long now = player.level().getGameTime();
        if (!player.getData(HemoAttachmentTypes.MUSCLE_MEMORY).isActive(MuscleMemory.HUSHED_GAIT, now)) return;
        boolean quiet = event.getVanillaEvent().equals(GameEvent.STEP)
                || event.getVanillaEvent().equals(GameEvent.SWIM)
                || event.getVanillaEvent().equals(GameEvent.SPLASH);
        boolean deepHush = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY)
                .isEmpowered(MuscleMemory.HUSHED_GAIT, now);
        boolean bootlaceLanding = event.getVanillaEvent().equals(GameEvent.HIT_GROUND)
                && (deepHush || (player.fallDistance <= 6F
                && MuscleMemoryActivationService.hasSignatureMorphling(player, BootlaceMorphlingItem.class)));
        if ((quiet && !player.isSprinting()) || bootlaceLanding) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void startEating(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getItem().is(CARRION_FOODS)) return;
        CARRION.put(player.getUUID(), CarrionSnapshot.capture(player));
    }

    @SubscribeEvent
    public static void finishEating(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getItem().is(CARRION_FOODS)) return;
        CarrionSnapshot snapshot = CARRION.remove(player.getUUID());
        if (snapshot == null) return;
        var trigger = MuscleMemoryActivationService.tryTriggerDetailed(
                player, MuscleMemory.CARRION_METABOLISM, 0, 0);
        if (!trigger.accepted()) return;
        snapshot.restore(player);
        FoodData food = player.getFoodData();
        food.setFoodLevel(Math.min(20, food.getFoodLevel() + (trigger.overexerted() ? 4 : 2)));
        food.setSaturation(Math.min(food.getFoodLevel(), food.getSaturationLevel() + (trigger.overexerted() ? 2F : 1F)));
        if (trigger.overexerted()) player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0));
        HemoCapabilityAccess.getEquippedMorphling(player).ifPresent(cap -> {
            ItemStack equipped = cap.getEquippedMorphling();
            if (equipped.getItem() instanceof GravecapMorphlingItem) MorphlingItem.markFedNow(equipped, player.level().getGameTime());
        });
    }

    private static void updateModifiers(ServerPlayer player) {
        MuscleMemoryState state = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        long now = player.level().getGameTime();
        updateModifier(player.getAttribute(Attributes.BLOCK_BREAK_SPEED), LABORING_ID,
                state.isActive(MuscleMemory.LABORING_ARMS, now),
                state.isEmpowered(MuscleMemory.LABORING_ARMS, now) ? .40D : .20D);
        updateModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), COURSING_ID,
                state.isActive(MuscleMemory.COURSING_LEGS, now),
                state.isEmpowered(MuscleMemory.COURSING_LEGS, now) ? .30D : .15D);
		updateModifier(player.getAttribute(Attributes.STEP_HEIGHT), COURSING_STEP_ID,
				state.isEmpowered(MuscleMemory.COURSING_LEGS, now), .5D, AttributeModifier.Operation.ADD_VALUE);
    }

    static boolean consumeEmberfangMomentum(ServerPlayer player) {
        Long until = EMBERFANG_MOMENTUM.remove(player.getUUID());
        return until != null && until > player.level().getGameTime();
    }

    static void clearRuntime(ServerPlayer player) {
        UUID id = player.getUUID();
        SNAPSHOTS.remove(id);
        SPRINT_DISTANCE.remove(id);
        CARRION.remove(id);
        EMBERFANG_MOMENTUM.remove(id);
    }

    private static void updateModifier(AttributeInstance attribute, ResourceLocation id, boolean active, double amount) {
		updateModifier(attribute, id, active, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}

	private static void updateModifier(AttributeInstance attribute, ResourceLocation id, boolean active, double amount,
			AttributeModifier.Operation operation) {
        if (attribute == null) return;
        AttributeModifier existing = attribute.getModifier(id);
		if (active && existing != null
				&& (Math.abs(existing.amount() - amount) > .000001D || existing.operation() != operation)) {
            attribute.removeModifier(id);
            existing = null;
        }
		if (active && existing == null) attribute.addTransientModifier(new AttributeModifier(id, amount, operation));
        else if (!active && existing != null) attribute.removeModifier(id);
    }

    private record TickSnapshot(Vec3 position, int food, float saturation, float exhaustion) {}

    private record CarrionSnapshot(MobEffectInstance poison, MobEffectInstance hunger, MobEffectInstance nausea) {
        static CarrionSnapshot capture(ServerPlayer player) {
            return new CarrionSnapshot(copy(player.getEffect(MobEffects.POISON)), copy(player.getEffect(MobEffects.HUNGER)), copy(player.getEffect(MobEffects.CONFUSION)));
        }
        void restore(ServerPlayer player) {
            restore(player, MobEffects.POISON, poison);
            restore(player, MobEffects.HUNGER, hunger);
            restore(player, MobEffects.CONFUSION, nausea);
        }
        private static MobEffectInstance copy(MobEffectInstance effect) { return effect == null ? null : new MobEffectInstance(effect); }
        private static void restore(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> type, MobEffectInstance old) {
            player.removeEffect(type);
            if (old != null) player.addEffect(new MobEffectInstance(old));
        }
    }
}
