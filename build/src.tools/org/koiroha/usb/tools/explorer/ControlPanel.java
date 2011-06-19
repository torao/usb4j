/* **************************************************************************
 * Copyright (C) 2009 koiroha.org All Right Reserved
 * **************************************************************************
 * This module, contains source code, binary and documentation, is in the
 * BSD License, and comes with NO WARRANTY.
 *
 *                                        takami torao <torao@mars.dti.ne.jp>
 *                                                     http://www.koiroha.org
 * $Id: ControlPanel.java,v 1.4 2009/05/21 12:02:55 torao Exp $
*/
package org.koiroha.usb.tools.explorer;

import static java.awt.GridBagConstraints.*;

import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.koiroha.usb.*;
import org.koiroha.usb.desc.*;
import org.koiroha.usb.tools.*;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// ControlPanel: コントロールパネル
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * デバイスのコントロールパネルです。
 * <p>
 * @version $Revision: 1.4 $ $Date: 2009/05/21 12:02:55 $
 * @author takami torao
 * @since usb4j 1.0 (Java2 SE 5.0) 2009/05/17
 */
public class ControlPanel extends JDialog{

	// ======================================================================
	// シリアルバージョン
	// ======================================================================
	/**
	 * このクラスのシリアルバージョンです。
	 * <p>
	 */
	private static final long serialVersionUID = 1L;

