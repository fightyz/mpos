package com.risetek.UnionPayLib.sd;

import android.util.Log;

/**
 * 银联支付异常类
 */
public class UnionPayPaymentException extends Exception {

	public static final int UPP_RSP_CODE_OK = 0;
	public static final int UPP_DEFAULT_ERR = 1; // 中国银联支付默认错误
	public static final int UPP_RSP_ARGUMENT_ERROR = 2; // 参数错误
	public static final int UPP_NO_SESSION_ID = 3; // 未配置会话ID
	public static final int UPP_NO_SESSION_KEY = 4; // 未配置会话密钥
	public static final int UPP_NO_PUBLIC_KEY = 5; // 未配置银联公钥
	public static final int UPP_PARSE_PAA_ERR = 6; // 支付激活文件XML格式错误
	public static final int UPP_PARSE_LPAA_ERR = 7; // 轻量级支付激活文件XML格式错误
	public static final int UPP_PAA_ERR = 8; // 支付激活文件不符合银联规范
	public static final int UPP_LPAA_ERR = 9; // 轻量级支付激活文件不符合银联规范
	public static final int UPP_TRANSACTION_NOT_INIT = 10; // 交易未初始化
	public static final int UPP_HTTP_TRANSFERERROR = 11; // http传输错误
	public static final int UPP_HTTP_RESPONSE_ERROR = 12; // http返回非200
	//SD卡部分
	public static final int UPP_NO_SDCARD = 16; // 无SD卡
	public static final int UPP_NOT_SMARTCARD = 17; // 不是智能卡
	public static final int UPP_SMARTCARD_SW_ERROR = 18; // 智能卡过程字节错误
	public static final int UPP_SMARTCARD_NOT_PERSONNAL = 19; // 智能卡未个人化
	public static final int UPP_SMARTCARD_TIMEOUT = 20; // 智能卡操作超时
	public static final int UPP_SMARTCARD_COMMUNICATIONERROR = 21; // 智能卡通讯错误
	public static final int UPP_SMARTCARD_RESPONSE_DATA_ERROR = 22; // 智能卡返回数据错误
	public static final int UPP_SMARTCARD_NOT_PBOC = 23; // 不是PBOC卡
	public static final int UPP_SMARTCARD_NONEEDTAGVALUE = 24; // 没有所对应的TAG值
	//用于verify
	public static final int UPP_SMARTCARD_LOCKED = 0xe5; //智能卡已经锁定
	public static final int UPP_SMARTCARD_VERIFY_ERROR_BASE = 0xe20; //PIN校验错误，智能卡未锁定
	//VIPOS部分
	public static final int UPP_NO_AUDIO = 25; //未插入音频设备
	public static final int UPP_NOT_VIPOSAUDIO = 26; //不支持的音频设备
	public static final int UPP_VIPOSAUDIO_CMD_FAIL = 27; //刷卡器通信失败
	public static final int UPP_VIPOSAUDIO_GETCARDINFO_FAIL = 28; //银行卡信息读取失败
	
	public static final int UPP_RESPONSECODE_ERROR = 30;//支付响应数据错误
	public static final int UPP_NOTRESPONSECODE_ERROR = 31; //支付响应报文不含responseCode元素
	public static final int UPP_LOADREVERSAL_SUCCESS = 32;//银联响应数据显示圈存冲正成功
	public static final int UPP_LOADREVERSAL_FAIL = 33;//银联响应数据显示圈存冲正失败
	public static final int UPP_LOADREVERSAL_TIMEOUT = 34;//其他原因造成的圈存冲正失败
	//网络连接错误码
	public static final int NET_CONNECT_HTTPHOST_ERROR = 35; // 未连接网络
	public static final int NET_CONNECT_TIMEOUT = 36; //连接网络超时
	public static final int NET_CONNECT_SOCKETTIMEOUT = 37;	//网络接收数据超时
	public static final int NET_CONNECT_URLPARSE = 38; //URL格式错误

	public static final String UPP_DEFAULT_ERR_MESSAGE = "Transaction default error!";
	public static final String UPP_RSP_ARGUMENT_ERROR_MESSAGE = "Argument error!";
	public static final String UPP_NO_SESSION_ID_MESSAGE = "Session id do not config!";
	public static final String UPP_NO_SESSION_KEY_MESSAGE = "Session key do not config!";
	public static final String UPP_NO_PUBLIC_KEY_MESSAGE = "UnionPay public key do not config!";
	public static final String UPP_PARSE_PAA_ERR_MESSAGE = "Parse paa error!";
	public static final String UPP_PARSE_LPAA_ERR_MESSAGE = "Parse lpaa!";
	public static final String UPP_PAA_ERR_MESSAGE = "Paa not follow with UnionPay protocol!";
	public static final String UPP_LPAA_ERR_MESSAGE = "Lpaa not follow with UnionPay protocol!";
	public static final String UPP_TRANSACTION_NOT_INIT_MESSAGE = "Transaction do not init!";
	public static final String UPP_HTTP_TRANSFERERROR_MESSAGE = "http transfer error!";
	public static final String UPP_HTTP_RESPONSE_ERROR_MESSAGE = "http response not 200!";
	protected int mErrorCode = UPP_DEFAULT_ERR;
	private String mMessage;

	private UnionPayPaymentException() {
		super();
	}

	public UnionPayPaymentException(int errorCode) {
		this();
		mErrorCode = errorCode;
	}

	public UnionPayPaymentException(String message) {
		this();
		mMessage = message;
	}

