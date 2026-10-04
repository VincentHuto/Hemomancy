package com.vincenthuto.hemomancy.gametest;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentContainer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.succession.*;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.*;
import java.util.*;

/** Disposable live review fixture; never loaded by normal clients. All assists are explicit. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class SuccessionClientReview {
    private static final Set<String> VIGIL_ORDEAL_WORLDS = Set.of(
            "PriorityVigilOrdeal_20261003", "PriorityVigilOrdealR2_20261003", "PriorityVigilOrdealR3_20261003");
    private static final String VIGIL_ACTIVATION_WORLD = "PriorityVigilActivation_20261003";
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.successionReview"))return;
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.getSingleplayerServer()==null)return;
        var root=mc.gameDirectory.toPath().toAbsolutePath().normalize();
        if(!root.endsWith("succession-client"))return;
        var request=root.resolve("succession-review.json");if(!Files.exists(request))return;
        try {
            String op=JsonParser.parseString(Files.readString(request)).getAsJsonObject().get("op").getAsString();Files.delete(request);
            if(op.equals("publish") || op.equals("publish_survival")) {
                mc.getSingleplayerServer().setUsesAuthentication(false);
                if(!mc.getSingleplayerServer().publishServer(op.equals("publish_survival") ? GameType.SURVIVAL : GameType.CREATIVE,false,25568))throw new IllegalStateException("Could not publish review world");
            } else if(op.equals("priority_loom_status")) {
                if (!mc.getSingleplayerServer().getWorldData().getLevelName().equals("PriorityLoomReload_20261003"))
                    throw new IllegalStateException("Loom preview readback requires its disposable world");
                writePriorityLoomStatus(mc.level, root.resolve("priority-loom-client-status.json"));
                mc.getSingleplayerServer().execute(() -> writePriorityLoomStatus(
                        mc.getSingleplayerServer().overworld(), root.resolve("priority-loom-server-status.json")));
            } else if(op.equals("labels")) {
                try {
                    var field=com.vincenthuto.hemomancy.client.render.entity.npc.SuccessionBrazierLabels.class.getDeclaredField("nearby");field.setAccessible(true);
                    Hemomancy.LOGGER.info("SUCCESSION_REVIEW label cache={}",field.get(null));
                } catch(ReflectiveOperationException failure) {throw new IllegalStateException(failure);}
                var block=mc.level.getBlockEntity(new BlockPos(5,101,0));
                Hemomancy.LOGGER.info("SUCCESSION_REVIEW label target={} renderer={} focus={} body={}",block,
                        block==null?null:mc.getBlockEntityRenderDispatcher().getRenderer(block),
                        mc.level.getBlockState(new BlockPos(0,100,0)),mc.level.getBlockState(new BlockPos(2,102,0)));
                for(var direction:Direction.Plane.HORIZONTAL) {
                    var focus=new BlockPos(5,101,0).relative(direction,-5).below();
                    Hemomancy.LOGGER.info("SUCCESSION_REVIEW label direction={} focus={} block={} body={}",direction,focus,mc.level.getBlockState(focus),mc.level.getBlockState(focus.relative(direction,2).above(2)));
                }
            } else if(op.equals("residents")) {
                com.vincenthuto.hemomancy.client.screen.item.SuccessionResidentsScreen.open(null);
            } else mc.getSingleplayerServer().execute(()-> {
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
                if(op.equals("setup"))setup(player);
                if(op.equals("priority_coop_setup")) setupPriorityCoop(player);
                if(op.equals("priority_coop_status")) writePriorityCoopStatus(player, root);
                if(op.equals("priority_vigil_setup")) setupPriorityVigil(player, root);
                if(op.equals("priority_vigil_ordeal_setup")) setupPriorityVigil(player, root, true);
                if(op.equals("priority_vigil_activation_setup")) setupPriorityVigil(player, root, true, true);
                if(op.equals("priority_vigil_activation_status")) writePriorityVigilOrdealStatus(player, root);
                if(op.equals("priority_vigil_ordeal_status")) writePriorityVigilOrdealStatus(player, root);
                if(op.equals("priority_vigil_status")) writePriorityVigilStatus(player, root);
                if(op.equals("priority_vigil_reward")) completePriorityVigil(player, root);
                if(op.equals("priority_pome_setup")) setupPriorityPomes(player, root);
                if(op.equals("priority_communion_setup")) setupPriorityPomes(player, root, true);
                if(op.equals("priority_pome_status")) writePriorityPomeStatus(player, root);
                if(op.equals("priority_pome_foreign_copy")) {
                    var guest = priorityPomeGuest(player);
                    var bloom = com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData
                            .get(player.server.overworld()).getBloomAt(new BlockPos(0,101,2),
                                    player.level().dimension().location().toString());
                    if (bloom == null || !guest.getMainHandItem().isEmpty())
                        throw new IllegalStateException("Foreign-pome negative fixture requires its Bloom and empty guest hand");
                    guest.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                            com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem.createPickedPomeStack(bloom, 0));
                    Hemomancy.LOGGER.info("PRIORITY_POME supplied a foreign-owned bound copy to guest for consumption refusal; not a natural transfer");
                    writePriorityPomeStatus(player, root);
                }
                if(op.equals("priority_vigil_clear_effects")) {
                    var guest = priorityVigilGuest(player);
                    player.removeAllEffects(); guest.removeAllEffects();
                    writePriorityVigilStatus(player, root);
                }
                if(op.equals("priority_scar_setup")) setupPriorityScars(player, root);
                if(op.equals("priority_dream_reward_claim")) claimPriorityDreamRewards(player, root);
                if(op.equals("priority_npc_reward_prepare")) preparePriorityNpcRewards(player, root);
                if(op.startsWith("priority_ledger_")) setupPriorityLedger(player, root, op.substring("priority_ledger_".length()));
                if(op.equals("priority_chamber_checkpoint")) {
                    if (!player.server.getWorldData().getLevelName().equals("PriorityChamberCrash_20261002")
                            || !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(player)
                            || !player.level().dimension().equals(com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.CHAMBER_OF_WILL))
                        throw new IllegalStateException("Checkpoint requires the active disposable Chamber visit");
                    player.server.saveEverything(false, true, true);
                    try { Files.writeString(root.resolve("priority-chamber-checkpoint.json"),
                            "{\"active\":true,\"insideChamber\":true,\"saveCompleted\":true}"); }
                    catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
                    Hemomancy.LOGGER.info("PRIORITY_CHAMBER_REVIEW active visit checkpoint flushed; no return/logout invoked");
                }
                if(op.equals("priority_chamber_coop_checkpoint")) checkpointPriorityChamberCoop(player, root);
                if(op.equals("priority_chamber_coop_baseline")) recordPriorityChamberCoopBaseline(player, root);
                if(op.equals("start")) {
                    player.setGameMode(GameType.SURVIVAL);
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ItemInit.living_staff.get()));
                    var result=com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket.tryStartCardinalRite(player,new BlockPos(0,100,0),CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
                    Hemomancy.LOGGER.info("SUCCESSION_REVIEW activation {}",result);
                }
                if(op.equals("hostiles")) for(int i=0;i<SuccessionProfessions.ROLES.size();i++) {
                    var npc=(ProfessionalHarbingerEntity)BuiltInRegistries.ENTITY_TYPE.get(SuccessionProfessions.entityType(SuccessionProfessions.ROLES.get(i))).create(player.serverLevel());
                    npc.initializeMisbegotten(player.getUUID(),i,"Broken Teacher");npc.setNoAi(true);npc.setPos(-8+i*4,101,10);player.serverLevel().addFreshEntity(npc);
                }
                if(op.equals("status")) {
                    var data=SuccessionSavedData.get(player.serverLevel());var rite=CardinalRiteSavedData.get(player.serverLevel()).getRite(player.getUUID());
                    Hemomancy.LOGGER.info("SUCCESSION_REVIEW residents={} locks={} rite={}",data.residents.size(),data.ledger.reservations().size(),rite==null?"none":rite.succession());
                }
            });
        }catch(Exception failure){Hemomancy.LOGGER.error("SUCCESSION_REVIEW",failure);}
    }
    private static void recordPriorityChamberCoopBaseline(ServerPlayer host, Path root) {
        if (!Set.of("PriorityChamberCrashCoopR2_20261002", "PriorityChamberCrashCoopR3_20261002")
                .contains(host.server.getWorldData().getLevelName()))
            throw new IllegalStateException("Co-op baseline requires its disposable world");
        var guest = host.server.getPlayerList().getPlayerByName("SucObserver");
        if (guest == null) throw new IllegalStateException("Co-op baseline requires the connected guest");
        var players = new com.google.gson.JsonArray();
        for (var participant : List.of(host, guest)) {
            if (com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(participant)
                    || participant.serverLevel() != host.server.overworld())
                throw new IllegalStateException("Co-op baseline must precede both visits");
            var state = new com.google.gson.JsonObject();
            state.addProperty("uuid", participant.getUUID().toString());
            state.addProperty("inventory", participant.getInventory().save(new net.minecraft.nbt.ListTag()).toString());
            var equipment = (HarbingerEquipmentContainer) HemoCapabilityAccess.requireEquipment(participant);
            state.addProperty("equipment", equipment.serializeNBT(participant.registryAccess()).toString());
            players.add(state);
        }
        try { Files.writeString(root.resolve("priority-chamber-coop-baseline.json"), players.toString()); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
    }
    private static void checkpointPriorityChamberCoop(ServerPlayer host, Path root) {
        if (!Set.of("PriorityChamberCrashCoopR2_20261002", "PriorityChamberCrashCoopR3_20261002")
                .contains(host.server.getWorldData().getLevelName()))
            throw new IllegalStateException("Co-op checkpoint requires its disposable world");
        var guest = host.server.getPlayerList().getPlayerByName("SucObserver");
        if (guest == null) throw new IllegalStateException("Co-op checkpoint requires the connected guest");
        var record = new com.google.gson.JsonObject();
        var players = new com.google.gson.JsonArray();
        for (var participant : List.of(host, guest)) {
            if (!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(participant)
                    || com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.mode(participant)
                    != com.vincenthuto.hemomancy.common.worldgen.ChamberVisitMode.GUIDED
                    || !participant.level().dimension().equals(
                            com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.CHAMBER_OF_WILL))
                throw new IllegalStateException("Co-op checkpoint requires both active guided visits inside the Chamber");
            var state = new com.google.gson.JsonObject();
            state.addProperty("uuid", participant.getUUID().toString());
            state.addProperty("persistent", participant.getPersistentData().toString());
            players.add(state);
        }
        record.add("players", players);
        record.addProperty("serverTick", host.serverLevel().getGameTime());
        if (!host.server.saveEverything(false, true, true))
            throw new IllegalStateException("Co-op checkpoint did not save the server");
        record.addProperty("saveCompleted", true);
        try { Files.writeString(root.resolve("priority-chamber-coop-checkpoint.json"), record.toString()); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
        Hemomancy.LOGGER.info("PRIORITY_CHAMBER_COOP active guided visits flushed; no return/logout invoked");
    }
    private static void preparePriorityNpcRewards(ServerPlayer host, Path root) {
        var guest = host.server.getPlayerList().getPlayerByName("SucObserver");
        if (!host.server.getWorldData().getLevelName().equals("PriorityNpcRewards_20261003") || guest == null)
            throw new IllegalStateException("NPC reward fixture requires its disposable world and connected guest");
        for (var participant : List.of(host, guest)) {
            if (HemoCapabilityAccess.getPlayerDegreeNumber(participant) != 3
                    || !com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge.eligible(participant)
                    || !com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment.canBrief(participant)
                    || com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.isFirstSeparationStarted(participant)
                    || com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.isFirstSeparationComplete(participant)
                    || com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment.isClaimed(participant)
                    || com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(participant)
                    || !participant.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD))
                throw new IllegalStateException("NPC reward preparation requires two fresh eligible D3 players outside visits");
        }
        com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment.markBriefed(host);
        com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(host,
                com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
        com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(host,
                com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
        var record = new com.google.gson.JsonObject();
        var expected = new net.minecraft.nbt.ListTag();
        com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment.rewardStacks()
                .forEach(stack -> expected.add(stack.save(host.registryAccess())));
        record.addProperty("expectedRewards", expected.toString());
        for (var participant : List.of(host, guest)) {
            var state = new com.google.gson.JsonObject();
            state.addProperty("uuid", participant.getUUID().toString());
            state.addProperty("inventory", participant.getInventory().save(new net.minecraft.nbt.ListTag()).toString());
            state.addProperty("persistent", participant.getPersistentData().toString());
            record.add(participant == host ? "host" : "guest", state);
        }
        try { Files.writeString(root.resolve("priority-npc-reward-fixture.json"), record.toString()); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
        Hemomancy.LOGGER.info("PRIORITY_NPC_REWARD supplied host assignment proofs only; no claim, delivery or guest proof invoked");
    }

    private static void claimPriorityDreamRewards(ServerPlayer player, Path root) {
        if (!player.server.getWorldData().getLevelName().equals("PriorityDreamRewards_20261002")
                || !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isObservational(player)
                || !player.level().dimension().equals(com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.CHAMBER_OF_WILL))
            throw new IllegalStateException("Reward fixture requires its disposable observational visit");
        com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment.markBriefed(player);
        com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(player,
                com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
        com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(player,
                com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
        if (!com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment.claimRewards(player))
            throw new IllegalStateException("Supplied assignment eligibility did not allow claim");
        var expected = new net.minecraft.nbt.ListTag();
        com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment.rewardStacks()
                .forEach(stack -> expected.add(stack.save(player.registryAccess())));
        var record = new com.google.gson.JsonObject();
        record.addProperty("expectedRewards", expected.toString());
        record.addProperty("persistentAfterClaim", player.getPersistentData().toString());
        try { Files.writeString(root.resolve("priority-dream-reward-fixture.json"), record.toString()); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
        Hemomancy.LOGGER.info("PRIORITY_DREAM_REWARD supplied assignment proofs and invoked real claim during observational visit; no natural quest completion claimed");
    }

    private static void setupPriorityLedger(ServerPlayer player, Path root, String state) {
        if (!player.server.getWorldData().getLevelName().equals("PriorityLedger_20261002"))
            throw new IllegalStateException("Ledger visual fixture requires its disposable world");
        var path = com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.valueOf(state);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(player);
        degree.setDegreeNumber(path == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.APOTHEOS
                || path == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_ARCHON ? 8 : 7);
        degree.setArchonPath(path);
        com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents.syncDegree(player, degree);
        player.getInventory().selected = 0;
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ItemInit.harbinger_assignment_ledger.get()));
        var record = new com.google.gson.JsonObject();
        record.addProperty("archonPath", path.name());
        record.addProperty("degree", degree.getDegreeNumber());
        try { Files.writeString(root.resolve("priority-ledger-fixture.json"), record.toString()); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
        Hemomancy.LOGGER.info("PRIORITY_LEDGER_REVIEW supplied ending state {} and ledger; no natural ending claimed", path);
    }

    private static void setupPriorityScars(ServerPlayer player, Path root) {
        if (!player.server.getWorldData().getLevelName().equals("PriorityScars_20261002"))
            throw new IllegalStateException("Scar visual fixture requires its disposable world");
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(player);
        degree.setDegreeNumber(6);
        var progress = HemoCapabilityAccess.requireSkillProgress(player);
        for (int rank = 0; rank < 3; rank++) progress.unlockOrLevel(SkillPointInit.skill_scar_resonance);
        var scars = HemoCapabilityAccess.getScarState(player).orElseThrow();
        var ids = ScarInit.SCARS_TYPE_REGISTRY.keySet().stream()
                .filter(id -> ScarInit.getByName(id.toString()).getScarType()
                        == com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarType.CEREBRAL)
                .sorted(Comparator.comparing(net.minecraft.resources.ResourceLocation::toString)).limit(7).toList();
        if (ids.size() != 7) throw new IllegalStateException("Seven cerebral scars required");
        ids.forEach(id -> { scars.addKnownCerebralScar(id); scars.activateCerebralScar(id); });
        com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents.syncDegree(player, degree);
        com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents.syncSkills(player);
        com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarStateSyncEvents.syncToClient(player);
        for (var pos : BlockPos.betweenClosed(-3, 100, -3, 3, 100, 3))
            player.serverLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        player.serverLevel().setBlock(new BlockPos(2, 101, 0), BlockInit.mason_effigy.get().defaultBlockState(), 3);
        player.teleportTo(0.5, 101, 0.5);
        var result = new com.google.gson.JsonObject();
        result.addProperty("degree", degree.getDegreeNumber());
        result.addProperty("capacity", ScarBrazierRite.getMaxActiveScars(player));
        result.addProperty("activeCount", scars.getActiveCerebralScars().size());
        var selected = new com.google.gson.JsonArray();
        ids.forEach(id -> selected.add(id.toString()));
        result.add("suppliedScars", selected);
        try { Files.writeString(root.resolve("priority-scar-fixture.json"), result.toString()); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
        Hemomancy.LOGGER.info("PRIORITY_SCAR_REVIEW supplied degree, ranks, known/active scars and station; no earned progression claimed");
    }

    private static void writePriorityLoomStatus(net.minecraft.world.level.Level level, Path output) {
        if (!(level.getBlockEntity(new BlockPos(0,101,2)) instanceof
                com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity loom))
            throw new IllegalStateException("Review Loom is missing");
        var result = new com.google.gson.JsonObject();
        result.addProperty("gameTime",level.getGameTime());
        result.addProperty("client",level.isClientSide);
        var recipeId = Hemomancy.rloc("memory_weaving/memory_blood_shot");
        result.addProperty("recipePresent",level.getRecipeManager().byKey(recipeId).isPresent());
        var preview = loom.getPreviewRecipe();
        result.addProperty("preview",preview == null ? "" : preview.getResultItem(level.registryAccess())
                .saveOptional(level.registryAccess()).toString());
        result.addProperty("loom",loom.saveWithoutMetadata(level.registryAccess()).toString());
        try { Files.writeString(output,result.toString()); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
    }

    private static ServerPlayer priorityPomeGuest(ServerPlayer host) {
        if (!host.server.getWorldData().getLevelName().equals("PriorityPomes_20261002")
                && !host.server.getWorldData().getLevelName().equals("PriorityCommunion_20261003"))
            throw new IllegalStateException("Pome review requires its disposable world");
        var guest = host.server.getPlayerList().getPlayerByName("SucObserver");
        if (guest == null || guest.serverLevel() != host.serverLevel())
            throw new IllegalStateException("Both pome clients must be connected in the same level");
        return guest;
    }

    private static void setupPriorityPomes(ServerPlayer host, Path root) {
        setupPriorityPomes(host, root, false);
    }

    private static void setupPriorityPomes(ServerPlayer host, Path root, boolean naturalRipening) {
        if (!host.server.getWorldData().getLevelName().equals(naturalRipening
                ? "PriorityCommunion_20261003" : "PriorityPomes_20261002"))
            throw new IllegalStateException("Pome setup requires its exact disposable world");
        var guest = priorityPomeGuest(host);
        var data = com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.get(host.server.overworld());
        if (!data.getBlooms().isEmpty()) throw new IllegalStateException("Pome setup requires a fresh Bloom-free world");
        for (var participant : List.of(host,guest)) {
            if (!participant.isAlive() || participant.isSpectator()
                    || HemoCapabilityAccess.requireInitiatoryDegree(participant).getTotalPomesConsumed() != 0
                    || !participant.getInventory().getItem(1).isEmpty())
                throw new IllegalStateException("Pome participants must have no consumption progress and an empty hotbar slot 1");
            participant.getInventory().selected = 1;
            participant.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(1));
        }
        var level = host.serverLevel();
        for (var pos : BlockPos.betweenClosed(-4,100,-4,4,100,4)) level.setBlock(pos, Blocks.STONE.defaultBlockState(),3);
        for (var pos : BlockPos.betweenClosed(-4,101,-4,4,109,4)) level.setBlock(pos, Blocks.AIR.defaultBlockState(),3);
        var line = new Bloodline("Priority Pomes",host.getUUID(),UUID.randomUUID(),
                new ArrayList<>(List.of(host.getUUID(),guest.getUUID())));
        BloodlineSavedData.get(host.server.overworld()).registerBloodline(line);
        for (var participant : List.of(host,guest)) {
            participant.setGameMode(GameType.SURVIVAL);
            var blood = HemoCapabilityAccess.requireBloodVolume(participant);
            blood.setBloodLine(line); blood.setActive(true); blood.setBloodVolume(1000);
            HemoCapabilityAccess.requireInitiatoryDegree(participant).setDegreeNumber(7);
            BloodVolumeEvents.syncVolume(participant,blood);
            com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents
                    .syncDegree(participant,HemoCapabilityAccess.requireInitiatoryDegree(participant));
            participant.teleportTo(level,participant == host ? .5 : 2.5,101,.5,0,20);
        }
        var center = new BlockPos(0,101,2);
        level.setBlock(center,BlockInit.qliphoth_bloom.get().defaultBlockState(),3);
        ((com.vincenthuto.hemomancy.common.block.harbinger.functional.QliphothBloomBlock)BlockInit.qliphoth_bloom.get())
                .setPlacedBy(level,center,level.getBlockState(center),host,ItemStack.EMPTY);
        var bloom = new com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.BloomEntry(
                host.getUUID(),center,level.dimension().location().toString(),1,level.getGameTime());
        data.addBloom(bloom);
        if (!naturalRipening) { data.incrementPomesDropped(bloom); data.setPendingPome(bloom,0); }
        com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents.syncQliphothBlooms(host.server);
        Hemomancy.LOGGER.info(naturalRipening
                ? "PRIORITY_COMMUNION supplied two D7 bloodline members and owned Bloom; no ripe husk, pick, consumption or Communion proof"
                : "PRIORITY_POME supplied two D7 bloodline members, owned Bloom and first ripe husk; no pick, consumption or Communion proof");
        writePriorityPomeStatus(host,root);
    }

    private static void writePriorityPomeStatus(ServerPlayer host, Path root) {
        var guest = priorityPomeGuest(host);
        var data = com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.get(host.server.overworld());
        var bloom = data.getBloomAt(new BlockPos(0,101,2),host.level().dimension().location().toString());
        if (bloom == null) throw new IllegalStateException("Pome review Bloom is missing");
        var result = new com.google.gson.JsonObject();
        result.addProperty("serverTick",host.serverLevel().getGameTime());
        result.addProperty("bloomId",bloom.bloomId().toString()); result.addProperty("owner",bloom.ownerUUID().toString());
        result.addProperty("ripened",data.getPomesDropped(bloom));
        result.addProperty("bloomState",data.getState(bloom).name());
        result.addProperty("pendingHusk",data.getPendingPomeHuskIndex(bloom));
        result.addProperty("claimed",data.isPendingPomeClaimed(bloom));
        var players = new com.google.gson.JsonArray();
        for (var participant : List.of(host,guest)) {
            var state = new com.google.gson.JsonObject();
            var degree = HemoCapabilityAccess.requireInitiatoryDegree(participant);
            state.addProperty("name",participant.getGameProfile().getName());
            state.addProperty("uuid",participant.getUUID().toString());
            state.addProperty("gameMode",participant.gameMode.getGameModeForPlayer().getName());
            state.addProperty("pomes",degree.getTotalPomesConsumed());
            state.addProperty("fromBloom",degree.getPomesConsumedFromBloom(bloom.bloomId(),bloom.center().asLong(),false));
            state.addProperty("communion",degree.isQliphothCommunionDone());
            state.addProperty("degree",degree.getDegreeNumber());
            state.addProperty("archonPath",degree.getArchonPath().name());
            state.addProperty("spineGranted",degree.hasFungalSpineGranted());
            state.addProperty("health",participant.getHealth());
            state.addProperty("inventory",participant.getInventory().save(new net.minecraft.nbt.ListTag()).toString());
            state.addProperty("mainHand",participant.getMainHandItem().saveOptional(participant.registryAccess()).toString());
            players.add(state);
        }
        result.add("players",players);
        try { Files.writeString(root.resolve("priority-pome-status.json"),result.toString()); }
        catch(java.io.IOException failure) { throw new IllegalStateException(failure); }
    }

    private static ServerPlayer priorityVigilGuest(ServerPlayer host) {
        if (!host.server.getWorldData().getLevelName().equals("PriorityVigil_20261002")
                && !VIGIL_ORDEAL_WORLDS.contains(host.server.getWorldData().getLevelName())
                && !VIGIL_ACTIVATION_WORLD.equals(host.server.getWorldData().getLevelName()))
            throw new IllegalStateException("Vigil review requires its disposable world");
        var guest = host.server.getPlayerList().getPlayerByName("SucObserver");
        if (guest == null || guest.serverLevel() != host.serverLevel())
            throw new IllegalStateException("Both Vigil clients must be connected in the same level");
        return guest;
    }

    private static void setupPriorityVigil(ServerPlayer host, Path root) {
        setupPriorityVigil(host, root, false);
    }

    private static void setupPriorityVigil(ServerPlayer host, Path root, boolean ordeal) {
        setupPriorityVigil(host, root, ordeal, false);
    }

    private static void setupPriorityVigil(ServerPlayer host, Path root, boolean ordeal, boolean activation) {
        String worldName = host.server.getWorldData().getLevelName();
        boolean validWorld = activation ? VIGIL_ACTIVATION_WORLD.equals(worldName)
                : ordeal ? VIGIL_ORDEAL_WORLDS.contains(worldName) : worldName.equals("PriorityVigil_20261002");
        if (!validWorld)
            throw new IllegalStateException("Vigil setup requires its exact disposable world");
        var guest = priorityVigilGuest(host);
        var level = host.serverLevel();
        var rites = CardinalRiteSavedData.get(level);
        if (rites.getRite(host.getUUID()) != null)
            throw new IllegalStateException("Vigil fixture must not replace an active rite");
        if (ordeal) for (var participant : List.of(host, guest)) {
            if (!participant.isAlive() || participant.isSpectator()
                    || rites.getRite(participant.getUUID()) != null
                    || !participant.getInventory().getItem(1).isEmpty()
                    || com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(
                            participant, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED))
                throw new IllegalStateException("Ordeal review requires living unproven participants with no rite and an empty hotbar slot 1");
        }
        var line = new Bloodline("Priority Vigil", host.getUUID(), UUID.randomUUID(),
                new ArrayList<>(List.of(host.getUUID(), guest.getUUID())));
        BloodlineSavedData.get(host.server.overworld()).registerBloodline(line);
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
                level, Hemomancy.rloc("cardinal_rite/covenant_vigil"));
        var floor = CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElseThrow();
        var center = new BlockPos(0, 101, 0);
        for (var pos : BlockPos.betweenClosed(-18, 99, -18, 18, 99, 18))
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        for (var pos : BlockPos.betweenClosed(-18, 100, -18, 18, 112, 18))
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        int physicalFocusY = floor.pattern().getBlockPattern().getHeight() - floor.focus().getY() - 1;
        for (var pair : floor.pattern().getBlockPosBlockList()) {
            if (pair.getBlock() == null || pair.getBlock() == Blocks.AIR) continue;
            var pos = pair.getPos();
            level.setBlock(center.offset(pos.getX() - floor.focus().getX(), pos.getY() - physicalFocusY,
                    floor.focus().getZ() - pos.getZ()), pair.getBlock().defaultBlockState(), 3);
        }
        int socketIndex = 0;
        for (var requirement : recipe.getBrazierSignature()) for (int i = 0; i < requirement.count(); i++) {
            var relative = floor.brazierSockets().get(socketIndex++);
            var socket = center.offset(relative.getX(), relative.getY(), -relative.getZ());
            level.setBlock(socket, BlockInit.iron_brazier.get().defaultBlockState()
                    .setValue(BrazierBlock.RITUAL_PHASE, 1), 3);
            if (!(level.getBlockEntity(socket) instanceof IronBrazierBlockEntity brazier)
                    || !brazier.insertOffering(null, requirement.ingredient().getItems()[0].copyWithCount(1)))
                throw new IllegalStateException("Vigil offering fixture failed");
        }
        ActiveCardinalRite rite = null;
        if (!activation) {
            rite = ActiveCardinalRite.interactive(host.getUUID(), center, recipe.getId(),
                    recipe.getCeremony().targetDurationTicks(), ordeal ? recipe.getRiteType().getSize() : 5,
                    recipe.getRequiredDegree(), recipe.getCeremony().abbreviated(), ordeal ? 3 : 0,
                    recipe.getCeremony().anchors().size());
            if (ordeal) {
                var stationMatch = CardinalRiteStationMatcher.find(level, center, recipe).orElseThrow(
                        () -> new IllegalStateException("Ordeal fixture must match the authored station"));
                rite.setMatchedFloor(stationMatch.floor().id(), stationMatch.floorMatch().getForwards(),
                        stationMatch.floorMatch().getUp());
                rite.captureOfferingItinerary(stationMatch.braziers().stream()
                        .map(offering -> new ActiveCardinalRite.RiteOffering(
                                offering.pos(), offering.stack(), offering.consumeOnSuccess())).toList());
            }
            for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
            if (!rite.enterInscription()) throw new IllegalStateException("Vigil fixture did not enter inscription");
        }
        var station = center.offset(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .markers(recipe).get(CardinalRiteAllyRole.ANCHOR));
        level.setBlock(station.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(station, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(station.above(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(station.offset(1, -1, 0), Blocks.STONE.defaultBlockState(), 3);
        for (var participant : List.of(host, guest)) {
            participant.setGameMode(GameType.SURVIVAL);
            participant.removeAllEffects();
            var blood = HemoCapabilityAccess.requireBloodVolume(participant);
            blood.setBloodLine(line); blood.setActive(true); blood.setBloodVolume(5000);
            HemoCapabilityAccess.requireInitiatoryDegree(participant).setDegreeNumber(activation && participant == guest ? 0 : 6);
            BloodVolumeEvents.syncVolume(participant, blood);
            com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents
                    .syncDegree(participant, HemoCapabilityAccess.requireInitiatoryDegree(participant));
            if (ordeal) {
                participant.getInventory().selected = 1;
                participant.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(1));
            } else participant.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        host.teleportTo(level, .5, 102, .5, 0, 0);
        guest.teleportTo(level, station.getX() + 1.5, station.getY(), station.getZ() + .5, 90, 60);
        if (!activation) rites.startRite(rite);
        if (activation) Hemomancy.LOGGER.info("PRIORITY_VIGIL_ACTIVATION supplied D6 host/D0 guest, bloodline, floor and offerings; no rite, staff, anchors, inscription or reward");
        else Hemomancy.LOGGER.info("PRIORITY_VIGIL supplied two D6 members, floor/offerings, paid anchors and inscription; no helper assignment, ordeal or completion");
        if (ordeal) writePriorityVigilOrdealStatus(host, root);
        else writePriorityVigilStatus(host, root);
    }

    private static void writePriorityVigilOrdealStatus(ServerPlayer host, Path root) {
        if (!VIGIL_ORDEAL_WORLDS.contains(host.server.getWorldData().getLevelName())
                && !VIGIL_ACTIVATION_WORLD.equals(host.server.getWorldData().getLevelName()))
            throw new IllegalStateException("Ordeal status requires its exact disposable world");
        var guest = priorityVigilGuest(host);
        var rite = CardinalRiteSavedData.get(host.serverLevel()).getRite(host.getUUID());
        var result = new com.google.gson.JsonObject();
        result.addProperty("serverTick", host.serverLevel().getGameTime());
        result.addProperty("rite", rite == null ? "" : rite.serialize(host.registryAccess()).toString());
        var players = new com.google.gson.JsonArray();
        for (var participant : List.of(host, guest)) {
            var state = new com.google.gson.JsonObject();
            state.addProperty("name", participant.getGameProfile().getName());
            state.addProperty("uuid", participant.getUUID().toString());
            state.addProperty("health", participant.getHealth());
            state.addProperty("alive", participant.isAlive());
            state.addProperty("gameMode", participant.gameMode.getGameModeForPlayer().getName());
            state.addProperty("degree", HemoCapabilityAccess.getPlayerDegreeNumber(participant));
            state.addProperty("bloodMl", HemoCapabilityAccess.requireBloodVolume(participant).getBloodVolume());
            state.addProperty("inventory", participant.getInventory().save(new net.minecraft.nbt.ListTag()).toString());
            state.addProperty("resistance", participant.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE));
            state.addProperty("regeneration", participant.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION));
            state.addProperty("vigilProof", com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(
                    participant, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED));
            players.add(state);
        }
        result.add("players", players);
        try { Files.writeString(root.resolve("priority-vigil-ordeal-status.json"), result.toString()); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
    }

    private static void completePriorityVigil(ServerPlayer host, Path root) {
        if (!host.server.getWorldData().getLevelName().equals("PriorityVigil_20261002"))
            throw new IllegalStateException("Reward callback assist is forbidden in the ordeal review world");
        priorityVigilGuest(host);
        var rite = CardinalRiteSavedData.get(host.serverLevel()).getRite(host.getUUID());
        if (rite == null || !rite.getRecipeId().equals(Hemomancy.rloc("cardinal_rite/covenant_vigil")))
            throw new IllegalStateException("Vigil reward review requires its supplied rite");
        try {
            var handler = com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents.class
                    .getDeclaredMethod("completeCovenantVigil", net.minecraft.server.level.ServerLevel.class,
                            ServerPlayer.class, ActiveCardinalRite.class);
            handler.setAccessible(true);
            handler.invoke(null, host.serverLevel(), host, rite);
        } catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
        Hemomancy.LOGGER.info("PRIORITY_VIGIL invoked production completion handler as explicit reward-isolation assist; no ordeal completion claimed");
        writePriorityVigilStatus(host, root);
    }

    private static void writePriorityVigilStatus(ServerPlayer host, Path root) {
        var guest = priorityVigilGuest(host);
        var rite = CardinalRiteSavedData.get(host.serverLevel()).getRite(host.getUUID());
        if (rite == null) throw new IllegalStateException("Vigil review rite is missing");
        var result = new com.google.gson.JsonObject();
        result.addProperty("serverTick", host.serverLevel().getGameTime());
        result.addProperty("phase", rite.getPhase().name());
        var station = rite.getCenterPos().offset(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .markers(com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
                        host.serverLevel(), rite.getRecipeId())).get(CardinalRiteAllyRole.ANCHOR));
        result.addProperty("stationX", station.getX()); result.addProperty("stationY", station.getY());
        result.addProperty("stationZ", station.getZ());
        var players = new com.google.gson.JsonArray();
        for (var participant : List.of(host, guest)) {
            var state = new com.google.gson.JsonObject();
            state.addProperty("name", participant.getGameProfile().getName());
            state.addProperty("uuid", participant.getUUID().toString());
            state.addProperty("role", String.valueOf(rite.getAllyRoles().get(participant.getUUID())));
            state.addProperty("optIn", rite.getSharedPoolOptIns().contains(participant.getUUID()));
            state.addProperty("available", com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                    .isAvailable(host.serverLevel(), rite, participant.getUUID()));
            state.addProperty("spectator", participant.isSpectator());
            state.addProperty("resistance", participant.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE));
            state.addProperty("regeneration", participant.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION));
            state.addProperty("vigilProof", com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter
                    .hasAdvancement(participant, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED));
            players.add(state);
        }
        result.add("players", players);
        try { Files.writeString(root.resolve("priority-vigil-status.json"), result.toString()); }
        catch (java.io.IOException failure) { throw new IllegalStateException(failure); }
    }

    private static void setupPriorityCoop(ServerPlayer host) {
        if (!host.server.getWorldData().getLevelName().equals("PriorityCoop_20261002"))
            throw new IllegalStateException("Priority co-op fixture requires its disposable world");
        var member = host.server.getPlayerList().getPlayerByName("SucObserver");
        if (member == null) throw new IllegalStateException("Connect the observer before fixture setup");
        var level = host.serverLevel();
        var heart = new BlockPos(0, 101, 2);
        var line = new Bloodline("Priority co-op", UUID.randomUUID(), UUID.randomUUID(),
                new ArrayList<>(List.of(host.getUUID(), member.getUUID())));
        BloodlineSavedData.get(host.server.overworld()).registerBloodline(line);
        for (var pos : BlockPos.betweenClosed(-4, 100, -4, 4, 100, 4))
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(heart, BlockInit.consecrated_bloodwell.get().defaultBlockState(), 3);
        FoundingFaneSavedData.get(level).consecrateHeart(line.getLeaderUUID(), heart);
        for (var player : List.of(host, member)) {
            player.setGameMode(GameType.SURVIVAL);
            var blood = HemoCapabilityAccess.requireBloodVolume(player);
            blood.setBloodLine(line);
            blood.setActive(true);
            blood.setBloodVolume(1000);
            var degree = HemoCapabilityAccess.requireInitiatoryDegree(player);
            degree.setDegreeNumber(5);
            BloodVolumeEvents.syncVolume(player, blood);
            com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents.syncDegree(player, degree);
            player.teleportTo(level, player == member ? .5 : 2.5, 101, .5, 0, 25);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                    new ItemStack(ItemInit.blood_projection.get()));
            if (com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player,
                    com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE))
                throw new IllegalStateException("Fresh co-op participant already has covenant proof");
        }
        Hemomancy.LOGGER.info("PRIORITY_COOP supplied two D5 members, absent leader, registered Fane, blood and projection tools; no chapter proof");
    }

    private static void writePriorityCoopStatus(ServerPlayer host, Path root) {
        if (!host.server.getWorldData().getLevelName().equals("PriorityCoop_20261002"))
            throw new IllegalStateException("Priority co-op status requires its disposable world");
        var result = new com.google.gson.JsonObject();
        result.addProperty("serverTick", host.serverLevel().getGameTime());
        var line = HemoCapabilityAccess.requireBloodVolume(host).getBloodLine();
        var canonical = BloodlineSavedData.get(host.server.overworld()).getBloodline(line.getBloodlineUUID());
        result.addProperty("poolBlood", canonical.getBloodVolume());
        var players = new com.google.gson.JsonArray();
        for (String name : List.of("SuccessionReview", "SucObserver")) {
            var player = host.server.getPlayerList().getPlayerByName(name);
            if (player == null) throw new IllegalStateException("Both co-op clients must remain connected");
            var state = new com.google.gson.JsonObject();
            state.addProperty("name", name);
            state.addProperty("blood", HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume());
            state.addProperty("degree", HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber());
            state.addProperty("canonicalMember", canonical.hasMember(player.getUUID()));
            state.addProperty("covenantProof", com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter
                    .hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE));
            players.add(state);
        }
        result.add("players", players);
        try {
            Files.writeString(root.resolve("priority-coop-status.json"), result.toString());
        } catch (java.io.IOException failure) {
            throw new java.io.UncheckedIOException(failure);
        }
    }

    private static void setup(ServerPlayer p) {
        var level=p.serverLevel();var center=new BlockPos(0,100,0);
        for(var pos:BlockPos.betweenClosed(-18,99,-18,18,100,18))level.setBlock(pos,Blocks.STONE.defaultBlockState(),3);
        for(var pos:BlockPos.betweenClosed(-18,101,-18,18,108,18))level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        p.teleportTo(level,.5,101,-4.5,0,10);p.setGameMode(GameType.CREATIVE);
        level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
        var line=new Bloodline("Succession Review",p.getUUID(),UUID.randomUUID(),new ArrayList<>(List.of(p.getUUID())));
        BloodlineSavedData.get(p.server.overworld()).registerBloodline(line);
        HemoCapabilityAccess.requireBloodVolume(p).setBloodLine(line);HemoCapabilityAccess.requireBloodVolume(p).setActive(true);HemoCapabilityAccess.requireBloodVolume(p).setBloodVolume(5000);
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        var heart=center.offset(0,1,12);level.setBlock(heart,BlockInit.consecrated_bloodwell.get().defaultBlockState(),3);FoundingFaneSavedData.get(level).consecrateHeart(p.getUUID(),heart);
        var floor=CardinalRiteFloorRegistry.get(Hemomancy.rloc("dominion_greater")).orElseThrow();var pattern=floor.pattern().getPatternArray();
        for(int z=0;z<7;z++)for(int x=0;x<7;x++)level.setBlock(center.offset(x-3,0,3-z),floor.pattern().getSymbolList().get(String.valueOf(pattern[z][0].charAt(x))).defaultBlockState(),3);
        level.setBlock(center.offset(2,1,0),BlockInit.venous_stone.get().defaultBlockState(),3);level.setBlock(center.offset(2,2,0),BlockInit.vacant_effigy.get().defaultBlockState(),3);
        ((CardinalFocusBlockEntity)level.getBlockEntity(center)).insertMedium(null,new ItemStack(ItemInit.sanguine_quintessence.get()));
        var donor=EntityInit.harbinger_alchemist.get().create(level);var data=SuccessionSavedData.get(level);data.bequeath(donor.getUUID(),line.getBloodlineUUID(),"alchemist","Review Teacher");
        var blood=new ItemStack(ItemInit.bloody_vial.get());SuccessionSamples.fill(blood,donor,line.getBloodlineUUID(),"alchemist");
        var own=new ItemStack(ItemInit.bloody_vial.get());SuccessionSamples.fill(own,p,null,"");var offerings=List.of(blood,own,new ItemStack(ItemInit.mnemonic_ambergris.get()));
        for(int i=0;i<3;i++) {var s=floor.brazierSockets().get(i);var pos=center.offset(s.getX(),s.getY(),-s.getZ());level.setBlock(pos,BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.RITUAL_PHASE,1),3);((IronBrazierBlockEntity)level.getBlockEntity(pos)).insertOffering(null,offerings.get(i));}
        var workplaces=List.of(BlockInit.ghastly_alembic.get(),BlockInit.somatic_loom.get(),BlockInit.hematic_armature.get(),BlockInit.mason_effigy.get(),BlockInit.cardinal_focus.get());
        for(int i=0;i<5;i++) {
            var pos=center.offset(-8+i*4,1,7);level.setBlock(pos,workplaces.get(i).defaultBlockState(),3);
            var npc=(ProfessionalHarbingerEntity)BuiltInRegistries.ENTITY_TYPE.get(SuccessionProfessions.entityType(SuccessionProfessions.ROLES.get(i))).create(level);
            SuccessionTestFixtures.resident(p,npc,pos);npc.setPos(pos.getX()+.5,101,pos.getZ()-1);npc.setNoAi(true);level.addFreshEntity(npc);
        }
        level.setBlock(center.offset(8,1,0),BlockInit.vial_centrifuge.get().defaultBlockState(),3);
        Hemomancy.LOGGER.info("SUCCESSION_REVIEW fixture prepared, five assisted residents and one unperformed rite");
    }
}
