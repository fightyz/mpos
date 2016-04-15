package com.mcg.mpos.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;

public class CAUtils {
	
	private static int intCaModLen;//整形的CA模长
	private static int intCaExpLen;//整形的CA指数长

	// 未签名根CA输出扩展 中各项的偏移量
	public static final int EX_OFFSET_HEAD = 0; //记录头
	public static final int EX_OFFSET_SERVICE_TAG = 1;//服务标识
	public static final int EX_OFFSET_MOD_LEN = 5;//根CA公钥模长
	public static final int EX_OFFSET_ALGORITHM_TAG = 7;//根CA公钥算法标识
	public static final int EX_OFFSET_EXP_LEN = 8; //根CA公钥指数长度
	public static final int EX_OFFSET_RID = 9;//注册的应用提供商标识(RID)
	public static final int EX_OFFSET_INDEX = 14;//根CA公钥索引
	public static final int EX_OFFSET_MOD  = 15;//根CA公钥模
	public static int EX_OFFSET_EXP;//根CA公钥指数
	public static int EX_OFFSET_HASH;//hash值
	
	//自签名根CA中各项偏移量
	public static final int SIGN_OFFSET_HEAD = 0;//记录头
	public static final int SIGN_OFFSET_SERVICE_TAG = 1;//服务标识
	public static final int SIGN_OFFSET_RID = 5;//注册的应用提供商标识(RID)
	public static final int SIGN_OFFSET_INDEX = 10;//根CA公钥索引
	public static final int SIGN_OFFSET_EXPIRE = 11;//证书失效日期
	public static final int SIGN_OFFSET_ALGORITHM_TAG = 13;////根CA公钥算法标识
	public static final int SIGN_OFFSET_MOD_LEFT = 14;//根 CA 公钥模的左边部分
	public static int SIGN_OFFSET_HASH_TAG;//hash算法标识
	public static int SIGN_OFFSET_EXP_LEN;//根CA公钥指数长度
	public static int SIGN_OFFSET_EXP;//根CA公钥指数
	public static int SIGN_OFFSET_HASH;//HASH值
	
	public static byte ALGORITHM_RSA = 0x01;//RSA算法的标识
	
	private byte[] exHead;//记录头 1byte
	private byte[] exServiceTag;//服务标识 4byte
	private byte[] exModLen;//根CA公钥模长 2byte
	private byte[] exAlgorithmTag;//根CA公钥算法标识 1byte
	private byte[] exExpLen;//根CA公钥指数长度 1byte
	private byte[] exRID;//注册的应用提供商标识 5byte
	private byte[] exIndex;//根CA公钥索引 1byte
	private byte[] exMod;//根CA公钥模 3byte
	private byte[] exExp;//根CA公钥指数 
	private byte[] exHash;//hash值 20byte
	
	private byte[] signHead;//记录头 1byte
	private byte[] signServiceTag;//服务标识 4byte
	private byte[] signRID;//注册的应用提供商标识 5byte
	private byte[] signIndex;//根CA公钥索引 1byte
	private byte[] signExpire;//证书失效日期 2byte
	private byte[] signAlgorithmTag;//根CA公钥算法标识 1byte
	private byte[] signModLeft;//公钥模的左边部分
	private byte[] signHashTag;//hash算法标识
	private byte[] signExpLen;//根CA公钥指数长度 1byte
	private byte[] signExp;//根CA公钥指数 
	private byte[] signHash;//hash值 20byte
	
	private DataInputStream ca;//整个CA
	private byte[] signPart;//CA中自签名部分
	private byte[] exPart;//CA中扩展部分
	private static PublicKey pubKey;//从CA中得到的公钥
	
	public CAUtils(byte[] ca) {
//		this.ca = ca;
		this.ca = new DataInputStream(new ByteArrayInputStream(ca));
//		this.ca.
	}
	
