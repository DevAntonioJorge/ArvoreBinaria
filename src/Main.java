import java.util.Scanner;

void main() {
    Scanner scanner = new Scanner(System.in);
    ArvoreBinaria arvoreBinaria = new ArvoreBinaria();


    label:
        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1 - Inserir nó");
            System.out.println("2 - Mostrar árvore");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");


            String opcao = scanner.next();


            switch (opcao) {
                case "1":
                    while(true) {
                        System.out.print("Digite um valor(Digite -1 para sair): ");
                        int valor = scanner.nextInt();
                        if (valor == -1) {
                            break;
                        }
                        arvoreBinaria.inserir(valor);
                    }
                    break;
                case "2":
                    System.out.println("\nEstrutura da árvore:");
                    arvoreBinaria.mostrar(arvoreBinaria.raiz);
                    break;
                case "0":
                    break label;
                default:
                    System.out.println("Opção inválida");
                    break;
            }
        }
}

