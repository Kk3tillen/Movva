package br.com.movva.components.treino.domain

data class NovoTreinoForm(
    val nome: String = "",
    val tipo: String = "",
    val duracao: String = "",
    val observacoes: String = "",
    val exercicios: List<ExercicioForm> = emptyList(),
)

data class ExercicioForm(
    val id: String,
    val nome: String = "",
    val series: String = "3",
    val reps: String = "10",
    val carga: String = "",
)

data class NovoTreinoErros(
    val nome: String? = null,
    val tipo: String? = null,
    val duracao: String? = null,
    val exerciciosVazios: String? = null,
    val exercicios: List<ExercicioErros> = emptyList(),
) {
    val temErro: Boolean
        get() = nome != null || tipo != null || duracao != null ||
            exerciciosVazios != null ||
            exercicios.any { it.temErro }
}

data class ExercicioErros(
    val nome: String? = null,
    val series: String? = null,
    val reps: String? = null,
    val carga: String? = null,
) {
    val temErro: Boolean
        get() = nome != null || series != null || reps != null || carga != null
}

fun validarNovoTreino(form: NovoTreinoForm): NovoTreinoErros = NovoTreinoErros(
    nome = if (form.nome.isBlank()) "Informe o nome do treino" else null,
    tipo = if (form.tipo.isBlank()) "Informe o tipo do treino" else null,
    duracao = validarDuracao(form.duracao),
    exerciciosVazios = if (form.exercicios.isEmpty()) "Adicione pelo menos um exercício" else null,
    exercicios = form.exercicios.map(::validarExercicio),
)

fun validarExercicio(form: ExercicioForm): ExercicioErros = ExercicioErros(
    nome = if (form.nome.isBlank()) "Informe o exercício" else null,
    series = validarInteiroPositivo(form.series),
    reps = validarInteiroPositivo(form.reps),
    carga = if (form.carga.isNotBlank() && form.carga.paraCargaKg() == null) "Inválida" else null,
)

// Converte o formulário (já validado) para o domínio.
fun ExercicioForm.toExercicio() = Exercicio(
    id = id,
    nome = nome.trim(),
    seriesPlanejadas = series.toInt(),
    repsPlanejadas = reps.toInt(),
    cargaPlanejadaKg = carga.paraCargaKg(),
)

// Aceita vírgula ou ponto: "42,5" -> 42.5
fun String.paraCargaKg(): Double? = trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 }

private fun validarInteiroPositivo(texto: String): String? {
    val valor = texto.toIntOrNull() ?: return "Obrigatório"
    return if (valor <= 0) "Maior que 0" else null
}

private fun validarDuracao(texto: String): String? {
    if (texto.isBlank()) return null // opcional
    val valor = texto.toIntOrNull() ?: return "Use apenas números"
    return if (valor <= 0) "A duração deve ser maior que zero" else null
}
