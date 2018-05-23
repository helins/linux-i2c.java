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


import com.sun.jna.Pointer                           ;
import com.sun.jna.Structure                         ;
import io.dvlopt.linux.i2c.internal.NativeI2CMessage ;
import java.util.Arrays                              ;
import java.util.List                                ;




/**
 * Internal class kept public for JNA to work, the user should not bother about this.
 */
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



