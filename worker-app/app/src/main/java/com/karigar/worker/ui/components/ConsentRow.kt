package com.karigar.worker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karigar.worker.data.Legal
import com.karigar.worker.data.LegalDoc

@Composable
fun ConsentRow(
    agreed: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onOpenDoc: (LegalDoc) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val annotated = buildAnnotatedString {
        append("I have read and agree to the ")
        pushStringAnnotation("doc", "terms")
        withStyle(SpanStyle(color = primary, fontWeight = FontWeight.Bold)) { append("Terms & Conditions") }
        pop()
        append(", ")
        pushStringAnnotation("doc", "privacy")
        withStyle(SpanStyle(color = primary, fontWeight = FontWeight.Bold)) { append("Privacy Policy") }
        pop()
        append(", ")
        pushStringAnnotation("doc", "refund")
        withStyle(SpanStyle(color = primary, fontWeight = FontWeight.Bold)) { append("Refund Policy") }
        pop()
        append(" and ")
        pushStringAnnotation("doc", "community")
        withStyle(SpanStyle(color = primary, fontWeight = FontWeight.Bold)) { append("Community Guidelines") }
        pop()
        append(".")
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = agreed, onCheckedChange = onCheckedChange)
        Spacer(Modifier.size(2.dp))
        ClickableText(
            text = annotated,
            style = TextStyle(fontSize = 13.sp, color = muted, lineHeight = 18.sp)
        ) { offset ->
            annotated.getStringAnnotations("doc", offset, offset).firstOrNull()?.let { ann ->
                Legal.byKey(ann.item)?.let(onOpenDoc)
            }
        }
    }
}
