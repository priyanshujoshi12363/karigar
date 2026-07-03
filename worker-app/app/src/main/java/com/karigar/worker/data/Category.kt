package com.karigar.worker.data

data class Category(val value: String, val label: String)

object Categories {
    private val labels = mapOf(
        "maid" to "Maid / Cleaning", "cook" to "Cook", "cook_catering" to "Catering Cook",
        "babysitter" to "Babysitter", "elderly_care" to "Elderly Care", "electrician" to "Electrician",
        "plumber" to "Plumber", "carpenter" to "Carpenter", "painter" to "Painter", "mason" to "Mason",
        "construction_worker" to "Construction Worker", "farm_labour" to "Farm Labour", "welder" to "Welder",
        "fabricator" to "Fabricator", "tile_marble" to "Tile / Marble", "pop_ceiling" to "POP / Ceiling",
        "ac_technician" to "AC Technician", "appliance_repair" to "Appliance Repair", "ro_technician" to "RO Technician",
        "water_tanker" to "Water Tanker", "borewell" to "Borewell", "solar_technician" to "Solar Technician",
        "mobile_repair" to "Mobile Repair", "cctv" to "CCTV Install", "mechanic" to "Mechanic",
        "driver" to "Driver", "delivery" to "Delivery", "packers_movers" to "Packers & Movers",
        "gardener" to "Gardener", "pest_control" to "Pest Control", "security_guard" to "Security Guard",
        "barber" to "Barber", "beautician" to "Beautician", "tailor" to "Tailor", "laundry" to "Laundry",
        "cleaner_commercial" to "Office Cleaner", "photographer" to "Photographer", "event_helper" to "Event Helper",
        "tutor" to "Tutor", "priest" to "Priest / Pandit", "other" to "Other"
    )

    fun label(value: String?): String = labels[value] ?: (value ?: "Service")
}
