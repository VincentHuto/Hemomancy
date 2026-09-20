package com.vincenthuto.hemomancy.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.*;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.*;
import com.vincenthuto.hemomancy.common.menu.tile.functional.PhlebotomistsCabinetMenu;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("blood_injection_validation")
@PrefixGameTestTemplate(false)
public final class PhlebotomistsCabinetGameTests {
    @GameTest(template = "empty")
    public static void worldInspectionTargetsEveryFacingAndCompartment(GameTestHelper h) {
        for (var facing : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            for (int cell = 0; cell < 9; cell++) {
                // Front-view columns match the GUI: cell zero is the upper-left compartment.
                double x = (12 - cell % 3 * 4) / 16.0, y = (12 - cell / 3 * 4) / 16.0;
                var eye = inspectionWorld(facing, new net.minecraft.world.phys.Vec3(x, y, -2));
                var hit = inspectionWorld(facing, new net.minecraft.world.phys.Vec3(x, y, 0));
                h.assertTrue(com.vincenthuto.hemomancy.common.tile.harbinger.functional.CabinetInspection.cellAt(facing, eye, hit) == cell,
                        "Incorrect world compartment for " + facing + " cell " + cell);
            }
            for (double x : new double[]{0, 1.5, 6, 10, 14.5, 16}) {
                var eye = inspectionWorld(facing, new net.minecraft.world.phys.Vec3(x / 16, .75, -2));
                var hit = inspectionWorld(facing, new net.minecraft.world.phys.Vec3(x / 16, .75, 0));
                h.assertTrue(com.vincenthuto.hemomancy.common.tile.harbinger.functional.CabinetInspection.cellAt(facing, eye, hit) == -1,
                        "Frame or divider selected a compartment");
            }
        }
        h.succeed();
    }
    private static net.minecraft.world.phys.Vec3 inspectionWorld(net.minecraft.core.Direction facing, net.minecraft.world.phys.Vec3 p) {
        return switch (facing) {
            case NORTH -> p;
            case SOUTH -> new net.minecraft.world.phys.Vec3(1 - p.x, p.y, 1 - p.z);
            case EAST -> new net.minecraft.world.phys.Vec3(1 - p.z, p.y, p.x);
            case WEST -> new net.minecraft.world.phys.Vec3(p.z, p.y, 1 - p.x);
            default -> p;
        };
    }
    @GameTest(template = "empty")
    public static void cabinetIsRegisteredWithCompartmentStates(GameTestHelper h) {
        var block = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse("hemomancy:phlebotomists_cabinet"));
        h.assertTrue(block.isPresent(), "Phlebotomist's Cabinet is not registered");
        h.assertTrue(block.get().getStateDefinition().getPossibleStates().size() == 16 * 512, "Cabinet must retain facing, door and glazing states with nine independent occupancy flags");
        h.succeed();
    }

    private static ItemStack vial(String source) {
        var stack = new ItemStack(ItemInit.bloody_vial.get());
        var tag = new CompoundTag();
        tag.putString("entity_type", source);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
    private static PhlebotomistsCabinetBlockEntity place(GameTestHelper h) {
        h.setBlock(new BlockPos(0, 2, 0), BlockInit.phlebotomists_cabinet.get());
        return (PhlebotomistsCabinetBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(new BlockPos(0, 2, 0)));
    }
    private static Player player(GameTestHelper h, PhlebotomistsCabinetBlockEntity cabinet) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(cabinet.getBlockPos().getCenter());
        return player;
    }
    private static PhlebotomistsCabinetMenu menu(Player player, PhlebotomistsCabinetBlockEntity cabinet) {
        var menu = new PhlebotomistsCabinetMenu(1, player.getInventory(), cabinet);
        player.containerMenu = menu;
        return menu;
    }
    @GameTest(template = "empty")
    public static void identityCapacitySimulationAndDefensiveCopies(GameTestHelper h) {
        int[] changes = {0, 0};
        var storage = new CabinetStorage(display -> { changes[0]++; if (display) changes[1]++; });
        var a = vial("minecraft:pig");
        h.assertTrue(storage.insertItem(0, a, true).isEmpty() && storage.totalCount() == 0 && changes[0] == 0, "Simulation changed cabinet");
        storage.insertItem(0, a, false);
        a.set(DataComponents.CUSTOM_NAME, Component.literal("Changed externally"));
        h.assertTrue(!storage.getStackInSlot(0).has(DataComponents.CUSTOM_NAME), "Incoming exemplar was retained by reference");
        var copy = storage.getStackInSlot(0);
        copy.set(DataComponents.CUSTOM_NAME, Component.literal("Mutated read"));
        h.assertTrue(!storage.getStackInSlot(0).has(DataComponents.CUSTOM_NAME), "Inspection exposed writable exemplar");
        h.assertTrue(!storage.insertItem(0, a, false).isEmpty(), "Different components merged");
        for (int i = 1; i < 64; i++) storage.insertItem(0, vial("minecraft:pig"), false);
        h.assertTrue(storage.count(0) == 64 && changes[1] == 1 && changes[0] == 64, "Capacity or display-change accounting wrong");
        h.assertTrue(!storage.insertItem(0, vial("minecraft:pig"), false).isEmpty(), "Capacity overflow accepted");
        h.assertTrue(storage.findInsertionSlot(vial("minecraft:pig")) == 1, "Full match did not fall back to empty cell");
        int before = changes[0];
        h.assertTrue(storage.extractItem(0, 0, false).isEmpty(), "Zero extraction removed a sample");
        h.assertTrue(storage.extractItem(0, 64, true).getCount() == 1 && storage.count(0) == 64 && changes[0] == before, "Simulated extraction changed cabinet or overstacked");
        h.assertTrue(storage.extractItem(0, 64, false).getCount() == 1 && storage.count(0) == 63, "Extraction returned virtual quantity");
        var identifiedA = vial("minecraft:pig"); var identifiedB = identifiedA.copy();
        BloodSampleData.identify(identifiedA); BloodSampleData.identify(identifiedB);
        storage.insertItem(1, identifiedA, false);
        h.assertTrue(storage.insertItem(1, identifiedB, false).isEmpty(), "Independent identification fragmented identity");
        h.assertTrue(!storage.insertItem(0, identifiedB, false).isEmpty(), "Identified and unknown vials merged");
        h.assertTrue(storage.findInsertionSlot(identifiedB) == 1, "Matching cell was not preferred");
        h.assertTrue(new ItemStack(ItemInit.bloody_vial.get()).getMaxStackSize() == 64 && identifiedA.getMaxStackSize() == 1,
                "Empty vial stack limit or filled sample identity changed");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void malformedLoadsPreserveSurvivorsAndUnresolvedSources(GameTestHelper h) {
        var storage = new CabinetStorage(ignored -> {});
        for (var invalid : new ItemStack[]{new ItemStack(Items.STONE), new ItemStack(ItemInit.bloody_vial.get()),
                new ItemStack(ItemInit.vial_rack.get()), vial("bad source")})
            h.assertTrue(!storage.insertItem(0, invalid, false).isEmpty(), "Invalid new item accepted");
        var partial = vial("minecraft:pig");
        var data = partial.get(DataComponents.CUSTOM_DATA).copyTag(); data.putBoolean("state", false);
        partial.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        h.assertTrue(!storage.insertItem(0, partial, false).isEmpty(), "Partial sample accepted");
        storage.insertItem(0, vial("missing:creature"), false);
        storage.insertItem(1, vial("minecraft:pig"), false);
        storage.insertItem(2, vial("minecraft:cow"), false);
        var saved = storage.save(h.getLevel().registryAccess());
        var entries = saved.getList("Entries", Tag.TAG_COMPOUND);
        entries.getCompound(0).putInt("Count", 1000);
        entries.getCompound(1).putInt("Count", -7);
        var bad = new CompoundTag(); bad.putInt("Count", 3); bad.put("Specimen", new ItemStack(Items.STONE).save(h.getLevel().registryAccess()));
        entries.set(3, bad);
        var undecodable = new CompoundTag(); undecodable.putInt("Count", 7);
        var missingItem = new CompoundTag(); missingItem.putString("id", "missing:no_item"); missingItem.putInt("count", 1);
        undecodable.put("Specimen", missingItem); entries.set(4, undecodable);
        // Old unreadable filled entries remain extractable even though new malformed input is refused.
        var unreadable = new CompoundTag(); unreadable.putInt("Count", 2); unreadable.put("Specimen", vial("bad source").save(h.getLevel().registryAccess()));
        entries.set(5, unreadable);
        storage.load(saved, h.getLevel().registryAccess());
        h.assertTrue(storage.count(0) == 64 && storage.getStackInSlot(0).getCount() == 1, "Unresolved source was lost or count not bounded");
        h.assertTrue(BloodSampleData.rawSource(storage.getStackInSlot(0)).equals("missing:creature"), "Unresolved source changed");
        h.assertTrue(storage.count(1) == 0 && storage.count(2) == 1 && storage.count(3) == 0 && storage.count(4) == 0 && storage.count(5) == 2, "Malformed load damaged surviving entries");
        var restored = new CabinetStorage(ignored -> {});
        restored.load(storage.save(h.getLevel().registryAccess()), h.getLevel().registryAccess());
        h.assertTrue(restored.totalCount() == 67, "Save/load changed quantity");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void everyVirtualClickIsAccountedAndOrdinarySlotsStillWork(GameTestHelper h) {
        var cabinet = place(h); var player = player(h, cabinet); var menu = menu(player, cabinet);
        menu.setCarried(vial("minecraft:pig"));
        menu.clicked(0, 0, ClickType.PICKUP, player);
        h.assertTrue(menu.getCarried().isEmpty() && cabinet.storage().count(0) == 1, "Click failed to deposit");
        menu.setCarried(vial("minecraft:cow")); menu.clicked(0, 0, ClickType.PICKUP, player);
        h.assertTrue(cabinet.storage().count(0) == 1 && BloodSampleData.rawSource(menu.getCarried()).equals("minecraft:cow"), "Nonmatching click swapped or deleted vials");
        menu.setCarried(ItemStack.EMPTY);
        for (var click : new ClickType[]{ClickType.SWAP, ClickType.CLONE, ClickType.THROW, ClickType.PICKUP_ALL, ClickType.QUICK_CRAFT}) {
            menu.clicked(0, 0, click, player);
            h.assertTrue(cabinet.storage().count(0) == 1 && menu.getCarried().isEmpty(), "Shortcut bypassed accounting: " + click);
        }
        for (int button : new int[]{0,1}) {
            menu.clicked(0, button, ClickType.PICKUP, player);
            h.assertTrue(menu.getCarried().getCount() == 1 && cabinet.storage().count(0) == 0, "Click did not extract exactly one");
            menu.clicked(0, button, ClickType.PICKUP, player);
        }
        player.getInventory().setItem(9, new ItemStack(Items.STONE, 17));
        menu.clicked(9, 0, ClickType.PICKUP, player);
        h.assertTrue(menu.getCarried().getCount() == 17 && player.getInventory().getItem(9).isEmpty(), "Ordinary inventory pickup broken");
        menu.clicked(10, 0, ClickType.PICKUP, player);
        h.assertTrue(player.getInventory().getItem(10).getCount() == 17, "Ordinary inventory deposit broken");
        player.getInventory().setItem(9, vial("minecraft:pig"));
        menu.clicked(9, 0, ClickType.QUICK_MOVE, player);
        h.assertTrue(cabinet.storage().count(0) == 2 && player.getInventory().getItem(9).isEmpty(), "Shift insertion did not prefer identity");
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        h.assertTrue(cabinet.storage().totalCount() == 0, "Shift withdrawal failed");
        int vials = 0;
        for (int i = 0; i < 36; i++) if (player.getInventory().getItem(i).getItem() instanceof BloodVialItem) {
            vials++; h.assertTrue(player.getInventory().getItem(i).getCount() == 1, "Withdrawal created an illegal stack");
        }
        h.assertTrue(vials == 2, "Shift withdrawal lost/duplicated vials");
        cabinet.storage().insertItem(0, vial("minecraft:pig"), false);
        player.containerMenu = player.inventoryMenu;
        menu.clicked(0,0,ClickType.PICKUP,player);
        h.assertTrue(cabinet.storage().count(0) == 1, "Inactive menu accepted a stale click");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void fullInventoryCapacityAndTwoViewersCannotDuplicate(GameTestHelper h) {
        var cabinet = place(h); var a = player(h, cabinet); var b = player(h, cabinet);
        var first = menu(a, cabinet); var second = menu(b, cabinet);
        for (int i = 0; i < 64; i++) cabinet.storage().insertItem(0, vial("minecraft:pig"), false);
        first.setCarried(vial("minecraft:pig")); first.clicked(0,0,ClickType.PICKUP,a);
        h.assertTrue(first.getCarried().getCount() == 1 && cabinet.storage().count(0) == 64, "Full cell consumed cursor");
        first.setCarried(ItemStack.EMPTY);
        a.getInventory().setItem(9,vial("minecraft:pig")); first.quickMoveStack(a,9);
        h.assertTrue(cabinet.storage().count(1) == 1, "Full matching cell did not route to empty cell");
        for (int i=0;i<36;i++) a.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        first.quickMoveStack(a,0);
        h.assertTrue(cabinet.storage().count(0) == 64, "Full inventory lost vials");
        a.getInventory().setItem(35,ItemStack.EMPTY); first.quickMoveStack(a,0);
        h.assertTrue(cabinet.storage().count(0) == 63 && a.getInventory().getItem(35).getCount() == 1, "Bounded withdrawal wrong");
        first.clicked(1,0,ClickType.PICKUP,a); second.clicked(1,0,ClickType.PICKUP,b);
        h.assertTrue(first.getCarried().getCount()==1 && second.getCarried().isEmpty(), "Two viewers extracted same vial");
        second.broadcastChanges();
        h.assertTrue(second.count(0)==63 && second.count(1)==0, "Other viewer snapshot stale");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void comparatorBoundariesAndDisplaySnapshots(GameTestHelper h) {
        var cabinet=place(h); int[] totals={0,1,288,575,576}; int[] outputs={0,1,8,14,15};
        for(int j=0;j<totals.length;j++) {
            while(cabinet.storage().totalCount()<totals[j]) {
                int slot=cabinet.storage().findInsertionSlot(vial("minecraft:pig"));
                cabinet.storage().insertItem(slot,vial("minecraft:pig"),false);
            }
            h.assertTrue(cabinet.getBlockState().getAnalogOutputSignal(h.getLevel(),cabinet.getBlockPos())==outputs[j],"Comparator boundary "+totals[j]);
        }
        h.assertTrue(!cabinet.getUpdateTag(h.getLevel().registryAccess()).contains("Storage"), "World update leaked aggregate storage");
        var display=cabinet.getUpdateTag(h.getLevel().registryAccess());
        cabinet.storage().extractItem(0,1,false);
        var updated = cabinet.getUpdateTag(h.getLevel().registryAccess());
        h.assertTrue(display.get("Display").equals(updated.get("Display")), "Count-only change altered representative vials");
        h.assertTrue(updated.getIntArray("DisplayCounts")[0] == 63 && display.getIntArray("DisplayCounts")[0] == 64, "World tooltip count did not update after extraction");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void viewersCloseAfterReplacementDeathAndReload(GameTestHelper h) {
        var cabinet=place(h); var a=player(h,cabinet);var b=player(h,cabinet);
        var first=menu(a,cabinet);var second=menu(b,cabinet);
        first.removed(a); a.containerMenu=a.inventoryMenu;
        for(int i=0;i<8;i++) PhlebotomistsCabinetBlockEntity.tick(h.getLevel(),cabinet.getBlockPos(),cabinet.getBlockState(),cabinet);
        h.assertTrue(cabinet.getBlockState().getValue(PhlebotomistsCabinetBlock.OPEN),"Closed on remaining viewer");
        b.setHealth(0);
        for(int i=0;i<8;i++) PhlebotomistsCabinetBlockEntity.tick(h.getLevel(),cabinet.getBlockPos(),cabinet.getBlockState(),cabinet);
        h.assertTrue(!cabinet.getBlockState().getValue(PhlebotomistsCabinetBlock.OPEN),"Dead viewer held cabinet open");
        var c=player(h,cabinet);menu(c,cabinet);
        cabinet.storage().insertItem(0,vial("minecraft:pig"),false);
        var saved=cabinet.saveWithFullMetadata(h.getLevel().registryAccess());
        cabinet.loadWithComponents(saved,h.getLevel().registryAccess());
        PhlebotomistsCabinetBlockEntity.tick(h.getLevel(),cabinet.getBlockPos(),cabinet.getBlockState(),cabinet);
        h.assertTrue(!cabinet.getBlockState().getValue(PhlebotomistsCabinetBlock.OPEN) && cabinet.storage().count(0)==1,"Reload retained viewer or lost specimen");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void hopperTransfersUseLegalSingleVials(GameTestHelper h) {
        var cabinet=place(h);var pos=cabinet.getBlockPos();
        h.getLevel().setBlock(pos.above(),Blocks.HOPPER.defaultBlockState(),3);
        h.getLevel().setBlock(pos.below(),Blocks.HOPPER.defaultBlockState(),3);
        var top=(HopperBlockEntity)h.getLevel().getBlockEntity(pos.above());
        var bottom=(HopperBlockEntity)h.getLevel().getBlockEntity(pos.below());
        top.setItem(0,vial("minecraft:pig"));top.setItem(1,vial("minecraft:pig"));
        var capability=h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,pos,Direction.UP);
        h.assertTrue(capability!=null,"Missing cabinet capability");
        var player=player(h,cabinet);var menu=menu(player,cabinet);
        for(int i=0;i<18;i++) {
            top.setCooldown(0);HopperBlockEntity.pushItemsTick(h.getLevel(),pos.above(),top.getBlockState(),top);
            bottom.setCooldown(0);HopperBlockEntity.pushItemsTick(h.getLevel(),pos.below(),bottom.getBlockState(),bottom);
            menu.broadcastChanges();
        }
        h.assertTrue(top.isEmpty() && cabinet.storage().totalCount()==0,"Hopper failed to move specimens in both directions");
        h.assertTrue(bottom.getItem(0).getCount()==1 && bottom.getItem(1).getCount()==1,"Hopper duplicated, lost, or overstacked a specimen");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void filledCabinetRefusesSurvivalBreakAndExplosion(GameTestHelper h) {
        var cabinet=place(h); var player=player(h,cabinet);var pos=cabinet.getBlockPos();
        cabinet.storage().insertItem(0,vial("minecraft:pig"),false);
        var event=new BlockEvent.BreakEvent(h.getLevel(),pos,cabinet.getBlockState(),player);
        NeoForge.EVENT_BUS.post(event);
        h.assertTrue(event.isCanceled() && cabinet.getBlockState().getDestroyProgress(player,h.getLevel(),pos)==0,"Filled cabinet was breakable");
        h.getLevel().explode(null,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,4,Level.ExplosionInteraction.BLOCK);
        h.assertTrue(h.getLevel().getBlockEntity(pos)==cabinet && cabinet.storage().count(0)==1,"Explosion destroyed contents");
        h.assertTrue(cabinet.getBlockState().getPistonPushReaction()==net.minecraft.world.level.material.PushReaction.BLOCK,"Piston can move cabinet");
        cabinet.storage().extractItem(0,1,false);
        var empty=new BlockEvent.BreakEvent(h.getLevel(),pos,cabinet.getBlockState(),player);NeoForge.EVENT_BUS.post(empty);
        h.assertTrue(!empty.isCanceled(),"Empty cabinet refused dismantling");
        h.succeed();
    }

    private static void use(PhlebotomistsCabinetBlockEntity cabinet, Player player, ItemStack item) {
        player.setItemInHand(InteractionHand.MAIN_HAND,item);
        var hit=new BlockHitResult(cabinet.getBlockPos().getCenter(),Direction.NORTH,cabinet.getBlockPos(),false);
        cabinet.getBlockState().useItemOn(item,cabinet.getLevel(),player,InteractionHand.MAIN_HAND,hit);
    }
    @GameTest(template = "empty")
    public static void glazingUsesKnapperAndReturnsOnlyOnePane(GameTestHelper h) {
        var cabinet=place(h);var player=player(h,cabinet); var pos=cabinet.getBlockPos();
        cabinet.storage().insertItem(0,vial("minecraft:pig"),false);
        h.getLevel().setBlock(pos,cabinet.getBlockState().setValue(PhlebotomistsCabinetBlock.FACING,Direction.WEST)
                .setValue(PhlebotomistsCabinetBlock.OPEN,true),3);
        var pane=new ItemStack(Items.GLASS_PANE,2);use(cabinet,player,pane);
        h.assertTrue(pane.getCount()==1 && cabinet.getBlockState().getValue(PhlebotomistsCabinetBlock.GLAZED),"Glazing did not consume one pane");
        h.assertTrue(cabinet.getBlockState().getValue(PhlebotomistsCabinetBlock.FACING)==Direction.WEST
                && cabinet.getBlockState().getValue(PhlebotomistsCabinetBlock.OPEN) && cabinet.storage().count(0)==1,"Glazing changed facing, open state or contents");
        player.setShiftKeyDown(true);use(cabinet,player,new ItemStack(Items.IRON_PICKAXE));
        h.assertTrue(cabinet.getBlockState().getValue(PhlebotomistsCabinetBlock.GLAZED),"Unrelated tool removed glazing");
        var knapper=new ItemStack(ItemInit.hematic_iron_knapper.get());
        for(int i=0;i<36;i++) player.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        use(cabinet,player,knapper);
        h.assertTrue(!cabinet.getBlockState().getValue(PhlebotomistsCabinetBlock.GLAZED) && knapper.getDamageValue()==0,"Knapper removal failed or damaged tool");
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(pos).inflate(1),e->e.getItem().is(Items.GLASS_PANE));
        h.assertTrue(drops.size()==1 && drops.getFirst().getItem().getCount()==1 && drops.getFirst().getX()<pos.getX(),"Pane not returned once in front when inventory full");
        drops.forEach(net.minecraft.world.entity.Entity::discard);
        var creative=h.makeMockPlayer(GameType.CREATIVE);creative.getAbilities().instabuild=true;
        var freePane=new ItemStack(Items.GLASS_PANE);use(cabinet,creative,freePane);
        h.assertTrue(freePane.getCount()==1,"Creative glazing consumed pane");
        creative.setShiftKeyDown(true);use(cabinet,creative,new ItemStack(ItemInit.hematic_iron_knapper.get()));
        h.assertTrue(creative.getInventory().countItem(Items.GLASS_PANE)==0 && !cabinet.getBlockState().getValue(PhlebotomistsCabinetBlock.GLAZED),"Creative removal refunded free pane");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void cabinetItemsNeverCarrySpecimenData(GameTestHelper h) {
        var cabinet=place(h);cabinet.storage().insertItem(0,vial("minecraft:pig"),false);
        var item=new ItemStack(BlockInit.phlebotomists_cabinet.get());
        cabinet.saveToItem(item,h.getLevel().registryAccess());
        h.assertTrue(!item.has(DataComponents.BLOCK_ENTITY_DATA),"Creative pick-block copied stationary specimen inventory");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void emptyLootPreservesGlazingAndPlacementRestoresIt(GameTestHelper h) {
        var cabinet=place(h);var pos=cabinet.getBlockPos();
        h.getLevel().setBlock(pos,cabinet.getBlockState().setValue(PhlebotomistsCabinetBlock.GLAZED,true),3);
        var drops=Block.getDrops(cabinet.getBlockState(),h.getLevel(),pos,cabinet);
        h.assertTrue(drops.size()==1 && drops.getFirst().is(BlockInit.phlebotomists_cabinet.get().asItem()),"Empty cabinet loot missing or duplicated");
        var stack=drops.getFirst();
        h.assertTrue(!stack.has(DataComponents.BLOCK_ENTITY_DATA) && stack.get(DataComponents.BLOCK_STATE).get(PhlebotomistsCabinetBlock.GLAZED),"Drop lost glazing or copied inventory data");
        h.getLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        var player=player(h,cabinet);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var context=new net.minecraft.world.item.context.UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(pos.getCenter(),Direction.UP,pos,false));
        ((BlockItem)stack.getItem()).place(new net.minecraft.world.item.context.BlockPlaceContext(context));
        h.assertTrue(h.getLevel().getBlockState(pos).getValue(PhlebotomistsCabinetBlock.GLAZED),"Placed item did not restore glazing");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void sneakingInteractionsUseTheActualPlayerActionPath(GameTestHelper h) {
        var cabinet=place(h);var pos=cabinet.getBlockPos();
        var player=new net.neoforged.neoforge.common.util.FakePlayer(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"cabinet-sneak"));
        player.setGameMode(GameType.SURVIVAL);player.setPos(pos.getCenter());player.setShiftKeyDown(true);
        var hit=new BlockHitResult(pos.getCenter(),Direction.NORTH,pos,false);
        h.getLevel().setBlock(pos,cabinet.getBlockState().setValue(PhlebotomistsCabinetBlock.GLAZED,true),3);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ItemInit.hematic_iron_knapper.get()));
        player.gameMode.useItemOn(player,h.getLevel(),player.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
        h.assertTrue(!cabinet.getBlockState().getValue(PhlebotomistsCabinetBlock.GLAZED),"Sneak-right-click skipped cabinet's Knapper interaction");
        player.setItemInHand(InteractionHand.MAIN_HAND,vial("minecraft:pig"));
        var result=player.gameMode.useItemOn(player,h.getLevel(),player.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
        h.assertTrue(result.consumesAction() && !player.isUsingItem(),"Sneaking vial fell through to injection");
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void actualSurvivalAndCreativeDestructionRespectContentsPolicy(GameTestHelper h) {
        var cabinet=place(h);var pos=cabinet.getBlockPos();
        var player=new net.neoforged.neoforge.common.util.FakePlayer(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"cabinet-break"));
        player.setGameMode(GameType.SURVIVAL);player.setPos(pos.getCenter());
        for(int i=0;i<64;i++)cabinet.storage().insertItem(0,vial("minecraft:pig"),false);
        h.assertTrue(!player.gameMode.destroyBlock(pos) && h.getLevel().getBlockEntity(pos)==cabinet,"Survival destruction bypassed protection");
        player.setGameMode(GameType.CREATIVE);
        h.assertTrue(player.gameMode.destroyBlock(pos) && h.getLevel().getBlockEntity(pos)==null,"Intentional creative removal was refused");
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(pos).inflate(1));
        h.assertTrue(drops.stream().noneMatch(e->e.getItem().getItem() instanceof BloodVialItem),"Creative removal spilled stored specimens");
        var empty=place(h);player.setGameMode(GameType.SURVIVAL);
        h.assertTrue(player.gameMode.destroyBlock(pos),"Empty cabinet could not be dismantled in survival");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void survivalMobsCannotDestroyFilledCabinets(GameTestHelper h) {
        var cabinet=place(h);var pos=cabinet.getBlockPos();
        cabinet.storage().insertItem(0,vial("minecraft:pig"),false);
        var wither=net.minecraft.world.entity.EntityType.WITHER.create(h.getLevel());
        var dragon=net.minecraft.world.entity.EntityType.ENDER_DRAGON.create(h.getLevel());
        h.assertTrue(!cabinet.getBlockState().canEntityDestroy(h.getLevel(),pos,wither),"Wither can destroy filled cabinet outside explosion path");
        h.assertTrue(!cabinet.getBlockState().canEntityDestroy(h.getLevel(),pos,dragon),"Dragon can destroy filled cabinet outside explosion path");
        h.succeed();
    }
}
