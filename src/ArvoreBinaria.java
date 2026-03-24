import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class ArvoreBinaria {
    private static class Cursor {
        int indice;
    }

    private static class InsercaoResultado {
        boolean inseriu;
    }

    No raiz;
    private boolean rebalanceamentoAtivo = true;

    boolean isRebalanceamentoAtivo() {
        return rebalanceamentoAtivo;
    }

    void setRebalanceamentoAtivo(boolean rebalanceamentoAtivo) {
        this.rebalanceamentoAtivo = rebalanceamentoAtivo;
    }

    void carregarDeSerializacao(String serializacao) {
        if (serializacao == null) {
            throw new IllegalArgumentException("Serialização inválida.");
        }

        String texto = serializacao.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("Serialização vazia.");
        }

        Cursor cursor = new Cursor();
        No novaRaiz = desserializarParenteses(texto, cursor);
        avancarEspacos(texto, cursor);

        if (cursor.indice != texto.length()) {
            throw new IllegalArgumentException("Formato inválido de serialização.");
        }

        raiz = novaRaiz;
    }

    int nivelMaximoArvore() {
        return profundidadeArvore();
    }

    int profundidadeArvore() {
        if (raiz == null) {
            return -1;
        }

        return profundidadeMaxima(raiz, 0);
    }

    int alturaArvore() {
        if (raiz == null) {
            return -1;
        }

        return alturaNo(raiz);
    }

    int nivelDoNo(No no) {
        return profundidadeDoNo(no);
    }

    int profundidadeDoNo(No no) {
        return profundidadeDoNo(raiz, no, 0);
    }

    int alturaDoNo(No no) {
        if (no == null) {
            return -1;
        }

        return alturaNo(no);
    }

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
        InsercaoResultado resultado = new InsercaoResultado();

        if (rebalanceamentoAtivo) {
            raiz = inserirAVL(raiz, valor, resultado);
        } else {
            raiz = inserirSemRebalanceamento(raiz, valor, resultado);
        }

        return resultado.inseriu;
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

    private int profundidadeMaxima(No no, int profundidadeAtual) {
        if (no == null) {
            return -1;
        }

        int profundidadeEsquerda = profundidadeMaxima(no.esquerda, profundidadeAtual + 1);
        int profundidadeDireita = profundidadeMaxima(no.direita, profundidadeAtual + 1);

        return Math.max(profundidadeAtual, Math.max(profundidadeEsquerda, profundidadeDireita));
    }

    private int profundidadeDoNo(No atual, No alvo, int profundidadeAtual) {
        if (atual == null || alvo == null) {
            return -1;
        }

        if (atual == alvo) {
            return profundidadeAtual;
        }

        int profundidadeEsquerda = profundidadeDoNo(atual.esquerda, alvo, profundidadeAtual + 1);
        if (profundidadeEsquerda != -1) {
            return profundidadeEsquerda;
        }

        return profundidadeDoNo(atual.direita, alvo, profundidadeAtual + 1);
    }

    private int alturaNo(No no) {
        return altura(no);
    }

    private No desserializarParenteses(String texto, Cursor cursor) {
        avancarEspacos(texto, cursor);

        if (cursor.indice >= texto.length() || texto.charAt(cursor.indice) != '(') {
            throw new IllegalArgumentException("Formato inválido de serialização.");
        }
        cursor.indice++;

        avancarEspacos(texto, cursor);
        if (cursor.indice < texto.length() && texto.charAt(cursor.indice) == ')') {
            cursor.indice++;
            return null;
        }

        int valor = lerInteiro(texto, cursor);
        No no = new No(valor);
        no.esquerda = desserializarParenteses(texto, cursor);
        no.direita = desserializarParenteses(texto, cursor);
        atualizarAltura(no);

        avancarEspacos(texto, cursor);
        if (cursor.indice >= texto.length() || texto.charAt(cursor.indice) != ')') {
            throw new IllegalArgumentException("Formato inválido de serialização.");
        }
        cursor.indice++;

        return no;
    }

    private No inserirAVL(No no, int valor, InsercaoResultado resultado) {
        if (no == null) {
            resultado.inseriu = true;
            return new No(valor);
        }

        if (valor < no.valor) {
            no.esquerda = inserirAVL(no.esquerda, valor, resultado);
        } else if (valor > no.valor) {
            no.direita = inserirAVL(no.direita, valor, resultado);
        } else {
            return no;
        }

        atualizarAltura(no);
        return rebalancear(no);
    }

    private No inserirSemRebalanceamento(No no, int valor, InsercaoResultado resultado) {
        if (no == null) {
            resultado.inseriu = true;
            return new No(valor);
        }

        if (valor < no.valor) {
            no.esquerda = inserirSemRebalanceamento(no.esquerda, valor, resultado);
        } else if (valor > no.valor) {
            no.direita = inserirSemRebalanceamento(no.direita, valor, resultado);
        } else {
            return no;
        }

        atualizarAltura(no);
        return no;
    }

    private int altura(No no) {
        return no == null ? -1 : no.altura;
    }

    private void atualizarAltura(No no) {
        if (no == null) {
            return;
        }

        no.altura = 1 + Math.max(altura(no.esquerda), altura(no.direita));
    }

    private int fatorBalanceamento(No no) {
        if (no == null) {
            return 0;
        }

        return altura(no.esquerda) - altura(no.direita);
    }

    private No rebalancear(No no) {
        int fator = fatorBalanceamento(no);

        if (fator > 1) {
            if (fatorBalanceamento(no.esquerda) < 0) {
                no.esquerda = rotacaoEsquerda(no.esquerda);
            }
            return rotacaoDireita(no);
        }

        if (fator < -1) {
            if (fatorBalanceamento(no.direita) > 0) {
                no.direita = rotacaoDireita(no.direita);
            }
            return rotacaoEsquerda(no);
        }

        return no;
    }

    private No rotacaoDireita(No no) {
        No novaRaiz = no.esquerda;
        No subArvoreDireita = novaRaiz.direita;

        novaRaiz.direita = no;
        no.esquerda = subArvoreDireita;

        atualizarAltura(no);
        atualizarAltura(novaRaiz);
        return novaRaiz;
    }

    private No rotacaoEsquerda(No no) {
        No novaRaiz = no.direita;
        No subArvoreEsquerda = novaRaiz.esquerda;

        novaRaiz.esquerda = no;
        no.direita = subArvoreEsquerda;

        atualizarAltura(no);
        atualizarAltura(novaRaiz);
        return novaRaiz;
    }

    private int lerInteiro(String texto, Cursor cursor) {
        avancarEspacos(texto, cursor);

        int inicio = cursor.indice;
        if (cursor.indice < texto.length() && texto.charAt(cursor.indice) == '-') {
            cursor.indice++;
        }

        int inicioDigitos = cursor.indice;
        while (cursor.indice < texto.length() && Character.isDigit(texto.charAt(cursor.indice))) {
            cursor.indice++;
        }

        if (inicioDigitos == cursor.indice) {
            throw new IllegalArgumentException("Valor numérico inválido na serialização.");
        }

        String numero = texto.substring(inicio, cursor.indice);
        try {
            return Integer.parseInt(numero);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor numérico inválido na serialização.");
        }
    }

    private void avancarEspacos(String texto, Cursor cursor) {
        while (cursor.indice < texto.length() && Character.isWhitespace(texto.charAt(cursor.indice))) {
            cursor.indice++;
        }
    }
}
