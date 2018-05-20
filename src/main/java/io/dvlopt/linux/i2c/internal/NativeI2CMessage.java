/*
 * Copyright 2018 Adam Helinski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
