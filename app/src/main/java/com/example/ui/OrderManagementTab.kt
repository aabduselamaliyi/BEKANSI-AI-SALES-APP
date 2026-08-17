package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderRecord
import com.example.ui.theme.*

@Composable
fun OrderManagementTab(viewModel: SalesViewModel) {
    val context = LocalContext.current
    val orders by viewModel.allOrders.collectAsState()
    var selectedFilterStage by remember { mutableStateOf("All") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedOrderForPdf by remember { mutableStateOf<OrderRecord?>(null) }
    var showPaymentDialogForOrder by remember { mutableStateOf<OrderRecord?>(null) }

    val pipelineStages = listOf(
        "Inquiry", "Quotation", "Negotiation", "Approval", "Payment",
        "Production", "Quality Check", "Dispatch", "Delivery", "Installation", "Warranty"
    )

    val filteredOrders = if (selectedFilterStage == "All") orders else orders.filter { it.orderStage == selectedFilterStage }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Quick Stats
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("11-Stage Order & Production Pipeline", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }

                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Order", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Track complete lifecycle from lead inquiry to custom production, payments, digital sign-off, and warranty.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Pipeline Stage Filter Chips
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Filter Pipeline Stage", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilterStage == "All",
                            onClick = { selectedFilterStage = "All" },
                            label = { Text("All (${orders.size})", fontSize = 11.sp) }
                        )
                        pipelineStages.take(4).forEach { stage ->
                            val count = orders.count { it.orderStage == stage }
                            FilterChip(
                                selected = selectedFilterStage == stage,
                                onClick = { selectedFilterStage = stage },
                                label = { Text("$stage ($count)", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Orders List
        items(filteredOrders, key = { it.id }) { order ->
            OrderCard(
                order = order,
                pipelineStages = pipelineStages,
                onAdvanceStage = { nextStage ->
                    viewModel.updateOrderStage(order.id, nextStage)
                    Toast.makeText(context, "Order #${order.id} advanced to $nextStage", Toast.LENGTH_SHORT).show()
                },
                onRecordPaymentClick = { showPaymentDialogForOrder = order },
                onViewPdfClick = { selectedOrderForPdf = order },
                onExportMessage = { channel ->
                    val msg = "Hello ${order.customerName}, your Bekansi order #${order.id} for '${order.productName}' is currently in [${order.orderStage}]. Balance remaining: ETB ${order.remainingBalance}. Thank you for choosing Bekansi Hardwood!"
                    Toast.makeText(context, "Exported notification to $channel: $msg", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    // PDF Preview Modal Dialog
    if (selectedOrderForPdf != null) {
        val order = selectedOrderForPdf!!
        AlertDialog(
            onDismissRequest = { selectedOrderForPdf = null },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Downloading PDF Invoice #${order.id}.pdf...", Toast.LENGTH_SHORT).show()
                        selectedOrderForPdf = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Download PDF Invoice", color = DarkCocoaBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOrderForPdf = null }) {
                    Text("Close")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = WarmMahogany)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bekansi Official Order Invoice #${order.id}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFBF8F5), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("BEKANSI FURNITURE & INTERIOR DESIGN PLC", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WarmMahogany)
                    Text("Bole Medhanialem Showroom, Addis Ababa, Ethiopia\nPhone: +251 911 223 344 | TIN: 009218349", fontSize = 11.sp, color = Color.Gray)

                    HorizontalDivider()

                    Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Delivery Address: ${order.deliveryAddress}", fontSize = 11.sp)
                    Text("Order Stage: ${order.orderStage}", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)

                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Item: ${order.productName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("ETB %,.2f".format(order.totalAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Deposit Paid:", fontSize = 11.sp, color = AccentSuccess)
                        Text("ETB %,.2f".format(order.depositPaid), fontSize = 11.sp, color = AccentSuccess)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Remaining Balance:", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        Text("ETB %,.2f".format(order.remainingBalance), fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Digital Signature & Verification QR Code", fontSize = 10.sp, color = Color.Gray)
                            Text("Signature: Verified by Client DAW/2026", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentSuccess)
                        }
                    }
                }
            }
        )
    }

    // Record Payment Dialog
    if (showPaymentDialogForOrder != null) {
        val order = showPaymentDialogForOrder!!
        var paymentInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPaymentDialogForOrder = null },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            val newDeposit = order.depositPaid + amount
                            val newRemaining = (order.totalAmount - newDeposit).coerceAtLeast(0.0)
                            val status = if (newRemaining <= 0) "Fully Paid" else "Deposit Paid"
                            viewModel.updateOrderPayment(order.id, newDeposit, newRemaining, status)
                            Toast.makeText(context, "Recorded ETB $amount payment!", Toast.LENGTH_SHORT).show()
                            showPaymentDialogForOrder = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany)
                ) {
                    Text("Record Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialogForOrder = null }) { Text("Cancel") }
            },
            title = { Text("Record Payment for Order #${order.id}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Remaining: ETB %,.2f".format(order.remainingBalance), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    OutlinedTextField(
                        value = paymentInput,
                        onValueChange = { paymentInput = it },
                        label = { Text("Payment Amount (ETB)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    // New Order Dialog
    if (showCreateDialog) {
        var custName by remember { mutableStateOf("") }
        var custPhone by remember { mutableStateOf("") }
        var prodName by remember { mutableStateOf("Wanza Curved L-Sofa 'Gara'") }
        var totalAmt by remember { mutableStateOf("135000") }
        var depAmt by remember { mutableStateOf("67500") }
        var address by remember { mutableStateOf("Bole Medhanialem, Addis Ababa") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (custName.isNotBlank() && custPhone.isNotBlank()) {
                            val total = totalAmt.toDoubleOrNull() ?: 0.0
                            val deposit = depAmt.toDoubleOrNull() ?: 0.0
                            val remaining = (total - deposit).coerceAtLeast(0.0)
                            val status = if (remaining == 0.0) "Fully Paid" else if (deposit > 0) "Deposit Paid" else "Unpaid"

                            viewModel.addOrder(
                                OrderRecord(
                                    quotationId = (100..999).random(),
                                    customerName = custName,
                                    customerPhone = custPhone,
                                    productName = prodName,
                                    totalAmount = total,
                                    depositPaid = deposit,
                                    remainingBalance = remaining,
                                    paymentStatus = status,
                                    orderStage = "Quotation",
                                    deliveryAddress = address
                                )
                            )
                            Toast.makeText(context, "Created Order for $custName!", Toast.LENGTH_SHORT).show()
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany)
                ) {
                    Text("Create Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            },
            title = { Text("Create Enterprise Order") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = custName, onValueChange = { custName = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = custPhone, onValueChange = { custPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = prodName, onValueChange = { prodName = it }, label = { Text("Product Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = totalAmt, onValueChange = { totalAmt = it }, label = { Text("Total Amount (ETB)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = depAmt, onValueChange = { depAmt = it }, label = { Text("Deposit Paid (ETB)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Delivery Address") }, modifier = Modifier.fillMaxWidth())
                }
            }
        )
    }
}

@Composable
fun OrderCard(
    order: OrderRecord,
    pipelineStages: List<String>,
    onAdvanceStage: (String) -> Unit,
    onRecordPaymentClick: () -> Unit,
    onViewPdfClick: () -> Unit,
    onExportMessage: (String) -> Unit
) {
    val currentStageIdx = pipelineStages.indexOf(order.orderStage).coerceAtLeast(0)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order #${order.id} • ${order.customerName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(order.customerPhone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    color = when (order.paymentStatus) {
                        "Fully Paid" -> AccentSuccess.copy(alpha = 0.2f)
                        "Deposit Paid" -> GoldAccent.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        order.paymentStatus,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.paymentStatus) {
                            "Fully Paid" -> AccentSuccess
                            "Deposit Paid" -> WarmMahogany
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            Text("Product: ${order.productName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WarmMahogany)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: ETB %,.0f".format(order.totalAmount), fontSize = 12.sp)
                Text("Deposit: ETB %,.0f".format(order.depositPaid), fontSize = 12.sp, color = AccentSuccess)
                Text("Balance: ETB %,.0f".format(order.remainingBalance), fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }

            // Stage Progress Visualizer Bar
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Stage: ${order.orderStage}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    Text("${currentStageIdx + 1}/11", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (currentStageIdx + 1) / 11f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = GoldAccent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStageIdx < pipelineStages.size - 1) {
                    val nextStage = pipelineStages[currentStageIdx + 1]
                    Button(
                        onClick = { onAdvanceStage(nextStage) },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Move to $nextStage", fontSize = 10.sp)
                    }
                }

                OutlinedButton(
                    onClick = onRecordPaymentClick,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Pay Deposit", fontSize = 10.sp)
                }

                IconButton(onClick = onViewPdfClick) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "View Invoice PDF", tint = GoldAccent)
                }

                IconButton(onClick = { onExportMessage("WhatsApp") }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Notify Client", tint = AccentSuccess)
                }
            }
        }
    }
}
