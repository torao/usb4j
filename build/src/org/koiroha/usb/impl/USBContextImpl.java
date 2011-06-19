/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: USBContextImpl.java,v 1.2 2009/05/18 20:34:17 torao Exp $
*/
package org.koiroha.usb.impl;

import java.util.*;
import java.util.logging.Level;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.DeviceDescriptor;
import org.koiroha.usb.event.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// USBContextImpl: USB コンテキスト実装
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB コンテキストの実装です。
 * <p>
 * @version $Revision: 1.2 $ $Date: 2009/05/18 20:34:17 $
 * @author takami torao
 * @since usb4j 1.0 (Java2 SE 5.0) 2009/05/18
 */
public class USBContextImpl implements USBContext {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(USBContextImpl.class.getName());// ======================================================================

	// ======================================================================
	// コンテキストマップ
	// ======================================================================
	/**
	 * 仮想マシン上で使用されている全てのコンテキストです。シャットダウンフック時に未解放のデバイス
	 * を全て解放するために使用します。マップの値には必ず null が設定されます。
	 * <p>
	 */
	private static final Map<USBContextImpl,Object> CONTEXT = new WeakHashMap<USBContextImpl,Object>();

	// ======================================================================
	// スタティックイニシャライザ
	// ======================================================================
	/**
	 * システム終了時に未解放のデバイスの解放を試行するためのシャットダウンフックを登録します。
	 * <p>
	 */
	static {
		Runtime.getRuntime().addShutdownHook(new Shutdown());
	}

	// ======================================================================
	// ブリッジ
	// ======================================================================
	/**
	 * USB ライブラリのブリッジインターフェースです。
	 * <p>
	 */
	private final USBBridge bridge;

	// ======================================================================
	// デバイス一覧
	// ======================================================================
	/**
	 * このコンテキストで使用している未解放のデバイスです。
	 * <p>
	 */
	private final List<DeviceImpl> devices = new ArrayList<DeviceImpl>();

	// ======================================================================
	// コンテキストリスナ
	// ======================================================================
	/**
	 * このコンテキストのリスナです。
	 * <p>
	 */
	private final List<USBContextListener> listener
		= Collections.synchronizedList(new ArrayList<USBContextListener>());

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param bridge USB ブリッジ
	 */
	public USBContextImpl(USBBridge bridge) {
		this.bridge = bridge;

		// 弱参照マップに追加
		synchronized(CONTEXT){
			CONTEXT.put(this, null);
		}
		return;
	}

	// ======================================================================
	// デストラクタ
	// ======================================================================
	/**
	 * 未解放の全てのデバイスを解放します。
	 * <p>
	 * @throws Throwable 例外が発生した場合
	 */
	@Override
	protected void finalize() throws Throwable{
		clearDevices(true);
		super.finalize();
		return;
	}

	// ======================================================================
	// USB デバイス構成の同期化
	// ======================================================================
	/**
	 * このコンテキストの保持する USB デバイスを同期化します。
	 * <p>
	 * @throws USBException USB デバイスの取得に失敗した場合
	*/
	public void sync() throws USBException{
		synchronized(devices){

			// 解放されていない全てのデバイスを解放
			dispose();

			// デバイスの一覧を取得
			ReleaseListener l = new ReleaseListener();
			List<BusImpl> buses = bridge.find(this);
			for(Bus bus: buses){
				for(Device device: bus.getDevices()){
					devices.add((DeviceImpl)device);
					device.addDeviceListener(l);
				}
			}
		}
		return;
	}

	// ======================================================================
	// デバイスの検索
	// ======================================================================
	/**
	 * 全てのデバイスを検索します。
	 * <p>
	 * @return 全てのデバイス
	 * @throws USBException デバイスの検索に失敗した場合
	 */
	public List<Device> lookup() throws USBException {
		return lookup(null);
	}

