/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: DeviceTreeModel.java,v 1.14 2009/05/18 20:34:18 torao Exp $
*/
package org.koiroha.usb.tools.explorer;

import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.event.*;
import org.koiroha.usb.tools.Resource;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// DeviceTreeModel: デバイスツリーモデル
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * USB デバイスの論理構造をツリー表示するためのモデルです。
 * <p>
 * @version usb4j 1.0 $Revision: 1.14 $ $Date: 2009/05/18 20:34:18 $
 * @author torao
 * @since 2009/04/29 Java2 SE 5.0
 */
public class DeviceTreeModel implements TreeModel {

	// ======================================================================
	// リソース
	// ======================================================================
	/**
	 * このパッケージのリソースです。
	 * <p>
	*/
	private static final Resource RS = new Resource(DeviceTreeModel.class);

	// ======================================================================
	// ツリーリスナー
	// ======================================================================
	/**
	 * ツリーリスナです。
	 * <p>
	*/
	private final java.util.List<TreeModelListener> listener = new ArrayList<TreeModelListener>();

	// ======================================================================
	// USB コンテキスト
	// ======================================================================
	/**
	 * USB コンテキストです。
	 * <p>
	*/
	private USBContext session = null;

	// ======================================================================
	// バス一覧
	// ======================================================================
	/**
	 * バスの一覧です。
	 * <p>
	*/
	private java.util.List<Bus> busses = null;

	// ======================================================================
	// デバイス一覧
	// ======================================================================
	/**
	 * デバイス一覧です。
	 * <p>
	*/
	private java.util.List<Device> devices = null;

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 */
	public DeviceTreeModel() {
		return;
	}

	// ======================================================================
	// デバイスのリリース
	// ======================================================================
	/**
	 * モデルが保持しているすべてのデバイスをリリースします。
	 * <p>
	 */
	public void release(){
		if(devices != null){

			// 全削除をリスナに通知
			TreeModelEvent e = new TreeModelEvent(this, new Object[]{getRoot()});
			for(TreeModelListener l: listener){
				l.treeStructureChanged(e);
			}
		}
		return;
	}

	// ======================================================================
	// デバイスの再読込
	// ======================================================================
	/**
	 * USB デバイスを再読込します。
	 * <p>
	 * @param async 非同期で再読込する場合 true
	 */
	public void refresh(boolean async){

		// 現在のデバイスを全てリリース
		release();

		// 別スレッドで実行
		Thread thread = new Thread(){
			@Override
			public void run(){
				refresh();
				return;
			}
		};
		if(async){
			thread.start();
		} else {
			thread.run();
		}

		return;
	}

	// ======================================================================
	// デバイスの再読込
	// ======================================================================
	/**
	 * USB デバイスを再読込します。
	 * <p>
	 */
	private void refresh(){
		try{

			// コンテキストの同期化
			if(session == null){
				session = USBServiceManager.getContext();
			} else {
				session.sync();
			}

			// デバイス構成の再読込
			devices = new ArrayList<Device>(session.lookup());

		} catch(USBException ex){
			ex.printStackTrace();
		}

		// リスナの設定
		for(Device d: devices){
			d.addDeviceListener(new DeviceListener(){
				public void deviceOpened(DeviceEvent e) {/* */}
				public void deviceClosed(DeviceEvent e) {/* */}
				public void deviceReleased(DeviceEvent e) {
					reject(e.getDevice());
					return;
				}
			});
		}

		// バスの参照
		busses = new ArrayList<Bus>();
		for(int i=0; i<devices.size(); i++){
			if(! busses.contains(devices.get(i).getBus())){
				busses.add(devices.get(i).getBus());
			}
		}

		// 再読込を通知
		Runnable r = new Runnable(){
			public void run(){
				TreeModelEvent e = new TreeModelEvent(this, new Object[]{devices});
				for(TreeModelListener l: listener){
					l.treeStructureChanged(e);
				}
				return;
			}
		};
		SwingUtilities.invokeLater(r);
		return;
	}

	// ======================================================================
	// 状態の更新
	// ======================================================================
	/**
	 * 指定されたノードの状態が更新された通知を受けます。
	 * <p>
	 * @param path 状態の変わったノードへのパス
	 */
	public void changeState(TreePath path){
		TreeModelEvent e = new TreeModelEvent(this, path);
		for(TreeModelListener l: listener){
			l.treeNodesChanged(e);
		}
		return;
	}

	// ======================================================================
	// ルートノードの参照
	// ======================================================================
	/**
	 * ルートノードを返します。
	 * <p>
	 * @return ルートノード
	 */
	public Object getRoot() {
		if(busses == null){
			busses = new ArrayList<Bus>();		// ※後続処理で再refreshが行われないように非null値にしておく
			refresh(true);
		}
		return busses;
	}

