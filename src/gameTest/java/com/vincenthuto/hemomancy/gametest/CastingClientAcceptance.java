package com.vincenthuto.hemomancy.gametest;

import net.neoforged.api.distmarker.Dist;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtEnding;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Only enabled by tools/casting-animation-review.init.gradle, in a disposable build directory. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CastingClientAcceptance {
    private static int ticks,waiting;
    private static net.minecraft.world.entity.Entity drained;
    private static boolean initialized;
    private static long started;
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.castingReview"))return;
        var server=event.getServer();
        if(++waiting>24000) { server.halt(false);return; }
        var caster=server.getPlayerList().getPlayers().stream().filter(p -> p.getName().getString().startsWith("CastCaster")).findFirst().orElse(null);
        var observer=server.getPlayerList().getPlayerByName("CastObserver");
        if(caster==null || observer==null)return;
        if(!initialized) {
            initialized=true;
            var level=caster.serverLevel();
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
            level.setDayTime(Boolean.getBoolean("hemomancy.castingReviewNight")?18000:6000);level.setWeatherParameters(100000,0,false,false);
            for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++) {
                var floor=new BlockPos(x,99,z);
                level.setBlockAndUpdate(floor,Blocks.SMOOTH_QUARTZ.defaultBlockState());
                for(int y=1;y<9;y++)level.setBlockAndUpdate(floor.above(y),Blocks.AIR.defaultBlockState());
            }
            caster.teleportTo(level,.5,100,.5,0,0);
            observer.teleportTo(level,5.5,100,7.5,144,0);
            for(var p:server.getPlayerList().getPlayers()) {
                p.setGameMode(GameType.CREATIVE);
                p.removeAllEffects();
                p.getInventory().clearContent();
            }
            observer.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,caster.getEyePosition());
            Hemomancy.LOGGER.info("CAST_REVIEW two real clients connected");
        }
        int age=ticks++;
        if(age<80) { if(age==60)shot(server,"baseline",caster); return; }
        if(Boolean.getBoolean("hemomancy.castingReviewImpactsOnly")) { impacts(server,caster,observer,age-80); return; }
        if(Boolean.getBoolean("hemomancy.castingReviewBasicsOnly")) { basics(server,caster,observer,age-80); return; }
        int raw=(age-80)/40+(Boolean.getBoolean("hemomancy.castingReviewIntegrationOnly")?46+BESPOKE_SCENES:0),frame=(age-80)%40;
        if(raw>=40 && raw<40+BESPOKE_SCENES) { bespoke(server,caster,raw-40,frame);return; }
        // Older scenes keep their original numbers (and therefore shot names) after the bespoke block.
        int scene=raw<40?raw:raw-BESPOKE_SCENES;
        if(scene<40) {
            var school=EnumBloodTendency.values()[scene/5];
            var rank=EnumManipulationRank.values()[scene%5];
            long now=caster.level().getGameTime();
            if(frame==0)started=now;
            // The client re-resolves a track only when the manipulation id changes, so every profile needs its own id.
            if(frame==0 || frame==16)send(caster,CastingAnimationPacket.harbinger(caster.getId(),caster.getUUID(),"review-"+scene,
                    -1000+scene,school,null,rank,CastPurpose.AREA,CastStyle.SCHOOL,frame==0?CastPhase.OPENING:CastPhase.HOLD,started,now,20,40,InteractionHand.MAIN_HAND));
            if(frame==10)shot(server,school.name().toLowerCase()+"-"+rank.name().toLowerCase()+"-hold",caster);
            if(frame==20)shot(server,school.name().toLowerCase()+"-"+rank.name().toLowerCase()+"-held",caster);
            if(frame==22)send(caster,CastingAnimationPacket.harbinger(caster.getId(),caster.getUUID(),"review-"+scene,-1000+scene,
                    school,null,rank,CastPurpose.AREA,CastStyle.SCHOOL,CastPhase.RELEASE,now,now,40,40,InteractionHand.MAIN_HAND));
            if(frame==25 && rank==EnumManipulationRank.SUMMA)shot(server,school.name().toLowerCase()+"-release",caster);
            return;
        }
        // Real channel packets plus repeated tracking entry exercise server-owned loop snapshots.
        if(scene==40) {
            if(frame==0) {
                var m=ManipulationInit.sanguine_ward.get();
                ready(caster,m);ManipulationChannelManager.start(caster);
                observer.teleportTo(observer.serverLevel(),200,100,200,0,0);
            }
            if(frame==20) {
                observer.teleportTo(observer.serverLevel(),5.5,100,7.5,144,0);
                observer.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,caster.getEyePosition());
            }
            if(frame==32)shot(server,"late-tracking-channel",caster);
            return;
        }
        if(scene==41) {
            if(frame==0)ManipulationChannelManager.stop(caster,false);
            if(frame==18)shot(server,"channel-cleanup",caster);
            return;
        }
        if(scene<46) {
            // Pair of simultaneous animated players, then the same scene with reduced effects.
            var school=EnumBloodTendency.FERRIC;
            for(var p:server.getPlayerList().getPlayers()) {
                long now=p.level().getGameTime();
                if(frame==0 || frame==16)send(p,CastingAnimationPacket.harbinger(p.getId(),p.getUUID(),"review-pair",
                        20000+scene,school,null,EnumManipulationRank.PERFECTUS,CastPurpose.AREA,CastStyle.SCHOOL,
                        CastPhase.HOLD,now-frame,now,20,40,InteractionHand.MAIN_HAND));
            }
            if(frame==20)shot(server,"pair-ferric-"+(scene<44?"full":"minimal")+"-"+scene,caster);
            return;
        }
        if(scene<55) {
            String name=switch(scene) {
                case 46 -> "armor"; case 47 -> "occupied-hands"; case 48 -> "shield-priority";
                case 49 -> "left-handed"; case 50 -> "walking"; case 51 -> "crouching";
                case 52 -> "cancel"; case 53 -> "release"; default -> "replace-recovery";
            };
            if(frame==0) {
                caster.stopUsingItem();
                // Walking carries the caster off the 19-block stage within two scenes; start every later scene on the mark.
                if(scene>50)caster.teleportTo(caster.serverLevel(),.5,100,.5,0,0);
                if(scene==46) {
                    caster.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE));
                    caster.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET));
                }
                if(scene==47) {
                    caster.setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
                    caster.setItemInHand(InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE));
                }
                if(scene==48) {
                    caster.setItemInHand(InteractionHand.OFF_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHIELD));
                    caster.startUsingItem(InteractionHand.OFF_HAND);
                }
                if(scene==49)caster.getInventory().clearContent();
                for(var p:server.getPlayerList().getPlayers())p.sendSystemMessage(Component.literal("CAST_REVIEW_MODE "+name));
            }
            long now=caster.level().getGameTime();
            var phase=scene==52?CastPhase.CANCEL:scene==53?CastPhase.RELEASE:CastPhase.HOLD;
            if(frame==0 || frame==16 && phase.sustained())send(caster,CastingAnimationPacket.harbinger(caster.getId(),caster.getUUID(),"review-integration",
                    30000+scene,EnumBloodTendency.LUX,null,EnumManipulationRank.SUMMA,CastPurpose.AREA,CastStyle.SCHOOL,
                    phase,now-frame,now,20,40,InteractionHand.MAIN_HAND));
            if(frame==(scene==52?5:20))shot(server,name,caster);
            return;
        }
        for(var p:server.getPlayerList().getPlayers())p.sendSystemMessage(Component.literal("CAST_REVIEW_DONE"));
        Hemomancy.LOGGER.info("CAST_REVIEW complete: 40 profiles, {} bespoke styles, {} Still Art stages, release frames, late tracking, cleanup and simultaneous casters",
                STYLES.length,EnumClarityStage.values().length);
        server.halt(false);
    }

    /**
     * Cast impacts on the caster's body: every school with every delivery, blows travelling across the observer's view so
     * streaks read side-on, then one hit through the real {@code CastImpacts.landed} path. Shots at the flash, burst and fray.
     */
    private static void impacts(net.minecraft.server.MinecraftServer server,ServerPlayer caster,ServerPlayer observer,int age) {
        var deliveries=com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.Delivery.values();
        int views=deliveries.length+1,scene=age/20,frame=age%20,total=EnumBloodTendency.values().length*views;
        var level=caster.serverLevel();
        net.minecraft.world.phys.Vec3 at=caster.position().add(0,1.15,0);
        net.minecraft.world.phys.Vec3 view=caster.position().subtract(observer.position()).multiply(1,0,1).normalize();
        net.minecraft.world.phys.Vec3 across=new net.minecraft.world.phys.Vec3(-view.z,0,view.x);
        if(scene<total) {
            var school=EnumBloodTendency.values()[scene/views];
            // The last view per school is the attacker's: a melee blow travelling away from the observer.
            boolean attacker=scene%views==deliveries.length;
            var delivery=attacker?com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.Delivery.MELEE:deliveries[scene%views];
            var dir=attacker?view:delivery==com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.Delivery.TETHER?across.scale(-1):across;
            var secondary=school==EnumBloodTendency.ANIMUS?EnumBloodTendency.LUX:null;
            String name="impact-"+school.name().toLowerCase()+"-"+(attacker?"attacker":delivery.name().toLowerCase());
            // As the server does: on the struck side of the body, just outside it (areas sit at the body centre).
            var contact=delivery==com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.Delivery.AREA?at:at.subtract(dir.scale(.38));
            if(frame==0)ManipulationVisuals.burst(level,ManipulationVisuals.Form.CAST_IMPACT,contact,contact.add(dir),.84,
                    com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.LIFE,
                    com.vincenthuto.hemomancy.common.manipulation.impact.CastImpactRules.encode(school,secondary,delivery,1));
            if(frame==1)shot(server,name+"-flash",caster);
            if(frame==4)shot(server,name+"-burst",caster);
            if(frame==8)shot(server,name+"-fray",caster);
            return;
        }
        if(scene==total) {
            if(frame==0)com.vincenthuto.hemomancy.common.manipulation.impact.CastImpacts.landed(caster,
                    level.damageSources().playerAttack(observer),
                    com.vincenthuto.hemomancy.common.damage.SchoolHitContext.direct(Hemomancy.rloc("blood_shot"),EnumBloodTendency.FERRIC,null,observer),6);
            if(frame==4)shot(server,"impact-real-path",caster);
            return;
        }
        for(var p:server.getPlayerList().getPlayers())p.sendSystemMessage(Component.literal("CAST_REVIEW_DONE"));
        Hemomancy.LOGGER.info("CAST_REVIEW complete: impacts, {} scenes",total+1);
        server.halt(false);
    }

    /** Staff forms in WeaponConjureRules order; the ninth scene restores the staff itself. */
    private static final String[] ARSENAL={"conjure_blade","conjure_axe","conjure_spear","conjure_claws","conjure_crossbow",
            "conjure_torch","conjure_flail","conjure_sickle"};
    private static final String[] AVATARS={"summon_avatar","summon_avatar_arms","summon_avatar_armor","summon_avatar_legs","summon_avatar_complete"};

    /** Body-anchored cast visuals: rush, ward, mending, every conjured weapon, the cell hands and the avatar. */
    private static void basics(net.minecraft.server.MinecraftServer server,ServerPlayer caster,ServerPlayer observer,int age) {
        int scene=age/30,frame=age%30;
        var level=caster.serverLevel();
        if(age==0) {
            // Closer than the sweep's observer: these effects sit on the body and hand.
            observer.teleportTo(level,2.9,100,3.4,144,0);
            observer.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,caster.position().add(0,1.1,0));
        }
        if(frame==0) {
            caster.teleportTo(level,.5,100,.5,0,0);
            caster.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        }
        if(scene==0) {
            if(frame==0) { var m=ManipulationInit.blood_rush.get(); ready(caster,m); m.getAction(caster,level,caster.getMainHandItem(),caster.blockPosition());
                caster.hurtMarked=true; }
            for(int f:new int[]{2,5,9,14})if(frame==f)shot(server,"basics-rush-"+f,caster);
            return;
        }
        if(scene==1 || scene==2) {
            int pool=scene==1?6:3;
            if(frame==0)ManipulationVisuals.replay(caster,ManipulationVisuals.Form.WARD,1,25,pool);
            if(frame==3)shot(server,"basics-ward-"+pool+"-raise",caster);
            if(frame==15)shot(server,"basics-ward-"+pool+"-held",caster);
            if(frame==26)ManipulationVisuals.attached(caster,ManipulationVisuals.Form.WARD,0,0,0);
            return;
        }
        if(scene==3) {
            if(frame==0) { var pick=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE);pick.setDamageValue(200);
                caster.setItemInHand(InteractionHand.MAIN_HAND,pick);
                ManipulationVisuals.replay(caster,ManipulationVisuals.Form.MENDING,1,18,1); }
            for(int f:new int[]{3,8,13})if(frame==f)shot(server,"basics-mending-"+f,caster);
            if(frame==29)caster.getInventory().clearContent();
            return;
        }
        int weapons=com.vincenthuto.hemomancy.common.manipulation.ferric.WeaponConjureRules.WEAPONS;
        if(scene<4+weapons) {
            int weapon=scene-4;
            // The real Living Arsenal: the staff reshapes through the same helper the key press uses, so the morph,
            // the conjured item and the cue all come from gameplay.
            if(frame==0) {
                if(weapon==0) { caster.getInventory().clearContent();
                    caster.setItemInHand(InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(
                            com.vincenthuto.hemomancy.common.init.ItemInit.living_staff.get())); }
                if(weapon<ARSENAL.length) { var m=ManipulationInit.getByName(ARSENAL[weapon]); ready(caster,m);
                    com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper.applySelection(caster,m); }
                else com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper.restoreMainHand(caster);
            }
            for(int f:new int[]{3,9,13,17,22})if(frame==f)shot(server,"basics-weapon-"+weapon+"-"+f,caster);
            return;
        }
        int hands=4+weapons;
        if(scene<hands+4) {
            int mode=scene-hands;
            if(mode==0) {
                if(frame==0) { var m=ManipulationInit.blood_projection.get(); caster.getInventory().clearContent(); ready(caster,m);
                    m.getAction(caster,level,caster.getMainHandItem(),caster.blockPosition()); }
                for(int f:new int[]{2,6,10})if(frame==f)shot(server,"basics-hand-summon-"+f,caster);
            } else if(mode<3) {
                // Real use: the client holds right-click and sweeps its view, so the palm, the cell particles and the
                // geometry are all checked against the drawn first-person hand at several facings.
                String name=mode==1?"projecting":"absorbing";
                if(mode==2 && frame==0) { var m=ManipulationInit.blood_absorption.get(); caster.getInventory().clearContent(); ready(caster,m);
                    m.getAction(caster,level,caster.getMainHandItem(),caster.blockPosition()); }
                // Absorption needs a body to drain: a still pig in front of the caster.
                if(mode==2 && frame==1) {
                    var pig=net.minecraft.world.entity.EntityType.PIG.create(level);
                    if(pig!=null) { pig.moveTo(.5,100,3.2,180,0); pig.setNoAi(true); level.addFreshEntity(pig); drained=pig; }
                }
                if(frame==2)mode(server,"use-sweep");
                for(int f:new int[]{10,18,26})if(frame==f)shot(server,"basics-hand-"+name+"-"+f,caster);
                if(frame==29) { mode(server,"none"); if(drained!=null) { drained.discard(); drained=null; } }
            } else {
                if(frame==0) { com.vincenthuto.hemomancy.common.manipulation.ferric.ConjurationManip.dispelled(caster,caster.getMainHandItem());
                    caster.getInventory().clearContent(); }
                for(int f:new int[]{3,7})if(frame==f)shot(server,"basics-hand-dispel-"+f,caster);
            }
            return;
        }
        int avatars=hands+4;
        if(scene<avatars+AVATARS.length) {
            int stage=scene-avatars;
            if(frame==0) { var m=(com.vincenthuto.hemomancy.common.manipulation.animus.SummonAvatarManip)ManipulationInit.getByName(AVATARS[stage]);
                ready(caster,m);
                var known=HemoCapabilityAccess.getKnownManipulations(caster);
                var forms=new java.util.LinkedHashSet<>(known.getGrandfatheredFamilyForms());forms.add(m.getName());
                known.setGrandfatheredFamilyForms(forms);
                com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestationManager.toggle(caster,m); }
            for(int f:new int[]{4,10,16})if(frame==f)shot(server,"basics-avatar-"+stage+"-"+f,caster);
            return;
        }
        if(scene==avatars+AVATARS.length) {
            if(frame==0)com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestationManager.dismiss(caster);
            for(int f:new int[]{3,9,15})if(frame==f)shot(server,"basics-avatar-dismiss-"+f,caster);
            return;
        }
        for(var p:server.getPlayerList().getPlayers())p.sendSystemMessage(Component.literal("CAST_REVIEW_DONE"));
        Hemomancy.LOGGER.info("CAST_REVIEW complete: basics, {} scenes",scene);
        server.halt(false);
    }

    /** Every non-school style, each played through the registered manipulation that owns it. */
    private static final CastStyle[] STYLES=java.util.Arrays.stream(CastStyle.values()).filter(s -> s!=CastStyle.SCHOOL).toArray(CastStyle[]::new);
    private static final String[] STYLE_IDS={"ironhearted","umbral_step","umbral_reversal","summon_avatar_complete","conjure_staff",
            "crimson_tithe","unclosing_eye","bloom_of_rot","endless_hour",
            "crimson_coronation","synaptic_storm","white_verdict","funeral_bell","absolute_stillness","lodestone_collapse","eclipse_well","phoenix_debt",
            "hematic_riposte","blood_rush","deadly_gaze","sanguine_marionette",
            "lignum_mortis","vascular_dowsing","furnace_veins","living_circuit","iron_choir","carrion_communion","penumbral_drift","radiant_wake",
            "blackhearted","sovereign_instinct"};
    private static final StillArtEnding[] STILL_ENDINGS={StillArtEnding.REBUKE,StillArtEnding.HUSH,StillArtEnding.SIGHT,StillArtEnding.SHEAR,StillArtEnding.INTERCESSION};
    private static final String[] STILL_ARTS={"silver_rebuke","lethean_mute","pale_diagnosis","memory_shear","pale_intercession"};
    private static final int BESPOKE_SCENES=STYLE_IDS.length+STILL_ENDINGS.length;

    // Sequences stay above the school sweep (-1000..-961) and below the integration scenes: the client guard drops lower sequences per player.
    private static void bespoke(net.minecraft.server.MinecraftServer server,ServerPlayer caster,int index,int frame) {
        long now=caster.level().getGameTime();
        if(index>=STYLES.length) {
            int i=index-STYLES.length;
            var stage=EnumClarityStage.values()[i];
            String name="still-"+stage.name().toLowerCase();
            if(frame==0)send(caster,CastingAnimationPacket.stillArt(caster.getId(),caster.getUUID(),STILL_ARTS[i],-900+i,
                    stage,STILL_ENDINGS[i],CastPhase.RELEASE,now,now,InteractionHand.MAIN_HAND));
            if(frame==4)shot(server,name+"-release",caster);
            if(frame==16)shot(server,name+"-settle",caster);
            return;
        }
        var style=STYLES[index];
        String id=STYLE_IDS[index];
        var m=ManipulationInit.getByName(id);
        if(m==null)throw new IllegalStateException("CAST_REVIEW style manipulation missing: "+id);
        if(com.vincenthuto.hemomancy.common.manipulation.animation.CastPresentation.forId(id).style()!=style)
            throw new IllegalStateException("CAST_REVIEW "+id+" does not resolve "+style);
        var school=m.getTend();var rank=m.getRank();
        String name="style-"+style.name().toLowerCase();
        long sequence=-950+index;
        if(m.getType()==EnumManipulationType.PASSIVE) {
            if(frame==0)send(caster,CastingAnimationPacket.harbinger(caster.getId(),caster.getUUID(),id,sequence,school,null,rank,
                    CastPurpose.AREA,style,CastPhase.RELEASE,now,now,0,0,InteractionHand.MAIN_HAND));
            if(frame==20)send(caster,CastingAnimationPacket.harbinger(caster.getId(),caster.getUUID(),id,sequence,school,null,rank,
                    CastPurpose.AREA,style,CastPhase.CANCEL,now,now,0,0,InteractionHand.MAIN_HAND));
            if(frame==8)shot(server,name+"-on",caster);
            if(frame==27)shot(server,name+"-off",caster);
            return;
        }
        boolean charged=m.getType()==EnumManipulationType.CHARGED;
        if(frame==0)started=now;
        if(frame==0 || frame==16)send(caster,CastingAnimationPacket.harbinger(caster.getId(),caster.getUUID(),id,sequence,school,null,rank,
                CastPurpose.AREA,style,frame==0?CastPhase.OPENING:CastPhase.HOLD,started,now,charged?(frame==0?20:40):0,charged?40:0,InteractionHand.MAIN_HAND));
        if(frame==10)shot(server,name+"-hold",caster);
        if(frame==20)shot(server,name+"-held",caster);
        if(frame==22)send(caster,CastingAnimationPacket.harbinger(caster.getId(),caster.getUUID(),id,sequence,school,null,rank,
                CastPurpose.AREA,style,CastPhase.RELEASE,now,now,charged?40:0,charged?40:0,InteractionHand.MAIN_HAND));
        if(frame==25)shot(server,name+"-release",caster);
    }
    private static void ready(ServerPlayer p,BloodManipulation m) {
        var volume=HemoCapabilityAccess.getBloodVolume(p);volume.setActive(true);volume.fill(5000);
        var known=HemoCapabilityAccess.getKnownManipulations(p);known.getKnownManips().put(m,new ManipLevel(0,0));
        known.setSelectedManip(m);known.setEquippedManipNames(java.util.List.of(m.getName()));
        HemoCapabilityAccess.getBloodTendency(p).setTendencyAlignment(m.getTend(),100);
        // Items such as the cell hands check the client's copy of the volume before they start being used.
        com.vincenthuto.hemomancy.common.network.PacketHandler.sendToPlayer(p,
                new com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket(volume));
    }
    private static void send(ServerPlayer p,CastingAnimationPacket packet) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,packet);
    }
    private static void mode(net.minecraft.server.MinecraftServer server,String mode) {
        for(var p:server.getPlayerList().getPlayers())p.sendSystemMessage(Component.literal("CAST_REVIEW_MODE "+mode));
    }
    private static void shot(net.minecraft.server.MinecraftServer server,String name,ServerPlayer caster) {
        for(var p:server.getPlayerList().getPlayers())p.sendSystemMessage(Component.literal("CAST_REVIEW_SHOT "+name+" "+caster.getId()));
    }

    @EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
    public static final class Client {
        private static long previousFrame;
        private static double sum,max;
        private static int frames;
        private static boolean reduced;
        private static String mode="";
        @SubscribeEvent public static void frame(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
            if(!Boolean.getBoolean("hemomancy.castingReviewClient") || event.getStage()!=net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL)return;
            long now=System.nanoTime();
            if(previousFrame!=0) {
                double ms=(now-previousFrame)/1_000_000.0;
                sum+=ms;max=Math.max(max,ms);frames++;
            }
            previousFrame=now;
        }
        @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
            if(!Boolean.getBoolean("hemomancy.castingReviewClient"))return;
            var mc=net.minecraft.client.Minecraft.getInstance();
            mc.options.pauseOnLostFocus=false;
            mc.options.framerateLimit().set(120);
            mc.options.renderDistance().set(5);
            mc.options.particles().set(reduced?net.minecraft.client.ParticleStatus.MINIMAL:net.minecraft.client.ParticleStatus.ALL);
            mc.options.hideGui=false;
            if(mc.player!=null && mc.getUser().getName().startsWith("CastCaster")) {
                mc.options.mainHand().set(mode.equals("left-handed")?net.minecraft.world.entity.HumanoidArm.LEFT:net.minecraft.world.entity.HumanoidArm.RIGHT);
                mc.options.keyUp.setDown(mode.equals("walking"));
                mc.options.keyShift.setDown(mode.equals("crouching"));
                mc.options.keyUse.setDown(mode.equals("shield-priority") || mode.equals("use-sweep"));
                if(mode.equals("use-sweep")) {
                    // Turn and tilt the view while the hand is in use, to check it stays on the drawn palm.
                    double t=mc.player.tickCount;
                    mc.player.setYRot((float)(30*Math.sin(t*.15)));mc.player.setXRot((float)(28+14*Math.sin(t*.1)));
                    mc.player.setYHeadRot(mc.player.getYRot());
                }
            }
            mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        }
        @SubscribeEvent public static void chat(net.neoforged.neoforge.client.event.ClientChatReceivedEvent event) {
            if(!Boolean.getBoolean("hemomancy.castingReviewClient"))return;
            String text=event.getMessage().getString();
            var mc=net.minecraft.client.Minecraft.getInstance();
            if(text.equals("CAST_REVIEW_DONE")) { event.setCanceled(true);mc.stop();return; }
            if(text.startsWith("CAST_REVIEW_MODE ")) {
                mode=text.substring("CAST_REVIEW_MODE ".length()); reduced=false;
                if(mode.equals("left-handed")) { mc.options.mainHand().set(net.minecraft.world.entity.HumanoidArm.LEFT);mc.options.broadcastOptions(); }
                event.setCanceled(true);return;
            }
            if(!text.startsWith("CAST_REVIEW_SHOT ") || mc.level==null)return;
            String[] parts=text.split(" ");
            if(parts[1].equals("pair-ferric-full-43"))reduced=true;
            var entity=mc.level.getEntity(Integer.parseInt(parts[2]));
            var animation=entity instanceof net.minecraft.world.entity.player.Player p
                    ?com.vincenthuto.hemomancy.client.player.CastingAnimationClientState.animation(p):null;
            if(parts[1].equals("baseline")) {
                for(var p:mc.level.players())Hemomancy.LOGGER.info("CAST_REVIEW_MODEL {} {}",p.getName().getString(),p.getSkin().model());
            }
            Hemomancy.LOGGER.info("CAST_REVIEW_FRAME {} {} state={} age={} frames={} meanMs={} maxMs={}",
                    mc.getUser().getName(),parts[1],animation==null?"none":animation.packet().phase(),
                    animation==null?0:animation.playback.elapsed(mc.level.getGameTime(),0),
                    frames,frames==0?0:sum/frames,max);
            if(parts[1].startsWith("basics-hand") && mc.player!=null) {
                var camera=mc.gameRenderer.getMainCamera();
                Hemomancy.LOGGER.info("CAST_REVIEW_PALM {} {} world={} camera={} look={} using={}",parts[1],
                        com.vincenthuto.hemomancy.client.render.item.harbinger.FirstPersonPalm.describe(),
                        com.vincenthuto.hemomancy.client.render.item.harbinger.FirstPersonPalm.world(mc.player.getMainArm()),
                        camera.getPosition(),new net.minecraft.world.phys.Vec3(camera.getLookVector()),mc.player.isUsingItem());
            }
            net.minecraft.client.Screenshot.grab(mc.gameDirectory,"casting-"+parts[1]+".png",mc.getMainRenderTarget(),ignored -> {});
            frames=0;sum=0;max=0;previousFrame=0;
            event.setCanceled(true);
        }
    }
}
