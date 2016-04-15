package com.mcg.mpos.pbocKeyGenerator;
import java.util.Scanner;


public class StringUtil {

	/**
	 * ���ַ��ȡ�ɳ���Ϊ16���ַ�����С��16�ڸ�λ��0
	 * @param input
	 * @return
	 */
	public static String truncate16(String input){
		String output = null;
		int len=input.length();
		//����ַ��ȴ��ڵ���16��ȡ�ַ��16������
		if(len>16){
			output=input.substring(len-16);
		}
		else{//����ַ���С��16�����ַ�����16������
			output=input;
			for(int i=0; i<16-len; i++){
				output="0"+output;
			}
		}
		
		return output;
	}

	/**
	 * ���ַ�β�����0��ֱ���õ�����Ϊ16���ַ�
	 * @param input
	 * @return β������Ľ��
	 */
	public static String append16(String input){
		String output=null;
		int len=input.length();
		if(len>16){
			System.err.println("ֵ��Ӧ�ó���16���ַ�");
		}else{
			output=input;
			for(int i=0; i<16-len; i++){
				output=output+"0";
			}
		}
		
		return output;
	}
	
	/**
	 * 80 00 00...
	 * @param input
	 * @return
	 */
	public static String padding7816M2(String input){
		String output=input;
		int len=output.length();
		//�ж��Ƿ���16�����ֵı���8���ֽڵı���
		if((len%16)!=0){//����ǣ������80
			output+="80";
			len+=2;
			while((len%16)!=0){//�����80֮�󣬻�����8���ֽڵı���������00��ֱ��Ϊ8�ֽڵı���
				output+="00";
				len+=2;
			}
		}else{
			output+="8000000000000000";
		}
		
		return output;
	}
	//���ڲ��Ժ�����
//	public static void main(String args[]){
//		Scanner scanner=new Scanner(System.in);
//		System.out.println("�������ⳤ�ȵ�һ���ַ������ܲ���ʵ��β����䵽16�����ֵĹ���");
//		String input=scanner.next();
//		String output=append16(input);
//		System.out.println(output);
//	}
}
