package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.antecedent.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ClairaudiographBlockEntity;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.neoforged.neoforge.gametest.*;
import java.util.UUID;
import static com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch.Evidence.*;

@GameTestHolder("antecedent_validation")
@PrefixGameTestTemplate(false)
public final class AntecedentGameTests {
    @GameTest(template="empty") public static void livingSyringeCollectsBothProvenancesAndPreservesCatalyst(GameTestHelper h) {
        var p=player(h);var syringe=new ItemStack(ItemInit.living_syringe.get());p.setItemInHand(InteractionHand.MAIN_HAND,syringe);
        p.getInventory().add(new ItemStack(ItemInit.vial_rack.get()));
        var warden=net.minecraft.world.entity.EntityType.WARDEN.create(h.getLevel());warden.setPos(p.position());
        h.assertTrue(syringe.getItem().interactLivingEntity(syringe,p,warden,InteractionHand.MAIN_HAND).consumesAction(),"Syringe rejected Warden");
        var catalyst=h.absolutePos(new BlockPos(1,2,1));h.getLevel().setBlock(catalyst,Blocks.SCULK_CATALYST.defaultBlockState(),3);
        var hit=new net.minecraft.world.phys.BlockHitResult(catalyst.getCenter(),net.minecraft.core.Direction.UP,catalyst,false);
        h.assertTrue(syringe.useOn(new net.minecraft.world.item.context.UseOnContext(p,InteractionHand.MAIN_HAND,hit)).consumesAction(),"Syringe rejected Catalyst");
        var rack=ItemStack.parseOptional(h.getLevel().registryAccess(),syringe.get(DataComponents.CUSTOM_DATA).copyTag().getCompound("loaded_rack"));
        var vials=com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem.getVials(rack);
        h.assertTrue(AhaematicSample.get(vials.get(0)).kind().equals("entity") && AhaematicSample.get(vials.get(1)).kind().equals("block"),"Rack lost specimen provenance");
        h.assertTrue(vials.get(0).is(ItemInit.ahaematic_colloid.get()) && vials.get(1).is(ItemInit.ahaematic_colloid.get()),"Ahaematic samples remained bloody vials");
        h.assertTrue(h.getLevel().getBlockState(catalyst).is(Blocks.SCULK_CATALYST),"Sampling consumed the Catalyst");
        h.assertTrue(!HemoCapabilityAccess.clinicalBlood(p).collected,"Nonblood sampling advanced clinical lesson");
        p.setItemInHand(InteractionHand.MAIN_HAND,vials.get(0));h.assertTrue(!vials.get(0).use(h.getLevel(),p,InteractionHand.MAIN_HAND).getResult().consumesAction(),"Warden specimen became injectable");h.succeed();
    }
    @GameTest(template="empty") public static void vicarRecognitionAndHousingValidateDegreeEvidenceAndPayment(GameTestHelper h) {
        var p=player(h);var vicar=EntityInit.harbinger_vicar.get().create(h.getLevel());vicar.setNoAi(true);vicar.setPos(p.position());h.getLevel().addFreshEntity(vicar);
        var event=new com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent(p,"antecedent_recognition",vicar.getId());
        h.assertTrue(!AntecedentDialogue.handle(event),"Vicar recognized unobserved evidence");
        var proof=HemoCapabilityAccess.antecedent(p);proof.record(CONTROLLED_REPLAY);HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(3);
        h.assertTrue(!AntecedentDialogue.handle(event),"Recognition bypassed Degree 4");HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(4);
        h.assertTrue(AntecedentDialogue.handle(event),"Vicar did not accept controlled replay");proof.record(ARCHIVE_RESPONSE);
        var exchange=new com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent(p,"antecedent_housing",vicar.getId());
        p.getInventory().add(new ItemStack(Items.IRON_NUGGET,4));
        h.assertTrue(!AntecedentDialogue.handle(exchange) && p.getInventory().countItem(Items.IRON_NUGGET)==4,"Incomplete payment was consumed");
        p.getInventory().add(new ItemStack(Items.ECHO_SHARD));h.assertTrue(AntecedentDialogue.handle(exchange),"Valid housing exchange failed");
        h.assertTrue(p.getInventory().countItem(ItemInit.listening_scar.get())==1 && p.getInventory().countItem(Items.IRON_NUGGET)==0 && p.getInventory().countItem(Items.ECHO_SHARD)==0,"Exchange did not conserve inputs/output");
        vicar.discard();h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=1520) public static void completeReplayCreditsOnlyContinuousPersonalWitnesses(GameTestHelper h) {
        var level=h.getLevel();var p=player(h);var absent=player(h);
        level.addNewPlayer(p);level.addNewPlayer(absent);
        h.setBlock(new BlockPos(0,2,0),BlockInit.clairaudiograph.get());
        h.setBlock(new BlockPos(3,2,0),BlockInit.antecedent_vessel.get());
        var machine=(ClairaudiographBlockEntity)level.getBlockEntity(h.absolutePos(new BlockPos(0,2,0)));
        var vial=sample("block","minecraft:sculk_catalyst");BloodSampleData.identify(vial);
        machine.inventory.setStackInSlot(0,vial);machine.inventory.setStackInSlot(1,AncientRecordings.cylinder(AncientRecordings.SEVERED));
        var alchemist=EntityInit.harbinger_alchemist.get().create(level);alchemist.setNoAi(true);alchemist.setNoGravity(true);alchemist.setPos(p.position());level.addFreshEntity(alchemist);
        HemoCapabilityAccess.antecedent(p).record(REPLAY_REQUESTED);
        var housing=new ItemStack(ItemInit.listening_scar.get());housing.set(DataComponentInit.LISTENING_SCAR_STATE.get(),1);p.setItemInHand(InteractionHand.MAIN_HAND,housing);
        machine.startPlayback(false);
        h.runAtTickTime(1200,()->absent.setPos(absent.position().add(30,0,0)));
        h.runAtTickTime(1500,()->{
            var proof=HemoCapabilityAccess.antecedent(p);
            h.assertTrue(proof.has(SEVERED_RECORD_HEARD) && proof.has(SAMPLE_RESPONSE) && proof.has(CONTROLLED_REPLAY) && proof.has(ARCHIVE_RESPONSE),"Continuous listener missed evidence: "+proof.serializeNBT(level.registryAccess()));
            h.assertTrue(!HemoCapabilityAccess.antecedent(absent).has(SAMPLE_RESPONSE),"Interrupted listener got another player's proof");
            h.assertTrue(ListeningScarItem.awake(p.getMainHandItem()),"Continuous signal did not awaken housing");
            h.assertTrue(!proof.complete(),"Out-of-order discovery bypassed Vicar interpretation");proof.record(VICAR_RECOGNITION);h.assertTrue(proof.complete(),"Earlier archive evidence was lost");
            h.assertTrue(!machine.playing() && BloodSampleData.identified(machine.inventory.getStackInSlot(0)),"Completed programme altered specimen or kept running");
            p.discard();absent.discard();alchemist.discard();h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=400) public static void twentyGeneratedCitiesKeepVanillaLayoutAndFitOptionalVigil(GameTestHelper h) {
        var level=h.getLevel();var source=level.getChunkSource();var generator=source.getGenerator();
        var structure=level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).get(net.minecraft.resources.ResourceLocation.withDefaultNamespace("ancient_city"));
        var rotations=new java.util.HashSet<Rotation>();var variants=new java.util.HashSet<String>();
        var vanilla=vanillaCity(structure);
        int fitted=0;boolean awayFromPortal=false;
        for(int index=0;index<20;index++) {
            var start=structure.generate(level.registryAccess(),generator,generator.getBiomeSource(),source.randomState(),level.getStructureManager(),index*7919L,new net.minecraft.world.level.ChunkPos(index*17,index*31),0,level,biome->true);
            h.assertTrue(start.isValid(),"City did not generate for seed "+index);
            var baseline=vanilla.generate(level.registryAccess(),generator,generator.getBiomeSource(),source.randomState(),level.getStructureManager(),index*7919L,new net.minecraft.world.level.ChunkPos(index*17,index*31),0,level,biome->true);
            var pieces=start.getPieces();
            var center=(net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece)pieces.getFirst();
            rotations.add(center.getRotation());variants.add(((com.vincenthuto.hemomancy.mixin.core.SinglePoolElementAccessor)center.getElement()).hemomancy$template().left().orElseThrow().toString());
            var vigils=pieces.stream().filter(piece->piece instanceof net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece pool && pool.getElement() instanceof net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement single && ((com.vincenthuto.hemomancy.mixin.core.SinglePoolElementAccessor)single).hemomancy$template().left().filter(id->id.getNamespace().equals("hemomancy") && id.getPath().startsWith("antecedent/vigil/")).isPresent()).toList();
            h.assertTrue(vigils.isEmpty() || vigils.size()==4,"Partial Vigil annex in city "+index);
            var originalPieces=pieces.stream().filter(piece->!vigils.contains(piece)).toList();
            h.assertTrue(originalPieces.size()==baseline.getPieces().size(),"Vigil removed vanilla city pieces for seed "+index);
            var serialization=net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext.fromLevel(level);
            for(int n=0;n<originalPieces.size();n++)
                h.assertTrue(originalPieces.get(n).createTag(serialization).equals(baseline.getPieces().get(n).createTag(serialization)),
                        "Vigil changed vanilla piece "+n+" for seed "+index);
            if(!vigils.isEmpty()) {
                fitted++;
                assertAnnexMatchesComposite(h,vigils);
                var entry=vigils.getFirst().getBoundingBox().getCenter();
                var portal=center.getBoundingBox().getCenter();
                long dx=(long)entry.getX()-portal.getX(),dz=(long)entry.getZ()-portal.getZ();
                awayFromPortal|=dx*dx+dz*dz>64*64;
            }
            for(var vigil:vigils) {
                assertVigilPreservesTerrain(h,start,vigil);
                h.assertTrue(vigil.getBoundingBox().minY()>=level.getMinBuildHeight()+5,"Vigil intersects bedrock");
                assertNoAuthoredBlockOverlap(h,originalPieces,(net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece)vigil);
            }
            h.assertTrue(VigilPlacement.find(level.getStructureManager(),
                    net.minecraft.world.level.LevelHeightAccessor.create(-64,5),originalPieces).isEmpty(),
                    "An impossible height range must skip the entire site");
        }
        Hemomancy.LOGGER.info("VIGIL_PLACEMENT fitted {} of 20 unchanged vanilla cities",fitted);
        h.assertTrue(fitted>3,"Modular placement did not improve on the compact baseline of 3/20 cities");
        h.assertTrue(awayFromPortal,"All modular sites remain trapped within the former portal radius");
        h.assertTrue(rotations.size()==4 && variants.size()==3,"Seed sweep did not cover every center variant and rotation");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=400)
    public static void normalWorldSeedHasNaturallyPlacedVigil(GameTestHelper h) {
        var level=h.getLevel();
        var registries=level.registryAccess();
        long seed=42L;
        var generator=(net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator)
                net.minecraft.world.level.levelgen.presets.WorldPresets.getNormalOverworld(registries).generator();
        var random=net.minecraft.world.level.levelgen.RandomState.create(generator.generatorSettings().value(),
                registries.lookupOrThrow(net.minecraft.core.registries.Registries.NOISE),seed);
        var set=registries.registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET)
                .get(net.minecraft.resources.ResourceLocation.withDefaultNamespace("ancient_cities"));
        var placement=(net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement)set.placement();
        var structure=set.structures().getFirst().structure().value();
        var height=net.minecraft.world.level.LevelHeightAccessor.create(-64,384);
        int cities=0;
        for(int radius=0;radius<=32;radius++) for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
            if(Math.max(Math.abs(x),Math.abs(z))!=radius)continue;
            var chunk=placement.getPotentialStructureChunk(seed,x*placement.spacing(),z*placement.spacing());
            var start=structure.generate(registries,generator,generator.getBiomeSource(),random,
                    level.getStructureManager(),seed,chunk,0,height,structure.biomes()::contains);
            if(!start.isValid())continue;
            cities++;
            for(var piece:start.getPieces()) {
                if(!(piece instanceof net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece pool)
                        || !(pool.getElement() instanceof net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement single))continue;
                var id=((com.vincenthuto.hemomancy.mixin.core.SinglePoolElementAccessor)single).hemomancy$template().left().orElse(null);
                if(id==null || !id.getNamespace().equals("hemomancy") || !id.getPath().startsWith("antecedent/vigil/modular/") || !id.getPath().endsWith("/entrance"))continue;
                var layout=VigilModules.ALL.stream().filter(l->("antecedent/"+l.parts().getFirst().name()).equals(id.getPath())).findFirst().orElseThrow();
                var origin=pool.getPosition().subtract(layout.parts().getFirst().offset().rotate(pool.getRotation()));
                var doorway=origin.offset(layout.entry().above().rotate(pool.getRotation()));
                Hemomancy.LOGGER.info("NATURAL_VIGIL seed={} cityChunk={} portal={} entrance={} rotation={} validCitiesSearched={}",
                        seed,chunk,start.getPieces().getFirst().getBoundingBox().getCenter(),doorway,pool.getRotation(),cities);
                h.succeed();return;
            }
        }
        h.fail("No naturally placed Vigil found for seed "+seed+" among "+cities+" valid cities");
    }

    private record TemplateBlock(net.minecraft.world.level.block.state.BlockState state, CompoundTag data) {}
    private static void assertAnnexMatchesComposite(GameTestHelper h,
            java.util.List<net.minecraft.world.level.levelgen.structure.StructurePiece> annex) {
        var entrance=(net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece)annex.getFirst();
        var rotation=entrance.getRotation();
        var entranceId=((com.vincenthuto.hemomancy.mixin.core.SinglePoolElementAccessor)entrance.getElement()).hemomancy$template().left().orElseThrow();
        var layout=VigilModules.ALL.stream().filter(l->("antecedent/"+l.parts().getFirst().name()).equals(entranceId.getPath())).findFirst().orElseThrow();
        var origin=entrance.getPosition().subtract(layout.parts().getFirst().offset().rotate(rotation));
        var actual=new java.util.HashMap<BlockPos,TemplateBlock>();
        for(var piece:annex) {
            var pool=(net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece)piece;
            var id=((com.vincenthuto.hemomancy.mixin.core.SinglePoolElementAccessor)pool.getElement()).hemomancy$template().left().orElseThrow();
            for(var entry:templateBlocks(h,id,pool.getPosition(),pool.getRotation()).entrySet())
                h.assertTrue(actual.put(entry.getKey(),entry.getValue())==null,"Natural annex pieces overwrite one another");
        }
        h.assertTrue(actual.equals(templateBlocks(h,Hemomancy.rloc("antecedent/"+layout.composite()),origin,rotation)),
                "Natural annex offsets differ from the navigable composite template");
    }
    private static java.util.Map<BlockPos,TemplateBlock> templateBlocks(GameTestHelper h,
            net.minecraft.resources.ResourceLocation id,BlockPos origin,Rotation rotation) {
        var template=h.getLevel().getStructureManager().getOrCreate(id);
        var blocks=new java.util.HashMap<BlockPos,TemplateBlock>();
        for(var block:((com.vincenthuto.hemomancy.mixin.core.VigilTemplateAccessor)template).hemomancy$palettes().getFirst().blocks())
            blocks.put(origin.offset(block.pos().rotate(rotation)),new TemplateBlock(block.state().rotate(rotation),block.nbt()));
        return blocks;
    }

    private static void assertNoAuthoredBlockOverlap(GameTestHelper h,
            java.util.List<net.minecraft.world.level.levelgen.structure.StructurePiece> city,
            net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece annex) {
        var templates=h.getLevel().getStructureManager();
        var occupied=new java.util.HashSet<BlockPos>();
        for(var piece:city) {
            if(!piece.getBoundingBox().intersects(annex.getBoundingBox().inflatedBy(3)))continue;
            var pool=(net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece)piece;
            if(!(pool.getElement() instanceof net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement)) {
                h.assertTrue(!piece.getBoundingBox().intersects(annex.getBoundingBox()),"Annex overlaps a non-template city piece");
                continue;
            }
            var id=((com.vincenthuto.hemomancy.mixin.core.SinglePoolElementAccessor)pool.getElement()).hemomancy$template().left().orElseThrow();
            for(var palette:((com.vincenthuto.hemomancy.mixin.core.VigilTemplateAccessor)templates.getOrCreate(id)).hemomancy$palettes())
                for(var block:palette.blocks()) if(!block.state().is(Blocks.STRUCTURE_VOID))
                    occupied.add(pool.getPosition().offset(block.pos().rotate(pool.getRotation())));
        }
        var id=((com.vincenthuto.hemomancy.mixin.core.SinglePoolElementAccessor)annex.getElement()).hemomancy$template().left().orElseThrow();
        for(var palette:((com.vincenthuto.hemomancy.mixin.core.VigilTemplateAccessor)templates.getOrCreate(id)).hemomancy$palettes())
            for(var block:palette.blocks()) h.assertTrue(!occupied.contains(annex.getPosition().offset(block.pos().rotate(annex.getRotation()))),
                    "Annex overwrites an authored vanilla block, including portal and walls");
    }

    private static net.minecraft.world.level.levelgen.structure.structures.JigsawStructure vanillaCity(
            net.minecraft.world.level.levelgen.structure.Structure source) {
        // A direct holder has the identical pool contents but no registry key, so the
        // Ancient City append hook does not apply. No JigsawPlacement reservation remains.
        net.minecraft.core.Holder<net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool> pool=jigsawField(source,"startPool");
        return new net.minecraft.world.level.levelgen.structure.structures.JigsawStructure(
                new net.minecraft.world.level.levelgen.structure.Structure.StructureSettings(source.biomes(),source.spawnOverrides(),source.step(),source.terrainAdaptation()),
                net.minecraft.core.Holder.direct(pool.value()),jigsawField(source,"startJigsawName"),jigsawField(source,"maxDepth"),
                jigsawField(source,"startHeight"),jigsawField(source,"useExpansionHack"),jigsawField(source,"projectStartToHeightmap"),
                jigsawField(source,"maxDistanceFromCenter"),jigsawField(source,"poolAliases"),jigsawField(source,"dimensionPadding"),jigsawField(source,"liquidSettings"));
    }
    @SuppressWarnings("unchecked")
    private static <T> T jigsawField(Object source,String name) {
        try {var field=source.getClass().getDeclaredField(name);field.setAccessible(true);return (T)field.get(source);}
        catch(ReflectiveOperationException failure) {throw new AssertionError(failure);}
    }

    private static void assertVigilPreservesTerrain(GameTestHelper h,
            net.minecraft.world.level.levelgen.structure.StructureStart start,
            net.minecraft.world.level.levelgen.structure.StructurePiece vigil) {
        var withoutVigil = new net.minecraft.world.level.levelgen.structure.StructureStart(
                start.getStructure(), start.getChunkPos(), 0,
                new net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer(
                        start.getPieces().stream().filter(piece -> piece != vigil).toList()));
        var bounds = vigil.getBoundingBox();
        for (int x = bounds.minX() - 8; x <= bounds.maxX() + 8; x += 8) {
            for (int z = bounds.minZ() - 8; z <= bounds.maxZ() + 8; z += 8) {
                var chunk = new net.minecraft.world.level.ChunkPos(new BlockPos(x, bounds.minY(), z));
                var actual = net.minecraft.world.level.levelgen.Beardifier.forStructuresInChunk(terrainManager(h, start), chunk);
                var expected = net.minecraft.world.level.levelgen.Beardifier.forStructuresInChunk(terrainManager(h, withoutVigil), chunk);
                for (int y = bounds.minY() - 4; y <= bounds.maxY() + 8; y += 4) {
                    var point = new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(x, y, z);
                    h.assertTrue(actual.compute(point) == expected.compute(point),
                            "Vigil changes surrounding terrain density at " + x + "," + y + "," + z);
                }
            }
        }
    }

    private static net.minecraft.world.level.StructureManager terrainManager(GameTestHelper h,
            net.minecraft.world.level.levelgen.structure.StructureStart start) {
        return new net.minecraft.world.level.StructureManager(h.getLevel(),
                h.getLevel().getServer().getWorldData().worldGenOptions(), null) {
            @Override public java.util.List<net.minecraft.world.level.levelgen.structure.StructureStart> startsForStructure(
                    net.minecraft.world.level.ChunkPos chunk,
                    java.util.function.Predicate<net.minecraft.world.level.levelgen.structure.Structure> predicate) {
                return predicate.test(start.getStructure()) ? java.util.List.of(start) : java.util.List.of();
            }
        };
    }

    private static ServerPlayer player(GameTestHelper h) {
        var cookie=CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),"antecedent-test"),false);
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new Connection(PacketFlow.SERVERBOUND);new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(p.server,connection,p,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        };
        p.setGameMode(GameType.SURVIVAL);p.setPos(h.absolutePos(new BlockPos(1,3,1)).getCenter());p.setNoGravity(true);
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(4);
        return p;
    }
    private static ItemStack sample(String kind,String source) { return AhaematicSample.create(kind,source); }
    @GameTest(template="empty") public static void bothSourcesAreExaminableButNeverBlood(GameTestHelper h) {
        var p=player(h);
        var spawned=new ItemStack(ItemInit.ahaematic_colloid.get());
        h.assertTrue(AhaematicColloidItem.DEFAULT_SAMPLE.equals(AhaematicSample.get(spawned)) && BloodSampleData.examinable(spawned),"Plain Ahaematic item lacks its creative/JEI Warden provenance");
        for(var vial:new ItemStack[]{sample("entity","minecraft:warden"),sample("block","minecraft:sculk_catalyst")}) {
            h.assertTrue(!BloodSampleData.identified(vial) && BloodSampleData.examinable(vial),"Sample classification failed");
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ItemInit.hematic_microscope.get()));p.setItemInHand(InteractionHand.OFF_HAND,vial);
            p.getMainHandItem().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
            for(int tick=0;tick<40;tick++)p.doTick();p.releaseUsingItem();
            h.assertTrue(BloodSampleData.identified(vial),"Microscope did not complete");
            h.assertTrue(BloodSampleData.entityType(vial)==null,"Ahaematic sample escaped into blood classification");
        }
        h.assertTrue(HemoCapabilityAccess.antecedent(p).has(SAMPLE_ANALYZED),"Personal analysis evidence missing");
        h.assertTrue(HemoCapabilityAccess.clinicalBlood(p).sourceCount()==0,"Ahaematic sample credited as clinical blood");h.succeed();
    }
    @GameTest(template="empty") public static void legacyAndUnknownProvenanceSurviveRoundTrip(GameTestHelper h) {
        var vial=new ItemStack(ItemInit.bloody_vial.get());var tag=new CompoundTag();tag.putString(BloodVialItem.TAG_ENTITY_TYPE,"minecraft:warden");tag.putString("owner_note","keep");
        vial.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        h.assertTrue(AhaematicSample.readable(vial) && BloodSampleData.entityType(vial)==null,"Legacy Warden still behaves as blood");
        vial=AhaematicSample.migrate(vial);
        vial.set(DataComponentInit.AHAEMATIC_SAMPLE.get(),new AhaematicSample("future","other:unknown"));
        var restored=ItemStack.parse(h.getLevel().registryAccess(),vial.save(h.getLevel().registryAccess())).orElseThrow();
        h.assertTrue(AhaematicSample.is(restored) && !AhaematicSample.readable(restored),"Unknown provenance discarded or accepted");
        var empty=BloodSampleData.emptyVessel(restored);
        h.assertTrue(!AhaematicSample.is(empty) && !BloodSampleData.isFilled(empty),"Empty vessel retained sample");
        h.assertTrue(empty.get(DataComponents.CUSTOM_DATA).copyTag().getString("owner_note").equals("keep"),"Unrelated vessel data lost");h.succeed();
    }
    @GameTest(template="empty") public static void scarConsumesOnlySampleAndSocketConservesItems(GameTestHelper h) {
        var p=player(h);var vial=sample("block","minecraft:sculk_catalyst");BloodSampleData.identify(vial);
        var scar=new ItemStack(ItemInit.listening_scar.get());p.setItemInHand(InteractionHand.MAIN_HAND,scar);p.setItemInHand(InteractionHand.OFF_HAND,vial);
        scar.use(h.getLevel(),p,InteractionHand.MAIN_HAND);
        h.assertTrue(ListeningScarItem.state(scar)==1 && !BloodSampleData.isFilled(p.getOffhandItem()) && p.getOffhandItem().is(ItemInit.bloody_vial.get()),"Housing did not return empty vessel");
        ListeningScarItem.awaken(p);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        HemoCapabilityAccess.requireEquipment(p).setStackInSlot(5,new ItemStack(ItemInit.charm_of_vascularium.get()));
        var menu=new HarbingerEquipmentMenu(1,p.level(),p.blockPosition(),p.getInventory(),p,true);p.containerMenu=menu;
        menu.setCarried(scar);menu.clicked(45,0,ClickType.PICKUP,p);
        h.assertTrue(menu.getCarried().isEmpty() && ListeningScarItem.awake(menu.getSlot(45).getItem()),"Socket insertion failed");
        var charm=HemoCapabilityAccess.requireEquipment(p).getStackInSlot(5);
        var restored=ItemStack.parse(h.getLevel().registryAccess(),charm.save(h.getLevel().registryAccess())).orElseThrow();
        h.assertTrue(ListeningScarItem.awake(restored.get(DataComponentInit.CHARM_TALISMAN.get()).copyOne()),"Socket did not persist with charm");
        menu.clicked(45,0,ClickType.PICKUP,p);
        h.assertTrue(menu.getSlot(45).getItem().isEmpty() && menu.getCarried().getCount()==1,"Socket extraction duplicated/lost talisman");
        var field=new HarbingerEquipmentMenu(2,p.getInventory());
        h.assertTrue(!field.getSlot(45).mayPlace(menu.getCarried()) && !field.getSlot(45).mayPickup(p),"Field menu bypassed Vanity");h.succeed();
    }
    @GameTest(template="empty") public static void ancientCylinderPlaybackAndSaveRemainNonDestructive(GameTestHelper h) {
        var pos=new BlockPos(0,2,0);h.setBlock(pos,BlockInit.clairaudiograph.get());
        var machine=(ClairaudiographBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(pos));
        var vial=sample("block","minecraft:sculk_catalyst");BloodSampleData.identify(vial);
        machine.inventory.setStackInSlot(0,vial);machine.inventory.setStackInSlot(1,AncientRecordings.cylinder(AncientRecordings.SEVERED));
        machine.startPlayback(false);h.assertTrue(machine.playing(),"Ancient cylinder rejected");
        var saved=machine.saveWithoutMetadata(h.getLevel().registryAccess());
        machine.stopPlayback();machine.loadWithComponents(saved,h.getLevel().registryAccess());
        h.assertTrue(!machine.playing() && machine.program()==AncientRecordings.SEVERED,"Reload resumed stale audio or lost recording");
        h.assertTrue(BloodSampleData.identified(machine.inventory.getStackInSlot(0)),"Playback consumed or reset specimen");
        machine.inventory.setStackInSlot(1,ItemStack.EMPTY);h.assertTrue(!machine.playable(),"Empty machine remains playable");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=240) public static void damagedFixturesDelayAndRetireWithoutChangingVanilla(GameTestHelper h) {
        var origin=h.absolutePos(new BlockPos(0,2,0)).subtract(VigilLayout.VESSEL);
        h.getLevel().setBlock(origin.offset(VigilLayout.VESSEL),BlockInit.antecedent_vessel.get().defaultBlockState(),3);
        var site=(VigilArchiveBlockEntity)h.getLevel().getBlockEntity(origin.offset(VigilLayout.VESSEL));
        site.onLoad();
        for(var sensor:VigilLayout.SENSORS)h.getLevel().setBlock(origin.offset(sensor),Blocks.SCULK_SENSOR.defaultBlockState(),3);
        var shrieker=origin.offset(VigilLayout.SHRIEKER);h.getLevel().setBlock(shrieker,Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON,true),3);
        for(var sensor:VigilLayout.SENSORS)h.getLevel().getBlockEntity(origin.offset(sensor)).getPersistentData().putBoolean(VigilSites.FIXTURE_MARKER,true);
        h.getLevel().getBlockEntity(shrieker).getPersistentData().putBoolean(VigilSites.FIXTURE_MARKER,true);
        site.disturbance(origin.offset(12,1,40).getCenter());
        var first=origin.offset(VigilLayout.SENSORS[0]);
        var activated=new java.util.concurrent.atomic.AtomicBoolean();
        h.onEachTick(()->{if(h.getLevel().getBlockState(first).is(Blocks.SCULK_SENSOR) && h.getLevel().getBlockState(first).getValue(SculkSensorBlock.PHASE)==SculkSensorPhase.ACTIVE)activated.set(true);});
        h.runAtTickTime(60,()->h.assertTrue(h.getLevel().getBlockState(first).getValue(SculkSensorBlock.PHASE)==SculkSensorPhase.INACTIVE,"Sensor reacted without damage delay"));
        h.runAtTickTime(125,()->{
            h.assertTrue(activated.get(),"Original fixture never answered the disturbance");
            h.assertTrue(!h.getLevel().getBlockState(shrieker).getValue(SculkShriekerBlock.CAN_SUMMON),"Failed Shrieker can summon");
            h.getLevel().setBlock(first,Blocks.AIR.defaultBlockState(),3);h.getLevel().setBlock(first,Blocks.SCULK_SENSOR.defaultBlockState(),3);
            h.assertTrue(VigilSites.fixture(h.getLevel(),first)==null,"Replacement retained fixture behavior");
            var elsewhere=first.offset(50,0,0);h.getLevel().setBlock(elsewhere,Blocks.SCULK_SENSOR.defaultBlockState(),3);
            h.assertTrue(VigilSites.fixture(h.getLevel(),elsewhere)==null,"Ordinary Sensor was registered");h.succeed();
        });
    }
    @GameTest(template="empty",timeoutTicks=400)
    public static void modularSitesRetainFixturesAndLayoutInEveryRotation(GameTestHelper h) {
        var level=h.getLevel();int index=0;
        for(var layout:VigilModules.ALL)for(var rotation:Rotation.values()) {
            var origin=h.absolutePos(new BlockPos(256+(index++%6)*80,30,256+(index/6)*80));
            var template=level.getStructureManager().getOrCreate(Hemomancy.rloc("antecedent/"+layout.composite()));
            h.assertTrue(template.placeInWorld(level,origin,origin,new StructurePlaceSettings().setRotation(rotation),level.random,2),"Module template failed to place");
            var vessel=origin.offset(layout.plan().vessel().rotate(rotation));
            var site=(VigilArchiveBlockEntity)level.getBlockEntity(vessel);
            h.assertTrue(site!=null && site.layout()==layout.plan() && site.origin().equals(origin),"Modular controller lost its site frame");
            for(var sensor:layout.plan().sensors())h.assertTrue(VigilSites.authored(level,site.world(sensor)),"Modular sensor lost authored identity");
            h.assertTrue(level.getBlockState(site.world(layout.plan().catalyst())).is(Blocks.SCULK_CATALYST),"Modular catalyst missing");
            h.assertTrue(level.getBlockEntity(site.world(layout.plan().record())) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity,"Modular final record missing");
            var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)level.getBlockEntity(site.world(layout.plan().chest()));
            h.assertTrue(chest!=null && AncientRecordings.get(chest.getItem(0))==AncientRecordings.SEVERED,"Modular entrance lost recording");
            var saved=site.saveWithFullMetadata(level.registryAccess());
            var restored=(VigilArchiveBlockEntity)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(vessel,site.getBlockState(),saved,level.registryAccess());
            h.assertTrue(restored!=null && restored.layout()==layout.plan() && restored.origin().equals(origin),"Modular layout lost on reload");
            h.assertTrue(site.getUpdateTag(level.registryAccess()).getInt("VigilLayout")==layout.plan().version(),"Client receives wrong module layout");
        }
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=400) public static void authoredVigilPlacesInEveryRotationWithRecordsAndFixtures(GameTestHelper h) {
        var level=h.getLevel();var template=level.getStructureManager().getOrCreate(Hemomancy.rloc("antecedent/vigil"));
        h.assertTrue(template.getSize().equals(VigilLayout.COMPACT.size()),"Vigil template missing or wrong size");
        int index=0;
        for(var rotation:Rotation.values()) {
            var origin=h.absolutePos(new BlockPos(100+index++*100,2,0));
            template.placeInWorld(level,origin,origin,new StructurePlaceSettings().setRotation(rotation),level.random,2);
            var vessel=origin.offset(VigilLayout.COMPACT.vessel().rotate(rotation));
            h.assertTrue(level.getBlockEntity(vessel) instanceof VigilArchiveBlockEntity,"Vessel missing after rotation "+rotation);
            var site=(VigilArchiveBlockEntity)level.getBlockEntity(vessel);
            h.assertTrue(site.layout().version()==2,"Compact layout version lost");
            h.assertTrue(site.getUpdateTag(level.registryAccess()).getInt("VigilLayout")==2,"Client layout update missing");
            var saved=site.saveWithFullMetadata(level.registryAccess());
            var restored=(VigilArchiveBlockEntity)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(vessel,site.getBlockState(),saved,level.registryAccess());
            h.assertTrue(restored!=null && restored.layout().version()==2 && restored.origin().equals(origin),"Compact layout lost on reload");
            saved.remove("VigilLayout");
            var legacy=(VigilArchiveBlockEntity)net.minecraft.world.level.block.entity.BlockEntity.loadStatic(vessel,site.getBlockState(),saved,level.registryAccess());
            h.assertTrue(legacy!=null && legacy.layout().version()==1 && legacy.origin().equals(vessel.subtract(VigilLayout.VESSEL.rotate(rotation))),"Old unversioned site was reinterpreted as compact");
            h.assertTrue(site.origin().equals(origin),"Rotated site coordinates incorrect");
            h.assertTrue(level.getBlockState(site.world(VigilLayout.COMPACT.shrieker())).is(Blocks.SCULK_SHRIEKER),"Shrieker missing");
            for(var sensor:VigilLayout.COMPACT.sensors())h.assertTrue(VigilSites.authored(level,site.world(sensor)),"Rotated Sensor lost its fixture marker");
            site.onChunkUnloaded();h.assertTrue(VigilSites.authored(level,site.world(VigilLayout.COMPACT.shrieker())),"Unloaded controller lost fixture isolation");site.onLoad();
            h.assertTrue(level.getBlockEntity(site.world(VigilLayout.COMPACT.record())) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity,"Record 7 missing");
            var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)level.getBlockEntity(site.world(VigilLayout.COMPACT.chest()));
            h.assertTrue(chest!=null && AncientRecordings.get(chest.getItem(0))==AncientRecordings.SEVERED,"Guaranteed Severed Record missing");
        }
        h.succeed();
    }
}
