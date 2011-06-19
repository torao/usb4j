/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: PropertyTableModel.java,v 1.12 2009/05/21 12:02:55 torao Exp $
*/
package org.koiroha.usb.tools.explorer;

import java.awt.Component;
import java.nio.ByteBuffer;
import java.util.*;

import javax.swing.JTable;
import javax.swing.table.*;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.tools.Resource;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// PropertyTableModel: プロパティテーブルモデル
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB デバイスのプロパティ値を表示するためのテーブルモデルです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.12 $ $Date: 2009/05/21 12:02:55 $
 * @author torao
 * @since 2009/05/01 Java2 SE 5.0
 */
public class PropertyTableModel extends AbstractTableModel {

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// リソース
	// ======================================================================
	/**
	 * このパッケージのリソースです。
	 * <p>
	*/
	private static final Resource RS = new Resource(DeviceTreeModel.class);

	// ======================================================================
	// プロパティ
	// ======================================================================
	/**
	 * プロパティを格納するリストです。
	 * <p>
	 */
	private final List<String[]> prop = new ArrayList<String[]>();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public PropertyTableModel() {
		return;
	}

	// ======================================================================
	// ディスクリプタの設定
	// ======================================================================
	/**
	 * ディスクリプタを設定します。記述子に null を指定すると表示をクリアします。
	 * <p>
	 * @param device デバイス
	 * @param desc ディスクリプタ
	 */
	public void setDescriptor(Device device, Descriptor desc){

		// テーブルをクリア
		if(prop.size() > 0){
			fireTableRowsDeleted(0, prop.size()-1);
		}
		prop.clear();
		if(desc == null){
			return;
		}

		// 基本情報
		add("bLength", desc.getLength(), 1, RS.format("bytes", desc.getLength()));
		add("bDescriptor", desc.getDescriptorType(), 1, getDescriptorType(desc.getDescriptorType()));

		// デバイス記述子の場合
		if(desc instanceof DeviceDescriptor){
			DeviceDescriptor d = (DeviceDescriptor)desc;
			add("bcdUSB", d.getUSBSpecification(), 2, String.format("USB %1.1f", getBCD(d.getUSBSpecification()) / 100.0));
			add("bDeviceClass", d.getDeviceClass(), 1, Resource.getClassID().getClass(d));
			add("bDeviceSubClass", d.getDeviceSubClass(), 1, Resource.getClassID().getSubClass(d));
			add("bDeviceProtocol", d.getDeviceProtocol(), 1, Resource.getClassID().getProtocol(d));
			add("bMaxPacketSize0", d.getMaxPacketSize(), 1, RS.format("bytes", d.getMaxPacketSize()));
			add("idVendor", d.getVendorId(), 2, Resource.getClassID().getVendor(d));
			add("idProduct", d.getProductId(), 2, Resource.getClassID().getProduct(d));
			add("bcdDevice", d.getDeviceRelease(), 2, String.format("Release %d", getBCD(d.getDeviceRelease())));
			add("iManufacturer", d.getManufacturerSDIX(), 1, getString(device, d.getManufacturerSDIX()));
			add("iProduct", d.getProductSDIX(), 1, getString(device, d.getProductSDIX()));
			add("iSerialNumber", d.getSerialNumberSDIX(), 1, getString(device, d.getSerialNumberSDIX()));
			add("bNumConfigurations", d.getNumConfigurations(), 1, String.format("%d", d.getNumConfigurations()));
			addExtra(desc);
			fireTableRowsInserted(0, prop.size()-1);
			return;
		}

		// コンフィギュレーション記述子の場合
		if(desc instanceof ConfigurationDescriptor){
			ConfigurationDescriptor c = (ConfigurationDescriptor)desc;
			add("wTotalLength", c.getTotalLength(), 2, RS.format("bytes", c.getTotalLength()));
			add("bNumInterfaces", c.getNumInterface(), 1, String.format("%d", c.getNumInterface()));
			add("bConfigurationValue", c.getConfigurationValue(), 1, String.format("%d", c.getConfigurationValue()));
			add("iConfiguration", c.getConfigurationSDIX(), 1, getString(device, c.getConfigurationSDIX()));
			add("bmAttributes", c.getAttributes(), 1,
				(c.isSelfPower()? "self power": "") + (c.isSelfPower() && c.isRemoteWakeup()? ", ": "") + (c.isRemoteWakeup()? "remote wakeup": ""));
			add("MaxPower", c.getMaxPower()/2, 1, String.format("%dmA", c.getMaxPower()));
			addExtra(desc);
			fireTableRowsInserted(0, prop.size()-1);
			return;
		}

		// インターフェース記述子の場合
		if(desc instanceof InterfaceDescriptor){
			InterfaceDescriptor i = (InterfaceDescriptor)desc;
			add("bInterfaceNumber", i.getInterfaceNumber(), 1, String.valueOf(i.getInterfaceNumber()));
			add("bAlternateSetting", i.getAlternateSetting(), 1, "");
			add("bNumEndpoints", i.getNumEndpoint(), 1, String.format("%d", i.getNumEndpoint()));
			add("bInterfaceClass", i.getInterfaceClass(), 1, Resource.getClassID().getClass(i));
			add("bInterfaceSubClass", i.getInterfaceSubClass(), 1, Resource.getClassID().getClass(i));
			add("bInterfaceProtocol", i.getInterfaceProtocol(), 1, Resource.getClassID().getClass(i));
			add("iInterface", i.getInterfaceSDIX(), 1, getString(device, i.getInterfaceSDIX()));
			addExtra(desc);
			fireTableRowsInserted(0, prop.size()-1);
			return;
		}

		// エンドポイント記述子の場合
		if(desc instanceof EndpointDescriptor){
			EndpointDescriptor e = (EndpointDescriptor)desc;
			add("bEndpointAddress", e.getEndpointAddress(), 1, "#" + e.getEndpointNumber() + "," + e.getDirection());
			add("bmAttributes", e.getAttribute(), 1,
					e.getTransferType() + getIsochSyncType(e.getIsochSyncType()) + getIsochUsage(e.getIsochUsageType()));
			add("wMaxPacketSize", e.getRawMaxPacketSize(), 2,
					RS.format("bytes", e.getMaxPacketSize()) + ", " +
					RS.format("tranPerMf", 1+e.getAdditionalTransaction()));
			add("bInterval", e.getInterval(), 1, String.valueOf(e.getInterval()));
			if(e.getLength() >= 0x08){
				add("bRefresh", e.getRefresh(), 1, "");
			}
			if(e.getLength() >= 0x09){
				add("bSynchAddress", e.getSynchAddress(), 1, "");
			}
			addExtra(desc);
			fireTableRowsInserted(0, prop.size()-1);
			return;
		}

		// エンドポイント記述子の場合
		if(desc instanceof EndpointDescriptor){
			EndpointDescriptor e = (EndpointDescriptor)desc;
			add("bEndpointAddress", e.getEndpointAddress(), 1, "#" + e.getEndpointNumber() + "," + e.getDirection());
			add("bmAttributes", e.getAttribute(), 1,
					e.getTransferType() + getIsochSyncType(e.getIsochSyncType()) + getIsochUsage(e.getIsochUsageType()));
			add("wMaxPacketSize", e.getRawMaxPacketSize(), 2,
					RS.format("bytes", e.getMaxPacketSize()) + ", " +
					RS.format("tranPerMf", 1+e.getAdditionalTransaction()));
			add("bInterval", e.getInterval(), 1, String.valueOf(e.getInterval()));
			if(e.getLength() >= 0x08){
				add("bRefresh", e.getRefresh(), 1, "");
			}
			if(e.getLength() >= 0x09){
				add("bSynchAddress", e.getSynchAddress(), 1, "");
			}
			addExtra(desc);
			fireTableRowsInserted(0, prop.size()-1);
			return;
		}

		// インターフェース割り当て記述子の場合
		if(desc instanceof InterfaceAssociationDescriptor){
			InterfaceAssociationDescriptor e = (InterfaceAssociationDescriptor)desc;
			add("bFirstInterface", e.getFirstInterface(), 1, (e.getFirstInterface() & 0xFF) + "");
			add("bInterfaceCount", e.getInterfaceCount(), 1, (e.getInterfaceCount() & 0xFF) + "");
			add("bFunctionClass", e.getFunctionClass(), 1, "");
			add("bFunctionSubClass", e.getFunctionSubClass(), 1, "");
			add("bFunctionProtocol", e.getFunctionProtocol(), 1, "");
			add("iFunction", e.getFunctionSDIX(), 1, getString(device, e.getFunctionSDIX()));
			addExtra(desc);
			fireTableRowsInserted(0, prop.size()-1);
			return;
		}

		// 機能記述子の場合
		if(desc instanceof FunctionalDescriptor){
			FunctionalDescriptor e = (FunctionalDescriptor)desc;
			add("bDescriptorSubType", e.getDescriptorSubType(), 1, "");
			addExtra(desc);
			fireTableRowsInserted(0, prop.size()-1);
			return;
		}

		addExtra(desc);
		fireTableRowsInserted(0, prop.size()-1);
		return;
	}

