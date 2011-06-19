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
// LibUSB0: libusb 0.1 JNI インターフェース
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Java から libusb 0.1 を利用するための JNI インターフェースです。libusb 0.1 で用意されて
 * いる関数をほぼそのまま利用することが出来ます。メソッドに関する詳細は libusb の同名の関数を
 * 参照してください。
 * <p>
 * 名前空間である {@code usb_} は省略しています。
 * バイト配列はそれ自体が長さも保持してるため length パラメータは省略しています。
 * ポインタは long で示されます。
 * ポインタに加算して開始位置を示せないためオフセットを追加しています。
 * <p>
 * libusb 0.1 はグローバル変数でデバイス情報を保持しているためマルチスレッドでの呼び出しに対応
 * していません。処理が競合するとアクセス違反で Java VM が異常終了する可能性がありますので
 * 十分に注意してください。
 * <p>
 * @version usb4j 1.0 $Revision: 1.7 $ $Date: 2009/05/14 17:03:56 $
 * @author torao
 * @since 2009/05/01 Java2 SE 5.0
 */
public final class LibUSB0 extends USBLibrary{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LibUSB0.class.getName());

	// ======================================================================
	// ライブラリ名
	// ======================================================================
	/**
	 * libusb 0.1 to Java API のネイティブライブラリ名 {@value} を表します。
	 * <p>
	 */
	public static final String LIBRARY_NAME = "lu04j";

	// ======================================================================
	// スタティックイニシャライザ
	// ======================================================================
	/**
	 * JNI 用の共有ライブラリをロードします。
	 * <p>
	 */
	static{
		new LibUSB0();		// ライブラリのロード

		// 初期化メソッドを呼び出す
		init();
		trace("init()");
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタはクラス内に隠蔽されています。
	 * <p>
	 */
	private LibUSB0() {
		super(LIBRARY_NAME, "libusb 0.1");
		return;
	}

	// ======================================================================
	// ネイティブライブラリバージョン
	// ======================================================================
	/**
	 * ネイティブライブラリのインターフェースバージョンを参照します。返値は上位 32bit に JNI
	 * のバージョン、下位 32bit 中上位 8 ビットにメジャーバージョン、下位 8 ビットにマイナー
	 * バージョンとなる 64bit 整数です。たとえば JNI 1.6 ライブラリバージョン 1.0 は
	 * 0x0001000600000100 となります。
	 * <p>
	 * @return インターフェースバージョン
	 */
	@Override
	protected native long nativeInterfaceVersion();

	// ======================================================================
	// エラーの確認
	// ======================================================================
	/**
	 * 指定されたリターンコードを判定して負の値であれば例外を発生させます。
	 * <p>
	 * @param ret リターンコード
	 * @throws USBException エラーが発生している場合
	 */
	static void checkError(int ret) throws USBException{
		if(ret < 0){
			throw new USBException(String.format("[%d] %s", ret, LibUSB0.strerror()));
		}
		return;
	}

	// ======================================================================
	// トレースログの出力
	// ======================================================================
	/**
	 * トレースログを出力します。
	 * <p>
	 * @param fmt フォーマット
	 * @param args 引数
	 */
	static void trace(String fmt, Object... args){
		if(logger.isLoggable(Level.FINEST)){
			logger.finest(String.format(fmt, args));
		}
		return;
	}

	// ======================================================================
	// libusb の初期化
	// ======================================================================
	/**
	 * libusb を初期化します。このメソッドはクラスがロードされた時点で暗黙的に呼び出されるため
	 * アプリケーションから明示的に呼び出す必要はありません。
	 * <p>
	*/
	public static native void init();

	// ======================================================================
	// デバッグレベルの設定
	// ======================================================================
	/**
	 * libusb のデバッグレベルを設定します。libusb のログは標準エラー ({@code stderr})
	 * に出力されます。有効な設定値は 0 から 4 程度で (未定義)、大きな値ほど詳細なメッセージを
	 * 出力します。
	 * <p>
	 * 環境変数 {@code USB_DEBUG} にレベル値を指定する事で {@link #init()} 実行時から
	 * ログ出力が有効になります。
	 * <p>
	 * @param level デバッグレベル
	*/
	public static native void set_debug(int level);

	// ======================================================================
	// USB バスの検索
	// ======================================================================
	/**
	 * システムに接続されている USB バスを検索します。このメソッドは {@link #get_busses()}
	 * で返されるデバイス情報の構成に影響を与えます。
	 * <p>
	 * @return 前回の検索結果から検出した USB バス数の差分
	*/
	public static native int find_busses();

	// ======================================================================
	// USB デバイスの検索
	// ======================================================================
	/**
	 * 接続中の USB デバイスを検索します。このメソッドは {@link #get_busses()} で返される
	 * デバイス情報の構成に影響を与えます。このメソッドは {@link #find_busses()} の後に
	 * 呼び出す必要があります。
	 * <p>
	 * @return 前回の検索結果から検出した USB デバイス数の差分
	*/
	public static native int find_devices();

	// ======================================================================
	// USB バスの取得
	// ======================================================================
	/**
	 * 前回の {@link #find_busses()}, {@link #find_devices()} 呼び出し時に検出した
	 * USB のバス情報を取得します。返値のバス情報からの参照をたどって全てのデバイスを取得する
	 * ことが出来ます。
	 * <p>
	 * @return USB バス
	*/
	public static native Bus get_busses();

	// ======================================================================
	// エラーメッセージの取得
	// ======================================================================
	/**
	 * 直前に発生したエラーのメッセージを取得します。
	 * <p>
	 * @return エラーメッセージ
	*/
	public static native String strerror();

	// ======================================================================
	// デバイスのオープン
	// ======================================================================
	/**
	 * 指定されたデバイスをオープンします。オープンに失敗した場合は 0 を返します。
	 * <p>
	 * @param dev デバイス
	 * @return デバイスハンドル
	*/
	public static native long open(Device dev);

	// ======================================================================
	// デバイスのクローズ
	// ======================================================================
	/**
	 * 指定されたデバイスハンドルをクローズします。
	 * <p>
	 * @param handle デバイスハンドル
	 * @return 成功した場合 0
	*/
	public static native int close(long handle);

	// ======================================================================
	// 文字列の参照
	// ======================================================================
	/**
	 * 指定されたデバイスハンドルを使用して文字列記述子を参照します。このメソッドが成功した場合、
	 * {@code buf} には文字列記述子のバイナリが格納されます。つまり最初の 1 バイト目に
	 * {@code bLength} が示す長さ、2 バイト目に {@code bDescriptorType} が文字列記述
	 * 子を表す 0x03、3 バイト目以降に Unicode で表された文字列が格納されます。
	 * <p>
	 * ただし、{@code index} に 0 を指定した場合はデフォルトとして使用すべき
	 * {@code languid} を示す 16bit 値 {@code wLANGID} が格納されます (この場合
	 * {@code bLength} は 4 となります)。{@code wLANGID} は
	 * {@code java.nio.ByteOrder#LITTLE_ENDIAN リトルエンディアン}のバイト順序です。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param index 文字列記述子のインデックス
	 * @param langid 言語ID
	 * @param buf 格納先のバッファ (255以上)
	 * @return バッファに取得した文字列の長さ。失敗した場合は負の値。
	*/
	public static native int get_string(long handle, int index, int langid, byte[] buf);

	// ======================================================================
	// 文字列の参照
	// ======================================================================
	/**
	 * デフォルト言語 ID の文字列記述子を ISO-8859-1 で取得するための簡易版 {@link
	 * #get_string(long, int, int, byte[])} です。C 言語では 1 文字 16bit の
	 * Unicode 文字が扱い辛いために用意されています。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param index 文字列記述子のインデックス
	 * @param buf 格納先のバッファ (255以上)
	 * @return バッファに取得した文字列の長さ。失敗した場合は負の値。
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
	// インターフェースの要求
	// ======================================================================
	/**
	 * インターフェースを要求します。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param intf インターフェース
	 * @return 成功した場合は 0。失敗した場合は負の値。
	*/
	public static native int claim_interface(long handle, int intf);

	// ======================================================================
	// インターフェースの解放
	// ======================================================================
	/**
	 * インターフェースを解放します。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param intf インターフェース
	 * @return 成功した場合は 0。失敗した場合は負の値。
	*/
	public static native int release_interface(long handle, int intf);

	// ======================================================================
	// 代替設定の設定
	// ======================================================================
	/**
	 * 代替設定を設定します。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param alternate 代替設定
	 * @return 成功した場合は 0。失敗した場合は負の値。
	*/
	public static native int set_altinterface(long handle, int alternate);

	// ======================================================================
	// エンドポイントのリセット
	// ======================================================================
	/**
	 * エンドポイントをリセットします。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param ep エンドポイント
	 * @return 成功した場合は 0。失敗した場合は負の値。
	 * @deprecated {@link #clear_halt(long, int)} を使用してください。
	*/
	@Deprecated
	public static native int resetep(long handle, int ep);

	// ======================================================================
	// HALT のクリア
	// ======================================================================
	/**
	 * 指定されたエンドポイントに対する HALT 状態をクリアします。
	 * <p>
	 * @param handle デバイスハンドル
	 * @param ep エンドポイント
	 * @return 成功した場合は 0。失敗した場合は負の値。
	*/
	public static native int clear_halt(long handle, int ep);

	// ======================================================================
	// デバイスのリセット
	// ======================================================================
	/**
	 * デバイスをリセットします。
	 * <p>
	 * @param handle デバイスハンドル
	 * @return 成功した場合は 0。失敗した場合は負の値。
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
		/** このインターフェースの代替設定です。 */
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
		/** インターフェース記述子です。 */
		public InterfaceDescriptor descriptor = null;
		/** エンドポイントです。 */
		public Endpoint[] endpoint = null;
		/** 追加の記述子です。 */
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
		/** エンドポイント記述子です。 */
		public EndpointDescriptor descriptor = null;
		/** 追加の記述子です。 */
		public byte[] extra = null;
	}

}
