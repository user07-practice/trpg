package trpg;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class WeaponRollFrame extends JFrame {
    private CharacterFrame mainFrame;
    private DiceLogFrame logFrame;

    private JComboBox<String> weaponSelectCombo; // 武器選択用ドロップダウン
    private JComboBox<String> modeCombo;         // 攻撃モード選択用ドロップダウン

    private JTextField weaponNameField;   // 武器名
    private JTextField damageField1;      // ダメージ箱1
    private JTextField damageField2;      // ダメージ箱2
    private JTextField damageField3;      // ダメージ箱3

    private JPanel multiDamagePanel;      // 2発目、3発目を乗せるパネル
    private JLabel label2nd, label3rd;    // ラベルの表示制御用

    private java.util.List<String[]> weaponList = new ArrayList<>(); // 武器データの保管用
    private Random random = new Random();

    public WeaponRollFrame(CharacterFrame mainFrame, DiceLogFrame logFrame) {
        super("⚔️ 武器・戦闘ツール");
        this.mainFrame = mainFrame;
        this.logFrame = logFrame;

        // 初期サイズ
        setSize(520, 180);
        setResizable(false);
        setLocation(mainFrame.getX() + mainFrame.getWidth() + 10, mainFrame.getY() + 360);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // ─── キャラシから武器データを読み込む ───
        loadWeaponsFromCharacter();

        // ─── パネル1：武器の選択とモード切替 ───
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        String[] comboItems = weaponList.stream().map(w -> w[0]).toArray(String[]::new);
        weaponSelectCombo = new JComboBox<>(comboItems);
        weaponSelectCombo.setFont(new Font("Meiryo", Font.PLAIN, 12));
        weaponSelectCombo.addActionListener(e -> onWeaponSelected());

        weaponNameField = new JTextField(10);
        weaponNameField.setFont(new Font("Meiryo", Font.PLAIN, 12));

        modeCombo = new JComboBox<>(new String[]{"💥 1回攻撃", "🔥 2発連射", "⚡ 3発連射"});
        modeCombo.setFont(new Font("Meiryo", Font.BOLD, 12));
        modeCombo.addActionListener(e -> updateLayoutByMode());

        topPanel.add(new JLabel("武器選択:"));
        topPanel.add(weaponSelectCombo);
        topPanel.add(weaponNameField);
        topPanel.add(modeCombo);

        // ─── パネル2：1発目のダメージ入力とロール ───
        JPanel baseDamagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        damageField1 = new JTextField(6);
        damageField1.setHorizontalAlignment(JTextField.CENTER);
        damageField1.setFont(new Font("Meiryo", Font.PLAIN, 12));

        JButton rollBtn = new JButton("🎲 攻撃ロール実行！");
        rollBtn.setFont(new Font("Meiryo", Font.BOLD, 12));
        rollBtn.setBackground(new Color(255, 235, 205));
        rollBtn.addActionListener(e -> rollWeaponAttack());

        baseDamagePanel.add(new JLabel("1発目ダメージ:"));
        baseDamagePanel.add(damageField1);
        baseDamagePanel.add(rollBtn);

        // ─── パネル3：連射時に飛び出す2発目・3発目用 ───
        multiDamagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));

        label2nd = new JLabel("  2発目:");
        label2nd.setFont(new Font("Meiryo", Font.PLAIN, 12));
        damageField2 = new JTextField("", 6);
        damageField2.setHorizontalAlignment(JTextField.CENTER);
        damageField2.setFont(new Font("Meiryo", Font.PLAIN, 12));

        label3rd = new JLabel("  3発目:");
        label3rd.setFont(new Font("Meiryo", Font.PLAIN, 12));
        damageField3 = new JTextField("", 6);
        damageField3.setHorizontalAlignment(JTextField.CENTER);
        damageField3.setFont(new Font("Meiryo", Font.PLAIN, 12));

        multiDamagePanel.add(label2nd);
        multiDamagePanel.add(damageField2);
        multiDamagePanel.add(label3rd);
        multiDamagePanel.add(damageField3);

        add(topPanel);
        add(baseDamagePanel);
        add(multiDamagePanel);

        if (!weaponList.isEmpty()) {
            onWeaponSelected();
        }
        updateLayoutByMode();

        setVisible(true);
    }

    // キャレシから3つのダメージを含む武器データを引っ張ってくる
    private void loadWeaponsFromCharacter() {
        weaponList.clear();
        try {
            // キャラシ側の「getExtendedWeaponData」メソッドを呼び出す
            java.util.List<String[]> cData = mainFrame.getExtendedWeaponData();
            weaponList.addAll(cData);
        } catch (Exception e) {
            // 万が一キャラシ側にメソッドがない場合のセーフティ
            for (int i = 0; i < 5; i++) {
                weaponList.add(new String[]{"武器枠 " + (i + 1), "1D6", "", ""});
            }
        }
        weaponList.add(new String[]{"（自由手入力）", "1D6", "1D6", "1D6"});
    }

    // 武器が選択されたときにすべてのダメージ枠を自動書き換え
    private void onWeaponSelected() {
        int idx = weaponSelectCombo.getSelectedIndex();
        if (idx < 0 || idx >= weaponList.size()) return;

        String[] selected = weaponList.get(idx);
        if (selected[0].startsWith("武器枠 ") && selected[1].isEmpty()) {
            weaponNameField.setText("");
            weaponNameField.setEditable(true);
        } else if (selected[0].equals("（自由手入力）")) {
            weaponNameField.setText("");
            weaponNameField.setEditable(true);
        } else {
            weaponNameField.setText(selected[0]);
            weaponNameField.setEditable(false);
        }

        damageField1.setText(selected[1]);
        damageField2.setText(selected[2]);
        damageField3.setText(selected[3]);

        // データが入っているかによって自動でモード選択を切り替える親切設計
        if (selected[2].isEmpty() && selected[3].isEmpty()) {
            modeCombo.setSelectedIndex(0); // 1回攻撃
        } else if (!selected[2].isEmpty() && selected[3].isEmpty()) {
            modeCombo.setSelectedIndex(1); // 2発連射
        } else {
            modeCombo.setSelectedIndex(2); // 3発連射
        }

        updateLayoutByMode();
    }

    // モードに応じた画面レイアウト切り替え
    private void updateLayoutByMode() {
        int modeIdx = modeCombo.getSelectedIndex();

        if (modeIdx == 0) {
            multiDamagePanel.setVisible(false);
            setSize(520, 140);
        } else if (modeIdx == 1) {
            multiDamagePanel.setVisible(true);
            label2nd.setVisible(true);
            damageField2.setVisible(true);
            label3rd.setVisible(false);
            damageField3.setVisible(false);
            setSize(520, 180);
        } else {
            multiDamagePanel.setVisible(true);
            label2nd.setVisible(true);
            damageField2.setVisible(true);
            label3rd.setVisible(true);
            damageField3.setVisible(true);
            setSize(520, 180);
        }

        revalidate();
        repaint();
    }

    // 攻撃ロール実行
    private void rollWeaponAttack() {
        String charName = mainFrame.getCharacterName();
        String weaponName = weaponNameField.getText().trim();
        if (weaponName.isEmpty()) weaponName = "未知の武器";

        int modeIdx = modeCombo.getSelectedIndex();

        StringBuilder sb = new StringBuilder();
        sb.append("⚔️ 武器戦闘ロール ━━━━━━━━━━━━━━━━━\n");
        sb.append("  【使用者】 ").append(charName).append("\n");
        sb.append("  【使用武器】 ").append(weaponName).append("\n");

        if (modeIdx == 0) {
            String dmgText = damageField1.getText().trim().toUpperCase();
            String result = evalDamage(dmgText);
            sb.append("  【ダメージ】 ").append(dmgText).append(" ➔ ［ ").append(result).append(" ］ダメージ！");
        } else {
            String dmgText1 = damageField1.getText().trim().toUpperCase();
            sb.append("  【1発目ダメージ】 ").append(dmgText1).append(" ➔ ［ ").append(evalDamage(dmgText1)).append(" ］\n");

            if (modeIdx >= 1) {
                String dmgText2 = damageField2.getText().trim().toUpperCase();
                sb.append("  【2発目ダメージ】 ").append(dmgText2).append(" ➔ ［ ").append(evalDamage(dmgText2)).append(" ］\n");
            }
            if (modeIdx == 2) {
                String dmgText3 = damageField3.getText().trim().toUpperCase();
                sb.append("  【3発目ダメージ】 ").append(dmgText3).append(" ➔ ［ ").append(evalDamage(dmgText3)).append(" ］\n");
            }
            sb.append("   ➔ 連射による波状攻撃だ！");
        }

        logFrame.addLog(sb.toString());
    }

    // ダイス解析エンジン
    private String evalDamage(String formula) {
        if (formula.isEmpty()) return "0";
        try {
            int total = 0;
            String[] parts = formula.split("\\+");
            for (String part : parts) {
                if (part.contains("D")) {
                    String[] sub = part.split("D");
                    int count = Integer.parseInt(sub[0]);
                    int sides = Integer.parseInt(sub[1]);
                    for (int i = 0; i < count; i++) {
                        total += random.nextInt(sides) + 1;
                    }
                } else {
                    total += Integer.parseInt(part);
                }
            }
            return String.valueOf(total);
        } catch (Exception e) {
            return "変換エラー";
        }
    }
}
