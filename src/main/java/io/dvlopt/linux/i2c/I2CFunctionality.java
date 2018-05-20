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
 * See the License  for the specific language governing permissions and
 * limitations under the License.
 */


package io.dvlopt.linux.i2c ;




public enum I2CFunctionality {




    TRANSACTIONS                   ( 0x00000001 ) ,

    TEN_BIT_ADDRESSING             ( 0x00000002 ) ,

    PROTOCOL_MANGLING              ( 0x00000004 ) ,

    SMBUS_PEC                      ( 0x00000008 ) ,

    QUICK                          ( 0x00010000 ) ,

    READ_BYTE_DIRECTLY             ( 0x00020000 ) ,

    READ_BYTE                      ( 0x00080000 ) ,

    READ_SHORT                     ( 0x00200000 ) ,

    READ_BLOCK                     ( 0x01000000 ) ,

    READ_I2C_BLOCK                 ( 0x04000000 ) ,

    WRITE_BYTE_DIRECTLY            ( 0x00040000 ) ,

    WRITE_BYTE                     ( 0x00100000 ) ,

    WRITE_SHORT                    ( 0x00400000 ) ,

    WRITE_BLOCK                    ( 0x08000000 ) ,

    WRITE_I2C_BLOCK                ( 0x02000000 ) ,

    PROCESS_CALL                   ( 0x00800000 ) ,

    BLOCK_PROCESS_CALL             ( 0x00008000 ) ;




    final int value ;




    private I2CFunctionality( int value ) {
    
        this.value = value ;
    }
}
