package io.dvlopt.linux.i2c ;




public enum I2CFlag {


    TEN         ( 0x00000010 ) ,
    READ        ( 0x00000001 ) ,
    NO_START    ( 0x00004000 ) ,
    REV_DIR_ADDR( 0x00002000 ) ,
    IGNORE_NAK  ( 0x00001000 ) ,
    NO_READ_ACK ( 0x00000800 ) ;




    final int value ;




    private I2CFlag( int value ) {
    
        this.value = value ;
    }
}
