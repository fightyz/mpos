package com.mcg.mpos.pbocKeyGenerator;
public class HexAndStringTransformer {
	private final static String HEX = "0123456789ABCDEF";//������ɿɶ�д��16�����ַ�
	
	
	/**
	 * ���ڽ���ɵļ��ܺ��ԭʼ��ݣ�String����ʽ��ʾ��ת���ɿɶ���16���Ʊ�ʾ���ַ�
	 * @param txt ��ɵ�String���͵ļ��ܺ����ַ�
	 * @return	��16���Ʊ�ʾ���ַ�
	 */
	public static String toHex(String txt) 
	{
		return toHex(txt.getBytes());
	}
	
	/**
	 * ���ڽ���ɵļ��ܺ��ԭʼ��ݣ�byte����ʽ��ʾ��ת���ɿɶ���16���Ʊ�ʾ���ַ�
	 * @param buf byte�ͱ�ʾ�ļ��ܺ��ԭʼ���
	 * @return ��16���Ʊ�ʾ���ַ���ʽ�ļ��ܺ����
	 */
	public static String toHex(byte[] buf) 
	{
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2 * buf.length);
		for (int i = 0; i < buf.length; i++) 
		{
			appendHex(result, buf[i]);
		}
		return result.toString();
	}

	/**
	 * ���ڽ�16���Ʊ�ʾ�ļ��ܺ���ַ�ת����byte��ݣ����λ����byte[]������������
	 * @param hex	String�͵�16�����ַ�
	 * @return	���byte��ʾ�ļ��ܺ�����(����Ϊ������decryptData����)
	 */
	public static byte[] toByte(String hexString) 
	{
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
		return result;
	}

	
	private static void appendHex(StringBuffer sb, byte b) 
	{
		sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
	}
}
