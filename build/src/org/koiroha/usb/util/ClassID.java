/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: ClassID.java,v 1.4 2009/05/14 17:03:59 torao Exp $
*/
package org.koiroha.usb.util;

import java.io.*;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.*;

import org.koiroha.usb.LangID;
import org.koiroha.usb.desc.*;
import org.w3c.dom.*;
import org.xml.sax.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// ClassID: USB クラス ID
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB デバイスのクラスやベンダー ID から名称を参照するためのクラスです。
 * ID に対する名称を定義
 * した ClassID XML ファイルを作成しアプリケーションにバンドルすることで
 * <p>
 * 構築したインスタンスは不変です。複数のスレッドで共有することが出来ます。
 * <p>
 * @version usb4j 1.0 $Revision: 1.4 $ $Date: 2009/05/14 17:03:59 $
 * @author torao
 * @since 2009/05/03 Java2 SE 5.0
 * @see <a href="http://www.linux-usb.org/usb.ids">List of USB ID&#x27;s</a>
 */
public final class ClassID {

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ClassID.class.getName());

	// ======================================================================
	// 名前空間 URL
	// ======================================================================
	/**
	 * XML 形式の USB クラスデータベースを表す名前空間 URL {@value} です。
	 * <p>
	 */
	public static final String XMLNS = "http://www.koiroha.org/xmlns/usb4j/usbclassid";

	// ======================================================================
	// ベンダー名マップ
	// ======================================================================
	/**
	 * idVendor/idProduct に対する名前のマップです。
	 * <p>
	 */
	private final Map<ID,String> vendorName = new HashMap<ID,String>();

	// ======================================================================
	// クラス名マップ
	// ======================================================================
	/**
	 * デバイスクラスとクラス名のマップです。
	 * <p>
	 */
	private final Map<ID,String> className = new HashMap<ID,String>();

	// ======================================================================
	// 言語名マップ
	// ======================================================================
	/**
	 * 言語 ID に対する名前のマップです。
	 * <p>
	 */
	private final Map<ID,LANGID> langid = new HashMap<ID,LANGID>();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * 指定された入力ソースに基づいて構築を行います。
	 * <p>
	 * @param in ClassID XML の入力ソース
	 * @throws IOException ファイルの読み込みに失敗した場合
	 * @throws SAXException ファイル形式が不正な場合
	 */
	private ClassID(InputSource[] in) throws IOException, SAXException{
		for(int i=0; i<in.length; i++){
			parse(in[i]);
		}
		return;
	}

	// ======================================================================
	// ベンダー名の参照
	// ======================================================================
	/**
	 * 指定されたベンダー ID に対する名称を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param idVendor ベンダー ID
	 * @return ベンダー名
	 */
	public String getVendor(int idVendor){
		String name = vendorName.get(new ID(idVendor & 0xFFFF));
		if(name == null){
			return "";
		}
		return name;
	}

	// ======================================================================
	// ベンダー名の参照
	// ======================================================================
	/**
	 * 指定されたデバイス記述子の {@link DeviceDescriptor#getVendorId() idVendor}
	 * に対する名称を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param desc デバイス記述子
	 * @return ベンダー名
	 */
	public String getVendor(DeviceDescriptor desc){
		return getVendor(desc.getVendorId());
	}

	// ======================================================================
	// 製品名の参照
	// ======================================================================
	/**
	 * 指定されたベンダーID、製品IDに対する製品名称を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param idVendor ベンダー ID
	 * @param idProduct 製品 ID
	 * @return 製品名
	 */
	public String getProduct(int idVendor, int idProduct){
		String name = vendorName.get(new ID(idVendor & 0xFFFF, idProduct & 0xFFFF));
		if(name == null){
			return "";
		}
		return name;
	}

	// ======================================================================
	// 製品名の参照
	// ======================================================================
	/**
	 * 指定されたデバイス記述子の {@link DeviceDescriptor#getVendorId() idVendor}、
	 * {@link DeviceDescriptor#getProductId() idProduct} に対する名称を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param desc デバイス記述子
	 * @return 製品名
	 */
	public String getProduct(DeviceDescriptor desc){
		return getProduct(desc.getVendorId(), desc.getProductId());
	}

	// ======================================================================
	// クラス名の参照
	// ======================================================================
	/**
	 * 指定されたクラスIDに対するクラス名称を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param classId クラス ID
	 * @return ID に対する名前
	 */
	public String getClass(int classId){
		String name = className.get(new ID(classId & 0xFF));
		if(name == null){
			return "";
		}
		return name;
	}

	// ======================================================================
	// クラス名の参照
	// ======================================================================
	/**
	 * 指定されたデバイス記述子の {@link DeviceDescriptor#getDeviceClass() bDeviceClass}
	 * に対するデバイスクラス名称を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param desc デバイス記述子
	 * @return デバイスクラス名
	 */
	public String getClass(DeviceDescriptor desc){
		return getClass(desc.getDeviceClass());
	}

	// ======================================================================
	// クラス名の参照
	// ======================================================================
	/**
	 * 指定されたインターフェース記述子の {@link InterfaceDescriptor#getInterfaceClass() bInterfaceClass}
	 * に対するインターフェースクラス名称を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param desc インターフェース記述子
	 * @return インターフェースクラス名
	 */
	public String getClass(InterfaceDescriptor desc){
		return getClass(desc.getInterfaceClass());
	}

	// ======================================================================
	// サブクラス名の参照
	// ======================================================================
	/**
	 * 指定されたサブクラス ID に対するサブクラス名称を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param classId クラス ID
	 * @param subClassId サブクラス ID
	 * @return ID に対する名前
	 */
	public String getSubClass(int classId, int subClassId){
		String name = className.get(new ID(classId & 0xFF, subClassId & 0xFF));
		if(name == null){
			return "";
		}
		return name;
	}

	// ======================================================================
	// クラス名の参照
	// ======================================================================
	/**
	 * 指定されたデバイス記述子の {@link DeviceDescriptor#getDeviceClass() bDeviceClass}、
	 *  {@link DeviceDescriptor#getDeviceSubClass() bDeviceSubClass} に対する
	 *  デバイスサブクラス名を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param desc デバイス記述子
	 * @return デバイスサブクラス名
	 */
	public String getSubClass(DeviceDescriptor desc){
		return getSubClass(desc.getDeviceClass(), desc.getDeviceSubClass());
	}

	// ======================================================================
	// クラス名の参照
	// ======================================================================
	/**
	 * 指定されたインターフェース記述子の {@link InterfaceDescriptor#getInterfaceClass() bInterfaceClass}、
	 * {@link InterfaceDescriptor#getInterfaceSubClass() bInterfaceSubClass}
	 * に対するインターフェースクラス名を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param desc デバイス記述子
	 * @return デバイスクラス名
	 */
	public String getSubClass(InterfaceDescriptor desc){
		return getSubClass(desc.getInterfaceClass(), desc.getInterfaceSubClass());
	}

	// ======================================================================
	// プロトコル名の参照
	// ======================================================================
	/**
	 * 指定されたプロトコルIDに対するプロトコル名を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param classId クラス ID
	 * @param subClassId サブクラス ID
	 * @param protocol プロトコル
	 * @return ID に対する名前
	 */
	public String getProtocol(int classId, int subClassId, int protocol){
		String name = className.get(new ID(classId & 0xFF, subClassId & 0xFF, protocol & 0xFF));
		if(name == null){
			return "";
		}
		return name;
	}

	// ======================================================================
	// プロトコル名の参照
	// ======================================================================
	/**
	 * 指定されたデバイス記述子に対するデバイスプロトコル名を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param desc デバイス記述子
	 * @return デバイスサブクラス名
	 */
	public String getProtocol(DeviceDescriptor desc){
		return getProtocol(desc.getDeviceClass(), desc.getDeviceSubClass(), desc.getDeviceProtocol());
	}

	// ======================================================================
	// プロトコル名の参照
	// ======================================================================
	/**
	 * 指定されたインターフェース記述子に対するインターフェースプロトコル名を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param desc インターフェース記述子
	 * @return インターフェースプロトコル名
	 */
	public String getProtocol(InterfaceDescriptor desc){
		return getProtocol(desc.getInterfaceClass(), desc.getInterfaceSubClass(), desc.getInterfaceProtocol());
	}

	// ======================================================================
	// 言語名の参照
	// ======================================================================
	/**
	 * 指定された言語 ID に対する名称を参照します。
	 * 名称が定義されていない場合には長さ 0 の文字列を返します。
	 * <p>
	 * @param langid 言語 ID
	 * @return ID に対する名前
	 */
	public String getLanguage(int langid){
		LANGID lang = getLangID(langid);
		if(lang == null){
			return "";
		}
		return lang.label;
	}

	// ======================================================================
	// ロケールの参照
	// ======================================================================
	/**
	 * 指定された言語 ID に対するロケールを参照します。
	 * ロケールが不明な場合は null を返します。
	 * <p>
	 * @param langid 言語 ID
	 * @return ID に対する名前
	 */
	public Locale getLanguageLocale(int langid){
		LANGID lang = getLangID(langid);
		if(lang == null){
			return null;
		}
		return lang.locale;
	}

	// ======================================================================
	// 言語 ID の参照
	// ======================================================================
	/**
	 * 言語 ID を参照します。
	 * <p>
	 * @return 言語 ID のセット
	 */
	public Set<Short> getAvailableLangIDs(){
		Set<Short> s = new HashSet<Short>();
		for(ID id: langid.keySet()){
			if(id.id.length == 1){
				s.add(LangID.getLangID(id.id[0], 0x01));
			} else if(id.id.length == 2){
				s.add(LangID.getLangID(id.id[0], id.id[1]));
			} else {
				assert(false);
			}
		}
		return s;
	}

	// ======================================================================
	// 言語 ID の参照
	// ======================================================================
	/**
	 * 指定された言語 ID に対するオブジェクトを参照します。
	 * <p>
	 * @param langid 言語 ID
	 * @return ID に対するオブジェクト
	 */
	private LANGID getLangID(int langid){
		int primary = LangID.getPrimaryLanguage(langid);
		int sub = LangID.getSubLanguage(langid);
		LANGID lang = this.langid.get(new ID(primary, sub));
		if(lang == null){
			lang = this.langid.get(new ID(primary));
		}
		return lang;
	}

	// ======================================================================
	// インスタンスの参照
	// ======================================================================
	/**
	 * 指定された URL から読み出される ClassID XML に基づいたインスタンスを構築します。
	 * <p>
	 * @param url ClassID XML ファイルの URL
	 * @return ClassID のインスタンス
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ファイル形式が不正な場合
	 */
	public static final ClassID getClassID(URL... url) throws IOException, SAXException{
		InputSource[] is = new InputSource[url.length];
		for(int i=0; i<url.length; i++){
			is[i] = new InputSource(url[i].toString());
		}
		logger.finer("loading class id: " + url);
		return new ClassID(is);
	}

	// ======================================================================
	// インスタンスの参照
	// ======================================================================
	/**
	 * 指定された入力ストリームから読み出される ClassID XML に基づいたインスタンスを構築します。
	 * <p>
	 * @param in IDS ファイルの入力ストリーム
	 * @return IDS のインスタンス
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ファイル形式が不正な場合
	 */
	public static final ClassID getClassID(InputStream... in) throws IOException, SAXException{
		InputSource[] is = new InputSource[in.length];
		for(int i=0; i<in.length; i++){
			is[i] = new InputSource(in[i]);
		}
		return new ClassID(is);
	}

	// ======================================================================
	// インスタンスの参照
	// ======================================================================
	/**
	 * 指定された URL から読み出される IDS 形式ファイルに基づいたインスタンスを構築します。
	 * <p>
	 * @param url ClassID XML ファイルの URL
	 * @return ClassID のインスタンス
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ファイル形式が不正な場合
	 * @see <a href="http://www.linux-usb.org/usb.ids">www.linux-usb.org</a>
	 */
	public static final ClassID getClassIDFromIDS(URL... url) throws IOException, SAXException{
		InputSource[] is = new InputSource[url.length];
		Reader[] r = new Reader[url.length];
		try{
			for(int i=0; i<url.length; i++){
				r[i] = new Ids2XmlReader(new InputStreamReader(url[i].openStream(), "UTF-8"));
				is[i] = new InputSource(r[i]);
				is[i].setSystemId(url[i].toString());
			}
			logger.finer("loading class id: " + url);
			return new ClassID(is);
		} finally {
			for(int i=0; i<r.length; i++){
				try{
					if(r[i] != null)	r[i].close();
				} catch(IOException ex){/* */}
			}
		}
	}

	// ======================================================================
	// インスタンスの参照
	// ======================================================================
	/**
	 * 指定された入力ストリームから読み出される IDS 形式ファイルに基づいたインスタンスを構築します。
	 * <p>
	 * @param in ClassID XML ファイルの URL
	 * @return ClassID のインスタンス
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException ファイル形式が不正な場合
	 * @see <a href="http://www.linux-usb.org/usb.ids">www.linux-usb.org</a>
	 */
	public static final ClassID getClassIDFromIDS(InputStream... in) throws IOException, SAXException{
		InputSource[] is = new InputSource[in.length];
		for(int i=0; i<in.length; i++){
			is[i] = new InputSource(in[i]);
		}
		return new ClassID(is);
	}

	// ======================================================================
	// 変換ストリームの参照
	// ======================================================================
	/**
	 * 指定された IDS の入力ストリームから、透過的に ClassID XML を読み出す事の出来る変換
	 * 入力ストリームを参照します。
	 * <p>
	 * @param in IDS ファイルの入力ストリーム
	 * @return IDS ファイルに対する ClassID XML を読み出す事の出来る入力ストリーム
	 * @throws IOException 変換に失敗した場合
	 * @see <a href="http://www.linux-usb.org/usb.ids">www.linux-usb.org</a>
	 */
	public static final Reader getIDSToClassIDStream(Reader in) throws IOException{
		return new Ids2XmlReader(in);
	}

	// ======================================================================
	// ストリームの解析
	// ======================================================================
	/**
	 * 指定された入力ストリームから読み出される定義を解析します。
	 * <p>
	 * @param in ClassID XML の入力ソース
	 * @throws IOException 読み込みに失敗した場合
	 * @throws SAXException データ形式が不正な場合
	 */
	private void parse(InputSource in) throws IOException, SAXException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			parseChild(doc);
		} catch(ParserConfigurationException ex){
			throw new IllegalStateException(ex);
		}
		return;
	}

	// ======================================================================
	// 要素の解析
	// ======================================================================
	/**
	 * 指定された要素を解析します。
	 * <p>
	 * @param elem 解析する要素
	 */
	private void parse(Element elem){

		// 名前空間が一致しなければこの要素は解析しない
		if(! XMLNS.equals(elem.getNamespaceURI())){
			parseChild(elem);
			return;
		}

		if(elem.getLocalName().equals("vendor")){
			String label = elem.getAttribute("label");
			ID id = getID(elem, "vendor");
			vendorName.put(id, label);
			logger.finest("vendor[" + id + "] " + label);
		} else if(elem.getLocalName().equals("product")){
			String label = elem.getAttribute("label");
			ID id = getID(elem, "vendor", "product");
			vendorName.put(id, label);
			logger.finest("vendor[" + id + "] " + label);
		} else if(elem.getLocalName().equals("interface")){
			String label = elem.getAttribute("label");
			ID id = getID(elem, "vendor", "product", "interface");
			vendorName.put(id, label);
			logger.finest("vendor[" + id + "] " + label);
		} else if(elem.getLocalName().equals("class")){
			String label = elem.getAttribute("label");
			ID id = getID(elem, "class");
			className.put(id, label);
			logger.finest("class[" + id + "] " + label);
		} else if(elem.getLocalName().equals("subclass")){
			String label = elem.getAttribute("label");
			ID id = getID(elem, "class", "subclass");
			className.put(id, label);
			logger.finest("class[" + id + "] " + label);
		} else if(elem.getLocalName().equals("protocol")){
			String label = elem.getAttribute("label");
			ID id = getID(elem, "class", "subclass", "protocol");
			className.put(id, label);
			logger.finest("class[" + id + "] " + label);
		} else if(elem.getLocalName().equals("lang")){
			String label = elem.getAttribute("label");
			ID id = getID(elem, "lang");
			Locale locale = getLocale(elem.getAttribute("locale"));
			langid.put(id, new LANGID(label, locale));
			logger.finest("lang[" + id + "] " + label);
		} else if(elem.getLocalName().equals("dialect")){
			String label = elem.getAttribute("label");
			ID id = getID(elem, "lang", "dialect");
			Locale locale = getLocale(elem.getAttribute("locale"));
			langid.put(id, new LANGID(label, locale));
			logger.finest("lang[" + id + "] " + label);
		} else {
			logger.fine("unknown element: " + elem.getLocalName());
		}
		parseChild(elem);
		return;
	}

	// ======================================================================
	// 子要素の解析
	// ======================================================================
	/**
	 * 指定されたノードの子要素を再帰的に解析します。
	 * <p>
	 * @param node ノード
	 */
	private void parseChild(Node node){
		NodeList nl = node.getChildNodes();
		for(int i=0; i<nl.getLength(); i++){
			if(nl.item(i) instanceof Element){
				parse((Element)nl.item(i));
			}
		}
		return;
	}

	// ======================================================================
	// ロケールの参照
	// ======================================================================
	/**
	 * ロケールを参照します。
	 * <p>
	 * @param locale ロケール
	 * @return ロケール
	 */
	private static Locale getLocale(String locale){
		String[] l = locale.split("_");
		if(l.length == 1){
			return new Locale(l[0]);
		}
		if(l.length == 2){
			return new Locale(l[0], l[1]);
		}
		return null;
	}

	// ======================================================================
	// ID の参照
	// ======================================================================
	/**
	 * 指定された要素から親をたどって ID を参照します。親が不正な場合は null を返します。
	 * <p>
	 * @param target 開始する要素
	 * @param localName ローカル名のリスト
	 * @return 検出した ID
	 */
	private static ID getID(Element target, String... localName){
		List<Integer> id = new ArrayList<Integer>();
		Node mover = target;
		while(mover instanceof Element){
			Element elem = (Element)mover;
			if(! XMLNS.equals(elem.getNamespaceURI())){
				continue;
			}
			if(! localName[localName.length - id.size() - 1].equals(elem.getLocalName())){
				return null;
			}
			id.add(0, Integer.parseInt(elem.getAttribute("id"), 16));
			if(id.size() == localName.length){
				int[] array = new int[id.size()];
				for(int i=0; i<id.size(); i++){
					array[i] = id.get(i);
				}
				return new ID(array);
			}
			mover = mover.getParentNode();
		}
		return null;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ID: ID クラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * クラス/サブクラス/プロトコルをキーとして使用するためのクラスです。
	 * <p>
	 */
	private static class ID{

		// ==================================================================
		// ID 数値
		// ==================================================================
		/**
		 * この ID を表す数値です。
		 * <p>
		 */
		private final int[] id;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * ID を指定して構築を行います。
		 * <p>
		 * @param id ID
		 */
		public ID(int... id){
			this.id = id;
			return;
		}

		// ==================================================================
		// ハッシュ値の参照
		// ==================================================================
		/**
		 * ハッシュ値を参照します。
		 * <p>
		 * @return ハッシュ値
		 */
		@Override
		public int hashCode(){
			int hash = 0;
			for(int i=0; i<id.length; i++){
				hash += id.length;
			}
			return hash;
		}

		// ==================================================================
		// 等価判定
		// ==================================================================
		/**
		 * 指定されたインスタンスと等しいかどうかを判定します。
		 * <p>
		 * @param o 比較するオブジェクト
		 */
		@Override
		public boolean equals(Object o){
			if(! (o instanceof ID)){
				return false;
			}
			ID other = (ID)o;
			return Arrays.equals(this.id, other.id);
		}

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
			StringBuilder buffer = new StringBuilder();
			for(int i=0; i<id.length; i++){
				if(i != 0){
					buffer.append(':');
				}
				if(id[i] < 0){
					buffer.append('-');
				} else {
					buffer.append(Integer.toHexString(id[i]).toUpperCase());
				}
			}
			return buffer.toString();
		}

	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ID: ID クラス
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * クラス/サブクラス/プロトコルをキーとして使用するためのクラスです。
	 * <p>
	 */
	private static class LANGID {

		// ==================================================================
		// ラベル
		// ==================================================================
		/**
		 * 言語 ID のラベルです。
		 */
		private final String label;

		// ==================================================================
		// ロケール
		// ==================================================================
		/**
		 * 言語 ID に対するロケールです。
		 */
		private final Locale locale;

		// ==================================================================
		// コンストラクタ
		// ==================================================================
		/**
		 * ラベルとロケールを指定して構築を行います。
		 * <p>
		 * @param label ラベル
		 * @param locale ロケール
		 */
		public LANGID(String label, Locale locale){
			this.label = label;
			this.locale = locale;
			return;
		}

	}

}
