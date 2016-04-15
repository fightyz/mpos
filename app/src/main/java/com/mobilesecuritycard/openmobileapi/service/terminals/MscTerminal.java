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
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



import com.mobilesecuritycard.openmobileapi.service.CardException;
import com.mobilesecuritycard.openmobileapi.service.SmartcardService;
import com.mobilesecuritycard.openmobileapi.service.Terminal;

/**
 * This terminal accesses the Mobile Security Card (MSC)
 * provided by Giesecke & Devrient Secure Flash Solutions GmbH
 */
final class MscTerminal extends Terminal {

    private final String MSC_FILE_NAME = "/msc.dat";

    private final int MAX_APDU_SIZE = 490;

    private String mMscFile = "";
    private byte[] mAtr = null;


    public MscTerminal(Context context) {
        super("SD: Mobile Security Card", context);
        setMscPath(context);
    }
    
    /**
     * The task of the keep alive thread is to keep the card
     * powered during a session. The thread reads from a
     * file during a session if the time that has passed since
     * the last command transmit operation is longer than the
     * keep alive interval.
     * A program using the MSC Terminal must first close a
     * smart card session to be able to terminate.
     */
    private static class KeepAliveThread extends Thread {
        
        KeepAliveThread(){
            setDaemon(true);
        }
        
        private static KeepAliveThread keepAliveThread = null;
        public static boolean needKeepAlive = true;
        private static boolean enabled = true;
        public static int keepAliveInterval = 5000;
        private static final Lock lock = new ReentrantLock();
        
        public static void activate(){
            if(keepAliveThread == null && enabled == true){
                Log.v("MSC", "start Keep Alive Thread.");
                keepAliveThread = new KeepAliveThread();
                keepAliveThread.start();
            }
        }
        
        public static void deactivate(){
            if(keepAliveThread != null){
                Log.v("MSC", "stop Keep Alive Thread.");
                keepAliveThread.interrupt();
                keepAliveThread = null;
            }
        }
        
        public void run() {
                
            while(!isInterrupted()){
                try {
                    Thread.sleep(keepAliveInterval);
                } catch (InterruptedException e) {
                    interrupt();
                    Log.v("MSC", "Alive Thread stopped");
                    return;
                }
                lock.lock();
                if(needKeepAlive)
                {
                    try{
                        KeepAlive();
                    }catch(Exception ignore){};
                    Log.v("MSC", "KEEP ALIVE");
                }
                needKeepAlive = true;
                lock.unlock();
            }
            Log.v("MSC", "Alive Thread stopped");
        }

    }


    private boolean setMscPath(Context appContext) {
        
        // (1) Check known SD card paths
        Resources res = appContext.getResources();
        String []sd_card_path = {"/sdcard", ">/mnt/sdcard/external_sd"};
        for(String path : sd_card_path) {
            File sdPath = new File(path);
            if(verifyMscPath(sdPath)) {
                mMscFile = sdPath.getAbsolutePath() + MSC_FILE_NAME;
                Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, 
                        "MSC path is a known path.");
                return true;
            }
        }
        
