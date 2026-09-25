package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.harbinger.ScriptoriumRites;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumAffinities;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumProvenance;
import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumUseEvents;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodStructureCraftingHelper;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.EnzymaticScriptoriumMenu;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import io.netty.channel.embedded.EmbeddedChannel;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.GameType;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder("scriptorium_validation")
@PrefixGameTestTemplate(false)
public final class EnzymaticScriptoriumGameTests {
    @GameTest(template = "empty", timeoutTicks = 100, batch = "scriptorium")
    public static void loadedEnzymeDeterminesPickaxeEnchantment(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(3);
        var blood = HemoCapabilityAccess.requireBloodVolume(player);
        blood.setActive(true);
        blood.setBloodVolume(1000);
        player.giveExperienceLevels(40);
        BlockPos pos = helper.absolutePos(new BlockPos(5, 3, 5));
        helper.getLevel().setBlockAndUpdate(pos, BlockInit.enzymatic_scriptorium.get().defaultBlockState());
        var station = (EnzymaticScriptoriumBlockEntity) helper.getLevel().getBlockEntity(pos);
        station.receiveBlood(1000);
        for (var tendency : new EnumBloodTendency[]{EnumBloodTendency.DUCTILIS, EnumBloodTendency.FERRIC}) {
            for (int i = 0; i < 8; i++) station.setItem(i, ItemStack.EMPTY);
            station.setItem(tendency.ordinal(), new ItemStack(EnumBloodTendency.getRepEnzyme(tendency), 64));
            station.setItem(8, new ItemStack(Items.DIAMOND_PICKAXE));
            station.setItem(9, new ItemStack(Items.LAPIS_LAZULI, 3));
            var menu = new EnzymaticScriptoriumMenu(1, player.getInventory(), station);
            helper.assertTrue(station.getItem(tendency.ordinal()).getCount() == 64, "Reservoir truncated full stack");
            for (int i = 0; i < 3; i++) menu.clickMenuButton(player, tendency.ordinal());
            int offer = -1;
            for (int i = 0; i < 3; i++) if (menu.offerCost(i) > 0) { offer = i; break; }
            helper.assertTrue(offer >= 0 && menu.clickMenuButton(player, offer + 8), "Loaded enzyme produced no usable offer");
            helper.assertTrue(station.getItem(tendency.ordinal()).getCount() == 61, "Selected three doses did not leave 61 stored");
            var expected = tendency == EnumBloodTendency.DUCTILIS ? Enchantments.EFFICIENCY : Enchantments.UNBREAKING;
            helper.assertTrue(!station.getItem(8).getEnchantments().isEmpty(), "No enchantment applied");
            for (var enchantment : station.getItem(8).getEnchantments().keySet())
                helper.assertTrue(enchantment.is(expected), "Loaded tendency allowed an unrelated enchantment");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "scriptorium")
    public static void powerSourcesRequireRangeAndClearGap(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(5, 4, 5));
        var level = helper.getLevel();
        for (int x = -3; x <= 3; x++) for (int y = 0; y <= 1; y++) for (int z = -3; z <= 3; z++)
            level.setBlockAndUpdate(pos.offset(x, y, z), Blocks.AIR.defaultBlockState());
        helper.assertTrue(com.vincenthuto.hemomancy.common.enchanting.ScriptoriumPower.power(level, pos) == 0,
                "Empty surroundings provided power");
        BlockPos offset = new BlockPos(2, 0, 0);
        level.setBlockAndUpdate(pos.offset(offset), Blocks.BOOKSHELF.defaultBlockState());
        helper.assertTrue(com.vincenthuto.hemomancy.common.enchanting.ScriptoriumPower.power(level, pos) == 1,
                "Bookshelf failed to provide power");
        level.setBlockAndUpdate(pos.offset(offset), BlockInit.blood_crystal.get().defaultBlockState());
        helper.assertTrue(com.vincenthuto.hemomancy.common.enchanting.ScriptoriumPower.power(level, pos) == 1,
                "Tagged blood crystal failed to provide power");
        level.setBlockAndUpdate(pos.offset(1, 0, 0), Blocks.STONE.defaultBlockState());
        helper.assertTrue(com.vincenthuto.hemomancy.common.enchanting.ScriptoriumPower.power(level, pos) == 0,
                "Blocked source provided power");
        level.setBlockAndUpdate(pos.offset(1, 0, 0), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos.offset(offset), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos.offset(3, 0, 0), Blocks.BOOKSHELF.defaultBlockState());
        helper.assertTrue(com.vincenthuto.hemomancy.common.enchanting.ScriptoriumPower.power(level, pos) == 0,
                "Untagged or out-of-range block provided power");
        for (BlockPos source : net.minecraft.world.level.block.EnchantingTableBlock.BOOKSHELF_OFFSETS)
            level.setBlockAndUpdate(pos.offset(source), Blocks.BOOKSHELF.defaultBlockState());
        helper.assertTrue(com.vincenthuto.hemomancy.common.enchanting.ScriptoriumPower.power(level, pos) == 15,
                "Power cap drifted");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "scriptorium")
    public static void menuCommitmentAndDenaturationSpendExactResources(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(3);
        var blood = HemoCapabilityAccess.requireBloodVolume(player);
        blood.setActive(true);
        blood.setBloodVolume(1000);
        player.giveExperienceLevels(40);
        BlockPos position = helper.absolutePos(new BlockPos(5, 3, 5));
        helper.getLevel().setBlockAndUpdate(position, BlockInit.enzymatic_scriptorium.get().defaultBlockState());
        var station = (EnzymaticScriptoriumBlockEntity) helper.getLevel().getBlockEntity(position);
        station.receiveBlood(1000);
        for (int i = 0; i < 8; i++)
            station.setItem(i, new ItemStack(EnumBloodTendency.getRepEnzyme(EnumBloodTendency.values()[i]), 3));
        station.setItem(EnzymaticScriptoriumBlockEntity.ITEM, new ItemStack(Items.DIAMOND_PICKAXE));
        station.setItem(EnzymaticScriptoriumBlockEntity.LAPIS, new ItemStack(Items.LAPIS_LAZULI, 3));
        EnzymaticScriptoriumMenu menu = new EnzymaticScriptoriumMenu(1, player.getInventory(), station);
        for (int i = 0; i < 3; i++) menu.clickMenuButton(player, EnumBloodTendency.DUCTILIS.ordinal());

        int offer = -1;
        for (int i = 0; i < 3; i++) if (menu.offerCost(i) > 0) { offer = i; break; }
        helper.assertTrue(offer >= 0, "No legal enchanting offer was generated");
        int[] required = new int[8];
        int missing = -1;
        for (int i = 0; i < 8; i++) {
            required[i] = menu.enzymeCost(offer, i);
            if (required[i] > 0) missing = i;
        }
        helper.assertTrue(missing >= 0, "Offer had no enzyme requirement");
        for (int i = 0; i < 8; i++) station.setItem(i, ItemStack.EMPTY);
        helper.assertTrue(!menu.clickMenuButton(player, 8 + offer), "Missing matching enzyme was accepted");
        helper.assertTrue(blood.getBloodVolume() == 1000 && station.getItem(9).getCount() == 3,
                "Rejected offer consumed blood or lapis");
        for (int i = 0; i < 8; i++) station.setItem(i, new ItemStack(EnumBloodTendency.getRepEnzyme(EnumBloodTendency.values()[i]), 3));
        for (int i = 0; i < 3; i++) menu.clickMenuButton(player, EnumBloodTendency.DUCTILIS.ordinal());
        menu.broadcastChanges();
        for (int i = 0; i < 8; i++) required[i] = menu.enzymeCost(offer, i);
        int beforeXp = player.experienceLevel;
        helper.assertTrue(menu.clickMenuButton(player, 8 + offer), "Server rejected a valid inscription");
        helper.assertTrue(station.getBloodVolume() == 1000 - 100 * (offer + 1), "Inscription blood charge drifted");
        for (int i = 0; i < 8; i++)
            helper.assertTrue(station.getItem(i).getCount() == 3 - required[i], "Enzyme cost drifted for tube " + i);
        helper.assertTrue(station.selectedCount() == 0, "Bias selection was not cleared");
        helper.assertTrue(station.getItem(EnzymaticScriptoriumBlockEntity.LAPIS).getCount() == 2 - offer,
                "Lapis cost drifted");
        helper.assertTrue(player.experienceLevel == beforeXp - offer - 1, "XP cost drifted");
        ItemStack item = station.getItem(EnzymaticScriptoriumBlockEntity.ITEM);
        helper.assertTrue(item.has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get()) && !item.getEnchantments().isEmpty(),
                "Inscription did not attach enchantments and provenance");
        item.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(),
                new ScriptoriumProvenance("sanguine_appetite", 1, 0));
        station.setItem(EnzymaticScriptoriumBlockEntity.SHARD, new ItemStack(ItemInit.blood_crystal_shard.get(), 1));
        double beforeDenaturation = station.getBloodVolume();
        helper.assertTrue(menu.clickMenuButton(player, 11), "Denaturation rejected a cursed item");
        helper.assertTrue(station.getBloodVolume() == beforeDenaturation - 300, "Denaturation blood cost drifted");
        helper.assertTrue(station.getItem(EnzymaticScriptoriumBlockEntity.SHARD).isEmpty(), "Denaturation did not spend shard");
        helper.assertTrue(item.get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get()) != null
                && item.get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get()).curse().isBlank(),
                "Denaturation lost provenance or kept the Hematic Curse");
        helper.assertTrue(item.getEnchantments().isEmpty(), "Denaturation kept removable enchantments");
        helper.assertTrue(blood.getBloodVolume() == 1000, "Station operations drained player blood");
        var saved = station.saveWithoutMetadata(helper.getLevel().registryAccess());
        var restored = new EnzymaticScriptoriumBlockEntity(position, station.getBlockState());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(restored.getBloodVolume() == station.getBloodVolume(), "Stored blood did not survive reload");
        helper.assertTrue(restored.receiveBlood(5000) == 2000 - station.getBloodVolume(), "Reservoir accepted overflow");
        helper.assertTrue(restored.drainBlood(250) == 250 && restored.getBloodVolume() == 1750,
                "Reservoir absorption did not drain blood");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "scriptorium")
    public static void anvilAndGrindstoneKeepRedProvenance(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        var unbreaking = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.UNBREAKING);
        ItemStack original = new ItemStack(Items.DIAMOND_PICKAXE);
        original.enchant(unbreaking, 1);
        original.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(),
                new ScriptoriumProvenance("sanguine_appetite", 2, 0));
        BlockPos grindstonePos = helper.absolutePos(new BlockPos(3, 3, 3));
        helper.getLevel().setBlockAndUpdate(grindstonePos, Blocks.GRINDSTONE.defaultBlockState());
        GrindstoneMenu grindstone = new GrindstoneMenu(1, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), grindstonePos));
        grindstone.getSlot(0).set(original.copy());
        ItemStack ground = grindstone.getSlot(2).getItem();
        helper.assertTrue(!ground.isEmpty(), "Grindstone produced no item");
        helper.assertTrue(ground.has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get()),
                "Grindstone removed Scriptorium provenance");
        BlockPos anvilPos = helper.absolutePos(new BlockPos(4, 3, 3));
        helper.getLevel().setBlockAndUpdate(anvilPos, Blocks.ANVIL.defaultBlockState());
        AnvilMenu anvil = new AnvilMenu(2, player.getInventory(),
                ContainerLevelAccess.create(helper.getLevel(), anvilPos));
        anvil.getSlot(0).set(original.copy());
        anvil.setItemName("Scripted Pickaxe");
        ItemStack renamed = anvil.getSlot(2).getItem();
        helper.assertTrue(!renamed.isEmpty() && renamed.has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get()),
                "Anvil removed Scriptorium provenance");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "script-test"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(player.server, connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(helper.absolutePos(new BlockPos(5, 3, 5)).getCenter());
        return player;
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "scriptorium")
    public static void toolCurseAndExcessWearFollowSuccessfulUse(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        tool.set(DataComponentInit.SCRIPTORIUM_PROVENANCE.get(),
                new ScriptoriumProvenance("sanguine_appetite", 1, 0));
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        var blood = HemoCapabilityAccess.getBloodVolume(player).orElseThrow();
        blood.setBloodVolume(100);
        var event = new BlockEvent.BreakEvent(helper.getLevel(), player.blockPosition(),
                Blocks.STONE.defaultBlockState(), player);
        ScriptoriumUseEvents.breakBlock(event);
        helper.assertTrue(blood.getBloodVolume() == 99, "Tool curse charged the wrong blood amount");
        blood.setBloodVolume(0);
        ScriptoriumUseEvents.breakBlock(event);
        helper.assertTrue(tool.getDamageValue() >= 1, "Unpaid curse did not add wear");
        var unbreaking = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.UNBREAKING);
        tool.enchant(unbreaking, unbreaking.value().getMaxLevel() + 1);
        var efficiency = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.EFFICIENCY);
        tool.enchant(efficiency, efficiency.value().getMaxLevel() + 1);
        int before = tool.getDamageValue();
        ScriptoriumUseEvents.breakBlock(event);
        helper.assertTrue(tool.getDamageValue() > before, "Excess wear was stopped by Unbreaking");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "scriptorium")
    public static void bloodStructureMatchesEnchantingTableFormation(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(5, 3, 5));
        level.setBlockAndUpdate(center, Blocks.ENCHANTING_TABLE.defaultBlockState());
        for (Direction direction : Direction.Plane.HORIZONTAL)
            level.setBlockAndUpdate(center.relative(direction), BlockInit.hematic_iron_block.get().defaultBlockState());
        for (int x : new int[]{-1, 1}) for (int z : new int[]{-1, 1})
            level.setBlockAndUpdate(center.offset(x, 0, z), BlockInit.sanguine_glass.get().defaultBlockState());
        var recipe = BloodStructureRecipe.getStructureByLocation(level, Hemomancy.rloc("enzymatic_scriptorium"));
        helper.assertTrue(recipe != null, "D3 recipe did not load");
        helper.assertTrue(BloodStructureCraftingHelper.findStructurePatternAtHit(recipe, level, center) != null,
                "Authoritative Blood Structure matcher rejected the formation");
        helper.assertTrue(recipe.getBloodCost() == 500, "D3 blood cost drifted");
        helper.succeed();
    }

    private static ActiveCardinalRite rite(GameTestHelper helper, boolean monolithic, Direction forwards) {
        BlockPos center = helper.absolutePos(new BlockPos(5, 3, 5));
        int degree = monolithic ? 7 : 5;
        ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), center,
                Hemomancy.rloc(monolithic ? "cardinal_rite/monolithic_script" : "cardinal_rite/eightfold_script"),
                3600, monolithic ? 9 : 7, degree, false, 1, degree * 4);
        rite.setMatchedFloor(Hemomancy.rloc(monolithic ? "working_grand" : "working_greater"), forwards, Direction.UP);
        return rite;
    }

    private static EnzymaticScriptoriumBlockEntity station(GameTestHelper helper, ActiveCardinalRite rite, boolean monolithic) {
        var level = helper.getLevel();
        BlockPos seat = ScriptoriumRites.seat(rite);
        level.setBlockAndUpdate(seat, BlockInit.enzymatic_scriptorium.get().defaultBlockState().setValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock.STAGE, monolithic ? 1 : 0));
        var station = (EnzymaticScriptoriumBlockEntity) level.getBlockEntity(seat);
        for (int i = 0; i < 8; i++)
            station.setItem(i, new ItemStack(EnumBloodTendency.getRepEnzyme(EnumBloodTendency.values()[i]), monolithic ? 2 : 1));
        return station;
    }

    private static void seal(ActiveCardinalRite rite) {
        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        if (!rite.enterInscription() || !rite.sealAltar(false)) throw new AssertionError("Could not seal rite");
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "scriptorium")
    public static void rotatedEightfoldOrbsPersistAcrossReload(GameTestHelper helper) {
        var eightfoldRecipe = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(),
                Hemomancy.rloc("cardinal_rite/eightfold_script"));
        var monolithicRecipe = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(),
                Hemomancy.rloc("cardinal_rite/monolithic_script"));
        helper.assertTrue(eightfoldRecipe != null && eightfoldRecipe.getCeremony().anchors().size() == 20,
                "Eightfold recipe lost its twenty anchors");
        helper.assertTrue(monolithicRecipe != null && monolithicRecipe.getCeremony().anchors().size() == 28,
                "Monolithic recipe lost its twenty-eight anchors");
        helper.assertTrue(BloodStructureRecipe.getAllRecipes(helper.getLevel()).stream()
                .anyMatch(recipe -> recipe.getId().equals(Hemomancy.rloc("blood_structure/enzymatic_scriptorium"))),
                "D3 Blood Structure recipe failed to load");
        helper.assertTrue(ScriptoriumAffinities.get(net.minecraft.resources.ResourceLocation.parse("minecraft:efficiency")) != null,
                "Efficiency affinity failed to load");
        ActiveCardinalRite east = rite(helper, false, Direction.EAST);
        ActiveCardinalRite south = rite(helper, false, Direction.SOUTH);
        helper.assertTrue(!ScriptoriumRites.orb(east, 0).equals(ScriptoriumRites.orb(south, 0)),
                "Orb placement did not rotate with the floor");
        station(helper, east, false);
        helper.assertTrue(ScriptoriumRites.prepare(helper.getLevel(), east), "Valid station rejected");
        seal(east);
        helper.assertTrue(east.getPhase() == CardinalRitePhase.SCRIPTORIAL_INSCRIPTION, "Skipped writing phase");
        helper.assertTrue(!east.fillScriptorialOrb(1, 50), "Wrong orb accepted blood");
        helper.assertTrue(east.fillScriptorialOrb(0, 23), "First orb rejected blood");
        var reloaded = ActiveCardinalRite.deserialize(east.serialize(helper.getLevel().registryAccess()),
                helper.getLevel().registryAccess());
        helper.assertTrue(reloaded.getScriptorialStage() == 0 && reloaded.getScriptorialBlood(0) == 23,
                "Partial orb progress did not persist");
        helper.assertTrue(reloaded.fillScriptorialOrb(0, 27) && reloaded.getScriptorialStage() == 1,
                "Reloaded rite could not finish its first orb");
        ScriptoriumRites.cleanup(helper.getLevel(), east);
        helper.assertTrue(!ScriptoriumRites.station(helper.getLevel(), east).isRiteLocked(),
                "Cancellation did not release station inventory");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "scriptorium")
    public static void eightfoldUpgradeConsumesExactlyOneOfEach(GameTestHelper helper) {
        ActiveCardinalRite rite = rite(helper, false, Direction.SOUTH);
        EnzymaticScriptoriumBlockEntity station = station(helper, rite, false);
        station.receiveBlood(725);
        var originalState = station.getBlockState();
        helper.assertTrue(ScriptoriumRites.prepare(helper.getLevel(), rite), "Valid station rejected");
        seal(rite);
        for (int i = 0; i < 8; i++) helper.assertTrue(rite.fillScriptorialOrb(i, 50), "Orb " + i + " rejected blood");
        helper.assertTrue(rite.getPhase() == CardinalRitePhase.ORDEAL, "Writing did not lead to ordeal");
        helper.assertTrue(ScriptoriumRites.complete(helper.getLevel(), rite), "Upgrade failed");
        helper.assertTrue(helper.getLevel().getBlockState(ScriptoriumRites.seat(rite)).getValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock.STAGE) == 1,
                "Wrong upgraded block");
        var upgraded = ScriptoriumRites.station(helper.getLevel(), rite);
        helper.assertTrue(upgraded == station, "Upgrade replaced the block entity");
        helper.assertTrue(upgraded.getBloodCapability().getBloodVolume() == 725, "Upgrade lost stored blood");
        helper.assertTrue(upgraded.getBlockState().getBlock() == originalState.getBlock()
                && upgraded.getBlockState().getValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock.FACING)
                == originalState.getValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock.FACING),
                "Upgrade replaced the block or changed its facing");
        for (int i = 0; i < 8; i++) helper.assertTrue(upgraded.getItem(i).isEmpty(), "Enzyme was not consumed once");
        helper.assertTrue(!upgraded.isRiteLocked(), "Upgraded station stayed locked");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "scriptorium")
    public static void monolithicSubjectRejectsMissingEnzymeAndNeedsTwoCircuits(GameTestHelper helper) {
        ActiveCardinalRite rite = rite(helper, true, Direction.WEST);
        EnzymaticScriptoriumBlockEntity station = station(helper, rite, true);
        station.removeItem(0, 1);
        helper.assertTrue(!ScriptoriumRites.prepare(helper.getLevel(), rite), "Missing enzyme accepted");
        station.setItem(0, new ItemStack(EnumBloodTendency.getRepEnzyme(EnumBloodTendency.values()[0]), 2));
        helper.assertTrue(ScriptoriumRites.prepare(helper.getLevel(), rite), "Repaired subject rejected");
        seal(rite);
        for (int i = 0; i < 8; i++) rite.fillScriptorialOrb(i, 50);
        helper.assertTrue(rite.getPhase() == CardinalRitePhase.SCRIPTORIAL_INSCRIPTION
                && rite.getScriptorialStage() == 8, "Second circuit was skipped");
        for (int i = 0; i < 8; i++) rite.fillScriptorialOrb(i, 50);
        helper.assertTrue(ScriptoriumRites.complete(helper.getLevel(), rite), "Monolithic upgrade failed");
        helper.assertTrue(helper.getLevel().getBlockState(ScriptoriumRites.seat(rite)).getValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock.STAGE) == 2,
                "Wrong Monolithic result");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100, batch = "scriptorium")
    public static void legacyStagesAndMinedStagePersist(GameTestHelper helper) {
        var block = BlockInit.enzymatic_scriptorium.get();
        for (int stage = 1; stage <= 2; stage++) {
            var tag = new net.minecraft.nbt.CompoundTag();
            tag.putString("Name", "hemomancy:" + (stage == 1 ? "eightfold_scriptorium" : "monolithic_scriptorium"));
            var props = new net.minecraft.nbt.CompoundTag();
            props.putString("facing", "east");
            tag.put("Properties", props);
            com.vincenthuto.hemomancy.common.enchanting.ScriptoriumLegacyMigration.migrateState(tag);
            var state = net.minecraft.nbt.NbtUtils.readBlockState(helper.getLevel().holderLookup(net.minecraft.core.registries.Registries.BLOCK), tag);
            helper.assertTrue(state.is(block) && state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock.STAGE) == stage,
                    "Legacy station lost its stage");
            helper.assertTrue(state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock.FACING) == Direction.EAST,
                    "Legacy station lost its facing");
            var drops = net.minecraft.world.level.block.Block.getDrops(state, helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)), null);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(block.asItem()), "Stage did not drop the shared item");
            var restored = drops.getFirst().get(net.minecraft.core.component.DataComponents.BLOCK_STATE).apply(block.defaultBlockState());
            helper.assertTrue(restored.getValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.EnzymaticScriptoriumBlock.STAGE) == stage,
                    "Mining lost the stage");
        }
        helper.succeed();
    }
}
