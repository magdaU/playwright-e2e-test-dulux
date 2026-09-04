package com.github.magdalena.page.pom;

import com.github.magdalena.page.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class CartPage extends BasePage {

    private static final String CART_PAGE_URL = "https://www.dulux.co.uk/en/store/cart";
    private static final String QUANTITY_INPUT_LABEL = "Quantity input";
    private static final String YOUR_BASKET_IS_EMPTY_TEXT = "Your basket is empty";
    private static final String CONTINUE_SHOPPING_TEXT = "Continue shopping";
    private static final String CHECKOUT_TEXT = "Checkout";

    public CartPage(Page page) {
        super(page);
    }

    public void openCartPage () {
        page.navigate(CART_PAGE_URL);
    }

    public void continueShopping() {
        // The header carries the same link, so "Continue shopping" is ambiguous
        // unless scoped to the empty-basket CTA in the main content.
        page.getByRole(AriaRole.MAIN)
                .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(CONTINUE_SHOPPING_TEXT))
                .click();
    }

    public void proceedToCheckout() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(CHECKOUT_TEXT)).click();
    }

    public Locator getQuantity() {
        // The quantity control is a <div role="group" aria-label="Quantity"> wrapping
        // decrease/input/increase elements that all mention "Quantity" in their
        // accessible name — getByLabel("Quantity") matches all 4 and fails Playwright's
        // strict-mode uniqueness. Target the input specifically by role instead.
        return page.getByRole(AriaRole.SPINBUTTON, new Page.GetByRoleOptions().setName(QUANTITY_INPUT_LABEL));
    }

    public Locator findText(String text) {
        return page.getByText(text);
    }

    public Locator getBasketEmptyText() {
        return page.getByText(YOUR_BASKET_IS_EMPTY_TEXT);
    }
}