class ArvoreBinaria {
    No raiz;

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
}
