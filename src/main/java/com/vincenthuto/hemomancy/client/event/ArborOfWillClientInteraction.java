package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgressClientCache;
import com.vincenthuto.hemomancy.common.entity.utility.ArborOfWillEntity;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ArborFruitInteractPacket;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.worldgen.arbor.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Gives hanging fruit normal-reach right-click targets without making each fruit an entity. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ArborOfWillClientInteraction {
    private ArborOfWillClientInteraction() { }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        double reach = player.isCreative() ? 5.0D : 4.5D;
        Target target = findTarget(minecraft, reach, true).orElse(null);
        if (target == null) return;
        PacketHandler.sendToServer(new ArborFruitInteractPacket(target.arbor().getId(), target.fruit().skillId()));
        event.setCanceled(true);
        event.setSwingHand(true);
    }

    public static Optional<Target> findTarget(Minecraft minecraft, double reach, boolean unlockedOnly) {
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null
                || !minecraft.level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) return Optional.empty();
        List<ArborFruitTargeting.Candidate> candidates = new ArrayList<>();
        List<ArborOfWillEntity> arbors = minecraft.level.getEntitiesOfClass(ArborOfWillEntity.class,
                new AABB(player.blockPosition()).inflate(reach + 8.0D), arbor -> arbor.isOwnedBy(player));
        for (ArborOfWillEntity arbor : arbors) {
            for (ArborOfWillLayout.FruitPlacement fruit : ArborSkillPresentation.placements(arbor.chamberRadius())) {
                SkillPoint skill = SkillPointInit.getById(fruit.skillId());
                if (skill == null || fruit.whorl() > Math.max(1, ArborOfWillVisualRules.visibleWhorls(arbor.degree()))) continue;
                if (unlockedOnly && !SkillProgressClientCache.current().isUnlocked(skill)) continue;
                Vec3 position = arbor.position().add(fruit.x(), fruit.y(), fruit.z());
                candidates.add(new ArborFruitTargeting.Candidate(arbor.getId(), fruit.skillId(),
                        new ArborCanopyGeometry.Point(position.x, position.y, position.z), .38D));
            }
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        ArborFruitTargeting.Candidate selected = ArborFruitTargeting.select(candidates,
                new ArborCanopyGeometry.Point(eye.x, eye.y, eye.z),
                new ArborCanopyGeometry.Point(look.x, look.y, look.z), reach).orElse(null);
        if (selected == null) return Optional.empty();
        ArborOfWillEntity arbor = arbors.stream().filter(candidate -> candidate.getId() == selected.arborEntityId())
                .findFirst().orElse(null);
        SkillPoint skill = SkillPointInit.getById(selected.skillId());
        if (arbor == null || skill == null) return Optional.empty();
        ArborOfWillLayout.FruitPlacement fruit = ArborSkillPresentation.placements(arbor.chamberRadius()).stream()
                .filter(candidate -> candidate.skillId() == selected.skillId()).findFirst().orElse(null);
        return fruit == null ? Optional.empty() : Optional.of(new Target(arbor, fruit, skill));
    }

    public record Target(ArborOfWillEntity arbor, ArborOfWillLayout.FruitPlacement fruit, SkillPoint skill) { }
}
