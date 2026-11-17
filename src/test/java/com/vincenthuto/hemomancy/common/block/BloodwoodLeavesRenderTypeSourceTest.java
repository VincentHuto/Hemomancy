package com.vincenthuto.hemomancy.common.block;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class BloodwoodLeavesRenderTypeSourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private BloodwoodLeavesRenderTypeSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String model = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/block/blood_wood_leaves.json"));

		assertContains("bloodwood leaves should render with transparent cutout pixels", model,
				"\"render_type\": \"cutout\"");
	}
}
