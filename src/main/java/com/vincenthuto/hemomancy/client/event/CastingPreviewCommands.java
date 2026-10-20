package com.vincenthuto.hemomancy.client.event;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.CastingAnimationClientState;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import com.vincenthuto.hemomancy.common.manipulation.stillarts.StillArtEnding;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import java.util.EnumSet;
import java.util.Locale;

/**
 * Local presentation preview: never sends a cast request or changes progression.
 * Harbinger: {@code <school> <rank> <purpose> <phase> [style <s>] [type <t>] [charge <0..1>] [section <vascular section>]},
 * options in any order; the section places the outpouring.
 * Unstained: {@code still <stage> <ending> <phase>}.
 */
@EventBusSubscriber(modid=Hemomancy.MOD_ID,value=Dist.CLIENT)
public final class CastingPreviewCommands {
    /** Full charge in preview ticks; held = round(charge * REQUIRED). */
    private static final int REQUIRED=40;
    /** A registered PASSIVE manipulation: the client decides passive on/off playback from the manipulation id. */
    private static final String PASSIVE_ID="blackhearted";
    private enum Option { STYLE, TYPE, CHARGE, SECTION }
    private CastingPreviewCommands() {}

    @SubscribeEvent public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("hemocastpreview")
            .then(Commands.literal("stop").executes(c -> { CastingAnimationClientState.preview(null); return 1; }))
            .then(Commands.literal("still")
                .then(Commands.argument("stage",StringArgumentType.word()).suggests(names(EnumClarityStage.values()))
                .then(Commands.argument("ending",StringArgumentType.word()).suggests(names(StillArtEnding.values()))
                .then(Commands.argument("phase",StringArgumentType.word()).suggests(names(CastPhase.values()))
                    .executes(CastingPreviewCommands::still)))))
            .then(Commands.argument("school",StringArgumentType.word()).suggests(names(EnumBloodTendency.values()))
            .then(Commands.argument("rank",StringArgumentType.word()).suggests(names(EnumManipulationRank.values()))
            .then(Commands.argument("purpose",StringArgumentType.word()).suggests(names(CastPurpose.values()))
            .then(options(Commands.argument("phase",StringArgumentType.word()).suggests(names(CastPhase.values())),
                    EnumSet.noneOf(Option.class)))))));
    }

    /** Each optional argument may follow the phase once, in any order; every node can execute. */
    private static ArgumentBuilder<CommandSourceStack,?> options(ArgumentBuilder<CommandSourceStack,?> node,EnumSet<Option> used) {
        node.executes(CastingPreviewCommands::harbinger);
        for(var option:Option.values()) {
            if(used.contains(option))continue;
            var next=EnumSet.copyOf(used); next.add(option);
            node.then(switch(option) {
                case STYLE -> Commands.literal("style").then(options(Commands.argument("style_name",StringArgumentType.word())
                        .suggests(names(CastStyle.values())),next));
                case TYPE -> Commands.literal("type").then(options(Commands.argument("cast_type",StringArgumentType.word())
                        .suggests(names(EnumManipulationType.QUICK,EnumManipulationType.CHARGED,
                                EnumManipulationType.CONTINUOUS,EnumManipulationType.PASSIVE)),next));
                case CHARGE -> Commands.literal("charge").then(options(Commands.argument("charge_fraction",FloatArgumentType.floatArg(0,1)),next));
                case SECTION -> Commands.literal("section").then(options(Commands.argument("section_name",StringArgumentType.word())
                        .suggests(names(EnumVeinSections.values())),next));
            });
        }
        return node;
    }

    private static int harbinger(CommandContext<CommandSourceStack> c) {
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.level==null)return 0;
        try {
            var school=parse(EnumBloodTendency.class,StringArgumentType.getString(c,"school"));
            var rank=parse(EnumManipulationRank.class,StringArgumentType.getString(c,"rank"));
            var purpose=parse(CastPurpose.class,StringArgumentType.getString(c,"purpose"));
            var phase=parse(CastPhase.class,StringArgumentType.getString(c,"phase"));
            String styleName=optionalString(c,"style_name"), typeName=optionalString(c,"cast_type");
            String sectionName=optionalString(c,"section_name");
            var section=sectionName==null?null:parse(EnumVeinSections.class,sectionName);
            var style=styleName==null?CastStyle.SCHOOL:parse(CastStyle.class,styleName);
            var type=typeName==null?null:parse(EnumManipulationType.class,typeName);
            Float charge=optionalFloat(c,"charge_fraction");
            // Quick and continuous casts carry no charge requirement, so they show no strain.
            boolean uncharged=type==EnumManipulationType.QUICK || type==EnumManipulationType.CONTINUOUS;
            int required=uncharged?0:REQUIRED;
            int held=uncharged?0:charge==null?REQUIRED/2:Math.round(charge*REQUIRED);
            long now=mc.level.getGameTime();
            CastingAnimationClientState.preview(CastingAnimationPacket.harbinger(mc.player.getId(),mc.player.getUUID(),
                    type==EnumManipulationType.PASSIVE?PASSIVE_ID:section==null?"preview":OutpouringRules.previewId(section),0,school,null,rank,purpose,style,phase,now,now,
                    held,required,InteractionHand.MAIN_HAND));
            mc.player.displayClientMessage(Component.literal("Casting preview: "+school+" / "+rank+" / "+purpose+" / "+phase
                    +(style==CastStyle.SCHOOL?"":" / style "+style)+(type==null?"":" / "+type)
                    +(uncharged?"":" / charge "+held+"/"+required)+(section==null?"":" / "+section)),false);
            return 1;
        } catch(IllegalArgumentException invalid) {
            c.getSource().sendFailure(Component.literal("Use the suggested school, rank, purpose, phase, style, type and section names."));
            return 0;
        }
    }

    private static int still(CommandContext<CommandSourceStack> c) {
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.level==null)return 0;
        try {
            var stage=parse(EnumClarityStage.class,StringArgumentType.getString(c,"stage"));
            var ending=parse(StillArtEnding.class,StringArgumentType.getString(c,"ending"));
            var phase=parse(CastPhase.class,StringArgumentType.getString(c,"phase"));
            long now=mc.level.getGameTime();
            CastingAnimationClientState.preview(CastingAnimationPacket.stillArt(mc.player.getId(),mc.player.getUUID(),
                    "preview",0,stage,ending,phase,now,now,InteractionHand.MAIN_HAND));
            mc.player.displayClientMessage(Component.literal("Still Art preview: "+stage+" / "+ending+" / "+phase),false);
            return 1;
        } catch(IllegalArgumentException invalid) {
            c.getSource().sendFailure(Component.literal("Use the suggested stage, ending and phase names."));
            return 0;
        }
    }

    @SafeVarargs private static <E extends Enum<E>> SuggestionProvider<CommandSourceStack> names(E... values) {
        return (c,b) -> { for(var e:values)b.suggest(e.name().toLowerCase(Locale.ROOT)); return b.buildFuture(); };
    }
    private static <E extends Enum<E>> E parse(Class<E> type,String name) { return Enum.valueOf(type,name.toUpperCase(Locale.ROOT)); }
    /** Brigadier throws IllegalArgumentException for an argument the matched branch did not include. */
    private static String optionalString(CommandContext<CommandSourceStack> c,String name) {
        try { return StringArgumentType.getString(c,name); } catch(IllegalArgumentException absent) { return null; }
    }
    private static Float optionalFloat(CommandContext<CommandSourceStack> c,String name) {
        try { return FloatArgumentType.getFloat(c,name); } catch(IllegalArgumentException absent) { return null; }
    }
}
