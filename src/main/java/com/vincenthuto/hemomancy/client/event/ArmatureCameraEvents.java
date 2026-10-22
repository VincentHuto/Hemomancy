package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.utility.ArmatureRestraintEntity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * Third-person camera while restrained on the Hematic Armature.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ArmatureCameraEvents {

    private ArmatureCameraEvents() {
    }

    private static CameraType cameraBeforeArmature = null;
    private static final float ARMATURE_CAMERA_DISTANCE = 7.0F;

    static void handleArmatureCameraFallback() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            cameraBeforeArmature = null;
            return;
        }
        boolean mountedOnArmature = mc.player.getVehicle() instanceof ArmatureRestraintEntity;
        if (mountedOnArmature) {
            if (cameraBeforeArmature == null) {
                cameraBeforeArmature = mc.options.getCameraType();
            }
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        } else if (cameraBeforeArmature != null) {
            mc.options.setCameraType(cameraBeforeArmature);
            cameraBeforeArmature = null;
        }
    }

    @SubscribeEvent
    public static void onDetachedCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        if (event.getCamera().getEntity() instanceof Player player
                && player.getVehicle() instanceof ArmatureRestraintEntity) {
            event.setDistance(Math.max(event.getDistance(), ARMATURE_CAMERA_DISTANCE));
        }
    }
}
