package com.risetek.UnionPayLib.sd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;
import android.util.Log;

/**
 * 智能卡设备类，完成智能卡文件读写操作
 */
public class SmartCardDevice {

	private static final int MAXFILEINDEX_INITVALUE = 256;
	public static final int READ_WRITE_LENGTH = 512;
	private static final String STRING_ZERO = "0";
	private static final int MAX_SINGLE_FINDING_COUNT = 4;
	private static final int MAX_MULTIPLE_FINDING_COUNT = 64;
	private static final String SINGEL_FILENAME = "MPAYSSD0.SYS";
	private static final String SINGEL_FILENAME_DY = "MPAY_SSD.SYS";
	private static final String FILE_DIR = "MPAYSSD";
	private static final String MUTI_FILENAME_PREFIX = "MPAY";
	private static final String MUTI_FILE_EXTENSION_NAME = ".SYS";
	private static final int UPCARD_FILE_EMPTY = 0;
	private static final int UPCARD_FILE_THREE = 1; // 普通的江波龙3文件处理方式
	private static final int UPCARD_FILE_MULT = 2; // 多文件处理方式
	private static final int UPCARD_FILE_SIGNAL = 3; // 单文件处理方式
	private static final int UPCARD_FILE_TYSIGNAL = 4; //天瑜动态单文件处理方式（附录D）
	private static final int UPCARD_FILE_SIGNAL_DY = 5; //银联动态单文件处理方式（附录C）
	private static final String MNT_DIR_PATH = "/mnt";
	private static final String EMMC_FOLDER_NAME = "emmc";
	private static final String NOT_OPEN_SMARTCARD = "smartcard not open";
	private int mCurrentFileIndex = 0;
	private int mStartFileIndex = 0;
	private int mMaxFileIndex = 0;
	private int mWriteFile = -1;
	private int mReadFile = -1;
	private boolean isReadFirst = true;
	private String mWriteFileName;
	private String mReadFileName0;
	private String mReadFileName1;
	private int mFileType = UPCARD_FILE_EMPTY;
	private static String mSmartCardRoot = null;
	
	private static String SMART_CARD_DEVICE = "SmartCardDevice";
	
	private static final int CARDCMD_HEADER_LEN = 32;
	private static final byte[] CARD_CMD_HEADER = { 0x06, 0x0F, 0x19, 0x16, 0x1F, 0x16,
		        0x0C, 0x0B, 0x1F, 0x1F, 0x18, 0x0E, 0x1A, 0x19, 0x03, 0x02, 0x1F, 0x0C, 0x04,
		        0x12, 0x16, 0x0F, 0x05, 0x0D, 0x06, 0x1B, 0x0D, 0x15, 0x1A, 0x03, 0x1B, 0x12};
	
	private static final byte[] CARD_RSP_HEADER = { 0x06, 0x0F, 0x19, 0x16, 0x1F, 0x16,
		        0x0C, 0x0B, 0x1F, 0x1F, 0x18, 0x0E, 0x1A, 0x19, 0x03, 0x02, 0x1F, 0x0C, 0x04,
		        0x12, 0x16, 0x0F, 0x05, 0x0D, 0x06, 0x1B,(byte)0xD0, 0x51,(byte)0xA1, 0x30,(byte) 0xB1, 0x21};
	private static final byte[] SCIF_BIND_COMMAND   = { 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x02};
	private static final byte[] SCIF_UNBIND_COMMAND = { 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x03};
	private static final byte[] RSP_ILLEAGAL_CMD = { 0x00, 0x00, 0x00, 0x01, 0x00, 0x00};


	private static String getPathFromDFInfo(String info, int startIndex) {
		int endIndex = 0;
		int len = info.length();
		for (endIndex = startIndex; endIndex < len; endIndex++) {
			char c = info.charAt(endIndex);
			if (!Character.isLetterOrDigit(c) && c != '_' && c != '/') {
				break;
			}
		}
		return info.substring(startIndex, endIndex);
	}

