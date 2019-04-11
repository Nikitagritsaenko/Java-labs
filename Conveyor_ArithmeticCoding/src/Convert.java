import java.nio.ByteBuffer;

public class Convert {
    /**
     * Converts one double to byte array
     * @param number the double number to convert
     * @return converted array of bytes
     */
    public static byte[] DoubleToByteArray(double number) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
        byteBuffer.putDouble(number);
        return byteBuffer.array();
    }

    /**
     * Converts byte array to double
     * @param data the array of bytes to convert
     * @return converted double number
     */
    public static double ByteArrayToDouble(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
        byteBuffer.put(data);
        byteBuffer.flip();
        return byteBuffer.getDouble(0);
    }

    /**
     * Converts byte array to one character
     * @param data the array of bytes to convert
     * @return converted char
     */
    public static char ByteArrayToCharacter(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Character.BYTES);
        byteBuffer.put(data);
        byteBuffer.flip();
        return byteBuffer.getChar(0);
    }

    /**
     * Converts byte array to one integer
     * @param data the array of bytes to convert
     * @return converted integer
     */
    public static int ByteArrayToInt(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
        byteBuffer.put(data);
        byteBuffer.flip();
        return byteBuffer.getInt(0);
    }

    /**
     * Converts one character to byte array
     * @param c the character to convert
     * @return converted array of bytes
     */
    public static byte[] CharacterToByteArray(char c) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Character.BYTES);
        byteBuffer.putChar(c);
        return byteBuffer.array();
    }


    /**
     * Converts one integer to byte array
     * @param n the integer to convert
     * @return converted array of bytes
     */
    public static byte[] IntegerToByteArray(int n) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
        byteBuffer.putInt(n);
        return byteBuffer.array();
    }

    /**
     * Converts double array to byte array
     * @param data double array to convert
     * @return converted byte array
     */
    public static byte[] DoubleArrayToByteArray(double[] data) {
        byte[] result = new byte[data.length * Double.BYTES];
        for (int i = 0; i < data.length; i++) {
            byte[] convertedDouble = DoubleToByteArray(data[i]);
            for (int j = 0; j < Double.BYTES; j++) {
                result[i * Double.BYTES + j] = convertedDouble[j];
            }
        }
        return result;
    }

    /**
     * Converts char array to byte array
     * @param data char array to convert
     * @return converted byte array
     */
    public static byte[] CharArrayToByteArray(char[] data) {
        byte[] result = new byte[data.length * Character.BYTES];
        for (int i = 0; i < data.length; i++) {
            byte[] convertedChar = CharacterToByteArray(data[i]);
            for (int j = 0; j < Character.BYTES; j++) {
                result[i * Character.BYTES + j] = convertedChar[j];
            }
        }
        return result;
    }
}
