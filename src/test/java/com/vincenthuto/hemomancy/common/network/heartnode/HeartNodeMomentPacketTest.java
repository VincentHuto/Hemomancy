package com.vincenthuto.hemomancy.common.network.heartnode;

import com.vincenthuto.hemomancy.common.world.HeartNodeMoment;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeartNodeMomentPacketTest {
	@Test
	void everyMomentSurvivesTheWire() {
		for (HeartNodeMoment moment : HeartNodeMoment.values()) {
			HeartNodeMomentPacket packet = new HeartNodeMomentPacket(new BlockPos(-31, 7, 1200), moment, 2);
			RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
			HeartNodeMomentPacket.STREAM_CODEC.encode(buf, packet);
			assertEquals(packet, HeartNodeMomentPacket.STREAM_CODEC.decode(buf));
			assertEquals(0, buf.readableBytes());
		}
	}

	@Test
	void aStageOutsideTheModelsIsClampedAndAnUnknownMomentOnlyPlaysAnEffect() {
		assertEquals(HeartNodeRules.MATURE_STAGE,
				new HeartNodeMomentPacket(BlockPos.ZERO, HeartNodeMoment.WITHERED, 99).stage());
		assertEquals(0, new HeartNodeMomentPacket(BlockPos.ZERO, HeartNodeMoment.WITHERED, -4).stage());
		assertEquals(HeartNodeMoment.PLANTED, HeartNodeMoment.byId(42));
	}
}
