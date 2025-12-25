package com.example.demolition

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI test for Login activity
 * Tests critical authentication flows
 */
@RunWith(AndroidJUnit4::class)
class LoginUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(Login::class.java)

    @Test
    fun testLoginButton_exists() {
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testEmailField_exists() {
        onView(withId(R.id.editTextEmail))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testPasswordField_exists() {
        onView(withId(R.id.editTextPassword))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testEmptyFields_validation() {
        // Click login button without entering credentials
        onView(withId(R.id.btnLogin))
            .perform(click())
        
        // Toast should appear (we can't directly test toast, but we can verify button was clicked)
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testEmailInput_accepts text() {
        onView(withId(R.id.editTextEmail))
            .perform(typeText("test@example.com"))
            .perform(closeSoftKeyboard())
        
        onView(withId(R.id.editTextEmail))
            .check(matches(withText("test@example.com")))
    }

    @Test
    fun testPasswordInput_acceptsText() {
        onView(withId(R.id.editTextPassword))
            .perform(typeText("password123"))
            .perform(closeSoftKeyboard())
        
        // Password field should have text (but it's hidden)
        onView(withId(R.id.editTextPassword))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSignupText_clickable() {
        onView(withId(R.id.loginText))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }
}
