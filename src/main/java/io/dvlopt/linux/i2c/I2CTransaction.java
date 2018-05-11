package io.dvlopt.linux.i2c ;


import com.sun.jna.Memory                              ;
import com.sun.jna.Pointer                             ;
import io.dvlopt.linux.i2c.I2CMessage                  ;
import io.dvlopt.linux.i2c.internal.NativeI2CIoctlData ;
import io.dvlopt.linux.i2c.internal.NativeI2CMessage   ;




public class I2CTransaction {


    public final static int MAX_LENGTH = 42 ;


    Memory memory         ;
    Memory memoryMessages ;

    private I2CMessage[] messages ;

    public final int length ;




    public I2CTransaction( int length ) {
    
        if ( length > MAX_LENGTH ) {
        
            throw new IllegalArgumentException( "The requested number of messages is higher than what is supported" ) ;
        }

        this.memoryMessages = new Memory( length * NativeI2CMessage.SIZE ) ;

        this.memoryMessages.clear() ;

        this.memory = new Memory( NativeI2CIoctlData.SIZE ) ;

        this.memory.setPointer( NativeI2CIoctlData.OFFSET_MESSAGES ,
                                this.memoryMessages                ) ;

        this.memory.setInt( NativeI2CIoctlData.OFFSET_LENGTH ,
                            length                           ) ;

        this.messages = new I2CMessage[ length ] ;

        for ( int i = 0  ;
              i < length ;
              i += 1     ) {

            this.messages[ i ] = new I2CMessage( this.memoryMessages.share( i * NativeI2CMessage.SIZE ) ) ;
        }

        this.length = length ;
    }




    public I2CMessage getMessage( int index ) {
    
        return this.messages[ index ] ;
    }
}
