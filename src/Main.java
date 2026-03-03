import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ArvoreBinaria arvore = new ArvoreBinaria();

            JFrame frame = new JFrame("Árvore Binária");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            PainelPrincipal painelArvore = new PainelPrincipal(arvore);
            frame.add(new JScrollPane(painelArvore), BorderLayout.CENTER);

            JMenuBar barraMenu = new JMenuBar();
            JMenu menuArvore = new JMenu("Árvore");

            JMenuItem itemInserir = new JMenuItem("Inserir nó");
            ActionListener acaoInserir = e -> {
                String entrada = JOptionPane.showInputDialog(frame, "Digite um valor inteiro:", "Inserir nó", JOptionPane.QUESTION_MESSAGE);

                if (entrada == null) {
                    return;
                }

                entrada = entrada.trim();
                if (entrada.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Digite um valor válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    int valor = Integer.parseInt(entrada);
                    boolean inseriu = arvore.inserir(valor);

                    if (!inseriu) {
                        JOptionPane.showMessageDialog(frame, "Esse valor já existe na árvore.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    painelArvore.atualizarLayout();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Use apenas números inteiros.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            };
            itemInserir.addActionListener(acaoInserir);

            JMenuItem itemVisualizar = new JMenuItem("Visualizar árvore");
            itemVisualizar.addActionListener(e -> painelArvore.atualizarLayout());

            JMenuItem itemLimpar = new JMenuItem("Limpar árvore");
            ActionListener acaoLimpar = e -> {
                int confirmacao = JOptionPane.showConfirmDialog(
                        frame,
                        "Deseja realmente limpar a árvore?",
                        "Confirmar limpeza",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirmacao == JOptionPane.YES_OPTION) {
                    arvore.limpar();
                    painelArvore.atualizarLayout();
                }
            };
            itemLimpar.addActionListener(acaoLimpar);

            JButton botaoInserir = new JButton("Inserir nó");
            botaoInserir.addActionListener(acaoInserir);

            JButton botaoLimpar = new JButton("Limpar árvore");
            botaoLimpar.addActionListener(acaoLimpar);

            JPanel painelAcoes = new JPanel();
            painelAcoes.add(botaoInserir);
            painelAcoes.add(botaoLimpar);
            frame.add(painelAcoes, BorderLayout.NORTH);

            menuArvore.add(itemInserir);
            menuArvore.add(itemVisualizar);
            menuArvore.add(itemLimpar);
            barraMenu.add(menuArvore);
            frame.setJMenuBar(barraMenu);

            frame.setSize(900, 500);
            frame.setLocationRelativeTo(null);
            painelArvore.atualizarLayout();
            frame.setVisible(true);
        });
    }
}
