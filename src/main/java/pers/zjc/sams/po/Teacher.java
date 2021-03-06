package pers.zjc.sams.po;

public class Teacher {
    private Integer id;

    private String major;

    private String tName;

    private String email;

    private String tel;

    private int sex;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major == null ? null : major.trim();
    }

    public String gettName() {
        return tName;
    }

    public void settName(String tName) {
        this.tName = tName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel == null ? null : tel.trim();
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + id +
                ", major='" + major + '\'' +
                ", tName='" + tName + '\'' +
                ", email='" + email + '\'' +
                ", tel='" + tel + '\'' +
                ", sex=" + sex +
                '}';
    }
}