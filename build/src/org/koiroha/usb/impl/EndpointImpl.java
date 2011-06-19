/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: EndpointImpl.java,v 1.11 2009/05/21 12:02:53 torao Exp $
*/
package org.koiroha.usb.impl;

import java.nio.ByteBuffer;

import org.koiroha.usb.*;
import org.koiroha.usb.ControlRequest.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.event.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// EndpointImpl: エンドポイント実装
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * エンドポイントの実装クラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.11 $ $Date: 2009/05/21 12:02:53 $
 * @author torao
 * @since 2009/04/23 Java2 SE 5.0
 */
public class EndpointImpl extends DeviceNode<AltSettingImpl, DeviceNode<?,?,?>, EndpointDescriptor> implements Endpoint{

	// ======================================================================
	// 等時間隔イベントディスパッチャー
	// ======================================================================
	/**
	 * 非同期 Isochronous 受信を行うためのディスパッチャースレッドです。
	 * <p>
	 */
	private IsocEventDispatcher dispatcher = null;

	// ======================================================================
	// インターフェース番号
	// ======================================================================
	/**
	 * このエンドポイントの所属するインターフェースの番号です。
	 * <p>
	 */
	private final byte intf;

	// ======================================================================
	// 代替設定
	// ======================================================================
	/**
	 * このエンドポイントの所属する代替設定の番号です。
	 * <p>
	 */
	private final byte alt;

	// ======================================================================
	// エンドポイント番号
	// ======================================================================
	/**
	 * このエンドポイントの番号です。
	 * <p>
	 */
	private final byte edpt;

	// ======================================================================
	// 転送方向
	// ======================================================================
	/**
	 * このエンドポイントの転送方向です。
	 * <p>
	 */
	private final Direction dir;

	// ======================================================================
	// 転送タイプ
	// ======================================================================
	/**
	 * このエンドポイントの転送タイプです。
	 * <p>
	 */
	private final TransferType type;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param altSetting 代替設定
	 * @param desc エンドポイント記述子
	 */
	protected EndpointImpl(AltSettingImpl altSetting, EndpointDescriptor desc) {
		super(altSetting, desc);
		this.intf = (byte)altSetting.getDescriptor().getInterfaceNumber();
		this.alt = (byte)altSetting.getAltSetting();
		this.edpt = (byte)desc.getEndpointNumber();
		this.dir = desc.getDirection();
		this.type = desc.getTransferType();
		return;
	}

	// ======================================================================
	// エンドポイント番号
	// ======================================================================
	/**
	 * このエンドポイントの番号 {@code bEndpointNumber} を参照します。
	 * <p>
	 * @return エンドポイント番号
	 */
	public int getEndpointNumber(){
		return edpt & 0xFF;
	}

	// ======================================================================
	// 代替設定の参照
	// ======================================================================
	/**
	 * このエンドポイントを持つ代替設定を参照します。
	 * <p>
	 * @return 代替設定
	 */
	public AltSetting getAltSetting(){
		return super.getParentNode();
	}

	// ======================================================================
	// データの読み込み
	// ======================================================================
	/**
	 * 指定されたバッファへデータを読み込みます。
	 * <p>
	 * @param buffer バッファ
	 * @param offset オフセット
	 * @param length 長さ
	 * @return 実際に読み込んだ長さ
	 * @param timeout タイムアウト (ミリ秒)
	 * @throws USBException 書き込みに失敗した場合
	 */
	public int read(byte[] buffer, int offset, int length, int timeout) throws USBException {
		ensureAltSetting(alt);
		switch(type){
		case BULK:
			return bridge.bulkTransfer(device, intf, edpt, buffer, offset, length, dir, timeout);
		case INTERRUPT:
			return bridge.interruptTransfer(device, intf, edpt, buffer, offset, length, dir, timeout);
		default:
			throw new USBException("read unsupported endpoint: " + type);
		}
	}