	private static boolean isRepeat(ArrayList<String> list, String path) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(path)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * 获取SD卡根目录
	 * 
	 * @return SD卡根目录
	 * @throws UnionPayPaymentException
	 *             SD卡不存在
	 */
	public static String[] getRootPath() throws UnionPayPaymentException {

		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			throw new UnionPayPaymentException(
					UnionPayPaymentException.UPP_NO_SDCARD);
		}

		// 一般结尾没有字符'/'
		String firstSDPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
//		Log.v(SMART_CARD_DEVICE, "getRootPath(): firstSDPath = " + firstSDPath);
		if (!firstSDPath.endsWith("/")) {
			firstSDPath += "/";
		}
		ArrayList<String> dirList = new ArrayList<String>(2);
		dirList.add(firstSDPath);

		InputStream is = null;
		InputStreamReader isReader = null;
		BufferedReader bufferReader = null;
		try {
			Process process = Runtime.getRuntime().exec("df");
			is = process.getInputStream();
			isReader = new InputStreamReader(is);
			bufferReader = new BufferedReader(isReader);
			String strLine = null;
			Pattern pattern = Pattern.compile("[/\\w-]+(s|S)(d|D)[/\\w-]*");
			Matcher matcher = null;
			while ((strLine = bufferReader.readLine()) != null) {
				strLine = strLine.trim();
				//这里匹配上面含有SD的正则表达式，这里过滤出 /mnt/sdcard/
				if ((matcher = pattern.matcher(strLine)).find()) {
					if (!isRepeat(dirList, matcher.group() + "/")) {
						dirList.add(matcher.group() + "/");
					}
				}
				if (!strLine.contains(MNT_DIR_PATH)) {
					continue;
				}
				//如果不匹配上面的正则表达式，但是字符串包含有MNT_DIR_PATH = /mnt
				//这里过滤出 /mnt/asec/ 和 /mnt/obb/
				String path = getPathFromDFInfo(strLine,
						strLine.indexOf(MNT_DIR_PATH));
				if (!path.endsWith("/")) {
					path += "/";
				}
				if (!isRepeat(dirList, path)) {
					dirList.add(path);
				}
			}
			bufferReader.close();
			isReader.close();
			is.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		String[] roots = new String[dirList.size()];
		dirList.toArray(roots);
		return roots;
	}

	/**
	 * 文件或文件夹是否存在
	 * 
	 * @param fileName
	 *            文件名
	 * @return true 存在 false 不存在
	 */
	private boolean isFileExist(String fileName) {
		File f = new File(fileName);
		return f.exists();
	}
	
	/**
     * 创建文件
     *
     * @param path  文件的绝对路径
     * @return 成功返回true,失败返回false
	 * @throws IOException
	 * 			创建文件失败
     */
	public boolean createFile(String fileName) throws IOException{
		File f = new File(fileName);
		f.createNewFile();
		return f.exists();
	}
	
	/**
	 * 创建目录
	 *
	 * @param path
	 *            目录
	 * @return true 创建成功 false 创建失败
	 */
	public boolean createDirectory(String path){
		File f = new File(path);		
		if(!f.exists())
			f.mkdirs();
		return f.exists();
	}
	
    /**
     * 删除给定路径的目录或文件，如果给定路径是一个目录，则递归删除目录
     *
     * @param path
     *            目录/文件路径
     * @return true 删除成功 false 删除失败
     */
	public boolean deleteFileRecursively(String path) {
        File destFile = new File(path);
        if (!destFile.exists()) {
            return true;
        }

        if (destFile.isFile()) {
            destFile.delete();
            return true;
        }

        String[] childNames = destFile.list();
        for (String child : childNames) {
            if (!deleteFileRecursively(new File(path, child).getAbsolutePath())) {
                return false;
            }
        }
        return destFile.delete();
	}

