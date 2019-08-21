package tigerworkshop.webapphardwarebridge.responses;

public class PrintRequest {
    private String url;
    private String type;
    private String id;

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "PrintRequest{" +
                "url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
