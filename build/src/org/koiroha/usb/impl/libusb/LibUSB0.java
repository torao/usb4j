/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: LibUSB0.java,v 1.7 2009/05/14 17:03:56 torao Exp $
*/
package org.koiroha.usb.impl.libusb;

import java.util.logging.Level;

import org.koiroha.usb.USBException;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.impl.USBLibrary;


// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// LibUSB0: libusb 0.1 JNI Interface
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * JNI interface to use libusb 0.1 functions from Java. It is available to
 * use libusb 0.1 functions almost as same. Please refer libusb reference to
 * know description.
 * <p>
 * <ul>
 * <li>Omit {@code usb_} prefix from method names in this library.</li>
 * <li>Omit length parameters because byte-array has its property itself.</li>
 * <li>Pointers are specified as long.</li>
 * <li>Add offset parameter because byte-array cannot increment like pointer.</li>
 * </ul>
 * <p>
 * The native libusb 0.1 is not support multi-thread call because it uses
 * global variable to keep device information. Note that if processes
 * conflict in each threads, the Java VM maybe abort by access violation.
 * <p>
 * @version usb4j 1.0 $Revision: 1.7 $ $Date: 2009/05/14 17:03:56 $
 * @author torao
 * @since 2009/05/01 Java2 SE 5.0
 */
public final class LibUSB0 extends USBLibrary{

	// ======================================================================
	// Log Output
	// ======================================================================
	/**
	 * Log output of this class.
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LibUSB0.class.getName());

	// ======================================================================
	// Library Name
	// ======================================================================
	/**
	 * The native library name {@value} to access libusb 0.1 from Java.
	 */
	public static final String LIBRARY_NAME = "lu04j";

	// ======================================================================
	// Static Initializer
	// ======================================================================
	/**
	 * Load shared library.
	 * <p>
	 */
	static{
		new LibUSB0();		// Load library

		// call initialize method
		init();
		trace("init()");
	}

	// ======================================================================
	// Constructor
	// ======================================================================
	/**
	 * Constructor is hidden in class internal.
	 * <p>
	 */
	private LibUSB0() {
		super(LIBRARY_NAME, "libusb 0.1");
		return;
	}

	// ======================================================================
	// Native Library Version
	// ======================================================================
	/**
	 * Refer the version of interface of native library. The returned 64bit
	 * value means that the  upper 32bit is JNI version, and upper 8bit in
	 * lower 32bit is major version, lower 8bit is minor version. For example,
	 * 0x0001000600000100 returned in library version 1.0 running on JNI 1.6.
	 *
	 * @return interface version
	 */
	@Override
	protected native long nativeInterfaceVersion();

	// ======================================================================
	// Check Error
	// ======================================================================
	/**
	 * Arise exception if specified return code was negative.
	 * <p>
	 * @param ret return code
	 * @throws USBException in case error occured
	 */
	static void checkError(int ret) throws USBException{
		if(ret < 0){
			throw new USBException(String.format("[%d] %s", ret, LibUSB0.strerror()));
		}
		return;
	}

	// ======================================================================
	// Output Trace Log
	// ======================================================================
	/**
	 * Output trace logs.
	 * <p>
	 * @param fmt output format
	 * @param args arguments
	 */
	static void trace(String fmt, Object... args){
		if(logger.isLoggable(Level.FINEST)){
			logger.finest(String.format(fmt, args));
		}
		return;
	}

	// ======================================================================
	// Initialize libusb
	// ======================================================================
	/**
	 * Initialize libusb. This method will be called implicity when class
	 * load, so there is no need to call this from application.
	*/
	public static native void init();

	// ======================================================================
	// Set Debug Level
	// ======================================================================
	/**
	 * Set debug level of libusb. The libusb will output debug log to
	 * standard error ({@code stderr}). Available value is from 0 to about 4
	 * (it's not specified), greater value will output more describe message.
	 * <p>
	 * You can set environment variable {@code USB_DEBUG} to be able to
	 * output debug logging in {@link #init()}.
	 * <p>
	 * @param level debug level
	*/
	public static native void set_debug(int level);

	// ======================================================================
	// Find USB Busses
	// ======================================================================
	/**
	 * Find USB busses connected in system. This method affects the device
	 * informations that will be returned by {@link #get_busses()}.
	 * <p>
	 * @return difference of usb busses number from previous search result
	*/
	public static native int find_busses();

