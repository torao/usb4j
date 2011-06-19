/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: EndpointDescriptor.java,v 1.8 2009/05/16 05:24:14 torao Exp $
*/
package org.koiroha.usb.desc;

import java.nio.ByteBuffer;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// EndpointDescriptor: エンドポイント記述子
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * エンドポイント記述子を表すクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.8 $ $Date: 2009/05/16 05:24:14 $
 * @author torao
 * @since 2009/04/27 Java2 SE 5.0
 */
public final class EndpointDescriptor extends Descriptor{

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
	public static final int MIN_LENGTH = 0x7;

	/** エンドポイントアドレス */
	private final byte bEndpointAddress;

	/** エンドポイントの属性 */
	private final byte bmAttributes;

	/** 最大パケットサイズ */
	private final short wMaxPacketSize;

	/** インターバル (msec) */
	private final byte bInterval;

	/** */
	private final byte bRefresh;

	/** */
	private final byte bSynchAddress;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * エンドポイント記述子のバイナリを指定して構築を行います。
	 * <p>
	 * @param buffer エンドポイント記述子
	 */
	public EndpointDescriptor(ByteBuffer buffer){
		super(buffer);
		this.bEndpointAddress = buffer.get();
		this.bmAttributes = buffer.get();
		this.wMaxPacketSize = buffer.getShort();
		this.bInterval = buffer.get();
		if(getLength() > 0x07){
			this.bRefresh = buffer.get();
		} else {
			this.bRefresh = 0;
		}
		if(getLength() > 0x08){
			this.bSynchAddress = buffer.get();
		} else {
			this.bSynchAddress = 0;
		}
		pack(buffer);
		return;
	}

	// ======================================================================
	// エンドポイントアドレスの参照
	// ======================================================================
	/**
	 * エンドポイントのアドレス ({@code bEndpointAddress}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return エンドポイントアドレス
	 */
	public int getEndpointAddress(){
		return bEndpointAddress & 0xFF;
	}

	// ======================================================================
	// 転送方向の参照
	// ======================================================================
	/**
	 * エンドポイントの転送方向 ({@code bEndpointAddress}[b7]) を参照します。エンドポイ
	 * ントがコントロール転送の場合は半二重を表す {@link Direction#DUPLEX} を返します。
	 * <p>
	 * @return 転送方向
	 */
	public Direction getDirection(){
		if(getTransferType().equals(TransferType.CONTROL)){
			return Direction.DUPLEX;
		}
		return Direction.valueOf((bEndpointAddress >> 7) & 0x1);
	}

	// ======================================================================
	// エンドポイント番号の参照
	// ======================================================================
	/**
	 * このエンドポイントのインターフェース内のインデックス ({@code bEndpointAddress}[b4-b0])
	 * を参照します。
	 * <p>
	 * @return エンドポイント番号
	 */
	public int getEndpointNumber(){
		return (bEndpointAddress >> 0) & 0xF;
	}

	// ======================================================================
	// 属性ビットの参照
	// ======================================================================
	/**
	 * エンドポイントの属性ビット ({@code bEndpointAddress}) を参照します。
	 * 返値は UINT8 の範囲を取ります。
	 * <p>
	 * @return 属性ビット
	 */
	public int getAttribute(){
		return bmAttributes & 0xFF;
	}

	// ======================================================================
	// 転送タイプの参照
	// ======================================================================
	/**
	 * エンドポイントの転送タイプ ({@code bmAttributes}[b1-b0]) を参照します。
	 * <p>
	 * @return 転送タイプ
	 */
	public TransferType getTransferType(){
		return TransferType.valueOf(bmAttributes & 0x03);
	}

	// ======================================================================
	// 等時間隔同期タイプの参照
	// ======================================================================
	/**
	 * 等時間隔 (Isochronous) 転送における同期タイプを ({@code bmAttributes}[b3-b2])
	 * を参照します。返値は 0:同期なし、1:非同期、2:アダプティブ、3:同期、を表します。
	 * <p>
	 * @return 等時間隔転送の同期タイプ
	 */
	public int getIsochSyncType(){
		return ((bmAttributes >> 2) & 0x03);
	}

	// ======================================================================
	// 等時間隔使用タイプの参照
	// ======================================================================
	/**
	 * 等時間隔 (Isochronous) 転送における使用タイプを ({@code bmAttributes}[b5-b4])
	 * を参照します。返値は 0:データエンドポイント、1:フィードバックエンドポイント、2:従属的な
	 * エンドポイント、3:予約、を表します。
	 * <p>
	 * @return 等時間隔転送の使用タイプ
	 */
	public int getIsochUsageType(){
		return ((bmAttributes >> 4) & 0x03);
	}

