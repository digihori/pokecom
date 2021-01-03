package tk.horiuchi.pokecom;

public class Queue {
    private Object[] data;
    private int front;
    private int rear;

    public Queue(int size) {
        data = new Object[size+1];
        front = 0;
        rear = 0;
    }

    public boolean offer(Object o) {
        // 配列をリングバッファとして考えた場合、一周回った（満杯）かをチェック
        if ((rear + 1) % data.length == front) {
            return false;
        }
        data[rear] = o;
        // リングバッファの次の格納位置を計算
        rear = (rear + 1) % data.length;
        return true;
    }

    public Object poll() {
        // キューが空
        if (front == rear) {
            return null;
        }
        Object o = data[front];
        data[front] = null;
        // リングバッファの次の先頭位置を計算
        front = (front + 1) % data.length;
        return o;
    }

    public int getNumOfData() {
        return ((rear >= front) ? rear - front : rear + data.length - front);
    }
}
