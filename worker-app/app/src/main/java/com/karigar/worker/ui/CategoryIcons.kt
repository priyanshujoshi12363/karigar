package com.karigar.worker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Carpenter
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MiscellaneousServices
import androidx.compose.material.icons.filled.PestControl
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Roofing
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

fun categoryIcon(value: String): ImageVector = when (value) {
    "maid" -> Icons.Filled.CleaningServices
    "cook" -> Icons.Filled.Restaurant
    "cook_catering" -> Icons.Filled.LocalDining
    "babysitter" -> Icons.Filled.ChildCare
    "elderly_care" -> Icons.Filled.Elderly
    "electrician" -> Icons.Filled.ElectricalServices
    "plumber" -> Icons.Filled.Plumbing
    "carpenter" -> Icons.Filled.Carpenter
    "painter" -> Icons.Filled.FormatPaint
    "mason" -> Icons.Filled.Construction
    "construction_worker" -> Icons.Filled.Engineering
    "farm_labour" -> Icons.Filled.Agriculture
    "welder" -> Icons.Filled.LocalFireDepartment
    "fabricator" -> Icons.Filled.Handyman
    "tile_marble" -> Icons.Filled.GridView
    "pop_ceiling" -> Icons.Filled.Roofing
    "ac_technician" -> Icons.Filled.AcUnit
    "appliance_repair" -> Icons.Filled.HomeRepairService
    "ro_technician" -> Icons.Filled.WaterDrop
    "water_tanker" -> Icons.Filled.LocalShipping
    "borewell" -> Icons.Filled.Water
    "solar_technician" -> Icons.Filled.SolarPower
    "mobile_repair" -> Icons.Filled.PhoneAndroid
    "cctv" -> Icons.Filled.Videocam
    "mechanic" -> Icons.Filled.Build
    "driver" -> Icons.Filled.DirectionsCar
    "delivery" -> Icons.Filled.DeliveryDining
    "packers_movers" -> Icons.Filled.Inventory2
    "gardener" -> Icons.Filled.Grass
    "pest_control" -> Icons.Filled.PestControl
    "security_guard" -> Icons.Filled.Security
    "barber" -> Icons.Filled.ContentCut
    "beautician" -> Icons.Filled.Spa
    "tailor" -> Icons.Filled.Checkroom
    "laundry" -> Icons.Filled.LocalLaundryService
    "cleaner_commercial" -> Icons.Filled.CleaningServices
    "photographer" -> Icons.Filled.CameraAlt
    "event_helper" -> Icons.Filled.Celebration
    "tutor" -> Icons.Filled.School
    "priest" -> Icons.Filled.SelfImprovement
    else -> Icons.Filled.MiscellaneousServices
}
