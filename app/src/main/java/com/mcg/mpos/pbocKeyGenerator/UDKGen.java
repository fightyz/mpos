package com.mcg.mpos.pbocKeyGenerator;
import java.util.Scanner;

import android.util.Log;

/**
 * <code>UDKGen</code>�����÷���������Կ����IC���ϵ�����Կ��
 * ��Ҫ��ͨ�����{@link UDKGen#generateUDK(byte[], byte[], byte[]) generateUDK()}����
 * @author ��չ��
 * @version 1.0
 */

public class UDKGen {

//	public static void main(String[] args) {
//		Scanner scanner=new Scanner(System.in);
//		System.out.println("����PAN�룺");
//		String PAN=scanner.next();//6217004220000843361
//		System.out.println("����PAN���кţ�");
//		String SerialNumber=scanner.next();//01
//		System.out.println("��������ԿMDK��");
//		String MDK=scanner.next();//FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF(Ҫ���Ƿ�ɢ��Կ����������ĸ���Կ�����ǵĿ�������02����֪���õ����ĸ���Կ)
//		
//		byte[] UDKBytes=generateUDK(PAN, SerialNumber, MDK);
//		String UDKString=HexAndStringTransformer.toHex(UDKBytes);
//		System.out.println("�õ�����ԿΪ��");
//		System.out.println(UDKString);
//	}
		
	/**
	 * ����64λ������ܵ��㷨��
	 * Zl=ALG(IMK)[Y], Zr=ALG(IMG)[Y^(FF FF FF FF FF FF FF FF)]
	 * Z=(Zl||Zr)
	 * @param seed
	 * @param key
	 * @return ɢ�г�������ԿZ
	 */
	private static byte[] PBOC_Diversify64(byte[] seed, byte[] key) {
		byte[] cipherText=new byte[16];
		byte[] seedAnti=ByteUtil.XNOT(seed);
		byte[] Zl=new byte[8];
		byte[] Zr=new byte[8];

		Zl=DESedeAgl.run_3DES(seed, key);
		Zr=DESedeAgl.run_3DES(seedAnti, key);
		
		cipherText=ByteUtil.byteMerge(Zl, Zr);
		
		return cipherText;
	}
	
	public static byte[] generateUDK(String PAN, String PANserialNumber, String MDK){
		
		Log.i("UDKGen", "PAN = " + PAN + " PANserialNumber = " + PANserialNumber + " MDK = " + MDK);
		String seed=StringUtil.truncate16(PAN+PANserialNumber);//��ȡPAN�����к����ӳɵ���16�����֣���Ϊ��Կ��������
		byte[] seedByte=HexAndStringTransformer.toByte(seed);//������ת����byte���ͣ�������ܲ���
		byte[] MDKByte=HexAndStringTransformer.toByte(MDK);//������Կת����byte����
		byte[] key=new byte[16];//���������ɵ�IC������Կ,16���ֽ�
		
		key=PBOC_Diversify64(seedByte, MDKByte);
		
		return key;		
	}

}
