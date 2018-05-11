package io.dvlopt.linux.i2c ;


import com.sun.jna.Pointer                           ;
import io.dvlopt.linux.i2c.I2CBuffer                 ;
import io.dvlopt.linux.i2c.I2CFlags                  ;
import io.dvlopt.linux.i2c.internal.NativeI2CMessage ;




public class I2CMessage {


    Pointer ptr ;

    private I2CBuffer buffer ;





    I2CMessage( Pointer ptr ) {
    
        this.ptr = ptr ;
    }




    public int getAddress() {
    
        return this.ptr.getShort( NativeI2CMessage.OFFSET_ADDRESS ) & 0xffffff ;
    }




    public I2CMessage setAddress( int address ) {
    
        this.ptr.setShort( NativeI2CMessage.OFFSET_ADDRESS ,
                           (short)address                  ) ;

        return this ;
    }




    public I2CFlags getFlags() {
    
        return new I2CFlags( this.ptr.getShort( NativeI2CMessage.OFFSET_FLAGS ) & 0xffffff ) ;
    }




    public I2CMessage setFlags( I2CFlags flags ) {
    
        this.ptr.setShort( NativeI2CMessage.OFFSET_FLAGS ,
                           (short)( flags.value )        ) ;

        return this ;
    }




    public I2CBuffer getBuffer() {

        return this.buffer ;
    }




    public I2CMessage setBuffer( I2CBuffer buffer ) {

        this.buffer = buffer ;
    
        this.ptr.setPointer( NativeI2CMessage.OFFSET_BUFFER ,
                             buffer.memory                  ) ;

        this.ptr.setShort( NativeI2CMessage.OFFSET_LENGTH ,
                           (short)( buffer.length )       ) ;

        return this ;
    }
}
