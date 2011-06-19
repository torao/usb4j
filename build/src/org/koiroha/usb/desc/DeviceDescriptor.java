/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: DeviceDescriptor.java,v 1.8 2009/05/21 12:02:55 torao Exp $
*/
package org.koiroha.usb.desc;

import java.nio.ByteBuffer;

import org.koiroha.usb.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// DeviceDescriptor: デバイス記述子
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * デバイス記述子を表すクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.8 $ $Date: 2009/05/21 12:02:55 $
 * @author torao
 * @since 2009/04/27 Java2 SE 5.0
 * @see Descriptor#TYPE_DEVICE
 */
public final class DeviceDescriptor extends Descriptor{

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	/** USB 仕様リリース番号です。 */
	private final short bcdUSB;

	/** デバイスクラスです。 */
	private final byte bDeviceClass;

	/** デバイスサブクラスです。 */
	private final byte bDeviceSubClass;

	/** デバイスプロトコルです。 */
	private final byte bDeviceProtocol;

	/** コントロール転送の最大パケットサイズです。 */
	private final byte bMaxPacketSize0;

	/** ベンダー ID です。 */
	private final short idVendor;

	/** 製品 ID です。 */
	private final short idProduct;

	/** デバイスリリース番号です。 */
	private final short bcdDevice;

	/** ベンダー情報の STRING DESCRIPTOR のインデックス番号です。指定なしの場合 0 が設定されます。 */
	private final byte iManufacture;

	/** 製品情報の STRING DESCRIPTOR のインデックス番号です。指定なしの場合 0 が設定されます。 */
	private final byte iProduct;

	/** シリアル番号の STRING DESCRIPTOR のインデックス番号です。指定なしの場合 0 が設定されます。 */
	private final byte iSerialNumber;

	/** このデバイスのコンフィギュレーション数です。 */
	private final byte bNumConfigurations;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * デバイスディスクリプタのバイナリを指定して構築を行います。
	 * <p>
	 * @param buffer デバイスディスクリプタ
	 */
	public DeviceDescriptor(ByteBuffer buffer){
		super(buffer);
		this.bcdUSB = buffer.getShort();
		this.bDeviceClass = buffer.get();
		this.bDeviceSubClass = buffer.get();
		this.bDeviceProtocol = buffer.get();
		this.bMaxPacketSize0 = buffer.get();
		this.idVendor = buffer.getShort();
		this.idProduct = buffer.getShort();
		this.bcdDevice = buffer.getShort();
		this.iManufacture = buffer.get();
		this.iProduct = buffer.get();
		this.iSerialNumber = buffer.get();
		this.bNumConfigurations = buffer.get();
		pack(buffer);
		return;
	}

	// ======================================================================
	// USB 仕様リリース番号の参照
	// ======================================================================
	/**
	 * USB 仕様リリース番号 ({@code bcdUSB}) を参照します。USB 1.1 の場合は
	 * {@code 0x0110}、USB 2.0 の場合は {@code 0x0200} などの値を返します。
	 * <p>
	 * @return USB 仕様リリース番号
	 */
	public int getUSBSpecification() {
		return bcdUSB & 0xFFFF;
	}

	// ======================================================================
	// デバイスクラスの参照
	// ======================================================================
	/**
	 * デバイスクラス ({@code bDeviceClass}) を参照します。返値は UINT8 の範囲となります。
	 * <p>
	 * @return デバイスクラス
	 * @see USB#getDeviceClass(int)
	 */
	public int getDeviceClass(){
		return bDeviceClass & 0xFF;
	}

	// ======================================================================
	// デバイスサブクラスの参照
	// ======================================================================
	/**
	 * デバイスサブクラス ({@code bDeviceSubClass}) を参照します。返値は UINT8 の範囲と
	 * なります。
	 * <p>
	 * @return デバイスサブクラス
	 */
	public int getDeviceSubClass(){
		return bDeviceSubClass & 0xFF;
	}

	// ======================================================================
	// デバイスサブプロトコルの参照
	// ======================================================================
	/**
	 * デバイスサブプロトコル ({@code bDeviceProtocol}) を参照します。返値は UINT8 の
	 * 範囲となります。
	 * <p>
	 * @return デバイスサブプロトコル
	 */
	public int getDeviceProtocol(){
		return bDeviceProtocol & 0xFF;
	}

