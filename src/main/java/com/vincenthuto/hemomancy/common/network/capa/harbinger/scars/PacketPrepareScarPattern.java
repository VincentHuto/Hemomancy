package com.vincenthuto.hemomancy.common.network.capa.harbinger.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarType;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MasonsEffigyMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class PacketPrepareScarPattern implements CustomPacketPayload {
	public static final Type<PacketPrepareScarPattern> TYPE = new Type<>(Hemomancy.rloc("packet_prepare_scar_pattern"));
	public static final StreamCodec<FriendlyByteBuf, PacketPrepareScarPattern> STREAM_CODEC =
			StreamCodec.of(PacketPrepareScarPattern::encode, PacketPrepareScarPattern::decode);

	private final BlockPos pos;
	private final List<ResourceLocation> scarIds;
	private final boolean announce;

	public PacketPrepareScarPattern(BlockPos pos, List<ResourceLocation> scarIds) {
		this(pos, scarIds, true);
	}

	public PacketPrepareScarPattern(BlockPos pos, List<ResourceLocation> scarIds, boolean announce) {
		this.pos = pos;
		this.scarIds = List.copyOf(scarIds);
		this.announce = announce;
	}

	public static void encode(FriendlyByteBuf buf, PacketPrepareScarPattern msg) {
		buf.writeBlockPos(msg.pos);
		buf.writeBoolean(msg.announce);
		buf.writeInt(msg.scarIds.size());
		for (ResourceLocation id : msg.scarIds) {
			buf.writeResourceLocation(id);
		}
	}

	public static PacketPrepareScarPattern decode(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		boolean announce = buf.readBoolean();
		int count = buf.readInt();
		List<ResourceLocation> ids = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			ids.add(buf.readResourceLocation());
		}
		return new PacketPrepareScarPattern(pos, ids, announce);
	}

	public static void handle(PacketPrepareScarPattern msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (!(ctx.player() instanceof ServerPlayer player)) {
				return;
			}
			MasonsEffigyMenu menu = ServerboundGuards.menu(player, MasonsEffigyMenu.class);
			if (menu == null || !menu.getPos().equals(msg.pos)) {
				fail(player, Component.translatable("message.hemomancy.packet_prepare_scar_pattern.the_effigy_is_no_longer_listening"));
				return;
			}
			BlockEntity be = player.level().getBlockEntity(msg.pos);
			if (!(be instanceof MasonsEffigyBlockEntity effigy) || be.getType() != BlockEntityInit.mason_effigy.get()) {
				fail(player, Component.translatable("message.hemomancy.packet_prepare_scar_pattern.no_mason_s_effigy_answers_from"));
				return;
			}
			List<ResourceLocation> selected = new ArrayList<>(new LinkedHashSet<>(msg.scarIds));
			if (effigy.hasPendingMotif()) {
				fail(player, Component.translatable("message.hemomancy.packet_prepare_scar_pattern.finish_charging_the_current_motif_before"));
				return;
			}
			int max = Math.min(MasonsEffigyMenu.MAX_SELECTED_SCARS, menu.getMaxSelectableScars());
			if (selected.size() > max) {
				fail(player, Component.translatable("message.hemomancy.packet_prepare_scar_pattern.that_pattern_exceeds_your_current_scar"));
				return;
			}
			var scars = HemoCapabilityAccess.getScarState(player);
			if (scars == null) {
				fail(player, Component.translatable("message.hemomancy.packet_prepare_scar_pattern.your_scar_memory_is_silent"));
				return;
			}
			for (ResourceLocation id : selected) {
				ScarDefinition definition = ScarInit.getByName(id.toString());
				if (definition == null || definition.getScarType() != ScarType.CEREBRAL || !scars.knowsCerebralScar(id)) {
					fail(player, HemoText.translatable("message.hemomancy.packet_prepare_scar_pattern.the_effigy_rejects_an_unknown_scar", id.getPath()));
					return;
				}
			}
			effigy.setSelectedScarIds(selected);
			if (!msg.announce) {
				return;
			}
			if (selected.isEmpty()) {
				player.displayClientMessage(Component.translatable("message.hemomancy.packet_prepare_scar_pattern.the_effigy_releases_its_scar_pattern").withStyle(ChatFormatting.DARK_RED), true);
				return;
			}
			player.displayClientMessage(Component.translatable("message.hemomancy.packet_prepare_scar_pattern.the_effigy_holds_the_selected_scars").withStyle(ChatFormatting.DARK_RED), true);
		});
	}

	private static void fail(ServerPlayer player, Component text) {
		player.displayClientMessage(text.copy().withStyle(ChatFormatting.RED), true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
