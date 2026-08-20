package com.example.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalUriHandler
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
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.Lead
import com.example.data.model.Product
import com.example.data.model.ProductDesignImage
import com.example.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun DesignGalleryTab(
    viewModel: SalesViewModel,
    galleryViewModel: DesignGalleryViewModel = viewModel(
        factory = DesignGalleryViewModelFactory(viewModel.repository)
    )
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    // State from ViewModel
    val designImages by viewModel.allDesignImages.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val leads by viewModel.allLeads.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()

    // UI View Mode (Grid vs List)
    var isGridView by remember { mutableStateOf(true) }

    // Search and Filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var filterOnlyFavorites by remember { mutableStateOf(false) }
    var filterOnlyPrimary by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("Newest") } // Newest, Price Low-High, Price High-Low, Category

    // Dialog States
    var showAddDesignDialog by remember { mutableStateOf(false) }
    var showUploadModal by remember { mutableStateOf(false) }
    var selectedImageForDetail by remember { mutableStateOf<ProductDesignImage?>(null) }
    var imageToDelete by remember { mutableStateOf<ProductDesignImage?>(null) }
    var imageToShareInConversation by remember { mutableStateOf<ProductDesignImage?>(null) }

    // Temporary upload queue state
    val selectedUris = remember { mutableStateListOf<Uri>() }

    // Android Pickers
    // 1. Modern Android PhotoPicker (Multiple images via PickMultipleVisualMedia)
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedUris.clear()
            selectedUris.addAll(uris)
            galleryViewModel.onImagesSelected(uris, context)
            showUploadModal = true
        }
    }

    // 2. Modern Android PhotoPicker (Single image via PickVisualMedia)
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUris.clear()
            selectedUris.add(uri)
            galleryViewModel.onSingleImageSelected(uri, context)
            showUploadModal = true
        }
    }

    // 3. Open Document / Storage Access Framework (Internal Storage / Local Files)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedUris.clear()
            selectedUris.addAll(uris)
            galleryViewModel.onImagesSelected(uris, context)
            showUploadModal = true
        }
    }

    // Suggested Categories
    val availableCategories = listOf(
        "All", "Sofa", "Bed", "Wardrobe", "Kitchen Cabinet", 
        "TV Stand", "Dining Table", "Office Furniture", "Custom Furniture", "Interior Design"
    )

    // Filter and Sort Logic
    val filteredDesigns = remember(designImages, searchQuery, selectedCategoryFilter, filterOnlyFavorites, filterOnlyPrimary, sortBy) {
        designImages.filter { item ->
            val matchesCategory = (selectedCategoryFilter == "All") || item.category.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || 
                item.productName.contains(searchQuery, ignoreCase = true) ||
                item.sku.contains(searchQuery, ignoreCase = true) ||
                item.tags.contains(searchQuery, ignoreCase = true) ||
                item.material.contains(searchQuery, ignoreCase = true) ||
                item.color.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
            val matchesFav = !filterOnlyFavorites || item.isFavorite
            val matchesPrimary = !filterOnlyPrimary || item.isPrimary

            matchesCategory && matchesSearch && matchesFav && matchesPrimary
        }.let { list ->
            when (sortBy) {
                "Price Low-High" -> list.sortedBy { it.price }
                "Price High-Low" -> list.sortedByDescending { it.price }
                "Category" -> list.sortedBy { it.category }
                else -> list.sortedByDescending { it.createdAt }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
        // Top Action Bar & Stats
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = WarmMahogany, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "BEKANSI DESIGN GALLERY",
                                color = WarmMahogany,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            "Official Multi-Tenancy DAM: ${designImages.size} High-Res Assets | ${products.size} Products Assigned",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    // + Add Design Action Button
                    Button(
                        onClick = { showAddDesignDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("add_design_button"),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Design", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar with Grid/List Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name, SKU, tag, material, color...", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("gallery_search_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarmMahogany,
                            unfocusedBorderColor = CardBorderGray
                        )
                    )

                    // View Mode Toggle (Grid vs List)
                    Surface(
                        color = LightSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, CardBorderGray, RoundedCornerShape(8.dp))
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            IconButton(
                                onClick = { isGridView = true },
                                modifier = Modifier.size(34.dp).background(if (isGridView) WarmMahogany else Color.Transparent, RoundedCornerShape(6.dp))
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Grid", tint = if (isGridView) Color.White else TextMuted, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { isGridView = false },
                                modifier = Modifier.size(34.dp).background(if (!isGridView) WarmMahogany else Color.Transparent, RoundedCornerShape(6.dp))
                            ) {
                                Icon(Icons.Default.List, contentDescription = "List", tint = if (!isGridView) Color.White else TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCategories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarmMahogany,
                                selectedLabelColor = Color.White,
                                containerColor = LightSurfaceVariant,
                                labelColor = TextDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Fast toggles: Favorites, Primary Only, Sort
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filterOnlyFavorites,
                            onClick = { filterOnlyFavorites = !filterOnlyFavorites },
                            leadingIcon = {
                                Icon(
                                    if (filterOnlyFavorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (filterOnlyFavorites) Color.Red else TextMuted
                                )
                            },
                            label = { Text("Favorites", fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(6.dp)
                        )

                        FilterChip(
                            selected = filterOnlyPrimary,
                            onClick = { filterOnlyPrimary = !filterOnlyPrimary },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (filterOnlyPrimary) GoldAccent else TextMuted
                                )
                            },
                            label = { Text("Primary Only", fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldAccent.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }

                    // Sort menu button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            sortBy = when (sortBy) {
                                "Newest" -> "Price Low-High"
                                "Price Low-High" -> "Price High-Low"
                                "Price High-Low" -> "Category"
                                else -> "Newest"
                            }
                            Toast.makeText(context, "Sorting by: $sortBy", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort", tint = WarmMahogany, modifier = Modifier.size(14.dp))
                        Text("Sort: $sortBy", fontSize = 9.sp, color = WarmMahogany, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Gallery Content Area
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (filteredDesigns.isEmpty()) {
                // Section 25: Empty State
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxSize().border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = LightSurfaceVariant,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = WarmMahogany, modifier = Modifier.size(36.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Design Gallery is Empty",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmMahogany
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Upload your first furniture design from your phone gallery or internal storage.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showAddDesignDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("empty_add_design_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Add Design", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredDesigns, key = { it.id }) { design ->
                            GalleryCardGridItem(
                                design = design,
                                onClick = { selectedImageForDetail = design },
                                onToggleFav = { viewModel.toggleDesignFavorite(design) },
                                onSetPrimary = { viewModel.setDesignAsPrimary(design) },
                                onDelete = { imageToDelete = design },
                                onShare = { imageToShareInConversation = design }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredDesigns, key = { it.id }) { design ->
                            GalleryListItem(
                                design = design,
                                onClick = { selectedImageForDetail = design },
                                onToggleFav = { viewModel.toggleDesignFavorite(design) },
                                onSetPrimary = { viewModel.setDesignAsPrimary(design) },
                                onDelete = { imageToDelete = design },
                                onShare = { imageToShareInConversation = design }
                            )
                        }
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // Section 2: + Add Design Source Selector Dialog
    // ----------------------------------------------------
    if (showAddDesignDialog) {
        Dialog(onDismissRequest = { showAddDesignDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorderGray, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Add Design",
                        color = WarmMahogany,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Upload high-resolution furniture photography or bespoke CAD renders directly into Bekansi DAM.",
                        color = TextMuted,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Option 1: Phone Gallery (PhotoPicker)
                    Surface(
                        color = LightSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            showAddDesignDialog = false
                            multiplePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }.border(1.dp, CardBorderGray, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = WarmMahogany.copy(alpha = 0.1f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Face, contentDescription = "Gallery", tint = WarmMahogany, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("🖼️ Choose from Gallery", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Select multiple photos from device albums & recent shots", color = TextMuted, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2: Internal Storage (SAF)
                    Surface(
                        color = LightSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            showAddDesignDialog = false
                            documentPickerLauncher.launch(
                                arrayOf("image/jpeg", "image/png", "image/webp", "image/heic", "image/heif")
                            )
                        }.border(1.dp, CardBorderGray, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AccentWarning.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Place, contentDescription = "Storage", tint = AccentWarning, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("📁 Choose from Internal Storage", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Browse system folders, SD Card, Downloads & Drive", color = TextMuted, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 3: Single Photo / Camera Quick Select
                    Surface(
                        color = LightSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            showAddDesignDialog = false
                            singlePhotoPickerLauncher.launch(
                                galleryViewModel.createVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }.border(1.dp, CardBorderGray, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AccentSuccess.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Camera", tint = AccentSuccess, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("📷 Take Photo / Quick Snap", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Capture showroom masterpiece directly with camera", color = TextMuted, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddDesignDialog = false }) {
                            Text("Cancel", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // Sections 4, 5, 6, 7: Multiple Image Upload & Preview Modal
    // ----------------------------------------------------
    if (showUploadModal && selectedUris.isNotEmpty()) {
        MultipleImageUploadModal(
            uris = selectedUris,
            products = products,
            galleryViewModel = galleryViewModel,
            onDismiss = {
                showUploadModal = false
                selectedUris.clear()
                galleryViewModel.clearSelection()
            },
            onUploadComplete = { uploadedDesigns ->
                viewModel.addDesignImages(uploadedDesigns)
                showUploadModal = false
                selectedUris.clear()
                galleryViewModel.clearSelection()
                Toast.makeText(context, "Successfully uploaded ${uploadedDesigns.size} design(s) via Retrofit DAM service!", Toast.LENGTH_LONG).show()
            }
        )
    }

    // ----------------------------------------------------
    // Section 18: Design Detail Page / Modal Dialog
    // ----------------------------------------------------
    selectedImageForDetail?.let { design ->
        DesignDetailModal(
            design = design,
            products = products,
            onDismiss = { selectedImageForDetail = null },
            onToggleFavorite = { viewModel.toggleDesignFavorite(design) },
            onSetPrimary = {
                viewModel.setDesignAsPrimary(design)
                selectedImageForDetail = design.copy(isPrimary = true)
                Toast.makeText(context, "Set as primary design for ${design.category}", Toast.LENGTH_SHORT).show()
            },
            onShareWhatsApp = {
                imageToShareInConversation = design
            },
            onUseInQuotation = {
                Toast.makeText(context, "Attached design to Quotation Engine!", Toast.LENGTH_SHORT).show()
            },
            onDelete = {
                selectedImageForDetail = null
                imageToDelete = design
            }
        )
    }

    // ----------------------------------------------------
    // Section 19: Delete Confirmation Modal (Soft Deletion Safety)
    // ----------------------------------------------------
    imageToDelete?.let { design ->
        AlertDialog(
            onDismissRequest = { imageToDelete = null },
            title = { Text("Delete Design?", color = WarmMahogany, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Text(
                    "This image may be used in products, quotations, and customer conversations.\n\n" +
                    "Soft deletion will safely archive the design while preserving historical quotation references.",
                    fontSize = 11.sp,
                    color = TextDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDesignImage(design, hardDelete = false)
                        imageToDelete = null
                        Toast.makeText(context, "Design safely archived in DAM", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { imageToDelete = null }) {
                    Text("Cancel", color = TextMuted, fontSize = 11.sp)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ----------------------------------------------------
    // Section 16: WhatsApp / Customer Sharing Flow
    // ----------------------------------------------------
    imageToShareInConversation?.let { design ->
        ShareToConversationDialog(
            design = design,
            leads = leads,
            onDismiss = { imageToShareInConversation = null },
            onSendWhatsApp = { lead, customMsg ->
                val whatsappUrl = "https://wa.me/251988828861?text=" + Uri.encode(
                    "Hello ${lead.name},\n" +
                    "Here is the Bekansi Furniture design proposal for *${design.productName}* (${design.category}):\n\n" +
                    "🪵 Material: ${design.material}\n" +
                    "📏 Dimensions: ${design.dimensions}\n" +
                    "🎨 Color/Finish: ${design.color}\n" +
                    "💰 Price Estimate: ETB ${String.format("%,.0f", design.price)}\n\n" +
                    "📷 High-Res Design Photo: ${design.publicUrl.ifBlank { design.imageUri }}\n\n" +
                    customMsg + "\n\n" +
                    "— Bekansi Furniture & Interior Design (Addis Ababa, Ethiopia)\n" +
                    "📞 +251 988 828 861"
                )
                try {
                    uriHandler.openUri(whatsappUrl)
                    viewModel.sendCustomerMessage("Sent Design Proposal for ${design.productName} to ${lead.name} (${lead.phone}) via official WhatsApp Business.")
                    Toast.makeText(context, "Forwarded design to WhatsApp!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                imageToShareInConversation = null
            }
        )
    }
}

// ==========================================
// GRID CARD ITEM COMPONENT
// ==========================================
@Composable
fun GalleryCardGridItem(
    design: ProductDesignImage,
    onClick: () -> Unit,
    onToggleFav: () -> Unit,
    onSetPrimary: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
            .testTag("design_card_${design.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(LightSurfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (design.publicUrl.isNotBlank()) design.publicUrl else design.imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = design.productName,
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LightSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = WarmMahogany
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LightSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Furniture Placeholder",
                                    tint = TextMuted.copy(alpha = 0.6f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Bekansi Design", fontSize = 8.sp, color = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Top badges (Primary & Favorite)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (design.isPrimary) {
                        Surface(
                            color = GoldAccent,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("PRIMARY", color = Color.Black, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = design.category,
                                color = Color.White,
                                fontSize = 8.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleFav,
                        modifier = Modifier
                            .size(26.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (design.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (design.isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Price badge bottom right
                if (design.price > 0.0) {
                    Surface(
                        color = WarmMahogany.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(topStart = 6.dp),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = "ETB ${String.format("%,.0f", design.price)}",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = design.productName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (design.sku.isNotBlank()) {
                    Text(
                        text = "SKU: ${design.sku}",
                        fontSize = 8.5.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Quick Action Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = design.designType,
                        fontSize = 8.sp,
                        color = WarmMahogany,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row {
                        IconButton(onClick = onShare, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = AccentSuccess, modifier = Modifier.size(12.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// LIST VIEW ITEM COMPONENT
// ==========================================
@Composable
fun GalleryListItem(
    design: ProductDesignImage,
    onClick: () -> Unit,
    onToggleFav: () -> Unit,
    onSetPrimary: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, CardBorderGray, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightSurfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (design.publicUrl.isNotBlank()) design.publicUrl else design.imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = design.productName,
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = WarmMahogany)
                        }
                    },
                    error = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = TextMuted, modifier = Modifier.size(24.dp))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                if (design.isPrimary) {
                    Surface(
                        color = GoldAccent,
                        shape = RoundedCornerShape(bottomEnd = 4.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text("⭐", fontSize = 8.sp, modifier = Modifier.padding(2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = design.productName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${design.category} • ${design.material.ifBlank { "Hardwood" }} • ${design.color.ifBlank { "Custom Finish" }}",
                    fontSize = 9.5.sp,
                    color = TextMuted,
                    maxLines = 1
                )

                if (design.tags.isNotBlank()) {
                    Text(
                        text = "Tags: ${design.tags}",
                        fontSize = 8.5.sp,
                        color = WarmMahogany,
                        maxLines = 1
                    )
                }

                if (design.price > 0.0) {
                    Text(
                        text = "ETB ${String.format("%,.0f", design.price)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmMahogany
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onToggleFav, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (design.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Fav",
                        tint = if (design.isFavorite) Color.Red else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = AccentSuccess, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ==========================================
// PREVIEW & METADATA UPLOAD MODAL
// ==========================================
@Composable
fun MultipleImageUploadModal(
    uris: List<Uri>,
    products: List<Product>,
    galleryViewModel: DesignGalleryViewModel,
    onDismiss: () -> Unit,
    onUploadComplete: (List<ProductDesignImage>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val galleryUiState by galleryViewModel.uiState.collectAsState()

    var selectedIndex by remember { mutableIntStateOf(0) }
    var primaryIndex by remember { mutableIntStateOf(0) }
    val mutableUris = remember { mutableStateListOf<Uri>().apply { addAll(uris) } }

    // Form inputs
    var productName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Sofa") }
    var sku by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dimensions by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("Solid Wanza (Cordia Africana)") }
    var priceText by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("Modern, Luxury, Addis Ababa") }
    var designType by remember { mutableStateOf("Showroom Piece") }
    var roomType by remember { mutableStateOf("Living Room") }
    var selectedProductId by remember { mutableStateOf("") }

    val isUploading = galleryUiState.isUploading
    val uploadProgress = galleryUiState.uploadProgress
    val uploadStatusText = galleryUiState.uploadStatusMessage.ifBlank { "Optimizing & uploading high-resolution image assets..." }

    val categories = listOf("Sofa", "Bed", "Wardrobe", "Kitchen Cabinet", "TV Stand", "Dining Table", "Office Furniture", "Custom Furniture", "Interior Design", "Other")

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .border(1.dp, CardBorderGray, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Preview & Product Metadata (${mutableUris.size} Images)",
                            color = WarmMahogany,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Assign high-res photo assets, tags, dimensions and primary showcase image",
                            color = TextMuted,
                            fontSize = 9.5.sp
                        )
                    }
                    if (!isUploading) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isUploading) {
                    // Upload Progress Bar
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uploadStatusText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmMahogany,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { uploadProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = WarmMahogany,
                                trackColor = CardBorderGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${(uploadProgress * 100).toInt()}% Transferred",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                                Text(
                                    if (galleryUiState.currentUploadingFile.isNotBlank()) 
                                        "Asset: ${galleryUiState.currentUploadingFile}" 
                                    else "Retrofit Multi-part Stream",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Section 5: Full Image Preview & Thumbnails Carousel
                        if (mutableUris.isNotEmpty()) {
                            val currentUri = mutableUris.getOrNull(selectedIndex) ?: mutableUris.first()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LightSurfaceVariant)
                            ) {
                                AsyncImage(
                                    model = currentUri,
                                    contentDescription = "Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Primary badge or button
                                if (selectedIndex == primaryIndex) {
                                    Surface(
                                        color = GoldAccent,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                                    ) {
                                        Text("⭐ Primary Design", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                    }
                                } else {
                                    Button(
                                        onClick = { primaryIndex = selectedIndex },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp).height(26.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("Set as Primary Design", color = Color.White, fontSize = 8.5.sp)
                                    }
                                }

                                // Remove Image Button
                                IconButton(
                                    onClick = {
                                        if (mutableUris.size > 1) {
                                            mutableUris.removeAt(selectedIndex)
                                            if (selectedIndex >= mutableUris.size) selectedIndex = mutableUris.size - 1
                                            if (primaryIndex >= mutableUris.size) primaryIndex = 0
                                        } else {
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(26.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Thumbnails Carousel
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(mutableUris.indices.toList()) { idx ->
                                    val uri = mutableUris[idx]
                                    val isCurrent = idx == selectedIndex
                                    val isPrimary = idx == primaryIndex

                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(if (isCurrent) 2.dp else 1.dp, if (isCurrent) WarmMahogany else CardBorderGray, RoundedCornerShape(6.dp))
                                            .clickable { selectedIndex = idx }
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Thumbnail",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        if (isPrimary) {
                                            Surface(
                                                color = GoldAccent,
                                                shape = CircleShape,
                                                modifier = Modifier.align(Alignment.TopStart).padding(2.dp).size(12.dp)
                                            ) {}
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Section 6: Product Information Form
                        Text("Product Information (Required)", color = WarmMahogany, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = productName,
                            onValueChange = { productName = it },
                            label = { Text("Product / Design Name *", fontSize = 10.sp) },
                            placeholder = { Text("e.g. Sheger Curved Master Sofa", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("upload_product_name_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Dropdown / Selector
                        Text("Category *", fontSize = 10.sp, color = TextMuted)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 9.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = WarmMahogany,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                label = { Text("Product Code / SKU", fontSize = 9.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                label = { Text("Price (ETB)", fontSize = 9.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = dimensions,
                                onValueChange = { dimensions = it },
                                label = { Text("Dimensions (e.g. 200x220cm)", fontSize = 9.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = color,
                                onValueChange = { color = it },
                                label = { Text("Color / Tone", fontSize = 9.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = material,
                            onValueChange = { material = it },
                            label = { Text("Material (Wanza, Mahogany, Acacia, Velvet...)", fontSize = 9.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("Tags (Modern, Luxury, Walnut, 1.50m...)", fontSize = 9.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description & Craftsmanship Notes", fontSize = 9.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Link to existing product if desired
                        if (products.isNotEmpty()) {
                            Text("Assign to Existing Product Catalog Item (Optional):", fontSize = 9.5.sp, color = TextMuted)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                items(products) { prod ->
                                    val isSelected = selectedProductId == prod.id.toString()
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (isSelected) {
                                                selectedProductId = ""
                                            } else {
                                                selectedProductId = prod.id.toString()
                                                if (productName.isBlank()) productName = prod.name
                                                if (dimensions.isBlank()) dimensions = prod.dimensions
                                                if (material.isBlank()) material = prod.material
                                            }
                                        },
                                        label = { Text("${prod.name} (#${prod.id})", fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentSuccess, selectedLabelColor = Color.White)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Buttons
                if (!isUploading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextMuted, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val finalName = if (productName.isBlank()) {
                                    "${selectedCategory} Design #${System.currentTimeMillis().toString().takeLast(4)}"
                                } else productName

                                val basePrice = priceText.toDoubleOrNull() ?: 0.0

                                galleryViewModel.uploadSelectedDesigns(
                                    context = context,
                                    productName = finalName,
                                    category = selectedCategory,
                                    sku = sku,
                                    description = description,
                                    dimensions = dimensions,
                                    color = color,
                                    material = material,
                                    price = basePrice,
                                    tags = tags,
                                    designType = designType,
                                    roomType = roomType,
                                    productId = selectedProductId,
                                    customUris = mutableUris.toList(),
                                    onSuccess = onUploadComplete
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("confirm_upload_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Upload", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload ${mutableUris.size} Design(s)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SECTION 18: GALLERY DETAIL MODAL
// ==========================================
@Composable
fun DesignDetailModal(
    design: ProductDesignImage,
    products: List<Product>,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetPrimary: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onUseInQuotation: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .border(1.dp, CardBorderGray, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Large Product Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .background(LightSurfaceVariant)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(if (design.publicUrl.isNotBlank()) design.publicUrl else design.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = design.productName,
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = WarmMahogany)
                            }
                        },
                        error = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(30.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    // Primary indicator
                    if (design.isPrimary) {
                        Surface(
                            color = GoldAccent,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                        ) {
                            Text("⭐ PRIMARY SHOWROOM DESIGN", color = Color.Black, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = design.productName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmMahogany
                            )
                            Text(
                                text = "Category: ${design.category} | SKU: ${design.sku.ifBlank { "N/A" }}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        if (design.price > 0.0) {
                            Text(
                                text = "ETB ${String.format("%,.0f", design.price)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentSuccess
                            )
                        }
                    }

                    HorizontalDivider(color = CardBorderGray, modifier = Modifier.padding(vertical = 8.dp))

                    // Attributes Matrix
                    DetailAttributeRow("Material", design.material.ifBlank { "Solid Ethiopian Hardwood" })
                    DetailAttributeRow("Dimensions", design.dimensions.ifBlank { "Custom Configurable" })
                    DetailAttributeRow("Color / Finish", design.color.ifBlank { "Warm Mahogany / Wanza Natural" })
                    DetailAttributeRow("Design Style", design.designType)
                    DetailAttributeRow("Room Type", design.roomType)
                    DetailAttributeRow("Tags", design.tags.ifBlank { "None" })
                    DetailAttributeRow("Uploaded By", design.uploadedBy)
                    DetailAttributeRow("Tenant ID", design.tenantId)

                    if (design.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Description", color = WarmMahogany, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(design.description, color = TextDark, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Actions Panel
                    Text("Actions & CRM Dispatch", color = WarmMahogany, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. WhatsApp Customer Proposal
                    Button(
                        onClick = onShareWhatsApp,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use in Customer Conversation / WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!design.isPrimary) {
                            OutlinedButton(
                                onClick = onSetPrimary,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Set Primary", fontSize = 10.sp, color = TextDark)
                            }
                        }

                        Button(
                            onClick = onToggleFavorite,
                            colors = ButtonDefaults.buttonColors(containerColor = if (design.isFavorite) Color(0xFFFFEBEE) else LightSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(
                                if (design.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (design.isFavorite) Color.Red else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (design.isFavorite) "Saved" else "Favorite", fontSize = 10.sp, color = TextDark)
                        }

                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", fontSize = 10.sp, color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailAttributeRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 9.5.sp)
        Text(text = value, color = TextDark, fontSize = 9.5.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
    }
}

// ==========================================
// SECTION 16: SHARE TO CONVERSATION DIALOG
// ==========================================
@Composable
fun ShareToConversationDialog(
    design: ProductDesignImage,
    leads: List<Lead>,
    onDismiss: () -> Unit,
    onSendWhatsApp: (Lead, String) -> Unit
) {
    var selectedLead by remember { mutableStateOf(leads.firstOrNull()) }
    var customMessage by remember { mutableStateOf("We would love to welcome you to our Bole Showroom to inspect this finish in person.") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorderGray, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Send Design via WhatsApp Business",
                    color = WarmMahogany,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Select customer lead to receive this curated ${design.productName} proposal.",
                    color = TextMuted,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Select Customer Lead:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
                if (leads.isEmpty()) {
                    Text("No CRM leads registered yet. Using general showroom inquiry.", fontSize = 9.sp, color = TextMuted)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                        items(leads) { lead ->
                            val isSelected = selectedLead?.id == lead.id
                            Surface(
                                color = if (isSelected) WarmMahogany.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedLead = lead }
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${lead.name} (${lead.phone})", fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = TextDark)
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = WarmMahogany, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text("Personalized Note", fontSize = 9.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            val leadToUse = selectedLead ?: Lead(name = "Valued Customer", phone = "0988828861", email = "", status = "Hot", source = "WhatsApp", requirements = design.productName, notes = "", language = "English")
                            onSendWhatsApp(leadToUse, customMessage)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send via WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
