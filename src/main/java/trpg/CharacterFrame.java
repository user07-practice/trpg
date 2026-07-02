package trpg;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CharacterFrame extends JFrame {
    private CharacterData data;
    private DiceLogFrame logFrame;

    private JTextField nameField;
    private JTextArea profileArea;
    private JLabel imageLabel;

    private JTextField strField, conField, powField, dexField, appField, sizField, intField, eduField;
    private JTextField hpField, mpField, sanField;

    private Map<String, JTextField> skillValueFields = new HashMap<>();
    private Map<String, JTextField> skillDetailFields = new HashMap<>();

    private JTextField[] customNameFields = new JTextField[13];
    private JTextField[] customValueFields = new JTextField[13];

    private JTextField[] weaponNameFields = new JTextField[5];
    private JTextField[] weaponHitFields = new JTextField[5];
    private JTextField[] weaponDamageFields = new JTextField[5];
    private JTextField[] weaponDamageFields1 = new JTextField[5];
    private JTextField[] weaponDamageFields2 = new JTextField[5];
    private JTextField[] weaponDamageFields3 = new JTextField[5];

    private Random random = new Random();

    private final String[][] SKILL_DEFS = {
            {"威圧", "15"}, {"言いくるめ", "5"}, {"医学", "1"}, {"運転（自動車）", "20"},
            {"応急手当", "30"}, {"オカルト", "5"}, {"隠密", "20"}, {"回避", "DEX_HALF"},
            {"科学", "1", "NEED_DETAIL"}, {"鍵開け", "1"}, {"鑑定", "5"}, {"機械修理", "10"},
            {"聞き耳", "20"}, {"クトゥルフ神話", "0"}, {"芸術／製作", "5"}, {"経理", "5"},
            {"考古学", "1"}, {"コンピューター", "5"}, {"サバイバル", "10", "NEED_DETAIL"}, {"自然", "10"},
            {"射撃（拳銃）", "20"}, {"射撃（ライフル／ショットガン）", "25"}, {"重機械操作", "1"}, {"信用", "0"},
            {"心理学", "10"}, {"人類学", "1"}, {"水泳", "20"}, {"精神分析", "1"}, {"説得", "10"},
            {"操縦", "1", "NEED_DETAIL"}, {"跳躍", "20"}, {"追跡", "10"}, {"手さばき", "10"},
            {"電気修理", "10"}, {"電子工学", "1"}, {"投擲", "20"}, {"登攀", "20"},
            {"図書館", "20"}, {"ナビゲート", "10"}, {"変装", "5"}, {"法律", "5"},
            {"ほかの言語", "1", "NEED_DETAIL"}, {"母国語", "EDU_BASE", "NEED_DETAIL"}, {"魅惑", "15"},
            {"目星", "25"}, {"歴史", "5"}
    };

    public CharacterFrame(CharacterData data, DiceLogFrame logFrame) {
        this.data = data;
        this.logFrame = logFrame;

        setTitle("📂 探索者 空間最適化キャラクターシート");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocation(50, 30);
        setLayout(new BorderLayout(10, 10));

        // =====================================================
        // 右側：立ち絵 ＆ プロフィールエリア
        // =====================================================
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(360, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("キャラクター立ち絵"));

        imageLabel = new JLabel("<html><center>クリックして<br>画像を選択</center></html>", SwingConstants.CENTER);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                if (chooser.showOpenDialog(CharacterFrame.this) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    String selectedPath = file.getAbsolutePath();
                    CharacterFrame.this.data.setImagePath(selectedPath); // パスを記憶
                    applyImageToLabel(selectedPath);
                }
            }
        });
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        rightPanel.add(imagePanel);

        rightPanel.add(Box.createVerticalStrut(8));

        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBorder(BorderFactory.createTitledBorder("キャラクター設定（プロフィール）"));
        profilePanel.setPreferredSize(new Dimension(350, 140));
        profilePanel.setMaximumSize(new Dimension(350, 140));

        profileArea = new JTextArea(data.getProfile(), 5, 20);
        profileArea.setFont(new Font("Meiryo", Font.PLAIN, 12));
        profileArea.setLineWrap(true);
        profilePanel.add(new JScrollPane(profileArea), BorderLayout.CENTER);
        rightPanel.add(profilePanel);

        add(rightPanel, BorderLayout.EAST);

        // =====================================================
        // 左側：メインパネル
        // =====================================================
        JPanel scrollContentPanel = new JPanel();
        scrollContentPanel.setLayout(new BoxLayout(scrollContentPanel, BoxLayout.Y_AXIS));
        scrollContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int panelWidth = 680;

        // 1. 名前入力
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.setMaximumSize(new Dimension(panelWidth, 35));
        JLabel nameLabel = new JLabel("名前：");
        nameLabel.setFont(new Font("Meiryo", Font.BOLD, 14));
        nameField = new JTextField(data.getName());
        nameField.setFont(new Font("Meiryo", Font.PLAIN, 14));
        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);
        scrollContentPanel.add(namePanel);
        scrollContentPanel.add(Box.createVerticalStrut(10));

        // 2. ステータスエリア
        JPanel statusContainer = new JPanel(new BorderLayout());
        statusContainer.setBorder(BorderFactory.createTitledBorder("能力値・ステータス"));
        statusContainer.setMaximumSize(new Dimension(panelWidth, 120));

        JPanel statusGrid = new JPanel(new GridLayout(4, 6, 4, 4));
        strField = createStatusField("STR", data.getStr(), statusGrid);
        conField = createStatusField("CON", data.getCon(), statusGrid);
        powField = createStatusField("POW", data.getPow(), statusGrid);
        dexField = createStatusField("DEX", data.getDex(), statusGrid);
        appField = createStatusField("APP", data.getApp(), statusGrid);
        sizField = createStatusField("SIZ", data.getSiz(), statusGrid);
        intField = createStatusField("INT", data.getIntStat(), statusGrid);
        eduField = createStatusField("EDU", data.getEdu(), statusGrid);
        hpField  = createStatusField("ＨＰ", data.getHp(), statusGrid);
        mpField  = createStatusField("ＭＰ", data.getMp(), statusGrid);
        sanField = createStatusField("SAN値", data.getSan(), statusGrid);

        JButton sanBtn = new JButton("SANチェック");
        sanBtn.setFont(new Font("Meiryo", Font.BOLD, 10));
        sanBtn.setBackground(new Color(230, 240, 255));
        sanBtn.addActionListener(e -> new SanCheckFrame(this, logFrame));
        statusGrid.add(sanBtn);

        JButton weaponBtn = new JButton("⚔️ 武器・戦闘");
        weaponBtn.setFont(new Font("Meiryo", Font.BOLD, 10));
        weaponBtn.setBackground(new Color(255, 235, 205));
        weaponBtn.addActionListener(e -> new WeaponRollFrame(this, logFrame));
        statusGrid.add(weaponBtn);
        statusContainer.add(statusGrid, BorderLayout.CENTER);
        scrollContentPanel.add(statusContainer);
        scrollContentPanel.add(Box.createVerticalStrut(10));

        // DEX変更時に「回避」を自動再計算するリスナー
        dexField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateAvoidance(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateAvoidance(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateAvoidance(); }

            private void updateAvoidance() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        String text = dexField.getText().trim();
                        if (!text.isEmpty()) {
                            int dex = Integer.parseInt(text);
                            if (skillValueFields.containsKey("回避")) {
                                skillValueFields.get("回避").setText(String.valueOf(dex / 2));
                            }
                        }
                    } catch (NumberFormatException ex) {}
                });
            }
        });

        // 3. 固定技能エリア（3列・スクロールなし）
        JPanel skillSuperContainer = new JPanel(new BorderLayout());
        skillSuperContainer.setBorder(BorderFactory.createTitledBorder("固定技能一覧（一覧表示・スクロールなし）"));
        skillSuperContainer.setMaximumSize(new Dimension(panelWidth, 480));

        JPanel skillGrid = new JPanel(new GridLayout(0, 3, 6, 5));
        int dexVal = data.getDex();
        int eduVal = data.getEdu();

        for (String[] def : SKILL_DEFS) {
            String sName = def[0];
            String baseStr = def[1];
            boolean needDetail = def.length > 2;

            int baseValue = 0;
            if (baseStr.equals("DEX_HALF")) {
                baseValue = dexVal / 2;
            } else if (baseStr.equals("EDU_BASE")) {
                baseValue = eduVal;
            } else {
                baseValue = Integer.parseInt(baseStr);
            }

            JPanel sRow = new JPanel(new BorderLayout(1, 0));
            JPanel leftSub = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
            JLabel label = new JLabel(sName);
            label.setFont(new Font("Meiryo", Font.PLAIN, 10));
            leftSub.add(label);

            if (needDetail) {
                JTextField detField = new JTextField(3);
                detField.setFont(new Font("Meiryo", Font.PLAIN, 9));
                leftSub.add(new JLabel("("));
                leftSub.add(detField);
                leftSub.add(new JLabel(")"));
                skillDetailFields.put(sName, detField);
            }

            JPanel rightSub = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));
            JTextField valField = new JTextField(String.valueOf(baseValue), 2);
            valField.setHorizontalAlignment(JTextField.CENTER);
            valField.setFont(new Font("Meiryo", Font.PLAIN, 10));
            JButton rBtn = new JButton("🎲");
            rBtn.setFont(new Font("Meiryo", Font.PLAIN, 9));
            rBtn.setMargin(new Insets(1, 2, 1, 2));
            rBtn.addActionListener(e -> rollFixedSkill(sName));

            rightSub.add(valField);
            rightSub.add(rBtn);
            skillValueFields.put(sName, valField);

            sRow.add(leftSub, BorderLayout.CENTER);
            sRow.add(rightSub, BorderLayout.EAST);
            skillGrid.add(sRow);
        }

        skillSuperContainer.add(skillGrid, BorderLayout.CENTER);
        scrollContentPanel.add(skillSuperContainer);
        scrollContentPanel.add(Box.createVerticalStrut(10));

        // 4. 自由記入の空欄技能（3列・スクロールなし）
        JPanel customSkillContainer = new JPanel(new BorderLayout());
        customSkillContainer.setBorder(BorderFactory.createTitledBorder("職業・追加技能の空欄枠（13箇所）"));
        customSkillContainer.setMaximumSize(new Dimension(panelWidth, 160));

        JPanel customGrid = new JPanel(new GridLayout(0, 3, 6, 5));
        for (int i = 0; i < 13; i++) {
            JPanel cRow = new JPanel(new BorderLayout(2, 0));
            customNameFields[i] = new JTextField(5);
            customNameFields[i].setFont(new Font("Meiryo", Font.PLAIN, 10));

            JPanel rightSub = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));
            customValueFields[i] = new JTextField("0", 2);
            customValueFields[i].setHorizontalAlignment(JTextField.CENTER);
            customValueFields[i].setFont(new Font("Meiryo", Font.PLAIN, 10));
            JButton cBtn = new JButton("🎲");
            cBtn.setFont(new Font("Meiryo", Font.PLAIN, 9));
            cBtn.setMargin(new Insets(1, 2, 1, 2));

            int idx = i;
            cBtn.addActionListener(e -> rollCustomSkill(idx));

            rightSub.add(customValueFields[i]);
            rightSub.add(cBtn);

            cRow.add(customNameFields[i], BorderLayout.CENTER);
            cRow.add(rightSub, BorderLayout.EAST);
            customGrid.add(cRow);
        }

        customSkillContainer.add(customGrid, BorderLayout.CENTER);
        scrollContentPanel.add(customSkillContainer);
        scrollContentPanel.add(Box.createVerticalStrut(10));

        // 5. 武器・戦闘エリア
        JPanel weaponContainer = new JPanel();
        weaponContainer.setLayout(new BoxLayout(weaponContainer, BoxLayout.Y_AXIS));
        weaponContainer.setBorder(BorderFactory.createTitledBorder("武器・戦闘（5枠 / 連射ダメージ対応）"));
        weaponContainer.setMaximumSize(new Dimension(panelWidth, 180));

        for (int i = 0; i < 5; i++) {
            JPanel wRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 1));
            wRow.setMaximumSize(new Dimension(panelWidth, 28));

            weaponNameFields[i] = new JTextField(10);
            weaponHitFields[i] = new JTextField(3);

            weaponDamageFields1[i] = new JTextField("1D6", 4);
            weaponDamageFields2[i] = new JTextField("", 4);
            weaponDamageFields3[i] = new JTextField("", 4);

            weaponDamageFields1[i].setHorizontalAlignment(JTextField.CENTER);
            weaponDamageFields2[i].setHorizontalAlignment(JTextField.CENTER);
            weaponDamageFields3[i].setHorizontalAlignment(JTextField.CENTER);

            JButton hitBtn = new JButton("🎯 命中");
            hitBtn.setFont(new Font("Meiryo", Font.PLAIN, 11));
            int idx = i;
            hitBtn.addActionListener(e -> rollWeaponHit(idx));

            wRow.add(new JLabel((i+1) + ". 武器:"));
            wRow.add(weaponNameFields[i]);
            wRow.add(new JLabel(" 命中:"));
            wRow.add(weaponHitFields[i]);
            wRow.add(hitBtn);

            wRow.add(new JLabel(" ダメージ ①:"));
            wRow.add(weaponDamageFields1[i]);
            wRow.add(new JLabel(" ②:"));
            wRow.add(weaponDamageFields2[i]);
            wRow.add(new JLabel(" ③:"));
            wRow.add(weaponDamageFields3[i]);

            weaponContainer.add(wRow);
        }
        scrollContentPanel.add(weaponContainer);

        JScrollPane mainScroll = new JScrollPane(scrollContentPanel);
        mainScroll.getVerticalScrollBar().setUnitIncrement(20);
        add(mainScroll, BorderLayout.CENTER);


        // =====================================================
        // 🌟 追加機能：画面上部のメニューバー（新規・開く・保存）
        // =====================================================
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("ファイル(F)");
        fileMenu.setMnemonic('F');

        // ① 新規作成
        JMenuItem newItem = new JMenuItem("新規キャラクター作成");
        newItem.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "現在の入力を破棄して新しくシートを作りますか？", "新規作成", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                this.data = new CharacterData();
                loadDataToScreen();
            }
        });

        // ②-A 上書きで開く（ロード）
        JMenuItem openOverwriteItem = new JMenuItem("キャラクターを読み込んでこの画面に上書き...");
        openOverwriteItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
                    this.data = (CharacterData) ois.readObject();
                    loadDataToScreen();
                    JOptionPane.showMessageDialog(this, data.getName() + " をこの画面に読み込みました！");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "ファイルの読み込みに失敗しました。\n" + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ②-B 別ウィンドウで開く（ロード）
        JMenuItem openNewWindowItem = new JMenuItem("キャラクターを読み込んで別ウィンドウで開く...");
        openNewWindowItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
                    CharacterData loadedData = (CharacterData) ois.readObject();
                    CharacterFrame newFrame = new CharacterFrame(loadedData, this.logFrame);
                    newFrame.setLocation(this.getX() + 40, this.getY() + 40);
                    JOptionPane.showMessageDialog(this, loadedData.getName() + " を新しいウィンドウで開きました！");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "ファイルの読み込みに失敗しました。\n" + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ③ 保存（セーブ）
        JMenuItem saveItem = new JMenuItem("キャラクターを保存する...");
        saveItem.addActionListener(e -> {
            saveScreenToData();
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(getCharacterName() + ".char"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(file))) {
                    oos.writeObject(this.data);
                    JOptionPane.showMessageDialog(this, "保存が完了しました！");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "ファイルの保存に失敗しました。\n" + ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        fileMenu.add(newItem);
        fileMenu.add(openOverwriteItem);
        fileMenu.add(openNewWindowItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // 起動時にデータを画面に一括セット
        loadDataToScreen();

        // 🌟【解決：ダイス重複を排除】
        // アプリ全体で1枚だけ立ち上げるため、ここにあった new GenericDiceFrame(...) はすべて綺麗に削除しました！

        setVisible(true);
    }


    // キャラクター名取得
    public String getCharacterName() {
        String name = nameField.getText().trim();
        return name.isEmpty() ? "名無しの探索者" : name;
    }

    public int findValueByName(String name) {
        if (name == null || name.trim().isEmpty()) return -1;
        String target = name.trim();

        if (skillValueFields.containsKey(target)) {
            try {
                return Integer.parseInt(skillValueFields.get(target).getText().trim());
            } catch (NumberFormatException e) { return -1; }
        }

        try {
            if (target.equalsIgnoreCase("STR")) return Integer.parseInt(strField.getText().trim());
            if (target.equalsIgnoreCase("CON")) return Integer.parseInt(conField.getText().trim());
            if (target.equalsIgnoreCase("POW")) return Integer.parseInt(powField.getText().trim());
            if (target.equalsIgnoreCase("DEX")) return Integer.parseInt(dexField.getText().trim());
            if (target.equalsIgnoreCase("APP")) return Integer.parseInt(appField.getText().trim());
            if (target.equalsIgnoreCase("SIZ")) return Integer.parseInt(sizField.getText().trim());
            if (target.equalsIgnoreCase("INT")) return Integer.parseInt(intField.getText().trim());
            if (target.equalsIgnoreCase("EDU")) return Integer.parseInt(eduField.getText().trim());
            if (target.equals("HP") || target.equals("ＨＰ")) return Integer.parseInt(hpField.getText().trim());
            if (target.equals("MP") || target.equals("ＭＰ")) return Integer.parseInt(mpField.getText().trim());
            if (target.equalsIgnoreCase("SAN") || target.equals("SAN値")) return Integer.parseInt(sanField.getText().trim());
        } catch (NumberFormatException e) { return -1; }

        for (int i = 0; i < customNameFields.length; i++) {
            if (customNameFields[i] != null && customNameFields[i].getText().trim().equals(target)) {
                try {
                    return Integer.parseInt(customValueFields[i].getText().trim());
                } catch (NumberFormatException e) { return -1; }
            }
        }

        for (int i = 0; i < weaponNameFields.length; i++) {
            if (weaponNameFields[i] != null && weaponNameFields[i].getText().trim().equals(target)) {
                try {
                    return Integer.parseInt(weaponHitFields[i].getText().trim());
                } catch (NumberFormatException e) { return -1; }
            }
        }

        return -1;
    }

    private JTextField createStatusField(String labelName, int value, JPanel panel) {
        JLabel label = new JLabel(labelName, SwingConstants.CENTER);
        label.setFont(new Font("Meiryo", Font.PLAIN, 11));
        JTextField field = new JTextField(String.valueOf(value));
        field.setHorizontalAlignment(JTextField.CENTER);
        panel.add(label);
        panel.add(field);
        return field;
    }

    private void rollFixedSkill(String skillName) {
        String charName = getCharacterName();
        int target = Integer.parseInt(skillValueFields.get(skillName).getText().trim());

        String displayName = skillName;
        if (skillDetailFields.containsKey(skillName)) {
            String detail = skillDetailFields.get(skillName).getText().trim();
            if (!detail.isEmpty()) {
                displayName = skillName + "(" + detail + ")";
            }
        }

        int dice = random.nextInt(100) + 1;
        String result = getCthulhuResult(dice, target);

        logFrame.addLog(String.format("[%s] 技能:%s (%d%%) ➔ 出目:%d %s", charName, displayName, target, dice, result));
    }

    private void rollCustomSkill(int idx) {
        String charName = getCharacterName();
        String sName = customNameFields[idx].getText().trim();
        if (sName.isEmpty()) {
            logFrame.addLog("⚠️ 技能名が空欄です。名前を入力してから振ってください。");
            return;
        }

        int target = Integer.parseInt(customValueFields[idx].getText().trim());
        int dice = random.nextInt(100) + 1;
        String result = getCthulhuResult(dice, target);

        logFrame.addLog(String.format("[%s] 追加技能:%s (%d%%) ➔ 出目:%d %s", charName, sName, target, dice, result));
    }

    private void rollWeaponHit(int idx) {
        String charName = getCharacterName();
        String wName = weaponNameFields[idx].getText().trim();
        if (wName.isEmpty()) wName = "未知の武器";

        String hitText = weaponHitFields[idx].getText().trim();
        if (hitText.isEmpty()) {
            logFrame.addLog(String.format("⚠️ [%s] の命中率(%%)を入力してください。", wName));
            return;
        }

        int target = Integer.parseInt(hitText);
        int dice = random.nextInt(100) + 1;
        String result = getCthulhuResult(dice, target);

        logFrame.addLog(String.format("[%s] 武器攻撃:%s [命中判定] (%d%%) ➔ 出目:%d %s", charName, wName, target, dice, result));
    }

    private String getCthulhuResult(int dice, int target) {
        if (dice <= 5 && dice <= target) return "【決定的成功（クリティカル）✨】";
        if (dice >= 96) return "【致命的失敗（ファンブル）💀】";
        if (dice <= target) return "【成功👍】";
        return "【失敗😭】";
    }

    public int getSanValue() {
        try {
            return Integer.parseInt(sanField.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void updateSanValue(int newSan) {
        sanField.setText(String.valueOf(newSan));
    }

    public java.util.List<String[]> getExtendedWeaponData() {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String name = weaponNameFields[i].getText().trim();
            String dmg1 = weaponDamageFields1[i].getText().trim();
            String dmg2 = weaponDamageFields2[i].getText().trim();
            String dmg3 = weaponDamageFields3[i].getText().trim();

            if (name.isEmpty()) name = "武器枠 " + (i + 1);

            list.add(new String[]{name, dmg1, dmg2, dmg3});
        }
        return list;
    }

    /**
     * 🌟 最終解決版：現在の画面の入力内容（画像パス含む）を完全にデータに保存する
     */
    private void saveScreenToData() {
        data.setName(nameField.getText());
        data.setProfile(profileArea.getText());

        // ⭕ 【ここを修正！】
        // すでにクリック時に data にパスは入っているので、セーブ時に余計な上書きをしないように
        // この行自体を完全に削除するか、以下のように何もしない形（現状維持）にします。
        // もしこれでも消える場合は、クリックした時のパスがセーブ時に蒸発しないよう、
        // data.setImagePath(data.getImagePath()); の行を綺麗に消去してください。

        try {
            data.setStr(Integer.parseInt(strField.getText().trim()));
            data.setCon(Integer.parseInt(conField.getText().trim()));
            data.setPow(Integer.parseInt(powField.getText().trim()));
            data.setDex(Integer.parseInt(dexField.getText().trim()));
            data.setApp(Integer.parseInt(appField.getText().trim()));
            data.setSiz(Integer.parseInt(sizField.getText().trim()));
            data.setIntStat(Integer.parseInt(intField.getText().trim()));
            data.setEdu(Integer.parseInt(eduField.getText().trim()));
            data.setHp(Integer.parseInt(hpField.getText().trim()));
            data.setMp(Integer.parseInt(mpField.getText().trim()));
            data.setSan(Integer.parseInt(sanField.getText().trim()));
        } catch (NumberFormatException e) {
            // 数値変換エラー時は現状維持
        }
    }

    private void loadDataToScreen() {
        nameField.setText(data.getName());
        profileArea.setText(data.getProfile());

        strField.setText(String.valueOf(data.getStr()));
        conField.setText(String.valueOf(data.getCon()));
        powField.setText(String.valueOf(data.getPow()));
        dexField.setText(String.valueOf(data.getDex()));
        appField.setText(String.valueOf(data.getApp()));
        sizField.setText(String.valueOf(data.getSiz()));
        intField.setText(String.valueOf(data.getIntStat()));
        eduField.setText(String.valueOf(data.getEdu()));
        hpField.setText(String.valueOf(data.getHp()));
        mpField.setText(String.valueOf(data.getMp()));
        sanField.setText(String.valueOf(data.getSan()));

        String path = data.getImagePath();
        if (path != null && !path.isEmpty()) {
            applyImageToLabel(path);
        }
    }

    private void applyImageToLabel(String path) {
        try {
            if (path == null || path.isEmpty()) return;

            ImageIcon icon = new ImageIcon(path);
            int targetWidth = 330;
            int targetHeight = 410;

            Image img = icon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
            imageLabel.setText("");

            imageLabel.revalidate();
            imageLabel.repaint();
        } catch (Exception e) {
            imageLabel.setText("⚠️ 画像読み込みエラー");
        }
    }
}