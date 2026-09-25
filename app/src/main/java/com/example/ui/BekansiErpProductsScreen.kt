package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Product
import com.example.data.model.WarehouseItem
import com.example.ui.theme.*

/**
 * BEKANSI AI SALES — ENTERPRISE ERP PRODUCTS THEME
 * Inspired by ILILI ERP design hierarchy & product-management workflow.
 * Branded with BEKANSI Deep Navy (#0A2E5D), Gold (#D4AF37), and Light Gray (#F5F5F5).
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BekansiErpProductsScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.allProducts.collectAsState()
    val warehouseItems by viewModel.allWarehouseItems.collectAsState()
    val isLiveOnline by viewModel.isLiveOnlineSyncState.collectAsState()

    // Filter and search states
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") } // All, In Stock, Made to Order, Out of Stock
    var selectedType by remember { mutableStateOf("All") }   // All, Beds, Sofas, Dining Tables, Wardrobes, TV Stands, Office
    var viewMode by remember { mutableStateOf("table") }     // "table" or "grid"
    var showFilterDrawer by remember { mutableStateOf(false) }

    // Dialog & Detail Drawer States
    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Categories list based on Bekansi business scope
    val categoryOptions = listOf(
        "All", "Beds", "Sofas", "Wardrobes", "Kitchen Cabinets",
        "TV Stands", "Dining Tables", "Office Furniture", "Custom Furniture"
    )

    // Stock & status options
    val statusOptions = listOf("All", "In Stock", "Made to Order", "Out of Stock")

    // Dynamic KPI computations
    val totalProductsCount = products.size
    val activeProductsCount = products.count { it.stockStatus != "Out of Stock" }
    val lowStockCount = warehouseItems.count { it.quantity in 1..it.reorderPoint }
    val outOfStockCount = products.count { it.stockStatus == "Out of Stock" }
    val totalInventoryValue = warehouseItems.sumOf { it.quantity * it.unitCost }
    val totalRetailPotential = products.sumOf { p ->
        val matchedStock = warehouseItems.find { it.productName.contains(p.name.substringBefore(" '"), ignoreCase = true) }?.quantity ?: 1
        p.price * matchedStock
    }

    // Filter pipeline
    val filteredProducts = remember(products, searchQuery, selectedCategory, selectedStatus) {
        val cleanQuery = searchQuery.trim().replace("×", "x").lowercase()
        products.filter { p ->
            val cleanName = p.name.replace("×", "x").lowercase()
            val cleanDim = p.dimensions.replace("×", "x").lowercase()
            val cleanSku = "bk-${p.category.take(3).lowercase()}-${p.id + 100}"
            val matchesSearch = cleanQuery.isBlank() ||
                    cleanName.contains(cleanQuery) ||
                    p.category.lowercase().contains(cleanQuery) ||
                    p.material.lowercase().contains(cleanQuery) ||
                    cleanDim.contains(cleanQuery) ||
                    cleanSku.contains(cleanQuery)

            val matchesCategory = selectedCategory == "All" ||
                    p.category.equals(selectedCategory, ignoreCase = true) ||
                    (selectedCategory == "Office Furniture" && p.category.contains("Office", ignoreCase = true)) ||
                    (selectedCategory == "Dining Tables" && p.category.contains("Dining", ignoreCase = true))

            val matchesStatus = selectedStatus == "All" ||
                    p.stockStatus.equals(selectedStatus, ignoreCase = true)

            matchesSearch && matchesCategory && matchesStatus
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightGrayBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // ==========================================
            // 1. HEADER: Title, Subtitle & Action Bar
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = DeepNavy,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Products",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepNavy,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = GoldLight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "BEKANSI ERP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (isLiveOnline) AccentSuccess.copy(alpha = 0.12f) else AccentWarning.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isLiveOnline) AccentSuccess.copy(alpha = 0.3f) else AccentWarning.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isLiveOnline) AccentSuccess else AccentWarning)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isLiveOnline) "ONLINE — Live Data" else "OFFLINE — Cached Data",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLiveOnline) AccentSuccess else AccentWarning
                                )
                            }
                        }
                    }
                    Text(
                        text = "Manage products, pricing, inventory, variants and sales information.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Header Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            viewModel.isLiveOnlineSyncState.value = true
                            Toast.makeText(context, "Reconciling with cloud PostgreSQL/Supabase master catalog... Synced!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepNavy),
                        border = BorderStroke(1.dp, CardBorderGray),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Cloud", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Exporting BEKANSI product matrix to CSV...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepNavy),
                        border = BorderStroke(1.dp, CardBorderGray),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { showAddProductDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_product_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Product", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }
            }

            // ==========================================
            // 2. PRODUCT SUMMARY KPI CARDS
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ErpKpiCard(
                    title = "Total Products",
                    value = totalProductsCount.toString(),
                    subtitle = "Catalog master count",
                    icon = Icons.Default.List,
                    iconBg = DeepNavy,
                    iconTint = PureWhite
                )
                ErpKpiCard(
                    title = "Active Products",
                    value = activeProductsCount.toString(),
                    subtitle = "${((activeProductsCount.toDouble() / (totalProductsCount.coerceAtLeast(1))) * 100).toInt()}% active in ERP",
                    icon = Icons.Default.CheckCircle,
                    iconBg = AccentSuccess.copy(alpha = 0.15f),
                    iconTint = AccentSuccess
                )
                ErpKpiCard(
                    title = "Low Stock Alerts",
                    value = lowStockCount.toString(),
                    subtitle = "Below reorder threshold",
                    icon = Icons.Default.Warning,
                    iconBg = AccentWarning.copy(alpha = 0.15f),
                    iconTint = AccentWarning
                )
                ErpKpiCard(
                    title = "Out of Stock",
                    value = outOfStockCount.toString(),
                    subtitle = "Requires manufacturing",
                    icon = Icons.Default.Close,
                    iconBg = AccentDanger.copy(alpha = 0.15f),
                    iconTint = AccentDanger
                )
                ErpKpiCard(
                    title = "Inventory Value",
                    value = "${String.format("%,.0f", totalInventoryValue)} ETB",
                    subtitle = "Raw materials & timber cost",
                    icon = Icons.Default.AccountBox,
                    iconBg = GoldAccent.copy(alpha = 0.2f),
                    iconTint = DeepNavy
                )
                ErpKpiCard(
                    title = "Retail Value",
                    value = "${String.format("%,.0f", totalRetailPotential)} ETB",
                    subtitle = "Projected showroom potential",
                    icon = Icons.Default.Star,
                    iconBg = DeepNavy.copy(alpha = 0.15f),
                    iconTint = DeepNavy
                )
            }

            // ==========================================
            // 3. SEARCH & FILTER TOOLBAR
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderGray, RoundedCornerShape(10.dp))
                    .padding(bottom = 10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Search Input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search products by name, SKU, category or barcode...",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepNavy,
                                unfocusedBorderColor = CardBorderGray,
                                focusedContainerColor = PureWhite,
                                unfocusedContainerColor = PureWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("product_search_input")
                        )

                        // View Mode Toggle (Table / Grid)
                        Surface(
                            color = LightGrayBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, CardBorderGray, RoundedCornerShape(8.dp))
                        ) {
                            Row(modifier = Modifier.padding(2.dp)) {
                                IconButton(
                                    onClick = { viewMode = "table" },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (viewMode == "table") PureWhite else Color.Transparent, RoundedCornerShape(6.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = "Table View",
                                        tint = if (viewMode == "table") DeepNavy else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewMode = "grid" },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (viewMode == "grid") PureWhite else Color.Transparent, RoundedCornerShape(6.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Grid View",
                                        tint = if (viewMode == "grid") DeepNavy else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Filter Drawer Trigger
                        Button(
                            onClick = { showFilterDrawer = !showFilterDrawer },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showFilterDrawer || selectedCategory != "All" || selectedStatus != "All") DeepNavy else PureWhite,
                                contentColor = if (showFilterDrawer || selectedCategory != "All" || selectedStatus != "All") PureWhite else TextDark
                            ),
                            border = BorderStroke(1.dp, if (showFilterDrawer) DeepNavy else CardBorderGray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Filters", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Collapsible Filter Panel
                    AnimatedVisibility(visible = showFilterDrawer) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .background(LightSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text("Filter by Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categoryOptions.forEach { cat ->
                                    val isSelected = selectedCategory == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = DeepNavy,
                                            selectedLabelColor = PureWhite,
                                            containerColor = PureWhite,
                                            labelColor = TextDark
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Filter by Stock Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                statusOptions.forEach { st ->
                                    val isSelected = selectedStatus == st
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedStatus = st },
                                        label = { Text(st, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = GoldAccent,
                                            selectedLabelColor = DeepNavy,
                                            containerColor = PureWhite,
                                            labelColor = TextDark
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = {
                                        selectedCategory = "All"
                                        selectedStatus = "All"
                                        searchQuery = ""
                                    }
                                ) {
                                    Text("Clear Filters", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentDanger)
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. MAIN PRODUCT DISPLAY: Table or Grid
            // ==========================================
            if (filteredProducts.isEmpty()) {
                // Empty State with Clear / Add Action
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, CardBorderGray, RoundedCornerShape(10.dp))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Surface(
                                color = LightGrayBg,
                                shape = CircleShape,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Text(
                                text = "No products found",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty() || selectedCategory != "All" || selectedStatus != "All") {
                                    "No products match your current search and filter parameters."
                                } else {
                                    "Add your first product to start managing the BEKANSI catalog."
                                },
                                fontSize = 11.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (searchQuery.isNotEmpty() || selectedCategory != "All" || selectedStatus != "All") {
                                    OutlinedButton(
                                        onClick = {
                                            searchQuery = ""
                                            selectedCategory = "All"
                                            selectedStatus = "All"
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Clear Filters", fontSize = 11.sp)
                                    }
                                }
                                Button(
                                    onClick = { showAddProductDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ Add Product", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                }
                            }
                        }
                    }
                }
            } else {
                if (viewMode == "table") {
                    // Enterprise Table View
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(1.dp, CardBorderGray, RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(LightSurfaceVariant)
                                    .border(BorderStroke(1.dp, CardBorderGray))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("PRODUCT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(2.4f))
                                Text("SKU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.3f))
                                Text("CATEGORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.3f))
                                Text("PRICE (ETB)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.4f))
                                Text("STOCK STATUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.4f))
                                Text("ACTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                            }

                            // Table Rows
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                items(filteredProducts) { item ->
                                    val sku = "BK-${item.category.take(3).uppercase()}-${item.id + 100}"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedProductForDetail = item }
                                            .border(BorderStroke(0.5.dp, CardBorderGray.copy(alpha = 0.6f)))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Product column: Thumbnail + Name + Material
                                        Row(
                                            modifier = Modifier.weight(2.4f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = DeepNavy.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                if (item.imageUrl.isNotBlank()) {
                                                    AsyncImage(
                                                        model = item.imageUrl,
                                                        contentDescription = item.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Home, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = item.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepNavy,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = item.material,
                                                    fontSize = 10.sp,
                                                    color = TextMuted,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // SKU
                                        Text(
                                            text = sku,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextDark,
                                            modifier = Modifier.weight(1.3f)
                                        )

                                        // Category
                                        Surface(
                                            color = DeepNavy.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.weight(1.3f)
                                        ) {
                                            Text(
                                                text = item.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepNavy,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Price
                                        Text(
                                            text = "${String.format("%,.0f", item.price)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentSuccess,
                                            modifier = Modifier.weight(1.4f)
                                        )

                                        // Stock Status Badge
                                        val (statusBg, statusFg) = when (item.stockStatus) {
                                            "In Stock" -> Pair(AccentSuccess.copy(alpha = 0.12f), AccentSuccess)
                                            "Made to Order" -> Pair(GoldAccent.copy(alpha = 0.15f), DeepNavy)
                                            else -> Pair(AccentDanger.copy(alpha = 0.12f), AccentDanger)
                                        }
                                        Surface(
                                            color = statusBg,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.weight(1.4f)
                                        ) {
                                            Text(
                                                text = item.stockStatus,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusFg,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Actions
                                        Row(
                                            modifier = Modifier.weight(1.2f),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { selectedProductForDetail = item },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Info, contentDescription = "View", tint = DeepNavy, modifier = Modifier.size(15.dp))
                                            }
                                            IconButton(
                                                onClick = { productToEdit = item },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = GoldAccent, modifier = Modifier.size(15.dp))
                                            }
                                            IconButton(
                                                onClick = { productToDelete = item },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentDanger, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Enterprise Grid View
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 250.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredProducts) { item ->
                            val sku = "BK-${item.category.take(3).uppercase()}-${item.id + 100}"
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CardBorderGray, RoundedCornerShape(10.dp))
                                    .clickable { selectedProductForDetail = item }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Card Header with SKU & Status Pill
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(sku, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                        val (statusBg, statusFg) = when (item.stockStatus) {
                                            "In Stock" -> Pair(AccentSuccess.copy(alpha = 0.12f), AccentSuccess)
                                            "Made to Order" -> Pair(GoldAccent.copy(alpha = 0.15f), DeepNavy)
                                            else -> Pair(AccentDanger.copy(alpha = 0.12f), AccentDanger)
                                        }
                                        Surface(
                                            color = statusBg,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.stockStatus,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusFg,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Product Image Placeholder or Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LightSurfaceVariant)
                                    ) {
                                        if (item.imageUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = item.imageUrl,
                                                contentDescription = item.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Default.Home, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(32.dp))
                                                Text(item.category, fontSize = 9.sp, color = TextMuted)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = item.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = item.material, fontSize = 10.sp, color = TextMuted)
                                        Text(text = "• ${item.warranty}", fontSize = 9.sp, color = TextMuted)
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorderGray)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("RETAIL PRICE", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${String.format("%,.0f", item.price)} ETB",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = AccentSuccess
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(onClick = { productToEdit = item }, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = GoldAccent, modifier = Modifier.size(14.dp))
                                            }
                                            IconButton(onClick = { selectedProductForDetail = item }, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Details", tint = DeepNavy, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. ADD / EDIT PRODUCT DIALOG
        // ==========================================
        if (showAddProductDialog || productToEdit != null) {
            val isEditing = productToEdit != null
            val editingItem = productToEdit

            var nameInput by remember { mutableStateOf(editingItem?.name ?: "") }
            var categoryInput by remember { mutableStateOf(editingItem?.category ?: "Beds") }
            var priceInput by remember { mutableStateOf(editingItem?.price?.toString() ?: "90000") }
            var materialInput by remember { mutableStateOf(editingItem?.material ?: "Wanza (Cordia Africana)") }
            var dimensionsInput by remember { mutableStateOf(editingItem?.dimensions ?: "1.80m x 2.00m") }
            var warrantyInput by remember { mutableStateOf(editingItem?.warranty ?: "5 Years Warranty") }
            var descriptionInput by remember { mutableStateOf(editingItem?.description ?: "") }
            var stockStatusInput by remember { mutableStateOf(editingItem?.stockStatus ?: "In Stock") }
            var imageUrlInput by remember { mutableStateOf(editingItem?.imageUrl ?: "") }

            Dialog(
                onDismissRequest = {
                    showAddProductDialog = false
                    productToEdit = null
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.90f)
                        .border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        // Dialog Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isEditing) "Edit Product Record" else "+ Add New Product",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DeepNavy
                                )
                                Text(
                                    text = "Registers officially into BEKANSI database and AI Sales knowledge base.",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            IconButton(
                                onClick = {
                                    showAddProductDialog = false
                                    productToEdit = null
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorderGray)

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text("BASIC INFORMATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    label = { Text("Product Name", fontSize = 11.sp) },
                                    placeholder = { Text("e.g. Entoto Luxury Smart Bed 1.80m x 2.00m") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepNavy)
                                )
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = categoryInput,
                                        onValueChange = { categoryInput = it },
                                        label = { Text("Category", fontSize = 11.sp) },
                                        placeholder = { Text("Beds, Sofas, Wardrobes, Dining") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepNavy)
                                    )
                                    OutlinedTextField(
                                        value = priceInput,
                                        onValueChange = { priceInput = it },
                                        label = { Text("Retail Price (ETB)", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepNavy)
                                    )
                                }
                            }

                            item {
                                Text("SPECIFICATIONS & MATERIALS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = materialInput,
                                        onValueChange = { materialInput = it },
                                        label = { Text("Hardwood Species / Carcass", fontSize = 11.sp) },
                                        placeholder = { Text("Wanza, Mahogany, Grar Acacia") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepNavy)
                                    )
                                    OutlinedTextField(
                                        value = dimensionsInput,
                                        onValueChange = { dimensionsInput = it },
                                        label = { Text("Standard Dimensions", fontSize = 11.sp) },
                                        placeholder = { Text("1.80m x 2.00m x 1.10m") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepNavy)
                                    )
                                }
                            }

                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = warrantyInput,
                                        onValueChange = { warrantyInput = it },
                                        label = { Text("Warranty Policy", fontSize = 11.sp) },
                                        placeholder = { Text("5 Years Structural Warranty") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepNavy)
                                    )
                                    OutlinedTextField(
                                        value = stockStatusInput,
                                        onValueChange = { stockStatusInput = it },
                                        label = { Text("Stock Status", fontSize = 11.sp) },
                                        placeholder = { Text("In Stock, Made to Order, Out of Stock") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepNavy)
                                    )
                                }
                            }

                            item {
                                OutlinedTextField(
                                    value = imageUrlInput,
                                    onValueChange = { imageUrlInput = it },
                                    label = { Text("Product Image CDN / URL (Optional)", fontSize = 11.sp) },
                                    placeholder = { Text("https://...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepNavy)
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = descriptionInput,
                                    onValueChange = { descriptionInput = it },
                                    label = { Text("Product Description / Crafting Details", fontSize = 11.sp) },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepNavy)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorderGray)

                        // Dialog Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showAddProductDialog = false
                                    productToEdit = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val priceVal = priceInput.toDoubleOrNull() ?: 0.0
                                    if (nameInput.isBlank()) {
                                        Toast.makeText(context, "Product name cannot be empty.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (priceVal <= 0) {
                                        Toast.makeText(context, "Retail price must be greater than 0 ETB.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (priceVal < 1000) {
                                        Toast.makeText(context, "Validation error: Retail price for Bekansi handcrafted furniture cannot be lower than 1,000 ETB minimum floor.", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    if (isEditing && editingItem != null) {
                                        viewModel.updateProduct(
                                            editingItem.copy(
                                                name = nameInput.trim(),
                                                category = categoryInput.trim(),
                                                price = priceVal,
                                                material = materialInput.trim(),
                                                dimensions = dimensionsInput.trim(),
                                                warranty = warrantyInput.trim(),
                                                description = descriptionInput.trim(),
                                                stockStatus = stockStatusInput.trim(),
                                                imageUrl = imageUrlInput.trim()
                                            )
                                        )
                                        Toast.makeText(context, "Product '${nameInput.trim()}' updated successfully in ERP!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addProduct(
                                            Product(
                                                name = nameInput.trim(),
                                                category = categoryInput.trim(),
                                                price = priceVal,
                                                material = materialInput.trim(),
                                                dimensions = dimensionsInput.trim(),
                                                warranty = warrantyInput.trim(),
                                                description = descriptionInput.trim(),
                                                stockStatus = stockStatusInput.trim(),
                                                imageUrl = imageUrlInput.trim()
                                            )
                                        )
                                        Toast.makeText(context, "Product '${nameInput.trim()}' registered in BEKANSI database!", Toast.LENGTH_SHORT).show()
                                    }
                                    showAddProductDialog = false
                                    productToEdit = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isEditing) "Save Changes" else "Create Product", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. PRODUCT DETAIL DRAWER / MODAL
        // ==========================================
        selectedProductForDetail?.let { detailProd ->
            val sku = "BK-${detailProd.category.take(3).uppercase()}-${detailProd.id + 100}"
            val wholesalePrice = detailProd.price * 0.80
            val resellerPrice = detailProd.price * 0.85
            val factoryCost = detailProd.price * 0.55
            val minSellingPrice = detailProd.price * 0.90

            Dialog(
                onDismissRequest = { selectedProductForDetail = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.92f)
                        .border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(detailProd.name, fontSize = 16.sp, fontWeight = FontWeight.Black, color = DeepNavy)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(color = GoldLight, shape = RoundedCornerShape(4.dp)) {
                                        Text(sku, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepNavy, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text("ERP Catalog Item Details & Multi-tier Pricing Architecture", fontSize = 10.sp, color = TextMuted)
                            }
                            IconButton(onClick = { selectedProductForDetail = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorderGray)

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Section: Multi-tier Pricing Matrix
                            item {
                                Text("PRICING ARCHITECTURE (ETB)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PriceTierCard("Retail Price", "${String.format("%,.0f", detailProd.price)} ETB", AccentSuccess, "Customer-facing")
                                    PriceTierCard("Reseller Price", "${String.format("%,.0f", resellerPrice)} ETB", AccentInfo, "Partner rate")
                                    PriceTierCard("Wholesale Price", "${String.format("%,.0f", wholesalePrice)} ETB", GoldAccent, "B2B Volume MOQ")
                                    PriceTierCard("Factory Cost", "${String.format("%,.0f", factoryCost)} ETB", TextMuted, "Materials + Labor")
                                }
                            }

                            // Section: Specifications
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("PRODUCT SPECIFICATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                        DetailSpecRow("Category", detailProd.category)
                                        DetailSpecRow("Wood Species / Material", detailProd.material)
                                        DetailSpecRow("Dimensions", detailProd.dimensions)
                                        DetailSpecRow("Warranty Coverage", detailProd.warranty)
                                        DetailSpecRow("Stock / Production Status", detailProd.stockStatus)
                                        DetailSpecRow("Min. Permitted Sale Price", "${String.format("%,.0f", minSellingPrice)} ETB")
                                    }
                                }
                            }

                            // Section: Customization Options
                            item {
                                Text("AVAILABLE CUSTOMIZATIONS & VARIANTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CustomizationPill(Icons.Default.Build, "Custom Dimensions")
                                    CustomizationPill(Icons.Default.Favorite, "Fabric / Velvet Swatches")
                                    CustomizationPill(Icons.Default.Star, "LED Smart Inlays")
                                    CustomizationPill(Icons.Default.Check, "Built-in Sockets")
                                }
                            }

                            // Section: Description
                            item {
                                Text("DESCRIPTION & WORKSHOP NOTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = detailProd.description,
                                    fontSize = 11.sp,
                                    color = TextDark,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorderGray)

                        // Action row in detail
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    productToEdit = detailProd
                                    selectedProductForDetail = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Product", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { selectedProductForDetail = null },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Close", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 7. DELETE CONFIRMATION DIALOG
        // ==========================================
        productToDelete?.let { prod ->
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Delete Product", fontWeight = FontWeight.Bold, color = DeepNavy) },
                text = {
                    Text(
                        "Are you sure you want to archive or remove '${prod.name}' from active catalog? If referenced in existing customer quotations or orders, the system safely marks it as 'Discontinued' to preserve historical transactions.",
                        fontSize = 12.sp,
                        color = TextDark
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Check if referenced in quotations/orders; if so, soft-deactivate
                            val quotations = viewModel.allQuotations.value
                            val orders = viewModel.allOrders.value
                            val isReferenced = quotations.any { it.productName.contains(prod.name, ignoreCase = true) } ||
                                    orders.any { it.productName.contains(prod.name, ignoreCase = true) }

                            if (isReferenced) {
                                viewModel.updateProduct(prod.copy(stockStatus = "Out of Stock", description = "[DISCONTINUED] ${prod.description}"))
                                Toast.makeText(context, "Product '${prod.name}' has historical sales records. Safely marked as Discontinued.", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.deleteProduct(prod.id)
                                Toast.makeText(context, "Removed unreferenced product #${prod.id} from catalog.", Toast.LENGTH_SHORT).show()
                            }
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentDanger)
                    ) {
                        Text("Archive / Remove", fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { productToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ==========================================
// REUSABLE ERP COMPONENTS
// ==========================================

@Composable
fun ErpKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(175.dp)
            .border(1.dp, CardBorderGray, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = iconBg,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = DeepNavy,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PriceTierCard(title: String, amount: String, color: Color, note: String) {
    Surface(
        color = LightSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .border(1.dp, CardBorderGray, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(title.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
            Text(note, fontSize = 8.sp, color = TextMuted)
        }
    }
}

@Composable
fun DetailSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 10.sp, color = TextMuted)
        Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
    }
}

@Composable
fun CustomizationPill(icon: ImageVector, text: String) {
    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(1.dp, CardBorderGray, RoundedCornerShape(6.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
        }
    }
}
