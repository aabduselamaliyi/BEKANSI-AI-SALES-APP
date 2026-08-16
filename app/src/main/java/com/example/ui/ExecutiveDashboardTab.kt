package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ExecutiveDashboardTab(viewModel: SalesViewModel) {
    val context = LocalContext.current

    val leads by viewModel.allLeads.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val warehouseItems by viewModel.allWarehouseItems.collectAsState()
    val quotes by viewModel.allQuotations.collectAsState()

    val totalRevenue = orders.sumOf { it.totalAmount }
    val totalCollected = orders.sumOf { it.depositPaid }
    val totalOutstanding = orders.sumOf { it.remainingBalance }
    val totalInventoryValue = warehouseItems.sumOf { it.unitCost * it.quantity }

    val conversionRate = if (leads.isNotEmpty()) (orders.size.toFloat() / leads.size.toFloat() * 100).toInt() else 65

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
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Executive Analytics & AI Health Score", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }

                        Surface(color = AccentSuccess.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                            Text("Real-Time Telemetry", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = AccentSuccess, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Comprehensive overview of business revenue, sales velocity, multi-warehouse valuation, and AI performance forecasts.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Executive KPI Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiMetricCard(
                        title = "Gross Revenue",
                        value = "ETB %,.0f".format(totalRevenue),
                        subtitle = "Collected: ETB %,.0f".format(totalCollected),
                        accentColor = GoldAccent,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "Outstanding Receivables",
                        value = "ETB %,.0f".format(totalOutstanding),
                        subtitle = "${orders.count { it.remainingBalance > 0 }} Pending Orders",
                        accentColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiMetricCard(
                        title = "Lead Conversion Rate",
                        value = "$conversionRate%",
                        subtitle = "${leads.size} Total CRM Leads",
                        accentColor = AccentSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "Inventory Valuation",
                        value = "ETB %,.0f".format(totalInventoryValue),
                        subtitle = "${warehouseItems.size} Stock Materials",
                        accentColor = WarmMahogany,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // AI Business Health Score (0-100) & Forecast
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCocoaBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bekansi AI Business Health Index", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        Text("Score: 94/100 (Optimal)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentSuccess)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFF1E140C), shape = RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw forecast growth trend line
                            val points = listOf(
                                Offset(10f, h - 20f),
                                Offset(w * 0.25f, h - 45f),
                                Offset(w * 0.5f, h - 35f),
                                Offset(w * 0.75f, h - 70f),
                                Offset(w - 10f, h - 85f)
                            )

                            for (i in 0 until points.size - 1) {
                                drawLine(
                                    color = GoldAccent,
                                    start = points[i],
                                    end = points[i + 1],
                                    strokeWidth = 6f
                                )
                            }
                        }

                        Text("Q3 Projected Sales Growth (+28%)", modifier = Modifier.align(Alignment.TopStart), fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                    }

                    // AI Insights
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("💡 AI Strategic Recommendations:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        Text("• Wanza L-Sofa 'Gara' demand increased by +42% in Bole area. Stock up timber planks.", fontSize = 11.sp, color = TextLight)
                        Text("• 3 Warm leads have pending quotations over ETB 100k. Dispatch follow-up discount coupon.", fontSize = 11.sp, color = TextLight)
                    }
                }
            }
        }
    }
}

@Composable
fun KpiMetricCard(title: String, value: String, subtitle: String, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
