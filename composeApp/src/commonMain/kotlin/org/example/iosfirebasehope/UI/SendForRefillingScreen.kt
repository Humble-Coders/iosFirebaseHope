package org.example.iosfirebasehope.UI


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.iosfirebasehope.navigation.components.SendForRefillingComponent
import org.example.iosfirebasehope.navigation.events.SendForRefillingEvent


@Composable
fun SendForRefillingScreenUI(
    component: SendForRefillingComponent,
    VendorName: String,
    db: FirebaseFirestore
) {



    // Use the platform-specific BackButtonHandler

    // Existing state variables
    val details = remember { mutableStateOf<Map<String, String>?>(null) }
    val creditValue = remember { mutableStateOf<String?>(null) }
    val phoneNumberValue = remember { mutableStateOf<String?>(null) }
    var showAddVendorDialog by remember { mutableStateOf(false) }
    var showAddCylinderDialog by remember { mutableStateOf(false) }
    var selectedVendor by remember { mutableStateOf<String?>(null) }
    var Vendors by remember { mutableStateOf<List<String>>(emptyList()) }
    var issuedCylinders by remember { mutableStateOf<List<IssuedCylinder>>(emptyList()) }
    var issueDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var alreadySelectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var alreadySelectedLPGQuantities by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var currentDateTime by remember {mutableStateOf("")}



    var isBackButtonEnabled = remember { mutableStateOf(true) }

    // New state variables for date pickers
    var showIssueDatePicker by remember { mutableStateOf(false) }
    var selectedIssueDate by remember { mutableStateOf<Long?>(null) }

    // State variable for bottom buttons visibility
    var showBottomButtons by remember { mutableStateOf(true) }

    // State variable for checkout dialog
    var showCheckoutDialog by remember { mutableStateOf(false) }

    // State variable for validation dialog
    var showValidationDialog by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }

    // Calculate the real-time total price


    // Fetch all fields from Firestore
    LaunchedEffect(VendorName) {
        val document = db.collection("Vendors")
            .document("Details")
            .collection("Names")
            .document(VendorName)
            .get()

        // Extract the "Details" map from the document
        details.value = document.get("Details") as? Map<String, String>

        // Extract the "Deposit", "Credit", and "Phone Number" values from the "Details" map
        creditValue.value = details.value?.get("Credit")?.toString()
        phoneNumberValue.value = details.value?.get("Phone Number")?.toString()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top bar with higher zIndex to ensure it stays on top
        Surface(
            color = Color(0xFF2f80eb),
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f) // Higher zIndex to ensure it stays on top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    enabled = isBackButtonEnabled.value,
                    onClick = { component.onEvent(SendForRefillingEvent.OnBackClick) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Send For Refilling",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Main content (below the top bar)
        Scaffold(
            topBar = {
                // Empty top bar (since we're manually placing the top bar above)
                Box {}
            }
        ) { innerPadding ->
            // Animate the main content sliding out when the dialog is shown
            AnimatedVisibility(
                visible = !showAddCylinderDialog, // Check both dialogs
                enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(durationMillis = 300))
            ) {
                Column(modifier = Modifier.padding(innerPadding)) {
                    // Box displaying current cylinder details
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, top = 64.dp) // Increased top and bottom padding
                            .background(Color(0xFFF3F4F6))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp), // Increased padding
                            verticalArrangement = Arrangement.spacedBy(8.dp) // Increased spacing
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = VendorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp // Increased font size
                                )
                            }
                            Divider()

                            // Display "Phone", "Deposit", and "Credit" in a single row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Top
                            ) {
                                // Column for keys
                                Column(
                                    modifier = Modifier.weight(0.3f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp) // Increased spacing
                                ) {
                                    Text(
                                        text = "Phone:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp // Increased font size
                                    )
                                    Text(
                                        text = "Credit:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp // Increased font size
                                    )
                                }

                                // Column for values
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp) // Increased spacing
                                ) {
                                    phoneNumberValue.value?.let { phoneNumber ->
                                        Text(
                                            text = phoneNumber,
                                            fontSize = 14.sp // Increased font size
                                        )
                                    }
                                    creditValue.value?.let { credit ->
                                        Text(
                                            text = credit,
                                            fontSize = 14.sp // Increased font size
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Add buttons below the grey box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp), // Reduced vertical padding
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // "Add Cylinder" button
                        Button(
                            onClick = {
                                showAddCylinderDialog = true
                                showBottomButtons = false // Hide bottom buttons
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                                .height(32.dp), // Reduced button height
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF2f80eb) // Use the same color as the top bar
                            ),
                            shape = RoundedCornerShape(8.dp) // Rectangular with rounded borders
                        ) {
                            Text(
                                text = "Add Cylinder",
                                fontSize = 12.sp, // Reduced font size
                                color = Color.White // White text
                            )
                        }


                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // "Current Cart" text with a Divider in the same row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (issuedCylinders.isEmpty()) "Current Cart: Empty" else "Current Cart",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp) // Add padding to separate text and divider
                        )
                        Divider(
                            modifier = Modifier
                                .weight(1f) // Take remaining space
                                .height(1.dp), // Thin divider
                            color = Color.Gray
                        )
                    }


                    CombinedListRef(
                        issuedCylinders = issuedCylinders,
                        onDeleteCylinder = { cylinder ->
                            // Filter out the deleted cylinders from the issuedCylinders list
                            val deletedCylinders = issuedCylinders.filter {
                                it.gasType == cylinder.gasType && it.volumeType == cylinder.volumeType
                            }

                            // Update the issuedCylinders list by removing the deleted cylinders
                            issuedCylinders = issuedCylinders.filterNot {
                                it.gasType == cylinder.gasType && it.volumeType == cylinder.volumeType
                            }

                            // Remove the serial numbers of the deleted cylinders from alreadySelectedCylinders
                            alreadySelectedCylinders = alreadySelectedCylinders.filterNot { serialNumber ->
                                deletedCylinders.any { it.serialNumber == serialNumber }
                            }

                            // If the deleted cylinders are LPG, update the alreadySelectedLPGQuantities
                            if (cylinder.gasType == "LPG") {
                                val formattedVolumeType = cylinder.volumeType.replace(".", ",")
                                val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                                alreadySelectedLPGQuantities = alreadySelectedLPGQuantities + mapOf(
                                    formattedVolumeType to (alreadySelectedQuantity - cylinder.quantity)
                                )
                            }
                        },
                        alreadySelectedLPGQuantities = alreadySelectedLPGQuantities,
                        onUpdateAlreadySelectedLPGQuantities = { updatedMap ->
                            alreadySelectedLPGQuantities = updatedMap
                        }
                    )
                }
            }
        }

        // Overlay the Add Cylinder dialog box on top of everything (but under the top bar)
        if (showAddCylinderDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f) // Ensure the dialog is above the main content but under the top bar
            ) {
                AddCylinderDialogRef(
                    onDismiss = {
                        isBackButtonEnabled.value = true // Enable the back button
                        showAddCylinderDialog = false
                        showBottomButtons = true // Show bottom buttons
                    },
                    onAddCylinder = { issuedCylinder ->
                        issuedCylinders = issuedCylinders + issuedCylinder
                        // Update alreadySelectedLPGQuantities if the gas type is LPG
                        if (issuedCylinder.gasType == "LPG") {
                            val formattedVolumeType = issuedCylinder.volumeType.replace(".", ",")
                            val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                            alreadySelectedLPGQuantities = alreadySelectedLPGQuantities + mapOf(
                                formattedVolumeType to (alreadySelectedQuantity + issuedCylinder.quantity)
                            )
                        }
                    },
                    db = db,
                    alreadySelectedCylinders = alreadySelectedCylinders,
                    onUpdateAlreadySelectedCylinders = { updatedList ->
                        alreadySelectedCylinders = updatedList
                    },
                    isBackButtonEnabled = isBackButtonEnabled
                    ,
                    alreadySelectedLPGQuantities = alreadySelectedLPGQuantities // Pass the state
                )
            }
        }




        // Bottom buttons with animation
        AnimatedVisibility(
            visible = showBottomButtons,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(durationMillis = 300)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(durationMillis = 300)),
            modifier = Modifier
                .align(Alignment.BottomCenter) // Align to the bottom of the screen
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Add some padding around the buttons
            ) {
                // First Row: Issue Date and Return Date Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp), // Add small bottom padding to separate rows
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Issue Date Button
                    Button(
                        onClick = { showIssueDatePicker = true },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                            .height(36.dp), // Thin button
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp) // Add horizontal padding
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Issue Date",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp) // Adjust icon size
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedIssueDate != null) {
                                    "Issue: ${LocalDate.fromEpochDays((selectedIssueDate!! / (1000 * 60 * 60 * 24)).toInt())}"
                                } else {
                                    "Issue Date"
                                },
                                fontSize = 12.sp, // Smaller font size
                                color = Color.White,
                                maxLines = 1, // Prevent text wrapping
                                overflow = TextOverflow.Ellipsis // Add ellipsis if text overflows
                            )
                        }
                    }

                    // Return Date Button
                }

                // Second Row: Total Button (full width)
                Button(
                    onClick = {
                        // Validate conditions
                        val errorMessage = validateCheckoutConditionsRef(
                            issuedCylinders,
                            selectedIssueDate
                        )
                        if (errorMessage == null) {
                            showCheckoutDialog = true // Show checkout dialog if validation passes
                        } else {
                            // Show validation dialog with the relevant message
                            validationMessage = errorMessage
                            showValidationDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp), // Thin button
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)) // Green color
                ) {
                    Text(
                        text = "Checkout",
                        fontSize = 14.sp, // Slightly larger font size for emphasis
                        color = Color.White,
                        maxLines = 1, // Prevent text wrapping
                        overflow = TextOverflow.Ellipsis // Add ellipsis if text overflows
                    )
                }
            }
        }

        // Date picker modals
        if (showIssueDatePicker) {
            DatePickerModal(
                onDateSelected = { dateMillis ->
                    selectedIssueDate = dateMillis
                    showIssueDatePicker = false
                },
                onDismiss = { showIssueDatePicker = false }
            )
        }


        if (showCheckoutDialog) {
            CheckoutDialogRef(

                selectedIssueDate = selectedIssueDate,
                onDismiss = { showCheckoutDialog = false },
                onConfirm = { cash, credit ->
                    // Call the checkoutCylinders function
                    coroutineScope.launch {
                        val issueDateString = selectedIssueDate?.let {
                            LocalDate.fromEpochDays((it / (1000 * 60 * 60 * 24)).toInt()).toString()
                        } ?: ""


                        // Push transaction details to Firestore
                        val success = checkoutCylindersRef(
                            db = db,
                            VendorName = VendorName,
                            issuedCylinders = issuedCylinders,
                            issueDate = issueDateString,
                            cash = cash,
                            credit = credit,
                            onCurrentDateTime = { currentDateTime = it }

                        )

                        if (success) {
                            component.onEvent(SendForRefillingEvent.OnChallanClick(VendorName, currentDateTime))
                            showCheckoutDialog = false // Close the dialog
                            println("Transaction successfully pushed to Firestore!")
                        } else {
                            println("Failed to push transaction to Firestore.")
                        }
                    }
                }
            )
        }

        // Validation dialog
        if (showValidationDialog) {
            ValidationDialogRef(
                message = validationMessage,
                onDismiss = { showValidationDialog = false } // Close the dialog
            )
        }
    }
}

