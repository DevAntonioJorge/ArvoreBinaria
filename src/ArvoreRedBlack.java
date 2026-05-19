class ArvoreRedBlack extends ArvoreBinaria {

    ArvoreRedBlack() {
        super();
        setBalanceamentoAtivo();
    }

    @Override
    boolean inserir(int valor) {
        InsercaoResultado resultado = new InsercaoResultado();
        raiz = inserirRB(raiz, valor, resultado);
        raiz.vermelho = false;
        return resultado.inseriu;
    }

    private No inserirRB(No no, int valor, InsercaoResultado resultado) {
        if (no == null) {
            resultado.inseriu = true;
            No novo = new No(valor);
            novo.vermelho = true;
            return novo;
        }

        if (valor < no.valor) {
            no.esquerda = inserirRB(no.esquerda, valor, resultado);
        } else if (valor > no.valor) {
            no.direita = inserirRB(no.direita, valor, resultado);
        } else {
            return no;
        }

        if (isVermelho(no.direita) && !isVermelho(no.esquerda)) {
            no = rotacaoEsquerdaRB(no);
        }
        if (isVermelho(no.esquerda) && isVermelho(no.esquerda.esquerda)) {
            no = rotacaoDireitaRB(no);
        }
        if (isVermelho(no.esquerda) && isVermelho(no.direita)) {
            inverterCores(no);
        }

        atualizarAltura(no);
        return no;
    }

    private boolean isVermelho(No no) {
        if (no == null) return false;
        return no.vermelho;
    }

    private void inverterCores(No no) {
        no.vermelho = !no.vermelho;
        if (no.esquerda != null) no.esquerda.vermelho = !no.esquerda.vermelho;
        if (no.direita != null) no.direita.vermelho = !no.direita.vermelho;
    }

    private No rotacaoEsquerdaRB(No no) {
        notificarRotacao("Red-Black: Rotação Esquerda em " + no.valor);
        No x = no.direita;
        no.direita = x.esquerda;
        x.esquerda = no;
        x.vermelho = no.vermelho;
        no.vermelho = true;
        atualizarAltura(no);
        atualizarAltura(x);
        return x;
    }

    private No rotacaoDireitaRB(No no) {
        notificarRotacao("Red-Black: Rotação Direita em " + no.valor);
        No x = no.esquerda;
        no.esquerda = x.direita;
        x.direita = no;
        x.vermelho = no.vermelho;
        no.vermelho = true;
        atualizarAltura(no);
        atualizarAltura(x);
        return x;
    }
    
    @Override
    public String tipoEstrutural() {
        return "Árvore Red-Black";
    }
}
