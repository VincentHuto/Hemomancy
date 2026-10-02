package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncLiberKnowledge;
import com.vincenthuto.hemomancy.common.network.capa.unstained.PacketSyncUnstainedProgress;
import com.vincenthuto.hutoslib.client.book.*;
import com.vincenthuto.hutoslib.client.screen.guide.BookReaderScreen;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import com.vincenthuto.hutoslib.common.data.book.BookPlaceboReloadListener;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.nio.file.Files;

/** Disposable-client review operator. Not packaged in release jars. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class BookKitClientReview {
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.bookKitReview"))return;
        var mc=Minecraft.getInstance();
        var root=mc.gameDirectory.toPath().toAbsolutePath().normalize();
        if(!root.endsWith("book-kit-client"))return;
        var request=root.resolve("book-review.json");if(!Files.exists(request))return;
        try {
            var data=JsonParser.parseString(Files.readString(request)).getAsJsonObject();Files.delete(request);
            String op=data.get("op").getAsString();
            if(op.equals("join")) {
                String address=java.net.InetAddress.getLoopbackAddress().getHostAddress();
                String endpoint=(address.contains(":")?"["+address+"]":address)+":25571";
                net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(),mc,
                        net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(endpoint),
                        new net.minecraft.client.multiplayer.ServerData("Book kit review",endpoint,net.minecraft.client.multiplayer.ServerData.Type.OTHER),false,null);
                return;
            }
            if(op.equals("quit")) {mc.stop();return;}
            if(mc.player==null)throw new IllegalStateException("Review operation requires a connected player");
            if(op.equals("desk")) {
                if(mc.getSingleplayerServer()==null)throw new IllegalStateException("Fixture requires its disposable local server");
                mc.setScreen(null);
                mc.getSingleplayerServer().execute(()-> {
                    var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    var level=player.serverLevel();
                    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                    for(int x=-4;x<=4;x++)for(int z=-3;z<=5;z++)level.setBlock(new net.minecraft.core.BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(),3);
                    var pos=new net.minecraft.core.BlockPos(0,100,0);
                    level.setBlock(pos,com.vincenthuto.hemomancy.common.init.BlockInit.harbinger_escritoire.get().defaultBlockState()
                            .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,net.minecraft.core.Direction.SOUTH),3);
                    var table=(com.vincenthuto.hutoslib.common.block.entity.DictationTableBlockEntity)level.getBlockEntity(pos);
                    table.setBook(new ItemStack(ItemInit.liber_sanguinum.get()));
                    var memo=Hemomancy.rloc("fixture/desk_visual");
                    var knowledge=player.getData(com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes.LIBER_KNOWLEDGE);
                    knowledge.removePendingMemos(java.util.List.copyOf(knowledge.getPendingMemos()));
                    if(data.has("pending")&&data.get("pending").getAsBoolean())knowledge.recordPendingMemo(memo);
                    PacketDistributor.sendToPlayer(player,new PacketSyncLiberKnowledge(knowledge));
                    player.teleportTo(0.5,100,3.4);player.setYRot(180);player.setXRot(12);
                    player.connection.teleport(0.5,100,3.4,180,12);
                    level.setDayTime(6000);
                });
            } else if(op.equals("setup")||op.equals("clarity")) {
                if(mc.getSingleplayerServer()==null)throw new IllegalStateException("Fixture requires its disposable local server");
                mc.getSingleplayerServer().execute(()-> {
                    var player=data.has("playerName")?mc.getSingleplayerServer().getPlayerList().getPlayerByName(data.get("playerName").getAsString())
                            :mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    if(op.equals("setup")) {
                        player.getInventory().add(new ItemStack(ItemInit.liber_sanguinum.get()));
                        player.getInventory().add(new ItemStack(ItemInit.liber_immaculatus.get()));
                        HemoCapabilityAccess.getLiberKnowledge(player).ifPresent(knowledge-> {
                            for(String name:new String[]{"libersanguinium","liberimmaculatus"}) {
                                var book=BookPlaceboReloadListener.INSTANCE.getBookByTitle(Hemomancy.rloc(name));
                                book.getChapters().forEach(chapter->chapter.getPages().forEach(page->knowledge.unlockEntry(page.getId(),CommonDiscoverySource.OTHER)));
                            }
                            PacketDistributor.sendToPlayer(player,new PacketSyncLiberKnowledge(knowledge));
                        });
                        player.getInventory().setChanged();player.containerMenu.broadcastChanges();
                    }
                    HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress-> {
                        progress.setClarityUnlocked(true);progress.setClarity(data.has("value")?data.get("value").getAsFloat():100);
                        PacketDistributor.sendToPlayer(player,new PacketSyncUnstainedProgress(progress));
                    });
                });
            } else if(op.equals("open")) {
                String name=data.get("book").getAsString();
                var item=(ItemGuideBook)(name.equals("libersanguinium")?ItemInit.liber_sanguinum.get():ItemInit.liber_immaculatus.get());
                // Exercise the production item opener.
                item.use(mc.level,mc.player,InteractionHand.MAIN_HAND);
            } else if(op.equals("scale")) {mc.options.guiScale().set(data.get("value").getAsInt());mc.resizeDisplay();}
            else if(op.equals("size"))org.lwjgl.glfw.GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),data.get("width").getAsInt(),data.get("height").getAsInt());
            else if(op.equals("tutorial"))mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            else if(op.equals("publish")) {
                // Development profiles have no account session. Bind exclusively to this machine.
                mc.getSingleplayerServer().setUsesAuthentication(false);
                mc.getSingleplayerServer().getConnection().startTcpServerListener(java.net.InetAddress.getLoopbackAddress(),25571);
            }
            else if(op.equals("access"))mc.getSingleplayerServer().execute(()-> {
                var player=data.has("playerName")?mc.getSingleplayerServer().getPlayerList().getPlayerByName(data.get("playerName").getAsString())
                        :mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                var id=ResourceLocation.parse(data.get("id").getAsString());
                HemoCapabilityAccess.getLiberKnowledge(player).ifPresent(knowledge-> {
                    if(data.get("allow").getAsBoolean())knowledge.unlockEntry(id,CommonDiscoverySource.OTHER);
                    else {
                        var copy=new com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge();
                        knowledge.getUnlockedEntries().stream().filter(entry->!entry.equals(id)).forEach(entry->copy.unlockEntry(entry,CommonDiscoverySource.OTHER));
                        knowledge.getKnownMemos().forEach(copy::recordMemo);knowledge.setFrom(copy);
                    }
                    PacketDistributor.sendToPlayer(player,new PacketSyncLiberKnowledge(knowledge,data.get("allow").getAsBoolean()?java.util.Set.of(id):java.util.Set.of()));
                });
            });
            else if(op.equals("reload"))mc.reloadResourcePacks();
            else if(op.equals("char"))data.get("text").getAsString().chars().forEach(c->mc.screen.charTyped((char)c,0));
            else if(op.equals("quit"))mc.stop();
            else if(mc.screen instanceof BookReaderScreen reader) {
                var field=BookReaderScreen.class.getDeclaredField("session");field.setAccessible(true);
                var session=(BookReaderSession)field.get(reader);
                if(op.equals("entry"))session.open(ResourceLocation.parse(data.get("id").getAsString()));
                if(op.equals("view"))session.show(BookReaderSession.View.valueOf(data.get("view").getAsString()),data.has("chapter")?ResourceLocation.parse(data.get("chapter").getAsString()):null);
                if(op.equals("search"))session.search(data.get("text").getAsString());
                if(op.equals("resume"))session.resume();
                if(op.equals("adjacent"))session.adjacent(data.get("direction").getAsInt());
                if(op.equals("custom")) {
                    var page=session.entry(session.view().location().entryId()).orElseThrow();
                    page.setBodyRenderer(context->context.graphics().drawString(context.font(),"Custom renderer review",context.body().x(),context.body().y(),context.theme().color("ink"),false));
                    BookReaderScreen.refreshIfOpen();
                }
                if(op.equals("legacy")) {
                    var chapter=session.book().getChapters().getFirst();
                    var filtered=session.book().copyWithChapters(java.util.List.of(chapter.copyWithPages(java.util.List.of(chapter.getPages().getFirst()))));
                    BookReaderScreen.open(filtered,null,null,null);
                }
                if(op.equals("longRecord")) {
                    var target=BookPlaceboReloadListener.INSTANCE.findTarget(Hemomancy.rloc("libersanguinium/the_infection/pages/antecedent_vigil_record_read")).orElseThrow();
                    ((com.vincenthuto.hutoslib.common.data.book.PageTemplate)target.template()).setText("Long record continuation review. ".repeat(60));
                    ItemInit.liber_sanguinum.get().use(mc.level,mc.player,InteractionHand.MAIN_HAND);
                }
                if(op.equals("state")) {
                    var out=new com.google.gson.JsonObject();out.addProperty("view",session.view().toString());out.addProperty("entries",session.visibleContent().size());
                    out.add("saved",session.saved().toJson());
                    out.addProperty("leafIndex",readerField(reader,"leafIndex").toString());
                    out.addProperty("leafCount",((java.util.List<?>)readerField(reader,"leaves")).size());
                    out.addProperty("geometry",readerField(reader,"geometry").toString());
                    out.addProperty("searchResults",session.searchResults().toString());
                    var ids=new com.google.gson.JsonArray();session.visibleContent().forEach(content->ids.add(content.page().getId().toString()));out.add("ids",ids);
                    out.addProperty("faneAttention",BookClientHooks.attention(mc.player,(ItemGuideBook)ItemInit.liber_sanguinum.get()));
                    out.addProperty("liberAttention",BookClientHooks.attention(mc.player,(ItemGuideBook)ItemInit.liber_immaculatus.get()));
                    Files.writeString(root.resolve("book-state.json"),out.toString());
                }
            }
            Hemomancy.LOGGER.info("BOOK_KIT_REVIEW {}",data);
        } catch(Exception failure) {Hemomancy.LOGGER.error("BOOK_KIT_REVIEW failed",failure);}
    }

    private static Object readerField(BookReaderScreen reader,String name) throws ReflectiveOperationException {
        var field=BookReaderScreen.class.getDeclaredField(name);field.setAccessible(true);return field.get(reader);
    }
}
