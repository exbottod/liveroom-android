package com.venderlive.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.venderlive.app.App
import com.venderlive.app.ui.home.HomeScreen
import com.venderlive.app.ui.login.LoginScreen
import com.venderlive.app.ui.login.LoginViewModel
import com.venderlive.app.ui.profile.ProfileSetupScreen
import com.venderlive.app.ui.profile.ProfileSetupViewModel
import com.venderlive.app.ui.room.RoomScreen
import com.venderlive.app.ui.room.RoomViewModel
import kotlinx.coroutines.launch

private object Route {
    const val LOGIN = "login"
    const val PROFILE_SETUP = "profile_setup"
    const val HOME = "home"
    const val ROOM = "room/{roomId}"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as App
    val scope = rememberCoroutineScope()

    // Returning user with a finished profile opens straight on home; a signed-in user
    // who never picked a name and gender opens on the profile screen instead.
    val startRoute = remember {
        runCatching {
            when {
                app.authRepository.currentUserId() == null -> Route.LOGIN
                app.authRepository.needsProfileSetup() -> Route.PROFILE_SETUP
                else -> Route.HOME
            }
        }.getOrDefault(Route.LOGIN)
    }

    NavHost(navController = navController, startDestination = startRoute) {
        composable(Route.LOGIN) {
            val vm: LoginViewModel = viewModel { LoginViewModel(app.authRepository) }
            LoginScreen(
                viewModel = vm,
                onGuest = {
                    navController.navigate(Route.HOME) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                },
                onLoggedIn = { result ->
                    // First login asks for name and gender; any later login goes straight in.
                    val firstTime = result.user.isNew || result.user.nickname.isNullOrBlank()
                    val target = if (firstTime) Route.PROFILE_SETUP else Route.HOME
                    navController.navigate(target) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.PROFILE_SETUP) {
            val vm: ProfileSetupViewModel = viewModel { ProfileSetupViewModel(app.authRepository) }
            ProfileSetupScreen(
                viewModel = vm,
                onDone = {
                    navController.navigate(Route.HOME) {
                        popUpTo(Route.PROFILE_SETUP) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    scope.launch {
                        app.authRepository.logout()
                        navController.navigate(Route.LOGIN) {
                            popUpTo(Route.PROFILE_SETUP) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Route.HOME) {
            val userId = app.authRepository.currentUserId().orEmpty()
            val cached = remember { runCatching { app.authRepository.cachedProfile() }.getOrNull() }

            HomeScreen(
                userId = userId,
                nickname = cached?.nickname,
                publicId = cached?.publicId,
                onOpenRoom = { id -> navController.navigate("room/$id") },
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

        composable(Route.ROOM) { entry ->
            val roomId = entry.arguments?.getString("roomId").orEmpty()
            val vm: RoomViewModel = viewModel { RoomViewModel(roomId) }
            RoomScreen(viewModel = vm, onExit = { navController.popBackStack() })
        }
    }
}
