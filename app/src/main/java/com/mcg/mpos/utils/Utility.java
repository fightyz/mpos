package com.mcg.mpos.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.view.View;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mcg.mpos.application.BaseApplication;

/**
 * User: Zenos Date: 12-10-18
 */
public class Utility {
	public final static int UTILITY_NORMAL = 0;
	public final static int UTILITY_GOBACK = 1;

	private static final long PREVENT_TIEM = 300;
	private static long mLastClickTime = 0L;
	private static final int ACCOUNT_FORMATE_LENGTH = 12;
	
	//阻止双击事件
	public static boolean preventDoubleClick(){
		long time = System.currentTimeMillis();
		long timeD = time - mLastClickTime;
		if (0 < timeD && timeD < PREVENT_TIEM) {
			return true;
		}
		mLastClickTime = time;
		return false;
	}
	
	
	/**
	 * 对银行卡进行加*号保护设置
	 * 
	 * @param cardNo
	 * @return result
	 */
	public static String protectBankCardNo(String cardNo) {
		String star = "*";
		int cardNoLength = cardNo.length();
		String start = cardNo.substring(0, 4);
		String end = cardNo.substring(cardNoLength - 4, cardNoLength);
		String stars = "";
		for (int i = 0; i < 8; i++) {
			stars += star;
		}
		String result = start + stars + end;
		return result;
	}

	/**
	 * 对银行卡好进行加空格格式化
	 * 
	 * @param cardNo
	 * @return
	 */
	public static String formatCard(String cardNo) {
		return cardNo.substring(0, 4) + "  " + cardNo.substring(4, 8) + "  "
				+ cardNo.substring(8, 12) + "  " + cardNo.substring(12, 16)
				+ "  " + cardNo.substring(16, cardNo.length());
	}

