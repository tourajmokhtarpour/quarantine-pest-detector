package com.example.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.DetectionResult
import com.example.data.HistoryEntry
import com.example.data.Pest
import com.example.data.PestDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PestApp(viewModel: PestViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    // Force RTL layout orientation for standard Persian presentation
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "سامانه قرنطینه نباتی",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "پردازش آفلاین با TFLite",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        IconButton(onClick = {
                            // Show a quick dialog/toast about quarantine pests of Iran
                            val text = "این سامانه مجهز به هوش مصنوعی آفلاین جهت تشخیص ۳۹ گونه آفت قرنطینه‌ای ورودی به کشور ایران می‌باشد."
                            android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_LONG).show()
                        }) {
                            Icon(Icons.Default.Info, contentDescription = "درباره سامانه", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                ) {
                    val navBarItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(if (selectedTab == 0) Icons.Default.CameraEnhance else Icons.Outlined.CameraEnhance, contentDescription = null) },
                        label = { Text("تشخیص آفت", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = navBarItemColors,
                        modifier = Modifier.testTag("nav_tab_detector")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(if (selectedTab == 1) Icons.AutoMirrored.Filled.LibraryBooks else Icons.AutoMirrored.Outlined.LibraryBooks, contentDescription = null) },
                        label = { Text("کتابخانه آفات", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = navBarItemColors,
                        modifier = Modifier.testTag("nav_tab_library")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(if (selectedTab == 2) Icons.Default.History else Icons.Outlined.History, contentDescription = null) },
                        label = { Text("تاریخچه", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = navBarItemColors,
                        modifier = Modifier.testTag("nav_tab_history")
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> DetectorTab(viewModel = viewModel)
                    1 -> LibraryTab()
                    2 -> HistoryTab(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun DetectorTab(viewModel: PestViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedBitmap by viewModel.selectedBitmap.collectAsStateWithLifecycle()
    val isModelLoaded = viewModel.isModelLoaded

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectImageUri(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.selectBitmap(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Model warning or activation status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isModelLoaded) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isModelLoaded) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isModelLoaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isModelLoaded) "هوش مصنوعی آفلاین فعال است" else "حالت شبیه‌سازی فعال است",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isModelLoaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = if (isModelLoaded) "تشخیص‌های آفلاین با استفاده از مدل محلی اجرا می‌شوند." 
                               else "فایل best_float32.tflite در پس‌زمینه یافت نشد. می‌توانید با فرضیات هوشمند برنامه را کاملا تست کنید.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Image Preview Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedBitmap != null) {
                Image(
                    bitmap = selectedBitmap!!.asImageBitmap(),
                    contentDescription = "تصویر انتخابی",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Landscape,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "عکسی برای پردازش وجود ندارد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "تصویری انتخاب کنید یا عکس بگیرید",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Selection Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("camera_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ثبت با دوربین", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("gallery_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("بارگذاری گالری", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Core Classification Trigger Button
        Button(
            onClick = { viewModel.runClassification() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("analyze_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Psychology, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("شروع پردازش هوشمند آفت", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // UI Inference State Display
        crossfadeState(uiState)
    }
}

@Composable
fun ColumnScope.crossfadeState(uiState: UiState) {
    when (uiState) {
        is UiState.Idle -> {
            // Wait for action
        }
        is UiState.Loading -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                CircularProgressIndicator(strokeWidth = 3.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "در حال تحلیل تصویر با مدل محلی...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        is UiState.Success -> {
            val results = uiState.results
            if (results.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HighlightOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "آفت قرنطینه‌ای شناسایی نشد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "هیچ آفتی با دقت بالای ۷۵٪ شناسایی نگردید. لطفاً مجدداً با نوردهی بهتر و کادربندی بسته‌تر امتحان کنید.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "نتایج تشخیص (حداکثر ۳ تشخیص برتر بالای ۷۵٪):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                results.forEachIndexed { idx, result ->
                    result.pest?.let { pest ->
                        PestResultCard(pest = pest, confidence = result.confidence, index = idx + 1)
                    }
                }
            }
        }
        is UiState.Error -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.message,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun PestResultCard(pest: Pest, confidence: Float, index: Int) {
    var expanded by remember { mutableStateOf(index == 1) } // Expand the top result by default

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { expanded = !expanded }
            .testTag("result_card_$index")
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Rank circle badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (index == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pest.nameFa,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = pest.scientificName,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Confidence badge
                val progressAngle by animateFloatAsState(targetValue = confidence)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(54.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progressAngle },
                        modifier = Modifier.fillMaxSize(),
                        color = if (confidence >= 0.85f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "${(confidence * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Expanded detail section
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                    Text(
                        text = "توضیحات و مشخصات آفت:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = pest.description,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp, top = 2.dp)
                    )

                    Text(
                        text = "نشانه‌ها و علائم خسارت:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = pest.symptoms,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp, top = 2.dp)
                    )

                    // Treatment section with background highlighting
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Healing, contentDescription = null, size = 16.dp, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "روش‌های مدیریت و تیمار درمانی:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pest.treatment,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Icon(imageVector: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        tint = tint
    )
}

@Composable
fun LibraryTab() {
    var searchQuery by remember { mutableStateOf("") }
    val pestsList = remember { PestDatabase.pests }

    val filteredPests = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            pestsList
        } else {
            pestsList.filter {
                it.nameFa.contains(searchQuery, ignoreCase = true) ||
                        it.nameEn.contains(searchQuery, ignoreCase = true) ||
                        it.scientificName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("library_search_input"),
            placeholder = { Text("جستجو در بین ۳۹ گونه آفت قرنطینه...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredPests) { pest ->
                var isExpanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pest.nameFa,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${pest.nameEn} • ${pest.scientificName}",
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))

                                Text(
                                    text = "توضیحات کلی:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = pest.description,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = "علائم خسارت در مزرعه/باغ:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = pest.symptoms,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Card(
                                    shape = RoundedCornerShape(6.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "تیمار بهداشتی و شیمیایی:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        Text(
                                            text = pest.treatment,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
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

@Composable
fun HistoryTab(viewModel: PestViewModel) {
    val historyList by viewModel.historyList.collectAsStateWithLifecycle()
    var selectedHistoryPest by remember { mutableStateOf<HistoryEntry?>(null) }
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تاریخچه ردیابی‌ها (${historyList.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (historyList.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearAllHistory() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف همه تاریخچه", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "هیچ تاریخچه‌ای وجود ندارد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "پس از اسکن و شناسایی آفت، سابقه ردیابی آن در این بخش ثبت می‌گردد.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedHistoryPest = entry }
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail from internal cache
                            val file = File(entry.localImageUri)
                            if (file.exists()) {
                                Image(
                                    bitmap = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap(),
                                    contentDescription = entry.pestName,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.pestName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "دقت تشخیص: ${(entry.confidence * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = if (entry.confidence >= 0.85f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = dateFormat.format(Date(entry.timestamp)),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteHistoryItem(entry.id, entry.localImageUri) },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف رکورد")
                            }
                        }
                    }
                }
            }
        }

        // Show pest details when a history entry is clicked
        selectedHistoryPest?.let { entry ->
            val pest = PestDatabase.getPestByIndex(entry.pestIndex)
            if (pest != null) {
                AlertDialog(
                    onDismissRequest = { selectedHistoryPest = null },
                    confirmButton = {
                        TextButton(onClick = { selectedHistoryPest = null }) {
                            Text("متوجه شدم", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "سابقه ردیابی آفت",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = pest.nameFa,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = pest.scientificName,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val file = File(entry.localImageUri)
                            if (file.exists()) {
                                Image(
                                    bitmap = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap(),
                                    contentDescription = pest.nameFa,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Text(
                                text = "دقت تشخیص: ${(entry.confidence * 100).toInt()}% در تاریخ ${dateFormat.format(Date(entry.timestamp))}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                text = "توضیحات و مشخصات آفت:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = pest.description,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )

                            Text(
                                text = "نشانه‌ها و علائم خسارت:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = pest.symptoms,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )

                            Card(
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "تیمار بهداشتی و شیمیایی:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = pest.treatment,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}
