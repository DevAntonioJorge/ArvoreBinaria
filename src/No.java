class No {

    int valor;
    int altura;

    boolean vermelho;

    No esquerda;
    No direita;
    No pai;

    No(int valor) {
        this.valor = valor;
        this.altura = 0;
        this.vermelho = true; // novos nós nascem vermelhos
    }
}