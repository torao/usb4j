/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: USBServiceImpl.java,v 1.8 2009/05/18 15:38:06 torao Exp $
*/
package org.koiroha.usb.impl;

import org.koiroha.usb.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// USBServiceImpl: USB サービス実装
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB サービスの実装クラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.8 $ $Date: 2009/05/18 15:38:06 $
 * @author torao
 * @since 2009/04/24 Java2 SE 5.0
 */
public abstract class USBServiceImpl implements USBService{

	// ======================================================================
	// ブリッジ
	// ======================================================================
	/**
	 * ブリッジです。
	 * <p>
	 */
	private final USBBridge bridge;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param bridge USB ブリッジ
	 */
	protected USBServiceImpl(USBBridge bridge) {
		this.bridge = bridge;
		return;
	}

	// ======================================================================
	// ライブラリ名の参照
	// ======================================================================
	/**
	 * ライブラリ名を参照します。
	 * <p>
	 * @return ライブラリ名
	 * @throws USBException ライブラリ名の取得に失敗した場合
	*/
	public String getLibraryName() throws USBException {
		return bridge.getLibraryName();
	}

	// ======================================================================
	// コンテキストの構築
	// ======================================================================
	/**
	 * コンテキストを構築します。
	 * <p>
	 * @return コンテキスト
	 * @throws USBException コンテキストの構築に失敗した場合
	*/
	public USBContext createSession() throws USBException {
		return bridge.create();
	}

}
