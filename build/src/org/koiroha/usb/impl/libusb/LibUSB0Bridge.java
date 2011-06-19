/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: LibUSB0Bridge.java,v 1.5 2009/05/21 12:02:55 torao Exp $
*/
package org.koiroha.usb.impl.libusb;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.*;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.event.IsocTransferEvent;
import org.koiroha.usb.impl.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LibUSB0Bridge: libusb 0.1 USB ブリッジ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * libusb 0.1 を使用する USB サービス実装です。
 * <p>
 * @version usb4j 1.0 $Revision: 1.5 $ $Date: 2009/05/21 12:02:55 $
 * @author torao
 * @since 2009/05/01 Java2 SE 5.0
 */
public class LibUSB0Bridge implements USBBridge{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LibUSB0Bridge.class.getName());

	// ======================================================================
	// 現在のコンテキスト
	// ======================================================================
	/**
	 * 現在使用されているコンテキストです。
	 * <p>
	 */
	private static LUContext CURRENT_SESSION = null;

	// ======================================================================
	// コンテキスト解放シグナル
	// ======================================================================
	/**
	 * コンテキスト会報を通知するためのシグナルです。
	 * <p>
	 */
	private static final Object SIGNAL = new Object();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public LibUSB0Bridge() {
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
		return "libusb 0.1";
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
		synchronized(SIGNAL){
			if(CURRENT_SESSION != null){

				// 未解放のコンテキストが残っている場合は GC を実行して解放を待機する
				logger.warning("unreleased session remaining. libusb can handle only one");
				logger.warning("usb session for process.");
				logger.warning("trying gc...");
				System.gc();
				try{
					SIGNAL.wait(3000);
				} catch(InterruptedException ex){/* */}

				// GC を実行してもコンテキストを解放できなかった場合は例外
				if(CURRENT_SESSION != null){
					throw new USBException("unreleased device remaining; see logging for detail");
				}
			}

			// 新規のコンテキストを構築して返す
			return new LUContext();
		}
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

		List<BusImpl> list = new ArrayList<BusImpl>();
		synchronized(session){

			// デバイスの検索
			int ret = LibUSB0.find_busses();
			LibUSB0.trace("find_busses():=%d", ret);
			ret = LibUSB0.find_devices();
			LibUSB0.trace("find_devices():=%d", ret);

			// バスからデバイスを構築してリストに格納
			LibUSB0.Bus bus = LibUSB0.get_busses();
			LibUSB0.trace("get_busses():=%s", bus);
			while(bus != null){

				// 接続デバイスの一覧を作成
				List<LUDevice> devices = new ArrayList<LUDevice>();
				Map<Long,LUDevice> map = new HashMap<Long, LUDevice>();
				LibUSB0.Device dev = bus.devices;
				while(dev != null){
					ByteBuffer[] conf = makeBinary(dev.descriptor.getNumConfigurations(), dev.config);
					LUDevice devImpl = new LUDevice(this, dev.descriptor, conf, dev);
					devices.add(devImpl);
					map.put(dev.peer, devImpl);
					dev = dev.next;
				}

				// デバイスのトポロジー構造を作成
				for(LUDevice devImpl: devices){
					long[] child = devImpl.dev.children;
					for(int i=0; child!=null && i<child.length; i++){
						LUDevice childDevice = map.get(child[i]);
						if(childDevice != null){
							devImpl.connect(childDevice);
						}
					}
				}

				// バスとルートハブを構築
				BusImpl b = new BusImpl(bus.dirname);
				for(LUDevice devImpl: devices){
					if(devImpl.getParentDevice() == null){
						b.connect(devImpl);
					}
				}

				// リストに格納し次のバスへ移動
				list.add(b);
				bus = bus.next;
			}
		}

		return list;
	}

	// ======================================================================
	// コンテキストの解放の解放
	// ======================================================================
	/**
	 * 指定されたコンテキストのリソースを解放します。次のコンテキストが作成可能な状態に初期化します。
	 * <p>
	 * @param session コンテキスト実装
	 * @throws USBException コンテキストの解放に失敗した場合
	 * @see USBContext#dispose()
	*/
	public void release(USBContextImpl session) throws USBException{
		synchronized(SIGNAL){
			assert(CURRENT_SESSION == session);
			CURRENT_SESSION = null;
			SIGNAL.notify();
		}
		return;
	}

	// ======================================================================
	// デバイスのオープン
	// ======================================================================
	/**
	 * デバイスをオープンします。
	 * <p>
	 * @param device オープンするデバイス実装
	 * @return デバイスハンドル
	 * @throws USBException デバイスのオープンに失敗した場合
	*/
	public Object open(DeviceImpl device) throws USBException{
		long handle = LibUSB0.open(((LUDevice)device).dev);
		LibUSB0.trace("open(dev):=0x%X", handle);
		if(handle == 0){
			throw new USBException(LibUSB0.strerror());
		}
		return handle;
	}

	// ======================================================================
	// デバイスのクローズ
	// ======================================================================
	/**
	 * デバイスをクローズします。
	 * <p>
	 * @param device クローズするデバイス実装
	 * @throws USBException デバイスのクローズに失敗した場合
	*/
	public void close(DeviceImpl device) throws USBException{
		long handle = (Long)device.getOpenedHandle();
		LibUSB0.trace("close(0x%X)", handle);
		int ret = LibUSB0.close(handle);
		LibUSB0.checkError(ret);
		return;
	}

	// ======================================================================
	// デバイスのリセット
	// ======================================================================
	/**
	 * デバイスをリセットします。
	 * <p>
	 * @param device リセットするデバイス実装
	 * @throws USBException デバイスのリセットに失敗した場合
	*/
	public void reset(DeviceImpl device) throws USBException{
		long handle = (Long)device.getOpenedHandle();
		int ret = LibUSB0.reset(handle);
		LibUSB0.checkError(ret);
		return;
	}

	// ======================================================================
	// デバイスの解放
	// ======================================================================
	/**
	 * 指定されたデバイスのリソースを解放します。
	 * <p>
	 * @param devid デバイス識別子
	 * @throws USBException デバイスの解放に失敗した場合
	*/
	public void release(DeviceImpl devid) throws USBException{
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
		long handle = (Long)device.getOpenedHandle();
		int ret = LibUSB0.claim_interface(handle, ifc);
		LibUSB0.checkError(ret);
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
		long handle = (Long)device.getOpenedHandle();
		int ret = LibUSB0.release_interface(handle, ifc);
		LibUSB0.checkError(ret);
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
	 * @param timeout 入出力タイムアウト (ミリ秒)
	 * @return 実際に転送されたバイト数
	 * @throws USBException コントロール転送に失敗した場合
	*/
	public int controlTransfer(DeviceImpl device, byte ifc, byte ept, ControlRequest request, int timeout) throws USBException{
		long handle = (Long)device.getOpenedHandle();
		timeout = (timeout < 0)? (int)0xFFFFFFFFL: timeout;
		int ret = LibUSB0.control_msg(
				handle, request.getRequestType(), request.getRequest(),
				request.getValue(), request.getIndex(),
				request.getRawBuffer(), timeout);
		LibUSB0.checkError(ret);
		return ret;
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
		long handle = (Long)device.getOpenedHandle();
		timeout = (timeout < 0)? (int)0xFFFFFFFFL: timeout;
		int ret = 0;
		if(inout == Direction.IN){
			ret = LibUSB0.interrupt_read(handle, ept & 0xFF, buffer, offset, length, timeout);
		} else {
			ret = LibUSB0.interrupt_write(handle, ept & 0xFF, buffer, offset, length, timeout);
		}
		LibUSB0.checkError(ret);

		// タイムアウトの場合は長さ 0 でリターンする
		if(ret == 0){
			throw new TimeoutException("operation timeout");
		}
		return ret;
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
		long handle = (Long)device.getOpenedHandle();
		timeout = (timeout < 0)? (int)0xFFFFFFFFL: timeout;
		int ret = 0;
		if(inout == Direction.IN){
			ret = LibUSB0.bulk_read(handle, ept & 0xFF, buffer, offset, length, timeout);
		} else {
			ret = LibUSB0.bulk_write(handle, ept & 0xFF, buffer, offset, length, timeout);
		}
		LibUSB0.checkError(ret);

		// タイムアウトの場合は長さ 0 でリターンする
		if(ret == 0){
			throw new TimeoutException("operation timeout");
		}
		return ret;
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
		throw new NotImplementedException("isochronous transfer not supported in libusb 0.1");
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
		long dev = (Long)device.getOpenedHandle();
		int ret = LibUSB0.clear_halt(dev, ept & 0xFF);
		LibUSB0.checkError(ret);
		return;
	}

	// ======================================================================
	// デバイスリクエストの実行
	// ======================================================================
	/**
	 * デバイス要求を実行します。
	 * <p>
	 * @param device デバイス実装
	 * @param request リクエスト
	 * @return 実際に入出力が行われたバイト数
	 * @throws USBException
	*/
	public int deviceRequest(DeviceImpl device, ControlRequest request) throws USBException {
		logger.finest("deviceRequest(" + device.getHandle() + "," + request + ")");

//		int req = request.getRequest();
//		int value = request.getValue();
//
//		// 既知の動作
//		int ret = 0;
//		long handle = (Long)device.getOpenedHandle();
//		switch(request.getRequest()){
//		case DeviceRequest.GET_CONFIGURATION:
//			buffer[0] = ((LUDevice)device).conf;
//			return 1;
//		case DeviceRequest.SET_CONFIGURATION:
//			ret = LibUSB0.set_configuration(handle, buffer[0] & 0xFF);
//			LibUSB0.checkError(ret);
//			((LUDevice)device).conf = buffer[0];
//			return 1;
//		case DeviceRequest.GET_INTERFACE:
//			buffer[0] = ((LUDevice)device).alt;
//			return 1;
//		case DeviceRequest.SET_INTERFACE:
//			ret = LibUSB0.set_altinterface(handle, buffer[0] & 0xFF);
//			LibUSB0.checkError(ret);
//			((LUDevice)device).alt = buffer[0];
//			return 1;
//		case DeviceRequest.GET_DESCRIPTOR:
//			ret = LibUSB0.get_descriptor(handle, (byte)((value >> 8) & 0xFF), (byte)(value & 0xFF), buffer);
//			LibUSB0.checkError(ret);
//			return ret;
//		}

		// コントロール転送を実行 FIXME それぞれの固定値は決め討ちで良いのか?
		int len = controlTransfer(device, (byte)0, (byte)0, request, 1000);
		return len;
	}

	// ======================================================================
	// コンフィギュレーション記述子の再構築
	// ======================================================================
	/**
	 * コンフィギュレーション以下の記述子を再構築します。
	 * <p>
	 * @param size 配列サイズ
	 * @param conf 解析済みのコンフィギュレーション記述子
	 * @return 記述子のバイナリ
	*/
	private static ByteBuffer[] makeBinary(int size, LibUSB0.Configuration[] conf){
		ByteBuffer[] array = new ByteBuffer[size];
		for(int i=0; conf != null && i<conf.length; i++){
			if(conf[i] != null){
				array[i] = makeBinary(conf[i]);
			}
		}
		return array;
	}

	// ======================================================================
	// コンフィギュレーション記述子の再構築
	// ======================================================================
	/**
	 * コンフィギュレーション以下の記述子を再構築します。
	 * <p>
	 * @param conf 解析済みのコンフィギュレーション記述子
	 * @return 記述子のバイナリ
	*/
	private static ByteBuffer makeBinary(LibUSB0.Configuration conf){
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		// コンフィギュレーション記述子のバイナリを構築
		ConfigurationDescriptor cd = conf.descriptor;
		write(out, cd.getRawBinary());
		out.write(conf.extra, 0, conf.extra.length);

		// インターフェース記述子のバイナリを構築
		for(int i=0; i<conf.interfaces.length; i++){
			LibUSB0.Interface intf = conf.interfaces[i];
			for(int j=0; j<intf.altsetting.length; j++){
				LibUSB0.AltSetting alt = intf.altsetting[j];
				InterfaceDescriptor id = alt.descriptor;
				write(out, id.getRawBinary());
				out.write(alt.extra, 0, alt.extra.length);

				// エンドポイント記述子のバイナリを構築
				for(int k=0; k<alt.endpoint.length; k++){
					LibUSB0.Endpoint edpt = alt.endpoint[k];
					EndpointDescriptor ed = edpt.descriptor;
					write(out, ed.getRawBinary());
					out.write(edpt.extra, 0, edpt.extra.length);
				}
			}
		}

		return ByteBuffer.wrap(out.toByteArray());
	}

	// ======================================================================
	// バイトバッファの出力
	// ======================================================================
	/**
	 * 指定されたバイトバッファをストリームに出力します。
	 * <p>
	 * @param out 出力先のストリーム
	 * @param buffer 出力するバイトバッファ
	*/
	private static void write(ByteArrayOutputStream out, ByteBuffer buffer){
		byte[] buf = new byte[256];
		while(buffer.remaining() > 0){
			int len = Math.min(buf.length, buffer.remaining());
			buffer.get(buf, 0, len);
			out.write(buf, 0, len);
		}
		return;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// LUContext: libusb コンテキスト実装
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * コンテキスト解放時に次のコンテキストが使用できるよう設定します。
	 * <p>
	 */
	private class LUContext extends USBContextImpl {

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * コンストラクタは何も行いません。
		 * <p>
		 */
		public LUContext() {
			super(LibUSB0Bridge.this);
			return;
		}

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// LUDevice: libusb デバイス実装
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * デバイス情報を保持するデバイス実装です。
	 * <p>
	 */
	private static class LUDevice extends DeviceImpl{

		// ==================================================================
		// デバイス情報
		// ==================================================================
		/**
		 * libusb から取得したデバイス情報です。
		 * <p>
		 */
		private final LibUSB0.Device dev;

		// ==================================================================
		// コンフィギュレーション番号
		// ==================================================================
		/**
		 * 現在のコンフィギュレーション番号です。
		 * <p>
		 */
//		private byte conf = 1;

		// ==================================================================
		// 代替設定番号
		// ==================================================================
		/**
		 * 現在の代替設定の番号です。
		 * <p>
		 */
//		private byte alt = 0;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * <p>
		 * @param driver ブリッジ
		 * @param dev デバイス情報
		 * @param conf コンフィギュレーション記述子
		 * @param desc デバイス記述子
		 */
		public LUDevice(USBBridge driver, DeviceDescriptor desc, ByteBuffer[] conf, LibUSB0.Device dev){
			super(driver, desc, conf);
			this.dev = dev;
			return;
		}
	}

}
