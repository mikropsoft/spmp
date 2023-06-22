package com.spectre7.spmp.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spectre7.spmp.resources.getStringTODO
import com.spectre7.spmp.ui.theme.Theme
import com.spectre7.utils.modifier.background

@Composable
fun ErrorInfoDisplay(error: Throwable, modifier: Modifier = Modifier) {
    var expanded: Boolean by remember { mutableStateOf(false) }

    Column(
        modifier
            .animateContentSize()
            .background(RoundedCornerShape(16.dp), Theme.current.accent_provider)
            .clickable {
                expanded = !expanded
            }
    ) {
        CompositionLocalProvider(LocalContentColor provides Theme.current.on_accent) {
            val message = if (expanded) null else error.message?.let { " - $it" }
            Text(error::class.java.simpleName + (message ?: ""))

            if (expanded) {
                Text(error.stackTraceToString(), Modifier.verticalScroll(rememberScrollState()))

                Button({ throw error }) {
                    Text(getStringTODO("Throw"))
                }
            }
        }
    }
}