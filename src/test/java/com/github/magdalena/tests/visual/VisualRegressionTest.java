package com.github.magdalena.tests.visual;

import com.github.magdalena.page.pom.CartPage;
import com.github.magdalena.support.VisualComparisonUtil;
import com.github.magdalena.tests.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Non-blocking visual checks for the key pages called out in the project backlog: the
 * colour finder landing page, the shade selection grid and the cart. Run standalone with
 * {@code -Dtest=VisualRegressionTest}; wired as its own soft-fail CI job so a real layout
 * change in production doesn't block the smoke suite while baselines stabilise.
 */
@Epic("Quality")
@Feature("Visual Regression")
@Owner("Magdalena")
@Tag("visual")
public class VisualRegressionTest extends BaseTest {

    private CartPage cartPage;

    @Override
    protected void createSetup(int width, int height) {
        super.createSetup(width, height);
        cartPage = new CartPage(page);
    }

    @Test
    @Story("Cart page appearance")
    @Severity(SeverityLevel.NORMAL)
    @Description("Empty basket page layout matches the last approved baseline")
    void emptyCartPage_shouldMatchBaseline() {
        setUpDesktop();
        cartPage.openCartPage();
        homePage.rejectAllCookies();

        VisualComparisonUtil.assertMatchesBaseline(page.screenshot(), "cart-page-empty");
    }

    @Test
    @Story("Colour finder landing page appearance")
    @Severity(SeverityLevel.NORMAL)
    @Description("Colour finder landing page layout matches the last approved baseline")
    void colourFinderPage_shouldMatchBaseline() {
        setUpDesktop();
        homePage.openHomePage();
        homePage.rejectAllCookies();
        navigationPage.clickDropdownFindColour();
        navigationPage.clickFindColour();

        VisualComparisonUtil.assertMatchesBaseline(page.screenshot(), "colour-finder-page");
    }

    @Test
    @Story("Shade selection page appearance")
    @Severity(SeverityLevel.NORMAL)
    @Description("Violet shade grid layout matches the last approved baseline")
    void shadeSelectionPage_shouldMatchBaseline() {
        setUpDesktop();
        homePage.openHomePage();
        homePage.rejectAllCookies();
        navigationPage.clickDropdownFindColour();
        navigationPage.clickFindColour();
        colorSelectionPage.chooseColour("Violet");

        VisualComparisonUtil.assertMatchesBaseline(page.screenshot(), "violet-shade-selection-page");
    }
}
