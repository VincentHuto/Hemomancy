package com.vincenthuto.hemomancy.gametest;
import net.minecraft.core.registries.BuiltInRegistries;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.component.*;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.*;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.*;
import com.vincenthuto.hemomancy.common.menu.tile.functional.PhlebotomistsCabinetMenu;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.capabilities.Capabilities;
import java.util.*;

import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("blood_injection_validation")
@PrefixGameTestTemplate(false)
public final class PhlebotomistsFieldCaseGameTests {
    @GameTest(template="empty")
    public static void compartmentModelsFollowContentsAndRecoverAfterLoad(GameTestHelper h) {
        for (var block : List.of(BlockInit.phlebotomists_cabinet.get(), BlockInit.phlebotomists_field_case.get())) {
            h.setBlock(POS, block);
            var entity = h.getLevel().getBlockEntity(h.absolutePos(POS));
            var container = (SpecimenContainer)entity;
            var storage = container.storage();
            for (int cell = 0; cell < 9; cell++) {
                var property = SpecimenDisplayState.OCCUPIED.get(cell);
                h.assertTrue(!entity.getBlockState().getValue(property), "Empty compartment has a model");
                storage.insertItem(cell, vial("cell " + cell), false);
                storage.insertItem(cell, vial("cell " + cell), false);
                h.assertTrue(entity.getBlockState().getValue(property), "Occupied compartment has no model");
                storage.extractItem(cell, 1, false);
                h.assertTrue(entity.getBlockState().getValue(property), "Partial extraction removed the model");
            }
            var saved = entity.saveWithFullMetadata(h.getLevel().registryAccess());
            h.getLevel().setBlock(entity.getBlockPos(), SpecimenDisplayState.empty(entity.getBlockState()), Block.UPDATE_CLIENTS);
            entity.loadWithComponents(saved, h.getLevel().registryAccess());
            if (entity instanceof PhlebotomistsCabinetBlockEntity cabinet)
                PhlebotomistsCabinetBlockEntity.tick(h.getLevel(), entity.getBlockPos(), entity.getBlockState(), cabinet);
            else PhlebotomistsFieldCaseBlockEntity.tick(h.getLevel(), entity.getBlockPos(), entity.getBlockState(), (PhlebotomistsFieldCaseBlockEntity)entity);
            for (int cell = 0; cell < 9; cell++) {
                h.assertTrue(entity.getBlockState().getValue(SpecimenDisplayState.OCCUPIED.get(cell)), "Reload did not restore occupancy");
                storage.extractItem(cell, 1, false);
                h.assertTrue(!entity.getBlockState().getValue(SpecimenDisplayState.OCCUPIED.get(cell)), "Last extraction left a model");
            }
        }
        h.succeed();
    }

    @GameTest(template="empty")
    public static void fieldCaseCanBePlacedAsUnstackableItem(GameTestHelper h) {
        var id=ResourceLocation.parse("hemomancy:phlebotomists_field_case");
        var block=BuiltInRegistries.BLOCK.getOptional(id);
        h.assertTrue(block.isPresent(), "Field Case is not registered");
        h.assertTrue(block.get().asItem().getDefaultInstance().getMaxStackSize()==1, "Cases must not stack");
        h.succeed();
    }

