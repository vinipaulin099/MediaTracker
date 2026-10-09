package com.betopompolo.mediatracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.betopompolo.mediatracker.data.MediaRepository
import com.betopompolo.mediatracker.ui.screens.DetailScreen
import com.betopompolo.mediatracker.ui.screens.FormScreen
import com.betopompolo.mediatracker.ui.screens.LibraryScreen
import com.betopompolo.mediatracker.ui.state.DetailViewModel
import com.betopompolo.mediatracker.ui.state.FormViewModel
import com.betopompolo.mediatracker.ui.state.LibraryViewModel
import com.betopompolo.mediatracker.ui.theme.MediaTrackerTheme
import kotlinx.serialization.Serializable

@Serializable data object LibraryRoute : NavKey
@Serializable data object FormRoute : NavKey
@Serializable data class DetailRoute(val id: String) : NavKey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as MediaTrackerApplication).repository
        setContent {
            MediaTrackerTheme {
                MediaTrackerApp(repository, onCatalog = {
                    startActivity(Intent(this, HomeActivity::class.java))
                })
            }
        }
    }
}

@Composable
fun MediaTrackerApp(repository: MediaRepository, onCatalog: () -> Unit) {
    val backStack = rememberNavBackStack(LibraryRoute)
    val onBack: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    val navigate: (NavKey) -> Unit = { if (backStack.last() != it) backStack.add(it) }
    val libraryFactory = remember(repository) { viewModelFactory {
        initializer { LibraryViewModel(repository, createSavedStateHandle()) }
    } }
    val formFactory = remember(repository) { viewModelFactory {
        initializer { FormViewModel(repository, createSavedStateHandle()) }
    } }

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<LibraryRoute> {
                val vm: LibraryViewModel = viewModel(factory = libraryFactory)
                val state by vm.state.collectAsStateWithLifecycle()
                LibraryScreen(state, vm::search, vm::filter, vm::retry,
                    onAdd = { navigate(FormRoute) }, onOpen = { navigate(DetailRoute(it)) }, onCatalog = onCatalog)
            }
            entry<FormRoute> {
                val vm: FormViewModel = viewModel(factory = formFactory)
                val state by vm.state.collectAsStateWithLifecycle()
                FormScreen(state, vm::change, vm::submit, onBack,
                    onSaved = { id ->
                        vm.acknowledgeSaved()
                        onBack()
                        navigate(DetailRoute(id))
                    })
            }
            entry<DetailRoute> { route ->
                val factory = remember(repository, route.id) { viewModelFactory {
                    initializer { DetailViewModel(route.id, repository, createSavedStateHandle()) }
                } }
                val vm: DetailViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                DetailScreen(state, vm::changeProgress, vm::changeRating, vm::changeNotes,
                    vm::save, vm::remove, vm::retry, vm::consumeMessage, onBack,
                    onDeleted = { vm.acknowledgeDeleted(); onBack() })
            }
        }
    )
}
