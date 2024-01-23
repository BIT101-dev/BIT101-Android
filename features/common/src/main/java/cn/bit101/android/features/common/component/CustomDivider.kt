package cn.bit101.android.features.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomDivider(
    height: Dp = 12.dp,
    padding: Dp = 4.dp,
    cornerRadius: Dp = 12.dp,
) {
    Divider(Modifier.padding(vertical = padding + height))

//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(MaterialTheme.colorScheme.surfaceVariant),
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(height),
//            shape = RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surface,
//                disabledContainerColor = MaterialTheme.colorScheme.surface,
//            ),
//        ) {}
//        Spacer(modifier = Modifier.padding(padding))
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(height),
//            shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surface,
//                disabledContainerColor = MaterialTheme.colorScheme.surface,
//            ),
//        ) {}
//    }
}