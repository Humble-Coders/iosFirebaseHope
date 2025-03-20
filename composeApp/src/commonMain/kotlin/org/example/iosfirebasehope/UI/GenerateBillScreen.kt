package org.example.iosfirebasehope.UI

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.essenty.backhandler.BackHandler

import dev.gitlive.firebase.firestore.FirebaseFirestore
import org.example.iosfirebasehope.navigation.RootComponent
import org.example.iosfirebasehope.navigation.components.GenerateBillScreenComponent
import org.example.iosfirebasehope.navigation.events.GenerateBillScreenEvent


@Composable
fun GenerateBillScreenUI(
    customerName: String,
    dateTime: String,
    db: FirebaseFirestore,
    component: GenerateBillScreenComponent
) {
    var transactionDetails by remember { mutableStateOf<TransactionDetails?>(null) }

    // Fetch transaction details
    LaunchedEffect(customerName, dateTime) {
        transactionDetails = fetchTransactionDetails(db, customerName, dateTime)
    }

    // UI
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Bill Screen") },
            contentColor = Color.White,
            backgroundColor = Color(0xFF2f80eb),
            navigationIcon = {
                IconButton(onClick = {component.onEvent(GenerateBillScreenEvent.OnBackClick)}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Column (
            modifier = Modifier.weight(1f).padding(16.dp)
        ) {
            Column (
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){

                Text(
                    text = "BILL",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Gobind Traders",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Issue Date: $dateTime",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                transactionDetails?.let { details ->
                    item {
                        Text(
                            text = "Price: ${details.price} | Credit: ${details.credit}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "Cash Out: ${details.CashOut} | Cash: ${details.cash}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "Items",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    itemsIndexed(details.cylindersIssued) { index, cylinder ->
                        Text(
                            text = "${index + 1}. Cylinder: S.No :  ${cylinder["Serial Number"]} - ${cylinder["Gas Type"]} - ${cylinder["Volume Type"]} -   Rs.${cylinder["Price"]}",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    itemsIndexed(details.inventoryIssued) { index, item ->
                        Text(
                            text = "${index + details.cylindersIssued.size + 1}. Inventory:   ${item["Name"]} -    Qty:${item["Quantity"]} -    Rs.${item["Price"]}",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    itemsIndexed(details.lpgIssued) { index, lpg ->
                        Text(
                            text = "${index + details.cylindersIssued.size + details.inventoryIssued.size + 1}. LPG:   ${lpg["Volume Type"]}-     Qty: ${lpg["Quantity"]}-    Rs.${lpg["Price"]}",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    itemsIndexed(details.cylindersReturned) { index, cylinder ->
                        Text(
                            text = "${index + details.cylindersIssued.size + details.inventoryIssued.size +details.lpgIssued.size + 1}. Cylinder: S.No :  ${cylinder["Serial Number"]}",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } ?: item {
                    Text(
                        text = "Loading...",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
        Button(
            onClick = { component.onEvent(GenerateBillScreenEvent.OnBackClick) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2f80eb), contentColor = Color.White)
        ) {
            Text("Go to HomeScreen")
        }
    }
}


suspend fun fetchTransactionDetails(
    db: FirebaseFirestore,
    customerName: String,
    dateTime: String
): TransactionDetails {
    // Reference to the transaction document
    val transactionRef = db.collection("Transactions")
        .document(customerName)
        .collection("DateAndTime")
        .document(dateTime)
        .collection("Transaction Details")

    // Fetch all required documents
    val cashDoc = transactionRef.document("Cash").get()
    val creditDoc = transactionRef.document("Credit").get()
    val cylindersIssuedDoc = transactionRef.document("Cylinders Issued").get()
    val cylindersReturnedDoc = transactionRef.document("Cylinders Returned").get()
    val inventoryIssuedDoc = transactionRef.document("Inventory Issued").get()
    val lpgIssuedDoc = transactionRef.document("LPG Issued").get()
    val priceDoc=transactionRef.document("Total Price").get()
    val CashOutDoc=transactionRef.document("Cash Out").get()


    // Extract data from documents
    val price = priceDoc.get("Amount") as? String ?: "0"
    val CashOut = CashOutDoc.get("Amount") as? String ?: "0"
    val cash = cashDoc.get("Amount") as? String ?: "0"
    val credit = creditDoc.get("Amount") as? String ?: "0"
    val cylindersIssued = cylindersIssuedDoc.get("CylindersIssued") as? List<Map<String, String>> ?: emptyList()
    val cylindersReturned = cylindersReturnedDoc.get("CylindersReturned") as? List<Map<String, String>> ?: emptyList()
    val inventoryIssued = inventoryIssuedDoc.get("InventoryIssued") as? List<Map<String, String>> ?: emptyList()
    val lpgIssued = lpgIssuedDoc.get("LPGIssued") as? List<Map<String, String>> ?: emptyList()

    // Fetch the Cylinders collection to get details of all cylinders
    val cylindersCollection = db.collection("Cylinders").get()
    val cylindersDetails = cylindersCollection.documents.flatMap { doc ->
        doc.get("CylinderDetails") as? List<Map<String, String>> ?: emptyList()
    }

    // Enrich cylindersIssued with Gas Type and Volume Type from Cylinders collection
    val enrichedCylindersIssued = cylindersIssued.map { cylinder ->
        val serialNumber = cylinder["Serial Number"] ?: ""
        val cylinderDetails = cylindersDetails.find { it["Serial Number"] == serialNumber } ?: emptyMap()
        cylinder.toMutableMap().apply {
            put("Gas Type", cylinderDetails["Gas Type"] ?: "Unknown")
            put("Volume Type", cylinderDetails["Volume Type"] ?: "Unknown")
        }
    }

    return TransactionDetails(
        cash = cash,
        credit = credit,
        cylindersIssued = enrichedCylindersIssued,
        cylindersReturned = cylindersReturned,
        inventoryIssued = inventoryIssued,
        lpgIssued = lpgIssued,
        price = price,
        CashOut = CashOut
    )
}

data class TransactionDetails(
    val cash: String,
    val credit: String,
    val cylindersIssued: List<Map<String, String>>,
    val cylindersReturned: List<Map<String, String>>,
    val inventoryIssued: List<Map<String, String>>,
    val lpgIssued: List<Map<String, String>>,
    val price: String,
    val CashOut: String
)