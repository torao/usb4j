/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: OpenUSB.java,v 1.8 2009/05/15 14:56:10 torao Exp $
*/
package org.koiroha.usb.impl.openusb;

import java.nio.*;
import java.util.*;
import java.util.logging.Level;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.impl.USBLibrary;


// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// OpenUSB: OpenUSB 1.0 JNI インターフェース
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Java から OpenUSB 1.0 API を使用するためのインターフェースです。
 * このクラスは利用互換性のためにオリジナルの C 言語に似た API としています。ただし Java への
 * ポーティングのためにいくつかの変更が加えられています。
 * <p>
 * 実行結果のエラー状態はリターンコードではなく例外として送出されます。またリターンバッファを使用
 * した値取得の代わりに値そのものを {@code return} で返します。API 上の
 * {@code uint8_t}, {@code uint16_t} はそれぞれ {@code byte}, {@code short} で
 * 表されます。これらの値を参照する場合は符号の有無に注意してください。
 * <p>
 * 利用レベルのミスで深刻な問題が発生しないようにチェックやフェールセーフの機構を追加しています。
 * ネイティブ側でのメモリリークや JavaVM の異常終了、他のタスクとのデバイス競合などを回避する
 * ため、未解放のリソースに対してはファイナライザやシャットダウンフックで暗黙的な解放が行われます。
 * このフェールセーフ動作は Logging API に対して警告ログを出力します。
 * <p>
 * Java 環境では {@link ByteBuffer}, {@link ByteOrder} が利用できるためバイト順序変換の
 * ユーティリティ関数の実装を省略しています。また利用意図が不明な {@code start()},
 * {@code stop()} を省略しています (将来のバージョンで追加される可能性があります)。
 * <p>
 * @version usb4j 1.0 $Revision: 1.8 $ $Date: 2009/05/15 14:56:10 $
 * @author takami torao
 * @since 2009/05/06 Java2 SE 5.0
 * @see <a href="http://openusb.sourceforge.net/documents/guide/openusb_guide.html">OpenUSB Developers Guide</a>
 */
