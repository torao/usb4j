/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: OpenUSBBridge.java,v 1.4 2009/05/21 12:02:55 torao Exp $
*/
package org.koiroha.usb.impl.openusb;

import java.nio.ByteBuffer;
import java.util.*;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.event.IsocTransferEvent;
import org.koiroha.usb.impl.*;
import org.koiroha.usb.impl.openusb.OpenUSB.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// OpenUSBBridge: OpenUSB ブリッジ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * OpenUSB ブリッジ実装です。
 * <p>
 * @version $Revision: 1.4 $ $Date: 2009/05/21 12:02:55 $
 * @author takami torao
 * @since 2009/05/14 Java2 SE 5.0
 */
public class OpenUSBBridge implements USBBridge {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OpenUSBBridge.class.getName());

	// ======================================================================
	// スタティックイニシャライザ
	// ======================================================================
	/**
	 * OpenUSB をロードします。
	 * <p>
	 */
	static {
		OpenUSB.trace("service initialized");
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public OpenUSBBridge() {
		return;
	}

	// ======================================================================
	// ライブラリ名の参照
	// ======================================================================
	/**
	 * この USB サービスのライブラリ名を参照します。
	 * <p>
	 * @return ライブラリ名
	 * @throws USBException ライブラリ名の取得に失敗した場合
	*/
	public String getLibraryName() throws USBException{
		return "OpenUSB 1.0";
	}

	// ======================================================================
	// コンテキストの構築
	// ======================================================================
	/**
	 * 新規のコンテキストを構築します。libusb 0.1 では同時に 2 つ以上のコンテキストを構築する事が
	 * 出来ません。
	 * <p>
	 * @return コンテキスト
	 * @throws USBException コンテキストの構築に失敗した場合
	*/
	public USBContextImpl create() throws USBException{
		logger.finest("create()");
		handle_t handle = OpenUSB.init(0);
		return new OUContext(handle);
	}

	// ======================================================================
	// コンテキストの解放の解放
	// ======================================================================
	/**
	 * 指定されたコンテキストのリソースを解放します。
	 * <p>
	 * @param session コンテキスト実装
	 * @throws USBException コンテキストの解放に失敗した場合
	 * @see USBContext#dispose()
	*/
	public void release(USBContextImpl session) throws USBException{
		synchronized(session){
			OUContext s = (OUContext)session;
			OpenUSB.free_devid_list(s.devids);
			OpenUSB.fini(s.handle);
		}
		return;
	}

	// ======================================================================
	// デバイスの検索
	// ======================================================================
	/**
	 * このブリッジの定義する方法でデバイスを検索し論理トポロジーを構築して返します。
	 * <p>
	 * @param session USB コンテキスト
	 * @return バス実装の一覧
	 * @throws USBException USB デバイスの取得に失敗した場合
	*/
	public List<BusImpl> find(USBContextImpl session) throws USBException{
		logger.finest("find()");

		List<BusImpl> bus = new ArrayList<BusImpl>();
		handle_t handle = ((OUContext)session).handle;
		synchronized(session){

			if(((OUContext)session).devids != null){
				OpenUSB.free_devid_list(((OUContext)session).devids);
			}

			// デバイス一覧を取得
			devid_array_ref devids = OpenUSB.get_devids_by_vendor(handle, -1, -1);
			((OUContext)session).devids = devids;
			Map<Long,BusImpl> busMap = new HashMap<Long, BusImpl>();
			Map<Long,OUDevice> map = new HashMap<Long, OUDevice>();
			for(int i=0; i<devids.value.length; i++){
				long devid = devids.value[i];

				// デバイスデータを取得
				dev_data_ref ref = OpenUSB.get_device_data(handle, devid, 0);
				dev_data_t data = ref.value;
				OpenUSB.free_device_data(ref);

				// デバイスを構築
				DeviceDescriptor desc = data.dev_desc;
				ByteBuffer[] confBinary = new ByteBuffer[desc.getNumConfigurations()];
				confBinary[0] = ByteBuffer.wrap(data.raw_cfg_desc);
				OUDevice device = new OUDevice(this, desc, confBinary, (OUContext)session, devid, data);
				map.put(devid, device);

				// バスを構築
				long busid = data.busid;
				if(! busMap.containsKey(busid)){
					BusImpl b = new BusImpl(data.bus_path);
					bus.add(b);
					busMap.put(busid, b);
				}
			}

			// デバイスのトポロジー構造を構築
			for(OUDevice device: map.values()){
				long pdevid = device.data.pdevid;
				if(pdevid != 0){
					OUDevice parent = map.get(pdevid);
					parent.connect(device);
				}
			}

			// ルートハブをバスに接続
			for(OUDevice device: map.values()){
				if(device.getParentDevice() == null){
					BusImpl busImpl = busMap.get(device.data.busid);
					assert(busImpl != null);
					busImpl.connect(device);
				}
			}
		}
		return bus;
	}

	// ======================================================================
	// デバイスのオープン
	// ======================================================================
	/**
	 * デバイスをオープンします。
	 * <p>
	 * @param device オープンするデバイス
	 * @return デバイスハンドル
	 * @throws USBException デバイスのオープンに失敗した場合
	*/
	public Object open(DeviceImpl device) throws USBException{
		OUDevice dev = (OUDevice)device;
		OUContext session = dev.session;
		return OpenUSB.open_device(session.handle, dev.devid, OpenUSB.INIT_DEFAULT);
	}

	// ======================================================================
	// デバイスのクローズ
	// ======================================================================
	/**
	 * デバイスをクローズします。
	 * <p>
	 * @param device デバイス実装
	 * @throws USBException デバイスのクローズに失敗した場合
	*/
	public void close(DeviceImpl device) throws USBException{
		dev_handle_t handle = (dev_handle_t)device.getOpenedHandle();
		OpenUSB.close_device(handle);
		return;
	}

	// ======================================================================
	// デバイスのリセット
	// ======================================================================
	/**
	 * デバイスをリセットします。
	 * <p>
	 * @param device デバイス実装
	 * @throws USBException デバイスのリセットに失敗した場合
	*/
	public void reset(DeviceImpl device) throws USBException{
		dev_handle_t handle = (dev_handle_t)device.getOpenedHandle();
		OpenUSB.reset(handle);
		return;
	}

	// ======================================================================
	// デバイスの解放
	// ======================================================================
	/**
	 * 指定されたデバイスのリソースを解放します。
	 * <p>
	 * @param device デバイス
	 * @throws USBException デバイスの解放に失敗した場合
	*/
	public void release(DeviceImpl device) throws USBException{
		/* do nothing */
		return;
	}

	// ======================================================================
	// インターフェースの要求
	// ======================================================================
	/**
	 * インターフェースを要求します。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @throws USBException インターフェースの要求に失敗した場合
	*/
	public void claim(DeviceImpl device, byte ifc) throws USBException{
		dev_handle_t handle = (dev_handle_t)device.getOpenedHandle();
		OpenUSB.claim_interface(handle, ifc, OpenUSB.INIT_DEFAULT);
		return;
	}

	// ======================================================================
	// インターフェースの解放
	// ======================================================================
	/**
	 * インターフェースを解放します。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @throws USBException インターフェースの解放に失敗した場合
	*/
	public void release(DeviceImpl device, byte ifc) throws USBException{
		dev_handle_t handle = (dev_handle_t)device.getOpenedHandle();
		OpenUSB.release_interface(handle, ifc);
		return;
	}

	// ======================================================================
	// コントロール転送の実行
	// ======================================================================
	/**
	 * コントロール転送を実行します。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @param ept エンドポイントインデックス
	 * @param request リクエスト
	 * @param timeout タイムアウト (ミリ秒)
	 * @return 実際に転送されたバイト数
	 * @throws USBException コントロール転送に失敗した場合
	*/
	public int controlTransfer(DeviceImpl device, byte ifc, byte ept, ControlRequest request, int timeout) throws USBException{
		dev_handle_t handle = (dev_handle_t)device.getOpenedHandle();

		// コントロール転送の実行
		ctrl_request_t ctrl = new ctrl_request_t(
			(byte)request.getRequestType(), (byte)request.getRequest(),
			(short)request.getValue(), (short)request.getIndex(), request.getRawBuffer(), timeout, 0);
		OpenUSB.ctrl_xfer(handle, ifc, ept, new ctrl_request_t[]{ctrl});
		return ctrl.result.transferred_bytes;
	}

	// ======================================================================
	// 割り込み転送の実行
	// ======================================================================
	/**
	 * 割り込み転送を実行します。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @param ept エンドポイントインデックス
	 * @param buffer データのバッファ
	 * @param offset バッファの入出力開始位置
	 * @param length 入出力データ長
	 * @param inout 入力/出力識別用
	 * @param timeout 入出力タイムアウト (ミリ秒)
	 * @return 実際に転送されたバイト数
	 * @throws USBException コントロール転送に失敗した場合
	*/
	public int interruptTransfer(DeviceImpl device, byte ifc, byte ept,
		byte[] buffer, int offset, int length, Direction inout, int timeout) throws USBException{
		dev_handle_t handle = (dev_handle_t)device.getOpenedHandle();

		// 出力データのコピー
		byte[] payload = new byte[length];
		if(inout == Direction.OUT){
			System.arraycopy(buffer, offset, payload, 0, length);
		}

		// 割り込み転送の実行
		timeout = (timeout < 0)? (int)0xFFFFFFFFL: timeout;
		intr_request_t intr = new intr_request_t(payload, timeout, 0, (short)0);
		OpenUSB.intr_xfer(handle, ifc, ept, new intr_request_t[]{intr});

		// 入力データのコピー
		if(inout == Direction.IN){
			System.arraycopy(payload, 0, buffer, offset, intr.result.transferred_bytes);
		}
		return intr.result.transferred_bytes;
	}

	// ======================================================================
	// バルク転送の実行
	// ======================================================================
	/**
	 * バルク転送を実行します。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @param ept エンドポイントインデックス
	 * @param buffer データのバッファ
	 * @param offset バッファの入出力開始位置
	 * @param length 入出力データ長
	 * @param inout 入力/出力識別用
	 * @param timeout 入出力タイムアウト (ミリ秒)
	 * @return 実際に転送されたバイト数
	 * @throws USBException バルク転送に失敗した場合
	*/
	public int bulkTransfer(DeviceImpl device, byte ifc, byte ept,
		byte[] buffer, int offset, int length, Direction inout, int timeout) throws USBException{
		dev_handle_t handle = (dev_handle_t)device.getOpenedHandle();

		// 出力データのコピー
		byte[] payload = new byte[length];
		if(inout == Direction.OUT){
			System.arraycopy(buffer, offset, payload, 0, length);
		}

		// バルク転送の実行
		timeout = (timeout < 0)? (int)0xFFFFFFFFL: timeout;
		bulk_request_t bulk = new bulk_request_t(payload, timeout, 0);
		OpenUSB.bulk_xfer(handle, ifc, ept, new bulk_request_t[]{bulk});

		// 入力データのコピー
		if(inout == Direction.IN){
			System.arraycopy(payload, 0, buffer, offset, bulk.result.transferred_bytes);
		}
		return bulk.result.transferred_bytes;
	}

	// ======================================================================
	// 等時間隔転送の実行
	// ======================================================================
	/**
	 * 等時間隔転送を実行します。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @param ept エンドポイントインデックス
	 * @param event 等時間隔転送イベント
	 * @throws USBException バルク転送に失敗した場合
	*/
	public void isochronousTransfer(DeviceImpl device, byte ifc, byte ept, IsocTransferEvent event) throws USBException{
		dev_handle_t handle = (dev_handle_t)device.getOpenedHandle();

		// 等時間隔転送の実行
		isoc_request_t isoc = new isoc_request_t(
			(int)event.getStartFrame(), 0, event.getFrameSize(), 0);
		for(int i=0; i<isoc.frames.length; i++){
			isoc.frames[i] = new isoc_pkts(event.getBuffer()[i]);
		}
		OpenUSB.isoc_xfer(handle, ifc, ept, new isoc_request_t[]{isoc});

		// 結果の設定
		int[] length = event.getLength();
		boolean[] error = event.getError();
		for(int i=0; i<isoc.frames.length; i++){
			length[i] = isoc.frames[i].result.transferred_bytes;
			error[i] = (isoc.frames[i].result.status != 0);
		}

		return;
	}

	// ======================================================================
	// エンドポイントのリセット
	// ======================================================================
	/**
	 * エンドポイントをリセットします。
	 * <p>
	 * @param device デバイス実装
	 * @param ifc インターフェース番号 {@code bInterfaceNumber}
	 * @param ept エンドポイント番号
	 * @throws USBException エンドポイントのリセットに失敗した場合
	*/
	public void clearHalt(DeviceImpl device, byte ifc, byte ept) throws USBException{
		logger.fine("clearHalt() not supported for OpenUSB");
		return;
	}

	// ======================================================================
	// デバイスリクエストの実行
	// ======================================================================
	/**
	 * デバイスリクエストを実行します。
	 * <p>
	 * @param device デバイス実装
	 * @param request リクエスト
	 * @return 実際に行われた入出力データサイズ
	 * @throws USBException デバイスリクエストに失敗した場合
	*/
	public int deviceRequest(DeviceImpl device, ControlRequest request) throws USBException {

//		int req = request.getRequest();
//		byte[] buffer = request.getRawBuffer();
//		dev_handle_t handle = (dev_handle_t)device.getOpenedHandle();
//		switch(req){
//		case DeviceRequest.GET_CONFIGURATION:
//			buffer[0] = OpenUSB.get_configuration(handle);
//			return 1;
//		case DeviceRequest.SET_CONFIGURATION:
//			OpenUSB.set_configuration(handle, buffer[0]);
//			return 1;
//		}

		// コントロール転送を実行 FIXME それぞれの固定値は決め討ちで良いのか?
		return controlTransfer(device, (byte)0, (byte)0, request, 1000);
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// OUContext: コンテキスト
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 全てのデバイスがクローズされた時に OpenUSB ハンドルを解放するためのコンテキストです。
	 * <p>
	 */
	private class OUContext extends USBContextImpl{

		// ==================================================================
		// OpenUSB ハンドル
		// ==================================================================
		/**
		 * OpenUSB のハンドルです。
		 * <p>
		 */
		private final handle_t handle;

		// ==================================================================
		// デバイス ID リスト
		// ==================================================================
		/**
		 * デバイス ID リストです
		 * <p>
		 */
		private devid_array_ref devids = null;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * コンストラクタは何も行いません。
		 * <p>
		 * @param handle ハンドル
		 */
		public OUContext(handle_t handle) {
			super(OpenUSBBridge.this);
			this.handle = handle;
			return;
		}

		// ==================================================================
		// ハンドルの参照
		// ==================================================================
		/**
		 * このコンテキストのハンドルを参照します。
		 * <p>
		 * @return ハンドル
		 */
		public handle_t getHandle(){
			return handle;
		}

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// OUDevice: OpenUSB デバイス実装
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * デバイス情報を保持するデバイス実装です。
	 * <p>
	 */
	private static class OUDevice extends DeviceImpl{

		/** このデバイスのコンテキストです。*/
		private final OUContext session;

		/** このデバイス ID です。*/
		private final long devid;

		/** デバイスデータです。*/
		private final dev_data_t data;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * <p>
		 * @param driver ブリッジ
		 * @param desc デバイス記述子
		 * @param conf コンフィギュレーション記述子のバイナリ
		 * @param session コンテキスト
		 * @param devid デバイス ID
		 * @param data デバイスデータ
		 */
		public OUDevice(USBBridge driver, DeviceDescriptor desc, ByteBuffer[] conf, OUContext session, long devid, dev_data_t data){
			super(driver, desc, conf);
			this.session = session;
			this.devid = devid;
			this.data = data;
			return;
		}

	}

}