	// ======================================================================
	// Find USB Devices
	// ======================================================================
	/**
	 * Find USB devices connected in system. This method affects the device
	 * informations that will be returned by {@link #get_busses()}. This
	 * method must call after {@link #find_busses()}.
	 * <p>
	 * @return difference of usb devices number from previous search result
	*/
	public static native int find_devices();

	// ======================================================================
	// Retrieve USB Bus
	// ======================================================================
	/**
	 * Retrieve USB bus information that was detected recent
	 * {@link #find_busses()}, {@link #find_devices()} call.
	 * It is able to retrieve all devices from returned bus information.
	 * <p>
	 * @return USB bus
	*/
	public static native Bus get_busses();

	// ======================================================================
	// Retrieve Error Message
	// ======================================================================
	/**
	 * Return message of recent error.
	 * <p>
	 * @return error message
	*/
	public static native String strerror();

	// ======================================================================
	// Open Device
	// ======================================================================
	/**
	 * Open specified device. Return 0 if it failed.
	 * <p>
	 * @param dev device
	 * @return device handle
	*/
	public static native long open(Device dev);

	// ======================================================================
	// Close Device
	// ======================================================================
	/**
	 * Close specified device handle.
	 * <p>
	 * @param handle device handle
	 * @return 0 for success
	*/
	public static native int close(long handle);

	// ======================================================================
	// Refer String
	// ======================================================================
	/**
	 * Refer string descriptor from specified device handle. If this method
	 * success, the binary of string descriptor will stored in {@code buf}.
	 * So, there is {@code bLength} that means length in first byte of
	 * {@code buf}, and 0x03 that means {@code bDescriptorType} is in second
	 * byte, after third bytes are Unicode string.
	 * <!--
	 * 指定されたデバイスハンドルを使用して文字列記述子を参照します。このメソッドが成功した場合、
	 * {@code buf} には文字列記述子のバイナリが格納されます。つまり最初の 1 バイト目に
	 * {@code bLength} が示す長さ、2 バイト目に {@code bDescriptorType} が文字列記述
	 * 子を表す 0x03、3 バイト目以降に Unicode で表された文字列が格納されます。
	 * -->
	 * <p>
	 * But in case 0 specified as {@code index}, 16bit value {@code wLANGID}
	 * that specify {@code langid} that should use as default will stored
	 * ({@code bLength} will be 4). {@code wLANGID} is
	 * {@code java.nio.ByteOrder#LITTLE_ENDIAN little endian} byte order.
	 * <!--
	 * ただし、{@code index} に 0 を指定した場合はデフォルトとして使用すべき
	 * {@code languid} を示す 16bit 値 {@code wLANGID} が格納されます (この場合
	 * {@code bLength} は 4 となります)。{@code wLANGID} は
	 * {@code java.nio.ByteOrder#LITTLE_ENDIAN リトルエンディアン}のバイト順序です。
	 * -->
	 * <p>
	 * @param handle device handle
	 * @param index the index of string descriptor
	 * @param langid language id
	 * @param buf the buffer of storage (more 255 byte)
	 * @return byte length of stored string. negative value if failed
	*/
	public static native int get_string(long handle, int index, int langid, byte[] buf);

	// ======================================================================
	// Refer String
	// ======================================================================
	/**
	 * The utility method to retrieve string descriptor of environment
	 * default language as ISO-8859-1.
	 * デフォルト言語 ID の文字列記述子を ISO-8859-1 で取得するための簡易版 {@link
	 * #get_string(long, int, int, byte[])} です。C 言語では 1 文字 16bit の
	 * Unicode 文字が扱い辛いために用意されています。
	 * <p>
	 * @param handle device handle
	 * @param index the index of string descriptor
	 * @param buf the buffer of storage (more 255 byte)
	 * @return byte length of stored string. negative value if failed
	*/
	public static native int get_string_simple(long handle, int index, byte[] buf);

	// ======================================================================
	// エンドポイント記述子の参照
	// ======================================================================
	/**
	 * デバイスに対して指定されたタイプとインデックスの記述子に対する {@code GET_DESCRIPTOR}
	 * 要求を実行します。このメソッドは ep0 でないコントロール転送を使用して要求を行う場合に使用
	 * します。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param ep 要求を行うエンドポイントの番号
	 * @param type 記述子のタイプ
	 * @param index 文字列記述子のインデックス
	 * @param buf 記述子の格納先バッファ
	 * @return バッファに読み込んだバイト数。失敗した場合は負の値。
	*/
	public static native int get_descriptor_by_endpoint(long handle, int ep, byte type, byte index, byte[] buf);

