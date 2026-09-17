package com.vincenthuto.hemomancy.client.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.CastingAnimationClientState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import java.util.Locale;

/** Local presentation preview: never sends a cast request or changes progression. */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class CastingPreviewCommands {
    private CastingPreviewCommands() {}
    @SubscribeEvent public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("hemocastpreview")
            .then(Commands.literal("stop").executes(c -> { CastingAnimationClientState.preview(null); return 1; }))
            .then(Commands.argument("school",StringArgumentType.word())
                .suggests((c,b) -> { for(var e:EnumBloodTendency.values())b.suggest(e.name().toLowerCase(Locale.ROOT)); return b.buildFuture(); })
            .then(Commands.argument("rank",StringArgumentType.word())
                .suggests((c,b) -> { for(var e:EnumManipulationRank.values())b.suggest(e.name().toLowerCase(Locale.ROOT)); return b.buildFuture(); })
            .then(Commands.argument("purpose",StringArgumentType.word())
                .suggests((c,b) -> { for(var e:CastPurpose.values())b.suggest(e.name().toLowerCase(Locale.ROOT)); return b.buildFuture(); })
            .then(Commands.argument("phase",StringArgumentType.word())
                .suggests((c,b) -> { for(var e:CastPhase.values())b.suggest(e.name().toLowerCase(Locale.ROOT)); return b.buildFuture(); })
                .executes(c -> {
                    var mc=Minecraft.getInstance();
                    if(mc.player==null || mc.level==null)return 0;
                    try {
                        var school=EnumBloodTendency.valueOf(StringArgumentType.getString(c,"school").toUpperCase(Locale.ROOT));
                        var rank=EnumManipulationRank.valueOf(StringArgumentType.getString(c,"rank").toUpperCase(Locale.ROOT));
                        var purpose=CastPurpose.valueOf(StringArgumentType.getString(c,"purpose").toUpperCase(Locale.ROOT));
                        var phase=CastPhase.valueOf(StringArgumentType.getString(c,"phase").toUpperCase(Locale.ROOT));
                        long now=mc.level.getGameTime();
                        CastingAnimationClientState.preview(new CastingAnimationPacket(mc.player.getId(),mc.player.getUUID(),
                                "preview",0,school,null,rank,purpose,CastStyle.SCHOOL,phase,now,now,20,40,InteractionHand.MAIN_HAND));
                        mc.player.displayClientMessage(Component.literal("Casting preview: "+school+" / "+rank+" / "+purpose+" / "+phase),false);
                        return 1;
                    } catch(IllegalArgumentException invalid) {
                        c.getSource().sendFailure(Component.literal("Use the suggested school, rank, purpose, and phase names."));
                        return 0;
                    }
                }))))));
    }
}
