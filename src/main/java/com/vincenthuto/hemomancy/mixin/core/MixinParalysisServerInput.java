package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinParalysisServerInput {
    @Shadow public ServerPlayer player;
    @org.spongepowered.asm.mixin.injection.Inject(method="handlePlayerAction",at=@At("HEAD"),cancellable=true,remap=false)
    private void hemomancy$signalDrop(net.minecraft.network.protocol.game.ServerboundPlayerActionPacket packet,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (player.serverLevel().getServer().isSameThread()
                && com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager.isTraveling(player)
                && (packet.getAction()==net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.DROP_ITEM
                    || packet.getAction()==net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS))
            ci.cancel();
    }
    @org.spongepowered.asm.mixin.injection.Inject(method="handleContainerClick",at=@At("HEAD"),cancellable=true,remap=false)
    private void hemomancy$signalInventory(net.minecraft.network.protocol.game.ServerboundContainerClickPacket packet,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (player.serverLevel().getServer().isSameThread()
                && com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager.isTraveling(player)) {
            player.containerMenu.sendAllDataToRemote();
            ci.cancel();
        }
    }
    @org.spongepowered.asm.mixin.injection.Inject(method="handlePlayerCommand",at=@At("HEAD"),cancellable=true,remap=false)
    private void hemomancy$paralysisMountJump(net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket packet,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (player.serverLevel().getServer().isSameThread() && Paralysis.isParalyzed(player)
                && packet.getAction()==net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.START_RIDING_JUMP) ci.cancel();
    }

    @ModifyVariable(method="handleMovePlayer",at=@At("HEAD"),argsOnly=true,remap=false)
    private ServerboundMovePlayerPacket hemomancy$paralysisMovement(ServerboundMovePlayerPacket packet) {
        if (player.serverLevel().getServer().isSameThread()
                && com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager.isTraveling(player))
            return new ServerboundMovePlayerPacket.Rot(packet.getYRot(player.getYRot()),packet.getXRot(player.getXRot()),false);
        if (!player.serverLevel().getServer().isSameThread()
                || !Paralysis.isParalyzed(player) || !packet.hasPosition()) return packet;
        return new ServerboundMovePlayerPacket.PosRot(player.getX(),
                Math.min(player.getY(),packet.getY(player.getY())),player.getZ(),
                packet.getYRot(player.getYRot()),packet.getXRot(player.getXRot()),packet.isOnGround());
    }
}
