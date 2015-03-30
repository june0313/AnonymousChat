package ssg.chat;
import java.io.Serializable;

// 과목 정보를 저장할 클래스
public class SubjectInfo implements Serializable {
	
	private String subjectName;		// 과목명
	private String subjectNo;		// 학수번호
	private String classNo;			// 분반
	
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