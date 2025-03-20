package org.example.iosfirebasehope.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FieldValue
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import org.example.iosfirebasehope.navigation.components.AddCylinderScreenComponent
import org.example.iosfirebasehope.navigation.events.AddCylinderScreenEvent

@Composable
fun AddCylinderScreenUI(component: AddCylinderScreenComponent, db: FirebaseFirestore, cylinderDetails: List<Map<String, String>>) {
    val coroutineScope = rememberCoroutineScope()

    var serialNumber by remember { mutableStateOf("") }
    var batchNumber by remember { mutableStateOf("") }
    var selectedGasType by remember { mutableStateOf<String?>(null) }
    var selectedVolumeType by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var remarks by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    val gasTypes = remember { mutableStateListOf<String>() }
    val volumeTypes = remember { mutableStateListOf<String>() }

    var gasTypeDropdownExpanded by remember { mutableStateOf(false) }
    var volumeTypeDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    var isAddingCylinder by remember { mutableStateOf(false) } // Track if cylinder is being added
    var showDialog by remember { mutableStateOf(false) } // Control dialog visibility
    var dialogMessage by remember { mutableStateOf("") } // Store dialog message

    var isSerialNumberExists by remember { mutableStateOf(false) } // Track if Serial Number exists

    // Fetch gas types on launch
    LaunchedEffect(Unit) {
        val gases = db.collection("Gases").get().documents
        gasTypes.clear()
        gasTypes.addAll(gases.map { it.id })
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    // Function to show dialog with a message
    fun showDialogWithMessage(message: String) {
        dialogMessage = message
        showDialog = true
        // Automatically close the dialog after 3 seconds
        coroutineScope.launch {
            delay(9000)
            showDialog = false
        }
    }

    // Function to validate input fields
    fun validateInputs(): Boolean {
        return if (selectedGasType == "LPG") {
            // For LPG, check Gas Type, Volume Type, Quantity, and Status
            if (selectedGasType.isNullOrEmpty() || selectedVolumeType.isNullOrEmpty() || quantity.isEmpty() || selectedStatus.isNullOrEmpty()) {
                showDialogWithMessage("Please fill all required fields: Gas Type, Volume Type, Quantity, and Status.")
                false
            } else {
                true
            }
        } else {
            // For non-LPG, check Gas Type, Volume Type, Serial Number, and Status
            if (selectedGasType.isNullOrEmpty() || selectedVolumeType.isNullOrEmpty() || serialNumber.isEmpty() || selectedStatus.isNullOrEmpty()) {
                showDialogWithMessage("Please fill all required fields: Gas Type, Volume Type, Serial Number, and Status.")
                false
            } else {
                true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Cylinder", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(AddCylinderScreenEvent.onBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = Color(0xFF2f80eb)
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                keyboardController?.hide()
                            }
                        )
                    }
            ) {
                Column {
                    // Gas Type Dropdown
                    Text("Gas Type", color = Color(0xFF2f80eb))
                    PrettyDropdownMenu(
                        selectedItem = selectedGasType ?: "Select Gas Type",
                        items = gasTypes,
                        onItemSelected = { selectedGasType = it },
                        onDropdownExpanded = { expanded -> gasTypeDropdownExpanded = expanded },
                        dropdownExpanded = gasTypeDropdownExpanded,
                        fetchDataOnItemClick = { gas ->
                            coroutineScope.launch {
                                val document = db.collection("Gases").document(gas).get()
                                volumeTypes.clear()
                                if (gas == "LPG") {
                                    // Fetch Volume Types from VolumesAndSP map
                                    val volumesAndSP = document.get("VolumesAndSP") as? Map<String, Int>
                                    volumesAndSP?.let {
                                        volumeTypes.addAll(it.keys.map { volume -> volume.replace(",", ".") })
                                    }
                                } else {
                                    // Fetch Volume Types from Volumes map for non-LPG gases
                                    val volumesMap = document.get("VolumesAndSP") as? Map<String, Int>
                                    volumesMap?.let {
                                        volumeTypes.addAll(it.keys.map { volume -> volume.replace(",", ".") })
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Volume Type Dropdown
                    Text("Volume Type", color = Color(0xFF2f80eb))
                    PrettyDropdownMenu(
                        selectedItem = selectedVolumeType?.replace(",", ".") ?: "Select Volume Type",
                        items = volumeTypes,
                        onItemSelected = { selectedVolumeType = it.replace(".", ",") },
                        onDropdownExpanded = { expanded -> volumeTypeDropdownExpanded = expanded },
                        dropdownExpanded = volumeTypeDropdownExpanded,
                        fetchDataOnItemClick = {
                            if (selectedGasType == null) {
                                showDialogWithMessage("Please select a gas type first")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Conditional UI for Serial Number or Quantity
                    if (selectedGasType != "LPG") {
                        Text("Serial Number", color = Color(0xFF2f80eb))
                        TextField(
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.White,
                                focusedIndicatorColor = Color.White,
                                unfocusedIndicatorColor = Color.White,
                                cursorColor = Color(0xFF2f80eb)
                            ),
                            value = serialNumber,
                            onValueChange = { newSerialNumber ->
                                serialNumber = newSerialNumber
                                // Check if the Serial Number already exists in cylinderDetails
                                isSerialNumberExists = cylinderDetails.any { it["Serial Number"] == newSerialNumber }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp)),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            )
                        )
                        // Show error message if Serial Number exists
                        if (isSerialNumberExists) {
                            Text(
                                text = "Serial Number already exists!",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Text("Quantity", color = Color(0xFF2f80eb))
                        TextField(
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.White,
                                focusedIndicatorColor = Color.White,
                                unfocusedIndicatorColor = Color.White,
                                cursorColor = Color(0xFF2f80eb)
                            ),
                            value = quantity,
                            onValueChange = { quantity = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp)),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(

                                onDone = { keyboardController?.hide() }
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Batch Number Field
                    Text("Batch Number", color = Color(0xFF2f80eb))
                    TextField(
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = if (selectedGasType == "LPG") Color.LightGray else Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            cursorColor = Color(0xFF2f80eb)
                        ),
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        enabled = selectedGasType != "LPG",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selectedGasType == "LPG") Color.LightGray else Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp)),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Dropdown
                    Text("Status", color = Color(0xFF2f80eb))
                    PrettyDropdownMenu(
                        selectedItem = selectedStatus ?: "Select Status",
                        items = listOf("Full", "Empty"),
                        onItemSelected = { selectedStatus = it },
                        onDropdownExpanded = { expanded -> statusDropdownExpanded = expanded },
                        dropdownExpanded = statusDropdownExpanded
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Remarks Field
                    Text("Remarks", color = Color(0xFF2f80eb))
                    TextField(
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = if (selectedGasType == "LPG") Color.LightGray else Color.White,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.White,
                            cursorColor = Color(0xFF2f80eb)
                        ),
                        value = remarks,
                        onValueChange = { remarks = it },
                        enabled = selectedGasType != "LPG",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (selectedGasType == "LPG") Color.LightGray else Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp)),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add Cylinder Button
                    Button(
                        onClick = {
                            if (!validateInputs()) {
                                return@Button
                            }
                            isAddingCylinder = true
                            coroutineScope.launch {
                                if (selectedGasType == "LPG") {
                                    // Validate quantity
                                    val quantityInt = quantity.toIntOrNull() ?: 0
                                    if (quantityInt <= 0) {
                                        showDialogWithMessage("Please enter a valid quantity.")
                                        isAddingCylinder = false
                                        return@launch
                                    }

                                    // Update the LPG document
                                    val lpgDocumentRef = db.collection("Cylinders").document("LPG")
                                    val lpgDocument = lpgDocumentRef.get()

                                    if (lpgDocument.exists) {
                                        val volumeTypeKey = selectedVolumeType!!.replace(".", ",") // Ensure correct key format
                                        val mapToUpdate = when (selectedStatus) {
                                            "Full" -> "LPGFull"
                                            "Empty" -> "LPGEmpty"
                                            else -> {
                                                showDialogWithMessage("Invalid status selected.")
                                                isAddingCylinder = false
                                                return@launch
                                            }
                                        }

                                        // Fetch the current quantity for the selected volume type
                                        val currentMap = lpgDocument.get(mapToUpdate) as? Map<String, Int>
                                        val currentQuantity = currentMap?.get(volumeTypeKey) ?: 0

                                        // Update the quantity
                                        val updatedQuantity = currentQuantity + quantityInt
                                        val updateData = mapOf("$mapToUpdate.$volumeTypeKey" to updatedQuantity)

                                        // Perform the update
                                        lpgDocumentRef.update(updateData)

                                        showDialogWithMessage("$quantityInt LPG cylinders added to $mapToUpdate.")
                                    } else {
                                        showDialogWithMessage("LPG document not found.")
                                    }
                                } else {
                                    // Non-LPG logic
                                    val cylinderDetails = hashMapOf(
                                        "Serial Number" to serialNumber,
                                        "Batch Number" to batchNumber,
                                        "Gas Type" to selectedGasType,
                                        "Volume Type" to selectedVolumeType?.replace(",", "."),
                                        "Status" to selectedStatus,
                                        "Remarks" to remarks,
                                        "Notifications Date" to "",
                                        "Issue Date" to "",
                                        "Issued At Price" to "",
                                        "Rent" to 0
                                    )
                                    val documentRef = db.collection("Cylinders").document("Cylinders")
                                    val documentSnapshot = documentRef.get()

                                    if (documentSnapshot.exists) {
                                        val existingCylinderDetails = documentSnapshot.get("CylinderDetails") as? List<Map<String, String>>
                                        val serialNumberExists = existingCylinderDetails?.any { it["Serial Number"] == serialNumber } ?: false

                                        if (!serialNumberExists) {
                                            documentRef.update(
                                                mapOf("CylinderDetails" to FieldValue.arrayUnion(cylinderDetails))
                                            )

                                            // Create "Customers" document and nested collections for the cylinder
                                            if (serialNumber.isNotEmpty()) {
                                                val customersCollection = db.collection("Cylinders")
                                                    .document("Customers")
                                                    .collection(serialNumber)

                                                // Create "Previous Customers" document with an empty array
                                                customersCollection.document("Previous Customers")
                                                    .set(mapOf("customers" to emptyList<Map<String, String>>()))

                                                // Create "Currently Issued To" document with empty fields
                                                customersCollection.document("Currently Issued To")
                                                    .set(mapOf("name" to "", "date" to "", "price" to ""))
                                            }

                                            showDialogWithMessage("Cylinder Successfully Added.")
                                        } else {
                                            showDialogWithMessage("Cylinder with SerialNumber $serialNumber already exists.")
                                        }
                                    } else {
                                        documentRef.set(
                                            mapOf("CylinderDetails" to listOf(cylinderDetails))
                                        )

                                        // Create "Customers" document and nested collections for the cylinder
                                        if (serialNumber.isNotEmpty()) {
                                            val customersCollection = db.collection("Cylinders")
                                                .document("Customers")
                                                .collection(serialNumber)

                                            // Create "Previous Customers" document with an empty array
                                            customersCollection.document("Previous Customers")
                                                .set(mapOf("customers" to emptyList<Map<String, String>>()))

                                            // Create "Currently Issued To" document with empty fields
                                            customersCollection.document("Currently Issued To")
                                                .set(mapOf("name" to "", "date" to "", "price" to ""))
                                        }
                                    }
                                }

                                // Clear input fields after adding
                                serialNumber = ""
                                batchNumber = ""
                                selectedGasType = null
                                selectedVolumeType = null
                                selectedStatus = null
                                remarks = ""
                                quantity = ""
                                isAddingCylinder = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (isAddingCylinder) Color.Gray else Color(0xFF2f80eb)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isAddingCylinder
                    ) {
                        Text("Add Cylinder", color = Color.White)
                    }
                }
            }
        }
    )

    // Dialog for showing messages
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Message") },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(onClick = { showDialog = false }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb))) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun PrettyDropdownMenu(
    selectedItem: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    onDropdownExpanded: (Boolean) -> Unit,
    dropdownExpanded: Boolean,
    fetchDataOnItemClick: ((String) -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF2f80eb), RoundedCornerShape(8.dp))
            .clickable { onDropdownExpanded(!dropdownExpanded) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedItem,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Drop Down Arrow",
                    tint = Color.Gray
                )
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                modifier = Modifier.width(300.dp),
                onDismissRequest = { onDropdownExpanded(false) }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onItemSelected(item)
                            onDropdownExpanded(false)
                            fetchDataOnItemClick?.invoke(item)
                        }
                    ) {
                        Text(
                            text = item,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}