	public void init() {
		
//		exHead = ArrayUtils.sub(ca, EX_OFFSET_HEAD, EX_OFFSET_SERVICE_TAG - 1);
//		
//		exServiceTag = ArrayUtils.sub(ca, EX_OFFSET_SERVICE_TAG, EX_OFFSET_MOD_LEN - 1);
//		
//		exModLen = ArrayUtils.sub(ca, EX_OFFSET_MOD_LEN, EX_OFFSET_ALGORITHM_TAG - 1);
//		
//		exAlgorithmTag = ArrayUtils.sub(ca, EX_OFFSET_ALGORITHM_TAG, EX_OFFSET_EXP_LEN - 1);
//		
//		exExpLen = ArrayUtils.sub(ca, EX_OFFSET_EXP_LEN, EX_OFFSET_RID - 1);
//		
//		exRID = ArrayUtils.sub(ca, EX_OFFSET_RID, EX_OFFSET_INDEX - 1);
//		
//		exIndex = ArrayUtils.sub(ca, EX_OFFSET_INDEX, EX_OFFSET_MOD - 1);
//		
//		EX_OFFSET_EXP = EX_OFFSET_MOD + ConvertUtils.convertTwoBytesToInt(exModLen, 0);
//		EX_OFFSET_HASH = EX_OFFSET_EXP + exExpLen[0];
//		
//		exMod = ArrayUtils.sub(ca, EX_OFFSET_MOD, EX_OFFSET_EXP - 1);
//		intCaModLen = exMod.length;
//		
//		exExp = ArrayUtils.sub(ca, EX_OFFSET_EXP, EX_OFFSET_HASH - 1);
//		intCaExpLen = exExpLen.length;
//		
//		exHash = ArrayUtils.sub(ca, EX_OFFSET_HASH, ca.length - 1);
//		
//		//截取出CA扩展部分
//		exPart = ArrayUtils.sub(ca, EX_OFFSET_HEAD, 35 + intCaModLen + intCaExpLen - 1);
//		
//		//截取出CA自签名部分
//		signPart = ArrayUtils.sub(ca, 35 + intCaModLen + intCaExpLen, ca.length - 1);
		
		//根据 公钥模数 和 指数 恢复生成 PublicKey 对象
		BigInteger bigIntPubModulus = new BigInteger(ConvertUtils.bytesToString(exMod), 16);
		BigInteger bigIntPubExponent = new BigInteger(ConvertUtils.bytesToString(exExp), 16);
		
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(bigIntPubModulus, bigIntPubExponent);
			pubKey = keyFactory.generatePublic(rsaPublicKeySpec);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 1. 根据扩展中的公钥，恢复出自签名部分
		try {
				signPart = decryptByPublicKey(signPart, pubKey, "RSA/ECB/NoPadding");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		// 截取自签名部分中的各个项，并保存
		signHead = ArrayUtils.sub(signPart, SIGN_OFFSET_HEAD, SIGN_OFFSET_SERVICE_TAG - 1);
		signServiceTag = ArrayUtils.sub(signPart, SIGN_OFFSET_SERVICE_TAG, SIGN_OFFSET_RID - 1);
		signIndex = ArrayUtils.sub(signPart, SIGN_OFFSET_INDEX, SIGN_OFFSET_EXP - 1);
		signExpire = ArrayUtils.sub(signPart, SIGN_OFFSET_EXPIRE, SIGN_OFFSET_ALGORITHM_TAG - 1);
		signAlgorithmTag = ArrayUtils.sub(signPart, SIGN_OFFSET_ALGORITHM_TAG, SIGN_OFFSET_MOD_LEFT - 1);
		
		SIGN_OFFSET_HASH_TAG = intCaModLen - intCaExpLen - 12;
		SIGN_OFFSET_EXP_LEN = SIGN_OFFSET_HASH_TAG + 1;
		SIGN_OFFSET_EXP = SIGN_OFFSET_EXP_LEN + 1;
		SIGN_OFFSET_HASH = SIGN_OFFSET_EXP + intCaExpLen;
		
		signModLeft = ArrayUtils.sub(signPart, SIGN_OFFSET_MOD_LEFT, SIGN_OFFSET_HASH_TAG - 1);
		signHashTag = ArrayUtils.sub(signPart, SIGN_OFFSET_HASH_TAG, SIGN_OFFSET_EXP_LEN - 1);
		signExpLen = ArrayUtils.sub(signPart, SIGN_OFFSET_EXP_LEN, SIGN_OFFSET_EXP - 1);
		signExp = ArrayUtils.sub(signPart, SIGN_OFFSET_EXP, SIGN_OFFSET_HASH - 1);
		signHash = ArrayUtils.sub(signPart, SIGN_OFFSET_HASH, signPart.length - 1);
	}
	
	public boolean verify(byte[] ca) {
		
		if(ca[EX_OFFSET_ALGORITHM_TAG] == ALGORITHM_RSA) {
			
			// 2. 检查恢复根CA数据的记录头是否是0x21
			if(ConvertUtils.bytesToHexString(signHead, 0) != "21") {
				return false;
			}
			// 3. 检查RID是否是 A000000333
			if(ConvertUtils.bytesToHexString(signRID, 0) != "A000000333") {
				return false;
			}
			
			// 4. 检查根CA索引是否符合规范
			//TODO 还未找到CA索引范围
			
			// 5. 检查失效日期
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			year = year - 2000;
			int month = calendar.get(Calendar.MONTH);
			int cardYear = signExpire[1];
			int cardMonth = signExpire[0];
			if(year > cardYear) {
				return false;
			} else if(year == cardYear && month > cardMonth) {
				return false;
			}
			
			// 6. 检查恢复的根 CA 公钥证书数据中根 CA 公钥算法标识,若不是‘01’,则拒绝该根 CA 公钥
			if(ConvertUtils.bytesToHexString(signAlgorithmTag, 0) != "01") {
				return false;
			}
			
			// 7. 检查恢复的根 CA 公钥证书数据中根 CA 公钥模长度
			//TODO 待查规范
			
			// 8. 检查根CA自签名数据和恢复的根CA公钥证书数据长度是否一致
			//TODO 待查
			
			// 9. 检查恢复的根 CA 公钥证书数据中根 CA 公钥指数长度,若不是‘01’,则拒绝该根 CA 公钥
			if(ConvertUtils.bytesToHexString(signExpLen, 0) != "01") {
				return false;
			}
			
			// 10. 检查恢复的根CA公钥证书数据中根CA公钥指数,若不是 3 或 2^16+1,则拒绝该根CA公钥
			if(ConvertUtils.bytesToHexString(signExp, 0) != "03" ||
					ConvertUtils.bytesToHexString(signExp, 0) != "010001") {
				return false;
			}
			
			// 11. 从记录头到根 CA 公钥指数将恢复的根 CA 公钥数据的各字段从左向右连接
			
		}
		return true;
	}
	
	/**
	 * 根据公钥、指定的解密算法来解密数据
	 * @param signPart 被加密的数据
	 * @param pubKey 公钥
	 * @param transformation 解密算法，用于获取Cipher实例
	 * @return 解密后的数据
	 * @throws Exception Cipher中没有该解密算法
	 */
	public static byte[] decryptByPublicKey(byte[] signPart, PublicKey pubKey, String transformation) throws Exception {
		Cipher cipher;
		cipher = Cipher.getInstance(transformation);
		cipher.init(Cipher.DECRYPT_MODE, pubKey);
		return cipher.doFinal(signPart);
	}
}
