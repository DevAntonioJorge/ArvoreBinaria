class ArvoreRedBlack extends ArvoreBinaria {

    ArvoreRedBlack() {
        super();
        setBalanceamentoAtivo();
    }

    @Override
    boolean inserir(int valor) {
        InsercaoResultado resultado = new InsercaoResultado();
        raiz = inserirRB(raiz, valor, resultado);
        raiz.vermelho = false; // A raiz sempre deve ser preta
        return resultado.inseriu;
    }

    private No inserirRB(No no, int valor, InsercaoResultado resultado) {
        // 1. Inserção padrão de Árvore Binária de Busca (BST)
        if (no == null) {
            resultado.inseriu = true;
            No novo = new No(valor);
            novo.vermelho = true; // Todo nó novo nasce vermelho
            return novo;
        }

        if (valor < no.valor) {
            no.esquerda = inserirRB(no.esquerda, valor, resultado);
        } else if (valor > no.valor) {
            no.direita = inserirRB(no.direita, valor, resultado);
        } else {
            return no; // Valor já existe
        }

        // 2. REBALANCEAMENTO (Na volta da recursão - Bottom-Up)

        // CASO A: O nó atual tem dois filhos vermelhos (Tio Vermelho)
        // Inverte as cores e joga o "problema" para o pai resolver acima na recursão
        if (isVermelho(no.esquerda) && isVermelho(no.direita)) {
            inverterCores(no);
        }
        // CASO B: Casos de desalinhamento (Tio Preto) -> Usando else if para evitar rotações duplas na mesma passada
        else {
            // Caso Esquerda-Direita: Filho esquerdo é vermelho e o neto direito é vermelho
            if (isVermelho(no.esquerda) && isVermelho(no.esquerda.direita)) {
                no.esquerda =  rotacaoEsquerdaRB(no.esquerda);
                no = rotacaoDireitaRB(no);
            }
            // Caso Direita-Esquerda: Filho direito é vermelho e o neto esquerdo é vermelho
            else if (isVermelho(no.direita) && isVermelho(no.direita.esquerda)) {
                no.direita = rotacaoDireitaRB(no.direita);
                no =  rotacaoEsquerdaRB(no);
            }
            // Caso Esquerda-Esquerda: Filho esquerdo e neto esquerdo são vermelhos
            else if (isVermelho(no.esquerda) && isVermelho(no.esquerda.esquerda)) {
                no = rotacaoDireitaRB(no);
            }
            // Caso Direita-Direita: Filho direito e neto direito são vermelhos
            else if (isVermelho(no.direita) && isVermelho(no.direita.direita)) {
                no =  rotacaoEsquerdaRB(no);
            }
        }

        atualizarAltura(no);
        return no;
    }

    private boolean isVermelho(No no) {
        if (no == null) return false;
        return no.vermelho;
    }

    private void inverterCores(No no) {
        no.vermelho = true;
        if (no.esquerda != null) no.esquerda.vermelho = false;
        if (no.direita != null) no.direita.vermelho = false;
    }

    private No  rotacaoEsquerdaRB(No no) {
        notificarRotacao("Red-Black: Rotação Esquerda em " + no.valor);
        No x = no.direita;
        No subarvoreTemporaria = x.esquerda;
        no.direita = x.esquerda;
        x.esquerda = no;

        x.vermelho = no.vermelho;
        no.vermelho = true;

        atualizarAltura(no);
        atualizarAltura(x);
        registrarRotacao("Rotação Esquerda (Red-Black)", no, x, subarvoreTemporaria);
        return x;
    }

    private No  rotacaoDireitaRB(No no) {
        notificarRotacao("Red-Black: Rotação Direita em " + no.valor);
        No x = no.esquerda;
        No subarvoreTemporaria = x.direita;
        no.esquerda = x.direita;
        x.direita = no;

        x.vermelho = no.vermelho;
        no.vermelho = true;

        atualizarAltura(no);
        atualizarAltura(x);
        registrarRotacao("Rotação Direita (Red-Black)", no, x, subarvoreTemporaria);
        return x;
    }

    @Override
    public String tipoEstrutural() {
        return "Árvore Red-Black";
    }
}