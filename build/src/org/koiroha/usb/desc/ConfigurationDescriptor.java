/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: ConfigurationDescriptor.java,v 1.7 2009/05/16 05:24:13 torao Exp $
*/
package org.koiroha.usb.desc;

import java.nio.ByteBuffer;

import org.koiroha.usb.Device;


// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// ConfigurationDescriptor: コンフィギュレーション記述子
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * コンフィギュレーション記述子を表すクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.7 $ $Date: 2009/05/16 05:24:13 $
 * @author torao
 * @since 2009/04/27 Java2 SE 5.0
 * @see Descriptor#TYPE_CONFIGURATION
 */
public final class ConfigurationDescriptor extends Descriptor{

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
	 * コンフィギュレーション記述子の最小サイズを表す定数 {@value} です。
	 * <p>
	 */
	public static final int MIN_LENGTH = 0x9;

	/** 構成全体の長さです。 */
	private final short wTotalLength;

	/** 構成のインターフェース数 */
	private final byte bNumInterfaces;

	/** この構成の番号 */
	private final byte bConfigurationValue;

	/** この構成を示す STRING DESCRIPTOR のインデックス番号です。 */
	private final byte iConfiguration;

	/** 構成の属性です。 */
	private final byte bmAttributes;

	/** 最大バス消費電力量 (2mA単位) です。 */
	private final byte MaxPower;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンフィギュレーション記述子のバイナリを指定して構築を行います。
	 * <p>
	 * @param buffer コンフィギュレーション記述子のバイナリ
	 */
	public ConfigurationDescriptor(ByteBuffer buffer){
		super(buffer);
		this.wTotalLength = buffer.getShort();
		this.bNumInterfaces = buffer.get();
		this.bConfigurationValue = buffer.get();
		this.iConfiguration = buffer.get();
		this.bmAttributes = buffer.get();
		this.MaxPower = buffer.get();
		pack(buffer);
		return;
	}

	// ======================================================================
	// コンフィギュレーションサイズの参照
	// ======================================================================
	/**
	 * このコンフィギュレーションのインターフェース、エンドポイント、その他ディスクリプタを含めた
	 * サイズ ({@code wTotalLength}) を参照します。
	 * 返値は UINT16 の範囲を取り
	 * ます。
	 * <p>
	 * @return 記述子の総サイズ
	 */
	public int getTotalLength(){
		return wTotalLength & 0xFFFF;
	}

	// ======================================================================
	// インターフェース数の参照
	// ======================================================================
	/**
	 * インターフェース数 ({@code bNumInterfaces}) を参照します。返値は UINT8 の範囲を
	 * 取ります。
	 * <p>
	 * @return インターフェース数
	 */
	public int getNumInterface(){
		return bNumInterfaces & 0xFF;
	}

	// ======================================================================
	// コンフィギュレーション値の参照
	// ======================================================================
	/**
	 * このコンフィギュレーションを示す番号 ({@code bConfigurationValue}) を参照します。
	 * 返値は 1 以上となります。UINT8 の範囲を取ります。
	 * <p>
	 * @return コンフィギュレーション番号
	 */
	public int getConfigurationValue(){
		return bConfigurationValue & 0xFF;
	}

	// ======================================================================
	// 属性の参照
	// ======================================================================
	/**
	 * 属性値 ({@code bmAttributes}) を参照します。
	 * <p>
	 * @return 属性値
	 */
	public int getAttributes(){
		return bmAttributes & 0xFF;
	}

	// ======================================================================
	// 自己電源の判定
	// ======================================================================
	/**
	 * 自己電源かどうかを判定します。
	 * <p>
	 * @return 自己電源の場合 true
	 */
	public boolean isSelfPower(){
		return ((bmAttributes >> 6) & 0x01) != 0;
	}

	// ======================================================================
	// リモートウェイクアップの判定
	// ======================================================================
	/**
	 * リモートウェイクアップかどうかを判定します。
	 * <p>
	 * @return リモートウェイクアップの場合 true
	 */
	public boolean isRemoteWakeup(){
		return ((bmAttributes >> 5) & 0x01) != 0;
	}

	// ======================================================================
	// 最大バス消費電力量の参照
	// ======================================================================
	/**
	 * 最大バス電流 ({@code bMaxPower}×2) をミリアンペア単位で返します。返値は 0 から
	 * 500 までの範囲となります。
	 * <p>
	 * @return 最大バス電流 [mA]
	 */
	public int getMaxPower(){
		return (MaxPower & 0xFF) * 2;
	}

	// ======================================================================
	// コンフィギュレーション情報インデックスの参照
	// ======================================================================
	/**
	 * このコンフィギュレーション情報に関する文字列記述子のインデックス ({@code iConfiguration})
	 * を参照します。
	 * <p>
	 * @return コンフィギュレーション情報のインデックス
	 * @see Device#getString(int, int)
	 */
	public int getConfigurationSDIX(){
		return iConfiguration & 0xFF;
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
			"wTotalLength=%d,bNumInterfaces=%d,bConfigurationValue=%d,iConfiguration=%d," +
			"bmAttributes=0x%02X(selfpower=%s,remotewakeup=%s)," +
			"bMaxPower=%dmA",
			getTotalLength(), getNumInterface(), getConfigurationValue(), getConfigurationSDIX(),
			getAttributes(), isSelfPower(), isRemoteWakeup(),
			getMaxPower())
		+ toHexExtra();
	}

}
