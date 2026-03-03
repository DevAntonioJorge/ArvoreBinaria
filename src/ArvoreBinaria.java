
import java.util.ArrayList;
import java.util.List;

class ArvoreBinaria {
    No raiz;
    boolean existe(No no, int valor) {
        if (no == null) {
            return false;
        }
        if (no.valor == valor) {
            return true;
        }
        return existe(no.esquerda, valor) || existe(no.direita, valor);
    }


    void inserir(int valor) {
        if (existe(raiz, valor)) {
            System.out.println("Valor já existe na árvore");
            return;
        }

        No novo = new No(valor);

        if (raiz == null) {
            raiz = novo;
            return;
        }

        No atual = raiz;
        No pai = null;

        while (atual != null) {
            pai = atual;

            if (valor < atual.valor) {
                atual = atual.esquerda;
            } else {
                atual = atual.direita;
            }
        }

        if (valor < pai.valor) {
            pai.esquerda = novo;
        } else {
            pai.direita = novo;
        }
    }


    void mostrar(No no) {
        if (no == null) {
            System.out.println("(árvore vazia)");
            return;
        }

        int h = altura(no);
        int cell = larguraMax(no) + 1;

        List<No> nivel = new ArrayList<>();
        nivel.add(no);

        for (int level = 1; level <= h; level++) {
            int first = (int) Math.pow(2, h - level) - 1;
            int between = (int) Math.pow(2, h - level + 1) - 1;

            printSpaces(first * cell);

            List<No> prox = new ArrayList<>();
            for (int i = 0; i < nivel.size(); i++) {
                No atual = nivel.get(i);

                if (atual == null) {
                    printSpaces(cell);
                    prox.add(null);
                    prox.add(null);
                } else {
                    printCentered(String.valueOf(atual.valor), cell);
                    prox.add(atual.esquerda);
                    prox.add(atual.direita);
                }

                if (i < nivel.size() - 1) {
                    printSpaces(between * cell);
                }
            }
            System.out.println();
            nivel = prox;
        }
    }

    private int altura(No no) {
        if (no == null) return 0;
        return 1 + Math.max(altura(no.esquerda), altura(no.direita));
    }

    private int larguraMax(No no) {
        if (no == null) return 1;
        int aqui = String.valueOf(no.valor).length();
        return Math.max(aqui, Math.max(larguraMax(no.esquerda), larguraMax(no.direita)));
    }

    private void printSpaces(int n) {
        for (int i = 0; i < n; i++) System.out.print(" ");
    }

    private void printCentered(String s, int width) {
        int left = Math.max(0, (width - s.length()) / 2);
        int right = Math.max(0, width - s.length() - left);
        printSpaces(left);
        System.out.print(s);
        printSpaces(right);
    }
}
