package ordenacao;

import java.util.Arrays;
import java.util.Random;

/**
 * Bubble Sort
 *
 * Ideia: percorre o vetor varias vezes trocando pares vizinhos fora de ordem.
 * A cada passada, o maior elemento ainda nao ordenado "borbulha" ate o fim.
 *
 * Complexidade:
 *   - Pior caso / caso medio: O(n^2)
 *   - Melhor caso (vetor ja ordenado): O(n), gracas a flag "trocou"
 *   - Espaco: O(1) (in-place)
 *   - Estavel: sim
 */
public class BubbleSort {

    public static void sort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            boolean trocou = false;
            // os ultimos i elementos ja estao na posicao final
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    swap(a, j, j + 1);
                    trocou = true;
                }
            }
            // nenhuma troca na passada => vetor ja esta ordenado
            if (!trocou) break;
        }
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
        int[] exemplo = {64, 34, 25, 12, 22, 11, 90};
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

        // 4) tempo (so pra ter nocao do O(n^2))
        int n = 10_000;
        int[] grande = rng.ints(n, 0, 1_000_000).toArray();
        long inicio = System.nanoTime();
        sort(grande);
        long fim = System.nanoTime();
        System.out.printf("n = %d ordenado em %.2f ms%n", n, (fim - inicio) / 1e6);
    }
}
