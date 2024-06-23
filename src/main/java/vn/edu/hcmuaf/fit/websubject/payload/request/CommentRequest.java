package vn.edu.hcmuaf.fit.websubject.payload.request;

public class CommentRequest {
    private int rate;
    private String detail;

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
