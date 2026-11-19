package com.exbottod.liveroom.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.exbottod.liveroom.App
import com.exbottod.liveroom.ui.home.HomeScreen
import com.exbottod.liveroom.ui.login.LoginScreen
import com.exbottod.liveroom.ui.login.LoginViewModel
import kotlinx.coroutines.launch

private object Route {
    const val LOGIN = "login"
    const val HOME = "home"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as App
    val scope = rememberCoroutineScope()

    val startRoute = if (app.authRepository.currentUserId() != null) Route.HOME else Route.LOGIN

    NavHost(navController = navController, startDestination = startRoute) {
        composable(Route.LOGIN) {
            val vm: LoginViewModel = viewModel { LoginViewModel(app.authRepository) }
            LoginScreen(
                viewModel = vm,
                onLoggedIn = { result ->
                    navController.navigate(Route.HOME) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                    navController.currentBackStackEntry?.savedStateHandle?.set("userId", result.user.id)
                },
            )
        }

        composable(Route.HOME) {
            val userId = navController.previousBackStackEntry
                ?.savedStateHandle?.get<String>("userId")
                ?: app.authRepository.currentUserId().orEmpty()

            HomeScreen(
                userId = userId,
                onLogout = {
                    scope.launch {
                        app.authRepository.logout()
                        navController.navigate(Route.LOGIN) {
                            popUpTo(Route.HOME) { inclusive = true }
                        }
                    }
                },
            )
        }
    }
}
