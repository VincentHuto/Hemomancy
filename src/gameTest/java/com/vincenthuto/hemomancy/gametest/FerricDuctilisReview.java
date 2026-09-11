package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.*;

/** Real paid casts in a disposable world, with repeatable views for before/after review. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID, value=Dist.CLIENT)
public final class FerricDuctilisReview {
    private static final String[] SPELLS={"ferric_rampart","ferric_spikes","sanguine_magnetism","ironhearted","iron_choir","iron_retort",
            "sanguine_mending","vascular_dowsing","conjure_staff","synaptic_jolt","activation_potential","synaptic_storm",
            "conductive_mark","sanguine_ward","living_circuit","hemolymphal_pulse","thread_ripper","synaptic_jolt",
            "synaptic_jolt","activation_potential","conductive_mark","hemolymphal_pulse","iron_retort","sanguine_magnetism",
            "blood_absorption","blood_projection","conjure_blade","conjure_axe","conjure_spear","conjure_crossbow","conjure_sickle","synaptic_storm"};
    private static final List<Integer> TARGETS=new ArrayList<>();
    private static int ticks=Integer.getInteger("hemomancy.ferricReviewStart",0)*160,frames;
    private static long lastFrame;
    private static final List<Double> intervals=new ArrayList<>();
    private static final List<String> timingRows=new ArrayList<>();
    private static boolean published;
    private static volatile boolean failed;
    private static net.minecraft.world.phys.Vec3 lockedPosition;
    private static int warmup;
    private static int npcCaster=-1;
    private static boolean started,respawnRequested;
    private FerricDuctilisReview() {}

    @SubscribeEvent public static void frame(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
        if(!Boolean.getBoolean("hemomancy.ferricReview") || event.getStage()!=net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL)return;
        long now=System.nanoTime();int phase=ticks%160;
        if(lastFrame!=0 && phase>=30 && phase<=140) {
            double ms=(now-lastFrame)/1e6;intervals.add(ms);
            timingRows.add(ticks/160+","+phase+","+ms);
        }
        lastFrame=now;
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.ferricReview"))return;
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.level==null || mc.getSingleplayerServer()==null)return;
        if(!mc.getSingleplayerServer().getWorldData().getLevelName().equals("VisceralReview"))throw new IllegalStateException("Disposable review world required");
        if(mc.player.isDeadOrDying()) {
            if(!started) {
                if(!respawnRequested){mc.player.respawn();respawnRequested=true;}
                return;
            }
            if(!failed)org.slf4j.LoggerFactory.getLogger(FerricDuctilisReview.class).error("Review caster died during scene {}",ticks/160);
            failed=true;
        }
        mc.options.pauseOnLostFocus=false;mc.options.hideGui=true;if(mc.screen!=null)mc.setScreen(null);
        if(Boolean.getBoolean("hemomancy.ferricMultiplayer")) {
            if(!published) {
                mc.getSingleplayerServer().setUsesAuthentication(false);
                published=mc.getSingleplayerServer().publishServer(GameType.CREATIVE,true,25579);
                org.slf4j.LoggerFactory.getLogger(FerricDuctilisReview.class).info("FERRIC OBSERVER READY {}",published);
            }
            if(mc.getSingleplayerServer().getPlayerList().getPlayerCount()<2)return;
        }
        int modes=Integer.getInteger("hemomancy.ferricReviewModes",1);
        int tick=ticks++,scene=tick/160,phase=tick%160;
        int index=scene<SPELLS.length?scene:(scene-SPELLS.length)%18;
        int mode=scene<SPELLS.length?0:1+(scene-SPELLS.length)/18;
        String label=System.getProperty("hemomancy.ferricReviewLabel","after");
        double x=12000+scene*40+.5;
        if(phase==0)warmup=0;
        if(phase==1) {
            int minimum=scene==Integer.getInteger("hemomancy.ferricReviewStart",0)?40:8;
            boolean ready=Math.abs(mc.player.getX()-x)<1;
            // Only require sections inside the forward view; offscreen sections may never compile.
            for(int dx:new int[]{-4,4})for(int z:new int[]{4,12})
                ready&=mc.levelRenderer.isSectionCompiled(new BlockPos((int)x+dx,99,z));
            if(++warmup<minimum || !ready) {
                ticks--;
                if(warmup>200){
                    failed=true;
                    var log=org.slf4j.LoggerFactory.getLogger(FerricDuctilisReview.class);
                    log.error("Review terrain did not finish loading for scene {}: expected x={}, player={}",scene,x,mc.player.position());
                    for(int dx:new int[]{-4,4})for(int z:new int[]{4,12}) {
                        var pos=new BlockPos((int)x+dx,99,z);
                        log.error("Review terrain {}: block={}, compiled={}",pos,mc.level.getBlockState(pos),mc.levelRenderer.isSectionCompiled(pos));
                    }
                    Screenshot.grab(mc.gameDirectory,"ferric-"+label+"-terrain-failure.png",mc.getMainRenderTarget(),message->{});
                }
                else return;
            }
        }
        if(failed || mode>=modes || scene>=Integer.getInteger("hemomancy.ferricReviewEnd",Integer.MAX_VALUE)) {
            try {
                int first=Integer.getInteger("hemomancy.ferricReviewStart",0);
                var file=mc.gameDirectory.toPath().resolve("ferric-"+label+"-frame-times"+(first==0?"":"-part"+first)+".csv");
                timingRows.addFirst("scene,phase,frame_ms");java.nio.file.Files.write(file,timingRows);
            } catch(java.io.IOException failure){throw new RuntimeException(failure);}
            Collections.sort(intervals);
            org.slf4j.LoggerFactory.getLogger(FerricDuctilisReview.class).info("FERRIC REVIEW {} frames={} meanMs={} p95Ms={}",failed?"FAILED":"COMPLETE",frames,
                    intervals.stream().mapToDouble(Double::doubleValue).average().orElse(0),intervals.isEmpty()?0:intervals.get((int)(intervals.size()*.95)));
            mc.stop();return;
        }
        String name=SPELLS[index];
        if(index==31) {
            boolean lockedAttempt=phase>=28&&phase<40,recoveredAttempt=phase>=49&&phase<60;
            mc.options.keyUp.setDown(lockedAttempt||recoveredAttempt);
            mc.options.keyJump.setDown(lockedAttempt);mc.options.keyAttack.setDown(lockedAttempt);mc.options.keyUse.setDown(lockedAttempt);
            if(lockedAttempt)mc.player.setYRot(mc.player.getYRot()+3);
        }
        if(phase==0) {
            started=true;
            mc.options.setCameraType(index==10 || index==11 || mode==2?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_BACK);
            mc.options.particles().set(mode==2?ParticleStatus.MINIMAL:ParticleStatus.ALL);
            if(scene==SPELLS.length)mc.reloadResourcePacks();
            mc.getSingleplayerServer().execute(()->{
                var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
                var level=player.serverLevel();
                for(int id:TARGETS){var old=level.getEntity(id);if(old!=null)old.discard();}TARGETS.clear();
                // The named disposable world may contain actors saved by a previous completed review.
                for(var old:level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new net.minecraft.world.phys.AABB(x-12,96,-10,x+12,110,40)))old.discard();
                ManipulationChannelManager.stop(player);BloodManipulation.clearSessionState();player.removeAllEffects();
                player.setHealth(player.getMaxHealth());player.getFoodData().setFoodLevel(20);npcCaster=-1;
                player.setGameMode(GameType.CREATIVE);player.getInventory().clearContent();player.teleportTo(level,x,100,0,0,index<=2?25:8);
                var state=HemoCapabilityAccess.getPowerGuardrails(player);state.setIronHeartHealth(0);state.setIronHeartExpiryTick(0);
                level.setDayTime(mode==1?18000:6000);level.setWeatherParameters(6000,0,false,false);
                for(int dx=-8;dx<=8;dx++)for(int dz=-6;dz<=16;dz++)level.setBlockAndUpdate(new BlockPos((int)x+dx,99,dz),Blocks.STONE.defaultBlockState());
                for(int dx=-4;dx<=4;dx++)for(int y=100;y<=104;y++)level.setBlockAndUpdate(new BlockPos((int)x+dx,y,14),dx<0?Blocks.GLASS.defaultBlockState():Blocks.DEEPSLATE.defaultBlockState());
                for(int z=2;z<=10;z++)level.setBlockAndUpdate(new BlockPos((int)x+4,99,z),Blocks.COPPER_BLOCK.defaultBlockState());
                if(index==17)for(int dx=-2;dx<=2;dx++)for(int z=2;z<=9;z++)level.setBlockAndUpdate(new BlockPos((int)x+dx,99,z),Blocks.WATER.defaultBlockState());
                for(int i=0;i<(mode==2?20:5);i++) {
                    var target=EntityType.HUSK.create(level);target.setPos(x+(i%3-1)*2,index==17?99.3:100,4+i*1.6);target.setNoAi(true);
                    target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);target.setHealth(200);level.addFreshEntity(target);TARGETS.add(target.getId());
                }
                var blood=HemoCapabilityAccess.requireBloodVolume(player);blood.setActive(true);blood.setBloodVolume(10000);
                for(var tend:EnumBloodTendency.values())HemoCapabilityAccess.getBloodTendency(player).orElseThrow().setTendencyAlignment(tend,100);
                var spell=ManipulationInit.getByName(name);var known=HemoCapabilityAccess.requireKnownManipulations(player);
                var baseline=ManipulationInit.getByName(com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry.baselineId(name));
                known.getKnownManips().put(baseline,new ManipLevel(4,185));
                known.getKnownManips().put(spell,new ManipLevel(4,185));known.setSelectedManip(spell);known.setEquippedManipNames(List.of(name));
                if(name.equals("sanguine_mending")){var sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(180);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,sword);}
                if(index>=26&&index<31)player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ItemInit.living_staff.get()));
                if(name.equals("vascular_dowsing"))level.setBlockAndUpdate(new BlockPos((int)x,98,5),Blocks.IRON_ORE.defaultBlockState());
                var team=level.getScoreboard().getPlayerTeam("ferric_review");
                if(team==null)team=level.getScoreboard().addPlayerTeam("ferric_review");
                level.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);
                for(var observer:level.getServer().getPlayerList().getPlayers())if(observer!=player) {
                    placeObserver(observer,level,x,index,mode==1&&index!=14);
                    level.getScoreboard().addPlayerToTeam(observer.getScoreboardName(),team);
                }
            });
        }
        if(phase>2 && phase<22 && phase%3==0)mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            var spell=ManipulationInit.getByName(name);var form=ManipulationVisuals.chargeForm(name);
            if(form!=null)ManipulationVisuals.attached(player,form,(phase-2)/20.0,7,1);
        });
        if(phase==50 && mode==1 && index!=14)mc.getSingleplayerServer().execute(()-> {
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            for(var observer:player.serverLevel().getServer().getPlayerList().getPlayers())if(observer!=player)
                placeObserver(observer,player.serverLevel(),x,index,false);
        });
        if(phase==35 && mode>0 && index==0)mc.getSingleplayerServer().execute(()-> {
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            for(int dx=-1;dx<=1;dx++)for(int y=100;y<103;y++)
                player.serverLevel().setBlockAndUpdate(new BlockPos((int)x+dx,y,2),Blocks.CYAN_STAINED_GLASS.defaultBlockState());
            var at=new net.minecraft.world.phys.Vec3(x,100.5,3.5);
            ManipulationVisuals.burst(player.serverLevel(),ManipulationVisuals.Form.LUX_MIST,at.add(-1,0,0),at,1.5,48);
            ManipulationVisuals.burst(player.serverLevel(),ManipulationVisuals.Form.UMBRA_MIST,at.add(1,0,0),at,1.5,48);
        });
        if(phase==42 && mode==2)mc.getSingleplayerServer().execute(()-> {
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            for(int i=0;i<TARGETS.size();i++) {
                var mob=player.serverLevel().getEntity(TARGETS.get(i));
                var target=player.serverLevel().getEntity(TARGETS.get((i+1)%TARGETS.size()));
                if(mob instanceof net.minecraft.world.entity.Mob living && target instanceof net.minecraft.world.entity.LivingEntity enemy) {
                    living.setNoAi(false);living.setTarget(enemy);
                }
            }
        });
        if(phase==24)mc.getSingleplayerServer().execute(()->{
            try {
                ServerPlayer player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
                var spell=ManipulationInit.getByName(name);double before=HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume();
                boolean cast;
                if(index==31) {
                    var choir=ManipulationInit.iron_choir.get();
                    var known=HemoCapabilityAccess.requireKnownManipulations(player);
                    known.getKnownManips().put(choir,new ManipLevel(4,185));known.setEquippedManipNames(List.of("iron_choir"));known.setSelectedManip(choir);
                    ManipulationChannelManager.start(player);
                    if(!ManipulationChannelManager.isChanneling(player.getUUID()))throw new IllegalStateException("Paralysis fixture did not start its channel");
                    player.setGameMode(GameType.SURVIVAL);player.getAbilities().flying=false;player.onUpdateAbilities();
                    lockedPosition=player.position();
                    cast=com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.apply(player,60);
                    if(ManipulationChannelManager.isChanneling(player.getUUID()))throw new IllegalStateException("Paralysis did not interrupt the active channel");
                } else if(index>=18&&index<24) {
                    var caster=EntityType.HUSK.create(player.serverLevel());caster.setPos(player.position().add(-2,0,1));caster.setNoAi(true);
                    player.serverLevel().addFreshEntity(caster);TARGETS.add(caster.getId());npcCaster=caster.getId();
                    var target=(net.minecraft.world.entity.LivingEntity)player.serverLevel().getEntity(TARGETS.get(1));caster.setTarget(target);
                    cast=EntityManipulationEffects.cast(spell,ManipulationCastContext.forWill(caster,target,ItemStack.EMPTY));
                } else if(name.equals("thread_ripper")) {
                    var puppet=EntityInit.veinwing_vulture.get().create(player.serverLevel());
                    puppet.hemomancy$setOwnerUUID(UUID.randomUUID());
                    puppet.setPos(player.getEyePosition().add(player.getLookAngle().scale(4)).add(0,-.3,0));
                    player.serverLevel().addFreshEntity(puppet);TARGETS.add(puppet.getId());
                    cast=spell.tryPerformAction(player,level(player),player.getMainHandItem(),player.blockPosition(),spell.getRequiredChargeTicks());
                    if(puppet.getHealth()==puppet.getMaxHealth()&&!puppet.isRemoved())throw new IllegalStateException("Thread Ripper did not sever its puppet");
                } else if(spell.getType()==EnumManipulationType.CONTINUOUS){ManipulationChannelManager.start(player);cast=ManipulationChannelManager.isChanneling(player.getUUID());}
                else cast=spell.tryPerformAction(player,level(player),player.getMainHandItem(),player.blockPosition(),spell.getRequiredChargeTicks());
                if(index>=26&&index<31&&!name.equals(com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper.currentFormName(player.getMainHandItem())))
                    throw new IllegalStateException("Staff did not transform into "+name);
                org.slf4j.LoggerFactory.getLogger(FerricDuctilisReview.class).info("FERRIC CAST {} accepted={} blood {} -> {}",name,cast,before,HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume());
                if(!cast)failed=true;
            } catch(Throwable failure){failed=true;org.slf4j.LoggerFactory.getLogger(FerricDuctilisReview.class).error("Ferric cast review",failure);}
        });
        if(phase==40)mc.getSingleplayerServer().execute(()-> {
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            if(index==31) {
                double horizontal=player.position().subtract(lockedPosition).horizontalDistance();
                boolean locked=horizontal<.15&&player.getY()<=lockedPosition.y+.1&&Math.abs(player.getYRot())>5
                        &&!player.isUsingItem()&&player.hasEffect(EffectInit.paralysis);
                org.slf4j.LoggerFactory.getLogger(FerricDuctilisReview.class).info("FERRIC PLAYER LOCK passed={} horizontal={} yaw={}",locked,horizontal,player.getYRot());
                if(!locked)failed=true;
            }
            if(name.equals("iron_choir"))for(int i=0;i<4;i++) {
                var arrow=EntityType.ARROW.create(player.serverLevel());var enemy=player.serverLevel().getEntity(TARGETS.getFirst());
                arrow.setOwner(enemy);arrow.setPos(player.position().add(i*.2,1,3));arrow.setDeltaMovement(0,0,-.6);player.serverLevel().addFreshEntity(arrow);
            }
            if(index==22) {
                var caster=player.serverLevel().getEntity(npcCaster);
                var enemy=player.serverLevel().getEntity(TARGETS.getFirst());
                if(caster instanceof net.minecraft.world.entity.LivingEntity living)
                    living.hurt(player.damageSources().mobAttack((net.minecraft.world.entity.LivingEntity)enemy),4);
            } else if(name.equals("iron_retort")||name.equals("sanguine_ward")||name.equals("ironhearted")) {
                player.setGameMode(GameType.SURVIVAL);
                var enemy=player.serverLevel().getEntity(TARGETS.getFirst());
                player.hurt(player.damageSources().mobAttack((net.minecraft.world.entity.LivingEntity)enemy),4);
                player.setGameMode(GameType.CREATIVE);
            }
            if(name.equals("sanguine_magnetism")) {
                var jolt=ManipulationInit.synaptic_jolt.get();
                jolt.tryPerformAction(player,player.serverLevel(),ItemStack.EMPTY,player.blockPosition(),0);
            }
            for(var observer:player.serverLevel().getServer().getPlayerList().getPlayers())if(observer!=player&&name.equals("living_circuit"))
                observer.teleportTo(player.serverLevel(),x+4,100,3,0,8);
        });
        if(phase==64&&index==31)mc.getSingleplayerServer().execute(()-> {
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            boolean recovered=!player.hasEffect(EffectInit.paralysis)&&player.position().subtract(lockedPosition).horizontalDistance()>.3;
            org.slf4j.LoggerFactory.getLogger(FerricDuctilisReview.class).info("FERRIC PLAYER RECOVERY passed={}",recovered);
            if(!recovered)failed=true;
        });
        if(phase==58&&name.equals("living_circuit"))mc.getSingleplayerServer().execute(()-> {
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player==null)return;
            for(var observer:player.serverLevel().getServer().getPlayerList().getPlayers())if(observer!=player) {
                var target=player.serverLevel().getEntity(TARGETS.getFirst());observer.attack(target);
            }
        });
        if(phase==75)mc.getSingleplayerServer().execute(()->{
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());if(player!=null)ManipulationChannelManager.stop(player);
            for(int id:TARGETS){var target=mc.getSingleplayerServer().overworld().getEntity(id);if(target!=null)target.setDeltaMovement(.04,0,0);}
        });
        if(phase>=4 && phase%4==0) {
            Screenshot.grab(mc.gameDirectory,String.format(Locale.ROOT,"ferric-%s-%02d-%03d.png",label,scene,phase),mc.getMainRenderTarget(),message->{});frames++;
        }
    }
    private static void placeObserver(ServerPlayer observer,net.minecraft.server.level.ServerLevel level,double x,int index,boolean away) {
        boolean self=index>=3&&index<=8 || index==13 || index==14 || index>=24;
        observer.setGameMode(GameType.CREATIVE);observer.getAbilities().flying=true;observer.onUpdateAbilities();
        observer.teleportTo(level,x+(away?150:self?4:7),100,away||!self?-1:4,away||!self?35:135,18);
    }
    private static net.minecraft.server.level.ServerLevel level(ServerPlayer player){return player.serverLevel();}
}
