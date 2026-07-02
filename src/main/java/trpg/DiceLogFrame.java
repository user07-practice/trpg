package trpg;

import javax.swing.*;
import java.awt.*;

public class DiceLogFrame extends JFrame {
    private JTextArea logArea;

    public DiceLogFrame() {
        super("📜 セッションログ");
        setSize(450, 500);
        setLocation(600, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Meiryo", Font.PLAIN, 13));
        logArea.setBackground(new Color(255, 255, 255));

        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // ログを追加するメソッド
    public void addLog(String text) {
        // ★末尾の改行を「\n\n」にすることで、ログとログの間に自動で1行分の「心地いいスキマ」を作ります！
        logArea.append(text + "\n\n");

        // 自動スクロール
        SwingUtilities.invokeLater(() -> {
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
