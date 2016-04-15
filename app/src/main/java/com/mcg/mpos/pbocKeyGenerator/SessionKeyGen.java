package com.mcg.mpos.pbocKeyGenerator;
import java.util.Scanner;

/**
 * <code>SessionKeyGen</code>��������ɵ�����Կ��������Կ��
 * ��Ҫ��ͨ�����{@link generateSessionKey(String, String) generateSessionKey()}����
 * @author ��չ��
 * @version 1.0
 */
public class SessionKeyGen {
	
//	public static void main(String[] args) {
//		Scanner scanner=new Scanner(System.in);
//		System.out.println("����ATC�룺");
//		String ATC=scanner.next();
//		System.out.println("����IC������ԿUDK��");
//		String UDK=scanner.next();
//		
//		System.out.println("�õ������ԿΪ��");
//		byte[] sessionKeyBytes=generateSessionKey(ATC, UDK);
//		String sessionKeyString=HexAndStringTransformer.toHex(sessionKeyBytes);
//		System.out.println(sessionKeyString);
//	}
	
	public static byte[] generateSessionKey(String ATC, String UDK){
		byte[] seed=HexAndStringTransformer.toByte(ATC);//������ת����byte���ͣ�������ܲ���
		byte[] seedAnti=ByteUtil.XNOT(seed);
		byte[] UDKByte=HexAndStringTransformer.toByte(UDK);//������Կת����byte����
		byte[] key=new byte[16];//���������ɵ�IC������Կ,16���ֽ�
		byte[] Zl=new byte[8];
		byte[] Zr=new byte[8];
		
		Zl=PBOC_DiversifySessionKey(ByteUtil.padTo8(seed), UDKByte);
		Zr=PBOC_DiversifySessionKey(ByteUtil.padTo8(seedAnti), UDKByte);
		
		key=ByteUtil.byteMerge(Zl, Zr);
		
		return key;		
	}

	private static byte[] PBOC_DiversifySessionKey(byte[] seed, byte[] key) {
		byte[] cipherText=new byte[8];

		cipherText=DESedeAgl.run_3DES(seed, key);
		return cipherText;
	}
}