	// ======================================================================
	// プロパティの設定
	// ======================================================================
	/**
	 * プロパティを設定します。
	 * <p>
	 * @param p プロパティ
	 */
	public void setProperty(Properties p){

		// テーブルをクリア
		if(prop.size() > 0){
			fireTableRowsDeleted(0, prop.size()-1);
		}
		prop.clear();

		SortedMap<String,String> map = new TreeMap<String, String>();
		for(Map.Entry<Object,Object> e: p.entrySet()){
			map.put(e.getKey().toString(), e.getValue().toString());
		}
		for(Map.Entry<String,String> e: map.entrySet()){
			prop.add(new String[]{e.getKey(), "", e.getValue()});
		}

		if(prop.size() > 0){
			fireTableRowsInserted(0, prop.size()-1);
		}
		return;
	}

	// ======================================================================
	// プロパティの追加
	// ======================================================================
	/**
	 * 指定されたプロパティ値を追加します。
	 * <p>
	 * @param name プロパティ名
	 * @param value プロパティの値 (数値)
	 * @param width プロパティ数値のビット幅
	 * @param label プロパティ値の文字列
	 */
	private void add(String name, int value, int width, String label){
		prop.add(new String[]{name, String.format("%0" + (width*2) + "X", value), label});
		return;
	}