        // (2) Read the file /proc/mounts
        //     to find additional SD card paths
        BufferedReader rd = null;
        try {
            File mounts = new File("/proc/mounts");
            rd = new BufferedReader(new FileReader(mounts));
            if(findAndSetMscPath(rd)){
                Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, 
                        "MSC path found in file '/proc/mounts'.");
                rd.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, 
                    "File '/proc/mounts' not found.");
        } catch (IOException ignore) {}; // closing rd failed
        
        // (3) Execute the 'mount' command
        //     to find additional SD card paths
        rd = runMountCommand();
        if(rd != null && findAndSetMscPath(rd)){
            Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, 
                    "MSC path found executing 'mount' command.");
            return true;
        }
        
        Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, 
                "Mobile Security Card not found.");
        return false;
    }

    private boolean findAndSetMscPath(BufferedReader rd) {
        String line = null;;
        try {
            while ((line = rd.readLine()) != null) {
                File sdPath = findMscPath(line);
                if(sdPath != null) {
                    try{rd.close();}catch(IOException ignore){};
                    mMscFile = sdPath.getAbsolutePath() + MSC_FILE_NAME;
                    return true;
                }
            }
            rd.close();
        } catch (IOException ignore) {}
        return false;
    }

    private BufferedReader runMountCommand() {
        Runtime rt = Runtime.getRuntime();
        Process ps = null;
        try {
            ps = rt.exec("mount");
            ps.waitFor();
        } catch (IOException io) {
            Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, 
                    "'Mount' command failed.");
            return null;
        } catch (InterruptedException ignore) {}
        return new BufferedReader(new InputStreamReader(ps.getInputStream()));
    }

    private File findMscPath(String line) {
        
        String[] tokens = line.split("[ \t]");
        for (String token : tokens) {
            token.trim();
            if (token.length() == 0) {
                continue;
            }
            File path = new File(token);
            if(verifyMscPath(path)){
                return path;
            }
        }
        return null;
    }

    private boolean verifyMscPath(File path) {
        
        if (path.isDirectory() && path.canWrite()) {
            String backup = mMscFile;
            mMscFile = path.getAbsolutePath() + MSC_FILE_NAME;
            try {
                internalConnect();
                Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, "MSC path: " + mMscFile);
                internalDisconnect();
                mMscFile = backup;
                return true;
            } catch (CardException e) {
                mMscFile = backup;
                if(path.exists()){
                    path.delete();
                }
            }
        }
        return false;
    }

    @Override
    protected void internalConnect() throws CardException {
        
        if(mIsConnected) {
            return;
        }
        
        if (mMscFile.equals("")) {
            throw new CardException("No Mobile Security Card detected");
        }
        
        try {
            if (Open(mMscFile) == false) {
                throw new CardException("Opening communication file failed");
            }
        } catch (Exception e) {
            throw new CardException("Opening communication file failed: " 
                    + e.getMessage());
        }
        
        byte[] response = null;
        try {
            response = Transmit(new byte[] {
                    0x20, 0x12, 0x01, 0x01, 0x00
            });
        } catch (Exception e) {
            try {
                Close();
            } catch (Exception ignore) {}
            throw new CardException("Requesting the Secure Element failed: " +
                    "REQUEST SE command could not be transmitted.");
        }
        
        if (response == null || response.length < 2) {
            try {
                Close();
            } catch (Exception ignore) {}
            throw new CardException("Requesting the Secure Element failed: " +
                    "Invalid response to REQUEST SE command.");
        }
        else if (response[response.length-2] != (byte) 0x90 
                || response[response.length-1] != (byte) 0x00) {
            try {
                Close();
            } catch (Exception ignore) {}
            String sw1 = String.format("%02X", response[response.length-2]);
            String sw2 = String.format("%02X", response[response.length-1]);
            throw new CardException("Requesting the Secure Element failed: " +
                    "Response to REQUEST SE command is " + sw1 + " " + sw2);
        } else {
            mAtr = new byte[response.length - 2];
            System.arraycopy(response, 0, mAtr, 0, response.length - 2);
            
            mDefaultApplicationSelectedOnBasicChannel = true;
            mIsConnected = true;
            
            KeepAliveThread.activate();
        }
    }

    @Override
    protected void internalDisconnect() throws CardException {

        if (mIsConnected == false) {
            return;
        }
        
        KeepAliveThread.deactivate();
        
        try {
            if (new File(mMscFile).exists()) {
                Transmit(new byte[] {0x20, 0x15, 0x01, 0x00, 0x00});
            }
        } catch (Exception ignore) {}
        
        try {
            Close();
        } catch (Exception ignore) {}

        mIsConnected = false;
    }

    public boolean isCardPresent() throws CardException {
        
        if (mMscFile.equals("")) {
            Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, "Mobile Security Card not found.");
            return false;
        }

        if (mIsConnected) {
            return true;
        } else {
            try {
                internalConnect();
                internalDisconnect();
                return true;
            } catch (CardException e) {
                internalDisconnect();
                Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, "Card not present: " + e.getMessage());
                return false;
            }
        }
    }

    public byte[] getAtr() {
        return mAtr;
    }

    @Override
    protected byte[] internalTransmit(byte[] command) throws CardException {
        
        if (!mIsConnected) {
            throw new CardException("Secure Element was not requested.");
        }
        
        if(command.length > MAX_APDU_SIZE) {
            throw new CardException("Command too long.");
        }
        
        KeepAliveThread.lock.lock();
        
        byte[] response = null;
        try {
            response = Transmit(command);
        } catch (Exception e) {
            throw new CardException(e);
        }
        
        if (response == null || response.length < 2) {
            throw new CardException("No response received");
        }
        
        KeepAliveThread.needKeepAlive = false;
        KeepAliveThread.lock.unlock();
        
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

    
    
    private static Throwable loadException;

    static {
        try {
            Runtime.getRuntime().loadLibrary("mobilesecuritycard");
        } catch (Throwable t) {
            loadException = t;
        }
    }

    private static Throwable getLoadError() {
        return loadException;
    }

    private static boolean isLoaded() {
        return (loadException == null);
    }
    
    private static native void Close() throws Exception;

    private static native boolean Open(String storageName) throws Exception;

    private static native byte[] Transmit(byte[] command) throws Exception;

    private static native void KeepAlive() throws Exception;
}
