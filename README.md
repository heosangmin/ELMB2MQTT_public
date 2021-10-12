# ModbusSample
## 概要
- ModbusのTCPプロトコルを利用しデバイスからデータを取得、MQTTへ配布（publish）する中継プログラムです。
- 現在、EnapterのElectrolyserバージョン2.1を対象としています。

## 設定ファイル
- `modbus-sample.properties`ではMQTTサーバーやModbus機器の情報等を設定します。
- `Sample.java`の`CONFIG`変数を変更します。
- `*.json`はデバイスのレジストリ情報を格納します。
  - `EnapterElectrolyser.json`は[EL21のmodbus_tcp_communication_interface](https://handbook.enapter.com/electrolyser/el21_firmware/1.8.1/modbus_tcp_communication_interface.html)から参考したデータです。

## 環境
### Java
JDK1.8をベースとします。