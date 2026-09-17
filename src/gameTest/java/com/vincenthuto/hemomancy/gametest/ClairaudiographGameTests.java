package com.vincenthuto.hemomancy.gametest;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.component.ClairaudiographRecording;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.*;
import com.vincenthuto.hemomancy.common.menu.tile.functional.ClairaudiographMenu;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("blood_injection_validation")
@PrefixGameTestTemplate(false)
public class ClairaudiographGameTests {
    private static ItemStack sample(){
        var s=new ItemStack(ItemInit.bloody_vial.get());var tag=new CompoundTag();tag.putString("entity_type","minecraft:pig");tag.putBoolean("state",true);tag.putString("container_marker","keep");s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));s.set(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get(),true);return s;
    }
    private static ClairaudiographBlockEntity machine(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(1,2,1));h.getLevel().setBlockAndUpdate(p,BlockInit.clairaudiograph.get().defaultBlockState());
        var be=(ClairaudiographBlockEntity)h.getLevel().getBlockEntity(p);be.inventory.setStackInSlot(0,sample());be.inventory.setStackInSlot(1,new ItemStack(ItemInit.ambergris_cylinder.get()));tick(be,1);return be;
    }
    private static ClairaudiographRecording choice(){return ClairaudiographCatalogue.choices("minecraft:pig").getFirst().recording();}
    private static void tick(ClairaudiographBlockEntity be,int n){for(int i=0;i<n;i++)ClairaudiographBlockEntity.tick(be.getLevel(),be.getBlockPos(),be.getBlockState(),be);}
    @GameTest(template="empty") public static void recipesLoadAndCylinderSlotStaysSingle(GameTestHelper h){
        var recipes=h.getLevel().getRecipeManager();
        h.assertTrue(recipes.byKey(net.minecraft.resources.ResourceLocation.parse("hemomancy:clairaudiograph")).isPresent(),"Machine recipe failed to load");
        h.assertTrue(recipes.byKey(net.minecraft.resources.ResourceLocation.parse("hemomancy:ambergris_cylinder")).isPresent(),"Cylinder recipe failed to load");
        var be=machine(h);be.inventory.setStackInSlot(1,ItemStack.EMPTY);
        var remainder=be.inventory.insertItem(1,new ItemStack(ItemInit.ambergris_cylinder.get(),2),false);
        h.assertTrue(remainder.getCount()==1 && be.inventory.getStackInSlot(1).getCount()==1,"Machine cylinder slot stacked");h.succeed();
    }
    @GameTest(template="empty") public static void carveConservesVesselAndPersistsRecording(GameTestHelper h){
        var be=machine(h);var before=be.inventory.getStackInSlot(0).copy();h.assertTrue(be.startCarve(choice()),"Valid carve rejected");
        h.assertTrue(be.inventory.extractItem(0,1,false).isEmpty() && be.inventory.extractItem(1,1,false).isEmpty(),"Inputs unlocked while carving");
        tick(be,79);h.assertTrue(be.recording()==null && ItemStack.matches(before,be.inventory.getStackInSlot(0)),"Consumed before tick 80");
        tick(be,1);h.assertTrue(choice().equals(be.recording()),"Missing recording component");
        var empty=be.inventory.getStackInSlot(0);h.assertTrue(empty.getCount()==1 && empty.is(ItemInit.bloody_vial.get()) && !BloodSampleData.isFilled(empty) && !BloodSampleData.identified(empty),"Vessel not cleared");
        h.assertTrue(empty.get(DataComponents.CUSTOM_DATA).copyTag().getString("container_marker").equals("keep"),"Lost vessel identity");
        var saved=be.saveWithFullMetadata(h.getLevel().registryAccess());
        var restored=new ClairaudiographBlockEntity(be.getBlockPos(),be.getBlockState());restored.loadWithComponents(saved,h.getLevel().registryAccess());
        h.assertTrue(choice().equals(restored.recording()) && restored.progress==0 && !restored.playing(),"Persistent recording or transient state wrong");
        h.assertTrue(!be.startCarve(choice()),"Recorded cylinder overwritten");h.succeed();
    }
    @GameTest(template="empty") public static void interruptionsAndInvalidSourcesPreserveInputs(GameTestHelper h){
        var be=machine(h);var before=be.inventory.getStackInSlot(0).copy();be.startCarve(choice());tick(be,20);be.onChunkUnloaded();
        h.assertTrue(be.progress==0 && be.recording()==null && ItemStack.matches(before,be.inventory.getStackInSlot(0)),"Unload consumed input");
        be.startCarve(choice());tick(be,20);var saved=be.saveWithFullMetadata(h.getLevel().registryAccess());
        var restored=new ClairaudiographBlockEntity(be.getBlockPos(),be.getBlockState());restored.loadWithComponents(saved,h.getLevel().registryAccess());
        h.assertTrue(restored.progress==0 && restored.recording()==null && ItemStack.matches(before,restored.inventory.getStackInSlot(0)),"Restart resumed a partial carve");
        be.cancelCarve();be.inventory.setStackInSlot(0,new ItemStack(Items.DIRT));h.assertTrue(!be.startCarve(choice()),"Wrong sample accepted");
        be.inventory.setStackInSlot(0,BloodSampleData.emptyVessel(before));h.assertTrue(!be.startCarve(choice()),"Empty sample accepted");
        be.inventory.setStackInSlot(0,before);be.startCarve(choice());be.inventory.getStackInSlot(0).setCount(2);tick(be,80);
        h.assertTrue(be.recording()==null && be.inventory.getStackInSlot(0).getCount()==2,"Invalid completion consumed input");h.succeed();
    }
    @GameTest(template="empty") public static void editedRecordingCannotAuthorizeArbitrarySound(GameTestHelper h){
        var be=machine(h);var forged=new ClairaudiographRecording("minecraft:pig","minecraft:entity.warden.roar","ambient",1);
        be.inventory.getStackInSlot(1).set(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get(),forged);be.startPlayback(false);
        h.assertTrue(!be.playing() && be.recording().equals(forged),"Forged recording played or was erased");
        var wrongItem = new ItemStack(Items.DIRT); wrongItem.set(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get(), choice());
        be.inventory.setStackInSlot(1, wrongItem); be.startPlayback(false);
        h.assertTrue(!be.playing(), "Non-cylinder item played a recording");
        h.assertTrue(ClairaudiographCatalogue.allowed(new ClairaudiographRecording(choice().source(),choice().sound(),choice().kind(),2))==null,"Forged pitch accepted");h.succeed();
    }
    @GameTest(template="empty") public static void staleMenuAndLockedSlotsRejectRequests(GameTestHelper h){
        var be=machine(h);var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(be.getBlockPos().getCenter());
        var menu=new ClairaudiographMenu(7,player.getInventory(),be);player.containerMenu=menu;menu.broadcastChanges();long old=menu.version;
        var other=sample();var tag=other.get(DataComponents.CUSTOM_DATA).copyTag();tag.putString("entity_type","minecraft:cow");other.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));be.inventory.setStackInSlot(0,other);
        menu.action(player,old,1,0);h.assertTrue(be.progress==0,"Stale source selection accepted");
        be.inventory.setStackInSlot(0,sample());menu.broadcastChanges();menu.action(player,menu.version,1,0);
        h.assertTrue(be.progress>0 && !menu.getSlot(0).mayPickup(player) && !menu.getSlot(1).mayPlace(new ItemStack(ItemInit.ambergris_cylinder.get())),"Menu failed to lock");h.succeed();
    }
    @GameTest(template="empty") public static void redstoneEdgesAndStopDoNotRetrigger(GameTestHelper h){
        var be=machine(h);be.inventory.getStackInSlot(1).set(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get(),choice());
        var power=be.getBlockPos().above();h.getLevel().setBlockAndUpdate(power,Blocks.REDSTONE_BLOCK.defaultBlockState());tick(be,1);h.assertTrue(be.playing(),"Rising edge failed");
        tick(be,150);h.assertTrue(!be.playing(),"Constant power retriggered");
        be.onChunkUnloaded();tick(be,1);h.assertTrue(!be.playing(),"Loaded powered machine started playback");
        h.getLevel().setBlockAndUpdate(power,Blocks.AIR.defaultBlockState());tick(be,1);h.getLevel().setBlockAndUpdate(power,Blocks.REDSTONE_BLOCK.defaultBlockState());tick(be,1);
        h.getLevel().setBlockAndUpdate(power,Blocks.AIR.defaultBlockState());tick(be,1);h.assertTrue(be.playing(),"Falling edge stopped one-shot");be.stopPlayback();
        be.loop=true;h.getLevel().setBlockAndUpdate(power,Blocks.AIR.defaultBlockState());tick(be,1);h.getLevel().setBlockAndUpdate(power,Blocks.REDSTONE_BLOCK.defaultBlockState());tick(be,150);h.assertTrue(be.playing(),"Powered loop stopped");
        be.stopPlayback();tick(be,150);h.assertTrue(!be.playing(),"Stop while powered retriggered");
        be.startPlayback(false);h.getLevel().setBlockAndUpdate(power,Blocks.AIR.defaultBlockState());tick(be,1);h.assertTrue(be.playing(),"Falling edge stopped manual loop");
        be.stopPlayback();h.getLevel().setBlockAndUpdate(power,Blocks.REDSTONE_BLOCK.defaultBlockState());tick(be,1);h.getLevel().setBlockAndUpdate(power,Blocks.AIR.defaultBlockState());tick(be,1);h.assertTrue(!be.playing(),"Falling edge did not stop redstone loop");h.succeed();
    }

    @GameTest(template="empty") public static void breakingDropsUnchangedInputsOnce(GameTestHelper h){
        var be=machine(h);be.startCarve(choice());tick(be,30);var area=new net.minecraft.world.phys.AABB(be.getBlockPos()).inflate(1);
        h.getLevel().setBlockAndUpdate(be.getBlockPos(),Blocks.AIR.defaultBlockState());
        var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area);
        h.assertTrue(drops.stream().filter(e->e.getItem().is(ItemInit.bloody_vial.get())).mapToInt(e->e.getItem().getCount()).sum()==1,"Vial drop duplicated or lost");
        h.assertTrue(drops.stream().filter(e->e.getItem().is(ItemInit.ambergris_cylinder.get())).mapToInt(e->e.getItem().getCount()).sum()==1,"Cylinder drop duplicated or lost");
        h.assertTrue(drops.stream().noneMatch(e->e.getItem().has(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get())),"Breaking completed carve");h.succeed();
    }
    @GameTest(template="empty") public static void reloadCancelsAndMissingEntityCanStillReplay(GameTestHelper h) throws Exception {
        var loader=new ClairaudiographCatalogue();
        var prepare=ClairaudiographCatalogue.class.getDeclaredMethod("prepare",net.minecraft.server.packs.resources.ResourceManager.class,net.minecraft.util.profiling.ProfilerFiller.class);prepare.setAccessible(true);
        var apply=ClairaudiographCatalogue.class.getDeclaredMethod("apply",java.util.Map.class,net.minecraft.server.packs.resources.ResourceManager.class,net.minecraft.util.profiling.ProfilerFiller.class);apply.setAccessible(true);
        var manager=h.getLevel().getServer().getResourceManager();var profiler=net.minecraft.util.profiling.InactiveProfiler.INSTANCE;
        var original=prepare.invoke(loader,manager,profiler);
        try {
            var be=machine(h);var oldChoice=choice();be.startCarve(oldChoice);tick(be,20);
            apply.invoke(loader,java.util.Map.of(),manager,profiler);tick(be,1);
            h.assertTrue(be.progress==0 && be.recording()==null && BloodSampleData.isFilled(be.inventory.getStackInSlot(0)),"Reload consumed or retained carving");
            be.inventory.getStackInSlot(1).set(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get(),oldChoice);be.startPlayback(false);h.assertTrue(!be.playing(),"Removed catalogue still authorized playback");
            var json=com.google.gson.JsonParser.parseString("{\"entity\":\"removed:creature\",\"sounds\":[{\"kind\":\"ambient\",\"sound\":\"minecraft:entity.pig.ambient\"}]}").getAsJsonObject();
            apply.invoke(loader,java.util.Map.of(net.minecraft.resources.ResourceLocation.parse("test:retained"),json),manager,profiler);
            var retained=ClairaudiographCatalogue.choices("removed:creature").getFirst().recording();be.inventory.getStackInSlot(1).set(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get(),retained);be.startPlayback(false);
            h.assertTrue(be.playing(),"Missing entity invalidated authorized sound");be.stopPlayback();
        } finally {apply.invoke(loader,original,manager,profiler);}
        h.succeed();
    }
}
