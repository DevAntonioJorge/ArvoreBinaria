import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

class PainelPrincipal extends JPanel {
    private static class MetricasLayout {
        int quantidadeNos;
        int profundidadeMaxima;
    }

    private final ArvoreBinaria arvore;
    private static final int RAIO_NO = 22;
    private static final int ALTURA_NIVEL = 90;
    private static final int MARGEM = 40;
    private static final int LARGURA_BASE = 900;
    private static final int ALTURA_BASE = 500;
    private static final int ESPACAMENTO_HORIZONTAL = 80;

    public PainelPrincipal(ArvoreBinaria arvore) {
        this.arvore = arvore;
        setBackground(Color.WHITE);
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        setPreferredSize(new Dimension(LARGURA_BASE, ALTURA_BASE));
    }

    void atualizarLayout() {
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
            g2.drawString("(árvore vazia)", 30, 50);
            g2.dispose();
            return;
        }

        Map<No, int[]> posicoes = new HashMap<>();
        MetricasLayout metricas = new MetricasLayout();
        preencherPosicoesEmOrdem(arvore.raiz, 0, metricas, posicoes);

        int larguraLogica = calcularLarguraLogica(metricas.quantidadeNos);
        int alturaLogica = calcularAlturaLogica(metricas.profundidadeMaxima + 1);

        double larguraDisponivel = Math.max(1, getWidth());
        double alturaDisponivel = Math.max(1, getHeight());
        double escalaX = larguraDisponivel / larguraLogica;
        double escalaY = alturaDisponivel / alturaLogica;
        double escala = Math.min(1.0, Math.min(escalaX, escalaY));

        if (Double.isNaN(escala) || Double.isInfinite(escala) || escala <= 0) {
            escala = 1.0;
        }

        double deslocamentoX = (getWidth() - (larguraLogica * escala)) / 2.0;
        double deslocamentoY = (getHeight() - (alturaLogica * escala)) / 2.0;

        g2.translate(deslocamentoX, deslocamentoY);
        g2.scale(escala, escala);

        desenharNo(g2, arvore.raiz, posicoes);

        g2.dispose();
    }

    private void desenharNo(Graphics2D g2, No no, Map<No, int[]> posicoes) {
        int[] posicaoNo = posicoes.get(no);
        int x = posicaoNo[0];
        int y = posicaoNo[1];

        if (no.esquerda != null) {
            int[] posicaoFilho = posicoes.get(no.esquerda);
            g2.setColor(Color.GRAY);
            desenharArestaDiagonal(g2, x, y, posicaoFilho[0], posicaoFilho[1]);
            desenharNo(g2, no.esquerda, posicoes);
        }

        if (no.direita != null) {
            int[] posicaoFilho = posicoes.get(no.direita);
            g2.setColor(Color.GRAY);
            desenharArestaDiagonal(g2, x, y, posicaoFilho[0], posicaoFilho[1]);
            desenharNo(g2, no.direita, posicoes);
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

    private void preencherPosicoesEmOrdem(No no, int profundidade, MetricasLayout metricas, Map<No, int[]> posicoes) {
        if (no == null) {
            return;
        }

        preencherPosicoesEmOrdem(no.esquerda, profundidade + 1, metricas, posicoes);

        int x = MARGEM + RAIO_NO + (metricas.quantidadeNos * ESPACAMENTO_HORIZONTAL);
        int y = MARGEM + RAIO_NO + (profundidade * ALTURA_NIVEL);
        posicoes.put(no, new int[] {x, y});
        metricas.quantidadeNos++;
        metricas.profundidadeMaxima = Math.max(metricas.profundidadeMaxima, profundidade);

        preencherPosicoesEmOrdem(no.direita, profundidade + 1, metricas, posicoes);
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

    private int calcularLarguraLogica(int quantidadeNos) {
        if (quantidadeNos <= 1) {
            return 220;
        }

        return (MARGEM * 2) + (RAIO_NO * 2) + ((quantidadeNos - 1) * ESPACAMENTO_HORIZONTAL);
    }

    private int calcularAlturaLogica(int alturaArvore) {
        return Math.max(220, (alturaArvore * ALTURA_NIVEL) + (MARGEM * 2));
    }
}
