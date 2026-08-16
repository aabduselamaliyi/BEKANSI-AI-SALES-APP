package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.TextStyle
import com.example.service.MediaUploadService
import com.example.service.MediaUploadWorker
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun DesignAlbumsTab(viewModel: SalesViewModel) {
    val context = LocalContext.current

    // Observe streams from Room Database
    val categories by viewModel.allCategories.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()
    val favorites by viewModel.allFavorites.collectAsState()
    val selections by viewModel.allSelections.collectAsState()
    val analytics by viewModel.allAnalytics.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    // Sub tabs within Albums Module
    var activeSubTab by remember { mutableStateOf("GALLERY") } // GALLERY, ADVISOR, WISHLIST, ADMIN_DASHBOARD

    // Selected album for detail modal dialog
    var selectedAlbumForDetail by remember { mutableStateOf<ProductAlbum?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // High-end inner sub-tabs navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, CardBorderGray, RoundedCornerShape(8.dp))
                .horizontalScroll(rememberScrollState())
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SubTabButton(
                title = "Showroom Gallery",
                icon = Icons.Default.Home,
                isSelected = activeSubTab == "GALLERY",
                onClick = { activeSubTab = "GALLERY" }
            )
            SubTabButton(
                title = "Design Portfolio",
                icon = Icons.Default.Star,
                isSelected = activeSubTab == "PORTFOLIO",
                onClick = { activeSubTab = "PORTFOLIO" }
            )
            SubTabButton(
                title = "Media & DAM Center",
                icon = Icons.Default.AddCircle,
                isSelected = activeSubTab == "DAM_UPLOAD",
                onClick = { activeSubTab = "DAM_UPLOAD" }
            )
            SubTabButton(
                title = "AI Media & SMM",
                icon = Icons.Default.Share,
                isSelected = activeSubTab == "AI_SMM_LINK",
                onClick = { activeSubTab = "AI_SMM_LINK" }
            )
            SubTabButton(
                title = "Saved Favorites",
                icon = Icons.Default.Favorite,
                isSelected = activeSubTab == "WISHLIST",
                onClick = { activeSubTab = "WISHLIST" }
            )
            SubTabButton(
                title = "DAM Analytics",
                icon = Icons.Default.Info,
                isSelected = activeSubTab == "ANALYTICS",
                onClick = { activeSubTab = "ANALYTICS" }
            )
            SubTabButton(
                title = "Admin Portal",
                icon = Icons.Default.Settings,
                isSelected = activeSubTab == "ADMIN_DASHBOARD",
                onClick = { activeSubTab = "ADMIN_DASHBOARD" }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle modules
        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "GALLERY" -> {
                    GalleryCatalogModule(
                        categories = categories,
                        albums = albums,
                        favorites = favorites,
                        selectedLanguage = selectedLanguage,
                        onAlbumClick = { album ->
                            viewModel.incrementViewCount(album.id)
                            selectedAlbumForDetail = album
                        },
                        onToggleFavorite = { album ->
                            viewModel.toggleFavorite("0911000000", album.id)
                            Toast.makeText(context, "Updated wishlist!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                "PORTFOLIO" -> {
                    InteriorDesignPortfolioModule(context, viewModel)
                }
                "DAM_UPLOAD" -> {
                    MediaUploadDamModule(context, viewModel)
                }
                "AI_SMM_LINK" -> {
                    AiMediaSmmIntegrationModule(context, viewModel, albums)
                }
                "WISHLIST" -> {
                    FavoritesAndCompareModule(
                        albums = albums,
                        favorites = favorites,
                        selectedLanguage = selectedLanguage,
                        onAlbumClick = { album ->
                            viewModel.incrementViewCount(album.id)
                            selectedAlbumForDetail = album
                        },
                        onRemoveFavorite = { albumId ->
                            viewModel.toggleFavorite("0911000000", albumId)
                            Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                "ANALYTICS" -> {
                    DamAnalyticsModule(context)
                }
                "ADMIN_DASHBOARD" -> {
                    AdminDashboardModule(
                        categories = categories,
                        albums = albums,
                        selections = selections,
                        favorites = favorites,
                        analytics = analytics,
                        onAddAlbum = { newAlbum ->
                            viewModel.addAlbum(newAlbum)
                            Toast.makeText(context, "New Design Album Seeding added!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Curated design album details modal with contextual CRM actions
    selectedAlbumForDetail?.let { album ->
        AlbumDetailModal(
            album = album,
            favorites = favorites,
            selectedLanguage = selectedLanguage,
            onDismiss = { selectedAlbumForDetail = null },
            onToggleFavorite = {
                viewModel.toggleFavorite("0911000000", album.id)
            },
            onSubmitQuotation = { phone, name, location, budget, req ->
                viewModel.requestQuotationForAlbum(phone, name, location, album.id, budget, req)
                selectedAlbumForDetail = null
            },
            onSubmitCustomization = { phone, name, customSpec ->
                viewModel.requestCustomizationForAlbum(phone, name, album.id, customSpec)
                selectedAlbumForDetail = null
            }
        )
    }
}

@Composable
fun SubTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) WarmMahogany else Color.Transparent,
            contentColor = if (isSelected) TextLight else TextMuted
        ),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        modifier = modifier.height(38.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ==========================================
// 1. SHOWROOM CATALOG GALLERY MODULE
// ==========================================
@Composable
fun GalleryCatalogModule(
    categories: List<AlbumCategory>,
    albums: List<ProductAlbum>,
    favorites: List<CustomerFavorite>,
    selectedLanguage: String,
    onAlbumClick: (ProductAlbum) -> Unit,
    onToggleFavorite: (ProductAlbum) -> Unit
) {
    var activeCategoryFilter by remember { mutableStateOf("Bedroom Sets") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Categories list
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat.name == activeCategoryFilter
                FilterChip(
                    selected = isSelected,
                    onClick = { activeCategoryFilter = cat.name },
                    label = {
                        val lbl = when (selectedLanguage) {
                            "am" -> cat.nameAm
                            "om" -> cat.nameOm
                            else -> cat.name
                        }
                        Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldAccent,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkWarmCard,
                        labelColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Hero Banner depicting current category
        CategoryDecorativeBanner(categoryName = activeCategoryFilter, selectedLanguage = selectedLanguage)

        Spacer(modifier = Modifier.height(8.dp))

        // Multi-Albums dynamic grid
        val filteredAlbums = albums.filter { it.category == activeCategoryFilter }
        if (filteredAlbums.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No albums registered in this category.", color = TextMuted, fontSize = 12.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
            ) {
                items(filteredAlbums) { album ->
                    val isFaved = favorites.any { it.albumId == album.id }
                    AlbumCardGridItem(
                        album = album,
                        isFaved = isFaved,
                        selectedLanguage = selectedLanguage,
                        onClick = { onAlbumClick(album) },
                        onToggleFav = { onToggleFavorite(album) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDecorativeBanner(categoryName: String, selectedLanguage: String) {
    val description = when (categoryName) {
        "Bedroom Sets" -> mapOf("en" to "Fine heirloom bed sets, nightstands, and luxury dressers.", "am" to "የተመረጡ የመኝታ አልጋዎች፣ የጎን ጠረጴዛዎችና የቅንጦት መስተዋቶች።", "om" to "Dizaayinii siree, saanduqa fi dhimma dhuunfaa mijeessu.")
        "Wardrobes" -> mapOf("en" to "Elegant modern sliding and custom walk-in organizers.", "am" to "ተንሸራታች እና ብጁ የልብስ ማስቀመጫ ቁምሳጥኖች።", "om" to "Masaalota uffataa filatamaa bal'ina qaban.")
        "Kitchen Cabinets" -> mapOf("en" to "Contemporary modular kitchen storage countertops.", "am" to "ዘመናዊ የወጥ ቤት ሞዱላር ካቢኔቶችና ጠረጴዛዎች።", "om" to "Kaabiineeta kichinii ammayyaa miidhagaa.")
        "TV Stands" -> mapOf("en" to "Minimalist floating consoles and entertainment systems.", "am" to "ግድግዳ ላይ የሚሰቀሉ የቲቪ ማስቀመጫዎችና ኮንሶሎች።", "om" to "Maasii TV haala ammayyaan midhaafame.")
        "Dining Sets" -> mapOf("en" to "Solid precious timber tables and tailor leather chairs.", "am" to "ከሀገር በቀል ጠንካራ እንጨት የተሰሩ የምግብ ጠረጴዛዎች።", "om" to "Gabatee nyaataa maatii hundaa mijeessu.")
        else -> mapOf("en" to "Curated hand-made luxury elements, designed in Addis Ababa.", "am" to "በአዲስ አበባ የተነደፉ በእጅ የተሰሩ ምርጥ የቤት እቃዎች።", "om" to "Dizayinii bilisaa fedhan qulqullina olaanan.")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = WarmMahogany),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName,
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description[selectedLanguage] ?: description["en"]!!,
                    color = TextLight.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Decor",
                tint = GoldAccent.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AlbumCardGridItem(
    album: ProductAlbum,
    isFaved: Boolean,
    selectedLanguage: String,
    onClick: () -> Unit,
    onToggleFav: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .testTag("album_card_${album.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                // Async image loading using Coil
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(album.imageUrls.split(",").firstOrNull())
                        .crossfade(true)
                        .build(),
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = album.id,
                            color = GoldAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onToggleFav,
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFaved) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isFaved) Color.Red else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Production time badge bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = album.estimatedProductionTime,
                        color = Color.White,
                        fontSize = 8.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = album.name,
                    color = TextLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = album.designStyle,
                        color = GoldAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Score",
                            tint = GoldAccent,
                            modifier = Modifier.size(9.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${album.popularityScore}%",
                            color = TextLight,
                            fontSize = 8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ETB ${String.format("%,.0f", album.priceRangeLower)} - ${String.format("%,.0f", album.priceRangeUpper)}",
                    color = AccentSuccess,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// 2. AI RECOMMENDATION ENGINE ADVISOR MODULE
// ==========================================
@Composable
fun AiAdvisorModule(
    albums: List<ProductAlbum>,
    selectedLanguage: String,
    onAlbumClick: (ProductAlbum) -> Unit
) {
    var budgetInput by remember { mutableStateOf("150000") }
    var preferredStyle by remember { mutableStateOf("Modern Luxury") }
    var preferredMaterial by remember { mutableStateOf("Wanza") }
    var preferredColor by remember { mutableStateOf("Walnut") }
    
    var recommendationsList by remember { mutableStateOf<List<ProductAlbum>>(emptyList()) }
    var recommendationOutputText by remember { mutableStateOf("") }
    var hasRequested by remember { mutableStateOf(false) }

    val styles = listOf("Modern Luxury", "Scandinavian", "Contemporary", "Classic", "Minimalist")
    val materials = listOf("Wanza", "Mahogany", "Grar", "MDF", "Oak")
    val colors = listOf("Walnut", "Dark Cocoa", "Natural Oak", "Espresso", "Classic White")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(4.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Bekansi Smart Advisor Engine",
                    color = GoldAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Enter your requirements below. Our high-fidelity AI recommendation engine will search the 50+ albums to find your match.",
                    color = TextMuted,
                    fontSize = 9.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("Max Budget (ETB)", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Design Style", fontSize = 9.sp, color = TextMuted)
                        Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                            // Quick Rotate Style
                            val nextIdx = (styles.indexOf(preferredStyle) + 1) % styles.size
                            preferredStyle = styles[nextIdx]
                        }, contentAlignment = Alignment.CenterStart) {
                            Text(preferredStyle, fontSize = 11.sp, color = TextLight, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hardwood Species", fontSize = 9.sp, color = TextMuted)
                        Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                            val nextIdx = (materials.indexOf(preferredMaterial) + 1) % materials.size
                            preferredMaterial = materials[nextIdx]
                        }, contentAlignment = Alignment.CenterStart) {
                            Text(preferredMaterial, fontSize = 11.sp, color = TextLight, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Stain Finish Paint", fontSize = 9.sp, color = TextMuted)
                        Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                            val nextIdx = (colors.indexOf(preferredColor) + 1) % colors.size
                            preferredColor = colors[nextIdx]
                        }, contentAlignment = Alignment.CenterStart) {
                            Text(preferredColor, fontSize = 11.sp, color = TextLight, modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }

                Button(
                    onClick = {
                        val maxB = budgetInput.toDoubleOrNull() ?: 150000.0
                        // Perform search over the 50 albums
                        val subset = albums.filter {
                            it.priceRangeLower <= maxB &&
                            (it.designStyle.lowercase().contains(preferredStyle.lowercase()) ||
                             it.materialOptions.lowercase().contains(preferredMaterial.lowercase()) ||
                             it.colorOptions.lowercase().contains(preferredColor.lowercase()))
                        }.sortedByDescending { it.popularityScore }.take(3)

                        recommendationsList = subset
                        hasRequested = true

                        // Build recommendation commentary
                        val matchedIds = if (subset.isEmpty()) "None" else subset.joinToString { it.id }
                        recommendationOutputText = when(selectedLanguage) {
                            "am" -> "የእርስዎን ዘመናዊ ምርጫዎች መሠረት በማድረግ ለየምርጥ እንጨት ጥራት በጀት ${String.format("%,.0f", maxB)} ብር፣ የሚከተሉትን አልበሞች እንመክራለን: $matchedIds።"
                            "om" -> "Filannoo keessan irratti hundhaa'un bajata ${String.format("%,.0f", maxB)} ETB xiyyeeffachuun albumoota dizaayinii: $matchedIds isiniif gorsina."
                            else -> "Based on your preference for $preferredStyle styles with $preferredMaterial hardwood ($preferredColor finish) and a max budget of ETB ${String.format("%,.0f", maxB)}, I recommend Albums $matchedIds."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("get_recommendations_button")
                ) {
                    Text("Generate Personalized Recommendations", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        if (hasRequested) {
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = LightWarmCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "AI Consultative Response",
                        color = WarmMahogany,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = recommendationOutputText,
                        color = TextDark,
                        fontSize = 10.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Matches Discovered (${recommendationsList.size})", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recommendationsList.forEach { alb ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumClick(alb) }
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = alb.imageUrls.split(",").firstOrNull(),
                                contentDescription = alb.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(alb.name, color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("${alb.category} • ${alb.designStyle}", color = GoldAccent, fontSize = 9.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("ETB ${String.format("%,.0f", alb.priceRangeUpper)}", color = AccentSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = "", tint = GoldAccent, modifier = Modifier.size(10.dp))
                                    Text("${alb.popularityScore}%", color = TextLight, fontSize = 8.sp)
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
// 3. FAVORITES SYSTEM & SIDE-BY-SIDE COMPARE
// ==========================================
@Composable
fun FavoritesAndCompareModule(
    albums: List<ProductAlbum>,
    favorites: List<CustomerFavorite>,
    selectedLanguage: String,
    onAlbumClick: (ProductAlbum) -> Unit,
    onRemoveFavorite: (String) -> Unit
) {
    val favesList = albums.filter { alb -> favorites.any { it.albumId == alb.id } }

    var comparisonAlbumId1 by remember { mutableStateOf<String?>(null) }
    var comparisonAlbumId2 by remember { mutableStateOf<String?>(null) }
    var showComparisonResult by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Design Comparison Workspace", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Compare up to 2 saved designs side-by-side on materials, size, delivery cost slots.", color = TextMuted, fontSize = 8.sp)
                
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Slot 1
                    Box(modifier = Modifier.weight(1f).height(40.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                        // Cycle through favorites names for slot 1
                        if (favesList.isNotEmpty()) {
                            val currIdx = favesList.indexOfFirst { it.id == comparisonAlbumId1 }
                            val nextIdx = (currIdx + 1) % favesList.size
                            comparisonAlbumId1 = favesList[nextIdx].id
                        }
                    }, contentAlignment = Alignment.Center) {
                        Text(comparisonAlbumId1 ?: "Choose Design 1", fontSize = 10.sp, color = if (comparisonAlbumId1 != null) GoldAccent else TextMuted)
                    }

                    // Slot 2
                    Box(modifier = Modifier.weight(1f).height(40.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                        // Cycle through favorites names for slot 2
                        if (favesList.isNotEmpty()) {
                            val currIdx = favesList.indexOfFirst { it.id == comparisonAlbumId2 }
                            val nextIdx = (currIdx + 1) % favesList.size
                            comparisonAlbumId2 = favesList[nextIdx].id
                        }
                    }, contentAlignment = Alignment.Center) {
                        Text(comparisonAlbumId2 ?: "Choose Design 2", fontSize = 10.sp, color = if (comparisonAlbumId2 != null) GoldAccent else TextMuted)
                    }

                    Button(
                        onClick = {
                            if (comparisonAlbumId1 != null && comparisonAlbumId2 != null) {
                                showComparisonResult = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                        enabled = comparisonAlbumId1 != null && comparisonAlbumId2 != null,
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Compare", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showComparisonResult && comparisonAlbumId1 != null && comparisonAlbumId2 != null) {
            val d1 = albums.find { it.id == comparisonAlbumId1 }
            val d2 = albums.find { it.id == comparisonAlbumId2 }
            if (d1 != null && d2 != null) {
                // Render Comparison Grid Dialog
                Dialog(onDismissRequest = { showComparisonResult = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCocoaBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Bespoke Comparison Matrix", color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            
                            Divider(color = Color.White.copy(alpha = 0.1f))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                keyCompareRow("Design Detail", d1.name, d2.name)
                            }
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("ID", d1.id, d2.id)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Style", d1.designStyle, d2.designStyle)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Wood species", d1.materialOptions, d2.materialOptions)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Finishes", d1.colorOptions, d2.colorOptions)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Dimensions", d1.dimensions, d2.dimensions)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Production Era", d1.estimatedProductionTime, d2.estimatedProductionTime)
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            keyCompareRow("Price Frame (ETB)", "ETB ${String.format("%,.0f", d1.priceRangeLower)}", "ETB ${String.format("%,.0f", d2.priceRangeLower)}")

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showComparisonResult = false },
                                colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close Comparison View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Text("Your Curated Wishlist (${favesList.size})", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(6.dp))

        if (favesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "", tint = TextMuted, modifier = Modifier.size(34.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("No designs saved yet. Visit Showroom Gallery!", color = TextMuted, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favesList) { album ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumClick(album) }
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = album.imageUrls.split(",").firstOrNull(),
                                contentDescription = album.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(album.name, color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(album.id, color = GoldAccent, fontSize = 9.sp)
                                Text("ETB ${String.format("%,.0f", album.priceRangeLower)} - ${String.format("%,.0f", album.priceRangeUpper)}", color = AccentSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { onRemoveFavorite(album.id) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun keyCompareRow(title: String, val1: String, val2: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(title, color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
        Text(val1, color = TextLight, fontSize = 8.sp, modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
        Text(val2, color = TextLight, fontSize = 8.sp, modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
    }
}

// ==========================================
// 4. ADMIN PORTAL METRICS & ALBUM MANAGEMENTS
// ==========================================
@Composable
fun AdminDashboardModule(
    categories: List<AlbumCategory>,
    albums: List<ProductAlbum>,
    selections: List<CustomerSelection>,
    favorites: List<CustomerFavorite>,
    analytics: List<AlbumAnalytics>,
    onAddAlbum: (ProductAlbum) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }

    var adminId by remember { mutableStateOf("BS-011") }
    var adminName by remember { mutableStateOf("Adama Modern King Suite") }
    var adminCategory by remember { mutableStateOf("Bedroom Sets") }
    var adminStyle by remember { mutableStateOf("Modern Luxury") }
    var adminPrice by remember { mutableStateOf("145000") }
    var adminMaterials by remember { mutableStateOf("Wanza, Acacia") }
    var adminColors by remember { mutableStateOf("Walnut finish") }
    var adminDescEn by remember { mutableStateOf("An outstanding carved bed with matching bedside structures.") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("E-Commerce PM Dashboard", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddForm = !showAddForm },
                colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                modifier = Modifier.height(28.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(if (showAddForm) "Close Form" else "Add New Album", fontSize = 9.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (showAddForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Register Curated Design Masterpiece", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextField(
                            value = adminId,
                            onValueChange = { adminId = it },
                            label = { Text("Album ID (e.g. BS-011)", fontSize = 8.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                        TextField(
                            value = adminName,
                            onValueChange = { adminName = it },
                            label = { Text("Name", fontSize = 8.sp) },
                            modifier = Modifier.weight(2f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextField(
                            value = adminPrice,
                            onValueChange = { adminPrice = it },
                            label = { Text("Average Price (ETB)", fontSize = 8.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                        TextField(
                            value = adminMaterials,
                            onValueChange = { adminMaterials = it },
                            label = { Text("Timber", fontSize = 8.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                        )
                    }

                    TextField(
                        value = adminDescEn,
                        onValueChange = { adminDescEn = it },
                        label = { Text("Aesthetic details / Customization bounds", fontSize = 8.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                    )

                    Button(
                        onClick = {
                            val pr = adminPrice.toDoubleOrNull() ?: 100000.0
                            val mockUrl = "https://images.unsplash.com/photo-1540518614846-7eded433c457?w=600"
                            val alb = ProductAlbum(
                                id = adminId,
                                name = adminName,
                                category = adminCategory,
                                designStyle = adminStyle,
                                descriptionEn = adminDescEn,
                                descriptionAm = "በበካንሲ ባለሙያዎች የተሰራ $adminName",
                                descriptionOm = "Meesha bareedaa haala kanaan hojjetame.",
                                dimensions = "220cm x 110cm x 90cm",
                                materialOptions = adminMaterials,
                                colorOptions = adminColors,
                                estimatedProductionTime = "14 Days",
                                priceRangeLower = pr - 15000.0,
                                priceRangeUpper = pr + 15000.0,
                                popularityScore = 85,
                                tags = "new, solid-wood, fine finish",
                                imageUrls = mockUrl
                            )
                            onAddAlbum(alb)
                            showAddForm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Design To Online Catalog", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Metrics Charts
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Operational Analytics Matrix", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricsCounter(title = "Total Catalog size", value = "${albums.size} Albums")
                    MetricsCounter(title = "Selection leads", value = "${selections.size} Captured")
                    MetricsCounter(title = "Total Fanbookmarks", value = "${favorites.size} Hearts")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Dynamic Product Album Rankings", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(4.dp))

        // Analytics list of albums sorted by metrics
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(albums.sortedByDescending { it.popularityScore }.take(8)) { alb ->
                val stats = analytics.find { it.albumId == alb.id }
                val views = stats?.viewCount ?: (20..150).random()
                val hearts = stats?.favoriteCount ?: (3..25).random()
                val sel = stats?.selectionCount ?: (1..15).random()
                
                // Pure scientific formula for e-commerce conversion: (Selections / Views) * 100
                val rate = if (views > 0) (sel.toFloat() / views.toFloat() * 100f) else 0.0f

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkWarmCard),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text("${alb.id} : ${alb.name}", color = TextLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(alb.category, color = TextMuted, fontSize = 8.sp)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(2f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$views", color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("Views", color = TextMuted, fontSize = 7.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$hearts", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("Saver", color = TextMuted, fontSize = 7.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$sel", color = AccentSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("Selected", color = TextMuted, fontSize = 7.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(String.format("%.1f%%", rate), color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("CVR", color = TextMuted, fontSize = 7.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricsCounter(title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.fillMaxWidth().height(45.dp).border(0.5.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(6.dp)).padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = TextMuted, fontSize = 7.sp)
            Text(value, color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 5. SELECTION DETAIL MODAL & LEAD CAPTURE FORM
// ==========================================
@Composable
fun AlbumDetailModal(
    album: ProductAlbum,
    favorites: List<CustomerFavorite>,
    selectedLanguage: String,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSubmitQuotation: (phone: String, name: String, location: String, budget: Double, requirements: String) -> Unit,
    onSubmitCustomization: (phone: String, name: String, customizedText: String) -> Unit
) {
    var activeActionTab by remember { mutableStateOf("SPECS") } // SPECS, QUOTE_REQ, CUSTOMIZE
    val isFaved = favorites.any { it.albumId == album.id }

    // Forms components
    var custName by remember { mutableStateOf("") }
    var custPhone by remember { mutableStateOf("") }
    var custLoc by remember { mutableStateOf("Addis Ababa") }
    var custBudget by remember { mutableStateOf(album.priceRangeLower.toString()) }
    var custReqs by remember { mutableStateOf("") }
    var customSpecs by remember { mutableStateOf("") }

    val locationsList = listOf("Addis Ababa", "Adama", "Hawassa", "Jimma", "Dire Dawa", "Bahir Dar")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCocoaBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("album_details_modal_${album.id}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Image Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = album.imageUrls.split(",").firstOrNull(),
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                // Header Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(album.name, color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${album.category} • ID: ${album.id}", color = GoldAccent, fontSize = 9.sp)
                    }

                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isFaved) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "",
                            tint = if (isFaved) Color.Red else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Middle Interaction Selector
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(6.dp)).padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { activeActionTab = "SPECS" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeActionTab == "SPECS") WarmMahogany else Color.Transparent, contentColor = TextLight),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.3f).height(30.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Aesthetic specs", fontSize = 9.sp)
                    }

                    Button(
                        onClick = { activeActionTab = "QUOTE_REQ" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeActionTab == "QUOTE_REQ") WarmMahogany else Color.Transparent, contentColor = TextLight),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.5f).height(30.dp).testTag("select_quote_tab"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Request Quotation", fontSize = 9.sp)
                    }

                    Button(
                        onClick = { activeActionTab = "CUSTOMIZE" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeActionTab == "CUSTOMIZE") WarmMahogany else Color.Transparent, contentColor = TextLight),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.5f).height(30.dp).testTag("select_customize_tab"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Custom Adaptation", fontSize = 9.sp)
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.1f))

                // Toggle internal view blocks
                when (activeActionTab) {
                    "SPECS" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Translate description inline
                            val dText = when (selectedLanguage) {
                                "am" -> album.descriptionAm
                                "om" -> album.descriptionOm
                                else -> album.descriptionEn
                            }
                            Text(dText, color = TextLight, fontSize = 9.sp, lineHeight = 14.sp)

                            Spacer(modifier = Modifier.height(4.dp))

                            SpecAttributeLine(label = "Timber Material", value = album.materialOptions)
                            SpecAttributeLine(label = "Fine Stain colors", value = album.colorOptions)
                            SpecAttributeLine(label = "Default Dimensions", value = album.dimensions)
                            SpecAttributeLine(label = "Production Era", value = album.estimatedProductionTime)
                            SpecAttributeLine(label = "Estimated Frame", value = "ETB ${String.format("%,.0f", album.priceRangeLower)} - ${String.format("%,.0f", album.priceRangeUpper)}")
                            SpecAttributeLine(label = "Design aesthetic", value = album.designStyle)
                        }
                    }

                    "QUOTE_REQ" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Fast Quotation & Lead capture", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Submit your info. Our sales hub will parse budget allocations and log inside CRM immediately.", color = TextMuted, fontSize = 8.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextField(
                                    value = custName,
                                    onValueChange = { custName = it },
                                    label = { Text("Name", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).testTag("quote_name_input"),
                                    colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                                )
                                TextField(
                                    value = custPhone,
                                    onValueChange = { custPhone = it },
                                    label = { Text("Phone Number", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).testTag("quote_phone_input"),
                                    colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val locations = locationsList
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Showroom nearest location", fontSize = 8.sp, color = TextMuted)
                                    Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Black.copy(alpha = 0.2f)).border(1.dp, Color.Gray.copy(alpha = 0.3f)).clickable {
                                        val idx = (locations.indexOf(custLoc) + 1) % locations.size
                                        custLoc = locations[idx]
                                    }, contentAlignment = Alignment.CenterStart) {
                                        Text(custLoc, fontSize = 11.sp, color = TextLight, modifier = Modifier.padding(horizontal = 8.dp))
                                    }
                                }

                                TextField(
                                    value = custBudget,
                                    onValueChange = { custBudget = it },
                                    label = { Text("Target Budget (ETB)", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).testTag("quote_budget_input"),
                                    colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                                )
                            }

                            TextField(
                                value = custReqs,
                                onValueChange = { custReqs = it },
                                label = { Text("Special dimensions / Custom tweaks", fontSize = 8.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("quote_req_input"),
                                colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                            )

                            Button(
                                onClick = {
                                    val bVal = custBudget.toDoubleOrNull() ?: album.priceRangeLower
                                    if (custName.isNotBlank() && custPhone.isNotBlank()) {
                                        onSubmitQuotation(custPhone, custName, custLoc, bVal, custReqs)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_quote_button")
                            ) {
                                Text("Generate Automated Quotation in CRM", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }

                    "CUSTOMIZE" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Bespoke Design Customization", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Need bespoke alterations? Talk to sales agents through simulated chat channel.", color = TextMuted, fontSize = 8.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextField(
                                    value = custName,
                                    onValueChange = { custName = it },
                                    label = { Text("Name", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).testTag("custom_name_input"),
                                    colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                                )
                                TextField(
                                    value = custPhone,
                                    onValueChange = { custPhone = it },
                                    label = { Text("Phone", fontSize = 8.sp) },
                                    modifier = Modifier.weight(1f).testTag("custom_phone_input"),
                                    colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                                )
                            }

                            TextField(
                                value = customSpecs,
                                onValueChange = { customSpecs = it },
                                label = { Text("How would you adapt this design style? (Materials, sizes, glass, leather)", fontSize = 8.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("custom_specs_input"),
                                colors = TextFieldDefaults.colors(focusedIndicatorColor = WarmMahogany)
                            )

                            Button(
                                onClick = {
                                    if (custName.isNotBlank() && custPhone.isNotBlank() && customSpecs.isNotBlank()) {
                                        onSubmitCustomization(custPhone, custName, customSpecs)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_customization_button")
                            ) {
                                Text("Forward Request to Consultant Chat", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecAttributeLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = WarmMahogany, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = TextDark, fontSize = 9.sp, textAlign = TextAlign.End)
    }
}

// ==========================================
// 2. INTERIOR DESIGN PORTFOLIO MODULE
// ==========================================
@Composable
fun InteriorDesignPortfolioModule(context: android.content.Context, viewModel: SalesViewModel) {
    var selectedCategory by remember { mutableStateOf("All Projects") }
    var selectedStyle by remember { mutableStateOf("All Styles") }
    var showConsultationDialog by remember { mutableStateOf(false) }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var projectType by remember { mutableStateOf("Residential Home") }

    val categories = listOf("All Projects", "Before & After", "Completed Homes", "Office Projects", "Hotels & Resorts", "Schools & Univ", "Restaurants", "Apartments", "Luxury Custom")
    val styles = listOf("All Styles", "Modern Minimalist", "Ethiopian Mahogany", "Contemporary Modular", "Classic Heritage")

    val portfolioItems = remember {
        listOf(
            PortfolioProject("1", "Bole Luxury Penthouse", "Completed Homes", "Ethiopian Mahogany", "Addis Ababa", "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=600", "Transforming 4-bedroom living & master bed into bespoke walnut retreat.", "ETB 1,850,000", "5 Stars"),
            PortfolioProject("2", "Commercial HQ Reception", "Office Projects", "Modern Minimalist", "Kazanchis", "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=600", "Custom reception counter, executive suites, and ergonomic conference desks.", "ETB 2,400,000", "5 Stars"),
            PortfolioProject("3", "Bishoftu Resort Villas", "Hotels & Resorts", "Contemporary Modular", "Bishoftu", "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=600", "Furnishing 12 eco-luxury villas with teak patio beds and modular dining.", "ETB 4,100,000", "5 Stars"),
            PortfolioProject("4", "Old Airport Villa Before & After", "Before & After", "Classic Heritage", "Old Airport", "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600", "Complete interior overhaul from dated layout to open-plan luxury mahogany.", "ETB 1,200,000", "5 Stars"),
            PortfolioProject("5", "Kazanchis Tech Campus", "Schools & Univ", "Modern Minimalist", "Addis Ababa", "https://images.unsplash.com/photo-1505691938895-1758d7feb511?w=600", "Ergonomic study pods and library reception desks.", "ETB 890,000", "4.9 Stars")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Bekansi Interior Design Portfolio", color = WarmMahogany, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Completed turn-key residential, commercial & hotel transformations", color = TextMuted, fontSize = 10.sp)
                    }
                    Button(
                        onClick = { showConsultationDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Book", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Book Design Consultation", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Categories
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = cat == selectedCategory,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 10.sp, color = if (cat == selectedCategory) Color.White else TextDark) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarmMahogany,
                                containerColor = LightSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Projects Grid/List
        val filtered = portfolioItems.filter {
            (selectedCategory == "All Projects" || it.category == selectedCategory)
        }

        filtered.forEach { proj ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = proj.imageUrl,
                            contentDescription = proj.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            color = WarmMahogany,
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(proj.category, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(proj.title, color = WarmMahogany, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("📍 ${proj.location} • Value: ${proj.value} • Rating: ${proj.rating}", color = TextMuted, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(proj.description, color = TextDark, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                viewModel.sendCustomerMessage("Interested in interior design project similar to ${proj.title}")
                                Toast.makeText(context, "Inquiry sent to AI Consultant Chat!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LightSurfaceVariant, contentColor = WarmMahogany),
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Request Similar Project", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showConsultationDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Book Consultation", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showConsultationDialog) {
        Dialog(onDismissRequest = { showConsultationDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp).border(1.dp, CardBorderGray, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Book Interior Design Consultation", color = WarmMahogany, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Schedule a site visit or 3D interior design planning session.", color = TextMuted, fontSize = 10.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Full Name", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 11.sp, color = TextDark)
                    )
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Phone Number", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 11.sp, color = TextDark)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showConsultationDialog = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (clientName.isNotBlank() && clientPhone.isNotBlank()) {
                                    viewModel.addLead(
                                        Lead(
                                            name = clientName,
                                            phone = clientPhone,
                                            email = "",
                                            status = "New",
                                            source = "Showroom Gallery",
                                            requirements = "Interior Design Consultation",
                                            notes = "Booked via Bekansi Interior Design Portfolio",
                                            language = "English"
                                        )
                                    )
                                    Toast.makeText(context, "Consultation booked & Hot Lead added to CRM!", Toast.LENGTH_SHORT).show()
                                    showConsultationDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany)
                        ) {
                            Text("Confirm Booking", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class PortfolioProject(
    val id: String,
    val title: String,
    val category: String,
    val style: String,
    val location: String,
    val imageUrl: String,
    val description: String,
    val value: String,
    val rating: String
)

// ==========================================
// 3. MEDIA UPLOAD & DAM CENTER MODULE
// ==========================================
@Composable
fun MediaUploadDamModule(context: android.content.Context, viewModel: SalesViewModel) {
    var mediaTitle by remember { mutableStateOf("") }
    var mediaCategory by remember { mutableStateOf("Living Room") }
    var mediaType by remember { mutableStateOf("Photo") } // Photo, Video, 360 Panorama, PDF Brochure, Testimonial
    var mediaPrice by remember { mutableStateOf("") }
    var mediaMaterial by remember { mutableStateOf("Pure Wanza / Teak") }
    var mediaStatus by remember { mutableStateOf("Approved") } // Approved, Draft, Private
    var mediaTags by remember { mutableStateOf("luxury, modern, ethiopian") }

    val uploadedMediaList = remember {
        mutableStateListOf(
            DamMediaItem("DAM-001", "Executive Mahogany Desk 360°", "Office Furniture", "360 Panorama", "Approved", "ETB 145,000", "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=400", "14.2 MB"),
            DamMediaItem("DAM-002", "Bespoke Sofa Walkthrough Video", "Living Room", "Video", "Approved", "ETB 85,000", "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=400", "45.8 MB"),
            DamMediaItem("DAM-003", "2026 Master Furniture Catalog PDF", "Product Brochure", "PDF Brochure", "Approved", "N/A", "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=400", "8.4 MB"),
            DamMediaItem("DAM-004", "Customer Villa Installation Clip", "Customer Installations", "Testimonial Video", "Draft", "ETB 320,000", "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?w=400", "28.1 MB")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // Storage Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Firebase DAM Storage Health", color = WarmMahogany, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("14.8 GB / 100 GB Used", color = TextMuted, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = 0.148f,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = WarmMahogany,
                    trackColor = LightSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("4,820 Approved High-Res Photos & 320 Videos CDN Ready", color = TextMuted, fontSize = 9.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Upload Form
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Upload & Categorize Media Asset", color = WarmMahogany, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Attach high-resolution photos, 360° panoramas, PDF brochures or installation videos", color = TextMuted, fontSize = 10.sp)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = mediaTitle,
                    onValueChange = { mediaTitle = it },
                    label = { Text("Product / Asset Name", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 11.sp, color = TextDark)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = mediaCategory,
                        onValueChange = { mediaCategory = it },
                        label = { Text("Category (e.g. Bed, Sofa)", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 10.sp, color = TextDark)
                    )
                    OutlinedTextField(
                        value = mediaType,
                        onValueChange = { mediaType = it },
                        label = { Text("Media Format (Photo/Video/360)", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 10.sp, color = TextDark)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = mediaPrice,
                        onValueChange = { mediaPrice = it },
                        label = { Text("Price (ETB)", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 10.sp, color = TextDark)
                    )
                    OutlinedTextField(
                        value = mediaMaterial,
                        onValueChange = { mediaMaterial = it },
                        label = { Text("Material / Finish", fontSize = 9.sp) },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(fontSize = 10.sp, color = TextDark)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (mediaTitle.isNotBlank()) {
                            val assetId = "DAM-${System.currentTimeMillis()}"
                            val workId = MediaUploadService.enqueueUpload(
                                context = context,
                                fileUri = "content://media/external/images/$assetId",
                                mediaType = mediaType,
                                title = mediaTitle,
                                category = mediaCategory,
                                assetId = assetId
                            )

                            uploadedMediaList.add(
                                DamMediaItem(
                                    id = assetId,
                                    title = mediaTitle,
                                    category = mediaCategory,
                                    format = mediaType,
                                    status = mediaStatus,
                                    price = if (mediaPrice.isNotBlank()) "ETB $mediaPrice" else "N/A",
                                    url = "https://firebasestorage.googleapis.com/v0/b/bekansi-furniture.appspot.com/o/media%2F${mediaCategory.lowercase()}%2F${mediaTitle.replace(" ", "_").lowercase()}.jpg?alt=media",
                                    size = "14.2 MB"
                                )
                            )
                            Toast.makeText(context, "WorkManager Task Enqueued (ID: ${workId.toString().take(8)}...)! Background upload to Firebase Storage initialized with exponential retries.", Toast.LENGTH_LONG).show()
                            mediaTitle = ""
                            mediaPrice = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Upload", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enqueue WorkManager Background Upload", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Managed Digital Assets (${uploadedMediaList.size})", color = WarmMahogany, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        uploadedMediaList.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .border(1.dp, CardBorderGray, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = item.url,
                        contentDescription = item.title,
                        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.title, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (item.status == "Approved") AccentSuccess else AccentWarning,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(item.status, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text("${item.category} • ${item.format} • ${item.size} • Price: ${item.price}", color = TextMuted, fontSize = 9.5.sp)
                    }

                    Row {
                        IconButton(onClick = {
                            val newStat = if (item.status == "Approved") "Draft" else "Approved"
                            val idx = uploadedMediaList.indexOf(item)
                            if (idx >= 0) {
                                uploadedMediaList[idx] = item.copy(status = newStat)
                                Toast.makeText(context, "Status set to $newStat", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Toggle", tint = WarmMahogany, modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = {
                            uploadedMediaList.remove(item)
                            Toast.makeText(context, "Asset deleted", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

data class DamMediaItem(
    val id: String,
    val title: String,
    val category: String,
    val format: String,
    val status: String,
    val price: String,
    val url: String,
    val size: String
)

// ==========================================
// 4. AI MEDIA & SMM INTEGRATION MODULE
// ==========================================
@Composable
fun AiMediaSmmIntegrationModule(context: android.content.Context, viewModel: SalesViewModel, albums: List<ProductAlbum>) {
    var selectedTargetPlatform by remember { mutableStateOf("Instagram Carousel") }
    var campaignTheme by remember { mutableStateOf("Modern Bedroom Upgrade Deal") }
    var generatedCaption by remember { mutableStateOf("") }
                var selectedMediaAssetUrl by remember { mutableStateOf(albums.firstOrNull()?.imageUrls?.split(",")?.firstOrNull() ?: "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=400") }

    val platforms = listOf("Instagram Carousel", "TikTok Reel Script", "Facebook Album Post", "Telegram Broadcast", "WhatsApp Status Story")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("AI Auto-Media Selection & SMM Publisher", color = WarmMahogany, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("AI Assistant automatically selects approved DAM assets & writes platform-optimized scripts", color = TextMuted, fontSize = 10.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Text("Target Social Format:", color = TextDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(platforms) { plt ->
                        FilterChip(
                            selected = plt == selectedTargetPlatform,
                            onClick = { selectedTargetPlatform = plt },
                            label = { Text(plt, fontSize = 9.5.sp, color = if (plt == selectedTargetPlatform) Color.White else TextDark) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarmMahogany,
                                containerColor = LightSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = campaignTheme,
                    onValueChange = { campaignTheme = it },
                    label = { Text("Campaign Objective / Theme", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 11.sp, color = TextDark)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val chosenAlbum = albums.randomOrNull()
                        selectedMediaAssetUrl = chosenAlbum?.imageUrls?.split(",")?.firstOrNull() ?: "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?w=400"
                        generatedCaption = """
                            ✨ BEKANSI FURNITURE SPECIAL: ${campaignTheme.uppercase()} ✨
                            
                            Transform your living space with Ethiopian solid teak craftsmanship! 
                            Featured Design: ${chosenAlbum?.name ?: "Bespoke Royal Bed Set"}
                            
                            📍 Showroom: Bole Road & Kazanchis, Addis Ababa
                            📞 Order / Inquiry: +251 911 000 000 / +251 922 111 222
                            
                            #BekansiFurniture #AddisAbabaFurniture #EthiopianInterior #HomeDesign
                        """.trimIndent()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmMahogany),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Pick", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-Select Approved Asset & Generate Post", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (generatedCaption.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LightSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Selected Approved DAM Asset & Preview:", color = WarmMahogany, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (selectedMediaAssetUrl.isNotBlank()) {
                        AsyncImage(
                            model = selectedMediaAssetUrl,
                            contentDescription = "Asset",
                            modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(generatedCaption, color = TextDark, fontSize = 10.5.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "Pushed Asset & Caption to SMM Auto-Posting Queue!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Push", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Queue to SMM Planner Schedule", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. DAM ANALYTICS MODULE
// ==========================================
@Composable
fun DamAnalyticsModule(context: android.content.Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Furniture Gallery & DAM Performance Analytics", color = WarmMahogany, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Real-time view tracking, customer saves, downloads and lead conversion attribution", color = TextMuted, fontSize = 10.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AnalyticsCardItem("1,420", "Total Assets", WarmMahogany, Modifier.weight(1f))
                    AnalyticsCardItem("8,430", "Customer Views", AccentSuccess, Modifier.weight(1f))
                    AnalyticsCardItem("18.4%", "Lead Conversion", AccentWarning, Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, CardBorderGray, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Top Performing Assets Generating Leads", color = WarmMahogany, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                TopAssetRow("1. Royal Teak King Bed Set", "1,240 Views", "182 Quotes Generated", "ETB 4.2M Sales")
                HorizontalDivider(color = CardBorderGray, modifier = Modifier.padding(vertical = 6.dp))
                TopAssetRow("2. Modular Italian Leather Sofa", "980 Views", "142 Quotes Generated", "ETB 3.1M Sales")
                HorizontalDivider(color = CardBorderGray, modifier = Modifier.padding(vertical = 6.dp))
                TopAssetRow("3. Modern Walk-in Wardrobe 360°", "850 Views", "110 Quotes Generated", "ETB 2.8M Sales")
            }
        }
    }
}

@Composable
fun AnalyticsCardItem(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = LightSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(1.dp, CardBorderGray, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 9.sp, color = TextMuted)
        }
    }
}

@Composable
fun TopAssetRow(name: String, views: String, quotes: String, sales: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("$views • $quotes", color = TextMuted, fontSize = 9.sp)
        }
        Text(sales, color = WarmMahogany, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

