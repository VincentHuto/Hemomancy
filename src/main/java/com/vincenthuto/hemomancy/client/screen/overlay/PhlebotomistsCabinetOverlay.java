package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.PhlebotomistsCabinetBlock;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CabinetInspection;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.PhlebotomistsCabinetBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public final class PhlebotomistsCabinetOverlay {
    private PhlebotomistsCabinetOverlay() {}

    public static void render(GuiGraphics graphics) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.screen != null || mc.options.hideGui
                || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK
                || !(mc.level.getBlockEntity(hit.getBlockPos()) instanceof PhlebotomistsCabinetBlockEntity cabinet)) return;
        var state = cabinet.getBlockState();
        var facing = state.getValue(PhlebotomistsCabinetBlock.FACING);
        if (!state.getValue(PhlebotomistsCabinetBlock.GLAZED) || hit.getDirection() != facing) return;
        var origin = Vec3.atLowerCornerOf(hit.getBlockPos());
        int cell = CabinetInspection.cellAt(facing, mc.gameRenderer.getMainCamera().getPosition().subtract(origin),
                hit.getLocation().subtract(origin));
        if (cell < 0) return;
        var specimen = cabinet.displaySpecimen(cell);
        int count = cabinet.displayCount(cell);
        if (specimen.isEmpty() || count <= 0) return;
        var type = BloodSampleData.entityType(specimen);
        var name = type == null ? Component.literal(BloodSampleData.rawSource(specimen)) : type.getDescription();
        graphics.renderComponentTooltip(mc.font, List.of(name,
                Component.translatable("tooltip.hemomancy.cabinet.world_count", count)),
                graphics.guiWidth() / 2 + 8, graphics.guiHeight() / 2 + 8);
    }
}
