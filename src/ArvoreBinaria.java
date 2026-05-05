import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class ArvoreBinaria {
    public record ResultadoProcessamento(
            int inseridosComSucesso,
            int jaExistiam,
            int duplicadosNaEntrada,
            List<String> erros
    ) {}

    public ResultadoProcessamento inserirMassa(String entrada, Runnable aoInserir) {
        if (entrada == null || entrada.trim().isEmpty()) {
            return new ResultadoProcessamento(0, 0, 0, List.of());
        }

        String[] partes = entrada.split(",");
        java.util.Set<Integer> valoresProcessados = new java.util.HashSet<>();
        int inseridosComSucesso = 0;
        int jaExistiam = 0;
        int duplicadosNaEntrada = 0;
        List<String> erros = new ArrayList<>();

        for (String parte : partes) {
            parte = parte.trim();
            if (parte.isEmpty()) continue;

            try {
                int valor = Integer.parseInt(parte);
                if (!valoresProcessados.add(valor)) {
                    duplicadosNaEntrada++;
                    continue;
                }

                if (inserir(valor)) {
                    inseridosComSucesso++;
                    if (aoInserir != null) aoInserir.run();
                } else {
                    jaExistiam++;
                }
            } catch (NumberFormatException ex) {
                erros.add(parte);
            }
        }

        return new ResultadoProcessamento(inseridosComSucesso, jaExistiam, duplicadosNaEntrada, erros);
    }

    private static class Cursor {
        int indice;
    }

    private static class InsercaoResultado {
        boolean inseriu;
    }

    public interface ListenerRotacao {
        void aoRotacionar(String mensagem);
    }

    No raiz;
    private boolean balanceamentoAtivo;
    private ListenerRotacao listenerRotacao;
    private List<Integer> historicoInsercoes;
    private List<String> historicoRotacoes;

    ArvoreBinaria() {
        this.balanceamentoAtivo = false;
        this.historicoInsercoes = new ArrayList<>();
        this.historicoRotacoes = new ArrayList<>();
    }

    void setListenerRotacao(ListenerRotacao listener) {
        this.listenerRotacao = listener;
    }

    private void notificarRotacao(String mensagem) {
        historicoRotacoes.add(mensagem);
        if (listenerRotacao != null) {
            listenerRotacao.aoRotacionar(mensagem);
        }
    }

    void setBalanceamentoAtivo(boolean ativo) {
        if (this.balanceamentoAtivo == ativo) {
            return;
        }

        this.balanceamentoAtivo = ativo;
        if (ativo && raiz != null) {
            raiz = balancearSubarvore(raiz);
        }
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

    int fatorBalanceamentoDo(No no) {
        if (no == null) {
            return 0;
        }

        return fatorBalanceamento(no);
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

        raiz = inserirNo(raiz, valor, resultado);
        if (resultado.inseriu) {
            historicoInsercoes.add(valor);
        }
        return resultado.inseriu;
    }

    void limpar() {
        raiz = null;
        historicoInsercoes.clear();
        historicoRotacoes.clear();
    }

    void inverterSubarvores() {
        if (raiz == null) {
            return;
        }

        inverterSubarvores(raiz.esquerda);
        inverterSubarvores(raiz.direita);
        atualizarAltura(raiz);
    }

    private boolean ehCheia() {
        return ehCheia(raiz);
    }

    private void inverterSubarvores(No no) {
        if (no == null) {
            return;
        }

        No filhoEsquerdo = no.esquerda;
        no.esquerda = no.direita;
        no.direita = filhoEsquerdo;

        inverterSubarvores(no.esquerda);
        inverterSubarvores(no.direita);
        atualizarAltura(no);
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

    private No inserirNo(No no, int valor, InsercaoResultado resultado) {
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

        if (balanceamentoAtivo) {
            return balancearNo(no);
        }

        return no;
    }

    private No balancearSubarvore(No no) {
        if (no == null) {
            return null;
        }

        no.esquerda = balancearSubarvore(no.esquerda);
        no.direita = balancearSubarvore(no.direita);
        atualizarAltura(no);
        return balancearNo(no);
    }

    private No balancearNo(No no) {
        int fatorBalanceamento = fatorBalanceamento(no);

        if (fatorBalanceamento > 1) {
            if (fatorBalanceamento(no.esquerda) < 0) {
                notificarRotacao("Rotação Dupla Direita (E-D): Aplicando Rotação Esquerda no filho à esquerda (" + no.esquerda.valor + ") de " + no.valor);
                no.esquerda = rotacaoEsquerda(no.esquerda);
            }
            notificarRotacao("Aplicando Rotação Direita em " + no.valor);
            return rotacaoDireita(no);
        }

        if (fatorBalanceamento < -1) {
            if (fatorBalanceamento(no.direita) > 0) {
                notificarRotacao("Rotação Dupla Esquerda (D-E): Aplicando Rotação Direita no filho à direita (" + no.direita.valor + ") de " + no.valor);
                no.direita = rotacaoDireita(no.direita);
            }
            notificarRotacao("Aplicando Rotação Esquerda em " + no.valor);
            return rotacaoEsquerda(no);
        }

        return no;
    }

    private int fatorBalanceamento(No no) {
        if (no == null) {
            return 0;
        }

        return altura(no.esquerda) - altura(no.direita);
    }

    private No rotacaoDireita(No no) {
        No novaRaiz = no.esquerda;
        No subarvoreTemporaria = novaRaiz.direita;

        novaRaiz.direita = no;
        no.esquerda = subarvoreTemporaria;

        atualizarAltura(no);
        atualizarAltura(novaRaiz);
        return novaRaiz;
    }

    private No rotacaoEsquerda(No no) {
        No novaRaiz = no.direita;
        No subarvoreTemporaria = novaRaiz.esquerda;

        novaRaiz.esquerda = no;
        no.direita = subarvoreTemporaria;

        atualizarAltura(no);
        atualizarAltura(novaRaiz);
        return novaRaiz;
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

    List<Integer> getHistoricoInsercoes() {
        return new ArrayList<>(historicoInsercoes);
    }

    List<String> getHistoricoRotacoes() {
        return new ArrayList<>(historicoRotacoes);
    }
}
