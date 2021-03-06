# Modbus to MQTT
## 概要
- ModbusのTCPプロトコルを利用しデバイスからデータを取得、MQTTへ配布（publish）する中継プログラムです。
- 現在、EnapterのElectrolyserバージョン2.1を対象としています。

## 設定ファイル
### modbus-sample.properties
- MQTTサーバーやModbus機器の情報等を設定します。
- 規定パスは`/etc/es_ver3/modbus-sample.properties`ですが、他のパスに変える場合は`Sample.java`の`CONFIG`変数を変更します。

### EnapterElectrolyser.json
- `*.json`はデバイスのレジストリ情報を格納します。
- `EnapterElectrolyser.json`は[EL21のmodbus_tcp_communication_interface](https://handbook.enapter.com/electrolyser/el21_firmware/1.8.1/modbus_tcp_communication_interface.html)から参考したデータです。

## 環境
### Java
JDK1.8+

### 必須ライブラリ
- [EashModbusJava](http://easymodbustcp.net/en/) : Modbusクライアント
- [Jackson](https://github.com/FasterXML/jackson) : Java JSON
- [Eclipse Paho](https://www.eclipse.org/paho/) : MQTT

## ソース管理（git）
`172.18.2.33:/home/lhems/gitrepo/MB2MQTT.git`

## 作成状況
- 2021/10/14
    - Modbus機器（Enapter Electrolyser）のHolding Register, Input Registerからデータをreadする機能まで作成