	// ======================================================================
	// エクストラ領域の追加
	// ======================================================================
	/**
	 * エクストラ領域を追加します。
	 * <p>
	 * @param desc 記述子
	 */
	private void addExtra(Descriptor desc){
		ByteBuffer extra = desc.getExtraBinary();
		if(extra == null){
			return;
		}
		byte[] buf = new byte[extra.remaining()];
		extra.get(buf);

		// 追加領域の内容をプロパティに設定
		StringBuilder hex = new StringBuilder();
		StringBuilder ascii = new StringBuilder();
		for(int i=0; i<buf.length; i++){
			hex.append(String.format("%02X", buf[i] & 0xFF));
			if(buf[i] >= ' ' && buf[i] < 0x7F){
				ascii.append((char)buf[i]);
			} else {
				ascii.append('.');
			}
		}
		prop.add(new String[]{"<html><i>Extra", hex.toString(), ascii.toString()});
		return;
	}

	// ======================================================================
	// 文字列記述子の参照
	// ======================================================================
	/**
	 * 指定されたインデックスの文字列記述子を参照します。
	 * <p>
	 * @param device デバイス
	 * @param index 文字列記述子のインデックス
	 * @return 文字列記述子の値
	 */
	private String getString(Device device, int index){
		if(! device.isOpen()){
			return "";
		}
		if(index == 0){
			return "-";
		}
		try{
			return device.getString(index, LangID.JAPANESE);
		} catch(USBException ex){
			return "<html><i>" + ex.getMessage();
		}
	}

