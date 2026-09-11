package com.vincenthuto.hemomancy.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.AnimusMortemRenderTypes;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.util.LinkedHashMap;

@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class MortemSkinLayer<T extends LivingEntity,M extends EntityModel<T>> extends RenderLayer<T,M> {
    private record Key(int entity,Form form) {}
    private static final class Skin {final long born;long until;Skin(long now,int ticks){born=now;until=now+ticks;}}
    private static final LinkedHashMap<Key,Skin> SKINS=new LinkedHashMap<>();
    private static ClientLevel world;
    public MortemSkinLayer(LivingEntityRenderer<T,M> renderer){super(renderer);}
    public static boolean handles(Form form){return form==Form.WOUND||form==Form.HUNGER||form==Form.GRAVE||form==Form.ROT_INFECTION;}
    public static void accept(ManipulationVisualPacket packet,ClientLevel level) {
        reset(level);if(level==null||packet.entityId()<0||!handles(packet.form()))return;
        long now=level.getGameTime();Key key=new Key(packet.entityId(),packet.form());Skin skin=SKINS.get(key);
        if(packet.ticks()<=0){if(skin!=null)skin.until=now;return;}
        if(skin==null){if(SKINS.size()>=192)SKINS.pollFirstEntry();SKINS.put(key,new Skin(now,packet.ticks()));}
        else skin.until=now+packet.ticks();
    }
    private static void reset(ClientLevel level){if(world!=level){SKINS.clear();world=level;}}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        reset(Minecraft.getInstance().level);
        if(world!=null)SKINS.entrySet().removeIf(e->world.getGameTime()>=e.getValue().until+6||world.getEntity(e.getKey().entity)==null||!world.getEntity(e.getKey().entity).isAlive());
    }
    @Override public void render(PoseStack p,MultiBufferSource buffer,int light,T entity,float limbSwing,float limbSwingAmount,float partial,float age,float yaw,float pitch) {
        if(entity.isInvisible()||world!=entity.level()||SKINS.isEmpty())return;
        double now=world.getGameTime()+partial;AnimusMortemRenderTypes.begin(now);
        for(var entry:SKINS.entrySet()) {
            if(entry.getKey().entity!=entity.getId())continue;
            Skin state=entry.getValue();
            float growth=Mth.clamp((float)(now-state.born)/10,0,1),fade=Mth.clamp((float)(state.until+6-now)/6,0,1);
            int style=switch(entry.getKey().form){case WOUND->0;case HUNGER->1;case GRAVE->2;default->3;};
            int color=(int)(fade*210)<<24|(int)(growth*255)<<16|Math.round(style*255f/7)<<8;
            getParentModel().renderToBuffer(p,buffer.getBuffer(AnimusMortemRenderTypes.INFECTION),light,OverlayTexture.NO_OVERLAY,color);
        }
    }
}
