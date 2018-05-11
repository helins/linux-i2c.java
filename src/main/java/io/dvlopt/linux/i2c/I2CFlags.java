package io.dvlopt.linux.i2c ;


import io.dvlopt.linux.i2c.I2CFlag ;




public class I2CFlags {


    int value ;



    public I2CFlags() {
    
        clear() ;
    }




    I2CFlags( int value ) {
    
        this.value = value ;
    }




    public boolean isSet( I2CFlag flag ) {
    
        return ( this.value & flag.value ) > 0 ;
    }




    public I2CFlags set( I2CFlag flag ) {

        this.value |= flag.value ;

        return this ;
    }




    public I2CFlags unset( I2CFlag flag ) {
    
        this.value &= ~( flag.value ) ;

        return this ;
    }




    public I2CFlags clear() {
    
        this.value = 0 ;

        return this ;
    }
}
