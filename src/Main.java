import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
    private static class PainelArvore extends JPanel {
        private final ArvoreBinaria arvore;
        private static final int RAIO_NO = 22;
        private static final int ALTURA_NIVEL = 90;
        private static final int MARGEM = 40;

        PainelArvore(ArvoreBinaria arvore) {
            this.arvore = arvore;
            setBackground(Color.WHITE);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        }

        void atualizarLayout() {
            int altura = altura(arvore.raiz);
            int largura = Math.max(900, ((int) Math.pow(2, Math.max(altura, 1))) * 45);
            int alturaPainel = Math.max(500, altura * ALTURA_NIVEL + (MARGEM * 2));
            setPreferredSize(new Dimension(largura, alturaPainel));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2f));

            if (arvore.raiz == null) {
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(getFont().deriveFont(Font.PLAIN, 22f));
                g2.drawString("", 30, 50);
                g2.dispose();
                return;
            }

            int xRaiz = getWidth() / 2;
            int yRaiz = MARGEM;
            int gapInicial = Math.max(60, getWidth() / 4);
            desenharNo(g2, arvore.raiz, xRaiz, yRaiz, gapInicial);

            g2.dispose();
        }

        private void desenharNo(Graphics2D g2, No no, int x, int y, int gap) {
            if (no.esquerda != null) {
                int filhoX = x - gap;
                int filhoY = y + ALTURA_NIVEL;
                g2.setColor(Color.GRAY);
                desenharArestaDiagonal(g2, x, y, filhoX, filhoY);
                desenharNo(g2, no.esquerda, filhoX, filhoY, Math.max(35, gap / 2));
            }

            if (no.direita != null) {
                int filhoX = x + gap;
                int filhoY = y + ALTURA_NIVEL;
                g2.setColor(Color.GRAY);
                desenharArestaDiagonal(g2, x, y, filhoX, filhoY);
                desenharNo(g2, no.direita, filhoX, filhoY, Math.max(35, gap / 2));
            }

            g2.setColor(Color.WHITE);
            g2.fillOval(x - RAIO_NO, y - RAIO_NO, RAIO_NO * 2, RAIO_NO * 2);
            g2.setColor(Color.BLACK);
            g2.drawOval(x - RAIO_NO, y - RAIO_NO, RAIO_NO * 2, RAIO_NO * 2);

            String valor = String.valueOf(no.valor);
            int larguraTexto = g2.getFontMetrics().stringWidth(valor);
            int ascent = g2.getFontMetrics().getAscent();
            g2.drawString(valor, x - (larguraTexto / 2), y + (ascent / 2) - 2);
        }

        private void desenharArestaDiagonal(Graphics2D g2, int xPai, int yPai, int xFilho, int yFilho) {
            double dx = xFilho - xPai;
            double dy = yFilho - yPai;
            double distancia = Math.hypot(dx, dy);

            if (distancia == 0) {
                return;
            }

            double ux = dx / distancia;
            double uy = dy / distancia;

            int xOrigem = (int) Math.round(xPai + ux * RAIO_NO);
            int yOrigem = (int) Math.round(yPai + uy * RAIO_NO);
            int xDestino = (int) Math.round(xFilho - ux * RAIO_NO);
            int yDestino = (int) Math.round(yFilho - uy * RAIO_NO);

            g2.drawLine(xOrigem, yOrigem, xDestino, yDestino);
        }

        private int altura(No no) {
            if (no == null) {
                return 0;
            }
            return 1 + Math.max(altura(no.esquerda), altura(no.direita));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ArvoreBinaria arvore = new ArvoreBinaria();

            JFrame frame = new JFrame("Árvore Binária");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            PainelArvore painelArvore = new PainelArvore(arvore);
            frame.add(new JScrollPane(painelArvore), BorderLayout.CENTER);

            JMenuBar barraMenu = new JMenuBar();
            JMenu menuArvore = new JMenu("Árvore");

            JMenuItem itemInserir = new JMenuItem("Inserir nó");
            itemInserir.addActionListener(e -> {
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
            });

            JMenuItem itemVisualizar = new JMenuItem("Visualizar árvore");
            itemVisualizar.addActionListener(e -> painelArvore.atualizarLayout());

            JMenuItem itemLimpar = new JMenuItem("Limpar árvore");
            itemLimpar.addActionListener(e -> {
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
            });

            JButton botaoInserir = new JButton("Inserir nó");
            botaoInserir.addActionListener(itemInserir.getActionListeners()[0]);

            JButton botaoLimpar = new JButton("Limpar árvore");
            botaoLimpar.addActionListener(itemLimpar.getActionListeners()[0]);

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
