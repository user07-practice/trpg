package trpg;
/*
クトゥルフ神話RPGのキャラシート・ダイスロール
キャラクターシート・ダイスロール・結果が残っていく
これらを実装したい。

CharacterData.java（データの管理役）
画面とは別で、純粋に「名前」「HP」「SAN値」「目星の数値」などのデータを記憶しておくためのクラスです。（C言語でいう struct（構造体）を強化したようなものです）
ウィンドウから直接書き込める（個々にステータス、技能は変わる・設定も変わるため）
SAN値は絶えず変わっていくため、今のSAN値の参照・毎回変わるダイスロール条件・減ったときはそれを反映（減る分の値も毎回変わる）

CharacterFrame.java（キャラシの画面）
ステータスや技能ボタンが並ぶウィンドウ。ここをポチッと押すと、ダイスを振る処理が走ります。

DiceLogFrame.java（ダイスとログの画面）
ダイスの結果が大きく出たり、これまでの行動履歴がずらりと残ったりするウィンドウ。
 */

import java.util.HashMap;
import java.util.Map;

public class CharacterData implements java.io.Serializable {

    // ===== 1. 基礎情報 ＆ キャラクター設定 =====
    private String name = "名無しの探索者";
    private String profile = "大学生";               // キャラクター設定（職業や経歴など）
    private String imagePath = "images/default.png"; // 立ち絵画像のパス

    // ===== 2. みんな持っているステータス =====
    private int str = 50;
    private int con = 50;
    private int pow = 50;
    private int dex = 50;
    private int app = 50;
    private int siz = 50;
    private int intStat = 50; // ※「int」はJavaの予約語なので「intStat」にしています
    private int edu = 50;

    // 変動する重要なステータス
    private int hp = 10;
    private int mp = 10;
    private int san = 50;     // ユーザーさん念願の「SAN値」！

    // ===== 3. 技能（目星など） =====
    // 「技能名」と「成功率の数値」をペアで管理するマップです
    private Map<String, Integer> skills = new HashMap<>();

    // コンストラクタ（初期値の設定）
    public CharacterData() {
        // 最初から持っている代表的な技能をいくつかセットしておきます
        skills.put("目星", 25);
        skills.put("聞き耳", 25);
        skills.put("図書館", 20);
        skills.put("心理学", 10);
        skills.put("オカルト", 5);
    }

    // ===== 4. データを出し入れするためのゲッター・セッター =====
    // (Javaの「カプセル化」というルールに基づき、安全にデータを読み書きします)

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // ステータス類のゲッター・セッター
    public int getStr() { return str; }
    public void setStr(int str) { this.str = str; }

    public int getCon() { return con; }
    public void setCon(int con) { this.con = con; }

    public int getPow() { return pow; }
    public void setPow(int pow) { this.pow = pow; }

    public int getDex() { return dex; }
    public void setDex(int dex) { this.dex = dex; }

    public int getApp() { return app; }
    public void setApp(int app) { this.app = app; }

    public int getSiz() { return siz; }
    public void setSiz(int siz) { this.siz = siz; }

    public int getIntStat() { return intStat; }
    public void setIntStat(int intStat) { this.intStat = intStat; }

    public int getEdu() { return edu; }
    public void setEdu(int edu) { this.edu = edu; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getMp() { return mp; }
    public void setMp(int mp) { this.mp = mp; }

    public int getSan() { return san; }
    public void setSan(int san) { this.san = san; }

    // 技能マップをごっそり取得する用
    public Map<String, Integer> getSkills() { return skills; }

    // 特定の技能の値を変更する用（例: skills.put("目星", 75); みたいな感じで使います）
    public void setSkillValue(String skillName, int value) {
        skills.put(skillName, value);
    }
}