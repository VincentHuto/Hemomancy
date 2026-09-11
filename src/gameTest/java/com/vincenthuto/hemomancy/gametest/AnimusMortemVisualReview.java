package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.client.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.List;

/** Actual casts in a separate, disposable copy of the review world. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class AnimusMortemVisualReview {
    private static final String[] SPELLS={"blood_shot","guided_blood_shot","hematic_mortar","sanguine_halo",
            "blood_needle","blood_needle_fan","blood_needle_lance","blood_binding","lingering_blood_binding",
            "chain_blood_binding","blood_lattice","blood_cloud","expansive_blood_cloud","pursuing_blood_cloud",
            "sanguine_tempest","blood_rush","vital_effusion","blood_aneurysm","hematic_rebuke","hematic_impressment",
            "crimson_coronation","sovereign_instinct","summon_avatar","summon_avatar_arms","summon_avatar_armor",
            "summon_avatar_legs","summon_avatar_complete","conjure_blade","hemorrhage","lignum_mortis",
            "canopy_mortis","worked_lignum","insatiable_hunger","grave_debt","exsanguinate","carrion_communion",
            "blackhearted","funeral_bell","crimson_tithe","bloom_of_rot","conjure_axe","conjure_sickle"};
    private static final String[] NPC_SPELLS={"blood_shot","blood_needle","blood_aneurysm","blood_cloud","hemorrhage",
            "exsanguinate","insatiable_hunger","grave_debt","bloom_of_rot"};
    private static int ticks,frame,warmup;
    private static volatile int npcId=-1;
    private static boolean published,started;
    private static net.minecraft.world.entity.Entity reviewCamera;
    private static volatile int targetId=-1;
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(AnimusMortemVisualReview.class);
    private static final java.util.List<Double> intervals=new java.util.ArrayList<>();
    private static long lastFrame;
    private AnimusMortemVisualReview() {}

    @SubscribeEvent public static void frame(net.neoforged.neoforge.client.event.RenderLevelStageEvent event) {
        if(!Boolean.getBoolean("hemomancy.animusMortemReview") || event.getStage()!=net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL)return;
        long now=System.nanoTime();
        if(lastFrame!=0 && ticks%100>=20 && ticks%100<85)intervals.add((now-lastFrame)/1e6);
        lastFrame=now;
    }

    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if(!Boolean.getBoolean("hemomancy.animusMortemReview"))return;
        var mc=Minecraft.getInstance();
        if(mc.level==null || mc.player==null || mc.getSingleplayerServer()==null)return;
        if(!mc.gameDirectory.toPath().toAbsolutePath().normalize().endsWith("animus-mortem-client")
                || !mc.getSingleplayerServer().getWorldData().getLevelName().equals("VisceralReview"))
            throw new IllegalStateException("Animus/Mortem review requires its disposable review directory");
        mc.options.pauseOnLostFocus=false;mc.options.hideGui=true;if(mc.screen!=null)mc.setScreen(null);
        if(warmup++<100)return;
        if(Boolean.getBoolean("hemomancy.animusMortemMultiplayer") && !started) {
            if(!published) {
                mc.getSingleplayerServer().setUsesAuthentication(false);
                published=mc.getSingleplayerServer().publishServer(GameType.CREATIVE,true,25579);
                if(!published)throw new IllegalStateException("Cannot publish disposable blood review");
                LOG.info("ANIMUS_MORTEM_SERVER_READY port=25579");
            }
            if(mc.getSingleplayerServer().getPlayerList().getPlayerCount()<2)return;
            started=true;
        }
        boolean npc=Boolean.getBoolean("hemomancy.animusMortemNpc");
        String[] roster=npc?NPC_SPELLS:SPELLS;
        int tick=ticks++,scene=tick/100,phase=tick%100,mode=scene/roster.length;
        if(mode>=Integer.getInteger("hemomancy.animusMortemReviewModes",3)) {
            intervals.sort(Double::compare);
            LOG.info("ANIMUS_MORTEM_REVIEW COMPLETE frames={} samples={} mean={}ms p95={}ms benchmark={}",frame,intervals.size(),intervals.stream().mapToDouble(Double::doubleValue).average().orElse(0),
                    intervals.isEmpty()?0:intervals.get((int)(intervals.size()*.95)),Boolean.getBoolean("hemomancy.animusMortemBenchmark"));
            mc.stop();return;
        }
        int start=Integer.getInteger("hemomancy.animusMortemStart",0),end=Integer.getInteger("hemomancy.animusMortemEnd",roster.length);
        if(scene%roster.length<start){ticks=(mode*roster.length+start)*100;return;}
        if(scene%roster.length>=end){ticks=((mode+1)*roster.length+start)*100;return;}
        String name=roster[scene%roster.length];double x=16000+scene*36+.5;
        if(phase==0) {
            mc.options.particles().set(mode==2?ParticleStatus.MINIMAL:ParticleStatus.ALL);
            mc.options.setCameraType(mode==1?CameraType.FIRST_PERSON:CameraType.THIRD_PERSON_BACK);
            if(scene==SPELLS.length)mc.reloadResourcePacks().thenRun(()->LOG.info("ANIMUS_MORTEM_REVIEW RESOURCE_RELOAD_COMPLETE"));
            mc.getSingleplayerServer().execute(()->{
                prepare(mc,name,x,mode);
                if(scene==0 && !System.getProperty("hemomancy.animusMortemReviewLabel","").startsWith("before"))
                    AnimusMortemVisualGameTests.verify(player(mc).serverLevel(),new Vec3(x+9,100,0));
            });
        }
        if(phase==3) {
            if(mode==1){mc.setCameraEntity(mc.player);mc.options.setCameraType(CameraType.FIRST_PERSON);}
            else {
                reviewCamera=EntityType.ARMOR_STAND.create(mc.level);
                boolean largeForm=name.startsWith("summon_avatar") || name.equals("crimson_coronation");
                reviewCamera.setPos(x+(largeForm?8:4),largeForm?103:100.7,largeForm?-12:-3);
                reviewCamera.setYRot(30);reviewCamera.setXRot(12);reviewCamera.setOldPosAndRot();
                mc.setCameraEntity(reviewCamera);mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
        }
        if(phase>=8 && phase<18)mc.getSingleplayerServer().execute(()->{
            var player=player(mc);if(player==null)return;
            var form=ManipulationVisuals.chargeForm(name);
            if(form!=null)PacketDistributor.sendToPlayer(player,new ManipulationVisualPacket(form,player.getId(),player.getEyePosition(),
                    player.getEyePosition().add(player.getLookAngle().scale(6)),(phase-7)/10f,3,1));
        });
        if(phase==18)mc.getSingleplayerServer().execute(()->{
            var player=player(mc);if(player==null)return;
            var spell=ManipulationInit.getByName(name);boolean cast;
            double before=HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume();
            if(npc && player.level().getEntity(npcId) instanceof LivingEntity caster && player.level().getEntity(targetId) instanceof LivingEntity target)
                cast=EntityManipulationEffects.cast(spell,ManipulationCastContext.forWill(caster,target,ItemStack.EMPTY));
            else if(spell instanceof com.vincenthuto.hemomancy.common.manipulation.animus.SummonAvatarManip avatar)
                cast=com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestationManager.toggle(player,avatar);
            else if(spell.getType()==EnumManipulationType.CONTINUOUS){ManipulationChannelManager.start(player);cast=ManipulationChannelManager.isChanneling(player.getUUID());}
            else if(spell.getType()==EnumManipulationType.PASSIVE){var known=HemoCapabilityAccess.requireKnownManipulations(player);if(!known.isPassiveActive(name))known.togglePassive(name);cast=true;}
            else cast=spell.tryPerformAction(player,player.level(),player.getMainHandItem(),player.blockPosition(),spell.getRequiredChargeTicks());
            LOG.info("ANIMUS_MORTEM_CAST scene={} spell={} success={} bloodBefore={} bloodAfter={}",scene,name,cast,before,HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume());
            if(name.equals("blackhearted"))player.hurt(player.damageSources().wither(),4);
            if(name.equals("crimson_coronation"))player.setYRot(0);
        });
        if(phase==38)mc.getSingleplayerServer().execute(()->{
            var player=player(mc);if(player==null)return;
            for(var observer:player.serverLevel().getServer().getPlayerList().getPlayers())if(observer!=player)
                observer.teleportTo(player.serverLevel(),x+4,100,-1,30,12);
        });
        if(phase==42 || phase==60)mc.getSingleplayerServer().execute(()->{
            var player=player(mc);if(player==null)return;
            if(player.level().getEntity(targetId) instanceof LivingEntity target) {
                if(name.equals("grave_debt")) {float before=target.getHealth();target.setHealth(phase==42?80:1);SchoolHitHelper.tryTriggerGraveDebtBurst(target,before);if(phase==60)target.hurt(player.damageSources().playerAttack(player),100);}
                if(name.equals("insatiable_hunger"))target.heal(8);
                if(name.equals("crimson_coronation"))player.attack(target);
                target.setYBodyRot(phase*3);target.setPos(target.getX()+.2,target.getY(),target.getZ());
            }
            if(name.equals("blackhearted")){player.setHealth(20);player.invulnerableTime=0;player.hurt(player.damageSources().wither(),18);
                LOG.info("ANIMUS_MORTEM_BLACKHEART saturation={}",HemoCapabilityAccess.getPowerGuardrails(player).getNecroticSaturation());}
            if(name.equals("crimson_tithe") && phase==42){
                if(mode!=1)com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve.drainToCover(player,250);
                player.getPersistentData().putLong("hemomancy:crimson_tithe_expiry",player.level().getGameTime());com.vincenthuto.hemomancy.common.manipulation.saint.CrimsonTitheManip.tickDebt(player);}
        });
        if(phase==70)mc.getSingleplayerServer().execute(()->{var player=player(mc);if(player!=null){ManipulationChannelManager.stop(player);com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestationManager.dismiss(player);if(player.level().getEntity(targetId) instanceof LivingEntity target)target.removeAllEffects();}});
        if(!Boolean.getBoolean("hemomancy.animusMortemBenchmark") && phase>=8 && phase<=96 && phase%4==0)
            {frame++;Screenshot.grab(mc.gameDirectory,String.format(java.util.Locale.ROOT,"blood-%s-%05d.png",System.getProperty("hemomancy.animusMortemReviewLabel","after"),scene*23+(phase-8)/4),mc.getMainRenderTarget(),message->{});}
    }

    private static ServerPlayer player(Minecraft mc){return mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());}
    private static void prepare(Minecraft mc,String name,double x,int mode) {
        var player=player(mc);if(player==null)return;var level=player.serverLevel();
        ManipulationChannelManager.stop(player);BloodManipulation.clearSessionState();
        player.removeAllEffects();player.setHealth(20);player.setAbsorptionAmount(0);player.getInventory().clearContent();player.setGameMode(name.equals("blackhearted")?GameType.SURVIVAL:GameType.CREATIVE);
        player.teleportTo(level,x,100,0,0,name.equals("vital_effusion")?40:name.contains("lignum")||name.contains("mortis")?5:0);
        level.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(x-12,95,-10,x+12,115,18),
                e->!(e instanceof net.minecraft.world.entity.player.Player)).forEach(net.minecraft.world.entity.Entity::discard);
        for(var observer:level.getServer().getPlayerList().getPlayers())if(observer!=player)
            observer.teleportTo(level,x+140,100,0,30,12);
        level.setDayTime(mode==1?18000:6000);level.setWeatherParameters(6000,0,false,false);
        for(int dx=-10;dx<=10;dx++)for(int dz=-6;dz<=15;dz++)level.setBlockAndUpdate(new BlockPos((int)x+dx,99,dz),Blocks.STONE.defaultBlockState());
        for(int y=100;y<104;y++){level.setBlockAndUpdate(new BlockPos((int)x-4,y,6),Blocks.GLASS.defaultBlockState());level.setBlockAndUpdate(new BlockPos((int)x+4,y,6),Blocks.DEEPSLATE.defaultBlockState());}
        if(name.contains("lignum")||name.contains("mortis"))for(int y=100;y<106;y++)level.setBlockAndUpdate(new BlockPos((int)x,y,4),(name.equals("worked_lignum")?Blocks.OAK_PLANKS:Blocks.OAK_LOG).defaultBlockState());
        if(name.equals("vital_effusion"))for(int dx=-2;dx<=2;dx++)for(int z=1;z<5;z++){level.setBlockAndUpdate(new BlockPos((int)x+dx,99,z),Blocks.FARMLAND.defaultBlockState());level.setBlockAndUpdate(new BlockPos((int)x+dx,100,z),Blocks.WHEAT.defaultBlockState());}
        for(int i=0;i<4;i++) {
            var target=EntityType.HUSK.create(level);target.setNoAi(true);target.setPos(x+(i==0?0:i-2),100,4+i);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(name.equals("hematic_rebuke")||name.equals("hematic_impressment")?40:200);target.setHealth(name.equals("exsanguinate")?20:160);level.addFreshEntity(target);if(i==0)targetId=target.getId();
            if(name.equals("funeral_bell")||name.equals("carrion_communion")){target.addEffect(new MobEffectInstance(MobEffects.POISON,180));target.addEffect(new MobEffectInstance(EffectInit.blood_loss,180));}
        }
        if(Boolean.getBoolean("hemomancy.animusMortemNpc")) {
            var caster=EntityType.HUSK.create(level);caster.setNoAi(true);caster.setPos(x,100,0);caster.setYRot(0);
            if(level.getEntity(targetId) instanceof LivingEntity target)caster.setTarget(target);
            level.addFreshEntity(caster);npcId=caster.getId();player.teleportTo(level,x+6,100,-2,30,12);
        }
        var blood=HemoCapabilityAccess.requireBloodVolume(player);blood.setActive(true);blood.setBloodVolume(10000);
        for(var tendency:EnumBloodTendency.values())HemoCapabilityAccess.getBloodTendency(player).orElseThrow().setTendencyAlignment(tendency,100);
        var spell=ManipulationInit.getByName(name);var known=HemoCapabilityAccess.requireKnownManipulations(player);
        if(!known.getKnownManips().containsKey(spell))known.getKnownManips().put(spell,new ManipLevel(4,185));known.setSelectedManip(spell);known.setEquippedManipNames(List.of(name));
        if(name.startsWith("conjure_"))player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(ItemInit.living_staff.get()));
    }
}
