package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketToggleSkill(int skillId) implements CustomPacketPayload {
	public static final Type<PacketToggleSkill> TYPE = new Type<>(Hemomancy.rloc("packet_toggle_skill"));
	public static final StreamCodec<FriendlyByteBuf, PacketToggleSkill> STREAM_CODEC = StreamCodec.of(
			(buf, msg) -> buf.writeInt(msg.skillId), buf -> new PacketToggleSkill(buf.readInt()));

	public static void handle(PacketToggleSkill msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (!(ctx.player() instanceof ServerPlayer player)) return;
			SkillPoint skill = SkillPointInit.getById(msg.skillId);
			SkillProgress progress = HemoCapabilityAccess.requireSkillProgress(player);
			if (skill == null || !progress.toggleEnabled(skill)) return;
			boolean enabled = progress.isEnabled(skill);
			player.displayClientMessage(Component.literal(skill.getName().replace("skill_", "").replace('_', ' ')
					+ (enabled ? ": enabled" : ": disabled"))
					.withStyle(enabled ? ChatFormatting.GOLD : ChatFormatting.GRAY), true);
			PacketHandler.sendToPlayer(player, new PacketSyncSkills(progress.toSyncTag()));
		});
	}

	@Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
