/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: FunctionalDescriptor.java,v 1.1 2009/05/16 05:24:14 torao Exp $
*/
package org.koiroha.usb.desc;

import java.nio.ByteBuffer;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// FunctionalDescriptor: 機能記述子
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 機能記述子を表すクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.1 $ $Date: 2009/05/16 05:24:14 $
 * @author torao
 * @since 2009/04/27 Java2 SE 5.0
 */
public final class FunctionalDescriptor extends Descriptor{

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// 最小サイズ
	// ======================================================================
	/**
	 * 機能記述子の最小サイズを表す定数 {@value} です。
	 * <p>
	 */
	public static final int MIN_LENGTH = 0x03;

	/** 記述子サブタイプ */
	private final byte bDescriptorSubType;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * インターフェース記述子のバイナリを指定して構築を行います。
	 * <p>
	 * @param buffer インターフェース記述子
	 */
	public FunctionalDescriptor(ByteBuffer buffer){
		super(buffer);
		this.bDescriptorSubType = buffer.get();
		pack(buffer);
		return;
	}

	// ======================================================================
	// 記述子サブタイプの参照
	// ======================================================================
	/**
	 * 記述子サブタイプ ({@code bDescriptorSubType}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return 記述子サブタイプ
	 */
	public int getDescriptorSubType(){
		return bDescriptorSubType & 0xFF;
	}

	// ======================================================================
	// インスタンスの文字列化
	// ======================================================================
	/**
	 * このインスタンスを文字列化します。
	 * <p>
	 * @return インスタンスの文字列
	 */
	@Override
	public String toString(){
		return super.toString() + "," +
		String.format(
			"bDescriptorSubType=0x%02X",
			getDescriptorSubType())
		+ toHexExtra();
	}

}
