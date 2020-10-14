package spring.cloud.common.pojo;

import java.io.Serializable;

public class UserInfo implements Serializable {

    public static final long serialVersionUID = 15213856L;

    private Long id;
    private String userName;
    private String note;

    public UserInfo() {
    }

    public UserInfo(Long id, String userName, String note) {
        this.id = id;
        this.userName = userName;
        this.note = note;
    }

    /**** setters and getters ****/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
