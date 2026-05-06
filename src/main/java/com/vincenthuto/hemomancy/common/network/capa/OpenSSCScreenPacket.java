package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.tile.functional.SSCScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → Client: tells the client to open the {@link SSCScreen}.
 * <p>
 * Carries all the data the screen needs up-front so no additional
 * round-trip is required:
 * <ul>
 *   <li>The SSC's world position.</li>
 *   <li>Current and maximum blood volume of the SSC.</li>
 *   <li>A snapshot of every bound {@link com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity}:
 *       memory name, passive/commanded mode, blood charge, rogue flag.</li>
 * </ul>
 */
public class OpenSSCScreenPacket implements CustomPacketPayload {

    public static final Type<OpenSSCScreenPacket> TYPE = new Type<>(Hemomancy.rloc("open_ssc_screen_packet"));
    public static final StreamCodec<FriendlyByteBuf, OpenSSCScreenPacket> STREAM_CODEC =
            StreamCodec.of(OpenSSCScreenPacket::encode, OpenSSCScreenPacket::decode);

    // ── Payload ──────────────────────────────────────────────────────────────

    private final BlockPos pos;
    private final double sscBlood;
    private final double sscMaxBlood;
    private final List<DrudgeEntry> drudges;

    // ── Drudge snapshot record ────────────────────────────────────────────────

    /**
     * Immutable snapshot of a single Drudge's display state.
     *
     * @param memoryName  The result of {@code BloodManipulation.getProperName()},
     *                    or empty string if no memory is equipped.
     * @param customName  The entity's custom name set via a name tag, or empty string if none.
     * @param passive     {@code true} if the drudge is in passive mode.
     * @param charge      Current blood charge, in mL.
     * @param maxCharge   Maximum blood charge, in mL.
     * @param rogue       {@code true} if the drudge has gone rogue.
     */
    public record DrudgeEntry(String memoryName, String customName, boolean passive, float charge, float maxCharge, boolean rogue) {}

    // ── Constructor ──────────────────────────────────────────────────────────

    public OpenSSCScreenPacket(BlockPos pos, double sscBlood, double sscMaxBlood, List<DrudgeEntry> drudges) {
        this.pos = pos;
        this.sscBlood = sscBlood;
        this.sscMaxBlood = sscMaxBlood;
        this.drudges = List.copyOf(drudges);
    }

    // ── Codec ────────────────────────────────────────────────────────────────

    public static void encode(FriendlyByteBuf buf, OpenSSCScreenPacket msg) {
        buf.writeBlockPos(msg.pos);
        buf.writeDouble(msg.sscBlood);
        buf.writeDouble(msg.sscMaxBlood);
        buf.writeVarInt(msg.drudges.size());
        for (DrudgeEntry d : msg.drudges) {
            buf.writeUtf(d.memoryName());
            buf.writeUtf(d.customName());
            buf.writeBoolean(d.passive());
            buf.writeFloat(d.charge());
            buf.writeFloat(d.maxCharge());
            buf.writeBoolean(d.rogue());
        }
    }

    public static OpenSSCScreenPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        double sscBlood = buf.readDouble();
        double sscMaxBlood = buf.readDouble();
        int count = buf.readVarInt();
        List<DrudgeEntry> drudges = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String name = buf.readUtf();
            String customName = buf.readUtf();
            boolean passive = buf.readBoolean();
            float charge = buf.readFloat();
            float maxCharge = buf.readFloat();
            boolean rogue = buf.readBoolean();
            drudges.add(new DrudgeEntry(name, customName, passive, charge, maxCharge, rogue));
        }
        return new OpenSSCScreenPacket(pos, sscBlood, sscMaxBlood, drudges);
    }

    // ── Handler ──────────────────────────────────────────────────────────────

    @OnlyIn(Dist.CLIENT)
    public static void handle(final OpenSSCScreenPacket msg, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() != null) {
                SSCScreen.open(msg.pos, msg.sscBlood, msg.sscMaxBlood, msg.drudges);
            }
        });
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public BlockPos getPos()          { return pos; }
    public double getSscBlood()       { return sscBlood; }
    public double getSscMaxBlood()    { return sscMaxBlood; }
    public List<DrudgeEntry> getDrudges() { return drudges; }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
