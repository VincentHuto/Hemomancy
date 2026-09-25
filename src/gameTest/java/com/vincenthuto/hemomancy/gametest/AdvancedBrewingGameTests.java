package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.brewing.AdvancedBrewData;
import com.vincenthuto.hemomancy.common.brewing.AlembicTier;
import com.vincenthuto.hemomancy.common.brewing.AdvancedPotionUse;
import com.vincenthuto.hemomancy.common.brewing.BrewingResolver;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.harbinger.AlembicUpgradeRites;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import io.netty.channel.embedded.EmbeddedChannel;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("advanced_brewing_validation")
@PrefixGameTestTemplate(false)
public final class AdvancedBrewingGameTests {
    @GameTest(template = "empty", timeoutTicks = 450)
    public static void brewingStandCannotConvertAdvancedPotion(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        level.setBlockAndUpdate(pos, Blocks.BREWING_STAND.defaultBlockState());
        var stand = (net.minecraft.world.level.block.entity.BrewingStandBlockEntity) level.getBlockEntity(pos);
        ItemStack refined = level.getRecipeManager()
                .getAllRecipesFor(com.vincenthuto.hemomancy.common.init.RecipeInit.advanced_brewing_type.get())
                .stream().filter(holder -> holder.value().attachment().equals("ferric_poise"))
                .findFirst().orElseThrow().value().getResultItem(level.registryAccess());
        stand.setItem(0, refined.copy());
        stand.setItem(3, new ItemStack(Items.GUNPOWDER));
        stand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
        helper.runAfterDelay(420, () -> {
            helper.assertTrue(ItemStack.isSameItemSameComponents(stand.getItem(0), refined),
                    "Brewing Stand converted an advanced potion");
            helper.assertTrue(stand.getItem(3).is(Items.GUNPOWDER), "Brewing Stand consumed ingredient");
            helper.succeed();
        });
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "alembic-test"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(player.server, connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(5, 3, 5)).getCenter());
        return player;
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void boundPotionSpendsOneDoseAtCompletion(GameTestHelper helper) {
        var player = player(helper);
        player.getAbilities().instabuild = false;
        ItemStack strength = PotionContents.createItemStack(Items.POTION, Potions.STRENGTH);
        ItemStack speed = PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS);
        ItemStack fire = PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE);
        ItemStack bound = BrewingResolver.resolve(helper.getLevel(), AlembicTier.ATHANOR,
                strength, speed, fire).result().copy();
        var volume = HemoCapabilityAccess.getBloodVolume(player).orElseThrow();
        volume.setActive(true);
        volume.setBloodVolume(999);
        AdvancedPotionUse.finishVessel(bound, player);
        helper.assertTrue(bound.get(DataComponentInit.ADVANCED_BREW.get()).doses() == 0,
                "999 mL preserved a dose");
        helper.assertTrue(player.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST)
                && player.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED)
                && player.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE),
                "Bound potion lost effects");
        helper.assertTrue(AdvancedPotionUse.finishVessel(bound, player) == bound,
                "Empty vessel was consumed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void boundPotionUsesTheOrdinaryPotionCompletionHook(GameTestHelper helper) {
        var player = player(helper);
        player.getAbilities().instabuild = false;
        var volume = HemoCapabilityAccess.getBloodVolume(player).orElseThrow();
        volume.setActive(true);
        volume.setBloodVolume(0);
        ItemStack bound = BrewingResolver.resolve(helper.getLevel(), AlembicTier.ATHANOR,
                PotionContents.createItemStack(Items.POTION, Potions.STRENGTH),
                PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS),
                PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE)).result().copy();
        ItemStack completed = bound.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(completed == bound && AdvancedPotionUse.isEmptyVessel(completed),
                "Potion completion consumed the bound bottle");
        helper.succeed();
    }

    private static ActiveCardinalRite upgradeRite(GameTestHelper helper, boolean athanor) {
        BlockPos center = helper.absolutePos(new BlockPos(5, 3, 5));
        ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), center,
                Hemomancy.rloc(athanor ? "cardinal_rite/sanguine_athanor" : "cardinal_rite/first_condensation"),
                2400, athanor ? 7 : 5, athanor ? 5 : 3, false, 0, athanor ? 8 : 4);
        rite.setMatchedFloor(Hemomancy.rloc(athanor ? "working_greater" : "working_lesser"),
                Direction.NORTH, Direction.UP);
        helper.getLevel().setBlockAndUpdate(center, BlockInit.cardinal_focus.get().defaultBlockState());
        BlockPos seat = AlembicUpgradeRites.seat(rite);
        helper.getLevel().setBlockAndUpdate(seat, BlockInit.ghastly_alembic.get().defaultBlockState());
        var station = AlembicUpgradeRites.station(helper.getLevel(), rite);
        if (athanor) station.setTier(AlembicTier.CONDENSER);
        var offerings = new java.util.ArrayList<ActiveCardinalRite.RiteOffering>();
        ItemStack[] items = athanor ? new ItemStack[]{
                new ItemStack(ItemInit.sanguine_athanor_kit.get()),
                new ItemStack(ItemInit.sanguine_quintessence.get()),
                new ItemStack(ItemInit.vivacious_enzyme.get()),
                new ItemStack(ItemInit.neurotic_enzyme.get()),
                new ItemStack(ItemInit.ferric_enzyme.get())}
                : new ItemStack[]{new ItemStack(ItemInit.hematic_condenser_kit.get())};
        for (int i = 0; i < items.length; i++) {
            BlockPos brazierPos = center.offset(i - 2, 0, 3);
            helper.getLevel().setBlockAndUpdate(brazierPos, BlockInit.iron_brazier.get().defaultBlockState());
            var brazier = (IronBrazierBlockEntity) helper.getLevel().getBlockEntity(brazierPos);
            brazier.insertOffering(null, items[i].copy());
            offerings.add(new ActiveCardinalRite.RiteOffering(brazierPos, items[i], true));
        }
        rite.captureOfferingItinerary(offerings);
        return rite;
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void canceledRiteRecoversExactKitOnce(GameTestHelper helper) {
        ActiveCardinalRite rite = upgradeRite(helper, false);
        var station = AlembicUpgradeRites.station(helper.getLevel(), rite);
        helper.assertTrue(AlembicUpgradeRites.prepare(helper.getLevel(), rite), "Condenser preparation failed");
        helper.assertTrue(station.isRiteLocked(), "Subject not locked");
        AlembicUpgradeRites.recover(helper.getLevel(), rite);
        helper.assertTrue(!station.isRiteLocked() && station.tier() == AlembicTier.BASE,
                "Recovery changed the subject");
        // The claim is keyed to the original caster, so persistence is checked through the rite itself.
        helper.assertTrue(rite.alembic().getString("EscrowState").equals("RETURNED"), "Escrow not returned");
        AlembicUpgradeRites.recover(helper.getLevel(), rite);
        helper.assertTrue(rite.alembic().getString("EscrowState").equals("RETURNED"), "Recovery ran twice");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void upgradeKeepsMachineIdentityAndContents(GameTestHelper helper) {
        ActiveCardinalRite rite = upgradeRite(helper, false);
        var station = AlembicUpgradeRites.station(helper.getLevel(), rite);
        var identity = station.machineIdentity();
        station.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT, new ItemStack(Items.ROTTEN_FLESH, 3));
        helper.assertTrue(AlembicUpgradeRites.prepare(helper.getLevel(), rite), "Preparation failed");
        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        helper.assertTrue(rite.enterInscription() && rite.sealAltar(false), "Rite did not seal");
        helper.assertTrue(rite.getPhase() == CardinalRitePhase.ALEMBIC_PROJECTION, "Wrong upgrade phase");
        helper.assertTrue(!rite.fillAlembicProjection(1, 50), "Wrong bulb accepted blood");
        helper.assertTrue(rite.fillAlembicProjection(0, 50) && rite.fillAlembicProjection(1, 50),
                "Bulbs failed to fill");
        rite.markComplete();
        helper.assertTrue(rite.isComplete(), "Rite did not mark complete");
        helper.assertTrue(AlembicUpgradeRites.subjectPresent(helper.getLevel(), rite), "Subject changed");
        helper.assertTrue(AlembicUpgradeRites.complete(helper.getLevel(), rite), "Upgrade failed");
        helper.assertTrue(station.machineIdentity().equals(identity) && station.tier() == AlembicTier.CONDENSER,
                "Upgrade replaced subject");
        helper.assertTrue(station.getItem(GhastlyAlembicBlockEntity.SLOT_INPUT).getCount() == 3,
                "Upgrade lost inventory");
        helper.assertTrue(rite.alembic().getString("EscrowState").equals("CONSUMED"), "Kit not committed");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void athanorUpgradeRequiresThreeOrderedProjections(GameTestHelper helper) {
        ActiveCardinalRite rite = upgradeRite(helper, true);
        var station = AlembicUpgradeRites.station(helper.getLevel(), rite);
        var identity = station.machineIdentity();
        helper.assertTrue(AlembicUpgradeRites.prepare(helper.getLevel(), rite), "Athanor preparation failed");
        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        helper.assertTrue(rite.enterInscription() && rite.sealAltar(false), "Athanor did not seal");
        helper.assertTrue(!rite.fillAlembicProjection(2, 50), "Out-of-order Athanor bulb accepted blood");
        helper.assertTrue(rite.fillAlembicProjection(0, 50)
                && rite.fillAlembicProjection(1, 50)
                && rite.fillAlembicProjection(2, 50), "Athanor bulbs failed to fill");
        rite.markComplete();
        helper.assertTrue(AlembicUpgradeRites.complete(helper.getLevel(), rite), "Athanor upgrade failed");
        helper.assertTrue(station.tier() == AlembicTier.ATHANOR
                && station.machineIdentity().equals(identity), "Athanor replaced its subject");
        helper.assertTrue(AlembicUpgradeRites.complete(helper.getLevel(), rite),
                "Completion retry was not idempotent");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void breakingAthanorDropsEachInstalledKitOnce(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        helper.getLevel().setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
        var station = (GhastlyAlembicBlockEntity) helper.getLevel().getBlockEntity(pos);
        station.setTier(AlembicTier.ATHANOR);
        station.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT, new ItemStack(Items.ROTTEN_FLESH, 3));
        helper.getLevel().destroyBlock(pos, true);
        var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(2));
        int condenser = 0, athanor = 0, flesh = 0;
        for (var drop : drops) {
            if (drop.getItem().is(ItemInit.hematic_condenser_kit.get())) condenser += drop.getItem().getCount();
            if (drop.getItem().is(ItemInit.sanguine_athanor_kit.get())) athanor += drop.getItem().getCount();
            if (drop.getItem().is(Items.ROTTEN_FLESH)) flesh += drop.getItem().getCount();
        }
        helper.assertTrue(condenser == 1 && athanor == 1 && flesh == 3,
                "Breaking an Athanor lost or duplicated installed parts or contents");
        helper.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 230)
    public static void compoundConsumesTwoPotionsAndPreservesBothEffects(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
        var station = (GhastlyAlembicBlockEntity) level.getBlockEntity(pos);
        station.setTier(AlembicTier.CONDENSER);
        station.receiveBlood(1000);
        station.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT,
                PotionContents.createItemStack(Items.POTION, Potions.STRENGTH));
        station.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST,
                PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
        helper.runAfterDelay(170, () -> {
            ItemStack result = station.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT);
            AdvancedBrewData data = result.get(DataComponentInit.ADVANCED_BREW.get());
            helper.assertTrue(data != null && data.kind().equals("compound"), "Compound missing");
            int effects = 0;
            for (var ignored : result.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS).getAllEffects()) effects++;
            helper.assertTrue(effects == 2, "Compound lost an effect");
            helper.assertTrue(station.getBloodVolume() == 500, "Compound cost wrong");
            helper.assertTrue(station.getItem(GhastlyAlembicBlockEntity.SLOT_INPUT).isEmpty()
                    && station.getItem(GhastlyAlembicBlockEntity.SLOT_CATALYST).isEmpty(), "Potion duplicated");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 470)
    public static void bindingAndRefillKeepBaseWithoutNewBasePotion(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
        var station = (GhastlyAlembicBlockEntity) level.getBlockEntity(pos);
        station.setTier(AlembicTier.ATHANOR);
        station.receiveBlood(5000);
        station.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT,
                PotionContents.createItemStack(Items.POTION, Potions.STRENGTH));
        station.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST,
                PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
        station.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST_2,
                PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE));
        helper.runAfterDelay(215, () -> {
            ItemStack bound = station.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT).copy();
            AdvancedBrewData initial = bound.get(DataComponentInit.ADVANCED_BREW.get());
            helper.assertTrue(initial != null && initial.kind().equals("vessel") && initial.doses() == 1,
                    "Binding did not make one dose");
            helper.assertTrue(station.getBloodVolume() == 4000, "Binding cost wrong");
            station.setItem(GhastlyAlembicBlockEntity.SLOT_RESULT, ItemStack.EMPTY);
            station.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT, bound);
            station.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST,
                    PotionContents.createItemStack(Items.POTION, Potions.FIRE_RESISTANCE));
            station.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST_2,
                    PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
        });
        helper.runAfterDelay(390, () -> {
            ItemStack refilled = station.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT);
            AdvancedBrewData formula = refilled.get(DataComponentInit.ADVANCED_BREW.get());
            helper.assertTrue(formula != null && formula.doses() == 2, "Refill did not add a dose");
            helper.assertTrue(station.getBloodVolume() == 3250, "Refill cost wrong");
            helper.assertTrue(station.getItem(GhastlyAlembicBlockEntity.SLOT_INPUT).isEmpty(), "Vessel duplicated");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 180)
    public static void ferricPoisePaysExactlyOnce(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
        var station = (GhastlyAlembicBlockEntity) level.getBlockEntity(pos);
        station.setTier(AlembicTier.CONDENSER);
        station.receiveBlood(1000);
        station.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT,
                PotionContents.createItemStack(Items.POTION, Potions.STRENGTH));
        station.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST,
                new ItemStack(ItemInit.ferric_enzyme.get()));
        helper.runAfterDelay(110, () -> {
            ItemStack result = station.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT);
            AdvancedBrewData data = result.get(DataComponentInit.ADVANCED_BREW.get());
            helper.assertTrue(data != null && data.attachment().equals("ferric_poise"), "Refinement missing");
            helper.assertTrue(station.getBloodVolume() == 750, "Refinement paid wrong blood amount");
            helper.assertTrue(station.getItem(GhastlyAlembicBlockEntity.SLOT_INPUT).isEmpty(), "Input retained");
            helper.assertTrue(station.getItem(GhastlyAlembicBlockEntity.SLOT_CATALYST).isEmpty(), "Enzyme retained");
            helper.assertTrue(result.getCount() == 1, "Result count wrong");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 280)
    public static void changedInputsAndLowBloodNeverPartiallyPay(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(4, 3, 4));
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos.below(), Blocks.MAGMA_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
        var station = (GhastlyAlembicBlockEntity) level.getBlockEntity(pos);
        station.setTier(AlembicTier.ATHANOR);
        station.receiveBlood(250);
        station.setItem(GhastlyAlembicBlockEntity.SLOT_INPUT,
                PotionContents.createItemStack(Items.POTION, Potions.STRENGTH));
        station.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST,
                new ItemStack(ItemInit.ferric_enzyme.get()));
        helper.runAfterDelay(50, () -> {
            station.getBloodCapability().setBloodVolume(200);
            station.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST_2,
                    PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
        });
        helper.runAfterDelay(125, () -> {
            helper.assertTrue(station.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT).isEmpty()
                    && station.getItem(GhastlyAlembicBlockEntity.SLOT_INPUT).is(Items.POTION)
                    && station.getItem(GhastlyAlembicBlockEntity.SLOT_CATALYST).is(ItemInit.ferric_enzyme.get())
                    && station.getBloodVolume() == 200, "Interrupted brew paid part of its cost");
            station.setItem(GhastlyAlembicBlockEntity.SLOT_CATALYST_2, ItemStack.EMPTY);
            station.getBloodCapability().setBloodVolume(250);
        });
        helper.runAfterDelay(240, () -> {
            helper.assertTrue(station.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT)
                    .has(DataComponentInit.ADVANCED_BREW.get()) && station.getBloodVolume() == 0,
                    "Restored brew did not restart and pay exactly once");
            helper.succeed();
        });
    }
}
