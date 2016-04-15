package com.mcg.mpos.tlv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 *
 * @brief <p>
 *        <b>TLVParser������</b>
 *        </p>
 *
 *        &nbsp;&nbsp;&nbsp;&nbsp;TLVParser������
 *
 *        <p>
 *        <center>COPYRIGHT (C) 2000-2006,CoreTek Systems Inc.All Rights
 *        Reserved.</center>
 *        </p>
 * @author wangl
 * @version eJPos SDK1.0
 * @see
 * @since 2008-6-23
 */
public class TLVParser {
	private InputStream mInputStream;

	/**
	 *
	 * @brief ���캯��
	 *
	 * @param in
	 *            ���������
	 */
	public TLVParser(InputStream input) {
		this.mInputStream = input;
	}

	/**
	 * ��ȡ���������
	 *
	 * @return ���������
	 */
	public InputStream getInput() {
		return mInputStream;
	}

	/**
	 * �������������
	 *
	 * @param input
	 *            ���������
	 */
	public void setInput(InputStream input) {
		this.mInputStream = input;
	}

	/**
	 *
	 * @brief ����Tag
	 *
	 * @param in
	 *            ���������
	 * @param out
	 *            Tag��������
	 * @return �Ƿ�����ɹ�
	 * @throws TLVParserException
	 *             TLV����ʧ��
	 */
	public boolean parseTag(InputStream in, OutputStream out)
			throws TLVParserException {
		try {
			byte value = TLVUtils.readByte(in);
			if (available() > 0) {
				if (value == (byte) 0x00 || value == (byte) 0xFF) {
					return false;
				} else {
					return parseTagImpl(in, out, value);
				}
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new TLVParserException("TLV parseTag Parse Fail");
		}
	}

	/**
	 * @brief ����TAG
	 *
	 * @param in
	 *            ���������
	 * @param out
	 *            TAG��������
	 * @param value
	 *            TAG��һ��ֵ
	 * @return �����Ƿ�ɹ�
	 * @throws TLVParserException
	 *             ����ʧ��
	 * @throws IOException
	 *             һ�㲻���ֳ���ڴ治��
	 */
	private boolean parseTagImpl(InputStream in, OutputStream out, byte value)
			throws TLVParserException {
		try {
			TLVUtils.writeByte(out, value);
			// �жϵ�ǰ�ֽڱ�ǵĺ���λ�Ƿ�Ϊ'1',���Ϊ1,��ô�ͼ������tag�ĺ���
			if (TLVUtils.lastFiveEqualsOne(value)) {
				parseTagRemaining(in, out);
			} else {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new TLVParserException("TLV parseTagImpl write byte is Fail");
		} catch (TLVParserException e) {
			e.printStackTrace();
			throw new TLVParserException("TLV parseTagImpl Parse Fail");
		}
		return true;
	}

	/**
	 *
	 * @brief ����Tag�ĺ����ֽ�
	 *
	 * @param in
	 *            ���������
	 * @param out
	 *            Tag��������
	 */
	void parseTagRemaining(InputStream in, OutputStream out)
			throws TLVParserException {
		byte value = -1;
		try {
			value = TLVUtils.readByte(in);
			if (TLVUtils.highBitEqualsOne(value)) {
				parseTagRemaining(in, out);
			} else {
				TLVUtils.writeByte(out, value);
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new TLVParserException("TLV TagRemaining Parse Fail");
		}
	}

	/**
	 *
	 * @brief ��������
	 *
	 * @param in
	 *            ���������
	 * @param out
	 *            ��������
	 * @throws TLVParserException
	 *             ����ʧ��
	 */
	public void parseLength(InputStream in, OutputStream out)
			throws TLVParserException {
		if (available() > 0) {
			try {
				byte value = TLVUtils.readByte(in);
				int highBit = value & TLVUtils.FIRST_MASK;
				int lowBit = value & TLVUtils.LASTSEVEN_MASK;
				if (highBit == TLVUtils.FIRST_MASK) {
					byte[] lenBuff = new byte[lowBit];
					in.read(lenBuff);
					out.write(lenBuff);
				} else {
					out.write(lowBit);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new TLVParserException("TLV Length Parse Fail");
			}
		}
	}

	/**
	 *
	 * @brief ����Valueֵ
	 *
	 * @param in
	 *            ���������
	 * @return
	 * @throws TLVParserException
	 *             ����ʧ��
	 */
	public void parseValue(long length, InputStream in, OutputStream out)
			throws TLVParserException {
		if (available() > 0) {
			byte[] valueBuffer = new byte[(int) length];
			try {
				in.read(valueBuffer);
				out.write(valueBuffer);
			} catch (IOException e) {
				e.printStackTrace();
				throw new TLVParserException("TLV Value Parse Fail");
			}
		}
	}

	/**
	 * @brief ����TLVElement
	 *
	 * @param in
	 *            ���������
	 * @throws TLVParserException
	 *             ����ʧ��
	 */
	public TLVElement parseTLVElement() throws TLVParserException {
		if (available() > 0) {
			ByteArrayOutputStream tagBuffer = new ByteArrayOutputStream();
			ByteArrayOutputStream lenBuffer = new ByteArrayOutputStream();
			ByteArrayOutputStream valueBuffer = new ByteArrayOutputStream();
			if (parseTag(mInputStream, tagBuffer)) {
				parseLength(mInputStream, lenBuffer);
				byte[] tag = tagBuffer.toByteArray();
				byte[] len = lenBuffer.toByteArray();
				long length = TLVUtils.convertBytesToNumber(len);
				TLVElement element = new TLVElement(
						TLVUtils.convertBytesToString(tag), length);
				// �жϵ�ǰ�ı���Ƿ�Ϊ�ṹ������
				if (TLVUtils.thirdBitEqualsOne(tag[0])) {
					element.setConstruct(true);
					TLVParser childParser = new TLVParser(mInputStream);
					while (childParser.available() > 0) {
						TLVElement child = childParser.parseTLVElement();
						if (child != null) {
							element.addChild(child);
						}
					}
				} else {
					parseValue(length, mInputStream, valueBuffer);
					byte[] bytesValue = valueBuffer.toByteArray();
					element.setConstruct(false);
					element.setValue(bytesValue);
				}
				return element;
			} else {
				return parseTLVElement();
			}
		} else {
			return null;
		}
	}

	public Vector<TLVElement> parseAllTLVElement() throws TLVParserException{
		Vector<TLVElement> tlv = new Vector<TLVElement>();
		while(available() > 0){
			TLVElement e = parseTLVElement();
			if(null != e){
				tlv.add(e);
			}else{
				break;
			}
		}
		return tlv;
	}

	private TLVElement parseDOLimp() throws TLVParserException {
		if (available() > 0) {
			ByteArrayOutputStream tagBuffer = new ByteArrayOutputStream();
			ByteArrayOutputStream lenBuffer = new ByteArrayOutputStream();
			if (parseTag(mInputStream, tagBuffer)) {
				parseLength(mInputStream, lenBuffer);
				byte[] tag = tagBuffer.toByteArray();
				byte[] len = lenBuffer.toByteArray();
				long length = TLVUtils.convertBytesToNumber(len);
				TLVElement element = new TLVElement(
						TLVUtils.convertBytesToString(tag), length);
				return element;
			} else {
				return parseDOLimp();
			}
		} else {
			return null;
		}
	}

	public Vector<TLVElement> parseDOL() throws TLVParserException{
		Vector<TLVElement> tlv = new Vector<TLVElement>();
		while(available() > 0){
			TLVElement e = parseDOLimp();
			if(null != e){
				tlv.add(e);
			}else{
				break;
			}
		}
		return tlv;
	}

	/**
	 *
	 * @brief �жϵ�ǰ�����Ƿ����
	 *
	 * @return δ��������ֽ���
	 * @throws IOException
	 *             һ�㲻����ֳ���ڴ治��
	 */
	public int available() {
		try {
			return mInputStream.available();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * @brief ����TLVElement,���жϵ�ǰtag�Ƿ�Ϊ�ṹ������ֱ�Ӷ�ȡvalueֵ
	 *
	 * @param in
	 *            ���������
	 * @throws TLVParserException
	 *             ����ʧ��
	 */
	public TLVElement parseParentTLVElement() throws TLVParserException {
		if (available() > 0) {
			ByteArrayOutputStream tagBuffer = new ByteArrayOutputStream();
			ByteArrayOutputStream lenBuffer = new ByteArrayOutputStream();
			ByteArrayOutputStream valueBuffer = new ByteArrayOutputStream();
			if (parseTag(mInputStream, tagBuffer)) {
				parseLength(mInputStream, lenBuffer);
				byte[] tag = tagBuffer.toByteArray();
				byte[] len = lenBuffer.toByteArray();
				long length = TLVUtils.convertBytesToNumber(len);
				TLVElement element = new TLVElement(
						TLVUtils.convertBytesToString(tag), length);
				// �жϵ�ǰ�ı���Ƿ�Ϊ�ṹ������
				parseValue(length, mInputStream, valueBuffer);
				byte[] bytesValue = valueBuffer.toByteArray();
				element.setValue(bytesValue);
				return element;
			} else {
				return parseParentTLVElement();
			}
		} else {
			return null;
		}
	}
}
