package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.client.render.entity.PossessionEchoRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.HunterFungusRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.AntibodyRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.facsimile.FacsimileRenderers;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.*;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronPillarRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronSpikeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.blood.iron.IronWallRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.AnnettaKnowlesRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.LatentAnnettaInfectionRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.annetta.StainedPriestessRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.endgame.MycophantGlobRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.endgame.MycophantHazardRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.endgame.MycophantLanceRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.endgame.MycophantRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.endgame.NaeglerophaeonRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.endgame.VesperTheCrownedRefusalRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.endgame.VesperTheEveningStarRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.hemorath.HemorathRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.putriciel.PutricielRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.seraphae.ContainmentAnchorRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.seraphae.SeraphaeFragmentRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.seraphae.SeraphaeRenderer;
import com.vincenthuto.hemomancy.client.render.entity.boss.velorum.VelorumRenderer;
import com.vincenthuto.hemomancy.client.render.entity.misc.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.animal.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.arthropod.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.monster.*;
import com.vincenthuto.hemomancy.client.render.entity.mob.will.WillAnchorRenderer;
import com.vincenthuto.hemomancy.client.render.entity.mob.will.WillRenderer;
import com.vincenthuto.hemomancy.client.render.entity.npc.*;
import com.vincenthuto.hemomancy.client.render.entity.projectile.*;
import com.vincenthuto.hemomancy.client.render.entity.summon.*;
import com.vincenthuto.hemomancy.client.render.item.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.decoration.NonEuclideanHallwayRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.*;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.PhlebotomistsCabinetRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.PhlebotomistsFieldCaseRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.plant.GourdvineTapRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.RingDoorRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.SaintSarcophagusRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.rite.IronBrazierRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.rite.SuspendedBloodCrystalRenderer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.rite.SuspendedVivaniteRenderer;
import com.vincenthuto.hemomancy.client.render.tile.inscription.DictationTableRenderer;
import com.vincenthuto.hemomancy.client.render.tile.inscription.DiscoveryInscriptionBlockRenderer;
import com.vincenthuto.hemomancy.client.render.tile.unstained.SuspendedCleansedBloodCrystalRenderer;
import com.vincenthuto.hemomancy.client.render.tile.unstained.crafting.PallidRetortRenderer;
import com.vincenthuto.hemomancy.client.render.tile.unstained.functional.AltarOfCleansingRenderer;
import com.vincenthuto.hemomancy.common.init.*;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * Entity and block-entity renderers.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class EntityRendererRegistrar {

    private EntityRendererRegistrar() {
    }

    @SubscribeEvent
    public static void renderEntities(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityInit.directed_blood_orb.get(), BloodOrbDirectedRenderer::new);
        event.registerEntityRenderer(EntityInit.blood_cloud_carrier.get(), BloodCloudCarrierRenderer::new);
        event.registerEntityRenderer(EntityInit.blood_cloud.get(), BloodCloudRenderer::new);
        event.registerEntityRenderer(EntityInit.tracking_blood_orb.get(), BloodOrbTrackingRenderer::new);
        event.registerEntityRenderer(EntityInit.tracking_snake.get(), TrackingSerpentRenderer::new);
        event.registerEntityRenderer(EntityInit.tracking_pests.get(), TrackingPestsRenderer::new);
        event.registerEntityRenderer(EntityInit.blood_bolt.get(), BloodBoltRenderer::new);
        event.registerEntityRenderer(EntityInit.living_sickle_hook.get(), LivingSickleHookRenderer::new);
        event.registerEntityRenderer(EntityInit.living_flail_head.get(), LivingFlailHeadProjectileRenderer::new);
        event.registerEntityRenderer(EntityInit.vesper_scute_projectile.get(), VesperScuteProjectileRenderer::new);
        event.registerEntityRenderer(EntityInit.veinwing_feather.get(), VeinwingFeatherRenderer::new);
        event.registerEntityRenderer(EntityInit.circus_knife.get(), CircusKnifeProjectileRenderer::new);
        event.registerEntityRenderer(EntityInit.circus_carousel.get(), CircusCarouselRenderer::new);
        event.registerEntityRenderer(EntityInit.circus_ringmaster.get(), CircusRingmasterRenderer::new);
        event.registerEntityRenderer(EntityInit.peacock_spider.get(), PeacockSpiderRenderer::new);
        event.registerEntityRenderer(EntityInit.vampire_bat.get(), VampireBatRenderer::new);
        event.registerEntityRenderer(EntityInit.blood_needle.get(), BloodNeedleRenderer::new);
        event.registerEntityRenderer(EntityInit.blood_shot.get(), BloodShotRenderer::new);
        event.registerEntityRenderer(EntityInit.blood_bullet.get(), BloodBulletRenderer::new);
        event.registerEntityRenderer(EntityInit.morphling_polyp_item.get(), MorphlingPolypItemRenderer::new);
        event.registerEntityRenderer(EntityInit.qliphoth_seed_item.get(), QliphothSeedItemEntityRenderer::new);
        event.registerEntityRenderer(EntityInit.iron_pillar.get(), IronPillarRenderer::new);
        event.registerEntityRenderer(EntityInit.iron_spike.get(), IronSpikeRenderer::new);
        event.registerEntityRenderer(EntityInit.iron_wall.get(), IronWallRenderer::new);
        event.registerEntityRenderer(EntityInit.wretched_will.get(), WretchedWillRenderer::new);
        event.registerEntityRenderer(EntityInit.will.get(), WillRenderer::new);
        event.registerEntityRenderer(EntityInit.will_anchor.get(), WillAnchorRenderer::new);
        event.registerEntityRenderer(EntityInit.arbor_of_will.get(), ArborOfWillRenderer::new);
        event.registerEntityRenderer(EntityInit.leech.get(), LeechRenderer::new);
        event.registerEntityRenderer(EntityInit.bloodlicker.get(), BloodlickerRenderer::new);
        event.registerEntityRenderer(EntityInit.unsettled_ichor.get(), UnsettledIchorRenderer::new);
        event.registerEntityRenderer(EntityInit.awakened_ichorian_sigil.get(),
                AwakenedIchorianSigilRenderer::new);
        event.registerEntityRenderer(EntityInit.humanity_sprite.get(), HumanitySpriteRenderer::new);
        event.registerEntityRenderer(EntityInit.bog_revenant.get(), BogRevenantRenderer::new);
        event.registerEntityRenderer(EntityInit.fargone.get(), FargoneRenderer::new);
        event.registerEntityRenderer(EntityInit.thirster.get(), ThirsterRenderer::new);
        event.registerEntityRenderer(EntityInit.facsimile_bat.get(), FacsimileRenderers.Bat::new);
        event.registerEntityRenderer(EntityInit.facsimile_enderman.get(), FacsimileRenderers.Enderman::new);
        event.registerEntityRenderer(EntityInit.facsimile_blaze.get(), FacsimileRenderers.Blaze::new);
        event.registerEntityRenderer(EntityInit.facsimile_golem.get(), FacsimileRenderers.Golem::new);
        event.registerEntityRenderer(EntityInit.facsimile_horse.get(), FacsimileRenderers.HorseFacsimile::new);
        event.registerEntityRenderer(EntityInit.facsimile_donkey.get(), FacsimileRenderers.DonkeyFacsimile::new);
        event.registerEntityRenderer(EntityInit.facsimile_wolf.get(), FacsimileRenderers.Wolf::new);
        event.registerEntityRenderer(EntityInit.facsimile_spider.get(), FacsimileRenderers.SpiderFacsimile::new);
        event.registerEntityRenderer(EntityInit.antibody.get(), AntibodyRenderer::new);
        event.registerEntityRenderer(EntityInit.possession_echo.get(), PossessionEchoRenderer::new);
        event.registerEntityRenderer(EntityInit.hunter_fungus.get(), HunterFungusRenderer::new);
        event.registerEntityRenderer(EntityInit.fungling.get(), FunglingRenderer::new);
        event.registerEntityRenderer(EntityInit.tooth_pecks.get(), ToothPecksRenderer::new);
        event.registerEntityRenderer(EntityInit.myelin_borer.get(), MyelinBorerRenderer::new);
        event.registerEntityRenderer(EntityInit.mortarbound.get(), MortarboundRenderer::new);
        event.registerEntityRenderer(EntityInit.naeglerophaeon.get(), NaeglerophaeonRenderer::new);
        event.registerEntityRenderer(EntityInit.chitinite.get(), ChitiniteRenderer::new);
        event.registerEntityRenderer(EntityInit.fervent_chitinite.get(), FerventChitiniteRenderer::new);
        event.registerEntityRenderer(EntityInit.chthonian.get(), ChthonianRenderer::new);
        event.registerEntityRenderer(EntityInit.blood_drunk_puppeteer.get(), BloodDrunkPuppeteerRenderer::new);
        event.registerEntityRenderer(EntityInit.enthralled_doll.get(), EnthralledDollRenderer::new);
        event.registerEntityRenderer(EntityInit.blood_thrall.get(), BloodThrallRenderer::new);
        event.registerEntityRenderer(EntityInit.veinwing_vulture.get(), VeinwingVultureRenderer::new);
        event.registerEntityRenderer(EntityInit.cinder_bellows.get(), com.vincenthuto.hemomancy.client.render.entity.summon.CinderBellowsRenderer::new);
        event.registerEntityRenderer(EntityInit.marrow_spitter.get(), MarrowSpitterRenderer::new);
        event.registerEntityRenderer(EntityInit.gorebound_hulk.get(), GoreboundHulkRenderer::new);
        event.registerEntityRenderer(EntityInit.mnemonist_puppet.get(), MnemonistPuppetRenderer::new);
        event.registerEntityRenderer(EntityInit.ringmaster_pattern.get(), com.vincenthuto.hemomancy.client.render.entity.summon.RingmasterPatternRenderer::new);
        event.registerEntityRenderer(EntityInit.scarlet_mummer.get(), ScarletMummerRenderer::new);
        event.registerEntityRenderer(EntityInit.sanguine_hound.get(), SanguineHoundRenderer::new);
        event.registerEntityRenderer(EntityInit.lump_of_thought.get(), LumpOfThoughtRenderer::new);
        event.registerEntityRenderer(EntityInit.chthonian_queen.get(), ChthonianQueenRenderer::new);
        event.registerEntityRenderer(EntityInit.abhorrent_thought.get(), AbhorrentThoughtRenderer::new);
        event.registerEntityRenderer(EntityInit.barbed_urchin.get(), BarbedUrchinRenderer::new);
        event.registerEntityRenderer(EntityInit.chalybeate_snail.get(), ChalybeateSnailRenderer::new);
        event.registerEntityRenderer(EntityInit.blood_lantern_jelly.get(), BloodLanternJellyRenderer::new);
        event.registerEntityRenderer(EntityInit.mnemonic_whale.get(), MnemonicWhaleRenderer::new);
        event.registerEntityRenderer(EntityInit.brined_votary.get(), BrinedVotaryRenderer::new);
        event.registerEntityRenderer(EntityInit.prism_cuttle.get(), PrismCuttleRenderer::new);
        event.registerEntityRenderer(EntityInit.chiton.get(), context -> new com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.PelagicCreatureRenderer<>(context, com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.PelagicCreatureModel.Style.CHITON));
        event.registerEntityRenderer(EntityInit.pyrosome.get(), context -> new com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.PelagicCreatureRenderer<>(context, com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.PelagicCreatureModel.Style.PYROSOME));
        event.registerEntityRenderer(EntityInit.pelagic_herring.get(), context -> new com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.PelagicCreatureRenderer<>(context, com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.PelagicCreatureModel.Style.HERRING));
        event.registerEntityRenderer(EntityInit.ice_fish.get(), com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.IceFishRenderer::new);
        event.registerEntityRenderer(EntityInit.siphonophore.get(), context -> new com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.PelagicCreatureRenderer<>(context, com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.PelagicCreatureModel.Style.SIPHONOPHORE));
        event.registerEntityRenderer(EntityInit.bloody_belly_comb_jelly.get(), context -> new com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.PelagicCreatureRenderer<>(context, com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.PelagicCreatureModel.Style.COMB_JELLY));
        event.registerEntityRenderer(EntityInit.hagfish.get(), context -> new com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.PelagicCreatureRenderer<>(context, com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.PelagicCreatureModel.Style.HAGFISH));
        event.registerEntityRenderer(EntityInit.vampire_squid.get(), com.vincenthuto.hemomancy.client.render.entity.mob.aquatic.VampireSquidRenderer::new);
        event.registerEntityRenderer(EntityInit.hemolymphopoda.get(), HemolymphopodaRenderer::new);
        event.registerEntityRenderer(EntityInit.lantern_tick.get(), LanternTickRenderer::new);
        event.registerEntityRenderer(EntityInit.erythromycelium_eruptus.get(), ErythromyceliumEruptusRenderer::new);
        event.registerEntityRenderer(EntityInit.morphling_polyp.get(), MorphlingPolypRenderer::new);
        event.registerEntityRenderer(EntityInit.flying_charm.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EntityInit.hemolytic_vial_projectile.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EntityInit.constrictor_cord_projectile.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EntityInit.blood_chum_projectile.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EntityInit.sanguis_lancea.get(), SanguisLanceaRenderer::new);
        event.registerEntityRenderer(EntityInit.unstained_zealot.get(), UnstainedZealotRenderer::new);
        event.registerEntityRenderer(EntityInit.unstained_guardian.get(), UnstainedGuardianRenderer::new);
        event.registerEntityRenderer(EntityInit.unstained_acolyte.get(), UnstainedAcolyteRenderer::new);
        event.registerEntityRenderer(EntityInit.unstained_scout.get(), UnstainedScoutRenderer::new);
        event.registerEntityRenderer(EntityInit.harbinger_hermit.get(), HarbingerHermitRenderer::new);
        event.registerEntityRenderer(EntityInit.harbinger_alchemist.get(), HarbingerAlchemistRenderer::new);
        event.registerEntityRenderer(EntityInit.harbinger_artificer.get(), HarbingerArtificerRenderer::new);
        event.registerEntityRenderer(EntityInit.harbinger_cicatrix_anchorite.get(), HarbingerCicatrixAnchoriteRenderer::new);
        event.registerEntityRenderer(EntityInit.harbinger_mnemonist.get(), HarbingerMnemonistRenderer::new);
        event.registerEntityRenderer(EntityInit.harbinger_vicar.get(), HarbingerVicarRenderer::new);
        event.registerEntityRenderer(EntityInit.harbinger_voyager.get(), HarbingerVoyagerRenderer::new);
        event.registerEntityRenderer(EntityInit.harbinger_votary_wayfarer.get(), HarbingerVotaryWayfarerRenderer::new);
        event.registerEntityRenderer(EntityInit.circus_strongman.get(), context -> new CircusPerformerRenderer<>(
                context, new CircusStrongmanModel(context.bakeLayer(CircusStrongmanModel.LAYER_LOCATION)),
                "circus_strongman", 0.55F));
        event.registerEntityRenderer(EntityInit.circus_beast_tamer.get(), context -> new CircusPerformerRenderer<>(
                context, new CircusBeastTamerModel(context.bakeLayer(CircusBeastTamerModel.LAYER_LOCATION)),
                "circus_beast_tamer", 0.45F));
        event.registerEntityRenderer(EntityInit.circus_threadkeeper.get(), context -> new CircusPerformerRenderer<>(
                context, new CircusThreadkeeperModel(context.bakeLayer(CircusThreadkeeperModel.LAYER_LOCATION)),
                "circus_threadkeeper", 0.4F));
        event.registerEntityRenderer(EntityInit.circus_understudy.get(), context -> new CircusPerformerRenderer<>(
                context, new CircusUnderstudyModel(context.bakeLayer(CircusUnderstudyModel.LAYER_LOCATION)),
                "circus_understudy", 0.4F));
        event.registerEntityRenderer(EntityInit.circus_fire_eater.get(), context -> new CircusPerformerRenderer<>(
                context, new CircusFireEaterModel(context.bakeLayer(CircusFireEaterModel.LAYER_LOCATION)),
                "fire_eater", 0.45F));
        event.registerEntityRenderer(EntityInit.circus_stilt_walker.get(), context -> new CircusPerformerRenderer<>(
                context, new CircusStiltWalkerModel(context.bakeLayer(CircusStiltWalkerModel.LAYER_LOCATION)),
                "stilt_walker", 0.5F));
        event.registerEntityRenderer(EntityInit.circus_acrobat.get(), context -> new CircusPerformerRenderer<>(
                context, new CircusAcrobatModel(context.bakeLayer(CircusAcrobatModel.LAYER_LOCATION)),
                "acrobat", 0.4F));
        event.registerEntityRenderer(EntityInit.circus_knife_thrower.get(), context -> new CircusPerformerRenderer<>(
                context, new CircusKnifeThrowerModel(context.bakeLayer(CircusKnifeThrowerModel.LAYER_LOCATION)),
                "knife_thrower", 0.45F));
        event.registerEntityRenderer(EntityInit.drudge.get(),DrudgeRenderer::new);
        event.registerEntityRenderer(EntityInit.hemorath.get(), HemorathRenderer::new);
        event.registerEntityRenderer(EntityInit.annetta_knowles.get(), AnnettaKnowlesRenderer::new);
        event.registerEntityRenderer(EntityInit.annetta_pool.get(), com.vincenthuto.hemomancy.client.render.entity.boss.annetta.AnnettaPoolRenderer::new);
        event.registerEntityRenderer(EntityInit.infected_bolt.get(), com.vincenthuto.hemomancy.client.render.entity.boss.annetta.InfectedBoltRenderer::new);
        event.registerEntityRenderer(EntityInit.stained_lance.get(), com.vincenthuto.hemomancy.client.render.entity.boss.annetta.StainedLanceRenderer::new);
        event.registerEntityRenderer(EntityInit.priestess_eruption.get(), com.vincenthuto.hemomancy.client.render.entity.boss.annetta.PriestessEruptionRenderer::new);
        event.registerEntityRenderer(EntityInit.stained_priestess.get(), StainedPriestessRenderer::new);
        event.registerEntityRenderer(EntityInit.latent_annetta_infection.get(), LatentAnnettaInfectionRenderer::new);
        event.registerEntityRenderer(EntityInit.phlegethosoma.get(), com.vincenthuto.hemomancy.client.render.entity.boss.phlegethosoma.PhlegethosomaRenderer::new);
        event.registerEntityRenderer(EntityInit.coagulum_projectile.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(EntityInit.temporary_coagulum.get(), com.vincenthuto.hemomancy.client.render.entity.boss.phlegethosoma.CoagulumRenderer::new);
        event.registerEntityRenderer(EntityInit.putriciel.get(), PutricielRenderer::new);
        event.registerEntityRenderer(EntityInit.velorum.get(), VelorumRenderer::new);
        event.registerEntityRenderer(EntityInit.vesper_crowned_refusal.get(), VesperTheCrownedRefusalRenderer::new);
        event.registerEntityRenderer(EntityInit.vesper_evening_star.get(), VesperTheEveningStarRenderer::new);
        event.registerEntityRenderer(EntityInit.mycophant.get(), MycophantRenderer::new);
        event.registerEntityRenderer(EntityInit.mycophant_hazard.get(), MycophantHazardRenderer::new);
        event.registerEntityRenderer(EntityInit.mycophant_glob.get(), MycophantGlobRenderer::new);
        event.registerEntityRenderer(EntityInit.mycophant_lance.get(), MycophantLanceRenderer::new);
        event.registerEntityRenderer(EntityInit.seraphae.get(), SeraphaeRenderer::new);
        event.registerEntityRenderer(EntityInit.seraphae_fragment.get(), SeraphaeFragmentRenderer::new);
        event.registerEntityRenderer(EntityInit.frayed_thread.get(), com.vincenthuto.hemomancy.client.render.entity.weave.FrayedThreadRenderer::new);
        event.registerEntityRenderer(EntityInit.thread_bolt.get(), com.vincenthuto.hemomancy.client.render.entity.weave.ThreadBoltRenderer::new);
        event.registerEntityRenderer(EntityInit.thread_hazard.get(), com.vincenthuto.hemomancy.client.render.entity.weave.ThreadHazardRenderer::new);
        event.registerEntityRenderer(EntityInit.containment_anchor.get(), ContainmentAnchorRenderer::new);
        event.registerEntityRenderer(EntityInit.hematic_construct.get(), HematicConstructRenderer::new);
        event.registerEntityRenderer(EntityInit.spectral_companion.get(), PaleIntercessionRenderer::new);
        event.registerEntityRenderer(EntityInit.phantasmal_echo.get(), PhantasmalEchoRenderer::new);
        event.registerEntityRenderer(EntityInit.dark_arrow.get(), DarkArrowRenderer::new);
        event.registerEntityRenderer(EntityInit.desiccant.get(), DesiccantRenderer::new);
        event.registerEntityRenderer(EntityInit.crimson_doe.get(), CrimsonDoeRenderer::new);
        event.registerEntityRenderer(EntityInit.verdigris_moth.get(), VerdigrisMothRenderer::new);
        event.registerEntityRenderer(EntityInit.choir_keeper.get(), ChoirKeeperRenderer::new);
        event.registerEntityRenderer(EntityInit.osteophage.get(), com.vincenthuto.hemomancy.client.render.entity.mob.animal.OsteophageRenderer::new);
        event.registerEntityRenderer(EntityInit.luminal_cicada.get(), LuminalCicadaRenderer::new);
        event.registerEntityRenderer(EntityInit.hematic_burrower.get(), HematicBurrowerRenderer::new);
        event.registerEntityRenderer(EntityInit.scarlet_serpent.get(), ScarletSerpentRenderer::new);
        event.registerEntityRenderer(EntityInit.hemojelly.get(), HemojellyRenderer::new);
        event.registerEntityRenderer(EntityInit.venous_strider.get(), VenousStriderRenderer::new);
        event.registerEntityRenderer(EntityInit.venom_rib_centipede.get(), VenomRibCentipedeRenderer::new);
        event.registerEntityRenderer(EntityInit.covenant_throne_seat.get(), CovenantThroneSeatRenderer::new);
        event.registerEntityRenderer(EntityInit.hematic_armature_restraint.get(), ArmatureRestraintRenderer::new);

    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityInit.discovery_inscription.get(),
                DiscoveryInscriptionBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.scar_station.get(), ScarStationRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.ghastly_alembic.get(), GhastlyAlembicRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.pallid_retort.get(), PallidRetortRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.morphling_incubator.get(), MorphlingIncubatorRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.puppeteers_spindle.get(), PuppeteersSpindleRenderer::new);
       event.registerBlockEntityRenderer(BlockEntityInit.mycelial_crucible.get(), MycelialCrucibleRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.mycelial_lantern.get(), MycelialLanternRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.morphling_cradle.get(), MorphlingCradleRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.gourdvine_tap.get(), GourdvineTapRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.pelagic_colony.get(), com.vincenthuto.hemomancy.client.render.tile.harbinger.plant.PelagicColonyRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.specimen_jar.get(), SpecimenJarRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.unstained_podium.get(), UnstainedPodiumRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.scrying_podium.get(), ScryingPodiumRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.sanguine_condenser.get(),
                com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.SanguineCondenserRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.scarlet_vanity.get(), ScarletVanityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.mason_effigy.get(), MasonsEffigyRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.iron_brazier.get(), IronBrazierRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.fungal_podium.get(), FungalPodiumRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.fungal_implantation_pylon.get(),
                FungalImplantationPylonRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.suspended_vivianite.get(),
                SuspendedVivaniteRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.suspended_blood_crystal.get(),
                SuspendedBloodCrystalRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.suspended_cleansed_blood_crystal.get(),
                SuspendedCleansedBloodCrystalRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.dendritic_distributor.get(),
                DendriticDistributorRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.hematic_stake.get(), HematicStakeRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.consecrated_bloodwell.get(), ConsecratedBloodwellRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.mortal_display.get(), MortalDisplayRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.cardinal_focus.get(), CardinalFocusRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.vial_centrifuge.get(), VialCentrifugeRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.enzymatic_scriptorium.get(), EnzymaticScriptoriumRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.resonant_forge.get(), ResonantForgeRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.somatic_loom.get(),
                SomaticLoomRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.earthen_vein.get(), EarthenVeinRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.mnemonic_reliquary.get(), MnemonicReliquaryRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.antecedent_vessel.get(), com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.AntecedentVesselRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.clairaudiograph.get(), com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.ClairaudiographRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.phlebotomists_cabinet.get(), PhlebotomistsCabinetRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.phlebotomists_field_case.get(), PhlebotomistsFieldCaseRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.dictation_table.get(), DictationTableRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.harbinger_escritoire.get(), com.vincenthuto.hutoslib.client.render.block.DictationTableRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.visceral_mirror.get(), VisceralMirrorRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.non_euclidean_hallway.get(), NonEuclideanHallwayRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.qliphoth_bloom.get(),
                QliphothBloomBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.saint_sarcophagus.get(),
                SaintSarcophagusRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.ring_door.get(), RingDoorRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.offering_stand.get(),
                com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.OfferingStandRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.pulse_pylon.get(),
                com.vincenthuto.hemomancy.client.render.tile.harbinger.puzzle.PulsePylonRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.rot_pyre.get(),
                com.vincenthuto.hemomancy.client.render.tile.harbinger.decoration.RotPyreRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.altar_of_cleansing.get(),
                AltarOfCleansingRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.sanguine_monolith.get(),
                SanguineMonolithRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.sanguine_conduit.get(),
                SanguineConduitBlockRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.covenant_throne.get(),
                CovenantThroneRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.warp_chair.get(),
                WarpChairRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityInit.hematic_armature.get(),
                HematicArmatureRenderer::new);
    }
}
