package com.vincenthuto.hemomancy.common.mission.hermit;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import net.minecraft.server.level.ServerPlayer;
/** Spent temples no longer grant additional hearts. */
public final class SpentTempleInitiation {
 private SpentTempleInitiation() {}
 public static boolean acceptBlessing(ServerPlayer player, HarbingerHermitEntity hermit) { return false; }
}
