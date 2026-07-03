package trpg;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleFrame extends JFrame {
    private CharacterFrame charFrame;
    private Random random = new Random();

    // 選択コンボボックス
    private JComboBox<String> attackerCombo;
    private JComboBox<String> defenderCombo;

    // 【攻撃側】の入力欄
    private JTextField weaponHitInput;
    private JTextField damageDiceInput;

    // 【防御側】の選択・ステータス
    private JRadioButton avoidRadio;
    private JRadioButton parryRadio;
    private JRadioButton noneRadio;
    private JLabel defenderStatusLabel;

    // 敵（エネミー）のデータ保持
    private int enemyMaxHp = 0;
    private int enemyCurrentHp = 0;
    private JLabel enemyHpLabel;
    private JTextField enemyMaxHpInput;
    private JCheckBox hideEnemyHpCheck;

    // ログ表示
    private JTextArea battleLogArea;

    private List<CharacterFrame> activeFrames = new ArrayList<>();

    public BattleFrame(CharacterFrame charFrame) {
        this.charFrame = charFrame;

        setTitle("⚔️ 統合決戦ステージ（攻撃・防御・HP管理がっちゃんこ版）");
        setSize(800, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(charFrame);
        setLayout(new BorderLayout(10, 10));

        // 参加キャラクターの自動検出
        refreshActiveCharacters();

        // ─── 左側：戦闘コントロールパネル ───
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.setPreferredSize(new Dimension(450, 0));

        // 1. だれが戦うかエリア
        JPanel vsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        vsPanel.setBorder(BorderFactory.createTitledBorder("1. 対戦者の選択"));
        attackerCombo = new JComboBox<>();
        defenderCombo = new JComboBox<>();
        populateCombos();

        // ディフェンダーが切り替わったら防御側のステータス表示を更新する
        defenderCombo.addActionListener(e -> updateDefenderStatusDisplay());

        vsPanel.add(new JLabel("⚔️ 攻撃側:"));
        vsPanel.add(attackerCombo);
        vsPanel.add(new JLabel("🛡️ 防御側:"));
        vsPanel.add(defenderCombo);
        leftPanel.add(vsPanel);
        leftPanel.add(Box.createVerticalStrut(10));

        // 2. 攻撃ウィンドウの機能（がっちゃんこ部分）
        JPanel attackPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        attackPanel.setBorder(BorderFactory.createTitledBorder("2. 攻撃側の宣言（旧・攻撃ウィンドウ）"));

        weaponHitInput = new JTextField("50", 5);
        damageDiceInput = new JTextField("1D6+DB", 8); // DB（ダメージボーナス）や通常の1D6+2など

        attackPanel.add(new JLabel("攻撃の命中率 (%):"));
        attackPanel.add(weaponHitInput);
        attackPanel.add(new JLabel("ダメージ数式 (例: 1D6):"));
        attackPanel.add(damageDiceInput);

        JButton attackBtn = new JButton("🎲 攻撃ダイスを振る！");
        attackBtn.setBackground(new Color(255, 230, 230));
        attackBtn.setFont(new Font("Meiryo", Font.BOLD, 12));
        attackBtn.addActionListener(e -> executeBattleRound());

        // レイアウト調整用にボタンを配置
        attackPanel.add(new JLabel("👇 すべての判定を自動処理:"));
        attackPanel.add(attackBtn);
        leftPanel.add(attackPanel);
        leftPanel.add(Box.createVerticalStrut(10));

        // 3. 防御側の選択エリア（回避 vs 受け流し）
        JPanel defensePanel = new JPanel();
        defensePanel.setLayout(new BoxLayout(defensePanel, BoxLayout.Y_AXIS));
        defensePanel.setBorder(BorderFactory.createTitledBorder("3. 防御側の選択（回避 or 受け流し）"));

        avoidRadio = new JRadioButton("回避を試みる（成功でダメージ0）", true);
        parryRadio = new JRadioButton("受け流し（武器やこぶしでガード・ダメージ軽減/0）");
        noneRadio = new JRadioButton("無防備（ダイスを振らずに直撃を受ける）");

        ButtonGroup bg = new ButtonGroup();
        bg.add(avoidRadio);
        bg.add(parryRadio);
        bg.add(noneRadio);

        JPanel radioPanel = new JPanel(new GridLayout(3, 1));
        radioPanel.add(avoidRadio);
        radioPanel.add(parryRadio);
        radioPanel.add(noneRadio);
        defensePanel.add(radioPanel);

        defenderStatusLabel = new JLabel("現在の防御側ステータス ➔ 回避: -- % | 受け流し(近接技能): -- %");
        defenderStatusLabel.setFont(new Font("Meiryo", Font.ITALIC, 11));
        defenderStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        defensePanel.add(defenderStatusLabel);

        leftPanel.add(defensePanel);
        leftPanel.add(Box.createVerticalStrut(10));

        // 4. エネミー設定エリア
        JPanel enemyPanel = new JPanel(new BorderLayout());
        enemyPanel.setBorder(BorderFactory.createTitledBorder("エネミー（敵NPC）専用設定"));
        JPanel enemyRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        enemyRow.add(new JLabel("最大HP:"));
        enemyMaxHpInput = new JTextField("30", 4);
        enemyRow.add(enemyMaxHpInput);
        JButton setEnemyBtn = new JButton("設定");
        setEnemyBtn.addActionListener(e -> setEnemyHp());
        enemyRow.add(setEnemyBtn);
        hideEnemyHpCheck = new JCheckBox("HPを隠す");
        hideEnemyHpCheck.addActionListener(e -> updateEnemyHpDisplay());
        enemyRow.add(hideEnemyHpCheck);

        JButton refreshBtn = new JButton("🔄 メンバー更新");
        refreshBtn.addActionListener(e -> { refreshActiveCharacters(); populateCombos(); });
        enemyRow.add(refreshBtn);

        enemyPanel.add(enemyRow, BorderLayout.CENTER);
        leftPanel.add(enemyPanel);

        add(leftPanel, BorderLayout.WEST);

        // ─── 右側：リアルタイム戦闘ログ ＆ HP表示 ───
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 上部に敵のHPバーっぽく表示
        enemyHpLabel = new JLabel("敵のHP: 未設定", SwingConstants.CENTER);
        enemyHpLabel.setFont(new Font("Meiryo", Font.BOLD, 22));
        enemyHpLabel.setForeground(Color.RED);
        enemyHpLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        rightPanel.add(enemyHpLabel, BorderLayout.NORTH);

        // 中央に戦闘ログエリア
        battleLogArea = new JTextArea();
        battleLogArea.setEditable(false);
        battleLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        battleLogArea.setBackground(new Color(245, 245, 245));
        JScrollPane logScroll = new JScrollPane(battleLogArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("⚔️ 戦闘ラウンド・リアルタイム実況ログ"));
        rightPanel.add(logScroll, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);

        updateDefenderStatusDisplay();
        updateEnemyHpDisplay();
        setVisible(true);
    }

    private void refreshActiveCharacters() {
        activeFrames.clear();
        for (Window window : Window.getWindows()) {
            if (window instanceof CharacterFrame && window.isShowing()) {
                activeFrames.add((CharacterFrame) window);
            }
        }
    }

    private void populateCombos() {
        String lastAtk = (String) attackerCombo.getSelectedItem();
        String lastDef = (String) defenderCombo.getSelectedItem();

        attackerCombo.removeAllItems();
        defenderCombo.removeAllItems();

        for (CharacterFrame frame : activeFrames) {
            String name = frame.getCharacterName();
            attackerCombo.addItem(name);
            defenderCombo.addItem(name);
        }
        attackerCombo.addItem("エネミー（敵）");
        defenderCombo.addItem("エネミー（敵）");

        if (lastAtk != null) attackerCombo.setSelectedItem(lastAtk);
        if (lastDef != null) defenderCombo.setSelectedItem(lastDef);
    }

    // 防御側に選ばれたキャラのステータスを自動でのぞき見して画面に表示する
    private void updateDefenderStatusDisplay() {
        String defenderName = (String) defenderCombo.getSelectedItem();
        if (defenderName == null) return;

        if (defenderName.equals("エネミー（敵）")) {
            defenderStatusLabel.setText("敵のステータス ➔ 回避: 30% | 受け流し: 30%");
        } else {
            CharacterFrame defFrame = findFrameByName(defenderName);
            if (defFrame != null) {
                int avoid = defFrame.findValueByName("回避");
                int parry = defFrame.findValueByName("近接戦闘") != -1 ? defFrame.findValueByName("近接戦闘") : defFrame.findValueByName("こぶし");
                if (parry == -1) parry = 25; // こぶしの初期値など
                defenderStatusLabel.setText(String.format("%s ➔ 回避: %d%% | 受け流し(こぶし等): %d%%", defenderName, avoid, parry));
            }
        }
    }

    private void setEnemyHp() {
        try {
            int hp = Integer.parseInt(enemyMaxHpInput.getText().trim());
            enemyMaxHp = hp;
            enemyCurrentHp = hp;
            updateEnemyHpDisplay();
            logAppend("📢 敵のエネミーデータが初期化されました。(最大HP: " + hp + ")");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "数値を入力してください。");
        }
    }

    private void updateEnemyHpDisplay() {
        if (enemyMaxHp == 0) {
            enemyHpLabel.setText("敵のHP: 未設定");
        } else if (hideEnemyHpCheck.isSelected()) {
            enemyHpLabel.setText("敵のHP: ？？？ / ？？？");
        } else {
            enemyHpLabel.setText(String.format("敵のHP: %d / %d", enemyCurrentHp, enemyMaxHp));
        }
    }

    // 🌟 攻撃・防御・受け流し・HP減少を一括処理する「がっちゃんこメインコア」
    private void executeBattleRound() {
        String attacker = (String) attackerCombo.getSelectedItem();
        String defender = (String) defenderCombo.getSelectedItem();
        if (attacker == null || defender == null) return;

        logAppend(String.format("\n【戦闘開始】%s  vs  %s", attacker, defender));

        // 1. 攻撃側の命中ロール
        int atkTarget = 50;
        try { atkTarget = Integer.parseInt(weaponHitInput.getText().trim()); } catch(Exception e){}

        int atkDice = random.nextInt(100) + 1;
        boolean isHit = atkDice <= atkTarget;

        logAppend(String.format(" ➔ [攻撃] 命中率:%d%% | 出目:%d ➔ %s", atkTarget, atkDice, isHit ? "命中成功！🎯" : "ミス！😭"));

        if (!isHit) {
            logAppend(" ➔ 攻撃が外れたため、このラウンドは終了です。");
            return;
        }

        // 2. 防御側の処理（回避 or 受け流し or なし）
        boolean damageApplies = true;

        if (noneRadio.isSelected()) {
            logAppend(String.format(" ➔ [防御] %s は無防備を宣言。攻撃が直撃します！", defender));
        } else {
            int defDice = random.nextInt(100) + 1;
            int defTarget = 0;
            String modeStr = "";

            if (avoidRadio.isSelected()) {
                modeStr = "回避";
                if (defender.equals("エネミー（敵）")) {
                    defTarget = 30;
                } else {
                    CharacterFrame defFrame = findFrameByName(defender);
                    if (defFrame != null) defTarget = defFrame.findValueByName("回避");
                }

                boolean isAvoidSuccess = defDice <= defTarget;
                logAppend(String.format(" ➔ [防御] %s が【回避】に挑戦！(目標:%d%%) | 出目:%d ➔ %s", defender, defTarget, defDice, isAvoidSuccess ? "大成功！華麗に避けた！" : "回避失敗！"));
                if (isAvoidSuccess) damageApplies = false; // ダメージなし！

            } else if (parryRadio.isSelected()) {
                modeStr = "受け流し";
                if (defender.equals("エネミー（敵）")) {
                    defTarget = 30;
                } else {
                    CharacterFrame defFrame = findFrameByName(defender);
                    if (defFrame != null) {
                        defTarget = defFrame.findValueByName("こぶし");
                        if (defTarget == -1) defTarget = 25; // デフォルト
                    }
                }

                boolean isParrySuccess = defDice <= defTarget;
                logAppend(String.format(" ➔ [防御] %s が【受け流し】に挑戦！(目標:%d%%) | 出目:%d ➔ %s", defender, defTarget, defDice, isParrySuccess ? "成功！攻撃を武器で受け流した！" : "受け流し失敗！"));
                if (isParrySuccess) {
                    // TRPGシステムによって「ダメージ半減」か「0」か変わりますが、今回は「受け流し成功でダメージを完全に防いだ」とします
                    // 半減にしたい場合は下のダメージ処理のところで割る2にカスタマイズ可能です！
                    damageApplies = false;
                }
            }
        }

        // 3. ダメージ算出 ＆ HP減少の自動適用
        if (damageApplies) {
            int damage = rollDamage(damageDiceInput.getText().trim());
            logAppend(String.format(" ➔ 💥 ダメージダイスの結果: 【 %d ダメージ 】 が直撃！", damage));

            if (defender.equals("エネミー（敵）")) {
                if (enemyMaxHp == 0) {
                    logAppend(" ⚠️ 敵のHPが未設定のため、減少処理をスキップしました。");
                    return;
                }
                enemyCurrentHp = Math.max(0, enemyCurrentHp - damage);
                updateEnemyHpDisplay();
                logAppend(String.format(" ➔ [HP変動] 敵のHPが減少しました。"));
                if (enemyCurrentHp <= 0) logAppend(" 💀 敵（エネミー）は倒れました！");
            } else {
                CharacterFrame defFrame = findFrameByName(defender);
                if (defFrame != null) {
                    int oldHp = defFrame.findValueByName("ＨＰ");
                    if (oldHp != -1) {
                        int newHp = Math.max(0, oldHp - damage);
                        defFrame.updateHpDirectly(newHp);
                        logAppend(String.format(" ➔ [HP変動] %s のHP: %d ➔ %d", defender, oldHp, newHp));
                        if (newHp <= 0) logAppend(String.format(" 💀 %s は意識を失ったか、倒れました！", defender));
                    }
                }
            }
        } else {
            logAppend(String.format(" ➔ ✨ %s は攻撃を完全に防いだ！[ダメージ: 0]", defender));
        }
        logAppend("--------------------------------------------------\n");
    }

    private int rollDamage(String formula) {
        String cleanForm = formula.toUpperCase().replaceAll("\\s+", "");
        int total = 0;
        try {
            if (cleanForm.contains("D")) {
                String[] sub = cleanForm.split("D");
                int num = sub[0].isEmpty() ? 1 : Integer.parseInt(sub[0]);
                String sidePart = sub[1];
                int sides = 6;
                int bonus = 0;

                if (sidePart.contains("+")) {
                    sides = Integer.parseInt(sidePart.split("\\+")[0]);
                    bonus = Integer.parseInt(sidePart.split("\\+")[1]);
                } else {
                    sides = Integer.parseInt(sidePart);
                }

                for (int i = 0; i < num; i++) total += random.nextInt(sides) + 1;
                total += bonus;
            } else {
                total = Integer.parseInt(cleanForm);
            }
        } catch (Exception e) {
            total = random.nextInt(6) + 1; // エラー時は1D6をデフォルトにする安全策
        }
        return total;
    }


    private void logAppend(String text) {
        battleLogArea.append(text + "\n");
        battleLogArea.setCaretPosition(battleLogArea.getDocument().getLength()); // 最下部へ自動スクロール
    }

    private CharacterFrame findFrameByName(String name) {
        for (CharacterFrame frame : activeFrames) {
            if (frame.getCharacterName().equals(name)) return frame;
        }
        return null;
    }
    /**
     * ⚔️ 外部の「武器・戦闘ウィンドウ」の攻撃ボタンと完全合体する窓口！
     * プレイヤーが自分の画面で振った「武器の命中・ダメージ」をここに引き継いで自動処理します。
     */
    /**
     * ⚔️ 外部の「武器・戦闘ウィンドウ」の攻撃ボタンと完全合体する窓口！
     * 【完全秘匿版】敵のHPが隠されている時は、ログの計算結果も「？？？」に隠します。
     */
    public void applyExternalDamage(String attackerName, String weaponName, int damage) {
        String targetName = (String) defenderCombo.getSelectedItem();
        if (targetName == null) targetName = "エネミー（敵）";

        logAppend(String.format("\n💥 【連携攻撃】%s が「%s」で急襲！", attackerName, weaponName));

        // 1. 防御側の処理（回避か、受け流しか、無防備か）
        boolean damageApplies = true;

        if (noneRadio.isSelected()) {
            logAppend(String.format(" ➔ [防御] %s は無防備！攻撃が直撃します。", targetName));
        } else {
            int defDice = random.nextInt(100) + 1;
            int defTarget = 0;

            if (avoidRadio.isSelected()) {
                if (targetName.equals("エネミー（敵）")) {
                    defTarget = 30;
                } else {
                    CharacterFrame defFrame = findFrameByName(targetName);
                    if (defFrame != null) defTarget = defFrame.findValueByName("回避");
                }
                boolean isAvoidSuccess = defDice <= defTarget;
                logAppend(String.format(" ➔ [防御] %s が【回避】に挑戦！(目標:%d%%) | 出目:%d ➔ %s",
                        targetName, defTarget, defDice, isAvoidSuccess ? "大成功！攻撃をかわした！" : "回避失敗！"));
                if (isAvoidSuccess) damageApplies = false;

            } else if (parryRadio.isSelected()) {
                if (targetName.equals("エネミー（敵）")) {
                    defTarget = 30;
                } else {
                    CharacterFrame defFrame = findFrameByName(targetName);
                    if (defFrame != null) {
                        defTarget = defFrame.findValueByName("こぶし");
                        if (defTarget == -1) defTarget = 25;
                    }
                }
                boolean isParrySuccess = defDice <= defTarget;
                logAppend(String.format(" ➔ [防御] %s が【受け流し】に挑戦！(目標:%d%%) | 出目:%d ➔ %s",
                        targetName, defTarget, defDice, isParrySuccess ? "成功！武器で受け流した！" : "受け流し失敗！"));
                if (isParrySuccess) damageApplies = false;
            }
        }

        // 2. 確定したダメージをHPに適用する
        if (damageApplies) {
            logAppend(String.format(" ➔ 💥 確定ダメージ: 【 %d 】 が突き刺さる！", damage));

            if (targetName.equals("エネミー（敵）")) {
                if (enemyMaxHp == 0) {
                    logAppend(" ⚠️ 敵のHPが未設定です。エネミー設定の「設定」ボタンを押してください。");
                    return;
                }

                // 内部ではちゃんとHPを減らす
                enemyCurrentHp = Math.max(0, enemyCurrentHp - damage);
                updateEnemyHpDisplay();

                // ⭕ 【ここを修正！】チェックが入っている時はログの数値も隠す！
                if (hideEnemyHpCheck.isSelected()) {
                    logAppend(" ➔ [HP変動] 敵のHP: ？？？ ➔ ？？？");
                    if (enemyCurrentHp <= 0) logAppend(" 📢 【GM秘密ログ】敵（エネミー）のHPが0になりました！倒れました！");
                } else {
                    // チェックがない通常時は数値を出す
                    logAppend(String.format(" ➔ [HP変動] 敵のHP: %d ➔ %d", enemyCurrentHp + damage, enemyCurrentHp));
                    if (enemyCurrentHp <= 0) logAppend(" 💀 敵（エネミー）は倒れました！");
                }

            } else {
                // 防御側がプレイヤーキャラクターの場合（こちらは常に数値を表示）
                CharacterFrame defFrame = findFrameByName(targetName);
                if (defFrame != null) {
                    int oldHp = defFrame.findValueByName("ＨＰ");
                    if (oldHp != -1) {
                        int newHp = Math.max(0, oldHp - damage);
                        defFrame.updateHpDirectly(newHp);
                        logAppend(String.format(" ➔ [HP変動] %s のHP: %d ➔ %d", targetName, oldHp, newHp));
                        if (newHp <= 0) logAppend(String.format(" 💀 %s は倒れました！", targetName));
                    }
                }
            }
        } else {
            logAppend(String.format(" ➔ ✨ %s は攻撃を完全に防いだ！[ダメージ: 0]", targetName));
        }
        logAppend("--------------------------------------------------\n");
    }

    // ⚠️ 二重定義になっていた「private CharacterFrame findFrameByName(String name)」の塊は丸ごと削除しました！
}