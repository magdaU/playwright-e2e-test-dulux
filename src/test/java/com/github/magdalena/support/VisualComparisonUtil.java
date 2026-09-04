package com.github.magdalena.support;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import io.qameta.allure.Allure;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Self-hosted visual regression assertions. Playwright's Java bindings don't ship the
 * snapshot-testing utilities the Node test runner has (assertThat(page).hasScreenshot()),
 * so comparison is done here with the image-comparison library instead.
 *
 * Baselines live in {@code src/test/resources/visual-baselines} and are committed to the
 * repo. The first run against a new snapshot name has nothing to compare against, so it
 * writes the current screenshot as the baseline rather than failing.
 */
public final class VisualComparisonUtil {

    private static final Path BASELINE_DIR = Paths.get("src", "test", "resources", "visual-baselines");
    private static final Path DIFF_DIR = Paths.get("target", "visual-diffs");

    private VisualComparisonUtil() {
    }

    public static void assertMatchesBaseline(byte[] actualPng, String snapshotName) {
        try {
            Path baselinePath = BASELINE_DIR.resolve(snapshotName + ".png");
            BufferedImage actual = readImage(actualPng);

            if (Files.notExists(baselinePath)) {
                Files.createDirectories(BASELINE_DIR);
                ImageIO.write(actual, "png", baselinePath.toFile());
                Allure.addAttachment(snapshotName + " (baseline created)", new ByteArrayInputStream(actualPng));
                return;
            }

            BufferedImage expected = ImageIO.read(baselinePath.toFile());
            // Baselines are captured on whatever OS generated them (locally on Windows, or
            // in CI on Ubuntu); font anti-aliasing/hinting differs enough between platforms
            // that pixel-identical text never matches at the library's default 0% allowance.
            // A small non-zero allowance absorbs that rendering noise without hiding a real
            // layout regression, which changes far more than 0.5% of the page.
            ImageComparisonResult result = new ImageComparison(expected, actual)
                    .setAllowingPercentOfDifferentPixels(0.5)
                    .compareImages();

            Allure.addAttachment(snapshotName + " (actual)", new ByteArrayInputStream(actualPng));
            attachDiffIfMismatched(result, snapshotName);

            assertThat(result.getImageComparisonState())
                    .as("Visual regression check for '%s' — see attached diff if this fails", snapshotName)
                    .isEqualTo(ImageComparisonState.MATCH);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not run visual comparison for " + snapshotName, e);
        }
    }

    private static void attachDiffIfMismatched(ImageComparisonResult result, String snapshotName) throws IOException {
        if (result.getImageComparisonState() == ImageComparisonState.MATCH) {
            return;
        }

        Files.createDirectories(DIFF_DIR);
        Path diffPath = DIFF_DIR.resolve(snapshotName + "_diff.png");
        ImageIO.write(result.getResult(), "png", diffPath.toFile());

        ByteArrayOutputStream diffBytes = new ByteArrayOutputStream();
        ImageIO.write(result.getResult(), "png", diffBytes);
        Allure.addAttachment(snapshotName + " (diff)", new ByteArrayInputStream(diffBytes.toByteArray()));
    }

    private static BufferedImage readImage(byte[] png) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(png));
    }
}
