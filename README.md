# ELMB to MQTT
## 개요
- Echonet 또는 Modbus 디바이스의 데이터(EPC 또는 Register)를 MQTT에 publish하고, 반대로 subscribe해서 디바이스를 조작하는 중계 프로그램입니다.
- Enapter사의 Electrolyser 버전 2.1을 기준으로 합니다.
- Echonet은 범용이지만 전기자동차충방전기(EV charger/discharger)를 기준으로 합니다.

## 환경
### Java
JDK1.8+

### 필수 라이브러리
- [EasyModbusJava](http://easymodbustcp.net/en/) : Modbus 클라이언트
- [Jackson](https://github.com/FasterXML/jackson) : Java JSON
- [Eclipse Paho](https://www.eclipse.org/paho/) : MQTT

## 작성 현황
- 2022/04/11
  - Echonet 프로토콜 대응 추가
- 2021/10/14
  - Modbus 기기（Enapter Electrolyser）의 Holding Register, Input Register로부터 값을 읽는 기능을 작성
