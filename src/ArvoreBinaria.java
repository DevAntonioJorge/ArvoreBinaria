
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


    boolean inserir(int valor) {
        if (existe(raiz, valor)) {
            return false;
        }

        No novo = new No(valor);

        if (raiz == null) {
            raiz = novo;
            return true;
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

        if (pai == null) {
            return false;
        }

        if (valor < pai.valor) {
            pai.esquerda = novo;
        } else {
            pai.direita = novo;
        }

        return true;
    }

    void limpar() {
        raiz = null;
    }

    String gerarVisualizacao() {
        if (raiz == null) {
            return "(árvore vazia)";
        }

        StringBuilder sb = new StringBuilder();
        int h = altura(raiz);
        int cell = larguraMax(raiz) + 1;

        List<No> nivel = new ArrayList<>();
        nivel.add(raiz);

        for (int level = 1; level <= h; level++) {
            int first = (int) Math.pow(2, h - level) - 1;
            int between = (int) Math.pow(2, h - level + 1) - 1;

            appendSpaces(sb, first * cell);

            List<No> prox = new ArrayList<>();
            for (int i = 0; i < nivel.size(); i++) {
                No atual = nivel.get(i);

                if (atual == null) {
                    appendSpaces(sb, cell);
                    prox.add(null);
                    prox.add(null);
                } else {
                    appendCentered(sb, String.valueOf(atual.valor), cell);
                    prox.add(atual.esquerda);
                    prox.add(atual.direita);
                }

                if (i < nivel.size() - 1) {
                    appendSpaces(sb, between * cell);
                }
            }
            sb.append(System.lineSeparator());
            nivel = prox;
        }

        return sb.toString();
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

    private void appendSpaces(StringBuilder sb, int n) {
        for (int i = 0; i < n; i++) {
            sb.append(' ');
        }
    }

    private void appendCentered(StringBuilder sb, String s, int width) {
        int left = Math.max(0, (width - s.length()) / 2);
        int right = Math.max(0, width - s.length() - left);
        appendSpaces(sb, left);
        sb.append(s);
        appendSpaces(sb, right);
    }
}
