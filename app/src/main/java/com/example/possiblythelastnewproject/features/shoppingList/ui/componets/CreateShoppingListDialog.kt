package com.example.possiblythelastnewproject.features.shoppingList.ui.componets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.possiblythelastnewproject.features.pantry.ui.PantryViewModel
import com.example.possiblythelastnewproject.features.recipe.ui.RecipesViewModel
import androidx.compose.ui.focus.FocusRequester


@Composable
fun CreateShoppingListDialog(
    existingListNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val dummyFocusRequester = remember { FocusRequester() }

    var name by remember { mutableStateOf("") }

    val nameExists = remember(name, existingListNames) {
        existingListNames.any { it.equals(name.trim(), ignoreCase = true) }
    }

    val isValid = name.isNotBlank() && !nameExists

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Create New Shopping List",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("List Name") },
                        isError = nameExists,
                        supportingText = {
                            if (nameExists) {
                                Text(
                                    "A list with this name already exists.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            dummyFocusRequester.requestFocus()
                            keyboardController?.hide()
                        })
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                dummyFocusRequester.requestFocus()
                                keyboardController?.hide()
                                onConfirm(name.trim())
                            },
                            enabled = isValid
                        ) {
                            Text("Create")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(1.dp)
                            .focusRequester(dummyFocusRequester)
                            .focusable()
                    )
                }
            }
        }
    }
}