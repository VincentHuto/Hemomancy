package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import net.minecraft.world.phys.Vec3;

final class BloodFlowState {
    BloodFlowPacket packet;
    final long born;
    private long until,retired=Long.MAX_VALUE;
    Vec3 start,end;
    BloodFlowState(BloodFlowPacket packet,long now){this.packet=packet;born=now;refresh(packet,now);}
    void refresh(BloodFlowPacket packet,long now){this.packet=packet;until=now+packet.config().clamped().totalLifetime();retired=Long.MAX_VALUE;}
    void retire(long now){retired=Math.min(retired,now);}
    boolean resolve(TendrilAnchor.EntityResolver resolver,long now) {
        if(retired!=Long.MAX_VALUE)return false;
        var a=packet.start().resolve(resolver);var b=packet.end().resolve(resolver);
        if(a.isEmpty()||b.isEmpty()||!Double.isFinite(a.get().lengthSqr())||!Double.isFinite(b.get().lengthSqr())){retire(now);return false;}
        start=a.get();end=b.get();return true;
    }
    float opacity(double now) {
        float grow=ManipulationLifecycle.smooth((float)(now-born)/2);
        float fade=ManipulationLifecycle.smooth((float)(until-now)/8);
        float stop=retired==Long.MAX_VALUE?1:1-ManipulationLifecycle.smooth((float)(now-retired)/8);
        return grow*fade*stop;
    }
    boolean finished(long now){return now>=until||retired!=Long.MAX_VALUE&&now>=retired+8;}
}
