# Algoritmos de Ordenação em Java

Cada algoritmo fica em uma classe própria, com uma `main` que testa a implementação.

```
src/ordenacao/
├── BubbleSort.java
├── SelectionSort.java
├── QuickSort.java
├── MergeSort.java
├── ParallelBubbleSort.java     (Fork/Join, variante par-ímpar)
├── ParallelSelectionSort.java  (Fork/Join, redução do mínimo)
├── ParallelQuickSort.java      (Fork/Join, reaproveita QuickSort)
├── ParallelMergeSort.java      (Fork/Join, reaproveita MergeSort)
└── Benchmark.java              (mede serial × paralelo e grava CSV)
```

| Algoritmo | Melhor     | Médio      | Pior       | Espaço   | Estável |
|-----------|------------|------------|------------|----------|---------|
| Bubble    | O(n)       | O(n²)      | O(n²)      | O(1)     | sim     |
| Selection | O(n²)      | O(n²)      | O(n²)      | O(1)     | não     |
| Quick     | O(n log n) | O(n log n) | O(n²)      | O(log n) | não     |
| Merge     | O(n log n) | O(n log n) | O(n log n) | O(n)     | sim     |

## Como rodar

```bash
javac -d out src/ordenacao/*.java
java -cp out ordenacao.BubbleSort
java -cp out ordenacao.SelectionSort
java -cp out ordenacao.QuickSort
java -cp out ordenacao.MergeSort
java -cp out ordenacao.ParallelBubbleSort
java -cp out ordenacao.ParallelSelectionSort
java -cp out ordenacao.ParallelQuickSort
java -cp out ordenacao.ParallelMergeSort
```

Cada `main` faz:
1. um exemplo simples (antes/depois);
2. casos de borda (vazio, 1 elemento, repetidos, já ordenado, invertido, negativos);
3. testes aleatórios comparando com `Arrays.sort`;
4. medição de tempo — nos paralelos, sequencial × 1, 2, 4, ... threads.

As versões paralelas expõem `sort(int[] a, int threads)`, usam `ForkJoinPool`
e caem na versão sequencial abaixo de `LIMITE` elementos.

## Benchmark

```bash
java -cp out ordenacao.Benchmark   # ~2 min
python3 scripts/graficos.py        # resumo.csv + figuras
```

O `Benchmark` varia o tamanho da entrada (2.500 a 20.000), a natureza da entrada
(aleatória, ordenada, invertida, quase ordenada, com muitas repetições) e o número
de threads (1, 2, 4, 8, ... até os núcleos da máquina), com 5 amostras de cada
configuração, e grava os tempos crus em `resultados/tempos.csv`.

O `scripts/graficos.py` lê esse CSV, grava `resultados/resumo.csv` com média, desvio
padrão e speedup, e gera três figuras em `resultados/figuras/`:

| Figura | Conteúdo |
|--------|----------|
| `fig1_tempo_vs_n` | tempo × n (log-log) na versão serial — O(n²) vs O(n log n) |
| `fig2_speedup`    | speedup × threads, um painel por algoritmo |
| `fig3_entradas`   | serial × paralelo para cada natureza de entrada |
