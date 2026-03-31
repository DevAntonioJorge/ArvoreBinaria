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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Main {
    private static final DateTimeFormatter FORMATO_NOME_ARQUIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
    private static final int TAMANHO_MAXIMO_PREVIEW_HISTORICO = 35;

    private record OpcaoHistorico(Path caminho, String serializacao, String preview) {
        @Override
        public String toString() {
            return preview;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ArvoreBinaria arvore = new ArvoreBinaria();
            final boolean[] houveAlteracao = {false};

            JFrame frame = new JFrame("Árvore Binária");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            PainelPrincipal painelArvore = new PainelPrincipal(arvore);
            frame.add(painelArvore, BorderLayout.CENTER);

            JMenuBar barraMenu = new JMenuBar();
            JMenu menuArvore = new JMenu("Árvore");

            JMenuItem itemInserir = new JMenuItem("Inserir nó");
            ActionListener acaoInserir = criarAcaoInserir(frame, arvore, painelArvore, houveAlteracao);
            itemInserir.addActionListener(acaoInserir);

            JMenuItem itemVisualizar = new JMenuItem("Visualizar árvore");
            itemVisualizar.addActionListener(e -> painelArvore.atualizarLayout());

            JMenuItem itemLimpar = new JMenuItem("Limpar árvore");
            ActionListener acaoLimpar = criarAcaoLimpar(frame, arvore, painelArvore, houveAlteracao);
            itemLimpar.addActionListener(acaoLimpar);

            JButton botaoInserir = new JButton("Inserir nó");
            botaoInserir.addActionListener(acaoInserir);

            JButton botaoCaminhonamento = getBotaoCaminhonamento(frame, arvore);

            JButton botaoLimpar = new JButton("Limpar árvore");
            botaoLimpar.addActionListener(acaoLimpar);

            JButton botaoHistorico = new JButton("Histórico");
            botaoHistorico.addActionListener(criarAcaoHistorico(frame, arvore, painelArvore, houveAlteracao));

            JPanel painelAcoes = new JPanel();
            painelAcoes.add(botaoInserir);
            painelAcoes.add(botaoCaminhonamento);
            painelAcoes.add(botaoLimpar);
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
            frame.setVisible(true);
        });
    }

    private static ActionListener criarAcaoInserir(
            JFrame frame,
            ArvoreBinaria arvore,
            PainelPrincipal painelArvore,
            boolean[] houveAlteracao
    ) {
        return e -> {
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

                houveAlteracao[0] = true;
                painelArvore.atualizarLayout();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Use apenas números inteiros.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };
    }

    private static ActionListener criarAcaoLimpar(
            JFrame frame,
            ArvoreBinaria arvore,
            PainelPrincipal painelArvore,
            boolean[] houveAlteracao
    ) {
        return e -> {
            int confirmacao = JOptionPane.showConfirmDialog(
                    frame,
                    "Deseja realmente limpar a árvore?",
                    "Confirmar limpeza",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if ((confirmacao == JOptionPane.YES_OPTION) && (arvore.raiz != null && houveAlteracao[0])) try {
                String serializacao = arvore.serializarParenteses();
                Path pastaArvores = Path.of("arvores");
                Files.createDirectories(pastaArvores);

                String dataHora = LocalDateTime.now().format(FORMATO_NOME_ARQUIVO);
                Path caminhoArquivo = pastaArvores.resolve("arvore_" + dataHora + ".txt");
                Files.writeString(caminhoArquivo, serializacao);
                JOptionPane.showMessageDialog(frame, "Árvore salva em " + caminhoArquivo, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Não foi possível salvar a árvore em arquivo.", "Erro", JOptionPane.ERROR_MESSAGE);
            } finally {
                arvore.limpar();
                houveAlteracao[0] = false;
                painelArvore.atualizarLayout();
            }
        };
    }

    private static ActionListener criarAcaoHistorico(
            JFrame frame,
            ArvoreBinaria arvore,
            PainelPrincipal painelArvore,
            boolean[] houveAlteracao
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
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, "Arquivo de histórico inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Não foi possível carregar o histórico.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };
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
