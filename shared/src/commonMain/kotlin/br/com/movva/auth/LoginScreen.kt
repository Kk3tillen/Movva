package br.com.movva.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val BackgroundColor = Color(0xFF161616)
private val FieldBackground = Color(0xFF1F1F1F)
private val FieldBorder = Color(0xFF2E2E2E)
private val OrangeAccent = Color(0xFFF5A623)
private val PlaceholderColor = Color(0xFF7A7A7A)
private val TextColor = Color(0xFFEDEDED)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit,
    onCreateAccountClick: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .background(BackgroundColor)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(OrangeAccent, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\uD83D\uDD25", // 🔥
                fontSize = 28.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Movva",
            color = TextColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Treino e dieta, todo dia",
            color = PlaceholderColor,
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // E-mail
        Text(
            text = "E-mail",
            color = TextColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("nome@email.com", color = PlaceholderColor) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = FieldBorder,
                focusedTextColor = TextColor,
                unfocusedTextColor = TextColor,
                cursorColor = OrangeAccent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Senha
        Text(
            text = "Senha",
            color = TextColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Sua senha", color = PlaceholderColor) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                focusedBorderColor = OrangeAccent,
                unfocusedBorderColor = FieldBorder,
                focusedTextColor = TextColor,
                unfocusedTextColor = TextColor,
                cursorColor = OrangeAccent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Esqueci minha senha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = { /* TODO: recuperar senha */ }) {
                Text("Esqueci minha senha", color = OrangeAccent, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Entrar
        Button(
            onClick = onLoginSuccess,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Entrar", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Divisor "ou"
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = FieldBorder)
            Text(
                text = "ou",
                color = PlaceholderColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = FieldBorder)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Criar conta
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Não tem conta? ",
                color = PlaceholderColor,
                fontSize = 13.sp,
            )
            Text(
                text = "Criar conta",
                color = OrangeAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}