	// ======================================================================
	// ログ出力先
	// ======================================================================
	/**
	 * このクラスのログ出力先です。
	 * <p>
	 */
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ControlPanel.class.getName());

	// ======================================================================
	// リソース
	// ======================================================================
	/**
	 * このクラスのリソースです。
	 * <p>
	 */
	private static final Resource RS = new Resource(ControlPanel.class);

	// ======================================================================
	// デバイス
	// ======================================================================
	/**
	 * デバイスです。
	 * <p>
	 */
	private final Device device;

	// ======================================================================
	// コンフィギュレーション
	// ======================================================================
	/**
	 * 現在選択されているコンフィギュレーションです。
	 * <p>
	 */
	private Configuration curConf = null;

	// ======================================================================
	// インターフェース
	// ======================================================================
	/**
	 * 現在選択されているインターフェースです。
	 * <p>
	 */
	private Interface curIntf = null;

	// ======================================================================
	// 代替設定
	// ======================================================================
	/**
	 * 現在選択されている代替設定です。
	 * <p>
	 */
	private AltSetting curAlt = null;

	// ======================================================================
	// エンドポイント
	// ======================================================================
	/**
	 * 現在選択されているエンドポイントです。
	 * <p>
	 */
	private Endpoint curEdpt = null;

	// ======================================================================
	// デバイス機能
	// ======================================================================
	/**
	 * デバイス機能です。
	 * <p>
	 */
	private final JTextField devFeature = new JTextField(6);

	// ======================================================================
	// デバイスステータス
	// ======================================================================
	/**
	 * デバイスのステータスです。
	 * <p>
	 */
	private final JTextField devStatus = new JTextField(6);

	// ======================================================================
	// コンフィギュレーション
	// ======================================================================
	/**
	 * コンフィギュレーション選択用のコンボボックスです。
	 * <p>
	 */
	private final JComboBox conf = new JComboBox();

	// ======================================================================
	// コンフィギュレーション名
	// ======================================================================
	/**
	 * コンフィギュレーションの名前です。
	 * <p>
	 */
	private final JLabel confName = new JLabel();

	// ======================================================================
	// インターフェース
	// ======================================================================
	/**
	 * インターフェース選択用のコンボボックスです。
	 * <p>
	 */
	private final JComboBox intf = new JComboBox();

	// ======================================================================
	// インターフェース機能
	// ======================================================================
	/**
	 * インターフェース機能です。
	 * <p>
	 */
	private final JTextField intfFeature = new JTextField(6);

	/** インターフェース機能クリアボタン */
	private final JButton intfClearFeature = new JButton();
	/** インターフェース機能設定ボタン */
	private final JButton intfSetFeature = new JButton();
	/** インターフェースステータス参照ボタン */
	private final JButton intfGetStatus = new JButton();

	// ======================================================================
	// インターフェースステータス
	// ======================================================================
	/**
	 * インターフェースのステータスです。
	 * <p>
	 */
	private final JTextField intfStatus = new JTextField(6);

	/** インターフェース名 */
	private final JLabel intfName = new JLabel();

	// ======================================================================
	// インターフェース要求ボタン
	// ======================================================================
	/**
	 * インターフェースの要求ボタンです。
	 * <p>
	 */
	private final JButton claim = new JButton();

	// ======================================================================
	// 代替設定
	// ======================================================================
	/**
	 * 代替設定選択用のコンボボックスです。
	 * <p>
	 */
	private final JComboBox alt = new JComboBox();

	/** 代替設定のボタン */
	private final JButton altSet = new JButton();
	/** 代替設定のボタン */
	private final JButton altGet = new JButton();

	// ======================================================================
	// エンドポイント
	// ======================================================================
	/**
	 * エンドポイント選択用のコンボボックスです。
	 * <p>
	 */
	private final JComboBox edpt = new JComboBox();

	/** エンドポイント機能 */
	private final JTextField edptFeature = new JTextField();
	/** エンドポイント機能のクリア */
	private final JButton edptClearFeature = new JButton();
	/** エンドポイント機能の設定 */
	private final JButton edptSetFeature = new JButton();
	/** エンドポイントステータス */
	private final JTextField edptStatus = new JTextField();
	/** エンドポイントステータスの参照 */
	private final JButton edptGetStatus = new JButton();
	/** 同期フレーム */
	private final JTextField edptSynchFrame = new JTextField();
	/** 同期フレームの取得 */
	private final JButton edptGetSynchFrame = new JButton();
	/** 転送タイプ */
	private final JLabel edptTransferType = new JLabel();
	/** 転送方向 */
	private final JLabel edptDirection = new JLabel();

	/** 入出力バイナリ */
	private final JTextArea buffer = new JTextArea();
	/** 読み込みボタン */
	private final JButton read = new JButton();
	/** 書き込みボタン */
	private final JButton write = new JButton();

	/** コントロール転送方向 */
	private final JComboBox ctrlDir = new JComboBox();
	/** コントロール転送タイプ */
	private final JComboBox ctrlType = new JComboBox();
	/** コントロール転送対象 */
	private final JComboBox ctrlRcpt = new JComboBox();
	/** コントロール転送リクエストタイプ */
	private final JTextField ctrlRequestType = new JTextField();
	/** コントロール転送リクエスト */
	private final JTextField ctrlRequest = new JTextField();
	/** コントロール転送の値 */
	private final JTextField ctrlValue = new JTextField();
	/** コントロール転送のインデックス */
	private final JTextField ctrlIndex = new JTextField();

	// ======================================================================
	// コンストラクタ
	// ======================================================================
	/**
	 * コンストラクタは何も行いません。
	 * <p>
	 * @param frame 親のフレーム
	 * @param device デバイス
	 */
	public ControlPanel(Frame frame, Device device) {
		super(frame);
		this.device = device;

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(BorderLayout.CENTER, getPanel());
		this.setTitle(RS.getString("dialog.cp.title"));
		this.setModal(true);
		this.pack();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screen.width - getSize().width) / 2, (screen.height - getSize().height) / 2);
		return;
	}

	// ======================================================================
	// デバイスのリセット
	// ======================================================================
	/**
	 * デバイスをリセットします。
	 * <p>
	 */
	private void reset(){
		int result = JOptionPane.showConfirmDialog(this, RS.getString("dialog.cp.msg.reset"),
				RS.getString("msg.dialog.title.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if(result != JOptionPane.OK_OPTION){
			return;
		}
		try{
			device.reset();
			this.dispose();
		} catch(Exception ex){
			JOptionPane.showMessageDialog(this, ex.toString(),
					RS.getString("msg.dialog.title.error"), JOptionPane.ERROR_MESSAGE);
		}
		return;
	}

	// ======================================================================
	// コンフィギュレーションの設定
	// ======================================================================
	/**
	 * 指定されたコンフィギュレーション値のコンボボックスを選択状態にします。
	 * <p>
	 * @param c コンフィギュレーション値
	 */
	private void setConfiguration(int c){
		if(c == 0){
			conf.setSelectedIndex(0);
			return;
		}

		// 選択するインデックスを参照
		int i = 1;
		for(Configuration ci: device.getConfigurations()){
			if(ci.getDescriptor().getConfigurationValue() == c){
				break;
			}
			i ++;
		}

		// コンボボックスの設定
		if(i == conf.getItemCount()){
			JOptionPane.showMessageDialog(this, "invalid configuration returned", RS.getString("msg.dialog.title.error"), JOptionPane.ERROR_MESSAGE);
			conf.setSelectedIndex(0);
		} else {
			conf.setSelectedIndex(i);
		}
		return;
	}

	// ======================================================================
	// コンフィギュレーションの設定
	// ======================================================================
	/**
	 * 現在のコンフィギュレーションを設定します。
	 * <p>
	 * @param c 現在のコンフィギュレーション
	 */
	private void setCurrentConfiguration(Configuration c){
		curConf = c;
		intf.removeAllItems();
		if(c == null){
			confName.setText(getString(0));
			setCurrentInterface(null);
		} else {
			for(int j=0; j<c.getInterfaces().size(); j++){
				intf.addItem("#" + j);
			}
			String name = getString(c.getDescriptor().getConfigurationSDIX());
			confName.setText(name);
			setCurrentInterface(c.getInterfaces().get(0));
		}
		return;
	}

	// ======================================================================
	// インターフェースの設定
	// ======================================================================
	/**
	 * 現在のインターフェースを設定します。
	 * <p>
	 * @param i 現在のインターフェース
	 */
	private void setCurrentInterface(Interface i){
		curIntf = i;
		intf.setEnabled(i != null);
		claim.setEnabled(i != null);
		intfFeature.setEnabled(i != null);
		intfClearFeature.setEnabled(i != null);
		intfSetFeature.setEnabled(i != null);
		intfGetStatus.setEnabled(i != null);
		intfStatus.setEnabled(i != null);
		alt.setEnabled(i != null);
		altGet.setEnabled(i != null);
		altSet.setEnabled(i != null);
		alt.removeAllItems();
		if(i == null){
			setCurrentAltSetting(null);
		} else {
			for(int j=0; j<i.getAltSettings().size(); j++){
				alt.addItem("#" + j);
			}
			setCurrentAltSetting(i.getAltSettings().get(0));
		}
		return;
	}

	// ======================================================================
	// 代替設定の設定
	// ======================================================================
	/**
	 * 現在の代替設定を設定します。
	 * <p>
	 * @param a 現在の代替設定
	 */
	private void setCurrentAltSetting(AltSetting a){
		curAlt = a;
		edpt.removeAllItems();
		if(a == null){
			setCurrentEndpoint(null);
			intfName.setText(getString(0));
		} else {
			for(int i=0; i<a.getEndpoints().size(); i++){
				edpt.addItem("#" + a.getEndpoints().get(i).getEndpointNumber());
			}
			setCurrentEndpoint(a.getEndpoints().get(0));
			intfName.setText(getString(a.getDescriptor().getInterfaceSDIX()));
		}
		return;
	}

	// ======================================================================
	// エンドポイントの設定
	// ======================================================================
	/**
	 * 現在のエンドポイントを設定します。
	 * <p>
	 * @param e 現在のエンドポイント
	 */
	private void setCurrentEndpoint(Endpoint e){
		curEdpt = e;
		edpt.setEnabled(e != null);
		edptFeature.setEnabled(e != null);
		edptClearFeature.setEnabled(e != null);
		edptSetFeature.setEnabled(e != null);
		edptStatus.setEnabled(e != null);
		edptGetStatus.setEnabled(e != null);
		edptSynchFrame.setEnabled(e != null);
		edptGetSynchFrame.setEnabled(e != null);
		buffer.setEnabled(e != null);
		read.setEnabled(e != null);
		write.setEnabled(e != null);
		if(e != null){
			EndpointDescriptor desc = e.getDescriptor();
			edptTransferType.setText(desc.getTransferType().toString());
			edptDirection.setText(desc.getDirection().toString());
			if(desc.getDirection() == Direction.IN){
				write.setEnabled(false);
			} else {
				read.setEnabled(false);
			}
		} else {
			edptTransferType.setText("-");
			edptDirection.setText("-");
		}
		return;
	}

	// ======================================================================
	// インターフェースの要求
	// ======================================================================
	/**
	 * インターフェースを要求します。
	 * <p>
	 */
	private void claim(){
		try{
			if(! curIntf.isClaimed()){
				curIntf.claim();
				claim.setText(RS.getString("dialog.cp.intf.release"));
			} else {
				curIntf.release();
				claim.setText(RS.getString("dialog.cp.intf.claim"));
			}
		} catch(USBException ex){
			error(ex);
		}
		return;
	}

	// ======================================================================
	//
	// ======================================================================
	/**
	 * 文字列記述子テーブルを参照します。
	 * <p>
	 * @return 文字列記述子のテーブル
	 */
	private JPanel getPanel(){
		JPanel panel = new JPanel();

		Utils.layout(panel, getDevicePanel(),        0, 0, 1, 1, CENTER, BOTH, 1, 0);
		Utils.layout(panel, getConfigurationPanel(), 0, 1, 1, 1, CENTER, BOTH, 1, 0);
		Utils.layout(panel, getInterfacePanel(),     0, 2, 1, 1, CENTER, BOTH, 1, 0);
		Utils.layout(panel, getEndpointPanel(),      1, 0, 1, 4, CENTER, BOTH, 1, 0);

		// 文字列記述子の構築
		JTable sd = getStringDescriptor();
		Utils.layout(panel, new JScrollPane(sd), 0, RELATIVE, REMAINDER, 1, NORTHWEST, BOTH, 1, 1);
		return panel;
	}

	// ======================================================================
	// デバイスパネルの取得
	// ======================================================================
	/**
	 * デバイスパネルを取得します。
	 * <p>
	 * @return デバイスパネル
	 */
	private JPanel getDevicePanel(){
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Device"));

		// デバイス情報の構築
		DeviceDescriptor desc = device.getDescriptor();
		String manufacturer = getString(desc.getManufacturerSDIX());
		String product = getString(desc.getProductSDIX());
		String serialNumber = getString(desc.getSerialNumberSDIX());

		// 文字列記述子の設定
		Utils.layout(panel, new JLabel("iManufacturer"), 0, 0, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, new JLabel("iProduct"),      0, 1, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, new JLabel("iSerialNumber"), 0, 2, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, new JLabel(manufacturer),    1, 0, 4, 1, WEST, HORIZONTAL, 0, 0);
		Utils.layout(panel, new JLabel(product),         1, 1, 4, 1, WEST, HORIZONTAL, 0, 0);
		Utils.layout(panel, new JLabel(serialNumber),    1, 2, 4, 1, WEST, HORIZONTAL, 0, 0);

		// デバイス機能の設定
		JLabel label = new JLabel("Feature");
		devFeature.setText("0x0000");
		devFeature.setMinimumSize(devFeature.getPreferredSize());
		JButton clearFeature = new JButton("CLEAR");
		JButton setFeature = new JButton("SET");
		Utils.layout(panel, label,        0, 3, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, devFeature,   1, 3, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, clearFeature, 2, 3, 1, 1, WEST, HORIZONTAL, 0, 0);
		Utils.layout(panel, setFeature,   3, 3, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, new JPanel(), 4, 3, 1, 1, WEST, NONE, 1, 0);

		// デバイスステータスの設定
		label = new JLabel("Status");
		devStatus.setText("------");
		devStatus.setEditable(false);
		devStatus.setMinimumSize(devStatus.getPreferredSize());
		JButton getStatus = new JButton("GET");
		Utils.layout(panel, label,     0, 4, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, devStatus, 1, 4, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, getStatus, 2, 4, 1, 1, WEST, HORIZONTAL, 0, 0);

		// セパレータ
		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		sep.setMinimumSize(new Dimension(1, sep.getPreferredSize().height));
		Utils.layout(panel, sep, 0, 5, 5, 1, CENTER, HORIZONTAL, 1, 0, 0, 0, 0, 0);

		// リセットボタン
		JButton reset = new JButton(RS.getString("dialog.cp.reset"));
		Utils.layout(panel, reset, 0, 6, 5, 1, EAST, NONE, 0, 0, 0, 0, 0, 0);
		reset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ reset(); }
		});

		// デバイス機能のクリア
		clearFeature.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int feature = Integer.decode(devFeature.getText()).intValue();
					device.clearFeature(feature);
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		// デバイス機能の設定
		setFeature.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int feature = Integer.decode(devFeature.getText()).intValue();
					device.setFeature(feature);
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		// デバイスステータスの取得
		getStatus.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int status = device.getStatus();
					devStatus.setText(String.format("0x%04X", status));
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		return panel;
	}

	// ======================================================================
	// コンフィギュレーションパネルの取得
	// ======================================================================
	/**
	 * コンフィギュレーションパネルを構築します。
	 * <p>
	 * @return コンフィギュレーションパネル
	 */
	private JPanel getConfigurationPanel(){
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(RS.getString("dialog.cp.conf")));

		// コンフィギュレーション情報の構築
		JLabel label = new JLabel("Configuration");
		conf.addItem("--");
		for(Configuration c: device.getConfigurations()){
			conf.addItem("#" + c.getDescriptor().getConfigurationValue());
		}
		JButton set = new JButton("SET");
		JButton get = new JButton("GET");
		Utils.layout(panel, label, 0, 0, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, conf,  1, 0, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, set,   2, 0, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, get,   3, 0, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, new JPanel(), 4, 0, 1, 1, WEST, NONE, 1, 0);
		if(device.getConfigurations().size() == 0){
			set.setEnabled(false);
			conf.setEnabled(false);
		} else {
			setCurrentConfiguration(null);
		}

		label = new JLabel("iConfiguration");
		Utils.layout(panel, label,    0, 1, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, confName, 1, 1, 1, 1, WEST, HORIZONTAL, 0, 0);

		// コンフィギュレーションの設定
		set.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int i = conf.getSelectedIndex();
				int val = 0;
				if(i > 0){
					val = device.getConfigurations().get(i - 1).getDescriptor().getConfigurationValue();
				}
				try{
					device.setActiveConfiguration(val);
				} catch(USBException ex){
					error(ex);
				}
				return;
			}
		});

		// コンフィギュレーションの参照
		get.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int c = device.getActiveConfiguration(true);
					setConfiguration(c);
				} catch(USBException ex){
					error(ex);
				}
				return;
			}
		});

		// コンフィギュレーションの選択
		conf.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				int i = conf.getSelectedIndex();
				if(i == 0){
					setCurrentConfiguration(null);
				} else {
					setCurrentConfiguration(device.getConfigurations().get(i - 1));
				}
				return;
			}
		});

		return panel;
	}

	// ======================================================================
	// インターフェースパネルの取得
	// ======================================================================
	/**
	 * インターフェースパネルを構築します。
	 * <p>
	 * @return インターフェースパネル
	 */
	private JPanel getInterfacePanel(){
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(RS.getString("dialog.cp.intf")));

		// インターフェース情報の構築
		JLabel label = new JLabel("Interface");
		claim.setText(RS.getString("dialog.cp.intf.claim"));
		Utils.layout(panel, label, 0, 0, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, intf,  1, 0, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, claim, 2, 0, 1, 1, WEST, HORIZONTAL, 0, 0);

		// インターフェース機能の設定
		label = new JLabel("Feature");
		intfFeature.setText("0x0000");
		intfFeature.setMinimumSize(intfFeature.getPreferredSize());
		intfClearFeature.setText("CLEAR");
		intfSetFeature.setText("SET");
		Utils.layout(panel, label,            0, 1, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, intfFeature,      1, 1, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, intfClearFeature, 2, 1, 1, 1, WEST, HORIZONTAL, 0, 0);
		Utils.layout(panel, intfSetFeature,   3, 1, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, new JPanel(),     4, 1, 1, 1, WEST, NONE, 1, 0);

		// インターフェースステータスの設定
		label = new JLabel("Status");
		intfStatus.setText("------");
		intfStatus.setEditable(false);
		intfStatus.setMinimumSize(intfStatus.getPreferredSize());
		intfGetStatus.setText("GET");
		Utils.layout(panel, label,          0, 2, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, intfStatus,     1, 2, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, intfGetStatus,  2, 2, 1, 1, WEST, HORIZONTAL, 0, 0);

		// 代替設定情報の構築
		label = new JLabel(RS.getString("dialog.cp.alt"));
		altSet.setText("SET");
		altGet.setText("GET");
		Utils.layout(panel, label,  0, 3, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, alt,    1, 3, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, altSet, 2, 3, 1, 1, WEST, HORIZONTAL, 0, 0);
		Utils.layout(panel, altGet, 3, 3, 1, 1, WEST, HORIZONTAL, 0, 0);

		// 代替設定情報の構築
		label = new JLabel("iInterface");
		Utils.layout(panel, label,    0, 4, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, intfName, 1, 4, 3, 1, WEST, HORIZONTAL, 1, 0);

		// インターフェース機能のクリア
		intfClearFeature.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int feature = Integer.decode(intfFeature.getText()).intValue();
					curIntf.clearFeature(feature);
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		// インターフェース機能の設定
		intfSetFeature.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int feature = Integer.decode(intfFeature.getText()).intValue();
					curIntf.setFeature(feature);
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		// インターフェースステータスの参照
		intfGetStatus.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int status = curIntf.getStatus();
					intfStatus.setText(String.format("0x%04X", status));
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		// インターフェースの要求
		claim.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				claim();
				return;
			}
		});

		// インターフェースの選択
		intf.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				int i = intf.getSelectedIndex();
				if(i < 0){
					setCurrentInterface(null);
				} else {
					Interface intf = curConf.getInterfaces().get(i);
					setCurrentInterface(intf);
				}
				return;
			}
		});

		// 代替設定の設定
		altSet.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				AltSetting a = curIntf.getAltSettings().get(alt.getSelectedIndex());
				try{
					curIntf.setActiveAltSetting(a.getAltSetting());
					setCurrentAltSetting(a);
				} catch(USBException ex){
					error(ex);
				}
				return;
			}
		});

		// 代替設定の参照
		altGet.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try{
					int a = curIntf.getActiveAltSetting(true);
					for(int i=0; i<curIntf.getAltSettings().size(); i++){
						if(curIntf.getAltSettings().get(i).getAltSetting() == a){
							alt.setSelectedIndex(i);
							setCurrentAltSetting(curIntf.getAltSettings().get(i));
							return;
						}
					}
					JOptionPane.showMessageDialog(ControlPanel.this, RS.format("dialog.cp.alt.invalid", a), RS.format("dialog.title.error"), JOptionPane.ERROR_MESSAGE);
				} catch(USBException ex){
					error(ex);
				}
				return;
			}
		});

		return panel;
	}

	// ======================================================================
	// コントロール転送パネルの取得
	// ======================================================================
	/**
	 * コントロール転送パネルを構築します。
	 * <p>
	 * @return コントロール転送パネル
	 */
	private JPanel getControlTransferPanel(){
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(RS.getString("dialog.cp.edpt")));
		return panel;
	}

	// ======================================================================
	// エンドポイントパネルの取得
	// ======================================================================
	/**
	 * エンドポイントパネルを構築します。
	 * <p>
	 * @return エンドポイントパネル
	 */
	private JPanel getEndpointPanel(){
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(RS.getString("dialog.cp.edpt")));

		// エンドポイント情報の構築
		JLabel label = new JLabel("Endpoint");
		Utils.layout(panel, label, 0, 0, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, edpt,  1, 0, 1, 1, WEST, NONE, 0, 0);

		// エンドポイント機能の設定
		label = new JLabel("Feature");
		edptFeature.setText("0x0000");
		edptFeature.setMinimumSize(edptFeature.getPreferredSize());
		edptClearFeature.setText("CLEAR");
		edptSetFeature.setText("SET");
		Utils.layout(panel, label,            0, 1, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, edptFeature,      1, 1, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, edptClearFeature, 2, 1, 1, 1, WEST, HORIZONTAL, 0, 0);
		Utils.layout(panel, edptSetFeature,   3, 1, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, new JPanel(),     4, 1, 1, 1, WEST, NONE, 1, 0);

		// エンドポイントステータスの設定
		label = new JLabel("Status");
		edptStatus.setText("------");
		edptStatus.setEditable(false);
		edptStatus.setMinimumSize(edptStatus.getPreferredSize());
		edptGetStatus.setText("GET");
		Utils.layout(panel, label,          0, 2, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, edptStatus,     1, 2, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, edptGetStatus,  2, 2, 1, 1, WEST, HORIZONTAL, 0, 0);

		// エンドポイント同期データの設定
		label = new JLabel("Synch Frame");
		edptSynchFrame.setText("------");
		edptSynchFrame.setEditable(false);
		edptSynchFrame.setMinimumSize(edptSynchFrame.getPreferredSize());
		edptGetSynchFrame.setText("GET");
		Utils.layout(panel, label,              0, 3, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, edptSynchFrame,     1, 3, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, edptGetSynchFrame,  2, 3, 1, 1, WEST, HORIZONTAL, 0, 0);

		// セパレータ
		JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
		sep.setMinimumSize(new Dimension(1, sep.getPreferredSize().height));
		Utils.layout(panel, sep, 0, 4, 5, 1, CENTER, HORIZONTAL, 1, 0, 0, 0, 0, 0);

		// エンドポイント情報
		label = new JLabel("Transfer Type");
		Utils.layout(panel, label,            0, 5, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, edptTransferType, 1, 5, 4, 1, WEST, HORIZONTAL, 1, 0);

		// エンドポイント情報
		label = new JLabel("Direction");
		Utils.layout(panel, label,         0, 6, 1, 1, WEST, NONE, 0, 0);
		Utils.layout(panel, edptDirection, 1, 6, 4, 1, WEST, HORIZONTAL, 1, 0);

		// 入出力バイナリ
		buffer.setTabSize(4);
		buffer.setWrapStyleWord(false);
		Utils.layout(panel, buffer, 0, RELATIVE, REMAINDER, 1, CENTER, BOTH, 1, 1);

		// 入出力ボタン
		read.setText("Read");
		write.setText("Write");
		JPanel button = new JPanel();
		Utils.layout(button, read,  0, 0, 1, 1, CENTER, HORIZONTAL, 0, 0);
		Utils.layout(button, write, 1, 0, 1, 1, CENTER, HORIZONTAL, 0, 0);
		Utils.layout(panel, button, 0, RELATIVE, REMAINDER, 1, CENTER, HORIZONTAL, 1, 0);

		Utils.layout(panel, new JLabel(), 1, RELATIVE, REMAINDER, 1, CENTER, BOTH, 1, 1);

		// エンドポイントの選択
		edpt.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				int i = edpt.getSelectedIndex();
				if(i >= 1){
					Endpoint ed = curAlt.getEndpoints().get(i);
					setCurrentEndpoint(ed);
				}
				return;
			}
		});

		// エンドポイント機能のクリア
		edptClearFeature.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int feature = Integer.decode(edptFeature.getText()).intValue();
					curEdpt.clearFeature(feature);
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		// エンドポイント機能の設定
		edptSetFeature.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int feature = Integer.decode(edptFeature.getText()).intValue();
					curEdpt.setFeature(feature);
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		// エンドポイントステータスの参照
		edptGetStatus.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int status = curEdpt.getStatus();
					edptStatus.setText(String.format("0x%04X", status));
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		// エンドポイント同期データの参照
		edptGetSynchFrame.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{
					int frame = curEdpt.synchFrame();
					edptSynchFrame.setText(String.format("0x%04X", frame));
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		// エンドポイントの入力
		read.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				byte[] buf = new byte[1024];
				try{
					int len = curEdpt.read(buf, 0, buf.length, 3000);
					StringBuilder b = new StringBuilder();
					for(int i=0; i<len; i++){
						if(i == 0){
							/* */
						} else if((i % 16) == 0){
							b.append('\n');
						} else if((i % 8) == 0){
							b.append("  ");
						} else {
							b.append(' ');
						}
						b.append(String.format("%02X", buf[i] & 0xFF));
					}
					buffer.setText(b.toString());
				} catch(Exception ex){
					error(ex);
				}
			}
		});

		return panel;
	}

	// ======================================================================
	// 文字列記述子の参照
	// ======================================================================
	/**
	 * 指定された文字列記述子を参照します。
	 * <p>
	 * @param index 文字列記述子
	 * @return 文字列
	 */
	private String getString(int index){
		if(index != 0){
			try{
				return device.getString(index, LangID.ENGLISH);
			} catch(USBException ex){/* */}
		}
		return "-";
	}

	// ======================================================================
	// 文字列記述子の参照
	// ======================================================================
	/**
	 * 文字列記述子テーブルを参照します。
	 * <p>
	 * @return 文字列記述子のテーブル
	 */
	private JTable getStringDescriptor(){

		Object[][] sd = new Object[256][2];

		// デフォルトの言語 ID を取得
		StringBuilder langid = new StringBuilder();
		try{
			for(int i: device.getLangID()){
				if(langid.length() > 0){
					langid.append(", ");
				}
				String lang = Resource.getClassID().getLanguage(i);
				langid.append(String.format("[%04X] %s", i, lang));
			}
		} catch(USBException ex){
			langid.append("<html><font color=#800000><i>" + ex.getMessage());
		} catch(Exception ex){
			ex.printStackTrace();
			langid.append("<html><font color=#800000><i>" + ex.toString());
		}
		sd[0][0] = "LANGID";
		sd[0][1] = langid;

		// 例外が発生するまで文字列記述子を取得
		for(int i=0x01; i<=0x0F; i++){
			try{
				sd[i][1] = device.getString(i, LangID.ENGLISH);
				sd[i][0] = String.format("%02X", i);
			} catch(USBException ex){
				break;
			} catch(Exception ex){
				ex.printStackTrace();
				break;
			}
		}
		JTable stringDesc = new JTable();
		stringDesc.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		stringDesc.setModel(new DefaultTableModel(sd, new Object[]{"HEX", "String"}));
		stringDesc.getColumnModel().getColumn(0).setPreferredWidth(70);
		stringDesc.getColumnModel().getColumn(1).setPreferredWidth(340);
		return stringDesc;
	}

	// ======================================================================
	// エラーダイアログの表示
	// ======================================================================
	/**
	 * エラーダイアログを表示します。
	 * <p>
	 * @param ex 発生した例外
	 */
	private void error(Throwable ex){
		logger.log(Level.SEVERE, "", ex);
		JOptionPane.showMessageDialog(this, ex.getMessage(), RS.getString("msg.dialog.title.error"), JOptionPane.ERROR_MESSAGE);
		return;
	}

}
