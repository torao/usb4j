/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: InterfaceImpl.java,v 1.12 2009/05/21 12:02:54 torao Exp $
*/
package org.koiroha.usb.impl;

import java.nio.ByteBuffer;
import java.util.*;

import org.koiroha.usb.*;
import org.koiroha.usb.ControlRequest.*;
import org.koiroha.usb.desc.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// InterfaceImpl: USB インターフェース実装
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB のインターフェースを表す実装クラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.12 $ $Date: 2009/05/21 12:02:54 $
 * @author torao
 * @since 2009/04/22 Java2 SE 5.0
 */
public class InterfaceImpl extends DeviceNode<ConfigurationImpl, AltSettingImpl, Descriptor> implements Interface{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(InterfaceImpl.class.getName());

	// ======================================================================
	// インターフェース番号
	// ======================================================================
	/**
	 * このインターフェースの番号です。
	 * <p>
	 */
	private byte intf = -1;

	// ======================================================================
	// 要求済みフラグ
	// ======================================================================
	/**
	 * このインターフェースが既に要求されているかを表すフラグです。
	 * <p>
	 */
	private boolean claimed = false;

	// ======================================================================
	// 要求済みフラグ
	// ======================================================================
	/**
	 * このインターフェースが既に要求されているかを表すフラグです。
	 * <p>
	 */
	private int altSetting = -1;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param config コンフィギュレーション実装
	 */
	protected InterfaceImpl(ConfigurationImpl config) {
		super(config, null);
		return;
	}

	// ======================================================================
	// インターフェース番号の参照
	// ======================================================================
	/**
	 * このインターフェースの番号 {@code bInterfaceNumber} を参照します。返値は UINT8
	 * の範囲となります。
	 * <p>
	 * @return インターフェース番号
	 */
	public int getInterfaceNumber(){
		return intf & 0xFF;
	}

	// ======================================================================
	// コンフィギュレーションの参照
	// ======================================================================
	/**
	 * このインターフェースが属しているコンフィギュレーションを参照します。
	 * <p>
	 * @return コンフィギュレーション
	 */
	public Configuration getConfiguration(){
		return super.getParentNode();
	}

	// ======================================================================
	// 代替設定の設定
	// ======================================================================
	/**
	 * このインターフェースに対する操作対象の代替設定
	 * {@link InterfaceDescriptor#getAlternateSetting() bAlternateSetting} を
	 * 設定します。
	 * このメソッドの呼び出しにはインターフェースが要求されている必要があります。
	 * <p>
	 * @param alt 設定する代替設定
	 * @throws USBException 代替設定の設定に失敗した場合
	 */
	public void setActiveAltSetting(int alt) throws USBException{
		ensureInterfaceClaimed();

		// パラメータに対応する代替設定が存在することを確認
		boolean bingo = false;
		for(AltSetting a: getAltSettings()){
			if((a.getDescriptor().getAlternateSetting() & 0xFF) == alt){
				bingo = true;
				break;
			}
		}

		// パラメータが無効な場合
		if(! bingo){
			throw new USBException("altsetting with bAlternateSetting=" + alt + " not found");
		}

		// デバイスリクエストを実行
		byte[] buffer = new byte[0];
		ControlRequest request = new ControlRequest(
			DIR.OUT, TYPE.STANDARD, ControlRequest.RCPT_INTERFACE,
			ControlRequest.SET_INTERFACE, toUINT8("alt", alt) & 0xFF, getInterfaceNumber(), buffer);
		device.deviceRequest(request);

		// 設定されたコンフィギュレーションを保持
		this.altSetting = alt & 0xFF;
		return;
	}

	// ======================================================================
	// 代替設定の参照
	// ======================================================================
	/**
	 * このインターフェースの現在の代替設定 {@code bAlternateSetting} を参照します。
	 * このメソッドは {@code getActiveAltSetting(false)} と等価です。
	 * <p>
	 * @return 現在の代替設定
	 * @throws USBException 代替設定の参照に失敗した場合
	 */
	public int getActiveAltSetting() throws USBException{
		return getActiveAltSetting(false);
	}

	// ======================================================================
	// 代替設定の参照
	// ======================================================================
	/**
	 * このインターフェースの現在の代替設定 {@code bAlternateSetting} を参照します。
	 * {@code forceRetrieve} に true を指定した場合、インターフェースのインスタンスが保持
	 * している現在の代替設定値を無視してデバイスに GET_INTERFACE を発行します。false を
	 * 指定した場合は
	 * このメソッドの呼び出しにはインターフェースが要求されている必要があります。
	 * <p>
	 * @param forceRetrieve デバイスに GET_INTERFACE を発行する場合
	 * @return 代替設定
	 * @throws USBException 代替設定の参照に失敗した場合
	 */
	public int getActiveAltSetting(boolean forceRetrieve) throws USBException{
		ensureInterfaceClaimed();

		// まだ代替設定が分かっていなければデバイスリクエストを実行して取得
		if(forceRetrieve || this.altSetting < 0){
			byte[] buffer = new byte[1];
			ControlRequest request = new ControlRequest(
				DIR.IN, TYPE.STANDARD, ControlRequest.RCPT_INTERFACE,
				ControlRequest.GET_INTERFACE, 0, getInterfaceNumber() & 0xFF, buffer);
			device.deviceRequest(request);
			this.altSetting = buffer[0] & 0xFF;
		}

		return this.altSetting;
	}

