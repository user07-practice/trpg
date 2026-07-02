package trpg;

public class Main {
    public static void main(String[] args) {
        // 1. 共通のダイスログ画面を1つだけ立ち上げる
        DiceLogFrame logFrame = new DiceLogFrame();

        // 2. 1枚目のキャラシ画面（1人目）を立ち上げる
        CharacterData data1 = new CharacterData();
        CharacterFrame frame1 = new CharacterFrame(data1, logFrame);

        // 3. ★ここがポイント！★
        // すでに完成した「frame1」を渡して、汎用ダイス画面を【ここで1回だけ】立ち上げる
        new GenericDiceFrame(frame1, logFrame);

        // 4. 2枚目のキャラシ画面（2人目）を同時に立ち上げる
        CharacterData data2 = new CharacterData();
        CharacterFrame frame2 = new CharacterFrame(data2, logFrame);
        frame2.setLocation(150, 100);
    }
}