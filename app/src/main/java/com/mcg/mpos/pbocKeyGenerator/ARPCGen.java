package com.mcg.mpos.pbocKeyGenerator;
import java.util.Scanner;

/**
 * <code>ARPCGen</code>�����ARPC
 * @author ��չ��
 * @version 1.0
 */
public class ARPCGen {

//	public static void main(String[] args) {
//		Scanner scanner=new Scanner(System.in);
//		System.out.println("����ARC�룺");
//		String ARC=scanner.next();
//		System.out.println("����ARQC��");
//		String ARQC=scanner.next();
//		System.out.println("����ػ���Կ��");
//		String sessionKey=scanner.next();
//		
//		System.out.println("�õ�ARPC��");
//		byte[] ARPC=generateARPC(ARC, ARQC, sessionKey);
//		String ARPCString=HexAndStringTransformer.toHex(ARPC);
//		System.out.println(ARPCString);
//
//	}
	
	public static byte[] generateARPC(String ARC, String ARQC, String sessionKey){
		byte[] ARPC=new byte[8];
		byte[] ARQCBytes=HexAndStringTransformer.toByte(ARQC);
		byte[] sessionKeyBytes=HexAndStringTransformer.toByte(sessionKey);
		String X=StringUtil.append16(ARC);
		byte[] XBytes=HexAndStringTransformer.toByte(X);
		byte[] Y=ByteUtil.XOR(ARQCBytes, XBytes);
		
		ARPC=DESedeAgl.run_3DES(Y, sessionKeyBytes);
		
		return ARPC;
	}
	

}
