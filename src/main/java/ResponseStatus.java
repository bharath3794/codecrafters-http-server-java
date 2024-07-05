public enum ResponseStatus {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found");

    private int code;
    private String status;

    ResponseStatus(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }
}
