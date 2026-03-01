package ch.rechenstar.app.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.rechenstar.app.ui.theme.RechenStarTheme
import org.junit.Rule
import org.junit.Test

class BottomNavBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAllTabsExist() {
        composeTestRule.setContent {
            RechenStarTheme {
                BottomNavBar(selectedTab = 0, onTabSelected = {})
            }
        }

        composeTestRule.onNodeWithContentDescription("tab-spielen").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("tab-fortschritt").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("tab-erfolge").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("tab-einstellungen").assertIsDisplayed()
    }

    @Test
    fun testTabSelectionCallsCallback() {
        var selectedIndex = -1

        composeTestRule.setContent {
            RechenStarTheme {
                BottomNavBar(selectedTab = 0, onTabSelected = { selectedIndex = it })
            }
        }

        composeTestRule.onNodeWithContentDescription("tab-fortschritt").performClick()
        assert(selectedIndex == 1) { "Expected tab index 1, got $selectedIndex" }

        composeTestRule.onNodeWithContentDescription("tab-erfolge").performClick()
        assert(selectedIndex == 2) { "Expected tab index 2, got $selectedIndex" }

        composeTestRule.onNodeWithContentDescription("tab-einstellungen").performClick()
        assert(selectedIndex == 3) { "Expected tab index 3, got $selectedIndex" }
    }

    @Test
    fun testTabLabelsDisplayed() {
        composeTestRule.setContent {
            RechenStarTheme {
                BottomNavBar(selectedTab = 0, onTabSelected = {})
            }
        }

        composeTestRule.onNodeWithText("Spielen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fortschritt").assertIsDisplayed()
        composeTestRule.onNodeWithText("Erfolge").assertIsDisplayed()
        composeTestRule.onNodeWithText("Einstellungen").assertIsDisplayed()
    }
}