    private static final BlockPos POS=new BlockPos(0,2,0);
    private static ItemStack vial(String name) {
        var stack=new ItemStack(ItemInit.bloody_vial.get());
        var tag=new CompoundTag();tag.putString("entity_type","minecraft:pig");
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME,Component.literal(name));return stack;
    }
    private static PhlebotomistsFieldCaseBlockEntity placed(GameTestHelper h) {
        h.setBlock(POS,BlockInit.phlebotomists_field_case.get());
        return (PhlebotomistsFieldCaseBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(POS));
    }
    private static Player viewer(GameTestHelper h, PhlebotomistsFieldCaseBlockEntity be) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(be.getBlockPos().getCenter().add(0,0,2));return player;
    }
    private static FakePlayer actor(GameTestHelper h,GameType type) {
        var player=new FakePlayer(h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"case-test"));
        player.setGameMode(type);player.setPos(h.absolutePos(POS).getCenter().add(0,0,2));return player;
    }
    private static PhlebotomistsCabinetMenu open(Player player,PhlebotomistsFieldCaseBlockEntity be) {
        var menu=new PhlebotomistsCabinetMenu(ContainerInit.phlebotomists_field_case.get(),1,player.getInventory(),be);
        player.containerMenu=menu;return menu;
    }
    private static ItemStack filledItem(GameTestHelper h,int count) {
        var list=new ArrayList<>(FieldCaseContents.EMPTY.entries());
        var sample=vial("Field specimen");BloodSampleData.identify(sample);
        list.set(0,new SpecimenStack(sample,count));
        var item=new ItemStack(BlockInit.phlebotomists_field_case.get());
        item.set(DataComponentInit.FIELD_CASE_CONTENTS.get(),new FieldCaseContents(list));return item;
    }
    private static InteractionResult placeItem(GameTestHelper h,FakePlayer player,ItemStack item) {
        h.setBlock(POS.below(),Blocks.STONE);player.setItemInHand(InteractionHand.MAIN_HAND,item);
        var at=h.absolutePos(POS.below());var hit=new BlockHitResult(at.getCenter().add(0,.5,0),Direction.UP,at,false);
        return player.gameMode.useItemOn(player,h.getLevel(),item,InteractionHand.MAIN_HAND,hit);
    }
    private static List<ItemEntity> caseDrops(GameTestHelper h) {
        return h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(h.absolutePos(POS)).inflate(3),e->e.getItem().is(BlockInit.phlebotomists_field_case.get().asItem()));
    }
    @GameTest(template="empty")
    public static void exactIdentityHasOneCellAndSixteenVials(GameTestHelper h) {
        var be=placed(h);var storage=be.storage();
        var sample=vial("A");
        for(int i=0;i<16;i++)h.assertTrue(storage.insertItem(0,sample,false).isEmpty(),"Full sample rejected");
        h.assertTrue(!storage.insertItem(0,sample,false).isEmpty() && !storage.insertItem(1,sample,false).isEmpty()
                && storage.findInsertionSlot(sample)==-1,"Identity exceeded sixteen by splitting cells");
        for(int cell=1;cell<9;cell++)for(int i=0;i<16;i++)storage.insertItem(cell,vial("specimen "+cell),false);
        h.assertTrue(storage.totalCount()==144 && storage.findInsertionSlot(vial("tenth"))==-1,"Total capacity or identity count wrong");
        var entry=storage.snapshot().getFirst();var copy=entry.exemplar();copy.set(DataComponents.CUSTOM_NAME,Component.literal("Mutated"));
        h.assertTrue(entry.matches(sample),"Immutable snapshot exposed writable exemplar");
        h.assertTrue(storage.extractItem(0,144,true).getCount()==1 && storage.count(0)==16,"Simulation mutated or overstacked");
        h.assertTrue(storage.extractItem(0,144,false).getCount()==1 && storage.count(0)==15,"Extraction was not one vial");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void componentsAndUnresolvedSourcesSurviveItemAndBlockRoundTrips(GameTestHelper h) {
        var item=filledItem(h,16);var data=item.get(DataComponentInit.FIELD_CASE_CONTENTS.get());
        var list=new ArrayList<>(data.entries());var missing=vial("Lost species");var tag=missing.get(DataComponents.CUSTOM_DATA).copyTag();tag.putString("entity_type","missing:creature");missing.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        list.set(1,new SpecimenStack(missing,7));data=new FieldCaseContents(list);item.set(DataComponentInit.FIELD_CASE_CONTENTS.get(),data);
        var restored=ItemStack.parseOptional(h.getLevel().registryAccess(),(CompoundTag)item.save(h.getLevel().registryAccess()));
        h.assertTrue(data.equals(restored.get(DataComponentInit.FIELD_CASE_CONTENTS.get())),"Carried component lost identity/count");
        var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
        try {FieldCaseContents.STREAM_CODEC.encode(buffer,data);h.assertTrue(data.equals(FieldCaseContents.STREAM_CODEC.decode(buffer)),"Network round trip changed contents");}finally{buffer.release();}
        var player=actor(h,GameType.SURVIVAL);h.assertTrue(placeItem(h,player,restored).consumesAction(),"Placement failed");
        h.assertTrue(player.getMainHandItem().isEmpty(),"Placement did not consume case");
        var be=(PhlebotomistsFieldCaseBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(POS));
        var saved=be.saveWithFullMetadata(h.getLevel().registryAccess());be.loadWithComponents(saved,h.getLevel().registryAccess());
        h.assertTrue(be.contents().equals(data),"Placed persistence changed specimens");
        h.assertTrue(be.pack(player,false),"Closed case did not pack");
        var packed=player.getInventory().getItem(0);
        h.assertTrue(data.equals(packed.get(DataComponentInit.FIELD_CASE_CONTENTS.get())) && h.getLevel().getBlockEntity(h.absolutePos(POS))==null,"Pickup failed to move exact contents");
        h.assertTrue(!be.pack(player,false),"Stale block entity packed twice");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void creativePlacementMovesRatherThanCopiesFilledCase(GameTestHelper h) {
        var player=actor(h,GameType.CREATIVE);var item=filledItem(h,16);
        h.assertTrue(placeItem(h,player,item).consumesAction(),"Creative placement failed");
        h.assertTrue(player.getMainHandItem().isEmpty(),"Creative mode restored consumed filled case");
        var be=(PhlebotomistsFieldCaseBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(POS));
        h.assertTrue(be.storage().count(0)==16,"Creative placement lost contents");
        var pick=new ItemStack(BlockInit.phlebotomists_field_case.get());be.saveToItem(pick,h.getLevel().registryAccess());
        h.assertTrue(!pick.has(DataComponentInit.FIELD_CASE_CONTENTS.get())&&!pick.has(DataComponents.BLOCK_ENTITY_DATA),"Creative pick-block copied contents");
        player.gameMode.destroyBlock(h.absolutePos(POS));
        var drops=caseDrops(h);h.assertTrue(drops.size()==1 && drops.getFirst().getItem().get(DataComponentInit.FIELD_CASE_CONTENTS.get()).totalCount()==16,"Creative destruction did not return exactly one filled case");
        drops.forEach(ItemEntity::discard);h.succeed();
    }
    @GameTest(template="empty")
    public static void cancelledAndInvalidPlacementPreserveSource(GameTestHelper h) {
        var player=actor(h,GameType.CREATIVE);var item=filledItem(h,4);var before=item.copy();
        java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent> cancel=event->{if(event.getPos().equals(h.absolutePos(POS)))event.setCanceled(true);};
        NeoForge.EVENT_BUS.addListener(cancel);
        try {h.assertTrue(!placeItem(h,player,item).consumesAction(),"Cancelled placement reported success");}finally{NeoForge.EVENT_BUS.unregister(cancel);}
        h.assertTrue(ItemStack.matches(player.getMainHandItem(),before)&&h.getLevel().getBlockEntity(h.absolutePos(POS))==null,"Cancelled creative placement lost or copied source");
        var invalid=new ArrayList<>(FieldCaseContents.EMPTY.entries());invalid.set(0,new SpecimenStack(vial("Too much"),17));item.set(DataComponentInit.FIELD_CASE_CONTENTS.get(),new FieldCaseContents(invalid));
        var bad=item.copy();h.assertTrue(!placeItem(h,player,item).consumesAction()&&ItemStack.matches(item,bad),"Invalid contents were placed or truncated");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void manualOnlyAndNoNestedContainers(GameTestHelper h) {
        var be=placed(h);var item=filledItem(h,1);
        h.assertTrue(h.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,be.getBlockPos(),Direction.UP)==null,"Case exposed hopper capability");
        h.assertTrue(item.getCapability(Capabilities.ItemHandler.ITEM)==null,"Carried case exposed inventory capability");
        for(var invalid:new ItemStack[]{item,new ItemStack(ItemInit.vial_rack.get()),new ItemStack(ItemInit.bloody_vial.get()),new ItemStack(Items.GLASS_BOTTLE)})
            h.assertTrue(!be.storage().insertItem(0,invalid,false).isEmpty(),"Invalid/nested container accepted");
        var player=viewer(h,be);player.setItemInHand(InteractionHand.MAIN_HAND,item);
        item.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(player.containerMenu==player.inventoryMenu&&!player.isUsingItem(),"Carried case opened in air");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void menuUsesCapacityAndRejectsAllBypasses(GameTestHelper h) {
        var be=placed(h);var player=viewer(h,be);var menu=open(player,be);
        h.assertTrue(menu.capacity()==16,"GUI retained cabinet capacity");
        menu.setCarried(vial("A"));menu.clicked(0,0,ClickType.PICKUP,player);
        menu.setCarried(vial("A"));menu.clicked(1,0,ClickType.PICKUP,player);
        h.assertTrue(menu.getCarried().getCount()==1&&be.storage().count(1)==0,"Direct click duplicated identity across cells");
        menu.setCarried(ItemStack.EMPTY);
        for(var type:new ClickType[]{ClickType.CLONE,ClickType.SWAP,ClickType.THROW,ClickType.QUICK_CRAFT,ClickType.PICKUP_ALL})menu.clicked(0,0,type,player);
        h.assertTrue(be.storage().count(0)==1&&menu.getCarried().isEmpty(),"Menu shortcut bypassed accounting");
        menu.clicked(0,1,ClickType.PICKUP,player);h.assertTrue(menu.getCarried().getCount()==1&&be.storage().count(0)==0,"Right click failed single extraction");
        menu.clicked(0,1,ClickType.PICKUP,player);
        for(int i=0;i<36;i++)player.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        menu.quickMoveStack(player,0);h.assertTrue(be.storage().count(0)==1,"Full inventory lost specimen");
        player.getInventory().setItem(35,ItemStack.EMPTY);menu.quickMoveStack(player,0);
        h.assertTrue(be.storage().count(0)==0&&player.getInventory().getItem(35).getCount()==1,"Bounded quick move failed");h.succeed();
    }
    @GameTest(template="empty")
    public static void breakClosesViewersAndKeepsTheirCarriedVialsSeparate(GameTestHelper h) {
        var be=placed(h);var a=actor(h,GameType.SURVIVAL);var b=actor(h,GameType.SURVIVAL);var first=open(a,be);var second=open(b,be);
        be.storage().insertItem(0,vial("A"),false);be.storage().insertItem(0,vial("A"),false);
        first.clicked(0,0,ClickType.PICKUP,a);var breaker=actor(h,GameType.SURVIVAL);breaker.gameMode.destroyBlock(be.getBlockPos());
        var drops=caseDrops(h);h.assertTrue(drops.size()==1&&drops.getFirst().getItem().get(DataComponentInit.FIELD_CASE_CONTENTS.get()).totalCount()==1,"Break packed cursor vial twice or lost stored vial");
        h.assertTrue(a.containerMenu==a.inventoryMenu&&b.containerMenu==b.inventoryMenu&&first.getCarried().isEmpty(),"Breaking retained viewers/cursor");
        h.assertTrue(a.getInventory().countItem(ItemInit.bloody_vial.get())==1,"Cursor vial did not return to its owner");
        second.clicked(0,0,ClickType.PICKUP,b);h.assertTrue(second.getCarried().isEmpty(),"Stale viewer extracted after packing");
        drops.forEach(ItemEntity::discard);h.succeed();
    }
    @GameTest(template="empty")
    public static void breakWaitsForActiveTransferAndPacksOnce(GameTestHelper h) {
        var be=placed(h);var player=viewer(h,be);be.storage().insertItem(0,vial("A"),false);
        h.assertTrue(be.beginTransfer(),"Could not begin transfer");
        h.assertTrue(!be.pack(player,true)&&!be.beginTransfer(),"Break did not defer or allowed new transfer");
        var removed=be.storage().extractItem(0,1,false);player.getInventory().add(removed);
        be.endTransfer();var drops=caseDrops(h);
        h.assertTrue(drops.size()==1&&drops.getFirst().getItem().get(DataComponentInit.FIELD_CASE_CONTENTS.get()).totalCount()==0,"Pending break captured pre-transfer count");
        h.assertTrue(player.getInventory().countItem(ItemInit.bloody_vial.get())==1&&!be.pack(player,true),"Deferred break lost/duplicated transfer");
        drops.forEach(ItemEntity::discard);h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=60)
    public static void sneakPickupWaitsForClosingAnimation(GameTestHelper h) {
        var be=placed(h);var p=viewer(h,be);var menu=open(p,be);be.storage().insertItem(0,vial("A"),false);
        h.assertTrue(!be.pack(p,false),"Open case packed");menu.removed(p);p.containerMenu=p.inventoryMenu;
        h.runAfterDelay(8,()->{
            h.assertTrue(!be.getBlockState().getValue(PhlebotomistsFieldCaseBlock.OPEN)&&!be.pack(p,false),"Closing lid allowed early pickup");
        });
        h.runAfterDelay(20,()->{h.assertTrue(be.pack(p,false),"Closed unviewed case would not pack");h.succeed();});
    }
    @GameTest(template="empty")
    public static void fullInventoryDropAndRejectedSpawnPreserveOwnership(GameTestHelper h) {
        var be=placed(h);var p=viewer(h,be);for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        be.storage().insertItem(0,vial("A"),false);
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> cancel=e->{if(e.getEntity() instanceof ItemEntity item && item.getItem().is(BlockInit.phlebotomists_field_case.get().asItem()))e.setCanceled(true);};
        NeoForge.EVENT_BUS.addListener(cancel);
        try {h.assertTrue(!be.pack(p,false)&&be.storage().totalCount()==1&&h.getLevel().getBlockEntity(be.getBlockPos())==be,"Rejected delivery removed source");}
        finally{NeoForge.EVENT_BUS.unregister(cancel);}
        h.assertTrue(be.pack(p,false),"Full inventory could not safely drop case");var drops=caseDrops(h);
        h.assertTrue(drops.size()==1&&drops.getFirst().getItem().get(DataComponentInit.FIELD_CASE_CONTENTS.get()).totalCount()==1,"Dropped case lost/duplicated contents");
        drops.forEach(ItemEntity::discard);h.succeed();
    }

    @GameTest(template="empty")
    public static void replacedSourceRollsBackDeliveryAndReentrantPickupIsRejected(GameTestHelper h) {
        var be=placed(h);var p=viewer(h,be);for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        be.storage().insertItem(0,vial("A"),false);boolean[] reentry={true};
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> replace=e->{
            if(e.getEntity() instanceof ItemEntity item && item.getItem().is(BlockInit.phlebotomists_field_case.get().asItem())) {
                reentry[0]=be.pack(p,false);
                // Simulate the source changing during delivery, before final removal validation.
                var replacement=new PhlebotomistsFieldCaseBlockEntity(be.getBlockPos(),be.getBlockState());
                replacement.readContents(be.contents());h.getLevel().setBlockEntity(replacement);
            }
        };
        NeoForge.EVENT_BUS.addListener(replace);
        try {h.assertTrue(!be.pack(p,false),"Packing removed a replacement block entity");}finally{NeoForge.EVENT_BUS.unregister(replace);}
        var current=(PhlebotomistsFieldCaseBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(POS));
        h.assertTrue(!reentry[0]&&caseDrops(h).isEmpty()&&current.storage().totalCount()==1,"Failed removal duplicated or lost delivery");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void realSneakPickupAndFilledVialInteractionAreRouted(GameTestHelper h) {
        var be=placed(h);var p=actor(h,GameType.SURVIVAL);var pos=be.getBlockPos();be.storage().insertItem(0,vial("A"),false);
        var hit=new BlockHitResult(pos.getCenter(),Direction.NORTH,pos,false);p.setShiftKeyDown(true);
        p.setItemInHand(InteractionHand.MAIN_HAND,vial("Held"));
        var use=p.gameMode.useItemOn(p,h.getLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
        h.assertTrue(use.consumesAction()&&!p.isUsingItem()&&h.getLevel().getBlockEntity(pos)==be,"Vial interaction injected or picked up case");
        // FakePlayer declines openMenu, so the case is still closed and eligible for pickup.
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        p.gameMode.useItemOn(p,h.getLevel(),p.getMainHandItem(),InteractionHand.MAIN_HAND,hit);
        h.assertTrue(h.getLevel().getBlockEntity(pos)==null && p.getInventory().getItem(0).get(DataComponentInit.FIELD_CASE_CONTENTS.get()).totalCount()==1,"Sneak empty-hand pickup failed actual player route");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void occupiedCasesResistExplosionMobsAndPistonsWithoutSpilling(GameTestHelper h) {
        var be=placed(h);var pos=be.getBlockPos();be.storage().insertItem(0,vial("A"),false);
        h.getLevel().explode(null,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,2,Level.ExplosionInteraction.BLOCK);
        h.assertTrue(h.getLevel().getBlockEntity(pos)==be&&be.storage().totalCount()==1,"Explosion removed occupied case");
        h.assertTrue(!be.getBlockState().canEntityDestroy(h.getLevel(),pos,net.minecraft.world.entity.EntityType.WITHER.create(h.getLevel()))
                && !be.getBlockState().canEntityDestroy(h.getLevel(),pos,net.minecraft.world.entity.EntityType.ENDER_DRAGON.create(h.getLevel())),"Mob destruction lost occupied case");
        h.assertTrue(be.getBlockState().getPistonPushReaction()==net.minecraft.world.level.material.PushReaction.BLOCK,"Piston can move case");
        h.assertTrue(caseDrops(h).isEmpty(),"Explosion duplicated case");h.succeed();
    }

    @GameTest(template="empty")
    public static void carriedPartialAndMalformedSamplesCannotBypassInsertionRules(GameTestHelper h) {
        for(boolean partial:new boolean[]{false,true}) {
            var sample=vial("Invalid");var tag=sample.get(DataComponents.CUSTOM_DATA).copyTag();
            if(partial)tag.putBoolean("state",false);else tag.putString("entity_type","bad source");
            sample.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
            var entries=new ArrayList<>(FieldCaseContents.EMPTY.entries());entries.set(0,new SpecimenStack(sample,1));
            var contents=new FieldCaseContents(entries);
            h.assertTrue(!contents.valid(),"Carried contents bypass full-sample classification");
            var item=new ItemStack(BlockInit.phlebotomists_field_case.get());item.set(DataComponentInit.FIELD_CASE_CONTENTS.get(),contents);
            var before=item.copy();var p=actor(h,GameType.SURVIVAL);
            h.assertTrue(!placeItem(h,p,item).consumesAction()&&ItemStack.matches(item,before),"Rejected source was placed or rewritten");
        }
        h.succeed();
    }

    @GameTest(template="empty")
    public static void discardedDeliveryCannotConsumeThePlacedCollection(GameTestHelper h) {
        var be=placed(h);var player=viewer(h,be);be.storage().insertItem(0,vial("A"),false);
        for(int i=0;i<36;i++)player.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> discard=e->{
            if(e.getEntity() instanceof ItemEntity item&&item.getItem().is(BlockInit.phlebotomists_field_case.get().asItem()))item.discard();
        };
        NeoForge.EVENT_BUS.addListener(discard);
        try {h.assertTrue(!be.pack(player,false)&&h.getLevel().getBlockEntity(be.getBlockPos())==be&&be.storage().totalCount()==1,"Discarded delivery consumed the source case");}
        finally{NeoForge.EVENT_BUS.unregister(discard);}
        h.succeed();
    }
}