@Composable
fun ValidationDialogRef(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)) // Blue color
            ) {
                Text("Close", color = Color.White)
            }
        }
    )
}

fun validateCheckoutConditionsRef(
    issuedCylinders: List<IssuedCylinder>,
    selectedIssueDate: Long?
): String? {
    return when {
        issuedCylinders.isEmpty() -> "Nothing added to cart."
        selectedIssueDate == null -> "No issue date selected."
        else -> null // No error
    }
}

@Composable
fun CheckoutDialogRef(
    selectedIssueDate: Long?,
    onDismiss: () -> Unit,
    onConfirm: (cash: String, credit: String) -> Unit
) {
    var cash by remember { mutableStateOf("") }
    var credit by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Checkout")
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF2f80eb)
                    )
                }
            }
        },
        text = {
            Column {
                // Display issue date
                Text(
                    text = "Issue Date: ${selectedIssueDate?.let {
                        LocalDate.fromEpochDays((it / (1000 * 60 * 60 * 24)).toInt())
                    } ?: "Not selected"}",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Cash input
                OutlinedTextField(
                    value = cash,
                    onValueChange = { cash = it },
                    label = { Text("Cash") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Credit input
                OutlinedTextField(
                    value = credit,
                    onValueChange = { credit = it },
                    label = { Text("Credit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    onConfirm(cash, credit)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                enabled = !isLoading
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb)),
                enabled = !isLoading
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
fun CombinedListRef(
    issuedCylinders: List<IssuedCylinder>,
    onDeleteCylinder: (IssuedCylinder) -> Unit,
    alreadySelectedLPGQuantities: Map<String, Int>,
    onUpdateAlreadySelectedLPGQuantities: (Map<String, Int>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 100.dp) // Add padding to avoid overlapping with bottom buttons
    ) {
        // Add cylinders to the list
        issuedCylinders
            .groupBy { Pair(it.gasType, it.volumeType) } // Group cylinders by gasType and volumeType
            .forEach { (_, groupCylinders) ->
                // Calculate total quantity and total price for the group
                val totalQuantity = groupCylinders.sumOf { it.quantity }
                val totalPrice = if (groupCylinders.first().gasType == "LPG") {
                    // For LPG cylinders, calculate total price as quantity * price per unit
                    groupCylinders.sumOf { it.quantity * it.totalPrice }
                } else {
                    // For non-LPG cylinders, use the existing totalPrice logic
                    groupCylinders.sumOf { it.totalPrice }
                }

                // Create a grouped cylinder for display
                val groupedCylinder = IssuedCylinder(
                    serialNumber = "", // Not needed for grouped card
                    gasType = groupCylinders.first().gasType,
                    volumeType = groupCylinders.first().volumeType,
                    quantity = totalQuantity,
                    totalPrice = totalPrice
                )

                item {
                    IssuedCylinderCardRef(
                        cylinder = groupedCylinder,
                        onDelete = {
                            // Delete all cylinders in this group
                            groupCylinders.forEach { onDeleteCylinder(it) }

                            // If the group is LPG, update the alreadySelectedLPGQuantities
                            if (groupedCylinder.gasType == "LPG") {
                                val formattedVolumeType = groupedCylinder.volumeType.replace(".", ",")
                                val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                                onUpdateAlreadySelectedLPGQuantities(
                                    alreadySelectedLPGQuantities + mapOf(
                                        formattedVolumeType to (alreadySelectedQuantity - groupedCylinder.quantity)
                                    )
                                )
                            }
                        }
                    )
                }
            }


    }
}




@Composable
fun AddCylinderDialogRef(
    onDismiss: () -> Unit,
    onAddCylinder: (IssuedCylinder) -> Unit,
    db: FirebaseFirestore,
    alreadySelectedCylinders: List<String>,
    onUpdateAlreadySelectedCylinders: (List<String>) -> Unit,
    alreadySelectedLPGQuantities: Map<String, Int>,
    isBackButtonEnabled: MutableState<Boolean>
) {
    isBackButtonEnabled.value = false // Disable the back button for this dial,
    var gasType by remember { mutableStateOf<String?>(null) }
    var volumeType by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf("") }
    var selectedCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var prices by remember { mutableStateOf("") }
    var totalPrice by remember { mutableStateOf<Double>(0.0) }
    var cylinderOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var volumeOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var availableCylinders by remember { mutableStateOf<List<String>>(emptyList()) }
    var localAlreadySelectedCylinders by remember { mutableStateOf(alreadySelectedCylinders) }
    var showValidationMessage by remember { mutableStateOf(false) } // State for validation message
    var availableLPGQuantity by remember { mutableStateOf<Int?>(null) } // State for available LPG quantity
    var quantityError by remember { mutableStateOf<String?>(null) } // State for quantity error message

    val coroutineScope = rememberCoroutineScope()

    // Fetch gas types
    LaunchedEffect(Unit) {
        val gases = db.collection("Gases").get().documents
        cylinderOptions = gases.map { it.id }
    }

    // Fetch volume types when gasType changes
    LaunchedEffect(gasType) {
        if (gasType != null) {
            val document = db.collection("Gases").document(gasType!!).get()
            val volumesAndSP = document.get("VolumesAndSP") as? Map<String, Int>
            volumesAndSP?.let {
                volumeOptions = it.keys.map { volume -> volume.replace(",", ".") }
            }
        } else {
            volumeOptions = emptyList()
        }
        // Clear available LPG quantity when gas type changes
        availableLPGQuantity = null
    }

    // Fetch available cylinders when gasType or volumeType changes (only for non-LPG gas types)
    LaunchedEffect(gasType, volumeType) {
        if (gasType == "LPG" && volumeType != null) {
            coroutineScope.launch {
                val lpgDocument = db.collection("Cylinders").document("LPG").get()
                val lpgEmptyMap = lpgDocument.get("LPGEmpty") as? Map<String, Int>
                lpgEmptyMap?.let {
                    // Replace commas in key names with dots
                    val formattedVolumeType = volumeType!!.replace(".", ",")
                    val totalQuantity = it[formattedVolumeType] ?: 0
                    val alreadySelectedQuantity = alreadySelectedLPGQuantities[formattedVolumeType] ?: 0
                    availableLPGQuantity = totalQuantity - alreadySelectedQuantity
                }
            }
        } else {
            availableCylinders = emptyList()
            availableLPGQuantity = null
        }
    }

    // Initialize selectedCylinders when quantity changes (only for non-LPG gas types)
    LaunchedEffect(quantity.toIntOrNull()) {
        if (gasType != "LPG") {
            val quantityInt = quantity.toIntOrNull() ?: 0
            selectedCylinders = List(quantityInt) { "" }
        }
    }

    // Hide validation message after 3 seconds
    LaunchedEffect(showValidationMessage) {
        if (showValidationMessage) {
            delay(3000) // 3 seconds
            showValidationMessage = false
        }
    }

    // Hide quantity error message after 3 seconds
    LaunchedEffect(quantityError) {
        if (quantityError != null) {
            delay(3000) // 3 seconds
            quantityError = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top transparent spacer
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Dialog box content
        item {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colors.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Add Cylinder", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                        // Gas Type Dropdown
                        Text("Gas Type", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        SearchableDropdownRef(
                            options = cylinderOptions,
                            selectedItem = gasType,
                            onItemSelected = { gasType = it },
                            placeholder = "Select Gas Type"
                        )

                        // Volume Type Dropdown
                        Text("Volume Type", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        SearchableDropdownRef(
                            options = volumeOptions,
                            selectedItem = volumeType,
                            onItemSelected = { volumeType = it },
                            placeholder = "Select Volume Type"
                        )

                        // Display available LPG quantity if gasType is LPG
                        if (gasType == "LPG" && availableLPGQuantity != null) {
                            Text(
                                text = "Available LPG Quantity: $availableLPGQuantity",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Text("Quantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        // Quantity Input
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Display quantity error message
                        if (quantityError != null) {
                            Text(
                                text = quantityError!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Cylinder Dropdowns (only for non-LPG gas types)
                        if (gasType != "LPG" && quantity.toIntOrNull() != null) {
                            LaunchedEffect(quantity.toIntOrNull()) {
                                localAlreadySelectedCylinders = emptyList()
                                selectedCylinders = List(quantity.toInt()) { "" }
                                if (gasType != null && volumeType != null) {
                                    coroutineScope.launch {
                                        val allCylinders = fetchCylindersByStatus(db, gasType!!, volumeType!!, "Empty")
                                        availableCylinders = allCylinders.filter { it !in alreadySelectedCylinders }
                                    }
                                }
                            }

                            repeat(quantity.toInt()) { index ->
                                Text("Cylinder ${index + 1}", fontWeight = FontWeight.Bold)
                                SearchableDropdownRef(
                                    options = availableCylinders,
                                    selectedItem = selectedCylinders.getOrNull(index),
                                    onItemSelected = { selectedCylinder ->
                                        selectedCylinders = selectedCylinders.toMutableList().apply { set(index, selectedCylinder) }
                                        localAlreadySelectedCylinders = localAlreadySelectedCylinders + selectedCylinder + alreadySelectedCylinders
                                        availableCylinders = availableCylinders.filter { it != localAlreadySelectedCylinders[index] }
                                    },
                                    placeholder = "Select Cylinder",
                                )
                            }
                        }

                        // Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Validation message
                            if (showValidationMessage) {
                                Text(
                                    text = "All fields are necessary",
                                    color = Color.Red,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            // Cancel Button
                            TextButton(onClick = onDismiss
                            ) {
                                Text("Cancel", color = Color(0xFF2f80eb))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Add Button
                            Button(
                                onClick = {
                                    // Check if all fields are filled
                                    if (gasType.isNullOrEmpty() || volumeType.isNullOrEmpty() || quantity.isEmpty()  || (gasType != "LPG" && selectedCylinders.any { it.isEmpty() })) {
                                        showValidationMessage = true
                                    } else {
                                        val quantityInt = quantity.toIntOrNull() ?: 0
                                        if (gasType == "LPG" && availableLPGQuantity != null) {
                                            // Validate quantity for LPG
                                            if (quantityInt <= 0 || quantityInt > availableLPGQuantity!!) {
                                                quantityError = "Quantity must be between 1 and $availableLPGQuantity"
                                                return@Button
                                            }
                                        }
                                        totalPrice = 0.0
                                        if (gasType == "LPG") {
                                            // For LPG, create an IssuedCylinder without serial numbers
                                            onAddCylinder(
                                                IssuedCylinder(
                                                    serialNumber = "", // Leave empty for LPG
                                                    gasType = gasType ?: "",
                                                    volumeType = volumeType ?: "",
                                                    quantity = quantityInt,
                                                    totalPrice = totalPrice
                                                )
                                            )
                                        } else {
                                            // For non-LPG gas types, create IssuedCylinder for each selected cylinder
                                            selectedCylinders.forEach { serialNumber ->
                                                onAddCylinder(
                                                    IssuedCylinder(
                                                        serialNumber = serialNumber,
                                                        gasType = gasType ?: "",
                                                        volumeType = volumeType ?: "",
                                                        quantity = 1,
                                                        totalPrice = totalPrice
                                                    )
                                                )
                                            }
                                        }
                                        onUpdateAlreadySelectedCylinders(localAlreadySelectedCylinders)
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                            ) {
                                Text("Add", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Bottom transparent spacer
        item {
            Spacer(modifier = Modifier.height(500.dp))
        }
    }
}



@Composable
fun SearchableDropdownRef(
    options: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit,
    onClearSelection: () -> Unit = {}, // Added callback for clearing selection
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(selectedItem ?: "") }
    val filteredOptions = options.filter { it.contains(searchQuery, ignoreCase = true) }

    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    expanded = it.isNotEmpty()
                    if (it.isEmpty()) {
                        onClearSelection() // Notify parent when input is cleared
                    }
                },
                label = { Text(placeholder) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.surface)
                        .border(1.dp, MaterialTheme.colors.onSurface)
                        .heightIn(max = 200.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        if (filteredOptions.isEmpty()) {
                            item {
                                Text(
                                    "No options found",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colors.onSurface
                                )
                            }
                        } else {
                            items(filteredOptions.size) { index ->
                                val option = filteredOptions[index]
                                Text(
                                    text = option,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = option
                                            onItemSelected(option)
                                            expanded = false
                                        }
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

suspend fun checkoutCylindersRef(
    db: FirebaseFirestore,
    VendorName: String?,
    issuedCylinders: List<IssuedCylinder>,
    issueDate: String,
    cash: String,
    credit: String,
    onCurrentDateTime: (String) -> Unit
): Boolean {
    if (VendorName == null || issueDate.isEmpty()) return false

    try {
        // Get the current date and time in the format "yyyy-MM-dd_HH:mm:ss"
        val currentTime = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time
            .toString()

        // Combine issueDate with current time
        val dateTimeString = "${issueDate}_${currentTime}"
            .replace(":", "-")
            .substringBefore(".")

        onCurrentDateTime(dateTimeString)

        // Reference to the Transactions collection
        val transactionsRef = db.collection("TransactionVendor")
            .document(VendorName)
            .collection("DateAndTime")
            .document(dateTimeString)

        transactionsRef.set(mapOf("Date" to dateTimeString))

        // Create the "Transaction Details" collection
        val transactionDetailsRef = transactionsRef.collection("Transaction Details")

        // Push Cash document
        transactionDetailsRef.document("Cash").set(mapOf("Amount" to cash))

        // Push Credit document
        transactionDetailsRef.document("Credit").set(mapOf("Amount" to credit))

        // Push Cylinders Issued document
        val cylindersIssued = issuedCylinders.filter { it.gasType != "LPG" }.map { cylinder ->
            mapOf(
                "Serial Number" to cylinder.serialNumber,
                "Issue Date" to issueDate
            )
        }
        transactionDetailsRef.document("Cylinders Issued").set(mapOf("CylindersIssued" to cylindersIssued))

        // Push Cylinders Returned document (empty for now)
        transactionDetailsRef.document("Cylinders Returned").set(mapOf("CylindersReturned" to emptyList<String>()))

        // Push LPG Issued document
        val lpgIssued = issuedCylinders.filter { it.gasType == "LPG" }.groupBy { it.volumeType }.map { (volumeType, cylinders) ->
            mapOf(
                "Volume Type" to volumeType,
                "Quantity" to cylinders.sumOf { it.quantity },
                "Date" to issueDate
            )
        }
        transactionDetailsRef.document("LPG Issued").set(mapOf("LPGIssued" to lpgIssued))



        // Update non-LPG cylinders in Vendors > Issued Cylinders > Names > VendorName > Details
        val nonLpgCylinders = issuedCylinders.filter { it.gasType != "LPG" }
        if (nonLpgCylinders.isNotEmpty()) {
            val issuedCylindersRef = db.collection("Vendors")
                .document("Issued Cylinders")
                .collection("Names")
                .document(VendorName)

            // Fetch existing Details array
            val snapshot = issuedCylindersRef.get()
            val existingDetails = if (snapshot.exists) {
                snapshot.get("Details") as? List<String> ?: emptyList()
            } else {
                emptyList()
            }

            // Add new serial numbers to the existing array
            val newSerialNumbers = nonLpgCylinders.map { it.serialNumber }
            val updatedDetails = existingDetails + newSerialNumbers

            // Update the document with the new array
            issuedCylindersRef.set(mapOf("Details" to updatedDetails))
        }

        // Update LPG quantities in Vendors > LPG Issued > Names > VendorName
        val lpgCylinders = issuedCylinders.filter { it.gasType == "LPG" }
        if (lpgCylinders.isNotEmpty()) {
            val lpgIssuedRef = db.collection("Vendors")
                .document("LPG Issued")
                .collection("Names")
                .document(VendorName)

            // Fetch existing LPG quantities map
            val snapshot = lpgIssuedRef.get()
            val existingLpgQuantities = if (snapshot.exists) {
                snapshot.get("Quantities") as? Map<String, String> ?: emptyMap()
            } else {
                emptyMap()
            }

            // Update quantities for each volume type
            val updatedLpgQuantities = lpgCylinders.groupBy { it.volumeType.replace(".",",") }.mapValues { (volumeType, cylinders) ->
                val existingQuantity = existingLpgQuantities[volumeType]?.toIntOrNull() ?: 0
                val newQuantity = existingQuantity + cylinders.sumOf { it.quantity }
                newQuantity.toString() // Store as string
            }

            // Merge the existing quantities with the updated quantities
            val mergedLpgQuantities = existingLpgQuantities.toMutableMap().apply {
                updatedLpgQuantities.forEach { (volumeType, quantity) ->
                    this[volumeType.replace(".",",")] = quantity
                }
            }

            // Update the document with the merged map
            lpgIssuedRef.set(mapOf("Quantities" to mergedLpgQuantities))
        }

        // Update CylinderDetails in Cylinders > Cylinders > CylinderDetails
        val nonLpgCylinderSerialNumbers = nonLpgCylinders.map { it.serialNumber }
        if (nonLpgCylinderSerialNumbers.isNotEmpty()) {
            val cylindersRef = db.collection("Cylinders").document("Cylinders")

            // Fetch existing CylinderDetails array
            val snapshot = cylindersRef.get()
            val cylinderDetails = if (snapshot.exists) {
                snapshot.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
            } else {
                emptyList()
            }

            // Update the status for cylinders
            val updatedCylinderDetails = cylinderDetails.map { cylinder ->
                if (cylinder["Serial Number"] in nonLpgCylinderSerialNumbers) {
                    cylinder.toMutableMap().apply {
                        this["Status"] = "At Plant" // Use issueDate as the Notifications Date
                    }
                } else {
                    cylinder
                }
            }

            // Save the updated CylinderDetails array back to Firestore
            cylindersRef.set(mapOf("CylinderDetails" to updatedCylinderDetails))
        }

        // Create collections for each non-LPG cylinder in Cylinders > Vendors
        for (cylinder in nonLpgCylinders) {
            val cylinderRef = db.collection("Cylinders")
                .document("Vendors")
                .collection(cylinder.serialNumber)

            // Create Currently Issued To document
            cylinderRef.document("Refill To").set(
                mapOf(
                    "name" to VendorName,
                    "date" to issueDate
                )
            )

            // Add the current Vendor details to the existing array


        }
        val customerDetailsRef = db.collection("Vendors")
            .document("Details")
            .collection("Names")
            .document(VendorName)

        // Fetch the existing "Details" map
        val customerDetailsSnapshot = customerDetailsRef.get()
        if (customerDetailsSnapshot.exists) {
            val detailsMap = customerDetailsSnapshot.get("Details") as? Map<String, String> ?: emptyMap()

            // Increment the "Credit" field
            val currentCredit = detailsMap["Credit"]?.toDoubleOrNull() ?: 0.0
            val newCredit = currentCredit + (credit.toDoubleOrNull() ?: 0.0)

            // Update the "Deposit" field with the updatedDeposit value


            // Create the updated map
            val updatedDetailsMap = detailsMap.toMutableMap().apply {
                this["Credit"] = newCredit.toString()
            }

            // Save the updated map back to Firestore
            customerDetailsRef.update("Details" to updatedDetailsMap)
            println("Successfully updated 'Credit' fields for customer $VendorName")
        } else {
            throw Exception("Document 'Customers > Details > Names > $VendorName' does not exist")
        }

        // Update LPGFull map in Cylinders > LPG document
        val lpgDocumentRef = db.collection("Cylinders").document("LPG")
        val lpgDocumentSnapshot = lpgDocumentRef.get()
        if (lpgDocumentSnapshot.exists) {
            val lpgEmptyMap = lpgDocumentSnapshot.get("LPGEmpty") as? Map<String, Int> ?: emptyMap()
            val updatedLpgEmptyMap = lpgCylinders.groupBy { it.volumeType.replace(".",",") }.entries.fold(lpgEmptyMap.toMutableMap()) { acc, entry ->
                val (volumeType, cylinders) = entry
                val issuedQuantity = cylinders.sumOf { it.quantity }
                val currentQuantity = acc[volumeType] ?: 0
                acc[volumeType] = currentQuantity - issuedQuantity
                acc
            }
            lpgDocumentRef.update("LPGEmpty" to updatedLpgEmptyMap)
        }

        return true
    } catch (e: Exception) {
        println("Error in checkoutCylinders: ${e.message}")
        return false
    }
}





@Composable
fun IssuedCylinderCardRef(
    cylinder: IssuedCylinder,
    onDelete: () -> Unit // Callback for delete button
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) } // State for delete confirmation dialog

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to delete this group?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete() // Trigger the delete action
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Card content (left side)
            Column {
                Text(
                    text = cylinder.gasType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Volume Type: ${cylinder.volumeType}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Total Quantity: ${cylinder.quantity}",
                    fontSize = 14.sp
                )

            }

            // Bin icon (right side)
            IconButton(
                onClick = { showDeleteConfirmation = true }, // Show delete confirmation dialog
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}