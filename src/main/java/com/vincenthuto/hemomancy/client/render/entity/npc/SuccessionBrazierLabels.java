package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierMount;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.succession.SuccessionBrazierOrder;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;

/**
 * In-world role markings over a succession rite's offerings. Braziers count anywhere in the space above the floor
 * ({@link com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteBrazierSpace}), so each lit, offered brazier is
 * marked with the role its place in that order gives it ({@link SuccessionBrazierOrder}), as the station matcher
 * reads them.
 */
public final class SuccessionBrazierLabels {
    private static final int ROLES = 3;
    private static net.minecraft.client.multiplayer.ClientLevel cachedLevel;
    private static long scannedAt = Long.MIN_VALUE;
    private static final java.util.List<IronBrazierBlockEntity> nearby = new java.util.ArrayList<>();
    private static final java.util.Map<BlockPos, Integer> roles = new java.util.HashMap<>();
    private SuccessionBrazierLabels() {}
    public static void renderWorld(PoseStack pose) {
        var mc=Minecraft.getInstance();
        if(mc.level==null || mc.player==null)return;
        if(cachedLevel!=mc.level || mc.level.getGameTime()-scannedAt>=20) {
            cachedLevel=mc.level;scannedAt=mc.level.getGameTime();nearby.clear();roles.clear();
            var origin=mc.player.chunkPosition();
            var focuses=new java.util.ArrayList<CardinalFocusBlockEntity>();
            for(int x=origin.x-1;x<=origin.x+1;x++)for(int z=origin.z-1;z<=origin.z+1;z++) {
                if(!mc.level.hasChunk(x,z))continue;
                for(var block:mc.level.getChunk(x,z).getBlockEntities().values()) {
                    if(block instanceof IronBrazierBlockEntity brazier)nearby.add(brazier);
                    if(block instanceof CardinalFocusBlockEntity focus)focuses.add(focus);
                }
            }
            for(var focus:focuses)assignRoles(mc.level,focus);
        }
        if(nearby.isEmpty() || roles.isEmpty())return;
        var camera=mc.gameRenderer.getMainCamera().getPosition();
        var buffers=mc.renderBuffers().bufferSource();
        for(var brazier:nearby) {
            if(brazier.isRemoved())continue;
            var pos=brazier.getBlockPos();pose.pushPose();pose.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);
            render(brazier,pose,buffers,15728880);pose.popPose();
        }
        buffers.endBatch();
    }
    /** Ranks the lit, offered braziers in a succession floor's space, in the order the station matcher reads them. */
    private static void assignRoles(net.minecraft.world.level.Level level,CardinalFocusBlockEntity focus) {
        if(focus.getFormedFloor()==null)return;
        var floor=CardinalRiteFloorRegistry.get(focus.getFormedFloor()).orElse(null);
        if(floor==null)return;
        var order=SuccessionBrazierOrder.roles(level,focus.getBlockPos(),floor);
        for(int role=0;role<Math.min(ROLES,order.size());role++)roles.put(order.get(role),role);
    }
    public static void render(IronBrazierBlockEntity brazier,PoseStack pose,MultiBufferSource buffer,int light) {
        var mc=Minecraft.getInstance(); var level=brazier.getLevel();
        if(level==null || mc.player==null || mc.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(brazier.getBlockPos()))>144) return;
        Integer role=roles.get(brazier.getBlockPos());
        if(role==null)return;
        var label=Component.translatable("hemomancy.succession.socket."+role);
        double drop=BrazierMount.drop(BrazierMount.face(brazier.getBlockState()));
        pose.pushPose();pose.translate(.5,1.65+drop,.5);pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());pose.scale(.022f,-.022f,.022f);
        mc.font.drawInBatch(label,-mc.font.width(label)/2f,0,0xFFE7B2AD,false,pose.last().pose(),buffer,Font.DisplayMode.SEE_THROUGH,0x80190509,15728880);
        pose.popPose();
    }
}
