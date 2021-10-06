public class Register {
    public int address;
    public DataType dataType;
    public String name;
    public String comment;

    public void setAddress(int address) {
        this.address = address;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getAddress() {
        return this.address;
    }

    public DataType getDataType() {
        return this.dataType;
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }
}