/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: Resource.java,v 1.7 2009/05/18 11:02:19 torao Exp $
*/
package org.koiroha.usb.tools;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import javax.swing.*;

import org.koiroha.usb.util.ClassID;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Resource: リソース用ユーティリティクラス
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * リソースを参照するためのユーティリティクラスです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.7 $ $Date: 2009/05/18 11:02:19 $
 * @author torao
 * @since 2009/05/02 Java2 SE 5.0
 */
public final class Resource {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Resource.class.getName());

	// ======================================================================
	// パッケージ名
	// ======================================================================
	/**
	 * このクラスのパッケージ名です。
	 * <p>
	 */
	private static ClassID classID = null;

	// ======================================================================
	// クラスIDの参照
	// ======================================================================
	/**
	 * クラスIDを参照します。
	 * <p>
	 * @return クラスID
	 */
	public static final ClassID getClassID(){
		if(classID != null){
			return classID;
		}
		Thread t = new Thread(){
			@Override
			public void run(){
				URL url = getResource("org/koiroha/usb/tools/usb.ids");
				try{
					classID = ClassID.getClassIDFromIDS(url);
				} catch(Exception ex){
					throw new RuntimeException(ex);
				}
			}
		};

		// ※EDT 内での実行の場合はスレッド内ログ表示で著しい速度低下が発生するため
		if(SwingUtilities.isEventDispatchThread()){
			t.start();
			try{ t.join(); } catch(InterruptedException ex){/* */}
		} else {
			t.run();
		}
		return classID;
	}

	// ======================================================================
	// パッケージパス
	// ======================================================================
	/**
	 * このクラスのパッケージパスです。
	 * <p>
	 */
	private final String packagePath;

	// ======================================================================
	// リソースバンドル
	// ======================================================================
	/**
	 * リソースバンドルです。
	 * <p>
	 */
	private final Map<String,String> res;

	// ======================================================================
	// リソースバンドル
	// ======================================================================
	/**
	 * パッケージ名に対するリソースバンドルです。
	 * <p>
	 */
	private static final Map<String,ResourceBundle> RESOURCE = Collections.synchronizedMap(new HashMap<String, ResourceBundle>());

	// ======================================================================
	// アイコンキャッシュ
	// ======================================================================
	/**
	 * アイコンのキャッシュです。
	 * <p>
	 */
	private static final Map<String,ImageIcon> ICON = Collections.synchronizedMap(new HashMap<String, ImageIcon>());

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * リソースを含むパッケージ名を指定して構築を行います。
	 * <p>
	 * @param packageName パッケージ名
	 */
	public Resource(String packageName) {
		this.packagePath = packageName.replace('.', '/');
		this.res = new HashMap<String, String>();
		try{
			ResourceBundle res = getResourceBundle(packageName);
			Enumeration<String> en = res.getKeys();
			while(en.hasMoreElements()){
				String name = en.nextElement();
				String value = res.getString(name);
				this.res.put(name, value);
			}
		} catch(MissingResourceException ex){
			logger.finer("package resource omitted: " + ex.getMessage());
		}
		return;
	}

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * リソースを含むパッケージのクラスを指定して構築を行います。
	 * <p>
	 * @param clazz クラス
	 */
	public Resource(Class<?> clazz) {
		this(getPackageName(clazz.getName()));
		return;
	}

	// ======================================================================
	// パッケージ名の参照
	// ======================================================================
	/**
	 * 指定されたクラス名に対するパッケージ名を参照します。
	 * <p>
	 * @param className クラス名
	 * @return パッケージ名
	 */
	private static String getPackageName(String className){

		// ※Class#getPackage() によるパッケージ取得は環境によって null を返す
		String pkg = "";
		int sep = className.lastIndexOf('.');
		if(sep >= 0){
			pkg = className.substring(0, sep);
		}
		return pkg;
	}

	// ======================================================================
	// 文字列の参照
	// ======================================================================
	/**
	 * リソースの文字列を参照します。
	 * <p>
	 * @param name リソース名
	 * @param def デフォルト値
	 * @return 文字列
	 */
	public String getString(String name, String def){
		if(res.containsKey(name)){
			return res.get(name);
		}
		logger.log(Level.SEVERE, "resource not found: " + name);
		return def;
	}

	// ======================================================================
	// 文字列の参照
	// ======================================================================
	/**
	 * リソースの文字列を参照します。
	 * <p>
	 * @param name リソース名
	 * @return 文字列
	 */
	public String getString(String name){
		return getString(name, "<html><font color='red'>ERROR");
	}

	// ======================================================================
	// メッセージのフォーマット
	// ======================================================================
	/**
	 * 指定されたメッセージをフォーマットします。
	 * <p>
	 * @param name リソース名
	 * @param args フォーマット引数
	 * @return フォーマットされたメッセージ
	 */
	public String format(String name, Object... args){
		return String.format(getString("msg." + name, "ERROR"), args);
	}

	// ======================================================================
	// アイコンの参照
	// ======================================================================
	/**
	 * 指定された名前のアイコンを参照します。
	 * <p>
	 * @param fileName アイコン画像のファイル名
	 * @return アイコン画像
	 */
	public ImageIcon getIcon(String fileName){
		String resourcePath = packagePath + "/" + fileName;
		return getIconResource(resourcePath);
	}

	// ======================================================================
	// ロケール用リソースの参照
	// ======================================================================
	/**
	 * 現在のロケール用に用意されたりソースを参照します。
	 * <p>
	 * @param fileName リソースファイル名
	 * @return リソースのURL
	 */
	public URL getLocaleResource(String fileName){
		return getLocaleResource(fileName, Locale.getDefault());
	}

	// ======================================================================
	// ロケール用リソースの参照
	// ======================================================================
	/**
	 * 指定されたロケール用のソース URL を参照します。
	 * <p>
	 * @param fileName リソースファイル名
	 * @param locale ロケール
	 * @return リソースのURL
	 */
	public URL getLocaleResource(String fileName, Locale locale){
		String resourcePath = packagePath + "/" + fileName;
		String base = resourcePath;
		String ext = "";
		int sep = resourcePath.lastIndexOf('.');
		if(sep >= 0){
			base = resourcePath.substring(0, sep);
			ext = resourcePath.substring(sep);
		}

		// 言語コード/国コードを指定してリソースを参照
		resourcePath = base + "_" + locale.getLanguage() + "_" + locale.getCountry() + ext;
		URL url = getResource(resourcePath);
		if(url != null){
			return url;
		}

		// 言語コードを指定してリソースを参照
		resourcePath = base + "_" + locale.getLanguage() + ext;
		url = getResource(resourcePath);
		if(url != null){
			return url;
		}

		// ロケール無指定でリソースを参照
		resourcePath = base + ext;
		return getResource(resourcePath);
	}

	// ======================================================================
	// リソースバンドルの参照
	// ======================================================================
	/**
	 * リソースバンドルを参照します。
	 * <p>
	 * @param packageName リソースバンドルのパッケージ名
	 * @return リソースバンドル
	 */
	private static ResourceBundle getResourceBundle(String packageName){
		ResourceBundle res = RESOURCE.get(packageName);
		if(res == null){
			res = ResourceBundle.getBundle(packageName + ".resource");
			RESOURCE.put(packageName, res);
		}
		return res;
	}

	// ======================================================================
	// アイコンの参照
	// ======================================================================
	/**
	 * 指定された名前のアイコンを参照します。
	 * <p>
	 * @param resourceName アイコン画像のファイル名
	 * @return アイコン画像
	 */
	private static ImageIcon getIconResource(String resourceName){
		ImageIcon icon = ICON.get(resourceName);
		if(icon == null){
			URL url = getResource(resourceName);
			if(url == null){
				logger.severe("icon resource not found: " + resourceName);
				return null;
			}
			icon = new ImageIcon(url);
			ICON.put(resourceName, icon);
		}
		return icon;
	}

	// ======================================================================
	// リソース URL の参照
	// ======================================================================
	/**
	 * リソース URL を参照します。
	 * <p>
	 * @param resourceName リソースファイル名
	 * @return リソースの URL
	 */
	private static URL getResource(String resourceName){
		URL url = Resource.class.getResource(resourceName);
		if(url == null){
			url = Resource.class.getResource("/" + resourceName);
		}
		return url;
	}

}
