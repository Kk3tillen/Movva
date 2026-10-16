package br.com.movva.components.treino.ui

import br.com.movva.components.treino.domain.Exercicio
import br.com.movva.components.treino.domain.Serie
import br.com.movva.components.treino.domain.TreinoDetalhe
import br.com.movva.components.treino.domain.formatarCarga
import br.com.movva.components.treino.domain.paraCargaKg
import br.com.movva.components.treino.domain.resumo

// Os campos ficam como texto para o usuário poder digitar livremente (ex: "42," antes de "42,5").
data class SerieUi(
    val reps: String,
    val carga: String,
    val concluida: Boolean = false,
) {
    val repsValidas: Boolean get() = (reps.toIntOrNull() ?: 0) > 0
}

data class ExercicioUi(
    val exercicio: Exercicio,
    val series: List<SerieUi>,
) {
    val id: String get() = exercicio.id
    val nome: String get() = exercicio.nome
    val resumo: String get() = exercicio.resumo()
    val concluido: Boolean get() = series.isNotEmpty() && series.all { it.concluida }
}

// Estado do registro série a série. Todas as operações são puras e devolvem uma cópia.
data class RegistroTreino(
    val treino: TreinoDetalhe,
    val exercicios: List<ExercicioUi>,
    val expandidoId: String? = null,
) {
    val total: Int get() = exercicios.size
    val concluidos: Int get() = exercicios.count { it.concluido }
    val progresso: Float get() = if (total == 0) 0f else concluidos.toFloat() / total
    val temSeriesRegistradas: Boolean get() = exercicios.any { ex -> ex.series.any { it.concluida } }

    // Só um card expandido por vez; tocar no que já está aberto recolhe.
    fun alternarExpandido(exercicioId: String) =
        copy(expandidoId = if (expandidoId == exercicioId) null else exercicioId)

    fun alterarReps(exercicioId: String, indice: Int, valor: String): RegistroTreino {
        val filtrado = valor.filter { it.isDigit() }.take(3)
        return atualizarSerie(exercicioId, indice) { it.copy(reps = filtrado) }
    }

    fun alterarCarga(exercicioId: String, indice: Int, valor: String): RegistroTreino {
        val filtrado = filtrarDecimal(valor)
        return atualizarSerie(exercicioId, indice) { it.copy(carga = filtrado) }
    }

    fun alternarSerie(exercicioId: String, indice: Int): RegistroTreino {
        val serie = exercicios.firstOrNull { it.id == exercicioId }?.series?.getOrNull(indice) ?: return this
        if (!serie.concluida && !serie.repsValidas) return this

        val atualizado = atualizarSerie(exercicioId, indice) { it.copy(concluida = !it.concluida) }
        val exercicio = atualizado.exercicios.first { it.id == exercicioId }
        if (serie.concluida || !exercicio.concluido) return atualizado

        // exercício acabou de ser concluído: recolhe e abre o próximo pendente
        return atualizado.copy(expandidoId = atualizado.proximoPendenteDepoisDe(exercicioId)?.id)
    }

    // Nova série copia os valores da última.
    fun adicionarSerie(exercicioId: String) = atualizarExercicio(exercicioId) { ex ->
        val ultima = ex.series.lastOrNull()
        val nova = SerieUi(
            reps = ultima?.reps ?: ex.exercicio.repsPlanejadas.toString(),
            carga = ultima?.carga ?: formatarCarga(ex.exercicio.cargaPlanejadaKg),
        )
        ex.copy(series = ex.series + nova)
    }

    fun removerSerie(exercicioId: String, indice: Int) = atualizarExercicio(exercicioId) { ex ->
        if (ex.series.size <= 1) ex else ex.copy(series = ex.series.filterIndexed { i, _ -> i != indice })
    }

    // Volta para o planejado, ignorando o que já tinha sido registrado hoje.
    fun reiniciar() = copy(
        exercicios = iniciar(treino.copy(exercicios = treino.exercicios.map { it.copy(series = emptyList()) })).exercicios,
        expandidoId = null,
    )

    // Converte para o domínio todas as séries (feitas ou não), para reabrir a tela no mesmo estado.
    fun exerciciosRegistrados(): List<Exercicio> = exercicios.map { ex ->
        ex.exercicio.copy(
            series = ex.series.mapIndexed { i, s ->
                Serie(
                    numero = i + 1,
                    reps = s.reps.toIntOrNull() ?: 0,
                    cargaKg = s.carga.paraCargaKg(),
                    concluida = s.concluida,
                )
            }
        )
    }

    private fun proximoPendenteDepoisDe(exercicioId: String): ExercicioUi? {
        val indice = exercicios.indexOfFirst { it.id == exercicioId }
        return exercicios.drop(indice + 1).firstOrNull { !it.concluido }
            ?: exercicios.take(indice).firstOrNull { !it.concluido }
    }

    private fun atualizarSerie(exercicioId: String, indice: Int, transformar: (SerieUi) -> SerieUi) =
        atualizarExercicio(exercicioId) { ex ->
            ex.copy(series = ex.series.mapIndexed { i, s -> if (i == indice) transformar(s) else s })
        }

    private fun atualizarExercicio(exercicioId: String, transformar: (ExercicioUi) -> ExercicioUi) =
        copy(exercicios = exercicios.map { if (it.id == exercicioId) transformar(it) else it })

    companion object {
        // Séries já registradas são mantidas; senão, cria as planejadas preenchidas com os valores-alvo.
        fun iniciar(treino: TreinoDetalhe) = RegistroTreino(
            treino = treino,
            exercicios = treino.exercicios.map { ex ->
                val series = if (ex.series.isNotEmpty()) {
                    ex.series.map { SerieUi(it.reps.toString(), formatarCarga(it.cargaKg), it.concluida) }
                } else {
                    List(ex.seriesPlanejadas.coerceAtLeast(1)) {
                        SerieUi(ex.repsPlanejadas.toString(), formatarCarga(ex.cargaPlanejadaKg))
                    }
                }
                ExercicioUi(ex, series)
            },
        )
    }
}

// Aceita dígitos e um único separador decimal ("," ou "."), com no máximo 1 casa.
private fun filtrarDecimal(valor: String): String {
    val sb = StringBuilder()
    var temSeparador = false
    var casas = 0
    for (c in valor) {
        when {
            c.isDigit() && !temSeparador && sb.length < 4 -> sb.append(c)
            c.isDigit() && temSeparador && casas < 1 -> { sb.append(c); casas++ }
            (c == ',' || c == '.') && !temSeparador && sb.isNotEmpty() -> { sb.append(c); temSeparador = true }
        }
    }
    return sb.toString()
}
