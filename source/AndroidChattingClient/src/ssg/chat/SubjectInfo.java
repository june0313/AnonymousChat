package ssg.chat;
import java.io.Serializable;

// ���� ������ ������ Ŭ����
public class SubjectInfo implements Serializable {
	
	private String subjectName;		// �����
	private String subjectNo;		// �м���ȣ
	private String classNo;			// �й�
	
	public String getSubjectName() {
		return subjectName;
	}
	
	public void setSubjectName(String sName) {
		this.subjectName = sName;
	}

	public String getSubjectNo() {
		return subjectNo;
	}

	public void setSubjectNo(String sNo) {
		this.subjectNo = sNo;
	}

	public String getClassNo() {
		return classNo;
	}

	public void setClassNo(String cNo) {
		this.classNo = cNo;
	}
}