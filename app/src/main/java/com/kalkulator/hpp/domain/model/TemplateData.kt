package com.kalkulator.hpp.domain.model

/**
 * Template resep bawaan yang bisa digunakan user sebagai starting point.
 */
data class RecipeTemplate(
    val name: String,
    val emoji: String,
    val category: String,
    val yield: Int = 1,
    val laborCost: Double = 0.0,
    val ingredients: List<Triple<String, Double, String>>, // (name, qty, unit)
    val description: String = ""
)

object TemplateData {

    val categories = listOf("Makanan", "Minuman", "Snack", "Kue")

    val templates = listOf(
        // === MAKANAN ===
        RecipeTemplate(
            name = "Nasi Goreng Spesial", emoji = "🍛", category = "Makanan", yield = 1, laborCost = 3500.0,
            description = "Nasi goreng dengan telur, ayam, dan bumbu spesial",
            ingredients = listOf(
                Triple("Beras", 200.0, "gram"), Triple("Telur", 1.0, "pcs"),
                Triple("Ayam", 50.0, "gram"), Triple("Bumbu Halus", 30.0, "gram"),
                Triple("Minyak Goreng", 15.0, "ml"), Triple("Kecap Manis", 10.0, "ml"),
                Triple("Bawang Merah", 20.0, "gram"), Triple("Bawang Putih", 10.0, "gram")
            )
        ),
        RecipeTemplate(
            name = "Mie Goreng", emoji = "🍜", category = "Makanan", yield = 1, laborCost = 3000.0,
            description = "Mie goreng dengan sayuran dan telur",
            ingredients = listOf(
                Triple("Mie Telur", 200.0, "gram"), Triple("Telur", 1.0, "pcs"),
                Triple("Sawi", 50.0, "gram"), Triple("Kol", 30.0, "gram"),
                Triple("Bawang Merah", 15.0, "gram"), Triple("Bawang Putih", 10.0, "gram"),
                Triple("Kecap Manis", 15.0, "ml"), Triple("Minyak Goreng", 20.0, "ml")
            )
        ),
        RecipeTemplate(
            name = "Ayam Geprek", emoji = "🍗", category = "Makanan", yield = 1, laborCost = 4000.0,
            description = "Ayam goreng tepung dengan sambal geprek",
            ingredients = listOf(
                Triple("Dada Ayam", 150.0, "gram"), Triple("Tepung Terigu", 50.0, "gram"),
                Triple("Tepung Maizena", 20.0, "gram"), Triple("Cabai Rawit", 30.0, "gram"),
                Triple("Bawang Putih", 15.0, "gram"), Triple("Minyak Goreng", 200.0, "ml"),
                Triple("Nasi", 200.0, "gram")
            )
        ),
        RecipeTemplate(
            name = "Soto Ayam", emoji = "🍲", category = "Makanan", yield = 1, laborCost = 4000.0,
            description = "Soto ayam kuah kuning dengan pelengkap",
            ingredients = listOf(
                Triple("Ayam", 100.0, "gram"), Triple("Beras", 200.0, "gram"),
                Triple("Kunyit", 10.0, "gram"), Triple("Serai", 5.0, "gram"),
                Triple("Daun Jeruk", 2.0, "pcs"), Triple("Bawang Merah", 20.0, "gram"),
                Triple("Bawang Putih", 10.0, "gram"), Triple("Tauge", 30.0, "gram")
            )
        ),
        RecipeTemplate(
            name = "Rendang", emoji = "🥘", category = "Makanan", yield = 5, laborCost = 5000.0,
            description = "Rendang daging sapi khas Padang",
            ingredients = listOf(
                Triple("Daging Sapi", 500.0, "gram"), Triple("Santan", 400.0, "ml"),
                Triple("Cabai Merah", 100.0, "gram"), Triple("Bawang Merah", 80.0, "gram"),
                Triple("Bawang Putih", 30.0, "gram"), Triple("Lengkuas", 20.0, "gram"),
                Triple("Serai", 10.0, "gram"), Triple("Daun Jeruk", 3.0, "pcs")
            )
        ),
        RecipeTemplate(
            name = "Bakso", emoji = "🍡", category = "Makanan", yield = 10, laborCost = 3000.0,
            description = "Bakso daging sapi dengan kuah kaldu",
            ingredients = listOf(
                Triple("Daging Sapi", 500.0, "gram"), Triple("Tepung Tapioka", 200.0, "gram"),
                Triple("Bawang Putih", 30.0, "gram"), Triple("Garam", 10.0, "gram"),
                Triple("Merica", 5.0, "gram"), Triple("Es Batu", 100.0, "gram"),
                Triple("Mie Kuning", 500.0, "gram")
            )
        ),

        // === MINUMAN ===
        RecipeTemplate(
            name = "Es Kopi Susu", emoji = "☕", category = "Minuman", yield = 1, laborCost = 2000.0,
            description = "Es kopi susu gula aren kekinian",
            ingredients = listOf(
                Triple("Kopi Bubuk", 20.0, "gram"), Triple("Susu UHT", 150.0, "ml"),
                Triple("Gula Aren", 30.0, "gram"), Triple("Es Batu", 100.0, "gram"),
                Triple("Cup Plastik", 1.0, "pcs")
            )
        ),
        RecipeTemplate(
            name = "Thai Tea", emoji = "🧋", category = "Minuman", yield = 1, laborCost = 2000.0,
            description = "Thai tea creamy dengan susu",
            ingredients = listOf(
                Triple("Teh Thai", 15.0, "gram"), Triple("Susu Kental Manis", 30.0, "ml"),
                Triple("Susu UHT", 100.0, "ml"), Triple("Gula Pasir", 20.0, "gram"),
                Triple("Es Batu", 100.0, "gram"), Triple("Cup Plastik", 1.0, "pcs")
            )
        ),
        RecipeTemplate(
            name = "Jus Alpukat", emoji = "🥑", category = "Minuman", yield = 1, laborCost = 2000.0,
            description = "Jus alpukat kental dengan susu coklat",
            ingredients = listOf(
                Triple("Alpukat", 150.0, "gram"), Triple("Susu Kental Manis", 30.0, "ml"),
                Triple("Coklat Bubuk", 10.0, "gram"), Triple("Gula Pasir", 15.0, "gram"),
                Triple("Es Batu", 100.0, "gram"), Triple("Cup Plastik", 1.0, "pcs")
            )
        ),
        RecipeTemplate(
            name = "Es Teh Manis", emoji = "🍵", category = "Minuman", yield = 1, laborCost = 1000.0,
            description = "Es teh manis segar",
            ingredients = listOf(
                Triple("Teh Celup", 1.0, "pcs"), Triple("Gula Pasir", 25.0, "gram"),
                Triple("Es Batu", 100.0, "gram"), Triple("Cup Plastik", 1.0, "pcs")
            )
        ),
        RecipeTemplate(
            name = "Lemon Tea", emoji = "🍋", category = "Minuman", yield = 1, laborCost = 1500.0,
            description = "Lemon tea segar dengan madu",
            ingredients = listOf(
                Triple("Teh Celup", 1.0, "pcs"), Triple("Lemon", 0.5, "pcs"),
                Triple("Madu", 20.0, "ml"), Triple("Es Batu", 100.0, "gram"),
                Triple("Cup Plastik", 1.0, "pcs")
            )
        ),
        RecipeTemplate(
            name = "Matcha Latte", emoji = "🍵", category = "Minuman", yield = 1, laborCost = 2500.0,
            description = "Matcha latte premium",
            ingredients = listOf(
                Triple("Bubuk Matcha", 5.0, "gram"), Triple("Susu UHT", 200.0, "ml"),
                Triple("Gula Pasir", 15.0, "gram"), Triple("Es Batu", 100.0, "gram"),
                Triple("Cup Plastik", 1.0, "pcs")
            )
        ),

        // === SNACK ===
        RecipeTemplate(
            name = "Pisang Goreng", emoji = "🍌", category = "Snack", yield = 5, laborCost = 2000.0,
            description = "Pisang goreng crispy",
            ingredients = listOf(
                Triple("Pisang", 5.0, "pcs"), Triple("Tepung Terigu", 100.0, "gram"),
                Triple("Gula Pasir", 30.0, "gram"), Triple("Minyak Goreng", 200.0, "ml"),
                Triple("Garam", 3.0, "gram")
            )
        ),
        RecipeTemplate(
            name = "Risol Mayo", emoji = "🥟", category = "Snack", yield = 10, laborCost = 3000.0,
            description = "Risol isi smoked beef dan mayo",
            ingredients = listOf(
                Triple("Tepung Terigu", 150.0, "gram"), Triple("Telur", 2.0, "pcs"),
                Triple("Smoked Beef", 100.0, "gram"), Triple("Mayonaise", 50.0, "gram"),
                Triple("Minyak Goreng", 200.0, "ml"), Triple("Keju", 50.0, "gram"),
                Triple("Tepung Panir", 100.0, "gram")
            )
        ),
        RecipeTemplate(
            name = "Cireng", emoji = "🟡", category = "Snack", yield = 10, laborCost = 2000.0,
            description = "Cireng aci goreng renyah",
            ingredients = listOf(
                Triple("Tepung Tapioka", 250.0, "gram"), Triple("Tepung Terigu", 50.0, "gram"),
                Triple("Bawang Putih", 10.0, "gram"), Triple("Daun Bawang", 20.0, "gram"),
                Triple("Garam", 5.0, "gram"), Triple("Minyak Goreng", 200.0, "ml")
            )
        ),
        RecipeTemplate(
            name = "Dimsum Ayam", emoji = "🥟", category = "Snack", yield = 10, laborCost = 3000.0,
            description = "Dimsum ayam kukus",
            ingredients = listOf(
                Triple("Dada Ayam", 200.0, "gram"), Triple("Udang", 100.0, "gram"),
                Triple("Kulit Dimsum", 10.0, "pcs"), Triple("Bawang Putih", 10.0, "gram"),
                Triple("Kecap Asin", 10.0, "ml"), Triple("Minyak Wijen", 5.0, "ml")
            )
        ),

        // === KUE ===
        RecipeTemplate(
            name = "Brownies", emoji = "🍫", category = "Kue", yield = 8, laborCost = 5000.0,
            description = "Brownies coklat panggang",
            ingredients = listOf(
                Triple("Dark Chocolate", 200.0, "gram"), Triple("Mentega", 150.0, "gram"),
                Triple("Telur", 3.0, "pcs"), Triple("Gula Pasir", 150.0, "gram"),
                Triple("Tepung Terigu", 100.0, "gram"), Triple("Coklat Bubuk", 30.0, "gram")
            )
        ),
        RecipeTemplate(
            name = "Donat", emoji = "🍩", category = "Kue", yield = 12, laborCost = 4000.0,
            description = "Donat empuk dengan topping gula",
            ingredients = listOf(
                Triple("Tepung Terigu", 500.0, "gram"), Triple("Ragi", 7.0, "gram"),
                Triple("Gula Pasir", 80.0, "gram"), Triple("Telur", 2.0, "pcs"),
                Triple("Mentega", 60.0, "gram"), Triple("Susu Bubuk", 30.0, "gram"),
                Triple("Minyak Goreng", 500.0, "ml")
            )
        ),
        RecipeTemplate(
            name = "Nastar", emoji = "🍪", category = "Kue", yield = 30, laborCost = 5000.0,
            description = "Nastar nanas klasik",
            ingredients = listOf(
                Triple("Tepung Terigu", 300.0, "gram"), Triple("Mentega", 200.0, "gram"),
                Triple("Kuning Telur", 3.0, "pcs"), Triple("Gula Halus", 80.0, "gram"),
                Triple("Selai Nanas", 200.0, "gram"), Triple("Susu Bubuk", 30.0, "gram")
            )
        ),
        RecipeTemplate(
            name = "Bolu Kukus", emoji = "🧁", category = "Kue", yield = 10, laborCost = 3000.0,
            description = "Bolu kukus mekar lembut",
            ingredients = listOf(
                Triple("Tepung Terigu", 250.0, "gram"), Triple("Gula Pasir", 200.0, "gram"),
                Triple("Telur", 3.0, "pcs"), Triple("Santan", 150.0, "ml"),
                Triple("Baking Powder", 5.0, "gram"), Triple("Pewarna Makanan", 2.0, "ml")
            )
        ),
        RecipeTemplate(
            name = "Kue Lapis", emoji = "🍰", category = "Kue", yield = 12, laborCost = 5000.0,
            description = "Kue lapis legit tradisional",
            ingredients = listOf(
                Triple("Tepung Terigu", 200.0, "gram"), Triple("Mentega", 250.0, "gram"),
                Triple("Telur", 10.0, "pcs"), Triple("Gula Pasir", 200.0, "gram"),
                Triple("Susu Kental Manis", 100.0, "ml"), Triple("Vanilla", 5.0, "ml")
            )
        ),
        RecipeTemplate(
            name = "Cheese Cake", emoji = "🧀", category = "Kue", yield = 8, laborCost = 6000.0,
            description = "Japanese cheese cake lembut",
            ingredients = listOf(
                Triple("Cream Cheese", 250.0, "gram"), Triple("Mentega", 50.0, "gram"),
                Triple("Susu UHT", 100.0, "ml"), Triple("Telur", 3.0, "pcs"),
                Triple("Tepung Terigu", 40.0, "gram"), Triple("Gula Pasir", 80.0, "gram")
            )
        )
    )
}
