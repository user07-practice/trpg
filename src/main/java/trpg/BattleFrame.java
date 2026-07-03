package trpg;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleFrame extends JFrame {
    private Random random = new Random();

    // コンポーネント（選択系）
    private JComboBox<String> attackerCombo;
    private JComboBox<String> defenderCombo;
    private JComboBox<String> weaponSelectCombo; // 攻撃側の武器を自動で選ぶ枠
    private JComboBox<String> modeCombo;         // 1回 / 2発 / 3発 連射モード

    // 防御側の選択
    private JRadioButton avoidRadio;
    private JRadioButton parryRadio;
    private JRadioButton noneRadio;
    private JLabel defenderStatusLabel;

    // 敵（エネミー）のデータ
    private int enemyMaxHp = 0;
    private int enemyCurrentHp = 0;
    private JLabel enemyHpLabel;
    private JTextField enemyMaxHpInput;
    private JCheckBox hideEnemyHpCheck;

    // 実況ログ
    private JTextArea battleLogArea;

    private List<CharacterFrame> activeFrames = new ArrayList<>();
    private List<String[]> currentAttackerWeapons = new ArrayList<>(); // 選択中のキャラの武器データ保持用

    public BattleFrame(CharacterFrame currentFrame) {
        setTitle("⚔️ 統合型・決戦バトルステージ");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(currentFrame);
        setLayout(new BorderLayout(10, 10));

        // 立ち上がっているキャラシの読み込み
        refreshActiveCharacters();

        // ─── 全体のレイアウト（左・中央・右の3分割パネル） ───
        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 📌 【左パネル】攻撃側の宣言
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createTitledBorder("⚔️ 1. 攻撃側の操作"));

        attackerCombo = new JComboBox<>();
        attackerCombo.addActionListener(e -> onAttackerSelected()); // 攻撃者を変えたら武器一覧を自動更新！

        weaponSelectCombo = new JComboBox<>();
        weaponSelectCombo.setFont(new Font("Meiryo", Font.PLAIN, 12));

        modeCombo = new JComboBox<>(new String[]{"💥 1回攻撃", "🔥 2発連射", "⚡ 3発連射"});
        modeCombo.setFont(new Font("Meiryo", Font.BOLD, 12));

        JPanel atkGrid = new JPanel(new GridLayout(3, 2, 5, 8));
        atkGrid.add(new JLabel("攻撃する人:"));
        atkGrid.add(attackerCombo);
        atkGrid.add(new JLabel("使用する武器:"));
        atkGrid.add(weaponSelectCombo);
        atkGrid.add(new JLabel("攻撃モード:"));
        atkGrid.add(modeCombo);
        leftPanel.add(atkGrid);
        leftPanel.add(Box.createVerticalStrut(20));

        // エネミー設定を左パネルの下部に配置
        JPanel enemyConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        enemyConfigPanel.setBorder(BorderFactory.createTitledBorder("エネミー初期設定"));
        enemyMaxHpInput = new JTextField("30", 4);
        JButton setEnemyBtn = new JButton("設定");
        setEnemyBtn.addActionListener(e -> setEnemyHp());
        hideEnemyHpCheck = new JCheckBox("HPを隠す");
        hideEnemyHpCheck.addActionListener(e -> updateEnemyHpDisplay());

        enemyConfigPanel.add(new JLabel("最大HP:"));
        enemyConfigPanel.add(enemyMaxHpInput);
        enemyConfigPanel.add(setEnemyBtn);
        enemyConfigPanel.add(hideEnemyHpCheck);
        leftPanel.add(enemyConfigPanel);

        // 📌 【中央パネル】防御側の選択
        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
        midPanel.setBorder(BorderFactory.createTitledBorder("🛡️ 2. 防御側の対応"));

        defenderCombo = new JComboBox<>();
        defenderCombo.addActionListener(e -> updateDefenderStatusDisplay());

        JPanel defGrid = new JPanel(new GridLayout(1, 2, 5, 5));
        defGrid.add(new JLabel("防御・回避する人:"));
        defGrid.add(defenderCombo);
        midPanel.add(defGrid);
        midPanel.add(Box.createVerticalStrut(15));

        avoidRadio = new JRadioButton("回避を試みる（成功でダメージ0）", true);
        parryRadio = new JRadioButton("受け流し（こぶし等でガード）");
        noneRadio = new JRadioButton("無防備（直撃を受ける）");
        ButtonGroup bg = new ButtonGroup();
        bg.add(avoidRadio); bg.add(parryRadio); bg.add(noneRadio);

        JPanel radioPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        radioPanel.add(avoidRadio);
        radioPanel.add(parryRadio);
        radioPanel.add(noneRadio);
        midPanel.add(radioPanel);
        midPanel.add(Box.createVerticalStrut(15));

        defenderStatusLabel = new JLabel("ステータス ➔ 回避: --% | 受け流し: --%");
        defenderStatusLabel.setFont(new Font("Meiryo", Font.ITALIC, 11));
        midPanel.add(defenderStatusLabel);
        midPanel.add(Box.createVerticalStrut(30));

        // 💥 すべてを全自動ジャッジする運命のボタン！
        JButton executeBtn = new JButton("🎲 選択内容で戦闘ロール実行！");
        executeBtn.setFont(new Font("Meiryo", Font.BOLD, 14));
        executeBtn.setBackground(new Color(255, 215, 0)); // 豪華なゴールドカラー
        executeBtn.setPreferredSize(new Dimension(200, 50));
        executeBtn.addActionListener(e -> executeBattleRound());
        midPanel.add(executeBtn);

        // 📌 【右パネル】HP表示 ＆ 実況ログ
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("📊 3. 戦況・リアルタイム実況"));

        enemyHpLabel = new JLabel("敵のHP: 未設定", SwingConstants.CENTER);
        enemyHpLabel.setFont(new Font("Meiryo", Font.BOLD, 22));
        enemyHpLabel.setForeground(Color.RED);
        enemyHpLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        rightPanel.add(enemyHpLabel, BorderLayout.NORTH);

        battleLogArea = new JTextArea();
        battleLogArea.setEditable(false);
        battleLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        battleLogArea.setBackground(new Color(248, 248, 248));
        battleLogArea.setLineWrap(true);       // 画面の端で自動的に折り返す
        battleLogArea.setWrapStyleWord(true);  // 単語の途中で変な折り返しをしないようにする
        JScrollPane logScroll = new JScrollPane(battleLogArea);
        rightPanel.add(logScroll, BorderLayout.CENTER);

        // 各パネルをがっちゃんこ
        centerPanel.add(leftPanel);
        centerPanel.add(midPanel);
        centerPanel.add(rightPanel);
        add(centerPanel, BorderLayout.CENTER);

        // 下部コントロールバー（メンバー更新ボタンなど）
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("🔄 参加メンバー・キャラシ一覧を更新");
        refreshBtn.addActionListener(e -> refreshAll());
        bottomBar.add(refreshBtn);
        add(bottomBar, BorderLayout.SOUTH);

        // 初期データ詰め込み
        refreshAll();
        setVisible(true);
    }

    private void refreshAll() {
        refreshActiveCharacters();
        populateCombos();
        onAttackerSelected();
        updateDefenderStatusDisplay();
        updateEnemyHpDisplay();
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

    // 攻撃者が選ばれたら、そのキャラクターの武器データをキャラシから泥棒してドロップダウンにセットする
    private void onAttackerSelected() {
        String attackerName = (String) attackerCombo.getSelectedItem();
        weaponSelectCombo.removeAllItems();
        currentAttackerWeapons.clear();

        if (attackerName == null) return;

        if (attackerName.equals("エネミー（敵）")) {
            weaponSelectCombo.addItem("通常攻撃 (1D6)");
            currentAttackerWeapons.add(new String[]{"通常攻撃", "1D6", "", ""});
            weaponSelectCombo.addItem("強攻撃 (2D6)");
            currentAttackerWeapons.add(new String[]{"強攻撃", "2D6", "", ""});
        } else {
            CharacterFrame atkFrame = findFrameByName(attackerName);
            if (atkFrame != null) {
                try {
                    // キャラシから武器データを直接引っ張る
                    List<String[]> wData = atkFrame.getExtendedWeaponData();
                    for (String[] w : wData) {
                        if (!w[0].isEmpty() && !w[1].isEmpty()) {
                            weaponSelectCombo.addItem(w[0] + " (" + w[1] + ")");
                            currentAttackerWeapons.add(w);
                        }
                    }
                } catch (Exception e) {
                    // メソッドがない場合のセーフティ
                    weaponSelectCombo.addItem("こぶし (1D3)");
                    currentAttackerWeapons.add(new String[]{"こぶし", "1D3", "", ""});
                }
            }
            if (weaponSelectCombo.getItemCount() == 0) {
                weaponSelectCombo.addItem("素手 (1D3)");
                currentAttackerWeapons.add(new String[]{"素手", "1D3", "", ""});
            }
        }
    }

    private void updateDefenderStatusDisplay() {
        String defenderName = (String) defenderCombo.getSelectedItem();
        if (defenderName == null) return;

        if (defenderName.equals("エネミー（敵）")) {
            defenderStatusLabel.setText("ステータス ➔ 回避: 30% | 受け流し: 30%");
        } else {
            CharacterFrame defFrame = findFrameByName(defenderName);
            if (defFrame != null) {
                int avoid = defFrame.findValueByName("回避");
                int parry = defFrame.findValueByName("こぶし");
                if (parry == -1) parry = 25;
                defenderStatusLabel.setText(String.format("➔ 回避: %d%% | 受け流し(こぶし): %d%%", avoid, parry));
            }
        }
    }

    private void setEnemyHp() {
        try {
            int hp = Integer.parseInt(enemyMaxHpInput.getText().trim());
            enemyMaxHp = hp;
            enemyCurrentHp = hp;
            updateEnemyHpDisplay();
            logAppend("📢 エネミーのHPが初期化されました。 (最大HP: " + hp + ")");
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

    // 🌟 攻撃ダイス・防御ダイス・HP減少をすべて全自動でワンアクション処理！
    private void executeBattleRound() {
        String attacker = (String) attackerCombo.getSelectedItem();
        String defender = (String) defenderCombo.getSelectedItem();
        int weaponIdx = weaponSelectCombo.getSelectedIndex();

        if (attacker == null || defender == null || weaponIdx < 0) return;

        String[] selectedWeapon = currentAttackerWeapons.get(weaponIdx);
        String weaponName = selectedWeapon[0];
        int modeIdx = modeCombo.getSelectedIndex();

        logAppend(String.format("\n【戦闘ラウンド】%s  vs  %s", attacker, defender));
        logAppend(String.format(" ➔ %s は「%s」を構えた！", attacker, weaponName));

        // 1. 命中判定（一律50%にするか、本来は技能値を見たいですが、今回は戦闘をスムーズにするため簡易命中50%とします。必要なら技能値連動へ改造可能）
        int atkDice = random.nextInt(100) + 1;
        boolean isHit = atkDice <= 50;
        logAppend(String.format(" ➔ [攻撃判定] 簡易命中率:50%% | 出目:%d ➔ %s", atkDice, isHit ? "成功！🎯" : "外れた！😭"));

        if (!isHit) {
            logAppend(" ➔ 攻撃が不発に終わったため、ラウンド終了です。");
            logAppend("--------------------------------------------------\n");
            return;
        }

        // 2. 防御側のジャッジ
        boolean damageApplies = true;
        if (!noneRadio.isSelected()) {
            int defDice = random.nextInt(100) + 1;
            int defTarget = 0;

            if (avoidRadio.isSelected()) {
                if (defender.equals("エネミー（敵）")) { defTarget = 30; }
                else {
                    CharacterFrame defFrame = findFrameByName(defender);
                    if (defFrame != null) defTarget = defFrame.findValueByName("回避");
                }
                boolean isAvoidSuccess = defDice <= defTarget;
                logAppend(String.format(" ➔ [防御判定] %s の【回避】(目標:%d%%) | 出目:%d ➔ %s", defender, defTarget, defDice, isAvoidSuccess ? "成功！華麗に避けた！" : "回避失敗！"));
                if (isAvoidSuccess) damageApplies = false;

            } else if (parryRadio.isSelected()) {
                if (defender.equals("エネミー（敵）")) { defTarget = 30; }
                else {
                    CharacterFrame defFrame = findFrameByName(defender);
                    if (defFrame != null) {
                        defTarget = defFrame.findValueByName("こぶし");
                        if (defTarget == -1) defTarget = 25;
                    }
                }
                boolean isParrySuccess = defDice <= defTarget;
                logAppend(String.format(" ➔ [防御判定] %s の【受け流し】(目標:%d%%) | 出目:%d ➔ %s", defender, defTarget, defDice, isParrySuccess ? "成功！攻撃をガードした！" : "受け流し失敗！"));
                if (isParrySuccess) damageApplies = false;
            }
        }

        // 3. ダメージ計算（連射モード対応！）
        if (damageApplies) {
            int totalDamage = 0;

            // 1発目
            int d1 = rollDamage(selectedWeapon[1]);
            totalDamage += d1;
            logAppend(String.format(" ➔ 💥 1発目のダメージ: [%d]", d1));

            // 連射分（データが空でなければ振る）
            if (modeIdx >= 1 && !selectedWeapon[2].isEmpty()) {
                int d2 = rollDamage(selectedWeapon[2]);
                totalDamage += d2;
                logAppend(String.format(" ➔ 💥 2発目のダメージ: [%d]", d2));
            }
            if (modeIdx == 2 && !selectedWeapon[3].isEmpty()) {
                int d3 = rollDamage(selectedWeapon[3]);
                totalDamage += d3;
                logAppend(String.format(" ➔ 💥 3発目のダメージ: [%d]", d3));
            }

            logAppend(String.format(" ➔ 🛑 総直撃ダメージ: 【 %d 】", totalDamage));

            // HP減少の適用
            if (defender.equals("エネミー（敵）")) {
                if (enemyMaxHp == 0) {
                    logAppend(" ⚠️ 敵のHPが未設定です。初期設定を行ってください。");
                    return;
                }
                enemyCurrentHp = Math.max(0, enemyCurrentHp - totalDamage);
                updateEnemyHpDisplay();

                if (hideEnemyHpCheck.isSelected()) {
                    logAppend(" ➔ [HP変動] 敵のHP: ？？？ ➔ ？？？");
                    if (enemyCurrentHp <= 0) logAppend(" 📢 【GM秘密ログ】エネミーのHPが0になりました！");
                } else {
                    logAppend(String.format(" ➔ [HP変動] 敵のHP: %d ➔ %d", enemyCurrentHp + totalDamage, enemyCurrentHp));
                    if (enemyCurrentHp <= 0) logAppend(" 💀 敵（エネミー）は倒れました！");
                }
            } else {
                // プレイヤーキャラのHP減少
                CharacterFrame defFrame = findFrameByName(defender);
                if (defFrame != null) {
                    int oldHp = defFrame.findValueByName("ＨＰ");
                    if (oldHp != -1) {
                        int newHp = Math.max(0, oldHp - totalDamage);
                        defFrame.updateHpDirectly(newHp);
                        logAppend(String.format(" ➔ [HP変動] %s のHP: %d ➔ %d", defender, oldHp, newHp));
                        if (newHp <= 0) logAppend(String.format(" 💀 %s は倒れました！", defender));
                    }
                }
            }
        } else {
            logAppend(String.format(" ➔ ✨ %s は攻撃を完全にシャットアウトした！[ダメージ: 0]", defender));
        }
        logAppend("--------------------------------------------------\n");
    }

    private int rollDamage(String formula) {
        if (formula == null || formula.isEmpty()) return 0;
        String clean = formula.toUpperCase().replaceAll("\\s+", "");
        int total = 0;
        try {
            String[] parts = clean.split("\\+");
            for (String part : parts) {
                if (part.contains("D")) {
                    String[] sub = part.split("D");
                    int count = Integer.parseInt(sub[0]);
                    int sides = Integer.parseInt(sub[1]);
                    for (int i = 0; i < count; i++) total += random.nextInt(sides) + 1;
                } else {
                    total += Integer.parseInt(part);
                }
            }
        } catch (Exception e) {
            total = random.nextInt(6) + 1; // エラー時安全用1D6
        }
        return total;
    }

    private void logAppend(String text) {
        battleLogArea.append(text + "\n");
        battleLogArea.setCaretPosition(battleLogArea.getDocument().getLength());
    }

    private CharacterFrame findFrameByName(String name) {
        for (Window window : Window.getWindows()) {
            if (window instanceof CharacterFrame && window.isShowing()) {
                CharacterFrame cf = (CharacterFrame) window;
                if (cf.getCharacterName().equals(name)) return cf;
            }
        }
        return null;
    }
}