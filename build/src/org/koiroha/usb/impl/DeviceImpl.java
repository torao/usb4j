/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: DeviceImpl.java,v 1.12 2009/05/21 12:02:53 torao Exp $
*/
package org.koiroha.usb.impl;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;

import org.koiroha.usb.*;
import org.koiroha.usb.ControlRequest.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.event.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Device: デバイス実装
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB デバイスの実装クラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.12 $ $Date: 2009/05/21 12:02:53 $
 * @author torao
 * @since 2009/04/22 Java2 SE 5.0
 */
public class DeviceImpl extends DeviceNode<DeviceNode<?,?,?>,ConfigurationImpl,DeviceDescriptor> implements Device{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DeviceImpl.class.getName());

	// ======================================================================
	// バス
	// ======================================================================
	/**
	 * このデバイスが接続されているバスです。
	 * <p>
	 */
	private BusImpl bus = null;

	// ======================================================================
	// 親デバイス
	// ======================================================================
	/**
	 * このデバイスの親です。
	 * <p>
	 */
	private DeviceImpl parent = null;

	// ======================================================================
	// 子デバイス
	// ======================================================================
	/**
	 * このデバイスの子です。
	 * <p>
	 */
	private List<Device> children = null;

	// ======================================================================
	// デバイスハンドル
	// ======================================================================
	/**
	 * このリソースのハンドルです。
	 * <p>
	 */
	private Object handle = null;

	// ======================================================================
	// コンフィギュレーション
	// ======================================================================
	/**
	 * 現在選択されているコンフィギュレーションです。まだコンフィギュレーションが設定されていない
	 * 場合は負の値をとります。
	 * <p>
	 */
	private int activeConfiguration = -1;

	// ======================================================================
	// 解放済みフラグ
	// ======================================================================
	/**
	 * このノードの所有者であるデバイスが解放されているかどうかを表すフラグです。
	 * <p>
	 */
	private boolean released = false;

	// ======================================================================
	// デバイスリスナ
	// ======================================================================
	/**
	 * デバイスリスナです。
	 * <p>
	 */
	private final List<DeviceListener> listener = new ArrayList<DeviceListener>();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定されたデバイス記述子を使用するデバイスを構築します。
	 * コンフィギュレーション記述子バイナリの配列はデバイス記述子の {@code bNumConfiguraion}
	 * と同じ長さです。コンフィギュレーションの取得に失敗している場合、その要素に null が含まれ
	 * ていてもかまいません。
	 * <p>
	 * @param driver USB ドライバ
	 * @param desc デバイス記述子
	 * @param conf コンフィギュレーション以下の記述子のバイナリデータ
	 */
	protected DeviceImpl(USBBridge driver, DeviceDescriptor desc, ByteBuffer[] conf) {
		super(driver, desc);
		assert(desc.getNumConfigurations() == conf.length);

		// コンフィギュレーション実装を構築
		for(int i=0; i<conf.length; i++){
			if(conf[i] != null){
				ConfigurationImpl confImpl = new ConfigurationImpl(this, conf[i]);
				addChildNode(confImpl);
			} else {
				logger.fine("raw configuration binary not found");
			}
		}
		return;
	}

	// ======================================================================
	// ファイナライザ
	// ======================================================================
	/**
	 * このインスタンスがハンドルを保持したままであれば解放を行います。
	 * <p>
	 * @throws Throwable スーパークラスのファイナライザが失敗した場合
	*/
	@Override
	protected void finalize() throws Throwable {
		if(handle != null){
			logger.warning("abandoned resource handle: " + handle);
			try{
				release();
			} catch(USBException ex){
				logger.log(Level.WARNING, "release failure", ex);
			}
		}
		super.finalize();
		return;
	}

	// ======================================================================
	// バスの参照
	// ======================================================================
	/**
	 * このデバイスが接続されているバスを参照します。
	 * <p>
	 * @return このデバイスのバス
	 */
	public Bus getBus(){
		if(parent == null){
			return bus;
		}
		return parent.getBus();
	}

	// ======================================================================
	// バスの設定
	// ======================================================================
	/**
	 * このデバイスのバスを設定します。このメソッドの呼び出しによりこのデバイスはルートハブとなり
	 * ます。
	 * <p>
	 * @param bus このデバイスのバス
	 */
	void setBus(BusImpl bus){
		this.bus = bus;
		this.parent = null;
		return;
	}

	// ======================================================================
	// 親デバイスの参照
	// ======================================================================
	/**
	 * このデバイスが接続されているデバイス (ハブなど) を参照します。
	 * <p>
	 * @return このデバイスの親デバイス
	 */
	public Device getParentDevice(){
		return parent;
	}

	// ======================================================================
	// 子デバイスの参照
	// ======================================================================
	/**
	 * このデバイスに接続されている別のデバイスを参照します。
	 * <p>
	 * @return このデバイスの子デバイス
	 */
	public List<Device> getChildDevices(){
		if(children == null){
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(children);
	}

	// ======================================================================
	// リソースハンドルの参照
	// ======================================================================
	/**
	 * このリソースのプラットフォーム依存のハンドル値を参照します。
	 * <p>
	 * @return このリソースのハンドル
	 */
	public Object getHandle(){
		return handle;
	}

	// ======================================================================
	// リソースオープンの判定
	// ======================================================================
	/**
	 * このリソースがオープンされているかどうかを判定します。
	 * <p>
	 * @return オープンされている場合 true
	 */
	public boolean isOpen(){
		return (getHandle() != null);
	}

	// ======================================================================
	// リソースのオープン
	// ======================================================================
	/**
	 * このリソースをオープンします。
	 * <p>
	 * @throws ResourceBusyException 他の処理によってこのリソースが既にオープンされている場合
	 * @throws USBException リソースのクローズに失敗した場合
	 */
	public synchronized void open() throws ResourceBusyException, USBException{
		logger.finest("open()");
		ensureUnreleased();

		// デバイスの状態を確認
		if(isOpen()){
			throw new USBException("device already opened");
		}

		// デバイスのオープンを実行
		this.handle = bridge.open(this);
		if(this.handle == null){
			throw new NullPointerException(bridge.getClass().getSimpleName() + " returns null as device handle");
		}

		// デバイスのオープンを通知
		DeviceEvent e = new DeviceEvent(this);
		for(DeviceListener l: new ArrayList<DeviceListener>(listener)){
			l.deviceOpened(e);
		}
		return;
	}

	// ======================================================================
	// デバイスのクローズ
	// ======================================================================
	/**
	 * このデバイスをクローズします。
	 * <p>
	 * @throws USBException 解放に失敗した場合
	 */
	public synchronized void close() throws USBException{

		// 既にクローズや解放が行われているなら何もしない
		if(! isOpen() || released){
			return;
		}

		// 全てのインターフェースをクローズ
		for(Configuration conf: getChildNode()){
			for(Interface intf: conf.getInterfaces()){
				intf.release();
			}
		}

		// デバイスハンドルを解放
		logger.finest("close()");
		bridge.close(this);

		// 状態の初期化
		this.handle = null;
		this.activeConfiguration = -1;

		// デバイスのクローズを通知
		DeviceEvent e = new DeviceEvent(this);
		for(DeviceListener l: new ArrayList<DeviceListener>(listener)){
			l.deviceClosed(e);
		}
		return;
	}

	// ======================================================================
	// デバイスのリセット
	// ======================================================================
	/**
	 * このデバイスをリセットします。このメソッドの呼び出しにより {@link USBBridge#close(DeviceImpl)}
	 * と {@link USBBridge#release(DeviceImpl)} が呼び出されます。
	 * <p>
	 * @throws USBException リセットに失敗した場合
	 */
	public synchronized void reset() throws USBException{
		logger.finest("reset()");
		ensureDeviceOpened();

		// デバイスのリセットを実行
		bridge.reset(this);

		// デバイスの解放
		this.release();
		return;
	}

	// ======================================================================
	// デバイスの解放
	// ======================================================================
	/**
	 * このデバイスを解放します。
	 * <p>
	 * @throws USBException 解放に失敗した場合
	 */
	public synchronized void release() throws USBException{

		// 既に解放されていれば何も行わない
		if(released){
			return;
		}

		// デバイスがオープンされたままであればクローズを行う
		try{
			if(isOpen()){
				close();
			}
		} catch(USBException ex){
			logger.log(Level.WARNING, "fail to close device", ex);
		}

		// デバイスの解放
		logger.finest("release()");
		bridge.release(this);
		released = true;

		// デバイスの解放を通知
		DeviceEvent e = new DeviceEvent(this);
		for(DeviceListener l: new ArrayList<DeviceListener>(listener)){
			l.deviceReleased(e);
		}
		return;
	}

	// ======================================================================
	// デバイス記述の参照
	// ======================================================================
	/**
	 * このデバイスの記述子を参照します。
	 * <p>
	 * @return デバイス記述子
	 */
	@Override
	public DeviceDescriptor getDescriptor(){
		return super.getDescriptor();
	}

	// ======================================================================
	// コンフィギュレーションの参照
	// ======================================================================
	/**
	 * このデバイスからコンフィギュレーションを参照します。
	 * <p>
	 * @return このデバイスのコンフィギュレーション
	 */
	public List<Configuration> getConfigurations() {
		return new ArrayList<Configuration>(getChildNode());
	}

	// ======================================================================
	// アクティブコンフィギュレーションの設定
	// ======================================================================
	/**
	 * このデバイス操作の前提とするアクティブなコンフィギュレーションを設定します。{@code conf}
	 * パラメータは一般的に 1 から始まるコンフィギュレーション記述子の {@code bConfigurationValue}
	 * 値です。{@code conf} に 0 を指定した場合はデバイスをコンフィギュレーションされていない
	 * 状態にします。
	 * <p>
	 * @param conf アクティブにするコンフィギュレーション値
	 * @throws USBException コンフィギュレーションの設定に失敗した場合
	 */
	public void setActiveConfiguration(int conf) throws USBException{
		ensureDeviceOpened();

		if(conf != 0){

			// パラメータに対応するコンフィギュレーションが存在することを確認
			boolean bingo = false;
			for(Configuration c: getConfigurations()){
				if((c.getDescriptor().getConfigurationValue() & 0xFF) == conf){
					bingo = true;
					break;
				}
			}

			// パラメータが無効な場合
			if(! bingo){
				throw new USBException("configuration with bConfigurationValue=" + conf + " not found");
			}
		}

		// デバイスリクエストを実行
		byte[] buffer = new byte[0];		// ※検知用
		ControlRequest request = new ControlRequest(
			DIR.OUT,
			TYPE.STANDARD,
			ControlRequest.RCPT_DEVICE,
			ControlRequest.SET_CONFIGURATION,
			toUINT8("conf", conf), 0, buffer);
		deviceRequest(request);

		// 設定されたコンフィギュレーションを保持
		this.activeConfiguration = conf & 0xFF;
		return;
	}

	// ======================================================================
	// アクティブコンフィギュレーションの参照
	// ======================================================================
	/**
	 * このデバイス操作の前提となっているアクティブなコンフィギュレーションを参照します。返値は
	 * 対応するコンフィギュレーション記述子の {@code bConfigurationValue} です。
	 * <p>
	 * @return アクティブにするコンフィギュレーション値
	 * @throws USBException コンフィギュレーションの取得に失敗した場合
	 */
	public int getActiveConfiguration() throws USBException{
		return getActiveConfiguration(false);
	}

	// ======================================================================
	// アクティブコンフィギュレーションの参照
	// ======================================================================
	/**
	 * デバイスに対する操作の対象となるコンフィギュレーションを参照します。返値は対応するコンフィ
	 * ギュレーション記述子の {@code bConfigurationValue} 値です。
	 * <p>
	 * {@code forceRetrieve} パラメータに true を指定した場合、デバイスのインスタンスが
	 * 保持している現在のコンフィギュレーション値を無視してデバイスに GET_CONFIGURATION 要求
	 * を発行します。false を指定した場合、インスタンスが既知のコンフィギュレーション値を保持し
	 * ていればそれが返されます。ただしコンフィギュレーション値が未定の場合はデバイスに
	 * GET_CONFIGURATION 要求を発行します。
	 * <p>
	 * このメソッドの呼び出しにはデバイスがオープンされている必要があります。
	 * <p>
	 * @param forceRetrieve GET_CONFIGURATION 要求を発行する場合 true
	 * @return アクティブにするコンフィギュレーション
	 * @throws USBException コンフィギュレーションの取得に失敗した場合
	 */
	public int getActiveConfiguration(boolean forceRetrieve) throws USBException{
		ensureDeviceOpened();

		// まだコンフィギュレーションが分かっていなければデバイスリクエストを実行して取得
		if(forceRetrieve || this.activeConfiguration < 0){
			byte[] buffer = new byte[1];
			ControlRequest request = new ControlRequest(
				DIR.IN, TYPE.STANDARD,
				ControlRequest.RCPT_DEVICE,
				ControlRequest.GET_CONFIGURATION,
				0, 0, buffer);
			deviceRequest(request);
			this.activeConfiguration = buffer[0] & 0xFF;
		}

		return this.activeConfiguration;
	}

	// ======================================================================
	// 文字列記述子の参照
	// ======================================================================
	/**
	 * 指定されたインデックスの文字列記述子の値を参照します。
	 * <p>
	 * このメソッドの実行はデバイスがオープンされている必要があります。
	 * <p>
	 * @param index 文字列記述子のインデックス
	 * @param langid 言語ID
	 * @return 文字列記述子の値
	 * @throws USBException 文字列記述子の取得に失敗した場合
	 */
	public String getString(int index, int langid) throws USBException{
		if(index == 0){
			throw new USBException("use getLangID() instead of zero index specified getString()");
		}
		ByteBuffer buffer = getStringDescriptor(index, langid);
		StringDescriptor desc = new StringDescriptor(buffer);
		return desc.getString();
	}

	// ======================================================================
	// デフォルト言語 ID の参照
	// ======================================================================
	/**
	 * このデバイスのデフォルトの言語 ID を問い合わせます。
	 * <p>
	 * @return デフォルトの言語 ID
	 * @throws USBException 言語 ID の取得に失敗した場合
	 */
	public int[] getLangID() throws USBException{

		// 文字列ディスクリプタのバイナリを取得
		ByteBuffer buffer = getStringDescriptor(0, 0);
		if(buffer.limit() == 0){
			return new int[0];
		}

		// 言語 ID を返す
		LangIDDescriptor desc = new LangIDDescriptor(buffer);
		short[] langid = desc.getLangID();
		int[] l = new int[langid.length];
		for(int i=0; i<langid.length; i++){
			l[i] = langid[i] & 0xFFFF;
		}
		return l;
	}

	// ======================================================================
	// 機能のクリア
	// ======================================================================
	/**
	 * このデバイスに対して CLEAR_FEATURE 要求を実行します。
	 * <p>
	 * @param feature 機能識別子
	 * @throws USBException コントロール要求に失敗した場合
	 */
	public void clearFeature(int feature) throws USBException{
		ControlRequest request = new ControlRequest(
			DIR.OUT,
			TYPE.STANDARD,
			ControlRequest.RCPT_DEVICE,
			ControlRequest.CLEAR_FEATURE,
			toUINT8("feature", feature), 0, EMPTY_BUFFER);
		deviceRequest(request);
		return;
	}

	// ======================================================================
	// 機能の設定
	// ======================================================================
	/**
	 * このデバイスに対して SET_FEATURE 要求を実行します。
	 * <p>
	 * @param feature 機能識別子
	 * @throws USBException コントロール要求に失敗した場合
	 */
	public void setFeature(int feature) throws USBException{
		ControlRequest request = new ControlRequest(
			DIR.OUT,
			TYPE.STANDARD,
			ControlRequest.RCPT_DEVICE,
			ControlRequest.SET_FEATURE,
			toUINT8("feature", feature), 0, EMPTY_BUFFER);
		deviceRequest(request);
		return;
	}

	// ======================================================================
	// ステータスの取得
	// ======================================================================
	/**
	 * GET_STATUS 要求を実行してデバイスのステータスを取得します。返値は UINT16 の範囲を
	 * とります。
	 * <p>
	 * @return ステータス
	 * @throws USBException コントロール要求に失敗した場合
	 */
	public int getStatus() throws USBException{
		byte[] buffer = new byte[2];
		ControlRequest request = new ControlRequest(
			DIR.IN,
			TYPE.STANDARD,
			ControlRequest.RCPT_DEVICE,
			ControlRequest.GET_STATUS,
			0, 0, buffer);
		int len = deviceRequest(request);
		ByteBuffer b = ByteBuffer.wrap(buffer, 0, len);
		b.order(USB.BYTE_ORDER);
		return b.getShort() & 0xFFFF;
	}

	// ======================================================================
	// デバイス要求の実行
	// ======================================================================
	/**
	 * このデバイスに対して要求を実行します。このメソッドはコンフィギュレーション0、インター
	 * フェース0、エンドポイント0 のコントロール転送に対して指定された要求を行うための簡易メソッ
	 * ドです。
	 * <p>
	 * @param request デバイスリクエスト
	 * @return 実際の入出力サイズ
	 * @throws USBException 言語 ID の取得に失敗した場合
	 */
	public int deviceRequest(ControlRequest request) throws USBException{
		ensureDeviceOpened();

		// デバイスリクエストの実行
		int len = bridge.deviceRequest(this, request);

		// 実行結果の確認
		if(len < 0){
			throw new USBException("usb bridge returns negative length: " + len);
		}
		if(len > request.getRawBuffer().length){
			throw new USBException("usb bridge returns too large length: " + len + "/" + request.getRawBuffer().length);
		}

		return len;
	}

	// ======================================================================
	// デバイスリスナの追加
	// ======================================================================
	/**
	 * このデバイスにリスナを追加します。
	 * <p>
	 * @param l デバイスリスナ
	 */
	public void addDeviceListener(DeviceListener l){
		listener.add(l);
		return;
	}

	// ======================================================================
	// デバイスリスナの削除
	// ======================================================================
	/**
	 * このデバイスにリスナを削除します。
	 * <p>
	 * @param l デバイスリスナ
	 */
	public void removeDeviceListener(DeviceListener l){
		listener.remove(l);
		return;
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
		return getHandle() + "," + getDescriptor();
	}

	// ======================================================================
	// コンフィギュレーション実装の構築
	// ======================================================================
	/**
	 * 指定された記述子バイナリからコンフィギュレーション実装を構築します。このメソッドはサブクラ
	 * スでオーバーイドし実装クラスを変更できるように公開されています。
	 * <p>
	 * @param rawBinary コンフィギュレーション記述子とそれに続く記述子のバイナリ
	 * @return コンフィギュレーション実装
	 */
	protected ConfigurationImpl getConfigurationImpl(ByteBuffer rawBinary){
		return new ConfigurationImpl(this, rawBinary);
	}

	// ======================================================================
	// デバイスの解放確認
	// ======================================================================
	/**
	 * このノードの所有者であるデバイス実装が解放されているかどうかを確認します。
	 * <p>
	 * @return 解放されている場合 true
	 */
	@Override
	public boolean isReleased(){
		return released;
	}

	// ======================================================================
	// オープンされたハンドルの参照
	// ======================================================================
	/**
	 * オープンされているハンドルを参照します。デバイスがオープンされていない場合は例外が発生しま
	 * す。
	 * <p>
	 * @return デバイスハンドル
	 * @throws USBException リソースがオープンされていない場合
	 */
	public Object getOpenedHandle() throws USBException{
		if(! isOpen()){
			throw new USBException("not opened");
		}
		return handle;
	}

	// ======================================================================
	// 子デバイスの設定
	// ======================================================================
	/**
	 * このデバイスに接続されている別のデバイスを設定します。
	 * <p>
	 * @param device このデバイスに接続するデバイス
	 */
	public void connect(DeviceImpl device){
		if(children == null){
			children = new ArrayList<Device>();
		}
		this.children.add(device);
		device.parent = this;
		device.bus = null;
		return;
	}

	// ======================================================================
	// デバイス要求の実行
	// ======================================================================
	/**
	 * デバイスリクエストを実行して文字列記述子を取得します。
	 * <p>
	 * @param index 文字列記述子のインデックス
	 * @param langid 言語ID
	 * @return 文字列記述子の値
	 * @throws USBException 文字列記述子の取得に失敗した場合
	 */
	protected ByteBuffer getStringDescriptor(int index, int langid) throws USBException{
		byte[] buffer = new byte[64];
		while(true){

			// 記述子の取得
			int len = getRawDescriptor(
				Descriptor.TYPE_STRING, toUINT8("index", index), toUINT16("langid", langid), buffer);

			// バッファサイズより小さい読み込みが行われたらバイナリを返す
			if(len < buffer.length){
				return ByteBuffer.wrap(buffer, 0, len);
			}

			// 倍のサイズのバッファを用意して再実行
			buffer = new byte[buffer.length * 2];
		}
	}

	// ======================================================================
	// 記述子バイナリの取得
	// ======================================================================
	/**
	 * 指定された記述子のバイナリを取得します。
	 * <p>
	 * @param type 記述子のタイプ ({code Descriptor.TYPE_XXX})
	 * @param index 文字列記述子のインデックス
	 * @param langid 言語ID
	 * @param buffer データを格納するバッファ
	 * @return バッファに読み込まれた長さ
	 * @throws USBException 文字列記述子の取得に失敗した場合
	 */
	protected int getRawDescriptor(byte type, byte index, short langid, byte[] buffer) throws USBException{
		if(buffer.length < 2){
			throw new IllegalArgumentException("too small buffer size: " + buffer.length);
		}
		buffer[0] = 0;
		buffer[1] = 0;		// ※バッファへのデータ未設定検出用に設定

		// デバイス要求の実行
		ControlRequest request = new ControlRequest(
			DIR.IN, TYPE.STANDARD, ControlRequest.RCPT_DEVICE, ControlRequest.GET_DESCRIPTOR,
			((type & 0xFF) << 8) | (index & 0xFF), langid & 0xFFFF, buffer);
		int length = deviceRequest(request);

		// 実行結果の確認
		// ※LANGID 0 の場合は長さ 0 で返ることがある
		if(length != 0 && (buffer[1] & 0xFF) < 2){
			throw new USBException("usb bridge returns bad descriptor bLength: " + (buffer[1] & 0xFF));
		}

		return length;
	}

}
