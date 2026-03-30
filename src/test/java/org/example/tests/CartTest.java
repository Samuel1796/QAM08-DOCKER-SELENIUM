package org.example.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.example.base.BaseTest;
import org.example.utils.DriverManager;
import org.example.utils.SeleniumUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Cart")
@Story("Product Selection")
public class CartTest extends BaseTest {

    @BeforeEach
    void login() {
        loginPage.enterUsername("standard_user");
        loginPage.enterPassword("secret_sauce");
        loginPage.clickLogin();
        SeleniumUtils.waitForUrlContains(DriverManager.getDriver(), "inventory");
    }

    @Test
    @Description("Adding a single item should increment the cart badge to 1")
    void addSingleItemIncreasesBadge() {
        inventoryPage.addItemToCartByIndex(0);
        assertThat(inventoryPage.getCartBadgeCount()).isEqualTo(1);
    }

    @Test
    @Description("Adding two items should increment the cart badge to 2")
    void addTwoItemsIncreasesBadgeToTwo() {
        inventoryPage.addItemToCartByIndex(0);
        inventoryPage.addItemToCartByIndex(1);
        assertThat(inventoryPage.getCartBadgeCount()).isEqualTo(2);
    }

    @Test
    @Description("Navigating to an empty cart should show no items")
    void emptyCartShowsNoItems() {
        inventoryPage.goToCart();
        assertThat(cartPage.getCartItemCount()).isEqualTo(0);
    }
}
