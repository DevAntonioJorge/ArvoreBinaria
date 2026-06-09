import java.util.IdentityHashMap;
import java.util.Map;

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

        Map<No, No> pais = new IdentityHashMap<>();
        No atual = raiz;
        No pai = null;

        while (atual != null) {
            pais.put(atual, pai);

            if (valor < atual.valor) {
                pai = atual;
                atual = atual.esquerda;
            } else if (valor > atual.valor) {
                pai = atual;
                atual = atual.direita;
            } else {
                return false;
            }
        }

        No novo = new No(valor);
        novo.vermelho = true;
        pais.put(novo, pai);

        if (valor < pai.valor) {
            pai.esquerda = novo;
        } else {
            pai.direita = novo;
        }

        corrigirInsercao(novo, pais);
        raiz.vermelho = false;
        recalcularAlturas(raiz);
        return true;
    }

    private void corrigirInsercao(No no, Map<No, No> pais) {
        while (no != raiz && isVermelho(pais.get(no))) {
            No pai = pais.get(no);
            No avo = pais.get(pai);

            if (avo == null) {
                break;
            }

            if (pai == avo.esquerda) {
                No tio = avo.direita;

                if (isVermelho(tio)) {
                    pai.vermelho = false;
                    tio.vermelho = false;
                    avo.vermelho = true;
                    no = avo;
                    continue;
                }

                if (no == pai.direita) {
                    no = pai;
                    rotacaoEsquerdaRB(no, pais);
                    pai = pais.get(no);
                    avo = pais.get(pai);
                }

                if (pai != null) {
                    pai.vermelho = false;
                }
                if (avo != null) {
                    avo.vermelho = true;
                    rotacaoDireitaRB(avo, pais);
                }
            } else {
                No tio = avo.esquerda;

                if (isVermelho(tio)) {
                    pai.vermelho = false;
                    tio.vermelho = false;
                    avo.vermelho = true;
                    no = avo;
                    continue;
                }

                if (no == pai.esquerda) {
                    no = pai;
                    rotacaoDireitaRB(no, pais);
                    pai = pais.get(no);
                    avo = pais.get(pai);
                }

                if (pai != null) {
                    pai.vermelho = false;
                }
                if (avo != null) {
                    avo.vermelho = true;
                    rotacaoEsquerdaRB(avo, pais);
                }
            }
        }
    }

    private boolean isVermelho(No no) {
        return no != null && no.vermelho;
    }

    private No rotacaoEsquerdaRB(No no, Map<No, No> pais) {
        notificarRotacao("Red-Black: Rotação Esquerda em " + no.valor);

        No x = no.direita;
        No subarvoreTemporaria = x.esquerda;
        No pai = pais.get(no);

        no.direita = subarvoreTemporaria;
        if (subarvoreTemporaria != null) {
            pais.put(subarvoreTemporaria, no);
        }

        x.esquerda = no;
        pais.put(no, x);
        pais.put(x, pai);

        if (pai == null) {
            raiz = x;
        } else if (pai.esquerda == no) {
            pai.esquerda = x;
        } else {
            pai.direita = x;
        }

        x.vermelho = no.vermelho;
        no.vermelho = true;
        registrarRotacao("Rotação Esquerda (Red-Black)", no, x, subarvoreTemporaria);
        return x;
    }

    private No rotacaoDireitaRB(No no, Map<No, No> pais) {
        notificarRotacao("Red-Black: Rotação Direita em " + no.valor);

        No x = no.esquerda;
        No subarvoreTemporaria = x.direita;
        No pai = pais.get(no);

        no.esquerda = subarvoreTemporaria;
        if (subarvoreTemporaria != null) {
            pais.put(subarvoreTemporaria, no);
        }

        x.direita = no;
        pais.put(no, x);
        pais.put(x, pai);

        if (pai == null) {
            raiz = x;
        } else if (pai.esquerda == no) {
            pai.esquerda = x;
        } else {
            pai.direita = x;
        }

        x.vermelho = no.vermelho;
        no.vermelho = true;
        registrarRotacao("Rotação Direita (Red-Black)", no, x, subarvoreTemporaria);
        return x;
    }

    private void recalcularAlturas(No no) {
        if (no == null) {
            return;
        }

        recalcularAlturas(no.esquerda);
        recalcularAlturas(no.direita);
        atualizarAltura(no);
    }

    @Override
    public String tipoEstrutural() {
        return "Árvore Red-Black";
    }
}
