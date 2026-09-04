package com.github.magdalena.tests.visualizer;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.magdalena.tests.BaseTest;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("Colour Discovery")
@Feature("Visualizer App")
@Owner("Magdalena")
public class VisualizerAppTest extends BaseTest {

    @Test
    @Story("Visualizer opens in new tab – Desktop")
    @Severity(SeverityLevel.NORMAL)
    @Description("Desktop customer searches for Violet Morning and opens the Visualizer App; it should open in a new browser tab")
    void whenDesktop_thenOpenVisualizerAppNewTab() {
        // GIVEN
        setUpDesktop();
        String colourType = "Violet Morning";

        // WHEN
        homePage.openHomePage();
        homePage.rejectAllCookies();
        navigationPage.searchClickOnPage();
        navigationPage.inputColorOnSearchBoxAndEnter(colourType);

        Page newPage = context.waitForPage(() -> {
            colorSelectionPage.openVisualizerApp();
        });

        // THEN
        assertThat(newPage.locator("body")).isVisible();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("Screenshots/VisualizerAppTest/desktop_" + timestamp + ".png")));
        Assertions.assertThat(newPage.url()).isEqualTo("https://www.dulux.co.uk/en/articles/dulux-visualizer-app");
    }

    @Test
    @Story("Visualizer not supported – Mobile")
    @Severity(SeverityLevel.MINOR)
    @Description("Mobile customer tries to open the Visualizer App; it should show an error message as the app is not supported on mobile")
    void whenMobile_thenShowContactSupport() {
        // GIVEN
        setUpMobile();
        String colourType = "Violet Morning";

        // WHEN
        homePage.openHomePage();
        homePage.rejectAllCookies();
        navigationPage.searchClickOnPage();
        navigationPage.inputColorOnSearchBoxAndEnter(colourType);
        colorSelectionPage.openVisualizerApp();

        // THEN
        assertThat(page.locator("pre")).isVisible();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("Screenshots/VisualizerAppTest/mobile_" + timestamp + ".png")));
        Assertions.assertThat(page.locator("pre").textContent())
                .contains("Inconsistent store data, contact support@adjust.com");
    }
}