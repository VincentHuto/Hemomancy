package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncLiberKnowledge;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import com.vincenthuto.hutoslib.common.data.book.*;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Set;

@GameTestHolder("book_kit_validation")
@PrefixGameTestTemplate(false)
public final class BookKitGameTests {
    @GameTest(template="empty",batch="book_kit")
    public static void fullCorpusBindsOnDedicatedServer(GameTestHelper helper) {
        var loader=BookPlaceboReloadListener.INSTANCE;
        var sanguinium=loader.getBookByTitle(Hemomancy.rloc("libersanguinium"));
        var liber=loader.getBookByTitle(Hemomancy.rloc("liberimmaculatus"));
        helper.assertTrue(sanguinium!=null&&liber!=null,"Both books must bind without client classes");
        helper.assertTrue(sanguinium.getSourceIndex().entries().size()>=89,
                "Expected the existing corpus and eight Troupe lessons, found " + sanguinium.getSourceIndex().entries().size());
        for (var page : new String[]{"school", "veinwing_vulture", "marrow_spitter", "scarlet_mummer", "gorebound_hulk", "sanguine_hound", "cinder_bellows", "mnemonist_puppet"})
            helper.assertTrue(loader.findTarget(Hemomancy.rloc("libersanguinium/the_hematic_order/pages/troupe_" + page)).isPresent(), "Missing Troupe book lesson: " + page);
        var scar=loader.findTarget(Hemomancy.rloc("libersanguinium/the_hematic_order/pages/scar_practice")).orElseThrow();
        helper.assertTrue(((PageTemplate)scar.template()).getText().contains("Blood Absorption"),"Scar control teaching did not bind");
        helper.assertTrue(liber.getSourceIndex().entries().size()==19,"Liber lost entries");
        helper.assertTrue(sanguinium.getTemplate().getThemeId().orElseThrow().equals(Hemomancy.rloc("fane")),"Theme ID missing");
        var cut=loader.findTarget(Hemomancy.rloc("libersanguinium/the_infection/pages/antecedent_vigil_record_read")).orElseThrow();
        helper.assertTrue(((PageTemplate)cut.template()).getPresentation().record().isPresent(),"Record metadata missing");
        helper.assertTrue(!sanguinium.getGlossary().isEmpty(),"Glossary did not bind");
        helper.succeed();
    }

    @GameTest(template="empty",batch="book_kit")
    public static void knowledgePacketRetainsExplicitDiscoveryAndMemoState(GameTestHelper helper) {
        var id=Hemomancy.rloc("liberimmaculatus/our_lady/pages/she_who_listens");
        var knowledge=new LiberKnowledge();knowledge.unlockEntry(id,CommonDiscoverySource.OTHER);
        knowledge.recordMemo(Hemomancy.rloc("memo/known"));knowledge.recordPendingMemo(Hemomancy.rloc("memo/pending"));
        var packet=new PacketSyncLiberKnowledge(knowledge,Set.of(id));
        var original=new FriendlyByteBuf(Unpooled.buffer());var roundTrip=new FriendlyByteBuf(Unpooled.buffer());
        try {
            PacketSyncLiberKnowledge.encode(original,packet);
            byte[] bytes=ByteBufUtil.getBytes(original);
            var decoded=PacketSyncLiberKnowledge.decode(original);
            PacketSyncLiberKnowledge.encode(roundTrip,decoded);
            helper.assertTrue(java.util.Arrays.equals(bytes,ByteBufUtil.getBytes(roundTrip)),"Packet lost discoveries, memos, sources or pending memos");
            helper.succeed();
        } finally {original.release();roundTrip.release();}
    }
}
