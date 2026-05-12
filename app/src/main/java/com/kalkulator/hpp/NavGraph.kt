package com.kalkulator.hpp

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kalkulator.hpp.di.AppModule
import com.kalkulator.hpp.ui.screen.*
import com.kalkulator.hpp.ui.viewmodel.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Ingredients : Screen("ingredients", "Bahan", Icons.Default.Inventory2)
    data object Recipes : Screen("recipes", "Resep", Icons.Default.MenuBook)
    data object Calculator : Screen("calculator", "Kalkulator", Icons.Default.Calculate)
    data object History : Screen("history", "Riwayat", Icons.Default.History)
    data object RecipeDetail : Screen("recipe/{recipeId}", "Detail Resep", Icons.Default.MenuBook)
}

private val bottomItems = listOf(Screen.Ingredients, Screen.Recipes, Screen.Calculator, Screen.History)

@Composable
fun NavGraph(appModule: AppModule) {
    val navController = rememberNavController()
    val ingredientVM: IngredientViewModel = viewModel(factory = IngredientViewModel.Factory(appModule.ingredientRepository))
    val recipeVM: RecipeViewModel = viewModel(factory = RecipeViewModel.Factory(appModule.recipeRepository))
    val calculatorVM: CalculatorViewModel = viewModel(factory = CalculatorViewModel.Factory(appModule.calculationRepository))
    val historyVM: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(appModule.calculationRepository))

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Ingredients.route, Modifier.padding(innerPadding)) {
            composable(Screen.Ingredients.route) { IngredientScreen(ingredientVM) }
            composable(Screen.Recipes.route) { RecipeScreen(recipeVM) { recipeId -> navController.navigate("recipe/$recipeId") } }
            composable(Screen.Calculator.route) { CalculatorScreen(calculatorVM, recipeVM) }
            composable(Screen.History.route) { HistoryScreen(historyVM) }
            composable(
                Screen.RecipeDetail.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: return@composable
                LaunchedEffect(recipeId) { recipeVM.selectRecipe(recipeId) }
                RecipeDetailScreen(recipeVM, ingredientVM) { navController.popBackStack() }
            }
        }
    }
}
