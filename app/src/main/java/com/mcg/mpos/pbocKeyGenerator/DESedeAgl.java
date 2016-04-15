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


public class DESedeAgl {
	
//	public static void main(String[] args){
//		Scanner scanner=new Scanner(System.in);
//		System.out.println("������ݣ�");
//		String data=scanner.next();
//		System.out.println("������Կ");
//		String key=scanner.next();
//
//
//		
//		byte[] result=run_3DES(HexAndStringTransformer.toByte(data), HexAndStringTransformer.toByte(key));
//		System.out.println("�õ���֤���Ϊ��");
//		System.out.println(HexAndStringTransformer.toHex(result));
//		
//	}

	public static byte[] run_3DES(byte[] seed, byte[] key) {
		byte[] cipherText=new byte[8];
		byte[] DESedeKey=new byte[24];
		System.arraycopy(key, 0, DESedeKey, 0, 16);
		System.arraycopy(key, 0, DESedeKey, 16, 8);
		final SecretKey secretkey=new SecretKeySpec(DESedeKey, "DESede");
		
		try {
			Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");//ѡNoPadding��Ҫ��Ȼ������Ϊ8�ֽڵ�����£���padding8���ֽ�
			cipher.init(Cipher.ENCRYPT_MODE, secretkey);
			cipherText = cipher.doFinal(seed);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

		return cipherText;
	}
	
}
