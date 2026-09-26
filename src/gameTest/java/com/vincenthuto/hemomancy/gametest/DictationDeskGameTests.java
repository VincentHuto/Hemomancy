package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.common.block.entity.DictationTableBlockEntity;
import com.vincenthuto.hutoslib.common.book.FieldNotes;
import com.vincenthuto.hutoslib.common.registry.HLBlockInit;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("book_kit_validation")
@PrefixGameTestTemplate(false)
public final class DictationDeskGameTests {
    @GameTest(template="empty",batch="book_kit")
    public static void escritoireCraftingConsumesTheSharedTable(GameTestHelper h) {
        var planks=new ItemStack(BlockInit.blood_wood_planks.get());
        var table=new ItemStack(HLBlockInit.dictation_table.get());
        var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,java.util.List.of(
                planks,new ItemStack(net.minecraft.world.item.Items.IRON_INGOT),planks,
                planks,table,planks,planks,ItemStack.EMPTY,planks));
        var recipe=h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("hemomancy:harbinger_escritoire")).orElseThrow().value();
        h.assertTrue(recipe instanceof net.minecraft.world.item.crafting.CraftingRecipe crafting
                && crafting.matches(input,h.getLevel())
                && crafting.assemble(input,h.getLevel().registryAccess()).is(BlockInit.harbinger_escritoire.get().asItem()),
                "Escritoire recipe must consume the HutosLib table and produce the functional desk");
        h.assertTrue(h.getLevel().getRecipeManager().byKey(ResourceLocation.parse("hutoslib:dictation_table")).isPresent(),"Shared table must be craftable");
        h.succeed();
    }
    @GameTest(template="empty",batch="book_kit")
    public static void sharedDeskStoresPersistsAndReturnsGuide(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(1,2,1));var level=h.getLevel();var player=player(h);
        level.setBlock(pos,HLBlockInit.dictation_table.get().defaultBlockState(),3);
        var stack=new ItemStack(HLItemInit.hl_guide_book.get());
        var hit=new BlockHitResult(pos.getCenter(),Direction.UP,pos,false);
        level.getBlockState(pos).useItemOn(stack,level,player,InteractionHand.MAIN_HAND,hit);
        var desk=(DictationTableBlockEntity)level.getBlockEntity(pos);
        h.assertTrue(stack.isEmpty()&&desk.getBook().is(HLItemInit.hl_guide_book.get()),"Placement must transfer one guide");
        var saved=desk.saveWithoutMetadata(level.registryAccess());
        var restored=new DictationTableBlockEntity(pos,desk.getBlockState());
        restored.loadWithComponents(saved,level.registryAccess());
        h.assertTrue(restored.getBook().is(HLItemInit.hl_guide_book.get()),"Stored guide must survive NBT round trip");
        player.setShiftKeyDown(true);
        desk.getBlockState().useWithoutItem(level,player,hit);
        h.assertTrue(desk.getBook().isEmpty()&&player.getInventory().contains(new ItemStack(HLItemInit.hl_guide_book.get())),"Sneak use must return the guide");
        level.removeBlock(pos,false);h.succeed();
    }
    @GameTest(template="empty",batch="book_kit")
    public static void bloodDictationRequiresEscritoireAndRetainsNotesOnFailure(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(1,2,1));var level=h.getLevel();var player=player(h);
        var memo=ResourceLocation.parse("hemomancy:fixture/shared_note");
        var knowledge=player.getData(HemoAttachmentTypes.LIBER_KNOWLEDGE);
        knowledge.recordPendingMemo(memo);
        var book=new ItemStack(ItemInit.liber_sanguinum.get());
        h.assertTrue(!FieldNotes.hasPending(player,book,HLBlockInit.dictation_table.get()),"Ordinary table must not bypass the Escritoire");
        h.assertTrue(FieldNotes.hasPending(player,book,BlockInit.harbinger_escritoire.get()),"Escritoire must expose writable notes");
        level.setBlock(pos,BlockInit.harbinger_escritoire.get().defaultBlockState(),3);
        var desk=(DictationTableBlockEntity)level.getBlockEntity(pos);desk.setBook(book);
        var hit=new BlockHitResult(pos.getCenter(),Direction.UP,pos,false);
        var blood=player.getData(HemoAttachmentTypes.BLOOD_VOLUME);blood.setActive(true);blood.setBloodVolume(0);
        desk.getBlockState().useWithoutItem(level,player,hit);
        h.assertTrue(knowledge.getPendingMemos().contains(memo)&&!knowledge.knowsMemo(memo),"Insufficient blood must not consume notes");
        blood.setMaxBloodVolume(2000);blood.setBloodVolume(2000);
        int cost=MemoHelper.getDictationBloodCost(knowledge.getKnownMemos().size(),1);
        desk.getBlockState().useWithoutItem(level,player,hit);
        h.assertTrue(knowledge.knowsMemo(memo)&&!knowledge.getPendingMemos().contains(memo),"Successful dictation must move pending to known");
        h.assertTrue(blood.getBloodVolume()==2000-cost,"Must charge existing blood formula exactly once");
        h.assertTrue(!FieldNotes.hasPending(player,book,BlockInit.harbinger_escritoire.get()),"Finished book must stop glowing and become readable");
        desk.getBlockState().useWithoutItem(level,player,hit);
        h.assertTrue(blood.getBloodVolume()==2000-cost,"Reading must not charge again");
        level.removeBlock(pos,false);h.succeed();
    }
    @GameTest(template="empty",batch="book_kit")
    public static void unstainedDictationKeepsXpCostAndLegacyTableInventoryLoads(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(1,2,1));var level=h.getLevel();var player=player(h);
        level.setBlock(pos,BlockInit.dictation_table.get().defaultBlockState(),3);
        var legacy=(DictationTableBlockEntity)level.getBlockEntity(pos);
        var book=new ItemStack(ItemInit.liber_immaculatus.get());legacy.setBook(book);
        var saved=legacy.saveWithoutMetadata(level.registryAccess());
        level.setBlock(pos,BlockInit.harbinger_escritoire.get().defaultBlockState(),3);
        var desk=(DictationTableBlockEntity)level.getBlockEntity(pos);desk.loadWithComponents(saved,level.registryAccess());
        h.assertTrue(desk.getBook().is(ItemInit.liber_immaculatus.get()),"Legacy inventory format must load in shared desk");
        var knowledge=player.getData(HemoAttachmentTypes.LIBER_KNOWLEDGE);
        var memo=ResourceLocation.parse("hemomancy:fixture/pale_note");knowledge.recordPendingMemo(memo);
        player.experienceLevel=0;
        var hit=new BlockHitResult(pos.getCenter(),Direction.UP,pos,false);
        desk.getBlockState().useWithoutItem(level,player,hit);
        h.assertTrue(knowledge.getPendingMemos().contains(memo),"Insufficient XP must retain notes");
        player.experienceLevel=3;desk.getBlockState().useWithoutItem(level,player,hit);
        h.assertTrue(player.experienceLevel==3-MemoHelper.getUnstainedDictationXpLevelCost()&&knowledge.knowsMemo(memo),"Unstained dictation retains its XP rule");
        level.removeBlock(pos,false);h.succeed();
    }
    private static ServerPlayer player(GameTestHelper helper) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"dictation-test"),false);
        var player=new ServerPlayer(helper.getLevel().getServer(),helper.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(helper.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        return player;
    }
}