	// ======================================================================
	// データの書き込み
	// ======================================================================
	/**
	 * 指定されたバッファのデータを書き込みます。
	 * <p>
	 * @param buffer バッファ
	 * @param offset オフセット
	 * @param length 長さ
	 * @param timeout タイムアウト (ミリ秒)
	 * @throws USBException 書き込みに失敗した場合
	 */
	public void write(byte[] buffer, int offset, int length, int timeout) throws USBException {
		ensureAltSetting(alt);

		// ※ length が 0 指定でも必ず 1 度は出力を行う
		switch(type){
		case BULK:
			do{
				int len = bridge.bulkTransfer(device, intf, edpt, buffer, offset, length, dir, timeout);
				length -= len;
			} while(length > 0);
			break;
		case INTERRUPT:
			do{
				int len = bridge.interruptTransfer(device, intf, edpt, buffer, offset, length, dir, timeout);
				length -= len;
			} while(length > 0);
			break;
		default:
			throw new USBException("write unsupported endpoint: " + type);
		}
		return;
	}

	// ======================================================================
	// 等時間隔受信の開始
	// ======================================================================
	/**
	 * デフォルトのスレッド優先度を使用してこのエンドポイントでの等時間隔受信を開始します。
	 * <p>
	 * @param l データ受信を通知するリスナ
	 * @param startFrame 開始フレーム
	 * @param frames 通知するフレーム数
	 * @param bufSize 1フレーム当たりのバッファサイズ
	 * @throws USBException 等時間隔受信の開始に失敗した場合
	 */
	public void startIsocRead(IsocTransferListener l, long startFrame, int frames, int bufSize) throws USBException{
		startIsocRead(l, startFrame, frames, bufSize, Thread.NORM_PRIORITY);
		return;
	}

	// ======================================================================
	// 等時間隔受信の開始
	// ======================================================================
	/**
	 * このエンドポイントでの等時間隔受信を開始します。
	 * <p>
	 * 受信データはいくつかのフレームをまとめてリスナに通知します。
	 * <p>
	 * @param l データ受信を通知するリスナ
	 * @param startFrame 開始フレーム
	 * @param frames リスナに通知するフレーム (ミリ秒) 単位
	 * @param bufSize 1フレーム当たりのバッファサイズ
	 * @param priority イベント配信スレッドの優先順位
	 * @throws USBException 等時間隔受信の開始に失敗した場合
	 */
	public void startIsocRead(IsocTransferListener l, long startFrame, int frames, int bufSize, int priority) throws USBException{
		ensureAltSetting(alt);
		EndpointDescriptor desc = getDescriptor();

		// Isochronous 転送でなければ例外
		if(! TransferType.ISOCHRONOUS.equals(desc.getTransferType())){
			throw new USBException("invalid transfer type: " + desc.getTransferType());
		}

		// IN でなければ例外
		if(! desc.equals(Direction.IN)){
			throw new USBException("invalid direction: " + desc.getDirection());
		}

		// フレーム数が負なら例外
		if(frames <= 0){
			throw new IllegalArgumentException("frames<=0; " + frames);
		}

		// ディスパッチャースレッドの開始
		byte[][] buffer = new byte[frames][bufSize];
		IsocTransferEvent e = new IsocTransferEvent(this, startFrame, buffer);
		synchronized(this){
			if(dispatcher != null){
				throw new USBException("isochronous reading already active");
			}
			dispatcher = new IsocEventDispatcher(this, l, e);
		}
		dispatcher.setPriority(priority);
		dispatcher.start();

		return;
	}

	// ======================================================================
	// 等時間隔受信の終了
	// ======================================================================
	/**
	 * このエンドポイントで行っている等時間隔受信を停止します。
	 * <p>
	 * @throws USBException 等時間隔受信の開始に失敗した場合
	 */
	public void stopIsocRead() throws USBException{
		synchronized(this){
			if(dispatcher != null){
				dispatcher.stopDispatch();
				dispatcher = null;
			}
		}
		return;
	}

	// ======================================================================
	// HALT 状態のクリア
	// ======================================================================
	/**
	 * このエンドポイントの HALT 状態をクリアします。
	 * <p>
	 * @throws USBException HALT 状態のクリアに失敗した場合
	*/
	public void clearHalt() throws USBException {
		bridge.clearHalt(device, intf, edpt);
		return;
	}

