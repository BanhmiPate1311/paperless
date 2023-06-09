package vn.mobileid.paperless.entity;

public class SignPosition {
    public String PageNumber;
    public Position position;

    public SignPosition(String page, int count, int pageHeight) {
        PageNumber = page;
        position = new Position(count, pageHeight);
    }

    public String getPageNumber() {
        return PageNumber;
    }

    public void setPageNumber(String pageNumber) {
        PageNumber = pageNumber;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
