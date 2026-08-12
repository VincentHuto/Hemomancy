import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Deterministically paints only the reserved Crowned Refusal wing UV rectangle. */
public final class AuthorCrownedWingTexture {
	private AuthorCrownedWingTexture() {
	}

	public static void main(String[] args) throws Exception {
		Path root = Path.of("").toAbsolutePath();
		Path texture = root.resolve("src/main/resources/assets/hemomancy/textures/entity/boss/endgame/vesper_crowned_refusal.png");
		BufferedImage source = ImageIO.read(texture.toFile());
		BufferedImage image = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setComposite(AlphaComposite.Src);
			graphics.drawImage(source, 0, 0, null);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			graphics.setColor(new Color(54, 8, 15, 255));
			graphics.fillRect(612, 0, 124, 124);
			graphics.setColor(new Color(8, 5, 8, 255));
			for (int width = 0; width < 4; width++) graphics.drawRect(614 + width, 2 + width, 120 - width * 2, 120 - width * 2);
			for (int offset : new int[] { 0, 28, 56, 84 }) {
				graphics.setColor(new Color(112, 12, 26, 255));
				graphics.fillOval(620 + offset, 18 + offset % 17, 20, 14);
				graphics.setColor(new Color(151, 20, 38, 255));
				graphics.fillOval(625 + offset, 22 + offset % 17, 8, 6);
			}
			graphics.setColor(new Color(20, 4, 8, 255));
			for (int width = -1; width <= 1; width++) {
				graphics.drawLine(616, 16 + width, 730, 74 + width);
				graphics.drawLine(618, 105 + width, 728, 34 + width);
				graphics.drawLine(650 + width, 4, 694 + width, 120);
			}
		} finally {
			graphics.dispose();
		}
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (x >= 612 && x < 736 && y < 124) continue;
				if (source.getRGB(x, y) != image.getRGB(x, y)) {
					throw new IllegalStateException("wing authoring changed a pixel outside its reserved UV rectangle");
				}
			}
		}
		if (!ImageIO.write(image, "png", texture.toFile())) throw new IllegalStateException("PNG writer unavailable");
	}
}
