package br.com.movva.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CampoTexto(
    label: String,
    valor: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    erro: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = colors.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = colors.onSurfaceVariant) },
            isError = erro != null,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                errorContainerColor = colors.surfaceVariant,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline,
                errorBorderColor = colors.error,
                focusedTextColor = colors.onBackground,
                unfocusedTextColor = colors.onBackground,
                errorTextColor = colors.onBackground,
                cursorColor = colors.primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (erro != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = erro, color = colors.error, fontSize = 12.sp)
        }
    }
}