package io.dvlopt.linux.i2c.internal ;


import com.sun.jna.Pointer   ;
import com.sun.jna.Structure ;
import java.util.Arrays      ;
import java.util.List        ;




public class NativeI2CMessage extends Structure {


    public short   address ;
    public short   flags   ;
    public short   length  ;
    public Pointer buffer  ; 


    public static final int SIZE           ;
    public static final int OFFSET_ADDRESS ;
    public static final int OFFSET_FLAGS   ;
    public static final int OFFSET_LENGTH  ;
    public static final int OFFSET_BUFFER  ;




    static {
    
        NativeI2CMessage message = new NativeI2CMessage() ;

        SIZE = message.size() ;

        OFFSET_ADDRESS = message.fieldOffset( "address" ) ;
        OFFSET_FLAGS   = message.fieldOffset( "flags"   ) ;
        OFFSET_LENGTH  = message.fieldOffset( "length"  ) ;
        OFFSET_BUFFER  = message.fieldOffset( "buffer"  ) ;
    }




    private NativeI2CMessage() {}




    NativeI2CMessage( Pointer ptr ) {
    
        super( ptr ) ;
    }




    @Override
    protected List< String > getFieldOrder() {
    
        return Arrays.asList( new String[] { "address" ,
                                             "flags"   ,
                                             "length"  ,
                                             "buffer"  } ) ;
    }
}
