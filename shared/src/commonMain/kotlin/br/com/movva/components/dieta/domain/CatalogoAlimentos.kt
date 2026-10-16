package br.com.movva.components.dieta.domain

// Catálogo local de alimentos comuns. Valores por 100 g, aproximados da Tabela TACO (UNICAMP, 4ª ed.);
// quando o item não existe na TACO (ex: macarrão cozido, salmão, whey), foram usados valores médios
// de referência (USDA / rótulos). Revisar com alguém de nutrição antes de tratar como dado oficial.
object CatalogoAlimentos {

    private val colherSopa = Porcao("colher de sopa", "colheres de sopa", 25.0)
    private fun unidade(g: Double) = Porcao("unidade", "unidades", g)
    private fun fatia(g: Double) = Porcao("fatia", "fatias", g)
    private fun copo(g: Double) = Porcao("copo", "copos", g)

    val alimentos: List<Alimento> = listOf(
        // cereais, massas e tubérculos
        a("arroz-branco", "Arroz branco cozido", "🍚", 128.0, 2.5, 28.1, 0.2,
            Porcao("xícara", "xícaras", 150.0), colherSopa),
        a("arroz-integral", "Arroz integral cozido", "🍚", 124.0, 2.6, 25.8, 1.0,
            Porcao("xícara", "xícaras", 150.0), colherSopa),
        a("feijao-carioca", "Feijão carioca cozido", "🍲", 76.0, 4.8, 13.6, 0.5,
            Porcao("concha", "conchas", 140.0)),
        a("feijao-preto", "Feijão preto cozido", "🍲", 77.0, 4.5, 14.0, 0.5,
            Porcao("concha", "conchas", 140.0)),
        a("macarrao", "Macarrão cozido", "🍝", 158.0, 5.8, 30.9, 0.9,
            Porcao("prato raso", "pratos rasos", 200.0), Porcao("pegador", "pegadores", 110.0)),
        a("batata-inglesa", "Batata inglesa cozida", "🥔", 52.0, 1.2, 11.9, 0.0, unidade(130.0)),
        a("batata-doce", "Batata doce cozida", "🍠", 77.0, 0.6, 18.4, 0.1, unidade(150.0)),
        a("mandioca", "Mandioca cozida", "🥔", 125.0, 0.6, 30.1, 0.3, Porcao("pedaço", "pedaços", 70.0)),
        a("cuscuz", "Cuscuz de milho cozido", "🌽", 113.0, 2.2, 25.3, 0.7, fatia(100.0)),
        a("pao-frances", "Pão francês", "🥖", 300.0, 8.0, 58.6, 3.1, unidade(50.0)),
        a("pao-forma-integral", "Pão de forma integral", "🍞", 253.0, 9.4, 49.9, 3.7, fatia(25.0)),
        a("pao-de-queijo", "Pão de queijo", "🧀", 363.0, 5.1, 34.2, 24.6, unidade(20.0)),
        a("tapioca", "Tapioca (goma)", "🫓", 240.0, 0.0, 60.0, 0.0, Porcao("disco", "discos", 50.0)),
        a("aveia", "Aveia em flocos", "🥣", 394.0, 13.9, 66.6, 8.5, Porcao("colher de sopa", "colheres de sopa", 15.0)),

        // proteínas
        a("frango-grelhado", "Peito de frango grelhado", "🍗", 159.0, 32.0, 0.0, 2.5, Porcao("filé", "filés", 100.0)),
        a("patinho-grelhado", "Patinho grelhado", "🥩", 219.0, 35.9, 0.0, 7.3, Porcao("bife", "bifes", 100.0)),
        a("carne-moida", "Carne moída refogada", "🥩", 212.0, 26.7, 0.0, 10.9, colherSopa),
        a("ovo-cozido", "Ovo cozido", "🥚", 146.0, 13.3, 0.6, 9.5, unidade(50.0)),
        a("ovo-frito", "Ovo frito", "🍳", 240.0, 15.6, 1.2, 18.6, unidade(50.0)),
        a("tilapia", "Tilápia grelhada", "🐟", 128.0, 26.2, 0.0, 2.7, Porcao("filé", "filés", 120.0)),
        a("salmao", "Salmão grelhado", "🐟", 206.0, 22.1, 0.0, 12.4, Porcao("filé", "filés", 120.0)),
        a("atum-lata", "Atum em lata (em água)", "🐟", 116.0, 25.5, 0.0, 0.8, Porcao("lata", "latas", 120.0)),
        a("presunto", "Presunto", "🥓", 94.0, 14.3, 2.1, 2.7, fatia(15.0)),
        a("whey", "Whey protein", "💪", 400.0, 80.0, 8.0, 6.0, Porcao("scoop", "scoops", 30.0)),

        // laticínios
        a("leite-integral", "Leite integral", "🥛", 61.0, 2.9, 4.3, 3.2, copo(200.0)),
        a("leite-desnatado", "Leite desnatado", "🥛", 35.0, 3.4, 4.9, 0.2, copo(200.0)),
        a("iogurte-natural", "Iogurte natural", "🥛", 51.0, 4.1, 1.9, 3.0, Porcao("pote", "potes", 170.0)),
        a("queijo-minas", "Queijo minas frescal", "🧀", 264.0, 17.4, 3.2, 20.2, fatia(30.0)),
        a("mucarela", "Queijo muçarela", "🧀", 330.0, 22.6, 3.0, 25.2, fatia(15.0)),
        a("requeijao", "Requeijão cremoso", "🧀", 257.0, 9.6, 2.4, 23.4, Porcao("colher de sopa", "colheres de sopa", 30.0)),

        // frutas
        a("banana-prata", "Banana prata", "🍌", 98.0, 1.3, 26.0, 0.1, unidade(70.0)),
        a("maca", "Maçã", "🍎", 56.0, 0.3, 15.2, 0.0, unidade(130.0)),
        a("laranja", "Laranja pera", "🍊", 37.0, 1.0, 8.9, 0.1, unidade(140.0)),
        a("mamao", "Mamão papaia", "🍈", 40.0, 0.5, 10.4, 0.1, fatia(100.0)),
        a("manga", "Manga", "🥭", 72.0, 0.4, 19.4, 0.2, fatia(100.0)),
        a("morango", "Morango", "🍓", 30.0, 0.9, 6.8, 0.3, unidade(12.0)),
        a("uva", "Uva", "🍇", 53.0, 0.7, 13.6, 0.2, Porcao("cacho pequeno", "cachos pequenos", 100.0)),
        a("abacate", "Abacate", "🥑", 96.0, 1.2, 6.0, 8.4, colherSopa),

        // verduras e legumes
        a("alface", "Alface", "🥬", 11.0, 1.3, 1.7, 0.2, Porcao("folha", "folhas", 10.0)),
        a("tomate", "Tomate", "🍅", 15.0, 1.1, 3.1, 0.2, unidade(100.0), fatia(15.0)),
        a("cenoura", "Cenoura crua", "🥕", 34.0, 1.3, 7.7, 0.2, Porcao("colher de sopa ralada", "colheres de sopa raladas", 12.0)),
        a("brocolis", "Brócolis cozido", "🥦", 25.0, 2.1, 4.4, 0.5, Porcao("ramo", "ramos", 20.0)),

        // gorduras, bebidas e outros
        a("azeite", "Azeite de oliva", "🫒", 884.0, 0.0, 0.0, 100.0, Porcao("colher de sopa", "colheres de sopa", 8.0)),
        a("manteiga", "Manteiga", "🧈", 726.0, 0.4, 0.1, 82.4, Porcao("ponta de faca", "pontas de faca", 5.0)),
        a("pasta-amendoim", "Pasta de amendoim", "🥜", 588.0, 25.0, 20.0, 50.0, Porcao("colher de sopa", "colheres de sopa", 15.0)),
        a("cafe", "Café sem açúcar", "☕", 9.0, 0.7, 1.5, 0.1, Porcao("xícara", "xícaras", 50.0)),
        a("suco-laranja", "Suco de laranja natural", "🧃", 33.0, 0.7, 7.6, 0.1, copo(200.0)),
        a("acucar", "Açúcar", "🍬", 387.0, 0.0, 99.5, 0.0, Porcao("colher de chá", "colheres de chá", 5.0)),
        a("chocolate-leite", "Chocolate ao leite", "🍫", 540.0, 7.2, 59.6, 30.3, Porcao("quadradinho", "quadradinhos", 5.0)),
    )

    private val porIdMap = alimentos.associateBy { it.id }

    fun porId(id: String): Alimento? = porIdMap[id]

    // Busca sem acento e sem diferenciar maiúsculas; quem começa com o termo vem primeiro.
    fun buscar(texto: String, limite: Int = 20): List<Alimento> {
        val termo = normalizar(texto)
        if (termo.isEmpty()) return emptyList()
        return alimentos
            .filter { normalizar(it.nome).contains(termo) }
            .sortedBy { if (normalizar(it.nome).startsWith(termo)) 0 else 1 }
            .take(limite)
    }

    private fun a(
        id: String,
        nome: String,
        icone: String,
        kcal: Double,
        proteina: Double,
        carbo: Double,
        gordura: Double,
        vararg porcoes: Porcao,
    ) = Alimento(id, nome, icone, porcoes.toList(), kcal, proteina, carbo, gordura)
}