	// ======================================================================
	// 記述子の参照
	// ======================================================================
	/**
	 * デバイスに対して指定されたタイプとインデックスの記述子に対する {@code GET_DESCRIPTOR}
	 * 要求を実行します。このメソッドはコントロール転送が ep0 の場合の簡易版 {@link
	 * #get_descriptor_by_endpoint(long, int, byte, byte, byte[])} です。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param type 記述子のタイプ
	 * @param index 文字列記述子のインデックス
	 * @param buf 記述子の格納先バッファ
	 * @return バッファに読み込んだバイト数。失敗した場合は負の値。
	*/
	public static native int get_descriptor(long handle, byte type, byte index, byte[] buf);

	// ======================================================================
	// バルク出力
	// ======================================================================
	/**
	 * バルクパイプに対して指定されたバッファの内容を出力します。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param ep エンドポイント番号
	 * @param buf 出力バッファ
	 * @param offset オフセット
	 * @param length 長さ
	 * @param timeout タイムアウト (ミリ秒)
	 * @return 実際に書き込んだバイト数。失敗した場合は負の値。
	*/
	public static native int bulk_write(long handle, int ep, byte[] buf, int offset, int length, int timeout);

	// ======================================================================
	// バルク入力
	// ======================================================================
	/**
	 * バルクパイプから入力を行います。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param ep エンドポイント番号
	 * @param buf 出力バッファ
	 * @param offset オフセット
	 * @param length 長さ
	 * @param timeout タイムアウト (ミリ秒)
	 * @return 実際に読み込んだバイト数。失敗した場合は負の値。
	*/
	public static native int bulk_read(long handle, int ep, byte[] buf, int offset, int length, int timeout);

	// ======================================================================
	// 割り込み出力
	// ======================================================================
	/**
	 * 割り込みパイプに出力を行います。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param ep エンドポイント番号
	 * @param buf 出力バッファ
	 * @param offset オフセット
	 * @param length 長さ
	 * @param timeout タイムアウト (ミリ秒)
	 * @return 実際に書き込んだバイト数。失敗した場合は負の値。
	*/
	public static native int interrupt_write(long handle, int ep, byte[] buf, int offset, int length, int timeout);

	// ======================================================================
	// 割り込み入力
	// ======================================================================
	/**
	 * 割り込みパイプから入力を行います。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param ep エンドポイント番号
	 * @param buf 出力バッファ
	 * @param offset オフセット
	 * @param length 長さ
	 * @param timeout タイムアウト (ミリ病)
	 * @return 実際に読み込んだバイト数。失敗した場合は負の値。
	*/
	public static native int interrupt_read(long handle, int ep, byte[] buf, int offset, int length, int timeout);

	// ======================================================================
	// コントロールメッセージ
	// ======================================================================
	/**
	 * コントロールメッセージの受送信を行います。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param requesttype
	 * @param request
	 * @param value
	 * @param index
	 * @param bytes
	 * @param timeout タイムアウト (ミリ秒)
	 * @return 実際に入出力を行ったバイト数。失敗した場合は負の値。
	*/
	public static native int control_msg(long handle, int requesttype, int request, int value, int index, byte[] bytes, int timeout);

	// ======================================================================
	// コンフィギュレーションの設定
	// ======================================================================
	/**
	 * デバイスのコンフィギュレーションを設定します。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param configuration コンフィギュレーション記述子
	 * @return 成功した場合は 0。失敗した場合は負の値。
	*/
	public static native int set_configuration(long handle, int configuration);

	// ======================================================================
	// Claim Interface
	// ======================================================================
	/**
	 * Claim specified interface.
	 * <p>
	 * @param handle device handle
	 * @param intf interface
	 * @return 0 successfully, negative if failed.
	*/
	public static native int claim_interface(long handle, int intf);

	// ======================================================================
	// Release Interface
	// ======================================================================
	/**
	 * Release specified interface.
	 * <p>
	 * @param handle device handle
	 * @param intf interface to release
	 * @return 0 successfully, negative if failed
	*/
	public static native int release_interface(long handle, int intf);