	/**
	 * 获取多文件下一个操作文件名
	 * 
	 * @return 文件名
	 * @throws UnionPayPaymentException
	 *             没有SD卡
	 */
	private String getNextMultiFileName() throws UnionPayPaymentException {
		if (null == mSmartCardRoot) {
			throw new UnionPayPaymentException(
					UnionPayPaymentException.UPP_NOT_SMARTCARD);
		}
		do {
			mCurrentFileIndex++;
			if (mCurrentFileIndex >= mMaxFileIndex) {
				mCurrentFileIndex = mStartFileIndex;
			}
			String index = Integer.toHexString(mCurrentFileIndex);
			if (index.length() < 2) {
				index = STRING_ZERO + index;
			}
			String fileName = mSmartCardRoot + FILE_DIR + "/" + MUTI_FILENAME_PREFIX + index+ MUTI_FILE_EXTENSION_NAME;
			//Log.i("getNextMultiFileName", "fileName:"+fileName);
			if (!isFileExist(fileName)) {
				mMaxFileIndex = mCurrentFileIndex;
			} else {
				return fileName;
			}
		} while (true);
	}

	private String OpenStaticFile(String root){
		String fileName = root + SINGEL_FILENAME;
		if (isFileExist(fileName)) {
			mWriteFileName = fileName;
			int found = 0;
			for (int i = 0; i < MAX_SINGLE_FINDING_COUNT; i++) {
				String index = Integer.toHexString(i);
				if (index.length() < 2) {
					index = STRING_ZERO + index;
				}
				String readFileName = root + MUTI_FILENAME_PREFIX + index
						+ MUTI_FILE_EXTENSION_NAME;
				if (isFileExist(readFileName)) {
					found++;
					if (1 == found) {
						mReadFileName0 = readFileName;
					} else if (2 == found) {
						mReadFileName1 = readFileName;
						mFileType = UPCARD_FILE_THREE;
						//Log.e("OpenStaticFile", "mWriteFileName:"+fileName+";::mFileType:"+mFileType);
						return root;
					} else {
						break;
					}
				}
			}
			if (found < 2) {
				mFileType = UPCARD_FILE_SIGNAL;
				//Log.e("OpenStaticFile", "mWriteFileName:"+fileName+";::mFileType:"+mFileType);
				return root;
			}
		} else {
			// MPAYSSD目录寻找MPAYxx.SYS 支持多文件卡
			int found = 0;
			String path = root + FILE_DIR;
			if (isFileExist(path)) {
				for (int i = 0; i < MAX_MULTIPLE_FINDING_COUNT; i++) {
					String index = Integer.toHexString(i);
					if (index.length() < 2) {
						index = STRING_ZERO + index;
					}
					String name = path + "/" + MUTI_FILENAME_PREFIX + index
							+ MUTI_FILE_EXTENSION_NAME;
					if (isFileExist(name)) {
						found++;
						if (1 == found) {
							mWriteFileName = name;
							mStartFileIndex = i + 1;
						} else if (found >= 3) {
							mFileType = UPCARD_FILE_MULT;
							mCurrentFileIndex = mStartFileIndex;
							mMaxFileIndex = MAXFILEINDEX_INITVALUE;
							//Log.e("OpenStaticFile", "mWriteFileName:"+mWriteFileName+";::mFileType:"+mFileType);
							return root;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * 对动态文件写入SCIF_BIND/SCIF_UNBIND命令
	 * @param filename 
	 *        文件路径
	 * @param isBind
	 *        true 代表写入SCIF_BIND，false 代表写入SCIF_UNBIND
	 * @return
	 *        成功返回写入的长度len>0,失败返回-1
	 */
	private int WriteBindOrUnBind(String filename ,boolean isBind){
		int len = 0;
		
		if (null == filename) {
			return -1;
		}
		byte[] temp = new byte[READ_WRITE_LENGTH];
		System.arraycopy(CARD_CMD_HEADER, 0, temp, 0, CARD_CMD_HEADER.length);
		if(isBind){
			System.arraycopy(SCIF_BIND_COMMAND, 0, temp, CARDCMD_HEADER_LEN, SCIF_BIND_COMMAND.length);
		}else{
			System.arraycopy(SCIF_UNBIND_COMMAND, 0, temp, CARDCMD_HEADER_LEN, SCIF_UNBIND_COMMAND.length);
		}
		int mmwrite = open(filename);
		
		if (mmwrite < 0) {
			return -1;
		}
		// write CARD_CMD_HEADER + SCIF_BIND_COMMAND/SCIF_UNBIND_COMMAND (total 512 bytes, supplement with 0x00)
		len = write(mmwrite, temp);
		close(mmwrite);
		mmwrite = -1;
		return len;
	}
	
	/**
	 * 读取动态文件SCIF_BIND响应
	 * @param filename
	 *        文件路径
	 * @param outData
	 *        读取结果（缓存空间需为512）
	 * @return
	 *        成功返回读取数据的长度len>0,失败返回-1
	 */
	private int ReadBind(String filename, byte[] outData){
		int len = 0;
	
		if (null == filename) {
			return -1;
		}

		int mmread = open(filename);
		if (mmread < 0) {
			return -1;
		}
		len = read(mmread, outData, 0, READ_WRITE_LENGTH);
		close(mmread);
		mmread = -1;
		return len;
		
	}
	private boolean WriteBindAndRead(String filename){
		int len = 0;
		
		mFileType = UPCARD_FILE_EMPTY;
		
		byte[] temp = new byte[READ_WRITE_LENGTH];
		len = WriteBindOrUnBind(filename, true);
		if(len>0){
			for(int i=0; i<32; i++){
				len = ReadBind(filename, temp);
				if(len<=0)
					break;

				//比较返回数据的低26至31字节的数据是否与发送的一直
//				Log.i( SMART_CARD_DEVICE,"Dynamic CardOpertor:Read " + StringEncode.hexEncode(temp));
				if(equals(temp,26, CARD_CMD_HEADER, 26,6)){
					if(equals(temp,32,RSP_ILLEAGAL_CMD,0,6)){
						mFileType = UPCARD_FILE_TYSIGNAL;//天喻标准动态卡（附录D）
						return true;
					}
				}else if(equals(temp, 26,CARD_RSP_HEADER, 26, 6)){
					mFileType = UPCARD_FILE_SIGNAL_DY;//银联标准动态卡（附录C）
					return true;
				}
					
			}
		}

		return false;
	}
	
	private String OpenDynamicFile(String root){
//		Log.v();
		String filename = root + SINGEL_FILENAME_DY;
		boolean isBindSuccess = false;
		if(isFileExist(filename)){
			isBindSuccess = WriteBindAndRead(filename);
			if(isBindSuccess){
				mWriteFileName = filename;
				Log.v("OpenDynamicFile", "mWriteFileName:"+filename+";::mFileType:"+mFileType);
				return root;
			}
		}
		return null;
	}
	
	private String CreateDynamicFile(String root){
		boolean isBindSuccess = false;

		String filename = root +SINGEL_FILENAME_DY;
		Log.i("CreateDynamicFile", filename);//在目录"/mnt/asec/MPAY_SSD0.SYS"下报java.io.IOException: Permission denied

		try {
			if(createFile(filename)){
				isBindSuccess = WriteBindAndRead(filename);
				if(isBindSuccess){
					mWriteFileName = filename;
					//Log.e("CreateDynamicFile", "mWriteFileName:"+filename+";::mFileType:"+mFileType);
					return root;
				}
				
				//删除非SD卡接口文件
				if(!isBindSuccess)
					deleteFileRecursively(filename);
				//Log.e("CreateDynamicFile", "delete file:"+filename);
			}
		} catch (IOException e) {
			Log.e(SMART_CARD_DEVICE, e.getMessage());
		}

		return null;
	}
	
	private String getSmartCardRoot() throws UnionPayPaymentException {
		// 按照规范，先找单文件，再找多文件
		// 根目录寻找MPAYSSD0.SYS 和 MPAYXX.SYS
		String[] allRoot = getRootPath();
		for(String root : allRoot) {
			Log.v(SMART_CARD_DEVICE, "getSmartCardRoot(): allRoot = " + root);
		}
		for (int n = 0; n < allRoot.length; n++) {
			String root = allRoot[n];
			if(null != OpenDynamicFile(root)){//先检测动态文件时，如果存在动态文件，但是智能卡不是动态卡
				return root;
			}
			
			if(null != OpenStaticFile(root)){
				return root;
			}
		}
		
		for(int n = 0; n < allRoot.length; n++){
			String root = allRoot[n];
			if(null != CreateDynamicFile(root)){
				return root;
			}
		}
		return null;
	}

	/**
	 * 打开智能卡
	 * 
	 * @throws UnionPayPaymentException
	 *             SD卡不存在
	 * @throws FileNotFoundException
	 *             非智能卡
	 */
	public void open() throws UnionPayPaymentException, FileNotFoundException {
		Log.v("SmartCardDevice", "open(): mWriteFileName: " + mWriteFileName);
		if (null != mWriteFileName) {
			return;
		}
		mSmartCardRoot = getSmartCardRoot();
		if (null == mSmartCardRoot) {
			throw new UnionPayPaymentException(
					UnionPayPaymentException.UPP_NOT_SMARTCARD);
		}
		switch (mFileType) {
		case UPCARD_FILE_SIGNAL:
		case UPCARD_FILE_MULT:
		case UPCARD_FILE_THREE:
		case UPCARD_FILE_SIGNAL_DY:
		case UPCARD_FILE_TYSIGNAL:
			break;
		default:
			throw new UnionPayPaymentException(
					UnionPayPaymentException.UPP_NOT_SMARTCARD);
		}
	}

	/**
	 * 写智能卡
	 * 
	 * @param data
	 *            待写入数据
	 * @param offset
	 *            待写入数据偏移量
	 * @param length
	 *            待写入数据长度
	 * @return 已经写入智能卡长度
	 * @throws IOException
	 *             写失败
	 */
	public int write(byte[] data, int offset, int length) throws IOException {
		if (null == mWriteFileName) {
			return -1;
		}
		if (length > READ_WRITE_LENGTH) {
			length = READ_WRITE_LENGTH;
		}
		byte[] temp = new byte[READ_WRITE_LENGTH];
		if (UPCARD_FILE_SIGNAL == mFileType || UPCARD_FILE_MULT == mFileType
				|| UPCARD_FILE_THREE == mFileType) {
			System.arraycopy(data, offset, temp, 0, length);
			mWriteFile = open(mWriteFileName);
			if (mWriteFile < 0) {
				return -1;
			}
			length = write(mWriteFile, temp);
			close(mWriteFile);
			mWriteFile = -1;
			return length;
		} else if(UPCARD_FILE_SIGNAL_DY == mFileType || UPCARD_FILE_TYSIGNAL == mFileType){
			//动态文件加头部
			System.arraycopy(CARD_CMD_HEADER, 0, temp, 0, CARD_CMD_HEADER.length);
			System.arraycopy(data, offset, temp, CARDCMD_HEADER_LEN, length);
			mWriteFile = open(mWriteFileName);
			if (mWriteFile < 0) {
				return -1;
			}
			length = write(mWriteFile, temp);
			close(mWriteFile);
			mWriteFile = -1;
			return length;
		} else {
			return -1;
		}
	}

	/**
	 * 写智能卡
	 * 
	 * @param data
	 *            待写入数据
	 * @return 已经写入智能卡长度
	 * @throws IOException
	 *             写失败
	 */
	public int write(byte[] data) throws IOException {
		return write(data, 0, data.length);
	}

	/**
	 * 从智能卡读取数据
	 * 
	 * @param data
	 *            读入数据缓存
	 * @param offset
	 *            缓存偏移量
	 * @param length
	 *            期望读取长度
	 * @return 读取长度
	 * @throws IOException
	 *             读失败
	 * @throws UnionPayPaymentException
	 *             非智能卡
	 */
	public int read(byte[] data, int offset, int length) throws IOException,
			UnionPayPaymentException {
		if (null == mWriteFileName) {
			return -1;	
		}
		if (length > READ_WRITE_LENGTH) {
			length = READ_WRITE_LENGTH;
		}
		int len = 0;

		switch (mFileType) {
		case UPCARD_FILE_SIGNAL:
			mReadFile = open(mWriteFileName);
			if (mReadFile < 0) {
				return -1;
			}
			len = read(mReadFile, data, offset, length);
			close(mReadFile);
			mReadFile = -1;
			return len;
		case UPCARD_FILE_MULT:
			mReadFile = open(getNextMultiFileName());
			if (mReadFile < 0) {
				return -1;
			}
			len = read(mReadFile, data, offset, length);
			close(mReadFile);
			mReadFile = -1;
			return len;
		case UPCARD_FILE_THREE:
			if (isReadFirst) {
				mReadFile = open(mReadFileName1);
			} else {
				mReadFile = open(mReadFileName0);
			}
			isReadFirst = !isReadFirst;
			if (mReadFile < 0) {
				return -1;
			}
			len = read(mReadFile, data, offset, length);
			close(mReadFile);
			mReadFile = -1;
			return len;
		case UPCARD_FILE_SIGNAL_DY:
		case UPCARD_FILE_TYSIGNAL:
			mReadFile = open(mWriteFileName);
			if (mReadFile < 0) {
				return -1;
			}

			byte[] temp = new byte[READ_WRITE_LENGTH];
			len = read(mReadFile,temp,0,READ_WRITE_LENGTH);
			close(mReadFile);
			mReadFile = -1;
			if(len < CARDCMD_HEADER_LEN){
				return -1;
			}
			System.arraycopy(temp, CARDCMD_HEADER_LEN, data, offset, len-CARDCMD_HEADER_LEN);
			return len-CARDCMD_HEADER_LEN;
		default:
			return -1;
		}
	}

	/**
	 * 从智能卡读取数据
	 * 
	 * @param data
	 *            读入数据缓存
	 * @return 读取长度
	 * @return 读取长度
	 * @throws IOException
	 *             读失败
	 * @throws UnionPayPaymentException
	 *             非智能卡
	 */
	public int read(byte[] data) throws IOException, UnionPayPaymentException {
		return read(data, 0, data.length);
	}

	public static String getAbsoluteRootPath() {
		return mSmartCardRoot;
	}
	
	private boolean DynamicUnBind(){
		if(UPCARD_FILE_SIGNAL_DY == mFileType){
			WriteBindOrUnBind(mWriteFileName,false);
		}
		return true;
	}

	/**
	 * 关闭智能卡
	 * 
	 * @throws IOException
	 *             关闭失败
	 */
	public void close() throws IOException {
		// 首先判断SD卡是否被移除
		if (Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			if (mReadFile > 0) {
				close(mReadFile);
			}
			if (mWriteFile > 0) {
				close(mWriteFile);
			}
			DynamicUnBind();//对动态文件发送SCIF_UNBIND命令
		}
		mReadFile = -1;
		mWriteFile = -1;
		mWriteFileName = null;
	}
	
	/**
	 * 比较两个数组的值
	 * @param b1
	 *        数组1
	 * @param b1srcPos
	 *        数组1的起点位置
	 * @param b2
	 *        数组2
	 * @param b2srcPos
	 *        数组2的起点位置
	 * @param len
	 *        要比较的长度
	 * @return
	 *        相等返回true,不相等返回false
	 */
	private boolean equals(byte[] b1, int b1srcPos,byte[] b2, int b2srcPos,int len) {
		int k = b1srcPos;
		int j = b2srcPos;
		for (int i = 0; i < len; i++) {
			if (b1[k++] != b2[j++]) {
				return false;
			}
		}
		return true;
	}

	private static native int open(String fileName);

	private static native void close(int file);

	private static native int write(int file, byte[] data);

	private static native int read(int file, byte[] data, int offset, int length);

	private static native int seek(int file, int position);

	private static native String getExternalPath();

	static {
		System.loadLibrary("smartcard");
	}
}
