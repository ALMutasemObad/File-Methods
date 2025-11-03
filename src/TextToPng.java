import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class TextToPng {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: TextToPng <input-text-file> <output-png>");
            System.exit(1);
        }
        System.setProperty("java.awt.headless", "true");
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        String text = Files.readString(input);
        String[] lines = text.split("\\R", -1);

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D scratchGraphics = scratch.createGraphics();
        scratchGraphics.setFont(font);
        FontMetrics metrics = scratchGraphics.getFontMetrics();
        int lineHeight = metrics.getHeight();
        int maxWidth = 0;
        for (String line : lines) {
            int width = metrics.stringWidth(line);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        scratchGraphics.dispose();

        int padding = 20;
        int imageWidth = Math.max(1, maxWidth + padding * 2);
        int imageHeight = Math.max(1, lineHeight * lines.length + padding * 2);

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(new Color(18, 18, 18));
        g2d.fillRect(0, 0, imageWidth, imageHeight);
        g2d.setColor(new Color(0, 255, 0));

        int y = padding + metrics.getAscent();
        for (String line : lines) {
            g2d.drawString(line, padding, y);
            y += lineHeight;
        }
        g2d.dispose();

        Files.createDirectories(output.getParent());
        ImageIO.write(image, "png", output.toFile());
    }
}
