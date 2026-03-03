class ArvoreBinaria {
    No raiz;

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
}
