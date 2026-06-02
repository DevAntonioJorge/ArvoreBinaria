class No {
    int valor;
    No esquerda;
    No direita;
    int altura;
    boolean vermelho;
    No(int valor) {
        this.valor = valor;
        this.esquerda = null;
        this.direita = null;
        this.altura = 0;
        this.vermelho = false;
    }
}