package com.realsil.WifiConfig.utility;

import java.io.UnsupportedEncodingException;

public class StringByteTrans {

	/**
	 * Convert hexadecimal string to byte array
	 * @param src Byte string, there is no separator between each Byte
	 * @return byte[] The corresponding byte array
	 */
	public static byte[] hexStringToByteArray(String src) {
		int len = src.length();
		byte[] data = new byte[len/2];
		src = src.toUpperCase();
		char[] hexs = src.toCharArray();
		// judge the input string good or not
		if (len % 2 == 1) {
			return null;
		}
		// judge the input string good or not
		for(int i = 0; i < len; i++) {
			if((hexs[i] >= '0' && hexs[i] <= '9') || (hexs[i] >= 'A' && hexs[i] <= 'F')) {
			} else {
				return null;
			}
		}
		for(int i = 0; i < len; i+=2){
			data[i/2] = (byte) ((Character.digit(src.charAt(i), 16) << 4) + Character.digit(src.charAt(i+1), 16));
		}

		return data;
	}

	/**
	 * ASCII string convert to Byte array
	 * @param str ascii string
	 * @return byte[]
	 */
	public static byte[] Str2Bytes(String str)
	{
		if (str == null) {
			throw new IllegalArgumentException(
					"Argument str ( String ) is null! ");
		}
		byte[] b = new byte[str.length() / 2];

		try {
			b = str.getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}

	/**
	 * bytes array convert to a normal string corresponding character (ASCII)
	 *
	 * @param bytearray
	 *            byte[]
	 * @return String
	 */
	public static String Byte2String(byte[] bytearray) {
		StringBuilder result = new StringBuilder("");

		int length = bytearray.length;
		for (int i = 0; i < length; i++) {
			String temp = Integer.toHexString(bytearray[i] & 255);
			result.append(temp.length() == 1?"0" + temp:temp);
		}
		return result.toString();
	}

	/**
	 * String into a unicode String
	 * @param  strText The Angle of the string
	 * @return String No separator between each unicode
	 * @throws Exception
	 */
	public static String strToUnicode(String strText)
			throws Exception
	{
		char c;
		StringBuilder str = new StringBuilder();
		int intAsc;
		String strHex;
		for (int i = 0; i < strText.length(); i++)
		{
			c = strText.charAt(i);
			intAsc = (int) c;
			strHex = Integer.toHexString(intAsc);
			if (intAsc > 128)
				str.append("\\u" + strHex);
			else // 00 low in the front
				str.append("\\u00" + strHex);
		}
		return str.toString();
	}

	/**
	 * Unicode String into a String
	 * @param hex Hexadecimal values string (a unicode 2 byte)
	 * @return String The Angle of the string
	 */
	public static String unicodeToString(String hex)
	{
		int t = hex.length() / 6;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < t; i++)
		{
			String s = hex.substring(i * 6, (i + 1) * 6);
			// Need to catch up on high 00 turn again
			String s1 = s.substring(2, 4) + "00";
			// Low directly
			String s2 = s.substring(4);
			// The hexadecimal string to an int
			int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
			// To convert an int to characters
			char[] chars = Character.toChars(n);
			str.append(new String(chars));
		}
		return str.toString();
	}
}
