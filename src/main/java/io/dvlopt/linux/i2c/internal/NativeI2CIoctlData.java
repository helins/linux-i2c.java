package io.dvlopt.linux.i2c.internal ;


import com.sun.jna.Pointer                           ;
import com.sun.jna.Structure                         ;
import io.dvlopt.linux.i2c.internal.NativeI2CMessage ;
import java.util.Arrays                              ;
import java.util.List                                ;




public class NativeI2CIoctlData extends Structure {


    public Pointer messages ;
    public int     length   ;

    public static final int OFFSET_MESSAGES ;
    public static final int OFFSET_LENGTH   ;
    public static final int SIZE            ;




    static {

        NativeI2CIoctlData nativeStruct = new NativeI2CIoctlData() ;

        OFFSET_MESSAGES = nativeStruct.fieldOffset( "messages" ) ;
        OFFSET_LENGTH   = nativeStruct.fieldOffset( "length"   ) ;
        SIZE            = nativeStruct.size()                    ;
    }




    @Override
    protected List< String > getFieldOrder() {

        return Arrays.asList( new String[] { "messages" ,
                                             "length"   } ) ;
    }
}



