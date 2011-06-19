/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: USBServiceManager.java,v 1.12 2009/05/21 12:02:54 torao Exp $
*/
package org.koiroha.usb;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// USBServiceManager: USB サービスマネージャ
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * 一連の USB サービスを管理するための基本的なサービスです。
 * <p>
 * {@code USBServiceManager} クラスの初期化時にローカルシステムに接続されている USB デバイ
 * スを扱う単一の USB サービスが選択されます。
 * <p>
 * このクラスはまずシステムプロパティ {@code org.koiroha.usb.services} で指定された USB
 * サービスクラスをロードします。例えばアプリケーション起動時に以下のようなオプションを指定する
 * 事でカスタマイズされた USB サービスを使用する事が出来ます。
 * <p>
 * <pre>-Dorg.koiroha.usb.services=com.foo.Service</pre>
 * <p>
 * システムプロパティが指定されなかった場合、JSE 6 以降の実行環境ではサービスプロバイダ機構に
 * よって USB サービスを検索します。アプリケーションのバンドルや追加パッケージの
 * <code>META-INF/services/org.koiroha.usb.USBService</code> に定義されている
 * サービスの中から最初にインスタンス化に成功した USB サービスが使用されます。
 * <p>
 * 最終的にどの方法でも USB サービスもロードできなかった場合、デフォルトの USB サービスが使用
 * されます。
 * <p>
 * アプリケーションは {@link #regist(USBService)} メソッドを使用して追加の USB サービスを
 * 登録することが出来ます。これにより仮想的な USB デバイスやリモートシステムのデバイスを透過的に
 * 扱うことができます。
 * <p>
 * エラーや未解放のリソース警告は全て Logging API に報告されます。問題分析時は
 * {@code org.koiroha.usb} のログに注意してください。
 * <p>
 * @version usb4j 1.0 $Revision: 1.12 $ $Date: 2009/05/21 12:02:54 $
 * @author torao
 * @since 2009/04/24 Java2 SE 5.0
 */
public final class USBServiceManager {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(USBServiceManager.class.getName());

	// ======================================================================
	// システムプロパティ名
	// ======================================================================
	/**
	 * デフォルトの {@link USBService} 実装クラスを指定するためのシステムプロパティ名
	 * {@value} です。
	 * <p>
	 */
	private static final String SERVICE_PROPERTY_NAME = "org.koiroha.usb.services";

	// ======================================================================
	// サービスリスト
	// ======================================================================
	/**
	 * このマネージャに登録されているサービスのリストです。
	 * <p>
	 */
	private static final List<USBService> SERVICES = Collections.synchronizedList(new ArrayList<USBService>());

	// ======================================================================
	// スタティックイニシャライザ
	// ======================================================================
	/**
	 * デフォルトの USB サービスを登録します。
	 * <p>
	 */
	static {
		initialize();
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	private USBServiceManager() {
		return;
	}

	// ======================================================================
	// コンテキストの取得
	// ======================================================================
	/**
	 * コンテキストを取得します。
	 * <p>
	 * @return USB コンテキスト
	 * @throws USBException USB デバイスの取得に失敗した場合
	*/
	public static USBContext getContext() throws USBException{

		// 全ての USB サービスからコンテキストを構築
		List<USBContext> sessions = new ArrayList<USBContext>();
		synchronized(SERVICES){
			for(USBService svc: SERVICES){
				sessions.add(svc.createSession());
			}
		}
		USBContext session = new GatheredUSBSession(sessions);

		// 現在の状態を同期化して返す
		session.sync();
		return session;
	}

	// ======================================================================
	// USB サービスの登録
	// ======================================================================
	/**
	 * 追加の USB サービスを登録します。このメソッドを使用する事によりアプリケーション定義の
	 * 仮想デバイスなどを透過的に使用する事が出来ます。
	 * <p>
	 * @param service 登録する USB サービス
	 */
	public static void regist(USBService service){
		SERVICES.add(service);
		logger.finest("regist usb service: " + service.getClass().getName());
		return;
	}

	// ======================================================================
	// USB サービスの登録解除
	// ======================================================================
	/**
	 * USB サービスを登録解除します。
	 * <p>
	 * @param service 登録解除する USB サービス
	 */
	public static void unregist(USBService service){
		SERVICES.remove(service);
		logger.finest("unregist usb service: " + service.getClass().getName());
		return;
	}

	// ======================================================================
	// ライブラリ名一覧の参照
	// ======================================================================
	/**
	 * 登録されている USB サービスのライブラリ名一覧を参照します。このメソッドが返す文字列は
	 * 問題分析の目的で人が認識するための情報です。
	 * <p>
	 * @return ライブラリ名一覧
	 */
	public static String[] getLibraryNames(){
		synchronized(SERVICES){
			String[] name = new String[SERVICES.size()];
			for(int i=0; i<SERVICES.size(); i++){
				try{
					name[i] = SERVICES.get(i).getLibraryName();
				} catch(USBException ex){
					name[i] = "ERROR:" + ex.getMessage();
				}
			}
			return name;
		}
	}

	// ======================================================================
	// USB サービスの初期化
	// ======================================================================
	/**
	 * USB サービスを初期化します。
	 * <p>
	 */
	private static void initialize(){
		logger.finest("initializing usb4j service manager: jdk" + System.getProperty("java.version") + " on " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
		logger.finest("java.library.path=" + System.getProperty("java.library.path"));

		// システムプロパティからユーザ指定のサービスを設定
		loadBySystemProperty();

		// サービスローダーからロード
		if(SERVICES.size() == 0){
			loadByServiceLoader();
		}

		// デフォルトのサービスをロード
		if(SERVICES.size() == 0){
			loadByDefaultResource();
		}

		// どのサービスもロードできなかった場合は警告
		if(SERVICES.size() == 0){
			logger.warning("no stable usb service");
		}
		return;
	}

	// ======================================================================
	// システムプロパティ指定 USB サービスのロード
	// ======================================================================
	/**
	 * システムプロパティで指定されている USB サービスをロードします。
	 * <p>
	 */
	private static void loadBySystemProperty(){
		String className = System.getProperty(SERVICE_PROPERTY_NAME, null);
		if(className != null && className.trim().length() != 0){
			logger.config("usb service specified: " + SERVICE_PROPERTY_NAME + "=" + className);
			USBService service = instantiate(className, "user spacified usb service");
			if(service != null){
				regist(service);
			}
		}
		return;
	}

	// ======================================================================
	// デフォルト USB サービスのロード
	// ======================================================================
	/**
	 * デフォルトのリソースから USB サービスを参照します。
	 * <p>
	 */
	private static void loadByDefaultResource(){
		String resourceName = "org.koiroha.usb.services";
		ResourceBundle res = ResourceBundle.getBundle(resourceName);
		String classNames = res.getString("default");

		// リソースから検索
		StringTokenizer tk = new StringTokenizer(classNames, ", \t\r\n");
		while(tk.hasMoreTokens()){
			String className = tk.nextToken();
			String serviceName = res.getString(className + ".name");
			USBService service = instantiate(className, serviceName);
			if(service != null){
				regist(service);
				break;
			}
		}
		return;
	}

	// ======================================================================
	// USB サービスのロード
	// ======================================================================
	/**
	 * JSE 6 でサポートされているサービスプロバイダ API を使用して USB サービスをロードします。
	 * このメソッドは J2SE 5.0 との互換性のため、実行環境でサービスプロバイダが利用可能な場合
	 * のみリフレクションを使用してロードを行います。
	 * <p>
	 */
	private static void loadByServiceLoader(){
		try{
			Class<?> clazz = Class.forName("java.util.ServiceLoader");
			Class<?> exception = Class.forName("java.util.ServiceConfigurationError");
			Method load = clazz.getMethod("load", Class.class);
			Object serviceLoader = load.invoke(null, USBService.class);
			Method iterator = clazz.getMethod("iterator");
			Iterator<?> it = (Iterator<?>)iterator.invoke(serviceLoader);
			while(it.hasNext()){
				try{
					USBService service = (USBService)it.next();
					regist(service);
					return;
				} catch(Error ex){
					if(! exception.isInstance(ex)){
						throw ex;
					}
					logger.log(Level.WARNING, "fail to load usb service", ex);
				}
			}
		} catch(Exception ex){
			logger.finer("service loader not supported in this environment: " + ex.toString());
		}
		return;
	}

	// ======================================================================
	// USB サービスのインスタンス化
	// ======================================================================
	/**
	 * 指定されたクラス名の USB サービスをインスタンス化します。インスタンス化に失敗した場合は
	 * ログに出力して null を返します。
	 * <p>
	 * @param className クラス名
	 * @param serviceName サービスの名前 (ログ出力用)
	 * @return USB サービスのインスタンス
	 */
	private static USBService instantiate(String className, String serviceName){
		try{
			return (USBService)Class.forName(className).newInstance();
		} catch(Exception ex){
			logger.log(Level.WARNING, "unexpected instantiation error", ex);
		} catch(UnsatisfiedLinkError ex){
			logger.fine(serviceName + " not available: " + ex);
		}
		return null;
	}

}
