package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryRecord
import com.example.data.model.WarehouseItem
import com.example.ui.theme.*

@Composable
fun LogisticsInventoryTab(viewModel: SalesViewModel) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableStateOf("Inventory") } // "Inventory", "Deliveries & GPS"

    val inventoryItems by viewModel.allWarehouseItems.collectAsState()
    val deliveries by viewModel.allDeliveries.collectAsState()

    var showAddStockDialog by remember { mutableStateOf(false) }
    var showScanSimDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
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
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logistics, Warehouse & Fleet Hub", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Multi-warehouse raw timber inventory control, barcode scanning, driver dispatching, and live GPS proof-of-delivery.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Sub-Tab Switcher Row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { selectedSubTab = "Inventory" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedSubTab == "Inventory") WarmMahogany else MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Warehouse Stock", fontSize = 12.sp)
                }

                Button(
                    onClick = { selectedSubTab = "Deliveries & GPS" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedSubTab == "Deliveries & GPS") WarmMahogany else MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fleet & Delivery GPS", fontSize = 12.sp)
                }
            }
        }

        if (selectedSubTab == "Inventory") {
            // Warehouse Controls Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Warehouse Materials & Timber", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showScanSimDialog = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Barcode Scanner Sim", tint = GoldAccent)
                        }

                        Button(
                            onClick = { showAddStockDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = DarkCocoaBg)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item", fontSize = 11.sp, color = DarkCocoaBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Inventory List
            items(inventoryItems, key = { it.id }) { item ->
                WarehouseItemCard(
                    item = item,
                    onUpdateStock = { newQty ->
                        viewModel.updateWarehouseStock(item.id, newQty)
                        Toast.makeText(context, "Updated ${item.productName} stock to $newQty", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = {
                        viewModel.deleteWarehouseItem(item.id)
                        Toast.makeText(context, "Deleted inventory item #${item.id}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            // Deliveries & Live GPS Tracker
            item {
                Text("Active Deliveries & Fleet Tracking", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            items(deliveries, key = { it.id }) { delivery ->
                DeliveryRecordCard(
                    delivery = delivery,
                    onUpdateStatus = { newStatus ->
                        val signed = newStatus == "Delivered & Signed"
                        viewModel.updateDeliveryStatus(delivery.id, newStatus, signed)
                        Toast.makeText(context, "Updated delivery status to $newStatus", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Add Stock Dialog
    if (showAddStockDialog) {
        var barcode by remember { mutableStateOf("BK-WNZ-00" + (10..99).random()) }
        var name by remember { mutableStateOf("") }
        var warehouse by remember { mutableStateOf("Addis Ababa Central") }
        var qty by remember { mutableStateOf("10") }
        var reorder by remember { mutableStateOf("3") }
        var cost by remember { mutableStateOf("15000") }

        AlertDialog(
            onDismissRequest = { showAddStockDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addWarehouseItem(
                                WarehouseItem(
                                    barcode = barcode,
                                    productName = name,
                                    warehouseName = warehouse,
                                    quantity = qty.toIntOrNull() ?: 1,
                                    reorderPoint = reorder.toIntOrNull() ?: 2,
                                    unitCost = cost.toDoubleOrNull() ?: 0.0
                                )
                            )
                            Toast.makeText(context, "Added $name to $warehouse!", Toast.LENGTH_SHORT).show()
                            showAddStockDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany)
                ) {
                    Text("Save Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStockDialog = false }) { Text("Cancel") }
            },
            title = { Text("Add Warehouse Stock Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode / SKU") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product / Material Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = warehouse, onValueChange = { warehouse = it }, label = { Text("Warehouse Location") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Initial Quantity") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = reorder, onValueChange = { reorder = it }, label = { Text("Low Stock Threshold Alert") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Unit Cost (ETB)") }, modifier = Modifier.fillMaxWidth())
                }
            }
        )
    }

    // Barcode Scanner Simulator Modal
    if (showScanSimDialog) {
        AlertDialog(
            onDismissRequest = { showScanSimDialog = false },
            confirmButton = {
                Button(
                    onClick = { showScanSimDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Simulate Scanned Code", color = DarkCocoaBg, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Camera Barcode / QR Scanner") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.Black, shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scanning Optical Code...", fontSize = 12.sp, color = TextLight)
                    Text("Target: BK-WNZ-001 (Wanza Timber Log)", fontSize = 11.sp, color = AccentSuccess, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun WarehouseItemCard(item: WarehouseItem, onUpdateStock: (Int) -> Unit, onDelete: () -> Unit) {
    val isLowStock = item.quantity <= item.reorderPoint

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("SKU: ${item.barcode} • Warehouse: ${item.warehouseName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    color = if (isLowStock) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else AccentSuccess.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (isLowStock) "LOW STOCK REORDER!" else "In Stock",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) MaterialTheme.colorScheme.error else AccentSuccess
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Unit Cost: ETB %,.0f".format(item.unitCost), fontSize = 12.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (item.quantity > 0) onUpdateStock(item.quantity - 1) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.error)
                    }
                    Text("Qty: ${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    IconButton(onClick = { onUpdateStock(item.quantity + 1) }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = AccentSuccess)
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryRecordCard(delivery: DeliveryRecord, onUpdateStatus: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Delivery #${delivery.id} (Order #${delivery.orderId})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Driver: ${delivery.driverName} • Plate: ${delivery.vehiclePlate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(color = GoldAccent.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        delivery.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmMahogany
                    )
                }
            }

            // GPS Simulator Overlay representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFF2C3E50), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AccentWarning)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Live GPS Coordinates: Lat ${delivery.currentGpsLat}, Lng ${delivery.currentGpsLng}\n[Addis Ababa Route active - ETA: 25 mins]",
                        fontSize = 10.sp,
                        color = TextLight
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = { onUpdateStatus("Delivered & Signed") },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Complete Proof-of-Delivery Sign-Off", fontSize = 10.sp)
                }
            }
        }
    }
}
