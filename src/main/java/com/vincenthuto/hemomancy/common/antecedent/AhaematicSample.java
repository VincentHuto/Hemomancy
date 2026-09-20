package com.vincenthuto.hemomancy.common.antecedent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public record AhaematicSample(String kind, String source) {
    public static final Codec<AhaematicSample> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("kind").forGetter(AhaematicSample::kind),
            Codec.STRING.fieldOf("source").forGetter(AhaematicSample::source)).apply(i,AhaematicSample::new));
    public static final StreamCodec<FriendlyByteBuf,AhaematicSample> STREAM_CODEC = StreamCodec.of(
            (b,s)->{b.writeUtf(s.kind); b.writeUtf(s.source);},b->new AhaematicSample(b.readUtf(),b.readUtf()));
    public boolean valid() {
        return kind.equals("entity") && source.equals("minecraft:warden")
                || kind.equals("block") && source.equals("minecraft:sculk_catalyst");
    }
    public static AhaematicSample get(ItemStack stack) {
        if (!stack.is(ItemInit.ahaematic_colloid.get()) && !(stack.getItem() instanceof BloodVialItem)) return null;
        var data = stack.get(DataComponentInit.AHAEMATIC_SAMPLE.get());
        if (data != null) return data;
        if (!(stack.getItem() instanceof BloodVialItem)) return null;
        String legacy = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(BloodVialItem.TAG_ENTITY_TYPE);
        return legacy.equals("minecraft:warden") ? new AhaematicSample("entity",legacy) : null;
    }
    public static boolean is(ItemStack stack) { return get(stack) != null; }
    public static boolean readable(ItemStack stack) { var sample = get(stack); return sample != null && sample.valid(); }
    public static ItemStack create(String kind, String source) {
        var stack = new ItemStack(ItemInit.ahaematic_colloid.get());
        stack.set(DataComponentInit.AHAEMATIC_SAMPLE.get(),new AhaematicSample(kind,source));
        return stack;
    }
    /** Converts the former component-backed bloody vial without discarding user metadata. */
    public static ItemStack migrate(ItemStack stack) {
        if (!(stack.getItem() instanceof BloodVialItem)) return stack;
        var sample = get(stack);
        if (sample == null) return stack;
        var migrated = create(sample.kind(),sample.source());
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        tag.remove(BloodVialItem.TAG_ENTITY_TYPE);
        tag.remove(BloodVialItem.TAG_STATE);
        if (!tag.isEmpty()) migrated.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        if (stack.getOrDefault(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get(),false))
            migrated.set(DataComponentInit.BLOOD_SAMPLE_IDENTIFIED.get(),true);
        return migrated;
    }
}