	// ======================================================================
	// 代替設定の参照
	// ======================================================================
	/**
	 * このインターフェースの代替設定を参照します。
	 * <p>
	 * @return 代替設定のリスト
	 */
	public List<AltSetting> getAltSettings(){
		return new ArrayList<AltSetting>(super.getChildNode());
	}

	// ======================================================================
	// インターフェースの要求確認
	// ======================================================================
	/**
	 * このインターフェースが要求済みかどうかを返します。
	 * <p>
	 * @return インターフェースが要求済みの場合 true
	*/
	public boolean isClaimed(){
		return claimed;
	}

	// ======================================================================
	// インターフェースの要求
	// ======================================================================
	/**
	 * インターフェースを要求します。
	 * <p>
	 * @throws ResourceBusyException
	 * @throws USBException
	*/
	public void claim() throws ResourceBusyException, USBException {
		synchronized(device){
			ensureDeviceOpened();

			// 既に要求が行われている場合
			if(isClaimed()){
				throw new USBException("interface already claimed");
			}

			// 現在のコンフィギュレーションがこのインターフェースの親でない場合
//			if(device.getActiveConfiguration() != config.getDescriptor().getConfigurationValue()){
//				throw new USBException("current configuration #" + device.getActiveConfiguration() + " is not parent of this interface");
//			}

			// このインターフェースを要求
			logger.finest("claim()");
			bridge.claim(device, intf);
			claimed = true;
		}
		return;
	}

	// ======================================================================
	// インターフェースの解放
	// ======================================================================
	/**
	 * インターフェースを解放します。
	 * <p>
	 * @throws USBException 解放に失敗した場合
	*/
	public void release() throws USBException {
		synchronized(device){
			if(isClaimed()){
				logger.finest("release()");
				bridge.release(device, intf);
				claimed = false;
				altSetting = -1;
			}
		}
		return;
	}

	// ======================================================================
	// 機能のクリア
	// ======================================================================
	/**
	 * このインターフェースに対して CLEAR_FEATURE 要求を実行します。
	 * <p>
	 * @param feature 機能識別子
	 * @throws USBException コントロール要求に失敗した場合
	 */
	public void clearFeature(int feature) throws USBException{
		ControlRequest request = new ControlRequest(
			DIR.OUT,
			TYPE.STANDARD,
			ControlRequest.RCPT_INTERFACE,
			ControlRequest.CLEAR_FEATURE,
			toUINT8("feature", feature), getInterfaceNumber(), EMPTY_BUFFER);
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
			DIR.OUT,
			TYPE.STANDARD,
			ControlRequest.RCPT_INTERFACE,
			ControlRequest.SET_FEATURE,
			toUINT8("feature", feature), getInterfaceNumber(), EMPTY_BUFFER);
		device.deviceRequest(request);
		return;
	}

	// ======================================================================
	// ステータスの取得
	// ======================================================================
	/**
	 * GET_STATUS 要求を実行してインターフェースのステータスを取得します。返値は UINT16 の
	 * 範囲をとります。
	 * <p>
	 * @return ステータス
	 * @throws USBException コントロール要求に失敗した場合
	 */
	public int getStatus() throws USBException{
		byte[] buffer = new byte[2];
		ControlRequest request = new ControlRequest(
			DIR.IN,
			TYPE.STANDARD,
			ControlRequest.RCPT_INTERFACE,
			ControlRequest.GET_STATUS,
			0, getInterfaceNumber(), buffer);
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
	 * @return インスタンスの文字列
	 */
	@Override
	public String toString(){
		return getAltSettings().toString();
	}

	// ======================================================================
	// 子ノードの追加
	// ======================================================================
	/**
	 * このインターフェースの番号を取得するためにオーバーライドします。
	 * <p>
	 * @param node 追加する代替設定
	*/
	@Override
	protected void addChildNode(AltSettingImpl node) {
		super.addChildNode(node);
		this.intf = (byte)node.getDescriptor().getInterfaceNumber();
		return;
	}

	// ======================================================================
	// 代替設定実装の構築
	// ======================================================================
	/**
	 * 指定された記述子を持つ代替設定実装を構築します。このメソッドはサブクラスでオーバーライドし
	 * 実装クラスを変更できるように公開されています。
	 * <p>
	 * @param desc インターフェース記述子
	 * @return 代替設定実装
	 */
	protected AltSettingImpl getAltSettingImpl(InterfaceDescriptor desc){
		return new AltSettingImpl(this, desc);
	}

	// ======================================================================
	// 要求済みの保証
	// ======================================================================
	/**
	 * このインターフェースが要求済みであることを保証します。
	 * <p>
	 * @throws USBException インターフェースが要求されていない場合
	*/
	@Override
	protected void ensureInterfaceClaimed() throws USBException{
		ensureDeviceOpened();
		if(! isClaimed()){
			throw new USBException("interface is not claimed");
		}
		return;
	}

}
