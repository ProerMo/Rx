package observable.base.bean;

public class Student {
    private String name;
    private int id;
    private int classId;

    public Student(String name, int id, int classId) {
        this.name = name;
        this.id = id;
        this.classId = classId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    @Override
    public String toString() {
        return "name:" + name + "---id:" + id + "---classId:" + classId;
    }
}
