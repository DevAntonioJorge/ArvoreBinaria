class ArvoreRedBlack extends ArvoreBinaria {

    ArvoreRedBlack() {
        super();
        setBalanceamentoAtivo();
    }

    @Override
    boolean inserir(int valor) {

        if (raiz == null) {
            raiz = new No(valor);
            raiz.vermelho = false;
            return true;
        }

        No novo = inserirBST(valor);

        if (novo == null) {
            return false;
        }

        corrigirInsercao(novo);

        raiz.vermelho = false;

        return true;
    }

    private No inserirBST(int valor) {

        No atual = raiz;
        No pai = null;

        while (atual != null) {

            pai = atual;

            if (valor < atual.valor) {
                atual = atual.esquerda;
            }
            else if (valor > atual.valor) {
                atual = atual.direita;
            }
            else {
                return null;
            }
        }

        No novo = new No(valor);
        novo.pai = pai;

        if (valor < pai.valor) {
            pai.esquerda = novo;
        }
        else {
            pai.direita = novo;
        }

        atualizarAlturasAteRaiz(novo);

        return novo;
    }

    private void corrigirInsercao(No no) {

        while (no != raiz &&
               no.pai != null &&
               no.pai.vermelho) {

            No pai = no.pai;
            No avo = pai.pai;

            if (avo == null) {
                break;
            }

            if (pai == avo.esquerda) {

                No tio = avo.direita;

                // Caso 1 - tio vermelho
                if (isVermelho(tio)) {

                    pai.vermelho = false;
                    tio.vermelho = false;
                    avo.vermelho = true;

                    no = avo;
                }
                else {

                    // Caso 2 - Esquerda-Direita
                    if (no == pai.direita) {

                        no = pai;
                        rotacaoEsquerdaRB(no);

                        pai = no.pai;
                        avo = pai.pai;
                    }

                    // Caso 3 - Esquerda-Esquerda
                    pai.vermelho = false;
                    avo.vermelho = true;

                    rotacaoDireitaRB(avo);
                }
            }
            else {

                No tio = avo.esquerda;

                // Caso 1 espelhado
                if (isVermelho(tio)) {

                    pai.vermelho = false;
                    tio.vermelho = false;
                    avo.vermelho = true;

                    no = avo;
                }
                else {

                    // Caso 2 - Direita-Esquerda
                    if (no == pai.esquerda) {

                        no = pai;
                        rotacaoDireitaRB(no);

                        pai = no.pai;
                        avo = pai.pai;
                    }

                    // Caso 3 - Direita-Direita
                    pai.vermelho = false;
                    avo.vermelho = true;

                    rotacaoEsquerdaRB(avo);
                }
            }
        }

        raiz.vermelho = false;
    }

    private boolean isVermelho(No no) {
        return no != null && no.vermelho;
    }

    private void rotacaoEsquerdaRB(No x) {

        notificarRotacao(
                "Red-Black: Rotação Esquerda em " + x.valor
        );

        No y = x.direita;
        No subarvoreMovida = y.esquerda;

        x.direita = subarvoreMovida;

        if (subarvoreMovida != null) {
            subarvoreMovida.pai = x;
        }

        y.pai = x.pai;

        if (x.pai == null) {
            raiz = y;
        }
        else if (x == x.pai.esquerda) {
            x.pai.esquerda = y;
        }
        else {
            x.pai.direita = y;
        }

        y.esquerda = x;
        x.pai = y;

        atualizarAltura(x);
        atualizarAltura(y);

        registrarRotacao(
                "Rotação Esquerda (Red-Black)",
                x,
                y,
                subarvoreMovida
        );
    }

    private void rotacaoDireitaRB(No y) {

        notificarRotacao(
                "Red-Black: Rotação Direita em " + y.valor
        );

        No x = y.esquerda;
        No subarvoreMovida = x.direita;

        y.esquerda = subarvoreMovida;

        if (subarvoreMovida != null) {
            subarvoreMovida.pai = y;
        }

        x.pai = y.pai;

        if (y.pai == null) {
            raiz = x;
        }
        else if (y == y.pai.esquerda) {
            y.pai.esquerda = x;
        }
        else {
            y.pai.direita = x;
        }

        x.direita = y;
        y.pai = x;

        atualizarAltura(y);
        atualizarAltura(x);

        registrarRotacao(
                "Rotação Direita (Red-Black)",
                y,
                x,
                subarvoreMovida
        );
    }

    private void atualizarAlturasAteRaiz(No no) {

        while (no != null) {
            atualizarAltura(no);
            no = no.pai;
        }
    }

    @Override
    public String tipoEstrutural() {
        return "Árvore Red-Black";
    }
}