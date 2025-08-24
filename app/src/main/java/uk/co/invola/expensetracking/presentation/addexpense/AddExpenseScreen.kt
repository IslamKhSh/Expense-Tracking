package uk.co.invola.expensetracking.presentation.addexpense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uk.co.invola.expensetracking.presentation.composables.CategorySelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle side effects
    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is AddExpenseSideEffect.NavigateBack -> onNavigateBack()
                is AddExpenseSideEffect.ShowError -> {
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage ?: "An unknown error occurred")
            }
        } else {
            AddExpenseContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseContent(
    uiState: AddExpenseUiState,
    onEvent: (AddExpenseUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TitleField(
                value = uiState.title,
                onValueChange = { onEvent(AddExpenseUiEvent.TitleChanged(it)) },
                enabled = !uiState.isSaving,
            )

            DateField(
                value = uiState.getFormattedDate(),
                onClick = { onEvent(AddExpenseUiEvent.ShowDatePicker) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                AmountField(
                    value = uiState.amount,
                    onValueChange = { onEvent(AddExpenseUiEvent.AmountChanged(it)) },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f),
                )

                CurrencyDropdown(
                    selectedCurrency = uiState.selectedCurrency,
                    availableCurrencies = uiState.availableCurrencies,
                    isExpanded = uiState.showCurrencyDropdown && !uiState.isLoading,
                    onExpandedChange = { expanded ->
                        if (expanded && !uiState.isLoading) {
                            onEvent(AddExpenseUiEvent.ShowCurrencyDropdown)
                        } else {
                            onEvent(AddExpenseUiEvent.HideCurrencyDropdown)
                        }
                    },
                    onCurrencySelected = { currency ->
                        // Rate will be updated in ViewModel
                        onEvent(AddExpenseUiEvent.CurrencySelected(currency to 1.0))
                    },
                    onDismiss = { onEvent(AddExpenseUiEvent.HideCurrencyDropdown) },
                )
            }

            CategorySelector(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { onEvent(AddExpenseUiEvent.CategorySelected(it)) },
            )
        }

        SaveButton(
            onClick = { onEvent(AddExpenseUiEvent.SaveExpense) },
            enabled = uiState.canSave,
            isLoading = uiState.isSaving,
        )
    }

    // Date Picker Dialog
    if (uiState.showDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = uiState.selectedDate.time,
            )

        DatePickerDialog(
            onDateSelected = { dateMillis ->
                dateMillis?.let {
                    onEvent(AddExpenseUiEvent.DateSelected(java.util.Date(it)))
                }
            },
            onDismiss = { onEvent(AddExpenseUiEvent.HideDatePicker) },
            datePickerState = datePickerState,
        )
    }
}

@Composable
private fun TitleField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Title") },
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
    )
}

@Composable
private fun DateField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            label = { Text("Date") },
            readOnly = true,
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            enabled = false,
        )
    }
}

@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Amount") },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        enabled = enabled,
        singleLine = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    selectedCurrency: Pair<String, Double>,
    availableCurrencies: List<String>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier.width(120.dp),
    ) {
        OutlinedTextField(
            value = selectedCurrency.first,
            onValueChange = { },
            readOnly = true,
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Currency",
                )
            },
            modifier =
                Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
            singleLine = true,
        )

        if (availableCurrencies.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = onDismiss,
            ) {
                availableCurrencies.forEach { currency ->
                    DropdownMenuItem(
                        text = { Text(currency) },
                        onClick = {
                            onCurrencySelected(currency)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        enabled = enabled,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text("Save Expense")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState,
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
