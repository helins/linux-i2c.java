/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */




package io.helins.linux.i2c.internal ;


import com.sun.jna.Pointer   ;
import com.sun.jna.Structure ;
import java.util.Arrays      ;
import java.util.List        ;




/**
 * Internal class kept public for JNA to work, the user should not bother about this.
 */
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
