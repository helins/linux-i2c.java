package io.dvlopt.linux.i2c ;


import com.sun.jna.Memory ;




public class I2CBuffer {


    final Memory memory ;

    public final int length ;




    public I2CBuffer( int length ) {
    
        this.memory = new Memory( length ) ;
        this.length = length               ;

        this.memory.clear() ;
    }




    public int get( int index ) {
    
        return ( this.memory.getByte( index ) ) & 0xffffffff ;
    }




    public I2CBuffer set( int index ,
                          int b     ) {
    
        this.memory.setByte( index   ,
                             (byte)b ) ;

        return this ;
    }




    public I2CBuffer clear() {
    
        this.memory.clear() ;

        return this ;
    }
}