	// ======================================================================
	// 最大パケットサイズの参照
	// ======================================================================
	/**
	 * このエンドポイントの最大パケットサイズフィールド ({@code wMaxPacketSize}) を参照します。
	 * <p>
	 * @return 最大パケットサイズフィールド
	 */
	public int getRawMaxPacketSize(){
		return (wMaxPacketSize & 0xFFFF);
	}

	// ======================================================================
	// 最大パケットサイズの参照
	// ======================================================================
	/**
	 * このエンドポイントの最大パケットサイズ ({@code wMaxPacketSize}[b10-b0]) を参照
	 * します。
	 * <p>
	 * @return 最大パケットサイズ (バイト)
	 */
	public int getMaxPacketSize(){
		return (wMaxPacketSize & 0x7FF);
	}

	// ======================================================================
	// 追加トランザクション数の参照
	// ======================================================================
	/**
	 * μフレームあたりの追加的なトランザクション数 ({@code wMaxPacketSize}[b12-b11])
	 * を参照します。この値は Hight Speed (480Mbps) の等時間隔転送と割り込み転送でのみ有効
	 * です。返値は 0:追加なし(1トランザクション/μフレーム)、1:1つ追加(2トランザクション/μフレーム)、
	 * 2:2つ追加(3トランザクション/μフレーム)、3:予約、を表します。
	 * <p>
	 * @return 追加のトランザクション数
	 */
	public int getAdditionalTransaction(){
		return (wMaxPacketSize >> 11) & 0x3;
	}

	// ======================================================================
	// ポーリング間隔の参照
	// ======================================================================
	/**
	 * このエンドポイントのポーリング間隔 ({@code bInterval}) を参照します。返値の意味は
	 * このエンドポイントの転送速度と転送タイプによって異なります。UInt8 の範囲をとります。
	 * <p>
	 * <table border="1">
	 * <tr><td>Low Speed</td><td>割り込み</td><td>ミリ秒単位 (フレーム数)</td><tr>
	 * <tr><td>Full Speed</td><td>割り込み</td><td>ミリ秒単位 (フレーム数)</td><tr>
	 * <tr><td>Full Speed</td><td>等時間隔</td><td>ミリ秒単位 (フレーム数) (2<sup><i>N</i>-1</sup>)</td><tr>
	 * <tr><td>Hight Speed</td><td>割り込み</td><td>μフレーム単位 (2<sup><i>N</i>-1</sup>)</td><tr>
	 * <tr><td>Hight Speed</td><td>等時間隔</td><td>μフレーム単位 (2<sup><i>N</i>-1</sup>)</td><tr>
	 * <tr><td>Hight Speed</td><td>バルク</td><td>最大NAKレート (μフレーム単位)</td><tr>
	 * <tr><td>Hight Speed</td><td>コントロール</td><td>最大NAKレート (μフレーム単位)</td><tr>
	 * </table>
	 * <p>
	 * @return ポーリング間隔
	 */
	public int getInterval(){
		return bInterval & 0xFF;
	}

	// ======================================================================
	// リフレッシュの参照
	// ======================================================================
	/**
	 * {@code bRefresh} を参照します。
	 * <p>
	 * @return {@code bRefresh}
	 */
	public int getRefresh() {
		return bRefresh & 0xFF;
	}

	// ======================================================================
	// リフレッシュの参照
	// ======================================================================
	/**
	 * {@code bSynchAddress} を参照します。
	 * <p>
	 * @return {@code bSynchAddress}
	 */
	public int getSynchAddress() {
		return bSynchAddress & 0xFF;
	}

	// ======================================================================
	// インスタンスの文字列化
	// ======================================================================
	/**
	 * このインスタンスを文字列化します。
	 * <p>
	 * @return このインスタンスの文字列
	*/
	@Override
	public String toString() {
		return super.toString() + "," +
			String.format(
				"bEndpointAddress=0x%02X(direction=%s,getEndpointNumber=%d)," +
				"bmAttributes=0x%02X(transfertype=%s,isochsynctype=%d,isochusagetype=%d)," +
				"wMaxPacketSize=%d(additionaltransaction=%d)," +
				"bInterval=%d,bRefresh=%d,bSynchAddress=%d",
				getEndpointAddress(), getDirection(), getEndpointNumber(),
				getAttribute(), getTransferType(), getIsochSyncType(), getIsochUsageType(),
				getMaxPacketSize(), getAdditionalTransaction(),
				getInterval(), getRefresh(), getSynchAddress())
				+ toHexExtra();
	}

}
