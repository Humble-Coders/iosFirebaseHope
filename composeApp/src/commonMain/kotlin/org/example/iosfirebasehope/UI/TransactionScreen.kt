package org.example.iosfirebasehope.UI

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Month
import org.example.iosfirebasehope.navigation.components.TransactionScreenComponent
import org.example.iosfirebasehope.navigation.events.TransactionScreenEvent


@Composable
fun TransactionScreenUI(
    customerName: String,
    component: TransactionScreenComponent,
    db: FirebaseFirestore // Callback for transaction click
) {
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    // State to hold transactions grouped by date and time
    var transactions by remember { mutableStateOf<Map<String, Map<String, Any>>?>(null) }

    // Fetch transactions on launch
    LaunchedEffect(customerName) {
        try {
            // Call the suspend function to fetch transactions
            val fetchedTransactions = fetchTransactions(db, customerName)
            transactions = fetchedTransactions
        } catch (e: Exception) {
            coroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Failed to fetch transactions: ${e.message}")
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Transactions for $customerName", color = Color.White) },
                backgroundColor = Color(0xFF2f80eb),
                contentColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { component.onEvent(TransactionScreenEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (transactions != null) {
                // Display transactions grouped by date and time
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(transactions!!.entries.sortedByDescending { it.key }) { (dateTime, transactionDetails) ->
                        // Split date and time
                        val (date, time) = dateTime.split("_")

                        // Calculate counts
                        val cylindersIssuedCount = countSerialNumbers(transactionDetails["CylindersIssued"] as List<Map<String, String>>)
                        val cylindersReturnedCount = countSerialNumbers(transactionDetails["CylindersReturned"] as List<Map<String, String>>)
                        val lpgIssuedCount = sumQuantities(transactionDetails["LPGIssued"] as List<Map<String, String>>)
                        val inventoryIssuedCount = sumQuantities(transactionDetails["InventoryIssued"] as List<Map<String, String>>)

                        TransactionCard(
                            date = date,
                            price = transactionDetails["Total Price"] as String,
                            cashAmount = transactionDetails["Cash"] as String,
                            creditAmount = transactionDetails["Credit"] as String,
                            cylindersIssuedCount = cylindersIssuedCount,
                            cylindersReturnedCount = cylindersReturnedCount,
                            lpgIssuedCount = lpgIssuedCount,
                            inventoryIssuedCount = inventoryIssuedCount,
                            cashOut=transactionDetails["Cash Out"] as String,
                            onClick = {
                                // Handle transaction click
                                component.onEvent(TransactionScreenEvent.OnTransactionClick(customerName, dateTime))
                            }
                        )
                    }
                }
            } else {
                // Show loading or error message
                Text(
                    text = "Loading transactions...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// Function to count serial numbers in a list of cylinders
private fun countSerialNumbers(cylinders: List<Map<String, String>>): Int {
    var count = 0
    for (cylinder in cylinders) {
        if (cylinder["Serial Number"] != null) {
            count++
        }
    }
    return count
}

// Function to sum quantities in a list of items
private fun sumQuantities(items: List<Map<String, String>>): Int {
    var sum = 0
    for (item in items) {
        val quantity = item["Quantity"]?.toIntOrNull() ?: 0
        sum += quantity
    }
    return sum
}

@Composable
private fun TransactionCard(
    date: String,
    price: String,
    cashAmount: String,
    creditAmount: String,
    cylindersIssuedCount: Int,
    cylindersReturnedCount: Int,
    lpgIssuedCount: Int,
    inventoryIssuedCount: Int,
    onClick: () -> Unit ,// Callback for card click
    cashOut: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }, // Make the card clickable
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color(0xFFE8F5E9) // Light green background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Date and Time Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Date on the left
                Text(
                    text = date,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Time on the right
                Text(
                    text = "Price: Rs. $price",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            // Divider
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            // Cash and Credit Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cash
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cash:",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "Rs. $cashAmount",
                        fontSize = 16.sp,
                        color = Color(0xFF2E7D32)
                    )
                }

                // Credit
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Credit:",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "Rs. $creditAmount",
                        fontSize = 16.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            // Cylinders Issued and Cylinders Returned Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cylinders Issued
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if(cashOut=="0"){
                        Text(
                            text = "Issued:",
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "$cylindersIssuedCount",
                            fontSize = 16.sp,
                            color = Color(0xFF2E7D32)
                        )}
                }

                // Cylinders Returned
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Returned:",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "$cylindersReturnedCount",
                        fontSize = 16.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }

            // LPG Issued and Inventory Issued Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LPG Issued
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if(cashOut=="0") {
                        Text(
                            text = "LPG Issued:",
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "$lpgIssuedCount",
                            fontSize = 16.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                // Inventory Issued
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if(cashOut!="0"){
                        Text(
                            text = "Cash Out:",
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "Rs. $cashOut",
                            fontSize = 16.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                    else{
                        Text(
                            text = "Inventory Issued:",
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "$inventoryIssuedCount",
                            fontSize = 16.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}

// Suspend function to fetch all transactions (unchanged)
private suspend fun fetchTransactions(
    db: FirebaseFirestore,
    customerName: String
): Map<String, Map<String, Any>> = coroutineScope {
    val transactionMap = mutableMapOf<String, Map<String, Any>>()

    // First fetch all DateAndTime documents
    val transactionsSnapshot = db.collection("Transactions")
        .document(customerName)
        .collection("DateAndTime")
        .get()

    // Then parallelize the fetching of details for each date
    val deferredResults = transactionsSnapshot.documents.map { dateTimeDoc ->
        async {
            val dateTime = dateTimeDoc.id
            val transactionDetailsCollection = dateTimeDoc.reference.collection("Transaction Details")

            // Fetch all documents in parallel
            val results = awaitAll(
                async { transactionDetailsCollection.document("Cash").get() },
                async { transactionDetailsCollection.document("Credit").get() },
                async { transactionDetailsCollection.document("Cylinders Issued").get() },
                async { transactionDetailsCollection.document("Cylinders Returned").get() },
                async { transactionDetailsCollection.document("LPG Issued").get() },
                async { transactionDetailsCollection.document("Inventory Issued").get() },
                async { transactionDetailsCollection.document("Total Price").get() },
                async { transactionDetailsCollection.document("Cash Out").get() }
            )

            // Map results to their respective values
            dateTime to mapOf(
                "Cash" to (results[0].get("Amount") as? String ?: "0"),
                "Credit" to (results[1].get("Amount") as? String ?: "0"),
                "CylindersIssued" to (results[2].get("CylindersIssued") as? List<Map<String, String>> ?: emptyList()),
                "CylindersReturned" to (results[3].get("CylindersReturned") as? List<Map<String, String>> ?: emptyList()),
                "LPGIssued" to (results[4].get("LPGIssued") as? List<Map<String, String>> ?: emptyList()),
                "InventoryIssued" to (results[5].get("InventoryIssued") as? List<Map<String, String>> ?: emptyList()),
                "Total Price" to (results[6].get("Amount") as? String ?: "0"),
                "Cash Out" to (results[7].get("Amount") as? String ?: "0")
            )
        }
    }

    // Collect all results
    deferredResults.awaitAll().forEach { (dateTime, data) ->
        transactionMap[dateTime] = data
    }

    transactionMap
}