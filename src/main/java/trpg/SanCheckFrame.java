package trpg;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class SanCheckFrame extends JFrame {
    private CharacterFrame mainFrame;
    private DiceLogFrame logFrame;

    private JTextField sanTargetField;   // 現在のSAN値
    private JTextField successLoseField; // 成功時の減少（例: 0 や 1）
    private JTextField failLoseField;    // 失敗時の減少（例: 1D6 や 2） 👈 コンボボックスからテキストフィールドに変更！

    private Random random = new Random();

    public SanCheckFrame(CharacterFrame mainFrame, DiceLogFrame logFrame) {
        super("汎用SANチェックツール");
        this.mainFrame = mainFrame;
        this.logFrame = logFrame;

        setSize(520, 160); // コンボボックスが消えてスッキリしたので、横幅を少し詰めました
        setResizable(false);
        setLocation(mainFrame.getX() + mainFrame.getWidth() + 10, mainFrame.getY() + 180);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 12));

        // 1. 現在のSAN値
        int currentSan = mainFrame.getSanValue();
        sanTargetField = new JTextField(String.valueOf(currentSan), 4);
        sanTargetField.setHorizontalAlignment(JTextField.CENTER);
        sanTargetField.setFont(new Font("Meiryo", Font.PLAIN, 12));

        // 2. 成功時の減少値
        successLoseField = new JTextField("0", 3);
        successLoseField.setHorizontalAlignment(JTextField.CENTER);
        successLoseField.setFont(new Font("Meiryo", Font.PLAIN, 12));

        // 3. 失敗時の減少値（初期値は定番の 1D6 にしておきます）
        failLoseField = new JTextField("1D6", 6);
        failLoseField.setHorizontalAlignment(JTextField.CENTER);
        failLoseField.setFont(new Font("Meiryo", Font.PLAIN, 12));

        // ロールボタン
        JButton rollBtn = new JButton("☠️ SANチェック！");
        rollBtn.setFont(new Font("Meiryo", Font.BOLD, 12));
        rollBtn.setBackground(new Color(255, 240, 240));
        rollBtn.addActionListener(e -> rollSanCheck());

        // UIの配置
        JLabel label1 = new JLabel("現在のSAN値:");
        label1.setFont(new Font("Meiryo", Font.PLAIN, 12));
        add(label1);
        add(sanTargetField);

        JLabel label2 = new JLabel(" ─ 成功時:");
        label2.setFont(new Font("Meiryo", Font.PLAIN, 12));
        add(label2);
        add(successLoseField);

        JLabel label3 = new JLabel(" / 失敗時:");
        label3.setFont(new Font("Meiryo", Font.PLAIN, 12));
        add(label3);
        add(failLoseField); // 👈 スッキリ合流！

        add(new JLabel(" ➔ "));
        add(rollBtn);

        setVisible(true);
    }

    private void rollSanCheck() {
        String charName = mainFrame.getCharacterName();
        String targetText = sanTargetField.getText().trim();

        // 大文字小文字のブレをなくすため、一律で大文字（1d6 ➔ 1D6）に変換します
        String succText = successLoseField.getText().trim().toUpperCase();
        String failText = failLoseField.getText().trim().toUpperCase();

        if (targetText.isEmpty() || succText.isEmpty() || failText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "入力欄が空欄になっています！");
            return;
        }

        int sanTarget;
        try {
            sanTarget = Integer.parseInt(targetText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "SAN値は半角数値で入力してください。");
            return;
        }

        // ─── 1. 1D100ロール ───
        int diceResult = random.nextInt(100) + 1;
        boolean isSuccess = diceResult <= sanTarget;
        String resultStr = isSuccess ? "【成功👍】" : "【失敗😭】";

        // ─── 2. 減少値の解析＆計算 ───
        int loseValue = 0;
        String loseDetail = "";

        // 判定結果によって、解析するテキストを切り替える
        String modeText = isSuccess ? succText : failText;

        // 【条件分岐】文字の中に「D」が含まれているか？
        if (modeText.contains("D")) {
            // 「1D6」などのダイス表記の場合 ➔ 「D」の前後で切り分ける
            String[] parts = modeText.split("D");
            if (parts.length == 2) {
                try {
                    int count = Integer.parseInt(parts[0]); // 個数（1）
                    int sides = Integer.parseInt(parts[1]); // 面数（6）

                    int total = 0;
                    StringBuilder rolls = new StringBuilder();
                    for (int i = 0; i < count; i++) {
                        int r = random.nextInt(sides) + 1;
                        total += r;
                        rolls.append(r).append(i == count - 1 ? "" : "+");
                    }
                    loseValue = total;

                    if (count > 1) {
                        loseDetail = modeText + " ➔ " + total + " (" + rolls.toString() + ")";
                    } else {
                        loseDetail = modeText + " ➔ " + total;
                    }
                } catch (NumberFormatException e) {
                    loseValue = 0;
                    loseDetail = "0 (ダイス入力エラー)";
                }
            } else {
                loseValue = 0;
                loseDetail = "0 (ダイス形式エラー)";
            }
        } else {
            // 「2」や「0」などの固定数値の場合
            try {
                loseValue = Integer.parseInt(modeText);
                loseDetail = String.valueOf(loseValue);
            } catch (NumberFormatException e) {
                loseValue = 0;
                loseDetail = "0 (数値変換エラー)";
            }
        }

        // ─── 3. メイン画面への反映 ───
        int remainingSan = sanTarget - loseValue;
        if (remainingSan < 0) remainingSan = 0;

        mainFrame.updateSanValue(remainingSan);

        // ─── 4. ログの組み立て ───
        StringBuilder sb = new StringBuilder();
        sb.append("🧠 SANチェック ━━━━━━━━━━━━━━━━━\n");
        sb.append("  【使用者】 ").append(charName).append("\n");
        sb.append("  【判定】 1D100 ➔ ［").append(diceResult).append("］\n");
        sb.append("  【目標】 SAN値 ").append(sanTarget).append(" 以下\n");
        sb.append("   ➔ 判定結果：").append(resultStr).append("\n");
        sb.append("  【SAN減少量】 ").append(loseDetail).append("\n");
        sb.append("   ➔ 残りSAN値：").append(sanTarget).append(" ➔ ［ ").append(remainingSan).append(" ］");

        logFrame.addLog(sb.toString());
    }
}