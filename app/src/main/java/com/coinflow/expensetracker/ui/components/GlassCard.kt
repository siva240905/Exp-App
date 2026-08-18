package com.coinflow.expensetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.coinflow.expensetracker.ui.theme.BorderOutline
import com.coinflow.expensetracker.ui.theme.PrimaryCyanBright
import com.coinflow.expensetracker.ui.theme.SurfaceDark

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = BorderOutline,
    isGlowing: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val effectiveBorder = if (isGlowing) PrimaryCyanBright.copy(alpha = 0.5f) else borderColor

    Box(
        modifier = modifier
            .clip(shape)
            .background(SurfaceDark.copy(alpha = 0.85f))
            .border(
                border = BorderStroke(1.dp, effectiveBorder),
                shape = shape
            )
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(16.dp)
    ) {
        content()
    }
}