	// ======================================================================
	// デバイスの検索
	// ======================================================================
	/**
	 * 指定されたベンダー ID / 製品 ID を持つデバイスを検索します。
	 * <p>
	 * @param idVendor ベンダー ID
	 * @param idProduct 製品 ID
	 * @return 一致するデバイス
	 * @throws USBException デバイスの検索に失敗した場合
	 */
	public List<Device> lookup(final int idVendor, final int idProduct) throws USBException {
		return lookup(new VendorFilter(idVendor, idProduct));
	}

	// ======================================================================
	// USB デバイスの取得
	// ======================================================================
	/**
	 * 指定されたフィルタを使用してデバイスを検索します。フィルタに null を指定すると全てのデバ
	 * イスが検索対象となります。
	 * <p>
	 * @param filter デバイスフィルタ
	 * @return USB デバイスのリスト
	 * @throws USBException USB デバイスの取得に失敗した場合
	*/
	public List<Device> lookup(DeviceFilter filter) throws USBException {

		// デバイスの一覧から該当するデバイスのみを保持
		List<Device> dev = new ArrayList<Device>();
		synchronized(devices){
			for(DeviceImpl d: devices){
				if(filter == null || filter.accept(d.getDescriptor())){
					dev.add(d);
				}
			}
		}

		return Collections.unmodifiableList(new ArrayList<Device>(devices));
	}

	// ======================================================================
	// コンテキストの解放
	// ======================================================================
	/**
	 * このコンテキスト内の未解放デバイスを全て解放します。
	 * <p>
	 * @throws USBException デバイスの解放に失敗した場合
	*/
	public void dispose() throws USBException {
		clearDevices(false);
		return;
	}

	// ======================================================================
	// リスナの追加
	// ======================================================================
	/**
	 * コンテキストにリスナを追加します。
	 * <p>
	 * @param l 追加するリスナ
	*/
	public void addContextListener(USBContextListener l){
		listener.add(l);
		return;
	}

	// ======================================================================
	// リスナの削除
	// ======================================================================
	/**
	 * コンテキストからリスナを削除します。
	 * <p>
	 * @param l 削除するリスナ
	*/
	public void removeContextListener(USBContextListener l){
		listener.remove(l);
		return;
	}

	// ======================================================================
	// デバイス取り付けイベントの通知
	// ======================================================================
	/**
	 * サブクラスから新規の USB デバイスを検出した通知を行います。このメソッドが呼び出された
	 * 時点でイベントに付加されているデバイスが利用可能な状態でなければいけません。
	 * <p>
	 * @param e イベント
	*/
	protected void fireDeviceAttached(DeviceEvent e){
		synchronized(listener){
			for(int i=0; i<listener.size(); i++){
				listener.get(i).deviceAttached(e);
			}
		}
		return;
	}

	// ======================================================================
	// デバイス取り外しイベントの通知
	// ======================================================================
	/**
	 * サブクラスから USB デバイスが取り外された通知を行います。このメソッドが呼び出された時点で
	 * イベントに付加されているデバイスは{@link DeviceImpl#release() 解放}されている必要
	 * があります。
	 * <p>
	 * @param e イベント
	*/
	protected void fireDeviceDetached(DeviceEvent e){
		synchronized(listener){
			for(int i=0; i<listener.size(); i++){
				listener.get(i).deviceDetached(e);
			}
		}
		return;
	}

	// ======================================================================
	// デバイスの解放
	// ======================================================================
	/**
	 * このコンテキスト内の全ての未解放デバイスを解放します。
	 * <p>
	 * @param force 強制的に解放する場合 true
	 * @throws USBException 解放に失敗した場合
	*/
	private void clearDevices(boolean force) throws USBException{

		// コンテキストに残っている全てのデバイスを解放
		// ※デバイス解放で DEVICES の remove() が実行されるためキーのコピーを生成
		synchronized(devices){
			for(DeviceImpl device: new ArrayList<DeviceImpl>(devices)){
				if(! force){
					device.release();
					continue;
				}
				logger.warning("unreleased device: " + device);
				try{
					device.release();
				} catch(USBException ex){
					logger.log(Level.WARNING, "fail to release device", ex);
				}
			}
		}
		return;
	}

