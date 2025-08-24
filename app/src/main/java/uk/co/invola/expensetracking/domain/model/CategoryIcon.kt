package uk.co.invola.expensetracking.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class CategoryIcon(
    val iconId: String,
    val icon: ImageVector,
    val color: Color,
) {
    GROCERIES("ICON_GROCERIES", Icons.Default.ShoppingCart, Color(0xFF4CAF50)),
    ENTERTAINMENT("ICON_ENTERTAINMENT", Icons.Default.Star, Color(0xFF9C27B0)),
    GAS("ICON_GAS_FUEL", Icons.Default.Build, Color(0xFFFF9800)),
    SHOPPING("ICON_SHOPPING_BAG", Icons.Default.ShoppingCart, Color(0xFF2196F3)),
    NEWS_PAPER("ICON_NEWS_MEDIA", Icons.Default.Info, Color(0xFF607D8B)),
    TRANSPORT("ICON_TRANSPORT", Icons.Default.Build, Color(0xFF795548)),
    RENT("ICON_RENT_HOME", Icons.Default.Home, Color(0xFFF44336)),
    RESTAURANT("ICON_RESTAURANT_FOOD", Icons.Default.Favorite, Color(0xFF009688)),
    HEALTH("ICON_HEALTH_MEDICAL", Icons.Default.Favorite, Color(0xFFE91E63)),
    EDUCATION("ICON_EDUCATION_SCHOOL", Icons.Default.Star, Color(0xFF3F51B5)),
    UTILITIES("ICON_UTILITIES_BILLS", Icons.Default.Build, Color(0xFFFF5722)),
    TRAVEL("ICON_TRAVEL_VACATION", Icons.Default.Place, Color(0xFF00BCD4)),
    CLOTHING("ICON_CLOTHING_FASHION", Icons.Default.Person, Color(0xFF8BC34A)),
    GIFTS("ICON_GIFTS_PRESENTS", Icons.Default.Favorite, Color(0xFFFF4081)),
    SPORTS("ICON_SPORTS_FITNESS", Icons.Default.Star, Color(0xFF673AB7)),
    TECHNOLOGY("ICON_TECHNOLOGY_GADGETS", Icons.Default.Phone, Color(0xFF03DAC5)),
    INSURANCE("ICON_INSURANCE_PROTECTION", Icons.Default.Info, Color(0xFF6200EE)),
    OTHER("ICON_OTHER_MISCELLANEOUS", Icons.Default.MoreVert, Color(0xFF757575)),
    ;

    companion object {
        /**
         * Get CategoryIcon by iconId
         */
        fun fromIconId(iconId: String): CategoryIcon =
            entries.find { it.iconId.equals(iconId, ignoreCase = true) } ?: OTHER
    }
}
