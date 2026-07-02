package com.karigar.app.data

data class Category(
    val value: String,
    val label: String,
    val skill: String,
    val emoji: String
)

object Categories {
    const val SKILLED = "skilled"
    const val UNSKILLED = "unskilled"

    val all: List<Category> = listOf(
        Category("maid", "Maid / Cleaning", UNSKILLED, "🧹"),
        Category("cook", "Cook", SKILLED, "🍳"),
        Category("cook_catering", "Catering Cook", SKILLED, "🍲"),
        Category("babysitter", "Babysitter", UNSKILLED, "🍼"),
        Category("elderly_care", "Elderly Care", SKILLED, "🧑‍⚕️"),
        Category("electrician", "Electrician", SKILLED, "💡"),
        Category("plumber", "Plumber", SKILLED, "🚰"),
        Category("carpenter", "Carpenter", SKILLED, "🪚"),
        Category("painter", "Painter", SKILLED, "🎨"),
        Category("mason", "Mason", SKILLED, "🧱"),
        Category("construction_worker", "Construction Worker", UNSKILLED, "👷"),
        Category("farm_labour", "Farm Labour", UNSKILLED, "🌾"),
        Category("welder", "Welder", SKILLED, "🔥"),
        Category("fabricator", "Fabricator", SKILLED, "⚙️"),
        Category("tile_marble", "Tile / Marble", SKILLED, "◻️"),
        Category("pop_ceiling", "POP / Ceiling", SKILLED, "🏗️"),
        Category("ac_technician", "AC Technician", SKILLED, "❄️"),
        Category("appliance_repair", "Appliance Repair", SKILLED, "🔧"),
        Category("ro_technician", "RO Technician", SKILLED, "💧"),
        Category("water_tanker", "Water Tanker", UNSKILLED, "🚛"),
        Category("borewell", "Borewell", SKILLED, "🕳️"),
        Category("solar_technician", "Solar Technician", SKILLED, "☀️"),
        Category("mobile_repair", "Mobile Repair", SKILLED, "📱"),
        Category("cctv", "CCTV Install", SKILLED, "📹"),
        Category("mechanic", "Mechanic", SKILLED, "🔩"),
        Category("driver", "Driver", SKILLED, "🚗"),
        Category("delivery", "Delivery", UNSKILLED, "📦"),
        Category("packers_movers", "Packers & Movers", UNSKILLED, "🚚"),
        Category("gardener", "Gardener", UNSKILLED, "🌱"),
        Category("pest_control", "Pest Control", SKILLED, "🐜"),
        Category("security_guard", "Security Guard", UNSKILLED, "🛡️"),
        Category("barber", "Barber", SKILLED, "💈"),
        Category("beautician", "Beautician", SKILLED, "💄"),
        Category("tailor", "Tailor", SKILLED, "🧵"),
        Category("laundry", "Laundry", UNSKILLED, "🧺"),
        Category("cleaner_commercial", "Office Cleaner", UNSKILLED, "🧽"),
        Category("photographer", "Photographer", SKILLED, "📷"),
        Category("event_helper", "Event Helper", UNSKILLED, "🎪"),
        Category("tutor", "Tutor", SKILLED, "📚"),
        Category("priest", "Priest / Pandit", SKILLED, "🕉️"),
        Category("other", "Other", UNSKILLED, "🧰")
    )

    fun byValue(value: String): Category? = all.firstOrNull { it.value == value }
}
