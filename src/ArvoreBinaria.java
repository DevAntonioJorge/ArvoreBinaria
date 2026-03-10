import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class ArvoreBinaria {
    No raiz;

    String tipoEstrutural() {
        if (raiz == null) {
            return "Árvore vazia";
        }

        if (ehCheia()) {
            return "Cheia";
        }

        if (ehCompleta()) {
            return "Completa";
        }

        return "Não completa";
    }

    String serializarParenteses() {
        return serializarParenteses(raiz);
    }

    private String serializarParenteses(No no) {
        if (no == null) {
            return "()";
        }

        return "(" + no.valor + serializarParenteses(no.esquerda) + serializarParenteses(no.direita) + ")";
    }

    boolean inserir(int valor) {
        No novo = new No(valor);

        if (raiz == null) {
            raiz = novo;
            return true;
        }

        No atual = raiz;
        No pai = null;

        while (atual != null) {
            pai = atual;

            if (valor == atual.valor) {
                return false;
            }

            atual = (valor < atual.valor) ? atual.esquerda : atual.direita;
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

    private boolean ehCheia() {
        return ehCheia(raiz);
    }

    private boolean ehCheia(No no) {
        if (no == null) {
            return true;
        }

        if (no.esquerda == null && no.direita == null) {
            return true;
        }

        if (no.esquerda != null && no.direita != null) {
            return ehCheia(no.esquerda) && ehCheia(no.direita);
        }

        return false;
    }

    private boolean ehCompleta() {
        Deque<No> fila = new ArrayDeque<>();
        fila.add(raiz);
        boolean encontrouFilhoAusente = false;

        while (!fila.isEmpty()) {
            No atual = fila.remove();

            if (atual.esquerda != null) {
                if (encontrouFilhoAusente) {
                    return false;
                }
                fila.add(atual.esquerda);
            } else {
                encontrouFilhoAusente = true;
            }

            if (atual.direita != null) {
                if (encontrouFilhoAusente) {
                    return false;
                }
                fila.add(atual.direita);
            } else {
                encontrouFilhoAusente = true;
            }
        }

        return true;
    }

    String caminhamentoLNR() {
        List<Integer> valores = new ArrayList<>();
        percorrerLNR(raiz, valores);
        return formatarSequencia(valores);
    }

    String caminhamentoNLR() {
        List<Integer> valores = new ArrayList<>();
        percorrerNLR(raiz, valores);
        return formatarSequencia(valores);
    }

    String caminhamentoLRN() {
        List<Integer> valores = new ArrayList<>();
        percorrerLRN(raiz, valores);
        return formatarSequencia(valores);
    }

    String caminhamentoLargura() {
        if (raiz == null) {
            return "(árvore vazia)";
        }

        List<Integer> valores = new ArrayList<>();
        Deque<No> fila = new ArrayDeque<>();
        fila.add(raiz);

        while (!fila.isEmpty()) {
            No atual = fila.remove();
            valores.add(atual.valor);

            if (atual.esquerda != null) {
                fila.add(atual.esquerda);
            }

            if (atual.direita != null) {
                fila.add(atual.direita);
            }
        }

        return formatarSequencia(valores);
    }

    private void percorrerLNR(No no, List<Integer> valores) {
        if (no == null) {
            return;
        }

        percorrerLNR(no.esquerda, valores);
        valores.add(no.valor);
        percorrerLNR(no.direita, valores);
    }

    private void percorrerNLR(No no, List<Integer> valores) {
        if (no == null) {
            return;
        }

        valores.add(no.valor);
        percorrerNLR(no.esquerda, valores);
        percorrerNLR(no.direita, valores);
    }

    private void percorrerLRN(No no, List<Integer> valores) {
        if (no == null) {
            return;
        }

        percorrerLRN(no.esquerda, valores);
        percorrerLRN(no.direita, valores);
        valores.add(no.valor);
    }

    private String formatarSequencia(List<Integer> valores) {
        if (valores.isEmpty()) {
            return "(árvore vazia)";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.size(); i++) {
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(valores.get(i));
        }

        return sb.toString();
    }
}
