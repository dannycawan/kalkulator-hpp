package com.kalkulator.hpp

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kalkulator.hpp.data.local.entity.Recipe
import com.kalkulator.hpp.di.AppModule
import com.kalkulator.hpp.ui.screen.*
import com.kalkulator.hpp.ui.viewmodel.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    data object Ingredients : Screen("ingredients", "Bahan", Icons.Default.Inventory2)
    data object Recipes : Screen("recipes", "Resep", Icons.Default.MenuBook)
    data object Calculator : Screen("calculator", "HPP", Icons.Default.Calculate)
    data object More : Screen("more", "Lainnya", Icons.Default.MoreHoriz)
    // Secondary screens (not in bottom nav)
    data object Equipment : Screen("equipment", "Alat", Icons.Default.Build)
    data object Overhead : Screen("overhead", "Overhead", Icons.Default.Receipt)
    data object History : Screen("history", "Riwayat", Icons.Default.History)
    data object Templates : Screen("templates", "Template", Icons.Default.AutoAwesome)
    data object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
    data object RecipeDetail : Screen("recipe/{recipeId}", "Detail Resep", Icons.Default.MenuBook)
}

private val bottomItems = listOf(Screen.Dashboard, Screen.Ingredients, Screen.Recipes, Screen.Calculator, Screen.More)

@Composable
fun NavGraph(appModule: AppModule, useDarkTheme: Boolean) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // ViewModels
    val ingredientVM: IngredientViewModel = viewModel(factory = IngredientViewModel.Factory(appModule.ingredientRepository))
    val recipeVM: RecipeViewModel = viewModel(factory = RecipeViewModel.Factory(appModule.recipeRepository))
    val calculatorVM: CalculatorViewModel = viewModel(factory = CalculatorViewModel.Factory(appModule.calculationRepository))
    val historyVM: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(appModule.calculationRepository))
    val dashboardVM: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(appModule.recipeRepository, appModule.calculationRepository))
    val equipmentVM: EquipmentViewModel = viewModel(factory = EquipmentViewModel.Factory(appModule.equipmentRepository))
    val overheadVM: OverheadViewModel = viewModel(factory = OverheadViewModel.Factory(appModule.overheadRepository))
    val settingsVM: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(context))

    val dailyProduction by settingsVM.dailyProduction.collectAsState()
    val totalDepreciation by equipmentVM.totalMonthlyDepreciation.collectAsState()
    val totalOverhead by overheadVM.totalMonthlyCost.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determine if bottom bar should be shown
    val showBottomBar = currentRoute in bottomItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
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
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Dashboard.route, Modifier.padding(innerPadding)) {

            // === Bottom Nav Screens ===
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardVM,
                    onNavigateToRecipes = { navController.navigate(Screen.Recipes.route) },
                    onNavigateToCalculator = { navController.navigate(Screen.Calculator.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Ingredients.route) {
                IngredientScreen(ingredientVM)
            }

            composable(Screen.Recipes.route) {
                RecipeScreen(recipeVM) { recipeId ->
                    navController.navigate("recipe/$recipeId")
                }
            }

            composable(Screen.Calculator.route) {
                CalculatorScreen(
                    calculatorViewModel = calculatorVM,
                    recipeViewModel = recipeVM,
                    totalMonthlyDepreciation = totalDepreciation,
                    totalMonthlyOverhead = totalOverhead,
                    dailyProduction = dailyProduction
                )
            }

            composable(Screen.More.route) {
                MoreMenuScreen { route ->
                    navController.navigate(route)
                }
            }

            // === Secondary Screens ===
            composable(Screen.Equipment.route) {
                EquipmentScreen(equipmentVM, dailyProduction)
            }

            composable(Screen.Overhead.route) {
                OverheadScreen(overheadVM, dailyProduction)
            }

            composable(Screen.History.route) {
                HistoryScreen(historyVM)
            }

            composable(Screen.Templates.route) {
                TemplateScreen { template ->
                    // Create recipe from template
                    recipeVM.insert(Recipe(
                        name = template.name,
                        description = template.description,
                        category = template.category,
                        laborCost = template.laborCost,
                        yield = template.yield
                    ))
                    // Navigate to recipes to see the new recipe
                    navController.navigate(Screen.Recipes.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            }

            composable(Screen.Settings.route) {
                SettingsScreen(settingsVM)
            }

            // Recipe detail
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
