import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
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
    private static final double FATOR_ZOOM = 1.1;
    private static final double ZOOM_MANUAL_MIN = 0.5;
    private static final double ZOOM_MANUAL_MAX = 4.0;

    private double zoomManual = 1.0;
    private double panX = 0;
    private double panY = 0;
    private double ultimoFitEscala = 1.0;
    private double ultimoFitDeslocamentoX = 0;
    private double ultimoFitDeslocamentoY = 0;
    private Rectangle areaLegenda = new Rectangle(0, 0, 0, 0);
    private Point ultimoPontoArraste;
    private final Map<No, int[]> posicoesUltimoDesenho = new HashMap<>();
    private final Map<Integer, int[]> posicoesAnterioresPorValor = new HashMap<>();
    private final Map<Integer, int[]> posicoesAtuaisPorValor = new HashMap<>();
    private javax.swing.Timer timerAnimacao;
    private double progressoAnimacao = 1.0; // 1.0 significa sem animação (estado final)
    private static final int DURACAO_ANIMACAO_MS = 600;
    private static final int FRAME_RATE_MS = 16; // aprox 60 fps

    public PainelPrincipal(ArvoreBinaria arvore) {
        this.arvore = arvore;
        setBackground(Color.WHITE);
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        setPreferredSize(new Dimension(LARGURA_BASE, ALTURA_BASE));

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ultimoPontoArraste = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (ultimoPontoArraste == null) {
                    return;
                }

                int dx = e.getX() - ultimoPontoArraste.x;
                int dy = e.getY() - ultimoPontoArraste.y;
                panX += dx;
                panY += dy;
                ultimoPontoArraste = e.getPoint();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ultimoPontoArraste = null;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                No noSelecionado = encontrarNoNoPontoTela(e.getX(), e.getY());
                if (noSelecionado == null) {
                    return;
                }

                int nivel = arvore.nivelDoNo(noSelecionado);
                int profundidade = arvore.profundidadeDoNo(noSelecionado);
                int altura = arvore.alturaDoNo(noSelecionado);
                int fatorBalanceamento = arvore.fatorBalanceamentoDo(noSelecionado);

                String mensagem = "Nó: " + noSelecionado.valor
                        + "\nNível do nó: " + nivel
                        + "\nProfundidade do nó: " + profundidade
                        + "\nAltura do nó: " + altura
                        + "\nFator de balanceamento: " + fatorBalanceamento;

                JOptionPane.showMessageDialog(
                        PainelPrincipal.this,
                        mensagem,
                        "Informações do nó",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double zoomAnterior = zoomManual;

                if (e.getWheelRotation() < 0) {
                    zoomManual *= FATOR_ZOOM;
                } else {
                    zoomManual /= FATOR_ZOOM;
                }

                zoomManual = Math.clamp(zoomManual, ZOOM_MANUAL_MIN, ZOOM_MANUAL_MAX);
                if (zoomManual == zoomAnterior) {
                    return;
                }

                double escalaAnterior = ultimoFitEscala * zoomAnterior;
                double escalaNova = ultimoFitEscala * zoomManual;

                if (escalaAnterior <= 0 || escalaNova <= 0) {
                    repaint();
                    return;
                }

                double txAnterior = ultimoFitDeslocamentoX + panX;
                double tyAnterior = ultimoFitDeslocamentoY + panY;

                double xLogico = (e.getX() - txAnterior) / escalaAnterior;
                double yLogico = (e.getY() - tyAnterior) / escalaAnterior;

                double txNovo = e.getX() - (xLogico * escalaNova);
                double tyNovo = e.getY() - (yLogico * escalaNova);

                panX = txNovo - ultimoFitDeslocamentoX;
                panY = tyNovo - ultimoFitDeslocamentoY;

                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    void atualizarLayout() {
        if (progressoAnimacao < 1.0 && timerAnimacao != null && timerAnimacao.isRunning()) {
            timerAnimacao.stop();
        }

        salvarPosicoesAtuaisComoAnteriores();
        progressoAnimacao = 0.0;

        if (timerAnimacao == null) {
            timerAnimacao = new javax.swing.Timer(FRAME_RATE_MS, e -> {
                progressoAnimacao += (double) FRAME_RATE_MS / DURACAO_ANIMACAO_MS;
                if (progressoAnimacao >= 1.0) {
                    progressoAnimacao = 1.0;
                    timerAnimacao.stop();
                }
                repaint();
            });
        }
        timerAnimacao.start();

        revalidate();
        repaint();
    }

    private void salvarPosicoesAtuaisComoAnteriores() {
        posicoesAnterioresPorValor.clear();
        posicoesAnterioresPorValor.putAll(posicoesAtuaisPorValor);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        posicoesUltimoDesenho.clear();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2f));

        desenharLegenda(g2);

        if (arvore.raiz == null) {
            g2.dispose();
            return;
        }

        Map<No, int[]> posicoes = new HashMap<>();
        MetricasLayout metricas = new MetricasLayout();
        preencherPosicoesEmOrdem(arvore.raiz, 0, metricas, posicoes);
        posicoesUltimoDesenho.putAll(posicoes);

        // Atualiza o mapa de posições por valor para animação
        posicoesAtuaisPorValor.clear();
        for (Map.Entry<No, int[]> entry : posicoes.entrySet()) {
            posicoesAtuaisPorValor.put(entry.getKey().valor, entry.getValue());
        }

        int larguraLogica = calcularLarguraLogica(metricas.quantidadeNos);
        int alturaLogica = calcularAlturaLogica(metricas.profundidadeMaxima + 1);

        double larguraDisponivel = Math.max(1, getWidth());
        double alturaDisponivel = Math.max(1, getHeight());
        double escalaX = larguraDisponivel / larguraLogica;
        double escalaY = alturaDisponivel / alturaLogica;
        double escalaFit = Math.min(1.0, Math.min(escalaX, escalaY));

        if (Double.isNaN(escalaFit) || Double.isInfinite(escalaFit) || escalaFit <= 0) {
            escalaFit = 1.0;
        }

        ultimoFitEscala = escalaFit;

        double deslocamentoX = (getWidth() - (larguraLogica * escalaFit)) / 2.0;
        double deslocamentoY = (getHeight() - (alturaLogica * escalaFit)) / 2.0;

        ultimoFitDeslocamentoX = deslocamentoX;
        ultimoFitDeslocamentoY = deslocamentoY;

        double escalaFinal = escalaFit * zoomManual;

        Area areaDesenho = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
        if (!areaLegenda.isEmpty()) {
            areaDesenho.subtract(new Area(areaLegenda));
        }

        g2.setClip(areaDesenho);
        g2.translate(deslocamentoX + panX, deslocamentoY + panY);
        g2.scale(escalaFinal, escalaFinal);

        desenharNo(g2, arvore.raiz, posicoes);

        g2.dispose();
    }

    private void desenharLegenda(Graphics2D g2) {
        String legendaTipo = "Tipo da árvore: " + arvore.tipoEstrutural();
        String legendaMetricas = montarLegendaMetricasArvore();
        Font fonteOriginal = g2.getFont();
        Font fonteLegenda = fonteOriginal.deriveFont(Font.BOLD, 12f);

        g2.setFont(fonteLegenda);
        int larguraTextoTipo = g2.getFontMetrics().stringWidth(legendaTipo);
        int larguraTextoMetricas = g2.getFontMetrics().stringWidth(legendaMetricas);
        int larguraTexto = Math.max(larguraTextoTipo, larguraTextoMetricas);
        int alturaTexto = g2.getFontMetrics().getHeight();

        int x = 10;
        int y = 10;
        int larguraCaixa = larguraTexto + 12;
        int alturaCaixa = (alturaTexto * 2) + 8;
        areaLegenda = new Rectangle(x, y, larguraCaixa, alturaCaixa);

        g2.setColor(new Color(255, 255, 255, 220));
        g2.fillRoundRect(x, y, larguraCaixa, alturaCaixa, 10, 10);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(x, y, larguraCaixa, alturaCaixa, 10, 10);

        int linhaBasePrimeira = y + 4 + g2.getFontMetrics().getAscent();
        int linhaBaseSegunda = linhaBasePrimeira + alturaTexto;

        g2.drawString(legendaTipo, x + 6, linhaBasePrimeira);
        g2.drawString(legendaMetricas, x + 6, linhaBaseSegunda);
        g2.setFont(fonteOriginal);
    }

    private String montarLegendaMetricasArvore() {
        if (arvore.raiz == null) {
            return "Nível: - | Profundidade: - | Altura: -";
        }

        int nivel = arvore.nivelMaximoArvore();
        int profundidade = arvore.profundidadeArvore();
        int altura = arvore.alturaArvore();

        return "Nível: " + nivel + " | Profundidade: " + profundidade + " | Altura: " + altura;
    }

    private No encontrarNoNoPontoTela(int xTela, int yTela) {
        if (arvore.raiz == null || posicoesUltimoDesenho.isEmpty()) {
            return null;
        }

        if (areaLegenda.contains(xTela, yTela)) {
            return null;
        }

        double escalaFinal = ultimoFitEscala * zoomManual;
        if (escalaFinal <= 0) {
            return null;
        }

        double xLogico = (xTela - (ultimoFitDeslocamentoX + panX)) / escalaFinal;
        double yLogico = (yTela - (ultimoFitDeslocamentoY + panY)) / escalaFinal;

        for (Map.Entry<No, int[]> entry : posicoesUltimoDesenho.entrySet()) {
            int[] posicao = entry.getValue();
            double dx = xLogico - posicao[0];
            double dy = yLogico - posicao[1];

            if ((dx * dx) + (dy * dy) <= (RAIO_NO * RAIO_NO)) {
                return entry.getKey();
            }
        }

        return null;
    }

    private void desenharNo(Graphics2D g2, No no, Map<No, int[]> posicoes) {
        int[] posAlvo = posicoes.get(no);
        int xAlvo = posAlvo[0];
        int yAlvo = posAlvo[1];

        int x, y;
        if (progressoAnimacao < 1.0 && posicoesAnterioresPorValor.containsKey(no.valor)) {
            int[] posAnt = posicoesAnterioresPorValor.get(no.valor);
            x = (int) (posAnt[0] + (xAlvo - posAnt[0]) * progressoAnimacao);
            y = (int) (posAnt[1] + (yAlvo - posAnt[1]) * progressoAnimacao);
        } else {
            x = xAlvo;
            y = yAlvo;
        }

        if (no.esquerda != null) {
            int[] posFilhoAlvo = posicoes.get(no.esquerda);
            int xf, yf;
            if (progressoAnimacao < 1.0 && posicoesAnterioresPorValor.containsKey(no.esquerda.valor)) {
                int[] posAnt = posicoesAnterioresPorValor.get(no.esquerda.valor);
                xf = (int) (posAnt[0] + (posFilhoAlvo[0] - posAnt[0]) * progressoAnimacao);
                yf = (int) (posAnt[1] + (posFilhoAlvo[1] - posAnt[1]) * progressoAnimacao);
            } else {
                xf = posFilhoAlvo[0];
                yf = posFilhoAlvo[1];
            }
            g2.setColor(Color.GRAY);
            desenharArestaDiagonal(g2, x, y, xf, yf);
            desenharNo(g2, no.esquerda, posicoes);
        }

        if (no.direita != null) {
            int[] posFilhoAlvo = posicoes.get(no.direita);
            int xf, yf;
            if (progressoAnimacao < 1.0 && posicoesAnterioresPorValor.containsKey(no.direita.valor)) {
                int[] posAnt = posicoesAnterioresPorValor.get(no.direita.valor);
                xf = (int) (posAnt[0] + (posFilhoAlvo[0] - posAnt[0]) * progressoAnimacao);
                yf = (int) (posAnt[1] + (posFilhoAlvo[1] - posAnt[1]) * progressoAnimacao);
            } else {
                xf = posFilhoAlvo[0];
                yf = posFilhoAlvo[1];
            }
            g2.setColor(Color.GRAY);
            desenharArestaDiagonal(g2, x, y, xf, yf);
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