	// ======================================================================
	// パケットサイズの参照
	// ======================================================================
	/**
	 * コントロール転送 (通常はパイプ 0) の最大パケットサイズ ({@code bMaxPacketSize0})
	 * を参照します。返値は UINT8 の範囲となります。
	 * <p>
	 * @return コントロール転送の最大パケットサイズ
	 */
	public int getMaxPacketSize(){
		return bMaxPacketSize0 & 0xFF;
	}

	// ======================================================================
	// ベンダー ID の参照
	// ======================================================================
	/**
	 * ベンダー ID ({@code idVendor}) を参照します。返値は UINT16 の範囲となります。
	 * <p>
	 * @return ベンダー ID
	 */
	public int getVendorId(){
		return idVendor & 0xFFFF;
	}

	// ======================================================================
	// 製品 ID の参照
	// ======================================================================
	/**
	 * 製品 ID ({@code idProduct}) を参照します。返値は UINT16 の範囲となります。
	 * <p>
	 * @return 製品 ID
	 */
	public int getProductId(){
		return idProduct & 0xFFFF;
	}

	// ======================================================================
	// デバイスリリース番号の参照
	// ======================================================================
	/**
	 * デバイスのリリース番号 ({@code bcdDevice}) を参照します。返値は UINT16 の範囲と
	 * なります。
	 * <p>
	 * @return デバイスリリース番号
	 */
	public int getDeviceRelease(){
		return bcdDevice & 0xFFFF;
	}

	// ======================================================================
	// コンフィギュレーション数の参照
	// ======================================================================
	/**
	 * コンフィギュレーション数 ({@code bNumConfigurations}) を参照します。返値は
	 * UINT8 の範囲となります。
	 * <p>
	 * @return コンフィギュレーション数
	 */
	public int getNumConfigurations(){
		return bNumConfigurations & 0xFF;
	}

	// ======================================================================
	// ベンダー情報インデックスの参照
	// ======================================================================
	/**
	 * ベンダー情報に関する文字列記述子のインデックス ({@code iManufacture}) を参照します。
	 * <p>
	 * @return ベンダー情報のインデックス
	 * @see Device#getString(int, int)
	 */
	public int getManufacturerSDIX(){
		return iManufacture & 0xFF;
	}

	// ======================================================================
	// 製品情報インデックスの参照
	// ======================================================================
	/**
	 * 製品情報に関する文字列記述子のインデックス ({@code iProduct}) を参照します。
	 * <p>
	 * @return 製品情報のインデックス
	 * @see Device#getString(int, int)
	 */
	public int getProductSDIX(){
		return iProduct & 0xFF;
	}

	// ======================================================================
	// シリアル番号インデックスの参照
	// ======================================================================
	/**
	 * シリアル番号に関する文字列記述子のインデックス ({@code iSerialNumber}) を参照します。
	 * <p>
	 * @return シリアル番号のインデックス
	 * @see Device#getString(int, int)
	 */
	public int getSerialNumberSDIX(){
		return iSerialNumber & 0xFF;
	}

	// ======================================================================
	// インスタンスの文字列化
	// ======================================================================
	/**
	 * このインスタンスを文字列化します。
	 * <p>
	 */
	@Override
	public String toString(){
		return super.toString() + "," +
		String.format(
			"bcdUSB=0x%04X," +
			"bDeviceClass=0x%02X,bDeviceSubClass=0x%02X,bDeviceProtocol=0x%02X," +
			"bMaxPacketSize0=%d," +
			"idVendor=0x%04X,idProduct=0x%04X," +
			"bcdDevice=0x%04X," +
			"iManufacturer=%d,iProduct=%d,iSerialNumber=%d," +
			"bNumConfiguration=%d",
			getUSBSpecification(),
			getDeviceClass(), getDeviceSubClass(), getDeviceSubClass(),
			getMaxPacketSize(),
			getVendorId(), getProductId(),
			getDeviceRelease(),
			getManufacturerSDIX(), getProductSDIX(), getSerialNumberSDIX(),
			getNumConfigurations())
			+ toHexExtra();
	}

}
