package io.dvlopt.linux.i2c ;


import com.sun.jna.Memory           ;
import io.dvlopt.linux.NativeMemory ;




public class I2CBuffer {


    final Memory memory ;

    public final int length ;




    public I2CBuffer( int length ) {
    
        this.memory = new Memory( length ) ;
        this.length = length               ;

        this.memory.clear() ;
    }




    public int get( int index ) {

        return NativeMemory.getUnsignedByte( this.memory ,
                                             index       ) ;
    }




    public I2CBuffer set( int index ,
                          int b     ) {

        NativeMemory.setUnsignedByte( this.memory   ,
                                      index         ,
                                      b             ) ;

        return this ;
    }




    public I2CBuffer clear() {
    
        this.memory.clear() ;

        return this ;
    }
}