	// ======================================================================
	// コンテキストの解放
	// ======================================================================
	/**
	 * 全てのコンテキストを解放します。
	 * <p>
	*/
	private static void shutdown(){
		synchronized(CONTEXT){

			// ※デバイス解放で DEVICES の remove() が実行されるためキーのコピーを生成
			Set<USBContextImpl> sessions = new HashSet<USBContextImpl>(CONTEXT.keySet());

			// 全てのデバイスの解放を呼び出し
			for(USBContextImpl session: sessions){
				logger.warning("unreleased session: " + session);
				try{
					session.dispose();
				} catch(USBException ex){
					logger.log(Level.WARNING, "fail to release device", ex);
				}
			}
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// VendorFilter: ベンダーIDフィルタ
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * ベンダー ID / 製品 ID によるデバイスフィルタです。
	 * <p>
	 */
	private static class VendorFilter implements DeviceFilter{

		// ==================================================================
		// ベンダー ID
		// ==================================================================
		/**
		 * ベンダー ID です。
		 * <p>
		*/
		private final int idVendor;

		// ==================================================================
		// 製品 ID
		// ==================================================================
		/**
		 * 製品 ID です。
		 * <p>
		*/
		private final int idProduct;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * ベンダー ID / 製品 ID を指定して構築を行います。
		 * <p>
		 * @param idVendor ベンダー ID
		 * @param idProduct 製品 ID
		*/
		public VendorFilter(int idVendor, int idProduct){

			// 引数の範囲確認
			if(idVendor > 0xFFFF){
				throw new IllegalArgumentException("idVendor out of range: " + String.format("0x%X", idVendor));
			}
			if(idProduct > 0xFFFF){
				throw new IllegalArgumentException("idProduct out of range: " + String.format("0x%X", idProduct));
			}

			this.idVendor = idVendor;
			this.idProduct = idProduct;
			return;
		}

		// ==================================================================
		// デバイスの判定
		// ==================================================================
		/**
		 * 指定されたデバイス記述子が該当するデバイスかを判定します。
		 * <p>
		 * @param desc デバイス記述子
		 * @return 該当する場合 true
		*/
		public boolean accept(DeviceDescriptor desc) {
			return (idVendor < 0 || idVendor == desc.getVendorId())
				&& (idProduct < 0 || idProduct == desc.getProductId());
		}

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// VendorFilter: ベンダーIDフィルタ
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * ベンダー ID / 製品 ID によるデバイスフィルタです。
	 * <p>
	 */
	private class ReleaseListener implements DeviceListener{

		// ==================================================================
		// デバイスのオープン
		// ==================================================================
		/**
		 * デバイスがオープンされた時に呼び出されます。
		 * <p>
		 * @param e デバイスイベント
		*/
		public void deviceOpened(DeviceEvent e){
			return;
		}

		// ==================================================================
		// デバイスのクローズ
		// ==================================================================
		/**
		 * デバイスがクローズされた時に呼び出されます。
		 * <p>
		 * @param e デバイスイベント
		*/
		public void deviceClosed(DeviceEvent e){
			return;
		}

		// ==================================================================
		// デバイスの解放
		// ==================================================================
		/**
		 * デバイスが解放された時に呼び出されます。このイベントが通知された時点で該当するデバイス
		 * インスタンスは解放済みです。
		 * <p>
		 * @param e デバイスイベント
		*/
		public void deviceReleased(DeviceEvent e){
			Device device = e.getDevice();
			synchronized(devices){
				devices.remove(device);
			}
			return;
		}

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Shutdown: シャットダウン処理
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * シャットダウン処理を行う為のスレッドです。
	 * <p>
	 */
	private static class Shutdown extends Thread{

		// ======================================================================
		// スレッドの実行
		// ======================================================================
		/**
		 * 未解放の全てのデバイスを解放します。
		 * <p>
		*/
		@Override
		public void run() {
			logger.finest("running shutdown hook");
			shutdown();
			logger.finest("finish shutdown hook");
			return;
		}
	}

}
