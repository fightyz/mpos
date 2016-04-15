package com.mcg.mpos.tasks;


import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import android.util.Log;

public class Dda {

	private String issuePKCertificate;
	private String issuePKRemainder;
	private String issuePKExponent;
	private String caPKExponent;
	private String caPKRemainder;
	private String signedSData;
	private String verSData;
	private String enIcPKCitificate; // 加密的IC卡公钥证书

	private String icPKExponent;
	private String icPKRemainder;
	private String unpredictableNum;
	private String signedDData;
	
	private String deIssuePKCitificate;// 解密后的发卡行公钥证书
	private String issuePKHashInput;// 发卡行公钥哈希输入
	private String deissuePKHash;// 解密后的发卡行公钥哈希
	private String issuePKHash;// 发卡行公钥哈希
	private String issuePKStr;// 发卡行公钥字符串
	private String issueVerifyingData;// 发卡行验证的数据
	private String issuePKMode;
	private String verIssuePKHash;
	private PublicKey issuePKey;
	private String issueLen;
	private PublicKey icPKey;
	private String deICPKCitificate;
	private String icPKHashInput;
	private String icLen;

	private String deDData;
	private int DDataLen;
	private String DDataHash;
	private String DDataHashInput;
	private String DDataHashcalculate;

	private String deSignedSData;// 读取卡片上的数据获得的静态数据的签名
	private String sDataHash;
	private String sDataInput;
	private String deSDataHash;// 验证的时候计算得到的静态数据签名*/
	
	private char[] cHex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F' };
	private String caPKIndex;

	
	private String caHashInput;
	private String icPKStr;
	private String icPKHash;
	private String deicPKHash;
	private String icPKMode;

