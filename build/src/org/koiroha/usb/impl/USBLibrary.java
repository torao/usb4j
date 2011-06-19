/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: USBLibrary.java,v 1.4 2009/05/14 17:03:56 torao Exp $
*/
package org.koiroha.usb.impl;

import java.util.logging.Logger;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// USBLibrary: USB ライブラリ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 汎用 USB ライブラリへの Wrapper インターフェースを作成するためのクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.4 $ $Date: 2009/05/14 17:03:56 $
 * @author takami torao
 * @since 2009/05/08 Java2 SE 5.0
 */
public abstract class USBLibrary {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final Logger logger = Logger.getLogger(USBLibrary.class.getName());

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定されたライブラリをロードします。
	 * <p>
	 * @param library ロードするライブラリ名
	 * @param name ライブラリのログ出力名
	 * @throws UnsatisfiedLinkError ネイティブライブラリのオープンに失敗した場合
	 */
	protected USBLibrary(String library, String name) throws UnsatisfiedLinkError{

		// ライブラリのロード
		logger.config("loading " + System.mapLibraryName(library) + " for " + name + " API...");
		System.loadLibrary(library);

		// ライブラリバージョンの確認
		long version = nativeInterfaceVersion();
		int jniVersion = (int)((version >> 32) & 0xFFFFFFFF);
		logger.config("java native interface version: " + (jniVersion>>16) + "." + (jniVersion&0xFFFF));
		int libVersion = bcdToInt((int)(version & 0xFFFF));
		int major = libVersion / 100;
		int minor1 = (libVersion % 100) / 10;
		int minor2 = (libVersion % 100) % 10;
		logger.config(System.mapLibraryName(library) + " interface version: " + major + "." + minor1 + "." + minor2);
		if(major < 1 || (major == 1 && minor1 < 0)){
			throw new UnsatisfiedLinkError("unknown library version: " + major + "." + minor1);
		}
		return;
	}

	// ======================================================================
	// ネイティブライブラリバージョン
	// ======================================================================
	/**
	 * ネイティブライブラリのインターフェースバージョンを参照します。返値は上位 32bit に JNI
	 * のバージョン、下位 32bit 中上位 8 ビットにメジャーバージョン、下位 8 ビットにマイナー
	 * バージョンとなる 64bit 整数です。たとえば JNI 1.6 ライブラリバージョン 1.0 は
	 * 0x0001000600000100 となります。
	 * <p>
	 * @return インターフェースバージョン
	 */
	protected abstract long nativeInterfaceVersion();

	// ======================================================================
	// BCD 値の int 変換
	// ======================================================================
	/**
	 * 指定された BCD 値を int に変換します。
	 * <p>
	 * @param bcd BCD 値
	 * @return int 値
	 */
	private static int bcdToInt(int bcd){
		int value = 0;
		for(int i=0; i<4*2; i++){
			int keta = 1;
			for(int j=0; j<i; j++){
				keta *= 10;
			}
			value += (((bcd >> (4*i)) & 0xF) * keta);
		}
		return value;
	}

}
