# Algoritmos de Ordenação em Java

Cada algoritmo fica em uma classe própria, com uma `main` que testa a implementação.

```
src/ordenacao/
├── BubbleSort.java
├── SelectionSort.java
├── QuickSort.java
├── MergeSort.java
├── ParallelQuickSort.java   (Fork/Join, reaproveita QuickSort)
└── ParallelMergeSort.java   (Fork/Join, reaproveita MergeSort)
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
java -cp out ordenacao.ParallelQuickSort
java -cp out ordenacao.ParallelMergeSort
```

Cada `main` faz:
1. um exemplo simples (antes/depois);
2. casos de borda (vazio, 1 elemento, repetidos, já ordenado, invertido, negativos);
3. 1000 testes aleatórios comparando com `Arrays.sort`;
4. medição de tempo para ver a diferença entre O(n²) e O(n log n).
