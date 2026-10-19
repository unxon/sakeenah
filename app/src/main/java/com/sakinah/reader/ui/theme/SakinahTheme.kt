package com.sakinah.reader.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

object SakinahDesign {
    val Ink = Color(0xFF213936)
    val Deep = Color(0xFF153A36)
    val Sage = Color(0xFF6D8B7D)
    val Gold = Color(0xFFC99C52)
    val Paper = Color(0xFFF7F5EF)
    val Mist = Color(0xFFE7EEE8)
    val Line = Color(0xFFD9E1DB)
    val Muted = Color(0xFF6F7F78)
    val RadiusCard = 20.dp
    val RadiusHero = 24.dp
    val TouchTarget = 48.dp
}

@Composable
fun SakinahTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = SakinahDesign.Deep, secondary = SakinahDesign.Sage, background = SakinahDesign.Paper, surface = Color.White),
        typography = Typography(),
        shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(SakinahDesign.RadiusCard), large = RoundedCornerShape(SakinahDesign.RadiusHero)),
        content = content,
    )
}

@Composable
fun SakinahMark(modifier: Modifier = Modifier) {
    Canvas(modifier.size(46.dp)) {
        drawRoundRect(SakinahDesign.Deep, cornerRadius = androidx.compose.ui.geometry.CornerRadius(15.dp.toPx()))
        val mark = Path().apply { moveTo(size.width * .28f, size.height * .65f); quadraticBezierTo(size.width * .48f, size.height * .2f, size.width * .72f, size.height * .65f) }
        drawPath(mark, SakinahDesign.Gold, style = Stroke(width = 2.dp.toPx()))
        drawCircle(SakinahDesign.Gold, radius = size.width * .08f, center = androidx.compose.ui.geometry.Offset(size.width * .5f, size.height * .35f), style = Stroke(width = 2.dp.toPx()))
        drawLine(SakinahDesign.Gold, androidx.compose.ui.geometry.Offset(size.width*.27f,size.height*.72f), androidx.compose.ui.geometry.Offset(size.width*.73f,size.height*.72f), strokeWidth = 2.dp.toPx())
    }
}