	public int getErrorCode() {
		return mErrorCode;
	}

//	public String getMessage() {
//		if(null != mMessage){
//			return mMessage;
//		}
//		Log.i("UnionPayPaymentException", "UnionPayPaymentException mErrorCode " + mErrorCode);
//		switch (mErrorCode) {
//		case UPP_RSP_ARGUMENT_ERROR:
//			return UPP_RSP_ARGUMENT_ERROR_MESSAGE;
//		case UPP_NO_SESSION_ID:
//			return UPP_NO_SESSION_ID_MESSAGE;
//		case UPP_NO_SESSION_KEY:
//			return UPP_NO_SESSION_KEY_MESSAGE;
//		case UPP_NO_PUBLIC_KEY:
//			return UPP_NO_PUBLIC_KEY_MESSAGE;
//		case UPP_PARSE_PAA_ERR:
//			return UPP_PARSE_PAA_ERR_MESSAGE;
//		case UPP_PARSE_LPAA_ERR:
//			return UPP_PARSE_LPAA_ERR_MESSAGE;
//		case UPP_PAA_ERR:
//			return UPP_PAA_ERR_MESSAGE;
//		case UPP_LPAA_ERR:
//			return UPP_LPAA_ERR_MESSAGE;
//		case UPP_TRANSACTION_NOT_INIT:
//			return UPP_TRANSACTION_NOT_INIT_MESSAGE;
//		case UPP_HTTP_TRANSFERERROR:
//			return UPP_HTTP_TRANSFERERROR_MESSAGE;
//		case UPP_HTTP_RESPONSE_ERROR:
//			return UPP_HTTP_RESPONSE_ERROR_MESSAGE;
//		default:
//			return UPP_DEFAULT_ERR_MESSAGE;
//		}
//	}


	public String getMessage() {
		if(null != mMessage){
			return mMessage;
		}
		Log.i("UnionPayPaymentException", "UnionPayPaymentException mErrorCode " + mErrorCode);
		switch (mErrorCode) {
		case UPP_RSP_ARGUMENT_ERROR:
			return "参数错误";
		case UPP_PARSE_PAA_ERR:
			return "账单数据不正确";
		case UPP_PARSE_LPAA_ERR:
			return "账单数据不正确";
		case UPP_PAA_ERR:
			return "账单数据不正确";
		case UPP_LPAA_ERR:
			return "账单数据不正确";
		case UPP_HTTP_TRANSFERERROR:
			return "向服务器请求数据失败";
		case UPP_HTTP_RESPONSE_ERROR:
			return "向服务器请求数据失败";
		case UPP_NO_SDCARD: // 无SD卡
			return "未插入SD卡";
		case UPP_NO_AUDIO: //未插入音频设备
			return "未插入音频设备";
		case UPP_NOT_VIPOSAUDIO: //不支持的音频设备
			return "不支持的音频设备";
		case UPP_VIPOSAUDIO_CMD_FAIL:
			return "刷卡器通信失败";
		case UPP_VIPOSAUDIO_GETCARDINFO_FAIL:
			return "银行卡信息读取失败，请重新刷卡";
		case UPP_NOT_SMARTCARD: // 不是智能卡
			return "插入的SD卡不支持银联手机支付";
		case UPP_SMARTCARD_SW_ERROR: // 智能卡过程字节错误
//			SmartCardStatusWordException t = (SmartCardStatusWordException)this;
//			return t.getMessage();
			return "智能卡过程字节错误";
		case UPP_SMARTCARD_NOT_PERSONNAL: // 智能卡未个人化
			return "插入的SD卡未个人化";
		case UPP_SMARTCARD_TIMEOUT: // 智能卡操作超时
			return "操作SD卡超时";
		case UPP_SMARTCARD_COMMUNICATIONERROR: // 智能卡通讯错误
			return "操作SD卡失败";
		case UPP_SMARTCARD_RESPONSE_DATA_ERROR: // 智能卡返回数据错误
			return "操作SD卡失败";
		case UPP_SMARTCARD_NOT_PBOC: // 不是PBOC卡
			return "SD卡没有PBOC应用";
		case UPP_SMARTCARD_NONEEDTAGVALUE: // 没有所对应的TAG值
			return "圈存失败";
			//用于verify
		case UPP_SMARTCARD_LOCKED: //智能卡已经锁定
			return "SD卡密码错误次数超限，已被锁定";
		case UPP_RESPONSECODE_ERROR://支付响应数据错误
//			UnipayResponseCodeException d = (UnipayResponseCodeException)this;
//			return d.getMessage();
			return "支付相应数据错误";
		case UPP_NOTRESPONSECODE_ERROR://支付响应报文不含responseCode元素
			return "抱歉，系统存在问题，请稍后重试";
		case UPP_LOADREVERSAL_SUCCESS://银联响应数据显示圈存冲正成功
			return "电子现金充值失败";
		case UPP_LOADREVERSAL_FAIL://银联响应数据显示圈存冲正失败
			return "电子现金充值失败，如您发现扣款，请联系发卡银行";
		case UPP_LOADREVERSAL_TIMEOUT://其他原因造成的圈存冲正失败
			return "电子现金充值失败，如您发现扣款，请联系发卡银行";
		case NET_CONNECT_HTTPHOST_ERROR:// 未连接网络
			return "未连接网络，请稍后重试！";
		case NET_CONNECT_TIMEOUT://连接网络超时
			return "暂时无法连接，请稍后重试！";
		case NET_CONNECT_SOCKETTIMEOUT://网络接收数据超时
			return "网络连接超时，请稍后重试！";
		case NET_CONNECT_URLPARSE://URL格式错误
			return "网络地址解析错误，请稍后重试！";
		default:
			return "操作失败";
		}
	}
}
