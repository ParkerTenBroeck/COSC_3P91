package traffic_sim.networking;

import java.io.IOException;
import java.io.InputStream;

public class Reader extends InputStream {
    private final InputStream other;

    public Reader(InputStream in){
        this.other = in;
    }

    @Override
    public int available() throws IOException {
        return other.available();
    }

    /**
     * @return reads a byte from the input stream
     */
    public byte readByte() throws IOException {
        return (byte)this.other.read();
    }

    /**
     * @return reads a short from the input stream
     */
    public short readShort() throws IOException {
        var b2 = this.other.read() << 8;
        var b1 = this.other.read() ;
        return (short)(b2 | b1);
    }

    /**
     * @return reads a int from the input stream
     */
    public int readInt() throws IOException {
        var b4 = this.other.read() << 24;
        var b3 = this.other.read() << 16;
        var b2 = this.other.read() << 8;
        var b1 = this.other.read() ;
        return b4 | b3 | b2 | b1;
    }


    /**
     * @return reads a float from the input stream
     */
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(this.readInt());
    }

    @Override
    public int read() throws IOException {
        return other.read();
    }


    /**
     * @return reads a long from the input stream
     */
    public long readLong() throws IOException {
        var b8 = ((long)this.other.read()) << 56;
        var b7 = ((long)this.other.read()) << 48;
        var b6 = ((long)this.other.read()) << 40;
        var b5 = ((long)this.other.read()) << 32;
        var b4 = ((long)this.other.read()) << 24;
        var b3 = ((long)this.other.read()) << 16;
        var b2 = ((long)this.other.read()) << 8;
        var b1 = ((long)this.other.read());
        return b8 | b7 | b6 | b5 | b4 | b3 | b2 | b1;
    }


    /**
     * @return reads a string from the input stream
     */
    public String readString() throws IOException {
        int len = this.readShort();
        return new String(this.readNBytes(len));
    }
}
