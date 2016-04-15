/*
 * Copyright 2012 Giesecke & Devrient GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobilesecuritycard.openmobileapi.service.terminals;

import android.content.Context;
import android.util.Log;

import java.util.MissingResourceException;
import java.util.NoSuchElementException;



import com.mobilesecuritycard.openmobileapi.service.CardException;
import com.mobilesecuritycard.openmobileapi.service.Terminal;
import com.mobilesecuritycard.openmobileapi.service.terminals.nfc.NFCContactlessCardReader;


final class ContactlessNFCTerminal extends Terminal {
    private final int MAX_APDU_SIZE = 490;
    private byte[] mAtr = null;
    private NFCContactlessCardReader mContactlessCardReader;
    private String TAG = "ContactlessNFCTerminal";


    public ContactlessNFCTerminal(Context context) {
        super("eSEContactless: eSE Card", context);
        Log.v(TAG, "ContactlessNFCTerminal(Context context)");
        mContactlessCardReader = NFCContactlessCardReader.getInstance();
    }
    
    @Override
    protected void internalConnect() throws CardException {
    	Log.v(TAG, "internalConnect()");		
        if(isConnected()) {
            return;
        }
        Log.v(TAG, "internalConnect(): start open()");
        mContactlessCardReader.open();
		Log.v(TAG, "internalConnect(): finish open()");
    }

    @Override
    protected void internalDisconnect() throws CardException {
    	Log.v(TAG, "internalDisconnect()");
        if (!isConnected()) {
            return;
        }
        try {
        	mContactlessCardReader.close();
        } catch (Exception ignore) {}
        mIsConnected = false;
    }

    public boolean isCardPresent() throws CardException {    	
    	Log.v(TAG, "isCardPresent()");
        if (isConnected()) {
            return true;
        } else {
            try {
                internalConnect();
                return true;
            } catch (CardException e) {
                internalDisconnect();
                Log.v(TAG, "Card not present: " + e.getMessage());
                return false;
            }
        }
    }

    public byte[] getAtr() {
        return mAtr;
    }

    @Override
    protected byte[] internalTransmit(byte[] command) throws CardException {
        Log.v(TAG, "internalTransmit(byte[] command)");
        if (!isConnected()) {
            throw new CardException("Secure Element was not requested.");
        }
        
        if(command.length > MAX_APDU_SIZE) {
            throw new CardException("Command too long.");
        }
        
        byte[] response = null;
        try {
            response = mContactlessCardReader.transmit(command);
        } catch (Exception e) {
            throw new CardException(e);
        }
        
        if (response == null || response.length < 2) {
            throw new CardException("No response received");
        }
        
        return response;
    }

    @Override
    protected void internalCloseLogicalChannel(int channelNumber) throws CardException {
        
        if (channelNumber > 0) {
            byte cla = (byte) channelNumber;
            if (channelNumber > 3) {
                cla |= 0x40;
            }

            byte[] manageChannelClose = new byte[] {
                    cla, 0x70, (byte) 0x80, (byte) channelNumber
            };
            transmit(manageChannelClose, 2, 0x9000, 0xFFFF, "MANAGE CHANNEL");
        }
    }

    @Override
    protected int internalOpenLogicalChannel() throws Exception {
        
    	mSelectResponse = null;
        byte[] manageChannelCommand = new byte[] {
                0x00, 0x70, 0x00, 0x00, 0x01
        };
        byte[] rsp = transmit(manageChannelCommand, 3, 0x9000, 0xFFFF, "MANAGE CHANNEL");
        if (rsp.length == 2 && (rsp[0] == (byte) 0x6A && rsp[1] == (byte) 0x81)) {
            throw new MissingResourceException("no free channel available", "", "");
        }
        if (rsp.length != 3) {
            throw new MissingResourceException("unsupported MANAGE CHANNEL response data", "", "");
        }
        int channelNumber = rsp[0] & 0xFF;
        if (channelNumber == 0 || channelNumber > 19) {
            throw new MissingResourceException("invalid logical channel number returned", "", "");
        }

        return channelNumber;
    }

    @Override
    protected int internalOpenLogicalChannel(byte[] aid) throws Exception {

        if (aid == null) {
            throw new NullPointerException("aid must not be null");
        }
    	mSelectResponse = null;
        byte[] manageChannelCommand = new byte[] {
                0x00, 0x70, 0x00, 0x00, 0x01
        };
        byte[] rsp = transmit(manageChannelCommand, 3, 0x9000, 0xFFFF, "MANAGE CHANNEL");
        if (rsp.length == 2 && (rsp[0] == (byte) 0x6A && rsp[1] == (byte) 0x81)) {
            throw new MissingResourceException("no free channel available", "", "");
        }
        if (rsp.length != 3) {
            throw new MissingResourceException("unsupported MANAGE CHANNEL response data", "", "");
        }
        int channelNumber = rsp[0] & 0xFF;
        if (channelNumber == 0 || channelNumber > 19) {
            throw new MissingResourceException("invalid logical channel number returned", "", "");
        }

        byte[] selectCommand = new byte[aid.length + 6];
        selectCommand[0] = (byte) channelNumber;
        if (channelNumber > 3) {
            selectCommand[0] |= 0x40;
        }
        selectCommand[1] = (byte) 0xA4;
        selectCommand[2] = 0x04;
        selectCommand[4] = (byte) aid.length;
        System.arraycopy(aid, 0, selectCommand, 5, aid.length);
        try {
        	mSelectResponse = transmit(selectCommand, 2, 0x9000, 0xFFFF, "SELECT");
        } catch (CardException exp) {
            internalCloseLogicalChannel(channelNumber);
            throw new NoSuchElementException(exp.getMessage());
        }

        return channelNumber;
    }
    
    @Override
    public boolean isConnected() {
    	// TODO Auto-generated method stub
    	Log.v(TAG, "isConnected()");
    	return mContactlessCardReader.isConnect();
    }    
}
