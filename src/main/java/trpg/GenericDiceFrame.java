package trpg;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GenericDiceFrame extends JFrame {
    private CharacterFrame mainFrame;
    private DiceLogFrame logFrame;

    private JComboBox<String> genericDiceTypeCombo;
    private JComboBox<Integer> genericDiceCountCombo;
    private JTextField genericTargetField;

    private Random random = new Random();

    public GenericDiceFrame(CharacterFrame mainFrame, DiceLogFrame logFrame) {
        super("🎲 汎用ダイスツール");
        this.mainFrame = mainFrame;
        this.logFrame = logFrame;

        setSize(530, 160);
        setResizable(false);

        setLocation(mainFrame.getX() + mainFrame.getWidth() + 10, mainFrame.getY());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 12));

        genericDiceTypeCombo = new JComboBox<>(new String[]{"4面体", "6面体", "8面体", "10面体"});
        genericDiceTypeCombo.setFont(new Font("Meiryo", Font.PLAIN, 12));

        genericDiceCountCombo = new JComboBox<>(new Integer[]{1, 2});
        genericDiceCountCombo.setFont(new Font("Meiryo", Font.PLAIN, 12));

        genericTargetField = new JTextField(6);
        genericTargetField.setHorizontalAlignment(JTextField.CENTER);
        genericTargetField.setFont(new Font("Meiryo", Font.PLAIN, 12));

        JButton genericRollBtn = new JButton("🎲 ロール！");
        genericRollBtn.setFont(new Font("Meiryo", Font.BOLD, 12));
        genericRollBtn.setBackground(new Color(240, 255, 240));
        genericRollBtn.addActionListener(e -> rollGenericDice());

        JLabel label1 = new JLabel("ダイス:");
        label1.setFont(new Font("Meiryo", Font.PLAIN, 12));
        add(label1);
        add(genericDiceTypeCombo);

        JLabel label2 = new JLabel("を");
        label2.setFont(new Font("Meiryo", Font.PLAIN, 12));
        add(label2);
        add(genericDiceCountCombo);

        JLabel label3 = new JLabel("個 ─ 目標（技能名可）:");
        label3.setFont(new Font("Meiryo", Font.PLAIN, 12));
        add(label3);
        add(genericTargetField);

        JLabel label4 = new JLabel("以下ならOK ➔");
        label4.setFont(new Font("Meiryo", Font.PLAIN, 12));
        add(label4);
        add(genericRollBtn);

        setVisible(true);
    }

    // 汎用ダイス判定ロジック（見やすさ大改造版）
    private void rollGenericDice() {
        String charName = mainFrame.getCharacterName();

        String diceTypeStr = (String) genericDiceTypeCombo.getSelectedItem();
        int count = (Integer) genericDiceCountCombo.getSelectedItem();
        String inputText = genericTargetField.getText().trim();

        int sides = 6;
        if (diceTypeStr.equals("4面体")) sides = 4;
        else if (diceTypeStr.equals("8面体")) sides = 8;
        else if (diceTypeStr.equals("10面体")) sides = 10;

        int total = 0;
        StringBuilder rolls = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int r = random.nextInt(sides) + 1;
            total += r;
            rolls.append(r).append(i == count - 1 ? "" : "+");
        }

        String resultStr = "";
        String targetNameForLog = "";
        int targetValue = -1;

        if (!inputText.isEmpty()) {
            try {
                targetValue = Integer.parseInt(inputText);
                targetNameForLog = String.valueOf(targetValue);
            } catch (NumberFormatException e) {
                targetValue = mainFrame.findValueByName(inputText);
                if (targetValue != -1) {
                    targetNameForLog = inputText + "(" + targetValue + ")";
                }
            }

            if (targetValue != -1) {
                if (total <= targetValue) {
                    resultStr = "【成功👍】";
                } else {
                    resultStr = "【失敗😭】";
                }
            } else {
                resultStr = "(⚠️数値または正しい技能名が見つかりません)";
            }
        }

        // ★文章を横に繋げず、StringBuilderを使って縦に綺麗に整列させます！
        StringBuilder sb = new StringBuilder();
        sb.append("🎲 汎用ロール ━━━━━━━━━━━━━━━━━\n");
        sb.append("  【使用者】 ").append(charName).append("\n");

        // ダイスの内訳（1+4など）は、複数個振った時だけ見せるようにしてスッキリ化
        if (count > 1) {
            sb.append("  【振ったダイス】 ").append(count).append("D").append(sides)
                    .append("  ➔  合計：").append(total).append("  (").append(rolls.toString()).append(")\n");
        } else {
            sb.append("  【振ったダイス】 ").append(count).append("D").append(sides)
                    .append("  ➔  出目：").append(total).append("\n");
        }

        // 目標値が設定されている場合のみ、目標と結果を表示
        if (!inputText.isEmpty()) {
            sb.append("  【目標値】 ").append(targetNameForLog).append(" 以下\n");
            sb.append("   ➔ 判定結果：").append(resultStr);
        } else {
            sb.append("   ➔ 判定結果：(判定なしの素振り)");
        }

        // 完成した縦書きテキストをログに送信
        logFrame.addLog(sb.toString());
    }
}