	/**
	 * 根据公钥的模和指数生成公钥的对象
	 * 
	 * @param exponent
	 *            用字符串表示的公钥的指数的值
	 * @param modulus
	 *            用字符串表示的公钥的模的值
	 * @return 公钥的对象
	 * @throws Exception
	 */
	private PublicKey getPublicKey(String exponent, String modulus)
			throws Exception {
		BigInteger b1 = new BigInteger(modulus, 16);// 大整数
		BigInteger b2 = new BigInteger(exponent, 16);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");// 返回RSA算法的公私密钥对
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);// 创造一个新的ＲＳＡ公钥的相关东西
		return keyFactory.generatePublic(keySpec);
	}

	/**
	 * 使用公钥的对象对数据进行解密
	 * 
	 * @param encryptedString
	 *            被加密的数据的byte型表示
	 * @param pubKey
	 *            用于解密的公钥对象
	 * @return 解密后的数据
	 * @throws Exception
	 */
	private byte[] decrypt(byte[] encryptedString, PublicKey pubKey)
			throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding"); // 得到转化之后的密文
		cipher.init(Cipher.DECRYPT_MODE, pubKey);
		// cipher.init(Cipher.DECRYPT_MODE, pKey);
		byte[] deBytes = cipher.doFinal(encryptedString);
		System.out.println("deBytes: " + byteToStringHex(deBytes));
		return deBytes;
	}

	/**
	 * 计算Hash值
	 * 
	 * @param hashInput
	 *            HASH的输入数据
	 * @return 计算出的HASH值
	 * @throws Exception
	 */
	private byte[] getHash(byte hashInput[]) throws Exception {
		MessageDigest msgDigest = MessageDigest.getInstance("SHA");
		byte digest[] = msgDigest.digest(hashInput);
		return digest;
	}

	/**
	 * 将字符串转化为byte数组
	 * 
	 * @param str
	 *            :字符串
	 * @return by:处理后的字符数组
	 * */
	private byte[] stringHexToByte(String str) {
		if (str == null)
			return null;
		int length = str.length();
		byte[] by = new byte[length / 2];
		for (int i = 0; i < length / 2; i++) {
			by[i] = (byte) Short
					.parseShort(str.substring(i * 2, i * 2 + 2), 16);
		}
		return by;
	}

	/**
	 * 将byte转化为字符串
	 * 
	 * @param b
	 *            要处理的byte
	 * @return 处理后的字符串
	 * */
	private String byteToStringHex(byte b) {
		StringBuffer hex = new StringBuffer();
		int byteL = 0x0F & (b >> 4);
		int byteR = 0x0F & b;
		hex.append(cHex[byteL]);
		hex.append(cHex[byteR]);
		return hex.toString();
	}

	/**
	 * 将byte数组转化为字符串
	 * 
	 * @param by是要转化的byte数组
	 * @return 转换之后的字符串
	 * */
	private String byteToStringHex(byte[] by) {
		if (by == null)
			return null;
		StringBuffer strHex = new StringBuffer();
		int len = by.length;
		for (int i = 0; i < len; i++) {
			String sTemp = Integer.toHexString(0xFF & by[i]).toUpperCase();
			if (sTemp.length() < 2)
				strHex.append(0);
			strHex.append(sTemp);
		}
		return strHex.toString();
	}

	private String byteToStringHex(byte[] by, int begin, int end) {
		StringBuffer strHex = new StringBuffer();
		for (int i = begin; i < end; i++) {
			String sTemp = Integer.toHexString(0xFF & by[i]).toUpperCase();
			if (sTemp.length() < 2)
				strHex.append(0);
			strHex.append(sTemp);
		}
		return strHex.toString();
	}

	/**
	 * 将解密后的数据按照《从发卡行公钥证书恢复的数据格式》显示
	 * 
	 * @param str
	 *            从发卡行公钥证书回复的数据，即解密之后的数据
	 * */
	private void printDeIPKC(String str) {
		int len = str.length();
		System.out.println("恢复数据头：" + str.substring(0, 2));
		System.out.println("证书格式：" + str.substring(2, 4));
		System.out.println("发卡行标识：" + str.substring(4, 12));
		System.out.println("证书失效日期：" + str.substring(12, 16));
		System.out.println("证书序列号：" + str.substring(16, 22));
		System.out.println("哈希算法标识：" + str.substring(22, 24));
		System.out.println("发卡行卡公钥算法标识：" + str.substring(24, 26));
		System.out.println("发卡行公钥长度：" + str.substring(26, 28));
		issueLen = str.substring(26, 28);
		// issueLen=Integer.parseInt(str.substring(26,28));
		System.out.println("发卡行公钥指数长度：" + str.substring(28, 30));
		System.out.println("发卡行公钥或发卡行公钥的最左边字节：" + str.substring(30, len - 42));
		issuePKStr = str.substring(30, len - 42);// 发卡行公钥数据最左边的数据
		System.out.println("hash结果：" + str.substring(len - 42, len - 2));
		deissuePKHash = str.substring(len - 42, len - 2);// 发卡行公钥哈希值
		System.out.println("恢复数据结尾：" + str.substring(len - 2, len));
	}

	/**
	 * 生成发卡行公钥证书Hash的输入数据
	 * */
	private String getIssueHashInput(String cic, String reminder,
			String caExp) {
		StringBuffer sb = new StringBuffer();
		sb.append(cic.subSequence(2, cic.length() - 42));
		sb.append(reminder);
		sb.append(caExp);
		return sb.toString();
	}

	/**
	 * 生成静态数据验证的哈希值
	 * 
	 * */
	private String getSDataInput(String deSData, String d) {
		StringBuffer sb = new StringBuffer();
		sb.append(deSData.subSequence(2, deSData.length() - 42));
		sb.append(d);
		sb.append("7C00");
		return sb.toString();
	}

	private String getDDataHashInput(String deDData,
			String unpredictableNum) {
		StringBuffer sb = new StringBuffer();
		sb.append(deDData.subSequence(2, deDData.length() - 42));
		sb.append(unpredictableNum);
		return sb.toString();
	}

	// 解析静态数据
	private void printDeSData(String str) {
		int len = str.length();
		System.out.println("恢复数据头：" + str.substring(0, 2));
		System.out.println("签名数据格式：" + str.substring(2, 4));
		System.out.println("hash算法标识：" + str.substring(4, 6));
		System.out.println("数据验证代码：" + str.substring(6, 10));
		System.out.println("填充字节：" + str.substring(10, len - 42));
		System.out.println("hash结果：" + str.substring(len - 42, len - 2));
		sDataHash = str.substring(len - 42, len - 2);
		System.out.println("恢复数据结尾：" + str.substring(len - 2, len));
	}

	private String deleteSpace(String str) {
		char[] cstr = str.toCharArray(); // 将字符串拆分为字符数组
		StringBuffer sb = new StringBuffer();// 建立字符缓冲区
		int strlen = str.length();
		for (int i = 0; i < strlen; i++) {
			if (cstr[i] == ' ')
				continue;
			sb.append(cstr[i]);
		}// 将字符连接起来，也就是说去掉中间的空格符
		return sb.toString();// 自动打印出来
	}

	private static String getICHashInput(String cic, String reminder,
			String icExp, String verSData) {
		StringBuffer sb = new StringBuffer();
		sb.append(cic.subSequence(2, cic.length() - 42));
		if (reminder != null)
			sb.append(reminder);
		sb.append(icExp);
		sb.append(verSData);
		sb.append("7C00");
		return sb.toString();
	}

	private void printDeICKCCitificate(String str) {
		int len = str.length();
		System.out.println("恢复数据头：" + str.substring(0, 2));
		System.out.println("证书格式：" + str.substring(2, 4));
		System.out.println("应用主账号：" + str.substring(4, 24));
		System.out.println("证书失效日期：" + str.substring(24, 28));
		System.out.println("证书序列号：" + str.substring(28, 34));
		System.out.println("哈希算法标识：" + str.substring(34, 36));
		System.out.println("IC卡公钥算法标识：" + str.substring(36, 38));
		System.out.println("IC卡公钥长度：" + str.substring(38, 40));
		icLen = str.substring(38, 40);
		System.out.println("IC卡公钥指数长度：" + str.substring(40, 42));
		System.out.println("IC卡公钥或IC卡公钥的最左边字节：" + str.substring(42, len - 42));
		icPKStr = str.substring(42, len - 42);// 发卡行公钥数据最左边的数据
		System.out.println("hash结果：" + str.substring(len - 42, len - 2));
		icPKHash = str.substring(len - 42, len - 2);// 发卡行公钥哈希值
		System.out.println("恢复数据结尾：" + str.substring(len - 2, len));

	}

	private void printDeDData(String str) {
		int len = str.length();
		System.out.println("恢复数据头：" + str.substring(0, 2));
		System.out.println("签名数据格式：" + str.substring(2, 4));
		System.out.println("哈希算法标识：" + str.substring(4, 6));
		System.out.println("IC卡动态数据长度：" + str.substring(6, 8));
		DDataLen = Integer.parseInt(str.substring(6, 8), 16);
		// issue=Integer.parseInt(issueLen, 16);
		System.out.println("IC卡动态数据：" + str.substring(8, 8 + DDataLen * 2));
		System.out.println("哈希算法标识："
				+ str.substring(8 + DDataLen * 2, len - 42));
		System.out.println("hash结果：" + str.substring(len - 42, len - 2));
		DDataHash = str.substring(len - 42, len - 2);// 发卡行公钥哈希值
		System.out.println("恢复数据结尾：" + str.substring(len - 2, len));
	}

	

	private String getCAHashInput(String cic, String reminder,
			String caExp) {
		StringBuffer sb = new StringBuffer();
		sb.append(cic.subSequence(2, cic.length() - 42));
		System.out.println("sb的长度：" + sb.length());
		sb.append(reminder);
		sb.append(caExp);
		return sb.toString();
	}
	
	public void setIssuePKCertificate(String issuePKCertificate) {
		this.issuePKCertificate = issuePKCertificate;
	}

	public void setIssuePKRemainder(String issuePKRemainder) {
		this.issuePKRemainder = issuePKRemainder;
	}

	public void setIssuePKExponent(String issuePKExponent) {
		this.issuePKExponent = issuePKExponent;
	}

	public void setCaPKExponent(String caPKExponent) {
		this.caPKExponent = caPKExponent;
	}

	public void setCaPKRemainder(String caPKRemainder) {
		this.caPKRemainder = caPKRemainder;
	}

	public void setSignedSData(String signedSData) {
		this.signedSData = signedSData;
	}

	public void setVerSData(String verSData) {
		this.verSData = verSData;
	}

	public void setEnIcPKCitificate(String enIcPKCitificate) {
		this.enIcPKCitificate = enIcPKCitificate;
	}

	public void setIcPKExponent(String icPKExponent) {
		this.icPKExponent = icPKExponent;
	}

	public void setIcPKRemainder(String icPKRemainder) {
		this.icPKRemainder = icPKRemainder;
	}

	public void setUnpredictableNum(String unpredictableNum) {
		this.unpredictableNum = unpredictableNum;
	}

	public void setSignedDData(String signedDData) {
		this.signedDData = signedDData;
	}

	/**
	 * doDda函数用来调整整个过程
	 * 
	 * @throws Exception
	 * */
	public void doDda() throws Exception {

		PublicKey CAKey;
		PublicKey IssuePKey;
		// 1、读取发卡行公钥证书
		//2、获得CA公钥对象 System.out.println("输入CA公钥指数：");
		try {
			CAKey = getPublicKey(caPKExponent, caPKRemainder);
			System.out.println("sda: getPublicKey CAKey = " + CAKey);
		} catch(Exception e) {
			System.out.println("解密发卡行公钥失败");
			e.printStackTrace();
			throw new Exception("解密发卡行公钥失败");	
		}
		
		// 3、解密发卡行公钥证书
		try {
			byte[] result = decrypt(stringHexToByte(issuePKCertificate), CAKey);
			deIssuePKCitificate = byteToStringHex(result);
			System.out.println("解密后的发卡行的公钥证书：长度："
					+ deIssuePKCitificate.length() + ";证书内容为："
					+ deIssuePKCitificate);
			printDeIPKC(deIssuePKCitificate);// 分析解密后的发卡行公钥证书
		} catch (Exception e) {
			System.out.println("解密发卡行公钥失败");
			e.printStackTrace();
			throw new Exception("解密发卡行公钥失败");
		}
		// 4、生成发卡行公钥哈希的输入值
		issuePKHashInput = getIssueHashInput(deIssuePKCitificate,
				issuePKRemainder, issuePKExponent);
		System.out.println("生成发卡行公钥证书Hash的输入数据：长度：" + issuePKHashInput.length()
				+ ";哈希输入数据为：" + issuePKHashInput);
		
		// 5、验证发卡行公钥证书
		try {
			issuePKHash = byteToStringHex(getHash(stringHexToByte(issuePKHashInput)));
			System.out.println("根据生成发卡行公钥证书Hash的输入数据得到的哈希值：长度："
				+ issuePKHash.length() + ";\n哈希值为：" + issuePKHash);
			if (issuePKHash.equals(deissuePKHash)) {
				System.out.println("该发卡行公钥证书可信");
			} else {
				System.out.println("该发卡行公钥证书不可信");
				throw new Exception("该发卡行公钥证书不可信");
			}
		} catch (Exception e) {
			System.out.println("验证发卡行公钥证书失败");
			e.printStackTrace();
			throw new Exception("该发卡行公钥证书不可信");
		}
		// 6、生成发卡行完整公钥
		try {
			issuePKMode = issuePKStr + issuePKRemainder;
			IssuePKey = getPublicKey(issuePKExponent, issuePKMode);// 获得发卡行公钥对象
		} catch (Exception e) {
			System.out.println("生成发卡行完整公钥失败");
			e.printStackTrace();
			throw new Exception("生成发卡行完整公钥失败");
		}
		// 7、获得卡上的静态应用数据
		
		// 8、解密静态应用数据
		try {
			deSignedSData = byteToStringHex(decrypt(stringHexToByte(signedSData),
				IssuePKey));
			System.out.println("解密后的静态数据：长度：" + deSignedSData.length() + ";数据为："
				+ deSignedSData);
			printDeSData(deSignedSData);
		} catch(Exception e) {
			System.out.println("解密静态应用数据失败");
			e.printStackTrace();
			throw new Exception("解密静态应用数据失败");
		}
		
		// 9、生成静态应用数据的哈希输入
		System.out.println("输入等待验证的静态应用数据：");
		// verSData=scanner.next();
		// 需要验证的数据
		sDataInput = getSDataInput(deSignedSData, verSData);
		System.out.println("生成的静态数据的哈希输入值：长度：" + sDataInput.length() + ";数据为："
				+ sDataInput);
		
		// 10、验证静态应用数据的哈希值
		try {
			deSDataHash = byteToStringHex(getHash(stringHexToByte(sDataInput)));
			System.out.println("根据生成发卡行公钥证书Hash的输入数据得到的哈希值：长度："
				+ deSDataHash.length() + ";\n哈希值为：" + deSDataHash);
			if (deSDataHash.equals(sDataHash)) {
				System.out.println("该静态数据可信");
			} else {
				System.out.println("该静态数据不可信");
				throw new Exception("该静态数据不可信");
			}
		} catch (Exception e) {
			System.out.println("该静态数据不可信");
			e.printStackTrace();
			throw new Exception("该静态数据不可信");
		}

		try {
			System.out.println("加密的IC卡公钥证书：长度" + enIcPKCitificate.length()
					+ "加密的公钥证书内容为：" + enIcPKCitificate);
			deICPKCitificate = byteToStringHex(decrypt(
					stringHexToByte(enIcPKCitificate), IssuePKey));
			System.out.println("解密后的IC卡公钥证书：长度" + deICPKCitificate.length()
					+ "解密后的公钥证书内容为：" + deICPKCitificate);
			printDeICKCCitificate(deICPKCitificate);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("解密IC卡公钥证书失败");
			throw new Exception("解密IC卡公钥证书失败");

		}
		icPKHashInput = getICHashInput(deICPKCitificate, icPKRemainder,
				icPKExponent, verSData);
		System.out.println("生成发ic卡公钥证书Hash的输入数据：长度：" + icPKHashInput.length()
				+ ";数据为：" + icPKHashInput);
		// 验证IC卡公钥证书
		try {
			deicPKHash = byteToStringHex(getHash(stringHexToByte(icPKHashInput)));
			System.out.println("根据生成ic卡公钥证书Hash的输入数据得到的哈希值：长度："
					+ deicPKHash.length() + ";\n哈希值为：" + deicPKHash);
			if (deicPKHash.equals(icPKHash)) {
				System.out.println("该IC卡公钥证书可信");
			} else {
				System.out.println("该IC卡公钥证书不可信");
				throw new Exception("该IC卡公钥证书不可信");
			}
		} catch (Exception e) {
			System.out.println("根据IC卡公钥哈希输入值得到哈希值失败！");
			e.printStackTrace();
			throw new Exception("该IC卡公钥证书不可信");
		}

		// 生成IC卡完整的公钥
		icPKMode = (icPKRemainder == null ? icPKStr : icPKStr + icPKRemainder);// 得到IC公钥模
		// if()
		int ic, issue;
		ic = Integer.parseInt(icLen, 16);
		issue = Integer.parseInt(issueLen, 16);
		if (ic <= issue - 42)
			icPKMode = icPKMode.substring(0, icPKMode.length()
					- (issue - 42 - ic) * 2);
		try {
			System.out.println("IC卡的公钥模为：" + icPKMode);
			icPKey = getPublicKey(icPKExponent, icPKMode);// 获得IC卡行公钥对象
		} catch (Exception e) {
			System.out.println("得到完整的icPKey失败");
			e.printStackTrace();
			throw new Exception("得到完整的icPKey失败");
		}
		try {
//			System.out.println("输入签名的动态应用数据：");
			System.out.println("签名的动态应用数据：长度" + signedDData.length()
					+ "签名的动态应用数据内容为：" + signedDData);
			deDData = byteToStringHex(decrypt(stringHexToByte(signedDData),
					icPKey));
			System.out.println("解密后的动态应用数据：长度" + deDData.length()
					+ "解密后的动态应用数据内容为：" + deDData);
			printDeDData(deDData);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("解密动态应用数据失败");
			throw new Exception("解密动态应用数据失败");
		}
//		System.out.println("输入不可预知数：");
		DDataHashInput = getDDataHashInput(deDData, unpredictableNum);
		System.out.println("动态数据验证哈希输入值：" + DDataHashInput);
		try {
			DDataHashcalculate = byteToStringHex(getHash(stringHexToByte(DDataHashInput)));
			System.out.println("根据生成动态应用数据Hash的输入数据得到的哈希值：长度："
					+ DDataHashcalculate.length() + ";\n哈希值为："
					+ DDataHashcalculate);
			if (DDataHashcalculate.equals(DDataHash)) {
				System.out.println("该动态应用数据可信");
				Log.v(DDataHash, "该动态应用数据可信");
			} else {
				System.out.println("该动态应用数据不可信"); 
				throw new Exception("该动态应用数据不可信");
			}
		} catch (Exception e) {
			System.out.println("根据动态应用数据哈希输入值得到哈希值失败！");
			e.printStackTrace();
			throw new Exception("该动态应用数据不可信");
		}
	}
	
