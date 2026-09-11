package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket.Style;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class BloodFlowVisuals {
    private BloodFlowVisuals() {}
    public static void connect(LivingEntity caster,LivingEntity target,Style style) {
        if(!(caster.level() instanceof ServerLevel level))return;
        boolean drain=style==Style.EXTRACTION||style==Style.COMMUNION;
        TendrilAnchor source=style==Style.EXTRACTION?new TendrilAnchor.Point(target.getEyePosition()):anchor(drain?target:caster);
        TendrilAnchor end=anchor(drain?caster:target);
        long key=((long)caster.getId()<<32)^(target.getId()&0xffffffffL)^((long)style.ordinal()<<56);
        if(style!=Style.COMMUNION)key^=level.getGameTime()*31;
        send(level,caster.getId(),key,style,source,end,style==Style.COMMUNION?24:18,false);
    }
    public static void lignum(LivingEntity caster,Vec3 target,long seed) {
        if(caster.level() instanceof ServerLevel level)send(level,caster.getId(),seed,Style.LIGNUM,anchor(caster),
                new TendrilAnchor.Point(target),10,false);
    }
    public static void stop(LivingEntity caster,Style style) {
        if(caster.level() instanceof ServerLevel level)send(level,caster.getId(),0,style,anchor(caster),anchor(caster),1,true);
    }
    private static TendrilAnchor anchor(LivingEntity entity){return new TendrilAnchor.Entity(entity.getId(),TendrilAnchor.AnchorPoint.CENTER,new Vec3(0,.1,0));}
    private static void send(ServerLevel level,int owner,long key,Style style,TendrilAnchor start,TendrilAnchor end,int ticks,boolean stop) {
        var resolver=TendrilAnchor.forLevel(level);var from=start.resolve(resolver);var to=end.resolve(resolver);
        if(from.isEmpty()||to.isEmpty())return;
        var config=TendrilEffectConfig.defaults().withRange((float)(from.get().distanceTo(to.get())+4))
                .withLifecycle(2,ticks,8).withShape(12,1,style==Style.COMMUNION?.12f:.085f,.2f)
                .withBranching(0,0,0,0).withWrithe(.08f,.05f,.4f,.04f).withFixedSeed(true,key).clamped();
        Vec3 center=from.get().lerp(to.get(),.5);
        PacketDistributor.sendToPlayersNear(level,null,center.x,center.y,center.z,64+from.get().distanceTo(to.get())*.5,
                new BloodFlowPacket(key,owner,style,start,end,config,stop));
    }
}
