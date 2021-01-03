package tk.horiuchi.pokecom;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Created by yoshimine on 2017/10/21.
 */

public class DigitalSoundGenerator {


    private AudioTrack audioTrack;

    // サンプリング周波数
    private int sampleRate;
    // バッファ・サイズ
    private int bufferSize;

    /*
     * コンストラクタ
     */
    public DigitalSoundGenerator(int sampleRate, int bufferSize) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;

        // AudioTrackを作成
        this.audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,  // 音楽ストリームを設定
                sampleRate, // サンプルレート
                AudioFormat.CHANNEL_OUT_MONO, // モノラル
                AudioFormat.ENCODING_DEFAULT,   // オーディオデータフォーマットPCM16とかPCM8とか
                bufferSize, // バッファ・サイズ
                AudioTrack.MODE_STATIC); // Streamモード。データを書きながら再生する
    }

    /*
     * サウンド生成
     * @param frequency 鳴らしたい音の周波数
     * @param soundLengh 音の長さ
     * @return 音声データ
     */
    public byte[] getSound(double frequency, double soundLength) {
        // byteバッファを作成
        byte[] buffer = new byte[(int)Math.ceil(bufferSize * soundLength)];
        for(int i=0; i<buffer.length; i++) {
            double wave = i / (this.sampleRate / frequency) * (Math.PI * 2);
            wave = Math.sin(wave);
            buffer[i] = (byte)(wave > 0.0 ? Byte.MAX_VALUE : Byte.MIN_VALUE);
        }

        return buffer;
    }

    /*
     *
     * @return
     */
    public AudioTrack getAudioTrack() {
        return this.audioTrack;
    }
}
