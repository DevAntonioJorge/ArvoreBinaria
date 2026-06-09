import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import javax.swing.*;

public class Main {
    private static final DateTimeFormatter FORMATO_NOME_ARQUIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
    private static final int TAMANHO_MAXIMO_PREVIEW_HISTORICO = 35;

    private record OpcaoHistorico(Path caminho, String serializacao, String preview) {
        @Override
        public String toString() {
            return preview;
        }
    }

    private record SnapshotArvore(No raiz) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] opcoes = {"Árvore Binária", "AVL", "Red-Black"};
            int escolha = JOptionPane.showOptionDialog(
                    null,
                    "Escolha o tipo de árvore:",
                    "Menu inicial",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opcoes,
                    opcoes[0]
            );

            if (escolha < 0) {
                return;
            }

            ArvoreBinaria arvore;
            String titulo;
            if (escolha == 2) {
                arvore = new ArvoreRedBlack();
                titulo = "Árvore Red-Black";
            } else if (escolha == 1) {
                arvore = new ArvoreAVL();
                titulo = "Árvore AVL";
            } else {
                arvore = new ArvoreBinaria();
                titulo = "Árvore Binária";
            }

            final boolean[] houveAlteracao = {false};
            final java.util.Deque<SnapshotArvore> historicoEstados = new java.util.ArrayDeque<>();

            JFrame frame = new JFrame(titulo);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            PainelPrincipal painelArvore = new PainelPrincipal(arvore);

            Semaphore semaforoRotacao = new Semaphore(0);
            JLabel labelStatus = new JLabel(" ");
            JButton botaoProximo = new JButton("Próximo Passo");
            botaoProximo.setVisible(false);
            botaoProximo.addActionListener(e -> semaforoRotacao.release());

            arvore.setListenerRotacao(mensagem -> {
                try {
                    SwingUtilities.invokeLater(() -> {
                        labelStatus.setText(mensagem);
                        botaoProximo.setVisible(true);
                        registrarEstadoAtual(arvore, historicoEstados);
                        painelArvore.atualizarLayout();
                    });
                    semaforoRotacao.acquire();
                    SwingUtilities.invokeLater(() -> {
                        botaoProximo.setVisible(false);
                        labelStatus.setText(" ");
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });

            frame.add(painelArvore, BorderLayout.CENTER);

            JPanel painelStatus = new JPanel(new BorderLayout());
            painelStatus.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, java.awt.Color.LIGHT_GRAY),
                    javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            labelStatus.setFont(labelStatus.getFont().deriveFont(java.awt.Font.BOLD));
            painelStatus.add(labelStatus, BorderLayout.CENTER);
            painelStatus.add(botaoProximo, BorderLayout.EAST);
            frame.add(painelStatus, BorderLayout.SOUTH);

            JMenuBar barraMenu = new JMenuBar();
            JMenu menuArvore = new JMenu("Árvore");

            JMenuItem itemInserir = new JMenuItem("Inserir nó");
            AcaoComComponentes acaoInserir = criarAcaoInserir(frame, arvore, painelArvore, houveAlteracao, historicoEstados);
            itemInserir.addActionListener(acaoInserir);

            JMenuItem itemVisualizar = new JMenuItem("Visualizar árvore");
            itemVisualizar.addActionListener(e -> painelArvore.atualizarLayout());

            JMenuItem itemLimpar = new JMenuItem("Limpar árvore");
            AcaoComComponentes acaoLimpar = criarAcaoLimpar(frame, arvore, painelArvore, houveAlteracao, historicoEstados);
            itemLimpar.addActionListener(acaoLimpar);

            JButton botaoInserir = new JButton("Inserir nó");
            botaoInserir.addActionListener(acaoInserir);

            JButton botaoCaminhonamento = getBotaoCaminhonamento(frame, arvore);

            JButton botaoDesfazer = new JButton("Desfazer");
            botaoDesfazer.addActionListener(e -> desfazerUltimoEstado(frame, arvore, painelArvore, houveAlteracao, historicoEstados));

            JButton botaoLimpar = new JButton("Limpar árvore");
            botaoLimpar.addActionListener(acaoLimpar);

            JButton botaoInverter = new JButton("Inverter subárvores");
            botaoInverter.addActionListener(criarAcaoInverter(frame, arvore, painelArvore, houveAlteracao, historicoEstados));

            JButton botaoHistorico = new JButton("Histórico");
            botaoHistorico.addActionListener(criarAcaoHistorico(frame, arvore, painelArvore, houveAlteracao, historicoEstados));

            java.util.List<javax.swing.JComponent> componentesAcao = java.util.List.of(
                    itemInserir, itemLimpar, botaoInserir, botaoCaminhonamento, botaoDesfazer, botaoLimpar, botaoInverter, botaoHistorico
            );

            JPanel painelAcoes = new JPanel();
            painelAcoes.add(botaoInserir);
            painelAcoes.add(botaoCaminhonamento);
            painelAcoes.add(botaoDesfazer);
            painelAcoes.add(botaoLimpar);
            painelAcoes.add(botaoInverter);
            painelAcoes.add(botaoHistorico);
            frame.add(painelAcoes, BorderLayout.NORTH);

            menuArvore.add(itemInserir);
            menuArvore.add(itemVisualizar);
            menuArvore.add(itemLimpar);
            barraMenu.add(menuArvore);
            frame.setJMenuBar(barraMenu);

            frame.setSize(900, 500);
            frame.setLocationRelativeTo(null);
            painelArvore.atualizarLayout();
            registrarEstadoAtual(arvore, historicoEstados);
            frame.setVisible(true);

            acaoInserir.setComponentes(componentesAcao);
            acaoLimpar.setComponentes(componentesAcao);
        });
    }

    private interface AcaoComComponentes extends ActionListener {
        void setComponentes(java.util.List<javax.swing.JComponent> componentes);
    }

    private static AcaoComComponentes criarAcaoInserir(
            JFrame frame,
            ArvoreBinaria arvore,
            PainelPrincipal painelArvore,
            boolean[] houveAlteracao,
            java.util.Deque<SnapshotArvore> historicoEstados
    ) {
        return new AcaoComComponentes() {
            private java.util.List<javax.swing.JComponent> componentes;

            @Override
            public void setComponentes(java.util.List<javax.swing.JComponent> componentes) {
                this.componentes = componentes;
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                String entrada = JOptionPane.showInputDialog(frame, "Digite um ou mais valores inteiros separados por vírgula:", "Inserir nó", JOptionPane.QUESTION_MESSAGE);

                if (entrada == null || (entrada = entrada.trim()).isEmpty()) {
                    if (entrada != null) JOptionPane.showMessageDialog(frame, "Digite um valor válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (componentes != null) componentes.forEach(c -> c.setEnabled(false));

                final String entradaFinal = entrada;
                new Thread(() -> {
                    try {
                        var res = arvore.inserirMassa(entradaFinal, () -> SwingUtilities.invokeLater(painelArvore::atualizarLayout));

                        SwingUtilities.invokeLater(() -> {
                            processarResultadoInsercao(frame, arvore, painelArvore, houveAlteracao, historicoEstados, res);
                            habilitarComponentes(componentes);
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> habilitarComponentes(componentes));
                    }
                }).start();
            }
        };
    }

    private static void processarResultadoInsercao(
            JFrame frame,
            ArvoreBinaria arvore,
            PainelPrincipal painelArvore,
            boolean[] houveAlteracao,
            java.util.Deque<SnapshotArvore> historicoEstados,
            ArvoreBinaria.ResultadoProcessamento res
    ) {
        boolean apenasErros = res.inseridosComSucesso() == 0
                && res.jaExistiam() == 0
                && res.duplicadosNaEntrada() == 0
                && !res.erros().isEmpty();

        if (apenasErros) {
            JOptionPane.showMessageDialog(frame, "Valores inválidos: " + String.join(", ", res.erros()), "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (res.inseridosComSucesso() > 0) {
            houveAlteracao[0] = true;
            painelArvore.atualizarLayout();
            registrarEstadoAtual(arvore, historicoEstados);
        }

        String msg = montarMensagemResultadoInsercao(res);
        if (!msg.isEmpty()) {
            JOptionPane.showMessageDialog(frame, msg, "Resultado da inserção", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static String montarMensagemResultadoInsercao(ArvoreBinaria.ResultadoProcessamento res) {
        StringBuilder msg = new StringBuilder();

        if (res.jaExistiam() > 0) msg.append("Já existiam na árvore: ").append(res.jaExistiam()).append("\n");
        if (res.duplicadosNaEntrada() > 0) msg.append("Duplicados na entrada: ").append(res.duplicadosNaEntrada()).append("\n");
        if (!res.erros().isEmpty()) msg.append("Valores inválidos: ").append(String.join(", ", res.erros())).append("\n");

        return msg.toString().trim();
    }

    private static void habilitarComponentes(List<JComponent> componentes) {
        if (componentes != null) componentes.forEach(c -> c.setEnabled(true));
    }

    private static AcaoComComponentes criarAcaoLimpar(
            JFrame frame,
            ArvoreBinaria arvore,
            PainelPrincipal painelArvore,
            boolean[] houveAlteracao,
            java.util.Deque<SnapshotArvore> historicoEstados
    ) {
        return new AcaoComComponentes() {

            @Override
            public void setComponentes(java.util.List<javax.swing.JComponent> componentes) {
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int confirmacao = JOptionPane.showConfirmDialog(
                        frame,
                        "Deseja realmente limpar a árvore?",
                        "Confirmar limpeza",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirmacao != JOptionPane.YES_OPTION) {
                    return;
                }

                boolean deveMostrarResumoRotacoes = (arvore instanceof ArvoreAVL) || (arvore instanceof ArvoreRedBlack);
                String resumoRotacoes = deveMostrarResumoRotacoes ? arvore.resumoRotacoes() : null;
                String mensagemSucesso = null;
                String mensagemErro = null;

                if (arvore.raiz != null && houveAlteracao[0]) {
                    try {
                        String serializacao = arvore.serializarParenteses();
                        Path pastaArvores = Path.of("arvores");
                        Path pastaResumos = pastaArvores.resolve("resumos");
                        Files.createDirectories(pastaArvores);
                        Files.createDirectories(pastaResumos);

                        String dataHora = LocalDateTime.now().format(FORMATO_NOME_ARQUIVO);
                        Path caminhoArquivo = pastaArvores.resolve("arvore_" + dataHora + ".txt");
                        Files.writeString(caminhoArquivo, serializacao);

                        Path caminhoResumo = null;
                        if (deveMostrarResumoRotacoes) {
                            caminhoResumo = pastaResumos.resolve("resumo_rotacoes_" + dataHora + ".txt");
                            Files.writeString(caminhoResumo, resumoRotacoes);
                        }

                        mensagemSucesso = "Árvore salva em " + caminhoArquivo;
                        if (caminhoResumo != null) {
                            mensagemSucesso += "\nResumo salvo em " + caminhoResumo;
                        }
                    } catch (IOException ex) {
                        mensagemErro = "Não foi possível salvar a árvore em arquivo.";
                    }
                }

                arvore.limpar();
                houveAlteracao[0] = false;
                painelArvore.atualizarLayout();
                registrarEstadoAtual(arvore, historicoEstados);

                if (mensagemErro != null) {
                    JOptionPane.showMessageDialog(frame, mensagemErro, "Erro", JOptionPane.ERROR_MESSAGE);
                }

                if (deveMostrarResumoRotacoes) {
                    JOptionPane.showMessageDialog(frame, resumoRotacoes, "Resumo de rotações", JOptionPane.INFORMATION_MESSAGE);
                }

                if (mensagemSucesso != null) {
                    JOptionPane.showMessageDialog(frame, mensagemSucesso, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
    }

    private static ActionListener criarAcaoHistorico(
            JFrame frame,
            ArvoreBinaria arvore,
            PainelPrincipal painelArvore,
            boolean[] houveAlteracao,
            java.util.Deque<SnapshotArvore> historicoEstados
    ) {
        return e -> {
            Path pastaArvores = Path.of("arvores");

            try {
                Files.createDirectories(pastaArvores);
                List<Path> arquivos;
                try (var stream = Files.list(pastaArvores)) {
                    arquivos = stream
                            .filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".txt"))
                            .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                            .toList();
                }

                if (arquivos.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Não há árvores salvas no histórico.", "Histórico", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                OpcaoHistorico[] opcoes = new OpcaoHistorico[arquivos.size()];
                for (int i = 0; i < arquivos.size(); i++) {
                    Path caminho = arquivos.get(i);
                    String serializacao = Files.readString(caminho).trim();
                    String preview = abreviarTexto(serializacao);
                    opcoes[i] = new OpcaoHistorico(caminho, serializacao, preview);
                }

                OpcaoHistorico selecionado = (OpcaoHistorico) JOptionPane.showInputDialog(
                        frame,
                        "Selecione um arquivo de árvore:",
                        "Histórico",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opcoes,
                        opcoes[0]
                );

                if (selecionado == null) {
                    return;
                }

                arvore.carregarDeSerializacao(selecionado.serializacao);
                houveAlteracao[0] = false;
                painelArvore.atualizarLayout();
                registrarEstadoAtual(arvore, historicoEstados);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, "Arquivo de histórico inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Não foi possível carregar o histórico.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };
    }

    private static ActionListener criarAcaoInverter(
            JFrame frame,
            ArvoreBinaria arvore,
            PainelPrincipal painelArvore,
            boolean[] houveAlteracao,
            java.util.Deque<SnapshotArvore> historicoEstados
    ) {
        return e -> {
            if (arvore.raiz == null) {
                JOptionPane.showMessageDialog(frame, "A árvore está vazia.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            arvore.inverterSubarvores();
            houveAlteracao[0] = true;
            painelArvore.atualizarLayout();
            registrarEstadoAtual(arvore, historicoEstados);
        };
    }

    private static void desfazerUltimoEstado(
            JFrame frame,
            ArvoreBinaria arvore,
            PainelPrincipal painelArvore,
            boolean[] houveAlteracao,
            java.util.Deque<SnapshotArvore> historicoEstados
    ) {
        if (historicoEstados.size() <= 1) {
            JOptionPane.showMessageDialog(frame, "Não há ação para desfazer.", "Desfazer", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        historicoEstados.removeLast();
        SnapshotArvore snapshotAnterior = historicoEstados.peekLast();

        if (snapshotAnterior == null) {
            return;
        }

        arvore.raiz = copiarSubarvore(snapshotAnterior.raiz());
        houveAlteracao[0] = arvore.raiz != null;
        painelArvore.atualizarLayout();
    }

    private static void registrarEstadoAtual(ArvoreBinaria arvore, java.util.Deque<SnapshotArvore> historicoEstados) {
        if (arvore == null || historicoEstados == null) {
            return;
        }

        SnapshotArvore estadoAtual = new SnapshotArvore(copiarSubarvore(arvore.raiz));
        SnapshotArvore ultimo = historicoEstados.peekLast();

        if (ultimo != null && mesmaArvore(ultimo.raiz(), estadoAtual.raiz())) {
            return;
        }

        historicoEstados.addLast(estadoAtual);
    }

    private static No copiarSubarvore(No no) {
        if (no == null) {
            return null;
        }

        No copia = new No(no.valor);
        copia.altura = no.altura;
        copia.vermelho = no.vermelho;
        copia.esquerda = copiarSubarvore(no.esquerda);
        copia.direita = copiarSubarvore(no.direita);
        return copia;
    }

    private static boolean mesmaArvore(No a, No b) {
        if (a == b) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        return a.valor == b.valor
                && a.altura == b.altura
                && a.vermelho == b.vermelho
                && mesmaArvore(a.esquerda, b.esquerda)
                && mesmaArvore(a.direita, b.direita);
    }

    private static JButton getBotaoCaminhonamento(JFrame frame, ArvoreBinaria arvore) {
        JButton botaoCaminhonamento = new JButton("Caminhonamento");
        botaoCaminhonamento.addActionListener(e -> {
            String[] opcoes = {"LNR", "NLR", "LRN", "Largura"};
            int opcaoSelecionada = JOptionPane.showOptionDialog(
                    frame,
                    "Selecione o tipo de caminhamento:",
                    "Caminhonamento",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opcoes,
                    opcoes[0]
            );

            if (opcaoSelecionada < 0 || opcaoSelecionada >= opcoes.length) {
                return;
            }

            String tipo = opcoes[opcaoSelecionada];
            String sequencia = switch (tipo) {
                case "LNR" -> arvore.caminhamentoLNR();
                case "NLR" -> arvore.caminhamentoNLR();
                case "LRN" -> arvore.caminhamentoLRN();
                default -> arvore.caminhamentoLargura();
            };

            JPanel painelResultado = new JPanel(new GridLayout(2, 1, 0, 8));
            painelResultado.add(new JLabel("Sequência do caminhamento " + tipo + ":"));
            painelResultado.add(new JLabel(sequencia));

            JOptionPane.showMessageDialog(
                    frame,
                    painelResultado,
                    "Resultado do caminhamento",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        return botaoCaminhonamento;
    }

    private static String abreviarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return "(vazio)";
        }

        String normalizado = texto.trim();
        if (normalizado.length() <= Main.TAMANHO_MAXIMO_PREVIEW_HISTORICO) {
            return normalizado;
        }

        return normalizado.substring(0, Main.TAMANHO_MAXIMO_PREVIEW_HISTORICO) + "...";
    }
}
