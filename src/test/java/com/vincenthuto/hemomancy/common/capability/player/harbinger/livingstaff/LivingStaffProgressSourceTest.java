package com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class LivingStaffProgressSourceTest {

	private LivingStaffProgressSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String iface = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/livingstaff/ILivingStaffProgress.java");
		String impl = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/livingstaff/LivingStaffProgress.java");
		String attachments = read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoAttachmentTypes.java");
		String keys = read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityKeys.java");
		String registrar = read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityRegistrar.java");
		String access = read("src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityAccess.java");
		String packetHandler = read("src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java");

		assertContains("interface exposes staff bond", iface, "boolean hasLivingStaffBond()");
		assertContains("interface exposes blood handled", iface, "double getBloodHandled()");
		assertContains("interface exposes vesper awakening", iface, "boolean isVesperMemoryAwakened()");
		assertContains("interface unlocks bond idempotently", iface, "boolean unlockLivingStaffBond()");
		assertContains("interface accumulates handled blood", iface, "void addBloodHandled(double amount)");
		assertContains("interface awakens vesper idempotently", iface, "boolean awakenVesperMemory()");

		assertContains("implementation serializes bond", impl, "\"hasLivingStaffBond\"");
		assertContains("implementation serializes handled blood", impl, "\"bloodHandled\"");
		assertContains("implementation serializes vesper", impl, "\"vesperMemoryAwakened\"");
		assertContains("implementation ignores invalid handled blood", impl, "if (amount <= 0.0D)");
		assertContains("implementation clamps loaded handled blood", impl, "Math.max(0.0D");

		assertContains("attachment registered", attachments, "LIVING_STAFF_PROGRESS");
		assertContains("capability key registered", keys, "LIVING_STAFF_PROGRESS");
		assertContains("capability registered for players", registrar, "HemoCapabilityKeys.LIVING_STAFF_PROGRESS");
		assertContains("capability access helper present", access, "getLivingStaffProgress(Player player)");
		assertContains("sync packet registered", packetHandler, "LivingStaffProgressServerPacket");
	}
}
