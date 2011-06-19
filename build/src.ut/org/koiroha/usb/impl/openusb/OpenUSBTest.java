/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: OpenUSBTest.java,v 1.7 2009/05/21 12:02:55 torao Exp $
*/
package org.koiroha.usb.impl.openusb;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.*;
import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.impl.openusb.OpenUSB.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// OpenUSBTest:
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 *
 * <p>
 * @version usb4j 1.0 $Revision: 1.7 $ $Date: 2009/05/21 12:02:55 $
 * @author takami torao
 * @since 2009/05/09 Java2 SE 5.0
 */
public class OpenUSBTest extends TestObject{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OpenUSBTest.class.getName());

	// ======================================================================
	// 有効なベンダー ID
	// ======================================================================
	/**
	 * このテストケースを動作させるマシンでテストに使用する USB デバイスのベンダーID/製品IDです。
	 * <p>
	 */
	private static final int[] VENDOR
		= {0x05A9, 0xA511};	// OmniVision Web カメラ

	// ======================================================================
	// 有効なデバイスクラス/サブクラス/プロトコル
	// ======================================================================
	/**
	 * このテストケースを動作させるマシンでテストに使用する USB デバイスのクラス/サブクラス/プロ
	 * トコルです。
	 * <p>
	 */
	private static final short[] CLASS = {0x09, -1, -1};	// ハブデバイス

	// ======================================================================
	// デバッグコールバック
	// ======================================================================
	/**
	 * デバッグコールバックです。
	 * <p>
	 */
	private static final debug_callback_t debug = new debug_callback_t(){
		public void callback(long handle, String msg) {
			logger.fine(msg);
		}
	};

	// ======================================================================
	// ハンドル初期化/終了のテスト
	// ======================================================================
	/**
	 * init/fini 前後での OpenUSB ハンドルを確認します。
	 * <p>
	 * @throws USBException テストに失敗した場合
	 */
	@Ignore
	public void testInitFini() throws USBException{
		logger.info("--------------------------------------------------");

		handle_t handle = OpenUSB.init(0);
		logger.info(String.format("init()実行後のハンドル: 0x%04X", handle));
		assertNotSame(0, handle);
		OpenUSB.fini(handle);
		assertEquals(0, handle.getReference());

		logger.info("●fini()にfini()済みのハンドル指定して例外が発生しない事を確認");
		OpenUSB.fini(handle);

		logger.info("●fini()にnullを指定して例外が発生することを確認");
		try{
			OpenUSB.fini(null);
			fail();
		} catch(NullPointerException ex){
			logger.info(ex.toString());
		}

		return;
	}

	// ======================================================================
	// デバッグコールバック設定のテスト
	// ======================================================================
	/**
	 * デバッグコールバック設定をテストします。
	 * <p>
	 * @throws USBException テストに失敗した場合
	 */
	@Ignore
	public void testDebugCallback() throws USBException{
		logger.info("--------------------------------------------------");

		logger.info("●コールバックに null を指定 (標準エラーに出力)");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, null);
		OpenUSB.fini(handle);
		logger.info("★↑標準エラーにトレースが出力されていることを確認");

		logger.info("●カスタムコールバックを設定 (Logging API に出力)");
		handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);
		OpenUSB.fini(handle);
		logger.info("★↑Logging API にトレースが出力されていることを確認");

		logger.info("●無効はハンドルに対してデバッグコールバックを設定");
		try{
			OpenUSB.set_debug(null, 10, 0, null);
			fail();
		} catch(NullPointerException ex){
			logger.info(ex.toString());
		}

		return;
	}

	// ======================================================================
	// イベントコールバック設定のテスト
	// ======================================================================
	/**
	 * イベントコールバック設定をテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testEventCallback() throws Exception {
		logger.info("--------------------------------------------------");

		final Object signal = new Object();
		OpenUSB.event_callback_t e = new OpenUSB.event_callback_t(){
			public void callback(long handle, long devid, int event, Object arg) {
				logger.info(String.format("callback(0x%04X,%d,%d,%s)", handle, devid, event, arg));
				synchronized(signal){
					signal.notify();
				}
				return;
			}
		};

		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 100, 0, debug);
		OpenUSB.set_event_callback(handle, OpenUSB.EVENT_ATTACH, e, "ATTACH");
		OpenUSB.set_event_callback(handle, OpenUSB.EVENT_REMOVE, e, "REMOVE");
		OpenUSB.set_event_callback(handle, OpenUSB.EVENT_SUSPEND, e, "SUSPEND");
		OpenUSB.set_event_callback(handle, OpenUSB.EVENT_RESUME, e, "RESUME");
		OpenUSB.set_event_callback(handle, OpenUSB.EVENT_HC_ATTACH, e, "HC_ATTACH");
		OpenUSB.set_event_callback(handle, OpenUSB.EVENT_HC_REMOVE, e, "HC_REMOVE");
		OpenUSB.set_event_callback(handle, OpenUSB.EVENT_COLDPLUG_COMPLETED, e, "COLDPLUG_COMPLETED");
		synchronized(signal){
			logger.info("USB イベントコールバック待機中...");
			signal.wait();
		}

		// ATTACH イベントのコールバックを削除してコールバックが行われないことを確認
		OpenUSB.set_event_callback(handle, OpenUSB.EVENT_ATTACH, null, "ATTACH");
		logger.info("USB デバイスを挿入しコールバックが行われないことを確認したら Enter を押してください...");
		System.in.read();

		OpenUSB.fini(handle);

		return;
	}

	// ======================================================================
	// コールドプラグコールバック完了のテスト
	// ======================================================================
	/**
	 * コールドプラグコールバック完了をテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testColdplugCallbacksDone() throws Exception{
		logger.info("--------------------------------------------------");

		OpenUSB.event_callback_t e = new OpenUSB.event_callback_t(){
			public void callback(long handle, long devid, int event, Object arg) {/* */}
		};

		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);
		OpenUSB.set_event_callback(handle, OpenUSB.EVENT_COLDPLUG_COMPLETED, e, "COLDPLUG_COMPLETED");
		OpenUSB.coldplug_callbacks_done(handle);
		OpenUSB.fini(handle);

		return;
	}

	// ======================================================================
	// デフォルトタイムアウト設定のテスト
	// ======================================================================
	/**
	 * デフォルトタイムアウト設定をテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testSetDefaultTimeout() throws Exception {
		logger.info("--------------------------------------------------");

		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);
		OpenUSB.set_default_timeout(handle, OpenUSB.TRANSFER_TYPE_ALL, 1 * 1000);
		OpenUSB.fini(handle);
		return;
	}

	// ======================================================================
	// バス ID リスト参照テスト
	// ======================================================================
	/**
	 * バス ID リスト参照をテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testGetBusID() throws Exception {
		logger.info("--------------------------------------------------");

		logger.info("●nullに対して例外が発生することを確認");
		try{
			OpenUSB.get_busid_list(null);
			fail();
		} catch(NullPointerException ex){
			logger.info(ex.toString());
		}

		logger.info("●インスタンスを初期化してバス ID リストを参照");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 100, 0, debug);
		busid_array_ref busids = OpenUSB.get_busid_list(handle);

		logger.info("●バス ID リストを解放");
		OpenUSB.free_busid_list(busids);
		assertEquals(0, busids.getReference());
		assertNull(busids.value);

		logger.info("●インスタンスを終了");
		OpenUSB.fini(handle);

		logger.info("●終了したインスタンスに使用して例外となることを確認");
		try{
			OpenUSB.get_busid_list(handle);
			fail();
		} catch(USBException ex){
			logger.info(ex.toString());
		}

		logger.info("●解放時に null を渡して例外となることを確認");
		try{
			OpenUSB.free_busid_list(null);
			fail();
		} catch(NullPointerException ex){
			logger.info(ex.toString());
		}

		logger.info("●未解放の busid_t_array で警告ログが発生することを確認");
		handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);
		busids = OpenUSB.get_busid_list(handle);
		OpenUSB.fini(handle);
		busids = null;
		logger.info("★↓未解放の警告ログを確認");
		System.gc();
		try{
			Thread.sleep(500);
		} catch(InterruptedException ex){/* */}

		return;
	}

	// ======================================================================
	// バス ID リスト参照テスト
	// ======================================================================
	/**
	 * バス ID リスト参照をテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testGetDevidsId() throws Exception {
		logger.info("--------------------------------------------------");

		logger.info("●nullに対して例外が発生することを確認");
		try{
			OpenUSB.get_devids_by_bus(null, 0);
			fail();
		} catch(NullPointerException ex){
			logger.info(ex.toString());
		}

		logger.info("●インスタンスを初期化");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 100, 0, debug);

		logger.info("●デバイス ID リストを参照");
		devid_array_ref devids = OpenUSB.get_devids_by_bus(handle, 0);

		logger.info("●デバイス ID リストを解放");
		OpenUSB.free_devid_list(devids);
		assertEquals(0, devids.getReference());
		assertNull(devids.value);

		logger.info("●解放済みの devid_t_array を解放しても例外とならないことを確認");
		OpenUSB.free_devid_list(devids);

		logger.info("●インスタンスを終了");
		OpenUSB.fini(handle);

		logger.info("●終了したハンドルに使用して例外となることを確認");
		try{
			OpenUSB.get_devids_by_bus(handle, 0);
			fail();
		} catch(USBException ex){
			logger.info(ex.toString());
		}

		logger.info("●解放に null を渡して例外となることを確認");
		try{
			OpenUSB.free_devid_list(null);
			fail();
		} catch(NullPointerException ex){
			logger.info(ex.toString());
		}

		logger.info("●バス ID に無効な値を指定して例外が発生する事を確認");
		handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);
		long[] busids = {-1, Long.MAX_VALUE, Long.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, 0x100000000L};
		for(int i=0; i<busids.length; i++){
			try{
				OpenUSB.get_devids_by_bus(handle, busids[i]);
				fail();
			} catch(USBException ex){
				logger.info(ex.toString());
			}
		}
		OpenUSB.fini(handle);

		logger.info("●未解放の devid_t_array で警告ログが発生することを確認");
		handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);
		devids = OpenUSB.get_devids_by_bus(handle, 0);
		OpenUSB.fini(handle);
		devids = null;
		logger.info("↓未解放の警告ログを確認");
		System.gc();
		try{
			Thread.sleep(500);
		} catch(InterruptedException ex){/* */}

		return;
	}

	// ======================================================================
	// バス ID リスト参照テスト
	// ======================================================================
	/**
	 * バス ID リスト参照をテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testGetDevidsIdBusId() throws Exception {
		logger.info("--------------------------------------------------");

		{
			logger.info("●バス ID 指定でデバイスの一覧を参照");
			handle_t handle = OpenUSB.init(0);
			OpenUSB.set_debug(handle, 10, 0, debug);
			busid_array_ref busids = OpenUSB.get_busid_list(handle);
			devid_array_ref devids = OpenUSB.get_devids_by_bus(handle, busids.value[0]);
			assertNotSame(0, devids.getReference());
			assertNotNull(devids.value);
			OpenUSB.free_devid_list(devids);
			OpenUSB.free_busid_list(busids);
			OpenUSB.fini(handle);
		}

		{
			logger.info("●バス ID に無効な値を指定してエラーとなることを確認");
			handle_t handle = OpenUSB.init(0);
			OpenUSB.set_debug(handle, 10, 0, debug);
			long[] busids = {-1, Long.MAX_VALUE, Long.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, 0x100000000L};
			for(int i=0; i<busids.length; i++){
				try{
					OpenUSB.get_devids_by_bus(handle, busids[i]);
					fail();
				} catch(USBException ex){
					logger.info(ex.toString());
				}
			}
			OpenUSB.fini(handle);
		}

		return;
	}

	// ======================================================================
	// バス ID リスト参照テスト
	// ======================================================================
	/**
	 * バス ID リスト参照をテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testGetDevidsIdByVendor() throws Exception {
		logger.info("--------------------------------------------------");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);

		logger.info("●接続されているベンダー ID と製品 ID で参照");
		devid_array_ref devids = OpenUSB.get_devids_by_vendor(handle, VENDOR[0], VENDOR[1]);
		assertNotSame(0, devids.getReference());
		assertNotNull(devids.value);
		logger.info(String.format("[%04X:%04X] 0x%x", VENDOR[0], VENDOR[1], devids.value[0]));
		OpenUSB.free_devid_list(devids);

		logger.info("●ベンダー ID に無効な値を指定してエラーとなることを確認");
		int[] vendor = {-100, Integer.MAX_VALUE, Integer.MIN_VALUE};
		for(int i=0; i<vendor.length; i++){
			try{
				OpenUSB.get_devids_by_vendor(handle, vendor[i], -1);
				fail();
			} catch(USBException ex){
				logger.info(ex.toString());
			}
		}

		OpenUSB.fini(handle);
		return;
	}

	// ======================================================================
	// バス ID リスト参照テスト
	// ======================================================================
	/**
	 * バス ID リスト参照をテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testGetDevidsIdByClass() throws Exception {
		logger.info("--------------------------------------------------");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);

		logger.info("●接続されているデバイスクラスIDで参照 (ハブデバイス)");
		devid_array_ref devids = OpenUSB.get_devids_by_class(handle, CLASS[0], CLASS[1], CLASS[2]);
		assertNotSame(0, devids.getReference());
		assertNotNull(devids.value);
		OpenUSB.free_devid_list(devids);

		logger.info("●デバイスクラスIDに無効な値を指定してエラーとなることを確認");
		short[] devclass = {-100, Short.MAX_VALUE, Short.MIN_VALUE};
		for(int i=0; i<devclass.length; i++){
			try{
				OpenUSB.get_devids_by_class(handle, devclass[i], (short)-1, (short)-1);
				fail();
			} catch(USBException ex){
				logger.info(ex.toString());
			}
		}

		OpenUSB.fini(handle);
		return;
	}

	// ======================================================================
	// デバイスデータの参照
	// ======================================================================
	/**
	 * デバイスデータを参照します。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testGetDeviceData() throws Exception {
		logger.info("--------------------------------------------------");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);
		devid_array_ref devids = OpenUSB.get_devids_by_bus(handle, 0);
		for(long devid: devids.value){
			dev_data_ref data = OpenUSB.get_device_data(handle, devid, OpenUSB.INIT_DEFAULT);
			OpenUSB.free_device_data(data);
		}
		OpenUSB.free_devid_list(devids);
		OpenUSB.fini(handle);
		return;
	}

	// ======================================================================
	// デバイスのオープン/クローズテスト
	// ======================================================================
	/**
	 * デバイスのオープンとクローズをテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testOpenDevice() throws Exception {
		logger.info("--------------------------------------------------");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);

		devid_array_ref devids = OpenUSB.get_devids_by_vendor(handle, VENDOR[0], VENDOR[1]);
		for(long devid: devids.value){

			logger.info("●デバイスのオープン");
			dev_handle_t dev = OpenUSB.open_device(handle, devid, OpenUSB.INIT_DEFAULT);

			logger.info("●別のデバイスハンドルを指定して同一のデバイスをオープンできることを確認");
			dev_handle_t dev2 = OpenUSB.open_device(handle, devid, OpenUSB.INIT_DEFAULT);
			assertNotSame(0, dev2);
			OpenUSB.close_device(dev2);

			logger.info("●デバイスハンドルからデバイス ID を参照");
			long di = OpenUSB.get_devid(dev);
			assertNotSame(0, di);

			logger.info("●デバイスハンドルにnullを指定して例外が発生することを確認");
			try{
				OpenUSB.get_devid(null);
				fail();
			} catch(NullPointerException ex){
				logger.info(ex.toString());
			}

			logger.info("●デバイスハンドルからライブラリのハンドルを参照");
			long h = OpenUSB.get_lib_handle(dev);
			assertEquals(handle, h);

			logger.info("●デバイスのクローズ");
			OpenUSB.close_device(dev);
		}
		OpenUSB.free_devid_list(devids);

		logger.info("●デバイスクローズに無効な値を指定して例外が発生することを確認");
		try{
			OpenUSB.close_device(null);
			fail();
		} catch(NullPointerException ex){
			logger.info(ex.toString());
		}

		OpenUSB.fini(handle);
		return;
	}

	// ======================================================================
	// 最大転送サイズテスト
	// ======================================================================
	/**
	 * 最大転送サイズを参照します。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testGetMaxXferSize() throws Exception {
		logger.info("--------------------------------------------------");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);

		busid_array_ref busids = OpenUSB.get_busid_list(handle);
		int[] types = new int[]{OpenUSB.TRANSFER_TYPE_CONTROL, OpenUSB.TRANSFER_TYPE_INTERRUPT, OpenUSB.TRANSFER_TYPE_BULK, OpenUSB.TRANSFER_TYPE_ISOCHRONOUS};
		for(long busid: busids.value){

			logger.info("●全てのタイプの最大転送サイズを取得できることを確認");
			for(int type: types){
				int bytes = OpenUSB.get_max_xfer_size(handle, busid, type);
				logger.info("bus[" + busid + "] " + bytes + " bytes");
			}
		}
		OpenUSB.free_busid_list(busids);

		logger.info("●不正なバスIDを指定して失敗することを確認");
		try{
			OpenUSB.get_max_xfer_size(handle, -100, OpenUSB.TRANSFER_TYPE_CONTROL);
			fail();
		} catch(USBException ex){
			logger.info(ex.toString());
		}

		OpenUSB.fini(handle);
		return;
	}

	// ======================================================================
	// 最大転送サイズテスト
	// ======================================================================
	/**
	 * 最大転送サイズを参照します。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testInterface() throws Exception {
		logger.info("--------------------------------------------------");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);

		devid_array_ref devids = OpenUSB.get_devids_by_vendor(handle, VENDOR[0], VENDOR[1]);
		for(long devid: devids.value){
			dev_handle_t dev = OpenUSB.open_device(handle, devid, OpenUSB.INIT_DEFAULT);

			logger.info("●コンフィギュレーションの取得");
			byte cfg = OpenUSB.get_configuration(dev);
			logger.info("現在のコンフィギュレーション: " + cfg);

			// ※複数のコンフィギュレーションを持つデバイスがほとんど無いため省略
			// ※同一のコンフィギュレーションを設定すると BUSY で例外となる
			logger.info("●コンフィギュレーションの設定");
			try{
				OpenUSB.set_configuration(dev, cfg);
				fail();
			} catch(ResourceBusyException ex){
				logger.info(ex.toString());
			}

			logger.info("●無効なコンフィギュレーションを設定");
			try{
				OpenUSB.set_configuration(dev, (byte)(cfg + 5));
				fail();
			} catch(USBException ex){
				logger.info(ex.toString());
			}

			OpenUSB.close_device(dev);
		}
		OpenUSB.free_devid_list(devids);

		logger.info("●デバイスハンドルにnullを指定して失敗することを確認");
		try{
			OpenUSB.get_configuration(null);
			fail();
		} catch(NullPointerException ex){
			logger.info(ex.toString());
		}

		OpenUSB.fini(handle);
		return;
	}

	// ======================================================================
	// 最大転送サイズテスト
	// ======================================================================
	/**
	 * 最大転送サイズを参照します。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Ignore
	public void testDescriptor() throws Exception {
		logger.info("--------------------------------------------------");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);

		devid_array_ref devids = OpenUSB.get_devids_by_vendor(handle, VENDOR[0], VENDOR[1]);
		for(long devid: devids.value){

			logger.info("●デバイス記述子を参照 (低レベル版)");
			ByteBuffer buffer = OpenUSB.get_raw_desc(handle, devid, Descriptor.TYPE_DEVICE, (byte)0, (short)0);
			DeviceDescriptor devdesc = new DeviceDescriptor(buffer);
			logger.info("長さ: " + buffer.limit() + " bytes");
			logger.info("デバイス記述子: " + devdesc);

			logger.info("●デバイス記述子を参照 (高レベル版/バッファ指定)");
			buffer.position(0);
			DeviceDescriptor devdesc2 = OpenUSB.parse_device_desc(handle, devid, buffer);
			logger.info("デバイス記述子: " + devdesc2);
			assertEquals(devdesc, devdesc2);

			logger.info("●デバイス記述子を参照 (高レベル版/独自指定)");
			DeviceDescriptor devdesc3 = OpenUSB.parse_device_desc(handle, devid, null);
			logger.info("デバイス記述子: " + devdesc3);
			assertEquals(devdesc, devdesc3);

			logger.info("●コンフィギュレーション記述子を参照 (低レベル版)");
			buffer = OpenUSB.get_raw_desc(handle, devid, Descriptor.TYPE_CONFIGURATION, (byte)0, (short)0);
			ConfigurationDescriptor cfgdesc = new ConfigurationDescriptor(buffer);
			logger.info("長さ: " + buffer.limit() + " bytes");
			logger.info("コンフィギュレーション記述子: " + cfgdesc);

			logger.info("●コンフィギュレーション記述子を参照 (高レベル版/バッファ指定)");
			buffer.position(0);
			ConfigurationDescriptor cfgdesc2 = OpenUSB.parse_config_desc(handle, devid, buffer, (byte)0);
			logger.info("コンフィギュレーション記述子: " + cfgdesc2);
			assertEquals(cfgdesc.toString(), cfgdesc2.toString());

			logger.info("●コンフィギュレーション記述子を参照 (高レベル版/独自指定)");
			ConfigurationDescriptor cfgdesc3 = OpenUSB.parse_config_desc(handle, devid, null, (byte)0);
			logger.info("コンフィギュレーション記述子: " + cfgdesc3);
			assertEquals(cfgdesc.toString(), cfgdesc3.toString());

			// ※インターフェース記述子はコンフィギュレーション記述子の一部のため直接生データを参照できない
			// logger.info("●インターフェース記述子を参照 (低レベル版)");
			// buffer = OpenUSB.get_raw_desc(handle, devid, OpenUSB.DESC_TYPE_INTERFACE, (byte)0, (short)0);
			// InterfaceDescriptor ifcdesc = new InterfaceDescriptor(buffer);
			// logger.info("長さ: " + buffer.limit() + " bytes");
			// logger.info("インターフェース記述子: " + ifcdesc);

			// ※コンフィギュレーションのバッファ内容の続きを使用
			logger.info("●インターフェース記述子を参照 (高レベル版/バッファ指定)");
			buffer.position(0);
			InterfaceDescriptor ifcdesc2 = OpenUSB.parse_interface_desc(handle, devid, buffer, (byte)0, (byte)0, (byte)0);
			logger.info("インターフェース記述子: " + ifcdesc2);

			logger.info("●インターフェース記述子を参照 (高レベル版/独自指定)");
			InterfaceDescriptor ifcdesc3 = OpenUSB.parse_interface_desc(handle, devid, null, (byte)0, (byte)0, (byte)0);
			logger.info("インターフェース記述子: " + ifcdesc3);
			assertEquals(ifcdesc2.toString(), ifcdesc3.toString());

			// ※コンフィギュレーションのバッファ内容の続きを使用
			logger.info("●エンドポイント記述子を参照 (高レベル版/バッファ指定)");
			buffer.position(0);
			EndpointDescriptor edpdesc2 = OpenUSB.parse_endpoint_desc(handle, devid, buffer, (byte)0, (byte)0, (byte)0, (byte)0);
			logger.info("エンドポイント記述子: " + edpdesc2);

			logger.info("●エンドポイント記述子を参照 (高レベル版/独自指定)");
			EndpointDescriptor edpdesc3 = OpenUSB.parse_endpoint_desc(handle, devid, null, (byte)0, (byte)0, (byte)0, (byte)0);
			logger.info("エンドポイント記述子: " + edpdesc3);
			assertEquals(edpdesc2.toString(), edpdesc3.toString());
		}
		OpenUSB.free_devid_list(devids);

		logger.info("●不正なバスIDを指定して失敗することを確認");
		try{
			OpenUSB.get_max_xfer_size(handle, -100, OpenUSB.TRANSFER_TYPE_CONTROL);
			fail();
		} catch(USBException ex){
			logger.info(ex.toString());
		}

		OpenUSB.fini(handle);
		return;
	}

	// ======================================================================
	// 同期転送テスト
	// ======================================================================
	/**
	 * 同期転送をテストします。
	 * <p>
	 * @throws Exception テストに失敗した場合
	 */
	@Test
	public void testSyncTransfer() throws Exception {
		logger.info("--------------------------------------------------");
		handle_t handle = OpenUSB.init(0);
		OpenUSB.set_debug(handle, 10, 0, debug);

		devid_array_ref devids = OpenUSB.get_devids_by_vendor(handle, VENDOR[0], VENDOR[1]);
		for(long devid: devids.value){
			dev_handle_t dev = OpenUSB.open_device(handle, devid, OpenUSB.INIT_DEFAULT);
			OpenUSB.claim_interface(dev, (byte)0, OpenUSB.INIT_DEFAULT);

			logger.info("●");
			final Object signal = new Object();
			request_handle_callback_t callback = new request_handle_callback_t(){
				public int callback(request_handle_t handle) {
					logger.info("コールバック取得: " + handle.arg);
					synchronized(signal){
						signal.notify();
					}
					return 0;
				}
			};
			request_handle_t request = new request_handle_t(dev, (byte)0, (byte)0, OpenUSB.TRANSFER_TYPE_CONTROL, callback, "hello, world");
			byte[] buffer = new byte[1024];
			request.ctrl = new ctrl_request_t[]{new ctrl_request_t(
					(byte)(0 << 7),
					ControlRequest.GET_DESCRIPTOR,
					(short)((Descriptor.TYPE_STRING << 8) | 0),
					(short)0,
					buffer, 100, 0)};
			OpenUSB.xfer_wait(request);
//			OpenUSB.xfer_aio(request);
//			request_handle_t t = OpenUSB.poll(new request_handle_t[]{request});
//			logger.info("poll(): " + t);
			synchronized(signal){
				OpenUSB.abort(request);
				logger.info("waiting abort callback...");
				signal.wait();
			}

//			logger.info("●");
//			OpenUSB.ctrl_request_t ctrl = new ctrl_request_t(1);
//			ctrl.bmRequestType = USB.REQTYPE_DEVICE_TO_HOST;
//			ctrl.bRequest = USB.REQ_GET_DESCRIPTOR;
//			ctrl.wIndex = (USB.DESCTYPE_STRING << 8) | 0;
//			ctrl.wValue = 0;
//			ctrl.timeout = 100;
//			ctrl.flags = 0;
//			OpenUSB.ctrl_xfer(dev, 0, 0, new ctrl_request_t[]{ctrl});

			OpenUSB.release_interface(dev, (byte)0);
			OpenUSB.close_device(dev);
		}
		OpenUSB.free_devid_list(devids);
		OpenUSB.fini(handle);
		return;
	}

}
