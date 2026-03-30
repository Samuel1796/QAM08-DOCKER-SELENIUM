package org.example.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.example.base.BaseTest;
import org.example.utils.DriverManager;
import org.example.utils.SeleniumUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Login")
@Story("Authentication")
public class LoginTest extends BaseTest {

    @Test
    @Description("Valid credentials should navigate to the inventory page")
    void validLoginNavigatesToInventory() {
        loginPage.enterUsername("standard_user");
        loginPage.enterPassword("secret_sauce");
        loginPage.clickLogin();
        SeleniumUtils.waitForUrlContains(DriverManager.getDriver(), "inventory");
        assertThat(DriverManager.getDriver().getCurrentUrl()).contains("inventory");
    }

    @ParameterizedTest
    @MethodSource("org.example.utils.TestDataFactory#invalidCredentials")
    @Description("Invalid credentials should display an error message")
    void invalidCredentialsShowError(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLogin();
        assertThat(loginPage.isErrorDisplayed()).isTrue();
    }
}
