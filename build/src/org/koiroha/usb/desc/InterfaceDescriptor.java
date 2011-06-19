/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: InterfaceDescriptor.java,v 1.7 2009/05/16 05:24:14 torao Exp $
*/
package org.koiroha.usb.desc;

import java.nio.ByteBuffer;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// InterfaceDescriptor: インターフェース記述子
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * インターフェース記述子を表すクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.7 $ $Date: 2009/05/16 05:24:14 $
 * @author torao
 * @since 2009/04/27 Java2 SE 5.0
 */
public final class InterfaceDescriptor extends Descriptor{

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
	 * インターフェース記述子の最小サイズを表す定数 {@value} です。
	 * <p>
	 */
	public static final int MIN_LENGTH = 0x9;

	/** インターフェース番号 */
	private final byte bInterfaceNumber;

	/** 代替設定番号 */
	private final byte bAlternateSetting;

	/** エンドポイント数 */
	private final byte bNumEndpoints;

	/** インターフェースクラス */
	private final byte bInterfaceClass;

	/** インターフェースサブクラス */
	private final byte bInterfaceSubClass;

	/** インターフェースプロトコル */
	private final byte bInterfaceProtocol;

	/** インターフェースの STRING DESCRIPTOR 番号 */
	private final byte iInterface;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * インターフェース記述子のバイナリを指定して構築を行います。
	 * <p>
	 * @param buffer インターフェース記述子
	 */
	public InterfaceDescriptor(ByteBuffer buffer){
		super(buffer);
		this.bInterfaceNumber = buffer.get();
		this.bAlternateSetting = buffer.get();
		this.bNumEndpoints = buffer.get();
		this.bInterfaceClass = buffer.get();
		this.bInterfaceSubClass = buffer.get();
		this.bInterfaceProtocol = buffer.get();
		this.iInterface = buffer.get();
		pack(buffer);
		return;
	}

	// ======================================================================
	// インターフェース番号の参照
	// ======================================================================
	/**
	 * コンフィギュレーション内でこのインターフェースを表すインデックス番号
	 * ({@code bInterfaceNumber}) を参照します。返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return インターフェース番号
	 */
	public int getInterfaceNumber(){
		return bInterfaceNumber & 0xFF;
	}

	// ======================================================================
	// 代替設定番号の参照
	// ======================================================================
	/**
	 * この記述子が示す代替設定の番号 ({@code bAlternateSetting}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return 代替設定の番号
	 */
	public int getAlternateSetting(){
		return bAlternateSetting & 0xFF;
	}

	// ======================================================================
	// エンドポイント数の参照
	// ======================================================================
	/**
	 * このインターフェースのエンドポイント数 ({@code bNumEndpoints}) を参照します。返値に
	 * はエンドポイント 0 は含まれません。
	 * <p>
	 * @return エンドポイントの数
	 */
	public int getNumEndpoint(){
		return (bNumEndpoints & 0xFF);
	}

	// ======================================================================
	// インターフェースクラスの参照
	// ======================================================================
	/**
	 * インターフェースクラス ({@code bInterfaceClass}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return インターフェースクラス
	 */
	public int getInterfaceClass(){
		return bInterfaceClass & 0xFF;
	}

	// ======================================================================
	// インターフェースサブクラスの参照
	// ======================================================================
	/**
	 * インターフェースサブクラス ({@code bInterfaceSubClass}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return インターフェースサブクラス
	 */
	public int getInterfaceSubClass(){
		return bInterfaceSubClass & 0xFF;
	}

	// ======================================================================
	// インターフェースプロトコルの参照
	// ======================================================================
	/**
	 * インターフェースプロトコル ({@code bInterfaceProtocol}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return インターフェースプロトコル
	 */
	public int getInterfaceProtocol(){
		return bInterfaceProtocol & 0xFF;
	}

	// ======================================================================
	// インターフェース情報インデックスの参照
	// ======================================================================
	/**
	 * このインターフェースの情報に対する文字列記述子のインデックス ({@code iInterface})
	 * を参照します。
	 * <p>
	 * @return インターフェース情報のインデックス
	 */
	public int getInterfaceSDIX(){
		return iInterface & 0xFF;
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
			"bInterfaceNumber=%d,bAlternateSetting=%d,bNumEndpoints=%d," +
			"bInterfaceClass=0x%02X,bInterfaceSubClass=0x%02X,bInterfaceProtocol=0x%02X," +
			"iInterface=%d",
			getInterfaceNumber(), getAlternateSetting(), getNumEndpoint(),
			getInterfaceClass(), getInterfaceSubClass(), getInterfaceProtocol(),
			getInterfaceSDIX())
			+ toHexExtra();
	}

}
