package jp.co.nttdatabizsys.elmb2mqtt.Echonet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EPC {
    private String epc;
    private String name;
    private String mqttId;
    private String dataType;
    private Type type;
    private float multiple;
    private long minimum;
    private long maximum;
    private Format format;
    private String unit;
    private int size;

    public String getEpc() { return epc; }
    public void setEpc(String value) { this.epc = value; }

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getMqttId() {return mqttId;}
    public void setMqttId(String mqttId) {this.mqttId = mqttId;}

    public String getDataType() {return dataType;}
    public void setDataType(String dataType) {this.dataType = dataType;}

    public Type getType() {return type;}
    public void setType(Type type) {this.type = type;}

    public float getMultiple() {return multiple;}
    public void setMultiple(float multiple) {this.multiple = multiple;}

    public long getMaximum() {return maximum;}
    public void setMaximum(long maximum) {this.maximum = maximum;}

    public long getMinimum() {return minimum;}
    public void setMinimum(long minimum) {this.minimum = minimum;}

    public Format getFormat() {return format;}
    public void setFormat(Format format) {this.format = format;}

    public String getUnit() {return unit;}
    public void setUnit(String unit) {this.unit = unit;}

    public int getSize() {return size;}
    public void setSize(int size) {this.size = size;}
}