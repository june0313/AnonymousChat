
/*
 * 수강편람(엑셀)로부터 강의 정보를 읽어와 파싱하는 클래스
 */

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ssg.chat.SubjectInfo;

public class ExcelHandler {
	
	/*
	 * 강의 정보를 파싱하여 리스트에 저장한다.
	 */
	public static ArrayList<SubjectInfo> ExcelParser() {
		File file = new File("./data/201302.xlsx");
		XSSFWorkbook wb = null;

		try {
			wb = new XSSFWorkbook(new FileInputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 강의 정보를 담기위한 리스트
		ArrayList<SubjectInfo> subjInfoList = new ArrayList<SubjectInfo>();
		SubjectInfo si = null;

		for (Row row : wb.getSheetAt(0)) {
			si = new SubjectInfo();
			String[] temp = new String[32];
			int count = 0;

			for (Cell cell : row) {
				temp[count] = cell.getRichStringCellValue().getString();
				count++;
			}
			si.setSubjectName(temp[4]);		// 과목명
			si.setSubjectNo(temp[2]);		// 학수번호
			si.setClassNo(temp[3]);			// 분반
			
			subjInfoList.add(si);
		}
		
		return subjInfoList;
	}
}
