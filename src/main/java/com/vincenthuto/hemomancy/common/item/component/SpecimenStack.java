package com.vincenthuto.hemomancy.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/** Immutable quantity plus a complete, legal one-vial exemplar. */
public record SpecimenStack(ItemStack exemplar, int count) {
    public static final SpecimenStack EMPTY = new SpecimenStack(ItemStack.EMPTY, 0);
    public static final Codec<SpecimenStack> CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("specimen").forGetter(SpecimenStack::exemplar),
            Codec.INT.fieldOf("count").forGetter(SpecimenStack::count)).apply(i, SpecimenStack::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpecimenStack> STREAM_CODEC = StreamCodec.of(
            (b, s) -> { ItemStack.OPTIONAL_STREAM_CODEC.encode(b, s.exemplar); b.writeVarInt(s.count); },
            b -> new SpecimenStack(ItemStack.OPTIONAL_STREAM_CODEC.decode(b), b.readVarInt()));
    public SpecimenStack { exemplar = exemplar.copyWithCount(exemplar.isEmpty() ? 0 : 1); }
    @Override public ItemStack exemplar() { return exemplar.copy(); }
    public boolean matches(ItemStack stack) { return ItemStack.isSameItemSameComponents(exemplar, stack); }
    @Override public boolean equals(Object other) {
        return other instanceof SpecimenStack s && count == s.count && ItemStack.matches(exemplar, s.exemplar);
    }
    @Override public int hashCode() { return 31 * ItemStack.hashItemAndComponents(exemplar) + count; }
}
