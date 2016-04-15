package com.mcg.mpos.pbocKeyGenerator;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * <code>MACGen</code>��ΪȦ��ű����MAC
 * @author ��չ��
 * @version 1.0
 */
public class MACGen {

//	public static void main(String[] args) {
//		Scanner scanner=new Scanner(System.in);
//		System.out.println("����������ݣ�");
//		String data=scanner.next();
//		System.out.println("����MAC�����Կ");//822492068CD484E4E4A5B17CF57ACD43
//		String key=scanner.next();
//
//		byte[] MAC=generateMAC(data, key);
//		System.out.println("�õ�MACֵΪ��");
//		System.out.println(HexAndStringTransformer.toHex(MAC));
//		
//	}
	
	public static byte[] generateMAC(String seed, String key){
		String padded_seed=StringUtil.padding7816M2(seed);
		byte[] rawData=HexAndStringTransformer.toByte(padded_seed);
		int noOfBlocks=rawData.length/8;
		
		byte[] singleKey=HexAndStringTransformer.toByte(key.substring(0, 16));
		byte[] doubleKey=HexAndStringTransformer.toByte(key);
		byte[] tripleKey=ByteUtil.byteMerge(doubleKey, singleKey);
		
		final SecretKey single_secretkey=new SecretKeySpec(singleKey, "DES");
		final SecretKey triple_secretkey=new SecretKeySpec(tripleKey, "DESede");
		
		byte[] init_iv=new byte[8];
//		byte[] init_iv=new byte[]{(byte)0x00,(byte)0x01,(byte)0x02,(byte)0x03,
//			(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
		
		byte[] I1=Arrays.copyOfRange(rawData, 0, 8);
		byte[] I2=ByteUtil.XOR(init_iv, I1);
		byte[] MAC=I2;
		
		Cipher singleDesCipher;
		try {
			singleDesCipher = Cipher.getInstance("DES/ECB/NoPadding");
			
			for(int i=1; i<noOfBlocks; i++){
				singleDesCipher.init(Cipher.ENCRYPT_MODE, single_secretkey);
				MAC = singleDesCipher.doFinal(MAC);
				MAC = ByteUtil.XOR(MAC, Arrays.copyOfRange(rawData, i*8, (i+1)*8));		
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}

		Cipher tripleDesCipher;
		try {
			tripleDesCipher = Cipher.getInstance("DESede/ECB/NoPadding");
			tripleDesCipher.init(Cipher.ENCRYPT_MODE, triple_secretkey);
			MAC = tripleDesCipher.doFinal(MAC);		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
		
		return Arrays.copyOfRange(MAC, 0, 4);
	}

}
