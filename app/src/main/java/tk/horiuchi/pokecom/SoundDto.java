package tk.horiuchi.pokecom;

/**
 * Created by yoshimine on 2017/10/21.
 */

public class SoundDto {

    // 音声データ
    private byte[] sound;
    // 長さ
    private double length;

    /*
     * 引数付きコンストラクタ
     * @param source
     * @param length
     */
    public SoundDto(byte[] source, double length) {
        this.sound = source;
        this.length = length;
    }

    public byte[] getSound() {
        return sound;
    }
    public void setSound(byte[] sound) {
        this.sound = sound;
    }
    public double getLength() {
        return length;
    }
    public void setLength(double length) {
        this.length = length;
    }
}
