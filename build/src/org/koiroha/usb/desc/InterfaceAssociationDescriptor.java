/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: InterfaceAssociationDescriptor.java,v 1.1 2009/05/16 05:24:14 torao Exp $
*/
package org.koiroha.usb.desc;

import java.nio.ByteBuffer;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// InterfaceAssociationDescriptor: インターフェース割り当て記述子
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * インターフェース割り当て記述子を表すクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.1 $ $Date: 2009/05/16 05:24:14 $
 * @author torao
 * @since 2009/04/27 Java2 SE 5.0
 */
public final class InterfaceAssociationDescriptor extends Descriptor{

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
	 * インターフェース割り当て記述子の最小サイズを表す定数 {@value} です。
	 * <p>
	 */
	public static final int MIN_LENGTH = 0x8;

	/** 先頭インターフェース番号 */
	private final byte bFirstInterface;

	/** インターフェース数 */
	private final byte bInterfaceCount;

	/** 機能クラス */
	private final byte bFunctionClass;

	/** 機能サブクラス */
	private final byte bFunctionSubClass;

	/** 機能プロトコル */
	private final byte bFunctionProtocol;

	/** 機能の文字列記述子インデックス */
	private final byte iFunction;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * インターフェース記述子のバイナリを指定して構築を行います。
	 * <p>
	 * @param buffer インターフェース記述子
	 */
	public InterfaceAssociationDescriptor(ByteBuffer buffer){
		super(buffer);
		this.bFirstInterface = buffer.get();
		this.bInterfaceCount = buffer.get();
		this.bFunctionClass = buffer.get();
		this.bFunctionSubClass = buffer.get();
		this.bFunctionProtocol = buffer.get();
		this.iFunction = buffer.get();
		pack(buffer);
		return;
	}

	// ======================================================================
	// 先頭インターフェース番号の参照
	// ======================================================================
	/**
	 * 先頭インターフェース番号 ({@code bFirstInterface}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return 先頭インターフェース番号
	 */
	public int getFirstInterface(){
		return bFirstInterface & 0xFF;
	}

	// ======================================================================
	// インターフェース数の参照
	// ======================================================================
	/**
	 * インターフェース数 ({@code bInterfaceCount}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return インターフェース数
	 */
	public int getInterfaceCount(){
		return bInterfaceCount & 0xFF;
	}

	// ======================================================================
	// 機能クラスの参照
	// ======================================================================
	/**
	 * 機能クラス ({@code bFunctionClass}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return 機能クラス
	 */
	public int getFunctionClass(){
		return (bFunctionClass & 0xFF);
	}

	// ======================================================================
	// 機能サブクラスの参照
	// ======================================================================
	/**
	 * 機能サブクラス ({@code bFunctionSubClass}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return 機能サブクラス
	 */
	public int getFunctionSubClass(){
		return bFunctionSubClass & 0xFF;
	}

	// ======================================================================
	// 機能プロトコルの参照
	// ======================================================================
	/**
	 * 機能プロトコル ({@code bFunctionProtocol}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return 機能プロトコル
	 */
	public int getFunctionProtocol(){
		return bFunctionProtocol & 0xFF;
	}

	// ======================================================================
	// 機能情報インデックスの参照
	// ======================================================================
	/**
	 * この機能情報に対する文字列記述子のインデックス ({@code iFunction})
	 * を参照します。
	 * <p>
	 * @return 機能情報のインデックス
	 */
	public int getFunctionSDIX(){
		return iFunction & 0xFF;
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
			"bFirstInterface=%d,bInterfaceCount=%d," +
			"bFunctionClass=0x%02X,bFunctionSubClass=0x%02X,bFunctionProtocol=0x%02X," +
			"iFunction=%d",
			getFirstInterface(), getInterfaceCount(),
			getFunctionClass(), getFunctionSubClass(), getFunctionProtocol(),
			getFunctionSDIX())
			+ toHexExtra();
	}

}