	// ======================================================================
	// 機能のクリア
	// ======================================================================
	/**
	 * このエンドポイントに対して CLEAR_FEATURE 要求を実行します。
	 * <p>
	 * @param feature 機能識別子
	 * @throws USBException コントロール要求に失敗した場合
	 */
	public void clearFeature(int feature) throws USBException{
		ControlRequest request = new ControlRequest(
			DIR.OUT, TYPE.STANDARD,
			ControlRequest.RCPT_ENDPOINT,
			ControlRequest.CLEAR_FEATURE,
			toUINT8("feature", feature), getEndpointNumber(), EMPTY_BUFFER);
		device.deviceRequest(request);
		return;
	}

	// ======================================================================
	// 機能の設定
	// ======================================================================
	/**
	 * このインターフェースに対して SET_FEATURE 要求を実行します。
	 * <p>
	 * @param feature 機能識別子
	 * @throws USBException コントロール要求に失敗した場合
	 */
	public void setFeature(int feature) throws USBException{
		ControlRequest request = new ControlRequest(
			DIR.OUT, TYPE.STANDARD,
			ControlRequest.RCPT_ENDPOINT,
			ControlRequest.SET_FEATURE,
			toUINT8("feature", feature), getEndpointNumber(), EMPTY_BUFFER);
		device.deviceRequest(request);
		return;
	}

	// ======================================================================
	// ステータスの取得
	// ======================================================================
	/**
	 * GET_STATUS 要求を実行してエンドポイントのステータスを取得します。返値は UINT16 の
	 * 範囲をとります。
	 * <p>
	 * @return ステータス
	 * @throws USBException コントロール要求に失敗した場合
	 */
	public int getStatus() throws USBException{
		byte[] buffer = new byte[2];
		ControlRequest request = new ControlRequest(
			DIR.IN, TYPE.STANDARD,
			ControlRequest.RCPT_ENDPOINT,
			ControlRequest.GET_STATUS,
			0, getEndpointNumber(), buffer);
		int len = device.deviceRequest(request);
		ByteBuffer b = ByteBuffer.wrap(buffer, 0, len);
		b.order(USB.BYTE_ORDER);
		return b.getShort() & 0xFFFF;
	}

	// ======================================================================
	// ステータスの取得
	// ======================================================================
	/**
	 * GET_STATUS 要求を実行してエンドポイントのステータスを取得します。返値は UINT16 の
	 * 範囲をとります。
	 * <p>
	 * @return ステータス
	 * @throws USBException コントロール要求に失敗した場合
	 */
	public int synchFrame() throws USBException{
		byte[] buffer = new byte[2];
		ControlRequest request = new ControlRequest(
			DIR.IN, TYPE.STANDARD,
			ControlRequest.RCPT_ENDPOINT,
			ControlRequest.SYNCH_FRAME,
			0, getEndpointNumber(), buffer);
		int len = device.deviceRequest(request);
		ByteBuffer b = ByteBuffer.wrap(buffer, 0, len);
		b.order(USB.BYTE_ORDER);
		return b.getShort() & 0xFFFF;
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
		return getDescriptor().toString();
	}

	// ======================================================================
	// 等時間隔読み込みの実行
	// ======================================================================
	/**
	 * このエンドポイントから等時間隔読み込みを開始します。このメソッドはディスパッチャースレッド
	 * から呼び出されます。
	 * <p>
	 * @param listener リスナ
	 * @param event 使用するイベントオブジェクト
	 * @throws USBException 読み込みに失敗した場合
	*/
	protected void runIsocRead(IsocTransferListener listener, IsocTransferEvent event) throws USBException{

		// このスレッドに割り込みが行われるまで読み込みと通知を実行
		while(! Thread.interrupted()){
			bridge.isochronousTransfer(device, intf, edpt, event);
			listener.receive(event);
		}
		return;
	}

	// ======================================================================
	// 終了コールバック
	// ======================================================================
	/**
	 * イベントスレッドからの終了のコールバックです。
	 * <p>
	*/
	void dispatcherFinished(){
		synchronized(this){
			this.dispatcher = null;
		}
		return;
	}

}