	// ======================================================================
	// 子ノード数の参照
	// ======================================================================
	/**
	 * 子ノード数を参照します。
	 * <p>
	 * @param parent 親ノード
	 * @return 子ノードの数
	 */
	public int getChildCount(Object parent) {

		// ルートのデバイス一覧を示す場合
		if(parent instanceof List<?>){
			return ((List<?>)parent).size();
		}

		// バスを示す場合はデバイスの数を返す
		if(parent instanceof Bus){
			Bus bus = (Bus)parent;
			return bus.getDevices().size();
		}

		// デバイスを示す場合はコンフィギュレーションの数を返す
		if(parent instanceof Device){
			Device device = (Device)parent;
			return device.getConfigurations().size();
		}

		// コンフィギュレーションを示す場合はインターフェース数＋追加のを返す
		if(parent instanceof Configuration){
			Configuration conf = (Configuration)parent;
			return conf.getInterfaces().size() + conf.getAdditionalDescriptor().size();
		}

		// インターフェースを示す場合は代替設定の数を返す
		if(parent instanceof Interface){
			Interface intf = (Interface)parent;
			return intf.getAltSettings().size();
		}

		// 代替設定を示す場合はエンドポイントの数を返す
		if(parent instanceof AltSetting){
			AltSetting alt = (AltSetting)parent;
			return alt.getEndpoints().size() + alt.getAdditionalDescriptor().size();
		}

		// エンドポイントの場合は追加の記述子の数を返す
		if(parent instanceof Endpoint){
			Endpoint edpt = (Endpoint)parent;
			return edpt.getAdditionalDescriptor().size();
		}

		// なぜか末端ノードに対して呼ばれることがある
		if(parent instanceof Descriptor){
			return 0;
		}

		assert(false): parent;
		return 0;
	}

	// ======================================================================
	// 子ノードの参照
	// ======================================================================
	/**
	 * 子ノードを参照します。
	 * <p>
	 * @param parent 親ノード
	 * @param index インデックス
	 * @return 子ノード
	 */
	public Object getChild(Object parent, int index) {

		// バスを参照
		if(parent instanceof List<?>){
			return ((List<?>)parent).get(index);
		}

		// デバイスを参照
		if(parent instanceof Bus){
			return ((Bus)parent).getDevices().get(index);
		}

		// コンフィギュレーションを参照
		if(parent instanceof Device){
			return ((Device)parent).getConfigurations().get(index);
		}

		// インターフェースを参照
		if(parent instanceof Configuration){
			Configuration conf = (Configuration)parent;
			if(index < conf.getAdditionalDescriptor().size()){
				return conf.getAdditionalDescriptor().get(index);
			}
			return conf.getInterfaces().get(index - conf.getAdditionalDescriptor().size());
		}

		// 代替設定を参照
		if(parent instanceof Interface){
			Interface intf = (Interface)parent;
			return intf.getAltSettings().get(index);
		}

		// エンドポイントの参照
		if(parent instanceof AltSetting){
			AltSetting alt = (AltSetting)parent;
			if(index < alt.getAdditionalDescriptor().size()){
				return alt.getAdditionalDescriptor().get(index);
			}
			return alt.getEndpoints().get(index - alt.getAdditionalDescriptor().size());
		}

		// エンドポイントの追加記述子の参照
		if(parent instanceof Endpoint){
			Endpoint edpt = (Endpoint)parent;
			return edpt.getAdditionalDescriptor().get(index);
		}

		assert(false);
		return null;
	}

	// ======================================================================
	// インデックスの参照
	// ======================================================================
	/**
	 * 指定されたノードのインデックスを参照します。
	 * <p>
	 * @param parent 親ノード
	 * @param child 子ノード
	 * @return 子ノードのインデックス
	 */
	public int getIndexOfChild(Object parent, Object child) {
		if(parent instanceof List){
			return ((List<?>)parent).indexOf(child);
		}
		if(parent instanceof Bus){
			return ((Bus)parent).getDevices().indexOf(child);
		}
		if(parent instanceof Device){
			return ((Device)parent).getConfigurations().indexOf(child);
		}
		if(parent instanceof Configuration){
			return ((Configuration)parent).getInterfaces().indexOf(child);
		}
		if(parent instanceof Interface){
			return ((Interface)parent).getAltSettings().indexOf(child);
		}
		if(parent instanceof AltSetting){
			return ((AltSetting)parent).getEndpoints().indexOf(child);
		}
		return 0;
	}

	// ======================================================================
	// 末端ノードの判定
	// ======================================================================
	/**
	 * 指定されたノードがエンドポイントを示す場合に true を返します。
	 * <p>
	 * @param node 判定するノード
	 * @return 末端の場合 true
	 */
	public boolean isLeaf(Object node) {
		return (node instanceof Descriptor);
	}

