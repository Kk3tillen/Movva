package br.com.movva.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import movva.shared.generated.resources.Res
import movva.shared.generated.resources.movva_logo
import org.jetbrains.compose.resources.painterResource

private val MovvaOrange = Color(0xFFE34200)
private val FieldBackground = Color.White.copy(alpha = 0.95f)
private val FieldBorder = MovvaOrange.copy(alpha = 0.22f)
private val PlaceholderColor = Color.Black.copy(alpha = 0.55f)

@Composable
fun LoginScreen(
    onLoginClick: (identifier: String, password: String) -> Unit = { _, _ -> },
    onSignUpClick: () -> Unit = {},
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to MovvaOrange,
                    0.78f to Color.White,
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.movva_logo),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 56.dp)
                    .size(200.dp),
            )

            Text(
                text = "Junte-se ao movimento!\nSeja Movva",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp),
            )

            LoginTextField(
                value = identifier,
                onValueChange = { identifier = it },
                placeholder = "000.000.000-00",
                keyboardType = KeyboardType.Text,
                modifier = Modifier.padding(top = 40.dp),
            )

            LoginTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Senha",
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(top = 16.dp),
            )

            Button(
                onClick = { onLoginClick(identifier, password) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MovvaOrange),
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .height(46.dp),
            ) {
                Text(
                    text = "ENTRAR",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }

            SignUpPrompt(
                onSignUpClick = onSignUpClick,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(FieldBackground, RoundedCornerShape(10.dp))
            .border(1.dp, FieldBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(text = placeholder, color = PlaceholderColor, fontSize = 16.sp)
                }
                innerTextField()
            },
        )
    }
}

@Composable
private fun SignUpPrompt(onSignUpClick: () -> Unit, modifier: Modifier = Modifier) {
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = Color.Black.copy(alpha = 0.75f))) {
            append("Ainda não tem conta? ")
        }
        withStyle(SpanStyle(color = MovvaOrange, fontWeight = FontWeight.SemiBold)) {
            append("Então se cadastre")
        }
    }
    Text(
        text = text,
        fontSize = 15.sp,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}