public final class OpenUSB extends USBLibrary{

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OpenUSB.class.getName());

	// ======================================================================
	// ライブラリ名
	// ======================================================================
	/**
	 * OpenUSB 0.1 to Java API のネイティブライブラリ名 {@value} を表します。
	 * <p>
	 */
	public static final String LIBRARY_NAME = "ou14j";

	// ======================================================================
	// スタティックイニシャライザ
	// ======================================================================
	/**
	 * JNI 用の共有ライブラリをロードします。
	 * <p>
	 */
	static{

		new OpenUSB();	// ライブラリのロード

		// シャットダウンフックの登録
		Runtime.getRuntime().addShutdownHook(new Shutdown());
	}

	// ======================================================================
	// OpenUSB ハンドルマップ
	// ======================================================================
	/**
	 * Java VM で使用している OpenUSB ハンドルとその付随オブジェクトです。ハンドル値からの
	 * コールバックの参照のために使用します。
	 * <p>
	 */
	private static final Map<Long,Handle> HANDLE
		= Collections.synchronizedMap(new HashMap<Long,Handle>());

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタはクラス内に隠蔽されています。
	 * <p>
	 */
	private OpenUSB() {
		super(LIBRARY_NAME, "OpenUSB 1.0");
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
	// ハンドルの参照
	// ======================================================================
	/**
	 * 指定された OpenUSB ハンドルに対する Handle オブジェクトを参照します。ハンドルに対する
	 * インスタンスが見付からない場合は null を返します。
	 * <p>
	 * @param handle ハンドル
	 * @return ハンドルに対する Handle オブジェクト
	 */
	private static Handle getHandle(long handle){
		return HANDLE.get(Long.valueOf(handle));
	}

	// ======================================================================
	// ハンドルの参照
	// ======================================================================
	/**
	 * 指定された OpenUSB ハンドルに対する Handle オブジェクトを参照します。ハンドルが無効な
	 * 場合は例外が発生します。
	 * <p>
	 * @param handle ハンドル
	 * @return ハンドルに対する Handle オブジェクト
	 * @throws USBException ハンドルが無効な場合
	 */
	private static Handle getValidHandle(handle_t handle) throws USBException{
		handle.verifyAvailable();
		Handle h = getHandle(handle.getReference());
		if(h == null){
			checkException(RET_INVALID_HANDLE);
		}
		return h;
	}

	// ======================================================================
	// 16 進数変換
	// ======================================================================
	/**
	 * 指定された数値を 16 進数文字列に変換して返します。このメソッドはハンドル値をログ出力する
	 * 場合などに使用します。
	 * <p>
	 * @param value 16進数に変換する文字列
	 * @return 16進数表記の文字列
	 */
	private static String hex(long value){
		StringBuilder buffer = new StringBuilder(Long.toHexString(value).toUpperCase());
		while(buffer.length() < 4){
			buffer.insert(0, '0');
		}
		return "0x" + buffer.toString();
	}

	// ######################################################################
	// OpenUSB ネイティブメソッド
	// ######################################################################

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
	// ライブラリの初期化
	// ======================================================================
	/**
	 * ネイティブライブラリから OpenUSB ハンドルを確保します。このメソッドが返した OpenUSB
	 * ハンドルは必ず {@link #fini(handle_t)} を呼び出して解放する必要があります。
	 * <p>
	 * 返値のハンドル及びそれから派生するオペレーションは複数のスレッドで共有することは出来ません。
	 * <p>
	 * @param flags 初期化フラグ (OpenUSB 1.0 のリリースでは使用されてないため 0 を指定)
	 * @return OpenUSB ハンドル
	 * @throws USBException ハンドルの構築に失敗した場合
	 */
	public static handle_t init(int flags) throws USBException{

		// フラグが指定されている場合はログ出力
		if(flags != 0){
			logger.finer("zero recommended for initialization flags in this vesion: " + hex(flags));
		}

		// OpenUSB ハンドルの確保
		long[] handle = new long[1];
		int ret = _init(flags, handle);
		checkException(ret);

		// ハンドルの保存
		HANDLE.put(Long.valueOf(handle[0]), new Handle());

		logger.finer("OpenUSB handle opened: " + hex(handle[0]));
		return new handle_t(handle[0]);
	}

	// ======================================================================
	// ライブラリの終了
	// ======================================================================
	/**
	 * 指定された OpenUSB ハンドルを解放します。既に解放済みのハンドルに対してこのメソッドを呼
	 * び出しても何も行いません。
	 * <p>
	 * OpenUSB のオリジナル実装と異なり、指定されたハンドル上でオープンされているデバイスは暗黙
	 * 的にクローズされます。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @throws USBException ハンドルの解放に失敗した場合
	 */
	public static void fini(handle_t handle) throws USBException{
		long ref = handle.getReference();
		if(ref == 0){
			logger.finer("specified " + handle_t.class.getSimpleName() + " is already released");
			return;
		}

		// 指定されたハンドルに関連づけられたデバイスハンドルを警告付きで全てクローズ
		// ※デバイスハンドルをクローズしないままプロセスを終了すると SIGSEGV が発生する事がある
		Handle h = getValidHandle(handle);
		if(! h.devHandle.isEmpty()){
			// ※解放で Set が変化するためコピーを生成
			Set<Long> devids = new HashSet<Long>(h.devHandle);
			for(long dev: devids){
				logger.warning("unreleased device handle: " + hex(dev) + " on handle: " + hex(ref));
				_close_device(dev);
			}
		}

		// ハンドルの解放
		_fini(ref);

		// ハンドルの解放
		HANDLE.remove(Long.valueOf(ref));

		logger.finer("OpenUSB handle closed: " + hex(ref));
		handle.ref = 0;
		return;
	}

	// ======================================================================
	// デバッグコールバックの設定
	// ======================================================================
	/**
	 * OpenUSB のデバッグレベルを設定します。このメソッドによりネイティブの OpenUSB ライブ
	 * ラリが出力するトレースメッセージを指定されたコールバック実装に渡す事が出来ます。コール
	 * バックに null を指定した場合は {@link System#err} に出力されます。デバッグレベルの
	 * 数値は大きくなるほど詳細なトレースが行われます。
	 * <p>
	 * デバッグコールバックの設定は該当する OpenUSB ハンドルに対して {@link #fini(handle_t)}
	 * が呼び出されるまで有効です。
	 * <p>
	 * デバッグレベルの設定は環境変数 {@code OPENUSB_DEBUG} にレベル値を指定することで置き
	 * 換えることが出来ます。{@link #init(int)} 処理に対しても詳細なトレース情報が必要な場合
	 * はこの環境変数を使用してください。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param level デバッグレベル
	 * @param flags 未使用 (0 を指定)
	 * @param cb デバッグ時に呼び出されるコールバック実装
	 * @throws USBException デバッグコールバックの設定に失敗した場合
	 */
	public static void set_debug(handle_t handle, int level, int flags, debug_callback_t cb) throws USBException{
		handle.verifyAvailable();

		// フラグが指定されている場合は警告をログ出力
		if(flags != 0){
			logger.finer("zero recommended for debug flags in this vesion: " + hex(flags));
		}

		// デバッグトレースのコールバックをハンドルに関連付け
		Handle h = getValidHandle(handle);
		h.debug = cb;

		// デバッグトレースのコールバックを設定
		_set_debug(handle.getReference(), level, flags, (cb != null));
		return;
	}

	// ======================================================================
	// イベントコールバックの設定
	// ======================================================================
	/**
	 * OpenUSB ハンドルに対して指定されたイベントに対するイベントコールバック実装を設定します。
	 * コールバックを中止したい場合はコールバック実装に null を指定します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param event {@code openusb_event_t} に相当するイベント種別 {@code EVENT_XXX}
	 * @param callback コールバック実装
	 * @param arg コールバック実装に渡すオブジェクト
	 * @throws USBException イベントコールバックの設定に失敗した場合
	 */
	public static void set_event_callback(handle_t handle, int event, event_callback_t callback, Object arg) throws USBException{
		handle.verifyAvailable();
		Handle h = getValidHandle(handle);
		int ret = RET_SUCCESS;
		synchronized(h.event){
			if(callback == null){
				h.event.remove(event);
			} else {
				EventCallbackEntry entry = new EventCallbackEntry(callback, arg);
				h.event.put(event, entry);
			}
			ret = _set_event_callback(handle.getReference(), event, (callback != null));
		}
		checkException(ret);
		return;
	}

	// ======================================================================
	// コールドプラグコールバック完了待機
	// ======================================================================
	/**
	 * {@link #EVENT_COLDPLUG_COMPLETED} イベントが終わるまで待機します。
	 * <p>
	 * OpenUSB のソースを見る限り、このメソッドは任意のイベントハンドラを設定するまで単純に
	 * 待機するだけです。これが何を意図して用意された機能かは分かりません。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @throws USBException コールバック完了待機に失敗した場合
	 */
	public static void coldplug_callbacks_done(handle_t handle) throws USBException{
		handle.verifyAvailable();
		_coldplug_callbacks_done(handle.getReference());
		return;
	}

	// ======================================================================
	// デフォルトタイムアウトの設定
	// ======================================================================
	/**
	 * 指定された転送タイプに対するデフォルトのタイムアウト (ミリ秒) を設定します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param type 転送タイプ ({@code TRANSFER_TYPE_XXX}定数)
	 * @param timeout 転送タイプに対するデフォルトのタイムアウト (ミリ秒)
	 * @throws USBException デフォルトタイムアウトの設定に失敗した場合
	 */
	public static void set_default_timeout(handle_t handle, int type, int timeout) throws USBException{
		handle.verifyAvailable();
		int ret = _set_default_timeout(handle.getReference(), type, timeout);
		checkException(ret);
		return;
	}

	// ======================================================================
	// デバッグコールバック
	// ======================================================================
	/**
	 * OpenUSB ライブラリからのデバッグトレースを処理するためのコールバックメソッドです。指定
	 * されたハンドルに対するコールバック実装を呼び出します。
	 * <p>
	 * @param handle ハンドル
	 * @param msg デバッグメッセージ
	 */
	static void _debug_callback(long handle, String msg){

		// 指定された OpenUSB ハンドルから Handle のインスタンスを参照
		Handle h = getHandle(handle);

		// コールバックの実行
		if(h != null){
			debug_callback_t cb = h.debug;
			if(cb != null){
				try{
					cb.callback(handle, msg);
				} catch(Throwable ex){
					if(ex instanceof ThreadDeath){
						throw (ThreadDeath)ex;
					}
					logger.log(Level.SEVERE, "uncaught exception in debug callback", ex);
				}
				return;
			}
		}

		// ハンドルが既に無効となっている場合やデバッグコールバックが設定されていない場合は標準エラーに出力
		// ※コールバック未設定の場合はコールバックが来ないはずだがスレッド間のタイミング問題回避目的
		System.err.println(msg);
		return;
	}

	// ======================================================================
	// イベントコールバックの実行
	// ======================================================================
	/**
	 * OpenUSB ライブラリからのイベントコールバックを受けるためのメソッドです。指定されたハンド
	 * ルとイベントタイプに対するコールバック実装を呼び出します。
	 * <p>
	 * @param handle OpenUSB のハンドル
	 * @param devid デバイス ID
	 * @param event イベントタイプ
	 */
	static void _event_callback(long handle, long devid, int event){

		// OpenUSB ハンドルから Handle を参照
		Handle h = getHandle(handle);

		// ハンドルに対するインスタンスが存在しない場合
		// ※fini() での切り離し直後にホットプラグスレッドからコールバックが行われるケース
		if(h == null){
			logger.finer("unhandled event callback: handle=" + hex(handle) + ", devid=" + hex(devid) + ", event=" + event);
			return;
		}

		synchronized(h.event){
			EventCallbackEntry entry = h.event.get(event);
			if(entry == null){
				logger.finest("no event callback specified: " + event);
				return;
			}

			// イベントコールバックの実行
			try{
				entry.call(handle, devid, event);
			} catch(Throwable ex){
				if(ex instanceof ThreadDeath){
					throw (ThreadDeath)ex;
				}
				logger.log(Level.SEVERE, "uncaught event-callback exception", ex);
			}
		}
		return;
	}

	// ======================================================================
	// ライブラリの初期化
	// ======================================================================
	/**
	 * OpenUSB のインスタンスを初期化します。
	 * <p>
	 * @param flags 初期化フラグ (予約/OpenUSB 1.0 のリリースでは使用されてない)
	 * @param handle OpenUSB ハンドルのリターンバッファ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _init(int flags, long[] handle);

	// ======================================================================
	// ライブラリの終了
	// ======================================================================
	/**
	 * この OpenUSB のインスタンスを終了します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 */
	private static native void _fini(long handle);

	// ======================================================================
	// デバッグレベルの設定
	// ======================================================================
	/**
	 * デバッグレベルを設定します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param level デバッグレベル
	 * @param flags 未使用 (0 を指定してください)
	 * @param enable JavaVM へのコールバックを有効にする場合 true
	 */
	private static native void _set_debug(long handle, int level, int flags, boolean enable);

	// ======================================================================
	// イベントコールバックの設定
	// ======================================================================
	/**
	 * 指定されたイベントに対するコールバックが行われるようにします。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param event {@code openusb_event_t} に相当するイベント種別 {@code EVENT_XXX}
	 * @param callback コールバックを行うようにする場合 true
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _set_event_callback(long handle, int event, boolean callback);

	// ======================================================================
	// コールドプラグコールバック完了待機
	// ======================================================================
	/**
	 * {@link #EVENT_COLDPLUG_COMPLETED} イベントが終わるまで待機します。
	 * <p>
	 * OpenUSB のソースを見る限り、このメソッドは任意のイベントハンドラを設定するまで単純に
	 * 待機するだけです。これが何を意図して用意された機能かは分かりません。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 */
	private static native void _coldplug_callbacks_done(long handle);

	// ======================================================================
	// デフォルトタイムアウトの設定
	// ======================================================================
	/**
	 * 指定された転送タイプに対するデフォルトのタイムアウト (ミリ秒) を設定します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param type 転送タイプ ({@code TRANSFER_TYPE_XXX}定数)
	 * @param timeout 転送タイプに対するデフォルトのタイムアウト (ミリ秒)
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _set_default_timeout(long handle, int type, int timeout);

	// ######################################################################
	// デバイス構成のビュー
	// ######################################################################

	// ======================================================================
	// バス ID リストの参照
	// ======================================================================
	/**
	 * 指定されたハンドルを使用して利用可能なバス ID を参照します。返値の
	 * {@link busid_array_ref#value} には利用可能なバス ID の配列 (64bit値) が格納
	 * されています。
	 * <p>
	 * 返値のインスタンスはネイティブリソースを保持しています。アプリケーションは返値のバス ID
	 * 配列が不用になったら {@link #free_busid_list(busid_array_ref)} を呼び出して
	 * リソースを解放する必要があります。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @return バス ID のリスト
	 * @throws USBException バス ID リストの取得に失敗した場合
	 */
	public static busid_array_ref get_busid_list(handle_t handle) throws USBException{
		handle.verifyAvailable();
		busid_array_ref busids = new busid_array_ref();
		int ret = _get_busid_list(handle.getReference(), busids);
		checkException(ret);
		return busids;
	}

	// ======================================================================
	// バス ID リストの解放
	// ======================================================================
	/**
	 * 指定されたバス ID リストを解放します。既に解放されているバス ID リストに対してこのメソッ
	 * ドを呼び出しても何も行われません。
	 * <p>
	 * @param busids 解放するバス ID のリスト
	 * @throws USBException バス ID リストの解放に失敗した場合
	 */
	public static void free_busid_list(busid_array_ref busids) throws USBException{
		if(! busids.isAlreadyReleased()){
			_free_busid_list(busids);
		} else {
			logger.finer("specified " + busids.getClass().getSimpleName() + " is already released");
		}
		return;
	}

	// ======================================================================
	// デバイス ID リストの参照
	// ======================================================================
	/**
	 * 指定されたバス ID に接続しているデバイスの ID リストを参照します。バス ID に 0 を指定
	 * した場合は全てのバスが対象となります。バス ID が無効な場合は例外が発生します。
	 * <p>
	 * このメソッドで取得したデバイス ID リストネイティブリソースを保持しています。アプリケー
	 * ションはデバイス ID リストが不用になったら {@link #free_devid_list(devid_array_ref)}
	 * を使用してリソースを解放する必要があります。
	 * <p>
	 * このメソッドはオリジナルの挙動と異なり該当するデバイスが見付からない場合
	 * ({@code NULL_LIST}) に例外とせず長さ 0 の配列を持つ {@code dev_array_ref} を
	 * 返します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param busid デバイス ID を取得するバスの ID
	 * @return デバイス ID のリスト
	 * @throws USBException デバイス ID の取得に失敗した場合
	 */
	public static devid_array_ref get_devids_by_bus(handle_t handle, long busid) throws USBException{
		handle.verifyAvailable();
		devid_array_ref devids = new devid_array_ref();
		int ret = _get_devids(handle.getReference(), 0, busid, -1, -1, devids);
		if(ret == RET_NULL_LIST){
			devids.value = new long[0];
			logger.finest("no device found");
		} else {
			checkException(ret);
		}
		return devids;
	}

	// ======================================================================
	// デバイス ID リストの参照
	// ======================================================================
	/**
	 * 指定されたベンダーID/製品IDを持つデバイス ID リストを取得します。{@code vendor},
	 * {@code product} は 0 から 0xFFFF の範囲で指定します。-1 を指定した場合はその全てが
	 * 対象となります。
	 * <p>
	 * このメソッドで取得したデバイス ID リストネイティブリソースを保持しています。アプリケー
	 * ションはデバイス ID リストが不用になったら {@link #free_devid_list(devid_array_ref)}
	 * を使用してリソースを解放する必要があります。
	 * <p>
	 * このメソッドはオリジナルの挙動と異なり該当するデバイスが見付からない場合
	 * ({@code NULL_LIST}) に例外とせず長さ 0 の配列を持つ {@code dev_array_ref} を
	 * 返します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param vendor ベンダー ID
	 * @param product 製品 ID
	 * @return デバイス ID のリスト
	 * @throws USBException デバイス ID の取得に失敗した場合
	 */
	public static devid_array_ref get_devids_by_vendor(handle_t handle, int vendor, int product) throws USBException{
		handle.verifyAvailable();
		devid_array_ref devids = new devid_array_ref();
		int ret = _get_devids(handle.getReference(), 1, vendor, product, -1, devids);
		if(ret == RET_NULL_LIST){
			devids.value = new long[0];
			logger.finest("no device found");
		} else {
			checkException(ret);
		}
		return devids;
	}

	// ======================================================================
	// デバイス ID リストの参照
	// ======================================================================
	/**
	 * 指定されたデバイスクラス/サブクラス/プロトコルに該当するデバイスの ID リストを参照します。
	 * それぞれのパラメータは 0 から 0xFF の範囲で指定します。-1 を指定した場合は全てが対象と
	 * なります。
	 * <p>
	 * このメソッドで取得したデバイス ID リストネイティブリソースを保持しています。アプリケー
	 * ションはデバイス ID リストが不用になったら {@link #free_devid_list(devid_array_ref)}
	 * を使用してリソースを解放する必要があります。
	 * <p>
	 * このメソッドはオリジナルの挙動と異なり該当するデバイスが見付からない場合
	 * ({@code NULL_LIST}) に例外とせず長さ 0 の配列を持つ {@code dev_array_ref} を
	 * 返します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devclass デバイスクラス
	 * @param subclass サブクラス
	 * @param protocol プロトコル
	 * @return デバイス ID のリスト
	 * @throws USBException デバイス ID の取得に失敗した場合
	 */
	public static devid_array_ref get_devids_by_class(handle_t handle, short devclass, short subclass, short protocol) throws USBException{
		handle.verifyAvailable();
		devid_array_ref devids = new devid_array_ref();
		int ret = _get_devids(handle.getReference(), 2, devclass, subclass, protocol, devids);
		if(ret == RET_NULL_LIST){
			devids.value = new long[0];
			logger.finest("no device found");
		} else {
			checkException(ret);
		}
		return devids;
	}

	// ======================================================================
	// デバイス ID リストの解放
	// ======================================================================
	/**
	 * 指定されたデバイス ID リストを解放します。
	 * <p>
	 * @param devids デバイス ID のリスト
	 * @throws USBException デバイス ID リストの解放に失敗した場合
	 */
	public static void free_devid_list(devid_array_ref devids) throws USBException{
		if(! devids.isAlreadyReleased()){
			_free_devid_list(devids);
		} else {
			logger.finer("specified " + devids.getClass().getSimpleName() + " is already released");
		}
		return;
	}

	// ======================================================================
	// デバイスデータの参照
	// ======================================================================
	/**
	 * 指定されたデバイス ID の詳細データを参照します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devid デバイス ID
	 * @param flags 0 (予約)
	 * @return デバイス情報
	 * @throws USBException デバイス情報の取得に失敗した場合
	 */
	public static dev_data_ref get_device_data(handle_t handle, long devid, int flags) throws USBException{
		handle.verifyAvailable();

		// フラグが指定されている場合はログ出力
		if(flags != 0){
			logger.finer("zero recommended for retrieve flags in this vesion: " + hex(flags));
		}

		dev_data_ref data = new dev_data_ref();
		int ret = _get_device_data(handle.getReference(), devid, flags, data);
		checkException(ret);
		return data;
	}

	// ======================================================================
	// デバイスデータの解放
	// ======================================================================
	/**
	 * 指定されたデバイスデータを解放します。デバイスデータが参照を保持していない場合や既に解放
	 * されている場合は何も行いません。
	 * <p>
	 * @param data 解放するデバイス情報
	 * @throws USBException デバイス情報の解放に失敗した場合
	 */
	public static void free_device_data(dev_data_ref data) throws USBException{
		if(! data.isAlreadyReleased()){
			_free_device_data(data);
		} else {
			logger.finer("specified " + data.getClass().getSimpleName() + " is already released");
		}
		return;
	}

	// ======================================================================
	// バス ID の参照
	// ======================================================================
	/**
	 * 利用可能なバス ID を参照します。引数 {@code busids} には利用可能なバス ID の配列が
	 * 格納されます。アプリケーションは {@code busids} が不用になったら必ず
	 * {@link #free_busid_list(busid_array_ref)} で解放する必要があります。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param busids バス ID を格納するリターンバッファ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _get_busid_list(long handle, busid_array_ref busids);

	// ======================================================================
	// バス ID の解放
	// ======================================================================
	/**
	 * 指定されたバス ID を解放します。
	 * <p>
	 * @param busids 解放するバス ID のリスト
	 */
	private static native void _free_busid_list(busid_array_ref busids);

	// ======================================================================
	// デバイス ID リストの参照
	// ======================================================================
	/**
	 * バスに接続されているデバイスの ID リストを参照します。バス ID に 0 を指定した場合は全て
	 * のバスが対象となります。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param mode モード (0:by_bus, 1:by_vendor, 2:by_class)
	 * @param arg1 引数1
	 * @param arg2 引数2
	 * @param arg3 引数3
	 * @param devids デバイス ID のリターンバッファ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _get_devids(long handle, int mode, long arg1, long arg2, long arg3, devid_array_ref devids);

	// ======================================================================
	// デバイス ID リストの解放
	// ======================================================================
	/**
	 * 指定されたデバイス ID リストを解放します。
	 * <p>
	 * @param devids デバイス ID のリスト
	 */
	private static native void _free_devid_list(devid_array_ref devids);

	// ======================================================================
	// デバイスデータの参照
	// ======================================================================
	/**
	 * 指定されたデバイス ID の詳細データを参照します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devid デバイス ID
	 * @param flags 0 (予約)
	 * @param data デバイス情報のリターンバッファ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _get_device_data(long handle, long devid, int flags, dev_data_ref data);

	// ======================================================================
	// デバイスデータの解放
	// ======================================================================
	/**
	 * デバイスデータを解放します。
	 * <p>
	 * @param data デバイス情報
	 */
	private static native void _free_device_data(dev_data_ref data);

	// ######################################################################
	// デバイスオペレーション
	// ######################################################################

	// ======================================================================
	// デバイスのオープン
	// ======================================================================
	/**
	 * 指定されたデバイスをオープンします。返値のデバイスハンドルは
	 * {@link #close_device(dev_handle_t)} を使用してクローズする必要があります。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devid オープンするデバイスの ID
	 * @param flags 初期化フラグ ({@code INIT_XXX})
	 * @return デバイスハンドル
	 * @throws USBException デバイスのオープンに成功した場合
	 */
	public static dev_handle_t open_device(handle_t handle, long devid, int flags) throws USBException{
		handle.verifyAvailable();

		// デバイスのオープン
		long[] dev = new long[1];
		int ret = _open_device(handle.getReference(), devid, flags, dev);
		checkException(ret);

		// デバイスハンドルをハンドルに関連づける
		Handle h = getValidHandle(handle);
		h.devHandle.add(Long.valueOf(dev[0]));

		return new dev_handle_t(dev[0]);
	}

	// ======================================================================
	// デバイスのクローズ
	// ======================================================================
	/**
	 * 指定されたデバイスハンドルをクローズします。デバイスハンドルが既にクローズされている場合は
	 * 何も行いません。
	 * <p>
	 * @param dev クローズするデバイスハンドル
	 * @throws USBException デバイスハンドルのクローズに成功した場合
	 */
	public static void close_device(dev_handle_t dev) throws USBException{

		// デバイスハンドルをクローズ
		int ret = _close_device(dev.getReference());
		checkException(ret);

		// ハンドルとデバイスハンドルの関連を削除
		synchronized(HANDLE){
			Long value = Long.valueOf(dev.getReference());
			for(Handle h: HANDLE.values()){
				if(h.devHandle.remove(value)){
					break;
				}
			}
		}

		dev.ref = 0;
		return;
	}

	// ======================================================================
	// デバイス ID の参照
	// ======================================================================
	/**
	 * 指定されたデバイスハンドルに対するデバイス ID を参照します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @return デバイス ID
	 * @throws USBException ハンドルの参照に失敗した場合
	 */
	public static long get_devid(dev_handle_t dev) throws USBException{
		dev.verifyAvailable();
		long[] devid = new long[1];
		int ret = _get_devid(dev.getReference(), devid);
		checkException(ret);
		return devid[0];
	}

	// ======================================================================
	// ライブラリハンドルの参照
	// ======================================================================
	/**
	 * 指定されたデバイスハンドルに対する OpenUSB のハンドルを参照します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @return OpenUSB ハンドル
	 * @throws USBException ハンドルの参照に失敗した場合
	 */
	public static long get_lib_handle(dev_handle_t dev) throws USBException{
		dev.verifyAvailable();
		long[] lib_handle = new long[1];
		int ret = _get_lib_handle(dev.getReference(), lib_handle);
		checkException(ret);
		return lib_handle[0];
	}

	// ======================================================================
	// 最大転送サイズの参照
	// ======================================================================
	/**
	 * バスと転送タイプに基づくリクエストに対する最大データ転送サイズをバイト単位で参照します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param bus バス ID
	 * @param type {@code openusb_transfer_type_t} に相当する転送タイプ ({@code TRANSFER_TYPE_XXX})
	 * @return 最大転送サイズ
	 * @throws USBException 最大転送サイズの取得に失敗した場合
	 */
	public static int get_max_xfer_size(handle_t handle, long bus, int type) throws USBException{
		handle.verifyAvailable();
		int[] bytes = new int[1];
		int ret = _get_max_xfer_size(handle.getReference(), bus, type, bytes);
		checkException(ret);
		return bytes[0];
	}

	// ======================================================================
	// デバイスのオープン
	// ======================================================================
	/**
	 * 指定されたデバイスをオープンします。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devid デバイス ID
	 * @param flags 初期化フラグ ({@code INIT_XXX})
	 * @param dev デバイスハンドルのリターンバッファ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _open_device(long handle, long devid, int flags, long[] dev);

	// ======================================================================
	// デバイスのクローズ
	// ======================================================================
	/**
	 * 指定されたデバイスをクローズします。
	 * <p>
	 * @param dev クローズするデバイスハンドル
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _close_device(long dev);

	// ======================================================================
	// デバイス ID の参照
	// ======================================================================
	/**
	 * 指定されたデバイスハンドルに対するデバイス ID を参照します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param devid デバイス ID のリターンバッファ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _get_devid(long dev, long[] devid);

	// ======================================================================
	// ライブラリハンドルの参照
	// ======================================================================
	/**
	 * 指定されたデバイスハンドルに対する OpenUSB インスタンスのハンドルを参照します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param lib_handle
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _get_lib_handle(long dev, long[] lib_handle);

	// ======================================================================
	// 最大転送サイズの参照
	// ======================================================================
	/**
	 * リクエストに対するバスと転送タイプに基づく最大データ転送サイズを参照します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param bus バス ID
	 * @param type {@code openusb_transfer_type_t} に相当する転送タイプ ({@code TRANSFER_TYPE_XXX})
	 * @param bytes 最大転送サイズのリターンバッファ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _get_max_xfer_size(long handle, long bus, int type, int[] bytes);

	// ######################################################################
	// コンフィギュレーションオペレーション
	// ######################################################################

	// ======================================================================
	// コンフィギュレーションの参照
	// ======================================================================
	/**
	 * 指定されたデバイスの現在のコンフィギュレーションを設定します。返値は {@code uint8_t}
	 * の範囲となります。
	 * <p>
	 * @param dev デバイスハンドル
	 * @return デバイスの現在のコンフィギュレーション
	 * @throws USBException コンフィギュレーションの参照に失敗した場合
	 */
	public static byte get_configuration(dev_handle_t dev) throws USBException{
		dev.verifyAvailable();
		byte[] cfg = new byte[1];
		int ret = _get_configuration(dev.getReference(), cfg);
		checkException(ret);
		return cfg[0];
	}

	// ======================================================================
	// コンフィギュレーションの設定
	// ======================================================================
	/**
	 * 指定されたデバイスのコンフィギュレーションを設定します。{@code cfg} は {@code uint8_t}
	 * の範囲で指定します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param cfg デバイスに設定するコンフィギュレーション
	 * @throws USBException コンフィギュレーションの参照に失敗した場合
	 */
	public static void set_configuration(dev_handle_t dev, byte cfg) throws USBException{
		dev.verifyAvailable();
		int ret = _set_configuration(dev.getReference(), cfg);
		checkException(ret);
		return;
	}

	// ======================================================================
	// コンフィギュレーションの参照
	// ======================================================================
	/**
	 * 指定されたデバイスの現在のコンフィギュレーションを設定します。返値は {@code uint8_t}
	 * の範囲となります。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param cfg デバイスの現在のコンフィギュレーション
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _get_configuration(long dev, byte[] cfg);

	// ======================================================================
	// コンフィギュレーションの設定
	// ======================================================================
	/**
	 * 指定されたデバイスのコンフィギュレーションを設定します。{@code cfg} は
	 * {@code uint8_t} の範囲で指定します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param cfg デバイスに設定するコンフィギュレーション
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _set_configuration(long dev, byte cfg);

	// ######################################################################
	// インターフェースオペレーション
	// ######################################################################

	// ======================================================================
	// インターフェースの要求
	// ======================================================================
	/**
	 * 指定されたデバイスのインターフェースを要求します。アプリケーションはエンドポイントに対する
	 * 転送操作を行う前に該当するインターフェースに対してこのメソッドを実行する必要があります。
	 * またこのメソッドが成功したインターフェースに対しては
	 * {@link #release_interface(dev_handle_t, byte)} を実行する必要があります。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @param flags 要求フラグ ({@code INIT_XXX} 定数)
	 * @throws USBException インターフェースの要求に失敗した場合
	 */
	public static void claim_interface(dev_handle_t dev, byte ifc, int flags) throws USBException{
		dev.verifyAvailable();
		int ret = _claim_interface(dev.getReference(), ifc, flags);
		checkException(ret);
		return;
	}

	// ======================================================================
	// インターフェースの解放
	// ======================================================================
	/**
	 * 指定されたインターフェースを解放します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @throws USBException インターフェースの解放に失敗した場合
	 */
	public static void release_interface(dev_handle_t dev, byte ifc) throws USBException{
		dev.verifyAvailable();
		int ret = _release_interface(dev.getReference(), ifc);
		checkException(ret);
		return;
	}

	// ======================================================================
	// インターフェースの要求確認
	// ======================================================================
	/**
	 * 指定されたインターフェースが要求されているかどうかを確認します。既に同一 Java VM 内で
	 * 要求が完了している場合に true を返します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @return 指定されたインターフェースが要求済みの場合
	 * @throws USBException インターフェースの解放に失敗した場合
	 */
	public static boolean is_interface_claimed(dev_handle_t dev, byte ifc) throws USBException{
		dev.verifyAvailable();
		return (_is_interface_claimed(dev.getReference(), ifc) != 0);
	}

	// ======================================================================
	// 代替設定の参照
	// ======================================================================
	/**
	 * 指定されたインターフェースの現在の代替設定を参照します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @return インターフェースの代替設定番号
	 * @throws USBException 代替設定の参照に失敗した場合
	 */
	public static byte get_altsetting(dev_handle_t dev, byte ifc) throws USBException{
		dev.verifyAvailable();
		byte[] alt = new byte[1];
		int ret = _get_altsetting(dev.getReference(), ifc, alt);
		checkException(ret);
		return alt[0];
	}

	// ======================================================================
	// 代替設定の設定
	// ======================================================================
	/**
	 * 指定されたインターフェースの代替設定を設定します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @param alt 代替設定番号
	 * @throws USBException 代替設定の設定に失敗した場合
	 */
	public static void set_altsetting(dev_handle_t dev, byte ifc, byte alt) throws USBException{
		dev.verifyAvailable();
		int ret = _set_altsetting(dev.getReference(), ifc, alt);
		checkException(ret);
		return;
	}

	// ======================================================================
	// デバイスのリセット
	// ======================================================================
	/**
	 * 指定されたデバイスをリセットします。
	 * <p>
	 * @param dev デバイスハンドル
	 * @throws USBException デバイスのリセットに失敗した場合
	 */
	public static void reset(dev_handle_t dev) throws USBException{
		dev.verifyAvailable();
		int ret = _reset(dev.getReference());
		checkException(ret);
		return;
	}

	// ======================================================================
	// インターフェースの要求
	// ======================================================================
	/**
	 * 指定されたデバイスのインターフェースを要求します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @param flags 要求フラグ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _claim_interface(long dev, byte ifc, int flags);

	// ======================================================================
	// インターフェースの解放
	// ======================================================================
	/**
	 * 指定されたインターフェースを解放します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _release_interface(long dev, byte ifc);

	// ======================================================================
	// インターフェースの解放
	// ======================================================================
	/**
	 * 指定されたインターフェースを解放します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @return インターフェースが要求済みの場合 0 以外の値
	 */
	private static native int _is_interface_claimed(long dev, byte ifc);

	// ======================================================================
	// 代替設定の参照
	// ======================================================================
	/**
	 * 指定されたインターフェースの現在の代替設定を参照します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @param alt インターフェースの代替設定番号のリターンバッファ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _get_altsetting(long dev, byte ifc, byte[] alt);

	// ======================================================================
	// 代替設定の設定
	// ======================================================================
	/**
	 * 指定されたインターフェースの代替設定を設定します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @param alt 代替設定番号
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _set_altsetting(long dev, byte ifc, byte alt);

	// ======================================================================
	// デバイスのリセット
	// ======================================================================
	/**
	 * 指定されたデバイスをリセットします。
	 * <p>
	 * @param dev デバイスハンドル
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _reset(long dev);

	// ######################################################################
	// 記述子オペレーション
	// ######################################################################

	// ======================================================================
	// 記述子バイナリの参照
	// ======================================================================
	/**
	 * デバイスに対して任意の記述子バイナリを要求します。返値のバイトバッファのバイト順序は
	 * {@link USB#BYTE_ORDER} に設定されており、内容は呼び出し側で変更が可能
	 * です。
	 * <p>
	 * 記述子のタイプには {@link Descriptor#TYPE_DEVICE} または {@link Descriptor#TYPE_CONFIGURATION}
	 * のみを指定することができます。
	 * <p>
	 * OpenUSB 1.0 API で必要な {@code openusb_free_raw_desc(buffer)} を使用した
	 * バッファの解放は不用です。また {@code openusb_parse_xxx_desc()} によるバイナリの
	 * 解析は返値のバイトバッファを {@code org.koiroha.usb.desc} パッケージのそれぞれの
	 * 記述子へ指定する事で代用できます。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devid デバイス ID
	 * @param type 記述子のタイプ
	 * @param descidx 記述子のインデックス
	 * @param langid 記述子の言語 ID (主に文字列記述子用)
	 * @return 記述子のバイナリ
	 * @throws USBException 記述子の取得に失敗した場合
	 */
	public static ByteBuffer get_raw_desc(handle_t handle, long devid, byte type, byte descidx, short langid) throws USBException{
		handle.verifyAvailable();
		byte[][] buffer = new byte[1][];
		int ret = _get_raw_desc(handle.getReference(), devid, type, descidx, langid, buffer);
		checkException(ret);
		ByteBuffer binary = ByteBuffer.wrap(buffer[0]);
		binary.order(USB.BYTE_ORDER);
		return binary;
	}

	// ======================================================================
	// デバイス記述子バイナリの解析
	// ======================================================================
	/**
	 * 指定されたデバイス記述子バイナリを解析します。{@code buffer} に null を指定した場合
	 * は新たにデバイス記述子を取得します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devid デバイス ID
	 * @param buffer 解析するデバイス記述子のバイナリ
	 * @return デバイス記述子
	 * @throws USBException 記述子の取得に失敗した場合
	 */
	public static DeviceDescriptor parse_device_desc(handle_t handle, long devid, ByteBuffer buffer) throws USBException{
		handle.verifyAvailable();
		Descriptor[] desc = new Descriptor[1];
		int ret = _parse_desc(Descriptor.TYPE_DEVICE, handle.getReference(), devid, (buffer==null)? null: buffer.array(), (byte)0, (byte)0, (byte)0, (byte)0, desc);
		checkException(ret);
		return (DeviceDescriptor)desc[0];
	}

	// ======================================================================
	// コンフィギュレーション記述子バイナリの解析
	// ======================================================================
	/**
	 * 指定されたコンフィギュレーション記述子バイナリを解析します。{@code buffer} に null
	 * を指定した場合は新たにコンフィギュレーション記述子を取得します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devid デバイス ID
	 * @param buffer 解析するデバイス記述子のバイナリ
	 * @param cfgidx コンフィギュレーションのインデックス
	 * @return コンフィギュレーション記述子
	 * @throws USBException 記述子の取得に失敗した場合
	 */
	public static ConfigurationDescriptor parse_config_desc(handle_t handle, long devid, ByteBuffer buffer, byte cfgidx) throws USBException{
		handle.verifyAvailable();
		Descriptor[] desc = new Descriptor[1];
		int ret = _parse_desc(Descriptor.TYPE_CONFIGURATION, handle.getReference(), 0, buffer==null? null: buffer.array(), cfgidx, (byte)0, (byte)0, (byte)0, desc);
		checkException(ret);
		return (ConfigurationDescriptor)desc[0];
	}

	// ======================================================================
	// インターフェース記述子バイナリの解析
	// ======================================================================
	/**
	 * 指定されたインターフェース記述子バイナリを解析します。{@code buffer} に null を指定
	 * した場合は新たにインターフェース記述子を取得します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devid デバイス ID
	 * @param buffer 解析するデバイス記述子のバイナリ
	 * @param cfgidx コンフィギュレーションのインデックス
	 * @param ifcidx インターフェースのインデックス
	 * @param alt 代替設定のインデックス
	 * @return インターフェース記述子
	 * @throws USBException 記述子の取得に失敗した場合
	 */
	public static InterfaceDescriptor parse_interface_desc(handle_t handle, long devid, ByteBuffer buffer, byte cfgidx, byte ifcidx, byte alt) throws USBException{
		handle.verifyAvailable();
		Descriptor[] desc = new Descriptor[1];
		int ret = _parse_desc(Descriptor.TYPE_INTERFACE, handle.getReference(), devid, buffer==null? null: buffer.array(), cfgidx, ifcidx, alt, (byte)0, desc);
		checkException(ret);
		return (InterfaceDescriptor)desc[0];
	}

	// ======================================================================
	// エンドポイント記述子バイナリの解析
	// ======================================================================
	/**
	 * 指定されたエンドポイント記述子バイナリを解析します。{@code buffer} に null を指定し
	 * た場合は新たにエンドポイント記述子を取得します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devid デバイス ID
	 * @param buffer 解析するデバイス記述子のバイナリ
	 * @param cfgidx コンフィギュレーションのインデックス
	 * @param ifcidx インターフェースのインデックス
	 * @param alt 代替設定のインデックス
	 * @param edtidx エンドポイントのインデックス
	 * @return エンドポイント記述子
	 * @throws USBException 記述子の取得に失敗した場合
	 */
	public static EndpointDescriptor parse_endpoint_desc(handle_t handle, long devid, ByteBuffer buffer, byte cfgidx, byte ifcidx, byte alt, byte edtidx) throws USBException{
		handle.verifyAvailable();
		Descriptor[] desc = new Descriptor[1];
		int ret = _parse_desc(Descriptor.TYPE_ENDPOINT, handle.getReference(), devid, buffer==null? null: buffer.array(), cfgidx, ifcidx, alt, edtidx, desc);
		checkException(ret);
		return (EndpointDescriptor)desc[0];
	}

	// ======================================================================
	// 記述子バイナリの参照
	// ======================================================================
	/**
	 * デバイスに対して任意の記述子バイナリを要求します。
	 * <p>
	 * @param handle OpenUSB ハンドル
	 * @param devid デバイス ID
	 * @param type 記述子のタイプ
	 * @param descidx 記述子のインデックス
	 * @param langid 記述子の言語 ID (主に文字列記述子用)
	 * @param buffer 記述子のバイナリ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _get_raw_desc(long handle, long devid, byte type, byte descidx, short langid, byte[][] buffer);

	// ======================================================================
	// 記述子バイナリの解析
	// ======================================================================
	/**
	 * 指定されたエンドポイント記述子バイナリを解析します。
	 * <p>
	 * @param type 記述子のタイプ ({@code DESC_TYPE_XXX})
	 * @param handle OpenUSB ハンドル
	 * @param devid デバイス ID
	 * @param buffer 解析するデバイス記述子のバイナリ
	 * @param cfgidx コンフィギュレーションのインデックス
	 * @param ifcidx インターフェースのインデックス
	 * @param alt 代替設定のインデックス
	 * @param eptidx エンドポイントのインデックス
	 * @param desc 記述子のリターンバッファ
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _parse_desc(byte type, long handle, long devid, byte[] buffer, byte cfgidx, byte ifcidx, byte alt, byte eptidx, Descriptor[] desc);

	// ######################################################################

	// ======================================================================
	// 同期リクエストの実行
	// ======================================================================
	/**
	 * 指定されたリクエストを同期実行します。このメソッドはリクエストが完了するまで処理を戻しま
	 * せん。リクエストに設定されているコールバック実装は無視されます。
	 * <p>
	 * @param request リクエスト
	 * @throws USBException 同期リクエストの実行に失敗した場合
	 */
	public static void xfer_wait(request_handle_t request) throws USBException{
		request.verify();

		// コールバックが指定されている場合は警告
		if(request.cb != null){
			logger.fine("request callback will ignore for sync transfer");
		}

		int ret = _xfer(request, false);
		checkException(ret);
		return;
	}

	// ======================================================================
	// 非同期リクエストの実行
	// ======================================================================
	/**
	 * 指定されたリクエストを非同期で実行します。このメソッドはリクエストの完了を待機せずにすぐ
	 * に処理を戻します。リクエストに対して{@link request_handle_t#cb コールバック実装}を
	 * 設定するか、
	 * {@link #wait(request_handle_t[])} または {@link #poll(request_handle_t[])}
	 * を使用する事でリクエストの完了を知ることが出来ます。
	 * <p>
	 * コールバック実装の指定と {@link #wait(request_handle_t[])} または
	 * {@link #poll(request_handle_t[])} での完了確認は両立できません。コールバック実装
	 * を指定した非同期リクエストに対して {@link #wait(request_handle_t[])},
	 * {@link #poll(request_handle_t[])} は例外を発生します (OpenUSB ライブラリの仕様)。
	 * <p>
	 * @param request リクエスト
	 * @throws USBException 非同期リクエストの実行に失敗した場合
	 */
	public static void xfer_aio(request_handle_t request) throws USBException{
		request.verify();
		int ret = _xfer(request, true);
		checkException(ret);
		return;
	}

	// ======================================================================
	// リクエストの実行
	// ======================================================================
	/**
	 * 指定された同期または非同期リクエストを実行します。
	 * <p>
	 * @param handle リクエストハンドル
	 * @param async 非同期 {@code xfer_aio()} の場合 true, 同期 {@code xfer_wait()} の場合 false
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _xfer(request_handle_t handle, boolean async);

	// ======================================================================
	// コントロール転送の実行
	// ======================================================================
	/**
	 * 指定されたエンドポイントに対してコントロール転送を実行します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @param ept エンドポイント番号
	 * @param ctrl コントロール転送リクエスト
	 * @throws USBException コントロール転送に失敗した場合
	 */
	public static void ctrl_xfer(dev_handle_t dev, byte ifc, byte ept, ctrl_request_t[] ctrl) throws USBException{
		dev.verifyAvailable();
		int ret = _xfer(TRANSFER_TYPE_CONTROL, dev.getReference(), ifc, ept, ctrl);
		checkException(ret);
		return;
	}

	// ======================================================================
	// 割り込み転送の実行
	// ======================================================================
	/**
	 * 指定されたエンドポイントに対して割り込み転送を実行します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @param ept エンドポイント番号
	 * @param intr 割り込み転送リクエスト
	 * @throws USBException 割り込み転送に失敗した場合
	 */
	public static void intr_xfer(dev_handle_t dev, byte ifc, byte ept, intr_request_t[] intr) throws USBException{
		dev.verifyAvailable();
		int ret = _xfer(TRANSFER_TYPE_INTERRUPT, dev.getReference(), ifc, ept, intr);
		checkException(ret);
		return;
	}

	// ======================================================================
	// バルク転送の実行
	// ======================================================================
	/**
	 * 指定されたエンドポイントに対してバルク転送を実行します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @param ept エンドポイント番号
	 * @param bulk バルク転送リクエスト
	 * @throws USBException バルク転送に失敗した場合
	 */
	public static void bulk_xfer(dev_handle_t dev, byte ifc, byte ept, bulk_request_t[] bulk) throws USBException{
		dev.verifyAvailable();
		int ret = _xfer(TRANSFER_TYPE_BULK, dev.getReference(), ifc, ept, bulk);
		checkException(ret);
		return;
	}

	// ======================================================================
	// 等時間隔転送の実行
	// ======================================================================
	/**
	 * 指定されたエンドポイントに対して等時間隔転送を実行します。
	 * <p>
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @param ept エンドポイント番号
	 * @param isoc 等時間隔転送リクエスト
	 * @throws USBException 等時間隔転送に失敗した場合
	 */
	public static void isoc_xfer(dev_handle_t dev, byte ifc, byte ept, isoc_request_t[] isoc) throws USBException{
		dev.verifyAvailable();
		int ret = _xfer(TRANSFER_TYPE_ISOCHRONOUS, dev.getReference(), ifc, ept, isoc);
		checkException(ret);
		return;
	}

	// ======================================================================
	// 転送の実行
	// ======================================================================
	/**
	 * 指定された転送処理を同期実行します。
	 * <p>
	 * @param type 転送タイプ
	 * @param dev デバイスハンドル
	 * @param ifc インターフェース番号
	 * @param ept エンドポイント番号
	 * @param request 実行するリクエスト
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _xfer(int type, long dev, byte ifc, byte ept, Object[] request);

	// ======================================================================
	// 非同期処理の中断
	// ======================================================================
	/**
	 * 指定された非同期リクエストを中断します。リクエストが非同期処理中でない場合は何も行いません。
	 * <p>
	 * @param request 中断する非同期リクエスト
	 * @throws USBException 中断に失敗した場合
	 */
	public static void abort(request_handle_t request) throws USBException{
		int ret = _abort(request);
		checkException(ret);
		return;
	}

	// ======================================================================
	// 非同期リクエストの待機
	// ======================================================================
	/**
	 * 非同期で開始したリクエストの完了を待機します。このメソッドはパラメータに指定されたリクエ
	 * ストのいずれかが完了した場合に該当するリクエストを返します。
	 * <p>
	 * 指定された非同期リクエストがコールバック実装付きで実行されている場合は例外が発生します。
	 * <p>
	 * @param requests 完了を待機する非同期リクエストの配列
	 * @return 配列内で非同期要求の完了したリクエスト
	 * @throws USBException リクエストの待機に失敗した場合
	 */
	public static request_handle_t wait(request_handle_t[] requests) throws USBException{
		return wait(requests, true);
	}

	// ======================================================================
	// 完了リクエストの参照
	// ======================================================================
	/**
	 * 非同期で開始したリクエストの完了を確認します。このメソッドはパラメータに指定されたリク
	 * エストの配列から完了しているものがあればそれを返します。完了したリクエストが存在しなければ
	 * null を返し、処理が待機することはありません。
	 * <p>
	 * 指定された非同期リクエストがコールバック実装付きで実行されている場合は例外が発生します。
	 * <p>
	 * @param requests 完了を待機する非同期リクエストの配列
	 * @return 配列内で非同期要求の完了したリクエスト
	 * @throws USBException リクエストの待機に失敗した場合
	 */
	public static request_handle_t poll(request_handle_t[] requests) throws USBException{
		return wait(requests, false);
	}

	// ======================================================================
	// リクエストの完了確認
	// ======================================================================
	/**
	 * 指定されたリクエストの完了を待機します。
	 * <p>
	 * @param requests 完了を待機する非同期リクエストの配列
	 * @param wait 完了を待機する場合 true
	 * @return 配列内で非同期要求の完了したリクエスト
	 * @throws USBException リクエストの待機に失敗した場合
	 */
	private static request_handle_t wait(request_handle_t[] requests, boolean wait) throws USBException{
		int[] index = new int[1];
		int ret = _wait(requests, index, wait);
		checkException(ret);
		if(index[0] < 0){
			logger.finest("no request_handler_t selected");
			return null;
		}
		return requests[index[0]];
	}

	// ======================================================================
	// 非同期リクエストの中断
	// ======================================================================
	/**
	 * 非同期リクエストを中断します。
	 * <p>
	 * @param request 中断する非同期リクエスト
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 * @throws USBException リクエストの中断に失敗した場合
	 */
	private static native int _abort(request_handle_t request) throws USBException;

	// ======================================================================
	// 完了リクエストの参照
	// ======================================================================
	/**
	 * 完了したリクエストのインデックスを参照します。{@code wait} に false が指定されている
	 * 場合、完了したリクエストが存在しなければインデックスに負の値が設定されます。
	 * <p>
	 * @param requests 完了リクエストを参照する配列
	 * @param index 完了リクエストのリターンバッファ
	 * @param wait 完了を待機する場合 true
	 * @return 成功した場合 {@link #RET_SUCCESS}
	 */
	private static native int _wait(request_handle_t[] requests, int[] index, boolean wait);

	// ======================================================================
	// エラーメッセージの参照
	// ======================================================================
	/**
	 * 指定されたコードに対するエラーメッセージを参照します。
	 * <p>
	 * @param error エラーコード
	 * @return エラーメッセージ
	 */
	public static native String strerror(int error);

	// ======================================================================
	// リターンコードの確認
	// ======================================================================
	/**
	 * 指定されたリターンコードに対応する例外を throw します。{@code ret} が
	 * {@link #RET_SUCCESS} を示す場合は何も行わないで終了します。
	 * <p>
	 * @param ret リターンコード
	 * @throws USBException リターンコードが {@link #RET_SUCCESS} 以外の場合
	 */
	private static void checkException(int ret) throws USBException{
		if(ret == RET_SUCCESS){
			return;
		}
		String msg = strerror(ret);
		switch(ret){
		case RET_NO_RESOURCES:
			throw new OutOfMemoryError(msg);
		case RET_NOT_SUPPORTED:
			throw new NotImplementedException(msg);
		case RET_BUSY:
			throw new ResourceBusyException(msg);
		default:
			throw new USBException(msg);
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ref_t: 参照クラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 参照を表すクラスです。ネイティブ側でのリソース参照とそれに対応するオブジェクトを保持します。
	 * <p>
	 * @param <T> この参照が表すオブジェクト
	 */
	public static abstract class ref_t<T>{

		// ==================================================================
		// ネイティブ参照
		// ==================================================================
		/**
		 * ネイティブ実装で使用しているリソースへの参照です。
		*/
		protected long ref = 0;

		// ==================================================================
		// オブジェクト
		// ==================================================================
		/**
		 * この参照が示すオブジェクトです。
		*/
		public T value = null;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * デフォルトコンストラクタは何も行いません。ネイティブ実装によって呼び出されます。
		 * <p>
		*/
		protected ref_t(){
			return;
		}

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * 指定された参照を指定して構築を行います。
		 * <p>
		 * @param ref リソースの参照
		*/
		protected ref_t(long ref){
			this.ref = ref;
			return;
		}

		// ==================================================================
		// デストラクタ
		// ==================================================================
		/**
		 * このインスタンスが示す参照が解放されていなければ警告を出力します。
		 * <p>
		*/
		@Override
		protected void finalize() throws Throwable{
			if(ref != 0){
				logger.warning("unreleased reference of " + getClass().getSimpleName() + ": " + hex(ref));
				release();
			}
			super.finalize();
			return;
		}

		// ==================================================================
		// 参照の参照
		// ==================================================================
		/**
		 * このインスタンスの示す参照を参照します。返値の意味は実装依存です。
		 * <p>
		 * @return 参照
		*/
		public long getReference(){
			return ref;
		}

		// ==================================================================
		// 解放確認
		// ==================================================================
		/**
		 * この参照が解放されているかどうかを確認します。このメソッドは参照の解放を行う前の冗長
		 * 呼び出し防止チェックとして使用します。
		 * <p>
		 * @return 参照が解放されている場合 true
		*/
		boolean isAlreadyReleased(){
			if(ref == 0){
				logger.finest("specified " + getClass().getSimpleName() + " is already released");
				return true;
			}
			return false;
		}

		// ==================================================================
		// 参照有効の検証
		// ==================================================================
		/**
		 * このインスタンスの示す参照が 0 以外であることを検証します。0 の場合は例外が発生しま
		 * す。
		 * <p>
		 * @throws USBException 参照が無効の場合
		*/
		void verifyAvailable() throws USBException{
			if(ref == 0){
				throw new USBException(getClass().getSimpleName() + " already released");
			}
			return;
		}

		// ==================================================================
		// 参照の解放
		// ==================================================================
		/**
		 * サブクラスの定義する方法でこのインスタンスの参照を解放します。
		 * <p>
		*/
		abstract void release();

		// ==================================================================
		// インスタンスの文字列化
		// ==================================================================
		/**
		 * このインスタンスを文字列化します。
		 * <p>
		 * @return インスタンスの文字列
		*/
		@Override
		public String toString(){
			return hex(ref)+ ": " + value;
		}

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// handle_t: OpenUSB ハンドル
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * OpenUSB のハンドルを表すためのクラスです。
	 * <p>
	 * アプリケーションはインスタンスが不用になった時に {@link OpenUSB#fini(handle_t)}
	 * を呼び出してリソースを解放する必要があります。
	 * <p>
	 */
	public static final class handle_t extends ref_t<Long>{
		/**
		 * コンストラクタはクラス内に隠蔽されています。
		 * <p>
		 * @param ref OpenUSB ハンドル
		 */
		private handle_t(long ref){
			super(ref);
			return;
		}
		/** このインスタンスが保持している参照を解放します。 */
		@Override
		void release(){
			try{ fini(this); }catch(USBException ex){/* */}
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// handle_t: OpenUSB ハンドル
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * OpenUSB のハンドルを表すためのクラスです。
	 * <p>
	 * アプリケーションはインスタンスが不用になった時に {@link OpenUSB#fini(handle_t)}
	 * を呼び出してリソースを解放する必要があります。
	 * <p>
	 */
	public static final class dev_handle_t extends ref_t<Long>{
		/**
		 * コンストラクタはクラス内に隠蔽されています。
		 * <p>
		 * @param ref デバイスハンドル
		 */
		private dev_handle_t(long ref){
			super(ref);
			return;
		}
		/** このインスタンスが保持している参照を解放します。 */
		@Override
		void release(){
			try{ close_device(this); }catch(USBException ex){/* */}
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// devid_array_ref: バス ID リスト参照クラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * バス ID {@code openusb_busid_t} のリストを表す参照クラスです。
	 * {@link OpenUSB#get_busid_list(handle_t)} によって生成されたインスタンスは {@link #value}
	 * にバス ID の配列を保持しています。
	 * <p>
	 * アプリケーションはバス ID が不用になった時に {@link OpenUSB#free_busid_list(busid_array_ref)}
	 * を呼び出してリソースを解放する必要があります。
	 * <p>
	 */
	public static final class busid_array_ref extends ref_t<long[]>{
		/** コンストラクタはクラス内に隠蔽されています。*/
		private busid_array_ref(){
			return;
		}
		/** このインスタンスが保持している参照を解放します。 */
		@Override
		void release(){
			try{ free_busid_list(this); }catch(USBException ex){/* */}
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// devid_array_ref: バス ID リスト参照クラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * デバイス ID {@code openusb_devid_t} のリストを表す参照クラスです。
	 * {@link OpenUSB#get_devids_by_bus(handle_t, long)} などによって生成されたインス
	 * タンスは {@link #value} にデバイス ID の配列を保持しています。
	 * <p>
	 * アプリケーションはデバイス ID が不用になった時に {@link OpenUSB#free_devid_list(devid_array_ref)}
	 * を呼び出してリソースを解放する必要があります。
	 * <p>
	 */
	public static final class devid_array_ref extends ref_t<long[]>{
		/** コンストラクタはクラス内に隠蔽されています。 */
		private devid_array_ref(){
			return;
		}
		/** 参照を解放します。 */
		@Override
		void release(){
			try{ free_devid_list(this); } catch(USBException ex){/* */}
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// dev_data_ref: デバイスデータ参照クラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * デバイスデータ {@code openusb_dev_data} の参照を表すクラスです。
	 * {@link OpenUSB#get_device_data(handle_t, long, int)} によって生成されたインス
	 * タンスは {@link #value} にデバイスデータを保持しています。
	 * <p>
	 * アプリケーションはデバイス ID が不用になった時に
	 * {@link OpenUSB#free_device_data(dev_data_ref)} を呼び出してリソースを解放する
	 * 必要があります。
	 * <p>
	 */
	public static final class dev_data_ref extends ref_t<dev_data_t>{
		/** コンストラクタはクラス内に隠蔽されています。*/
		private dev_data_ref(){
			return;
		}
		/** 参照を解放します。 */
		@Override
		void release(){
			try{ free_device_data(this); } catch(USBException ex){/* */}
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// dev_data_t: デバイスデータクラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * デバイスデータ {@code openusb_dev_data_t} を表すクラスです。
	 * <p>
	 */
	public static final class dev_data_t{
		/** このデバイスのバス ID です。*/
		public long busid = 0;
		/** このデバイスの ID です。*/
		public long devid = 0;
		/** このデバイスのバスアドレスです。*/
		public byte bus_address = 0;
		/** 親のデバイス ID です。0 はルートハブを示します。 */
		public long pdevid = 0;
		/** デバイスの接続している親ポートです。*/
		public byte pport = 0;
		/** デバイス上のポートの数です。非ハブデバイスには 0 が設定されます。*/
		public byte nports = 0;
		/** {@code /dev/bus/usb/xxx} のようなパス。 */
		public String sys_path = null;
		/** {@code 1.2.1} のようなトポロジー的パス。*/
		public String bus_path = null;
		/** デバイス記述子。 */
		public DeviceDescriptor dev_desc = null;
		/** コンフィギュレーション記述子。 */
		public ConfigurationDescriptor cfg_desc = null;
		/** コンフィギュレーション記述子の生バイナリ。 */
		public byte[] raw_cfg_desc = null;
		/** デフォルト langid でのメーカー名。 */
		public String manufacturer = null;
		/** デフォルト langid での製品名。*/
		public String product = null;
		/** デフォルト langid でのシリアルナンバー。*/
		public String serialnumber = null;
		/** コントロール転送の最大転送サイズ。サポートしていない場合は 0。*/
		public int ctrl_max_xfer_size = 0;
		/** 割り込み転送の最大転送サイズ。サポートしていない場合は 0。*/
		public int intr_max_xfer_size = 0;
		/** バルク転送の最大転送サイズ。サポートしていない場合は 0。*/
		public int bulk_max_xfer_size = 0;
		/** 等時間隔転送の最大転送サイズ。サポートしていない場合は 0。*/
		public int isoc_max_xfer_size = 0;

		/** コンストラクタはクラス内に隠蔽されています。*/
		private dev_data_t(){
			return;
		}
		/** このインスタンスを文字列化します。 */
		@Override
		public String toString(){
			return String.format(
				"busid=%d,devid=%d,bus_address=%d,pdevid=%d,pport=%d,nports=%d,sys_path=%s,bus_path=%s," +
				"dev_desc=[%s],cfg_desc=[%s],raw_cfg_desc=%s,manufacturer=%s,product=%s,serialnumber=%s," +
				"ctrl_max_xfer_size=%d,intr_max_xfer_size=%d,bulk_max_xfer_size=%d,isoc_max_xfer_size=%d",
				busid, devid, bus_address, pdevid, pport, nports, sys_path, bus_path,
				dev_desc, cfg_desc, raw_cfg_desc, manufacturer, product, serialnumber,
				ctrl_max_xfer_size, intr_max_xfer_size, bulk_max_xfer_size, isoc_max_xfer_size);
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// request_result_t: リクエスト結果
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * リクエスト結果を表すクラスです。
	 * <p>
	 */
	public static class request_result_t {
		/** ステータス */
		public int status = 0;
		/** 転送バイト数 */
		public int transferred_bytes = 0;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ctrl_request_t: コントロール転送リクエスト
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * コントロール転送のリクエストを行うための構造体です。
	 * <p>
	 */
	public static class ctrl_request_t {
		/** リクエストタイプのビットフィールド */
		public final byte bmRequestType;
		/** リクエスト種別 */
		public final byte bRequest;
		/** 値 */
		public final short wValue;
		/** インデックス */
		public final short wIndex;

		/** このリクエストのバッファ */
		public final byte[] payload;
		/** タイムアウト (ミリ秒) */
		public final int timeout;
		/** フラグ */
		public final int flags;
		/** リクエスト結果 */
		public final request_result_t result = new request_result_t();
		/**
		 * フィールドの値を指定して構築を行います。
		 * <p>
		 * @param bmRequestType
		 * @param bRequest
		 * @param wValue
		 * @param wIndex
		 * @param payload リクエストのバッファ
		 * @param timeout
		 * @param flags
		 */
		public ctrl_request_t(byte bmRequestType, byte bRequest, short wValue, short wIndex, byte[] payload, int timeout, int flags){
			this.bmRequestType = bmRequestType;
			this.bRequest = bRequest;
			this.wValue = wValue;
			this.wIndex = wIndex;
			this.payload = payload;
			this.timeout = timeout;
			this.flags = flags;
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// intr_request_t: 割り込み転送リクエスト
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 割り込み転送のリクエストを行うための構造体です。
	 * <p>
	 */
	public static class intr_request_t {
		/** 間隔 */
		public final short interval;
		/** このリクエストのバッファ */
		public final byte[] payload;
		/** タイムアウト (ミリ秒) */
		public final int timeout;
		/** フラグ */
		public final int flags;
		/** リクエスト結果 */
		public final request_result_t result = new request_result_t();
		/**
		 * フィールドの値を指定して構築を行います。
		 * <p>
		 * @param payload
		 * @param timeout
		 * @param flags
		 * @param interval
		 */
		public intr_request_t(byte[] payload, int timeout, int flags, short interval){
			this.payload = payload;
			this.timeout = timeout;
			this.flags = flags;
			this.interval = interval;
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// bulk_request_t: バルク転送リクエスト
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * バルク転送のリクエストを行うための構造体です。
	 * <p>
	 */
	public static class bulk_request_t {
		/** このリクエストのバッファ */
		public final byte[] payload;
		/** タイムアウト (ミリ秒) */
		public final int timeout;
		/** フラグ */
		public final int flags;
		/** リクエスト結果 */
		public final request_result_t result = new request_result_t();
		/**
		 * フィールドの値を指定して構築を行います。
		 * <p>
		 * @param payload
		 * @param timeout
		 * @param flags
		 */
		public bulk_request_t(byte[] payload, int timeout, int flags){
			this.payload = payload;
			this.timeout = timeout;
			this.flags = flags;
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// isoc_pkts: バルク転送リクエスト
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * バルク転送のリクエストを行うための構造体です。
	 * <p>
	 */
	public static class isoc_pkts {
		/** このパケットが示すバッファ */
		public final byte[] payload;
		/** リクエスト結果 */
		public final request_result_t result = new request_result_t();
		/**
		 * バッファサイズを指定して構築を行います。
		 * <p>
		 * @param payload
		 */
		public isoc_pkts(byte[] payload){
			this.payload = payload;
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// isoc_request_t: 等時間隔転送リクエスト
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 等時間隔転送のリクエストを行うための構造体です。
	 * <p>
	 */
	public static class isoc_request_t {
		/** 開始フレーム */
		public final int start_frame;
		/** フラグ */
		public final int flags;
		/** パケット */
		public final isoc_pkts[] frames;
		/** Isochronous ステータス */
		public final int isoc_status;
		/**
		 * パケット数を指定して構築を行います。
		 * <p>
		 * @param start_frame
		 * @param flags
		 * @param frameSize このリクエストのフレーム数
		 * @param isoc_status
		 */
		public isoc_request_t(int start_frame, int flags, int frameSize, int isoc_status){
			this.start_frame = start_frame;
			this.flags = flags;
			this.frames = new isoc_pkts[frameSize];
			this.isoc_status = isoc_status;
			return;
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// isoc_request_t: 等時間隔転送リクエスト
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 等時間隔転送のリクエストを行うための構造体です。
	 * <p>
	 */
	public static class request_handle_t {
		/** メッセージ生成用のクラス名 */
		private static final String typename = request_handle_t.class.getSimpleName();
		/** ネイティブ側の openusb_request_handle ポインタ。 */
		private long ref = 0;
		/** リクエストを行うデバイスのハンドルです。*/
		public final long dev;
		/** リクエストを行うインターフェース番号です。*/
		public final byte intf;
		/** リクエストを行うエンドポイント番号です。*/
		public final byte edpt;
		/** 転送タイプです。*/
		public final int type;
		/** コントロール転送用のリクエストです。 */
		public ctrl_request_t[] ctrl = null;
		/** 割り込み転送用のリクエストです。 */
		public intr_request_t[] intr = null;
		/** バルク転送用のリクエストです。 */
		public bulk_request_t[] bulk = null;
		/** 等時間隔転送用のリクエストです。 */
		public isoc_request_t[] isoc = null;
		/** 非同期リクエストの完了を受けるためのコールバックです。 */
		public final request_handle_callback_t cb;
		/** 非同期リクエストコールバックのための任意のオブジェクトです。 */
		public final Object arg;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * コールバック無指定のリクエストを構築します。このコンストラクタは {@link
		 * OpenUSB#xfer_wait(request_handle_t)} を使用する同期リクエストや {@link
		 * OpenUSB#wait(request_handle_t[])}、{@link OpenUSB#poll(request_handle_t[])}
		 * を使用する非同期リクエストを行う場合に使用します。
		 * <p>
		 * @param dev デバイスハンドラ
		 * @param intf インターフェース番号
		 * @param edpt エンドポイント番号
		 * @param type 転送タイプ ({@code TRANSFER_TYPE_XXX})
		 */
		public request_handle_t(dev_handle_t dev, byte intf, byte edpt, int type){
			this(dev, intf, edpt, type, null, null);
			return;
		}

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * このリクエストを使用して要求が可能かどうかを検証します。
		 * <p>
		 * @param dev デバイスハンドラ
		 * @param intf インターフェース番号
		 * @param edpt エンドポイント番号
		 * @param type 転送タイプ ({@code TRANSFER_TYPE_XXX})
		 * @param cb コールバック実装
		 * @param arg コールバック実装用オブジェクト
		 */
		public request_handle_t(dev_handle_t dev, byte intf, byte edpt, int type, request_handle_callback_t cb, Object arg){
			this.dev = dev.getReference();
			this.intf = intf;
			this.edpt = edpt;
			this.type = type;
			this.cb = cb;
			this.arg = arg;
			return;
		}

		// ==================================================================
		// リクエストの検証
		// ==================================================================
		/**
		 * このリクエストを使用して要求が可能かどうかを検証します。
		 * <p>
		 * @throws USBException リクエストの状態が不正な場合
		 */
		private void verify() throws USBException{

			// このリクエストハンドルが処理中でないことを確認
			if(ref != 0){
				throw new USBException(typename + " processing async request: " + hex(ref));
			}

			// 転送タイプに対する構造体が設定されていることを確認
			switch(type){
			case TRANSFER_TYPE_CONTROL:
				if(ctrl == null || ctrl.length == 0){
					throw new IllegalArgumentException(typename + ".ctrl not specified");
				}
				break;
			case TRANSFER_TYPE_INTERRUPT:
				if(intr == null || intr.length == 0){
					throw new IllegalArgumentException(typename + ".intr not specified");
				}
				break;
			case TRANSFER_TYPE_BULK:
				if(bulk == null || bulk.length == 0){
					throw new IllegalArgumentException(typename + ".bulk not specified");
				}
				break;
			case TRANSFER_TYPE_ISOCHRONOUS:
				if(isoc == null || isoc.length == 0){
					throw new IllegalArgumentException(typename + ".isoc not specified");
				}
				break;
			default:
				throw new IllegalArgumentException("unsupported transfer type: " + type);
			}
			return;
		}

		// ==================================================================
		// 非同期コールバック
		// ==================================================================
		/**
		 * JNI 側から非同期コールバックを受けます。このリクエストハンドルにコールバックが設定
		 * されていれば呼び出しを行います。
		 * <p>
		 * @return コールバックの処理結果
		 */
		int async_callback(){
			logger.finest("callback()");
			if(cb == null){
				return RET_CB_CONTINUE;
			}
			try{
				return cb.callback(this);
			} catch(Throwable ex){
				logger.log(Level.SEVERE, "", ex);
				return RET_CB_TERMINATE;
			}
		}
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// request_handle_callback_t: リクエストハンドルコールバック
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * {@link OpenUSB#xfer_aio(request_handle_t) 非同期リクエスト}を行う時にコール
	 * バックを受けるためのインターフェースです。サブクラスを作成して {@link request_handle_t#cb}
	 * に設定することでリクエスト終了時のコールバックが行われます。
	 * <p>
	 */
	public static interface request_handle_callback_t{

		// ==================================================================
		// コールバックの実行
		// ==================================================================
		/**
		 * 非同期リクエストのコールバックを受けます。
		 * <p>
		 * @param request 非同期リクエストの完了したリクエスト
		 * @return {@link OpenUSB#RET_CB_TERMINATE}, {@link OpenUSB#RET_CB_CONTINUE}
		*/
		public int callback(request_handle_t request);

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// EventCallback: イベントコールバック
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * OpenUSB ライブラリからイベントのコールバックを受けるためのインターフェースです。
	 * <p>
	 * @see OpenUSB#set_event_callback(handle_t, int, event_callback_t, Object)
	 */
	public static interface event_callback_t{

		// ==================================================================
		// コールバックの実行
		// ==================================================================
		/**
		 * OpenUSB ライブラリからのイベントコールバックを受けます。
		 * <p>
		 * @param handle {@code openusb_handle_t} OpenUSB インスタンスのハンドル
		 * @param devid {@code openusb_devid_t} デバイス ID
		 * @param event {@code openusb_event_t} イベント種別 ({@code EVENT_XXX}定数)
		 * @param arg コールバック設定時の引数
		 */
		public void callback(long handle, long devid, int event, Object arg);

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// DebugCallback: デバッグコールバック
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * OpenUSB ライブラリからのデバッグトレースメッセージを受け取るためのインターフェースです。
	 * <p>
	 * @see OpenUSB#set_debug(handle_t, int, int, debug_callback_t)
	 */
	public static interface debug_callback_t{

		// ==================================================================
		// コールバックの実行
		// ==================================================================
		/**
		 * OpenUSB ライブラリからのトレースメッセージを受け取ります。
		 * <p>
		 * @param handle {@code openusb_handle_t} OpenUSB インスタンスのハンドル
		 * @param msg トレースメッセージ
		 */
		public void callback(long handle, String msg);

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// EventCallbackEntry: イベントコールバックエントリ
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * イベントタイプに対するコールバックを実行するためのエントリです。
	 * <p>
	 */
	private static class EventCallbackEntry{

		// ==================================================================
		// イベントコールバック
		// ==================================================================
		/**
		 * イベントコールバックです。
		 * <p>
		 */
		private final event_callback_t callback;

		// ==================================================================
		// 呼び出し引数
		// ==================================================================
		/**
		 * コールバック時の呼び出し引数です。
		 * <p>
		 */
		private final Object arg;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * <p>
		 * @param callback イベントコールバック実装
		 * @param arg コールバック時の引数
		 */
		public EventCallbackEntry(event_callback_t callback, Object arg){
			this.callback = callback;
			this.arg = arg;
			return;
		}

		// ==================================================================
		// コールバックの実行
		// ==================================================================
		/**
		 * コールバックを実行します。
		 * <p>
		 * @param handle ハンドル
		 * @param devid デバイス ID
		 * @param event イベント ID
		 */
		public void call(long handle, long devid, int event){
			callback.callback(handle, devid, event, arg);
			return;
		}

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Handleハンドル: ハンドル
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * OpenUSB ハンドルに対するオブジェクトを保持するためのクラスです。
	 * <p>
	 */
	private static class Handle{

		// ==================================================================
		// デバッグコールバック
		// ==================================================================
		/**
		 * デバッグトレース用のコールバックです。
		 */
		private volatile debug_callback_t debug = null;

		// ==================================================================
		// イベントコールバック
		// ==================================================================
		/**
		 * イベント ID に対するコールバックです。
		 */
		private final Map<Integer,EventCallbackEntry> event
			= Collections.synchronizedMap(new HashMap<Integer, EventCallbackEntry>());

		// ==================================================================
		// デバイスハンドル
		// ==================================================================
		/**
		 * このハンドル上でオープンされている全てのデバイスハンドルです。
		 */
		private final Set<Long> devHandle
			= Collections.synchronizedSet(new HashSet<Long>());
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Shutdown: シャットダウンフック
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * {@link OpenUSB#fini(handle_t)} の呼び出されていない OpenUSB ハンドルを警告ログ
	 * 付きで全て解放します。
	 * <p>
	 */
	private static class Shutdown extends Thread{

		// ==================================================================
		// 処理の実行
		// ==================================================================
		/**
		 * 初期化されている全てのインスタンスを解放します。
		 * <p>
		 */
		@Override
		public void run(){
			logger.finest("shuting down OpenUSB 1.0");
			Set<Long> handle = new HashSet<Long>(HANDLE.keySet());
			if(handle.size() > 0){
				logger.warning(handle.size() + " unreleased OpenUSB handles were found in shutdown task.");
				logger.warning("All of these are implicitly closed by fail-safe behavior.");
				for(long h: handle){
					logger.warning("unreleased OpenUSB handle: " + hex(h));
					try{
						fini(new handle_t(h));
					} catch(USBException ex){
						logger.log(Level.WARNING, "fail to release handle, continue closing", ex);
					}
				}
			}
			return;
		}

	}

	// ######################################################################
	// イベントコールバック定数
	// ######################################################################

	/**
	 * USB デバイスが取り付けられた時のコールバックを受けるためのイベント種別です。
	 * <p>
	 * @see OpenUSB#set_event_callback(handle_t, int, event_callback_t, Object)
	 */
	public static final int EVENT_ATTACH = 0;

	/**
	 * USB デバイスが取り外された時のコールバックを受けるためのイベント種別です。
	 * <p>
	 * @see OpenUSB#set_event_callback(handle_t, int, event_callback_t, Object)
	 */
	public static final int EVENT_REMOVE = 1;

	/**
	 * USB デバイスがサスペンドされた時のコールバックを受けるためのイベント種別です。
	 * <p>
	 * @see OpenUSB#set_event_callback(handle_t, int, event_callback_t, Object)
	 */
	public static final int EVENT_SUSPEND = 2;

	/**
	 * USB デバイスがレジュームした時のコールバックを受けるためのイベント種別です。
	 * <p>
	 * @see OpenUSB#set_event_callback(handle_t, int, event_callback_t, Object)
	 */
	public static final int EVENT_RESUME = 3;

	/**
	 * ホストコントローラが取り付けられた時のコールバックを受けるためのイベント種別です。
	 * <p>
	 * @see OpenUSB#set_event_callback(handle_t, int, event_callback_t, Object)
	 */
	public static final int EVENT_HC_ATTACH = 4;

	/**
	 * ホストコントローラが取り外された時のコールバックを受けるためのイベント種別です。
	 * <p>
	 * @see OpenUSB#set_event_callback(handle_t, int, event_callback_t, Object)
	 */
	public static final int EVENT_HC_REMOVE = 5;

	/**
	 * ホストコントローラが取り外された時のコールバックを受けるためのイベント種別です。
	 * <p>
	 * @see OpenUSB#set_event_callback(handle_t, int, event_callback_t, Object)
	 */
	public static final int EVENT_COLDPLUG_COMPLETED = 6;

	// ######################################################################
	// 転送タイプ
	// ######################################################################

	/**
	 * 全ての転送タイプを表す定数です。
	 */
	public static final int TRANSFER_TYPE_ALL = 0;

	/**
	 * コントロール転送を表す定数です。
	 */
	public static final int TRANSFER_TYPE_CONTROL = 1;

	/**
	 * 割り込み転送を表す定数です。
	 */
	public static final int TRANSFER_TYPE_INTERRUPT = 2;

	/**
	 * バルク転送を表す定数です。
	 */
	public static final int TRANSFER_TYPE_BULK = 3;

	/**
	 * 等時間隔転送を表す定数です。
	 */
	public static final int TRANSFER_TYPE_ISOCHRONOUS = 4;

	/**
	 * 割り込み転送を表す定数です。
	 */
	public static final int TRANSFER_TYPE_LAST = 5;

	// ######################################################################
	// 初期化
	// ######################################################################

	/** デフォルトの初期化を指定する定数です。*/
	public static final int INIT_DEFAULT = 0;
	/** すぐに有効にならなければ失敗とする定数です。*/
	public static final int INIT_FAIL_FAST = 1;
	/** クローズ時に reverse となる事を保証するプラットフォーム依存のオープンを試行する定数です。 */
	public static final int INIT_REVERSIBLE = 2;
	/** クローズ時に reverse となる保証がないプラットフォーム依存のオープンを試行する定数です。 */
	public static final int INIT_NON_REVERSIBLE = 3;

	// ######################################################################
	// 終了コード
	// ######################################################################

	/** 呼び出し成功を表します。*/
	public static final int RET_SUCCESS = 0;
	/** カーネルまたはドライバでの未定義のエラーを表します。 */
	public static final int RET_PLATFORM_FAILURE = -1;
	/** 実行のためのリソースが不足していることを表します。 */
	public static final int RET_NO_RESOURCES = -2;
	/** 実行のための帯域が不足していることを表します。 */
	public static final int RET_NO_BANDWIDTH = -3;
	/** ホストコントロールドライバがサポートしていないことを表します。 */
	public static final int RET_NOT_SUPPORTED = -4;
	/** ホストコントローラのエラーを表します。 */
	public static final int RET_HC_HARDWARE_ERROR = -5;
	/** 操作が許可されていないことを表します。 */
	public static final int RET_INVALID_PERM = -6;
	/** デバイスがビジーであることを表します。 */
	public static final int RET_BUSY = -7;
	/** 引数が不正であることを表します。 */
	public static final int RET_BADARG = -8;
	/** デバイスへのアクセスが拒否されたことを表します。 */
	public static final int RET_NOACCESS = -9;
	/** データを解析できないことを表します。 */
	public static final int RET_PARSE_ERROR = -10;
	/** デバイスがストールしているか無効であることを表します。 */
	public static final int RET_UNKNOWN_DEVICE = -11;
	/** ハンドルが不正なことを表します。 */
	public static final int RET_INVALID_HANDLE = -12;
	/** システムコールに失敗したことを表します。 */
	public static final int RET_SYS_FUNC_FAILURE = -13;
	/** バスまたはデバイスが見付からないことを表します。 */
	public static final int RET_NULL_LIST = -14;

	/**
	 * 次のリクエスト転送を続行する場合に非同期リクエストコールバック実装が返す値です。
	 * <p>
	 * @see request_handle_callback_t#callback(OpenUSB.request_handle_t)
	*/
	public static final int RET_CB_CONTINUE = -20;

	/**
	 * 次のリクエスト転送を中止する場合に非同期リクエストコールバック実装が返す値です。
	 * <p>
	 * @see request_handle_callback_t#callback(OpenUSB.request_handle_t)
	*/
	public static final int RET_CB_TERMINATE = -21;

	/** エンドポイントがストールした事を表します。 */
	public static final int RET_IO_STALL = -50;
	/** CRC エラーが発生したことを表します。 */
	public static final int RET_IO_CRC_ERROR = -51;
	/** デバイスがハングした事を表します。 */
	public static final int RET_IO_DEVICE_HUNG = -52;
	/** リクエストが大きすぎることを表します。 */
	public static final int RET_IO_REQ_TOO_BIG = -53;
	/** bit stuffing エラーを表します。 */
	public static final int RET_IO_BIT_STUFFING = -54;
	/** 予期しない PID を表します。 */
	public static final int RET_IO_UNEXPECTED_PID = -55;
	/** データオーバーランが発生したことを表します。 */
	public static final int RET_IO_DATA_OVERRUN = -56;
	/** データアンダーランが発生したことを表します。 */
	public static final int RET_IO_DATA_UNDERRUN = -57;
	/** バッファオーバーランが発生したことを表します。 */
	public static final int RET_IO_BUFFER_OVERRUN = -58;
	/** バッファアンダーランが発生したことを表します。 */
	public static final int RET_IO_BUFFER_UNDERRUN = -59;
	/** PID チェックに失敗したことを表します。 */
	public static final int RET_IO_PID_CHECK_FAILURE = -60;
	/** データトルグの不一致 */
	public static final int RET_IO_DATA_TOGGLE_MISMATCH = -61;
	/** I/O タイムアウトが発生したことを表します。 */
	public static final int RET_IO_TIMEOUT = -62;
	/** I/O がキャンセルされたことを表します。 */
	public static final int RET_IO_CANCELED = -63;

}