	// ======================================================================
	// 値変更の通知
	// ======================================================================
	/**
	 * GUI から値が変更された時に呼び出されます。何も行いません。
	 * <p>
	 * @param path 変更の行われたノードのパス
	 * @param newValue 新しい値
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		return;
	}

	// ======================================================================
	// ツリーモデルリスナの追加
	// ======================================================================
	/**
	 * ツリーモデルリスナを追加します。
	 * <p>
	 * @param l ツリーモデルリスナ
	 */
	public void addTreeModelListener(TreeModelListener l) {
		listener.add(l);
		return;
	}

	// ======================================================================
	// ツリーモデルリスナの削除
	// ======================================================================
	/**
	 * ツリーモデルリスナを削除します。
	 * <p>
	 * @param l ツリーモデルリスナ
	 */
	public void removeTreeModelListener(TreeModelListener l) {
		listener.remove(l);
		return;
	}

	// ======================================================================
	// デバイスの除去
	// ======================================================================
	/**
	 * 指定されたデバイスをモデルから除去します。
	 * <p>
	 * @param device 除去するデバイス
	 */
	private void reject(Device device){
		devices.remove(device);
		TreePath path = new TreePath(new Object[]{getRoot(), device.getBus()});
		int[] indices = new int[]{getIndexOfChild(device.getBus(), device)};
		Object[] children = new Object[]{device};
		TreeModelEvent e = new TreeModelEvent(this, path, indices, children);
		for(int i=0; i<listener.size(); i++){
			listener.get(i).treeNodesRemoved(e);
		}
		return;
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Renderer: レンダラ
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * USB 論理構成をツリー表示するためのレンダラクラスです。
	 * <p>
	 */
	public static class Renderer extends DefaultTreeCellRenderer{

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
		 * @param tree ツリービュー
		 * @param value 表示する値
		 * @param sel 選択されている場合 true
		 * @param expanded 開かれている場合 true
		 * @param leaf 末端の場合 true
		 * @param row 行番号
		 * @param hasFocus フォーカスを持っている場合 true
		 * @return 描画用のコンポーネント
		*/
		@Override
		public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			Object node = value;

			this.setFont(tree.getFont());
			this.setForeground(tree.getForeground());
			this.setBackground(tree.getBackground());

			String text = "";
			if(value instanceof List){
				try{
					text = InetAddress.getLocalHost().getHostName();
				} catch(UnknownHostException ex){
					text = "My Computer";
				}
			} else if(value instanceof Bus){
				Bus bus = (Bus)value;
				text = RS.format("bus", bus.getName());
			} else if(value instanceof Device){
				Device device = (Device)value;
				DeviceDescriptor desc = device.getDescriptor();
				text = USB.getDeviceClass(desc.getDeviceClass()) + " (" + Resource.getClassID().getVendor(desc.getVendorId()) + ")";
			} else if(value instanceof Configuration){
				Configuration conf = (Configuration)value;
				text = "Configuration #" + conf.getDescriptor().getConfigurationValue();
			} else if(value instanceof Interface){
				Interface intf = (Interface)value;
				InterfaceDescriptor desc = intf.getAltSettings().get(0).getDescriptor();
				text = "Interface #" + desc.getInterfaceNumber();
			} else if(value instanceof AltSetting){
				AltSetting alt = (AltSetting)value;
				text = "AltSetting #" + alt.getAltSetting();
			} else if(value instanceof Endpoint){
				Endpoint edpt = (Endpoint)value;
				text = "Endpoint #" + edpt.getDescriptor().getEndpointNumber()
					+ " (" + edpt.getDescriptor().getTransferType() + "," + edpt.getDescriptor().getDirection() + ")";
			} else if(value instanceof Descriptor){
				text = value.getClass().getSimpleName();
			}

			// デフォルトの設定
			super.getTreeCellRendererComponent(tree, text, sel, expanded, leaf, row, hasFocus);

			// アイコンの設定
			if(value instanceof Endpoint){
				setIcon(RS.getIcon("pipe.png"));
			}

			java.awt.Font font = this.getFont();
			if(font != null){

				// ルートを示す場合はボールドで表示
				if(node instanceof List){
					this.setFont(font.deriveFont(java.awt.Font.BOLD));
				} else {
					this.setFont(font.deriveFont(java.awt.Font.PLAIN));
				}

				// デバイスがコンフィギュレーション記述子の取得に失敗している場合
				if(node instanceof Device){
					Device device = (Device)node;
					DeviceDescriptor desc = device.getDescriptor();
					if(desc.getNumConfigurations() != device.getConfigurations().size()){
						this.setForeground(java.awt.Color.red.darker().darker()	);
						this.setFont(font.deriveFont(java.awt.Font.ITALIC));
					} else if(device.isOpen()){
						this.setForeground(java.awt.Color.GREEN.darker().darker());
					}
				}
			}

			return this;
		}
	}

}
