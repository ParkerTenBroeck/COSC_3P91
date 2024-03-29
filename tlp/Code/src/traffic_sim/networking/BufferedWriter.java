package traffic_sim.networking;

import java.io.OutputStream;

public class BufferedWriter extends OutputStream {
    private int index = 0;
    private byte[] data;

    public int getSize(){
        return this.index;
    }

    public byte[] getAllData(){
        return this.data;
    }

    public void clear() {
        this.index = 0;
    }

    public BufferedWriter(){
        this.data = new byte[1 << 16];
    }
    public BufferedWriter(int size){
        this.data = new byte[size];
    }

    public BufferedWriter(byte[] data){
        this.data = data;
    }

    public BufferedWriter(byte[] data, int index){
        this.data = data;
        this.index = index;
    }

    public void writeByte(byte v) {
        this.data[this.index++] = v;
    }

    public void writeShort(short v) {
        this.data[this.index++] = (byte) (v >> 8);
        this.data[this.index++] = (byte) v;
    }

    public void writeInt(int v) {
        this.data[this.index++] = (byte) (v >> 24);
        this.data[this.index++] = (byte) (v >> 16);
        this.data[this.index++] = (byte) (v >> 8);
        this.data[this.index++] = (byte) v;
    }

    public void writeFloat(float v) {
        this.writeInt(Float.floatToRawIntBits(v));
    }

    public void writeLong(long v) {
        this.data[this.index++] = (byte) (v >> 56);
        this.data[this.index++] = (byte) (v >> 48);
        this.data[this.index++] = (byte) (v >> 40);
        this.data[this.index++] = (byte) (v >> 32);
        this.data[this.index++] = (byte) (v >> 24);
        this.data[this.index++] = (byte) (v >> 16);
        this.data[this.index++] = (byte) (v >> 8);
        this.data[this.index++] = (byte) v;
    }

    public void writeDouble(double v) {
        this.writeLong(Double.doubleToRawLongBits(v));
    }

    public void writeString(String s) {
        var bytes = s.getBytes();
        this.writeShort((short)bytes.length);
        System.arraycopy(bytes, 0, this.data, this.index, bytes.length);
        this.index += bytes.length;
    }

    @Override
    public void write(int b) {
        this.data[this.index++] = (byte)b;
    }
}
