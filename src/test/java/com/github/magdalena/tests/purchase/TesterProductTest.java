package com.github.magdalena.tests.purchase;

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

import com.github.magdalena.page.component.AlertComponent;
import com.github.magdalena.page.pom.CartPage;
import com.github.magdalena.tests.BaseTest;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("Shopping")
@Feature("Tester Purchase")
@Owner("Magdalena")
public class TesterProductTest extends BaseTest {

    private CartPage cartPage;
    private AlertComponent alertComponent;

    @Override
    protected void createSetup(int width, int height) {
        super.createSetup(width, height);
        cartPage = new CartPage(page);
        alertComponent = new AlertComponent(page);
    }

    @Test
    @Story("Add tester to cart – Desktop")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Desktop customer (1920×1080) navigates via top nav colour finder and adds a Gentle Lavender tester to the basket")
    void whenDesktop_thenShouldAddTesterToCart() {
        // GIVEN
        setUpDesktop();
        String colour = "Violet";
        String colourType = "Gentle Lavender";

        cartPage.openCartPage();
        homePage.rejectAllCookies();
        assertThat(cartPage.getBasketEmptyText()).isVisible();

        // WHEN
        homePage.openHomePage();
        navigationPage.clickDropdownFindColour();
        navigationPage.clickFindColour();
        colorSelectionPage.chooseColour(colour);
        colorSelectionPage.chooseShade(colourType);
        colorSelectionPage.buyATesterColour();
        alertComponent.closeAlert();
        navigationPage.openShoppingCart();

        // THEN
        assertThat(cartPage.getQuantity()).isVisible();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("Screenshots/TesterProductTest/desktop_" + timestamp + ".png")));

        Assertions.assertThat(cartPage.getQuantity().inputValue()).isEqualTo("1");
        assertThat(cartPage.findText("Dulux Colour Tester")).isVisible();
        assertThat(cartPage.findText(colourType)).isVisible();
    }

    @Test
    @Story("Add tester to cart – Mobile")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Mobile customer (375×667) navigates via hamburger menu colour finder and adds a Gentle Lavender tester to the basket")
    void whenMobile_thenShouldAddTesterToCart() {
        // GIVEN
        setUpMobile();
        String colour = "Violet";
        String colourType = "Gentle Lavender";

        cartPage.openCartPage();
        homePage.rejectAllCookies();
        assertThat(cartPage.getBasketEmptyText()).isVisible();

        // WHEN
        homePage.openHomePage();
        navigationPage.clickDropdownHamburgerMenu();
        navigationPage.clickDropdownFindColour();
        navigationPage.clickFindColour();
        colorSelectionPage.chooseColour(colour);
        colorSelectionPage.chooseShade(colourType);
        colorSelectionPage.buyATesterColour();
        alertComponent.closeAlert();
        navigationPage.openShoppingCart();

        // THEN
        assertThat(cartPage.getQuantity()).isVisible();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("Screenshots/TesterProductTest/mobile_" + timestamp + ".png")));

        Assertions.assertThat(cartPage.getQuantity().inputValue()).isEqualTo("1");
        assertThat(cartPage.findText("Dulux Colour Tester")).isVisible();
        assertThat(cartPage.findText(colourType)).isVisible();
    }
}