//	public void doFdda() throws Exception {
//
//		PublicKey CAKey;
//		PublicKey IssuePKey;
//		// 1、读取发卡行公钥证书
//		//2、获得CA公钥对象 System.out.println("输入CA公钥指数：");
//		try {
//			CAKey = getPublicKey(caPKExponent, caPKRemainder);
//			System.out.println("sda: getPublicKey CAKey = " + CAKey);
//		} catch(Exception e) {
//			System.out.println("解密发卡行公钥失败");
//			e.printStackTrace();
//			throw new Exception("解密发卡行公钥失败");	
//		}
//		
//		// 3、解密发卡行公钥证书
//		try {
//			byte[] result = decrypt(stringHexToByte(issuePKCertificate), CAKey);
//			deIssuePKCitificate = byteToStringHex(result);
//			System.out.println("解密后的发卡行的公钥证书：长度："
//					+ deIssuePKCitificate.length() + ";证书内容为："
//					+ deIssuePKCitificate);
//			printDeIPKC(deIssuePKCitificate);// 分析解密后的发卡行公钥证书
//		} catch (Exception e) {
//			System.out.println("解密发卡行公钥失败");
//			e.printStackTrace();
//			throw new Exception("解密发卡行公钥失败");
//		}
//		// 4、生成发卡行公钥哈希的输入值
//		issuePKHashInput = getIssueHashInput(deIssuePKCitificate,
//				issuePKRemainder, issuePKExponent);
//		System.out.println("生成发卡行公钥证书Hash的输入数据：长度：" + issuePKHashInput.length()
//				+ ";哈希输入数据为：" + issuePKHashInput);
//		
//		// 5、验证发卡行公钥证书
//		try {
//			issuePKHash = byteToStringHex(getHash(stringHexToByte(issuePKHashInput)));
//			System.out.println("根据生成发卡行公钥证书Hash的输入数据得到的哈希值：长度："
//				+ issuePKHash.length() + ";\n哈希值为：" + issuePKHash);
//			if (issuePKHash.equals(deissuePKHash)) {
//				System.out.println("该发卡行公钥证书可信");
//			} else {
//				System.out.println("该发卡行公钥证书不可信");
//				throw new Exception("该发卡行公钥证书不可信");
//			}
//		} catch (Exception e) {
//			System.out.println("验证发卡行公钥证书失败");
//			e.printStackTrace();
//			throw new Exception("该发卡行公钥证书不可信");
//		}
//		// 6、生成发卡行完整公钥
//		try {
//			issuePKMode = issuePKStr + issuePKRemainder;
//			IssuePKey = getPublicKey(issuePKExponent, issuePKMode);// 获得发卡行公钥对象
//		} catch (Exception e) {
//			System.out.println("生成发卡行完整公钥失败");
//			e.printStackTrace();
//			throw new Exception("生成发卡行完整公钥失败");
//		}
//		// 7、获得卡上的静态应用数据
//		
//		// 8、解密静态应用数据
////		try {
////			deSignedSData = byteToStringHex(decrypt(stringHexToByte(signedSData),
////				IssuePKey));
////			System.out.println("解密后的静态数据：长度：" + deSignedSData.length() + ";数据为："
////				+ deSignedSData);
////			printDeSData(deSignedSData);
////		} catch(Exception e) {
////			System.out.println("解密静态应用数据失败");
////			e.printStackTrace();
////			throw new Exception("解密静态应用数据失败");
////		}
//		
//		// 9、生成静态应用数据的哈希输入
////		System.out.println("输入等待验证的静态应用数据：");
//		// verSData=scanner.next();
//		// 需要验证的数据
////		sDataInput = getSDataInput(deSignedSData, verSData);
////		System.out.println("生成的静态数据的哈希输入值：长度：" + sDataInput.length() + ";数据为："
////				+ sDataInput);
//		
//		// 10、验证静态应用数据的哈希值
////		try {
////			deSDataHash = byteToStringHex(getHash(stringHexToByte(sDataInput)));
////			System.out.println("根据生成发卡行公钥证书Hash的输入数据得到的哈希值：长度："
////				+ deSDataHash.length() + ";\n哈希值为：" + deSDataHash);
////			if (deSDataHash.equals(sDataHash)) {
////				System.out.println("该静态数据可信");
////			} else {
////				System.out.println("该静态数据不可信");
////				throw new Exception("该静态数据不可信");
////			}
////		} catch (Exception e) {
////			System.out.println("该静态数据不可信");
////			e.printStackTrace();
////			throw new Exception("该静态数据不可信");
////		}
//
//		try {
//			System.out.println("加密的IC卡公钥证书：长度" + enIcPKCitificate.length()
//					+ "加密的公钥证书内容为：" + enIcPKCitificate);
//			deICPKCitificate = byteToStringHex(decrypt(
//					stringHexToByte(enIcPKCitificate), IssuePKey));
//			System.out.println("解密后的IC卡公钥证书：长度" + deICPKCitificate.length()
//					+ "解密后的公钥证书内容为：" + deICPKCitificate);
//			printDeICKCCitificate(deICPKCitificate);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("解密IC卡公钥证书失败");
//			throw new Exception("解密IC卡公钥证书失败");
//
//		}
//		icPKHashInput = getICHashInput(deICPKCitificate, icPKRemainder,
//				icPKExponent, verSData);
//		System.out.println("生成发ic卡公钥证书Hash的输入数据：长度：" + icPKHashInput.length()
//				+ ";数据为：" + icPKHashInput);
//		// 验证IC卡公钥证书
//		try {
//			deicPKHash = byteToStringHex(getHash(stringHexToByte(icPKHashInput)));
//			System.out.println("根据生成ic卡公钥证书Hash的输入数据得到的哈希值：长度："
//					+ deicPKHash.length() + ";\n哈希值为：" + deicPKHash);
//			if (deicPKHash.equals(icPKHash)) {
//				System.out.println("该IC卡公钥证书可信");
//			} else {
//				System.out.println("该IC卡公钥证书不可信");
//				throw new Exception("该IC卡公钥证书不可信");
//			}
//		} catch (Exception e) {
//			System.out.println("根据IC卡公钥哈希输入值得到哈希值失败！");
//			e.printStackTrace();
//			throw new Exception("该IC卡公钥证书不可信");
//		}
//
//		// 生成IC卡完整的公钥
//		icPKMode = (icPKRemainder == null ? icPKStr : icPKStr + icPKRemainder);// 得到IC公钥模
//		// if()
//		int ic, issue;
//		ic = Integer.parseInt(icLen, 16);
//		issue = Integer.parseInt(issueLen, 16);
//		if (ic <= issue - 42)
//			icPKMode = icPKMode.substring(0, icPKMode.length()
//					- (issue - 42 - ic) * 2);
//		try {
//			System.out.println("IC卡的公钥模为：" + icPKMode);
//			icPKey = getPublicKey(icPKExponent, icPKMode);// 获得IC卡行公钥对象
//		} catch (Exception e) {
//			System.out.println("得到完整的icPKey失败");
//			e.printStackTrace();
//			throw new Exception("得到完整的icPKey失败");
//		}
//		try {
////			System.out.println("输入签名的动态应用数据：");
//			System.out.println("签名的动态应用数据：长度" + signedDData.length()
//					+ "签名的动态应用数据内容为：" + signedDData);
//			deDData = byteToStringHex(decrypt(stringHexToByte(signedDData),
//					icPKey));
//			System.out.println("解密后的动态应用数据：长度" + deDData.length()
//					+ "解密后的动态应用数据内容为：" + deDData);
//			printDeDData(deDData);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("解密动态应用数据失败");
//			throw new Exception("解密动态应用数据失败");
//		}
////		System.out.println("输入不可预知数：");
//		DDataHashInput = getDDataHashInput(deDData, unpredictableNum);
//		System.out.println("动态数据验证哈希输入值：" + DDataHashInput);
//		try {
//			DDataHashcalculate = byteToStringHex(getHash(stringHexToByte(DDataHashInput)));
//			System.out.println("根据生成动态应用数据Hash的输入数据得到的哈希值：长度："
//					+ DDataHashcalculate.length() + ";\n哈希值为："
//					+ DDataHashcalculate);
//			if (DDataHashcalculate.equals(DDataHash)) {
//				System.out.println("该动态应用数据可信");
//				Log.v(DDataHash, "该动态应用数据可信");
//			} else {
//				System.out.println("该动态应用数据不可信"); 
//				throw new Exception("该动态应用数据不可信");
//			}
//		} catch (Exception e) {
//			System.out.println("根据动态应用数据哈希输入值得到哈希值失败！");
//			e.printStackTrace();
//			throw new Exception("该动态应用数据不可信");
//		}
//	}
}