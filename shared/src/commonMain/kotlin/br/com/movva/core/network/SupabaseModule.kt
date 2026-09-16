package br.com.movva.core.network

import br.com.movva.shared.config.SupabaseConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.dsl.module

// Temporário: substituir pelo SupabaseClient singleton da task de Auth quando disponível.
val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.URL,
            supabaseKey = SupabaseConfig.ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