	// ======================================================================
	// Set AltSettings
	// ======================================================================
	/**
	 * Set alternate settings.
	 * <p>
	 * @param handle device handle
	 * @param alternate alternate settings
	 * @return 0 successfully, negative if failed
	*/
	public static native int set_altinterface(long handle, int alternate);

	// ======================================================================
	// Reset Endpoint
	// ======================================================================
	/**
	 * Reset specified endpoint.
	 * <p>
	 * @param handle device handle
	 * @param ep endpoint
	 * @return 0 successful, negative if failed.
	 * @deprecated Please use {@link #clear_halt(long, int)}.
	*/
	@Deprecated
	public static native int resetep(long handle, int ep);

	// ======================================================================
	// Clear HALT
	// ======================================================================
	/**
	 * Clear HALT status for specified endpoint.
	 * <p>
	 * @param handle device handle
	 * @param ep endpoint
	 * @return 0 normally, negative if failed
	*/
	public static native int clear_halt(long handle, int ep);

	// ======================================================================
	// Reset Device
	// ======================================================================
	/**
	 * Reset specified device.
	 * <p>
	 * @param handle device handle
	 * @return 0 if success, negative if failed
	*/
	public static native int reset(long handle);

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Bus: バスクラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * {@code struct usb_bus} に相当する USB バスを表すクラスです。双方向リンクリスト構造
	 * で表されます。
	 * <p>
	 */
	public static class Bus{
		/** 次のバス情報の参照です。 */
		public Bus next = null;
		/** 前のバス情報の参照です。 */
		public Bus prev = null;
		/** バスの名前です。 */
		public String dirname = null;
		/** USB デバイスの参照です。 */
		public Device devices = null;
		/** ロケーションを表す無符号 32bit 値です。 */
		public int location = 0;
		/** ルートデバイスです。不明な場合は null 値となります。*/
		public Device root_dev = null;
		/** コンストラクタは何も行いません。 */
		Bus(){/* */}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Device: デバイスクラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * {@code struct usb_device} に相当する USB デバイスを表すクラスです。双方向のリンク
	 * リストで表されます。
	 * <p>
	 */
	public static class Device{
		/** このインスタンスに対する struct usb_device へのポインタです。 */
		public long peer = 0;
		/** 次のデバイス情報の参照です。 */
		public Device next = null;
		/** 前のデバイス情報の参照です。 */
		public Device prev = null;
		/** デバイスの名前です。 */
		public String filename = null;
		/** このデバイスの USB バスです。 */
		public Bus bus = null;
		/** デバイス記述子です。 */
		public DeviceDescriptor descriptor = null;
		/** コンフィギュレーションです。 */
		public Configuration[] config = null;
		/**  */
		public long dev = 0;
		/**  */
		public byte devnum = 0;
		/** このデバイスに接続されている別のデバイスの peer 値です。 */
		public long[] children = null;
		/** コンストラクタは何も行いません。 */
		Device(){/* */}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Configuration: コンフィギュレーションクラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * {@code struct usb_device} に相当するコンフィギュレーションのクラスです。
	 * <p>
	 */
	public static class Configuration{
		/** コンフィギュレーション記述子です。情報が取得できない場合は null 値となる可能性があります。 */
		public ConfigurationDescriptor descriptor = null;
		/** このコンフィギュレーションのインターフェースです。 */
		public Interface[] interfaces = null;
		/** 追加の記述子です。 */
		public byte[] extra = null;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Interface: インターフェースクラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * {@code struct usb_interface} に相当する USB インターフェースを表すクラスです。
	 * <p>
	 */
	public static class Interface{
		/** alternate settings of this interface. */
		public AltSetting[] altsetting = null;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// AltSetting: 代替設定クラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * {@code struct usb_interface_descriptor} に相当する USB インターフェースの代替
	 * 設定を表すクラスです。
	 * <p>
	 */
	public static class AltSetting{
		/** Interface descriptor. */
		public InterfaceDescriptor descriptor = null;
		/** endpoints. */
		public Endpoint[] endpoint = null;
		/** extra descriptors. */
		public byte[] extra = null;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Endpoint: エンドポイントクラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * {@code struct usb_endpoint_descriptor} に相当する USB エンドポイントを表す
	 * クラスです。
	 * <p>
	 */
	public static class Endpoint{
		/** Endpoint descriptor. */
		public EndpointDescriptor descriptor = null;
		/** Extra descriptors. */
		public byte[] extra = null;
	}

}