	// ======================================================================
	// 記述子タイプの参照
	// ======================================================================
	/**
	 * 指定された記述子のタイプを文字列として参照します.
	 * <p>
	 * @param type 記述子のタイプ
	 * @return 記述子の名前
	 */
	private static String getDescriptorType(int type){
		switch(type){
		case Descriptor.TYPE_DEVICE:
			return RS.format("desc.device");
		case Descriptor.TYPE_CONFIGURATION:
			return RS.format("desc.configuration");
		case Descriptor.TYPE_INTERFACE:
			return RS.format("desc.interface");
		case Descriptor.TYPE_ENDPOINT:
			return RS.format("desc.endpoint");
		case Descriptor.TYPE_INTERFACE_ASSOCIATION:
			return RS.format("desc.intfassoc");
		case Descriptor.TYPE_CS_INTERFACE:
		case Descriptor.TYPE_CS_ENDPOINT:
			return RS.format("desc.functional");
		}
		return RS.format("desc.unknown");
	}

	// ======================================================================
	// 等時間隔同期タイプの参照
	// ======================================================================
	/**
	 * 等時間隔同期タイプを参照します。
	 * <p>
	 * @param type 等時間隔同期タイプ
	 * @return 等時間隔同期タイプの文字列
	 */
	private static String getIsochSyncType(int type){
		switch(type){
		case 0:
			return "";
		case 1:
			return ", ASYNC";
		case 2:
			return ", ADAPTIVE";
		case 3:
			return ", SYNC";
		}
		return ", " + type;
	}

	// ======================================================================
	// 等時間隔使用の参照
	// ======================================================================
	/**
	 * 等時間隔使用乃文字列値を参照します。
	 * <p>
	 * @param type 等時間隔使用
	 * @return 等時間隔使用の文字列値
	 */
	private static String getIsochUsage(int type){
		switch(type){
		case 0:
			return ", Data Endpoint";
		case 1:
			return ", Feedback Endpoint";
		case 2:
			return ", Subordinate Endpoint";
		case 3:
			return "";
		}
		return ", " + type;
	}

	// ======================================================================
	// BCD 値の参照
	// ======================================================================
	/**
	 * 指定された値を BCD として整数値に変換します。
	 * <p>
	 * @param bcd BCD 値
	 * @return 整数値
	 */
	private static int getBCD(int bcd){
		return Integer.parseInt(String.format("%X", bcd));
	}

	// ======================================================================
	// カラム数の参照
	// ======================================================================
	/**
	 * カラム数を参照します。
	 * <p>
	 * @return カラム数
	 */
	public int getColumnCount() {
		return 3;
	}

	// ======================================================================
	// カラム名の参照
	// ======================================================================
	/**
	 * カラム名を参照します.
	 * <p>
	 * @return カラム名
	 */
	@Override
	public String getColumnName(int column) {
		switch(column){
		case 0:		return RS.format("column.name");
		case 1:		return RS.format("column.hex");
		case 2:		return RS.format("column.value");
		}
		return null;
	}

	// ======================================================================
	// 行数の参照
	// ======================================================================
	/**
	 * 行数を参照します。
	 * <p>
	 * @return 行数
	 */
	public int getRowCount() {
		return prop.size();
	}

	// ======================================================================
	// 値の参照
	// ======================================================================
	/**
	 * 値を参照します。
	 * <p>
	 * @param rowIndex 行
	 * @param columnIndex 列
	 * @return 値
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		return prop.get(rowIndex)[columnIndex];
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Renderer: レンダラ
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 記述子のプロパティを表示するためのレンダラクラスです。
	 * <p>
	 */
	public static class Renderer extends DefaultTableCellRenderer{

		// ==================================================================
		// シリアルバージョン
		// ==================================================================
		/**
		 * このクラスのシリアルバージョンです。
		 * <p>
		 */
		private static final long serialVersionUID = 1L;

		// ==================================================================
		// 描画コンポーネントの参照
		// ==================================================================
		/**
		 * 指定されたツリーノードを表示するためのコンポーネントを参照します。
		 * <p>
		 * @param table テーブル
		 * @param value 表示する値
		 * @param isSelected 選択されている場合 true
		 * @param hasFocus フォーカスを持っている場合 true
		 * @param row 行
		 * @param column 列
		 * @return 描画用のコンポーネント
		*/
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
		}
	}

}
