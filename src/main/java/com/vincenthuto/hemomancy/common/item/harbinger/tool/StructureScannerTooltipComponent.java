package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record StructureScannerTooltipComponent(List<Component> lines) implements TooltipProvider {

	public static final int MAX_LINES = 16;
	public static final Codec<StructureScannerTooltipComponent> CODEC = ComponentSerialization.FLAT_CODEC
			.sizeLimitedListOf(MAX_LINES)
			.xmap(StructureScannerTooltipComponent::new, StructureScannerTooltipComponent::lines);
	public static final StreamCodec<RegistryFriendlyByteBuf, StructureScannerTooltipComponent> STREAM_CODEC = ComponentSerialization.STREAM_CODEC
			.apply(ByteBufCodecs.list(MAX_LINES))
			.map(StructureScannerTooltipComponent::new, StructureScannerTooltipComponent::lines);

	public StructureScannerTooltipComponent {
		if (lines.size() > MAX_LINES) {
			throw new IllegalArgumentException("Got " + lines.size() + " lines, but maximum is " + MAX_LINES);
		}
		lines = List.copyOf(lines);
	}

	@Override
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
		lines.forEach(tooltipAdder);
	}
}

