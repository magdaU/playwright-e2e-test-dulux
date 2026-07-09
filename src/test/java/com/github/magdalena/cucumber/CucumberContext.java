package com.github.magdalena.cucumber;

import com.github.magdalena.page.component.AlertComponent;
import com.github.magdalena.page.component.NavigationComponent;
import com.github.magdalena.page.pom.CartPage;
import com.github.magdalena.page.pom.ColorSelectionPage;
import com.github.magdalena.page.pom.HomePage;
import com.github.magdalena.support.PlaywrightConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class CucumberContext {

    private static final int DESKTOP_WIDTH = 1920;
    private static final int DESKTOP_HEIGHT = 1080;
    private static final int MOBILE_WIDTH = 375;
    private static final int MOBILE_HEIGHT = 667;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private boolean desktop;

    private HomePage homePage;
    private NavigationComponent navigationPage;
    private ColorSelectionPage colorSelectionPage;
    private CartPage cartPage;
    private AlertComponent alertComponent;
    private Page visualizerTab;

    public void initBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(PlaywrightConfig.isHeadless())
        );
    }

    public void useViewport(String viewport) {
        if ("mobile".equalsIgnoreCase(viewport)) {
            initContext(MOBILE_WIDTH, MOBILE_HEIGHT);
            desktop = false;
            return;
        }

        initContext(DESKTOP_WIDTH, DESKTOP_HEIGHT);
        desktop = true;
    }

    public void openEmptyCart() {
        cartPage.openCartPage();
        homePage.rejectAllCookies();
    }

    public void openHomePage() {
        homePage.openHomePage();
    }

    public void openHomePageAndRejectCookies() {
        homePage.openHomePage();
        homePage.rejectAllCookies();
    }

    public void browseToShade(String colourFamily, String shade, boolean mobileNavigation) {
        openHomePage();

        if (mobileNavigation) {
            navigationPage.clickDropdownHamburgerMenu();
        }

        navigationPage.clickDropdownFindColour();
        navigationPage.clickFindColour();
        colorSelectionPage.chooseColour(colourFamily);
        colorSelectionPage.chooseShade(shade);
    }

    public void searchForShade(String shade) {
        navigationPage.searchClickOnPage();
        navigationPage.inputColorOnSearchBoxAndEnter(shade);
    }

    public void addTesterToBasket() {
        colorSelectionPage.buyATesterColour();
        alertComponent.closeAlert();
    }

    public void openShoppingCart() {
        navigationPage.openShoppingCart();
    }

    public void openVisualizerExperience() {
        if (desktop) {
            visualizerTab = context.waitForPage(colorSelectionPage::openVisualizerApp);
            return;
        }

        colorSelectionPage.openVisualizerApp();
    }

    public byte[] takeScreenshot() {
        if (page == null || page.isClosed()) {
            return new byte[0];
        }

        return page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
    }

    public CartPage cartPage() {
        return cartPage;
    }

    public Page page() {
        return page;
    }

    public Page visualizerTab() {
        return visualizerTab;
    }

    public boolean isDesktop() {
        return desktop;
    }

    public void tearDown() {
        if (context != null) {
            context.close();
            context = null;
        }
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    private void initContext(int width, int height) {
        if (context != null) {
            context.close();
        }

        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height));
        page = context.newPage();
        homePage = new HomePage(page);
        navigationPage = new NavigationComponent(page);
        colorSelectionPage = new ColorSelectionPage(page);
        cartPage = new CartPage(page);
        alertComponent = new AlertComponent(page);
        visualizerTab = null;
    }
}
