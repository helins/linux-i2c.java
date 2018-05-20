package io.dvlopt.linux.i2c.internal ;


import com.sun.jna.Pointer   ;
import com.sun.jna.Structure ;
import java.util.Arrays      ;
import java.util.List        ;




public class NativeI2CSmbusIoctlData extends Structure {


    public byte    readWrite ;
    public byte    command   ;
    public int     size      ;
    public Pointer data      ;


    public static final int OFFSET_READ_WRITE ;
    public static final int OFFSET_COMMAND    ;
    public static final int OFFSET_SIZE       ;
    public static final int OFFSET_DATA       ;
    public static final int SIZE              ;


    static {
    
        NativeI2CSmbusIoctlData nativeStruct = new NativeI2CSmbusIoctlData() ;

        OFFSET_READ_WRITE = nativeStruct.fieldOffset( "readWrite" ) ;
        OFFSET_COMMAND    = nativeStruct.fieldOffset( "command"   ) ;
        OFFSET_SIZE       = nativeStruct.fieldOffset( "size"      ) ;
        OFFSET_DATA       = nativeStruct.fieldOffset( "data"      ) ;
        SIZE              = nativeStruct.size()                     ;
    }




    @Override
    protected List< String > getFieldOrder() {
    
        return Arrays.asList( new String[] { "readWrite" ,
                                             "command"   ,
                                             "size"      ,
                                             "data"      } ) ;
    }
}
