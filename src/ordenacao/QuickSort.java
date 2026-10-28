package ordenacao;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Quick Sort
 *
 * Ideia (dividir e conquistar):
 *   1. Escolhe um pivo.
 *   2. Particiona: tudo menor que o pivo vai pra esquerda, maior pra direita.
 *   3. Ordena recursivamente as duas metades.
 *
 * Detalhes desta implementacao:
 *   - Pivo ALEATORIO: evita o pior caso O(n^2) em vetores ja ordenados.
 *     Usa ThreadLocalRandom pra a versao paralela nao disputar o mesmo gerador.
 *   - Particao de Lomuto (mais facil de entender e provar correta).
 *   - Recursao so na parte menor + loop na maior => pilha O(log n).
 *
 * Complexidade:
 *   - Caso medio (esperado): O(n log n)
 *   - Pior caso: O(n^2) (muito improvavel com pivo aleatorio)
 *   - Espaco: O(log n) da pilha de recursao
 *   - Estavel: nao
 */
public class QuickSort {

    public static void sort(int[] a) {
        quickSort(a, 0, a.length - 1);
    }

    static void quickSort(int[] a, int lo, int hi) {
        while (lo < hi) {
            int p = partition(a, lo, hi);
            // recursao no lado menor, itera no lado maior
            if (p - lo < hi - p) {
                quickSort(a, lo, p - 1);
                lo = p + 1;
            } else {
                quickSort(a, p + 1, hi);
                hi = p - 1;
            }
        }
    }

    /**
     * Particao de Lomuto com pivo aleatorio.
     * Ao final: a[lo..i-1] < pivo, a[i] == pivo, a[i+1..hi] >= pivo.
     * Retorna i (posicao final do pivo).
     */
    static int partition(int[] a, int lo, int hi) {
        int r = lo + ThreadLocalRandom.current().nextInt(hi - lo + 1);
        swap(a, r, hi);              // coloca o pivo no fim
        int pivo = a[hi];

        int i = lo;                  // fronteira dos elementos < pivo
        for (int j = lo; j < hi; j++) {
            if (a[j] < pivo) {
                swap(a, i, j);
                i++;
            }
        }
        swap(a, i, hi);              // pivo vai pra posicao final
        return i;
    }

    private static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    private static boolean estaOrdenado(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        // 1) exemplo simples
        int[] exemplo = {10, 80, 30, 90, 40, 50, 70};
        System.out.println("Antes:  " + Arrays.toString(exemplo));
        sort(exemplo);
        System.out.println("Depois: " + Arrays.toString(exemplo));

        // 2) casos de borda
        int[][] bordas = { {}, {7}, {2, 1}, {5, 5, 5, 5}, {1, 2, 3, 4, 5}, {5, 4, 3, 2, 1}, {-3, 0, -1, 8, -10} };
        for (int[] caso : bordas) {
            int[] copia = caso.clone();
            sort(copia);
            System.out.printf("%-20s -> %-20s %s%n",
                    Arrays.toString(caso), Arrays.toString(copia), estaOrdenado(copia) ? "OK" : "FALHOU");
        }

        // 3) teste aleatorio: compara com Arrays.sort
        Random rng = new Random(42);
        int testes = 1000;
        for (int t = 0; t < testes; t++) {
            int[] v = rng.ints(rng.nextInt(50), -100, 100).toArray();
            int[] esperado = v.clone();
            Arrays.sort(esperado);
            sort(v);
            if (!Arrays.equals(v, esperado)) {
                System.out.println("ERRO no teste " + t);
                return;
            }
        }
        System.out.println(testes + " testes aleatorios passaram.");

        // 4) tempo: O(n log n) aguenta n bem maior que Bubble/Selection
        int n = 1_000_000;
        int[] grande = rng.ints(n, 0, 1_000_000).toArray();
        long inicio = System.nanoTime();
        sort(grande);
        long fim = System.nanoTime();
        System.out.printf("n = %d ordenado em %.2f ms%n", n, (fim - inicio) / 1e6);

        // vetor ja ordenado: com pivo fixo seria O(n^2), com pivo aleatorio continua rapido
        inicio = System.nanoTime();
        sort(grande);
        fim = System.nanoTime();
        System.out.printf("n = %d (ja ordenado) em %.2f ms%n", n, (fim - inicio) / 1e6);
    }
}
