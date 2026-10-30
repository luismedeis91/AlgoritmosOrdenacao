# Ordenação Sequencial e Paralela em Java

> Avaliação experimental de quatro algoritmos de ordenação com o framework **Fork/Join**.

**Universidade de Fortaleza (UNIFOR)** · Centro de Ciências Tecnológicas · Ciência da Computação

| Autor | Matrícula |
|-------|-----------|
| Luis Eduardo | 2413528 |
| Theo Passos | 2416515 |

---

## Algoritmos

| Algoritmo | Complexidade | Versão paralela |
|-----------|--------------|-----------------|
| Bubble    | O(n²)        | par-ímpar (*odd-even transposition*) |
| Selection | O(n²)        | busca do mínimo como redução paralela |
| Quick     | O(n log n)   | lados do pivô em tarefas · partição de 3 vias |
| Merge     | O(n log n)   | metades em tarefas · intercalação sequencial |

Todas as versões paralelas expõem `sort(int[] a, int threads)` e caem na versão
sequencial abaixo de `LIMITE` (10.000 em Quick/Merge, 16.384 em Bubble/Selection).

## Estrutura

```
src/ordenacao/
├── BubbleSort.java      ParallelBubbleSort.java
├── SelectionSort.java   ParallelSelectionSort.java
├── QuickSort.java       ParallelQuickSort.java
├── MergeSort.java       ParallelMergeSort.java
└── Benchmark.java
scripts/graficos.py
```

## Como rodar

```bash
javac -d out src/ordenacao/*.java
java -cp out ordenacao.QuickSort           # testes + tempo de um algoritmo
java -cp out ordenacao.ParallelQuickSort   # sequencial × 1, 2, 4… threads
```

Cada `main` roda um exemplo, casos de borda e testes aleatórios contra `Arrays.sort`.

## Benchmark

```bash
java -cp out ordenacao.Benchmark   # resultados/tempos.csv
python3 scripts/graficos.py        # resultados/resumo.csv + figuras
```

| Parâmetro | Valores |
|-----------|---------|
| n         | 2.500 · 5.000 · 10.000 · 20.000 |
| Entradas  | aleatória · ordenada · invertida · quase ordenada · repetições |
| Threads   | sequencial · 1 · 2 · 4 · 8 · 12 |
| Amostras  | 5 por configuração → **2.400 medições** |

## Resultados

Speedup em n = 20.000, entrada aleatória (i5-13420H, 12 threads, OpenJDK 27):

| Algoritmo | 1    | 2        | 4    | 8    | 12   |
|-----------|------|----------|------|------|------|
| Bubble    | 1,99 | **2,42** | 1,38 | 1,37 | 1,40 |
| Selection | 0,78 | 1,01     | 0,81 | 0,84 | 0,80 |
| Quick     | 0,96 | 0,82     | 0,87 | 0,99 | 1,15 |
| Merge     | 1,35 | **1,49** | 1,26 | 1,24 | 1,07 |

- **O algoritmo pesa mais que o paralelismo** — Bubble sequencial é 194× mais lento que Quick sequencial.
- **Nessa faixa de n, paralelizar não compensa** — o custo de criar e sincronizar tarefas supera o trabalho útil.
- **A entrada importa** — Selection é indiferente ao conteúdo; Bubble varia quatro ordens de grandeza.
