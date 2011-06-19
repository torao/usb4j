/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: USBService.java,v 1.6 2009/05/18 15:38:05 torao Exp $
*/
package org.koiroha.usb;


// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// USBService: USB サービスインターフェース
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB サービスを提供するためのインターフェースです。
 * <p>
 * ネイティブライブラリとリンクしている場合はスタティックイニシャライザあるいはコンストラクタで
 * ロードを行う必要があります。
 * <p>
 * @version usb4j 1.0 $Revision: 1.6 $ $Date: 2009/05/18 15:38:05 $
 * @author torao
 * @since 2009/04/24 Java2 SE 5.0
 */
public interface USBService {

	// ======================================================================
	// ライブラリ名の参照
	// ======================================================================
	/**
	 * この USB サービスのライブラリ名を参照します。
	 * <p>
	 * @return ライブラリ名
	 * @throws USBException ライブラリ名の取得に失敗した場合
	*/
	public String getLibraryName() throws USBException;

	// ======================================================================
	// 新規コンテキストの作成
	// ======================================================================
	/**
	 * 新規の USB コンテキストを作成します。サブクラスはデバイスインスタンスなどのリソースを持た
	 * ない状態のコンテキストを返します。初期化のための {@link USBContext#sync()} は
	 * サービスマネージャによって呼び出されます。
	 * <p>
	 * @return 空の USB コンテキスト
	 * @throws USBException USB コンテキストの作成に失敗した場合
	*/
	public USBContext createSession() throws USBException;

}
