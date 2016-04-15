package com.mcg.mpos.pbocKeyGenerator;
import java.util.Arrays;
import java.util.Scanner;


public class ByteUtil {

	/**
	 * �ϲ��ֽ�����
	 * @param byte1 Ҫ�ϲ��ĵ�һ���ֽ�����
	 * @param byte2 Ҫ�ϲ��ĵڶ����ֽ�����
	 * @return �ϲ�֮����ֽ�����
	 */
	public static byte[] byteMerge(byte[] byte1, byte[] byte2){
		byte[] byte3=new byte[byte1.length+byte2.length];
		
		System.arraycopy(byte1, 0, byte3, 0, byte1.length);
		System.arraycopy(byte2, 0, byte3, byte1.length, byte2.length);
		
		return byte3;
	}
	
	/**
	 * ��byte��������8���ֽڣ�������8���ֽڣ���ֻȡ��8���ֽ�
	 * @param src ������byte����
	 * return �����8�ֽ�����
	 */
	public static byte[] padTo8(byte[] src){
		byte[] result= new byte[]
				{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
				 (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};//��ʼ��Ϊ0��8���ֽ�
//		Arrays.fill(result, (byte)0x00);//�����������ʽ������������Ԫ�ظ�ֵΪ0x00
		
		if(src.length>=result.length){
			System.arraycopy(src, 0, result, 0, result.length);
		}else{
			System.arraycopy(src, 0, result, result.length-src.length, src.length);
		}
		
		return result;
	}
	
	/**
	 * 80 00 00...
	 * @param input
	 * @return
	 */
	public static byte[] padding7816M2(byte[] input){
		int len=input.length;
		byte[] result=new byte[input.length+8-input.length%8];
		
		System.arraycopy(input, 0, result, 0, len);
		
		result[len++]=(byte)0x80;
		while(len%8!=0){
			result[len++]=(byte)0x00;
		}
	
		return result;
	}
	
	/**
	 * ��byte����ȡ������
	 * @param input
	 * @return ȡ�����byte���
	 */
	public static byte[] XNOT(byte[] input){
		byte[] output=new byte[input.length];
		byte[] operator=new byte[input.length];
		Arrays.fill(operator, (byte)0xFF);

		for(int i=0; i<output.length; i++){
			output[i]=(byte) (input[i]^operator[i]);
		}
		
		return output;
	}
	
	/**
	 * ����byte��������������
	 * @param input
	 * @return �������Ľ��
	 */
	public static byte[] XOR(byte[] a, byte[] b){
		int aLen=a.length;
		int bLen=b.length;
		if(aLen!=bLen){
			System.err.println("�������������������鳤�Ȳ����");
		}
		
		byte[] c=new byte[aLen];
		for(int i=0; i<aLen; i++){
			c[i]=(byte) (a[i]^b[i]);
		}
	
		return c;
	}

	//���ڲ���
	public static void main(String[] args){
		byte[] byte1=new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
		byte[] byte2=new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01};
		
		byte[] byte3;
//		
//		byte[] afterMerge=byteMerge(byte1, byte2);
//		
//		Arrays.fill(byte1, (byte)0x00);
		byte3=padding7816M2(byte1);
//		byte3=XOR(byte1,byte2);
		System.out.println(HexAndStringTransformer.toHex(byte3));

	}
}
