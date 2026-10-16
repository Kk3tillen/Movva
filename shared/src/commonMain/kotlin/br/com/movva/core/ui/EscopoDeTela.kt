package br.com.movva.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner

// ViewModels criados dentro do conteúdo vivem só enquanto a tela está aberta (sobrevivem à rotação)
// e são descartados ao sair, para que abrir a tela de novo comece com estado limpo.
@Composable
fun EscopoDeTela(conteudo: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides rememberViewModelStoreOwner(),
        content = conteudo,
    )
}
