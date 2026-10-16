package com.example.notes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notes.ui.edit.NoteEditScreen
import com.example.notes.ui.list.NoteListScreen

const val NOTE_ID_ARG = "noteId"
const val NEW_NOTE_ID = -1L

private const val ROUTE_LIST = "notes"
private const val ROUTE_EDIT = "edit/{$NOTE_ID_ARG}"

private fun editRoute(noteId: Long) = "edit/$noteId"

@Composable
fun NotesNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_LIST) {
        composable(ROUTE_LIST) {
            NoteListScreen(
                onAddNote = { navController.navigate(editRoute(NEW_NOTE_ID)) },
                onOpenNote = { id -> navController.navigate(editRoute(id)) }
            )
        }
        composable(
            route = ROUTE_EDIT,
            arguments = listOf(navArgument(NOTE_ID_ARG) { type = NavType.LongType })
        ) {
            NoteEditScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
