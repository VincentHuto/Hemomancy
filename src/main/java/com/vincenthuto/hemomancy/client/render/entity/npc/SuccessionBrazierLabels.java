package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;

/** In-world role markings, oriented by the plinth beside the Focus. */
public final class SuccessionBrazierLabels {
    private static final int[][] SOCKETS={{5,0},{4,2},{3,4}};
    private static net.minecraft.client.multiplayer.ClientLevel cachedLevel;
    private static long scannedAt = Long.MIN_VALUE;
    private static final java.util.List<IronBrazierBlockEntity> nearby = new java.util.ArrayList<>();
    private SuccessionBrazierLabels() {}
    public static void renderWorld(PoseStack pose) {
        var mc=Minecraft.getInstance();
        if(mc.level==null || mc.player==null)return;
        if(cachedLevel!=mc.level || mc.level.getGameTime()-scannedAt>=20) {
            cachedLevel=mc.level;scannedAt=mc.level.getGameTime();nearby.clear();
            var origin=mc.player.chunkPosition();
            for(int x=origin.x-1;x<=origin.x+1;x++)for(int z=origin.z-1;z<=origin.z+1;z++) {
                if(!mc.level.hasChunk(x,z))continue;
                for(var block:mc.level.getChunk(x,z).getBlockEntities().values())
                    if(block instanceof IronBrazierBlockEntity brazier)nearby.add(brazier);
            }
        }
        if(nearby.isEmpty())return;
        var camera=mc.gameRenderer.getMainCamera().getPosition();
        var buffers=mc.renderBuffers().bufferSource();
        for(var brazier:nearby) {
            if(brazier.isRemoved())continue;
            var pos=brazier.getBlockPos();pose.pushPose();pose.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);
            render(brazier,pose,buffers,15728880);pose.popPose();
        }
        buffers.endBatch();
    }
    public static void render(IronBrazierBlockEntity brazier,PoseStack pose,MultiBufferSource buffer,int light) {
        var mc=Minecraft.getInstance(); var level=brazier.getLevel();
        if(level==null || mc.player==null || mc.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(brazier.getBlockPos()))>144) return;
        for(var right:Direction.Plane.HORIZONTAL) for(int index=0;index<SOCKETS.length;index++) {
            var forward=right.getCounterClockWise();var offset=SOCKETS[index];
            var focus=brazier.getBlockPos().relative(right,-offset[0]).relative(forward,-offset[1]).below();
            if(!level.getBlockState(focus).is(BlockInit.cardinal_focus.get())
                    || !level.getBlockState(focus.relative(right,2).above(2)).is(BlockInit.vacant_effigy.get()))continue;
            var label=Component.translatable("hemomancy.succession.socket."+index);
            pose.pushPose();pose.translate(.5,1.65,.5);pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());pose.scale(.022f,-.022f,.022f);
            mc.font.drawInBatch(label,-mc.font.width(label)/2f,0,0xFFE7B2AD,false,pose.last().pose(),buffer,Font.DisplayMode.SEE_THROUGH,0x80190509,15728880);
            pose.popPose();return;
        }
    }
}
