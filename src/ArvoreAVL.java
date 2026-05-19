class ArvoreAVL extends ArvoreBinaria {

    ArvoreAVL() {
        super();
        this.balanceamentoAtivo = true;
    }

    @Override
    protected No inserirNo(No no, int valor, InsercaoResultado resultado) {
        if (no == null) {
            resultado.inseriu = true;
            return new No(valor);
        }

        if (valor < no.valor) {
            no.esquerda = inserirNo(no.esquerda, valor, resultado);
        } else if (valor > no.valor) {
            no.direita = inserirNo(no.direita, valor, resultado);
        } else {
            return no;
        }

        atualizarAltura(no);
        return balancearNo(no);
    }

    @Override
    protected No balancearNo(No no) {
        int fator = fatorBalanceamento(no);

        if (fator > 1) {
            if (fatorBalanceamento(no.esquerda) < 0) {
                notificarRotacao("Rotação Dupla Direita (E-D): Aplicando Rotação Esquerda no filho à esquerda (" + no.esquerda.valor + ") de " + no.valor);
                no.esquerda = rotacaoEsquerda(no.esquerda);
            }
            notificarRotacao("Aplicando Rotação Direita em " + no.valor);
            return rotacaoDireita(no);
        }

        if (fator < -1) {
            if (fatorBalanceamento(no.direita) > 0) {
                notificarRotacao("Rotação Dupla Esquerda (D-E): Aplicando Rotação Direita no filho à direita (" + no.direita.valor + ") de " + no.valor);
                no.direita = rotacaoDireita(no.direita);
            }
            notificarRotacao("Aplicando Rotação Esquerda em " + no.valor);
            return rotacaoEsquerda(no);
        }

        return no;
    }

    @Override
    public String tipoEstrutural() {
        return "Árvore AVL";
    }
}
