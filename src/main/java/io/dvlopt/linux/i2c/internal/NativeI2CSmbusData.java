package io.dvlopt.linux.i2c.internal ;


import com.sun.jna.Union ;




public class NativeI2CSmbusData extends Union {


    // I2C_SMBUS_BLOCK_MAX( 32 ) + 2( 1 for length + 1 for PEC)
    //
    public static final int SIZE = 34 ;




    public byte   byt                      ;
    public short  word                     ;
    public byte[] block = new byte[ SIZE ] ;
}
