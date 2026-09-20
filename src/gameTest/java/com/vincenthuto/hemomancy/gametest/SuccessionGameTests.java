package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.*;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.*;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.succession.*;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.*;
import net.minecraft.network.protocol.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("succession_validation")
@PrefixGameTestTemplate(false)
public final class SuccessionGameTests {
    @GameTest(template = "empty")
    public static void requiredHelpersGatherExactlyAndReturnToTheirWorkplaces(GameTestHelper h) {
        var f = fixture(h);
        var recipeId = Hemomancy.rloc("cardinal_rite/ancestral_communion");
        var recipe = CardinalRiteRecipe.getRiteByLocation(h.getLevel(), recipeId);
        var rite = ActiveCardinalRite.interactive(f.player.getUUID(), f.center, recipeId, 3600, 7, 7, false, 0, 4);
        for (var offset : CardinalRiteAllyService.markers(recipe).values())
            h.getLevel().setBlock(f.center.offset(offset).below(), Blocks.STONE.defaultBlockState(), 3);
        var npcs = new ArrayList<ProfessionalHarbingerEntity>();
        for (int i = 0; i < 4; i++) {
            var work = f.work.offset(0, 0, i * 3);
            h.getLevel().setBlock(work, BlockInit.vial_centrifuge.get().defaultBlockState(), 3);
            h.getLevel().setBlock(work.east().below(), Blocks.STONE.defaultBlockState(), 3);
            var npc = EntityInit.harbinger_alchemist.get().create(h.getLevel());
            npc.setPos(work.getX() + 1.5, work.getY(), work.getZ() + .5);
            h.getLevel().addFreshEntity(npc);
            SuccessionTestFixtures.resident(f.player, npc, work); npcs.add(npc);
        }
        var saved = CardinalRiteSavedData.get(h.getLevel()); saved.startRite(rite);
        CardinalRiteAllyService.maintainNpcStations(h.getLevel(), rite);
        h.assertTrue(rite.getAllyRoles().size() == 3, "Required NPC helpers were not automatically gathered, or too many were summoned");
        h.assertTrue(new HashSet<>(rite.getAllyRoles().values()).size() == 3, "Helpers shared a rite station");
        h.assertTrue(CardinalRiteAllyService.hasRequiredHelpers(h.getLevel(), rite), "Gathered helpers were not available at their stations");
        var loaded = ActiveCardinalRite.deserialize(rite.serialize(h.getLevel().registryAccess()), h.getLevel().registryAccess());
        saved.startRite(loaded);
        CardinalRiteAllyService.returnNpcAlliesToFane(h.getLevel(), loaded); saved.removeRite(f.player.getUUID());
        for (var npc : npcs) {
            var home = BlockPos.of(SuccessionSavedData.get(h.getLevel()).residents.get(npc.getUUID()).workplace);
            h.assertTrue(npc.blockPosition().distSqr(home) <= 4, "Helper did not return to its own POI after rite reload and termination");
            npc.discard();
        }
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty")
    public static void playerTakesHelperSlotAndUnusedNpcReturnsHome(GameTestHelper h) {
        var f = fixture(h); var rite = helperRite(h, f, "covenant_vigil");
        var npc = helperResident(h, f, f.work);
        CardinalRiteAllyService.maintainNpcStations(h.getLevel(), rite);
        h.assertTrue(rite.getAllyRoles().size() == 1, "One-helper rite did not gather exactly one resident");
        var guest = fixture(h); var player = guest.player;
        BloodlineSavedData.get(h.getLevel().getServer().overworld()).disbandBloodline(guest.line.getBloodlineUUID());
        f.line.addMember(player.getUUID());
        // Register the packet-suppressing test connection without running an unrelated client mod handshake.
        var online = onlinePlayers(h); online.put(player.getUUID(), player);
        var recipe = CardinalRiteRecipe.getRiteByLocation(h.getLevel(), rite.getRecipeId());
        var role = rite.getAllyRoles().get(npc.getUUID());
        var station = f.center.offset(CardinalRiteAllyService.markers(recipe).get(role));
        for (int i = 0; i < 4; i++) rite.fillAnchor(i, 1000);
        rite.enterInscription();
        player.setPos(station.getX()+.5, station.getY(), station.getZ()+.5);
        h.assertTrue(CardinalRiteAllyService.tryClaimPlayerRole(h.getLevel(), player, rite, station), "Player could not replace an automatic helper");
        CardinalRiteAllyService.maintainNpcStations(h.getLevel(), rite);
        h.assertTrue(rite.getAllyRoles().size() == 1 && rite.getAllyRoles().containsKey(player.getUUID())
                && npc.blockPosition().distSqr(f.work) <= 4, "Player participation left an unnecessary NPC at the rite");
        h.assertTrue(CardinalRiteAllyService.hasRequiredHelpers(h.getLevel(), rite), "Present player did not count as helper");
        player.setPos(player.getX()+50, player.getY(), player.getZ());
        h.assertTrue(!CardinalRiteAllyService.hasRequiredHelpers(h.getLevel(), rite), "Distant player counted as an available helper");
        CardinalRiteAllyService.maintainNpcStations(h.getLevel(), rite);
        h.assertTrue(rite.getAllyRoles().containsKey(npc.getUUID()), "Departing player was not replaced during preparation");
        CardinalRiteAllyService.returnNpcAlliesToFane(h.getLevel(), rite);
        CardinalRiteSavedData.get(h.getLevel()).removeRite(f.player.getUUID());
        online.remove(player.getUUID()); cleanup(guest); npc.discard(); cleanup(f); h.succeed();
    }

    @GameTest(template = "empty")
    public static void helpersCannotBeBorrowedTwiceAndReturnAfterCasterDeparture(GameTestHelper h) {
        var f = fixture(h); var rite = helperRite(h, f, "covenant_vigil");
        var npc = helperResident(h, f, f.work);
        CardinalRiteAllyService.maintainNpcStations(h.getLevel(), rite);
        var other = fixture(h);
        BloodlineSavedData.get(h.getLevel().getServer().overworld()).disbandBloodline(other.line.getBloodlineUUID());
        f.line.addMember(other.player.getUUID());
        var second = helperRite(h, new Fixture(other.player, f.line, f.center.offset(20,0,0), f.heart, f.work), "covenant_vigil");
        CardinalRiteAllyService.maintainNpcStations(h.getLevel(), second);
        h.assertTrue(second.getAllyRoles().isEmpty(), "Concurrent rite stole an assigned helper");
        f.line.removeMember(f.player.getUUID());
        CardinalRiteAllyService.returnNpcAlliesToFane(h.getLevel(), rite);
        h.assertTrue(npc.blockPosition().distSqr(f.work) <= 4, "Caster departure prevented workplace return");
        CardinalRiteSavedData.get(h.getLevel()).removeRite(f.player.getUUID());
        CardinalRiteSavedData.get(h.getLevel()).removeRite(other.player.getUUID());
        npc.discard(); cleanup(f); cleanup(other); h.succeed();
    }

    @GameTest(template = "empty")
    public static void removedRiteReturnsPersistedHelperAndBloodspentResidentsStayHome(GameTestHelper h) {
        var f = fixture(h); var rite = helperRite(h, f, "covenant_vigil");
        var npc = helperResident(h, f, f.work);
        CardinalRiteAllyService.maintainNpcStations(h.getLevel(), rite);
        var tag = new CompoundTag(); npc.save(tag); var id = npc.getUUID(); npc.discard();
        CardinalRiteSavedData.get(h.getLevel()).removeRite(f.player.getUUID());
        var loaded = EntityInit.harbinger_alchemist.get().create(h.getLevel()); loaded.load(tag); h.getLevel().addFreshEntity(loaded);
        SuccessionResidents.tick(h.getLevel(), loaded);
        h.assertTrue(loaded.getUUID().equals(id) && loaded.blockPosition().distSqr(f.work) <= 4, "Unloaded helper lost its return trip");
        f.line.drawNpcRiteReserve(id, 1000, h.getLevel().getGameTime());
        rite = helperRite(h, f, "covenant_vigil"); CardinalRiteAllyService.maintainNpcStations(h.getLevel(), rite);
        h.assertTrue(rite.getAllyRoles().isEmpty(), "Bloodspent resident was summoned");
        CardinalRiteSavedData.get(h.getLevel()).removeRite(f.player.getUUID());
        loaded.discard(); cleanup(f); h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1000)
    public static void helperTravelsAcrossDimensionsAndReturnsToOriginalWorkstation(GameTestHelper h) {
        var f = fixture(h); var rite = helperRite(h, f, "covenant_vigil");
        var home = h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.NETHER);
        var work = new BlockPos(f.center.getX(), 200, f.center.getZ());
        for (int x=-2; x<=2; x++) for (int z=-2; z<=2; z++) {
            home.setBlock(work.offset(x,-1,z), Blocks.STONE.defaultBlockState(), 3);
            home.setBlock(work.offset(x,0,z), Blocks.AIR.defaultBlockState(), 3);
            home.setBlock(work.offset(x,1,z), Blocks.AIR.defaultBlockState(), 3);
            home.setBlock(work.offset(x,2,z), Blocks.AIR.defaultBlockState(), 3);
        }
        home.setBlock(work, BlockInit.vial_centrifuge.get().defaultBlockState(), 3);
        FoundingFaneSavedData.get(home).consecrateHeart(f.player.getUUID(), work.south(5));
        var npc = EntityInit.harbinger_alchemist.get().create(home);
        npc.setPos(work.getX()+1.5, work.getY(), work.getZ()+.5); home.addFreshEntity(npc);
        var initial = SuccessionTestFixtures.resident(f.player, npc, work);
        var tag = initial.save(); tag.putString("Dimension", home.dimension().location().toString());
        var record = new SuccessorRecord(tag); var data = SuccessionSavedData.get(home);
        data.ledger.unclaim(record.id); h.assertTrue(data.ledger.claim(record.id, record.place()), "Cross-dimension fixture workplace is occupied"); data.residents.put(record.id, record);
        CardinalRiteAllyService.maintainNpcStations(h.getLevel(), rite);
        boolean[] returning = {false};
        h.succeedWhen(() -> {
            if (!returning[0]) {
                rite.setDisconnectTicks(0);
                CardinalRiteAllyService.maintainNpcStations(h.getLevel(), rite);
                h.assertTrue(rite.getAllyRoles().size() == 1 && h.getLevel().getEntity(record.id) != null
                        && home.getEntity(record.id) == null, "Waiting for the existing helper to arrive from its home dimension");
                CardinalRiteAllyService.returnNpcAlliesToFane(h.getLevel(), rite); returning[0] = true;
            }
            var returned = home.getEntity(record.id);
            h.assertTrue(returned != null && returned.blockPosition().distSqr(work) <= 4
                    && h.getLevel().getEntity(record.id) == null, "Cross-dimension return did not restore the original resident to its workstation");
            returned.discard(); CardinalRiteSavedData.get(h.getLevel()).removeRite(f.player.getUUID());
            FoundingFaneSavedData.get(home).remove(f.player.getUUID()); cleanup(f);
        });
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, ServerPlayer> onlinePlayers(GameTestHelper h) {
        try {
            var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");
            field.setAccessible(true);
            return (Map<UUID, ServerPlayer>) field.get(h.getLevel().getServer().getPlayerList());
        } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }

    private static ActiveCardinalRite helperRite(GameTestHelper h, Fixture f, String name) {
        var id = Hemomancy.rloc("cardinal_rite/"+name);
        var recipe = CardinalRiteRecipe.getRiteByLocation(h.getLevel(), id);
        var rite = ActiveCardinalRite.interactive(f.player.getUUID(), f.center, id, 3600, 7, 7, false, 0, 4);
        for (var offset : CardinalRiteAllyService.markers(recipe).values())
            h.getLevel().setBlock(f.center.offset(offset).below(), Blocks.STONE.defaultBlockState(), 3);
        CardinalRiteSavedData.get(h.getLevel()).startRite(rite); return rite;
    }

    private static ProfessionalHarbingerEntity helperResident(GameTestHelper h, Fixture f, BlockPos work) {
        h.getLevel().setBlock(work, BlockInit.vial_centrifuge.get().defaultBlockState(), 3);
        h.getLevel().setBlock(work.east().below(), Blocks.STONE.defaultBlockState(), 3);
        var npc = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        npc.setPos(work.getX()+1.5,work.getY(),work.getZ()+.5); h.getLevel().addFreshEntity(npc);
        SuccessionTestFixtures.resident(f.player,npc,work); return npc;
    }

    @GameTest(template = "empty")
    public static void manufactureConsumesPatternAndTwoHundredFiftyBlood(GameTestHelper h) {
        var f=fixture(h); var center=f.center; var volume=HemoCapabilityAccess.requireBloodVolume(f.player);volume.setBloodVolume(1000);
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++) {
            var block=x==0&&z==0?BlockInit.venous_stone.get():x!=0&&z!=0?BlockInit.conscious_mass.get():BlockInit.hematic_iron_block.get();
            h.getLevel().setBlock(center.offset(x,0,z),block.defaultBlockState(),3);
        }
        var formation=new ItemStack(ItemInit.sanguine_formation.get());
        h.assertTrue(com.vincenthuto.hemomancy.common.event.BloodStructureFeedManager.feedStructure(f.player,h.getLevel(),center,formation,250),"Effigy pattern did not accept blood projection");
        for(int tick=0;tick<30;tick++)com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager.tick();
        h.assertTrue(volume.getBloodVolume()==750 && formation.isEmpty(),"Effigy manufacture charged wrong blood or retained catalyst");
        h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(center).inflate(2),e->e.getItem().is(BlockInit.vacant_effigy.get().asItem())).size()==1,"Manufacture did not produce exactly one portable effigy");
        h.assertTrue(h.getLevel().getBlockState(center).isAir(),"Manufacture retained structure ingredients");cleanup(f);h.succeed();
    }

    @GameTest(template = "empty")
    public static void normalActivationEscrowsAndCancellationReturnsStaff(GameTestHelper h) {
        var f=fixture(h);var prepared=setup(h,f,false,null,false);SuccessionRites.cleanup(h.getLevel(),prepared);
        var staff=new ItemStack(ItemInit.living_staff.get());f.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,staff);
        var result=com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket.tryStartCardinalRite(f.player,f.center,com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
        var saved=CardinalRiteSavedData.get(h.getLevel());var rite=saved.getRite(f.player.getUUID());
        h.assertTrue(result==com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED && rite!=null && rite.hasEscrowedStaff(),"Normal activation failed to create succession or capture staff");
        SuccessionRites.tick(h.getLevel(),f.player,rite,recipe(h,false));
        rite.markCollapsed();SuccessionRites.cleanup(h.getLevel(),rite);CardinalRiteStaffEscrow.restore(f.player,rite);saved.removeRite(f.player.getUUID());
        h.assertTrue(f.player.getInventory().contains(new ItemStack(ItemInit.living_staff.get())) && !rite.hasEscrowedStaff(),"Cancellation lost or duplicated escrowed staff");
        h.assertTrue(!h.getLevel().getBlockState(BlockPos.of(rite.succession().getLong("Body"))).is(BlockInit.vacant_effigy.get()),"Cancellation retained committed body");cleanup(f);h.succeed();
    }
    @GameTest(template = "empty")
    public static void consentChecksDegreeDistanceAndSurvivesOrdinaryExpulsion(GameTestHelper h) {
        var f = fixture(h); var npc = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        npc.setPos(f.player.position()); h.getLevel().addFreshEntity(npc);
        var data = SuccessionSavedData.get(h.getLevel());
        HemoCapabilityAccess.requireInitiatoryDegree(f.player).setDegreeNumber(4);
        SuccessionDialogue.handle(f.player, npc, SuccessionDialogue.BEQUEST);
        h.assertTrue(!data.hasBequest(npc.getUUID(), f.line.getBloodlineUUID(), "alchemist"), "D4 forged consent accepted");
        HemoCapabilityAccess.requireInitiatoryDegree(f.player).setDegreeNumber(5);
        SuccessionDialogue.handle(f.player, npc, SuccessionDialogue.BEQUEST);
        f.line.addNpcMember(npc.getUUID()); f.line.removeNpcMember(npc.getUUID());
        h.assertTrue(data.hasBequest(npc.getUUID(), f.line.getBloodlineUUID(), "alchemist"), "Pool expulsion revoked independent consent");
        f.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ItemInit.bloody_vial.get()));
        SuccessionDialogue.handle(f.player, npc, SuccessionDialogue.DONATE);
        h.assertTrue(!com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData.isFilled(f.player.getMainHandItem()), "Restricted NPC donation bypassed syringe requirement");
        var syringe = new ItemStack(ItemInit.living_syringe.get());
        f.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, syringe);
        f.player.getInventory().setItem(1, new ItemStack(ItemInit.vial_rack.get()));
        SuccessionDialogue.handle(f.player, npc, SuccessionDialogue.DONATE);
        var rack = ItemStack.parseOptional(h.getLevel().registryAccess(), syringe.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getCompound("loaded_rack"));
        var donated = com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem.getVials(rack).getFirst();
        h.assertTrue(npc.isInvulnerable(), "Donation removed original NPC protection");
        h.assertTrue(data.authentic(SuccessionSamples.identity(donated)), "Consenting donor did not issue authentic vial through syringe");
        f.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(ItemInit.bloody_vial.get()));
        npc.setPos(f.player.getX()+30, f.player.getY(), f.player.getZ());
        SuccessionDialogue.handle(f.player, npc, SuccessionDialogue.DONATE);
        h.assertTrue(SuccessionSamples.identity(f.player.getMainHandItem()).isEmpty(), "Remote donation accepted");
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty")
    public static void selfSampleChargesExactlyOneHundredAndPreservesForeignData(GameTestHelper h) {
        var f = fixture(h); var volume = HemoCapabilityAccess.requireBloodVolume(f.player);
        volume.setBloodVolume(500); var stack = new ItemStack(ItemInit.bloody_vial.get(), 64);
        var tag = new CompoundTag(); tag.putString("foreign", "keep");
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        f.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack); f.player.setShiftKeyDown(true);
        stack.getItem().use(h.getLevel(), f.player, net.minecraft.world.InteractionHand.MAIN_HAND);
        h.assertTrue(volume.getBloodVolume() == 400, "Self sampling charged incorrect amount");
        var sample = f.player.getMainHandItem();
        h.assertTrue(sample.getCount() == 1 && SuccessionSamples.identity(sample).getUUID("Donor").equals(f.player.getUUID()), "Self sampling lost individual UUID or overstacked specimen");
        int remaining = f.player.getInventory().items.stream().filter(com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem::isEmptyVial).mapToInt(ItemStack::getCount).sum();
        h.assertTrue(remaining == 63, "Self sampling did not preserve the other empty vials: " + remaining);
        var empty = com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData.emptyVessel(sample);
        h.assertTrue(SuccessionSamples.identity(empty).isEmpty() && empty.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getString("foreign").equals("keep"), "Emptying lost foreign data or retained provenance");
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty")
    public static void changedBloodOfferingCancelsWithoutPartiallyConsumingPair(GameTestHelper h) {
        var f = fixture(h); var rite = setup(h, f, false, null, false);
        for (int i=0;i<200;i++) SuccessionRites.tick(h.getLevel(), f.player, rite, recipe(h,false));
        var entries=rite.succession().getList("Offerings", net.minecraft.nbt.Tag.TAG_COMPOUND);
        var donor=(IronBrazierBlockEntity)h.getLevel().getBlockEntity(BlockPos.of(entries.getCompound(0).getLong("Position")));
        var will=(IronBrazierBlockEntity)h.getLevel().getBlockEntity(BlockPos.of(entries.getCompound(1).getLong("Position")));
        will.extractOffering(); will.insertOffering(null,new ItemStack(ItemInit.bloody_vial.get()));
        h.assertTrue(!SuccessionRites.tick(h.getLevel(),f.player,rite,recipe(h,false)),"Changed vessel was accepted");
        SuccessionRites.cleanup(h.getLevel(),rite);
        h.assertTrue(donor.hasOffering() && ((CardinalFocusBlockEntity)h.getLevel().getBlockEntity(f.center)).hasMedium(),"Cancellation partially consumed blood or life");
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty")
    public static void activeRiteKeepsCapturedOwnershipAfterDepartureAndDissolution(GameTestHelper h) {
        var f=fixture(h);var rite=setup(h,f,false,null,false);
        BloodlineSavedData.get(h.getLevel().getServer().overworld()).disbandBloodline(f.line.getBloodlineUUID());
        run(h,f,rite);var data=SuccessionSavedData.get(h.getLevel());var r=data.residents.get(rite.succession().getUUID("Identity"));
        h.assertTrue(r != null && r.dormant && r.bloodline.equals(f.line.getBloodlineUUID()),"Dissolution lost captured ownership or deleted successor");
        h.assertTrue(!data.ledger.owns(r.id,r.place()),"Dormant successor retained workplace");cleanup(f);h.succeed();
    }

    @GameTest(template = "empty")
    public static void duplicateCreationCannotReserveOccupiedWorkplace(GameTestHelper h) {
        var f=fixture(h);var rite=setup(h,f,false,null,false);
        var next=active(f,false);var match=CardinalRiteStationMatcher.find(h.getLevel(),f.center,recipe(h,false)).orElseThrow();
        h.assertTrue(!SuccessionRites.prepare(h.getLevel(),f.player,next,match),"Concurrent rite shared a workplace");
        SuccessionRites.cleanup(h.getLevel(),rite);cleanup(f);h.succeed();
    }

    @GameTest(template = "empty")
    public static void allFiveMalformedAttacksHaveProfessionSpecificConsequences(GameTestHelper h) {
        var f=fixture(h);
        for(String role:SuccessionProfessions.ROLES) {
            var npc=(ProfessionalHarbingerEntity)net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(SuccessionProfessions.entityType(role)).create(h.getLevel());
            npc.initializeMisbegotten(f.player.getUUID(),42,"Teacher");npc.setPos(f.player.position());
            var target=net.minecraft.world.entity.EntityType.IRON_GOLEM.create(h.getLevel());target.setPos(f.player.position());
            float before=target.getHealth();MisbegottenBehavior.attack(h.getLevel(),npc,target);
            boolean result=switch(role) {
                case "alchemist" -> target.hasEffect(net.minecraft.world.effect.MobEffects.POISON);
                case "artificer" -> target.getHealth()<before;
                case "mnemonist" -> target.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS);
                case "cicatrix_anchorite" -> target.hasEffect(EffectInit.counterfeit_scar);
                default -> target.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
            };
            h.assertTrue(result,"Missing malformed attack: "+role);
        }
        cleanup(f);h.succeed();
    }
    @GameTest(template = "empty")
    public static void formingEffigyCannotBeMinedForARefund(GameTestHelper h) {
        var f = fixture(h); var rite = setup(h, f, false, null, false);
        SuccessionRites.tick(h.getLevel(), f.player, rite, recipe(h, false));
        var body = BlockPos.of(rite.succession().getLong("Body"));
        var drops = net.minecraft.world.level.block.Block.getDrops(h.getLevel().getBlockState(body), h.getLevel(), body, null);
        h.assertTrue(drops.stream().noneMatch(s -> s.is(BlockInit.vacant_effigy.get().asItem())), "Committed body refunded its effigy when broken");
        SuccessionRites.cleanup(h.getLevel(), rite); cleanup(f); h.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void creationDeathAndRestorationKeepIdentityAndSpentReserve(GameTestHelper h) {
        var f = fixture(h); var data = SuccessionSavedData.get(h.getLevel());
        var first = setup(h, f, false, null, false);
        run(h, f, first);
        UUID id = first.succession().getUUID("Identity");
        var original = data.residents.get(id);
        h.assertTrue(original != null && data.ledger.life(id).alive(), "Creation did not commit identity");
        var entity = (ProfessionalHarbingerEntity) h.getLevel().getEntity(id);
        h.assertTrue(entity != null && entity.isSuccessor() && !entity.isInvulnerable(), "Successor is missing or immortal");
        original.encounters.putInt(f.player.getUUID().toString(), 7);
        f.line.drawNpcRiteReserve(id, 600, h.getLevel().getGameTime());
        SuccessionResidents.died(h.getLevel(), entity); entity.discard();
        h.assertTrue(!f.line.hasNpcMember(id), "Death retained pool membership");
        var restored = setup(h, f, true, BoundMnemonicRemnantItem.create(original, data.ledger.life(id).generation()), false);
        run(h, f, restored);
        var person = data.residents.get(id);
        h.assertTrue(data.ledger.life(id).alive() && h.getLevel().getEntity(id) != null, "Restoration changed or lost UUID");
        h.assertTrue(person.name.equals(original.name) && person.seed == original.seed
                && person.encounters.getInt(f.player.getUUID().toString()) == 7, "Restoration lost personal history");
        h.assertTrue(f.line.getNpcRiteReserve(id, h.getLevel().getGameTime()) == 400, "Restoration refilled spent reserve");
        h.assertTrue(f.line.getNpcMemberCount() == 1, "Restoration duplicated pool membership");
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void wrongOfficiantCreatesHostileAndDoesNotRefundIngredients(GameTestHelper h) {
        var f = fixture(h); var rite = setup(h, f, false, null, true); run(h, f, rite);
        var data = SuccessionSavedData.get(h.getLevel());
        h.assertTrue(!data.residents.containsKey(rite.succession().getUUID("Identity")), "Fraud created legitimate resident");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ProfessionalHarbingerEntity.class,
                new net.minecraft.world.phys.AABB(f.center).inflate(8), ProfessionalHarbingerEntity::isMisbegotten).size() == 1,
                "Rejection did not create exactly one hostile");
        h.assertTrue(!((CardinalFocusBlockEntity) h.getLevel().getBlockEntity(f.center)).hasMedium(), "Rejection refunded life");
        h.assertTrue(data.ledger.reservations().values().stream().noneMatch(r -> r.workplace().position() == f.work.asLong()), "Rejected rite leaked workplace");
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void failedRestorationPreservesIdentityAndAllowsRetry(GameTestHelper h) {
        var f = fixture(h); var creation = setup(h, f, false, null, false); run(h, f, creation);
        UUID id = creation.succession().getUUID("Identity"); var data = SuccessionSavedData.get(h.getLevel());
        var entity = (ProfessionalHarbingerEntity) h.getLevel().getEntity(id);
        SuccessionResidents.died(h.getLevel(), entity); entity.discard();
        var relic = BoundMnemonicRemnantItem.create(data.residents.get(id), data.ledger.life(id).generation());
        var failure = setup(h, f, true, relic, true); run(h, f, failure);
        h.assertTrue(!data.ledger.life(id).alive() && !data.ledger.locked(id), "Failed restoration erased or locked the dead identity");
        var retry = setup(h, f, true, BoundMnemonicRemnantItem.create(data.residents.get(id), data.ledger.life(id).generation()), false);
        run(h, f, retry); h.assertTrue(data.ledger.life(id).alive(), "Failed restoration prevented a legitimate retry");
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty")
    public static void legacyVialsAreRejectedBeforeConsumption(GameTestHelper h) {
        var f = fixture(h); var rite = setup(h, f, false, null, false);
        SuccessionRites.cleanup(h.getLevel(), rite);
        var source = (IronBrazierBlockEntity) h.getLevel().getBlockEntity(f.center.offset(5,1,0));
        var sample = source.extractOffering();
        var tag = sample.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        tag.remove(SuccessionSamples.KEY); sample.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
        source.insertOffering(null, sample);
        var next = active(f, false); var match = CardinalRiteStationMatcher.find(h.getLevel(), f.center, recipe(h, false)).orElseThrow();
        h.assertTrue(!SuccessionRites.prepare(h.getLevel(), f.player, next, match), "Legacy sample entered costly rite");
        h.assertTrue(source.hasOffering() && ((CardinalFocusBlockEntity) h.getLevel().getBlockEntity(f.center)).hasMedium(), "Early refusal consumed offerings");
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void reissueInvalidatesCopiesAndRequiresNearbyBloodwell(GameTestHelper h) {
        var f = fixture(h); var rite = setup(h, f, false, null, false); run(h, f, rite);
        var data = SuccessionSavedData.get(h.getLevel()); UUID id = rite.succession().getUUID("Identity");
        var entity = (ProfessionalHarbingerEntity) h.getLevel().getEntity(id);
        SuccessionResidents.died(h.getLevel(), entity); entity.discard(); int old = data.ledger.life(id).generation();
        f.player.setPos(f.center.getX()+100, f.center.getY(), f.center.getZ());
        h.assertTrue(!ResidentsRequestPacket.process(f.player, id, false), "Remote player reissued a remnant");
        f.player.setPos(f.heart.getX()+.5, f.heart.getY()+1, f.heart.getZ()+.5);
        h.assertTrue(ResidentsRequestPacket.process(f.player, id, false), "Bloodwell recovery failed");
        h.assertTrue(data.ledger.life(id).generation() == old+1, "Reissue did not invalidate old copies");
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void restartAtMemoryDoesNotConsumeBloodTwice(GameTestHelper h) {
        var f = fixture(h); var rite = setup(h, f, false, null, false);
        for (int i = 0; i < 650; i++) h.assertTrue(SuccessionRites.tick(h.getLevel(), f.player, rite, recipe(h, false)), "Rite stopped before save");
        var loaded = ActiveCardinalRite.deserialize(rite.serialize(h.getLevel().registryAccess()), h.getLevel().registryAccess());
        h.assertTrue(loaded.succession().getBoolean("BloodConsumed") && loaded.succession().getBoolean("MemoryConsumed"), "Restart lost consumed stages");
        run(h, f, loaded); h.assertTrue(SuccessionSavedData.get(h.getLevel()).ledger.life(loaded.succession().getUUID("Identity")).alive(), "Resumed rite failed");
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void displacedSuccessorKeepsIdentityAndFindsReplacement(GameTestHelper h) {
        var f = fixture(h); var rite = setup(h, f, false, null, false); run(h, f, rite);
        UUID id = rite.succession().getUUID("Identity"); var data = SuccessionSavedData.get(h.getLevel());
        var entity = (ProfessionalHarbingerEntity) h.getLevel().getEntity(id);
        h.getLevel().removeBlock(f.work, false); SuccessionResidents.tick(h.getLevel(), entity);
        h.assertTrue(data.residents.get(id).displaced && !SuccessionResidents.mayServe(f.player, entity), "Destroyed workplace did not pause services");
        h.getLevel().setBlock(f.work, BlockInit.vial_centrifuge.get().defaultBlockState(), 3); SuccessionResidents.tick(h.getLevel(), entity);
        h.assertTrue(!data.residents.get(id).displaced && entity.getUUID().equals(id), "Replacement duplicated or lost resident");
        cleanup(f); h.succeed();
    }

    @GameTest(template = "empty")
    public static void fiveProfessionsRetainServicesAndOriginalsCannotAssist(GameTestHelper h) {
        var f = fixture(h);
        for (String role : SuccessionProfessions.ROLES) {
            var npc = (ProfessionalHarbingerEntity) net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(SuccessionProfessions.entityType(role)).create(h.getLevel());
            h.assertTrue(npc instanceof com.vincenthuto.hemomancy.common.entity.npc.dialogue.ProgressionDialogueNpc, "Profession lost dialogue");
            var tree = ((com.vincenthuto.hemomancy.common.entity.npc.dialogue.ProgressionDialogueNpc) npc).progressionDialogue(f.player);
            h.assertTrue(tree != null && !tree.nodes().isEmpty(), "Missing profession service tree");
            h.assertTrue(!SuccessionResidents.helper(npc), "Original teacher became physical rite helper");
            var record=SuccessionTestFixtures.resident(f.player,npc,f.work.offset(0,0,SuccessionProfessions.ROLES.indexOf(role)));
            var successorTree=SuccessionDialogue.decorate(f.player,npc,tree);
            h.assertTrue(successorTree.speakerName().equals(record.name) && SuccessionResidents.mayServe(f.player,npc),"Successor lost personal identity or service access: "+role);
            var reply=com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree.builder("Teacher",Hemomancy.rloc("textures/item/mnemonic_ambergris.png"),npc.getId())
                    .addNode(new com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode("result",List.of("Service result"),List.of())).build();
            h.assertTrue(SuccessionDialogue.decorate(f.player,npc,reply).getStartNode().lines().equals(List.of("Service result")),"Personal greeting erased service feedback");
            h.assertTrue(successorTree.nodes().values().stream().flatMap(n->n.options().stream()).noneMatch(o->
                    "recruit_harbinger".equals(o.eventId()) || SuccessionDialogue.BEQUEST.equals(o.eventId()) || SuccessionDialogue.DONATE.equals(o.eventId())),"Successor exposes original-only actions");
            SuccessionResidents.died(h.getLevel(),npc);
            h.assertTrue(!SuccessionResidents.mayServe(f.player,npc) && !SuccessionResidents.helper(npc),"Stale entity retained services after authoritative death");
        }
        cleanup(f); h.succeed();
    }

    private record Fixture(ServerPlayer player, Bloodline line, BlockPos center, BlockPos heart, BlockPos work) {}
    private static Fixture fixture(GameTestHelper h) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "succession-test"), false);
        var p = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND); new io.netty.channel.embedded.EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, p, cookie) { @Override public void send(Packet<?> packet) {} };
        var line = new Bloodline("Test", p.getUUID(), UUID.randomUUID(), new ArrayList<>(List.of(p.getUUID())));
        BloodlineSavedData.get(h.getLevel().getServer().overworld()).registerBloodline(line);
        HemoCapabilityAccess.requireBloodVolume(p).setBloodLine(line); HemoCapabilityAccess.requireBloodVolume(p).setActive(true);
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        BlockPos center = h.absolutePos(new BlockPos(10,4,10)), heart = center.offset(0,0,10), work = center.offset(9,0,0);
        p.setPos(center.getX()+.5, center.getY()+1, center.getZ()+.5);
        h.getLevel().setBlock(heart, BlockInit.consecrated_bloodwell.get().defaultBlockState(), 3);
        FoundingFaneSavedData.get(h.getLevel()).consecrateHeart(p.getUUID(), heart);
        h.getLevel().setBlock(work, BlockInit.vial_centrifuge.get().defaultBlockState(), 3);
        return new Fixture(p,line,center,heart,work);
    }
    private static CardinalRiteRecipe recipe(GameTestHelper h, boolean restore) {
        return CardinalRiteRecipe.getRiteByLocation(h.getLevel(), Hemomancy.rloc("cardinal_rite/"+(restore ? "mnemonic_restoration" : "hematic_succession")));
    }
    private static ActiveCardinalRite active(Fixture f, boolean restore) {
        return ActiveCardinalRite.interactive(f.player.getUUID(),f.center,Hemomancy.rloc("cardinal_rite/"+(restore ? "mnemonic_restoration" : "hematic_succession")),1200,7,5,false,0,4);
    }
    private static ActiveCardinalRite setup(GameTestHelper h, Fixture f, boolean restore, ItemStack remnant, boolean wrong) {
        var floor = CardinalRiteFloorRegistry.get(Hemomancy.rloc("dominion_greater")).orElseThrow();
        var pattern = floor.pattern().getPatternArray();
        for (int z=0;z<7;z++) for (int x=0;x<7;x++) h.getLevel().setBlock(f.center.offset(x-3,0,3-z),floor.pattern().getSymbolList().get(String.valueOf(pattern[z][0].charAt(x))).defaultBlockState(),3);
        h.getLevel().setBlock(f.center.offset(2,1,0),BlockInit.venous_stone.get().defaultBlockState(),3);
        h.getLevel().setBlock(f.center.offset(2,2,0),BlockInit.vacant_effigy.get().defaultBlockState(),3);
        ((CardinalFocusBlockEntity) h.getLevel().getBlockEntity(f.center)).insertMedium(null,new ItemStack(ItemInit.sanguine_quintessence.get()));
        var donor = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        var data = SuccessionSavedData.get(h.getLevel()); data.bequeath(donor.getUUID(),f.line.getBloodlineUUID(),"alchemist","Teacher");
        var donated = new ItemStack(ItemInit.bloody_vial.get()); SuccessionSamples.fill(donated,donor,f.line.getBloodlineUUID(),"alchemist");
        var own = new ItemStack(ItemInit.bloody_vial.get()); SuccessionSamples.fill(own,f.player,null,"");
        if (wrong) {
            var tag = own.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            tag.getCompound(SuccessionSamples.KEY).putUUID("Donor",UUID.randomUUID()); own.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));
        }
        var stacks = restore ? List.of(remnant,own) : List.of(donated,own,new ItemStack(ItemInit.mnemonic_ambergris.get()));
        for (int i=0;i<stacks.size();i++) {
            var socket=floor.brazierSockets().get(i); var pos=f.center.offset(socket.getX(),socket.getY(),-socket.getZ());
            h.getLevel().setBlock(pos,BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.RITUAL_PHASE,1),3);
            var brazier=(IronBrazierBlockEntity)h.getLevel().getBlockEntity(pos); brazier.extractOffering(); brazier.insertOffering(null,stacks.get(i));
        }
        var recipe=recipe(h,restore); h.assertTrue(recipe!=null,"Succession recipe failed to load");
        var match=CardinalRiteStationMatcher.find(h.getLevel(),f.center,recipe).orElse(null);h.assertTrue(match!=null,"Succession station did not match");
        var rite=active(f,restore);rite.setMatchedFloor(match.floor().id(),match.floorMatch().getForwards(),match.floorMatch().getUp());
        h.assertTrue(SuccessionRites.prepare(h.getLevel(),f.player,rite,match),"Eligible succession failed preparation");return rite;
    }
    private static void run(GameTestHelper h,Fixture f,ActiveCardinalRite rite) {
        boolean active=true;for(int i=0;i<1300&&active;i++)active=SuccessionRites.tick(h.getLevel(),f.player,rite,recipe(h,rite.succession().getBoolean("Restoring")));
        h.assertTrue(rite.succession().getBoolean("Resolved"),"Rite stopped before resolution at "+rite.succession().getInt("Ticks"));SuccessionRites.cleanup(h.getLevel(),rite);
    }
    private static void cleanup(Fixture f) { f.player.discard(); }
}