	/**
	 * 对日期格式化方法
	 * 
	 * @param dateStr
	 * @return
	 */
	public static String dateFormat(String dateStr) {
		String date = "";
		if (dateStr.length() < 6) {
			date = dateStr;
		} else if (dateStr.length() == 6) {
			date = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6);
		} else if (dateStr.length() == 8) {
			date = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6)
					+ "-" + dateStr.substring(6, 8);
		} else if (dateStr.length() == 14) {
			date = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6)
					+ "-" + dateStr.substring(6, 8) + " "
					+ dateStr.substring(8, 10) + ":"
					+ dateStr.substring(10, 12) + ":"
					+ dateStr.substring(12, 14);
		} else if (dateStr.length() == 12) {
			date = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6)
			+ "-" + dateStr.substring(6, 8) + " "
			+ dateStr.substring(8, 10) + ":"
			+ dateStr.substring(10, 12);
		} else {
			date = dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6)
					+ "-" + dateStr.substring(6, 8) + " "
					+ dateStr.substring(8, 10) + ":"
					+ dateStr.substring(10, 12) + ":"
					+ dateStr.substring(12, 14) + " "
					+ dateStr.substring(14, dateStr.length());
		}
		return date;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 * 
	 * @param context
	 * @param dpValue
	 * @return
	 */
	public static int dp2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 * 
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dp(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 格式化金额(元)
	 * 
	 * @param s
	 * @param len
	 * @return
	 */
	public static String formatMoney(String s, int len) {
		if (s == null || s.length() < 1) {
			return "0.00元";
		}
		NumberFormat formater = null;
		double num = Double.parseDouble(s);
		if (len == 0) {
			formater = new DecimalFormat("###,###");

		} else {
			StringBuffer buff = new StringBuffer();
			buff.append("###,###.");
			for (int i = 0; i < len; i++) {
				buff.append("#");
			}
			formater = new DecimalFormat(buff.toString());
		}
		String result = formater.format(num);
		if (result.indexOf(".") == -1) {
			result = result + ".00" + "元";
		} else {
			result = result + "元";
		}
		return result;
	}

	/**
	 * 格式化金额(分)
	 * 
	 * @param s
	 * @return
	 */
	public static String formatMoney(String s) {
		if (s == null || s.length() < 1) {
			return "0.00元";
		}

		if (s.length() == 1) {
			return "0.0" + s+"元";
		}
		if (s.length() == 2) {
			return "0." + s+"元";
		}

		String part = s.substring(s.length() - 2, s.length());
		String part2 = s.substring(0, s.length() - 2);

		double num = Double.parseDouble(part2);
		NumberFormat formater = null;
		formater = new DecimalFormat("###,###");
		String result = formater.format(num);
		result = result + "." + part+"元";
		return result;
	}

	public static String formatMoney2(String s) {
		if (s == null || s.length() < 1) {
			return "0.00";
		}

		if (s.length() == 1) {
			return "0.0" + s;
		}
		if (s.length() == 2) {
			return "0." + s;
		}

		String part = s.substring(s.length() - 2, s.length());
		String part2 = s.substring(0, s.length() - 2);

		double num = Double.parseDouble(part2);
		NumberFormat formater = null;
		formater = new DecimalFormat("###,###");
		String result = formater.format(num);
		result = result + "." + part;
		return result;
	}

	/**
	 * 格式化时间字符串
	 * 
	 * @param dateString
	 * @return
	 */
	public static String formatStringDate(String dateString) {
		if (dateString == null || dateString.length() < 1) {
			return "";
		}
		String year = dateString.substring(0, 4); // 年份
		String mouth = dateString.substring(4, 6); // 月份
		String day = dateString.substring(6, 8); // 日期
		String hour = dateString.substring(8, 10); // 时
		String minute = dateString.substring(10, 12); // 分

		return year + "-" + mouth + "-" + day + " " + hour + ":" + minute;
	}

	/**
	 * 格式化时间字符串
	 * 
	 * @param dateString
	 * @return
	 */
	public static String formatStringDate2(String dateString) {
		if (dateString == null || dateString.length() < 1) {
			return "";
		}
		String year = dateString.substring(0, 4); // 年份
		String mouth = dateString.substring(4, 6); // 月份
		String day = dateString.substring(6, 8); // 日期

		return year + "年" + mouth + "月" + day + "日";
	}

	/**
	 * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param dateDate
	 * @return
	 */
	public static String formatDateToString(java.util.Date dateDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	/**
	 * 获取现在时间
	 * 
	 * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
	 */
	public static String getStringDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	public static boolean emailFormat(String email) {
		boolean tag = true;
		final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		final Pattern pattern = Pattern.compile(pattern1);
		final Matcher mat = pattern.matcher(email);
		if (!mat.find()) {
			tag = false;
		}
		return tag;
	}

	/**
	 * 设置视图背景图片
	 * 
	 * @param context
	 * @param view
	 * @param resource
	 */
	public static void setViewBackground(Context context, View view,
			int resource) {
		// 设置平铺分割栏背景样式
		try {
			Bitmap bitmap = BitmapFactory.decodeResource(
					context.getResources(), resource);
			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			drawable.setTileModeXY(Shader.TileMode.REPEAT,
					Shader.TileMode.REPEAT);
			drawable.setDither(true);
			view.setBackgroundDrawable(drawable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***
	 * 半角转换为全角
	 * 
	 * @param input
	 * @return
	 */
	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	public static boolean isHaveExternalStorage(){
		if(Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)){
			return true;
		}else{
			return false;
		}
	}
	
	// 编辑图片大小，保持图片不变形。
	public static Bitmap resetImage(Bitmap sourceBitmap, int resetWidth,
			int resetHeight) {
		int width = sourceBitmap.getWidth();
		int height = sourceBitmap.getHeight();
		int tmpWidth;
		int tmpHeight;
		float scaleWidth = (float) resetWidth / (float) width;
		float scaleHeight = (float) resetHeight / (float) height;
		float maxTmpScale = scaleWidth >= scaleHeight ? scaleWidth : scaleHeight;
		// 保持不变形
		tmpWidth = (int) (maxTmpScale * width);
		tmpHeight = (int) (maxTmpScale * height);
		Matrix m = new Matrix();
		m.setScale(maxTmpScale, maxTmpScale, tmpWidth, tmpHeight);
		sourceBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, width, height, m, false);
		// 切图
		int x = (tmpWidth - resetWidth) / 2;
		int y = (tmpHeight - resetHeight) / 2;
		return Bitmap.createBitmap(sourceBitmap, x, y, resetWidth, resetHeight);
	}

	/**
	 * 获取习惯意义金额单位为分，比如formateAccountBalance为"000000012345"，转化后为12345
	 * 
	 * @param formateAccountBalance
	 *            按银联标准格式化金额，长度必须为12
	 * @return 转化后的金额值（单位为分），若转化失败返回null
	 */
	public static String getAccountBalance(String formateAccountBalance) {
		if (null == formateAccountBalance
				|| formateAccountBalance.length() != ACCOUNT_FORMATE_LENGTH) {
			return null;
		}
		try {
			int a = Integer.parseInt(formateAccountBalance);
			return "" + a;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 按银联格式要求格式化后金额
	 * 
	 * @param accountBalance
	 *            习惯意义上的金额单位为分
	 * @return 格式化后金额
	 */
	public static String formateAccountBalance(String accountBalance) {
		String s = accountBalance;
		int cout = ACCOUNT_FORMATE_LENGTH - s.length();
		while (cout-- > 0) {
			s = "0" + s;
		}
		return s;
	}
	/**
     * 获取版本号
     * @return 当前应用的版本号
     */
    public static String getVersion() {
        try {
            PackageManager manager = BaseApplication.getInstance().getPackageManager();
            PackageInfo info = manager.getPackageInfo(BaseApplication.getInstance().getPackageName(), 0);
            String version = info.versionName;
            return  version;
        } catch (Exception e) {
            e.printStackTrace();
            return "0.0.0";
        }
    }
    /**
     * 获取MEI
     * @return 手机MEI
     */
  public static String getIMEI(){ 
    BaseApplication.getInstance();
	TelephonyManager tm = (TelephonyManager)BaseApplication.getInstance().getSystemService(BaseApplication.TELEPHONY_SERVICE);     
    return tm.getDeviceId();
    
    }
  /**
   * 获取手机型号
   * @return 手机型号
   */
public static String getMODEL(){ 
	
  return android.os.Build.MODEL ;
  }
/**
 * 获取SDK版本号
 * @return SDK版本号
 */
public static String getSDKVersion(){ 
return android.os.Build.VERSION.SDK;
                              }
/**
 * 判别手机是否为正确手机号码
*/
public static boolean isMobileNum(String mobiles) {
	Pattern p = Pattern
			.compile("^(1)\\d{10}$");
	Matcher m = p.matcher(mobiles);
	return m.matches();
}

}
